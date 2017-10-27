package org.openbase.bco.psc.sm.transformation;

/*
 * #%L
 * BCO PSC Skeleton Merging
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
import java.util.Objects;
import javax.media.j3d.Transform3D;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Configurable;
import org.slf4j.LoggerFactory;
import rsb.Scope;
import rst.domotic.unit.UnitConfigType.UnitConfig;

/**
 * This class creates a <code>Transformer</code> for a specific device
 * registered in the bco-unit-registry. The <code>RegistryTransformer</code> is
 * used to transform the coordinates of
 * <code>TrackedPosture3dFloat</code>-objects and can be synchronized with the
 * data in the registry.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class RegistryTransformer extends Transformer implements Configurable<String, UnitConfig> {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RegistryTransformer.class);
    /**
     * UnitConfig of the registry object.
     */
    private UnitConfig config;
    /**
     * The scope on which the posture data is sent.
     */
    private Scope scope;

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
        this.scope = new Scope(config.getMetaConfig().getEntryList().stream()
                .filter(e -> "scope".equals(e.getKey()))
                .findFirst()
                .orElseThrow(() -> new CouldNotPerformException("No scope was found for UnitConfig " + config.getLabel())).getValue());
        Transform3D transform;
        try {
            transform = Registries.getLocationRegistry(true).getUnitToRootTransform3D(config);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not get the transformation.", ex);
        }
        setTransform(transform);
        LOGGER.debug("RegistryTransformer for id " + config.getId() + " updated.");
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

    /**
     * Gets the scope on which the posture data of this transformer is sent.
     *
     * @return the scope.
     * @throws NotAvailableException is thrown if the scope is not available.
     */
    public synchronized Scope getScope() throws NotAvailableException {
        if (scope == null) {
            throw new NotAvailableException("Scope");
        }
        return scope;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(config)
                .append(scope)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RegistryTransformer other = (RegistryTransformer) obj;
        if (!Objects.equals(this.config, other.config)) {
            return false;
        }
        return Objects.equals(this.scope, other.scope);
    }
}
