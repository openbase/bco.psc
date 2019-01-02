package org.openbase.bco.psc.lib.rsb;

/*-
 * #%L
 * BCO PSC Library
 * %%
 * Copyright (C) 2016 - 2019 openbase.org
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

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.schedule.WatchDog;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;

/**
 * This class wraps a single rsb listener.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class AbstractRSBListenerConnection implements Launchable<Void>, VoidInitializable, LocalConfigProviderInterface {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractRSBListenerConnection.class);
    /**
     * The handler used to handle incoming events.
     */
    private final AbstractEventHandler handler;

    /**
     * RSB Listener used to receive events.
     */
    private RSBListener listener;

    /**
     * WatchDog to control states of the listener.
     */
    protected WatchDog listenerWatchDog;

    /**
     * Initialized state of this class.
     */
    protected boolean initialized = false;

    /**
     * Constructor.
     *
     * @param handler is used to handle incoming events.
     */
    public AbstractRSBListenerConnection(AbstractEventHandler handler) {
        this.handler = handler;
    }

    /**
     * Gets the initialized RSB Listener.
     *
     * @return the initialized RSB Listener.
     * @throws InitializationException is thrown, if the initialization of the
     * Listener fails.
     */
    protected abstract RSBListener getInitializedListener() throws InitializationException;

    /**
     * Registers the converters needed by the listener.
     */
    protected abstract void registerConverters();

    /**
     * Registers the converter of the given type for the use of informers and
     * listeners.
     *
     * @param <M> The type that is being registered.
     * @param defaultInstance The default instance of the type to be registered.
     */
    protected <M extends Message & MessageOrBuilder> void registerConverterForType(M defaultInstance) {
        DefaultConverterRepository.getDefaultConverterRepository()
                .addConverter(new ProtocolBufferConverter<>(defaultInstance));
    }

    /**
     * Initializes the RSB Listener
     *
     * @throws org.openbase.jul.exception.InitializationException is thrown if
     * the initialization fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    protected void initListener() throws InitializationException, InterruptedException {
        try {
            listener = getInitializedListener();
            listener.addHandler(handler, true);
            listenerWatchDog = new WatchDog(listener, "listener");
        } catch (CouldNotPerformException ex) {
            throw new InitializationException("RSB Listener", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InitializationException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void init() throws InitializationException, InterruptedException {
        if (!initialized) {
            try {
                LOGGER.info("Initializing RSB connection.");
                registerConverters();
                initListener();
            } catch (InitializationException ex) {
                throw new InitializationException(AbstractRSBListenerConnection.class, ex);
            }
            initialized = true;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        if (!initialized) {
            throw new CouldNotPerformException("Do not call activate before init!");
        }
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
