/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.spi;

import org.quartz.SchedulerConfigException;

/**
 * <p>
 * The interface to be implemented by classes that want to provide a thread
 * pool for the <code>{@link org.quartz.core.QuartzScheduler}</code>'s use.
 * </p>
 * 
 * @see org.quartz.core.QuartzScheduler
 * 
 * @author James House
 */
public interface ThreadPool {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Execute the given <code>{@link java.lang.Runnable}</code> in the next
     * available <code>Thread</code>.
     * </p>
     * 
     * <p>
     * The implementation of this interface should not throw exceptions unless
     * there is a serious problem (i.e. a serious misconfiguration). If there
     * are no available threads, rather it should either queue the Runnable, or
     * block until a thread is available, depending on the desired strategy.
     * </p>
     */
    public boolean runInThread(Runnable runnable);

    /**
     * <p>
     * Called by the QuartzScheduler before the <code>ThreadPool</code> is
     * used, in order to give the it a chance to initialize.
     * </p>
     */
    public void initialize() throws SchedulerConfigException;

    /**
     * <p>
     * Called by the QuartzScheduler to inform the <code>ThreadPool</code>
     * that it should free up all of it's resources because the scheduler is
     * shutting down.
     * </p>
     */
    public void shutdown(boolean waitForJobsToComplete);

    public int getPoolSize();
}
