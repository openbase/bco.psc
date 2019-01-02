package org.openbase.bco.psc.sm.merging;

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
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class MergingHistory {

    private final PostureFrame lastResult;
    private final HashMap<String, PostureFrame> lastInputFrames;
    private final List<HashMap<String, Integer>> connections;

    public MergingHistory(final PostureFrame lastResult, final HashMap<String, PostureFrame> lastInputFrames, final List<HashMap<String, Integer>> connections) {
        this.lastResult = lastResult;
        this.lastInputFrames = lastInputFrames;
        this.connections = connections;
    }

    public PostureFrame getLastResult() {
        return lastResult;
    }

    public List<HashMap<String, Integer>> getConnections() {
        return connections;
    }
}
