package org.quartz.ui.web.action.schedule;

import org.quartz.SchedulerException;

public class UnSchedule extends ScheduleBase {

	String jobName="";
	String jobGroup="";
	String triggerGroup = "";
			
	public String execute()  {

	  	   if (hasFieldErrors()) {
			   LOG.info("this thing has errors");
			return ERROR;
			}
			
			try {
				getCurrentScheduler().unscheduleJob(triggerName, triggerGroup);
			} catch (SchedulerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return SUCCESS;

		}


	/**
	 * @return
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param string
	 */
	public void setJobName(String string) {
		jobName = string;
	}

	/**
	 * @param string
	 */
	public void setTriggerGroup(String string) {
		triggerGroup = string;
	}

	/**
	 * @param string
	 */
	public void setTriggerName(String string) {
		triggerName = string;
	}

	/**
	 * @return
	 */
	public String getJobGroup() {
		return jobGroup;
	}

	/**
	 * @param string
	 */
	public void setJobGroup(String string) {
		jobGroup = string;
	}

}
