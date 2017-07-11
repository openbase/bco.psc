package org.openbase.bco.psc.selection;

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
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rst.geometry.AxisAlignedBoundingBox3DFloatType.AxisAlignedBoundingBox3DFloat;
import rst.geometry.TranslationType.Translation;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class BoundingBoxTest {
    Vector3d unitTranslation;
    Matrix3d orientation;
    Vector3d lfb;
    Vector3d size;
    
    BoundingBox box1;
    
    Transform3D rotation;
    Transform3D inverse;
    
    
    public BoundingBoxTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        unitTranslation = new Vector3d();
        orientation = new Matrix3d();
        lfb = new Vector3d();
        size = new Vector3d();
        exampleValues(unitTranslation, orientation, lfb, size);
        box1 = createFromAxisAligned(unitTranslation, orientation, lfb, size);
        rotation = new Transform3D(orientation, new Vector3d(), 1);
        inverse = new Transform3D(rotation);
        inverse.invert();
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testConstructorsEqual(){
        System.out.println("Constructors equal?");
        BoundingBox box2 = createFromDimensions(unitTranslation, orientation, lfb, size);
        BoundingBox box3 = createFromSize(unitTranslation, orientation, lfb, size);
        assertEquals(box1, box2);
        assertEquals(box2, box3);
    }
    
    @Test
    public void testSize(){
        System.out.println("testSize");
        assertTrue(box1.getWidth() == (float)size.x);
        assertTrue(box1.getDepth() == (float)size.y);
        assertTrue(box1.getHeight() == (float)size.z);
        
        assertAlmostEquals(box1.getBoxVector(), size);
    }
    
    @Test
    public void testCenter(){
        System.out.println("testCenter");
        Point3d center = new Point3d(size);
        center.scale(0.5);
        assertEquals(box1.getLocalCenter(), center);
        assertEquals(box1.getRootCenter(), getCenterVector(getLeftCornerVector(unitTranslation, orientation, lfb), orientation, size));
    }
    
    @Test
    public void testOrientation(){
        System.out.println("testCenter");
        Quat4d quat = new Quat4d();
        rotation.get(quat);
        assertEquals(box1.getOrientation(), quat);
    }
    
    @Test
    public void testVectorTransformations(){
        System.out.println("testVectorTransformations");
        Vector3d testVector = new Vector3d(1, 2, 0);
        
        Vector3d changedVector = new Vector3d(testVector);
        rotation.transform(changedVector);
        
        Vector3d inversedVector = new Vector3d(changedVector);
        inverse.transform(inversedVector);
        
        assertAlmostEquals(box1.toBoxCoordinates(changedVector), inversedVector);
    }
    
    @Test
    public void testPointTransformations(){
        System.out.println("testPointTransformations");
        Point3d testPoint = new Point3d(1, 3, -2);
        
        Point3d changedPoint = new Point3d(testPoint);
        rotation.transform(changedPoint);
        changedPoint.add(getLeftCornerVector(unitTranslation, orientation, lfb));
        
        Point3d inversedPoint = new Point3d(changedPoint);
        inversedPoint.sub(getLeftCornerVector(unitTranslation, orientation, lfb));
        inverse.transform(inversedPoint);
        
        assertAlmostEquals(box1.toBoxCoordinates(changedPoint), inversedPoint);
    }
    
    @Test
    public void testVectorCenterTransformations(){
        System.out.println("testVectorCenterTransformations");
        Vector3d testVector = new Vector3d(1, -1.2, 5);
        
        Vector3d changedVector = new Vector3d(testVector);
        rotation.transform(testVector);
        
        Vector3d inversedVector = new Vector3d(changedVector);
        inverse.transform(inversedVector);
        
        assertAlmostEquals(box1.toCenterCoordinates(changedVector), inversedVector);
    }
    
    @Test
    public void testPointCenterTransformations(){
        System.out.println("testPointCenterTransformations");
        Point3d testPoint = new Point3d(0.5, -7, 2);
        
        Point3d changedPoint = new Point3d(testPoint);
        rotation.transform(testPoint);
        changedPoint.add(getCenterVector(getLeftCornerVector(unitTranslation, orientation, lfb), orientation, size));
        
        Point3d inversedPoint = new Point3d(changedPoint);
        inversedPoint.sub(getCenterVector(getLeftCornerVector(unitTranslation, orientation, lfb), orientation, size));
        inverse.transform(inversedPoint);
        
        assertAlmostEquals(box1.toCenterCoordinates(changedPoint), inversedPoint);
    }
    
    private static void exampleValues(Vector3d unitTranslation, Matrix3d orientation, Vector3d lfb, Vector3d size){
        unitTranslation.set(10, 2, 13);
        Transform3D rotation = new Transform3D();
        rotation.setEuler(new Vector3d(45, 90, 0));
        rotation.get(orientation);
        lfb.set(0, 1, 0.5);
        size.set(0.2f, 0.3f, 0.8f);
    }
    
    private void assertAlmostEquals(Vector3d vector1, Vector3d vector2){
        assertTrue(Math.abs(vector1.x - vector2.x) < 0.00000000000001);
        assertTrue(Math.abs(vector1.y - vector2.y) < 0.00000000000001);
        assertTrue(Math.abs(vector1.z - vector2.z) < 0.00000000000001);
    }
    
    private void assertAlmostEquals(Point3d point1, Point3d point2){
        assertTrue(Math.abs(point1.x - point2.x) < 0.00000000000001);
        assertTrue(Math.abs(point1.y - point2.y) < 0.00000000000001);
        assertTrue(Math.abs(point1.z - point2.z) < 0.00000000000001);
    }
    
    private static BoundingBox createFromDimensions(Vector3d unitTranslation, Matrix3d orientation, Vector3d lfb, Vector3d size){
        return new BoundingBox(getForwardTransform(unitTranslation, orientation, lfb), (float)size.x, (float)size.y, (float)size.z);
    }
    
    private static BoundingBox createFromSize(Vector3d unitTranslation, Matrix3d orientation, Vector3d lfb, Vector3d size){
        return new BoundingBox(getForwardTransform(unitTranslation, orientation, lfb), size);
    }
    
    private static BoundingBox createFromAxisAligned(Vector3d unitTranslation, Matrix3d orientation, Vector3d lfb, Vector3d size){
        return new BoundingBox(getFromUnitToRootCoordinates(unitTranslation, orientation), 
                getAxisAlignedBoundingBox(lfb, size));
    }
    
    private static Vector3d getLeftCornerVector(Vector3d unitTranslation, Matrix3d orientation, Vector3d lfb){
        Transform3D rotation = new Transform3D(orientation, new Vector3d(), 1);
        Vector3d lfbInRoot = new Vector3d(lfb);
        rotation.transform(lfbInRoot);
        
        Vector3d toBox = new Vector3d();
        toBox.add(unitTranslation, lfbInRoot);
        return toBox;
    }
    
    private static Vector3d getCenterVector(Vector3d leftCornerVector, Matrix3d orientation, Vector3d size){
        Transform3D rotation = new Transform3D(orientation, new Vector3d(), 1);
        
        Vector3d localCenter = new Vector3d(size);
        localCenter.scale(0.5);
        
        Vector3d toCenterInRoot = new Vector3d(localCenter);
        rotation.transform(toCenterInRoot);
        
        Vector3d toCenter = new Vector3d();
        toCenter.add(leftCornerVector, toCenterInRoot);
        return toCenter;
    }
    
    private static Transform3D getForwardTransform(Vector3d unitTranslation, Matrix3d orientation, Vector3d lfb){
        return new Transform3D(orientation, getLeftCornerVector(unitTranslation, orientation, lfb), 1);
    }
    
    private static Transform3D getForwardCenterTransform(Vector3d unitTranslation, Matrix3d orientation, Vector3d lfb, Vector3d size){
        return new Transform3D(orientation, getCenterVector(getLeftCornerVector(unitTranslation, orientation, lfb), orientation, size), 1);
    }
    
    private static Transform3D getFromUnitToRootCoordinates(Vector3d unitTranslation, Matrix3d orientation){
        return new Transform3D(orientation, unitTranslation, 1);
    }
    
    private static AxisAlignedBoundingBox3DFloat getAxisAlignedBoundingBox(Vector3d lfb, Vector3d size){
        Translation transl = Translation.newBuilder().setX(lfb.x).setY(lfb.y).setZ(lfb.z).build();
        return AxisAlignedBoundingBox3DFloat.newBuilder()
                .setWidth((float) size.x)
                .setDepth((float) size.y)
                .setHeight((float) size.z)
                .setLeftFrontBottom(transl).build();
    }
}
