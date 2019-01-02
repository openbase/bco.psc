package org.openbase.bco.psc.re.pointing;

/*
 * #%L
 * BCO PSC Ray Extractor
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
import java.util.ArrayList;
import java.util.List;
import org.openbase.bco.psc.re.pointing.selectors.RaySelectorInterface;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.type.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import org.openbase.type.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * An interface for different implementations of ray extractors.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class AbstractRayExtractor {

    /**
     * The ray selector that is used to select the correct rays.
     */
    private final RaySelectorInterface raySelector;

    /**
     * Constructor.
     *
     * @param raySelector The ray selector that is used to select the correct rays.
     */
    public AbstractRayExtractor(final RaySelectorInterface raySelector) {
        this.raySelector = raySelector;
    }

    //TODO: 1. Maybe add implementation that checks the other arm (whole posture) as well
    // 2. An implementation that consideres the arm trajectory instead of fixed angles.
    //================================================================================
    // Abstract methods
    //================================================================================
    /**
     * Update the internal posture data.
     *
     * @param postures new posture data.
     */
    public abstract void updatePostures(final TrackedPostures3DFloat postures);

    /**
     * Returns the current pointing rays based on the posture data provided in the updatePostures method.
     *
     * @return current pointing rays.
     * @throws org.openbase.jul.exception.NotAvailableException is thrown, if the PointingRays are not available (if update Postures has not been called).
     */
    public abstract List<PointingRay3DFloatDistribution> getPointingRays() throws NotAvailableException;

    /**
     * Gets the pointing rays for both arms of a single posture with cummulated probabilities specified in the parameters.
     *
     * @param posture posture to get the pointing rays for.
     * @param probabilityLeft current probability of a pointing gesture with the left arm.
     * @param probabilityRight current probability of a pointing gesture with the right arm.
     * @return Pointing rays of the tracked person.
     */
    protected List<PointingRay3DFloatDistribution> getRays(final TrackedPosture3DFloat posture, final double probabilityLeft, final double probabilityRight) {
        final List<PointingRay3DFloatDistribution> tempList = new ArrayList<>();
        final PointingRay3DFloatDistribution rightRays = raySelector.getRays(posture, true, probabilityRight);
        if (rightRays.getRayCount() > 0) {
            tempList.add(rightRays);
        }
        final PointingRay3DFloatDistribution leftRays = raySelector.getRays(posture, false, probabilityLeft);
        if (leftRays.getRayCount() > 0) {
            tempList.add(leftRays);
        }
        return tempList;
    }
}
