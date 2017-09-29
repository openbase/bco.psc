package org.openbase.bco.psc.control;

/*
 * -
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import org.openbase.bco.dal.remote.service.PowerStateServiceRemote;
import org.openbase.bco.dal.remote.service.ServiceRemoteFactoryImpl;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Configurable;
import org.slf4j.LoggerFactory;
import rst.domotic.service.ServiceTemplateType;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 * This class represents a Unit which whose power state can be controlled.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class ControllableObject implements Configurable<String, UnitConfig> {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ControllableObject.class);
    /**
     * Cooldown time that is required before the power state can be switched again.
     */
    private final long cooldownTime;
    /**
     * UnitConfig of the corresponding Unit.
     */
    private UnitConfig config;
    /**
     * PowerStateServiceRemote used to control the power state.
     */
    private PowerStateServiceRemote serviceRemote;
    /**
     * Timestamp of the last power switch action.
     */
    private long lastSwitch = 0;

    /**
     * Constructor.
     *
     * @param cooldownTime Cooldown time that is required before the power state can be switched again.
     */
    public ControllableObject(final long cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    /**
     * Switches the power state of the corresponding unit (from off to on and
     * vice versa).
     *
     * @return true, if the power switch was successful.
     * @throws CouldNotPerformException is thrown if something goes wrong during the power switch.
     */
    public synchronized boolean switchPowerState() throws CouldNotPerformException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSwitch > cooldownTime) {
            PowerState.State newState;
            try {
                switch (serviceRemote.getPowerState().getValue()) {
                    case OFF:
                    case UNKNOWN:
                    default:
                        newState = PowerState.State.ON;
                        break;
                    case ON:
                        newState = PowerState.State.OFF;
                        break;
                }
                LOGGER.debug("Switching power of " + config.getLabel() + " to " + newState.toString());
                serviceRemote.setPowerState(PowerState.newBuilder().setValue(newState).build());
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not switch power state.", ex);
            }
            lastSwitch = currentTime;
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param config {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public synchronized UnitConfig applyConfigUpdate(UnitConfig config) throws CouldNotPerformException, InterruptedException {
        this.config = config;
        try {
            serviceRemote = (PowerStateServiceRemote) ServiceRemoteFactoryImpl.getInstance().newInitializedInstance(
                    ServiceTemplateType.ServiceTemplate.ServiceType.POWER_STATE_SERVICE,
                    config);
            serviceRemote.activate();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not apply ConfigUpdate on ControllableObject", ex);
        }
        return this.config;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public synchronized String getId() throws NotAvailableException {
        if (config == null) {
            throw new NotAvailableException("Id");
        }
        return config.getId();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public synchronized UnitConfig getConfig() throws NotAvailableException {
        if (config == null) {
            throw new NotAvailableException("Config");
        }
        return config;
    }
}
