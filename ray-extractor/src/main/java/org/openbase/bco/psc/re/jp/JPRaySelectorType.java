package org.openbase.bco.psc.re.jp;

/*-
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.openbase.bco.psc.re.pointing.selectors.SelectorType;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPEnum;

/**
 * JavaProperty used to specify the RaySelectorInterface implementation to be
 * used.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPRaySelectorType extends AbstractJPEnum<SelectorType> {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--re-selector"};
    /**
     * Names of the enum values.
     */
    private String typeNames;

    /**
     * Constructor.
     */
    public JPRaySelectorType() {
        super(COMMAND_IDENTIFIERS);
        SelectorType[] types = SelectorType.values();
        typeNames = "[";
        for (int i = 0; i < types.length; i++) {
            if (i != 0) {
                typeNames += ", ";
            }
            typeNames += types[i].name();
        }
        typeNames += "]";
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected SelectorType getPropertyDefaultValue() throws JPNotAvailableException {
        return SelectorType.CHOICE;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Defines which implementation of the RaySelectorInterface is used. Possible choices are: " + typeNames;
    }

}
