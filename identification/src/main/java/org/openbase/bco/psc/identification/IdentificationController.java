package org.openbase.bco.psc.identification;

/*-
 * #%L
 * BCO PSC Identification
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openbase.bco.psc.identification.jp.JPDistanceType;
import org.openbase.bco.psc.identification.jp.JPIdentificationThreshold;
import org.openbase.bco.psc.identification.jp.JPUnitSelectorType;
import org.openbase.bco.psc.identification.registry.SelectableObject;
import org.openbase.bco.psc.identification.registry.SelectableObjectFactory;
import org.openbase.bco.psc.identification.rsb.RSBConnection;
import org.openbase.bco.psc.identification.selection.AbstractUnitSelector;
import org.openbase.bco.psc.identification.selection.MaxSelector;
import org.openbase.bco.psc.identification.selection.MeanSelector;
import org.openbase.bco.psc.identification.selection.SelectorType;
import static org.openbase.bco.psc.identification.selection.SelectorType.*;
import org.openbase.bco.psc.identification.selection.distance.AbstractDistanceMeasure;
import org.openbase.bco.psc.identification.selection.distance.AngleMeasure;
import org.openbase.bco.psc.identification.selection.distance.AngleVsMaxMeasure;
import org.openbase.bco.psc.identification.selection.distance.DistanceType;
import static org.openbase.bco.psc.identification.selection.distance.DistanceType.*;
import org.openbase.bco.psc.identification.selection.distance.OrthogonalMeasure;
import org.openbase.bco.psc.identification.selection.distance.OrthogonalVsMaxMeasure;
import org.openbase.bco.psc.identification.selection.distance.PearsonMeasure;
import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.lib.registry.PointingUnitChecker;
import org.openbase.bco.registry.remote.Registries;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.storage.registry.RegistrySynchronizer;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.domotic.unit.UnitConfigType.UnitConfig;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class IdentificationController extends AbstractEventHandler implements Identification, Launchable<Void>, VoidInitializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IdentificationController.class);
    private AbstractUnitSelector selector;
    private RSBConnection rsbConnection;

    private RegistrySynchronizer<String, SelectableObject, UnitConfig, UnitConfig.Builder> selectableObjectRegistrySynchronizer;

    private List<String> registryFlags;

    private boolean initialized = false;
    private boolean active = false;

    // TODO list:
    //-decide for double or float! (Single unitConfig/unitProbabilityDistribution)
    @Override
    public void handleEvent(final Event event) {
//        LOGGER.trace(event.toString());
        if ((event.getData() instanceof PointingRay3DFloatDistributionCollection)) {
            PointingRay3DFloatDistributionCollection collection = (PointingRay3DFloatDistributionCollection) event.getData();
            try {
                UnitProbabilityCollection selectedUnits = selector.getUnitProbabilities(collection);
                rsbConnection.sendUnitProbabilities(selectedUnits);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        }
    }

    private void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        try {
            LOGGER.info("Initializing Registry synchronization.");
            Registries.getUnitRegistry().waitForData(3, TimeUnit.SECONDS);

            this.selectableObjectRegistrySynchronizer = new RegistrySynchronizer<String, SelectableObject, UnitConfig, UnitConfig.Builder>(
                    selector.getSelectedObjectRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(), SelectableObjectFactory.getInstance()) {
                @Override
                public boolean verifyConfig(UnitConfig config) throws VerificationFailedException {
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

    private void initSelector() throws JPNotAvailableException, InstantiationException {
        SelectorType selectorType = JPService.getProperty(JPUnitSelectorType.class).getValue();
        LOGGER.info("Selected Selector implementation: " + selectorType.name());
        DistanceType distanceType = JPService.getProperty(JPDistanceType.class).getValue();
        LOGGER.info("Selected Distance implementation: " + distanceType.name());
        double threshold = JPService.getProperty(JPIdentificationThreshold.class).getValue();
        LOGGER.info("Selected threshold: " + threshold);
        AbstractDistanceMeasure distanceMeasure;
        switch (distanceType) {
            case ANGLE:
                distanceMeasure = new AngleMeasure();
                break;
            case ANGLE_MAX:
                distanceMeasure = new AngleVsMaxMeasure();
                break;
            case ORTHOGONAL:
                distanceMeasure = new OrthogonalMeasure();
                break;
            case ORTHOGONAL_MAX:
                distanceMeasure = new OrthogonalVsMaxMeasure();
                break;
            case PEARSON:
                distanceMeasure = new PearsonMeasure();
                break;
            default:
                distanceMeasure = new AngleMeasure();
                break;
        }
        switch (selectorType) {
            case MAX:
                selector = new MaxSelector(threshold, distanceMeasure);
                break;
            case MEAN:
                selector = new MeanSelector(threshold, distanceMeasure);
                break;
            default:
                selector = new MeanSelector(threshold, distanceMeasure);
                break;
        }

    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        if (!initialized) {
            initialized = true;
            try {
                initSelector();
                registryFlags = JPService.getProperty(JPPscUnitFilterList.class).getValue();

                initializeRegistryConnection();

                rsbConnection = new RSBConnection(this);
                rsbConnection.init();

                //TODO: Remove this!
                //            rsbConnection = new RSBConnection(this);
                //            rsbConnection.sendUnitProbabilities(UnitProbabilityCollection.newBuilder().addElement(
                //                    //                    UnitProbability.newBuilder().setId("c8b2bfb5-45d9-4a2b-9994-d4062ab19cab").setProbability(1.0f)
                //                    //                    UnitProbability.newBuilder().setId("c8b2bfb5-45da9-4a2b-9994-d4062ab19cab").setProbability(1.0f)
                //                    //                    UnitProbability.newBuilder().setId("2c95255e-a491-46d7-a6a6-f66d5e6c2d3b").setProbability(1.0f)
                //                    UnitProbability.newBuilder().setId("8f7b2513-4f33-4e8d-8b7e-eced4d54108c").setProbability(0.66f)
                //            ).build());
                //            rsbConnection.deactivate();
            } catch (JPNotAvailableException | CouldNotPerformException ex) {
                throw new InitializationException(IdentificationController.class, ex);
            }
        }
    }

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
            selectableObjectRegistrySynchronizer.activate();
            rsbConnection.activate();
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Deactivating " + getClass().getName() + ".");
        if (active) {
            active = false;
            rsbConnection.deactivate();
            LOGGER.info("Deactivating Registry synchronization.");
            selectableObjectRegistrySynchronizer.deactivate();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

}
