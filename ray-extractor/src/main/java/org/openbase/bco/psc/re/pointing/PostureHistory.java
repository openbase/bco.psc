package org.openbase.bco.psc.re.pointing;

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
import java.util.LinkedList;
import java.util.ListIterator;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.type.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;

/**
 * This class handles the history of a single posture up to a certain duration.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PostureHistory {

    /**
     * Duration that is kept for future evaluation in milliseconds.
     */
    private final long lookback;
    /**
     * The internal history of postures.
     */
    private final LinkedList<PostureHistoryEntry> history = new LinkedList<>();
    /**
     * The last tracked instance of the posture.
     */
    private TrackedPosture3DFloat lastPosture;

    /**
     * Constructor.
     *
     * @param lookback duration of posture history that is kept for future evaluation in milliseconds.
     */
    public PostureHistory(final long lookback) {
        this.lookback = lookback;
    }

    /**
     * Adds the entry to the internal posture history.
     *
     * @param entry entry to be added.
     */
    private void add(final PostureHistoryEntry entry) {
        history.addFirst(entry);
        while (history.size() > 1 && history.getFirst().getTimestamp() - history.get(history.size() - 1).getTimestamp() > lookback) {
            history.removeLast();
        }
    }

    /**
     * Updates the history with new posture data.
     *
     * @param timestamp the time of the current observation of the posture.
     * @param posture the current instance of the posture.
     * @param pointingProbabilityRight the base probability of a pointing gesture using the right arm.
     * @param pointingProbabilityLeft the base probability of a pointing gesture using the left arm.
     */
    public void update(final long timestamp, final TrackedPosture3DFloat posture, final double pointingProbabilityRight, final double pointingProbabilityLeft) {
        this.lastPosture = posture;
        add(new PostureHistoryEntry(timestamp, posture, pointingProbabilityRight, pointingProbabilityLeft));
    }

    /**
     * Clears all the history data including the last posture.
     */
    public void clear() {
        history.clear();
        lastPosture = null;
    }

    /**
     * Gets the last base probability of a pointing gesture using the given arm.
     *
     * @param right if true, the base probability of a pointing gesture using the <b>right</b> arm is returned.
     * @return the last base probability of a pointing gesture using the given arm.
     * @throws NotAvailableException is thrown, if the history has not been updated since the last call of <code>clear()</code>.
     */
    public double getLastProbability(final boolean right) throws NotAvailableException {
        if (history.isEmpty()) {
            throw new NotAvailableException("Last probability");
        }
        return history.getFirst().getProbability(right);
    }

    /**
     * Gets the last instance of the tracked posture.
     *
     * @return the last observed instance of the posture.
     * @throws NotAvailableException is thrown, if the history has not been updated since the last call of <code>clear()</code>.
     */
    public TrackedPosture3DFloat getLastPosture() throws NotAvailableException {
        if (lastPosture == null) {
            throw new NotAvailableException("Last posture");
        }
        return lastPosture;
    }

    /**
     * Gets the maximal duration in the lookback period during which the given thresholds hold for the given arm.
     *
     * @param probabilityThreshold minimal probability that should occur during the duration.
     * @param maxAngle maximal divergence in the direction angle, that can occur compared to the last posture.
     * @param right if true, the right arm is used, else the left one.
     * @return The maximal duration in the lookback period during which the given thresholds hold for the given arm.
     * @throws NotAvailableException is thrown, if the history has not been updated since the last call of <code>clear()</code>.
     */
    public long getDuration(final double probabilityThreshold, final double maxAngle, final boolean right) throws NotAvailableException {
        if (history.isEmpty()) {
            throw new NotAvailableException("Duration", new CouldNotPerformException("getDuration called on empty history."));
        }
        long currentTs = history.getFirst().getTimestamp();
        final ListIterator<PostureHistoryEntry> it = history.listIterator();
        while (it.hasNext()) {
            PostureHistoryEntry entry = it.next();
            if (entry.getProbability(right) < probabilityThreshold || entry.directionAngle(history.getFirst(), right) > maxAngle) {
                break;
            }
            currentTs = entry.getTimestamp();
        }
        return history.getFirst().getTimestamp() - currentTs;
    }

    /**
     * Returns whether the history is empty.
     *
     * @return true, if the internal history is empty.
     */
    public boolean isEmpty() {
        return history.isEmpty();
    }
}
