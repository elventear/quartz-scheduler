/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.spi;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * <p>
 * Provides an interface for a class to become a "plugin" to Quartz.
 * </p>
 * 
 * <p>
 * Plugins can do virtually anything you wish, though the most interesting ones
 * will obviously interact with the scheduler in some way - either actively: by
 * invoking actions on the scheduler, or passively: by being a <code>JobListener</code>,
 * <code>TriggerListener</code>, and/or <code>SchedulerListener</code>.
 * </p>
 * 
 * <p>
 * If you use <code>{@link org.quartz.impl.StdSchedulerFactory}</code> to
 * initialize your Scheduler, it can also create and initialize your plugins -
 * look at the configuration docs for details.
 * </p>
 * 
 * @author James House
 */
public interface SchedulerPlugin {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give
     * the <code>SchedulerPlugin</code> a chance to initialize.
     * </p>
     * 
     * <p>
     * At this point, the Scheduler's <code>JobStore</code> is not yet
     * initialized.
     * </p>
     * 
     * @param name
     *          The name by which the plugin is identified.
     * @param scheduler
     *          The scheduler to which the plugin is registered.
     * 
     * @throws SchedulerConfigException
     *           if there is an error initializing.
     */
    public void initialize(String name, Scheduler scheduler)
            throws SchedulerException;

    /**
     * <p>
     * Called when the associated <code>Scheduler</code> is started, in order
     * to let the plug-in know it can now make calls into the scheduler if it
     * needs to.
     * </p>
     */
    public void start();

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it
     * should free up all of it's resources because the scheduler is shutting
     * down.
     * </p>
     */
    public void shutdown();

}
