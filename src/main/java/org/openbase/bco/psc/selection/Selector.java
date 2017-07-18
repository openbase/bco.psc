package org.openbase.bco.psc.selection;

/*-
 * #%L
 * BCO Pointing Smart Control
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

import org.openbase.bco.psc.selection.distance.AbstractDistanceMeasure;
import org.openbase.bco.psc.jp.JPThreshold;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Selector implements SelectorInterface {
    //TODO: Ideas: maximal bounding box and kdtree first.
    // Then rating by distances
    // Maybe fast enough for required numbers
    private List<AbstractSelectable> selectables = new ArrayList<AbstractSelectable>();
    private AbstractDistanceMeasure distance;
    private final Float threshold;
    
    public Selector(AbstractDistanceMeasure distance) throws CouldNotPerformException{
        this.distance = distance;
        try {
            this.threshold = JPService.getProperty(JPThreshold.class).getValue();
        } catch (JPNotAvailableException ex) {
            throw new CouldNotPerformException("Could not initialize Selector.", ex);
        }
    }

    @Override
    public synchronized AbstractSelectable checkForSelections(PointingRay3DFloatCollection pointingRays) {
        double maxProb = 0.0;
        AbstractSelectable bestObject = null;
        for (AbstractSelectable selectable : selectables) {
            for (PointingRay3DFloat pointingRay : pointingRays.getElementList()) {
                double prob = distance.probability(pointingRay, selectable.getBoundingBox()) * pointingRay.getCertainty();
                if(prob > maxProb){
                    bestObject = selectable;
                    maxProb = prob;
                }
            }
        }
        if(maxProb > this.threshold){
            return bestObject;
        }
        return null;
    }
    
    @Override
    public synchronized void setSelectables(List<AbstractSelectable> selectables) {
        this.selectables = selectables;
    }
    
}
