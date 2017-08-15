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

import org.openbase.bco.psc.identification.selection.distance.AbstractDistanceMeasure;
import org.openbase.jul.exception.InstantiationException;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class MeanSelector extends AbstractSelector {
    private final AbstractDistanceMeasure distance;
    
    public MeanSelector(double threshold, AbstractDistanceMeasure distance) throws InstantiationException{
        super(threshold);
        this.distance = distance;
    }

    @Override
    protected float calculateProbability(BoundingBox boundingBox, PointingRay3DFloatDistribution pointingRays) {
        float totalProb = 0.0f;
        for (PointingRay3DFloat pointingRay : pointingRays.getRayList()) {
            totalProb += (float) (distance.probability(pointingRay.getRay(), boundingBox) * pointingRay.getCertainty());
        }
        return totalProb;
    }
}
