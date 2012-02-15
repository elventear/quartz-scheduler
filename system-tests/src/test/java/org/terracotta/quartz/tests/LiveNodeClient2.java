package org.terracotta.quartz.tests;

import org.quartz.Scheduler;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Date;

public class LiveNodeClient2 extends ClientBase {

  private final int node1Port;

  public LiveNodeClient2(String[] args) {
    super(args);
    this.node1Port = Integer.parseInt(System.getProperty("listenPort"));
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    JobDetailImpl jobDetail = new JobDetailImpl("futurejob", null, NullJob.class);
    jobDetail.setDurability(true);

    SimpleTriggerImpl trigger = new SimpleTriggerImpl("futuretrigger", "group");
    // run a week from now (ie. don't run)
    trigger.setStartTime(new Date(System.currentTimeMillis() + (1000L * 60L * 60L * 24L * 7L)));
    trigger.setJobName("futurejob");

    scheduler.addJob(jobDetail, false);
    scheduler.scheduleJob(trigger);

    // not strictly necessary but this should make the newly added trigger get ACQUIRED locally
    Thread.sleep(5000L);

    Socket s = null;
    for (int i = 0; i < 30; i++) {
      try {
        s = new Socket("127.0.0.1", node1Port);
      } catch (ConnectException ce) {
        Thread.sleep(1000L);
      }
    }

    if (s == null) { throw new AssertionError("no listening port found on " + node1Port); }

    InputStream in = s.getInputStream();

    final long end = System.currentTimeMillis() + 60000L;
    while (System.currentTimeMillis() < end) {
      log("Sleeping");
      Thread.sleep(1000L);

      try {
        if (in.read() < 0) {
          break;
        }
      } catch (IOException ioe) {
        break;
      }
    }

    if (System.currentTimeMillis() > end) { throw new AssertionError(); }

    SimpleJob.localBarrier.await();
  }

  private void log(String msg) {
    System.err.println("[" + new Date() + "] " + msg);
  }
}
