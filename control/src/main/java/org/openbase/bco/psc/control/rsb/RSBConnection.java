package org.openbase.bco.psc.control.rsb;

/*
 * #%L
 * BCO PSC Control
 * %%
 * Copyright (C) 2016 - 2021 openbase.org
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
import org.openbase.bco.psc.lib.jp.*;
import org.openbase.bco.psc.lib.rsb.AbstractRSBDualConnection;
import org.openbase.bco.psc.lib.rsb.AbstractRSBListenerConnection;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.com.RSBSharedConnectionConfig;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Scope;
import org.openbase.type.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;

/**
 * This class handles the RSB connections of the project.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 * @author <a href="mailto:dreinsch@techfak.uni-bielefeld.de">Dennis Reinsch</a>
 * @author <a href="mailto:jbitschene@techfak.uni-bielefeld.de">Jennifer Bitschene</a>
 * @author <a href="mailto:jniermann@techfak.uni-bielefeld.de">Julia Niermann</a>
 */
public class RSBConnection extends AbstractRSBDualConnection<UnitProbabilityCollection> {

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
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws InitializationException {@inheritDoc}
     */
    @Override
    protected RSBListener getInitializedListener() throws InitializationException {
        try {
            Scope inScope = JPService.getProperty(JPIntentScope.class).getValue()
                    .concat(JPService.getProperty(JPMergeScope.class).getValue());
            LOGGER.info("Initializing RSB Control Listener on scope: " + inScope);
            return RSBFactoryImpl.getInstance().createSynchronizedListener(inScope, RSBSharedConnectionConfig.getParticipantConfig());
        } catch (CouldNotPerformException | JPNotAvailableException ex) {
            throw new InitializationException(RSBConnection.class, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerConverters() {
        LOGGER.debug("Registering ActionParameter converter for Listener.");
        registerConverterForType(ActionParameter.getDefaultInstance());
        LOGGER.debug("Registering UnitProbabilityCollection converter for Listener.");
        registerConverterForType(UnitProbabilityCollection.getDefaultInstance());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws InitializationException {@inheritDoc}
     */
    @Override
    protected RSBInformer<UnitProbabilityCollection> getInitializedInformer() throws InitializationException {
        try {
            Scope outScope = JPService.getProperty(JPPSCBaseScope.class).getValue()
                    .concat(JPService.getProperty(JPSelectedUnitScope.class).getValue());
            LOGGER.info("Initializing RSB Informer on scope: " + outScope);
            return RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, UnitProbabilityCollection.class, RSBSharedConnectionConfig.getParticipantConfig());
        } catch (JPNotAvailableException | CouldNotPerformException ex) {
            throw new InitializationException(RSBConnection.class, ex);
        }
    }
}
