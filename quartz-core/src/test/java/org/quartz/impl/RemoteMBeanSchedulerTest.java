package org.quartz.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.core.QuartzSchedulerResources;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.impl.matchers.GroupMatcher;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * RemoteMBeanSchedulerTest
 */
public class RemoteMBeanSchedulerTest {

    public static final String TRIGGER_KEY = "trigger1";
    public static final String GROUP_KEY = "group1";
    public static final String JOB_KEY = "job1";
    public static final String CALENDAR_KEY = "calendar1";

    private Scheduler scheduler;
    private RemoteMBeanScheduler remoteScheduler;

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.put("org.quartz.scheduler.instanceName", "TestScheduler");
        props.put("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        props.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        props.put("org.quartz.threadPool.threadCount", "1");
        props.put("org.quartz.scheduler.jmx.export", "true");

        scheduler = new StdSchedulerFactory(props).getScheduler();

        JobDetail jobDetail = newJob(HelloJob.class).withIdentity(JOB_KEY, GROUP_KEY).build();
        Trigger trigger = newTrigger().withIdentity(TRIGGER_KEY, GROUP_KEY).startAt(new Date()).build();

        scheduler.addCalendar(CALENDAR_KEY, new BaseCalendar(), false, false);

        scheduler.scheduleJob(jobDetail, trigger);

        String objectName = QuartzSchedulerResources.generateJMXObjectName(scheduler.getSchedulerName(), scheduler.getSchedulerInstanceId());
        remoteScheduler = new TestRemoteScheduler(objectName);
    }

    @After
    public void tearDown() throws SchedulerException {
        scheduler.shutdown();
    }

    @Test
    public void testJMXAttributesAccess() throws Exception {
        assertThat(remoteScheduler.getCalendarNames(), equalTo(scheduler.getCalendarNames()));
        assertThat(remoteScheduler.getJobGroupNames(), equalTo(scheduler.getJobGroupNames()));
        assertThat(remoteScheduler.getPausedTriggerGroups(), equalTo(scheduler.getPausedTriggerGroups()));
        assertThat(remoteScheduler.getSchedulerInstanceId(), equalTo(scheduler.getSchedulerInstanceId()));
        assertThat(remoteScheduler.getSchedulerName(), equalTo(scheduler.getSchedulerName()));
        assertThat(remoteScheduler.getTriggerGroupNames(), equalTo(scheduler.getTriggerGroupNames()));
    }

    @Test
    public void testSchedulerMetaData() throws Exception{
        SchedulerMetaData remoteSchedulerMetaData = remoteScheduler.getMetaData();
        SchedulerMetaData metaData = scheduler.getMetaData();
        assertThat(remoteSchedulerMetaData.getSchedulerName(), equalTo(metaData.getSchedulerName()));
        assertThat(remoteSchedulerMetaData.getSchedulerInstanceId(), equalTo(metaData.getSchedulerInstanceId()));
        assertThat(remoteSchedulerMetaData.isInStandbyMode(), is(metaData.isInStandbyMode()));
        assertThat(remoteSchedulerMetaData.getSchedulerClass(), equalTo((Class)TestRemoteScheduler.class));
        assertThat(remoteSchedulerMetaData.isSchedulerRemote(), is(true));
        assertThat(remoteSchedulerMetaData.isStarted(), is(false)); // information not available through JMX
        assertThat(remoteSchedulerMetaData.isInStandbyMode(), is(metaData.isInStandbyMode()));
        assertThat(remoteSchedulerMetaData.isShutdown(), is(metaData.isShutdown()));
        assertThat(remoteSchedulerMetaData.getRunningSince(), nullValue()); // Information not available through JMX
        assertThat(remoteSchedulerMetaData.getNumberOfJobsExecuted(), is(metaData.getNumberOfJobsExecuted()));
        assertThat(remoteSchedulerMetaData.getJobStoreClass(), equalTo((Class)metaData.getJobStoreClass()));
        assertThat(remoteSchedulerMetaData.isJobStoreSupportsPersistence(), is(false)); // Information not available through JMX
        assertThat(remoteSchedulerMetaData.isJobStoreClustered(), is(false)); // Information not available through JMX
        assertThat(remoteSchedulerMetaData.getThreadPoolClass(), equalTo((Class)metaData.getThreadPoolClass()));
        assertThat(remoteSchedulerMetaData.getThreadPoolSize(), is(metaData.getThreadPoolSize()));
        assertThat(remoteSchedulerMetaData.getVersion(), equalTo(metaData.getVersion()));
        assertThat(remoteSchedulerMetaData.getJobStoreClass(), equalTo((Class)metaData.getJobStoreClass()));
    }

    @Test
    public void testCalendarOperations() throws Exception {
        try {
            remoteScheduler.addCalendar("testCal", new BaseCalendar(), true, true);
            fail("Method was not exposed in MBean API");
        } catch (SchedulerException e) {
            // expected
        }

        try {
            remoteScheduler.getCalendar("test");
            fail("Method was not exposed in MBean API");
        } catch (SchedulerException e) {
            // expected
        }

        remoteScheduler.deleteCalendar(CALENDAR_KEY);
        assertThat(scheduler.getCalendar(CALENDAR_KEY), nullValue());
    }

    @Test
    public void testTriggerOperations() throws Exception {
        TriggerKey triggerKey = new TriggerKey(TRIGGER_KEY, GROUP_KEY);
        GroupMatcher<TriggerKey> groupMatcher = GroupMatcher.triggerGroupEquals(GROUP_KEY);

        try {
            remoteScheduler.getTrigger(triggerKey);
            fail("Method had a different return type in MBean API");
        } catch (SchedulerException e) {
            // expected
        }

        try {
            remoteScheduler.getTriggersOfJob(new JobKey(JOB_KEY, GROUP_KEY));
            fail("Method had a different return type in MBean API");
        } catch (SchedulerException e) {
            // expected
        }

        assertThat(remoteScheduler.getTriggerState(triggerKey), is(scheduler.getTriggerState(triggerKey)));

        try {
            remoteScheduler.getTriggerKeys(groupMatcher);
            fail("Method was not exposed in MBean API");
        } catch (SchedulerException e) {
            // expected
        }

        remoteScheduler.pauseTrigger(triggerKey);
        assertThat(scheduler.getTriggerState(triggerKey), is(Trigger.TriggerState.PAUSED));

        remoteScheduler.resumeTrigger(triggerKey);
        assertThat(scheduler.getTriggerState(triggerKey), is(Trigger.TriggerState.NORMAL));

        remoteScheduler.pauseTriggers(groupMatcher);
        assertThat(scheduler.getTriggerState(triggerKey), is(Trigger.TriggerState.PAUSED));

        remoteScheduler.resumeTriggers(groupMatcher);
        assertThat(scheduler.getTriggerState(triggerKey), is(Trigger.TriggerState.NORMAL));

    }

    @Test
    public void testJMXOperations() throws Exception {
        remoteScheduler.clear();
        assertThat(remoteScheduler.getJobGroupNames().isEmpty(), is(true));
    }

    @Test
    public void testUnsupportedMethods() {
        try {
            remoteScheduler.getListenerManager();
            fail("Operation should not be supported");
        } catch (SchedulerException e) {
            // expected
        }
    }

    @Test
    public void testListBrokenAttributes() throws Exception {
        try {
            remoteScheduler.getContext();
            fail("Method was not exposed in MBean API");
        } catch (SchedulerException e) {
            // expected
        }

        try {
            remoteScheduler.getCurrentlyExecutingJobs();
            fail("Method had a different return type in MBean API");
        } catch (SchedulerException e) {
            // expected
        }

    }

    public static class HelloJob implements Job {
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("hello world!");
        }
    }

    public static class TestRemoteScheduler extends RemoteMBeanScheduler {

        private MBeanServer mBeanServer;
        private ObjectName objectName;

        public TestRemoteScheduler(String objectName) throws SchedulerException, MalformedObjectNameException {
            this.objectName = new ObjectName(objectName);
            initialize();
        }

        @Override
        public void initialize() throws SchedulerException {
            mBeanServer = ManagementFactory.getPlatformMBeanServer();
        }

        @Override
        protected Object getAttribute(String attribute) throws SchedulerException {
            try {
                return mBeanServer.getAttribute(objectName, attribute);
            } catch (Exception e) {
                throw new SchedulerException(e);
            }
        }

        @Override
        protected AttributeList getAttributes(String[] attributes) throws SchedulerException {
            try {
                return mBeanServer.getAttributes(objectName, attributes);
            }catch (Exception e) {
                throw new SchedulerException(e);
            }

        }

        @Override
        protected Object invoke(String operationName, Object[] params, String[] signature) throws SchedulerException {
            try {
                return mBeanServer.invoke(objectName, operationName, params, signature);
            } catch (Exception e) {
                throw new SchedulerException(e);
            }
        }
    }
}
