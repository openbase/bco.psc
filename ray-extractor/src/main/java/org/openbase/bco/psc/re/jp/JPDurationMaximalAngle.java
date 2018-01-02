package org.openbase.bco.psc.re.jp;

/*
 * -
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
import org.openbase.bco.psc.re.pointing.ExtractorType;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPDouble;

/**
 * JavaProperty used to specify the maximal angle deviation from current pointing direction allowed for a probability increase by the PostureHistoryExtractor.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPDurationMaximalAngle extends AbstractJPDouble {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--re-duration-max-angle"};

    /**
     * Constructor.
     */
    public JPDurationMaximalAngle() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected Double getPropertyDefaultValue() throws JPNotAvailableException {
        return 10.0;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "This is only used if the ray-extractor is " + ExtractorType.POSTURE_DURATION.toString()
                + ". It specifies the maximal angle deviation from current pointing direction allowed for a probability increase.";
    }

}
