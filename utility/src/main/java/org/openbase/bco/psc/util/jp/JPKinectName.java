package org.openbase.bco.psc.util.jp;

/*
 * -
 * #%L
 * BCO PSC Utility
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
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPString;

/**
 * JavaProperty representing the name of the Kinect that is to be updated or created in the registry.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPKinectName extends AbstractJPString {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"-n", "--name"};

    /**
     * Constructor.
     */
    public JPKinectName() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected String getPropertyDefaultValue() throws JPNotAvailableException {
        return "";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "The name of the computer connected to the Kinect or another name that is used as a label and scope.";
    }

}
