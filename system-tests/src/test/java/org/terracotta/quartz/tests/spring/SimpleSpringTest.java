/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests.spring;

import org.springframework.context.ApplicationContext;
import org.terracotta.quartz.tests.AbstractStandaloneTest;
import org.terracotta.test.util.TestBaseUtil;

import com.tc.test.config.model.TestConfig;

import java.util.ArrayList;
import java.util.List;

public class SimpleSpringTest extends AbstractStandaloneTest {

  public SimpleSpringTest(TestConfig testConfig) {
    super(testConfig, SimpleSpringClient1.class, SimpleSpringClient2.class);
    testConfig.getClientConfig().setParallelClients(false);
    timebombTest("2013-01-15");
  }

  @Override
  protected List<String> getExtraJars() {
    List<String> rv = new ArrayList<String>();
    rv.add(TestBaseUtil.jarFor(ApplicationContext.class));
    rv.add(TestBaseUtil.jarFor(org.apache.commons.logging.LogFactory.class));
    return rv;
  }
}
