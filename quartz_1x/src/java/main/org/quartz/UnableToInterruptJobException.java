/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */

package org.quartz;

/**
 * <p>
 * An exception that is thrown to indicate that a call to 
 * InterruptableJob.interrupt() failed without interrupting the Job.
 * </p>
 * 
 * @see org.quartz.InterruptableJob#interrupt()
 * 
 * @author James House
 */
public class UnableToInterruptJobException extends SchedulerException {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a <code>UnableToInterruptJobException</code> with the given message.
     * </p>
     */
    public UnableToInterruptJobException(String msg) {
        super(msg);
    }
    
    /**
     * <p>
     * Create a <code>UnableToInterruptJobException</code> with the given cause.
     * </p>
     */
    public UnableToInterruptJobException(Exception cause) {
        super(cause);
    }
    
}
