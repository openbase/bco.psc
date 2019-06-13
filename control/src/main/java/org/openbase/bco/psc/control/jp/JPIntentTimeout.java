package org.openbase.bco.psc.control.jp;

/*
 * -
 * #%L
 * BCO PSC Control
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
import org.openbase.bco.psc.lib.jp.AbstractJPTimeout;
import org.openbase.jps.exception.JPNotAvailableException;


/**
 * JavaProperty used to specify the timeout defining how long an intent will be kept and used for matching.
 *
 * @author <a href="mailto:dreinsch@techfak.uni-bielefeld.de">Dennis Reinsch</a>
 */
public class JPIntentTimeout extends AbstractJPTimeout {

    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--intent-timneout"};

    /**
     * Constructor.
     */
    public JPIntentTimeout() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected Integer getPropertyDefaultValue() throws JPNotAvailableException {
        return 5000;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Intent timeout defining how long an intent will be kept and used for matching with other intents.";
    }

}
