package org.openbase.bco.psc.lib.rsb;

/*-
 * #%L
 * BCO PSC Library
 * %%
 * Copyright (C) 2016 - 2018 openbase.org
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
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.schedule.WatchDog;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;

/**
 * This class wraps an RSB Listener and RSB Informer.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 * @param <T> The type that the Informer is publishing.
 */
public abstract class AbstractRSBDualConnection<T extends Message & MessageOrBuilder> extends AbstractRSBListenerConnection {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractRSBDualConnection.class);

    /**
     * RSB Informer used to publish events.
     */
    private RSBInformer<T> informer;

    /**
     * WatchDog to control states of the informer.
     */
    private WatchDog informerWatchDog;

    /**
     * Constructor.
     *
     * @param handler is used to handle incoming events.
     */
    public AbstractRSBDualConnection(AbstractEventHandler handler) {
        super(handler);
    }

    /**
     * Gets the initialized RSB Informer.
     *
     * @return the initialized RSB Informer.
     * @throws InitializationException is thrown, if the initialization of the
     * Informer fails.
     */
    protected abstract RSBInformer<T> getInitializedInformer() throws InitializationException;

    public void publishData(T data) throws CouldNotPerformException, InterruptedException {
        LOGGER.trace("Publishing data via RSB.");
        try {
            informer.publish(data);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("RSB informer could not send the data.", ex);
        }
    }

    public void publishEvent(Event event) throws CouldNotPerformException, InterruptedException {
        LOGGER.trace("Publishing an event via RSB.");
        try {
            informer.publish(event);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("RSB informer could not send the event.", ex);
        }
    }

    /**
     * Registers the converters required by the listener and informer.
     */
    @Override
    protected abstract void registerConverters();

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
                informer = getInitializedInformer();
                informerWatchDog = new WatchDog(informer, "informer");
                initListener();
            } catch (CouldNotPerformException ex) {
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
        super.deactivate();
        informerWatchDog.deactivate();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return super.isActive() && informerWatchDog.isActive();
    }

}
