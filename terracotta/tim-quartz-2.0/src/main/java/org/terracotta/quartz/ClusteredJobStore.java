/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz;

import org.quartz.spi.JobStore;
import org.terracotta.cluster.ClusterListener;

/**
 * @author Alex Snaps
 */
public interface ClusteredJobStore extends JobStore, ClusterListener {
  void setMisfireThreshold(long misfireThreshold);

  void setEstimatedTimeToReleaseAndAcquireTrigger(long estimate);
}
