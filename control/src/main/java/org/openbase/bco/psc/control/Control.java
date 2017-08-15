package org.openbase.bco.psc.control;

/*-
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.openbase.bco.psc.control.rsb.RSBConnection;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;

/**
 * 
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Control extends AbstractEventHandler {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Control.class);
    
    private RSBConnection rsbConnection;
    
//    private RegistrySynchronizer<String, SelectableObject, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder> selectableObjectRegistrySynchronizer;
//    
//    private List<String> registryFlags;
//    private boolean connectedRegistry = false;
//    
//    // TODO list:
//    //-decide for double or float! (Single unitConfig/unitProbabilityDistribution)
//
    @Override
    public void handleEvent(final Event event) {
        LOGGER.trace(event.toString());
        if ((event.getData() instanceof UnitProbabilityCollection)) {
            UnitProbabilityCollection collection = (UnitProbabilityCollection) event.getData();
//            try {
//                UnitProbabilityCollection selectedUnits = selector.getUnitProbabilities(collection);
                // TODO process the results!
//            } catch (CouldNotPerformException ex) {
//                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
//            }
        }
    }
//    
//    public Control() {
//        try {
//            initSelector();
//            try {
//                registryFlags = JPService.getProperty(JPRegistryFlags.class).getValue();
//                
//                initializeRegistryConnection();
//
//                rsbConnection = new RSBConnection(this);
//            } catch (CouldNotPerformException | JPNotAvailableException | InterruptedException ex) {
////                selectableObjectRegistrySynchronizer.deactivate();
//                throw ex;
//            }
//            try {
//                // Wait for events.
//                while (true) {
//                    Thread.sleep(1);
//                }
//            } finally {
//                // Deactivate the listener after use.
//                rsbConnection.deactivate();
//            }
//        } catch (Exception ex) { 
//            ExceptionPrinter.printHistory(new CouldNotPerformException("Control failed", ex), LOGGER);
//            System.exit(255);
//        }
//    }
//
//    public final void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException{
//        if(connectedRegistry) return;
//        try {
//            LOGGER.info("Initializing Registry synchronization.");
//            Registries.getUnitRegistry().waitForData(3, TimeUnit.SECONDS);
//            
//            this.selectableObjectRegistrySynchronizer = new RegistrySynchronizer<String, SelectableObject, UnitConfigType.UnitConfig, UnitConfigType.UnitConfig.Builder>(
//                    selector.getSelectedObjectRegistry(), getUnitRegistry().getUnitConfigRemoteRegistry(), SelectableObjectFactory.getInstance()) {
//                @Override
//                public boolean verifyConfig(UnitConfigType.UnitConfig config) throws VerificationFailedException {
//                    try {
//                        return PointingUnitChecker.isApplicableUnit(config, registryFlags);
//                    } catch (InterruptedException ex) {
//                        ExceptionPrinter.printHistory(ex, logger);
//                        return false;
//                    }
//                }
//            };
//            
//            Registries.waitForData(); 
//            selectableObjectRegistrySynchronizer.activate();
//            connectedRegistry = true;
//        } catch (NotAvailableException ex) {
//            throw new CouldNotPerformException("Could not connect to the registry.", ex);
//        } catch (CouldNotPerformException ex) {
//            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
//        }
//    }
//    
//    private void initSelector() throws JPNotAvailableException, InstantiationException{
//        SelectorType selectorType = JPService.getProperty(JPSelectorType.class).getValue();
//        LOGGER.info("Selected Selector implementation: " + selectorType.name());
//        DistanceType distanceType = JPService.getProperty(JPDistanceType.class).getValue();
//        LOGGER.info("Selected Distance implementation: " + distanceType.name());
//        AbstractDistanceMeasure distanceMeasure;
//        switch(distanceType) {
//            case ANGLE:
//                distanceMeasure = new AngleMeasure();
//                break;
//            case ANGLE_MAX:
//                distanceMeasure = new AngleVsMaxMeasure();
//                break;
//            case ORTHOGONAL:
//                distanceMeasure = new OrthogonalMeasure();
//                break;
//            case ORTHOGONAL_MAX:
//                distanceMeasure = new OrthogonalVsMaxMeasure();
//                break;
//            case PEARSON:
//                distanceMeasure = new PearsonMeasure();
//                break;
//            default:
//                distanceMeasure = new AngleMeasure();
//                break;
//        }
//        switch(selectorType) {
//            case MAX:
//                selector = new MaxSelector(distanceMeasure);
//                break;
//            case MEAN:
//                selector = new MeanSelector(distanceMeasure);
//                break;
//            default:
//                selector = new MeanSelector(distanceMeasure);
//                break;
//        }
//        
//    }
    
    public static void main(String[] args) throws InterruptedException {
//        /* Setup JPService */
//        JPService.setApplicationName(Control.class);
//        JPService.registerProperty(JPRegistryFlags.class);
//        JPService.registerProperty(JPThreshold.class);
//        JPService.registerProperty(JPSelectorType.class);
//        JPService.registerProperty(JPDistanceType.class);
//        JPService.registerProperty(JPInScope.class);
//        JPService.registerProperty(JPLocalInput.class);
//        JPService.parseAndExitOnError(args);
//        
//        Control app = new Control();
    }
}
