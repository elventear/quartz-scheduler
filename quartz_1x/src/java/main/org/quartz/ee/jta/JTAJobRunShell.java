/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.ee.jta;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.core.JobRunShell;
import org.quartz.core.JobRunShellFactory;
import org.quartz.core.SchedulingContext;

/**
 * <p>
 * An extension of <code>{@link org.quartz.core.JobRunShell}</code> that
 * begins an XA transaction before executing the Job, and commits (or
 * rolls-back) the transaction after execution completes.
 * </p>
 * 
 * @see org.quartz.core.JobRunShell
 * 
 * @author James House
 */
public class JTAJobRunShell extends JobRunShell {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private UserTransaction ut;

    private UserTransactionHelper userTxHelper;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a JTAJobRunShell instance with the given settings.
     * </p>
     */
    public JTAJobRunShell(JobRunShellFactory jobRunShellFactory,
            Scheduler scheduler, SchedulingContext schdCtxt,
            UserTransactionHelper userTxHelper) {
        super(jobRunShellFactory, scheduler, schdCtxt);

        this.userTxHelper = userTxHelper;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected void begin() throws SchedulerException {
        try {
            ut = userTxHelper.lookup();

            ut.begin();
        } catch (SchedulerException se) {
            throw se;
        } catch (Exception nse) {

            throw new SchedulerException(
                    "JTAJobRunShell could not start UserTransaction.", nse);
        }
    }

    protected void complete(boolean successfulExecution)
            throws SchedulerException {

        if (ut == null) return;

        try {
            if (ut.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                    successfulExecution = false;
        } catch (SystemException e) {
            throw new SchedulerException(
                    "JTAJobRunShell could not read UserTransaction status.", e);
        }

        if (successfulExecution) {
            try {
                ut.commit();
            } catch (Exception nse) {
                throw new SchedulerException(
                        "JTAJobRunShell could not commit UserTransaction.", nse);
            }
        } else {
            try {
                ut.rollback();
            } catch (Exception nse) {
                throw new SchedulerException(
                        "JTAJobRunShell could not rollback UserTransaction.",
                        nse);
            }
        }

        ut = null;
    }

}