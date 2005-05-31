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
	
	String newSchedulerName="";
	
	
	ChooseSchedulerForm scheduleInfo=new ChooseSchedulerForm();

	
	public String execute() {
	
		if (LOG.isDebugEnabled()) {
			LOG.debug("command=" + command);
		}
		
		if (hasErrors() && 1 ==2) {
			LOG.info("this thing has errors");
		    LOG.info(this.getActionErrors().toString());
			return INPUT;
		} else { 
			
			Scheduler chosenScheduler = null;
			try {

				chosenScheduler = getScheduler();
				
				if (command.equals("start")) {
					chosenScheduler.start();
				} else if (command.equals("stop")) {
					chosenScheduler.shutdown();
				} else if (command.equals("pause")) {
					chosenScheduler.pause();
				} else if (command.equals("waitAndStopScheduler")) {
					chosenScheduler.shutdown(true);
				} else if (command.equals("pauseAll")) {
					chosenScheduler.pauseAll();
				} else if (command.equals("resumeAll")) {
					chosenScheduler.resumeAll();
				}
				
				this.populateSchedulerForm(chosenScheduler, scheduleInfo);
			
			} catch (SchedulerException e) {
				LOG.error("error in Scheduler Controller,  command=:" + command, e);
			} catch (Exception e) {
				LOG.error("error in Scheduler Controller,  command=:" + command, e);
			}
			
		}
	
		return SUCCESS;
	}
	
	/**
	 * @return
	 * @throws SchedulerException
	 */
	private Scheduler getScheduler() throws SchedulerException {
		Scheduler chosenScheduler;
		if 	(newSchedulerName != null && newSchedulerName.length() > 0)
			chosenScheduler = new StdSchedulerFactory().getScheduler(newSchedulerName);
		else {
			chosenScheduler = StdSchedulerFactory.getDefaultScheduler();
		}
		return chosenScheduler;
	}

	/**
	 * populate DTO with scheduler information summary.
	 * @param chosenScheduler
	 * @param form
	 * @throws Exception
	 */
	private void populateSchedulerForm(Scheduler chosenScheduler, ChooseSchedulerForm form)
	throws Exception
{
	
	Collection scheduleCollection =  new StdSchedulerFactory().getAllSchedulers();
	Iterator itr = scheduleCollection.iterator();

	form.setSchedulers(new ArrayList());
	try {
		form.setChoosenSchedulerName(chosenScheduler.getSchedulerName());
		
		while (itr.hasNext()) {
			Scheduler scheduler  = (Scheduler) itr.next();
			form.getSchedulers().add(scheduler);			
		}
	
	} catch (SchedulerException e) {
		throw new Exception(e);
	}


	SchedulerDTO schedForm = new SchedulerDTO();
	schedForm.setSchedulerName(chosenScheduler.getSchedulerName());
	schedForm.setNumJobsExecuted(String.valueOf(chosenScheduler.getMetaData().numJobsExecuted()));

	if (chosenScheduler.getMetaData().jobStoreSupportsPersistence()) {
		schedForm.setPersistenceType("value.scheduler.persiststenceType.database");
	} else {
		schedForm.setPersistenceType("value.scheduler.persiststenceType.memory");  // mp possible bugfix
	}
	schedForm.setRunningSince(String.valueOf(chosenScheduler.getMetaData().runningSince()));
	if (chosenScheduler.isShutdown()) {
		schedForm.setState("value.scheduler.state.stopped");
	} else if (chosenScheduler.isPaused()) {
		schedForm.setState("value.scheduler.state.paused");
	} else {
		schedForm.setState("value.scheduler.state.started");
	}
	
	schedForm.setThreadPoolSize(String.valueOf(chosenScheduler.getMetaData().getThreadPoolSize()));
	schedForm.setVersion(chosenScheduler.getMetaData().getVersion());
	schedForm.setSummary(chosenScheduler.getMetaData().getSummary());

	List jobDetails = chosenScheduler.getCurrentlyExecutingJobs();
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
	
	String calendars[];
    if (!chosenScheduler.isShutdown())
    	calendars = chosenScheduler.getCalendarNames();

	List jobListeners = chosenScheduler.getGlobalJobListeners();
	for (Iterator iter = jobListeners.iterator(); iter.hasNext();) {
		JobListener jobListener = (JobListener) iter.next();
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerName(jobListener.getName());
		listenerForm.setListenerClass(jobListener.getClass().getName());
		schedForm.getGlobalJobListeners().add(listenerForm);
	}

	
	// The section commented out below is not currently used, but may be used to show triggers that have been
	// added to jobs
	
	/* List triggerListeners = chosenScheduler.getGlobalTriggerListeners();
	for (Iterator iter = triggerListeners.iterator(); iter.hasNext();) {
		TriggerListener triggerListener = (TriggerListener) iter.next();
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerName(triggerListener.getName());
		listenerForm.setListenerClass(triggerListener.getClass().getName());
		schedForm.getGlobalJobListeners().add(listenerForm);
	}
	
	Set jobListenerNames = chosenScheduler.getJobListenerNames();
	for (Iterator iter = jobListenerNames.iterator(); iter.hasNext();) {
		JobListener jobListener = chosenScheduler.getJobListener((String) iter.next());
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerName(jobListener.getName());
		listenerForm.setListenerClass(jobListener.getClass().getName());
		schedForm.getRegisteredJobListeners().add(listenerForm);
	}
	
	Set triggerListenerNames = chosenScheduler.getTriggerListenerNames();
	for (Iterator iter = triggerListenerNames.iterator(); iter.hasNext();) {
		TriggerListener triggerListener = chosenScheduler.getTriggerListener((String) iter.next());
		ListenerForm listenerForm = new ListenerForm();
		listenerForm.setListenerName(triggerListener.getName());
		listenerForm.setListenerClass(triggerListener.getClass().getName());
		schedForm.getRegisteredTriggerListeners().add(listenerForm);
	}

	List schedulerListeners = chosenScheduler.getSchedulerListeners();
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
	 * @return Returns the newSchedulerName.
	 */
	public String getNewSchedulerName() {
		return newSchedulerName;
	}
	/**
	 * @param newSchedulerName The newSchedulerName to set.
	 */
	public void setNewSchedulerName(String newSchedulerName) {
		this.newSchedulerName = newSchedulerName;
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


