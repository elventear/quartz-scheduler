/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz;

/**
 * <p>
 * An exception that is thrown to indicate that there has been a failure in the
 * scheduler's underlying persistence mechanism.
 * </p>
 * 
 * @author James House
 */
public class JobPersistenceException extends SchedulerException {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a <code>JobPersistenceException</code> with the given message.
     * </p>
     */
    public JobPersistenceException(String msg) {
        super(msg);
        setErrorCode(ERR_PERSISTENCE);
    }

    /**
     * <p>
     * Create a <code>JobPersistenceException</code> with the given message
     * and error code.
     * </p>
     */
    public JobPersistenceException(String msg, int errCode) {
        super(msg, errCode);
    }

    /**
     * <p>
     * Create a <code>JobPersistenceException</code> with the given message
     * and cause.
     * </p>
     */
    public JobPersistenceException(String msg, Exception cause) {
        super(msg, cause);
        setErrorCode(ERR_PERSISTENCE);
    }

    /**
     * <p>
     * Create a <code>JobPersistenceException</code> with the given message,
     * cause and error code.
     * </p>
     */
    public JobPersistenceException(String msg, Exception cause, int errorCode) {
        super(msg, cause, errorCode);
    }

}
