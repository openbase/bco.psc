package org.openbase.bco.psc.jp;

/*-
 * #%L
 * BCO Pointing Smart Control
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

import java.util.Arrays;
import java.util.List;
import org.openbase.jps.core.AbstractJavaProperty;
import org.openbase.jps.exception.JPNotAvailableException;

/**
 * JavaProperty used to specify flags by which controllable objects are selected from the registry.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPRegistryFlags extends AbstractJavaProperty<List<String>>{
    /** String identifying the type of the argument. */
    public final static String[] ARGUMENT_IDENTIFIERS = {"STRING-LIST"};
    /** The identifiers that can be used in front of the command line argument. */
    public final static String[] COMMAND_IDENTIFIERS = {"--rf", "--registry-flags"};

    /** Constructor. */
    public JPRegistryFlags() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected String[] generateArgumentIdentifiers() {
        return ARGUMENT_IDENTIFIERS;
    }

    @Override
    protected List<String> getPropertyDefaultValue() throws JPNotAvailableException {
        return Arrays.asList(new String[]{"POINTING_GESTURE"});
    }

    @Override
    protected List<String> parse(List<String> arguments) throws Exception {
//        for (String argument : arguments) {
//            if(argument == null || "".equals(argument)){
//                throw new JPBadArgumentException("Arguments have to be something else ");
//            }
//        }
        return arguments;
    }

    @Override
    public String getDescription() {
        return "Flags that can be set in the Meta Data of Registry UnitConfigs, to make them controllable via pointing gestures.";
    }
}
