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

import javax.media.j3d.Transform3D;
import org.openbase.bco.dal.remote.unit.AbstractUnitRemote;
import org.openbase.bco.dal.remote.unit.Units;
import org.openbase.bco.psc.selection.AbstractSelectable;
import org.openbase.bco.psc.selection.BoundingBox;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Configurable;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SelectableObject implements Configurable<String, UnitConfig>, AbstractSelectable{
    private UnitConfig config;
    private BoundingBox boundingBox;
    public SelectableObject(){}

    @Override
    public synchronized UnitConfig applyConfigUpdate(UnitConfig config) throws CouldNotPerformException, InterruptedException {
        this.config = config;
        //TODO check this again and follow through bounding box... this may now be done in a better fashion.
        Transform3D transform3D = ((AbstractUnitRemote)Units.getUnit(config, false)).getTransform3D();
        boundingBox = new BoundingBox(transform3D, config.getPlacementConfig().getShape().getBoundingBox());
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//        return this.config;
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

    @Override
    public synchronized BoundingBox getBoundingBox() throws NotAvailableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
