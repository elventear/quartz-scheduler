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
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.quartz.core.jmx.JobDetailSupport;
import org.quartz.core.jmx.JobExecutionContextSupport;
import org.quartz.core.jmx.QuartzSchedulerMBean;
import org.quartz.core.jmx.TriggerSupport;

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
		this.scheduler.addGlobalJobListener(this);
		this.scheduler.addSchedulerListener(this);
		this.sampledStatistics = NULL_SAMPLED_STATISTICS;
		this.sampledStatisticsEnabled = false;
	}

	public TabularData getCurrentlyExecutingJobs() throws SchedulerException {
		return JobExecutionContextSupport.toTabularData(scheduler
				.getCurrentlyExecutingJobs());
	}

	public TabularData getAllJobDetails(String instanceId)
			throws SchedulerException {
		SchedulingContext cntx = new SchedulingContext(instanceId);
		List<JobDetail> detailList = new ArrayList<JobDetail>();

		for (String jobGroupName : scheduler.getJobGroupNames(cntx)) {
			for (String jobName : scheduler.getJobNames(cntx, jobGroupName)) {
				detailList.add(scheduler.getJobDetail(cntx, jobName,
						jobGroupName));
			}
		}

		return JobDetailSupport.toTabularData(detailList
				.toArray(new JobDetail[detailList.size()]));
	}

	public TabularData getAllTriggers(String instanceId)
			throws SchedulerException {
		SchedulingContext cntx = new SchedulingContext(instanceId);
		List<Trigger> triggerList = new ArrayList<Trigger>();

		for (String triggerGroupName : scheduler.getTriggerGroupNames(cntx)) {
			for (String triggerName : scheduler.getTriggerNames(cntx,
					triggerGroupName)) {
				triggerList.add(scheduler.getTrigger(cntx, triggerName,
						triggerGroupName));
			}
		}

		return TriggerSupport.toTabularData(triggerList
				.toArray(new Trigger[triggerList.size()]));
	}

	public void addJob(String instanceId, CompositeData jobDetail,
			boolean replace) throws SchedulerException {
		scheduler.addJob(new SchedulingContext(instanceId), JobDetailSupport
				.newJobDetail(jobDetail), replace);
	}

	public void deleteCalendar(String instanceId, String name)
			throws SchedulerException {
		scheduler.deleteCalendar(new SchedulingContext(instanceId), name);
	}

	public boolean deleteJob(String instanceId, String jobName,
			String jobGroupName) throws SchedulerException {
		return scheduler.deleteJob(new SchedulingContext(instanceId), jobName,
				jobGroupName);
	}

	public String[] getCalendarNames(String instanceId)
			throws SchedulerException {
		return scheduler.getCalendarNames(new SchedulingContext(instanceId));
	}

	public CompositeData getJobDetail(String instanceId, String jobName,
			String jobGroupName) throws SchedulerException {
		return JobDetailSupport.toCompositeData(scheduler.getJobDetail(
				new SchedulingContext(instanceId), jobName, jobGroupName));
	}

	public String[] getJobGroupNames(String instanceId)
			throws SchedulerException {
		return scheduler.getJobGroupNames(new SchedulingContext(instanceId));
	}

	public String[] getJobNames(String instanceId, String groupName)
			throws SchedulerException {
		return scheduler.getJobNames(new SchedulingContext(instanceId),
				groupName);
	}

	public String getJobStoreClassName() {
		return scheduler.getJobStoreClass().getName();
	}

	public Set<String> getPausedTriggerGroups(String instanceId)
			throws SchedulerException {
		return scheduler.getPausedTriggerGroups(new SchedulingContext(
				instanceId));
	}

	public CompositeData getTrigger(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException {
		return TriggerSupport.toCompositeData(scheduler.getTrigger(
				new SchedulingContext(instanceId), triggerName,
				triggerGroupName));
	}

	public String[] getTriggerGroupNames(String instanceId)
			throws SchedulerException {
		return scheduler
				.getTriggerGroupNames(new SchedulingContext(instanceId));
	}

	public String[] getTriggerNames(String instanceId, String triggerGroupName)
			throws SchedulerException {
		return scheduler.getTriggerNames(new SchedulingContext(instanceId),
				triggerGroupName);
	}

	public int getTriggerState(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException {
		return scheduler.getTriggerState(new SchedulingContext(instanceId),
				triggerName, triggerGroupName);
	}

	public TabularData getTriggersOfJob(String instanceId, String jobName,
			String jobGroupName) throws SchedulerException {
		return TriggerSupport.toTabularData(scheduler.getTriggersOfJob(
				new SchedulingContext(instanceId), jobName, jobGroupName));
	}

	public boolean interruptJob(String instanceId, String jobName,
			String jobGroupName) throws UnableToInterruptJobException {
		return scheduler.interrupt(new SchedulingContext(instanceId), jobName,
				jobGroupName);
	}

	public Date scheduleJob(String instanceId, String jobName, String jobGroup,
			String triggerName, String triggerGroup) throws SchedulerException {
		SchedulingContext cntx = new SchedulingContext(instanceId);
		JobDetail jobDetail = scheduler.getJobDetail(cntx, jobName, jobGroup);
		if (jobDetail == null) {
			throw new SchedulerException("No such job: " + jobName + "."
					+ jobGroup);
		}
		Trigger trigger = scheduler.getTrigger(cntx, triggerName, triggerGroup);
		if (trigger == null) {
			throw new SchedulerException("No such trigger: " + triggerName
					+ "." + triggerGroup);
		}
		return scheduler.scheduleJob(cntx, jobDetail, trigger);
	}

	public boolean unscheduleJob(String instanceId, String triggerName,
			String triggerGroup) throws SchedulerException {
		SchedulingContext cntx = new SchedulingContext(instanceId);
		return scheduler.unscheduleJob(cntx, triggerName, triggerGroup);
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
		scheduler.pauseJob(new SchedulingContext(instanceId), jobName,
				groupName);
	}

	public void pauseJobGroup(String instanceId, String jobGroupName)
			throws SchedulerException {
		scheduler
				.pauseJobGroup(new SchedulingContext(instanceId), jobGroupName);
	}

	public void pauseAllTriggers(String instanceId) throws SchedulerException {
		scheduler.pauseAll(new SchedulingContext(instanceId));
	}

	public void pauseTriggerGroup(String instanceId, String groupName)
			throws SchedulerException {
		scheduler.pauseTriggerGroup(new SchedulingContext(instanceId), groupName);
	}

	public void pauseTrigger(String instanceId, String triggerName,
			String triggerGroup) throws SchedulerException {
		scheduler.pauseTrigger(new SchedulingContext(instanceId), triggerName,
				triggerGroup);
	}

	public void resumeAllTriggers(String instanceId) throws SchedulerException {
		scheduler.resumeAll(new SchedulingContext(instanceId));
	}

	public void resumeJob(String instanceId, String jobName, String jobGroupName)
			throws SchedulerException {
		scheduler.resumeJob(new SchedulingContext(instanceId), jobName,
				jobGroupName);
	}

	public void resumeJobGroup(String instanceId, String jobGroupName)
			throws SchedulerException {
		scheduler.resumeJobGroup(new SchedulingContext(instanceId),
				jobGroupName);
	}

	public void resumeTrigger(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException {
		scheduler.resumeTrigger(new SchedulingContext(instanceId), triggerName,
				triggerGroupName);
	}

	public void resumeTriggerGroup(String instanceId, String groupName)
			throws SchedulerException {
		scheduler.resumeTriggerGroup(new SchedulingContext(instanceId),
				groupName);
	}

	public void triggerJobWithVolatileTrigger(String instanceId,
			String jobName, String jobGroupName, Map<String, String> jobDataMap)
			throws SchedulerException {
		scheduler.triggerJobWithVolatileTrigger(new SchedulingContext(
				instanceId), jobName, jobGroupName, new JobDataMap(jobDataMap));
	}

	public void triggerJob(String instanceId, String jobName,
			String jobGroupName, Map<String, String> jobDataMap)
			throws SchedulerException {
		scheduler.triggerJob(new SchedulingContext(instanceId), jobName,
				jobGroupName, new JobDataMap(jobDataMap));
	}

	// ScheduleListener

	public void jobAdded(JobDetail jobDetail) {
		sendNotification(JOB_ADDED, JobDetailSupport.toCompositeData(jobDetail));
	}

	public void jobDeleted(String jobName, String groupName) {
		sendNotification(JOB_DELETED, groupName + "." + jobName);
	}

	public void jobScheduled(Trigger trigger) {
		sendNotification(JOB_SCHEDULED, TriggerSupport.toCompositeData(trigger));
	}

	public void jobUnscheduled(String triggerName, String triggerGroup) {
		sendNotification(JOB_UNSCHEDULED, triggerGroup + "." + triggerName);
	}

	public void jobsPaused(String jobName, String jobGroup) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("jobName", jobName);
		map.put("jobGroup", jobGroup);
		sendNotification(JOBS_PAUSED, map);
	}

	public void jobsResumed(String jobName, String jobGroup) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("jobName", jobName);
		map.put("jobGroup", jobGroup);
		sendNotification(JOBS_RESUMED, map);
	}

	public void schedulerError(String msg, SchedulerException cause) {
		sendNotification(SCHEDULER_ERROR, cause.getErrorCode(), cause
				.getMessage());
	}

	public void schedulerStarted() {
		sendNotification(SCHEDULER_STARTED);
	}

	public void schedulerInStandbyMode() {
		sendNotification(SCHEDULER_PAUSED);
	}

	public void schedulerShutdown() {
		scheduler.removeSchedulerListener(this);
		scheduler.removeGlobalJobListener(getName());

		sendNotification(SCHEDULER_SHUTDOWN);
	}

    public void schedulerShuttingdown() {
    }

	public void triggerFinalized(Trigger trigger) {
		sendNotification(TRIGGER_FINALIZED, trigger.getFullName());
	}

	public void triggersPaused(String triggerName, String triggerGroup) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("triggerName", triggerName);
		map.put("triggerGroup", triggerGroup);
		sendNotification(TRIGGERS_PAUSED, map);
	}

	public void triggersResumed(String triggerName, String triggerGroup) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("triggerName", triggerName);
		map.put("triggerGroup", triggerGroup);
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
