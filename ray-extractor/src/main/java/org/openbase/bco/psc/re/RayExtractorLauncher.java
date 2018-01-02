package org.openbase.bco.psc.re;

import org.openbase.bco.psc.lib.jp.JPLocalInput;
import org.openbase.bco.psc.lib.jp.JPLocalOutput;
import org.openbase.bco.psc.lib.jp.JPPSCBaseScope;
import org.openbase.bco.psc.lib.jp.JPPostureScope;
import org.openbase.bco.psc.lib.jp.JPRayScope;
import org.openbase.bco.psc.re.jp.JPDurationLookback;
import org.openbase.bco.psc.re.jp.JPDurationMaximalAngle;
import org.openbase.bco.psc.re.jp.JPDurationProbabilityThreshold;
import org.openbase.bco.psc.re.jp.JPDurationReductionFactor;
import org.openbase.bco.psc.re.jp.JPRayExtractorThreshold;
import org.openbase.bco.psc.re.jp.JPRayExtractorType;
import org.openbase.bco.psc.re.jp.JPRaySelectorType;
import org.openbase.bco.registry.lib.BCO;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.launch.AbstractLauncher;

/*
 * #%L
 * BCO PSC Ray Extractor
 * %%
 * Copyright (C) 2016 - 2018 openbase.org
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
        // Scopes
        JPService.registerProperty(JPPSCBaseScope.class);
        JPService.registerProperty(JPPostureScope.class);
        JPService.registerProperty(JPRayScope.class);

        // Threshold
        JPService.registerProperty(JPRayExtractorThreshold.class);

        // Component specific
        JPService.registerProperty(JPRayExtractorType.class);
        JPService.registerProperty(JPRaySelectorType.class);

        // PostureHistoryExtractor stuff
        JPService.registerProperty(JPDurationLookback.class);
        JPService.registerProperty(JPDurationProbabilityThreshold.class);
        JPService.registerProperty(JPDurationMaximalAngle.class);
        JPService.registerProperty(JPDurationReductionFactor.class);

        // Transport specification
        JPService.registerProperty(JPLocalInput.class);
        JPService.registerProperty(JPLocalOutput.class);
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
}
