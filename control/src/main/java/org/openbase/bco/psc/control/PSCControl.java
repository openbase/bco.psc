package org.openbase.bco.psc.control;

/*
 * -
 * #%L
 * BCO PSC Control
 * %%
 * Copyright (C) 2016 - 2020 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.openbase.bco.dal.remote.action.RemoteAction;
import org.openbase.bco.dal.remote.layer.unit.*;
import org.openbase.bco.dal.remote.layer.unit.connection.ConnectionRemote;
import org.openbase.bco.dal.remote.layer.unit.location.LocationRemote;
import org.openbase.bco.psc.control.jp.JPControlThreshold;
import org.openbase.bco.psc.control.jp.JPIntentTimeout;
import org.openbase.bco.psc.control.jp.JPMultimodalMode;
import org.openbase.bco.psc.control.rsb.RSBConnection;
import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.lib.registry.PointingUnitChecker;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.bco.registry.remote.login.BCOLogin;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.storage.registry.RegistrySynchronizer;
import org.openbase.jul.storage.registry.SynchronizableRegistryImpl;
import org.openbase.type.domotic.action.ActionParameterType;
import org.openbase.type.domotic.action.ActionParameterType.ActionParameter;
import org.openbase.type.domotic.service.ServiceConfigType;
import org.openbase.type.domotic.service.ServiceStateDescriptionType;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import org.openbase.type.domotic.state.MotionStateType;
import org.openbase.type.domotic.state.WindowStateType.WindowState;
import org.openbase.type.domotic.unit.UnitConfigType;
import org.openbase.type.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import org.openbase.type.domotic.unit.UnitProbabilityType.UnitProbability;
import org.openbase.type.domotic.unit.UnitTemplateType;
import org.openbase.type.domotic.unit.connection.ConnectionConfigType.ConnectionConfig.ConnectionType;
import org.openbase.type.domotic.unit.location.LocationConfigType;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import static org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType.*;

/**
 * The controller class of this application.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 * @author <a href="mailto:dreinsch@techfak.uni-bielefeld.de">Dennis Reinsch</a>
 * @author <a href="mailto:jbitschene@techfak.uni-bielefeld.de">Jennifer Bitschene</a>
 * @author <a href="mailto:jniermann@techfak.uni-bielefeld.de">Julia Niermann</a>
 */
public class PSCControl extends AbstractEventHandler implements Control, Launchable<Void>, VoidInitializable {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PSCControl.class);

    /**
     * The object handling the rsb connection.
     */
    private RSBConnection rsbConnection;

    /**
     * The synchronizer which synchronizes the unit registry with the internal controllableObjectRegistry.
     */
    private RegistrySynchronizer<String, ControllableObject, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder> controllableObjectRegistrySynchronizer;

    /**
     * Internal synchronized registry containing all controllable objects.
     */
    private SynchronizableRegistryImpl<String, ControllableObject> controllableObjectRegistry;

    /**
     * The flags used to identify controllable objects in the unit registry.
     */
    private List<String> registryFlags;

    /**
     * Probability threshold, that has to be exceeded for a control action to take place.
     */
    private double threshold;

    /**
     * Timeout for keeping and matching intent events.
     */
    private Long intentTimeout;

    /**
     * Flag for multimodal mode.
     */
    private Boolean inMultimodalMode;

    /**
     * Activation state of this class.
     */
    private boolean active = false;

    /**
     * Initialization state of this class.
     */
    private boolean initialized = false;

    /**
     * Collection of the selected UnitProbabilityCollections with the selection time.
     */
    private TreeMap<Long, UnitProbabilityCollection> selectedUnitIntents;

    /**
     * Collection of the received ActionParameters with the time of arrival.
     */
    private TreeMap<Long, ActionParameter> receivedStatesIntents;

    /**
     * List of location IDs of locations where movement is detected.
     */
    private List<String> movementLocations;

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(final Event event) throws InterruptedException {
        try {
            LOGGER.trace(event.toString());
            if (inMultimodalMode) {
                if (event.getData() instanceof UnitProbabilityCollection) {  // collect intents and handle them
                    UnitProbabilityCollection unitProbabilityCollection = (UnitProbabilityCollection) event.getData();

                    rsbConnection.publishData(unitProbabilityCollection);

                    List<UnitProbability> selectedUnits = unitProbabilityCollection.getElementList().stream()
                            .filter(x -> x.getProbability() >= threshold)
                            .collect(Collectors.toList());

                    if (selectedUnits.size() > 0) {
                        unitProbabilityCollection = UnitProbabilityCollection.newBuilder().addAllElement(selectedUnits).build();
                        selectedUnitIntents.put(event.getMetaData().getReceiveTime(), unitProbabilityCollection);
                    }
                } else if (event.getData() instanceof ActionParameter) {
                    receivedStatesIntents.put(event.getMetaData().getReceiveTime(), (ActionParameter) event.getData());
                    handleIntents();
                }
            } else { // uni modal
                LOGGER.info("in uni modal mode.");
                if (event.getData() instanceof UnitProbabilityCollection) {
                    UnitProbabilityCollection collection = (UnitProbabilityCollection) event.getData();
                    collection.getElementList().stream().filter(x -> x.getProbability() >= threshold).forEach(x -> {
                        if (controllableObjectRegistry.contains(x.getId())) {
                            try {
                                if (controllableObjectRegistry.get(x.getId()).switchPowerState()) {
                                    LOGGER.info("Switched power state of unit " + controllableObjectRegistry.get(x.getId()).getConfig().getLabel() + " with id " + x.getId());
                                } else {
                                    LOGGER.trace("Did not switch power state of unit " + controllableObjectRegistry.get(x.getId()).getConfig().getLabel() + " with id " + x.getId());
                                }
                            } catch (InterruptedException ex) {
                                // skip run because of interruption
                                Thread.currentThread().interrupt();
                            } catch (CouldNotPerformException ex) {
                                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
                            }
                        }
                    });
                }
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }

    private synchronized void handleIntents() throws CouldNotPerformException, InterruptedException {
        LOGGER.trace("Updated stack: #units: " + selectedUnitIntents.size() + " #states: " + receivedStatesIntents.size());
        removeOldIntents();
        LOGGER.trace("After remove: #units: " + selectedUnitIntents.size() + " #states: " + receivedStatesIntents.size());
        executeMatchingIntents();
    }

    private void removeOldIntents() {
        long currentTime = System.currentTimeMillis() * 1000;

        // logging
        LOGGER.trace("Remove: time " + currentTime + " timeout: " + intentTimeout);
        selectedUnitIntents.keySet().forEach(receiveTime ->
                LOGGER.trace("unit receive time " + receiveTime + " diff " + (receiveTime - currentTime)));
        receivedStatesIntents.keySet().forEach(receiveTime ->
                LOGGER.trace("state receive time " + receiveTime + " diff " + (receiveTime - currentTime)));

        selectedUnitIntents.keySet().removeIf(receiveTime -> currentTime > intentTimeout + receiveTime);
        receivedStatesIntents.keySet().removeIf(receiveTime -> currentTime > intentTimeout + receiveTime);
    }

    private void executeMatchingIntents() throws CouldNotPerformException, InterruptedException {
        try {
            LOGGER.trace("executeMatchingIntents");
            if (receivedStatesIntents.size() > 0) { // if there are action parameters from speech controller
                while (receivedStatesIntents.size() > 0) {
                    Long receivedStateTime = receivedStatesIntents.firstKey();
                    ActionParameter actionParameter = receivedStatesIntents.remove(receivedStateTime);
                    ServiceType serviceType = actionParameter.getServiceStateDescription().getServiceType();
                    UnitTemplateType.UnitTemplate.UnitType unitType = actionParameter.getServiceStateDescription().getUnitType();
                    UnitConfigType.UnitConfig unitConfig;

                    if (actionParameter.getServiceStateDescription().hasUnitId()) {
                        completeActionDescription(actionParameter);
                    } else {
                        if (selectedUnitIntents.size() > 0) { // if there are selected units from pointing component
                            while (selectedUnitIntents.size() > 0) {
                                Long selectedUnitTime = selectedUnitIntents.firstKey();
                                UnitProbabilityCollection unitProbabilityCollection = selectedUnitIntents.get(selectedUnitTime);
                                List<String> selectedUnitIds = unitProbabilityCollection.getElementList().stream()
                                        .map(UnitProbability::getId)
                                        .collect(Collectors.toList());
                                for (String unitId : selectedUnitIds) {
                                    unitConfig = getUnitRegistry().getUnitConfigById(unitId);
                                    if (unitConfig.getServiceConfigList().stream().noneMatch(isMatchingAndOperationServiceType(serviceType))) { //unit does not match the service type
                                        selectedUnitIds.remove(unitId);
                                    }
                                    if (unitType != null) {
                                        if (unitType != unitConfig.getUnitType() && unitType != unitConfig.getUnitGroupConfig().getUnitType()) {
                                            selectedUnitIds.remove(unitId);
                                        }
                                    }
                                }
                                if (selectedUnitIds.size() > 0) {
                                    ActionParameter.Builder builder = actionParameter.toBuilder();
                                    builder.getServiceStateDescriptionBuilder().setUnitId(selectedUnitIds.get(0));
                                    actionParameter = builder.build();

                                    completeActionDescription(actionParameter);
                                    break;
                                }
                            }
                        } else if (actionParameter.getServiceStateDescription().hasUnitType()){ // no input from pointing component
                            for (String locationId : movementLocations) {
                                ServiceStateDescriptionType.ServiceStateDescription.Builder serviceStateDescriptionBuilder =
                                        actionParameter.getServiceStateDescription().toBuilder().setUnitId(locationId);
                                actionParameter = actionParameter.toBuilder().setServiceStateDescription(serviceStateDescriptionBuilder).build();
                                completeActionDescription(actionParameter);
                            }
                        }
                    }
                }
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("cannot match intents.", ex);
        }
    }

    private Predicate<ServiceConfigType.ServiceConfig> isMatchingAndOperationServiceType(ServiceType serviceType) {
        return x -> x.getServiceDescription().getServiceType() == serviceType
                && x.getServiceDescription().getPattern() == ServiceTemplate.ServicePattern.OPERATION;
    }

    private void completeActionDescription(ActionParameterType.ActionParameter actionParameter)
            throws CouldNotPerformException, InterruptedException {
        LOGGER.info("completeActionDescription( "+actionParameter+")");
            ServiceType serviceType = actionParameter.getServiceStateDescription().getServiceType();
            UnitConfigType.UnitConfig unitConfig = getUnitRegistry().getUnitConfigById(actionParameter.getServiceStateDescription().getUnitId());
            if (unitConfig.getUnitType() == UnitTemplateType.UnitTemplate.UnitType.LOCATION) {
                UnitTemplateType.UnitTemplate.UnitType unitType = actionParameter.getServiceStateDescription().getUnitType();
                List<UnitConfigType.UnitConfig> unitConfigs = getUnitRegistry().getUnitConfigsByLocationIdAndUnitType(unitConfig.getId(), unitType);
                for (UnitConfigType.UnitConfig unit : unitConfigs) {
                    if (unit.getUnitType() != UnitTemplateType.UnitTemplate.UnitType.LOCATION
                    && unit.getUnitType() != UnitTemplateType.UnitTemplate.UnitType.UNIT_GROUP
                    && unit.getUnitType() != UnitTemplateType.UnitTemplate.UnitType.DIMMER) {
                        ServiceStateDescriptionType.ServiceStateDescription.Builder builder = actionParameter.getServiceStateDescription().toBuilder()
                                .setUnitId(unit.getId());
                        ActionParameter newActionParameter = actionParameter.toBuilder().setServiceStateDescription(builder.build()).build();
                        completeActionDescription(newActionParameter);
                    } else {
                        LOGGER.info("unitType was: "+unit.getUnitType());
                    }
                }
                return;
            }
            if (unitConfig.getServiceConfigList().stream().noneMatch(isMatchingAndOperationServiceType(serviceType))) {
                return;
            }
            if (serviceType == BRIGHTNESS_STATE_SERVICE || serviceType == COLOR_STATE_SERVICE) {

                ColorableLightRemote l = Units.getUnit(unitConfig,true,Units.COLORABLE_LIGHT);
                Double brightness = l.getData().getBrightnessState().getBrightness();
                String serviceState = actionParameter.getServiceStateDescription().getServiceState();
                String newServiceState = serviceState;

                if (serviceType == BRIGHTNESS_STATE_SERVICE) {
                    Double actionBrightness = getValueFromServiceState(serviceState, "brightness");
                    if (actionBrightness == 0.1) { //dunkler
                        if (brightness < 0.4) {
                            return;
                        }
                        brightness = (brightness*10 - 2)/10;
                        newServiceState = setValueInServiceState(serviceState, "brightness", brightness);
                    }else if (actionBrightness == 0.9) { //heller
                        if (brightness > 0.8) {
                            return;
                        }
                        brightness = (brightness*10+2)/10;
                        newServiceState = setValueInServiceState(serviceState, "brightness", brightness);
                    }
                } else if (serviceType == COLOR_STATE_SERVICE) {
                    newServiceState = setValueInServiceState(serviceState, "brightness", brightness);
                }
                ServiceStateDescriptionType.ServiceStateDescription serviceStateDescription = actionParameter.getServiceStateDescription().toBuilder().setServiceState(newServiceState).build();
                actionParameter = actionParameter.toBuilder().setServiceStateDescription(serviceStateDescription).build();
            }

            // WORKAROUND: security check if windows are closed when using blind service
            if (actionParameter.getServiceStateDescription().getServiceType() == BLIND_STATE_SERVICE) {
                LocationRemote living = Units.getUnitByAlias("LIVING", false, Units.LOCATION);
                List<? extends ConnectionRemote> connectionRemotes = living.getUnits(UnitTemplateType.UnitTemplate.UnitType.CONNECTION, false, Units.CONNECTION);
                for (ConnectionRemote connectionRemote : connectionRemotes) {
                    if (connectionRemote.getConfig().getConnectionConfig().getConnectionType() == ConnectionType.WINDOW) {
                        if (connectionRemote.getWindowState().getValue() != WindowState.State.CLOSED) {
                            LOGGER.warn("RemoteAction was canceled because of open window!");
                            return;
                        }
                    }
                }
            }
        try {
            RemoteAction remoteAction = new RemoteAction(actionParameter);
            if (remoteAction.getTargetUnit().isInfrastructure()) {
                LOGGER.info(remoteAction.getTargetUnit().getConfig().getAlias(0)+"is infrastructure. No remote action executed.");
                return;
            }
            remoteAction.execute().get(5, TimeUnit.SECONDS);
            LOGGER.info("RemoteAction was delivered " + remoteAction);
            return;
        } catch (CouldNotPerformException | ExecutionException | TimeoutException ex) {
            //throw new CouldNotPerformException("could not complete action.", ex);
            LOGGER.warn(ex.toString());
        }
    }
    // gets the the value from String key in (JSON) String serviceState. used to get brightness.
    private Double getValueFromServiceState(String serviceState, String key) {
        int keyIndex = serviceState.indexOf(key);
        char[] serviceStateArray = serviceState.toCharArray();
        char[] valueArray = new char[3];
        valueArray[0] = serviceStateArray[keyIndex + key.length() + 3];
        valueArray[1] = serviceStateArray[keyIndex + key.length() + 4];
        valueArray[2] = serviceStateArray[keyIndex + key.length() + 5];
        return Double.parseDouble(new String(valueArray));
    }
    // sets the the value from String key in (JSON) String serviceState to value. used to set brightness.
    private String setValueInServiceState(String serviceState, String key, Double value) {

        int keyIndex = serviceState.indexOf(key);
        char[] serviceStateArray = serviceState.toCharArray();
        char[] valueArray = Double.toString(value).toCharArray();
        serviceStateArray[keyIndex + key.length() + 3] = valueArray[0];
        serviceStateArray[keyIndex + key.length() + 4] = valueArray[1];
        serviceStateArray[keyIndex + key.length() + 5] = valueArray[2];

        return new String(serviceStateArray);
    }

    /**
     * {@inheritDoc}
     *
     * @throws InitializationException {@inheritDoc}
     * @throws InterruptedException    {@inheritDoc}
     */
    @Override
    public void init() throws InitializationException, InterruptedException {
        if (!initialized) {
            try {
                LOGGER.info("Initializing PSCControl.");

                controllableObjectRegistry = new SynchronizableRegistryImpl<>();
                registryFlags = JPService.getProperty(JPPscUnitFilterList.class).getValue();
                LOGGER.info("Selected Control Registry flags: " + registryFlags.toString());
                threshold = JPService.getProperty(JPControlThreshold.class).getValue();
                LOGGER.info("Selected Control threshold: " + threshold);
                intentTimeout = JPService.getProperty(JPIntentTimeout.class).getValue();
                LOGGER.info("Selected intent timeout: " + intentTimeout);
                inMultimodalMode = JPService.getProperty(JPMultimodalMode.class).getValue();
                LOGGER.info("Selected intent timeout: " + intentTimeout);

                Registries.waitForData();
                BCOLogin.getSession().loginUserViaUsername("admin", "admin", true);
                movementLocations = new ArrayList<>();
                final CustomUnitPool locationPool = new CustomUnitPool();

                locationPool.init(
                        unitConfig -> unitConfig.getUnitType() != UnitTemplateType.UnitTemplate.UnitType.LOCATION,
                        unitConfig -> unitConfig.getLocationConfig().getLocationType() != LocationConfigType.LocationConfig.LocationType.TILE
                );

                locationPool.activate();

                locationPool.addObserver((source, data) -> {

                    if (source.getServiceType() != ServiceType.MOTION_STATE_SERVICE) {
                        return;
                    }

                    final MotionStateType.MotionState motionState = (MotionStateType.MotionState) data;
                    final LocationRemote locationRemote = (LocationRemote) source.getServiceProvider();
                    final String locationId = locationRemote.getConfig().getId();
                    if (motionState.getValue() == MotionStateType.MotionState.State.MOTION) {
                        movementLocations.add(locationId);
                        //LOGGER.info("added "+locationRemote.getLabel());
                    } else if (motionState.getValue() == MotionStateType.MotionState.State.NO_MOTION) {
                        movementLocations.remove(locationId);
                        //LOGGER.info("removed "+locationRemote.getLabel());
                    }

                });


                initializeRegistryConnection();

                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
                initialized = true;
            } catch (JPNotAvailableException | CouldNotPerformException ex) {
                throw new InitializationException(PSCControl.class, ex);
            }
            selectedUnitIntents = new TreeMap<>();
            receivedStatesIntents = new TreeMap<>();
        }
    }

    /**
     * Initializes the synchronization of the internal controllableObjectRegistry with the unit registry.
     *
     * @throws InterruptedException     is thrown in case of an external interruption.
     * @throws CouldNotPerformException is thrown, if the registry synchronization could not be initialized.
     */
    private void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        try {
            LOGGER.info("Waiting for bco registry synchronization...");
            Registries.getUnitRegistry().waitForData();

            controllableObjectRegistrySynchronizer = new RegistrySynchronizer<>(
                    controllableObjectRegistry, getUnitRegistry().getUnitConfigRemoteRegistry(true), getUnitRegistry(), ControllableObjectFactory.getInstance());
            controllableObjectRegistrySynchronizer.addFilter(config -> {
                try {
                    return !PointingUnitChecker.isPointingControlUnit(config, registryFlags);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return true;
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.WARN);
                    return true;
                }
            });
        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not connect to the registry.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException     {@inheritDoc}
     */
    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        if (!initialized) {
            throw new CouldNotPerformException("Activate can only be called after init.");
        }
        if (!active) {
            active = true;
            LOGGER.info("Waiting for bco registry synchronization...");
            Registries.waitForData();
            LOGGER.info("Activating Registry synchronization.");
            controllableObjectRegistrySynchronizer.activate();
            rsbConnection.activate();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException     {@inheritDoc}
     */
    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Deactivating " + getClass().getName() + ".");
        if (active) {
            active = false;
            rsbConnection.deactivate();
            LOGGER.info("Deactivating Registry synchronization.");
            controllableObjectRegistrySynchronizer.deactivate();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return active;
    }

}
