package org.quartz.impl;

import org.quartz.JobPersistenceException;
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
	
	public void addQueueJobDetail(QueueJobDetail queueJob) throws SchedulerException {
		logger.debug("Adding queueJob={} into queue.", queueJob);
		try {
			jobStore.storeQueueJobDetail(queueJob);
		} catch (JobPersistenceException e) {
			throw new SchedulerException("Unable to store queueJob={}" + queueJob, e);
		}
	}

}
