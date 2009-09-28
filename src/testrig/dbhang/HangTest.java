

package dbhang;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;


public class HangTest {

    private static Log _log = LogFactory.getLog(HangTest.class);
    
    public void cleanUp(Scheduler inScheduler) throws Exception {
        _log.warn("***** Deleting existing jobs/triggers *****");

        // unschedule jobs
        String[] groups = inScheduler.getTriggerGroupNames();
        for (int i = 0; i < groups.length; i++) {
            String[] names = inScheduler.getTriggerNames(groups[i]);
            for (int j = 0; j < names.length; j++) {
                inScheduler.unscheduleJob(names[j], groups[i]);
            }
        }

        // delete jobs
        groups = inScheduler.getJobGroupNames();
        for (int i = 0; i < groups.length; i++) {
            String[] names = inScheduler.getJobNames(groups[i]);
            for (int j = 0; j < names.length; j++) {
                inScheduler.deleteJob(names[j], groups[i]);
            }
        }
    }
    
    public void run(boolean inClearJobs, boolean inScheduleJobs) 
        throws Exception {

        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();
        
        if (inClearJobs) {
            cleanUp(sched);
        }

        _log.info("------- Initialization Complete -----------");

        if (inScheduleJobs) {

            _log.info("------- Scheduling Jobs ------------------");

            String schedId = sched.getSchedulerInstanceId();

            int count = 1;

            JobDetail job = new JobDetail("job_" + count, schedId,
                    SimpleRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            SimpleTrigger trigger = 
                new SimpleTrigger("triger_" + count, schedId, 2000, 5000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + (120 * 1000L)));
            _log.info(job.getFullName() +
                    " will run at: " + trigger.getNextFireTime() +  
                    " and repeat: " + trigger.getRepeatCount() + 
                    " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId, 
                    SimpleRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 2000, 5000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + (120 * 1000L)));
            _log.info(job.getFullName() +
                    " will run at: " + trigger.getNextFireTime() +  
                    " and repeat: " + trigger.getRepeatCount() + 
                    " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId,
                    SimpleRecoveryStatefulJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(true);
            trigger = new SimpleTrigger("trig_" + count, schedId, 2000, 3000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + (120 * 1000L)));
            _log.info(job.getFullName() +
                    " will run at: " + trigger.getNextFireTime() +  
                    " and repeat: " + trigger.getRepeatCount() + 
                    " times, every " + trigger.getRepeatInterval() / 1000 + " seconds");
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId, SimpleRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(false);
            trigger = new SimpleTrigger("trig_" + count, schedId, 2000, 4000L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + (120 * 1000L)));
            _log.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);

            count++;
            job = new JobDetail("job_" + count, schedId, SimpleRecoveryJob.class);
            // ask scheduler to re-execute this job if it was in progress when
            // the scheduler went down...
            job.setRequestsRecovery(false);
            trigger = new SimpleTrigger("trig_" + count, schedId, 2000, 4500L);
            trigger.setStartTime(new Date(System.currentTimeMillis() + (120 * 1000L)));
            _log.info(job.getFullName() + " will run at: "
                    + trigger.getNextFireTime() + " & repeat: "
                    + trigger.getRepeatCount() + "/"
                    + trigger.getRepeatInterval());
            sched.scheduleJob(job, trigger);
        }

        // jobs don't start firing until start() has been called...
        _log.info("------- Starting Scheduler ---------------");
        sched.start();
        _log.info("------- Started Scheduler ----------------");

        _log.info("------- Waiting for one hour... ----------");
        try {
            Thread.sleep(3600L * 1000L);
        } catch (Exception e) {
        }

        _log.info("------- Shutting Down --------------------");
        sched.shutdown();
        _log.info("------- Shutdown Complete ----------------");
    }

    public static void main(String[] args) throws Exception {
        boolean clearJobs = false;
        boolean scheduleJobs = true;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("clearJobs")) {
                clearJobs = true;                
            } else if (args[i].equalsIgnoreCase("dontScheduleJobs")) {
                scheduleJobs = false;
            }
        }

        HangTest example = new HangTest();
        example.run(clearJobs, scheduleJobs);
    }
}

