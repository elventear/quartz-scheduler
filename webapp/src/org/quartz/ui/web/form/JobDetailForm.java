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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.util.ArrayList;

/**
  *  FIXME: Document class no.ezone.quartz.web.form.JobDetailForm
  *
  * @since Feb 1, 2003
  * @version $Revision$
 * @author Erick Romson
 * @author Rene Eigenheer
 * @author Matthew Payne
  */

public class JobDetailForm  {

	public static String FORM_NAME = "jobDetailForm";

	protected String name;
	protected String groupName;
	protected String description;
	protected String jobClass;
	boolean volatility;
	boolean durable = true;	
	boolean stateful;	
	boolean recoveryRequesting;
	

	protected String saveAction;
	protected String cancelAction;
	protected String deleteAction;
	protected String editAction;
	protected String scheduleSimpleTriggerAction;
	protected String scheduleCronTriggerAction;
	protected String scheduleUICronTriggerAction;

	protected String executeJobAction;
	protected String unscheduleAction;

	protected String unscheduleTriggerName;
	protected String unscheduleTriggerGroup;

	private ArrayList triggers;
	private ArrayList values;
	private ArrayList jobListeners;

	public JobDetailForm() {
		this.values = new ArrayList();
		this.triggers = new ArrayList();
		this.jobListeners = new ArrayList();
	}
			
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public boolean isVolatile() {
		return volatility;
	}

	public void setVolatile(boolean volatility) {
		this.volatility = volatility;
	}

	public boolean isRecoveryRequesting() {
		return recoveryRequesting;
	}

	public void setRecoveryRequesting(boolean recover) {
		this.recoveryRequesting = recover;
	}

	public String getSaveAction() {
		return saveAction;
	}

	public void setSaveAction(String saveAction) {
		this.saveAction = saveAction;
	}

	public String getCancelAction() {
		return cancelAction;
	}

	public void setCancelAction(String cancelAction) {
		this.cancelAction = cancelAction;
	}

	/**
	 * Returns the scheduleCronTriggerAction.
	 * @return String
	 */
	public String getScheduleCronTriggerAction() {
		return scheduleCronTriggerAction;
	}

	/**
	 * Returns the scheduleSimpleTriggerAction.
	 * @return String
	 */
	public String getScheduleSimpleTriggerAction() {
		return scheduleSimpleTriggerAction;
	}

	/**
	 * Returns the unscheduleAction.
	 * @return String
	 */
	public String getUnscheduleAction() {
		return unscheduleAction;
	}

	/**
	 * Returns the unscheduleTriggerGroup.
	 * @return String
	 */
	public String getUnscheduleTriggerGroup() {
		return unscheduleTriggerGroup;
	}

	/**
	 * Returns the unscheduleTriggerName.
	 * @return String
	 */
	public String getUnscheduleTriggerName() {
		return unscheduleTriggerName;
	}

	/**
	 * Sets the scheduleCronTriggerAction.
	 * @param scheduleCronTriggerAction The scheduleCronTriggerAction to set
	 */
	public void setScheduleCronTriggerAction(String scheduleCronTriggerAction) {
		this.scheduleCronTriggerAction = scheduleCronTriggerAction;
	}

	/**
	 * Sets the scheduleSimpleTriggerAction.
	 * @param scheduleSimpleTriggerAction The scheduleSimpleTriggerAction to set
	 */
	public void setScheduleSimpleTriggerAction(String scheduleSimpleTriggerAction) {
		this.scheduleSimpleTriggerAction = scheduleSimpleTriggerAction;
	}

	/**
	 * Sets the unscheduleAction.
	 * @param unscheduleAction The unscheduleAction to set
	 */
	public void setUnscheduleAction(String unscheduleAction) {
		this.unscheduleAction = unscheduleAction;
	}

	/**
	 * Sets the unscheduleTriggerGroup.
	 * @param unscheduleTriggerGroup The unscheduleTriggerGroup to set
	 */
	public void setUnscheduleTriggerGroup(String unscheduleTriggerGroup) {
		this.unscheduleTriggerGroup = unscheduleTriggerGroup;
	}

	/**
	 * Sets the unscheduleTriggerName.
	 * @param unscheduleTriggerName The unscheduleTriggerName to set
	 */
	public void setUnscheduleTriggerName(String unscheduleTriggerName) {
		this.unscheduleTriggerName = unscheduleTriggerName;
	}

	/**
	 * Returns the deleteAction.
	 * @return String
	 */
	public String getDeleteAction() {
		return deleteAction;
	}

	/**
	 * Sets the deleteAction.
	 * @param deleteAction The deleteAction to set
	 */
	public void setDeleteAction(String deleteAction) {
		this.deleteAction = deleteAction;
	}

	/**
	 * Returns the editAction.
	 * @return String
	 */
	public String getEditAction() {
		return editAction;
	}

	/**
	 * Returns the executeJobAction.
	 * @return String
	 */
	public String getExecuteJobAction() {
		return executeJobAction;
	}

	/**
	 * Sets the editAction.
	 * @param editAction The editAction to set
	 */
	public void setEditAction(String editAction) {
		this.editAction = editAction;
	}

	/**
	 * Sets the executeJobAction.
	 * @param executeJobAction The executeJobAction to set
	 */
	public void setExecuteJobAction(String executeJobAction) {
		this.executeJobAction = executeJobAction;
	}

	/**
	 * Returns the triggers.
	 * @return ArrayList
	 */
	public ArrayList getTriggers() {
		return triggers;
	}

	/**
	 * Sets the triggers.
	 * @param triggers The triggers to set
	 */
	public void setTriggers(ArrayList triggers) {
		this.triggers = triggers;
	}
	public void setTrigger(int i, TriggerForm form) {
			this.triggers.set(i,form);
	}

	public TriggerForm getTrigger(int i) {
		while(i >= this.triggers.size() ){
			this.triggers.add(new TriggerForm());
		}
		return (TriggerForm) this.triggers.get(i);
	}


	public static Log getLog() {
		return LogFactory.getLog(JobDetailForm.class);
	}

	/**
	 * Returns the values.
	 * @return ArrayList
	 */
	public ArrayList getValues() {
		return values;
	}

	/**
	 * Sets the values.
	 * @param values The values to set
	 */
	public void setValues(ArrayList values) {
		this.values = values;
	}

	public void setValue(int i, ValueForm form) {
			this.values.set(i,form);
	}

	public ValueForm getValue(int i) {
		while(i >= this.values.size() ){
			this.values.add(new ValueForm());
		}
		return (ValueForm) this.values.get(i);
	}

	/**
	 * Returns the jobListeners.
	 * @return ArrayList
	 */
	public ArrayList getJobListeners() {
		return jobListeners;
	}

	/**
	 * Sets the jobListeners.
	 * @param jobListeners The jobListeners to set
	 */
	public void setJobListeners(ArrayList jobListeners) {
		this.jobListeners = jobListeners;
	}

	public void setJobListener(int i, ListenerForm form) {
			this.jobListeners.set(i,form);
	}

	public ListenerForm getJobListener(int i) {
		while(i >= this.jobListeners.size() ){
			this.jobListeners.add(new ListenerForm());
		}
		return (ListenerForm) this.jobListeners.get(i);
	}
	
	/**
	 * Returns the durable.
	 * @return boolean
	 */
	public boolean isDurable() {
		return durable;
	}

	/**
	 * Returns the stateful.
	 * @return boolean
	 */
	public boolean isStateful() {
		return stateful;
	}

	/**
	 * Sets the durable.
	 * @param durable The durable to set
	 */
	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	/**
	 * Sets the stateful.
	 * @param stateful The stateful to set
	 */
	public void setStateful(boolean stateful) {
		this.stateful = stateful;
	}


	/**
	 * 
	 */
	public String getScheduleUICronTriggerAction() {
		return scheduleUICronTriggerAction;
	}

	/**
	 * @param string
	 */
	public void setScheduleUICronTriggerAction(String string) {
		scheduleUICronTriggerAction = string;
	}

}
