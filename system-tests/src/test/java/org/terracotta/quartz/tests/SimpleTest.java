/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class SimpleTest extends AbstractStandaloneTest {
  public SimpleTest(TestConfig testConfig) {
    super(testConfig, SimpleClient.class, SimpleClient.class);
    testConfig.getClientConfig().setParallelClients(true);
  }
}
