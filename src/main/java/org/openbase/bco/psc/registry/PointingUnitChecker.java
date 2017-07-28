package org.openbase.bco.psc.registry;

/*-
 * #%L
 * BCO Pointing Smart Control
 * %%
 * Copyright (C) 2016 - 2017 openbase.org
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

import java.util.List;
import org.openbase.bco.dal.remote.unit.AbstractUnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;
import rst.configuration.MetaConfigType;
import rst.domotic.service.ServiceConfigType;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.unit.UnitConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PointingUnitChecker {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PointingUnitChecker.class);
    
    public static boolean isApplicableUnit(UnitConfigType.UnitConfig config, List<String> registryFlags) throws InterruptedException {
        if (config != null && isRegistryFlagSet(config.getMetaConfig(), registryFlags)) {
            return hasPowerStateService(config) && hasLocationData(config);
        }
        return false;
    }
    
    public static boolean hasLocationData(UnitConfigType.UnitConfig config) throws InterruptedException{
        try {
            AbstractUnitRemote unitRemote = (AbstractUnitRemote) Units.getUnit(config, false);
            unitRemote.getGlobalBoundingBoxCenterPoint3d();
            return true;
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.WARN);
            return false;
        }
    }
    
    public static boolean hasPowerStateService(UnitConfigType.UnitConfig config) throws InterruptedException{
        for (ServiceConfigType.ServiceConfig sc : config.getServiceConfigList()) {
            ServiceTemplateType.ServiceTemplate.ServiceType type;
            try {
                type = getUnitRegistry().getServiceTemplateById(sc.getServiceDescription().getServiceTemplateId()).getType();
            } catch (CouldNotPerformException ex) {
                type = sc.getServiceDescription().getType();
            } 
            if (ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE == type
                    && ServiceTemplateType.ServiceTemplate.ServicePattern.OPERATION == sc.getServiceDescription().getPattern()) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isRegistryFlagSet(MetaConfigType.MetaConfig meta, List<String> registryFlags){
        if(meta == null || meta.getEntryList() == null) 
            return false;
        return meta.getEntryList().stream().anyMatch((entry) -> (registryFlags.contains(entry.getKey())));
    }
}
