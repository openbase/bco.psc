package org.openbase.bco.psc.control.jp;

import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPBoolean;

public class JPLegacyMode extends AbstractJPBoolean {
    /**
     * The identifiers that can be used in front of the command line argument.
     */
    public final static String[] COMMAND_IDENTIFIERS = {"--enable-legacy-mode"};

    /**
     * Constructor.
     */
    public JPLegacyMode() {
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
        return "Enables the non multi-modal legacy mode.";
    }

}
