package org.terracotta.quartz.tests;

import org.junit.Assert;
import org.quartz.Scheduler;
import org.terracotta.api.ClusteringToolkit;
import org.terracotta.cluster.ClusterInfo;
import org.terracotta.coordination.Barrier;

public class ShutdownClient2 extends ClientBase {

  private Barrier barrier;

  public ShutdownClient2(String[] args) {
    super(args);
  }

  public static void main(String[] args) throws Exception {
    new ShutdownClient2(args).doTest();
  }

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
    ClusteringToolkit clustering = getTerracottaClient().getToolkit();
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