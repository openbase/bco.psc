package org.openbase.bco.psc.speech.conversion;

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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.openbase.type.domotic.unit.UnitConfigType;
import org.openbase.type.domotic.unit.UnitTemplateType;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;

/**
 * A class that extracts intents from speech and converts them to action parameters.
 *
 * @author <a href="mailto:dreinsch@techfak.uni-bielefeld.de">Dennis Reinsch</a>
 * @author <a href="mailto:jbitschene@techfak.uni-bielefeld.de">Jennifer Bitschene</a>
 * @author <a href="mailto:jniermann@techfak.uni-bielefeld.de">Julia Niermann</a>
 */
public class KeywordConverter {
    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KeywordConverter.class);

    private HashMap<String, ActionParameter> keywordIntentMap;
    private HashMap<String, UnitTemplateType.UnitTemplate.UnitType> keywordUnitTypeMap;

    /**
     * Class that extracts intents from speech and converts them to action parameters.
     *
     * @param intentActionMap the mapping from speech (strings) to corresponding action parameters
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public KeywordConverter(HashMap<String, ActionParameter> intentActionMap, HashMap<String, UnitTemplateType.UnitTemplate.UnitType> unitTypeMap) throws IOException, ClassNotFoundException {

        keywordIntentMap = intentActionMap;
        keywordUnitTypeMap = unitTypeMap;

    }

    /***
     * Takes two Strings: an action and a value and returns the corresponding ActionParameter or null.
     * @param actionString String of the action
     * @param valueString String of the value for the action
     * @return ActionParameter or null.
     */
    public ActionParameter getActionParameter(String actionString, String valueString) {
        actionString = actionString+":"+valueString;
        if (keywordIntentMap.containsKey(actionString)) {
            LOGGER.info("Intent detected: " + actionString);
            return keywordIntentMap.get(actionString);
        } else {
            LOGGER.info("Intent (" + actionString + ") not in Map.");
            return null;
        }

    }

    /***
     * Takes a list of Strings and returns a list of UnitConfig which have a given String as alias or label.
     * @param locationStrings list of strings that could resolve a location
     * @return list of UnitConfig of locations or null
     */
    public List<UnitConfigType.UnitConfig> getLocations(List<String> locationStrings) {
        List<UnitConfigType.UnitConfig> locations = new ArrayList<>();
        List<UnitConfigType.UnitConfig> locationList = new ArrayList<>();
        for (String locationString : locationStrings) {
            try {
                locationList.addAll(getUnitRegistry().getUnitConfigsByLabel(locationString));
            } catch (NotAvailableException e) {
                //e.printStackTrace();
                LOGGER.info(e.toString());
            } catch (CouldNotPerformException e) {
                //e.printStackTrace();
                LOGGER.info(e.toString());

            }
            try {
                locationList.add(getUnitRegistry().getUnitConfigByAlias(locationString));
            } catch (NotAvailableException e) {
                //e.printStackTrace();
                LOGGER.info(e.toString());

            }
        }
        for (UnitConfigType.UnitConfig location : locationList) {
            if ((!locations.contains(location)) && location.getUnitType() == UnitTemplateType.UnitTemplate.UnitType.LOCATION) {
                LOGGER.info("Location detected: "+location.getLabel().getEntry(1).getValue(0));
                locations.add(location);
            }
        }
        return locations;

    }

    /***
     * Takes a list of Strings and returns a list of UnitConfig which have a given String as alias or label.
     * @param entityStrings list of strings that could resolve a unit
     * @return list of UnitConfig of units or null
     */
    public List<UnitConfigType.UnitConfig> getUnitConfigs(List<String> entityStrings) {
        List<UnitConfigType.UnitConfig> units = new ArrayList<>();
        List<UnitConfigType.UnitConfig> unitList = new ArrayList<>();
        for (String entityString : entityStrings) {
            try {
                unitList.addAll(getUnitRegistry().getUnitConfigsByLabel(entityString));
            } catch (NotAvailableException e) {
                //e.printStackTrace();
                LOGGER.info(e.toString());

            } catch (CouldNotPerformException e) {
                //e.printStackTrace();
                LOGGER.info(e.toString());

            }
            try {
                unitList.add(getUnitRegistry().getUnitConfigByAlias(entityString));
            } catch (NotAvailableException e) {
                //e.printStackTrace();
                LOGGER.info(e.toString());

            }
        }
        for (UnitConfigType.UnitConfig unit : unitList) {
            if ((!units.contains(unit)) && unit.getUnitType() != UnitTemplateType.UnitTemplate.UnitType.LOCATION) {
                LOGGER.info("UnitConfig detected: "+unit.getLabel().getEntry(1).getValue(0));
                units.add(unit);
            }
        }
        return units;
    }

    /***
     * Takes a list of strings and returns the corresponding UnitType or null.
     * @param entityStrings list of strings that could match an unit type
     * @return list of UnitType or null
     */
    public List<UnitTemplateType.UnitTemplate.UnitType> getUnitTypes(List<String> entityStrings) {
        List<UnitTemplateType.UnitTemplate.UnitType> unitTypes = new ArrayList<>();
        for (String entityString : entityStrings) {
            if (keywordUnitTypeMap.containsKey(entityString)) {
                LOGGER.info("UnitType detected: "+entityString);
                unitTypes.add(keywordUnitTypeMap.get(entityString));
            }
        }
        return unitTypes;
    }

}