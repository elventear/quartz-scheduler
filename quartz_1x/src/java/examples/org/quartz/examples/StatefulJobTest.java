/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleTrigger;
import org.quartz.helpers.TriggerUtils;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * Demonstrates the behavior of <code>StatefulJob</code>s, as well as how
 * misfire instructions affect the firings of triggers of <code>StatefulJob</code>
 * s - when the jobs take longer to execute that the frequency of the trigger's
 * repitition.
 * 
 * <p>
 * While the example is running, you should note that there are two triggers
 * with identical schedules, firing identical jobs. The triggers "want" to fire
 * every 3 seconds, but the jobs take 10 seconds to execute. Therefore, by the
 * time the jobs complete their execution, the triggers have already "misfired"
 * (unless the scheduler's "misfire threshold" has been set to more than 7
 * seconds). You should see that one of the jobs has its misfire instruction
 * set to <code>SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT</code>-
 * which causes it to fire immediately, when the misfire is detected. The other
 * trigger uses the default "smart policy" misfire instruction, which causes
 * the trigger to advance to its next fire time (skipping those that it has
 * missed) - so that it does not refire immediately, but rather at the next
 * scheduled time.
 * </p>
 * 
 * @author James House
 */
public class StatefulJobTest {

    public static void schedTest(SchedulerFactory sf) throws Exception {
        Log lg = LogFactory.getLog(StatefulJobTest.class);

        lg.info("------- Initializing -------------------");

        Scheduler sched = sf.getScheduler();

        lg.info("------- Initializing 2 -------------------");

        // remove job/trigger entries that may be lingering in a JDBCJobStore
        // from
        // previous run of this example... (obviously this doesn't matter for
        // RAMJobStore -- but is necessary for JDBCJobStore because this
        // example
        // program is 'dumb' - it blindly inserts the same jobs every time it
        // executes - even though they may still be in the JobStore)
        String[] groups = sched.getTriggerGroupNames();
        for (int i = 0; i < groups.length; i++) {
            String[] names = sched.getTriggerNames(groups[i]);
            for (int j = 0; j < names.length; j++)
                sched.unscheduleJob(names[j], groups[i]);
        }
        groups = sched.getJobGroupNames();
        for (int i = 0; i < groups.length; i++) {
            String[] names = sched.getJobNames(groups[i]);
            for (int j = 0; j < names.length; j++)
                sched.deleteJob(names[j], groups[i]);
        }

        lg.info("------- Initialization Complete -----------");

        lg.info("------- Scheduling Jobs -----------");

        // jobs can be scheduled before start() has been called

        long ts = TriggerUtils.getNextGivenSecondDate(null, 5).getTime(); // get
                                                                          // a
                                                                          // 'nice
                                                                          // round'
                                                                          // time
                                                                          // a
                                                                          // few
                                                                          // seconds
                                                                          // in
                                                                          // the
                                                                          // future...

        JobDetail job = new JobDetail("statefulJob1", "group1",
                StatefulDumbJob.class);
        job.getJobDataMap().put(StatefulDumbJob.EXECUTION_DELAY, 10000l);
        SimpleTrigger trigger = new SimpleTrigger("trigg1", "group1", new Date(
                ts), null, SimpleTrigger.REPEAT_INDEFINITELY, 3000l);
        Date ft = sched.scheduleJob(job, trigger);
        lg.info(job.getFullName() + " will run at: " + ft + " & repeat: "
                + trigger.getRepeatCount() + "/" + trigger.getRepeatInterval());

        job = new JobDetail("statefulJob2", "group1", StatefulDumbJob.class);
        job.getJobDataMap().put(StatefulDumbJob.EXECUTION_DELAY, 10000l);
        trigger = new SimpleTrigger("trigg2", "group1", new Date(ts), null,
                SimpleTrigger.REPEAT_INDEFINITELY, 3000l);
        trigger
                .setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT);
        ft = sched.scheduleJob(job, trigger);
        lg.info(job.getFullName() + " will run at: " + ft + " & repeat: "
                + trigger.getRepeatCount() + "/" + trigger.getRepeatInterval());

        lg.info("------- Starting Scheduler ----------------");

        // jobs don't start firing until start() has been called...

        sched.start();

        lg.info("------- Started Scheduler -----------------");

        try {
            Thread.sleep(600l * 1000l); // sleep 10 minutes for triggers to
                                        // fire....
        } catch (Exception e) {
        }

        lg.info("------- Shutting Down ---------------------");

        sched.shutdown(true);

        lg.info("------- Shutdown Complete -----------------");
        SchedulerMetaData metaData = sched.getMetaData();
        lg.info("Executed " + metaData.numJobsExecuted() + " jobs.");

    }

    public static void main(String[] args) throws Exception {

        // Configure Log4J
        // org.apache.log4j.PropertyConfigurator.configure(System.getProperty("log4jConfigFile",
        // "log4j.properties"));

        try {
            schedTest(new org.quartz.impl.StdSchedulerFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}