/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class ManyTriggerTest extends AbstractStandaloneTest {

  public ManyTriggerTest(TestConfig testConfig) {
    super(testConfig, ManyTriggerClient.class, ManyTriggerClient.class);
    testConfig.getClientConfig().setParallelClients(true);
  }

}
