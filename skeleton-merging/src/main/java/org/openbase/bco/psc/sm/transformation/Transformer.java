package org.openbase.bco.psc.sm.transformation;

/*
 * #%L
 * BCO PSC Skeleton Merging
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.stream.Collectors;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.LoggerFactory;
import org.openbase.type.geometry.RotationType.Rotation;
import org.openbase.type.geometry.TranslationType.Translation;
import org.openbase.type.kinematics.Posture3DFloatType.Posture3DFloat;
import org.openbase.type.tracking.TrackedPosture3DFloatType.TrackedPosture3DFloat;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * The Transformer class is used to transform the coordinates of TrackedPosture3DFloat-objects to the root coordinate system.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Transformer {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Transformer.class);
    /**
     * Transform object used to transform the postures.
     */
    private Transform3D transform;

    /**
     * Constructor for the use in child-classes.
     */
    protected Transformer() {
    }

    /**
     * Public Constructor.
     *
     * @param transform the transform object used to transform the postures.
     */
    public Transformer(Transform3D transform) {
        this.transform = transform;
    }

    /**
     * Sets the internal transform object to the argument.
     * For the use in child-classes.
     *
     * @param transform New transform object.
     */
    protected final synchronized void setTransform(Transform3D transform) {
        this.transform = transform;
    }

    /**
     * Transforms the argument's coordinates to root coordinates.
     *
     * @param skeletons postures that shall be transformed.
     * @return A copy of the <code>skeletons</code> argument with transformed coordinates.
     * @throws CouldNotPerformException is thrown, if the internal transform object is not set.
     */
    public synchronized TrackedPostures3DFloat transform(TrackedPostures3DFloat skeletons) throws CouldNotPerformException {
        if (transform == null) {
            throw new CouldNotPerformException("Transform is null.");
        }
        LOGGER.trace("Transforming postures.");
        TrackedPostures3DFloat.Builder posturesBuilder = skeletons.toBuilder().clone().clearPosture();
        posturesBuilder.addAllPosture(skeletons.getPostureList().stream()
                .map(this::transformTrackedPosture)
                .collect(Collectors.toList()));
        return posturesBuilder.build();
    }

    /**
     * Transforms the argument's coordinates to root coordinates.
     *
     * @param skeleton posture that shall be transformed.
     * @return A copy of the <code>skeleton</code> argument with transformed coordinates.
     */
    private TrackedPosture3DFloat transformTrackedPosture(TrackedPosture3DFloat skeleton) {
        TrackedPosture3DFloat.Builder tpBuilder = skeleton.toBuilder().clone().clearPosture();
        tpBuilder.setPosture(transformPosture(skeleton.getPosture()));
        return tpBuilder.build();
    }

    /**
     * Transforms the argument's coordinates to root coordinates.
     *
     * @param posture posture that shall be transformed.
     * @return A copy of the <code>posture</code> argument with transformed coordinates.
     */
    private Posture3DFloat transformPosture(Posture3DFloat posture) {
        Posture3DFloat.Builder postureBuilder = posture.toBuilder().clone().clearPosition().clearRotation();
        postureBuilder.addAllPosition(posture.getPositionList().stream()
                .map(this::transformPosition)
                .collect(Collectors.toList()));
        postureBuilder.addAllRotation(posture.getRotationList().stream()
                .map(this::transformRotation)
                .collect(Collectors.toList()));
        return postureBuilder.build();
    }

    /**
     * Transforms the argument to root coordinates.
     *
     * @param translation translation that shall be transformed.
     * @return The transformed <code>translation</code> argument.
     */
    private Translation transformPosition(Translation translation) {
        Translation.Builder transformedTranslation = translation.toBuilder().clone().clearX().clearY().clearZ();
        if (translation.hasX() && translation.hasY() && translation.hasZ()) {
            Point3d point = new Point3d(-translation.getX(), -translation.getY(), translation.getZ());
            transform.transform(point);
            transformedTranslation.setX(point.x).setY(point.y).setZ(point.z);
        }
        return transformedTranslation.build();
    }

    /**
     * Transforms the argument to root coordinates.
     *
     * @param rotation rotation that shall be transformed.
     * @return The <code>rotation</code> argument.
     */
    private Rotation transformRotation(Rotation rotation) {
        Rotation.Builder transformedRotation = rotation.toBuilder().clone().clearQw().clearQx().clearQy().clearQz();
        if (rotation.hasQw() && rotation.hasQx() && rotation.hasQy() && rotation.hasQz()) {
            Transform3D t = new Transform3D();
            t.set(new Quat4d(rotation.getQw(), rotation.getQx(), rotation.getQy(), rotation.getQz()));
            Transform3D multiply = new Transform3D();
            multiply.mul(transform, t);
            Quat4d result = new Quat4d();
            multiply.get(result);
            transformedRotation.setQw(result.w).setQx(result.x).setQy(result.y).setQz(result.z);
        }
        return transformedRotation.build();
    }
}
