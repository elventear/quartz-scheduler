package org.quartz.ui.web.action.job;

import java.util.HashMap;
import java.util.Map;

import org.quartz.ui.web.action.schedule.ScheduleBase;
import org.quartz.ui.web.base.BaseWebWork;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException; 

import com.opensymphony.xwork.Action;

/**
 * @author Matthew Payne Save update operations for a JobDefinition
 */
public class CreateJob extends BaseWebWork implements Action {

	JobDetail jobDetail = new JobDetail();

	String className = "";

	boolean recoverable = true;

	String parameterNames[] = {};

	String parameterValues[] = {};

	Map parameters = new HashMap();

	String jobName = "";

	String jobGroup = "";

	public void validate() {

		Class jobClass = null;

		if ((className != null) && (className.length() > 0)) {
			try {
				jobClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				this.addFieldError("className", "error " + className
						+ " class is not found");
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.opensymphony.xwork.Action#execute()
	 */

	public String start() {
		Scheduler scheduler = ScheduleBase.getCurrentScheduler();
		jobDetail.setJobDataMap(new JobDataMap());
		return SUCCESS;
	}

	public String edit() throws SchedulerException {
		if (jobName.length() > 0 && jobGroup.length() > 0) {
			this.jobDetail = ScheduleBase.getCurrentScheduler().getJobDetail(
					jobName, jobGroup);
			return SUCCESS;

		} else {
			return ERROR;
		}

	}

	public String execute() {

			Class jobClass = null;
			try {
				jobClass = Class.forName(className);
				jobDetail.setJobClass(jobClass);

				for (int i = 0; i < parameterNames.length; i++) {
					if (parameterNames[i].trim().length() > 0
							&& parameterValues[i].trim().length() > 0) {
						jobDetail.getJobDataMap().put(parameterNames[i].trim(),
								parameterValues[i].trim());
					}

				}

				boolean replace = true;
				ScheduleBase.getCurrentScheduler().addJob(jobDetail, replace);

			} catch (ClassNotFoundException e) {
				this.addFieldError("className", "error " + className
						+ " class is not found");
				return ERROR;
			} catch (SchedulerException e) {
				this.addActionError(e.getMessage());
				return ERROR;
			}

	
		return SUCCESS;
	}

	/**
	 * @return
	 */
	public JobDetail getJobDetail() {
		return jobDetail;
	}

	/**
	 * @param detail
	 */
	public void setJobDetail(JobDetail detail) {
		jobDetail = detail;
	}

	/**
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param string
	 */
	public void setClassName(String string) {
		className = string.trim();
	}

	/**
	 * @return
	 */
	public boolean isRecoverable() {
		return recoverable;
	}

	/**
	 * @param map
	 */
	public void setParameter(int i, String p) {
		System.out.println("param is +" + p);

		// params[i] = p;
	}

	public Object getParameters(String key) {
		return parameters.get(key);
	}

	public void setParameters(String key, String value) {
		parameters.put(key, value);
	}

	/**
	 * @param b
	 */
	public void setRecoverable(boolean b) {
		recoverable = b;
	}

	public String getParameterName(int i) {
		if (i >= parameterNames.length)
			return "";
		else
			return parameterNames[i];
	}

	/**
	 * @return
	 */
	public String[] getParameterNames() {
		return parameterNames;
	}

	/**
	 * @return
	 */
	public String[] getParameterValues() {
		return parameterValues;
	}

	/**
	 * @param strings
	 */
	public void setParameterNames(String[] strings) {
		parameterNames = strings;
	}

	/**
	 * @param strings
	 */
	public void setParameterValues(String[] strings) {
		parameterValues = strings;
	}

	/**
	 * @param string
	 */
	public void setJobGroup(String string) {
		jobGroup = string;
	}

	/**
	 * @param string
	 */
	public void setJobName(String string) {
		jobName = string;
	}

}