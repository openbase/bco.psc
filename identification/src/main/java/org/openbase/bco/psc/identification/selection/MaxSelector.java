package org.openbase.bco.psc.identification.selection;

/*-
 * #%L
 * BCO PSC Identification
 * %%
 * Copyright (C) 2016 - 2020 openbase.org
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
import org.openbase.type.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class MaxSelector extends AbstractUnitSelector {

    private final AbstractDistanceMeasure distance;

    public MaxSelector(double threshold, AbstractDistanceMeasure distance) throws InstantiationException {
        super(threshold);
        this.distance = distance;
    }

    @Override
    protected float calculateProbability(BoundingBox boundingBox, PointingRay3DFloatDistribution pointingRays) {
        float p = pointingRays.getRayList().stream()
                .map((pointingRay) -> (float) (distance.probability(pointingRay.getRay(), boundingBox) * pointingRay.getCertainty()))
                .reduce(0.0f, (accumulator, _item) -> Math.max(accumulator, _item));
        return p;
    }
}
