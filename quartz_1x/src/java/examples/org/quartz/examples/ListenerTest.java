/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author James House
 */
public class ListenerTest {

    public static void schedTest(SchedulerFactory sf) throws Exception {
        Log lg = LogFactory.getLog(ListenerTest.class);

        lg.info("------- Initializing -------------------");

        Scheduler sched = sf.getScheduler();

        // 'global' listeners are called when ANY job is executed
        sched.addGlobalJobListener(new MyJobListener("myListener1"));
        // 'non-global' listeners are called when jobs that register for them
        // are executed. (see job creation below)
        sched.addJobListener(new MyJobListener("myListener2"));

        // remove entries that may be lingering in a JDBCJobStore from previous
        // run of this example... (obviously this doesn't matter for
        // RAMJobStore)
        sched.unscheduleJob("trigg1", "group1");

        lg.info("------- Initialization Complete -----------");

        lg.info("------- Scheduling Jobs -----------");

        // jobs can be scheduled before start() has been called

        long ts = System.currentTimeMillis() + 4000l; // get time a few secons
                                                      // in the future...

        JobDetail job = new JobDetail("job1", "group1", DumbJob.class);
        // register JobListener for this job (more than 1 can be registered if
        // you wish)
        job.addJobListener("myListener2");
        SimpleTrigger trigger = new SimpleTrigger("trigg1", "group1", "job1",
                "group1", new Date(ts), null, 0, 0);
        lg.info(job.getFullName() + " will run at: "
                + trigger.getNextFireTime() + " & repeat: "
                + trigger.getRepeatCount() + "/" + trigger.getRepeatInterval());
        sched.scheduleJob(job, trigger);

        job = new JobDetail("job2", "group1", DumbJob.class);
        // this job does not register for 'myListener2' so it will not be
        // notified...
        trigger = new SimpleTrigger("trigg2", "group1", "job2", "group1",
                new Date(ts + 4000l), null, 0, 0);
        lg.info(job.getFullName() + " will run at: "
                + trigger.getNextFireTime() + " & repeat: "
                + trigger.getRepeatCount() + "/" + trigger.getRepeatInterval());
        sched.scheduleJob(job, trigger);

        lg.info("------- Starting Scheduler ----------------");

        // jobs don't start firing until start() has been called...

        sched.start();

        lg.info("------- Started Scheduler -----------------");

        lg.info("------- Waiting... -----------------------");

        try {
            Thread.sleep(30l * 1000l);
        } catch (Exception e) {
        }

        lg.info("------- Shutting Down ---------------------");

        sched.shutdown();

        lg.info("------- Shutdown Complete -----------------");
    }

    public static void main(String[] args) throws Exception {
        try {

            // Configure Log4J
            // org.apache.log4j.PropertyConfigurator.configure(System.getProperty("log4jConfigFile",
            // "log4j.properties"));

            schedTest(new org.quartz.impl.StdSchedulerFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MyJobListener implements JobListener {

    String name;

    public MyJobListener(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void jobToBeExecuted(JobExecutionContext context) {
        System.err.println(".......(" + name + "): jobToBeExecuted: "
                + context.getJobDetail().getFullName());
    }

    public void jobWasExecuted(JobExecutionContext context,
            JobExecutionException jobException) {
        System.err.println(".......(" + name + "): jobWasExecuted: "
                + context.getJobDetail().getFullName());
    }

    /** 
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    public void jobExecutionVetoed(JobExecutionContext context) {
         // TODO Auto-generated method stub
    }

}