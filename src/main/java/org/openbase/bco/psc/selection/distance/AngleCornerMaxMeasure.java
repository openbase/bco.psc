package org.openbase.bco.psc.selection.distance;

/*-
 * #%L
 * BCO Pointing Smart Control
 * %%
 * Copyright (C) 2016 openbase.org
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

import org.openbase.bco.psc.selection.BoundingBox;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import rst.tracking.PointingRay3DFloatType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public class AngleCornerMaxMeasure extends AbstractDistanceMeasure {
    
    //TODO: Implement the change from center to translation.
    
    @Override
    protected double distanceProbability(Point3d origin, Vector3d direction, BoundingBox boundingBox) {
        Vector3d centerVec = new Vector3d(boundingBox.getRootCenter());
        centerVec.sub(origin);
        double angle = getAngle(direction, centerVec);
        if(angle > Math.PI/2) return Double.MAX_VALUE;
        double maxAngle = getMaxAngle(origin, boundingBox);
        return angle/maxAngle;
    }
}
