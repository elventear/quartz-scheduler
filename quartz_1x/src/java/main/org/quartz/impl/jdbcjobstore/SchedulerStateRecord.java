/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.impl.jdbcjobstore;

/**
 * <p>
 * Conveys a scheduler-instance state record.
 * </p>
 * 
 * @author James House
 */
public class SchedulerStateRecord implements java.io.Serializable {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String schedulerInstanceId;

    private long checkinTimestamp;

    private long checkinInterval;

    private String recoverer;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     */
    public long getCheckinInterval() {
        return checkinInterval;
    }

    /**
     */
    public long getCheckinTimestamp() {
        return checkinTimestamp;
    }

    /**
     */
    public String getRecoverer() {
        return recoverer;
    }

    /**
     */
    public String getSchedulerInstanceId() {
        return schedulerInstanceId;
    }

    /**
     */
    public void setCheckinInterval(long l) {
        checkinInterval = l;
    }

    /**
     */
    public void setCheckinTimestamp(long l) {
        checkinTimestamp = l;
    }

    /**
     */
    public void setRecoverer(String string) {
        recoverer = string;
    }

    /**
     */
    public void setSchedulerInstanceId(String string) {
        schedulerInstanceId = string;
    }

}

// EOF
