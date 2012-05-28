/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.terracotta.quartz.tests;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.impl.StdSchedulerFactory;
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
  protected String createClassPath(Class client) {
    String test = TestBaseUtil.jarFor(client);
    String standalone = TestBaseUtil.jarFor(TerracottaJobStore.class);
    String quartz = TestBaseUtil.jarFor(StdSchedulerFactory.class);
    String quartzJobs = TestBaseUtil.jarFor(MyJob.class);
    String expressRuntime = TestBaseUtil.jarFor(ClientFactory.class);
    String logging = TestBaseUtil.jarFor(org.slf4j.LoggerFactory.class);
    String binder = TestBaseUtil.jarFor(org.slf4j.impl.StaticLoggerBinder.class);
    String log4j = TestBaseUtil.jarFor(org.apache.log4j.Level.class);
    String junit = TestBaseUtil.jarFor(org.junit.Assert.class);

    return makeClasspath(test, standalone, quartz, quartzJobs, expressRuntime, logging, binder, log4j, junit);
  }
  
  /** An empty job for testing purpose. */
  public static class MyJob implements Job {
      public void execute(JobExecutionContext context) throws JobExecutionException {
          //
      }
  }
}
