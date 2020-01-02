package org.openbase.bco.psc.identification.jp;

/*-
 * #%L
 * BCO PSC Identification
 * %%
 * Copyright (C) 2016 - 2020 openbase.org
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
import org.openbase.bco.psc.identification.selection.distance.DistanceType;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPEnum;

/**
 * JavaProperty used to specify the AbstractDistanceMeasure implementation to be
 * used.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPDistanceType extends AbstractJPEnum<DistanceType> {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--identification-distance-measure"};
    /**
     * Names of the enum values.
     */
    private String typeNames;

    /**
     * Constructor.
     */
    public JPDistanceType() {
        super(COMMAND_IDENTIFIERS);
        DistanceType[] types = DistanceType.values();
        typeNames = "[";
        for (int i = 0; i < types.length; i++) {
            if (i != 0) {
                typeNames += ", ";
            }
            typeNames += types[i].name();
        }
        typeNames += "]";
    }

    @Override
    protected DistanceType getPropertyDefaultValue() throws JPNotAvailableException {
        return DistanceType.ANGLE;
    }

    @Override
    public String getDescription() {
        return "Defines which implementation of the AbstractDistanceProbabilityMeasure is used. Possible choices are: " + typeNames;
    }

}
