package org.openbase.bco.psc.re.pointing;

/*
 * -
 * #%L
 * BCO PSC Ray Extractor
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import javafx.geometry.Point3D;
import org.openbase.bco.psc.lib.pointing.JointPair;
import org.openbase.bco.psc.lib.pointing.Joints;
import org.openbase.bco.psc.re.utils.PostureFunctions;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;

/**
 * This class represents a single observation of a tracked posture and keeps the most relevant data for future evaluation of the posture history.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PostureHistoryEntry {

    /**
     * The timestamp of the posture being received.
     */
    final long timestamp;
    /**
     * The base probability of a pointing gesture using the right arm.
     */
    final double probabilityRight;
    /**
     * The base probability of a pointing gesture using the right arm.
     */
    final double probabilityLeft;
//    final Point3D head;
//
//    final Point3D shoulderRight;
//    final Point3D shoulderLeft;
//    final Point3D elbowRight;
//    final Point3D elbowLeft;
//    final Point3D handRight;
//    final Point3D handLeft;

    /**
     * The shoulder-hand direction of the right arm.
     */
    final Point3D directionRight;
    /**
     * The shoulder-hand direction of the left arm.
     */
    final Point3D directionLeft;

    /**
     * Constructor.
     *
     * @param timestamp time of observation of the posture.
     * @param posture the tracked posture.
     * @param pointingProbabilityRight the base probability of a pointing gesture using the right arm.
     * @param pointingProbabilityLeft the base probability of a pointing gesture using the left arm.
     */
    PostureHistoryEntry(final long timestamp, final TrackedPosture3DFloat posture, final double pointingProbabilityRight, final double pointingProbabilityLeft) {
        this.timestamp = timestamp;
        this.probabilityRight = pointingProbabilityRight;
        this.probabilityLeft = pointingProbabilityLeft;
//        this.head = PostureFunctions.getPoint3D(posture, Joints.Head);
//        this.shoulderRight = PostureFunctions.getPoint3D(posture, Joints.ShoulderRight);
//        this.shoulderLeft = PostureFunctions.getPoint3D(posture, Joints.ShoulderLeft);
//        this.elbowRight = PostureFunctions.getPoint3D(posture, Joints.ElbowRight);
//        this.elbowLeft = PostureFunctions.getPoint3D(posture, Joints.ElbowLeft);
//        this.handRight = PostureFunctions.getPoint3D(posture, Joints.HandRight);
//        this.handLeft = PostureFunctions.getPoint3D(posture, Joints.HandLeft);
        this.directionRight = PostureFunctions.getDirection(posture, new JointPair(Joints.ShoulderRight, Joints.HandRight));
        this.directionLeft = PostureFunctions.getDirection(posture, new JointPair(Joints.ShoulderLeft, Joints.HandLeft));
    }

    /**
     * Gets the timestamp of this posture history event.
     *
     * @return the timestamp.
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Gets the base probability of a pointing gesture using the given arm.
     *
     * @param right if true, the probability of the right arm is returned.
     * @return The probability of the given arm.
     */
    public double getProbability(boolean right) {
        if (right) {
            return probabilityRight;
        } else {
            return probabilityLeft;
        }
    }

//    public double maxDistance(PostureHistoryEntry other, boolean right) {
//        double distance = other.head.distance(head);
//        if (right) {
//            distance = Double.max(distance, other.shoulderRight.distance(shoulderRight));
//            distance = Double.max(distance, other.elbowRight.distance(elbowRight));
//            distance = Double.max(distance, other.handRight.distance(handRight));
//        } else {
//            distance = Double.max(distance, other.shoulderLeft.distance(shoulderLeft));
//            distance = Double.max(distance, other.elbowLeft.distance(elbowLeft));
//            distance = Double.max(distance, other.handLeft.distance(handLeft));
//        }
//        return distance;
//    }
    /**
     * Returns the angle between the shoulder-hand direction of this and the one of the other entry for the given arm.
     *
     * @param other The other entry to be compared to.
     * @param right if true, the direction of the right arm is compared.
     * @return The direction angle.
     */
    public double directionAngle(PostureHistoryEntry other, boolean right) {
        if (right) {
            return directionRight.angle(other.directionRight);
        } else {
            return directionLeft.angle(other.directionLeft);
        }
    }
}
