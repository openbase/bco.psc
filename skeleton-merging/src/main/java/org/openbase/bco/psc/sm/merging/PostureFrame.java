package org.openbase.bco.psc.sm.merging;

/*
 * -
 * #%L
 * BCO PSC Skeleton Merging
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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import rsb.Scope;
import org.openbase.type.kinematics.Posture3DFloatType.Posture3DFloat;
import org.openbase.type.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PostureFrame {

    private final long timestamp;
    private final Scope scope;
    private final TrackedPostures3DFloat postures;
    private final List<Skeleton3D> joints;

    public PostureFrame(final long timestamp, final Scope scope, final TrackedPostures3DFloat postures) {
        this.timestamp = timestamp;
        this.scope = scope;
        this.postures = postures;
        this.joints = postures.getPostureList().stream()
                .map(p -> {
                    final Iterator<Float> i = p.getConfidenceList().iterator();
                    // Check whether this works:
                    return p.getPosture().getPositionList().stream()
                            .filter(x -> i.hasNext())
                            .map(pos -> new Joint3D(pos, i.next()))
                            .collect(Collectors.toCollection(Skeleton3D::new));
                })
                .collect(Collectors.toList());
    }

    public PostureFrame(final long timestamp, final List<Skeleton3D> joints) {
        this.timestamp = timestamp;
        this.scope = new Scope("/");
        this.joints = joints;
        this.postures = TrackedPostures3DFloat.newBuilder()
                .addAllPosture(joints.stream()
                        .map((Skeleton3D jl)
                                -> TrackedPosture3DFloat.newBuilder()
                                .setPosture(Posture3DFloat.newBuilder()
                                        .addAllPosition(jl.stream()
                                                .map((Joint3D j) -> j.getTranslation())
                                                .collect(Collectors.toList())
                                        )
                                )
                                .addAllConfidence(jl.stream()
                                        .map((Joint3D j) -> (float) j.getConfidence())
                                        .collect(Collectors.toList())
                                ).build()
                        )
                        .collect(Collectors.toList())
                ).build();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getAge(final long currentTimestamp) {
        return currentTimestamp - timestamp;
    }

    public String getKey() {
        return scope.toString();
    }

    public Scope getScope() {
        return scope;
    }

    public TrackedPostures3DFloat getPostures() {
        return postures;
    }

    public List<Skeleton3D> getSkeletons() {
        return joints;
    }
}
