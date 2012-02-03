package org.terracotta.quartz.tests;

import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Properties;
import java.util.concurrent.CyclicBarrier;

public class StartStopStartClient extends ClientBase {
  public static final CyclicBarrier localBarrier = new CyclicBarrier(2);

  public StartStopStartClient(String[] args) {
    super(args);
  }

  @Override
  protected void test(Scheduler sched) throws Throwable {
    Properties props = getSchedulerProps();
    props.setProperty(StdSchedulerFactory.PROP_SCHED_IDLE_WAIT_TIME, "1000");

    sched.start();
    sched.shutdown();

    SchedulerFactory schedFact = new StdSchedulerFactory(props);
    sched = schedFact.getScheduler();
    sched.start();
    sched.shutdown();
  }

}
