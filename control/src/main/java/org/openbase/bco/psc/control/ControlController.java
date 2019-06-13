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

import org.openbase.bco.dal.lib.action.ActionDescriptionProcessor;
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
import org.openbase.type.domotic.action.ActionDescriptionType;
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

import java.util.List;
import java.util.Stack;
import java.util.concurrent.Future;
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
    private Integer intentTimeout;
    /**
     * Activation state of this class.
     */
    private boolean active = false;
    /**
     * Initialization state of this class.
     */
    private boolean initialized = false;

    private Stack<Event> selectedUnitIntents;

    private Stack<Event> receivedStatesIntents;

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void handleEvent(final Event event) {
        LOGGER.trace(event.toString());

        if ((event.getData() instanceof UnitProbabilityCollection)) {  // this could also be done with event.scope
            selectedUnitIntents.add(event);
        } else { // assume we got a State, we should only receive (Power,..)States and UnitProbabilityCollections
            receivedStatesIntents.add(event);
        }
        removeOldIntents();

        try {
            executeMatchingIntents();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }

    private void removeOldIntents() {
        long currentTime = System.currentTimeMillis();
        selectedUnitIntents.removeIf(event -> currentTime > intentTimeout + event.getMetaData().getDeliverTime());
        receivedStatesIntents.removeIf(event -> currentTime > intentTimeout + event.getMetaData().getDeliverTime());
    }

    private void executeMatchingIntents() throws CouldNotPerformException {
        try {
            for (Event selectedUnit : selectedUnitIntents) {
                UnitProbabilityCollection collection = (UnitProbabilityCollection) selectedUnit.getData();
                List<String> selectedUnitIds = collection.getElementList().stream()
                        .filter(x -> x.getProbability() >= threshold)
                        .map(UnitProbability::getId)
                        .collect(Collectors.toList());
                for (String unitId : selectedUnitIds) {
                    UnitConfigType.UnitConfig unitConfig = getUnitRegistry().getUnitConfigById(unitId);
                    for (Event receivedState : receivedStatesIntents) {
                        ActionParameter actionParameter = (ActionParameter) receivedState.getData();
                        ServiceType serviceType = actionParameter.getServiceStateDescription().getServiceType();
                        if (unitConfig.getServiceConfigList().stream().anyMatch(isMatchingAndOperationServiceType(serviceType))) {
                            completeActionDescription(actionParameter, unitId);
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

    private Future<ActionDescriptionType.ActionDescription> completeActionDescription(ActionParameterType.ActionParameter actionParameter, String unitId) throws CouldNotPerformException {
        try {
            ActionDescriptionType.ActionDescription.Builder builder = ActionDescriptionProcessor.generateActionDescriptionBuilder(actionParameter);
            builder.getServiceStateDescriptionBuilder().setUnitId(unitId);
            RemoteAction remoteAction = new RemoteAction(builder.build());
            return remoteAction.execute();
        } catch (CouldNotPerformException ex) {
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
            selectedUnitIntents = new Stack<>();
            receivedStatesIntents = new Stack<>();
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
