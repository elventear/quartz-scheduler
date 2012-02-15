/*
 * All content copyright (c) 2003-2009 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package org.terracotta.quartz.tests;

import com.tc.test.config.model.TestConfig;

public class ShutdownHookTest extends AbstractStandaloneTest {
  public ShutdownHookTest(TestConfig testConfig) {
    super(testConfig, ShutdownHookClient.class);
  }

}
