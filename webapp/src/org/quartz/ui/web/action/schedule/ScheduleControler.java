package org.quartz.ui.web.action.schedule;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.ui.web.form.ChooseSchedulerForm;
import org.quartz.ui.web.form.JobDetailForm;
import org.quartz.ui.web.form.ListenerForm;
import org.quartz.ui.web.form.SchedulerDTO;

/**
 * Process scheduler command, and populate schedule summary information 
 *
 */
public class ScheduleControler extends ScheduleBase {
	
	String command="";
	String schedulerName="";
	
	ChooseSchedulerForm scheduleInfo=new ChooseSchedulerForm();

	
	public String execute() {
	
		if (LOG.isDebugEnabled()) {
			LOG.debug("command=" + command);
		}
		
			Scheduler choosenScheduler = null;
			try {
			
				if 	(schedulerName != null && schedulerName.length() > 0)
					choosenScheduler = new StdSchedulerFactory().getScheduler(schedulerName);
				else {
					choosenScheduler = StdSchedulerFactory.getDefaultScheduler();
				}
				
				
				if (command.equals("start")) {
					choosenScheduler.start();
				} else if (command.equals("stop")) {
					choosenScheduler.shutdown();
					//choosenScheduler = StdSchedulerFactory.getDefaultScheduler();			
				} else if (command.equals("pause")) {
					choosenScheduler.pause();
				} else if (command.equals("waitAndStopScheduler")) {
					choosenScheduler.shutdown(true);
				} else if (command.equals("pauseAll")) {
					choosenScheduler.pauseAll();
				} else if (command.equals("resumeAll")) {
					choosenScheduler.resumeAll();
				}
				
				this.populateSchedulerForm(choosenScheduler, scheduleInfo);
			
			} catch (SchedulerException e) {
				LOG.error("error in Scheduler Controller,  command=:" + command, e);
			} catch (Exception e) {
				LOG.error("error in Scheduler Controller,  command=:" + command, e);
			}
			
	
		return SUCCESS;
	}
	
	/**
	 * populate DTO with scheduler information summary.
	 * @param choosenScheduler
	 * @param form
	 * @throws Exception
	 */
	private void populateSchedulerForm(Scheduler choosenScheduler, ChooseSchedulerForm form)
	throws Exception
{
	
	Collection scheduleCollection =  new StdSchedulerFactory().getAllSchedulers();
	Iterator itr = scheduleCollection.iterator();

	form.setSchedulers(new ArrayList());
	try {
		form.setChoosenSchedulerName(choosenScheduler.getSchedulerName());
		
		while (itr.hasNext()) {
			Scheduler scheduler  = (Scheduler) itr.next();
			form.getSchedulers().add(scheduler);			
		}
	
	} catch (SchedulerException e) {
		throw new Exception(e);
	}


	SchedulerDTO schedForm = new SchedulerDTO();
	schedForm.setSchedulerName(choosenScheduler.getSchedulerName());
	schedForm.setNumJobsExecuted(String.valueOf(choosenScheduler.getMetaData().numJobsExecuted()));

	if (choosenScheduler.getMetaData().jobStoreSupportsPersistence()) {
		schedForm.setPersistenceType("value.scheduler.persiststenceType.database");
	} else {
		schedForm.setPersistenceType("value.scheduler.persiststenceType.memory");  // mp possible bugfix
	}
	schedForm.setRunningSince(String.valueOf(choosenScheduler.getMetaData().runningSince()));
	if (choosenScheduler.isShutdown()) {
		schedForm.setState("value.scheduler.state.stopped");
	} else if (choosenScheduler.isPaused()) {
		schedForm.setState("value.scheduler.state.paused");
	} else {
		schedForm.setState("value.scheduler.state.started");
	}
	
	schedForm.setThreadPoolSize(String.valueOf(choosenScheduler.getMetaData().getThreadPoolSize()));
	schedForm.setVersion(choosenScheduler.getMetaData().getVersion());
	schedForm.setSummary(choosenScheduler.getMetaData().getSummary());

	List jobDetails = choosenScheduler.getCurrentlyExecutingJobs();
	for (Iterator iter = jobDetails.iterator(); iter.hasNext();) {
		JobExecutionContext job = (JobExecutionContext) iter.next();
		JobDetail jobDetail = job.getJobDetail();

		JobDetailForm jobForm = new JobDetailForm();
		jobForm.setGroupName(jobDetail.getGroup());
		jobForm.setName(jobDetail.getName());
		jobForm.setDescription(jobDetail.getDescription());
		jobForm.setJobClass(jobDetail.getJobClass().getName());
		
		form.getExecutingJobs().add(jobForm);
	}
	String calendars[] = choosenScheduler.getCalendarNames();

	List jobListeners = choosenScheduler.getGlobalJobListeners();
	for (Iterator iter = jobListeners.iterator(); iter.hasNext();) {
		JobListener jobListener = (JobListener) iter.next();
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerName(jobListener.getName());
		listenerForm.setListenerClass(jobListener.getClass().getName());
		schedForm.getGlobalJobListeners().add(listenerForm);
	}

	
	// The section commented out below is not currently used, but may be used to show triggers that have been
	// added to jobs
	
	/* List triggerListeners = choosenScheduler.getGlobalTriggerListeners();
	for (Iterator iter = triggerListeners.iterator(); iter.hasNext();) {
		TriggerListener triggerListener = (TriggerListener) iter.next();
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerName(triggerListener.getName());
		listenerForm.setListenerClass(triggerListener.getClass().getName());
		schedForm.getGlobalJobListeners().add(listenerForm);
	}
	
	Set jobListenerNames = choosenScheduler.getJobListenerNames();
	for (Iterator iter = jobListenerNames.iterator(); iter.hasNext();) {
		JobListener jobListener = choosenScheduler.getJobListener((String) iter.next());
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerName(jobListener.getName());
		listenerForm.setListenerClass(jobListener.getClass().getName());
		schedForm.getRegisteredJobListeners().add(listenerForm);
	}
	
	Set triggerListenerNames = choosenScheduler.getTriggerListenerNames();
	for (Iterator iter = triggerListenerNames.iterator(); iter.hasNext();) {
		TriggerListener triggerListener = choosenScheduler.getTriggerListener((String) iter.next());
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerName(triggerListener.getName());
		listenerForm.setListenerClass(triggerListener.getClass().getName());
		schedForm.getRegisteredTriggerListeners().add(listenerForm);
	}

	List schedulerListeners = choosenScheduler.getSchedulerListeners();
	for (Iterator iter = schedulerListeners.iterator(); iter.hasNext();) {
		SchedulerListener schedulerListener = (SchedulerListener) iter.next();
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerClass(schedulerListener.getClass().getName());
		schedForm.getSchedulerListeners().add(listenerForm);
	}

	*/
	
	//TODO fix this
	form.setScheduler(schedForm);


}
	
	/**
	 * @return Returns the command.
	 */
	public String getCommand() {
		return command;
	}
	/**
	 * @param command The command to set.
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	
	/**
	 * @return Returns the schedulerName.
	 */
	public String getSchedulerName() {
		return schedulerName;
	}

	/**
	 * @param schedulerName The schedulerName to set.
	 */
	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	/**
	 * @return Returns the scheduleInfo.
	 */
	public ChooseSchedulerForm getScheduleInfo() {
		return scheduleInfo;
	}
}


/*
 * need to populate the following 
 * 
 * schedulerName
scheduleState
runningSince
numJobsExecuted
persistenceType
threadPoolSize
version


lists Schedulers-name

chooseScheduler - executing jobs
	groupName
	name
	description
	jobClass
	
	
chooseScheduler.summary	

*/


