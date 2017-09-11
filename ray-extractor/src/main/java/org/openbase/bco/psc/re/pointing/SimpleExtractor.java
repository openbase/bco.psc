package org.openbase.bco.psc.re.pointing;

/*
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
import rst.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SimpleExtractor implements RayExtractorInterface {
    private final RaySelectorInterface raySelector;
    private TrackedPostures3DFloat lastPostures;
    
    public SimpleExtractor(RaySelectorInterface raySelector){
        this.raySelector = raySelector;
    }
    
    @Override
    public synchronized void updatePostures(TrackedPostures3DFloat postures) {
        lastPostures = postures;
    }

    @Override
    public synchronized List<PointingRay3DFloatDistribution> getPointingRays() {
        return lastPostures.getPostureList().stream()
                        .filter(posture -> checkPosture(posture))
                        .map(posture -> getRays(posture))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());
    }
    
    private List<PointingRay3DFloatDistribution> getRays(TrackedPosture3DFloat posture){
        List<PointingRay3DFloatDistribution> tempList = new ArrayList<>();
        PointingRay3DFloatDistribution rightRays = raySelector.getRays(posture, true, 1);
        if(rightRays.getRayCount()> 0){
            tempList.add(rightRays);
        }
        PointingRay3DFloatDistribution leftRays = raySelector.getRays(posture, false, 1);
        if(leftRays.getRayCount()> 0){
            tempList.add(leftRays);
        }
        return tempList;
        //TODO: Differentiate here later (both belong to the same posture).
    }
}
