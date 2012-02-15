/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class StartStopStartTest extends AbstractStandaloneTest {
  public StartStopStartTest(TestConfig testConfig) {
    super(testConfig, StartStopStartClient.class);
  }
}
