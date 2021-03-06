package org.openbase.bco.psc.util.jp;

/*
 * -
 * #%L
 * BCO PSC Utility
 * %%
 * Copyright (C) 2016 - 2021 openbase.org
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
import java.io.File;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPFile;
import org.openbase.jps.tools.FileHandler;

/**
 * JavaProperty representing the file containing placement information for the creation or update of a Kinect device config in the registry.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPKinectPlacementFile extends AbstractJPFile {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"-p", "--placement-file"};

    /**
     * Existence handling mode used for the file validation.
     */
    private final static FileHandler.ExistenceHandling EXISTENCE_HANDLING = FileHandler.ExistenceHandling.CanExist;
    /**
     * Auto create mode used for the file validation.
     */
    private final static FileHandler.AutoMode AUTO_CREATE_MODE = FileHandler.AutoMode.Off;

    /**
     * Constructor.
     */
    public JPKinectPlacementFile() {
        super(COMMAND_IDENTIFIERS, EXISTENCE_HANDLING, AUTO_CREATE_MODE);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected File getPropertyDefaultValue() throws JPNotAvailableException {
        return new File("");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "The file containing position and rotation of the Kinect created by the camera-calibration component.";
    }

}
