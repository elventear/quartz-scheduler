/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.spi;

import java.util.Date;

import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;

/**
 * <p>
 * The interface to be implemented by classes that want to provide a mechanism
 * by which the <code>{@link org.quartz.core.QuartzScheduler}</code> can
 * reliably determine the current time.
 * </p>
 * 
 * <p>
 * In general, the default implementation of this interface (<code>{@link org.quartz.simpl.SimpleTimeBroker}</code>-
 * which simply uses <code>System.getCurrentTimeMillis()</code> )is
 * sufficient. However situations may exist where this default scheme is
 * lacking in its robustsness - especially when Quartz is used in a clustered
 * configuration. For example, if one or more of the machines in the cluster
 * has a system time that varies by more than a few seconds from the clocks on
 * the other systems in the cluster, scheduling confusion will result.
 * </p>
 * 
 * @see org.quartz.core.QuartzScheduler
 * 
 * @author James House
 */
public interface TimeBroker {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Get the current time, as known by the <code>TimeBroker</code>.
     * </p>
     * 
     * @throws SchedulerException
     *           with the error code set to
     *           SchedulerException.ERR_TIME_BROKER_FAILURE
     */
    public Date getCurrentTime() throws SchedulerException;

    /**
     * <p>
     * Called by the QuartzScheduler before the <code>TimeBroker</code> is
     * used, in order to give the it a chance to initialize.
     * </p>
     */
    public void initialize() throws SchedulerConfigException;

    /**
     * <p>
     * Called by the QuartzScheduler to inform the <code>TimeBroker</code>
     * that it should free up all of it's resources because the scheduler is
     * shutting down.
     * </p>
     */
    public void shutdown();

}
