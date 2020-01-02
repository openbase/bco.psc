package org.openbase.bco.psc.sm.jp;

/*
 * #%L
 * BCO PSC Skeleton Merging
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.openbase.jps.exception.JPValidationException;
import org.openbase.jps.preset.AbstractJPListString;

/**
 * JavaProperty used to specify RSB Scopes(optional) and registry ids of the used Skeleton Sensors (Kinect).
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPRegistryTransformers extends AbstractJPListString {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--sm-registry-transformers"};

    private final static String HEXA_REGEX = "[0-9a-fA-F]";
    private final static String UNIT_ID_REGEX = HEXA_REGEX + "{8}-" + HEXA_REGEX + "{4}-" + HEXA_REGEX + "{4}-" + HEXA_REGEX + "{4}-" + HEXA_REGEX + "{12}";
    private final static String FULL_REGEX = UNIT_ID_REGEX;

    /**
     * Constructor.
     */
    public JPRegistryTransformers() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Registry ids of the used Skeleton Sensors (Kinect). The registry should contain placement information and scopes. "
                + "If the parameter is not used, all Kinects in the registry are used.";
    }

    /**
     * {@inheritDoc}
     *
     * @throws JPValidationException {@inheritDoc}
     */
    @Override
    protected void validate() throws JPValidationException {
        super.validate();
        for (String s : getValue()) {
            if (!s.matches(FULL_REGEX)) {
                throw new JPValidationException("Every argument has to be a unit id and thus match \"" + FULL_REGEX + "\"");
            }
        }
    }
}
