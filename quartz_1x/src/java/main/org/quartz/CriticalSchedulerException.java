/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */

package org.quartz;

/**
 * <p>
 * An exception that is thrown to indicate that there has been a critical
 * failure within the scheduler's core services (such as loss of database
 * connectivity).
 * </p>
 * 
 * @author James House
 */
public class CriticalSchedulerException extends SchedulerException {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a <code>CriticalSchedulerException</code> with the given message.
     * </p>
     */
    public CriticalSchedulerException(String msg, int errCode) {
        super(msg);
        setErrorCode(errCode);
    }
}
