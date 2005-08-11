package org.quartz.ui.web.action.schedule;

import java.util.ArrayList;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public class ListJobs extends ScheduleBase {

	private ArrayList jobList;

	/**
	 * Returns the jobs.
	 * @return ArrayList
	 */
	public java.util.List getJobs() {
		return jobList;
	}

	public String execute() throws Exception {

		Scheduler scheduler = ScheduleBase.getCurrentScheduler();
		this.jobList = new ArrayList();

		try {

				String[] jobGroups = scheduler.getJobGroupNames();
				ArrayList addedJobs = new ArrayList(jobGroups.length);
				//
				// have had some problems multiple jobs showing
				for (int i = 0; i < jobGroups.length; i++) {
					String groupName = jobGroups[i];
					String[] jobs = scheduler.getJobNames(groupName);
					for (int j = 0; j < jobs.length; j++) {
						String job = jobs[j];
						JobDetail jobDetail = scheduler.getJobDetail(job,
								groupName);
						String key = job + groupName;
						if (!addedJobs.contains(key)) {
							this.jobList.add(jobDetail);
							addedJobs.add(key);
						}
					}
				}
		
		} catch (SchedulerException e) {
			LOG.error("Problem listing jobs, schedule may be paused or stopped", e);
		}

		return SUCCESS;

	}

}
