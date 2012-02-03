package org.terracotta.quartz.tests;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.terracotta.collections.ClusteredMap;
import org.terracotta.coordination.Barrier;

import com.tc.util.concurrent.ThreadUtil;

public class FastTriggerClient extends ClientBase {

  public static ClusteredMap<String, Long> counts;
  private final Barrier                    barrier;

  public FastTriggerClient(String[] args) {
    super(args);
    counts = getTerracottaClient().getToolkit().getMap("counts");
    barrier = getTerracottaClient().getToolkit().getBarrier("barrier", 2);
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    final int NUM = 500;
    final int ITERATIONS = 25;

    int index = barrier.await();

    if (index == 0) {
      for (int cnt = 0; cnt < NUM; cnt++) {
        System.out.println("Scheduling Job: " + "myJob" + cnt);
        String jobName = "myJob" + cnt;
        counts.put(jobName, 0L);

        JobDetail jobDetail = JobBuilder.newJob(TestJob.class).withIdentity(jobName, "myJobGroup").build();

        Trigger trigger = TriggerBuilder
            .newTrigger()
            .withIdentity("triggerName" + cnt, "triggerGroup")
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1)
                              .withRepeatCount(ITERATIONS - 1)).build();

        scheduler.scheduleJob(jobDetail, trigger);
      }
    }

    barrier.await();

    scheduler.start();

    if (index == 0) {
      int doneCount = 0;
      while (doneCount != NUM) {
        doneCount = 0;
        ThreadUtil.reallySleep(1000L);

        for (Long count : counts.values()) {
          if (count.intValue() == ITERATIONS) {
            doneCount++;
          }
        }

        System.err.println("doneCount: " + doneCount);
      }
    }

    barrier.await();

    scheduler.shutdown();
  }

  public static class TestJob implements Job {

    public void execute(JobExecutionContext context) {
      String name = context.getJobDetail().getKey().getName();

      long val = incrementAndGet(FastTriggerClient.counts, name);
      if ((val % 5) == 0) {
        System.err.println(name + ": " + val);
      }
    }

    long incrementAndGet(ClusteredMap<String, Long> map, String key) {
      for (;;) {
        long current = map.get(key);
        long next = current + 1;
        if (map.replace(key, current, next)) { return next; }
      }
    }
  }
}
