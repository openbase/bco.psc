package org.openbase.bco.psc.re.pointing.selectors;

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
import org.openbase.bco.psc.lib.pointing.Joints;
import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import rst.geometry.Ray3DFloatType;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat.PointingType;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class AbstractPolynomialSelector implements RaySelectorInterface {

    @Override
    public PointingRay3DFloatDistribution getRays(TrackedPosture3DFloat posture, boolean right, double pointingProbability) {
        double handHeightAngle = getHandHeightAngle(posture, right, false);
        Point3D hand = getPoint3D(posture, right ? Joints.HandRight : Joints.HandLeft);
        Point3D spineShoulder = getPoint3D(posture, Joints.SpineShoulder);
        Point3D spineHeadDirection = getPoint3D(posture, Joints.Head).subtract(spineShoulder);
        double factor = 0;
        final double[] parameters = getParameters();
        for (int i = 0; i < parameters.length; i++) {
            factor += Math.pow(handHeightAngle, parameters.length - i - 1) * parameters[i];
        }
        Point3D start = spineShoulder.add(spineHeadDirection.multiply(factor));
        return PointingRay3DFloatDistribution.newBuilder().addRay(PointingRay3DFloat.newBuilder()
                .setType(PointingType.OTHER)
                .setRightHandPointing(right)
                .setCertainty((float) pointingProbability)
                .setRay(Ray3DFloatType.Ray3DFloat.newBuilder()
                        .setOrigin(toVec3DFloat(hand))
                        .setDirection(toVec3DFloat(hand.subtract(start).normalize()))
                )
        ).build();
    }

    protected abstract double[] getParameters();
}
