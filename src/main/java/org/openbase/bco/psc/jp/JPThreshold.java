package org.openbase.bco.psc.jp;

/*-
 * #%L
 * BCO Pointing Smart Control
 * %%
 * Copyright (C) 2016 openbase.org
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

import java.util.List;
import org.openbase.jps.exception.JPBadArgumentException;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPFloat;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPThreshold extends AbstractJPFloat{
    public final static String[] COMMAND_IDENTIFIERS = {"-t", "--threshold"};
    
    public JPThreshold(){
        super(COMMAND_IDENTIFIERS);
    }
    
    @Override
    protected Float getPropertyDefaultValue() throws JPNotAvailableException {
        return 0.0f;
    }

    @Override
    public String getDescription() {
        return "Probability threshold defining how big the combined probability of a pointing gesture at a selection object has to be, to trigger its corresponding action.";
    }

    @Override
    protected Float parse(List<String> arguments) throws JPBadArgumentException {
        Float f = super.parse(arguments);
        if(f > 1.0f || f < 0.0f)
            throw new JPBadArgumentException("Threshold has to be between 0.0 and 1.0!");
        return f;
    }
}
