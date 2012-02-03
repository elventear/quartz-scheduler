/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class FastTriggerTest extends AbstractStandaloneTest {
  public FastTriggerTest(TestConfig testConfig) {
    super(testConfig, FastTriggerClient.class, FastTriggerClient.class);
    testConfig.getClientConfig().setParallelClients(true);
  }
}
