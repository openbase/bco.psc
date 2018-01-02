package org.openbase.bco.psc.re.pointing.selectors;

/*-
 * #%L
 * BCO PSC Ray Extractor
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

import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import org.openbase.bco.psc.lib.pointing.JointPair;
import org.openbase.bco.psc.lib.pointing.Joints;
import rst.tracking.PointingRay3DFloatDistributionType;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat.PointingType;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PolynomialOrNeckSelector5 implements RaySelectorInterface {
    private final PolynomialSelectorDegree5 polySelector = new PolynomialSelectorDegree5();
    private static final double MIN_NECK_ANGLE = 65;
    private static final double MAX_NECK_ANGLE = 114;

    @Override
    public PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution getRays(TrackedPosture3DFloat posture, boolean right, double pointingProbability) {
        double handHeightAngle = getHandHeightAngle(posture, right, false);
        if(MIN_NECK_ANGLE < handHeightAngle && handHeightAngle < MAX_NECK_ANGLE){
            JointPair jointPair = new JointPair(Joints.Neck, right ? Joints.HandRight: Joints.HandLeft);
            return PointingRay3DFloatDistribution.newBuilder().addRay(PointingRay3DFloat.newBuilder()
                    .setType(PointingType.OTHER)
                    .setRightHandPointing(right)
                    .setCertainty((float)pointingProbability)
                    .setRay(getRay(posture, jointPair))
            ).build();
        }
        return polySelector.getRays(posture, right, pointingProbability);
    }
}
