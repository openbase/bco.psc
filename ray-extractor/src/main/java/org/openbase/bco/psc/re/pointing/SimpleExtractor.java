package org.openbase.bco.psc.re.pointing;

/*
 * #%L
 * BCO PSC Ray Extractor
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
import java.util.List;
import java.util.stream.Collectors;
import org.openbase.bco.psc.re.pointing.selectors.RaySelectorInterface;
import static org.openbase.bco.psc.lib.pointing.PostureFunctions.*;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.type.tracking.PointingRay3DFloatDistributionType.PointingRay3DFloatDistribution;
import org.openbase.type.tracking.TrackedPostures3DFloatType.TrackedPostures3DFloat;

/**
 * The simple extractor returns pointing rays that always have the probability 1 and should not be used.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SimpleExtractor extends AbstractRayExtractor {

    /**
     * The last postures provided in the updatePostures method.
     */
    private TrackedPostures3DFloat lastPostures;

    /**
     * Constructor.
     *
     * @param raySelector The ray selector that is used to select the correct rays.
     */
    public SimpleExtractor(RaySelectorInterface raySelector) {
        super(raySelector);
    }

    /**
     * {@inheritDoc}
     *
     * @param postures {@inheritDoc}
     */
    @Override
    public synchronized void updatePostures(TrackedPostures3DFloat postures) {
        lastPostures = postures;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public synchronized List<PointingRay3DFloatDistribution> getPointingRays() throws NotAvailableException {
        if (lastPostures == null) {
            throw new NotAvailableException("Pointing Rays");
        }
        return lastPostures.getPostureList().stream()
                .filter(posture -> checkPosture(posture))
                .map(posture -> getRays(posture, 1, 1))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
