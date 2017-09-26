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
import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import org.openbase.bco.psc.re.jp.JPDurationLookback;
import org.openbase.bco.psc.re.jp.JPDurationMaximalAngle;
import org.openbase.bco.psc.re.jp.JPDurationProbabilityThreshold;
import org.openbase.bco.psc.re.jp.JPDurationReductionFactor;
import static org.openbase.bco.psc.re.pointing.ArmPostureExtractor.pointingProbability;
import org.openbase.bco.psc.re.pointing.selectors.RaySelectorInterface;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.LoggerFactory;
import rst.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * Calculates a probability of a pointing gesture similar to the ArmPostureExtractor but using several thresholds on past posture data.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PostureHistoryExtractor extends AbstractRayExtractor {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PostureHistoryExtractor.class);

    /**
     * Duration that is considered when calculating the probability increase.
     */
    private final long lookback;
    /**
     * Minimal base probability required for the probability increase to become active.
     */
    private final double probabilityThreshold;
    /**
     * Maximal angle deviation from current pointing direction allowed for a probability increase.
     */
    private final double maxAngle;
    /**
     * The factor that the base probability is reduced with outside the thresholds.
     */
    private final double reductionFactor;
    /**
     * The additional factor that can be added, if the pointing gesture lasts as long as lookback.
     */
    private final double reductionRange;

    /**
     * History of important stats on the past tracked postures.
     */
    private final LinkedList<PostureHistory> postureHistory = new LinkedList<>();

    /**
     * Constructor.
     *
     * @param raySelector The ray selector that is used to select the correct rays.
     * @throws JPNotAvailableException is thrown, if the necessary JavaProperties are not available.
     */
    public PostureHistoryExtractor(final RaySelectorInterface raySelector) throws JPNotAvailableException {
        super(raySelector);
        this.lookback = JPService.getProperty(JPDurationLookback.class).getValue();
        LOGGER.info("Selected lookback time: " + lookback + " ms");
        this.probabilityThreshold = JPService.getProperty(JPDurationProbabilityThreshold.class).getValue();
        LOGGER.info("Selected probability threshold: " + probabilityThreshold);
        this.maxAngle = JPService.getProperty(JPDurationMaximalAngle.class).getValue();
        LOGGER.info("Selected maximal angle: " + maxAngle);
        this.reductionFactor = JPService.getProperty(JPDurationReductionFactor.class).getValue();
        this.reductionRange = 1.0 - reductionFactor;
        LOGGER.info("Selected reduction factor: " + reductionFactor + ", resulting reduction range: " + reductionRange);
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
            postureHistory.addLast(new PostureHistory(lookback));
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
                final double durationFactorRight = ((double) Long.min(lookback, postureHistoryList.getDuration(probabilityThreshold, maxAngle, true))) / lookback * reductionRange + reductionFactor;
                final double durationFactorLeft = ((double) Long.min(lookback, postureHistoryList.getDuration(probabilityThreshold, maxAngle, false))) / lookback * reductionRange + reductionFactor;
                pointingRays.addAll(getRays(postureHistoryList.getLastPosture(), durationFactorLeft * postureHistoryList.getLastProbability(false), durationFactorRight * postureHistoryList.getLastProbability(true)));
            }
        }
        return pointingRays;
    }
}
