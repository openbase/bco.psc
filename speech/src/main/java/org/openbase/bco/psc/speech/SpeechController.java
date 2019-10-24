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
import org.openbase.type.domotic.state.BlindStateType;
import org.openbase.type.domotic.state.ColorStateType;
import org.openbase.type.domotic.state.PowerStateType;
import org.openbase.type.vision.ColorType.Color;
import org.openbase.type.vision.HSBColorType.HSBColor;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.dialog.SpeechHypothesesType.SpeechHypotheses;
import rst.dialog.SpeechHypothesisType.SpeechHypothesis;

import java.io.IOException;
import java.util.HashMap;

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

        try {
            // try mapping speech hypothesis to action parameter
            ActionParameter actionParameter = keywordConverter.getAction(speechHypothesis);
            if (actionParameter == null) {
                LOGGER.warn("No matching action found.");
                return;
            }

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

        try {
            // Create ActionParameter for PowerState=ON
            PowerStateType.PowerState onState = PowerStateType.PowerState.newBuilder().setValue(PowerStateType.PowerState.State.ON).build();
            ServiceTemplateType.ServiceTemplate.ServiceType powerServiceType = ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE;
            ServiceTemplateType.ServiceTemplate.ServiceType blindServiceType = ServiceTemplateType.ServiceTemplate.ServiceType.BLIND_STATE_SERVICE;
            ServiceTemplateType.ServiceTemplate.ServiceType colorServiceType = ServiceTemplateType.ServiceTemplate.ServiceType.COLOR_STATE_SERVICE;
            ActionInitiatorType.ActionInitiator.InitiatorType initiator = ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN;
            ActionParameter.Builder builder = ActionDescriptionProcessor.generateDefaultActionParameter(onState, powerServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN);

            ActionParameter powerOn = builder.build();
            intentActionMap.put("anmachen", powerOn);
            intentActionMap.put("on", powerOn);
            intentActionMap.put("light[power:an]", powerOn);


            // Create ActionParameter for PowerState=OFF
            PowerStateType.PowerState offState = PowerStateType.PowerState.newBuilder().setValue(PowerStateType.PowerState.State.OFF).build();
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(offState, powerServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(initiator);

            ActionParameter powerOff = builder.build();
            intentActionMap.put("ausmachen", powerOff);
            intentActionMap.put("light[power:aus]", powerOff);

            //Create ActionParameter for Blindstate=UP
            BlindStateType.BlindState upState = BlindStateType.BlindState.newBuilder().setValue(BlindStateType.BlindState.State.UP).build();
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(upState, blindServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(initiator);

            intentActionMap.put("blind[blind:auf]", builder.build());
            intentActionMap.put("blind[blind:hoch]", builder.build());

            //Create ActionParameter for Blindstate=DOWN
            BlindStateType.BlindState downState = BlindStateType.BlindState.newBuilder().setValue(BlindStateType.BlindState.State.DOWN).build();
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(downState, blindServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(initiator);

            intentActionMap.put("blind[blind:zu]", builder.build());
            intentActionMap.put("blind[blind:runter]", builder.build());


            // Create color states
            Color blue = Color.newBuilder().setHsbColor(HSBColor.newBuilder().setHue(229).setSaturation(0.5).setBrightness(0.5)).build();
            Color red = Color.newBuilder().setHsbColor(HSBColor.newBuilder().setHue(1.0).setSaturation(1.0).setBrightness(0.5)).build();
            Color green = Color.newBuilder().setHsbColor(HSBColor.newBuilder().setHue(110).setSaturation(1.0).setBrightness(0.5)).build();
            Color pink = Color.newBuilder().setHsbColor(HSBColor.newBuilder().setHue(324).setSaturation(0.99).setBrightness(0.5)).build();
            Color lila = Color.newBuilder().setHsbColor(HSBColor.newBuilder().setHue(276).setSaturation(1.0).setBrightness(1.0)).build();

            ColorStateType.ColorState blueState = ColorStateType.ColorState.newBuilder().setColor(blue).build();
            ColorStateType.ColorState redState = ColorStateType.ColorState.newBuilder().setColor(red).build();
            ColorStateType.ColorState greenState = ColorStateType.ColorState.newBuilder().setColor(green).build();
            ColorStateType.ColorState pinkState = ColorStateType.ColorState.newBuilder().setColor(pink).build();
            ColorStateType.ColorState lilaState = ColorStateType.ColorState.newBuilder().setColor(lila).build();

            // Create ActionParameter for Color=BLUE
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(blueState, colorServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(initiator);

            intentActionMap.put("light[color:blau]", builder.build());
            intentActionMap.put("light[color:laut]", builder.build());

            // Create ActionParameter for Color=RED
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(redState, colorServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(initiator);

            intentActionMap.put("light[color:rot]", builder.build());
            intentActionMap.put("light[color:ott]", builder.build());


            // Create ActionParameter for Color=GREEN
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(greenState, colorServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(initiator);

            intentActionMap.put("light[color:grün]", builder.build());
            intentActionMap.put("light[color:grünen]", builder.build());


            // Create ActionParameter for Color=PINK
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(pinkState, colorServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(initiator);

            intentActionMap.put("light[color:pink]", builder.build());
            intentActionMap.put("light[color:peking]", builder.build());

            // Create ActionParameter for Color=LILA
            builder = ActionDescriptionProcessor.generateDefaultActionParameter(lilaState, colorServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(initiator);


            intentActionMap.put("light[color:lila]", builder.build());

            keywordConverter = new KeywordConverter(intentActionMap);

        } catch (IOException | ClassNotFoundException ex) {
            throw new IOException("Keyword Converter could not be initialized.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Keyword Converter could not be initialized.", ex);
        }
    }

}
