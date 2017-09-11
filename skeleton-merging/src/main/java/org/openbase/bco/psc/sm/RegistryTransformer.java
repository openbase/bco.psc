package org.openbase.bco.psc.sm;

/*
 * #%L
 * BCO PSC Skeleton Merging
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.media.j3d.Transform3D;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Configurable;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 * This class creates a <code>Transformer</code> for a specific device registered in the bco-unit-registry. 
 * The <code>RegistryTransformer</code> is used to transform the coordinates of <code>TrackedPosture3dFloat</code>-objects 
 * and can be synchronized with the data in the registry.
 * 
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RegistryTransformer extends Transformer implements Configurable<String, UnitConfig>{
    /** Logger instance. */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RegistryTransformer.class);
    /** UnitConfig of the registry object. */
    private UnitConfig config;
    
    @Override
    public synchronized UnitConfig applyConfigUpdate(UnitConfig config) throws CouldNotPerformException, InterruptedException {
        this.config = config;
        //TODO: is a wait necessary here?!
//        Registries.waitForData();
        Transform3D transform;
        try {
            transform = Units.getUnitTransformation(config).get(1, TimeUnit.SECONDS).getTransform();
        } catch (TimeoutException | ExecutionException ex) {
            throw new CouldNotPerformException("Could not get the transformation.", ex);
        }
        transform.invert();
//        System.out.println(transform);
        setTransform(transform);
        LOGGER.debug("RegistryTransformer for id "+config.getId() + " updated.");
        return this.config;
    }

    @Override
    public synchronized String getId() throws NotAvailableException {
        if(config == null) throw new NotAvailableException("Id");
        return config.getId();
    }

    @Override
    public synchronized UnitConfig getConfig() throws NotAvailableException {
        if(config == null) throw new NotAvailableException("Config");
        return config;
    }
}
