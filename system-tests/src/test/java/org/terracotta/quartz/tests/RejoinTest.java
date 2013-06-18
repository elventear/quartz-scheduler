/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;
import com.tc.util.concurrent.ThreadUtil;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import org.terracotta.test.util.TestBaseUtil;
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.concurrent.ToolkitBarrier;
import org.terracotta.toolkit.concurrent.atomic.ToolkitAtomicLong;
import org.terracotta.toolkit.rejoin.RejoinException;

/**
 *
 * @author cdennis
 */
public class RejoinTest extends AbstractStandaloneTest {
  
  public RejoinTest(TestConfig testConfig) {
    super(testConfig, RejoinClient.class, RejoinClient.class);

    testConfig.setNumOfGroups(1);
    testConfig.getGroupConfig().setMemberCount(2);
    testConfig.getGroupConfig().setElectionTime(5);
    testConfig.setRestartable(false);
    testConfig.setClientReconnectWindow(1);
    
    // sets L2_L1RECONNECT_ENABLED true and L2_L1RECONNECT_TIMEOUT_MILLS 20 sec
    TestBaseUtil.enableL1Reconnect(testConfig);
    
    //XXX Disabled pending resolution of ENG-7
    disableTest();
  }

  @Override
  protected boolean isDisabled() {
    return false;
  }

  public static class RejoinClient extends ClientBase {
    private static final int                           NUM    = 100;

    public static final Map<String, ToolkitAtomicLong> counts = new ConcurrentHashMap<String, ToolkitAtomicLong>();
    private final ToolkitBarrier                       barrier;
    private final Toolkit                              toolkit;

    public RejoinClient(String[] args) {
      super(args);

      toolkit = getClusteringToolkit();
      barrier = toolkit.getBarrier("barrier", 2);
    }

    @Override
    protected boolean isStartingScheduler() {
      return false;
    }

    
    @Override
    public void addSchedulerProperties(Properties properties) {
      super.addSchedulerProperties(properties);
      properties.setProperty("org.quartz.jobStore.synchronousWrite", "true");
      properties.setProperty("org.quartz.jobStore.rejoin", "true");
      properties.setProperty("org.quartz.threadPool.threadCount", "1");
    }
    
    @Override
    public Properties getToolkitProps() {
      Properties props = super.getToolkitProps();
      props.setProperty("rejoin", "true");
      return props;
    }

    @Override
    protected void test(Scheduler scheduler) throws Throwable {
      final int ITERATIONS = 5;

      int index = barrier.await();

      for (int cnt = 0; cnt < NUM; cnt++) {
        String jobName = "myJob" + cnt;
        counts.put(jobName, toolkit.getAtomicLong(jobName));
        if (index == 0) {
          System.out.println("Scheduling Job: " + "myJob" + cnt);
          JobDetail jobDetail = JobBuilder.newJob(RejoinClient.TestJob.class).withIdentity(jobName, "myJobGroup").build();

          Trigger trigger = TriggerBuilder
              .newTrigger()
              .withIdentity("triggerName" + cnt, "triggerGroup")
              .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5)
                                .withRepeatCount(ITERATIONS - 1)).build();

          scheduler.scheduleJob(jobDetail, trigger);
        }
      }

      barrier.await();

      scheduler.start();

      Thread.sleep(10000);
      
      if (index == 0) {
        getTestControlMbean().crashActiveServer(0);
        int doneCount = 0;
        while (doneCount != NUM) {
          try {
            doneCount = 0;
            ThreadUtil.reallySleep(1000L);

            for (Map.Entry<String, ToolkitAtomicLong> entry : counts.entrySet()) {
              if (entry.getValue().longValue() >= ITERATIONS) {
                doneCount++;
              }
              // System.err.println("Entries --" + entry.getKey() + " " + entry.getValue().longValue());
            }

            System.err.println("doneCount: " + doneCount);
          } catch (RejoinException _) {
            //ignore
          }
        }
      }

      while (true) {
        try {
          barrier.await();
          break;
        } catch (RejoinException e) {
          while (true) {
            try {
              barrier.reset();
              break;
            } catch (RejoinException f) {
              //nothing to do
            }
          }
        }
      }

      scheduler.shutdown(true);
    }

    @PersistJobDataAfterExecution
    public static class TestJob implements Job {

      public void execute(JobExecutionContext context) {
        String name = context.getJobDetail().getKey().getName();
        long val = incrementAndGet(RejoinClient.counts, name);
        System.out.println("Called:" + name + ": " + val);
      }

      long incrementAndGet(Map<String, ToolkitAtomicLong> map, String key) {
        ToolkitAtomicLong current = map.get(key);
        return current.incrementAndGet();
      }
    }
  }

}
