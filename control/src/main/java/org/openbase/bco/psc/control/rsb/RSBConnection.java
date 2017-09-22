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
import org.openbase.bco.psc.lib.jp.JPPSCBaseScope;
import org.openbase.bco.psc.lib.jp.JPSelectedUnitScope;
import org.openbase.bco.psc.lib.rsb.AbstractRSBListenerConnection;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Scope;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;

/**
 * This class handles the RSB connections of the project.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RSBConnection extends AbstractRSBListenerConnection {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);

    /**
     * Constructor.
     *
     * @param handler is used to handle incoming events.
     */
    public RSBConnection(final AbstractEventHandler handler) {
        super(handler);
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
            Scope selectedUnitScope = JPService.getProperty(JPPSCBaseScope.class).getValue().concat(JPService.getProperty(JPSelectedUnitScope.class).getValue());
            LOGGER.info("Initializing RSB Selected Unit Listener on scope: " + selectedUnitScope);
            if (JPService.getProperty(JPLocalInput.class).getValue()) {
                LOGGER.warn("RSB input set to socket and localhost.");
                return RSBFactoryImpl.getInstance().createSynchronizedListener(selectedUnitScope, getLocalConfig());
            } else {
                return RSBFactoryImpl.getInstance().createSynchronizedListener(selectedUnitScope);
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
        LOGGER.debug("Registering UnitProbabilityCollection converter for Listener.");
        registerConverterForType(UnitProbabilityCollection.getDefaultInstance());
    }
}
