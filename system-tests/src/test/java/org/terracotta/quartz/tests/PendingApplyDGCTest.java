/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class PendingApplyDGCTest extends AbstractStandaloneTest {

  public PendingApplyDGCTest(TestConfig testConfig) {
    super(testConfig, PendingApplyDGCClient.class, PendingApplyDGCClient.class);

    testConfig.getClientConfig().setParallelClients(true);

    testConfig.getL2Config().setPersistenceMode(com.tc.test.config.model.PersistenceMode.TEMPORARY_SWAP_ONLY);
    testConfig.getL2Config().setDgcEnabled(true);
    testConfig.getL2Config().setDgcIntervalInSec(PendingApplyDGCClient.DGC_SECONDS);
  }
}
