package org.quartz.ui.web.action.schedule;

import java.util.ArrayList;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

public class ListTriggers extends ScheduleBase {

	String jobName="";
	String jobGroup=""; 
	String triggerGroup = "";
		
	private ArrayList triggerList = new ArrayList();
	
	/**
	 * Returns the jobs.
	 * @return ArrayList
	 */
	public java.util.List getTriggers() {
		return  triggerList;
	}
	
				
	public String execute() throws Exception  {

	  	   if (hasFieldErrors()) {
			   LOG.info("this thing has errors");
			return ERROR;
			}
			
			Scheduler scheduler = ScheduleBase.getCurrentScheduler();
			this.triggerList = new ArrayList();		

				try {

					if (!scheduler.isPaused() || !scheduler.isShutdown()) {
						
					
					String[] triggerGroups = scheduler.getTriggerGroupNames();
							
							for (int i = 0; i < triggerGroups.length; i++) {
								String groupName = triggerGroups[i];
								String[] triggerNames = scheduler.getTriggerNames(groupName);
								for (int j = 0; j < triggerNames.length; j++) {
									String triggerName = triggerNames[j];
									Trigger trigger =
										scheduler.getTrigger(triggerName, groupName);
					
								/*	tForm.setDescription(trigger.getDescription());
									tForm.setJobGroup(trigger.getJobGroup());
									tForm.setJobName(trigger.getJobName());
									tForm.setMisFireInstruction(trigger.getMisfireInstruction());
				
									tForm.setStartTime(Util.getDateAsString(trigger.getStartTime()));
									tForm.setStopTime(Util.getDateAsString(trigger.getEndTime()));
				
									tForm.setTriggerGroup(trigger.getGroup());
									tForm.setTriggerName(trigger.getName());
				
									tForm.setNextFireTime(Util.getDateAsString(trigger.getNextFireTime()));
									tForm.setPreviousFireTime(Util.getDateAsString(trigger.getPreviousFireTime()));
									tForm.setType(Util.getTriggerType(trigger));*/
								
								this.triggerList.add(trigger);
								
							}
						}
							
					} else {
						addActionError(getText("error.listtriggers.pausestop", "Cannot list triggers when scheduler is stopped/paused"));
						
					}
				} catch (SchedulerException e) {
					LOG.error("Problem listing triggers, schedule may be paused or stopped", e);
							}
			
		return SUCCESS;

		}



}
