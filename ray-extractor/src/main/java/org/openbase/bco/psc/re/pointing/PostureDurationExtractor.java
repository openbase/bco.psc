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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import static org.openbase.bco.psc.re.pointing.ArmPostureExtractor.pointingProbability;
import org.openbase.bco.psc.re.pointing.selectors.RaySelectorInterface;
import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import org.openbase.jul.exception.NotAvailableException;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * Calculates a probability of a pointing gesture similar to the ArmPostureExtractor but using several thresholds on past posture data.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PostureDurationExtractor extends AbstractRayExtractor {

    private final static long LOOKBACK = 2000;
    private final static double PROBABILITY_THRESHOLD = 0.8;
    private final static double MAX_ANGLE = 30.0;
    private final static double DEFAULT_FACTOR = 0.6;
    private final static double VARIANCE = 1.0 - DEFAULT_FACTOR;

    /**
     * History of important stats on the past tracked postures.
     */
    private final LinkedList<PostureHistory> postureHistory = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param raySelector The ray selector that is used to select the correct rays.
     */
    public PostureDurationExtractor(final RaySelectorInterface raySelector) {
        super(raySelector);
    }

    /**
     * {@inheritDoc}
     *
     * @param postures {@inheritDoc}
     */
    @Override
    public synchronized void updatePostures(final TrackedPostures3DFloat postures) {
        final int postureCount = postures.getPostureCount();
        while (postureCount > postureHistory.size()) {
            postureHistory.addLast(new PostureHistory(LOOKBACK));
        }
        while (postureHistory.size() > postureCount) {
            postureHistory.removeLast();
        }
        final long timestamp = System.currentTimeMillis();
        ListIterator<PostureHistory> it = postureHistory.listIterator();
        while (it.hasNext()) {
            final TrackedPosture3DFloat posture = postures.getPosture(it.nextIndex());
            if (checkPosture(posture)) {
                it.next().update(timestamp, posture, pointingProbability(posture, true), pointingProbability(posture, false));
            } else {
                it.next().clear();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public synchronized List<PointingRay3DFloatDistribution> getPointingRays() throws NotAvailableException {
        List<PointingRay3DFloatDistribution> pointingRays = new ArrayList<>();
        for (PostureHistory postureHistoryList : postureHistory) {
            if (!postureHistoryList.isEmpty()) {
                final double durationFactorRight = ((double) Long.min(LOOKBACK, postureHistoryList.getDuration(PROBABILITY_THRESHOLD, MAX_ANGLE, true))) / LOOKBACK * VARIANCE + DEFAULT_FACTOR;
                final double durationFactorLeft = ((double) Long.min(LOOKBACK, postureHistoryList.getDuration(PROBABILITY_THRESHOLD, MAX_ANGLE, false))) / LOOKBACK * VARIANCE + DEFAULT_FACTOR;
                pointingRays.addAll(getRays(postureHistoryList.getLastPosture(), durationFactorLeft * postureHistoryList.getLastProbability(false), durationFactorRight * postureHistoryList.getLastProbability(true)));
            }
        }
        return pointingRays;
    }
}
