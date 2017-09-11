package org.openbase.bco.psc.re.pointing;

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

import org.openbase.bco.psc.re.pointing.selectors.RaySelectorInterface;
import static org.openbase.bco.psc.re.utils.PostureFunctions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.distribution.NormalDistribution;
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ArmPostureExtractor implements RayExtractorInterface {
    private final RaySelectorInterface selector;
    private TrackedPostures3DFloat lastPostures;
    
    public ArmPostureExtractor(RaySelectorInterface selector){
        this.selector = selector;
    }

    @Override
    public synchronized void updatePostures(TrackedPostures3DFloat postures) {
        lastPostures = postures;
    }

    @Override
    public synchronized List<PointingRay3DFloatDistribution> getPointingRays() {
//        for(int i = 0; i < lastPostures.getPostureCount(); i++){
//            if(checkPosture(lastPostures.getPosture(i))){
//                System.out.println("Posture " + i + ", right prob: " + pointingProbability(lastPostures.getPosture(i), true));
//                System.out.println("Posture " + i + ", left prob: " + pointingProbability(lastPostures.getPosture(i), false));
//            }
//        }
        return lastPostures.getPostureList().stream()
                        .filter(posture -> checkPosture(posture))
                        .map(posture -> getRays(posture))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
    }
    
    private List<PointingRay3DFloatDistribution> getRays(TrackedPosture3DFloat posture){
        List<PointingRay3DFloatDistribution> tempList = new ArrayList<>();
        PointingRay3DFloatDistribution rightRays = selector.getRays(posture, true, pointingProbability(posture, true));
        if(rightRays.getRayCount() > 0){
            tempList.add(rightRays);
        }
        PointingRay3DFloatDistribution leftRays = selector.getRays(posture, false, pointingProbability(posture, false));
        if(leftRays.getRayCount() > 0){
            tempList.add(leftRays);
        }
        return tempList;
        //TODO: Differentiate here later (both belong to the same posture). (Maybe just use one!)
    }
    
    private double pointingProbability(TrackedPosture3DFloat posture, boolean right) {
        //Recommended threshold: 0.3/0.4
        double elbowAngle = getElbowAngle(posture, right);
//        System.out.println("elbowAngle: " + elbowAngle);
        double handHeightAngle = getHandHeightAngle(posture, right, false);
//        System.out.println("handHeightAngle: " + handHeightAngle);
        double heightFactor = 0.5 + 0.5*Math.tanh((140-handHeightAngle)/20);
        double expectedElbowAngle = handHeightAngle >= 60 ? 180: (handHeightAngle - 20)*0.75 + 150;
        double extension_factor = (new NormalDistribution(expectedElbowAngle, 40)).density(elbowAngle)*100;
        return Math.min(heightFactor*extension_factor, 1.0);
    }
    
    // Higher AUC (0.945 instead of 0.928), but only works well for very low thresholds.
    private double pointingProbabilityHigherAUC(TrackedPosture3DFloat posture, boolean right) {
        double elbowAngle = getElbowAngle(posture, right);
        double handHeightAngle = getHandHeightAngle(posture, right, true);
        double heightFactor = 0.5 + 0.5*Math.tanh((107-handHeightAngle)/18);
        double expectedElbowAngle = handHeightAngle >= 80 ? 180: (handHeightAngle - 4)*30/76 + 150;
        double extension_factor = (new NormalDistribution(expectedElbowAngle, 40)).density(elbowAngle)*100;
        return Math.min(heightFactor*extension_factor, 1.0);
    }
}
