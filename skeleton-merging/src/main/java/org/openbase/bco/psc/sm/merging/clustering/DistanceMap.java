package org.openbase.bco.psc.sm.merging.clustering;

/*-
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.util.Pair;
import org.openbase.bco.psc.sm.merging.PostureFrame;
import org.openbase.bco.psc.sm.merging.Skeleton3D;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class DistanceMap {

    private final Double[][] distances;
    private final List<Pair<String, Integer>> orderedIndices;
    private final HashMap<String, List<Integer>> indexMap = new HashMap<>();

    public DistanceMap(HashMap<String, PostureFrame> inputFrames) {
        AtomicInteger index = new AtomicInteger();
        orderedIndices = inputFrames.entrySet().stream()
                .map(e -> {
                    indexMap.put(e.getKey(), new ArrayList<>());
                    return IntStream.range(0, e.getValue().getSkeletons().size())
                            .mapToObj(i -> {
                                indexMap.get(e.getKey()).add(index.getAndIncrement());
                                return new Pair<>(e.getKey(), i);
                            })
                            .collect(Collectors.toList());
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());
        distances = new Double[orderedIndices.size()][orderedIndices.size()];
        for (int i = 0; i < orderedIndices.size(); i++) {
            for (int j = i + 1; j < orderedIndices.size(); j++) {
                final Pair<String, Integer> index1 = orderedIndices.get(i);
                final Pair<String, Integer> index2 = orderedIndices.get(j);
                final Skeleton3D skeleton1 = inputFrames.get(index1.getKey()).getSkeletons().get(index1.getValue());
                final Skeleton3D skeleton2 = inputFrames.get(index2.getKey()).getSkeletons().get(index2.getValue());
                if (index1.getKey().equals(index2.getKey()) || skeleton1.isEmpty() || skeleton2.isEmpty()) {
                    distances[i][j] = Double.MAX_VALUE;
                } else {
                    distances[i][j] = skeleton1.distance(skeleton2);
                }
                distances[j][i] = distances[i][j];
            }
            distances[i][i] = 0.0;
        }
    }

}
