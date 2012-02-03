/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class RecoveryTest extends AbstractStandaloneTest {

  public RecoveryTest(TestConfig testConfig) {
    super(testConfig, Client1.class, Client2.class);
    testConfig.getClientConfig().setParallelClients(false);
  }
}
