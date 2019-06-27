package org.openbase.bco.psc.control;

/*
 * -
 * #%L
 * BCO PSC Control
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.openbase.bco.dal.remote.action.RemoteAction;
import org.openbase.bco.psc.control.jp.JPControlThreshold;
import org.openbase.bco.psc.control.jp.JPIntentTimeout;
import org.openbase.bco.psc.control.rsb.RSBConnection;
import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.lib.registry.PointingUnitChecker;
import org.openbase.bco.registry.remote.Registries;
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
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate;
import org.openbase.type.domotic.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import org.openbase.type.domotic.unit.UnitConfigType;
import org.openbase.type.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import org.openbase.type.domotic.unit.UnitProbabilityType.UnitProbability;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;

/**
 * The controller class of this application.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ControlController extends AbstractEventHandler implements Control, Launchable<Void>, VoidInitializable {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ControlController.class);

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
     * Activation state of this class.
     */
    private boolean active = false;
    /**
     * Initialization state of this class.
     */
    private boolean initialized = false;

    private TreeMap<Long, UnitProbabilityCollection> selectedUnitIntents;

    private TreeMap<Long, ActionParameter> receivedStatesIntents;


    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void handleEvent(final Event event) throws InterruptedException {
        LOGGER.trace(event.toString());
        LOGGER.info("enter handleEvent");

        if (event.getData() instanceof UnitProbabilityCollection) {  // this could also be done with event.scope
            selectedUnitIntents.put(event.getMetaData().getReceiveTime(), (UnitProbabilityCollection) event.getData());
        } else if (event.getData() instanceof ActionParameter) {
            receivedStatesIntents.put(event.getMetaData().getReceiveTime(), (ActionParameter) event.getData());
        }

        try {
            handleIntents();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }

    private synchronized void handleIntents() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Updated stack: #units: " + selectedUnitIntents.size() + " #states: " + receivedStatesIntents.size());
        removeOldIntents();
        LOGGER.info("After remove: #units: " + selectedUnitIntents.size() + " #states: " + receivedStatesIntents.size());

        executeMatchingIntents();
    }

    private void removeOldIntents() {
        long currentTime = System.currentTimeMillis() * 1000;

        LOGGER.info("Remove: time " + currentTime + " timeout: " + intentTimeout);
        selectedUnitIntents.keySet().forEach(receiveTime ->
                LOGGER.info("unit receive time " + receiveTime + " diff " + (receiveTime - currentTime)));
        receivedStatesIntents.keySet().forEach(receiveTime ->
                LOGGER.info("state receive time " + receiveTime + " diff " + (receiveTime - currentTime)));

        selectedUnitIntents.keySet().removeIf(receiveTime -> currentTime > intentTimeout + receiveTime);
        receivedStatesIntents.keySet().removeIf(receiveTime -> currentTime > intentTimeout + receiveTime);
    }

    private void executeMatchingIntents() throws CouldNotPerformException, InterruptedException {
        try {
            Map<Long, UnitProbabilityCollection> unmatchedSelectedUnitIntents = new TreeMap<>();
            Map<Long, ActionParameter> unmatchedReceivedStatesIntents = new TreeMap<>();

            LOGGER.info("executeMatchingIntents");
            while (selectedUnitIntents.size() > 0) {
                Long selectedUnitKey = selectedUnitIntents.firstKey();
                UnitProbabilityCollection unitProbabilityCollection = selectedUnitIntents.remove(selectedUnitKey);
                List<String> selectedUnitIds = unitProbabilityCollection.getElementList().stream()
                        .filter(x -> x.getProbability() >= threshold)
                        .map(UnitProbability::getId)
                        .collect(Collectors.toList());
                for (String unitId : selectedUnitIds) {
                    LOGGER.info("unitId " + unitId + " used for matching");
                    UnitConfigType.UnitConfig unitConfig = getUnitRegistry().getUnitConfigById(unitId);

                    while (receivedStatesIntents.size() > 0) {
                        Long receivedUnitKey = receivedStatesIntents.firstKey();
                        ActionParameter actionParameter = receivedStatesIntents.remove(receivedUnitKey);
                        ServiceType serviceType = actionParameter.getServiceStateDescription().getServiceType();

                        LOGGER.info(" >   matching with " + serviceType);

                        if (unitConfig.getServiceConfigList().stream().anyMatch(isMatchingAndOperationServiceType(serviceType))) {
                            LOGGER.info(" >>>>>>>>>>> MATCH <<<<<<<<<<<<<<<");
                            completeActionDescription(actionParameter, unitId);
                            break;
                        } else {
                            // store unmatched intents for later
                            unmatchedSelectedUnitIntents.put(selectedUnitKey, unitProbabilityCollection);
                            unmatchedReceivedStatesIntents.put(receivedUnitKey, actionParameter);
                        }

                    }
                }
            }
            selectedUnitIntents.putAll(unmatchedSelectedUnitIntents);
            receivedStatesIntents.putAll(unmatchedReceivedStatesIntents);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("cannot match intents.", ex);
        }
    }

    private Predicate<ServiceConfigType.ServiceConfig> isMatchingAndOperationServiceType(ServiceType serviceType) {
        return x -> x.getServiceDescription().getServiceType() == serviceType
                && x.getServiceDescription().getPattern() == ServiceTemplate.ServicePattern.OPERATION;
    }

    private void completeActionDescription(ActionParameterType.ActionParameter actionParameter, String unitId) throws CouldNotPerformException, InterruptedException {
        try {
            ActionParameter.Builder builder = actionParameter.toBuilder();
            builder.getServiceStateDescriptionBuilder().setUnitId(unitId);
            ActionParameter actionParameterNew = builder.build();
            RemoteAction remoteAction = new RemoteAction(actionParameterNew);
            remoteAction.execute().get(5, TimeUnit.SECONDS);
            LOGGER.info("RemoteAction was delivered " + remoteAction);
            //remoteAction.waitUntilDone();

        } catch (CouldNotPerformException | ExecutionException | TimeoutException ex) {
            throw new CouldNotPerformException("could not complete action.", ex);
        }
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
                LOGGER.info("Initializing ControlController.");

                controllableObjectRegistry = new SynchronizableRegistryImpl<>();
                registryFlags = JPService.getProperty(JPPscUnitFilterList.class).getValue();
                LOGGER.info("Selected Control Registry flags: " + registryFlags.toString());
                threshold = JPService.getProperty(JPControlThreshold.class).getValue();
                LOGGER.info("Selected Control threshold: " + threshold);
                intentTimeout = JPService.getProperty(JPIntentTimeout.class).getValue();
                LOGGER.info("Selected intent timeout: " + intentTimeout);

                initializeRegistryConnection();

                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
                initialized = true;
            } catch (JPNotAvailableException | CouldNotPerformException ex) {
                throw new InitializationException(ControlController.class, ex);
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
                    controllableObjectRegistry, getUnitRegistry().getUnitConfigRemoteRegistry(), getUnitRegistry(), ControllableObjectFactory.getInstance());
            controllableObjectRegistrySynchronizer.addFilter(config -> {
                try {
                    return !PointingUnitChecker.isPointingControlUnit(config, registryFlags);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
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
            Registries.waitForData();
            LOGGER.debug("Activating Registry synchronization.");
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
