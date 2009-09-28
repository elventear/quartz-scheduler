

package threadlock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;


public class ThreadLockTest {

    private static Log _log = LogFactory.getLog(ThreadLockTest.class);
    
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

            for(int count=0; count < 11; count++) {

	            JobDetail job = new JobDetail("job_" + count, "foo",
	                    SimpleLongJob.class);
	            // ask scheduler to re-execute this job if it was in progress when
	            // the scheduler went down...
	            job.setRequestsRecovery(false);
	            job.setDurability(true);
	            
	            sched.addJob(job, true);
            }
        }

        // jobs don't start firing until start() has been called...
        _log.info("------- Starting Scheduler ---------------");
        sched.start();
        _log.info("------- Started Scheduler ----------------");

        _log.info("------- Triggering Jobs... ----------------");

        String[] groups = sched.getJobGroupNames();
        
        for(int k =0; k < 3000; k++) {
	        for(int i=0; i < groups.length; i++) {
	        	String[] names = sched.getJobNames(groups[i]);
	            for(int j=0; j < names.length; j++) {
	               sched.triggerJob(names[j], groups[i]);
	            }
	        } 
	        if(k%100 == 0) {
	        	System.out.println("iterations complete: " + k);
	        	Thread.sleep(5000L);
	        }
        }
        
        _log.info("------- Waiting for one hour... ----------");
        try {
            Thread.sleep(7200L * 1000L);
        } catch (Exception e) {
        }

        _log.info("------- Shutting Down --------------------");
        sched.shutdown();
        _log.info("------- Shutdown Complete ----------------");
    }

    public static void main(String[] args) throws Exception {
        boolean clearJobs = true;
        boolean scheduleJobs = true;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("clearJobs")) {
                clearJobs = true;                
            } else if (args[i].equalsIgnoreCase("dontScheduleJobs")) {
                scheduleJobs = false;
            }
        }

        ThreadLockTest example = new ThreadLockTest();
        example.run(clearJobs, scheduleJobs);
    }
}

