/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz;

/**
 * <p>
 * An exception that is thrown to indicate that there is a misconfiguration of
 * the <code>SchedulerFactory</code>- or one of the components it
 * configures.
 * </p>
 * 
 * @author James House
 */
public class SchedulerConfigException extends SchedulerException {

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
    public SchedulerConfigException(String msg) {
        super(msg, ERR_BAD_CONFIGURATION);
    }

    /**
     * <p>
     * Create a <code>JobPersistenceException</code> with the given message
     * and cause.
     * </p>
     */
    public SchedulerConfigException(String msg, Exception cause) {
        super(msg, cause);
        setErrorCode(ERR_BAD_CONFIGURATION);
    }

}
