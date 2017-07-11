/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.psc.registry;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceConfigType;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.unit.UnitConfigType;
import org.openbase.bco.dal.remote.service.PowerStateServiceRemote;
import org.openbase.bco.dal.remote.unit.ColorableLightRemote;
import rst.domotic.unit.UnitTemplateType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RegistryTemp {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RegistryTemp.class);

    private UnitRegistryRemote unitRemote;

    public RegistryTemp() {
        try {
            unitRemote = Registries.getUnitRegistry();
            unitRemote.waitForData(3000, TimeUnit.MILLISECONDS);
            List<UnitConfigType.UnitConfig> unitGroupConfigs = unitRemote.getUnitGroupConfigs();
            for(UnitConfigType.UnitConfig config : unitGroupConfigs){
                for(ServiceConfigType.ServiceConfig sc : config.getServiceConfigList()){
                    if(sc.getServiceDescription().getType() == ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE && 
                            sc.getServiceDescription().getPattern() == ServiceTemplateType.ServiceTemplate.ServicePattern.OPERATION){
                            
                        if(config.getLabel().equals("LLamp")){
                            System.out.println(config.getLabel());
                            System.out.println(sc);
//                            PowerStateServiceRemote pssr = new PowerStateServiceRemote();
//                            pssr.init(config);
//                            System.out.println(pssr.getPowerState());
                        }
                        
                    }
                }
            }
            System.exit(0);
        } catch (InterruptedException | CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("App failed", ex), LOGGER);
            System.exit(255);
        }
    }
    
    
}
