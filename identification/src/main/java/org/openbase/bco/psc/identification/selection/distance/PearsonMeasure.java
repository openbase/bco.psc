package org.openbase.bco.psc.identification.selection.distance;

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
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PearsonMeasure extends AbstractDistanceMeasure {
    //TODO: Not really a probability here...

    /**
     * {@inheritDoc}
     *
     * @param origin {@inheritDoc}
     * @param direction {@inheritDoc}
     * @param width {@inheritDoc}
     * @param depth {@inheritDoc}
     * @param height {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected double distanceProbability(Point3d origin, Vector3d direction, float width, float depth, float height) {
        Point3d closestPoint = getClosestPoint(origin, direction);
        if (closestPoint == null) {
            return Double.MAX_VALUE;
        }
        return pearsonLength(new Vector3d(closestPoint), new Vector3d(width, depth, height));
    }

    private double pearsonLength(final Vector3d vector, final Vector3d size) {
        return Math.sqrt((vector.x * vector.x) / (size.x * size.x) + (vector.y * vector.y) / (size.y * size.y) + (vector.z * vector.z) / (size.z * size.z));
    }
}
