package org.openbase.bco.psc.lib.registry;

/*-
 * #%L
 * BCO PSC Library
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openbase.bco.registry.lib.util.UnitConfigProcessor;
import org.openbase.bco.registry.remote.Registries;

import static org.openbase.bco.registry.remote.Registries.getTemplateRegistry;

import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.LoggerFactory;
import org.openbase.type.configuration.MetaConfigType.MetaConfig;
import org.openbase.type.domotic.service.ServiceConfigType.ServiceConfig;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate.UnitType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PointingUnitChecker {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PointingUnitChecker.class);

    public static boolean isPointingControlUnit(UnitConfig config, List<String> registryFlags) throws InterruptedException, CouldNotPerformException {
        if (config != null && isRegistryFlagSet(config.getMetaConfig(), registryFlags)) {
            return hasPowerStateService(config) && isDalOrGroupWithLocation(config);
        }
        return false;
    }

    public static boolean isDalOrGroupWithLocation(UnitConfig config) throws InterruptedException, CouldNotPerformException {
        try {
            if (config != null && (config.getUnitType() == UnitType.UNIT_GROUP || UnitConfigProcessor.isDalUnit(config))) {
                return hasLocationDataAndBoundingBox(config);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not check if unit " + config.getLabel() + " is dal unit.", ex);
        }
        return false;
    }

    public static boolean hasLocationDataAndBoundingBox(UnitConfig config) throws InterruptedException, CouldNotPerformException {
        if (!hasLocationData(config)) {
            return false;
        }
        try {
            Registries.getUnitRegistry(true).getUnitBoundingBoxCenterGlobalPoint3d(config);
            return true;
        } catch (CouldNotPerformException ex) {
            return false;
        }
    }

    public static boolean hasLocationData(UnitConfig config) throws InterruptedException, CouldNotPerformException {
        try {
            Registries.getUnitRegistry(true).getUnitToRootTransformationFuture(config).get(10000, TimeUnit.SECONDS);
        } catch (CouldNotPerformException | TimeoutException ex) {
            throw new CouldNotPerformException("GlobalTransformReceiver not available.", ex);
        } catch (ExecutionException ex) {
            return false;
        }
        return true;
    }

    private static boolean hasPowerStateService(UnitConfig config) throws InterruptedException {
        for (ServiceConfig sc : config.getServiceConfigList()) {
            ServiceTemplate.ServiceType type;
            try {
                type = getTemplateRegistry().getServiceTemplateById(sc.getServiceDescription().getServiceTemplateId()).getServiceType();
            } catch (CouldNotPerformException ex) {
                type = sc.getServiceDescription().getServiceType();
            }
            if (ServiceTemplate.ServiceType.POWER_STATE_SERVICE == type
                    && ServiceTemplate.ServicePattern.OPERATION == sc.getServiceDescription().getPattern()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRegistryFlagSet(MetaConfig meta, List<String> registryFlags) {

        // filter non if no matches are defined.
        if(registryFlags.isEmpty()) {
            return true;
        }
        if (meta == null || meta.getEntryList() == null) {
            return false;
        }
        return meta.getEntryList().stream().anyMatch((entry) -> (registryFlags.contains(entry.getKey())));
    }
}
