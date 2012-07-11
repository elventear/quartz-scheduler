/* 
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */
package org.terracotta.quartz.tests;

import org.junit.Assert;
import org.quartz.Scheduler;
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.cluster.ClusterInfo;
import org.terracotta.toolkit.concurrent.ToolkitBarrier;

public class ShutdownClient2 extends ClientBase {

  private ToolkitBarrier barrier;

  public ShutdownClient2(String[] args) {
    super(args);
  }

  public static void main(String[] args) throws Exception {
    new ShutdownClient2(args).doTest();
  }

  @Override
  public void doTest() throws Exception {
    Scheduler scheduler = null;
    try {
      scheduler = setupScheduler();
      test(scheduler);
      pass();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    } finally {
      if (scheduler != null) {
        try {
          scheduler.shutdown();
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
      System.exit(0);
    }
  }

  private int getConnectedClients() {
    Toolkit clustering = getClusteringToolkit();
    ClusterInfo clusterInfo = clustering.getClusterInfo();
    return clusterInfo.getClusterTopology().getNodes().size();
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    barrier = getClusteringToolkit().getBarrier("shutdownBarrier", 2);
    Assert.assertEquals(2, getConnectedClients());
    System.out.println("Connected clients: " + getConnectedClients());
    System.out.println("waiting for clustered job to setup...");
    barrier.await();
    System.out.println("asserting clustered job");
    SimpleJob.localBarrier.await();

    System.out.println("Waiting for client1 to shutdown...");
    Thread.sleep(30 * 1000L);

    Assert.assertEquals(1, getConnectedClients());
  }
}
