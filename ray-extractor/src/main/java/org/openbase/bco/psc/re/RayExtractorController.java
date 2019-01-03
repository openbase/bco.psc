package org.openbase.bco.psc.re;

/*
 * -
 * #%L
 * BCO PSC Ray Extractor
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
import java.util.Arrays;
import java.util.stream.Collectors;
import org.openbase.bco.psc.re.jp.JPRayExtractorThreshold;
import org.openbase.bco.psc.re.jp.JPRayExtractorType;
import org.openbase.bco.psc.re.jp.JPRaySelectorType;
import org.openbase.bco.psc.re.pointing.AbstractRayExtractor;
import org.openbase.bco.psc.re.pointing.ArmPostureExtractor;
import org.openbase.bco.psc.re.pointing.ExtractorType;
import static org.openbase.bco.psc.re.pointing.ExtractorType.*;
import org.openbase.bco.psc.re.pointing.PostureHistoryExtractor;
import org.openbase.bco.psc.re.pointing.SimpleExtractor;
import org.openbase.bco.psc.re.pointing.selectors.ChoiceSelector;
import org.openbase.bco.psc.re.pointing.selectors.DistributedSelector;
import org.openbase.bco.psc.re.pointing.selectors.PolynomialOrNeckSelector5;
import org.openbase.bco.psc.re.pointing.selectors.PolynomialSelectorDegree3;
import org.openbase.bco.psc.re.pointing.selectors.PolynomialSelectorDegree5;
import org.openbase.bco.psc.re.pointing.selectors.RaySelectorInterface;
import org.openbase.bco.psc.re.pointing.selectors.SelectorType;
import static org.openbase.bco.psc.re.pointing.selectors.SelectorType.*;
import org.openbase.bco.psc.re.rsb.RSBConnection;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import org.openbase.type.tracking.PointingRay3DFloatDistributionCollectionType.PointingRay3DFloatDistributionCollection;
import org.openbase.type.tracking.PointingRay3DFloatType.PointingRay3DFloat.PointingType;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RayExtractorController extends AbstractEventHandler implements RayExtractor, Launchable<Void>, VoidInitializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RayExtractorLauncher.class);
    private double threshold;
    private RSBConnection rsbConnection;
    private AbstractRayExtractor pointingExtractor;

    private boolean initialized;
    private boolean active;

    @Override
    public void handleEvent(final Event event) {
        if (!(event.getData() instanceof TrackedPostures3DFloat)) {
            return;
        }
        LOGGER.trace("New TrackedPostures3DFloat event received.");
        TrackedPostures3DFloat postures = (TrackedPostures3DFloat) event.getData();
        pointingExtractor.updatePostures(postures);
        try {
            LOGGER.trace("Getting pointing rays.");
            //TODO: Include the quality of the skeletons into the calculation of probabilities!!!

            //TODO add either posture id to pointingRays or make a PointingRayCollectionList possible!
            // Maybe even PointingRays3DFloat + Collection including ID?!aswell!!
            rsbConnection.publishData(PointingRay3DFloatDistributionCollection.newBuilder()
                    .addAllElement(pointingExtractor.getPointingRays().stream()
                            .filter(rd -> rd.getRayList().stream()
                            .map(r -> r.getCertainty())
                            .reduce(0.0f, Float::sum) >= threshold)
                            .collect(Collectors.toList()))
                    .build());
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not send the pointing rays.", ex), LOGGER);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            ExceptionPrinter.printHistory(new CouldNotPerformException("Sending the rays failed.", ex), LOGGER, LogLevel.ERROR);
        }
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        if (!initialized) {
            try {

                initExtractor();
                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
                initialized = true;
            } catch (JPNotAvailableException | CouldNotPerformException ex) {
                throw new InitializationException(RayExtractorController.class, ex);
            }
        }
    }

    private void initExtractor() throws JPNotAvailableException {
        ExtractorType extractorType = JPService.getProperty(JPRayExtractorType.class).getValue();
        LOGGER.info("Selected Extractor implementation: " + extractorType.name());
        SelectorType selectorType = JPService.getProperty(JPRaySelectorType.class).getValue();
        LOGGER.info("Selected Selector implementation: " + selectorType.name());
        threshold = JPService.getProperty(JPRayExtractorThreshold.class).getValue();
        LOGGER.info("Selected threshold: " + threshold);
        RaySelectorInterface raySelector;
        switch (selectorType) {
            case CHOICE:
                raySelector = new ChoiceSelector(Arrays.asList(PointingType.HEAD_HAND, PointingType.SHOULDER_HAND));
                break;
            case POLYNOMIAL_3:
                raySelector = new PolynomialSelectorDegree3();
                break;
            case POLYNOMIAL_5:
                raySelector = new PolynomialSelectorDegree5();
                break;
            case POLY_NECK_5:
                raySelector = new PolynomialOrNeckSelector5();
                break;
            case DISTRIBUTED:
                raySelector = new DistributedSelector();
                break;
            default:
                raySelector = new ChoiceSelector(Arrays.asList(PointingType.HEAD_HAND, PointingType.SHOULDER_HAND));
                break;
        }
        switch (extractorType) {
            case SIMPLE:
                pointingExtractor = new SimpleExtractor(raySelector);
                break;
            case ARM_POSTURE:
                pointingExtractor = new ArmPostureExtractor(raySelector);
                break;
            case POSTURE_DURATION:
                pointingExtractor = new PostureHistoryExtractor(raySelector);
                break;
            default:
                pointingExtractor = new SimpleExtractor(raySelector);
                break;
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Activating " + getClass().getName() + ".");
        if (!initialized) {
            throw new CouldNotPerformException("Activate can only be called after init.");
        }
        if (!active) {
            active = true;
            rsbConnection.activate();
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Deactivating " + getClass().getName() + ".");
        if (active) {
            active = false;
            rsbConnection.deactivate();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
