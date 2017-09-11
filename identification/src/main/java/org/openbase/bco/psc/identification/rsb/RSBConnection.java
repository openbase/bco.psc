package org.openbase.bco.psc.identification.rsb;

/*-
 * #%L
 * BCO PSC Identification
 * %%
 * Copyright (C) 2016 - 2017 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.openbase.bco.psc.lib.jp.JPLocalInput;
import org.openbase.bco.psc.lib.jp.JPLocalOutput;
import org.openbase.bco.psc.lib.jp.JPRayScope;
import org.openbase.bco.psc.lib.jp.JPSelectedUnitScope;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.schedule.WatchDog;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Factory;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.util.Properties;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;

/**
 * This class handles the RSB connections of the project.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RSBConnection implements Launchable<Void>, VoidInitializable {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);
    private final AbstractEventHandler handler;
    /**
     * RSB Informer used to send events of type UnitProbabilityCollection.
     */
    private RSBInformer<UnitProbabilityCollection> unitInformer;
    /**
     * RSB Listener used to receive events.
     */
    private RSBListener rayListener;

    private WatchDog listenerWatchDog;
    private WatchDog informerWatchDog;

    /**
     * Constructor.
     *
     * @param handler is used to handle incoming events.
     */
    public RSBConnection(AbstractEventHandler handler) {
        this.handler = handler;
    }

    /**
     * Initializes the RSB Listener.
     *
     * @param handler is used to handle incoming events.
     * @throws CouldNotPerformException is thrown, if the initialization of the
     * Listener fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    private void initializeListener(AbstractEventHandler handler) throws InterruptedException, InitializationException {
        final ProtocolBufferConverter<PointingRay3DFloatDistributionCollection> converter = new ProtocolBufferConverter<>(
                PointingRay3DFloatDistributionCollection.getDefaultInstance());

        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(converter);

        try {
            Scope inScope = JPService.getProperty(JPRayScope.class).getValue();
            LOGGER.info("Initializing RSB Listener on scope: " + inScope);
            if (JPService.getProperty(JPLocalInput.class).getValue()) {
                rayListener = RSBFactoryImpl.getInstance().createSynchronizedListener(inScope, getLocalConfig());
            } else {
                rayListener = RSBFactoryImpl.getInstance().createSynchronizedListener(inScope);
            }

            // Add an EventHandler.
            rayListener.addHandler(handler, true);
            listenerWatchDog = new WatchDog(rayListener, "rayListener");

        } catch (CouldNotPerformException | JPNotAvailableException ex) {
            throw new InitializationException(RSBConnection.class, ex);
        }
    }

    /**
     * Initializes the RSB Informer.
     *
     * @throws CouldNotPerformException is thrown, if the initialization of the
     * Informer fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    private void initializeInformer() throws InitializationException {
        LOGGER.debug("Registering PointingRay3DFloatCollection converter for Informer.");
        final ProtocolBufferConverter<UnitProbabilityCollection> converter = new ProtocolBufferConverter<>(
                UnitProbabilityCollection.getDefaultInstance());

        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(converter);

        try {
            Scope outScope = JPService.getProperty(JPSelectedUnitScope.class).getValue();
            LOGGER.info("Initializing RSB Informer on scope: " + outScope);
            if (JPService.getProperty(JPLocalOutput.class).getValue()) {
                LOGGER.warn("RSB output set to socket and localhost.");
                unitInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, UnitProbabilityCollection.class, getLocalConfig());
            } else {
                unitInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, UnitProbabilityCollection.class);
            }
            informerWatchDog = new WatchDog(unitInformer, "unitInformer");
        } catch (JPNotAvailableException | CouldNotPerformException ex) {
            throw new InitializationException(RSBConnection.class, ex);
        }
    }

    /**
     * Sends the event via RSB on the <code>outScope</code>.
     *
     * @param selection Units and probabilities selected by pointing gestures.
     * @throws CouldNotPerformException is thrown, if sending the event fails.
     * @throws java.lang.InterruptedException is thrown in case of an external
     * Interruption.
     */
    public void sendUnitProbabilities(UnitProbabilityCollection selection) throws CouldNotPerformException, InterruptedException {
        try {
            LOGGER.trace("Sending selected units via RSB.");
            unitInformer.publish(selection);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("RSB informer could not send rays.", ex);
        }
    }

    /**
     * Creates an RSB configuration for connecting via socket and localhost.
     *
     * @return the local communication configuration.
     */
    private ParticipantConfig getLocalConfig() {
        ParticipantConfig localConfig = Factory.getInstance().getDefaultParticipantConfig().copy();
        Properties localProperties = new Properties();
        localProperties.setProperty("transport.socket.host", "localhost");
        localConfig.getTransports().values().forEach((tc) -> {
            tc.setEnabled(false);
        });
        localConfig.getOrCreateTransport("socket").setEnabled(true);
        localConfig.getOrCreateTransport("socket").setOptions(localProperties);
        return localConfig;
    }

    /**
     * {@inheritDoc}
     *
     * @throws InitializationException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void init() throws InitializationException, InterruptedException {
        LOGGER.info("Initializing RSB connection.");
        initializeInformer();
        initializeListener(handler);
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Activating RSB connection.");
        informerWatchDog.activate();
        listenerWatchDog.activate();
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Deactivating RSB connection.");
        informerWatchDog.deactivate();
        listenerWatchDog.deactivate();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return informerWatchDog.isActive() && listenerWatchDog.isActive();
    }
}
