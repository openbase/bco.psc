package org.openbase.bco.psc.util;

/*
 * -
 * #%L
 * BCO PSC Utility
 * %%
 * Copyright (C) 2016 - 2017 openbase.org
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
import org.openbase.bco.psc.util.jp.JPKinectLocation;
import org.openbase.bco.psc.util.jp.JPKinectName;
import org.openbase.bco.psc.util.jp.JPKinectPlacementFile;
import org.openbase.bco.psc.util.jp.JPKinectUnitId;
import org.openbase.bco.psc.util.kinect.KinectManager;
import org.openbase.bco.registry.lib.BCO;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/**
 * Launches the update of a Kinect device config in the registry.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class UpdateKinectLauncher {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UpdateKinectLauncher.class);

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        try {
            BCO.printLogo();
            JPService.registerProperty(JPKinectName.class);
            JPService.registerProperty(JPKinectUnitId.class);
            JPService.registerProperty(JPKinectPlacementFile.class);
            JPService.registerProperty(JPKinectLocation.class);
            JPService.parseAndExitOnError(args);
            KinectManager.updateKinect();
            System.exit(0);
        } catch (InterruptedException | CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Kinect update failed", ex, LOGGER);
            System.exit(255);
        }
    }

}
