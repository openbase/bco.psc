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
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Factory;
import rsb.Listener;
import rsb.RSBException;
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
public class RSBConnection {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);
    /**
     * RSB Listener used to receive selected unit events.
     */
    private Listener selectedUnitListener;

    /**
     * Constructor.
     *
     * @param handler is used to handle incoming events.
     * @throws CouldNotPerformException is thrown, if the initialization of the
     * class fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    public RSBConnection(AbstractEventHandler handler) throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Initializing RSB connection.");
        initializeListener(handler);
    }

    /**
     * Deactivates the RSB connection.
     *
     * @throws CouldNotPerformException is thrown, if the deactivation fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Deactivating RSB connection.");
        try {
            selectedUnitListener.deactivate();
        } catch (RSBException ex) {
            throw new CouldNotPerformException("Could not deactivate informer and listener.", ex);
        }
    }

    /**
     * Initializes the RSB Listeners.
     *
     * @param handler is used to handle incoming events.
     * @throws CouldNotPerformException is thrown, if the initialization of the
     * Listeners fails.
     * @throws InterruptedException is thrown in case of an external
     * interruption.
     */
    private void initializeListener(AbstractEventHandler handler) throws CouldNotPerformException, InterruptedException {
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
                selectedUnitListener = Factory.getInstance().createListener(selectedUnitScope, getLocalConfig());
            } else {
                selectedUnitListener = Factory.getInstance().createListener(selectedUnitScope);
            }
            selectedUnitListener.activate();

            // Add an EventHandler.
            selectedUnitListener.addHandler(handler, true);

        } catch (JPNotAvailableException | RSBException ex) {
            throw new CouldNotPerformException("RSB listener could not be initialized.", ex);
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
}
