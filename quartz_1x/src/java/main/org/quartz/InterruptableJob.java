/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz;

/**
 * <p>
 * The interface to be implemented by <code>{@link Job}s</code> that provide a 
 * mechanism for having their execution interrupted.  It is NOT a requirment
 * for jobs to implement this interface - in fact, for most people, none of
 * their jobs will.
 * </p>
 * 
 * <p>
 * The means of actually interrupting the Job must be implemented within the
 * <code>Job</code> itself (the <code>interrupt()</code> method of this 
 * interface is simply a means for the scheduler to inform the <code>Job</code>
 * that a request has been made for it to be interrupted). The mechanism that
 * your jobs use to interrupt themselves might vary between implementations.
 * However the principle idea in any implementation should be to have the
 * body of the job's <code>execute(..)</code> periodically check some flag to
 * see if an interruption has been requested, and if the flag is set, somehow
 * abort the performance of the rest of the job's work.  An example of 
 * interrupting a job can be found in the java source for the  class 
 * <code>org.quartz.examples.DumbInterruptableJob</code>.  It is legal to use
 * some combination of <code>wait()</code> and <code>notify()</code> 
 * synchronization within <code>interrupt()</code> and <code>execute(..)</code>
 * in order to have the <code>interrupt()</code> method block until the
 * <code>execute(..)</code> signals that it has noticed the set flag.
 * </p>
 * 
 * @see Job
 * @see StatefulJob
 * @see Scheduler#interrupt(String, String)
 * @see org.quartz.examples.DumbInterruptableJob
 * 
 * @author James House
 */
public interface InterruptableJob extends Job {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a user
     * interrupts the <code>Job</code>.
     * </p>
     * 
     * @return void (nothing) if job interrupt is successful.
     * @throws UnableToInterruptJobException
     *           if there is an exception while interrupting the job.
     */
    public void interrupt()
            throws UnableToInterruptJobException;

    
}
