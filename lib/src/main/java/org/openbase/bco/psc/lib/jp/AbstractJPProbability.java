package org.openbase.bco.psc.lib.jp;

/*
 * -
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.List;
import org.openbase.jps.exception.JPBadArgumentException;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPDouble;

/**
 * JavaProperty used to specify the probability threshold that is applied before
 * sending data.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class AbstractJPProbability extends AbstractJPDouble {

    public AbstractJPProbability(String[] commandIdentifier) {
        super(commandIdentifier);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected Double getPropertyDefaultValue() throws JPNotAvailableException {
        return 0.0;
    }

    /**
     * {@inheritDoc}
     *
     * @param arguments {@inheritDoc}
     * @return {@inheritDoc}
     * @throws JPBadArgumentException {@inheritDoc}
     */
    @Override
    protected Double parse(List<String> arguments) throws JPBadArgumentException {
        Double d = super.parse(arguments);
        if (d > 1.0 || d < 0.0) {
            throw new JPBadArgumentException("Threshold has to be between 0.0 and 1.0!");
        }
        return d;
    }
}
