package org.openbase.bco.psc.gc.gesture;

/*-
 * #%L
 * BCO PSC Gesture Control
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

import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import rst.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class GestureDetector {

    public static GestureType getGesture(final TrackedPosture3DFloat posture) {
        if (checkArm(posture, true) && checkArm(posture, false)) {
            return GestureType.WOAAAH;
        }
        return GestureType.UNSPECIFIED;
    }

    private static boolean checkArm(final TrackedPosture3DFloat posture, boolean right) {
        final double elbowAngle = getElbowAngle(posture, right);
        final double elbowHeightAngle = getElbowHeightAngle(posture, right, false);
        final double handHeightAngle = getHandHeightAngle(posture, right, false);
        return elbowHeightAngle > 50 && elbowHeightAngle < 85
                && elbowAngle > 95 && elbowAngle < 150
                && handHeightAngle < 60;
    }
}
