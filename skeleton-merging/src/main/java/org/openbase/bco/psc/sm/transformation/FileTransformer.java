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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;
import org.openbase.jul.exception.CouldNotPerformException;
import org.slf4j.LoggerFactory;

/**
 * This class creates a <code>Transformer</code> from a specific transform-file
 * that can be created using for example the
 * <a href=https://projects.cit-ec.uni-bielefeld.de/projects/lsp-csra/repository/camera-calibration>camera-calibration</a>-tool.
 * The <code>FileTransformer</code> is used to transform the coordinates of
 * <code>TrackedPosture3dFloat</code>-objects.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class FileTransformer extends Transformer {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FileTransformer.class);

    /**
     * Constructor.
     *
     * @param transformFile File used to create a
     * <code>Transform3D</code>-object used for the transformation.
     * @throws CouldNotPerformException is thrown, if the file can not be
     * parsed.
     */
    public FileTransformer(File transformFile) throws CouldNotPerformException {
        LOGGER.info("Initializing Transformer from file: " + transformFile.getAbsolutePath());
        try {
            setTransform(parseFile(transformFile));
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not parse the transform-file " + transformFile.getAbsolutePath(), ex);
        }
    }

    /**
     * The function parses the file <code>transformFile</code> and creates a
     * <code>Transform3D</code>-object from it.
     *
     * @param transformFile File used to create the
     * <code>Transform3D</code>-object.
     * @return The <code>Transform3D</code>-object parsed from the file.
     * @throws CouldNotPerformException is thrown, if the file can not be
     * parsed.
     */
    public static Transform3D parseFile(File transformFile) throws CouldNotPerformException {
        LOGGER.debug("Parsing transformation file");
        if (!transformFile.isFile()) {
            throw new CouldNotPerformException(transformFile.getAbsolutePath() + " ist not a file.");
        }
        if (!transformFile.canRead()) {
            throw new CouldNotPerformException(transformFile.getAbsolutePath() + " is not readable.");
        }
        FileReader input;
        try {
            // Ugly stuff here:
            input = new FileReader(transformFile);
            BufferedReader bufRead = new BufferedReader(input);
            String myLine;
            Matrix3d rotation = new Matrix3d();
            Vector3d translation = new Vector3d();
            int i = 0;
            boolean rot = false;
            boolean trans = false;
            try {
                while ((myLine = bufRead.readLine()) != null) {
                    String[] line = new String[3];
                    double[] values = new double[3];
                    if (myLine.startsWith("Rotation Matrix: ")) {
                        rot = true;
                        line = myLine.substring("Rotation Matrix: ".length() + 1, myLine.length() - 1).split(",");
                    } else if (myLine.startsWith("Translation Vector: ")) {
                        trans = true;
                        line = myLine.substring("Translation Vector: ".length() + 1, myLine.length() - 1).split(",");
                    } else if (rot) {
                        line = myLine.substring(0, myLine.length() - 1).split(",");
                    }
                    if (rot || trans) {
                        for (int j = 0; j < 3; j++) {
                            values[j] = Double.parseDouble(line[j].trim());
                        }
                    }
                    if (rot) {
                        rotation.setRow(i, values);
                        i++;
                        if (i == 3) {
                            i = 0;
                            rot = false;
                        }
                    }
                    if (trans) {
                        translation.set(values);
                        trans = false;
                    }
                }
                Transform3D camera_transform = new Transform3D(rotation, translation, 1.0);
                return camera_transform;
            } finally {
                try {
                    input.close();
                } catch (IOException ex) {
                    throw new CouldNotPerformException("Could not close input " + transformFile.getAbsolutePath(), ex);
                }
            }
        } catch (IOException ex) {
            throw new CouldNotPerformException("Could not parse file " + transformFile.getAbsolutePath(), ex);
        }
    }
}
