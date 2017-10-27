package org.openbase.bco.psc.sm;

import org.openbase.bco.psc.lib.jp.JPLocalInput;
import org.openbase.bco.psc.lib.jp.JPLocalOutput;
import org.openbase.bco.psc.lib.jp.JPPSCBaseScope;
import org.openbase.bco.psc.lib.jp.JPPostureScope;
import org.openbase.bco.psc.sm.jp.JPDeviceClassList;
import org.openbase.bco.psc.sm.jp.JPDisableRegistry;
import org.openbase.bco.psc.sm.jp.JPFileTransformers;
import org.openbase.bco.psc.sm.jp.JPFrameRate;
import org.openbase.bco.psc.sm.jp.JPRawPostureBaseScope;
import org.openbase.bco.psc.sm.jp.JPRegistryTransformers;
import org.openbase.bco.psc.sm.jp.JPStabilizationFactor;
import org.openbase.bco.registry.lib.BCO;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.launch.AbstractLauncher;

/*
 * #%L
 * BCO PSC Skeleton Merging
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
/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SkeletonMergingLauncher extends AbstractLauncher<SkeletonMergingController> {
    
    public SkeletonMergingLauncher() throws InstantiationException {
        super(SkeletonMerging.class, SkeletonMergingController.class);
    }
    
    @Override
    protected void loadProperties() {
        // Scopes
        JPService.registerProperty(JPPSCBaseScope.class);
        JPService.registerProperty(JPRawPostureBaseScope.class);
        JPService.registerProperty(JPPostureScope.class);

        // Component specific
        JPService.registerProperty(JPFileTransformers.class);
        JPService.registerProperty(JPRegistryTransformers.class);
        JPService.registerProperty(JPDisableRegistry.class);
        JPService.registerProperty(JPDeviceClassList.class);
        JPService.registerProperty(JPFrameRate.class);
        JPService.registerProperty(JPStabilizationFactor.class);

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
        AbstractLauncher.main(args, SkeletonMerging.class, SkeletonMergingLauncher.class);
    }
}
