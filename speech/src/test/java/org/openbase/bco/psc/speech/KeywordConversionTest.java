package org.openbase.bco.psc.speech;

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

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.bco.dal.lib.action.ActionDescriptionProcessor;
import org.openbase.bco.psc.speech.conversion.KeywordConverter;
import org.openbase.bco.psc.speech.conversion.KeywordManager;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.type.domotic.action.ActionInitiatorType;
import org.openbase.type.domotic.action.ActionParameterType;
import org.openbase.type.domotic.service.ServiceTemplateType;
import org.openbase.type.domotic.state.BrightnessStateType;
import org.openbase.type.domotic.state.PowerStateType;
import org.openbase.type.domotic.unit.UnitTemplateType;

import java.io.IOException;

public class KeywordConversionTest {

    private KeywordConverter keywordConverter;
    private KeywordManager keywordManager;

    public KeywordConversionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {


        try {

            keywordManager = new KeywordManager();


            PowerStateType.PowerState onState = PowerStateType.PowerState.newBuilder().setValue(PowerStateType.PowerState.State.ON).build();

            ServiceTemplateType.ServiceTemplate.ServiceType powerServiceType = ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE;

            ActionParameterType.ActionParameter.Builder builder = ActionDescriptionProcessor.generateDefaultActionParameter(onState, powerServiceType);
            builder.getActionInitiatorBuilder().setInitiatorType(ActionInitiatorType.ActionInitiator.InitiatorType.HUMAN);

            ActionParameterType.ActionParameter powerOn = builder.build();
            keywordManager.addKeywordActionPair("anmachen", powerOn);



            keywordConverter = new KeywordConverter("servicekeywords.dat");
        } catch (CouldNotPerformException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @After
    public void tearDown() {
    }

    @Test
    public void test() {

    }


}
