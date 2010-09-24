package org.quartz;

import org.quartz.utils.Key;

public class JobKey extends Key<Job> {

    public JobKey(String name, String group) {
        super(name, group);
    }

    public JobKey(JobDetail job) {
        super(job.getName(), job.getGroup());
    }
}
