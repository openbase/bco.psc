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

import com.google.protobuf.Message;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.type.domotic.state.PowerStateType.PowerState;
import org.openbase.type.domotic.state.BrightnessStateType.BrightnessState;
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
    private HashMap<String, Message> keywordServiceMap;
    //private HashMap<String, ?> keywordUnitMap;

    /**
     * Class to manage the mapping of keywords to services and unit types
     *
     * @param fileName file to read existing mapping (<String, Message>) from
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public KeywordManager(String fileName) throws IOException, ClassNotFoundException {
        if (fileName.length() > 0) {
            keywordServiceMap = readFile(fileName);
        } else {
            keywordServiceMap = new HashMap<>();
            //keywordUnitMap = new HashMap<>();
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
        //keywordUnitMap = new HashMap<>();
    }


    /**
     * Add a keyword with its corresponding service to the mapping
     *
     * @param keyword Keyword to be recognized such as "anschalten"
     * @param service Corresponding service such as PowerState.ON
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void addKeywordServicePair(String keyword, Message service) throws IOException, ClassNotFoundException {
        keywordServiceMap.put(keyword, service);
        saveFile();
    }

    private HashMap<String, Message> readFile(String fileName) throws IOException, ClassNotFoundException {

        File file = new File(fileName);
        ObjectInputStream input = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
        Object readObject = input.readObject();
        input.close();

        if (!(readObject instanceof HashMap)) throw new IOException("Data is not a hashmap");

        return (HashMap<String, Message>) readObject;
    }


    private void saveFile() throws IOException, ClassNotFoundException {
        File file = new File("servicekeywords.dat");
        ObjectOutputStream output = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));

        output.writeObject(keywordServiceMap);
        output.flush();
        output.close();
    }

    public static void main(String[] args) {

        // todo add keywords via cmd (see JPService)
        // todo method that transforms strings into Message objects (for cmd tool)?

        KeywordManager keywordManager = new KeywordManager();

        PowerState onState = PowerState.newBuilder().setValue(PowerState.State.ON).build();
        PowerState offState = PowerState.newBuilder().setValue(PowerState.State.OFF).build();
        BrightnessState brightState = BrightnessState.newBuilder().setBrightness(0.5).build();

        try {
            keywordManager.addKeywordServicePair("anmachen", onState);
            keywordManager.addKeywordServicePair("an", onState);

            keywordManager.addKeywordServicePair("ausmachen", offState);
            keywordManager.addKeywordServicePair("aus", offState);

            keywordManager.addKeywordServicePair("dimmen", brightState);

        } catch (IOException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);

        } catch (ClassNotFoundException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);

        }
    }

}
