/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */

package org.quartz.impl.jdbcjobstore;

import org.quartz.JobPersistenceException;

/**
 * <p>
 * Exception class for when there is a failure obtaining or releasing a
 * resource lock.
 * </p>
 * 
 * @see Semaphore
 * 
 * @author James House
 */
public class LockException extends JobPersistenceException {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public LockException(String msg) {
        super(msg);
    }

    public LockException(String msg, Exception cause) {
        super(msg, cause);
    }
}

// EOF
