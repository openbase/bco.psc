package org.openbase.bco.psc.re.pointing.selectors;

/*-
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class PolynomialSelectorDegree5 extends AbstractPolynomialSelector {
    private static final double[] PARAMETERS = new double[]{-5.20475862e-10, 2.08743028e-07, -3.48681176e-05, 2.90782728e-03, -9.66157841e-02, 4.20892295e-01};
    
    @Override
    protected double[] getParameters() {
        return PARAMETERS;
    }
}
