/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.ee.jta;

import javax.naming.InitialContext;

import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

/**
 * <p>
 * Responsible for creating the instances of <code>{@link org.quartz.ee.jta.JTAJobRunShell}</code>
 * to be used within the <class>{@link org.quartz.core.QuartzScheduler}
 * </code> instance.
 * </p>
 * 
 * <p>
 * This implementation does not re-use any objects, it simply makes a new
 * JTAJobRunShell each time <code>borrowJobRunShell()</code> is called.
 * </p>
 * 
 * @author James House
 */
public class JTAJobRunShellFactory implements JobRunShellFactory {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private Scheduler scheduler;

    private SchedulingContext schedCtxt;

    private InitialContext initCtxt;

    private UserTransactionHelper userTxHelper;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public JTAJobRunShellFactory(UserTransactionHelper userTxHelper) {
        this.userTxHelper = userTxHelper;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Initialize the factory, providing a handle to the <code>Scheduler</code>
     * that should be made available within the <code>JobRunShell</code> and
     * the <code>JobExecutionContext</code> s within it, and a handle to the
     * <code>SchedulingContext</code> that the shell will use in its own
     * operations with the <code>JobStore</code>.
     * </p>
     */
    public void initialize(Scheduler scheduler, SchedulingContext schedCtxt)
            throws SchedulerConfigException {
        this.scheduler = scheduler;
        this.schedCtxt = schedCtxt;
    }

    /**
     * <p>
     * Called by the <class>{@link org.quartz.core.QuartzSchedulerThread}
     * </code> to obtain instances of <code>
     * {@link org.quartz.core.JobRunShell}</code>.
     * </p>
     */
    public JobRunShell borrowJobRunShell() {
        return new JTAJobRunShell(this, scheduler, schedCtxt, userTxHelper);
    }

    /**
     * <p>
     * Called by the <class>{@link org.quartz.core.QuartzSchedulerThread}
     * </code> to return instances of <code>
     * {@link org.quartz.core.JobRunShell}</code>.
     * </p>
     */
    public void returnJobRunShell(JobRunShell jobRunShell) {
        jobRunShell.passivate();
    }

}