package org.openbase.bco.psc.util;

/*-
 * #%L
 * BCO PSC Utility
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.openbase.bco.psc.control.ControlLauncher;
import org.openbase.bco.psc.identification.IdentificationLauncher;
import org.openbase.bco.psc.re.RayExtractorLauncher;
import org.openbase.bco.psc.sm.SkeletonMergingLauncher;
import org.openbase.bco.authentication.lib.BCO;
import org.openbase.jul.pattern.launch.AbstractLauncher;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PSCLauncher {

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        BCO.printLogo();
        AbstractLauncher.main(args, PSC.class,
                SkeletonMergingLauncher.class,
                RayExtractorLauncher.class,
                IdentificationLauncher.class,
                ControlLauncher.class
        );
    }

}
