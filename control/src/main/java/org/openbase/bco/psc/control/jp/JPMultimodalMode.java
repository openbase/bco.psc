package org.openbase.bco.psc.control.jp;

/*-
 * #%L
 * BCO PSC Control
 * %%
 * Copyright (C) 2016 - 2020 openbase.org
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

import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPBoolean;
/**
 * JavaProperty used to enable the multimodal mode.
 *
 * @author <a href="mailto:dreinsch@techfak.uni-bielefeld.de">Dennis Reinsch</a>
 * @author <a href="mailto:jbitschene@techfak.uni-bielefeld.de">Jennifer Bitschene</a>
 * @author <a href="mailto:jniermann@techfak.uni-bielefeld.de">Julia Niermann</a>
 */
public class JPMultimodalMode extends AbstractJPBoolean {
    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--enable-multimodal-mode"};

    /**
     * Constructor.
     */
    public JPMultimodalMode() {
        super(COMMAND_IDENTIFIERS);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws JPNotAvailableException {@inheritDoc}
     */
    @Override
    protected Boolean getPropertyDefaultValue() throws JPNotAvailableException {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Enables the multi-modal mode.";
    }

}
