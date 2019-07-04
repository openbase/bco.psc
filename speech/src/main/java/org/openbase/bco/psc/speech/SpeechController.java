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

import org.openbase.bco.dal.lib.action.ActionDescriptionProcessor;
import org.openbase.bco.psc.speech.conversion.KeywordConverter;
import org.openbase.bco.psc.speech.rsb.RSBConnection;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.type.domotic.action.ActionInitiatorType;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.openbase.type.domotic.service.ServiceTemplateType;
import org.openbase.type.domotic.state.ColorStateType;
import org.openbase.type.domotic.state.PowerStateType;
import org.openbase.type.vision.ColorType.Color;
import org.openbase.type.vision.HSBColorType.HSBColor;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.dialog.SpeechHypothesisType.SpeechHypothesis;

import java.io.IOException;
import java.util.HashMap;


public class SpeechController extends AbstractEventHandler implements Speech, Launchable<Void>, VoidInitializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SpeechController.class);
    private RSBConnection rsbConnection;
    private boolean initialized = false;
    private boolean active = false;

    private KeywordConverter keywordConverter;


    @Override
    public void handleEvent(final Event event) {
        LOGGER.trace(event.toString());

        if (event.getData() instanceof SpeechHypothesis) {
            SpeechHypothesis speechHypothesis = (SpeechHypothesis) event.getData();
            LOGGER.info("SpeechHypothesis detected: " + speechHypothesis);

            try {
                //ArrayList<ActionParameter> actionParameters = keywordConverter.getActions(intents);
                ActionParameter actionParameter = keywordConverter.getAction(speechHypothesis);
                if (actionParameter == null) {
                    LOGGER.warn("No matching action found.");
                }

                rsbConnection.publishData(actionParameter);
                LOGGER.info("PUBLISHED action: " + actionParameter);

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

        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not connect to the registry.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        LOGGER.info("Initializing speech controller...");
        if (!initialized) {
            try {
                initKeywordConverter();

                initializeRegistryConnection();
                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
                initialized = true;
            } catch (CouldNotPerformException ex) {
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
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private void initKeywordConverter() throws IOException, CouldNotPerformException {

        HashMap<String, ActionParameter> intentActionMap = new HashMap<>();

        try {
            //BrightnessStateType.BrightnessState brightState = BrightnessStateType.BrightnessState.newBuilder().setBrightness(-0.5).build();

            // Create ActionParameter for PowerState=ON
            PowerStateType.PowerState onState = PowerStateType.PowerState.newBuilder().setValue(PowerStateType.PowerState.State.ON).build();
            ServiceTemplateType.ServiceTemplate.ServiceType powerServiceType = ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE;
            ActionParameter.Builder builder = ActionDescriptionProcessor.generateDefaultActionParameter(onState, powerServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN);

            ActionParameter powerOn = builder.build();
            intentActionMap.put("anmachen", powerOn);
            intentActionMap.put("on", powerOn);
            intentActionMap.put("powerstate[an]", powerOn);


            // Create ActionParameter for PowerState=OFF
            PowerStateType.PowerState offState = PowerStateType.PowerState.newBuilder().setValue(PowerStateType.PowerState.State.OFF).build();
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(offState, powerServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN);

            ActionParameter powerOff = builder.build();
            intentActionMap.put("ausmachen", powerOff);
            intentActionMap.put("powerstate[aus]", powerOff);

            // Create color states
            Color blue = Color.newBuilder().setHsbColor(HSBColor.newBuilder().setHue(229).setSaturation(52).setBrightness(43)).build();
            Color red = Color.newBuilder().setHsbColor(HSBColor.newBuilder().setHue(1).setSaturation(100).setBrightness(51)).build();
            Color green = Color.newBuilder().setHsbColor(HSBColor.newBuilder().setHue(110).setSaturation(100).setBrightness(51)).build();

            ColorStateType.ColorState blueState = ColorStateType.ColorState.newBuilder().setColor(blue).build();
            ColorStateType.ColorState redState = ColorStateType.ColorState.newBuilder().setColor(red).build();
            ColorStateType.ColorState greenState = ColorStateType.ColorState.newBuilder().setColor(green).build();

            // Create ActionParameter for Color=BLUE
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(blueState, ServiceTemplateType.ServiceTemplate.ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN);

            intentActionMap.put("coloring[blau]", builder.build());

            // Create ActionParameter for Color=RED
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(redState, ServiceTemplateType.ServiceTemplate.ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN);

            intentActionMap.put("coloring[rot]", builder.build());

            // Create ActionParameter for Color=GREEN
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(greenState, ServiceTemplateType.ServiceTemplate.ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN);

            intentActionMap.put("coloring[grün]", builder.build());

            keywordConverter = new KeywordConverter(intentActionMap);

        } catch (IOException | ClassNotFoundException ex) {
            throw new IOException("Keyword Converter could not be initialized.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Keyword Converter could not be initialized.", ex);
        }
    }

}
