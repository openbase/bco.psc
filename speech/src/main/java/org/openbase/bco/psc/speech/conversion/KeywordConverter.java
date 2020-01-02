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

import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.slf4j.LoggerFactory;
import rst.dialog.SpeechHypothesisType.SpeechHypothesis;

import java.io.IOException;
import java.util.HashMap;


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

    /**
     * Class that extracts intents from speech and converts them to action parameters.
     *
     * @param map the mapping from speech (strings) to corresponding action parameters
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public KeywordConverter(HashMap<String, ActionParameter> map) throws IOException, ClassNotFoundException {

        keywordIntentMap = map;

        LOGGER.info("Map <String, ActionParameter> :");
        for (String key : keywordIntentMap.keySet()) {
            LOGGER.info(key + ": " + keywordIntentMap.get(key));
        }
    }

    /**
     * Get action parameter from speech hypothesis
     *
     * @param speechHypothesis the speech hypothesis
     * @return corresponding action parameter
     */
    public ActionParameter getAction(SpeechHypothesis speechHypothesis) {

        String grammarTree = speechHypothesis.getGrammarTree();

        if (keywordIntentMap.containsKey(grammarTree.trim())) {
            LOGGER.info("Intent detected: " + grammarTree);
            return keywordIntentMap.get(grammarTree.trim()); // intent[entity]
        } else {
            LOGGER.info("Intent (" + grammarTree + ") not in Map.");
            return null;
        }

    }

}
