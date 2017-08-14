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
import org.openbase.bco.psc.lib.jp.JPRayScope;
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
import rst.tracking.PointingRay3DFloatCollectionType;

/**
 * This class handles the RSB connections of the project.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RSBConnection {
    /** Logger instance. */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBConnection.class);
    /** RSB Listener used to receive events. */
    private Listener listener;
    
    /**
     * Constructor.
     * 
     * @param handler is used to handle incoming events.
     * @throws CouldNotPerformException is thrown, if the initialization of the class fails.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    public RSBConnection(AbstractEventHandler handler) throws CouldNotPerformException, InterruptedException {
        initializeListener(handler);
    }
    
    /**
     * Deactivates the RSB connection.
     * 
     * @throws CouldNotPerformException is thrown, if the deactivation fails.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    public void deactivate() throws CouldNotPerformException, InterruptedException{
        try{
            listener.deactivate();
        } catch (RSBException ex) {
            throw new CouldNotPerformException("Could not deactivate listener.", ex);
        } 
    }
    
    /**
     * Initializes the RSB Listener.
     * 
     * @param handler is used to handle incoming events.
     * @throws CouldNotPerformException is thrown, if the initialization of the Listener fails.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    private void initializeListener(AbstractEventHandler handler) throws CouldNotPerformException, InterruptedException{
        final ProtocolBufferConverter<PointingRay3DFloatCollectionType.PointingRay3DFloatCollection> converter = new ProtocolBufferConverter<>(
                    PointingRay3DFloatCollectionType.PointingRay3DFloatCollection.getDefaultInstance());

        DefaultConverterRepository.getDefaultConverterRepository()
            .addConverter(converter);
        
        try {
            Scope inScope = JPService.getProperty(JPRayScope.class).getValue();
            LOGGER.info("Initializing RSB Listener on scope: " + inScope);
            if(JPService.getProperty(JPLocalInput.class).getValue()){
                listener = Factory.getInstance().createListener(inScope, getLocalConfig());
            } else {
                listener = Factory.getInstance().createListener(inScope);
            }
            listener.activate();
            
            // Add an EventHandler.
            listener.addHandler(handler, true);
            
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
