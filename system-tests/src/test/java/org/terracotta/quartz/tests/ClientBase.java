/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.terracotta.api.ClusteringToolkit;
import org.terracotta.api.TerracottaClient;
import org.terracotta.quartz.AbstractTerracottaJobStore;
import org.terracotta.quartz.TerracottaJobStore;
import org.terracotta.tests.base.AbstractClientBase;

import java.io.IOException;
import java.util.Properties;

public abstract class ClientBase extends AbstractClientBase {

  protected TerracottaClient terracottaClient;
  private final Properties   props = new Properties();

  public ClientBase(String args[]) {
    super(args);
  }

  public final void run() {
    Scheduler scheduler = null;
    try {
      scheduler = setupScheduler();
      test(scheduler);
      pass();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    } finally {
      if (scheduler != null) {
        try {
          scheduler.shutdown();
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  public void addSchedulerProperties(Properties properties) {
    // to be overridden
  }

  public Properties getSchedulerProps() {
    return props;
  }

  protected Scheduler setupScheduler() throws IOException, SchedulerException {
    props.load(getClass().getResourceAsStream("/org/quartz/quartz.properties"));
    props.setProperty(StdSchedulerFactory.PROP_JOB_STORE_CLASS, TerracottaJobStore.class.getName());
    props.setProperty(AbstractTerracottaJobStore.TC_CONFIGURL_PROP, getTerracottaUrl());
    props.setProperty("org.quartz.jobStore.synchronousWrite", String.valueOf(isSynchWrite()));
    props.setProperty("org.quartz.jobStore.estimatedTimeToReleaseAndAcquireTrigger", "10");
    props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, StdSchedulerFactory.AUTO_GENERATE_INSTANCE_ID);

    addSchedulerProperties(props);

    SchedulerFactory schedFact = new StdSchedulerFactory(props);
    Scheduler sched = schedFact.getScheduler();
    sched.start();

    return sched;
  }

  protected boolean isSynchWrite() {
    return false;
  }

  protected abstract void test(Scheduler scheduler) throws Throwable;

  @Override
  protected void pass() {
    System.err.println("[PASS: " + getClass().getName() + "]");
  }

  protected ClusteringToolkit getClusteringToolkit() {
    return getTerracottaClient().getToolkit();
  }

  public synchronized void clearTerracottaClient() {
    terracottaClient = null;
  }

  protected synchronized TerracottaClient getTerracottaClient() {
    if (terracottaClient == null) {
      terracottaClient = new TerracottaClient(getTerracottaUrl());
    }
    return terracottaClient;
  }
}
