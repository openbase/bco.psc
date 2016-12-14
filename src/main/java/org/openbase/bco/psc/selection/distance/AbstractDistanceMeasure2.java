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

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.openbase.bco.psc.selection.BoundingBox;
import rst.math.Vec3DFloatType;
import rst.tracking.PointingRay3DFloatType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public abstract class AbstractDistanceMeasure2 {
    
    public static Vector3d toVector(Vec3DFloatType.Vec3DFloat vec3d){
        return new Vector3d(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }
    
    public static Point3d toPoint(Vec3DFloatType.Vec3DFloat vec3d){
        return new Point3d(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }
    
    public static final double getAngle(final Vector3d vec1, final Vector3d vec2){
        return Math.acos(vec1.dot(vec2) / (vec1.length()*vec2.length()));
    }
    
    public final double probability(PointingRay3DFloatType.PointingRay3DFloat ray, BoundingBox box){
        Point3d origin = toPoint(ray.getRay().getOrigin());
        Vector3d direction = toVector(ray.getRay().getDirection());
        //Transform everything to center coordinates of bounding box.
        Point3d transformedOrigin = box.toCenterCoordinates(origin);
        Vector3d transformedDirection = box.toCenterCoordinates(direction);
        //TODO: check distance to box first, maybe touching the object?!
        return distanceProbability(transformedOrigin, transformedDirection, box.getWidth(), box.getDepth(), box.getHeight());
    }
    
    protected abstract double distanceProbability(final Point3d origin, final Vector3d direction, final float width, final float depth, final float height);
    
}
