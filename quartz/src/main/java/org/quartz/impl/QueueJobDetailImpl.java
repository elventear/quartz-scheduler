/*
 * Copyright 2012 Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.quartz.impl;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.QueueJobDetail;

/**
 * A QueueJobDetail implementation. 
 * 
 * @author Zemian Deng
 *
 */
public class QueueJobDetailImpl implements QueueJobDetail {
	
	private static final long serialVersionUID = 4179200497747781404L;
	private JobKey key;
	private String description;
	private Class<? extends Job> jobClass;
	private int priority;
	private JobDataMap jobDataMap = new JobDataMap();
	private QueueJobDetail.Status status = QueueJobDetail.Status.QUEUED;
	
	public Status getStatus() {
		return status;
	}
	public void setStatus(QueueJobDetail.Status status) {
		this.status = status;
	}
	
	public JobKey getKey() {
		return key;
	}
	public void setKey(JobKey key) {
		this.key = key;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Class<? extends Job> getJobClass() {
		return jobClass;
	}
	public void setJobClass(Class<? extends Job> jobClass) {
		this.jobClass = jobClass;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public JobDataMap getJobDataMap() {
		return jobDataMap;
	}
	public void setJobDataMap(JobDataMap jobDataMap) {
		this.jobDataMap = jobDataMap;
	}
	
	@Override
	public String toString() {
		return "QueueJobDetailImpl[" + key + ", jobClass=" + jobClass.getName() + "]";
	}
	public boolean isDurable() {
		return false;
	}
	public boolean isPersistJobDataAfterExecution() {
		return false;
	}
	public boolean isConcurrentExectionDisallowed() {
		return false;
	}
	public boolean requestsRecovery() {
		return false;
	}
	public JobBuilder getJobBuilder() {
		return null;
	}
	
	public Object clone() {
		QueueJobDetailImpl copy;
        try {
            copy = (QueueJobDetailImpl) super.clone();
            if (jobDataMap != null) {
                copy.jobDataMap = (JobDataMap) jobDataMap.clone();
            }
        } catch (CloneNotSupportedException ex) {
            throw new IncompatibleClassChangeError("Not Cloneable.");
        }

        return copy;
	}
}
