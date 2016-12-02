package org.openbase.bco.psc.registry;

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

import org.openbase.bco.psc.selection.AbstractSelectable;
import org.openbase.bco.psc.selection.BoundingBox;
import org.openbase.bco.dal.remote.service.PowerStateServiceRemote;
import rct.Transform;
import rst.domotic.unit.UnitConfigType;
import rst.spatial.PlacementConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public class UnitSelectable extends AbstractSelectable{
    private PowerStateServiceRemote powerRemote;
    private BoundingBox boundingBox;
    private UnitConfigType.UnitConfig unitConfig;
    //TODO: Maybe remove unitConfig from here?! 
    
    public UnitSelectable(UnitConfigType.UnitConfig unitConfig, Transform transform, PowerStateServiceRemote powerRemote) {
        setBoundingBox(unitConfig.getPlacementConfig(), transform);
        setPowerRemote(powerRemote);
        this.unitConfig = unitConfig;
    }
    
    public synchronized void update(UnitSelectable newObject){
        this.unitConfig = newObject.getUnitConfig();
        setPowerRemote(newObject.getPowerRemote());
        boundingBox = newObject.getBoundingBox();
    }
    
    public UnitConfigType.UnitConfig getUnitConfig(){
        return unitConfig;
    }
    
    public synchronized PowerStateServiceRemote getPowerRemote(){
        return powerRemote; 
    }
    
    private synchronized void setPowerRemote(PowerStateServiceRemote powerRemote){
        this.powerRemote = powerRemote;
    }

    private synchronized void setBoundingBox(PlacementConfigType.PlacementConfig placement, Transform transform) {
        //TODO: Placement not only from config but from device class?!?! but not here...
        //TODO: calculate bounding box here.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized BoundingBox getBoundingBox() {
        return boundingBox;
    }
}
