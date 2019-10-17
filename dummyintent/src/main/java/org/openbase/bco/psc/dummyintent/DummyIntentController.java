package org.openbase.bco.psc.dummyintent;

/*
 * -
 * #%L
 * BCO PSC Dummy Intent
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

import org.openbase.bco.psc.dummyintent.rsb.RSBConnection;
import org.openbase.bco.registry.remote.Registries;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.RSBFactoryImpl;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.openbase.type.domotic.unit.UnitProbabilityCollectionType;
import org.slf4j.LoggerFactory;
import rsb.AbstractEventHandler;
import rsb.Event;
import rst.dialog.SpeechHypothesisType.SpeechHypothesis;

/**
 * This class controls the dummy intent
 *
 * @author <a href="mailto:dreinsch@techfak.uni-bielefeld.de">Dennis Reinsch</a>
 * @author <a href="mailto:jbitschene@techfak.uni-bielefeld.de">Jennifer Bitschene</a>
 * @author <a href="mailto:jniermann@techfak.uni-bielefeld.de">Julia Niermann</a>
 *
 */
public class DummyIntentController extends AbstractEventHandler implements DummyIntent, Launchable<Void>, VoidInitializable {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DummyIntentController.class);
    private RSBConnection rsbConnection;
    private boolean initialized = false;
    private boolean active = false;
    private RSBInformer<UnitProbabilityCollectionType.UnitProbabilityCollection> unitInformer;

    @Override
    public void handleEvent(final Event event) {
        LOGGER.trace(event.toString());

        if (event.getData() instanceof String) {

            String intent = (String) event.getData(); // should be something like colorstate[color:blau]

            try {

                if (intent.contains("unit")) {
                    intent = intent.replaceFirst("unit", "");

                    // publish unit for testing
                    UnitProbabilityCollectionType.UnitProbabilityCollection.Builder collectionBuilder = UnitProbabilityCollectionType.UnitProbabilityCollection.newBuilder();
                    collectionBuilder.addElementBuilder().setId("47e63f5a-ff30-4b0d-905a-815f94aa8b50").setProbability(1.0f);
                    UnitProbabilityCollectionType.UnitProbabilityCollection unit = collectionBuilder.build();
                    unitInformer.publish(unit);
                    LOGGER.info("PUBLISHED unit");
                }

                // publish SpeechHypothesis for testing
                SpeechHypothesis speechHypothesis = SpeechHypothesis.newBuilder().setGrammarTree(intent).build();
                rsbConnection.publishData(speechHypothesis);
                LOGGER.info("PUBLISHED SpeechHypothesis for testing");


            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
            }
        }
    }

    private void initializeRegistryConnection() throws InterruptedException, CouldNotPerformException {
        try {
            LOGGER.info("Waiting for bco registry synchronization...");
            Registries.getUnitRegistry().waitForData();

        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not connect to the registry.", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("The RegistrySynchronization could not be activated although connection to the registry is possible.", ex);
        }
    }

    @Override
    public void init() throws InitializationException, InterruptedException {
        LOGGER.info("Initializing dummyintent controller...");
        if (!initialized) {
            try {
                initializeRegistryConnection();
                rsbConnection = new RSBConnection(this);
                rsbConnection.init();
                unitInformer = RSBFactoryImpl.getInstance().createSynchronizedInformer("/intents/merge", UnitProbabilityCollectionType.UnitProbabilityCollection.class);

                initialized = true;
            } catch (CouldNotPerformException ex) {
                throw new InitializationException(DummyIntentController.class, ex);
            }
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        LOGGER.debug("Activating " + getClass().getName() + ".");
        if (!initialized) {
            throw new CouldNotPerformException("Activate can only be called after init.");
        }
        if (!active) {
            active = true;
            Registries.waitForData();
            LOGGER.info("Activating Registry synchronization.");
            unitInformer.activate();
            rsbConnection.activate();
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.debug("Deactivating " + getClass().getName() + ".");
        if (active) {
            active = false;
            unitInformer.deactivate();
            rsbConnection.deactivate();
            LOGGER.info("Deactivating Registry synchronization.");
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }


}
