/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class SimpleOrderingTest extends AbstractStandaloneTest {
  public SimpleOrderingTest(TestConfig testConfig) {
    super(testConfig, SimpleOrderingClient.class, SimpleOrderingClient.class, SimpleOrderingClient.class,
          SimpleOrderingClient.class, SimpleOrderingClient.class);
    testConfig.getClientConfig().setParallelClients(true);
  }
}
