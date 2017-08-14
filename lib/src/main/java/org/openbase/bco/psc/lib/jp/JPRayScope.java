package org.openbase.bco.psc.lib.jp;

/*
 * #%L
 * BCO PSC Library
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

import org.openbase.jps.exception.JPNotAvailableException;
import rsb.Scope;

/**
 * JPScope used to parse the scope used for receiving pointing ray events from a command line argument.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPRayScope extends AbstractJPScope {
    /** The identifiers that can be used in front of the command line argument. */
    public final static String[] COMMAND_IDENTIFIERS = {"--rs", "--ray-scope"};
    
    /** Constructor. */
    public JPRayScope(){
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public String getDescription() {
        return "Defines the scope used to send or receive pointing ray data.";
    }

    @Override
    protected Scope getPropertyDefaultValue() throws JPNotAvailableException {
        return new Scope("/pointing_rays");
    }
}
