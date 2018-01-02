package org.openbase.bco.psc.re.pointing;

/*
 * -
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.openbase.bco.psc.re.pointing.selectors.RaySelectorInterface;
import org.openbase.bco.psc.lib.pointing.PostureFunctions;
import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import org.openbase.jul.exception.NotAvailableException;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * Calculates a probability of a pointing gesture based on a model using joint angles which was derived from empirical data.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ArmPostureExtractor extends AbstractRayExtractor {

    /**
     * The last postures provided in the updatePostures method.
     */
    private TrackedPostures3DFloat lastPostures;

    /**
     * Constructor.
     *
     * @param raySelector The ray selector that is used to select the correct rays.
     */
    public ArmPostureExtractor(RaySelectorInterface raySelector) {
        super(raySelector);
    }

    /**
     * {@inheritDoc}
     *
     * @param postures {@inheritDoc}
     */
    @Override
    public synchronized void updatePostures(TrackedPostures3DFloat postures) {
        lastPostures = postures;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public synchronized List<PointingRay3DFloatDistribution> getPointingRays() throws NotAvailableException {
        if (lastPostures == null) {
            throw new NotAvailableException("Pointing Rays");
        }
        return lastPostures.getPostureList().stream()
                .filter(posture -> checkPosture(posture))
                .map(posture -> getRays(posture, pointingProbability(posture, false), pointingProbability(posture, true)))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Returns the overall probability that the observed posture is performing a pointing the gesture on the specified side.
     *
     * @param posture the observed posture.
     * @param right true, if the arm for which the probability is calculated is the right (not left) one.
     * @return The probability of the tracked person pointing with the specified arm calculated based on empirical data.
     */
    public static double pointingProbability(final TrackedPosture3DFloat posture, final boolean right) {
        //Recommended threshold: 0.3/0.4
        final double elbowAngle = getElbowAngle(posture, right);
        final double handHeightAngle = getHandHeightAngle(posture, right, false);
        final double heightFactor = 0.5 + 0.5 * Math.tanh((140 - handHeightAngle) / 20);
        final double expectedElbowAngle = handHeightAngle >= 60 ? 180 : (handHeightAngle - 20) * 0.75 + 150;
        final double extension_factor = (new NormalDistribution(expectedElbowAngle, 40)).density(elbowAngle) * 100;
        return Math.min(heightFactor * extension_factor, 1.0) * PostureFunctions.postureConfidence(posture, right);
    }

    // Higher AUC (0.945 instead of 0.928), but only works well for very low thresholds.
    /**
     * Does the same as pointingProbability except using a different model that lead to a higher AUC for the training data, but does not offer thresholds of a similar feasability.
     *
     * @param posture the observed posture.
     * @param right true, if the arm for which the probability is calculated is the right (not left) one.
     * @return The probability of the tracked person pointing with the specified arm calculated based on empirical data.
     */
    private double pointingProbabilityHigherAUC(final TrackedPosture3DFloat posture, final boolean right) {
        final double elbowAngle = getElbowAngle(posture, right);
        final double handHeightAngle = getHandHeightAngle(posture, right, true);
        final double heightFactor = 0.5 + 0.5 * Math.tanh((107 - handHeightAngle) / 18);
        final double expectedElbowAngle = handHeightAngle >= 80 ? 180 : (handHeightAngle - 4) * 30 / 76 + 150;
        final double extension_factor = (new NormalDistribution(expectedElbowAngle, 40)).density(elbowAngle) * 100;
        return Math.min(heightFactor * extension_factor, 1.0) * PostureFunctions.postureConfidence(posture, right);
    }
}
