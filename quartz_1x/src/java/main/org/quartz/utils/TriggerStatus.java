/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */

package org.quartz.utils;

import java.util.Date;

/**
 * <p>
 * Object representing a job or trigger key.
 * </p>
 * 
 * @author James House
 */
public class TriggerStatus extends Pair {

    // TODO: Repackage under spi or root pkg ?, put status constants here.
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private Key key;

    private Key jobKey;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Construct a new TriggerStatus with the status name and nextFireTime.
     * 
     * @param status
     *          the trigger's status
     * @param nextFireTime
     *          the next time the trigger will fire
     */
    public TriggerStatus(String status, Date nextFireTime) {
        super();
        super.setFirst(status);
        super.setSecond(nextFireTime);
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * @return
     */
    public Key getJobKey() {
        return jobKey;
    }

    /**
     * @param jobKey
     */
    public void setJobKey(Key jobKey) {
        this.jobKey = jobKey;
    }

    /**
     * @return
     */
    public Key getKey() {
        return key;
    }

    /**
     * @param key
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * <p>
     * Get the name portion of the key.
     * </p>
     * 
     * @return the name
     */
    public String getStatus() {
        return (String) getFirst();
    }

    /**
     * <p>
     * Get the group portion of the key.
     * </p>
     * 
     * @return the group
     */
    public Date getNextFireTime() {
        return (Date) getSecond();
    }

    /**
     * <p>
     * Return the string representation of the TriggerStatus.
     * </p>
     *  
     */
    public String toString() {
        return "status: " + getStatus() + ", next Fire = " + getNextFireTime();
    }
}

// EOF
