package org.openbase.bco.psc.sm.rsb;

/*
 * #%L
 * BCO PSC Skeleton Merging
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
import rsb.Event;
import rsb.Factory;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.util.Properties;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

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
     * Scope used to receive events.
     */
    private final Scope baseScope;
    /**
     * Scope used to send events.
     */
    private final Scope outScope;
    /**
     * RSB Informer used to send events of type TrackedPostures3DFloat.
     */
    private RSBInformer<TrackedPostures3DFloat> mergedInformer;
    /**
     * RSB Listener used to receive events.
     */
    private RSBListener unmergedListener;

    private WatchDog listenerWatchDog;
    private WatchDog informerWatchDog;

    /**
     * Constructor.
     *
     * @param handler is used to handle incoming events.
     * @param baseScope all events received on this scope will be passed to the
     * <code>handler</code>.
     * @param outScope this scope is used to publish events.
     * @throws CouldNotPerformException is thrown, if the initialization of the
     * class fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    public RSBConnection(AbstractEventHandler handler, Scope baseScope, Scope outScope) throws CouldNotPerformException, InterruptedException {
        this.handler = handler;
        this.baseScope = baseScope;
        this.outScope = outScope;
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
    private void initializeListener(AbstractEventHandler handler) throws InitializationException, InterruptedException {
        LOGGER.debug("Registering TrackedPostures3DFloat converter for Listener.");
        final ProtocolBufferConverter<TrackedPostures3DFloat> converter = new ProtocolBufferConverter<>(
                TrackedPostures3DFloat.getDefaultInstance());

        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(converter);

        try {
            LOGGER.info("Initializing RSB Listener on scope: " + baseScope);
            if (JPService.getProperty(JPLocalInput.class).getValue()) {
                LOGGER.warn("RSB input set to socket and localhost.");
                unmergedListener = RSBFactoryImpl.getInstance().createSynchronizedListener(baseScope, getLocalConfig());
            } else {
                unmergedListener = RSBFactoryImpl.getInstance().createSynchronizedListener(baseScope);
            }

            // Add an EventHandler.
            unmergedListener.addHandler(handler, true);
            listenerWatchDog = new WatchDog(unmergedListener, "unmergedListener");

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
        LOGGER.debug("Registering TrackedPostures3DFloat converter for Informer.");
        final ProtocolBufferConverter<TrackedPostures3DFloat> converter = new ProtocolBufferConverter<>(
                TrackedPostures3DFloat.getDefaultInstance());

        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(converter);

        try {
            LOGGER.info("Initializing RSB Informer on scope: " + outScope);
            if (JPService.getProperty(JPLocalOutput.class).getValue()) {
                LOGGER.warn("RSB output set to socket and localhost.");
                mergedInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, TrackedPostures3DFloat.class, getLocalConfig());
            } else {
                mergedInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, TrackedPostures3DFloat.class);
            }
            informerWatchDog = new WatchDog(mergedInformer, "informer");
        } catch (CouldNotPerformException | JPNotAvailableException ex) {
            throw new InitializationException(RSBConnection.class, ex);
        }
    }

    /**
     * Sends the event via RSB on the <code>outScope</code>.
     *
     * @param transformedEvent event containing the transformed postures of a
     * received event.
     * @throws CouldNotPerformException is thrown, if sending the event fails.
     * @throws java.lang.InterruptedException is thrown in case of an external
     * Interruption.
     */
    public void sendTransformedEvent(Event transformedEvent) throws CouldNotPerformException, InterruptedException {
        try {
            LOGGER.trace("Sending transformed posture via RSB.");
            transformedEvent.setScope(outScope);
            mergedInformer.publish(transformedEvent);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("RSB informer could not send postures.", ex);
        }
    }

    /**
     * Returns the outScope set in the constructor.
     *
     * @return the outScope on which events are sent.
     */
    public Scope getOutScope() {
        return outScope;
    }

    /**
     * Returns the baseScope set in the constructor.
     *
     * @return the baseScope used to receive events.
     */
    public Scope getBaseScope() {
        return baseScope;
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
            System.out.println("transport: " + tc.toString());
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
