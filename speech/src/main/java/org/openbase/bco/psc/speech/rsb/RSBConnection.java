package org.openbase.bco.psc.speech.rsb;

/*-
 * #%L
 * BCO PSC Speech
 * %%
 * Copyright (C) 2016 - 2020 openbase.org
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
import org.openbase.bco.psc.lib.jp.*;
import org.openbase.bco.psc.lib.rsb.AbstractRSBDualConnection;
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
import rst.dialog.SpeechHypothesesType.SpeechHypotheses;
import rst.dialog.SpeechHypothesisType.SpeechHypothesis;

public class RSBConnection extends AbstractRSBDualConnection<Message> {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);

    /**
     * Constructor.
     *
     * @param handler is used to handle incoming events.
     */
    public RSBConnection(AbstractEventHandler handler) {
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
                    .concat(JPService.getProperty(JPSpeechScope.class).getValue());
            LOGGER.info("Initializing RSB Listener on scope: " + inScope);
            return RSBFactoryImpl.getInstance().createSynchronizedListener(inScope, RSBSharedConnectionConfig.getParticipantConfig());
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
    protected RSBInformer<Message> getInitializedInformer() throws InitializationException {
        try {
            Scope outScope = JPService.getProperty(JPIntentScope.class).getValue()
                    .concat(JPService.getProperty(JPMergeScope.class).getValue());
            LOGGER.info("Initializing RSB Informer on scope: " + outScope);
            return RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, Message.class, RSBSharedConnectionConfig.getParticipantConfig());
        } catch (JPNotAvailableException | CouldNotPerformException ex) {
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
        LOGGER.debug("Registering SpeechHypothesis converter for Informer.");
        registerConverterForType(SpeechHypothesis.getDefaultInstance());
        LOGGER.debug("Registering SpeechHypotheses converter for Informer.");
        registerConverterForType(SpeechHypotheses.getDefaultInstance());
    }
}
