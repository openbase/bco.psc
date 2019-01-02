package org.openbase.bco.psc.sm.merging.stabilizing;

/*
 * -
 * #%L
 * BCO PSC Skeleton Merging
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
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import org.openbase.bco.psc.sm.merging.MergingHistory;
import org.openbase.bco.psc.sm.merging.Skeleton3D;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class StabilizerImpl implements Stabilizer {

    private final double stabilizationFactor;
    private final double remainingFactor;

    public StabilizerImpl(final double stabilizationFactor) {
        this.stabilizationFactor = stabilizationFactor;
        remainingFactor = 1.0 - stabilizationFactor;
    }

    @Override
    public List<Skeleton3D> stabilize(final List<Skeleton3D> mergedPostures, final MergingHistory history) {
        final ListIterator<Skeleton3D> oldIterator = history.getLastResult().getSkeletons().listIterator();

        return mergedPostures.stream()
                .map(s -> {
                    if (oldIterator.hasNext()) {
                        final Skeleton3D old = oldIterator.next();
                        if (old.isEmpty()) {
                            return s;
                        }

                        // TODO: test this agains the next:
//                        ListIterator<Joint3D> oldSkeletonIter = old.listIterator();
//                        return new Skeleton3D(s.stream()
//                                .map(j -> {
//                                    final Joint3D j2 = oldSkeletonIter.next();
//                                    return new Joint3D(j.getPosition().multiply(remainingFactor).add(j2.getPosition().multiply(stabilizationFactor)), j.getConfidence());
//                                })
//                                .collect(Collectors.toList())
//                        );
                        return s.multiply(remainingFactor).addPositions(old.multiply(stabilizationFactor));
                    }
                    return s;
                })
                .collect(Collectors.toList());
    }
}
