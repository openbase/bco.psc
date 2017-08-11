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

import org.openbase.bco.psc.selection.distance.AbstractDistanceProbabilityMeasure;
import org.openbase.jul.exception.InstantiationException;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class MaxSelector extends AbstractSelector {
    private final AbstractDistanceProbabilityMeasure distance;
    
    public MaxSelector(AbstractDistanceProbabilityMeasure distance) throws InstantiationException {
        super();
        this.distance = distance;
    }

    @Override
    protected float calculateProbability(BoundingBox boundingBox, PointingRay3DFloatCollection pointingRays) {
        float maxProb = 0.0f;
        for (PointingRay3DFloat pointingRay : pointingRays.getElementList()) {
            float prob = (float) (distance.probability(pointingRay.getRay(), boundingBox) * pointingRay.getCertainty());
            if(prob > maxProb){
                maxProb = prob;
            }
        }
        return maxProb;
    }
}
