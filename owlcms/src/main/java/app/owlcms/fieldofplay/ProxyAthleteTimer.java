/***
 * Copyright (c) 2009-2019 Jean-François Lamy
 *
 * Licensed under the Non-Profit Open Software License version 3.0  ("Non-Profit OSL" 3.0)
 * License text at https://github.com/jflamy/owlcms4/blob/master/LICENSE.txt
 */
package app.owlcms.fieldofplay;

import org.slf4j.LoggerFactory;

import app.owlcms.utils.LoggerUtils;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Class ProxyBreakTimer. Relay timer instructions from {@link FieldOfPlay} to the actual timers
 * associated with each screen. Memorize the elapsed time and timer state.
 *
 * @author Jean-François Lamy
 */
public class ProxyAthleteTimer implements IProxyTimer {

    final private Logger logger = (Logger) LoggerFactory.getLogger(ProxyAthleteTimer.class);
    {
        logger.setLevel(Level.DEBUG);
    }

    private int timeRemaining;
    private FieldOfPlay fop;
    private long startMillis;
    private long stopMillis;
    private boolean running = false;
    private int timeRemainingAtLastStop;

    /**
     * Instantiates a new countdown timer.
     *
     * @param fop
     */
    public ProxyAthleteTimer(FieldOfPlay fop) {
        this.fop = fop;
    }

    @Override
    public void finalWarning(Object origin) {
        fop.emitFinalWarning();
    }

    /* (non-Javadoc)
     * @see app.owlcms.tests.ICountDownTimer#getTimeRemaining() */
    @Override
    public int getTimeRemaining() {
        return timeRemaining;
    }

    @Override
    public int getTimeRemainingAtLastStop() {
        return timeRemainingAtLastStop;
    }

    @Override
    public void initialWarning(Object origin) {
        fop.emitInitialWarning();
    }

    /* (non-Javadoc)
     * @see app.owlcms.fieldofplay.IProxyTimer#setTimeRemaining(int) */
    @Override
    public void setTimeRemaining(int timeRemaining) {
        if (running) {
            computeTimeRemaining();
        }
        logger.debug("setting Time -- timeRemaining = {} [{}]", timeRemaining, LoggerUtils.whereFrom()); //$NON-NLS-1$
        this.timeRemaining = timeRemaining;
        fop.getUiEventBus().post(new UIEvent.SetTime(timeRemaining, null));
        running = false;
    }

    /* (non-Javadoc)
     * @see app.owlcms.tests.ICountDownTimer#start() */
    @Override
    public void start() {
        if (!running) {
            startMillis = System.currentTimeMillis();
            logger.debug("starting Time -- timeRemaining = {} [{}]", timeRemaining, LoggerUtils.whereFrom()); //$NON-NLS-1$
            timeRemainingAtLastStop = timeRemaining;
        }
        fop.getUiEventBus().post(new UIEvent.StartTime(timeRemaining, null));
        running = true;
    }

    /* (non-Javadoc)
     * @see app.owlcms.tests.ICountDownTimer#stop() */
    @Override
    public void stop() {
        if (running) {
            computeTimeRemaining();
        }
        logger.trace("***stopping Time -- timeRemaining = {} [{}]", timeRemaining, LoggerUtils.whereFrom()); //$NON-NLS-1$
        timeRemainingAtLastStop = timeRemaining;
        fop.getUiEventBus().post(new UIEvent.StopTime(timeRemaining, null));
        running = false;
    }

    @Override
    public void timeOver(Object origin) {
        if (running) {
            this.stop();
        }
        fop.emitTimeOver();
        fop.getFopEventBus().post(new FOPEvent.TimeOver(origin));
    }

    /**
     * Compute time elapsed since start and adjust time remaining accordingly.
     */
    private void computeTimeRemaining() {
        stopMillis = System.currentTimeMillis();
        long elapsed = stopMillis - startMillis;
        timeRemaining = (int) (timeRemaining - elapsed);
    }

}
