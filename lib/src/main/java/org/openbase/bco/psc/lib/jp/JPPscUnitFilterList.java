package org.openbase.bco.psc.lib.jp;

/*-
 * #%L
 * BCO PSC Library
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
import java.util.Arrays;
import java.util.List;
import org.openbase.jps.core.AbstractJavaProperty;
import org.openbase.jps.exception.JPNotAvailableException;

/**
 * JavaProperty used to specify flags by which controllable objects are filtered
 * from the registry.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPPscUnitFilterList extends AbstractJavaProperty<List<String>> {

    /**
     * String identifying the type of the argument.
     */
    public final static String[] ARGUMENT_IDENTIFIERS = {"STRING-LIST"};
    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--psc-unit-filter"};

    /**
     * Constructor.
     */
    public JPPscUnitFilterList() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    protected String[] generateArgumentIdentifiers() {
        return ARGUMENT_IDENTIFIERS;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected List<String> getPropertyDefaultValue() throws JPNotAvailableException {
        return Arrays.asList(new String[]{"POINTING_GESTURE"});
    }

    /**
     * {@inheritDoc}
     *
     * @param arguments {@inheritDoc}
     * @return {@inheritDoc}
     * @throws Exception {@inheritDoc}
     */
    @Override
    protected List<String> parse(List<String> arguments) throws Exception {
//        for (String argument : arguments) {
//            if(argument == null || "".equals(argument)){
//                throw new JPBadArgumentException("Arguments have to be something else ");
//            }
//        }
        return arguments;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Filters that can be set in the Meta Data of Registry UnitConfigs, to make them controllable via PSC.";
    }
}
