package org.openbase.bco.psc.identification.selection.distance;

/*-
 * #%L
 * BCO PSC Identification
 * %%
 * Copyright (C) 2016 - 2019 openbase.org
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
import org.openbase.bco.psc.identification.selection.BoundingBox;
import org.openbase.type.geometry.Ray3DFloatType.Ray3DFloat;
import org.openbase.type.math.Vec3DFloatType.Vec3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class AbstractDistanceMeasure {

    protected static final Vector3d X_AXIS = new Vector3d(1, 0, 0);
    protected static final Vector3d Y_AXIS = new Vector3d(0, 1, 0);
    protected static final Vector3d Z_AXIS = new Vector3d(0, 0, 1);
    protected static final Point3d ZERO_POINT = new Point3d(0, 0, 0);

    protected static int argMin(double[] values) {
        if (values.length < 1) {
            return -1;
        }
        int min_i = 0;
        double min_val = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min_val) {
                min_i = i;
                min_val = values[i];
            }
        }
        return min_i;
    }

    protected static double min(double[] values) {
        if (values.length < 1) {
            return Integer.MIN_VALUE;
        }
        double min_val = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min_val) {
                min_val = values[i];
            }
        }
        return min_val;
    }

    protected static Vector3d toVector(Vec3DFloat vec3d) {
        return new Vector3d(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    protected static Point3d toPoint(Vec3DFloat vec3d) {
        return new Point3d(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

    protected static final double getAngle(final Vector3d vec1, final Vector3d vec2) {
        return Math.acos(vec1.dot(vec2) / (vec1.length() * vec2.length()));
    }

    protected static final Point3d getMaximalPointOnBox(final Point3d origin, final Vector3d direction, final float width, final float depth, final float height) {
        Vector3d size = new Vector3d(width, depth, height);
        Vector3d dims = new Vector3d(size);
        dims.scale(0.5);
        Vector3d originDir = new Vector3d(origin);
        Vector3d toCenter = new Vector3d(originDir);
        toCenter.scale(-1);
        Vector3d planeNormal = new Vector3d(toCenter);
        planeNormal.normalize();

        double factor = planeNormal.dot(toCenter) / planeNormal.dot(direction);
        Vector3d orthoDir = new Vector3d(direction);
        orthoDir.scale(factor);
        orthoDir.add(originDir);

        double[] possibleFactors = new double[]{Math.abs(dims.x / orthoDir.x), Math.abs(dims.y / orthoDir.y), Math.abs(dims.z / orthoDir.z)};
        int index = argMin(possibleFactors);
        Vector3d indexVector = index == 0 ? X_AXIS : index == 1 ? Y_AXIS : Z_AXIS;
        Vector3d facePoint = new Vector3d(orthoDir);
        facePoint.scale(possibleFactors[index]);

        Vector3d normalToPointingPlane = new Vector3d();
        normalToPointingPlane.cross(direction, orthoDir);
        Vector3d faceNormal = new Vector3d(indexVector);
        faceNormal.scale(Math.signum(facePoint.dot(indexVector)));
        Vector3d inFaceDir = new Vector3d();
        inFaceDir.cross(faceNormal, normalToPointingPlane);

        double sign = Math.signum(Math.abs(originDir.dot(indexVector)) - dims.dot(indexVector));
        sign = sign != 0 ? sign : -1;

        double[] possibleScales = new double[3];
        possibleScales[0] = Math.abs((dims.x - Math.signum(inFaceDir.x) * sign * facePoint.x) / inFaceDir.x);
        possibleScales[1] = Math.abs((dims.y - Math.signum(inFaceDir.y) * sign * facePoint.y) / inFaceDir.y);
        possibleScales[2] = Math.abs((dims.z - Math.signum(inFaceDir.z) * sign * facePoint.z) / inFaceDir.z);
        possibleScales[index] = Double.MAX_VALUE;
        double scale = min(possibleScales);

        Point3d result = new Point3d(inFaceDir);
        result.scale(sign * scale);
        result.add(facePoint);
        return result;
    }

    /**
     * Projects the Vector 'vector' onto the Vector 'onto'.
     *
     * @param onto the Vector to be projected on.
     * @param vector the Vector to be projected.
     * @return the projection of 'vector' on 'onto'
     */
    protected static final Vector3d getProjection(final Vector3d onto, final Vector3d vector) {
        Vector3d projection = new Vector3d(onto);
        projection.scale(projection.dot(vector) / onto.lengthSquared());
        return projection;
    }

    /**
     * Calculates the Point on the ray that is closest to the center (0, 0, 0).
     *
     * @param origin
     * @param direction
     * @return the direction vector.
     */
    protected static Point3d getClosestPoint(Point3d origin, Vector3d direction) {
        Vector3d toCenter = new Vector3d(origin);
        toCenter.scale(-1);
        Vector3d projection = getProjection(direction, toCenter);
        if (projection.dot(direction) < 0) {
            return null;
        }
        Point3d closestPoint = new Point3d(origin);
        closestPoint.add(projection);
        return closestPoint;
    }

    public final double probability(Ray3DFloat ray, BoundingBox box) {
        Point3d origin = toPoint(ray.getOrigin());
        Vector3d direction = toVector(ray.getDirection());
        //Transform everything to center coordinates of bounding box.
        Point3d transformedOrigin = box.toCenterCoordinates(origin);
        Vector3d transformedDirection = box.toCenterCoordinates(direction);
        double distance = new Vector3d(origin).length();
        //TODO: How much is that?!
        if (distance < 0.05) {
            return 1;
        }
        return distanceProbability(transformedOrigin, transformedDirection, box.getWidth(), box.getDepth(), box.getHeight());
    }

    protected abstract double distanceProbability(final Point3d origin, final Vector3d direction, final float width, final float depth, final float height);
}
