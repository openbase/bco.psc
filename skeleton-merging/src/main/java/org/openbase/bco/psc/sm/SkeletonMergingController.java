package org.openbase.bco.psc.sm;

/*
 * -
 * #%L
 * BCO PSC Skeleton Merging
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

import org.openbase.bco.psc.lib.jp.JPPSCBaseScope;
import org.openbase.bco.psc.lib.jp.JPPostureScope;
import org.openbase.bco.psc.lib.registry.PointingUnitChecker;
import org.openbase.bco.psc.sm.jp.*;
import org.openbase.bco.psc.sm.merging.MergingScheduler;
import org.openbase.bco.psc.sm.merging.PostureFrame;
import org.openbase.bco.psc.sm.merging.SkeletonMerger;
import org.openbase.bco.psc.sm.merging.SkeletonMergerInterface;
import org.openbase.bco.psc.sm.merging.stabilizing.StabilizerImpl;
import org.openbase.bco.psc.sm.rsb.RSBConnection;
import org.openbase.bco.psc.sm.transformation.FileTransformer;
import org.openbase.bco.psc.sm.transformation.RegistryTransformer;
import org.openbase.bco.psc.sm.transformation.RegistryTransformerFactory;
import org.openbase.bco.psc.sm.transformation.Transformer;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.exception.JPValidationException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.storage.registry.RegistrySynchronizer;
import org.openbase.jul.storage.registry.SynchronizableRegistryImpl;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rsb.MetaData;
import rsb.Scope;
import org.openbase.type.domotic.state.EnablingStateType.EnablingState;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitTemplateType.UnitTemplate;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import static org.openbase.bco.registry.remote.Registries.getUnitRegistry;

/**
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SkeletonMergingController extends AbstractEventHandler implements SkeletonMerging, Launchable<Void>, VoidInitializable, Observer<DataProvider<Map<String, RegistryTransformer>>, Map<String, RegistryTransformer>> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SkeletonMergingController.class);

    private final List<String> idRestriction = new ArrayList<>();
    private final List<String> deviceClassList = new ArrayList<>();
    private final Map<Scope, String> scopeIdMap = new HashMap<>();
    private final Map<Scope, FileTransformer> scopeFileTransformerMap = new HashMap<>();
    private RegistrySynchronizer<String, RegistryTransformer, UnitConfig, UnitConfig.Builder> registryTransformerRegistrySynchronizer;
    private SynchronizableRegistryImpl<String, RegistryTransformer> registryTransformerRegistry;

    // Merging stuff:
    private MergingScheduler mergingScheduler;
    private SkeletonMergerInterface merger;
    private boolean mergingEnabled = false;

    private RSBConnection rsbConnection;

    private boolean initialized;
    private boolean active;

    @Override
    public synchronized void handleEvent(Event event) {
        if (!(event.getData() instanceof TrackedPostures3DFloat) || rsbConnection.getOutScope().equals(event.getScope())) {
            return;
        }
        LOGGER.trace("New TrackedPostures3DFloat event received on scope " + event.getScope().toString());
        Optional<Scope> bestScope = event.getScope().superScopes(true).stream()
                .filter(s -> scopeIdMap.containsKey(s) || scopeFileTransformerMap.containsKey(s))
                .sorted((o1, o2) -> o2.toString().length() - o1.toString().length())
                .findFirst();
        if (!bestScope.isPresent()) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("No Transformer registered for the event's scope " + event.getScope().toString()),
                    LOGGER, LogLevel.DEBUG);
            return;
        }
        try {
            Transformer currentTransformer;
            final Scope scope = bestScope.get();
            if (scopeIdMap.containsKey(scope)) {
                LOGGER.trace("Using transformation for scope " + scope.toString() + " unit " + scopeIdMap.get(scope));
                currentTransformer = registryTransformerRegistry.get(scopeIdMap.get(scope));
            } else {
                LOGGER.trace("Using transformation for scope " + scope.toString() + " from file.");
                currentTransformer = scopeFileTransformerMap.get(scope);
            }

            final TrackedPostures3DFloat postures = (TrackedPostures3DFloat) event.getData();
            final TrackedPostures3DFloat transformedPostures = currentTransformer.transform(postures);

            if (mergingEnabled) {
                LOGGER.trace("Passing the transformed postures to the merger.");
                merger.postureUpdate(new PostureFrame(System.currentTimeMillis(), scope, transformedPostures));
                //TODO merge the data here!
//                return;
            } else {
                LOGGER.trace("Creating and sending transformed event.");
                final Event transformedEvent = copyEventMetaData(event);
                transformedEvent.setData(transformedPostures);

                rsbConnection.publishEvent(transformedEvent);
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Processing or sending the transformed postures failed.", ex), LOGGER, LogLevel.WARN);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            ExceptionPrinter.printHistory(new CouldNotPerformException("Sending the transformed postures failed.", ex), LOGGER, LogLevel.ERROR);
        }
    }

    private Event copyEventMetaData(Event event) {
        Event copy = new Event(event.getData().getClass());
        MetaData meta = event.getMetaData();
        MetaData cMeta = copy.getMetaData();
        meta.userInfoKeys().forEach((key) -> {
            cMeta.setUserInfo(key, meta.getUserInfo(key));
        });
        meta.userTimeKeys().forEach((key) -> {
            cMeta.setUserTime(key, meta.getUserTime(key));
        });
        cMeta.setCreateTime(meta.getCreateTime());
        cMeta.setSendTime(meta.getSendTime());
        return copy;
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        if (!initialized) {
            try {
                registryTransformerRegistry = new SynchronizableRegistryImpl<>();
                registryTransformerRegistry.setName("registryTransformers");

                handleJPArguments();

                rsbConnection.init();
                initialized = true;
            } catch (JPValidationException | JPNotAvailableException | CouldNotPerformException ex) {
                throw new InitializationException(SkeletonMergingController.class, ex);
            }
        }
    }

    private void handleJPArguments() throws JPValidationException, JPNotAvailableException, CouldNotPerformException, InterruptedException {
        //TODO: remove this as soon as merging is enabled!
        if (JPService.getProperty(JPFileTransformers.class).isParsed() && JPService.getProperty(JPRegistryTransformers.class).isParsed()
                || JPService.getProperty(JPFileTransformers.class).isParsed() && JPService.getProperty(JPFileTransformers.class).getValue().size() > 1
                || JPService.getProperty(JPRegistryTransformers.class).isParsed() && JPService.getProperty(JPRegistryTransformers.class).getValue().size() > 1) {
            throw new JPValidationException("So far, only one transformer can be specified via -r or -f, as merging is not yet implemented.");
        }

        Scope rawBaseScope = JPService.getProperty(JPRawPostureBaseScope.class).getValue();
        Scope pscBaseScope = JPService.getProperty(JPPSCBaseScope.class).getValue();
        Scope outScope = pscBaseScope.concat(JPService.getProperty(JPPostureScope.class).getValue());

        //TODO: Remove this part!
        if (!(JPService.getProperty(JPFileTransformers.class).isParsed() || JPService.getProperty(JPRegistryTransformers.class).isParsed())) {
            throw new JPValidationException("At least one of --registry-id or --transform-file has to be specified");
        }

        if (JPService.getProperty(JPFileTransformers.class).isParsed()) {
            for (Entry<Scope, File> entry : JPService.getProperty(JPFileTransformers.class).getValue().entrySet()) {
                Scope scope = rawBaseScope.concat(entry.getKey());
                scopeFileTransformerMap.put(scope, new FileTransformer(entry.getValue()));
                LOGGER.info("Registering on scope " + scope.toString() + " Transformer from file " + entry.getValue().getAbsolutePath());
            }
        }

        idRestriction.addAll(JPService.getProperty(JPRegistryTransformers.class).getValue());
        deviceClassList.addAll(JPService.getProperty(JPDeviceClassList.class).getValue());

        checkScopeMaps();
        if (!JPService.getProperty(JPDisableRegistry.class).getValue()) {
            initializeRegistryConnection();
        }

        rsbConnection = new RSBConnection(this, rawBaseScope, outScope);

        //TODO: merging should always be on after it is finalized (remove mergingEnabled variable)
        if (scopeIdMap.size() + scopeFileTransformerMap.size() > 1) {
            mergingEnabled = true;
            merger = new SkeletonMerger(new StabilizerImpl(JPService.getProperty(JPStabilizationFactor.class).getValue()));
            final int frameRate = JPService.getProperty(JPFrameRate.class).getValue();
            mergingScheduler = new MergingScheduler(frameRate, rsbConnection, merger);
        }
    }

    private void checkScopeMaps() throws JPValidationException {
        if (scopeFileTransformerMap.keySet().stream().anyMatch(k -> scopeFileTransformerMap.containsKey(k))) {
            throw new JPValidationException("The same scope occurs multiple times in the file transformers.");
        }
        if (scopeIdMap.keySet().stream().anyMatch(k -> scopeFileTransformerMap.keySet().stream().filter(k2 -> k == k2).count() > 1)) {
            throw new JPValidationException("The same scope appeared in the file transformers and the registry transformers.");
        }
        if (scopeIdMap.keySet().stream().anyMatch(k -> scopeIdMap.keySet().stream().filter(k2 -> k == k2).count() > 1)) {
            throw new JPValidationException("The same scope appeared multiple times in the registry.");
        }
    }

    private void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        try {
            LOGGER.info("Initializing Registry synchronization.");
            Registries.getUnitRegistry().waitForData(3, TimeUnit.SECONDS);

            registryTransformerRegistrySynchronizer = new RegistrySynchronizer<>(
                    registryTransformerRegistry, getUnitRegistry().getUnitConfigRemoteRegistry(), getUnitRegistry(), RegistryTransformerFactory.getInstance());
            registryTransformerRegistrySynchronizer.addFilter(unitConfig -> {
                //TODO: Load Kinects from the registry by a flag or so and device type and get the scopes somehow. Also check enabled state.
                if (!idRestriction.isEmpty() && !idRestriction.contains(unitConfig.getId())) {
                    return true;
                }
                if (unitConfig.getUnitType() != UnitTemplate.UnitType.DEVICE
                        || !deviceClassList.contains(unitConfig.getDeviceConfig().getDeviceClassId())
                        || unitConfig.getMetaConfig().getEntryList().stream().noneMatch(e -> "scope".equals(e.getKey()))
                        || unitConfig.getEnablingState().getValue() != EnablingState.State.ENABLED) {
                    if (!idRestriction.isEmpty()) {
                        LOGGER.warn("Config of specified id " + unitConfig.getId() + " is not applicable for skeleton merging.");
                    }
                    return true;
                }
                try {
                    if (PointingUnitChecker.hasLocationData(unitConfig)) {
                        return false;
                    } else {
                        throw new CouldNotPerformException("Registry Id found in the arguments, but no location data available.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not initialize registry connection", ex), LOGGER, LogLevel.ERROR);
                    return true;
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
                    return true;
                }
            });
            registryTransformerRegistry.addObserver(this);
        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not connect to the registry.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Activating " + getClass().getName() + ".");
        if (!initialized) {
            throw new CouldNotPerformException("Activate can only be called after init.");
        }
        if (!active) {
            Registries.waitForData();
            LOGGER.info("Activating Registry synchronization.");
            registryTransformerRegistrySynchronizer.activate();
            rsbConnection.activate();
            if (mergingEnabled) {
                mergingScheduler.activate();
            }
            active = true;
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Deactivating " + getClass().getName() + ".");
        if (active) {
            if (mergingEnabled) {
                mergingScheduler.deactivate();
            }
            rsbConnection.deactivate();
            LOGGER.info("Deactivating Registry synchronization.");
            registryTransformerRegistrySynchronizer.deactivate();
            active = false;
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public synchronized void update(DataProvider<Map<String, RegistryTransformer>> source, Map<String, RegistryTransformer> data) throws Exception {
        scopeIdMap.clear();
        LOGGER.info("Clearing registry scopes");
        for (Entry<String, RegistryTransformer> e : data.entrySet()) {
            scopeIdMap.put(e.getValue().getScope(), e.getKey());
            LOGGER.info("Registering on scope " + e.getValue().getScope().toString() + " Unit with id " + e.getKey());
        }
        try {
            checkScopeMaps();
        } catch (JPValidationException ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.WARN);
        }
    }

}
