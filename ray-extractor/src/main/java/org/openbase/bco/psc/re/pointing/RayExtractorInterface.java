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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.List;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public interface RayExtractorInterface {

    //TODO: 1. Add implementation that checks the other arm (whole posture) aswell
    // 2. Also implementation that tracks the movement over time and increases probability.
    // 3. An implementation that consideres the arm trajectory instead of fixed angles.
    //================================================================================
    // Abstract methods
    //================================================================================
    public void updatePostures(TrackedPostures3DFloat postures);

    public List<PointingRay3DFloatDistribution> getPointingRays();
}
