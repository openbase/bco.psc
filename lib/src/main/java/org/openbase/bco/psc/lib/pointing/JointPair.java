package org.openbase.bco.psc.lib.pointing;

/*-
 * #%L
 * BCO PSC Library
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
/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JointPair {

    private final Joints joint1;
    private final Joints joint2;

    public JointPair(Joints joint1, Joints joint2) {
        this.joint1 = joint1;
        this.joint2 = joint2;
    }

    public Joints getJoint1() {
        return joint1;
    }

    public Joints getJoint2() {
        return joint2;
    }
}
