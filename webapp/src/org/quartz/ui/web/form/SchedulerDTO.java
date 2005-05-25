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

import java.util.ArrayList;

/**
 * @author Erick Romson
 * @author Rene Eigenheer
 */
public class SchedulerDTO  {
	private String schedulerName;
	private String runningSince;
	private String numJobsExecuted;
	private String persistenceType;
	private String threadPoolSize;
	private String version;
	private String state;
	private String summary;

	private ArrayList globalJobListeners;
	private ArrayList globalTriggerListeners;
	private ArrayList schedulerListeners;
	private ArrayList registeredJobListeners;
	private ArrayList registeredTriggerListeners;

	public SchedulerDTO() {
		this.schedulerListeners = new ArrayList();
		this.globalJobListeners = new ArrayList();
		this.globalTriggerListeners = new ArrayList();
		this.registeredJobListeners = new ArrayList();
		this.registeredTriggerListeners = new ArrayList();
	}
	
	/**
	 * Returns the numJobsExecuted.
	 * @return String
	 */
	public String getNumJobsExecuted() {
		return numJobsExecuted;
	}

	/**
	 * Returns the persistenceType.
	 * @return String
	 */
	public String getPersistenceType() {
		return persistenceType;
	}

	/**
	 * Returns the runningSince.
	 * @return String
	 */
	public String getRunningSince() {
		return runningSince;
	}

	/**
	 * Returns the schedulerName.
	 * @return String
	 */
	public String getSchedulerName() {
		return schedulerName;
	}

	/**
	 * Returns the state.
	 * @return String
	 */
	public String getState() {
		return state;
	}

	/**
	 * Returns the threadPool.
	 * @return String
	 */
	public String getThreadPoolSize() {
		return threadPoolSize;
	}

	/**
	 * Returns the version.
	 * @return String
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the numJobsExecuted.
	 * @param numJobsExecuted The numJobsExecuted to set
	 */
	public void setNumJobsExecuted(String numJobsExecuted) {
		this.numJobsExecuted = numJobsExecuted;
	}

	/**
	 * Sets the persistenceType.
	 * @param persistenceType The persistenceType to set
	 */
	public void setPersistenceType(String persistenceType) {
		this.persistenceType = persistenceType;
	}

	/**
	 * Sets the runningSince.
	 * @param runningSince The runningSince to set
	 */
	public void setRunningSince(String runningSince) {
		this.runningSince = runningSince;
	}

	/**
	 * Sets the schedulerName.
	 * @param schedulerName The schedulerName to set
	 */
	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}

	/**
	 * Sets the state.
	 * @param state The state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Sets the threadPool.
	 * @param threadPool The threadPool to set
	 */
	public void setThreadPoolSize(String threadPool) {
		this.threadPoolSize = threadPool;
	}

	/**
	 * Sets the version.
	 * @param version The version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Returns the summary.
	 * @return String
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * Sets the summary.
	 * @param summary The summary to set
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}

	/**
	 * Returns the globalJobListeners.
	 * @return ArrayList
	 */
	public ArrayList getGlobalJobListeners() {
		return globalJobListeners;
	}

	/**
	 * Returns the globalTriggerListeners.
	 * @return ArrayList
	 */
	public ArrayList getGlobalTriggerListeners() {
		return globalTriggerListeners;
	}

	/**
	 * Returns the schedulerListeners.
	 * @return ArrayList
	 */
	public ArrayList getSchedulerListeners() {
		return schedulerListeners;
	}

	/**
	 * Sets the globalJobListeners.
	 * @param globalJobListeners The globalJobListeners to set
	 */
	public void setGlobalJobListeners(ArrayList globalJobListeners) {
		this.globalJobListeners = globalJobListeners;
	}

	/**
	 * Sets the globalTriggerListeners.
	 * @param globalTriggerListeners The globalTriggerListeners to set
	 */
	public void setGlobalTriggerListeners(ArrayList globalTriggerListeners) {
		this.globalTriggerListeners = globalTriggerListeners;
	}

	/**
	 * Sets the schedulerListeners.
	 * @param schedulerListeners The schedulerListeners to set
	 */
	public void setSchedulerListeners(ArrayList schedulerListeners) {
		this.schedulerListeners = schedulerListeners;
	}

	/**
	 * Returns the registeredJobListeners.
	 * @return ArrayList
	 */
	public ArrayList getRegisteredJobListeners() {
		return registeredJobListeners;
	}

	/**
	 * Returns the registeredTriggerListeners.
	 * @return ArrayList
	 */
	public ArrayList getRegisteredTriggerListeners() {
		return registeredTriggerListeners;
	}

	/**
	 * Sets the registeredJobListeners.
	 * @param registeredJobListeners The registeredJobListeners to set
	 */
	public void setRegisteredJobListeners(ArrayList registeredJobListeners) {
		this.registeredJobListeners = registeredJobListeners;
	}

	/**
	 * Sets the registeredTriggerListeners.
	 * @param registeredTriggerListeners The registeredTriggerListeners to set
	 */
	public void setRegisteredTriggerListeners(ArrayList registeredTriggerListeners) {
		this.registeredTriggerListeners = registeredTriggerListeners;
	}

}
