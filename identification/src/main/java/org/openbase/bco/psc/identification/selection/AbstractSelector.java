package org.openbase.bco.psc.identification.selection;

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

import java.util.stream.Collectors;
import org.openbase.bco.psc.identification.registry.SelectableObject;
import org.openbase.bco.psc.lib.registry.SynchronizableRegistryImpl;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;
import rst.domotic.unit.UnitProbabilityCollectionType.UnitProbabilityCollection;
import rst.domotic.unit.UnitProbabilityType.UnitProbability;
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class AbstractSelector {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractSelector.class);
    private final double threshold;
    //TODO: Ideas: maximal bounding box and kdtree first.
    // Then rating by distances
    // Maybe fast enough for required numbers
    
    private SynchronizableRegistryImpl<String, SelectableObject> selectedObjectRegistry;
    
    public AbstractSelector(double threshold) throws InstantiationException{
        this.threshold = threshold;
        try {
            this.selectedObjectRegistry = new SynchronizableRegistryImpl<>();
        } catch (InstantiationException ex) {
            throw new InstantiationException(this, ex);
        }
    }
    
    public SynchronizableRegistryImpl<String, SelectableObject> getSelectedObjectRegistry(){
        return selectedObjectRegistry;
    }
    
    public UnitProbabilityCollection getUnitProbabilities(PointingRay3DFloatDistributionCollection pointingRays) throws CouldNotPerformException{
        UnitProbabilityCollection.Builder collectionBuilder = UnitProbabilityCollection.newBuilder();
        collectionBuilder.addAllElement(selectedObjectRegistry.getEntries().stream()
                .map(entry -> pointingRays.getElementList().stream()
                        .map(rayDist -> getUnitProbability(entry, rayDist))
                        .min((u1, u2) -> Float.compare(u1.getProbability(), u2.getProbability())))
                .filter(u -> u.isPresent() && u.get().getProbability() >= threshold)
                .map(u -> u.get())
                .collect(Collectors.toList()));
        return collectionBuilder.build();
    }
    
    private UnitProbability getUnitProbability(SelectableObject object, PointingRay3DFloatDistribution rayDistribution) {
        try {
            float prob = calculateProbability(object.getBoundingBox(), rayDistribution);
            return UnitProbability.newBuilder().setId(object.getId()).setProbability(prob).build();
        } catch (NotAvailableException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not calculate the probability for a SelectableObject", ex), LOGGER, LogLevel.WARN);
            return UnitProbability.newBuilder().setProbability(Float.NEGATIVE_INFINITY).build();
        }
    }
    
    protected abstract float calculateProbability(BoundingBox boundingBox, PointingRay3DFloatDistribution pointingRays);
}
