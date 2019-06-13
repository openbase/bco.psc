package org.openbase.bco.psc.speech;

/*
 * -
 * #%L
 * BCO PSC Speech
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.speech.conversion.KeywordConverter;
import org.openbase.bco.psc.speech.rsb.RSBConnection;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.type.domotic.action.ActionParameterType;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.dialog.SpeechHypothesisType.SpeechHypothesis;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SpeechController extends AbstractEventHandler implements Speech, Launchable<Void>, VoidInitializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SpeechController.class);
    //private AbstractUnitSelector selector;
    private RSBConnection rsbConnection;

    //private RegistrySynchronizer<String, SelectableObject, UnitConfig, UnitConfig.Builder> selectableObjectRegistrySynchronizer;

    private List<String> registryFlags;

    private KeywordConverter keywordConverter;

    private boolean initialized = false;
    private boolean active = false;

    @Override
    public void handleEvent(final Event event) {

        LOGGER.trace(event.toString());

        if (event.getData() instanceof SpeechHypothesis) {
            SpeechHypothesis speechHypothesis = (SpeechHypothesis) event.getData();
            LOGGER.info("SpeechHypothesis detected: " + speechHypothesis);

            try {
                ArrayList<ActionParameter> actionParameters = keywordConverter.getActions(speechHypothesis);
                for (ActionParameter ap : actionParameters) {
                    rsbConnection.publishData(ap);
                    LOGGER.info("Data published: " + ap);
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        } else if (event.getData() instanceof String) {
            String[] keywords = ((String) event.getData()).split(" ");
            LOGGER.info("keywords:" + keywords);
            try {
                ArrayList<ActionParameter> actionParameters = keywordConverter.getActions(keywords);
                for (ActionParameter ap : actionParameters) {
                    rsbConnection.publishData(ap);
                    LOGGER.info("Data published: " + ap);
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        }
    }

    private void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        try {
            LOGGER.info("Waiting for bco registry synchronization...");
            Registries.getUnitRegistry().waitForData();
//
//            selectableObjectRegistrySynchronizer = new RegistrySynchronizer<String, SelectableObject, UnitConfig, UnitConfig.Builder>(
//                    selector.getSelectedObjectRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(), getUnitRegistry(), SelectableObjectFactory.getInstance());
//            selectableObjectRegistrySynchronizer.addFilter(config -> {
//                try {
//                    return !PointingUnitChecker.isPointingControlUnit(config, registryFlags);
//                } catch (InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                    ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
//                    return true;
//                } catch (CouldNotPerformException ex) {
//                    ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.WARN);
//                    return true;
//                }
//            });
        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not connect to the registry.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        if (!initialized) {
            try {
                initKeywordConverter();

                registryFlags = JPService.getProperty(JPPscUnitFilterList.class).getValue();
                initializeRegistryConnection();
                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
                initialized = true;
            } catch (JPNotAvailableException | CouldNotPerformException ex) {
                throw new InitializationException(SpeechController.class, ex);
            } catch (IOException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        LOGGER.debug("Activating " + getClass().getName() + ".");
        if (!initialized) {
            throw new CouldNotPerformException("Activate can only be called after init.");
        }
        if (!active) {
            active = true;
            Registries.waitForData();
            LOGGER.info("Activating Registry synchronization.");
            //selectableObjectRegistrySynchronizer.activate();
            rsbConnection.activate();
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.debug("Deactivating " + getClass().getName() + ".");
        if (active) {
            active = false;
            rsbConnection.deactivate();
            LOGGER.info("Deactivating Registry synchronization.");
            // selectableObjectRegistrySynchronizer.deactivate();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private void initKeywordConverter() throws IOException {
        try {
            keywordConverter = new KeywordConverter("servicekeywords.dat");
        } catch (IOException | ClassNotFoundException ex) {
            throw new IOException("Keyword Converter could not be initialized.", ex);
        }
    }

}
