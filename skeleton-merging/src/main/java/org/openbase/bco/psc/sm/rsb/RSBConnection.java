package org.openbase.bco.psc.sm.rsb;

/*
 * #%L
 * BCO PSC Skeleton Merging
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.openbase.bco.psc.lib.jp.JPLocalInput;
import org.openbase.bco.psc.lib.jp.JPLocalOutput;
import org.openbase.bco.psc.lib.rsb.AbstractRSBDualConnection;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rsb.Scope;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * This class handles the RSB connections of the project.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RSBConnection extends AbstractRSBDualConnection<TrackedPostures3DFloat> {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);

    /**
     * Scope used to receive events.
     */
    private final Scope baseScope;
    /**
     * Scope used to send events.
     */
    private final Scope outScope;

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
        super(handler);
        this.baseScope = baseScope;
        this.outScope = outScope;
    }

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void publishEvent(Event event) throws CouldNotPerformException, InterruptedException {
        event.setScope(outScope);
        super.publishEvent(event);
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
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws InitializationException {@inheritDoc}
     */
    @Override
    protected RSBInformer<TrackedPostures3DFloat> getInitializedInformer() throws InitializationException {
        try {
            LOGGER.info("Initializing RSB Informer on scope: " + outScope);
            if (JPService.getProperty(JPLocalOutput.class).getValue()) {
                LOGGER.warn("RSB output set to socket and localhost.");
                return RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, TrackedPostures3DFloat.class, getLocalConfig());
            } else {
                return RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, TrackedPostures3DFloat.class);
            }
        } catch (CouldNotPerformException | JPNotAvailableException ex) {
            throw new InitializationException(RSBConnection.class, ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws InitializationException {@inheritDoc}
     */
    @Override
    protected RSBListener getInitializedListener() throws InitializationException {
        try {
            LOGGER.info("Initializing RSB Listener on scope: " + baseScope);
            if (JPService.getProperty(JPLocalInput.class).getValue()) {
                LOGGER.warn("RSB input set to socket and localhost.");
                return RSBFactoryImpl.getInstance().createSynchronizedListener(baseScope, getLocalConfig());
            } else {
                return RSBFactoryImpl.getInstance().createSynchronizedListener(baseScope);
            }
        } catch (CouldNotPerformException | JPNotAvailableException ex) {
            throw new InitializationException(RSBConnection.class, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerConverters() {
        LOGGER.debug("Registering TrackedPostures3DFloat converter for Informer and Listener.");
        registerConverterForType(TrackedPostures3DFloat.getDefaultInstance());
    }
}
