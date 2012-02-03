/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

import org.quartz.spi.JobStore;

import com.tc.test.TCTestCase;
import com.tc.text.Banner;

public class JobStoreInterfaceCheckerTest extends TCTestCase {

  public void testForNewMethod() {
    Class jobStoreInterface = JobStore.class;

    try {
      jobStoreInterface.getMethod("getEstimatedTimeToReleaseAndAcquireTrigger", Long.TYPE);
      throw new AssertionError(
                               "TerracottaJobStoreExtensions.getEstimatedTimeToReleaseAndAcquireTrigger() can now be removed and then this test case deleted");
    } catch (NoSuchMethodException e) {
      Banner.warnBanner("Method does not yet exist -- it should someday!");
      e.printStackTrace();
      return;
    }
  }
}
