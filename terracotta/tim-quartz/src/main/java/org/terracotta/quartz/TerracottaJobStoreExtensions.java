/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz;

import org.quartz.JobListener;
import org.quartz.spi.JobStore;

/**
 * This interface defines convenience methods on the terracotta job store implementation Without this interface we would
 * need to use reflection to invoke these "extra" methods (ie. not present on core JobStore) from the express context
 */
public interface TerracottaJobStoreExtensions extends JobStore, JobListener {

  public void setMisfireThreshold(long threshold);

  public void setEstimatedTimeToReleaseAndAcquireTrigger(long estimate);

  public void setSynchronousWrite(String synchWrite);

  public void setThreadPoolSize(int size);

  public String getUUID();
}
