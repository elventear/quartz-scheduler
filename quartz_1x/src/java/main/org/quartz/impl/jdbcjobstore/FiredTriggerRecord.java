/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.impl.jdbcjobstore;

import org.quartz.utils.Key;

/**
 * <p>
 * Conveys the state of a fired-trigger record.
 * </p>
 * 
 * @author James House
 */
public class FiredTriggerRecord implements java.io.Serializable {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String fireInstanceId;

    private long fireTimestamp;

    private String schedulerInstanceId;

    private Key triggerKey;

    private String fireInstanceState;

    private boolean triggerIsVolatile;

    private Key jobKey;

    private boolean jobIsStateful;

    private boolean jobRequestsRecovery;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public String getFireInstanceId() {
        return fireInstanceId;
    }

    public long getFireTimestamp() {
        return fireTimestamp;
    }

    public boolean isJobIsStateful() {
        return jobIsStateful;
    }

    public Key getJobKey() {
        return jobKey;
    }

    public String getSchedulerInstanceId() {
        return schedulerInstanceId;
    }

    public Key getTriggerKey() {
        return triggerKey;
    }

    public String getFireInstanceState() {
        return fireInstanceState;
    }

    public void setFireInstanceId(String string) {
        fireInstanceId = string;
    }

    public void setFireTimestamp(long l) {
        fireTimestamp = l;
    }

    public void setJobIsStateful(boolean b) {
        jobIsStateful = b;
    }

    public void setJobKey(Key key) {
        jobKey = key;
    }

    public void setSchedulerInstanceId(String string) {
        schedulerInstanceId = string;
    }

    public void setTriggerKey(Key key) {
        triggerKey = key;
    }

    public void setFireInstanceState(String string) {
        fireInstanceState = string;
    }

    public boolean isJobRequestsRecovery() {
        return jobRequestsRecovery;
    }

    public boolean isTriggerIsVolatile() {
        return triggerIsVolatile;
    }

    public void setJobRequestsRecovery(boolean b) {
        jobRequestsRecovery = b;
    }

    public void setTriggerIsVolatile(boolean b) {
        triggerIsVolatile = b;
    }

}

// EOF
