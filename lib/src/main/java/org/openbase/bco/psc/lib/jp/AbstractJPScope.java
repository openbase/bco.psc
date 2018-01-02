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
import java.util.List;
import org.openbase.jps.core.AbstractJavaProperty;
import org.openbase.jps.exception.JPBadArgumentException;
import rsb.Scope;

/**
 * JavaProperty used to parse an RSB Scope from a command line argument.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public abstract class AbstractJPScope extends AbstractJavaProperty<Scope> {

    /**
     * String identifying the type of the argument.
     */
    public final static String[] ARGUMENT_IDENTIFIERS = {"SCOPE"};

    /**
     * Constructor.
     *
     * @param commandIdentifiers the identifiers used before the property in the
     * command line arguments.
     */
    public AbstractJPScope(String[] commandIdentifiers) {
        super(commandIdentifiers);
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
     * @param arguments {@inheritDoc}
     * @return {@inheritDoc}
     * @throws JPBadArgumentException {@inheritDoc}
     */
    @Override
    protected Scope parse(List<String> arguments) throws JPBadArgumentException {
        String oneArgumentResult = getOneArgumentResult();
        try {
            return new Scope(oneArgumentResult);
        } catch (IllegalArgumentException ex) {
            throw new JPBadArgumentException("Given Scope[" + oneArgumentResult + "] is not a Scope.", ex);
        }
    }
}
