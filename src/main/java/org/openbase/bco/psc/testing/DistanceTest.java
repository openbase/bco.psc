package org.openbase.bco.psc.testing;

/*-
 * #%L
 * BCO Pointing Smart Control
 * %%
 * Copyright (C) 2016 openbase.org
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

import org.openbase.bco.psc.selection.BoundingBox;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import org.openbase.bco.psc.selection.distance.AbstractDistanceMeasure2;
import org.openbase.bco.psc.selection.distance.AngleMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public class DistanceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistanceTest.class);
    
    public static void test(){
        Point3d origin = new Point3d(1, 0, 0);
        Vector3d direction = new Vector3d(-1, .1, 0);
//        Vector3d vec = new Vector3d(direction);vec.normalize();
        //System.out.println(vec);
//        Quat4d quat = new Quat4d();
//        quat.set(new AxisAngle4d(1, 0, 0, Math.toRadians(45)));
        
        AngleMeasure distance = new AngleMeasure();
        distance.distanceProbability(origin, direction, 1, 1, 1);

//        BoundingBox box = new BoundingBox(new Point3d(2, 0, 0), new Vector3d(1, 1, 1), quat);
//        LOGGER.debug(String.valueOf(DistanceMeasures.centerProjectionExtensionMeasure(origin, direction, box)));
//        LOGGER.debug(String.valueOf(DistanceMeasures.pearsonMeasure(origin, direction, box)));
//        LOGGER.debug(String.valueOf(DistanceMeasures.angleCornerMaxMeasure(origin, direction, box)));
//        LOGGER.debug(String.valueOf(DistanceMeasures.getAngle(new Vector3d(0, 1, 0), new Vector3d(-1, 1, 0))));
        
    }
    
}
