package org.quartz.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import static org.quartz.JobKey.*;
import static org.quartz.TriggerKey.*;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.UnableToInterruptJobException;
import org.quartz.Trigger.TriggerState;
import org.quartz.core.jmx.JobDetailSupport;
import org.quartz.core.jmx.JobExecutionContextSupport;
import org.quartz.core.jmx.QuartzSchedulerMBean;
import org.quartz.core.jmx.TriggerSupport;
import org.quartz.impl.matchers.EverythingMatcher;

public class QuartzSchedulerMBeanImpl extends StandardMBean implements
		NotificationEmitter, QuartzSchedulerMBean, JobListener,
		SchedulerListener {
	private static final MBeanNotificationInfo[] NOTIFICATION_INFO;

	private final QuartzScheduler scheduler;
	private boolean sampledStatisticsEnabled;
	private SampledStatistics sampledStatistics;

	private final static SampledStatistics NULL_SAMPLED_STATISTICS = new NullSampledStatisticsImpl();

	static {
		final String[] notifTypes = new String[] { SCHEDULER_STARTED,
				SCHEDULER_PAUSED, SCHEDULER_SHUTDOWN, };
		final String name = Notification.class.getName();
		final String description = "QuartzScheduler JMX Event";
		NOTIFICATION_INFO = new MBeanNotificationInfo[] { new MBeanNotificationInfo(
				notifTypes, name, description), };
	}

	/**
	 * emitter
	 */
	protected final Emitter emitter = new Emitter();

	/**
	 * sequenceNumber
	 */
	protected final AtomicLong sequenceNumber = new AtomicLong();

	/**
	 * QuartzSchedulerMBeanImpl
	 * 
	 * @throws NotCompliantMBeanException
	 */
	protected QuartzSchedulerMBeanImpl(QuartzScheduler scheduler)
			throws NotCompliantMBeanException {
		super(QuartzSchedulerMBean.class);
		this.scheduler = scheduler;
		this.scheduler.addInternalJobListener(this);
		this.scheduler.addInternalSchedulerListener(this);
		this.sampledStatistics = NULL_SAMPLED_STATISTICS;
		this.sampledStatisticsEnabled = false;
	}

	public TabularData getCurrentlyExecutingJobs() throws SchedulerException {
		return JobExecutionContextSupport.toTabularData(scheduler
				.getCurrentlyExecutingJobs());
	}

	public TabularData getAllJobDetails(String instanceId)
			throws SchedulerException {
		List<JobDetail> detailList = new ArrayList<JobDetail>();

		for (String jobGroupName : scheduler.getJobGroupNames()) {
			for (JobKey jobKey : scheduler.getJobKeys(jobGroupName)) {
				detailList.add(scheduler.getJobDetail(jobKey));
			}
		}

		return JobDetailSupport.toTabularData(detailList
				.toArray(new JobDetail[detailList.size()]));
	}

	public TabularData getAllTriggers(String instanceId)
			throws SchedulerException {
		List<Trigger> triggerList = new ArrayList<Trigger>();

		for (String triggerGroupName : scheduler.getTriggerGroupNames()) {
			for (TriggerKey triggerKey : scheduler.getTriggerKeys(triggerGroupName)) {
				triggerList.add(scheduler.getTrigger(triggerKey));
			}
		}

		return TriggerSupport.toTabularData(triggerList);
	}

	public void addJob(String instanceId, CompositeData jobDetail,
			boolean replace) throws SchedulerException {
		scheduler.addJob(JobDetailSupport
				.newJobDetail(jobDetail), replace);
	}

	public void deleteCalendar(String instanceId, String name)
			throws SchedulerException {
		scheduler.deleteCalendar(name);
	}

	public boolean deleteJob(String instanceId, String jobName,
			String jobGroupName) throws SchedulerException {
		return scheduler.deleteJob(jobKey(jobName, jobGroupName));
	}

	public List<String> getCalendarNames(String instanceId)
			throws SchedulerException {
		return scheduler.getCalendarNames();
	}

	public CompositeData getJobDetail(String instanceId, String jobName,
			String jobGroupName) throws SchedulerException {
		return JobDetailSupport.toCompositeData(scheduler.getJobDetail(
		        jobKey(jobName, jobGroupName)));
	}

	public List<String> getJobGroupNames(String instanceId)
			throws SchedulerException {
		return scheduler.getJobGroupNames();
	}

	public List<String> getJobNames(String instanceId, String groupName)
			throws SchedulerException {
		List<JobKey> keys = scheduler.getJobKeys(groupName);
		List<String> names = new ArrayList<String>(keys.size());
		for(JobKey key: keys) {
		    names.add(key.getName());
		}
		return names;
	}

	public String getJobStoreClassName() {
		return scheduler.getJobStoreClass().getName();
	}

	public Set<String> getPausedTriggerGroups(String instanceId)
			throws SchedulerException {
		return scheduler.getPausedTriggerGroups();
	}

	public CompositeData getTrigger(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException {
		return TriggerSupport.toCompositeData(scheduler.getTrigger(
				triggerKey(triggerName, triggerGroupName)));
	}

	public List<String> getTriggerGroupNames(String instanceId)
			throws SchedulerException {
		return scheduler
				.getTriggerGroupNames();
	}

	public List<String> getTriggerNames(String instanceId, String triggerGroupName)
			throws SchedulerException {
        List<TriggerKey> keys = scheduler.getTriggerKeys(triggerGroupName);
        List<String> names = new ArrayList<String>(keys.size());
        for(TriggerKey key: keys) {
            names.add(key.getName());
        }
        return names;
	}

	public String getTriggerState(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException {
		return  scheduler.getTriggerState(
				triggerKey(triggerName, triggerGroupName)).name();
	}

	public TabularData getTriggersOfJob(String instanceId, String jobName,
			String jobGroupName) throws SchedulerException {
		return TriggerSupport.toTabularData(scheduler.getTriggersOfJob(
				jobKey(jobName, jobGroupName)));
	}

	public boolean interruptJob(String instanceId, String jobName,
			String jobGroupName) throws UnableToInterruptJobException {
		return scheduler.interrupt(jobKey(jobName, jobGroupName));
	}

	public Date scheduleJob(String instanceId, String jobName, String jobGroup,
			String triggerName, String triggerGroup) throws SchedulerException {
		JobDetail jobDetail = scheduler.getJobDetail(jobKey(jobName, jobGroup));
		if (jobDetail == null) {
			throw new SchedulerException("No such job: " + jobName + "."
					+ jobGroup);
		}
		Trigger trigger = scheduler.getTrigger(triggerKey(triggerName, triggerGroup));
		if (trigger == null) {
			throw new SchedulerException("No such trigger: " + triggerName
					+ "." + triggerGroup);
		}
		return scheduler.scheduleJob(jobDetail, trigger);
	}

	public boolean unscheduleJob(String instanceId, String triggerName,
			String triggerGroup) throws SchedulerException {
		return scheduler.unscheduleJob(triggerKey(triggerName, triggerGroup));
	}

   public void clear() throws SchedulerException {
        scheduler.clear();
    }

	public String getVersion() {
		return scheduler.getVersion();
	}

	public boolean isShutdown() {
		return scheduler.isShutdown();
	}

	public boolean isStarted() {
		return scheduler.isStarted();
	}

	public void start() throws SchedulerException {
		scheduler.start();
	}

	public void shutdown() {
		scheduler.shutdown();
	}

	public void standby() {
		scheduler.standby();
	}

	public boolean isStandbyMode() {
		return scheduler.isInStandbyMode();
	}

	public String getSchedulerName() {
		return scheduler.getSchedulerName();
	}

	public String getSchedulerInstanceId() {
		return scheduler.getSchedulerInstanceId();
	}

	public String getThreadPoolClassName() {
		return scheduler.getThreadPoolClass().getName();
	}

	public int getThreadPoolSize() {
		return scheduler.getThreadPoolSize();
	}

	public void pauseJob(String instanceId, String jobName, String groupName)
			throws SchedulerException {
		scheduler.pauseJob(jobKey(jobName, groupName));
	}

	public void pauseJobGroup(String instanceId, String jobGroupName)
			throws SchedulerException {
		scheduler
				.pauseJobGroup(jobGroupName);
	}

	public void pauseAllTriggers(String instanceId) throws SchedulerException {
		scheduler.pauseAll();
	}

	public void pauseTriggerGroup(String instanceId, String groupName)
			throws SchedulerException {
		scheduler.pauseTriggerGroup(groupName);
	}

	public void pauseTrigger(String instanceId, String triggerName,
			String triggerGroup) throws SchedulerException {
		scheduler.pauseTrigger(triggerKey(triggerName, triggerGroup));
	}

	public void resumeAllTriggers(String instanceId) throws SchedulerException {
		scheduler.resumeAll();
	}

	public void resumeJob(String instanceId, String jobName, String jobGroupName)
			throws SchedulerException {
		scheduler.resumeJob(jobKey(jobName, jobGroupName));
	}

	public void resumeJobGroup(String instanceId, String jobGroupName)
			throws SchedulerException {
		scheduler.resumeJobGroup(jobGroupName);
	}

	public void resumeTrigger(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException {
		scheduler.resumeTrigger(triggerKey(triggerName, triggerGroupName));
	}

	public void resumeTriggerGroup(String instanceId, String groupName)
			throws SchedulerException {
		scheduler.resumeTriggerGroup(groupName);
	}

	public void triggerJob(String instanceId, String jobName,
			String jobGroupName, Map<String, String> jobDataMap)
			throws SchedulerException {
		scheduler.triggerJob(jobKey(jobName, jobGroupName), new JobDataMap(jobDataMap));
	}

	// ScheduleListener

	public void jobAdded(JobDetail jobDetail) {
		sendNotification(JOB_ADDED, JobDetailSupport.toCompositeData(jobDetail));
	}

	public void jobDeleted(JobKey jobKey) {
		sendNotification(JOB_DELETED, jobKey.toString());
	}

	public void jobScheduled(Trigger trigger) {
		sendNotification(JOB_SCHEDULED, TriggerSupport.toCompositeData(trigger));
	}

	public void jobUnscheduled(TriggerKey triggerKey) {
		sendNotification(JOB_UNSCHEDULED, triggerKey.toString());
	}
	
    public void schedulingDataCleared() {
        sendNotification(SCHEDULING_DATA_CLEARED);
    }
    
	public void jobPaused(JobKey jobKey) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("jobName", jobKey.getName());
		map.put("jobGroup", jobKey.getGroup());
		sendNotification(JOBS_PAUSED, map);
	}

    public void jobsPaused(String jobGroup) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("jobName", null);
        map.put("jobGroup", jobGroup);
        sendNotification(JOBS_PAUSED, map);
    }
    
	public void jobsResumed(String jobGroup) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("jobName", null);
		map.put("jobGroup", jobGroup);
		sendNotification(JOBS_RESUMED, map);
	}

    public void jobResumed(JobKey jobKey) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("jobName", jobKey.getName());
        map.put("jobGroup", jobKey.getGroup());
        sendNotification(JOBS_RESUMED, map);
    }
    
	public void schedulerError(String msg, SchedulerException cause) {
		sendNotification(SCHEDULER_ERROR, cause.getMessage());
	}

	public void schedulerStarted() {
		sendNotification(SCHEDULER_STARTED);
	}

	public void schedulerInStandbyMode() {
		sendNotification(SCHEDULER_PAUSED);
	}

	public void schedulerShutdown() {
		scheduler.removeInternalSchedulerListener(this);
		scheduler.removeInternalJobListener(getName());

		sendNotification(SCHEDULER_SHUTDOWN);
	}

	public void schedulerShuttingdown() {
	}

	public void triggerFinalized(Trigger trigger) {
		sendNotification(TRIGGER_FINALIZED, trigger.getKey().toString());
	}

	public void triggersPaused(String triggerGroup) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("triggerName", null);
		map.put("triggerGroup", triggerGroup);
		sendNotification(TRIGGERS_PAUSED, map);
	}

    public void triggerPaused(TriggerKey triggerKey) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("triggerName", triggerKey.getName());
        map.put("triggerGroup", triggerKey.getGroup());
        sendNotification(TRIGGERS_PAUSED, map);
    }

	public void triggersResumed(String triggerGroup) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("triggerName", null);
		map.put("triggerGroup", triggerGroup);
		sendNotification(TRIGGERS_RESUMED, map);
	}

    public void triggerResumed(TriggerKey triggerKey) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("triggerName", triggerKey.getName());
        map.put("triggerGroup", triggerKey.getGroup());
        sendNotification(TRIGGERS_RESUMED, map);
    }
    
	// JobListener

	public String getName() {
		return "QuartzSchedulerMBeanImpl.listener";
	}

	public void jobExecutionVetoed(JobExecutionContext context) {
		try {
			sendNotification(JOB_EXECUTION_VETOED, JobExecutionContextSupport
					.toCompositeData(context));
		} catch (SchedulerException se) {
			// logger.warn(se);
		}
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		try {
			sendNotification(JOB_TO_BE_EXECUTED, JobExecutionContextSupport
					.toCompositeData(context));
		} catch (SchedulerException se) {
			// logger.warn(se);
		}
	}

	public void jobWasExecuted(JobExecutionContext context,
			JobExecutionException jobException) {
		try {
			sendNotification(JOB_WAS_EXECUTED, JobExecutionContextSupport
					.toCompositeData(context));
		} catch (SchedulerException se) {
			// logger.warn(se);
		}
	}

	// NotificationBroadcaster

	/**
	 * sendNotification
	 * 
	 * @param eventType
	 */
	public void sendNotification(String eventType) {
		sendNotification(eventType, null, null);
	}

	/**
	 * sendNotification
	 * 
	 * @param eventType
	 * @param data
	 */
	public void sendNotification(String eventType, Object data) {
		sendNotification(eventType, data, null);
	}

	/**
	 * sendNotification
	 * 
	 * @param eventType
	 * @param data
	 * @param msg
	 */
	public void sendNotification(String eventType, Object data, String msg) {
		Notification notif = new Notification(eventType, this, sequenceNumber
				.incrementAndGet(), System.currentTimeMillis(), msg);
		if (data != null) {
			notif.setUserData(data);
		}
		emitter.sendNotification(notif);
	}

	/**
	 * @author gkeim
	 */
	private class Emitter extends NotificationBroadcasterSupport {
		/**
		 * @see javax.management.NotificationBroadcasterSupport#getNotificationInfo()
		 */
		@Override
		public MBeanNotificationInfo[] getNotificationInfo() {
			return QuartzSchedulerMBeanImpl.this.getNotificationInfo();
		}
	}

	/**
	 * @see javax.management.NotificationBroadcaster#addNotificationListener(javax.management.NotificationListener,
	 *      javax.management.NotificationFilter, java.lang.Object)
	 */
	public void addNotificationListener(NotificationListener notif,
			NotificationFilter filter, Object callBack) {
		emitter.addNotificationListener(notif, filter, callBack);
	}

	/**
	 * @see javax.management.NotificationBroadcaster#getNotificationInfo()
	 */
	public MBeanNotificationInfo[] getNotificationInfo() {
		return NOTIFICATION_INFO;
	}

	/**
	 * @see javax.management.NotificationBroadcaster#removeNotificationListener(javax.management.NotificationListener)
	 */
	public void removeNotificationListener(NotificationListener listener)
			throws ListenerNotFoundException {
		emitter.removeNotificationListener(listener);
	}

	/**
	 * @see javax.management.NotificationEmitter#removeNotificationListener(javax.management.NotificationListener,
	 *      javax.management.NotificationFilter, java.lang.Object)
	 */
	public void removeNotificationListener(NotificationListener notif,
			NotificationFilter filter, Object callBack)
			throws ListenerNotFoundException {
		emitter.removeNotificationListener(notif, filter, callBack);
	}

	public synchronized boolean isSampledStatisticsEnabled() {
		return sampledStatisticsEnabled;
	}

	public void setSampledStatisticsEnabled(boolean enabled) {
		if (enabled != this.sampledStatisticsEnabled) {
			this.sampledStatisticsEnabled = enabled;
			if(enabled) {
	            this.sampledStatistics = new SampledStatisticsImpl(scheduler);
			}
			else {
			     this.sampledStatistics.shutdown(); 
		         this.sampledStatistics = NULL_SAMPLED_STATISTICS;
			}
			sendNotification(SAMPLED_STATISTICS_ENABLED, Boolean.valueOf(enabled));
		}
	}

	public long getJobsCompletedMostRecentSample() {
		return this.sampledStatistics.getJobsCompletedMostRecentSample();
	}

	public long getJobsExecutedMostRecentSample() {
		return this.sampledStatistics.getJobsExecutingMostRecentSample();
	}

	public long getJobsScheduledMostRecentSample() {
		return this.sampledStatistics.getJobsScheduledMostRecentSample();
	}

	public Map<String, Long> getPerformanceMetrics() {
		Map<String, Long> result = new HashMap<String, Long>();
		result.put("JobsCompleted", Long
				.valueOf(getJobsCompletedMostRecentSample()));
		result.put("JobsExecuted", Long
				.valueOf(getJobsExecutedMostRecentSample()));
		result.put("JobsScheduled", Long
				.valueOf(getJobsScheduledMostRecentSample()));
		return result;
	}

}
