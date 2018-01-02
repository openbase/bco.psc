package org.openbase.bco.psc.identification.selection;

/*-
 * #%L
 * BCO PSC Identification
 * %%
 * Copyright (C) 2016 - 2018 openbase.org
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
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import rst.geometry.AxisAlignedBoundingBox3DFloatType.AxisAlignedBoundingBox3DFloat;
import rst.geometry.TranslationType.Translation;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class BoundingBox {

    private float width; //X-Coordinate
    private float depth; //Y-Coordinate
    private float height; //Z-Coordinate
    private Vector3d boxVector; //Width, Depth and Height
    private Point3d localCenter; //Box center in box coordinates
    private Point3d rootCenter; //Box center in root coordinates
    private Transform3D forwardTransform; //Transforms a point or vector in box coordinates to root coordinates.
    private Transform3D reverseTransform; //Transforms a point or vector in root coordinates to box coordinates.
    private Transform3D forwardCenterTransform; //Transforms a point or vector in box center coordinates (center is (0,0,0)) to root coordinates.
    private Transform3D reverseCenterTransform; //Transforms a point or vector in root coordinates to box center coordinates (center is (0,0,0)).

    public BoundingBox(Transform3D forwardTransform, float width, float depth, float height) {
        setForwardTransform(forwardTransform);
        setDimensions(width, depth, height);
        calculateCenters();
    }

    public BoundingBox(Transform3D forwardTransform, Vector3d boxVector) {
        setForwardTransform(forwardTransform);
        setBoxVector(boxVector);
        calculateCenters();
    }

    public BoundingBox(Transform3D fromUnitCoordinateToRootCoordinateTransform, AxisAlignedBoundingBox3DFloat box) {
        setDimensions(box.getWidth(), box.getDepth(), box.getHeight());

        Transform3D lfb = toTransform(toVector(box.getLeftFrontBottom()));
        forwardTransform = new Transform3D(fromUnitCoordinateToRootCoordinateTransform);
        forwardTransform.mul(lfb);
        setForwardTransform(forwardTransform);

        calculateCenters();
    }

    public Vector3d toBoxCoordinates(final Vector3d vector) {
        Vector3d transformed = new Vector3d(vector);
        reverseTransform.transform(transformed);
        return transformed;
    }

    public Point3d toBoxCoordinates(final Point3d point) {
        Point3d transformed = new Point3d(point);
        reverseTransform.transform(transformed);
        return transformed;
    }

    public Vector3d toCenterCoordinates(final Vector3d vector) {
        Vector3d transformed = new Vector3d(vector);
        reverseCenterTransform.transform(transformed);
        return transformed;
    }

    public Point3d toCenterCoordinates(final Point3d point) {
        Point3d transformed = new Point3d(point);
        reverseCenterTransform.transform(transformed);
        return transformed;
    }

    private void setForwardTransform(Transform3D forwardTransform) {
        this.forwardTransform = forwardTransform;
        reverseTransform = new Transform3D(forwardTransform);
        reverseTransform.invert();
    }

    private void setDimensions(float width, float depth, float height) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.boxVector = new Vector3d(width, depth, height);
    }

    private void setBoxVector(Vector3d boxVector) {
        this.boxVector = new Vector3d(boxVector);
        width = (float) boxVector.x;
        depth = (float) boxVector.y;
        height = (float) boxVector.z;
    }

    private void calculateCenters() {
        localCenter = new Point3d(boxVector);
        localCenter.scale(0.5);
        rootCenter = new Point3d(localCenter);
        forwardTransform.transform(rootCenter);

        forwardCenterTransform = new Transform3D(forwardTransform);
        forwardCenterTransform.mul(toTransform(new Vector3d(localCenter)));
        reverseCenterTransform = new Transform3D(forwardCenterTransform);
        reverseCenterTransform.invert();
    }

    private Vector3d toVector(final Translation translation) {
        return new Vector3d(translation.getX(), translation.getY(), translation.getZ());
    }

    private Transform3D toTransform(final Vector3d translation) {
        return new Transform3D(new Quat4d(), translation, 1);
    }

    public float getWidth() {
        return width;
    }

    public float getDepth() {
        return depth;
    }

    public float getHeight() {
        return height;
    }

    public Vector3d getBoxVector() {
        return boxVector;
    }

    public Point3d getLocalCenter() {
        return localCenter;
    }

    public Point3d getRootCenter() {
        return rootCenter;
    }

    public Quat4d getOrientation() {
        //TODO: Get rid of this part!
        Quat4d orientation = new Quat4d();
        forwardTransform.get(orientation);
        return orientation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BoundingBox)) {
            return false;
        }

        BoundingBox other = (BoundingBox) obj;

        return new EqualsBuilder()
                .append(width, other.width)
                .append(depth, other.depth)
                .append(height, other.height)
                .append(boxVector, other.boxVector)
                .append(localCenter, other.localCenter)
                .append(forwardTransform, other.forwardTransform)
                .append(reverseTransform, other.reverseTransform)
                .append(forwardCenterTransform, other.forwardCenterTransform)
                .append(reverseCenterTransform, other.reverseCenterTransform)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(width)
                .append(depth)
                .append(height)
                .append(boxVector)
                .append(localCenter)
                .append(forwardTransform)
                .append(reverseTransform)
                .append(forwardCenterTransform)
                .append(reverseCenterTransform)
                .toHashCode();
    }
}
