package org.openbase.bco.psc.sm.merging;

/*-
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Timer;
import java.util.TimerTask;
import org.openbase.bco.psc.sm.rsb.RSBConnection;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Launchable;
import org.openbase.jul.iface.VoidInitializable;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class MergingScheduler extends TimerTask implements Launchable<Void>, VoidInitializable {

    /**
     * Logger instance.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MergingScheduler.class);

    private final long updatePeriod;
    private final RSBConnection rsbConnection;
    private final SkeletonMergerInterface merger;
    private Timer timer;

    public MergingScheduler(final int frameRate, final RSBConnection rsbConnection, final SkeletonMergerInterface merger) {
        LOGGER.info("Merging Scheduler initialized for the selected framerate of " + frameRate + "/second.");
        this.updatePeriod = 1000 / frameRate;
        this.rsbConnection = rsbConnection;
        this.merger = merger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            if (rsbConnection.isActive()) {
                rsbConnection.publishData(merger.createMergedData());
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Sending the merged postures failed.", ex), LOGGER, LogLevel.ERROR);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            ExceptionPrinter.printHistory(new CouldNotPerformException("Sending the merged postures failed.", ex), LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Activating merging scheduler.");
        timer = new Timer();
        timer.scheduleAtFixedRate(this, 0, updatePeriod);
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        LOGGER.info("Deactivating merging scheduler.");
        timer.cancel();
        timer.purge();
        timer = null;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return timer != null;
    }

}
