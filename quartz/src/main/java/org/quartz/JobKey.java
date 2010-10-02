package org.quartz;

import org.quartz.utils.Key;

public final class JobKey extends Key<JobKey> {

    public JobKey(String name) {
        super(name, null);
    }

    public JobKey(String name, String group) {
        super(name, group);
    }

    public static JobKey jobKey(String name) {
        return new JobKey(name, null);
    }
    
    public static JobKey jobKey(String name, String group) {
        return new JobKey(name, group);
    }

}
