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

import org.openbase.bco.psc.selection.SelectableManager;
import org.openbase.bco.psc.selection.AbstractSelectable;
import org.openbase.bco.psc.jp.JPRegistryFlags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.openbase.bco.dal.remote.service.PowerStateServiceRemote;
import org.openbase.bco.registry.location.remote.LocationRegistryRemote;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.protobuf.ProtobufListDiff;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rct.Transform;
import rct.TransformReceiver;
import rct.TransformerFactory;
import rst.configuration.EntryType;
import rst.configuration.MetaConfigType;
import rst.domotic.registry.UnitRegistryDataType;
import rst.domotic.service.ServiceConfigType;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.unit.UnitConfigType;
import rst.domotic.unit.unitgroup.UnitGroupConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RegistryObjectManager implements Observer<UnitRegistryDataType.UnitRegistryData>, SelectableManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryObjectManager.class);
    
    
    //
    //
    // TODO: Ersetzen durch ähnliches wie in Stage...
    //
    //
    
    private UnitRegistryRemote unitRemote;
    private LocationRegistryRemote locationRemote;
    private TransformReceiver transformReceiver;
    private final List<String> REGISTRY_FLAGS;
    private ProtobufListDiff<String, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder> dalUnitDiffer;
    private ProtobufListDiff<String, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder> groupDiffer;

    private final Map<String, AbstractSelectable> selectables;

    public RegistryObjectManager() throws CouldNotPerformException, InterruptedException {
        selectables = new HashMap<>();
        try {
            REGISTRY_FLAGS = JPService.getProperty(JPRegistryFlags.class).getValue();
            
            Registries.getUnitRegistry().waitForData();
            
            unitRemote = new UnitRegistryRemote();
            unitRemote.init();
            unitRemote.activate();
            
            locationRemote = new LocationRegistryRemote();
            locationRemote.init();
            locationRemote.activate();
            
            dalUnitDiffer = new ProtobufListDiff<>();
            groupDiffer = new ProtobufListDiff<>();
            
            
            TransformerFactory factory = TransformerFactory.getInstance();
            // Create a receiver object. Should be live as long as the application runs
            transformReceiver = factory.createTransformReceiver();

            // The receiver needs some time to receive the coordinate system tree
            //TODO: Workaround
            Thread.sleep(200);
            
            unitRemote.waitForData(3000, TimeUnit.MILLISECONDS);
            locationRemote.waitForData(100, TimeUnit.MILLISECONDS);
            //TODO: Workaround
            unitRemote.requestData();
            locationRemote.requestData();
            unitRemote.addDataObserver(this);
            
            updateSelectables();
        } catch (JPNotAvailableException | CouldNotPerformException | TransformerFactory.TransformerFactoryException ex) {
            throw new CouldNotPerformException("Could not initialize connection!", ex);
        }
    }
    
    public void shutdown() throws CouldNotPerformException{
        unitRemote.removeDataObserver(this);
        unitRemote.shutdown();
        locationRemote.shutdown();
        for (AbstractSelectable obj : selectables.values()) {
            ((UnitSelectable)obj).getPowerRemote().shutdown();
        }
    }
    
    private boolean isRegistryFlagSet(MetaConfigType.MetaConfig meta){
        if(meta == null || meta.getEntryList() == null) 
            return false;
        for (EntryType.Entry entry : meta.getEntryList()) {
            if (REGISTRY_FLAGS.contains(entry.getKey()))
                return true;
        }
        return false;
    }
    
    private synchronized void updateSelectables() throws CouldNotPerformException {
        try {
            dalUnitDiffer.diff(unitRemote.getDalUnitConfigs());
            updateUnits(dalUnitDiffer, false);
            groupDiffer.diff(unitRemote.getUnitGroupConfigs());
            updateUnits(groupDiffer, true);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update selectables", ex);
        } 
    }

    private void updateUnits(ProtobufListDiff<String, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder> unitDiffer, boolean isGroup) throws CouldNotPerformException {
        try{
            for(UnitConfigType.UnitConfig config : unitDiffer.getRemovedMessageMap().getMessages()){
                removeSelectableIfAvailable(config.getId());
            }
            for(UnitConfigType.UnitConfig config : unitDiffer.getUpdatedMessageMap().getMessages()){
                if(isApplicableUnit(config, isGroup)){
                    ((UnitSelectable) selectables.get(config.getId())).update(createSelectable(config, isGroup));
                } else {
                    removeSelectableIfAvailable(config.getId());
                }
            }
            for(UnitConfigType.UnitConfig config : unitDiffer.getNewMessageMap().getMessages()){
                if(isApplicableUnit(config, isGroup)){
                    selectables.put(config.getId(), createSelectable(config, isGroup));
                }
            }
        } catch (CouldNotPerformException ex) {
            if(isGroup)
                throw new CouldNotPerformException("Could not update group units", ex);
            else 
                throw new CouldNotPerformException("Could not update dal units", ex);
        }
    }
    
    private UnitSelectable createSelectable(UnitConfigType.UnitConfig config, boolean isGroup) throws CouldNotPerformException, InterruptedException{
        try{
            String rootTransform = locationRemote.getRootLocationConfig().getPlacementConfig().getTransformationFrameId();
            // Lookup the transform
            Transform toRootCoordinateTransform = transformReceiver.requestTransform(config.getPlacementConfig().getTransformationFrameId(), 
                    rootTransform,
                    System.currentTimeMillis()).get();
            //TODO: Check whether the Transform that is gotten here is right and enough (Maybe does not include placement of unit?!).

            PowerStateServiceRemote powerRemote = new PowerStateServiceRemote();
            if(isGroup){
                UnitGroupConfigType.UnitGroupConfig groupConfig = config.getUnitGroupConfig();
                List<UnitConfigType.UnitConfig> members = new ArrayList<>();
                for (String memberId : groupConfig.getMemberIdList()) {
                    members.add(unitRemote.getUnitConfigById(memberId));
                }
                powerRemote.init(members);
            } else {
                powerRemote.init(config);
            }
            powerRemote.activate();
            return new UnitSelectable(config, toRootCoordinateTransform, powerRemote);
        } catch (CouldNotPerformException | ExecutionException ex) {
            throw new CouldNotPerformException("Could not create selectable for config with id: " + config.getId(), ex);
        }
    }

    private boolean isApplicableUnit(UnitConfigType.UnitConfig config, boolean isGroup) {
        if (config != null && isRegistryFlagSet(config.getMetaConfig())) {
            if(isGroup){
                UnitGroupConfigType.UnitGroupConfig groupConfig = config.getUnitGroupConfig();
                for (ServiceTemplateType.ServiceTemplate st : groupConfig.getServiceTemplateList()) {
                    if (ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE == st.getType()
                            && ServiceTemplateType.ServiceTemplate.ServicePattern.OPERATION == st.getPattern()) {
                        return true;
                    }
                }
            } else {
                for (ServiceConfigType.ServiceConfig sc : config.getServiceConfigList()) {
                    if (ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE == sc.getServiceTemplate().getType()
                            && ServiceTemplateType.ServiceTemplate.ServicePattern.OPERATION == sc.getServiceTemplate().getPattern()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void removeSelectableIfAvailable(String id) {
        if(selectables.containsKey(id)){
            ((UnitSelectable)selectables.get(id)).getPowerRemote().shutdown();
            selectables.remove(id);
        }
    }

    @Override
    public void update(Observable<UnitRegistryDataType.UnitRegistryData> source, UnitRegistryDataType.UnitRegistryData data) throws Exception {
        updateSelectables();
    }

    @Override
    public synchronized List<AbstractSelectable> getSelectables() {
        return new ArrayList(selectables.values());
    }

    @Override
    public void processSelectedObject(AbstractSelectable selectedObject) throws CouldNotPerformException, InterruptedException {
        //TODO: Get rid of instance casting?! Template in SelectableManager and action in AbstractSelectable?!
        if(selectedObject instanceof UnitSelectable){
            try {
                UnitSelectable cObject = (UnitSelectable) selectedObject;
                cObject.getPowerRemote().waitForData();
                PowerState.State newState;
                switch(cObject.getPowerRemote().getPowerState().getValue()){
                    case OFF:
                        newState = PowerState.State.ON;
                        break;
                    default:
                        newState = PowerState.State.OFF;
                        break;
                }
                cObject.getPowerRemote().setPowerState(PowerState.newBuilder().setValue(newState).build());
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not process the selected object!", ex);
            }
        } else {
            throw new CouldNotPerformException("The object to be processed is of class " + selectedObject.getClass().toString() + 
                    " not of expected class " + UnitSelectable.class.toString() + ".");
        }
    }
}
