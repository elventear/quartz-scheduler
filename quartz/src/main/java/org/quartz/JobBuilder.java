package org.quartz;

import java.util.Date;
import java.util.Random;

import org.quartz.impl.JobDetailImpl;
import org.quartz.jobs.NoOpJob;
import org.quartz.utils.Key;

public class JobBuilder {

    private JobKey key;
    private String description;
    private Class<? extends Job> jobClass = NoOpJob.class;
    private boolean durability;
    private boolean shouldRecover;
    
    private JobDataMap jobDataMap = new JobDataMap();
    
    private JobBuilder() {
    }
    
    public static JobBuilder newJob() {
        return new JobBuilder();
    }
    
    public static JobBuilder newJob(Class <? extends Job> jobClass) {
        JobBuilder b = new JobBuilder();
        b.ofType(jobClass);
        return b;
    }

    public JobDetail build() {

        JobDetailImpl job = new JobDetailImpl();
        
        job.setDescription(description);
        if(key == null)
            key = new JobKey(Key.createUniqueName(null), null);
        job.setKey(key); 
        job.setDurability(durability);
        job.setRequestsRecovery(shouldRecover);
        
        if(!jobDataMap.isEmpty())
            job.setJobDataMap(jobDataMap);
        
        return job;
    }
    
    public JobBuilder withIdentity(String name) {
        key = new JobKey(name, null);
        return this;
    }  
    
    public JobBuilder withIdentity(String name, String group) {
        key = new JobKey(name, group);
        return this;
    }
    
    public JobBuilder withIdentity(JobKey key) {
        this.key = key;
        return this;
    }
    
    public JobBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public JobBuilder ofType(Class <? extends Job> jobClass) {
        this.jobClass = jobClass;
        return this;
    }
    
    public JobBuilder requestRecovery() {
        this.shouldRecover = true;
        return this;
    }
    
    public JobBuilder requestRecovery(boolean shouldRecover) {
        this.shouldRecover = shouldRecover;
        return this;
    }
    
    public JobBuilder storeDurably() {
        this.durability = true;
        return this;
    }
    
    public JobBuilder storeDurably(boolean durability) {
        this.durability = durability;
        return this;
    }
    
    public JobBuilder usingJobData(String key, String value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public JobBuilder usingJobData(String key, Integer value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public JobBuilder usingJobData(String key, Long value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public JobBuilder usingJobData(String key, Float value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public JobBuilder usingJobData(String key, Double value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public JobBuilder usingJobData(String key, Boolean value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public JobBuilder usingJobData(JobDataMap newJobDataMap) {
        // add any existing data to this new map
        for(Object key: jobDataMap.keySet()) {
            newJobDataMap.put(key, jobDataMap.get(key));
        }
        jobDataMap = newJobDataMap; // set new map as the map to use
        return this;
    }
    
}
