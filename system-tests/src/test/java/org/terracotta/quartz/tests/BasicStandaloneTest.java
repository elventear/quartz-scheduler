/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class BasicStandaloneTest extends AbstractStandaloneTest {
  public BasicStandaloneTest(TestConfig testConfig) {
    super(testConfig, BasicClient1.class, BasicClient2.class);
    testConfig.getClientConfig().setParallelClients(false);
  }
}
