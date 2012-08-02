package org.quartz.integrations.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import org.apache.derby.drda.NetworkServerControl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QTZ283_IgnoreMisfirePolicyJdbcStore_Test {

    private static final long DURATION_OF_FIRST_SCHEDULING = 10L;
    private static final Logger LOG = LoggerFactory.getLogger(QTZ283_IgnoreMisfirePolicyJdbcStore_Test.class);
    private static final int INTERVAL_IN_SECONDS = 5;
    private static NetworkServerControl derbyServer;
    private static Scheduler sched;

    @BeforeClass
    public static void initialize() throws Exception {
        LOG.info("------- Starting Database ---------------------");
        derbyServer = new NetworkServerControl();
        derbyServer.start(new PrintWriter(System.out));
        int tries = 0;
        while (tries < 5) {
            try {
                Thread.sleep(500);
                derbyServer.ping();
                break;
            } catch (Exception e) {
                tries++;
            }
        }
        if (tries == 5) {
            throw new Exception("Failed to start Derby!");
        }
        LOG.info("------- Database started ---------------------");
        try {
            LOG.info("------- Creating Database tables ---------------------");
            JdbcQuartzDerbyUtilities.createDatabase();
            LOG.info("------- Database tables created ---------------------");
        } catch (SQLException e) {
            e.printStackTrace();
            e.getNextException().printStackTrace();
        }

        Properties properties = new Properties();
        
        properties.put("org.quartz.scheduler.instanceName","TestScheduler");
        properties.put("org.quartz.scheduler.instanceId","AUTO");
        properties.put("org.quartz.scheduler.skipUpdateCheck","true");
        properties.put("org.quartz.threadPool.class","org.quartz.simpl.SimpleThreadPool");
        properties.put("org.quartz.threadPool.threadCount","12");
        properties.put("org.quartz.threadPool.threadPriority","5");
        properties.put("org.quartz.jobStore.misfireThreshold","10000");
        properties.put("org.quartz.jobStore.class","org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.put("org.quartz.jobStore.driverDelegateClass","org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.put("org.quartz.jobStore.useProperties","true");
        properties.put("org.quartz.jobStore.dataSource","myDS");
        properties.put("org.quartz.jobStore.tablePrefix","QRTZ_");
        properties.put("org.quartz.jobStore.isClustered","false");
        properties.put("org.quartz.dataSource.myDS.driver","org.apache.derby.jdbc.ClientDriver");
        properties.put("org.quartz.dataSource.myDS.URL",JdbcQuartzDerbyUtilities.DATABASE_CONNECTION_PREFIX);
        properties.put("org.quartz.dataSource.myDS.user","quartz");
        properties.put("org.quartz.dataSource.myDS.password","quartz");
        properties.put("org.quartz.dataSource.myDS.maxConnections","5");
        // we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory(properties);
        sched = sf.getScheduler();
        LOG.info("------- Initializing ----------------------");

        LOG.info("------- Initialization Complete -----------");

        LOG.info("------- Scheduling Job  -------------------");

        // define the jobs and tie them to our HelloJob class
        JobDetail job1 = newJob(HelloJob.class).withIdentity("job1", "group1").build();

        // trigger should have started the even minute before now
        // due to its ignore policy, it will be triggered
        Date startTime1 = DateBuilder.evenMinuteDateBefore(null);
        SimpleTrigger oldtriggerMisfirePolicyIgnore = newTrigger()
                .withIdentity("trigger1", "group1")
                .startAt(startTime1)
                .withSchedule(
                        simpleSchedule().withIntervalInSeconds(INTERVAL_IN_SECONDS).repeatForever().withMisfireHandlingInstructionIgnoreMisfires())
                .build();

        if (sched.checkExists(job1.getKey())) {
            // the job already exists in jdbcjobstore; let's reschedule it
            sched.rescheduleJob(oldtriggerMisfirePolicyIgnore.getKey(), oldtriggerMisfirePolicyIgnore);
        } else {
            sched.scheduleJob(job1, oldtriggerMisfirePolicyIgnore);
        }

        // Start up the scheduler (nothing can actually run until the
        // scheduler has been started)
        sched.start();

        LOG.info("------- Scheduler Started -----------------");

        // wait long enough so that the scheduler as an opportunity to
        // run the job!
        try {
            Thread.sleep(DURATION_OF_FIRST_SCHEDULING * 1000L);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void checkOldTriggerGetsFired() throws SQLException {
        BigDecimal misfirePolicyIgnoreTimesTriggered = JdbcQuartzDerbyUtilities.timesTriggered("trigger1", "group1");
        assertThat("The old trigger has never been fired, even if the policy is ignore", misfirePolicyIgnoreTimesTriggered,
                not(equalTo(BigDecimal.ZERO)));
    }

    @AfterClass
    public static void shutdownDb() throws Exception {

        // shut down the scheduler
        LOG.info("------- Shutting Down Scheduler---------------------");
        sched.shutdown(true);
        LOG.info("------- Shutdown Complete -----------------");
        try {
            LOG.info("------- Destroying Database ---------------------");
            JdbcQuartzDerbyUtilities.destroyDatabase();
            LOG.info("------- Database destroyed ---------------------");
        } catch (SQLException e) {
            e.printStackTrace();
            e.getNextException().printStackTrace();
            throw new AssertionError(e);
        }

        derbyServer.shutdown();
        LOG.info("------- Database shutdown ---------------------");

    }
}
