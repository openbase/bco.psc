package org.openbase.bco.psc.identification.selection.distance;

/*-
 * #%L
 * BCO PSC Identification
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
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.openbase.bco.psc.identification.Testing.*;
import org.openbase.type.math.Vec3DFloatType.Vec3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class AbstractDistanceProbabilityMeasureTest {

    /**
     * Test of toVector method, of class AbstractDistanceMeasure.
     */
    @Test
    public void testToVector() {
        System.out.println("toVector");
        Vec3DFloat vec3d = Vec3DFloat.newBuilder().setX(-1).setY((float) Math.PI).setZ(5.12f).build();
        Vector3d expResult = new Vector3d(-1, (float) Math.PI, 5.12f);
        Vector3d result = AbstractDistanceMeasure.toVector(vec3d);
        assertEquals(expResult, result);
    }

    /**
     * Test of toPoint method, of class AbstractDistanceMeasure.
     */
    @Test
    public void testToPoint() {
        System.out.println("toPoint");
        Vec3DFloat vec3d = Vec3DFloat.newBuilder().setX(-1).setY((float) Math.PI).setZ(5.12f).build();
        Point3d expResult = new Point3d(-1, (float) Math.PI, 5.12f);
        Point3d result = AbstractDistanceMeasure.toPoint(vec3d);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAngle method, of class AbstractDistanceMeasure.
     */
    @Test
    public void testGetAngle() {
        System.out.println("getAngle");
        double delta = 0.00000000000001;
        assertEquals(0.0, AbstractDistanceMeasure.getAngle(new Vector3d(0, 1, 0), new Vector3d(0, 1, 0)), delta);
        assertEquals(Math.PI, AbstractDistanceMeasure.getAngle(new Vector3d(0, 1, 0), new Vector3d(0, -1, 0)), delta);
        assertEquals(Math.PI / 2, AbstractDistanceMeasure.getAngle(new Vector3d(0, 1, 0), new Vector3d(1, 0, 0)), delta);
    }

    /**
     * Test of getProjection method, of class AbstractDistanceMeasure.
     */
    @Test
    public void testGetProjection() {
        System.out.println("getProjection");
        assertAlmostEquals(new Vector3d(0, 1, 0), AbstractDistanceMeasure.getProjection(new Vector3d(0, 20, 0), new Vector3d(0, 1, 0)));
        assertAlmostEquals(new Vector3d(0, 0, 0), AbstractDistanceMeasure.getProjection(new Vector3d(5, 0, 0), new Vector3d(0, 1, 0)));
        assertAlmostEquals(new Vector3d(1, 0, 0), AbstractDistanceMeasure.getProjection(new Vector3d(3, 0, 0), new Vector3d(1, 1, 0)));
    }

    /**
     * Test of getClosestPoint method, of class AbstractDistanceMeasure.
     */
    @Test
    public void testGetClosestPoint() {
        System.out.println("getClosestPoint");
        assertAlmostEquals(new Point3d(0, 0, 0), AbstractDistanceMeasure.getClosestPoint(new Point3d(0, 20, 0), new Vector3d(0, -1, 0)));
        assertNull(AbstractDistanceMeasure.getClosestPoint(new Point3d(0, -10, 0), new Vector3d(-1, -1, 0)));
        assertAlmostEquals(new Point3d(-1, 0, 0), AbstractDistanceMeasure.getClosestPoint(new Point3d(-1, -10, 0), new Vector3d(0, 1, 0)));
        assertAlmostEquals(new Point3d(-2, 2, 0), AbstractDistanceMeasure.getClosestPoint(new Point3d(-4, 0, 0), new Vector3d(1, 1, 0)));
    }

    /**
     * Test of getMaximalPointOnBox method, of class AbstractDistanceMeasure.
     */
    @Test
    public void testGetMaximalPointOnBox() {
        System.out.println("getMaximalPointOnBox");
        double delta = 0.000001;
        System.out.println("ray 0");
        Point3d maxPointEst = new Point3d(-0.500000, 0.500000, -0.054545);
        Point3d origin = new Point3d(-2.000000, 0.500000, 0.600000);
        Vector3d direction = new Vector3d(1.500000, -0.100000, -0.600000);
        Point3d maxPoint = AbstractDistanceMeasure.getMaximalPointOnBox(origin, direction, 1.000000f, 1.000000f, 1.000000f);
        assertAlmostEquals(maxPointEst, maxPoint, delta);
//        Vector3d maxDir = new Vector3d(maxPoint);maxDir.sub(origin);
//        System.out.println(AbstractDistanceMeasure.getClosestPoint(origin, maxDir));
//        assertAlmostEquals(maxPoint, AbstractDistanceMeasure.getClosestPoint(origin, maxDir), delta);
        System.out.println("ray 1");
        assertAlmostEquals(new Point3d(0.500000, 0.216667, -0.500000),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(-2.000000, 0.300000, 0.600000),
                        new Vector3d(1.500000, -0.100000, -0.600000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 2");
        assertAlmostEquals(new Point3d(-0.500000, 0.500000, 0.320000),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(-2.000000, 0.300000, 0.600000),
                        new Vector3d(1.500000, -0.100000, -0.400000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 3");
        assertAlmostEquals(new Point3d(-0.500000, 0.500000, -0.040000),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(-2.000000, 0.100000, 0.600000),
                        new Vector3d(1.500000, 0.300000, -0.600000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 4");
        assertAlmostEquals(new Point3d(-0.500000, -0.500000, -0.200000),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(-2.000000, 0.100000, 0.600000),
                        new Vector3d(1.500000, -0.300000, -0.600000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 5");
        assertAlmostEquals(new Point3d(-0.500000, -0.500000, 0.466667),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(-2.000000, -0.500000, 1.200000),
                        new Vector3d(1.500000, -0.300000, -0.600000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 6");
        assertAlmostEquals(new Point3d(0.500000, -0.500000, 0.033333),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(-2.000000, -0.600000, 1.600000),
                        new Vector3d(1.500000, -0.300000, -0.700000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 7");
        assertAlmostEquals(new Point3d(0.500000, -0.120000, 0.500000),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(-2.000000, -0.600000, 1.600000),
                        new Vector3d(1.500000, 0.300000, -0.700000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 8");
        assertAlmostEquals(new Point3d(-0.500000, 0.120000, -0.500000),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(2.000000, 0.600000, -1.600000),
                        new Vector3d(-1.500000, -0.300000, 0.700000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 9");
        assertAlmostEquals(new Point3d(-0.500000, 0.120000, 0.500000),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(2.000000, 0.600000, 1.600000),
                        new Vector3d(-1.500000, -0.300000, -0.700000), 1.000000f, 1.000000f, 1.000000f), delta);
        System.out.println("ray 10");
        assertAlmostEquals(new Point3d(-0.500000, 0.120000, 0.500000),
                AbstractDistanceMeasure.getMaximalPointOnBox(new Point3d(2.000000, 0.600000, 1.600000),
                        new Vector3d(-1.500000, -0.300000, -0.700000), 1.000000f, 1.000000f, 1.000000f), delta);
    }
}
