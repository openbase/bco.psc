package org.openbase.bco.psc.identification.jp;

/*-
 * #%L
 * BCO PSC Identification
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
import org.openbase.bco.psc.lib.jp.AbstractJPProbabilityThreshold;

/**
 * JavaProperty used to specify the probability threshold defining how big the
 * probability of a selected unit has to be, to be sent by the identification
 * component.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPIdentificationThreshold extends AbstractJPProbabilityThreshold {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--identification-threshold"};

    /**
     * Constructor.
     */
    public JPIdentificationThreshold() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Probability threshold defining how big the probability of a selected unit has to be, to be sent by the identification component.";
    }

}
