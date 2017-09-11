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
import org.openbase.bco.psc.identification.jp.JPSelectorType;
import org.openbase.bco.psc.identification.registry.SelectableObject;
import org.openbase.bco.psc.identification.registry.SelectableObjectFactory;
import org.openbase.bco.psc.identification.rsb.RSBConnection;
import org.openbase.bco.psc.identification.selection.AbstractSelector;
import org.openbase.bco.psc.identification.selection.MaxSelector;
import org.openbase.bco.psc.identification.selection.MeanSelector;
import org.openbase.bco.psc.identification.selection.SelectorType;
import org.openbase.bco.psc.identification.selection.distance.AbstractDistanceMeasure;
import org.openbase.bco.psc.identification.selection.distance.AngleMeasure;
import org.openbase.bco.psc.identification.selection.distance.AngleVsMaxMeasure;
import org.openbase.bco.psc.identification.selection.distance.DistanceType;
import org.openbase.bco.psc.identification.selection.distance.OrthogonalMeasure;
import org.openbase.bco.psc.identification.selection.distance.OrthogonalVsMaxMeasure;
import org.openbase.bco.psc.identification.selection.distance.PearsonMeasure;
import org.openbase.bco.psc.lib.jp.JPLocalInput;
import org.openbase.bco.psc.lib.jp.JPLocalOutput;
import org.openbase.bco.psc.lib.jp.JPPscUnitFilterList;
import org.openbase.bco.psc.lib.jp.JPRayScope;
import org.openbase.bco.psc.lib.jp.JPSelectedUnitScope;
import org.openbase.bco.psc.lib.jp.JPThreshold;
import org.openbase.bco.psc.lib.registry.PointingUnitChecker;
import org.openbase.bco.registry.remote.Registries;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.storage.registry.RegistrySynchronizer;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.domotic.unit.UnitConfigType;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Identification extends AbstractEventHandler {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Identification.class);
    private AbstractSelector selector;
    private RSBConnection rsbConnection;

    private RegistrySynchronizer<String, SelectableObject, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder> selectableObjectRegistrySynchronizer;

    private List<String> registryFlags;

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
            }
        }
    }

    public Identification() {
        try {
            initSelector();
            try {
                registryFlags = JPService.getProperty(JPPscUnitFilterList.class).getValue();

                initializeRegistryConnection();

                rsbConnection = new RSBConnection(this);
            } catch (CouldNotPerformException | JPNotAvailableException | InterruptedException ex) {
                selectableObjectRegistrySynchronizer.deactivate();
                throw ex;
            }
            addShutdownHook();
            // Wait for events.
            while (true) {
                Thread.sleep(1000);
            }

            //TODO: Remove this!
//            rsbConnection = new RSBConnection(this);
//            rsbConnection.sendUnitProbabilities(UnitProbabilityCollection.newBuilder().addElement(
//                    //                    UnitProbability.newBuilder().setId("c8b2bfb5-45d9-4a2b-9994-d4062ab19cab").setProbability(1.0f)
//                    //                    UnitProbability.newBuilder().setId("c8b2bfb5-45da9-4a2b-9994-d4062ab19cab").setProbability(1.0f)
//                    //                    UnitProbability.newBuilder().setId("2c95255e-a491-46d7-a6a6-f66d5e6c2d3b").setProbability(1.0f)
//                    UnitProbability.newBuilder().setId("8f7b2513-4f33-4e8d-8b7e-eced4d54108c").setProbability(0.66f)
//            ).build());
//            rsbConnection.deactivate();
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("PSC Identification failed", ex), LOGGER);
            System.exit(255);
        }
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                rsbConnection.deactivate();
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER);
            } catch (InterruptedException ex) {
                LOGGER.error("Interruption during deactivation of rsb connection.");
                Thread.currentThread().interrupt();
            }
        }));
    }

    private void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        try {
            LOGGER.info("Initializing Registry synchronization.");
            Registries.getUnitRegistry().waitForData(3, TimeUnit.SECONDS);

            this.selectableObjectRegistrySynchronizer = new RegistrySynchronizer<String, SelectableObject, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder>(
                    selector.getSelectedObjectRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(), SelectableObjectFactory.getInstance()) {
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

            Registries.waitForData();
            selectableObjectRegistrySynchronizer.activate();
        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not connect to the registry.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }

    private void initSelector() throws JPNotAvailableException, InstantiationException {
        SelectorType selectorType = JPService.getProperty(JPSelectorType.class).getValue();
        LOGGER.info("Selected Selector implementation: " + selectorType.name());
        DistanceType distanceType = JPService.getProperty(JPDistanceType.class).getValue();
        LOGGER.info("Selected Distance implementation: " + distanceType.name());
        double threshold = JPService.getProperty(JPThreshold.class).getValue();
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

    public static void main(String[] args) throws InterruptedException {
        /* Setup JPService */
        JPService.setApplicationName(Identification.class);
        JPService.registerProperty(JPPscUnitFilterList.class);
        JPService.registerProperty(JPThreshold.class);
        JPService.registerProperty(JPSelectorType.class);
        JPService.registerProperty(JPDistanceType.class);
        JPService.registerProperty(JPRayScope.class);
        JPService.registerProperty(JPSelectedUnitScope.class);
        JPService.registerProperty(JPLocalInput.class);
        JPService.registerProperty(JPLocalOutput.class);
        JPService.parseAndExitOnError(args);

        Identification app = new Identification();
    }
}
