package org.openbase.bco.psc.sm.merging;

/*
 * -
 * #%L
 * BCO PSC Skeleton Merging
 * %%
 * Copyright (C) 2016 - 2020 openbase.org
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.geometry.Point3D;
import org.openbase.bco.psc.lib.pointing.Joints;
import org.openbase.bco.psc.sm.merging.stabilizing.Stabilizer;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SkeletonMerger implements SkeletonMergerInterface {

    private static final long FRAME_DURATION = 100;
    private static final double MIN_DISTANCE = 0.5;

    private final Stabilizer stabilizer;
    private final HashMap<String, PostureFrame> inputFrames = new HashMap<>();
    private MergingHistory history;
    final AtomicInteger lastLostCounter = new AtomicInteger();
    final AtomicInteger lastAddCounter = new AtomicInteger();

    public SkeletonMerger(final Stabilizer stabilizer) {
        this.stabilizer = stabilizer;
    }

    @Override
    public synchronized void postureUpdate(final PostureFrame postureFrame) {
        inputFrames.put(postureFrame.getKey(), postureFrame);
    }

    @Override
    public synchronized TrackedPostures3DFloat createMergedData() {
        final long currentTime = System.currentTimeMillis();
        lastLostCounter.set(0);
        lastAddCounter.set(0);

        // Remove too old frames
        inputFrames.entrySet().removeIf(e -> e.getValue().getAge(currentTime) > FRAME_DURATION);

        //TODO: Clustering approach where all distances are calculated first and then merging takes place in ascending order (Would probably be more accurate)?! Can use DistanceMap for that.
        // Create groups that belong to a single person based on previous groups
        final List<HashMap<String, Integer>> postureCollection = collectGroups();

        // Merging the collected posture groups to single postures.
        final List<Skeleton3D> mergedPostures = postureCollection.stream()
                .map(
                        map -> mergePostures(map.entrySet().stream()
                                .map(entry -> inputFrames.get(entry.getKey()).getSkeletons().get(entry.getValue()))
                                .collect(Collectors.toList()))
                )
                .collect(Collectors.toList());

        // Applying a stabilization on the postures.
        List<Skeleton3D> stabilizedPostures = stabilizer.stabilize(mergedPostures, history);

        // Creating the new history object.
        history = new MergingHistory(new PostureFrame(currentTime, stabilizedPostures), (HashMap<String, PostureFrame>) inputFrames.clone(), postureCollection);
        return history.getLastResult().getPostures();
    }

    private List<HashMap<String, Integer>> collectGroups() {
        final List<HashMap<String, Integer>> postureCollection = history != null ? new ArrayList(history.getConnections()) : new ArrayList<>();
        final ListIterator<HashMap<String, Integer>> historyIterator = postureCollection.listIterator();
        while (historyIterator.hasNext()) {
            final HashMap<String, Integer> map = historyIterator.next();
            if (!map.isEmpty()) {
                map.entrySet().removeIf(entry -> !inputFrames.containsKey(entry.getKey()) || inputFrames.get(entry.getKey()).getSkeletons().get(entry.getValue()).isEmpty());
                if (map.isEmpty()) {
                    lastLostCounter.getAndIncrement();
                }
            }
            //TODO: remove empty ones in the back...
        }

        //TODO: Maybe check again if the old connections are still valid for the new data?! Maybe distances in groups too high?!
        final AtomicInteger firstEmpty = new AtomicInteger();
        nextEmpty(firstEmpty, postureCollection);

        // Collect skeletons with low distance in groups.
        inputFrames.entrySet().stream().forEach(entry -> {
            final ListIterator<Skeleton3D> postureIterator = entry.getValue().getSkeletons().listIterator();
            while (postureIterator.hasNext()) {
                final int postureIndex = postureIterator.nextIndex();
                final Skeleton3D posture = postureIterator.next();
                if (!posture.isEmpty()) {
                    if (placeInList(entry.getKey(), postureIndex, posture, postureCollection, firstEmpty)) {
                        lastAddCounter.getAndIncrement();
                    }
//                        placeInList2(entry.getKey(), index, p, firstCollection);
                }
            }
        });
        return postureCollection;
    }

    private Skeleton3D mergePostures(final List<Skeleton3D> postures) {
        // Use the posture with the highest confidence as base.
        Collections.sort(postures, (s1, s2) -> Double.compare(s2.getTotalConfidence(), s1.getTotalConfidence()));
        final Skeleton3D basePosture = postures.get(0);

        final Point3D[] points = new Point3D[Joints.values().length];
        Arrays.fill(points, Point3D.ZERO);
        final double[] totalConf = new double[Joints.values().length];
        Arrays.fill(totalConf, 0.0);
        final double[] maxConf = new double[Joints.values().length];
        Arrays.fill(maxConf, 0.0);

        final AtomicInteger mirrorCount = new AtomicInteger();
        // TODO: This needs to be done in a better way:
        postures.forEach((p) -> {
            // If the distance for the mirrored posture is smaller, use the mirrored one.
            final boolean mirrored = basePosture.jointDistance(p, true) < basePosture.jointDistance(p, false);
            if (mirrored) {
                mirrorCount.getAndIncrement();
            }
            for (Joints joint : Joints.values()) {
                final int jointIndex = joint.getValue();
                final Joint3D currentJoint = p.get(joint, mirrored);
                final double confidence = currentJoint.getConfidence() == 1.0 ? 1.0 : 0.25;
                points[jointIndex] = points[jointIndex].add(currentJoint.getPosition().multiply(confidence));
                totalConf[jointIndex] += confidence;
                maxConf[jointIndex] = Double.max(maxConf[jointIndex], confidence);
            }
        });

        // TODO: So does this:
        final Skeleton3D skeleton = new Skeleton3D();
        for (int i = 0; i < points.length; i++) {
            skeleton.add(new Joint3D(points[i].multiply(1.0 / totalConf[i]), maxConf[i]));
        }

        // Mirror the skeleton if most of the input would suggest it.
        if (mirrorCount.get() > postures.size() / 2) {
            return skeleton.mirrored();
        }
        return skeleton;
    }

    private <K, V> void nextEmpty(final AtomicInteger emptyIndex, final List<HashMap<K, V>> list) {
        while (emptyIndex.get() < list.size() && !list.get(emptyIndex.get()).isEmpty()) {
            emptyIndex.getAndIncrement();
        }
    }

    private boolean placeInList(final String inputKey, final int postureIndex, final Skeleton3D posture, final List<HashMap<String, Integer>> list, final AtomicInteger firstEmpty) {
        final ListIterator<HashMap<String, Integer>> listIterator = list.listIterator();
        double minDistance = Double.MAX_VALUE;
        int minIndex = -1;
        while (listIterator.hasNext()) {
            final int comparisonIndex = listIterator.nextIndex();
            final HashMap<String, Integer> next = listIterator.next();
            if (next.containsKey(inputKey)) {
                if (next.get(inputKey) == postureIndex) {
                    return false;
                } else {
                    //TODO: What if the new distance is smaller?!
                    continue;
                }
            }
            final double distance = maxDistance(posture, next);
            if (distance < minDistance) {
                minDistance = distance;
                minIndex = comparisonIndex;
            }
        }
        if (minDistance < MIN_DISTANCE) {
            list.get(minIndex).put(inputKey, postureIndex);
            return false;
        } else {
            if (firstEmpty.get() < list.size()) {
                list.get(firstEmpty.getAndIncrement()).put(inputKey, postureIndex);
            } else {
                final HashMap<String, Integer> newMap = new HashMap<>();
                newMap.put(inputKey, postureIndex);
                list.add(newMap);
            }
            nextEmpty(firstEmpty, list);
            return true;
        }
    }

    private double maxDistance(final Skeleton3D posture, final HashMap<String, Integer> indexMap) {
        return indexMap.entrySet().stream()
                .mapToDouble(e -> posture.distance(inputFrames.get(e.getKey()).getSkeletons().get(e.getValue())))
                .max()
                .orElse(Double.MAX_VALUE);
    }
}
