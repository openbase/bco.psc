package org.openbase.bco.psc.re;

import org.openbase.bco.psc.lib.jp.JPLocalInput;
import org.openbase.bco.psc.lib.jp.JPLocalOutput;
import org.openbase.bco.psc.lib.jp.JPPostureScope;
import org.openbase.bco.psc.lib.jp.JPRayScope;
import org.openbase.bco.psc.lib.jp.JPThreshold;
import org.openbase.bco.psc.re.jp.JPExtractorType;
import org.openbase.bco.psc.re.jp.JPSelectorType;
import org.openbase.bco.registry.lib.BCO;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.launch.AbstractLauncher;

/*
 * #%L
 * BCO PSC Ray Extractor
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RayExtractorLauncher extends AbstractLauncher<RayExtractorController> {

    public RayExtractorLauncher() throws InstantiationException {
        super(RayExtractor.class, RayExtractorController.class);
    }

    @Override
    protected void loadProperties() {
        JPService.registerProperty(JPExtractorType.class);
        JPService.registerProperty(JPSelectorType.class);
        JPService.registerProperty(JPPostureScope.class);
        JPService.registerProperty(JPRayScope.class);
        JPService.registerProperty(JPLocalInput.class);
        JPService.registerProperty(JPLocalOutput.class);
        JPService.registerProperty(JPThreshold.class);
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public static void main(final String[] args) throws InterruptedException, CouldNotPerformException {
        BCO.printLogo();
        AbstractLauncher.main(args, RayExtractor.class, RayExtractorLauncher.class);
    }

//    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RayExtractorLauncher.class);
//    private double threshold;
//    private RSBConnection rsbConnection;
//    private RayExtractorInterface pointingExtractor;
//
//    @Override
//    public void handleEvent(final Event event) {
//        if (!(event.getData() instanceof TrackedPostures3DFloat)) {
//            return;
//        }
//        LOGGER.trace("New TrackedPostures3DFloat event received.");
//        TrackedPostures3DFloat postures = (TrackedPostures3DFloat) event.getData();
//        pointingExtractor.updatePostures(postures);
//        try {
//            LOGGER.trace("Getting pointing rays.");
//            //TODO: Include the quality of the skeletons into the calculation of probabilities!!!
//
//            //TODO add either posture id to pointingRays or make a PointingRayCollectionList possible!
//            // Maybe even PointingRays3DFloat + Collection including ID?!aswell!!
//            rsbConnection.sendPointingRays(PointingRay3DFloatDistributionCollection.newBuilder()
//                    .addAllElement(pointingExtractor.getPointingRays().stream()
//                            .filter(rd -> rd.getRayList().stream()
//                            .map(r -> r.getCertainty())
//                            .reduce(0.0f, Float::sum) >= threshold)
//                            .collect(Collectors.toList()))
//                    .build());
//        } catch (CouldNotPerformException ex) {
//            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not send the pointing rays.", ex), LOGGER);
//        }
//    }
//
//    private void initExtractor() throws JPNotAvailableException {
//        ExtractorType extractorType = JPService.getProperty(JPExtractorType.class).getValue();
//        LOGGER.info("Selected Extractor implementation: " + extractorType.name());
//        SelectorType selectorType = JPService.getProperty(JPSelectorType.class).getValue();
//        LOGGER.info("Selected Selector implementation: " + selectorType.name());
//        threshold = JPService.getProperty(JPThreshold.class).getValue();
//        LOGGER.info("Selected threshold: " + threshold);
//        RaySelectorInterface raySelector;
//        switch (selectorType) {
//            case CHOICE:
//                raySelector = new ChoiceSelector(Arrays.asList(new PointingType[]{PointingType.HEAD_HAND, PointingType.SHOULDER_HAND}));
//                break;
//            case POLYNOMIAL_3:
//                raySelector = new PolynomialSelectorDegree3();
//                break;
//            case POLYNOMIAL_5:
//                raySelector = new PolynomialSelectorDegree5();
//                break;
//            case POLY_NECK_5:
//                raySelector = new PolynomialOrNeckSelector5();
//                break;
//            case DISTRIBUTED:
//                raySelector = new DistributedSelector();
//                break;
//            default:
//                raySelector = new ChoiceSelector(Arrays.asList(new PointingType[]{PointingType.HEAD_HAND, PointingType.SHOULDER_HAND}));
//                break;
//        }
//        switch (extractorType) {
//            case SIMPLE:
//                pointingExtractor = new SimpleExtractor(raySelector);
//                break;
//            case ARM_POSTURE:
//                pointingExtractor = new ArmPostureExtractor(raySelector);
//                break;
//            default:
//                pointingExtractor = new SimpleExtractor(raySelector);
//                break;
//        }
//    }
//
//    public RayExtractorLauncher() {
//        try {
//            initExtractor();
//            rsbConnection = new RSBConnection(this);
//            addShutdownHook();
//            // Wait for events.
//            while (true) {
//                Thread.sleep(1);
//            }
//        } catch (Exception ex) {
//            ExceptionPrinter.printHistory(new CouldNotPerformException("App failed", ex), LOGGER);
//            System.exit(255);
//        }
//    }
//
//    private void addShutdownHook() {
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                rsbConnection.deactivate();
//            } catch (CouldNotPerformException ex) {
//                ExceptionPrinter.printHistory(ex, LOGGER);
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//                ExceptionPrinter.printHistory(ex, LOGGER);
//            }
//        }));
//    }
//
//    public static void main(String[] args) throws InterruptedException {
//        /* Setup JPService */
//        JPService.setApplicationName(RayExtractorLauncher.class);
//        JPService.registerProperty(JPExtractorType.class);
//        JPService.registerProperty(JPSelectorType.class);
//        JPService.registerProperty(JPPostureScope.class);
//        JPService.registerProperty(JPRayScope.class);
//        JPService.registerProperty(JPLocalInput.class);
//        JPService.registerProperty(JPLocalOutput.class);
//        JPService.registerProperty(JPThreshold.class);
//        JPService.parseAndExitOnError(args);
////        JPService.printHelp();
//
//        RayExtractorLauncher app = new RayExtractorLauncher();
//    }
}