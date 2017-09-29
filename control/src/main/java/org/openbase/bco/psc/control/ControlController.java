package org.openbase.bco.psc.control;

/*
 * -
 * #%L
 * BCO PSC Control
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openbase.bco.psc.control.jp.JPControlThreshold;
import org.openbase.bco.psc.control.rsb.RSBConnection;
import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.lib.registry.PointingUnitChecker;
import org.openbase.bco.registry.remote.Registries;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.storage.registry.RegistrySynchronizer;
import org.openbase.jul.storage.registry.SynchronizableRegistryImpl;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.domotic.unit.UnitConfigType;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;

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
     * Activation state of this class.
     */
    private boolean active = false;
    /**
     * Initialization state of this class.
     */
    private boolean initialized = false;

    /**
     * {@inheritDoc}
     *
     * @param event {@inheritDoc}
     */
    @Override
    public void handleEvent(final Event event) {
        LOGGER.trace(event.toString());
        if ((event.getData() instanceof UnitProbabilityCollection)) {
            UnitProbabilityCollection collection = (UnitProbabilityCollection) event.getData();
            collection.getElementList().stream().filter(x -> x.getProbability() >= threshold).forEach(x -> {
                try {
                    if (controllableObjectRegistry.contains(x.getId())) {
                        try {
                            if (controllableObjectRegistry.get(x.getId()).switchPowerState()) {
                                LOGGER.info("Switched power state of unit " + controllableObjectRegistry.get(x.getId()).getConfig().getLabel() + " with id " + x.getId());
                            } else {
                                LOGGER.trace("Did not switch power state of unit " + controllableObjectRegistry.get(x.getId()).getConfig().getLabel() + " with id " + x.getId());
                            }
                        } catch (CouldNotPerformException ex) {
                            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
                        }
                    }
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Id of the UnitProbability is not set.", ex), LOGGER, LogLevel.WARN);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InitializationException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
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

                initializeRegistryConnection();

                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
                initialized = true;
            } catch (JPNotAvailableException | CouldNotPerformException ex) {
                throw new InitializationException(ControlController.class, ex);
            }
        }
    }

    /**
     * Initializes the synchronization of the internal controllableObjectRegistry with the unit registry.
     *
     * @throws InterruptedException is thrown in case of an external interruption.
     * @throws CouldNotPerformException is thrown, if the registry synchronization could not be initialized.
     */
    private void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        try {
            LOGGER.info("Initializing Registry synchronization.");
            Registries.getUnitRegistry().waitForData(3, TimeUnit.SECONDS);

            this.controllableObjectRegistrySynchronizer = new RegistrySynchronizer<String, ControllableObject, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder>(
                    controllableObjectRegistry, getUnitRegistry().getUnitConfigRemoteRegistry(), ControllableObjectFactory.getInstance()) {
                @Override
                public boolean verifyConfig(UnitConfigType.UnitConfig config) throws VerificationFailedException {
                    try {
                        return PointingUnitChecker.isPointingControlUnit(config, registryFlags);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
                        return false;
                    } catch (CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
                        return false;
                    }
                }
            };
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
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Activating " + getClass().getName() + ".");
        if (!initialized) {
            throw new CouldNotPerformException("Activate can only be called after init.");
        }
        if (!active) {
            active = true;
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
     * @throws InterruptedException {@inheritDoc}
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
