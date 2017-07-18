package org.openbase.bco.psc.registry;

import java.util.List;
import org.openbase.bco.dal.remote.unit.AbstractUnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;
import rst.configuration.EntryType;
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
        for (EntryType.Entry entry : meta.getEntryList()) {
            if (registryFlags.contains(entry.getKey()))
                return true;
        }
        return false;
    }
}
