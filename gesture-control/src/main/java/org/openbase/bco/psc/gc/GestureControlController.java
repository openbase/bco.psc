package org.openbase.bco.psc.gc;

/*
 * -
 * #%L
 * BCO PSC Gesture Control
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
import java.util.concurrent.TimeUnit;
import org.openbase.bco.psc.control.ControllableObject;
import org.openbase.bco.psc.control.ControllableObjectFactory;
import org.openbase.bco.psc.gc.gesture.GestureDetector;
import org.openbase.bco.psc.gc.gesture.GestureType;
import org.openbase.bco.psc.gc.rsb.RSBConnection;
import org.openbase.bco.psc.lib.pointing.PostureFunctions;
import org.openbase.bco.registry.remote.Registries;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.storage.registry.RegistrySynchronizer;
import org.openbase.jul.storage.registry.SynchronizableRegistryImpl;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.domotic.unit.UnitConfigType;
import rst.domotic.unit.UnitTemplateType.UnitTemplate;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class GestureControlController extends AbstractEventHandler implements GestureControl, Launchable<Void>, VoidInitializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GestureControlLauncher.class);
    private double threshold;
    private RSBConnection rsbConnection;
    /**
     * The synchronizer which synchronizes the unit registry with the internal controllableObjectRegistry.
     */
    private RegistrySynchronizer<String, ControllableObject, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder> controllableObjectRegistrySynchronizer;
    /**
     * Internal synchronized registry containing all controllable objects.
     */
    private SynchronizableRegistryImpl<String, ControllableObject> controllableObjectRegistry;

    private boolean initialized;
    private boolean active;

    @Override
    public void handleEvent(final Event event) {
        if (!(event.getData() instanceof TrackedPostures3DFloat)) {
            return;
        }
        LOGGER.trace("New TrackedPostures3DFloat event received.");
        TrackedPostures3DFloat postures = (TrackedPostures3DFloat) event.getData();

        for (TrackedPosture3DFloat posture : postures.getPostureList()) {
            if (PostureFunctions.checkPosture(posture)) {
                GestureType type = GestureDetector.getGesture(posture);
                if (type == GestureType.WOAAAH) {
                    try {
                        for (ControllableObject co : controllableObjectRegistry.getEntries()) {
                            co.switchPowerState();
                        }
                    } catch (CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory(ex, LOGGER);
                    }
                }
            }
        }
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        if (!initialized) {
            try {
                LOGGER.info("Initializing ControlController.");

                controllableObjectRegistry = new SynchronizableRegistryImpl<>();

                initializeRegistryConnection();

                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
                initialized = true;
            } catch (CouldNotPerformException ex) {
                throw new InitializationException(GestureControlController.class, ex);
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
                        if (config.getType() == UnitTemplate.UnitType.COLORABLE_LIGHT) {
                            String location = Registries.getLocationRegistry(true).getLocationConfigById(config.getPlacementConfig().getLocationId()).getLabel();
                            if ("Living".equals(location) || "Kitchen".equals(location) || "Dining".equals(location) || "Wardrobe".equals(location) || "Lounge".equals(location)) {
//                            System.out.println(config.getLabel());
                                return true;
                            }
//                            System.out.println(location);
                        }
                        return false;
                    } catch (CouldNotPerformException ex) {
                        throw new VerificationFailedException(ex);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new VerificationFailedException(ex);
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
            System.out.println("registry size: " + controllableObjectRegistry.size());
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
