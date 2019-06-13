package org.openbase.bco.psc.speech.conversion;

/*-
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.openbase.bco.dal.lib.action.ActionDescriptionProcessor;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.type.domotic.action.ActionInitiatorType;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.openbase.type.domotic.state.PowerStateType.PowerState;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;


import org.openbase.type.domotic.state.BrightnessStateType.BrightnessState;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate;


import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class KeywordManager {
    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KeywordConverter.class);
    private HashMap<String, ActionParameter> keywordServiceMap;
    //private HashMap<String, UnitProbabilityCollection> keywordUnitMap;

    /**
     * Class to manage the mapping of keywords to services and unit types
     *
     * @param fileName file to read existing mapping (<String, ActionParameter>) from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public KeywordManager(String fileName) throws IOException, ClassNotFoundException {
        if (fileName.length() > 0) {
            keywordServiceMap = readFile(fileName);
        } else {
            keywordServiceMap = new HashMap<>();
        }
    }

    /**
     * Class to manage the mapping of keywords to services and unit types
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public KeywordManager() {
        keywordServiceMap = new HashMap<>();
    }


    /**
     * Add a keyword with its corresponding service to the mapping
     *
     * @param keyword Keyword to be recognized such as "anschalten"
     * @param action Corresponding action
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void addKeywordActionPair(String keyword, ActionParameter action) throws IOException, ClassNotFoundException {
        keywordServiceMap.put(keyword, action);
        saveFile();
    }

    private HashMap<String, ActionParameter> readFile(String fileName) throws IOException, ClassNotFoundException {

        File file = new File(fileName);
        ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
        Object readObject = input.readObject();
        input.close();

        if (!(readObject instanceof HashMap)) throw new IOException("Data is not a hashmap");

        return (HashMap<String, ActionParameter>) readObject;
    }


    private void saveFile() throws IOException, ClassNotFoundException {
        File file = new File("servicekeywords.dat");
        ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

        output.writeObject(keywordServiceMap);
        output.flush();
        output.close();
    }

    public static void main(String[] args) throws CouldNotPerformException {

        // todo add keywords via cmd (see JPService)

        KeywordManager keywordManager = new KeywordManager();

        PowerState onState = PowerState.newBuilder().setValue(PowerState.State.ON).build();
        PowerState offState = PowerState.newBuilder().setValue(PowerState.State.OFF).build();
        BrightnessState brightState = BrightnessState.newBuilder().setBrightness(0.5).build();
        //ColorState colorState = ColorState.newBuilder().setColor()
        UnitTemplate light = UnitTemplate.newBuilder().setUnitType(UnitType.LIGHT).build();


        ServiceType powerServiceType = ServiceType.POWER_STATE_SERVICE;

        ActionParameter.Builder builder = ActionDescriptionProcessor.generateDefaultActionParameter(onState, powerServiceType);
        builder.getActionInitiatorBuilder().setInitiatorType(ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN);

        ActionParameter powerOn = builder.build();


        try {
            keywordManager.addKeywordActionPair("anmachen", powerOn);


        } catch (IOException | ClassNotFoundException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);

        }
    }

}
