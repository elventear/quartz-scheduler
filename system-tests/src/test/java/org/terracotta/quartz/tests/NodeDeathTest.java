/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;
import com.tc.util.PortChooser;

/**
 * This test ensures that a live node can recover acquired triggers (as opposed to doing recovery just at startup of new
 * nodes)
 */
public class NodeDeathTest extends AbstractStandaloneTest {

  public NodeDeathTest(TestConfig testConfig) {
    super(testConfig, NodeDeathTestClient1.class, NodeDeathTestClient2.class);
    testConfig.getClientConfig().addExtraClientJvmArg("-DlistenPort=" + new PortChooser().chooseRandomPort());
  }
}
