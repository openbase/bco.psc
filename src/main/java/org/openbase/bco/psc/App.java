package org.openbase.bco.psc;

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

import org.openbase.bco.psc.registry.RegistryObjectManager;
import org.openbase.bco.psc.selection.SelectableManager;
import org.openbase.bco.psc.selection.AbstractSelectable;
import org.openbase.bco.psc.selection.SelectorInterface;
import org.openbase.bco.psc.jp.JPInScope;
import org.openbase.bco.psc.jp.JPLocalInput;
import org.openbase.bco.psc.jp.JPRegistryFlags;
import org.openbase.bco.psc.jp.JPThreshold;
import org.openbase.bco.psc.rsb.RSBConnection;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.tracking.PointingRay3DFloatCollectionType.PointingRay3DFloatCollection;
import org.openbase.bco.psc.testing.TransformTestOffline;

/**
 * 
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de>Thoren Huppke</a>
 */
public class App extends AbstractEventHandler {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(App.class);
    private SelectableManager selectableManager;
    private SelectorInterface selector;
    private RSBConnection rsbConnection;

    @Override
    public void handleEvent(final Event event) {
//        LOGGER.info(event.toString());
        if (!(event.getData() instanceof PointingRay3DFloatCollection)) {
            return;
        }
        PointingRay3DFloatCollection collection = (PointingRay3DFloatCollection) event.getData();
    }
    
    public App() {
        try {
            selectableManager = new RegistryObjectManager();
            //TODO: remove this bit
            for (AbstractSelectable obj : selectableManager.getSelectables()) {
                LOGGER.info(obj.toString());
                selectableManager.processSelectedObject(obj);
            }
            
//            selector = new Selector(new AngleCornerMaxMeasure());
            rsbConnection = new RSBConnection(this);
//            
            try {
//                // Wait for events.
//                while (true) {
//                    Thread.sleep(1);
//                }
            } finally {
                // Deactivate the listener after use.
                rsbConnection.deactivate();
                ((RegistryObjectManager) selectableManager).shutdown();
            }
            System.exit(0);
        } catch (Exception ex) { 
           ExceptionPrinter.printHistory(new CouldNotPerformException("App failed", ex), LOGGER);
            System.exit(255);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        /* Setup JPService */
        JPService.setApplicationName(App.class);
        JPService.registerProperty(JPInScope.class);
        JPService.registerProperty(JPLocalInput.class);
        JPService.registerProperty(JPRegistryFlags.class);
        JPService.registerProperty(JPThreshold.class);
        JPService.parseAndExitOnError(args);
//        App app = new App();

        try {
//            JPService.printHelp();
            //TODO: Mit Marian angucken:
//            TransformTest.test();
            TransformTestOffline.test();
            
            
//            ServiceRemoteTest.test1();
//            ServiceRemoteTest.test2();
            
//            DistanceTest.test();
            
            System.exit(0);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("App failed", ex), LOGGER);
            System.exit(255);
        }
    }
}
