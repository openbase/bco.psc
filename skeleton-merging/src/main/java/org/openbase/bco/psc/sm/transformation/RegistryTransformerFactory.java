package org.openbase.bco.psc.sm.transformation;

/*
 * -
 * #%L
 * BCO PSC Skeleton Merging
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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.Factory;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

/**
 * Factory for the creation of RegistryTransformers from UnitConfigs.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RegistryTransformerFactory implements Factory<RegistryTransformer, UnitConfig> {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RegistryTransformerFactory.class);
    /**
     * The singleton <code>RegistryTransformerFactory</code>.
     */
    public static RegistryTransformerFactory instance;

    /**
     * Private constructor to avoid instantiation beside the singleton.
     */
    private RegistryTransformerFactory() {
    }

    /**
     * Method returns a singelton instance of the unit factory.
     *
     * @return the singleton instance of the factory.
     */
    public synchronized static RegistryTransformerFactory getInstance() {
        if (instance == null) {
            instance = new RegistryTransformerFactory();
        }
        return instance;
    }

    @Override
    public RegistryTransformer newInstance(UnitConfig config) throws InstantiationException, InterruptedException {
        try {
            RegistryTransformer transformer = new RegistryTransformer();
            transformer.applyConfigUpdate(config);
            LOGGER.info("Created RegistryTransformer for id " + transformer.getId());
            return transformer;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException("RegistryTransformerInstance", ex);
        }
    }
}
