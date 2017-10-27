/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.bco.psc.re.pointing;

/*
 * -
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
/**
 * An enum of the different types of extractors used to select the correct implementation.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public enum ExtractorType {
    /**
     * Represents the SimpleExtractor assigning a probability of 1 to every pointing action.
     *
     * @deprecated will be removed in later versions.
     */
    @Deprecated
    SIMPLE,
    /**
     * Represents the ArmPostureExtractor which calculates a probability of a pointing gesture based on a model using joint angles which was derived from empirical data.
     */
    ARM_POSTURE,
    /**
     * Represents the PostureHistoryExtractor which calculates a probability of a pointing gesture like the ArmPostureExtractor, but also includes a pointing duration.
     */
    POSTURE_DURATION
}
