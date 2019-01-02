package org.openbase.bco.psc.lib.rsb;

/*-
 * #%L
 * BCO PSC Library
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import rsb.Factory;
import rsb.config.ParticipantConfig;
import rsb.util.Properties;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public interface LocalConfigProviderInterface {

    /**
     * Creates an RSB configuration for connecting via socket and localhost.
     *
     * @return the local communication configuration.
     */
    public default ParticipantConfig getLocalConfig() {
        ParticipantConfig localConfig = Factory.getInstance().getDefaultParticipantConfig().copy();
        Properties localProperties = new Properties();
        localProperties.setProperty("transport.socket.host", "localhost");
        localConfig.getTransports().values().forEach((tc) -> {
            tc.setEnabled(false);
        });
        localConfig.getOrCreateTransport("socket").setEnabled(true);
        localConfig.getOrCreateTransport("socket").setOptions(localProperties);
        return localConfig;
    }
}
