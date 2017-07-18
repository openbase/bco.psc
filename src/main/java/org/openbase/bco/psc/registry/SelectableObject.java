package org.openbase.bco.psc.registry;

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
//        Transform3D transform3D = ((AbstractUnitRemote)Units.getUnit(config, false)).getTransform3D();
//        boundingBox = new BoundingBox(transform3D, config.getPlacementConfig().getShape().getBoundingBox());
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
