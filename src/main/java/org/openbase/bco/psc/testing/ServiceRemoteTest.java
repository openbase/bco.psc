package org.openbase.bco.psc.testing;

/*-
 * #%L
 * BCO Pointing Smart Control
 * %%
 * Copyright (C) 2016 openbase.org
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.openbase.bco.dal.lib.layer.service.operation.PowerStateOperationService;
import org.openbase.bco.dal.lib.layer.unit.ColorableLight;
import org.openbase.bco.dal.remote.service.ColorStateServiceRemote;
import org.openbase.bco.dal.remote.service.PowerStateServiceRemote;
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import org.openbase.bco.dal.remote.unit.UnitRemote;
import org.openbase.bco.registry.device.remote.DeviceRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote;
import org.slf4j.LoggerFactory;
import rst.configuration.EntryType;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.unit.UnitConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public class ServiceRemoteTest {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ServiceRemoteTest.class);
    
    public static void test1() throws CouldNotPerformException, InterruptedException{
        DeviceRegistryRemote deviceRegistry = new DeviceRegistryRemote();
        try{ 
            deviceRegistry.init();
            deviceRegistry.activate();
        } catch (CouldNotPerformException ex) {
            deviceRegistry.shutdown();
            throw new CouldNotPerformException("Could not initialize connection!", ex);
        }
        deviceRegistry.waitForConnectionState(Remote.ConnectionState.CONNECTED, 3000);

        List<UnitConfigType.UnitConfig> applicable_confs = new ArrayList<UnitConfigType.UnitConfig>();
        String configLabel = "TestUnit_0";
        for(UnitConfigType.UnitConfig config : deviceRegistry.getUnitConfigs()){
            if(config != null && config.getMetaConfig() != null && config.getMetaConfig().getEntryList() != null){
//                for (EntryType.Entry entry : config.getMetaConfig().getEntryList()) {
//                    if("POINTING_GESTURE".equals(entry.getKey()) && "active".equals(entry.getValue())){
//                        for (ServiceConfigType.ServiceConfig sc : config.getServiceConfigList()) {
//                            if(ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE == sc.getServiceTemplate().getType()
//                                    && ServiceTemplateType.ServiceTemplate.ServicePattern.OPERATION == sc.getServiceTemplate().getPattern()){
//                                applicable_confs.add(config);
//                                break;
//                            }
//                        }
//                    }
//                }
                if(configLabel.equals(config.getLabel())){
                    applicable_confs.add(config);
                }
            }
        }
        System.out.println(applicable_confs);

        PowerStateServiceRemote powerRemote = new PowerStateServiceRemote();
        if(!applicable_confs.isEmpty()){
            UnitConfigType.UnitConfig conf = applicable_confs.get(0);
            powerRemote.init(conf);
            powerRemote.activate();
            for(PowerStateOperationService os : powerRemote.getPowerStateOperationServices()){
                os.setPowerState(PowerState.newBuilder().setValue(PowerState.State.ON).build());
            }
            powerRemote.deactivate();
        }


        powerRemote = new PowerStateServiceRemote();
        if(!applicable_confs.isEmpty()){
            UnitConfigType.UnitConfig conf = applicable_confs.get(0);
            powerRemote.init(conf);
            powerRemote.activate();
            for(UnitRemote unitRemote : powerRemote.getInternalUnits()) {
                unitRemote.waitForData();
            }
            for (PowerStateOperationService os : powerRemote.getPowerStateOperationServices()) {
                System.out.println(os.getPowerState().toString() + "test");
            }
            powerRemote.deactivate();
        }
            
    }
    
    public static void test2() throws InitializationException, InterruptedException, CouldNotPerformException{
//        ColorStateServiceRemote csr = new ColorStateServiceRemote();
        ColorableLightRemote colorableLightRemote = new ColorableLightRemote();
        colorableLightRemote.initByLabel("502");
//        remote.initByLabel("503");
        colorableLightRemote.activate();
            
        colorableLightRemote.waitForData();
        colorableLightRemote.setColor(Color.YELLOW);
//        colorableLightRemote.setPowerState(PowerStateType.PowerState.newBuilder().setValue(State.ON).build());
//
//        colorableLightRemote.setBrightnessState(BrightnessState.newBuilder().setBrightness(100.0).build());
//        colorableLightRemote.addDataObserver(new Observer<ColorableLightDataType.ColorableLightData>() {
//
//            @Override
//            public void update(Observable<ColorableLightDataType.ColorableLightData> source, ColorableLightDataType.ColorableLightData data) throws Exception {
//                LOGGER.info("data update: \n" + data);
//            }
//        });
        LOGGER.info(colorableLightRemote.getPowerState().toString());
        colorableLightRemote.deactivate();
        colorableLightRemote.shutdown();
        LOGGER.info("after shutdown");
    }
    
}
