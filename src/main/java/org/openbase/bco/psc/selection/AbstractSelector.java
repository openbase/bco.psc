package org.openbase.bco.psc.selection;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openbase.bco.psc.registry.SelectableObject;
import org.openbase.bco.psc.registry.SynchronizableRegistryImpl;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.domotic.unit.UnitProbabilityType.UnitProbability;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class AbstractSelector {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractSelector.class);
    //TODO: Ideas: maximal bounding box and kdtree first.
    // Then rating by distances
    // Maybe fast enough for required numbers
    
    private SynchronizableRegistryImpl<String, SelectableObject> selectedObjectRegistry;
    
    public AbstractSelector() throws InstantiationException{
        try {
            this.selectedObjectRegistry = new SynchronizableRegistryImpl<>();
        } catch (InstantiationException ex) {
            throw new InstantiationException(this, ex);
        }
    }
    
    public SynchronizableRegistryImpl<String, SelectableObject> getSelectedObjectRegistry(){
        return selectedObjectRegistry;
    }
    
    public UnitProbabilityCollection getUnitProbabilities(PointingRay3DFloatCollection pointingRays) throws CouldNotPerformException{
        UnitProbabilityCollection.Builder collectionBuilder = UnitProbabilityCollection.newBuilder();
        try {
            for(SelectableObject entry : selectedObjectRegistry.getEntries()){
                try {
                    float prob = calculateProbability(entry.getBoundingBox(), pointingRays);
                    collectionBuilder.addElement(UnitProbability.newBuilder().setId(entry.getId()).setProbability(prob).build());
                } catch (NotAvailableException ex) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not calculate the probability for a SelectableObject", ex), LOGGER, LogLevel.WARN);
                }
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not get UnitProbabilitites", ex);
        }
        return collectionBuilder.build();
    }
    
    protected abstract float calculateProbability(BoundingBox boundingBox, PointingRay3DFloatCollection pointingRays);
}
