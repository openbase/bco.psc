package org.openbase.bco.psc.lib.pointing;

/*
 * -
 * #%L
 * BCO PSC Library
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import javafx.geometry.Point3D;
import org.openbase.type.geometry.Ray3DFloatType.Ray3DFloat;
import org.openbase.type.geometry.TranslationType.Translation;
import org.openbase.type.kinematics.Posture3DFloatType.Posture3DFloat;
import org.openbase.type.math.Vec3DFloatType.Vec3DFloat;
import org.openbase.type.tracking.PointingRay3DFloatType.PointingRay3DFloat;
import org.openbase.type.tracking.PointingRay3DFloatType.PointingRay3DFloat.PointingType;
import org.openbase.type.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PostureFunctions {

    public static final Point3D UP = new Point3D(0, 0, 1);

    //================================================================================
    // Joint Pair and Joints
    //================================================================================
    public static final JointPair getJointPair(final boolean right, final PointingType type) {
        if (right) {
            switch (type) {
                case HEAD_HAND:
                    return new JointPair(Joints.Head, Joints.HandRight);
                case HEAD_FINGERTIP:
                    return new JointPair(Joints.Head, Joints.HandTipRight);
                case SHOULDER_HAND:
                    return new JointPair(Joints.ShoulderRight, Joints.HandRight);
                case FOREARM:
                    return new JointPair(Joints.ElbowRight, Joints.HandRight);
                case HAND:
                    return new JointPair(Joints.HandRight, Joints.HandTipRight);
                default:
                    return new JointPair(Joints.Head, Joints.HandRight);
            }
        } else {
            switch (type) {
                case HEAD_HAND:
                    return new JointPair(Joints.Head, Joints.HandLeft);
                case HEAD_FINGERTIP:
                    return new JointPair(Joints.Head, Joints.HandTipLeft);
                case SHOULDER_HAND:
                    return new JointPair(Joints.ShoulderLeft, Joints.HandLeft);
                case FOREARM:
                    return new JointPair(Joints.ElbowLeft, Joints.HandLeft);
                case HAND:
                    return new JointPair(Joints.HandLeft, Joints.HandTipLeft);
                default:
                    return new JointPair(Joints.Head, Joints.HandLeft);
            }
        }
    }

    public static Joints otherJoint(final Joints joint) {
        switch (joint) {
            case AnkleLeft:
                return Joints.AnkleRight;
            case AnkleRight:
                return Joints.AnkleLeft;
            case ElbowLeft:
                return Joints.ElbowRight;
            case ElbowRight:
                return Joints.ElbowLeft;
            case FootLeft:
                return Joints.FootRight;
            case FootRight:
                return Joints.FootLeft;
            case HandLeft:
                return Joints.HandRight;
            case HandRight:
                return Joints.HandLeft;
            case HandTipLeft:
                return Joints.HandTipRight;
            case HandTipRight:
                return Joints.HandTipLeft;
            case HipLeft:
                return Joints.HipRight;
            case HipRight:
                return Joints.HipLeft;
            case KneeLeft:
                return Joints.KneeRight;
            case KneeRight:
                return Joints.KneeLeft;
            case ShoulderLeft:
                return Joints.ShoulderRight;
            case ShoulderRight:
                return Joints.ShoulderLeft;
            case ThumbLeft:
                return Joints.ThumbRight;
            case ThumbRight:
                return Joints.ThumbLeft;
            case WristLeft:
                return Joints.WristRight;
            case WristRight:
                return Joints.WristLeft;
            default:
                return joint;
        }
    }

    //================================================================================
    // Type-Conversions
    //================================================================================
    public static final Vec3DFloat toVec3DFloat(final Translation translation) {
        return Vec3DFloat.newBuilder()
                .setX((float) translation.getX())
                .setY((float) translation.getY())
                .setZ((float) translation.getZ()).build();
    }

    public static final Vec3DFloat toVec3DFloat(final Point3D point) {
        return Vec3DFloat.newBuilder()
                .setX((float) point.getX())
                .setY((float) point.getY())
                .setZ((float) point.getZ()).build();
    }

    public static final Vec3DFloat getVec(final TrackedPosture3DFloat posture, final Joints joint) {
        return toVec3DFloat(posture.getPosture().getPosition(joint.getValue()));
    }

    public static final Vec3DFloat subtract(final Vec3DFloat first, final Vec3DFloat second) {
        return Vec3DFloat.newBuilder()
                .setX(first.getX() - second.getX())
                .setY(first.getY() - second.getY())
                .setZ(first.getZ() - second.getZ()).build();
    }

    public static Ray3DFloat getRay(final TrackedPosture3DFloat posture, final JointPair jointPair) {
        final Vec3DFloat startVec = getVec(posture, jointPair.getJoint1());
        final Vec3DFloat endVec = getVec(posture, jointPair.getJoint2());
        final Vec3DFloat direction = subtract(endVec, startVec);
        return Ray3DFloat.newBuilder().setOrigin(endVec).setDirection(direction).build();
    }

    public static final PointingRay3DFloat getPointingRay(final TrackedPosture3DFloat posture, final boolean right,
            final double pointingProbability, final PointingType type) {
        final JointPair jointPair = getJointPair(right, type);
        return PointingRay3DFloat.newBuilder().setRay(getRay(posture, jointPair)).setCertainty((float) pointingProbability).setType(type).setRightHandPointing(right).build();
    }

    public static final PointingRay3DFloat getPointingRayWithConfidence(final TrackedPosture3DFloat posture, final boolean right,
            final double pointingProbability, final PointingType type) {
        final JointPair jointPair = getJointPair(right, type);
        final float certainty = posture.getConfidence(jointPair.getJoint1().getValue())
                * posture.getConfidence(jointPair.getJoint2().getValue());
        return getPointingRay(posture, right, certainty * pointingProbability, type);
    }

    public static final Collection<PointingRay3DFloat> getAllRaysForSideAndTypesWithConfidence(final TrackedPosture3DFloat posture,
            final boolean right, final double pointingProbability, final Collection<PointingType> types) {
        return types.stream()
                .map(type -> getPointingRayWithConfidence(posture, right, pointingProbability / types.size(), type))
                .collect(Collectors.toList());
    }

    public static final Collection<PointingRay3DFloat> getAllRaysForSideWithConfidence(final TrackedPosture3DFloat posture, final boolean right,
            final double pointingProbability) {
        return getAllRaysForSideAndTypesWithConfidence(posture, right, pointingProbability, Arrays.asList(PointingType.values()));
    }

    //================================================================================
    // Posture check
    //================================================================================
    public static final boolean checkPosture(final TrackedPosture3DFloat posture) {
        if (!posture.hasPosture()) {
            return false;
        }
        final Posture3DFloat post = posture.getPosture();
        return post.getPositionCount() != 0 && post.getPositionCount() == posture.getConfidenceCount();
    }

    public static final double postureConfidence(final TrackedPosture3DFloat posture, final boolean right) {
        double confidence = posture.getConfidence(Joints.Head.getValue());
        if (right) {
            confidence *= posture.getConfidence(Joints.ShoulderRight.getValue());
            confidence *= posture.getConfidence(Joints.ElbowRight.getValue());
            confidence *= posture.getConfidence(Joints.WristRight.getValue());
            confidence *= posture.getConfidence(Joints.HandRight.getValue());
        } else {
            confidence *= posture.getConfidence(Joints.ShoulderLeft.getValue());
            confidence *= posture.getConfidence(Joints.ElbowLeft.getValue());
            confidence *= posture.getConfidence(Joints.WristLeft.getValue());
            confidence *= posture.getConfidence(Joints.HandLeft.getValue());
        }
        return confidence;
    }

    public static final boolean checkConfidences(final TrackedPosture3DFloat posture, final boolean right) {
        return postureConfidence(posture, right) == 1.0;
    }

    //================================================================================
    // Point3D calculations
    //================================================================================
    public static final Translation toTranslation(final Point3D point) {
        return Translation.newBuilder().setX(point.getX()).setY(point.getY()).setZ(point.getZ()).build();
    }

    public static final Point3D toPoint3D(final Translation translation) {
        return new Point3D(translation.getX(), translation.getY(), translation.getZ());
    }

    public static final Point3D getPoint3D(final TrackedPosture3DFloat posture, final Joints joint) {
        return toPoint3D(posture.getPosture().getPosition(joint.getValue()));
    }

    public static final Point3D getDirection(final TrackedPosture3DFloat posture, final JointPair jointPair) {
        final Point3D start = getPoint3D(posture, jointPair.getJoint1());
        final Point3D end = getPoint3D(posture, jointPair.getJoint2());
        return end.subtract(start);
    }

    public static final Point3D projectOn(final Point3D vector, final Point3D onto) {
        return onto.multiply(onto.dotProduct(vector) / (Math.pow(onto.magnitude(), 2)));
    }

    public static final Point3D projectOrthogonal(final Point3D vector, final Point3D orthogonal) {
        return vector.subtract(projectOn(vector, orthogonal));
    }

    public static final double getSignedAngle(final Point3D vector1, final Point3D vector2, final Point3D up) {
        final double angle = vector1.angle(vector2);
        // positive if vector2 turned to the right from vector1 viewed from up.
        return up.dotProduct(vector2.crossProduct(vector1)) < 0 ? -angle : angle;
    }

    //================================================================================
    // Posture specific calculations
    //================================================================================
    public static final Point3D postureUpDirection(final TrackedPosture3DFloat posture) {
        return getDirection(posture, new JointPair(Joints.SpineBase, Joints.Neck)).normalize();
    }

    public static final Point3D postureUpDirection(final TrackedPosture3DFloat posture, final boolean relative) {
        return relative ? postureUpDirection(posture) : UP;
    }

    public static final Point3D postureRightDirection(final TrackedPosture3DFloat posture, final Point3D up) {
        final Point3D hipDir = getDirection(posture, new JointPair(Joints.HipLeft, Joints.HipRight));
        final Point3D shoulderDir = getDirection(posture, new JointPair(Joints.ShoulderLeft, Joints.ShoulderRight));
        return projectOrthogonal(hipDir.add(shoulderDir.multiply(0.5)), up).normalize();
    }

    public static final Point3D postureRightDirection(final TrackedPosture3DFloat posture, final boolean relative) {
        return postureRightDirection(posture, postureUpDirection(posture, relative));
    }

    public static final Point3D postureFrontDirection(final Point3D up, final Point3D right) {
        return up.crossProduct(right);
    }

    public static final Point3D postureFrontDirection(final TrackedPosture3DFloat posture, final Point3D up) {
        return postureFrontDirection(up, postureRightDirection(posture, up));
    }

    public static final Point3D postureFrontDirection(final TrackedPosture3DFloat posture, final boolean relative) {
        return postureFrontDirection(posture, postureUpDirection(posture, relative));
    }

    //================================================================================
    // Angle calculations
    //================================================================================
    public static final double getElbowAngle(final TrackedPosture3DFloat posture, final boolean right) {
        final JointPair pair1 = right ? new JointPair(Joints.ElbowRight, Joints.ShoulderRight) : new JointPair(Joints.ElbowLeft, Joints.ShoulderLeft);
        final JointPair pair2 = right ? new JointPair(Joints.ElbowRight, Joints.HandRight) : new JointPair(Joints.ElbowLeft, Joints.HandLeft);
        return getDirection(posture, pair1).angle(getDirection(posture, pair2));
    }

    public static final double getHandHeightAngle(final TrackedPosture3DFloat posture, final boolean right, final boolean relative) {
        final JointPair armJoints = right ? new JointPair(Joints.ShoulderRight, Joints.HandRight) : new JointPair(Joints.ShoulderLeft, Joints.HandLeft);
        final Point3D direction = getDirection(posture, armJoints);
        final Point3D up = postureUpDirection(posture, relative);
        return up.angle(direction);
    }

    public static final double getElbowHeightAngle(final TrackedPosture3DFloat posture, final boolean right, final boolean relative) {
        final JointPair armJoints = right ? new JointPair(Joints.ShoulderRight, Joints.ElbowRight) : new JointPair(Joints.ShoulderLeft, Joints.ElbowLeft);
        final Point3D direction = getDirection(posture, armJoints);
        final Point3D up = postureUpDirection(posture, relative);
        return up.angle(direction);
    }

    public static final double getSignedHorizontalAngle(final TrackedPosture3DFloat posture, final boolean right, final boolean relative) {
        final Joints handJoint = right ? Joints.HandRight : Joints.HandLeft;
        final Point3D up = postureUpDirection(posture, relative);
        final Point3D front = postureFrontDirection(posture, up);
        final Point3D direction = projectOrthogonal(getDirection(posture, new JointPair(Joints.SpineMid, handJoint)), up);
        return getSignedAngle(front, direction, up);
    }
}
