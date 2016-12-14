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
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import rst.math.Vec3DFloatType.Vec3DFloat;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public abstract class AbstractDistanceMeasure {
    
    public static Vector3d toVector(Vec3DFloat vec3d){
        return new Vector3d(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }
    
    public static Point3d toPoint(Vec3DFloat vec3d){
        return new Point3d(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }
    
    //TODO: Implement the change from center to translation.
    //TODO: Check all the code and inheriting classes.
    
    public final double probability(PointingRay3DFloat ray, BoundingBox box){
        Point3d origin = toPoint(ray.getRay().getOrigin());
        Vector3d direction = toVector(ray.getRay().getDirection());
        return distanceProbability(origin, direction, box);
    }
    
    protected abstract double distanceProbability(final Point3d origin, final Vector3d direction, final BoundingBox boundingBox);
    
    protected final double getAngle(final Vector3d vec1, final Vector3d vec2){
        return Math.acos(vec1.dot(vec2) / (vec1.length()*vec2.length()));
    }
    
    protected final void scaleDirectionToBoundingBox(Vector3d direction, final BoundingBox boundingBox){
        // intersect with closest plane in direction
        direction.absolute();
        direction.normalize();
        Vector3d size = new Vector3d(boundingBox.getBoxVector());
        Vector3d sizeNormalized = new Vector3d(boundingBox.getBoxVector());
        sizeNormalized.normalize();
        double x = direction.x / sizeNormalized.x;
        double y = direction.y / sizeNormalized.y;
        double z = direction.z / sizeNormalized.z;
        if(x >= y){
            if(x >= z){
                direction.scale(size.x/direction.x);
            } else {
                direction.scale(size.z/direction.z);
            }
        } else {
            if(z >= y){
                direction.scale(size.z/direction.z);
            } else {
                direction.scale(size.y/direction.y);
            }
        }
    }
    
    protected final int intersectWithPlanes(final Point3d origin, final Vector3d direction, final BoundingBox boundingBox){
        // TODO: nicht so!!
        
        double factor = Double.MAX_VALUE;
        int type = -1;
        Vector3d centerVec = new Vector3d(boundingBox.getRootCenter());
        centerVec.sub(origin);
        removeRotation(boundingBox.getOrientation(), centerVec);
        Vector3d dir = new Vector3d(direction);
        removeRotation(boundingBox.getOrientation(), dir);
        double temp = (centerVec.x - boundingBox.getBoxVector().x)/dir.x;
        if (temp < 0) return -1;
        if (temp < factor){ 
            factor = temp;
            type = 0;
        }
        temp = (centerVec.x + boundingBox.getBoxVector().x)/dir.x;
        if (temp < 0) return -1;
        if (temp < factor){ 
            factor = temp;
            type = 1;
        }
        temp = (centerVec.y - boundingBox.getBoxVector().y)/dir.y;
        if (temp < 0) return -1;
        if (temp < factor){ 
            factor = temp;
            type = 2;
        }
        temp = (centerVec.y + boundingBox.getBoxVector().y)/dir.y;
        if (temp < 0) return -1;
        if (temp < factor){ 
            factor = temp;
            type = 3;
        }
        temp = (centerVec.z - boundingBox.getBoxVector().z)/dir.z;
        if (temp < 0) return -1;
        if (temp < factor){ 
            factor = temp;
            type = 4;
        }
        temp = (centerVec.z + boundingBox.getBoxVector().z)/dir.z;
        if (temp < 0) return -1;
        if (temp < factor){ 
            factor = temp;
            type = 5;
        }
        if(type != -1){
            dir.scale(factor);
        }
        return -1;
    }
    
    protected final double getMaxAngle(final Point3d origin, final BoundingBox boundingBox){
        double max = 0;
        Vector3d centerVec = new Vector3d(boundingBox.getRootCenter());
        centerVec.sub(origin);
        removeRotation(boundingBox.getOrientation(), centerVec);
        Vector3d cornerVec = new Vector3d(centerVec);
        cornerVec.add(boundingBox.getBoxVector());
        double temp = getAngle(cornerVec, centerVec);
        if(temp > max) max = temp;
        cornerVec.z -= 2*boundingBox.getBoxVector().z;
        temp = getAngle(cornerVec, centerVec);
        if(temp > max) max = temp;
        cornerVec.y -= 2*boundingBox.getBoxVector().y;
        temp = getAngle(cornerVec, centerVec);
        if(temp > max) max = temp;
        cornerVec.z += 2*boundingBox.getBoxVector().z;
        temp = getAngle(cornerVec, centerVec);
        if(temp > max) max = temp;
        cornerVec.x -= 2*boundingBox.getBoxVector().x;
        temp = getAngle(cornerVec, centerVec);
        if(temp > max) max = temp;
        cornerVec.z -= 2*boundingBox.getBoxVector().z;
        temp = getAngle(cornerVec, centerVec);
        if(temp > max) max = temp;
        cornerVec.y += 2*boundingBox.getBoxVector().y;
        temp = getAngle(cornerVec, centerVec);
        if(temp > max) max = temp;
        cornerVec.z += 2*boundingBox.getBoxVector().z;
        temp = getAngle(cornerVec, centerVec);
        if(temp > max) max = temp;
        return max;
    }
    
    /**
     * Applies the inverse rotation of the Quaternion 'orientation' on the Vector 'direction'.
     * 
     * @param orientation the orientation to be removed from 'direction'.
     * @param direction the direction whose rotation should be removed.
     */
    protected final void removeRotation(final Quat4d orientation, Vector3d direction){
        // Transform direction
        if(orientation != null && orientation.w != 0){
            Matrix4d rotation = new Matrix4d();
            rotation.set(orientation);
            rotation.invert();
//            System.out.println(rotation);
            rotation.transform(direction);
        }
    }
    
    /**
     * Projects the Vector 'vector' onto the Vector 'onto'.
     * 
     * @param onto the Vector to be projected on.
     * @param vector the Vector to be projected.
     * @return the projection of 'vector' on 'onto'
     */
    protected final Vector3d getProjection(final Vector3d onto, final Vector3d vector){
        Vector3d direction = new Vector3d(vector);
        Vector3d projection = new Vector3d(onto);
        projection.scale(projection.dot(direction)/projection.lengthSquared());
        return projection;
    }
    
    /**
     * Calculates the direction vector from the Point 'point' towards its perpendicular foot on the Ray 'ray'.
     * 
     * @param ray the Ray to project on.
     * @param point the Point to be projected.
     * @return the direction vector.
     */
    protected final Vector3d getPerpendicularFootDirection(final Point3d origin, final Vector3d direction, final Point3d point){
        Vector3d ap = new Vector3d(point);
        ap.sub(origin);
        Vector3d projection = getProjection(direction, ap);
        if(direction.dot(projection) < 0) return null;
        Vector3d dir = new Vector3d(projection);
        dir.sub(ap);
        return dir;
    }
}
