package org.openbase.bco.psc.lib.pointing;

/*
 * #%L
 * BCO PSC Library
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
public enum Joints {
    SpineBase(0),
    SpineMid(1),
    Neck(2),
    Head(3),
    ShoulderLeft(4),
    ElbowLeft(5),
    WristLeft(6),
    HandLeft(7),
    ShoulderRight(8),
    ElbowRight(9),
    WristRight(10),
    HandRight(11),
    HipLeft(12),
    KneeLeft(13),
    AnkleLeft(14),
    FootLeft(15),
    HipRight(16),
    KneeRight(17),
    AnkleRight(18),
    FootRight(19),
    SpineShoulder(20),
    HandTipLeft(21),
    ThumbLeft(22),
    HandTipRight(23),
    ThumbRight(24);

    private final int value;

    Joints(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }
    
}
