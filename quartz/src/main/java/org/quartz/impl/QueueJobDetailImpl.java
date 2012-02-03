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

import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.QueueJob;
import org.quartz.QueueJobDetail;

/**
 * A QueueJobDetail implementation. 
 * 
 * @author Zemian Deng
 *
 */
public class QueueJobDetailImpl implements QueueJobDetail {
	private JobKey key;
	private String description;
	private Class<? extends QueueJob> queueJobClass;
	private int priority;
	private JobDataMap jobDataMap;
	
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
	public Class<? extends QueueJob> getQueueJobClass() {
		return queueJobClass;
	}
	public void setQueueJobClass(Class<? extends QueueJob> queueJobClass) {
		this.queueJobClass = queueJobClass;
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
		return "QueueJobDetailImpl[" + key + "]";
	}
}
