package org.quartz.core.jmx;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
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

    static final String SCHEDULING_DATA_CLEARED = "schedulingDataCleared";

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
	 * @throws Exception
	 */
	TabularData getCurrentlyExecutingJobs() throws Exception;

	/**
	 * @return TabularData of CompositeData:JobDetail
	 * @throws Exception
	 * @see JobDetailSupport
	 */
	TabularData getAllJobDetails() throws Exception;

	/**
	 * @return List of CompositeData:[CronTrigger|SimpleTrigger]
	 * @throws Exception
	 * @see TriggerSupport
	 */
	List<CompositeData> getAllTriggers() throws Exception;

	List<String> getJobGroupNames() throws Exception;

	List<String> getJobNames(String groupName)
			throws Exception;

	/**
	 * @return CompositeData:JobDetail
	 * @throws Exception
	 * @see JobDetailSupport
	 */
	CompositeData getJobDetail(String jobName, String jobGroupName) throws Exception;

	boolean isStarted();

	void start() throws Exception;

	void shutdown();

	void standby();

	void clear() throws Exception;
	
	/**
	 * Schedule an existing job with an existing trigger.
	 * 
	 * @param jobName
	 * @param jobGroup
	 * @param triggerName
	 * @param triggerGroup
	 * @return date of nextFireTime
	 * @throws Exception
	 */
	Date scheduleJob(String jobName, String jobGroup,
			String triggerName, String triggerGroup) throws Exception;

	/**
	 * Schedules a job using the given Cron/Simple triggerInfo.
	 * 
	 * The triggerInfo and jobDetailInfo must contain well-known attribute values.
	 *     TriggerInfo attributes: name, group, description, calendarName, priority,
	 *       CronExpression | (startTime, endTime, repeatCount, repeatInterval) 
	 *     JobDetailInfo attributes: name, group, description, jobClass, jobDataMap, durability,
	 *       shouldRecover
	 */
	void scheduleBasicJob(Map<String, Object> jobDetailInfo, Map<String, Object> triggerInfo)
			throws Exception;

	/**
	 * Schedules an arbitrary job described by abstractJobInfo using a trigger specified by abstractTriggerInfo.
	 * 
	 * AbtractTriggerInfo and AbstractJobInfo must contain the following String attributes.
	 *     AbstractTriggerInfo: triggerClass, the fully-qualified class name of a concrete Trigger type
	 *     AbstractJobInfo: jobDetailClass, the fully-qualified class name of a concrete JobDetail type
	 *
	 * If the Trigger and JobDetail can be successfully instantiated, the remaining attributes will be
	 * reflectively applied to those instances. The remaining attributes are limited to the types:
	 *   Integer, Double, Float, String, Boolean, Date, Character, Map<String, Object>.
	 * Maps are further limited to containing values from the same set of types, less Map itself.
	 * 
	 * @throws Exception 
	 */
	void scheduleJob(Map<String, Object> abstractJobInfo,
			Map<String, Object> abstractTriggerInfo) throws Exception;
	
	/**
	 * Schedules the specified job using a trigger described by abstractTriggerInfo, which must contain the
	 * fully-qualified trigger class name under the key "triggerClass."  That trigger type must contain a
	 * no-arg constructor and have public access. Other attributes are applied reflectively and are limited
	 * to the types:
	 *   Integer, Double, Float, String, Boolean, Date, Character, Map<String, Object>.
	 * Maps are limited to containing values from the same set of types, less Map itself.
	 * 
	 * @param jobName
	 * @param jobGroup
	 * @param abstractTriggerInfo
	 * @throws Exception
	 */
	void scheduleJob(String jobName, String jobGroup,
			Map<String, Object> abstractTriggerInfo) throws Exception;
	
	boolean unscheduleJob(String triggerName, String triggerGroup) throws Exception;

	boolean interruptJob(String jobName, String jobGroupName) throws Exception;

	void triggerJob(String jobName, String jobGroupName,
			Map<String, String> jobDataMap) throws Exception;

	boolean deleteJob(String jobName, String jobGroupName)
			throws Exception;

	void addJob(CompositeData jobDetail, boolean replace) throws Exception;

	/**
	 * Adds a durable job described by abstractJobInfo, which must contain the fully-qualified JobDetail
	 * class name under the key "jobDetailClass."  That JobDetail type must contain a no-arg constructor
	 * and have public access. Other attributes are applied reflectively and are limited
	 * to the types:
	 *   Integer, Double, Float, String, Boolean, Date, Character, Map<String, Object>.
	 * Maps are limited to containing values from the same set of types, less Map itself.
	 * 
	 * @param jobDetailMap
	 * @param replace
	 * @throws Exception
	 */
	void addJob(Map<String, Object> abstractJobInfo, boolean replace)
			throws Exception;

	void pauseJobGroup(String jobGroupName)	throws Exception;

	void resumeJobGroup(String jobGroupName) throws Exception;

	void pauseJob(String jobName, String groupName) throws Exception;

	void resumeJob(String jobName, String jobGroupName)	throws Exception;

	List<String> getTriggerGroupNames() throws Exception;

	List<String> getTriggerNames(String triggerGroupName) throws Exception;

	CompositeData getTrigger(String triggerName, String triggerGroupName) throws Exception;

	String getTriggerState(String triggerName, String triggerGroupName) throws Exception;

	/**
	 * @return List of CompositeData:[CronTrigger|SimpleTrigger] for the specified job.
	 * @see TriggerSupport
	 */
	List<CompositeData> getTriggersOfJob(String jobName, String jobGroupName) throws Exception;

	Set<String> getPausedTriggerGroups() throws Exception;

	void pauseAllTriggers() throws Exception;

	void resumeAllTriggers() throws Exception;

	void pauseTriggerGroup(String groupName) throws Exception;

	void resumeTriggerGroup(String groupName) throws Exception;

	void pauseTrigger(String triggerName, String triggerGroupName) throws Exception;

	void resumeTrigger(String triggerName, String triggerGroupName) throws Exception;

	List<String> getCalendarNames() throws Exception;

	void deleteCalendar(String name) throws Exception;

	void setSampledStatisticsEnabled(boolean enabled);

	boolean isSampledStatisticsEnabled();
}
