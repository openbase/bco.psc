package org.openbase.bco.psc.re.pointing.selectors;

/*-
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.PointingRay3DFloatType.PointingRay3DFloat.PointingType;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class DistributedSelector implements RaySelectorInterface {
    @Override
    public PointingRay3DFloatDistribution getRays(TrackedPosture3DFloat posture, boolean right, double pointingProbability) {
        double handHeightAngle = getHandHeightAngle(posture, right, false);
        return PointingRay3DFloatDistribution.newBuilder()
                .addRay(getPointingRay(posture, right, pointingProbability*getShoulderHandProb(handHeightAngle), PointingType.SHOULDER_HAND))
                .addRay(getPointingRay(posture, right, pointingProbability*getHeadHandProb(handHeightAngle), PointingType.HEAD_HAND))
                .addRay(getPointingRay(posture, right, pointingProbability*getElbowHandProb(handHeightAngle), PointingType.FOREARM))
                .addRay(getPointingRay(posture, right, pointingProbability*getHandTipProb(handHeightAngle), PointingType.HAND)).build();
    }
    
    private double getShoulderHandProb(double handHeightAngle){
        return handHeightAngle < 115 ? -0.005*(handHeightAngle)+0.775 : 0.2;
    }
    
    private double getHeadHandProb(double handHeightAngle){
        return handHeightAngle < 115 ? 0.005*(handHeightAngle)+0.075 : -0.2/30*(handHeightAngle-115)+0.65;
    }
    
    private double getElbowHandProb(double handHeightAngle){
        return handHeightAngle < 115 ? -0.001*(handHeightAngle-25)+0.15 : 0.12/30*(handHeightAngle-115)+0.06;
    }
    
    private double getHandTipProb(double handHeightAngle){
        return handHeightAngle < 115 ? 0.001*(handHeightAngle-25)+0 : 0.08/30*(handHeightAngle-115)+0.09;
    }
}
