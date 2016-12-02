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
import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import rct.Transform;
import rst.domotic.unit.UnitConfigType;
import rst.geometry.AxisAlignedBoundingBox3DFloatType;
import rst.geometry.TranslationType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public class TransformationStuff {
    private Transform3D getCombinedTransform(Transform3D t, Vector3d frontLeftBottom){
        System.out.println(frontLeftBottom);
//        t.transform(frontLeftBottom);
        System.out.println(frontLeftBottom);
        Transform3D translation = new Transform3D();
        translation.setTranslation(frontLeftBottom);
        System.out.println(t);
        System.out.println(translation);
        Transform3D result = new Transform3D();
        result.mul(t, translation);
        System.out.println(result);
        return result;
    }
    
    public TransformationStuff(Transform t, UnitConfigType.UnitConfig config){
        
//        thuppke@pek:~/Master/csra-db/dal-unit-config-db$ gedit 47e63f5a-ff30-4b0d-905a-815f94aa8b50.json
//        System.out.println(config.getPlacementConfig());
        AxisAlignedBoundingBox3DFloatType.AxisAlignedBoundingBox3DFloat bb = config.getPlacementConfig().getShape().getBoundingBox();
        if(bb.hasLeftFrontBottom()){
            TranslationType.Translation flb = bb.getLeftFrontBottom();
            Vector3d frontLeftBottom = new Vector3d(flb.getX(), flb.getY(), flb.getZ());
            getCombinedTransform(t.getTransform(), frontLeftBottom);
        } else {
            Vector3d frontLeftBottom = new Vector3d(0, 0, 1);
            getCombinedTransform(t.getTransform(), frontLeftBottom);
        }
        
//        System.out.println("t:");
//        System.out.println(t);
//        System.out.println("transform:");
//        Transform3D tm = t.getTransform();
//        System.out.println(tm);
//        System.out.println("inverse");
//        Transform3D inverse = new Transform3D();
//        inverse.invert(tm);
//        System.out.println(inverse);
//        System.out.println("matrix:");
//        System.out.println(t.getRotationMatrix());
////        System.out.println("quat:");
////        System.out.println(t.getRotationQuat());
////        System.out.println("ypr:");
////        System.out.println(t.getRotationYPR());
////        System.out.println("translation:");
////        System.out.println(t.getTranslation());
//        
//        Vector4d point = new Vector4d(0.5, 1.1, 2.0,1);
//        System.out.println("point");
//        System.out.println(point);
//        System.out.println("transformation of point:");
//        tm.transform(point);
//        System.out.println(point);
//        
//        
//        AxisAlignedBoundingBox3DFloatType.AxisAlignedBoundingBox3DFloat boundingBox = config.getPlacementConfig().getShape().getBoundingBox();
//        System.out.println("boundingBox:");
//        System.out.println(boundingBox.hasDepth());
//        System.out.println(boundingBox);
//        System.out.println("size:");
//        System.out.println(new Vector3d(boundingBox.getWidth(), boundingBox.getDepth(), boundingBox.getHeight()));
    }
    
}
