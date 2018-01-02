package org.openbase.bco.psc.identification;

/*-
 * #%L
 * BCO PSC Identification
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Testing {

    public static void assertAlmostEquals(Vector3d vector1, Vector3d vector2, double delta) {
        assertEquals(vector1.x, vector2.x, delta);
        assertEquals(vector1.y, vector2.y, delta);
        assertEquals(vector1.z, vector2.z, delta);
    }

    public static void assertAlmostEquals(Vector3d vector1, Vector3d vector2) {
        assertAlmostEquals(vector1, vector2, 0.00000000000001);
    }

    public static void assertAlmostEquals(Point3d point1, Point3d point2, double delta) {
        assertEquals(point1.x, point2.x, delta);
        assertEquals(point1.y, point2.y, delta);
        assertEquals(point1.z, point2.z, delta);
    }

    public static void assertAlmostEquals(Point3d point1, Point3d point2) {
        assertAlmostEquals(point1, point2, 0.00000000000001);
    }
}
