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

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public class TransformTestOffline {
    public static void test(){
        Matrix3d rotation = new Matrix3d();
        rotation.rotX(Math.PI / 2);
        Transform3D rot = new Transform3D();
        rot.setRotation(rotation);
        
        Vector3d translation = new Vector3d(10, 8, 1);
        Transform3D trans = new Transform3D();
        trans.setTranslation(translation);
        
        Vector3d lfb = new Vector3d(1, 0.5, 3);
        Transform3D lfbTrans = new Transform3D();
        lfbTrans.setTranslation(lfb);
        
        
        Transform3D fromBoxToRoot = new Transform3D();
        fromBoxToRoot.setRotation(rotation);
        fromBoxToRoot.setTranslation(translation);
        System.out.println(fromBoxToRoot);
        
        Transform3D fromBoxToRoot2 = new Transform3D();
        fromBoxToRoot2.mul(trans, rot);
        System.out.println(fromBoxToRoot2);
        
        Transform3D withLFB1 = new Transform3D();
        withLFB1.mul(fromBoxToRoot2, lfbTrans);
        System.out.println(withLFB1);
        withLFB1.invert();
        System.out.println(withLFB1);
        
        Transform3D withLFB2 = new Transform3D(fromBoxToRoot2);
        withLFB2.invert();
        lfbTrans.invert();
        withLFB2.mul(lfbTrans, withLFB2);
        System.out.println(withLFB2);
        
        Point3d toTransform = new Point3d(1, 2, 3);
        System.out.println(toTransform);
        withLFB2.transform(toTransform);
        System.out.println(toTransform);
        toTransform.scale(0.5);
        System.out.println(toTransform);
    }
}
