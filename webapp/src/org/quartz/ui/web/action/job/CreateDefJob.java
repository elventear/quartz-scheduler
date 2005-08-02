package org.quartz.ui.web.action.job;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import org.quartz.ui.web.action.schedule.ScheduleBase;
import org.quartz.ui.web.base.BaseWebWork;
import org.quartz.ui.web.model.JobDefinition;
import org.quartz.ui.web.model.JobParameter;

/**
 * @author Matthew Payne
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CreateDefJob extends CreateJob {
	
	JobDefinition jobDefinition = new JobDefinition();
	
	String definitionName = "";
	
	public String start() {
		jobDetail.setJobDataMap(new JobDataMap());
	
		if (definitionName.length() > 0) {
			jobDefinition = BaseWebWork.getDefinitionManager().getDefinition(definitionName);
		}

		if (jobDefinition != null) {
			return SUCCESS;
		} else {

			this.addActionError("error.jobdefinition.missing");
			return INPUT;
		}

	}
	
	public String execute() {
	    
		if (definitionName.length() > 0) {
			jobDefinition = BaseWebWork.getDefinitionManager().getDefinition(definitionName);
		}
	
			Class jobClass = null;
			try {
				jobClass = Class.forName(className);
				jobDetail.setJobClass(jobClass);
				
				for (int i =0; i < parameterNames.length; i++) {
								if (parameterNames[i].trim().length() > 0 && parameterValues[i].trim().length() > 0) {
									jobDetail.getJobDataMap().put(parameterNames[i].trim(), parameterValues[i].trim());
								}
									
				}

				if (this.validateJobData()) {
					boolean replace = true;
					ScheduleBase.getCurrentScheduler().addJob(jobDetail, replace);
				} else {
					
					return ERROR;

				}
			
			} catch (ClassNotFoundException e) {
				this.addFieldError(
					"className",
					"error " + className + " class is not found");
					return ERROR;
			} catch (SchedulerException e) {
				this.addActionError(e.getMessage());
				return ERROR;
			}
		

		
		jobName = jobDetail.getName();
		jobGroup = jobDetail.getGroup();
		return SUCCESS;
	}
	
	
	private boolean validateJobData() {
		
		Iterator itr = jobDefinition.getParameters().iterator();
		Set keys = this.getJobDetail().getJobDataMap().keySet();
		
		while (itr.hasNext()) {
			JobParameter param = (JobParameter)itr.next();

			if (param.isRequired() && !(keys.contains(param.getName()))) {
				this.addActionError("missing.parameter" + param.getName());
				return false;
			}
		
		}
		
		return true;
		
	}
	
	
	/**
	 * @return Returns the jobDefinition.
	 */
	public JobDefinition getJobDefinition() {
		return jobDefinition;
	}
	/**
	 * @param jobDefinition The jobDefinition to set.
	 */
	public void setJobDefinition(JobDefinition jobDefinition) {
		this.jobDefinition = jobDefinition;
	}
	/**
	 * @return Returns the definitionName.
	 */
	public String getDefinitionName() {
		return definitionName;
	}
	/**
	 * @param definitionName The definitionName to set.
	 */
	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}
}
