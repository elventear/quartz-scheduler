package prioscrew;

import java.text.ParseException;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class Main
{
	public static void main(String[]  argv)
	{
		Scheduler scheduler = null;
		
		SchedulerFactory factory = new StdSchedulerFactory();
		try
		{
			scheduler = factory.getScheduler();
		} catch (SchedulerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JobDetail JobDetail1 = new JobDetail("Job3",scheduler.DEFAULT_GROUP, PJob.class);
		JobDetail JobDetail2 = new JobDetail("Job1",scheduler.DEFAULT_GROUP,PJob.class);
		JobDetail JobDetail3 = new JobDetail("Job2",scheduler.DEFAULT_GROUP,PJob.class);
		CronTrigger cronTrigger1 = null;
		CronTrigger cronTrigger2 = null;
		CronTrigger cronTrigger3 = null;
		try
		{
			cronTrigger1 = new CronTrigger("Trig3",scheduler.DEFAULT_GROUP,"0/5 * * * * ?");
			cronTrigger1.setPriority(3);
			cronTrigger2 = new CronTrigger("Trig1",scheduler.DEFAULT_GROUP,"0/5 * * * * ?");
			cronTrigger2.setPriority(1);
			cronTrigger3 = new CronTrigger("Trig2",scheduler.DEFAULT_GROUP,"0/5 * * * * ?");
			cronTrigger3.setPriority(2);
		} catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try
		{
			scheduler.scheduleJob(JobDetail1,cronTrigger1);
			scheduler.scheduleJob(JobDetail2,cronTrigger2);
			scheduler.scheduleJob(JobDetail3,cronTrigger3);
		} catch (SchedulerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			scheduler.start();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
