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

import java.util.concurrent.TimeUnit;
import org.openbase.bco.registry.device.remote.DeviceRegistryRemote;
import org.openbase.bco.registry.location.remote.LocationRegistryRemote;
import org.openbase.bco.registry.unit.remote.UnitRegistryRemote;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;
import rct.Transform;
import rct.TransformReceiver;
import rct.TransformerException;
import rct.TransformerFactory;
import rst.domotic.unit.UnitConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class TransformTest {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TransformTest.class);
    
    public static void test() throws InstantiationException, InitializationException, InterruptedException, CouldNotPerformException, TransformerFactory.TransformerFactoryException{
        UnitRegistryRemote unitRemote = new UnitRegistryRemote();
        unitRemote.init();
        unitRemote.activate();

        LocationRegistryRemote locationRemote = new LocationRegistryRemote();
        locationRemote.init();
        locationRemote.activate();

        TransformerFactory factory = TransformerFactory.getInstance();
        // Create a receiver object. Should be live as long as the application runs
        TransformReceiver transformReceiver = factory.createTransformReceiver();

        unitRemote.waitForData(3000, TimeUnit.MILLISECONDS);
        locationRemote.waitForData(3000, TimeUnit.MILLISECONDS);

        String target_frame = locationRemote.getRootLocationConfig().getPlacementConfig().getTransformationFrameId();
//        String second = locationRemote.getLocationConfigById("81b9efa4-2dc9-432e-b47c-1d73021ff0f3").getPlacementConfig().getTransformationFrameId();
//        String second = deviceRemote.getUnitConfigById("38b6605a-6265-45a7-b710-576294c3e2c8").getPlacementConfig().getTransformationFrameId();
        Thread.sleep(1000);
        String source_frame = "";
        UnitConfigType.UnitConfig config = null;
        for (UnitConfigType.UnitConfig conf : unitRemote.getUnitConfigs()) {
            //Unit LLamp5
//            if ("3249a1a5-52d1-4be1-910f-2063974b53f5".equals(conf.getId())) {
            //Unit where it works.
            if("81b9efa4-2dc9-432e-b47c-1d73021ff0f3".equals(conf.getPlacementConfig().getLocationId())){
                System.out.println(conf.getPlacementConfig().getLocationId());
                System.out.println(conf.getPlacementConfig().getTransformationFrameId());
                System.out.println(conf.getId());
//                System.out.println(deviceRemote.getUnitConfigById(conf.getId()));
                source_frame = conf.getPlacementConfig().getTransformationFrameId();
                config = conf;
                break;
            }
        }
        Transform t = null;
        while (!Thread.currentThread().isInterrupted()) {
            // Lookup the transform
            try {
                t = transformReceiver.lookupTransform(target_frame, source_frame, System.currentTimeMillis());
                break;
            } catch(TransformerException ex) {
                ExceptionPrinter.printHistory("Try again because lookup failed.", ex, LOGGER);
                Thread.sleep(500);
            }


        }
        new TransformationStuff(t, config);
    }
    
}
