/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz;

/**
 * <p>
 * A marker interface for <code>{@link org.quartz.JobDetail}</code> s that
 * wish to have their state maintained between executions.
 * </p>
 * 
 * <p>
 * <code>StatefulJob</code> instances follow slightly different rules from
 * regular <code>Job</code> instances. The key difference is that their
 * associated <code>{@link JobDataMap}</code> is re-persisted after every
 * execution of the job, thus preserving state for the next execution. The
 * other difference is that stateful jobs are not allowed to execute
 * concurrently, which means new triggers that occur before the completion of
 * the <code>execute(xx)</code> method will be delayed.
 * </p>
 * 
 * @see Job
 * @see JobDetail
 * @see JobDataMap
 * @see Scheduler
 * 
 * @author James House
 */
public interface StatefulJob extends Job {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

}
