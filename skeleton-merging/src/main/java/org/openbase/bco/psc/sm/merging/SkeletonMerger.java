package org.openbase.bco.psc.sm.merging;

/*
 * -
 * #%L
 * BCO PSC Skeleton Merging
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
import java.util.HashMap;
import rst.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SkeletonMerger implements SkeletonMergerInterface {

    private final HashMap<String, PostureFrame> inputFrames = new HashMap<>();
    // TODO: Replace these two by the MergingHistory
    private final HashMap<String, PostureFrame> lastInputFrames = new HashMap<>();
    private PostureFrame lastOutput;

    @Override
    public synchronized TrackedPostures3DFloat createMergedData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void postureUpdate(PostureFrame postureFrame) {
        inputFrames.put(postureFrame.getKey(), postureFrame);
    }

}
