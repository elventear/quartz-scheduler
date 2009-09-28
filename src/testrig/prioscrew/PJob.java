package prioscrew;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class PJob implements Job
{

	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		// TODO Auto-generated method stub
		System.out.println("This Job is : " + arg0.getJobDetail().getName() 
				+ " with Priority : " + 
				arg0.getTrigger().getPriority());
	}

}

