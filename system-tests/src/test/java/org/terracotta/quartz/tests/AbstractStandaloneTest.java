/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.quartz.tests;

import org.quartz.impl.StdSchedulerFactory;
import org.quartz.jobs.NoOpJob;
import org.terracotta.express.ClientFactory;
import org.terracotta.quartz.TerracottaJobStore;
import org.terracotta.test.util.TestBaseUtil;
import org.terracotta.tests.base.AbstractClientBase;
import org.terracotta.tests.base.AbstractTestBase;

import com.tc.test.config.model.TestConfig;

public abstract class AbstractStandaloneTest extends AbstractTestBase {

  protected AbstractStandaloneTest(TestConfig testConfig, Class<? extends AbstractClientBase>... c) {
    super(testConfig);
    testConfig.getClientConfig().setClientClasses(c);
  }

  @Override
  protected String createClassPath(Class client, boolean withStandaloneJar) {
    String test = TestBaseUtil.jarFor(client);
    String standalone = TestBaseUtil.jarFor(TerracottaJobStore.class);
    String quartz = TestBaseUtil.jarFor(StdSchedulerFactory.class);
    String quartzJobs = TestBaseUtil.jarFor(NoOpJob.class);
    String expressRuntime = TestBaseUtil.jarFor(ClientFactory.class);
    String logging = TestBaseUtil.jarFor(org.slf4j.LoggerFactory.class);
    String binder = TestBaseUtil.jarFor(org.slf4j.impl.StaticLoggerBinder.class);
    String log4j = TestBaseUtil.jarFor(org.apache.log4j.Level.class);
    String junit = TestBaseUtil.jarFor(org.junit.Assert.class);

    return makeClasspath(test, standalone, quartz, quartzJobs, expressRuntime, logging, binder, log4j, junit);
  }
}
