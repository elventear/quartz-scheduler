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

package org.quartz;

import java.util.List;


/**
 * A manager interface to add, remove or update QueueJob into the scheduler.
 * 
 * @author Zemian Deng
 */
public interface QueueJobManager {
	
	public boolean checkQueueJobExists(JobKey jobKey) throws SchedulerException;

	public void addQueueJobDetail(QueueJobDetail queueJob) throws SchedulerException, ObjectAlreadyExistsException;

	public void removeQueueJobDetail(JobKey jobKey) throws SchedulerException;

	public QueueJobDetail getQueueJobDetail(JobKey jobKey) throws SchedulerException;
	
	public void updateQueueJobDetail(QueueJobDetail queueJobDetail) throws SchedulerException;

	public List<JobKey> getQueueJobKeys() throws SchedulerException;
}