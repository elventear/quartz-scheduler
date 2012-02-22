package org.quartz.impl;

import java.util.List;

import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.QueueJobDetail;
import org.quartz.QueueJobManager;
import org.quartz.SchedulerException;
import org.quartz.spi.JobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueJobManagerImpl implements QueueJobManager {
	
	private Logger logger = LoggerFactory.getLogger(QueueJobManagerImpl.class);
	private JobStore jobStore;
			
	public QueueJobManagerImpl(JobStore jobStore) {
		this.jobStore = jobStore;
	}
	
	public void addQueueJobDetail(QueueJobDetail queueJob) throws SchedulerException, ObjectAlreadyExistsException {
		logger.debug("Adding queue job {}.", queueJob);
		try {
			jobStore.storeQueueJobDetail(queueJob);
		} catch (JobPersistenceException e) {
			throw new SchedulerException("Unable to store queue job " + queueJob, e);
		}
	}

	public void removeQueueJobDetail(JobKey jobKey) throws SchedulerException {
		logger.debug("Removing queue job {}.", jobKey);
		try {
			jobStore.removeQueueJobDetail(jobKey);
		} catch (JobPersistenceException e) {
			throw new SchedulerException("Unable to remove queue job " + jobKey, e);
		}
	}

	public QueueJobDetail getQueueJobDetail(JobKey jobKey) throws SchedulerException {
		logger.debug("Get queue job {}.", jobKey);
		try {
			return jobStore.getQueueJobDetail(jobKey);
		} catch (JobPersistenceException e) {
			throw new SchedulerException("Unable to store queue job " + jobKey, e);
		}
	}

	public void updateQueueJobDetail(QueueJobDetail queueJobDetail) throws SchedulerException {
		logger.debug("Updating queue job {}.", queueJobDetail);
		try {
			jobStore.updateQueueJobDetail(queueJobDetail);
		} catch (JobPersistenceException e) {
			throw new SchedulerException("Unable to store queue job " + queueJobDetail, e);
		}
	}

	public List<JobKey> getQueueJobKeys() throws SchedulerException {
		try {
			return jobStore.getQueueJobKeys();
		} catch (JobPersistenceException e) {
			throw new SchedulerException("Unable to get all the queue jobs.", e);
		}
	}
	
	public boolean checkQueueJobExists(JobKey jobKey) throws SchedulerException {
		try {
			return jobStore.checkQueueJobExists(jobKey);
		} catch (JobPersistenceException e) {
			throw new SchedulerException("Unable to get all the queue jobs.", e);
		}
	}
}
