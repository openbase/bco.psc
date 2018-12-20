package org.openbase.bco.psc.identification.selection;

/*
 * -
 * #%L
 * BCO PSC Identification
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Factory;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.unit.UnitConfigType;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SelectableObjectFactory implements Factory<SelectableObject, UnitConfigType.UnitConfig> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SelectableObjectFactory.class);
    public static SelectableObjectFactory instance;

    private SelectableObjectFactory() {
    }

    /**
     * Method returns a new singelton instance of the unit factory.
     *
     * @return
     */
    public synchronized static SelectableObjectFactory getInstance() {
        if (instance == null) {
            instance = new SelectableObjectFactory();
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
    public SelectableObject newInstance(UnitConfigType.UnitConfig config) throws InstantiationException, InterruptedException {
        try {
            SelectableObject box = new SelectableObject();
            box.applyConfigUpdate(config);
            LOGGER.info("Created selectable object for unit " + config.getLabel() + " with id " + config.getId());
            return box;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException("SelectableObjectInstance", ex);
        }
    }

}
