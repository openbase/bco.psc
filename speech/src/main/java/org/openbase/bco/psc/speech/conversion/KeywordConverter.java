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

import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.slf4j.LoggerFactory;
import rst.dialog.SpeechHypothesisType.SpeechHypothesis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class KeywordConverter {
    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KeywordConverter.class);

    private HashMap<String, ActionParameter> keywordServiceMap;


    public KeywordConverter(HashMap<String, ActionParameter> map) throws IOException, ClassNotFoundException {

        keywordServiceMap = map;

        LOGGER.info("Map <String, ActionParameter> :");
        for (String key : keywordServiceMap.keySet()) {
            LOGGER.info(key + ": " + keywordServiceMap.get(key));
        }
    }

    public ActionParameter getAction(SpeechHypothesis speechHypothesis) {

        String[] intentEntity = speechHypothesis.getGrammarTree().split("\\[");
        String intent = intentEntity[0];
        String entity;
        if (intentEntity.length > 1) entity = intentEntity[1];
        ActionParameter event;

        if (keywordServiceMap.containsKey(intent)) {
            event = keywordServiceMap.get(intent);
            LOGGER.info("Intent detected: " + intent + " corresponding event: " + event);
            return event;
        } else {
            LOGGER.info("Intent (" + intent + ") not in Map.");
            return null;
        }


    }

    public ArrayList<ActionParameter> getActions(List<String> keywords) {
        LOGGER.info("Converting keywords -> actions");

        ArrayList<ActionParameter> actionParameters = new ArrayList<>();
        for (String kw : keywords) {
            if (keywordServiceMap.containsKey(kw)) {
                ActionParameter event = keywordServiceMap.get(kw);
                actionParameters.add(event);
                LOGGER.info("Keyword detected: " + kw + " corresponding event: " + event);
            } else {
                LOGGER.info("Keyword (" + kw + ") not in Map.");
            }
        }
        return actionParameters;
    }



}
