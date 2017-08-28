package org.openbase.bco.psc.control.registry;

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

import org.openbase.bco.psc.control.jp.JPCooldownTime;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Factory;
import rst.domotic.unit.UnitConfigType;

/**
 * A factory used for the creation of <code>ControllableObject</code>s.
 * 
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ControllableObjectFactory implements Factory<ControllableObject, UnitConfigType.UnitConfig>  {
    /** Singleton instance. */
    public static ControllableObjectFactory instance;
    /** Cooldown time used to initialize the <code>ControllableObject</code>s. */
    private final long cooldownTime;
    
    /**
     * Constructor.
     * 
     * @throws InstantiationException Is thrown, if the factory could not be instantiated.
     */
    private ControllableObjectFactory() throws InstantiationException {
        try {
            cooldownTime = JPService.getProperty(JPCooldownTime.class).getValue();
        } catch (JPNotAvailableException ex) {
            throw new InstantiationException(ControllableObjectFactory.class, ex);
        }
    }

    /**
     * Method returns a singelton instance of the unit factory.
     *
     * @return The singleton instance of the factory.
     * @throws org.openbase.jul.exception.InstantiationException Is thrown if the factory could not be instantiated.
     */
    public synchronized static ControllableObjectFactory getInstance() throws InstantiationException {
        if (instance == null) {
            instance = new ControllableObjectFactory();
        }
        return instance;
    }

    /**
     * {@inheritDoc}
     * 
     * @param config {@inheritDoc}
     * @return {@inheritDoc}
     * @throws InstantiationException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public ControllableObject newInstance(UnitConfigType.UnitConfig config) throws InstantiationException, InterruptedException {
        try {
            ControllableObject object = new ControllableObject(cooldownTime);
            object.applyConfigUpdate(config);
            return object;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException("SelectableObjectInstance", ex);
        }
    }
    
}
