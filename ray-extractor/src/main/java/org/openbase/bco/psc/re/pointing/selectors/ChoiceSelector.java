package org.openbase.bco.psc.re.pointing.selectors;

/*
 * -
 * #%L
 * BCO PSC Ray Extractor
 * %%
 * Copyright (C) 2016 - 2021 openbase.org
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
import java.util.List;
import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import org.openbase.type.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import org.openbase.type.tracking.PointingRay3DFloatType.PointingRay3DFloat.PointingType;
import org.openbase.type.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ChoiceSelector implements RaySelectorInterface {

    private final List<PointingType> choice;

    public ChoiceSelector(List<PointingType> choice) {
        this.choice = choice;
    }

    public ChoiceSelector() {
        this(null);
    }

    @Override
    public PointingRay3DFloatDistribution getRays(TrackedPosture3DFloat posture, boolean right, double pointingProbability) {
        if (choice == null) {
            return PointingRay3DFloatDistribution.newBuilder().addAllRay(getAllRaysForSideWithConfidence(posture, right, pointingProbability)).build();
        }
        return PointingRay3DFloatDistribution.newBuilder().addAllRay(getAllRaysForSideAndTypesWithConfidence(posture, right, pointingProbability, choice)).build();
    }

}
