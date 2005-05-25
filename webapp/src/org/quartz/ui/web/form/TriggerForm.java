/*
 *  Copyright James House (c) 2001-2004
 *
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *
 *
 * This product uses and includes within its distribution, 
 * software developed by the Apache Software Foundation 
 *     (http://www.apache.org/)
 *
 */
package org.quartz.ui.web.form;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  FIXME: Document class no.ezone.quartz.web.form.TriggerForm
 *
 * @author Erick Romson
 * @author Rene Eigenheer
 */

public class TriggerForm {
	public static final String DATE_FORMAT_PATTERN = "yy.MM.dd hh:mm";
	SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);

	public static final String START_TIME_PROP = "startTime";
	public static final String STOP_TIME_PROP = "stopTime";
	public static final String VOLATILITY_PROP = "volatility";
	public static final String MISFIRE_INSTRUCTION_PROP = "misFireInstruction";
	public static final String TRIGGER_NAME_PROP = "triggerName";
	public static final String TRIGGER_GROUP_PROP = "triggerGroup";
	public static final String DESCRIPTION_PROP = "description";
	public static final String JOB_NAME_PROP = "jobName";
	public static final String JOB_GROUP_PROP = "jobGroup";

	public static final String SCHEDULE_ACTION_PROP = "scheduleAction";
	public static final String CANCEL_ACTION_PROP = "cancelAction";

	protected String startTime;
	protected String stopTime;
	private boolean volatility;
	private int misFireInstruction;
	private String triggerName;
	private String triggerGroup;
	private String jobName;
	private String jobGroup;
	private String description;
	private String nextFireTime;
	private String previousFireTime;
	private String type;

	private String scheduleAction;
	private String cancelAction;
	
	public TriggerForm() {
	}

	
	public String getStartTime() {
		return startTime;
	}

	public Date getStartTimeAsDate() throws ParseException {
		return dateFormatter.parse(startTime);
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public Date getStopTimeAsDate() throws ParseException {
		return dateFormatter.parse(stopTime);
	}

	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}

	public String getScheduleAction() {
		return scheduleAction;
	}

	public void setScheduleAction(String scheduleAction) {
		this.scheduleAction = scheduleAction;
	}

	public String getCancelAction() {
		return cancelAction;
	}

	public void setCancelAction(String cancelAction) {
		this.cancelAction = cancelAction;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isVolatility() {
		return volatility;
	}

	public void setVolatility(boolean volatility) {
		this.volatility = volatility;
	}

	public int getMisFireInstruction() {
		return misFireInstruction;
	}

	public void setMisFireInstruction(int misFireInstruction) {
		this.misFireInstruction = misFireInstruction;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String name) {
		this.triggerName = name;
	}

	public String getTriggerGroup() {
		return triggerGroup;
	}

	public void setTriggerGroup(String group) {
		this.triggerGroup = group;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}

	
	
	/**
	 * Returns the nextFireTime.
	 * @return String
	 */
	public String getNextFireTime() {
		return nextFireTime;
	}

	/**
	 * Sets the nextFireTime.
	 * @param nextFireTime The nextFireTime to set
	 */
	public void setNextFireTime(String nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	/**
	 * Returns the previousFireTime.
	 * @return String
	 */
	public String getPreviousFireTime() {
		return previousFireTime;
	}

	/**
	 * Sets the previousFireTime.
	 * @param previousFireTime The previousFireTime to set
	 */
	public void setPreviousFireTime(String previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	/**
	 * Returns the type.
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	
}
