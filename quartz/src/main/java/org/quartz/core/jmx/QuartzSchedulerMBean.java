package org.quartz.core.jmx;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;

public interface QuartzSchedulerMBean {
	static final String SCHEDULER_STARTED = "schedulerStarted";
	static final String SCHEDULER_PAUSED = "schedulerPaused";
	static final String SCHEDULER_SHUTDOWN = "schedulerShutdown";
	static final String SCHEDULER_ERROR = "schedulerError";

	static final String JOB_ADDED = "jobAdded";
	static final String JOB_DELETED = "jobDeleted";
	static final String JOB_SCHEDULED = "jobScheduled";
	static final String JOB_UNSCHEDULED = "jobUnscheduled";
	static final String JOBS_PAUSED = "jobsPaused";
	static final String JOBS_RESUMED = "jobsResumed";

	static final String JOB_EXECUTION_VETOED = "jobExecutionVetoed";
	static final String JOB_TO_BE_EXECUTED = "jobToBeExecuted";
	static final String JOB_WAS_EXECUTED = "jobWasExecuted";

	static final String TRIGGER_FINALIZED = "triggerFinalized";
	static final String TRIGGERS_PAUSED = "triggersPaused";
	static final String TRIGGERS_RESUMED = "triggersResumed";

	static final String SAMPLED_STATISTICS_ENABLED = "sampledStatisticsEnabled";
	static final String SAMPLED_STATISTICS_RESET = "sampledStatisticsReset";

	String getSchedulerName();

	String getSchedulerInstanceId();

	boolean isStandbyMode();

	boolean isShutdown();

	String getVersion();

	String getJobStoreClassName();

	String getThreadPoolClassName();

	int getThreadPoolSize();

	long getJobsScheduledMostRecentSample();

	long getJobsExecutedMostRecentSample();

	long getJobsCompletedMostRecentSample();

	Map<String, Long> getPerformanceMetrics();

	/**
	 * @return TabularData of CompositeData:JobExecutionContext
	 * @throws SchedulerException
	 */
	TabularData getCurrentlyExecutingJobs() throws SchedulerException;

	/**
	 * @return TabularData of CompositeData:JobDetail
	 * @throws SchedulerException
	 * @see JobDetailSupport
	 */
	TabularData getAllJobDetails(String instanceId) throws SchedulerException;

	/**
	 * @return TabularData of CompositeData:Trigger
	 * @throws SchedulerException
	 * @see TriggerSupport
	 */
	TabularData getAllTriggers(String instanceId) throws SchedulerException;

	String[] getJobGroupNames(String instanceId) throws SchedulerException;

	String[] getJobNames(String instanceId, String groupName)
			throws SchedulerException;

	/**
	 * @return CompositeData:JobDetail
	 * @throws SchedulerException
	 * @see JobDetailSupport
	 */
	CompositeData getJobDetail(String instanceId, String jobName,
			String jobGroupName) throws SchedulerException;

	boolean isStarted();

	void start() throws SchedulerException;

	void shutdown();

	void standby();

	Date scheduleJob(String instanceId, String jobName, String jobGroup,
			String triggerName, String triggerGroup) throws SchedulerException;

	boolean unscheduleJob(String instanceId, String triggerName,
			String triggerGroup) throws SchedulerException;

	boolean interruptJob(String instanceId, String jobName, String jobGroupName)
			throws UnableToInterruptJobException;

	void triggerJob(String instanceId, String jobName, String jobGroupName,
			Map<String, String> jobDataMap) throws SchedulerException;

	void triggerJobWithVolatileTrigger(String instanceId, String jobName,
			String jobGroupName, Map<String, String> jobDataMap)
			throws SchedulerException;

	boolean deleteJob(String instanceId, String jobName, String jobGroupName)
			throws SchedulerException;

	void addJob(String instanceId, CompositeData jobDetail, boolean replace)
			throws SchedulerException;

	void pauseJobGroup(String instanceId, String jobGroupName)
			throws SchedulerException;

	void resumeJobGroup(String instanceId, String jobGroupName)
			throws SchedulerException;

	void pauseJob(String instanceId, String jobName, String groupName)
			throws SchedulerException;

	void resumeJob(String instanceId, String jobName, String jobGroupName)
			throws SchedulerException;

	String[] getTriggerGroupNames(String instanceId) throws SchedulerException;

	String[] getTriggerNames(String instanceId, String triggerGroupName)
			throws SchedulerException;

	CompositeData getTrigger(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException;

	int getTriggerState(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException;

	TabularData getTriggersOfJob(String instanceId, String jobName,
			String jobGroupName) throws SchedulerException;

	Set<String> getPausedTriggerGroups(String instanceId)
			throws SchedulerException;

	void pauseAllTriggers(String instanceId) throws SchedulerException;

	void resumeAllTriggers(String instanceId) throws SchedulerException;

	void pauseTriggerGroup(String instanceId, String groupName)
			throws SchedulerException;

	void resumeTriggerGroup(String instanceId, String groupName)
			throws SchedulerException;

	void pauseTrigger(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException;

	void resumeTrigger(String instanceId, String triggerName,
			String triggerGroupName) throws SchedulerException;

	String[] getCalendarNames(String instanceId) throws SchedulerException;

	void deleteCalendar(String instanceId, String name)
			throws SchedulerException;

	void setSampledStatisticsEnabled(boolean enabled);

	boolean isSampledStatisticsEnabled();
}
