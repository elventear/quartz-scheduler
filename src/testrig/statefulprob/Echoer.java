
package statefulprob;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;


/**
 * @author Maciej Świętochowski <m@swietochowski.eu>
 *
 */
public class Echoer implements Job {

	public void execute(JobExecutionContext jex) throws JobExecutionException {
		System.out.println(jex.getJobDetail().getName() + " Executing... " + new Date());
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
