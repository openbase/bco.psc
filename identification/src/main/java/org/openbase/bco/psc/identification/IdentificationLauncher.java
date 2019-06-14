package org.openbase.bco.psc.identification;

/*
 * -
 * #%L
 * BCO PSC Identification
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
import org.openbase.bco.psc.identification.jp.JPDistanceType;
import org.openbase.bco.psc.identification.jp.JPIdentificationThreshold;
import org.openbase.bco.psc.identification.jp.JPUnitSelectorType;
import org.openbase.bco.psc.lib.jp.*;
import org.openbase.bco.psc.lib.jp.JPIntentScope;
import org.openbase.bco.authentication.lib.BCO;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.pattern.launch.AbstractLauncher;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class IdentificationLauncher extends AbstractLauncher<IdentificationController> {

    public IdentificationLauncher() throws InstantiationException {
        super(Identification.class, IdentificationController.class);
    }

    @Override
    protected void loadProperties() {
        // Scopes
        JPService.registerProperty(JPPSCBaseScope.class);
        JPService.registerProperty(JPRayScope.class);
        JPService.registerProperty(JPIntentScope.class);

        // Threshold
        JPService.registerProperty(JPIdentificationThreshold.class);

        // Unit filter
        JPService.registerProperty(JPPscUnitFilterList.class);

        // Component specific
        JPService.registerProperty(JPUnitSelectorType.class);
        JPService.registerProperty(JPDistanceType.class);

        // Transport specification
        JPService.registerProperty(JPLocalInput.class);
        JPService.registerProperty(JPLocalOutput.class);
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public static void main(final String[] args) throws InterruptedException, CouldNotPerformException {
        BCO.printLogo();
        AbstractLauncher.main(args, Identification.class, IdentificationLauncher.class);

        //TODO: Remove this!
//        try {
//            DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(UnitProbabilityCollection.getDefaultInstance()));
//            Scope outScope = JPService.getProperty(JPPSCBaseScope.class).getValue().concat(JPService.getProperty(JPIntentScope.class).getValue());
//            RSBInformer<UnitProbabilityCollection> informer = RSBFactoryImpl.getInstance().createSynchronizedInformer(outScope, UnitProbabilityCollection.class);
//            informer.activate();
//            System.out.println("sending unit");
//            informer.publish(UnitProbabilityCollectionType.UnitProbabilityCollection.newBuilder().addElement(
//                    UnitProbabilityType.UnitProbability.newBuilder().setId("fab86638-192e-4231-a813-25a920a08089").setProbability(1.0f)
//            //                    UnitProbabilityType.UnitProbability.newBuilder().setId("f1397800-9741-401d-a46f-8bf139c12e92").setProbability(1.0f)
//            //                        UnitProbability.newBuilder().setId("c8b2bfb5-45d9-4a2b-9994-d4062ab19cab").setProbability(1.0f)
//            //                    UnitProbability.newBuilder().setId("c8b2bfb5-45da9-4a2b-9994-d4062ab19cab").setProbability(1.0f)
//            //                    UnitProbability.newBuilder().setId("2c95255e-a491-46d7-a6a6-f66d5e6c2d3b").setProbability(1.0f)
//            //                        UnitProbability.newBuilder().setId("8f7b2513-4f33-4e8d-8b7e-eced4d54108c").setProbability(0.66f)
//            ).build());
//            informer.deactivate();
//            System.exit(0);
//        } catch (JPNotAvailableException ex) {
//            throw new CouldNotPerformException("could not send", ex);
//        }
    }
}
