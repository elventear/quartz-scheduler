/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests.spring;

import org.quartz.Scheduler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.terracotta.quartz.tests.ClientBase;

public abstract class SpringClientBase extends ClientBase {

  private final String         contextFile;
  protected ApplicationContext context;

  public SpringClientBase(String contextFile, String[] args) {
    super(args);
    this.contextFile = contextFile;
  }

  @Override
  protected final Scheduler setupScheduler() {
    // Set this to get it substituted in the context file
    System.setProperty("tcConfigUrl", getTerracottaUrl());

    // using an application context so that it will be set in the JobDetailBean instances
    context = new ClassPathXmlApplicationContext(contextFile, getClass());

    return (Scheduler) context.getBean("scheduler");

  }

  @Override
  protected final void test(Scheduler scheduler) throws Throwable {

    try {
      test0(scheduler);
    } finally {
      if (context.containsBean("taskExecutor")) {
        DisposableBean exec = (DisposableBean) context.getBean("taskExecutor");
        exec.destroy();
      }
    }
  }

  protected abstract void test0(Scheduler scheduler) throws Throwable;

}
