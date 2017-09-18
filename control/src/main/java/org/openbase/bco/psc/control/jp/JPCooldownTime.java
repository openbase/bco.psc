package org.openbase.bco.psc.control.jp;

/*-
 * #%L
 * BCO PSC Control
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
import org.openbase.jps.preset.AbstractJPLong;

/**
 * JavaProperty used to specify the cooldown time in milliseconds required
 * before an object can be turned on/off again after it has been switched.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPCooldownTime extends AbstractJPLong {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--control-cooldown-time"};

    /**
     * Constructor.
     */
    public JPCooldownTime() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected Long getPropertyDefaultValue() throws JPNotAvailableException {
        return 2000L;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Time in milliseconds required to pass after switching the power state of an object before it can be switched again.";
    }

}
