
package statefulprob;


import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;


/**
 * @author Maciej Świętochowski <m@swietochowski.eu>
 *
 */
public class QuartzTest {

	public static void main(String[] args) {
		
		try {
			//SchedulerFactory schedFactory = new StdSchedulerFactory("statefulprob.properties");
			SchedulerFactory schedFactory = new StdSchedulerFactory();
			Scheduler sched = schedFactory.getScheduler();
			JobDetail jd1 = new JobDetail("zeroJob10112", "foo", Echoer.class);
			JobDetail jd2 = new JobDetail("zeroJob30a112", "foo", Echoer.class);
			JobDetail jd3 = new JobDetail("zeroJob30b112", "foo", Echoer.class);
			Trigger tr1 = new CronTrigger("zeroTr10112", "foo", "0/10 * * * * ?");
			Trigger tr2 = new CronTrigger("zeroTr30a112", "foo", "0/30 * * * * ?");
			Trigger tr3 = new CronTrigger("zeroTr30b112", "foo", "0/30 * * * * ?");
			sched.scheduleJob(jd1, tr1);
			sched.scheduleJob(jd2, tr2);
			sched.scheduleJob(jd3, tr3);
			//sched.start();
			Thread.sleep(20L * 1000L);
			
			String[] names = sched.getJobNames("foo");
			
			for(int i=0; i < names.length; i++)
				System.out.println(names[i]);
			
			//sched.start();
			Thread.sleep(190L * 1000L);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
