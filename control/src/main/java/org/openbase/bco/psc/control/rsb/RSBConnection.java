package org.openbase.bco.psc.control.rsb;

/*
 * #%L
 * BCO PSC Control
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
import org.openbase.bco.psc.lib.jp.JPSelectedUnitScope;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
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
     * RSB Listener used to receive selected unit events.
     */
    private RSBListener selectedUnitListener;

    private WatchDog listenerWatchDog;

    /**
     * Constructor.
     *
     * @param handler is used to handle incoming events.
     */
    public RSBConnection(final AbstractEventHandler handler) {
        this.handler = handler;
    }

    /**
     * Initializes the RSB Listeners.
     *
     * @param handler is used to handle incoming events.
     * @throws InitializationException is thrown, if the initialization of the
     * Listeners fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    private void initializeListener(AbstractEventHandler handler) throws InitializationException, InterruptedException {
        LOGGER.debug("Registering converter.");
        final ProtocolBufferConverter<UnitProbabilityCollection> selectedUnitConverter = new ProtocolBufferConverter<>(
                UnitProbabilityCollection.getDefaultInstance());
        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(selectedUnitConverter);

        try {
            Scope selectedUnitScope = JPService.getProperty(JPSelectedUnitScope.class).getValue();
            LOGGER.info("Initializing RSB Selected Unit Listener on scope: " + selectedUnitScope);
            if (JPService.getProperty(JPLocalInput.class).getValue()) {
                LOGGER.warn("RSB input set to socket and localhost.");
                RSBListener l;
                selectedUnitListener = RSBFactoryImpl.getInstance().createSynchronizedListener(selectedUnitScope, getLocalConfig());
            } else {
                selectedUnitListener = RSBFactoryImpl.getInstance().createSynchronizedListener(selectedUnitScope);
            }

            // Add an EventHandler.
            selectedUnitListener.addHandler(handler, true);
            listenerWatchDog = new WatchDog(selectedUnitListener, "unitListener");

        } catch (CouldNotPerformException | JPNotAvailableException ex) {
            throw new InitializationException(RSBConnection.class, ex);
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
        listenerWatchDog.deactivate();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return listenerWatchDog.isActive();
    }
}
