package org.openbase.bco.psc.util.kinect;

/*
 * -
 * #%L
 * BCO PSC Utility
 * %%
 * Copyright (C) 2016 - 2019 openbase.org
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
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import org.openbase.bco.psc.util.jp.JPKinectLocation;
import org.openbase.bco.psc.util.jp.JPKinectName;
import org.openbase.bco.psc.util.jp.JPKinectPlacementFile;
import org.openbase.bco.psc.util.jp.JPKinectSerialNumber;
import org.openbase.bco.psc.util.jp.JPKinectUnitId;
import org.openbase.bco.registry.remote.Registries;
import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.type.processing.LabelProcessor;
import org.slf4j.LoggerFactory;
import org.openbase.rct.Transform;
import org.openbase.type.configuration.EntryType.Entry;
import org.openbase.type.configuration.MetaConfigType.MetaConfig;
import org.openbase.type.domotic.state.EnablingStateType.EnablingState;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate;
import org.openbase.type.domotic.unit.device.DeviceConfigType.DeviceConfig;
import org.openbase.type.domotic.unit.location.LocationConfigType;
import org.openbase.type.geometry.PoseType.Pose;
import org.openbase.type.geometry.RotationType.Rotation;
import org.openbase.type.geometry.TranslationType.Translation;
import org.openbase.type.math.Vec3DDoubleType.Vec3DDouble;
import org.openbase.type.spatial.PlacementConfigType.PlacementConfig;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class KinectManager {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(KinectManager.class);

    /**
     * Id of the Kinect device class.
     */
    private static final String KINECT_CLASS_ID = "e18d2b16-1969-4e5b-b40c-8b255c2cdb8c";
    /**
     * String added to the name as a prefix to create the unit label.
     */
    private static final String NAME_PREFIX = "Kinect ";

    /**
     * Updates the Kinect device config as specified in the JavaProperties.
     *
     * @throws CouldNotPerformException is thrown, if the update could not be executed.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    public static void updateKinect() throws CouldNotPerformException, InterruptedException {
        try {
            if (!JPService.getProperty(JPKinectUnitId.class).isParsed()) {
                throw new CouldNotPerformException("The unit id is required for updating the Kinect config.");
            }
            final String unitId = JPService.getProperty(JPKinectUnitId.class).getValue();
            LOGGER.info("Loading device config to update.");
            UnitConfig.Builder deviceConfigBuilder = Registries.getUnitRegistry(true).getUnitConfigById(unitId).toBuilder();
            addInformation(deviceConfigBuilder);
            LOGGER.info("Updating the registry.");
            Future<UnitConfig> configResult = Registries.getUnitRegistry(true).updateUnitConfig(deviceConfigBuilder.build());
            UnitConfig finalResult = updateLocationId(configResult.get());
            LOGGER.info("Updated config result: \n" + finalResult.toString());
        } catch (CouldNotPerformException | ExecutionException | JPNotAvailableException ex) {
            throw new CouldNotPerformException("Could not update the Kinect config.", ex);
        }
    }

    /**
     * Creates the Kinect device config as specified in the JavaProperties.
     *
     * @throws CouldNotPerformException is thrown, if the creation could not be executed.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    public static void createKinect() throws InterruptedException, CouldNotPerformException {
        try {
            if (!JPService.getProperty(JPKinectSerialNumber.class).isParsed()) {
                throw new CouldNotPerformException("The serial number is required for creating the Kinect config.");
            }
            if (!JPService.getProperty(JPKinectPlacementFile.class).isParsed()) {
                throw new CouldNotPerformException("The placement file is required for creating the Kinect config.");
            }
            if (!JPService.getProperty(JPKinectName.class).isParsed()) {
                throw new CouldNotPerformException("The kinect name is required for creating the Kinect config.");
            }
            final String name = JPService.getProperty(JPKinectName.class).getValue();
            if (!Registries.getUnitRegistry(true).getUnitConfigsByLabel(NAME_PREFIX + name).isEmpty()) {
                throw new CouldNotPerformException("There already is a Kinect under the name " + name + ". Please use bco-psc-update-kinect instead or select another name.");
            }
            final String serialNumber = JPService.getProperty(JPKinectSerialNumber.class).getValue();
            LOGGER.info("Creating new device config.");
            UnitConfig.Builder deviceConfigBuilder = UnitConfig.newBuilder()
                    .setUnitType(UnitTemplate.UnitType.DEVICE)
                    .setDeviceConfig(DeviceConfig.newBuilder()
                            .setDeviceClassId(KINECT_CLASS_ID)
                            .setSerialNumber(serialNumber))
                    .setEnablingState(EnablingState.newBuilder().setValue(EnablingState.State.ENABLED));
            addInformation(deviceConfigBuilder);
            LOGGER.info("Creating the registry entry.");
            Future<UnitConfig> configResult = Registries.getUnitRegistry(true).registerUnitConfig(deviceConfigBuilder.build());
            UnitConfig finalResult = updateLocationId(configResult.get());
            LOGGER.info("Created config result: \n" + finalResult.toString());
        } catch (CouldNotPerformException | ExecutionException | JPNotAvailableException ex) {
            throw new CouldNotPerformException("Could not update the Kinect config.", ex);
        }
    }

    /**
     * Updates the registry entry of the given UnitConfig so that the location matches the tile that the Kinect is placed in.
     *
     * @param config Kinect device config to be updated.
     * @return The resulting UnitConfig saved in the registry.
     * @throws CouldNotPerformException is thrown, if the update could not be executed.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    private static UnitConfig updateLocationId(UnitConfig config) throws CouldNotPerformException, InterruptedException {
        try {
            if (JPService.getProperty(JPKinectPlacementFile.class).isParsed()) {
                UnitConfig targetLocation;
                if (JPService.getProperty(JPKinectLocation.class).isParsed()) {
                    targetLocation = getUnitRegistry(true).getUnitConfigsByLabel(JPService.getProperty(JPKinectLocation.class).getValue()).get(0);
                    LOGGER.info("Location selected by user: " + targetLocation.getLabel() + ", calculating the correct transformation.");
                } else {
                    LOGGER.info("Finding the correct location id.");
                    Translation rootTranslation = config.getPlacementConfig().getPose().getTranslation();
                    Vec3DDouble rootVector = Vec3DDouble.newBuilder().setX(rootTranslation.getX()).setY(rootTranslation.getY()).setZ(rootTranslation.getZ()).build();
                    try {
                        System.out.println("root coordinate vector: " + rootVector.toString());
                        List<UnitConfig> locationConfigsByCoordinate = getUnitRegistry(true).getLocationUnitConfigsByCoordinate(rootVector, LocationConfigType.LocationConfig.LocationType.TILE);
                        if (locationConfigsByCoordinate.isEmpty()) {
                            throw new ExecutionException(new CouldNotPerformException("No fitting location could be found."));
                        }
                        targetLocation = locationConfigsByCoordinate.get(0);
                        LOGGER.info("Selected location is " + targetLocation.getLabel() + ", calculating the correct transformation.");
                    } catch (ExecutionException ex) {
                        LOGGER.info("No optimal location could be found, keeping Home as location. If you want a specific location, you can specify it using the -l parameter.");
                        return config;
                    }
                }
                Future<Transform> unitTransformationFuture = getUnitRegistry().getUnitTransformationFuture(config, targetLocation);
                Transform3D transform = unitTransformationFuture.get().getTransform();
                Quat4d quat = new Quat4d();
                Vector3d vec = new Vector3d();
                transform.get(quat, vec);
                Rotation.Builder rot = Rotation.newBuilder().setQw(quat.w).setQx(quat.x).setQy(quat.y).setQz(quat.z);
                Translation.Builder trans = Translation.newBuilder().setX(vec.x).setY(vec.y).setZ(vec.z);

                UnitConfig updatedConfig = config.toBuilder()
                        .setPlacementConfig(config.getPlacementConfig().toBuilder()
                                .setLocationId(targetLocation.getId())
                                .setPose(Pose.newBuilder().setRotation(rot).setTranslation(trans)))
                        .build();
                LOGGER.info("Updating the placement config in the registry.");
                return Registries.getUnitRegistry(true).updateUnitConfig(updatedConfig).get();
            } else {
                return config;
            }
        } catch (JPNotAvailableException | CouldNotPerformException | ExecutionException ex) {
            throw new CouldNotPerformException("Could not update the location id.", ex);
        }
    }

    /**
     * Adds information from the JavaProperties to the builder.
     *
     * @param deviceConfigBuilder builder that should be extended.
     * @throws JPNotAvailableException is thrown if a JavaProperty is not available.
     * @throws CouldNotPerformException is thrown if something went wrong.
     * @throws InterruptedException is thrown in case of an external interruption.
     */
    private static void addInformation(final UnitConfig.Builder deviceConfigBuilder) throws JPNotAvailableException, CouldNotPerformException, InterruptedException {
        if (JPService.getProperty(JPKinectName.class).isParsed()) {
            final String name = JPService.getProperty(JPKinectName.class).getValue();
            final String scope = "/pointing/skeleton/" + name;
            LOGGER.info("Setting Kinect name to " + name + " and scope to " + scope);
            LabelProcessor.addLabel(deviceConfigBuilder.getLabelBuilder(), Locale.ENGLISH, "Kinect " + name);
            final MetaConfig.Builder metaBuilder = deviceConfigBuilder.getMetaConfigBuilder();
            final ListIterator<Entry.Builder> listIterator = metaBuilder.getEntryBuilderList().listIterator();
            boolean included = false;
            while (listIterator.hasNext()) {
                final int i = listIterator.nextIndex();
                final Entry.Builder value = listIterator.next();
                if ("scope".equals(value.getKey())) {
                    value.setValue(scope);
                    metaBuilder.setEntry(i, value);
                    included = true;
                    break;
                }
            }
            if (!included) {
                metaBuilder.addEntry(Entry.newBuilder().setKey("scope").setValue(scope));
            }
            deviceConfigBuilder.setMetaConfig(metaBuilder);
        }
        if (JPService.getProperty(JPKinectPlacementFile.class).isParsed()) {
            try {
                LOGGER.info("Setting placement config to transformation from file in root location.");
                // Adding the placement in root coordinates. Needs to be updated to location later.
                final File placementFile = JPService.getProperty(JPKinectPlacementFile.class).getValue();
                Transform3D transform = parseFile(placementFile);
                Quat4d quat = new Quat4d();
                Vector3d vec = new Vector3d();
                transform.get(quat, vec);
                Rotation.Builder rot = Rotation.newBuilder().setQw(quat.w).setQx(quat.x).setQy(quat.y).setQz(quat.z);
                Translation.Builder trans = Translation.newBuilder().setX(vec.x).setY(vec.y).setZ(vec.z);
                String rootId = Registries.getUnitRegistry(true).getRootLocationConfig().getId();
                PlacementConfig.Builder placementBuilder = PlacementConfig.newBuilder()
                        .setPose(Pose.newBuilder().setRotation(rot).setTranslation(trans))
                        .setLocationId(rootId);
                deviceConfigBuilder.setPlacementConfig(placementBuilder);
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not add placement information.", ex);
            }
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
    private static Transform3D parseFile(File transformFile) throws CouldNotPerformException {
        //TODO: Adapt this to take the correct file format!
        LOGGER.debug("Parsing placement file");
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
            int rot_i = 0;
            int trans_i = 0;
            boolean rot = false;
            boolean trans = false;
            try {
                while ((myLine = bufRead.readLine()) != null) {
                    String[] line = new String[3];
                    double[] values = new double[3];
                    if (myLine.startsWith("RVEC: ")) {
                        rot = true;
                        line = myLine.substring("RVEC: ".length() + 1, myLine.length() - 1).split(",");
                    } else if (myLine.startsWith("TVEC: ")) {
                        trans = true;
                        line = myLine.substring("TVEC: ".length() + 1, myLine.length() - 1).split(",");
                    } else if (rot) {
                        line = myLine.substring(0, myLine.length() - 1).split(",");
                    } else if (trans) {
                        line = myLine.substring(0, myLine.length() - 1).split(",");
                    }
                    if (rot) {
                        for (int j = 0; j < 3; j++) {
                            values[j] = Double.parseDouble(line[j].trim());
                        }
                        rotation.setRow(rot_i, values);
                        rot_i++;
                        if (rot_i == 3) {
                            rot_i = 0;
                            rot = false;
                        }
                    }
                    if (trans) {
                        switch (trans_i) {
                            case 0:
                                translation.x = Double.parseDouble(line[0].trim());
                                break;
                            case 1:
                                translation.y = Double.parseDouble(line[0].trim());
                                break;
                            case 2:
                                translation.z = Double.parseDouble(line[0].trim());
                                break;
                        }
                        trans_i++;
                        if (trans_i == 3) {
                            trans = false;
                        }
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
