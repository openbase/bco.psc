package org.openbase.bco.psc.speech;

/*
 * -
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import com.google.gson.Gson;
import org.openbase.bco.dal.lib.action.ActionDescriptionProcessor;
import org.openbase.bco.dal.lib.state.States.Blind;
import org.openbase.bco.dal.lib.state.States.Color;
import org.openbase.bco.dal.lib.state.States.Power;
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
import org.openbase.type.domotic.action.ActionInitiatorType.ActionInitiator.InitiatorType;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.openbase.type.domotic.service.ServiceStateDescriptionType;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import org.openbase.type.domotic.state.BrightnessStateType;
import org.openbase.type.domotic.unit.UnitConfigType;
import org.openbase.type.domotic.unit.UnitTemplateType;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.dialog.SpeechHypothesesType.SpeechHypotheses;
import rst.dialog.SpeechHypothesisType.SpeechHypothesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The speech component of this application.
 *
 * @author <a href="mailto:dreinsch@techfak.uni-bielefeld.de">Dennis Reinsch</a>
 * @author <a href="mailto:jbitschene@techfak.uni-bielefeld.de">Jennifer Bitschene</a>
 * @author <a href="mailto:jniermann@techfak.uni-bielefeld.de">Julia Niermann</a>
 */
public class SpeechController extends AbstractEventHandler implements Speech, Launchable<Void>, VoidInitializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SpeechController.class);
    private RSBConnection rsbConnection;
    private boolean initialized = false;
    private boolean active = false;

    private KeywordConverter keywordConverter;


    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(final Event event) {
        LOGGER.trace(event.toString());

        SpeechHypothesis speechHypothesis = null;
        if (event.getData() instanceof SpeechHypothesis) {
            speechHypothesis = (SpeechHypothesis) event.getData();
        } else if (event.getData() instanceof SpeechHypotheses) {
            SpeechHypotheses speechHypotheses = (SpeechHypotheses) event.getData();
            speechHypothesis = speechHypotheses.getBestResult();
        }

        if (speechHypothesis == null) return;
        LOGGER.info("SpeechHypothesis detected: " + speechHypothesis);

        String grammarTreeString = speechHypothesis.getGrammarTree();

        Gson gson = new Gson();
        GrammarTree grammarTree = gson.fromJson(grammarTreeString, GrammarTree.class);

        List<String> locationStrings = grammarTree.locations;
        String actionString = grammarTree.state;
        String valueString = grammarTree.value;
        List<String> entityStrings = grammarTree.entities;

        List<UnitConfigType.UnitConfig> locationConfigs = new ArrayList<>();
        List<UnitConfigType.UnitConfig> unitConfigs = new ArrayList<>();
        List<UnitTemplateType.UnitTemplate.UnitType> unitTypes = new ArrayList<>();

        // try mapping speech hypothesis to action parameter
        ActionParameter actionParameter = keywordConverter.getActionParameter(actionString, valueString);
        if (actionParameter == null) {
            LOGGER.warn("No matching action found.");
            return;
        }

        if (locationStrings.size() > 0) {
            locationConfigs = keywordConverter.getLocations(locationStrings);
        }
        if (entityStrings.size() > 0) {
            unitConfigs = keywordConverter.getUnitConfigs(entityStrings);
            unitTypes = keywordConverter.getUnitTypes(entityStrings);
        }

        ServiceStateDescriptionType.ServiceStateDescription serviceStateDescription = actionParameter.getServiceStateDescription();

        for (UnitConfigType.UnitConfig unitConfig : unitConfigs) {
            ServiceStateDescriptionType.ServiceStateDescription newServiceStateDescription = serviceStateDescription.toBuilder().setUnitId(unitConfig.getId()).build();
            sendActionParameter(actionParameter.toBuilder().setServiceStateDescription(newServiceStateDescription).build());
        }

        for (UnitTemplateType.UnitTemplate.UnitType unitType : unitTypes) {
            ServiceStateDescriptionType.ServiceStateDescription.Builder newServiceStateDescriptionBuilder =
                    serviceStateDescription.toBuilder().setUnitType(unitType);
            if (locationConfigs.size() > 0) {
                for (UnitConfigType.UnitConfig locationConfig : locationConfigs) {
                    ServiceStateDescriptionType.ServiceStateDescription newServiceStateDescription = newServiceStateDescriptionBuilder
                            .setUnitId(locationConfig.getId()).setUnitType(unitType).build();
                    sendActionParameter(actionParameter.toBuilder().setServiceStateDescription(newServiceStateDescription).build());
                }
            } else {
                sendActionParameter(actionParameter.toBuilder().setServiceStateDescription(newServiceStateDescriptionBuilder).build());

            }
        }
        if (unitConfigs.size() == 0 && unitTypes.size() == 0) {
            sendActionParameter(actionParameter);
        }


    }

    /**
     * Takes an ActionParameter and publishes it on scope.
     *
     * @param actionParameter the ActionParameter to publish
     */
    private void sendActionParameter(ActionParameter actionParameter) {
        try {
            // send action parameter to PSCControl component
            rsbConnection.publishData(actionParameter);
            LOGGER.info("published ActionParameter: " + actionParameter);

        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
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

    private class GrammarTree {
        public String state = null;
        public String value = null;
        public List<String> locations = new ArrayList<>();
        public List<String> entities = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
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
            } catch (CouldNotPerformException | IOException ex) {
                throw new InitializationException(SpeechController.class, ex);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        LOGGER.debug("Activating " + getClass().getName() + ".");
        if (!initialized) {
            throw new CouldNotPerformException("Activate can only be called after init.");
        }
        if (!active) {
            active = true;
            LOGGER.info("Waiting for bco registry synchronization...");
            Registries.waitForData();
            LOGGER.info("Activating Registry synchronization.");
            rsbConnection.activate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.debug("Deactivating " + getClass().getName() + ".");
        if (active) {
            active = false;
            LOGGER.info("Deactivating Registry synchronization.");
            rsbConnection.deactivate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return active;
    }

    private void initKeywordConverter() throws IOException, CouldNotPerformException {

        HashMap<String, ActionParameter> intentActionMap = new HashMap<>();
        HashMap<String, UnitTemplateType.UnitTemplate.UnitType> unitTypeMap = new HashMap<>();

        try {
            // Create ActionParameter for PowerState=ON
            ActionParameter.Builder builder = ActionDescriptionProcessor.generateDefaultActionParameter(Power.ON, ServiceType.POWER_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            ActionParameter powerOn = builder.build();
            intentActionMap.put("power:an", powerOn);

            // Create ActionParameter for PowerState=OFF
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Power.OFF, ServiceType.POWER_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            ActionParameter powerOff = builder.build();
            intentActionMap.put("power:aus", powerOff);

            //Create ActionParameter for Blindstate=UP
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Blind.UP, ServiceType.BLIND_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("rollo:auf", builder.build());
            intentActionMap.put("rollladen:auf", builder.build());
            intentActionMap.put("rollo:hoch", builder.build());
            intentActionMap.put("rollladen:auf", builder.build());

            //Create ActionParameter for Blindstate=DOWN
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Blind.DOWN, ServiceType.BLIND_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("rollo:zu", builder.build());
            intentActionMap.put("rollladen:zu", builder.build());
            intentActionMap.put("rollo:runter", builder.build());
            intentActionMap.put("rolladen:runter", builder.build());

            // Create ActionParameter for Color=WEIß
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Color.WHITE, ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("color:weiß", builder.build());
            intentActionMap.put("color:weiss", builder.build());
            intentActionMap.put("color:farblos", builder.build());


            // Create ActionParameter for Color=RED
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Color.RED, ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("color:rot", builder.build());
            intentActionMap.put("color:ott", builder.build());

            // Create ActionParameter for Color=ORANGE
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Color.ORANGE, ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("color:orange", builder.build());

            // Create ActionParameter for Color=YELLOW
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Color.YELLOW, ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("color:gelb", builder.build());

            // Create ActionParameter for Color=GREEN
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Color.GREEN, ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("color:grün", builder.build());

            // Create ActionParameter for Color=BLUE
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Color.BLUE, ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("color:blau", builder.build());

            // Create ActionParameter for Color=PURPLE
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Color.PURPLE, ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("color:violett", builder.build());
            intentActionMap.put("color:violet", builder.build());
            intentActionMap.put("color:lila", builder.build());

            // Create ActionParameter for Color=PINK
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(Color.PINK, ServiceType.COLOR_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("color:pink", builder.build());
            intentActionMap.put("color:magenta", builder.build());
            intentActionMap.put("color:rosa", builder.build());


            //Create ActionParameter for BrightnessState=light
            BrightnessStateType.BrightnessState lightState = BrightnessStateType.BrightnessState.newBuilder().setBrightness(1.0).build();
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(lightState, ServiceType.BRIGHTNESS_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("brightness:hell", builder.build());

            //Create ActionParameter for BrightnessState=dark
            BrightnessStateType.BrightnessState darkState = BrightnessStateType.BrightnessState.newBuilder().setBrightness(0.2).build();
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(darkState, ServiceType.BRIGHTNESS_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("brightness:dunkel", builder.build());

            //Create ActionParameter for BrightnessState=lighter
            BrightnessStateType.BrightnessState lighterState = BrightnessStateType.BrightnessState.newBuilder().setBrightness(0.9).build();
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(lighterState, ServiceType.BRIGHTNESS_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("brightness:heller", builder.build());

            //Create ActionParameter for BrightnessState=darker
            BrightnessStateType.BrightnessState darkerState = BrightnessStateType.BrightnessState.newBuilder().setBrightness(0.1).build();
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(darkerState, ServiceType.BRIGHTNESS_STATE_SERVICE);
            builder.getActionInitiatorBuilder().setInitiatorType(InitiatorType.HUMAN);

            intentActionMap.put("brightness:dunkler", builder.build());

            unitTypeMap.put("licht", UnitType.COLORABLE_LIGHT);
            unitTypeMap.put("television", UnitType.TELEVISION);
            unitTypeMap.put("tv", UnitType.TELEVISION);
            unitTypeMap.put("lamellen", UnitType.ROLLER_SHUTTER);

            keywordConverter = new KeywordConverter(intentActionMap, unitTypeMap);

        } catch (IOException | ClassNotFoundException ex) {
            throw new IOException("Keyword Converter could not be initialized.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Keyword Converter could not be initialized.", ex);
        }
    }

}
