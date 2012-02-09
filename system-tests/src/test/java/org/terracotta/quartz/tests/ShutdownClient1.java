package org.terracotta.quartz.tests;

import org.junit.Assert;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.terracotta.coordination.Barrier;
import org.terracotta.quartz.AbstractTerracottaJobStore;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class ShutdownClient1 extends ClientBase {
  private static final List<WeakReference<ClassLoader>> CLASS_LOADER_LIST = new ArrayList<WeakReference<ClassLoader>>();

  public static final CyclicBarrier                     localBarrier      = new CyclicBarrier(2);

  private Scheduler                                     myScheduler;

  public ShutdownClient1(String[] args) {
    super(args);
  }

  public static void main(String[] args) throws Throwable {
    ShutdownClient1 client = new ShutdownClient1(args);
    client.doTest();
  }

  @Override
  public void doTest() throws Throwable {
    Set<SimpleThreadInfo> baseLineThreads = SimpleThreadInfo.parseThreadInfo(getThreadDump());

    for (int i = 0; i < 5; i++) {
      System.out.println("***** Iteration " + (i + 1) + " *****");
      myScheduler = setupScheduler();
      if (i == 0) test(myScheduler);

      storeL1ClassLoaderWeakReferences(myScheduler);

      shutdownExpressClient();
      Thread.sleep(TimeUnit.SECONDS.toMillis(30));

      clearTerracottaClient();
    }

    Set<SimpleThreadInfo> afterShutdownThreads = SimpleThreadInfo.parseThreadInfo(getThreadDump());

    afterShutdownThreads.removeAll(baseLineThreads);
    System.out.println("******** Threads Diff: ");
    printThreads(afterShutdownThreads);

    assertThreadShutdown(afterShutdownThreads);

    // let's trigger 10 full GC's to make sure the L1Loader got collected
    for (int i = 0; i < 10; i++) {
      new PermStress().stress(100000);
    }

    assertClassloadersGCed();

    pass();
  }

  // if only a single L1 loader got GC'ed, we can consider the test passed
  private void assertClassloadersGCed() {
    boolean failed = true;
    StringBuilder sb = new StringBuilder();
    for (WeakReference<ClassLoader> wr : CLASS_LOADER_LIST) {
      ClassLoader cl = wr.get();
      if (cl != null) {
        sb.append(cl).append(", ");
      } else {
        failed = false;
      }
    }
    if (failed) {
      sb.deleteCharAt(sb.length() - 1);
      sb.deleteCharAt(sb.length() - 1);
      dumpHeap(ShutdownClient1.class.getSimpleName());
      throw new AssertionError("Classloader(s) " + sb + " not GC'ed");
    }
  }

  public static void printThreads(Set<SimpleThreadInfo> threads) {
    for (SimpleThreadInfo ti : threads) {
      System.out.println(ti);
    }
  }

  public void shutdownExpressClient() throws SchedulerException {
    myScheduler.shutdown();
    myScheduler = null;
    getTerracottaClient().shutdown();
    clearTerracottaClient();
  }

  @Override
  protected void test(Scheduler scheduler) throws Throwable {
    Barrier barrier = getClusteringToolkit().getBarrier("shutdownBarrier", 2);

    JobDetailImpl jobDetail = new JobDetailImpl("testjob", null, SimpleJob.class);
    jobDetail.setDurability(true);

    SimpleTriggerImpl trigger = new SimpleTriggerImpl("trigger1", "group");
    trigger.setRepeatInterval(30000);
    trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
    trigger.setJobName("testjob");
    trigger.setCalendarName("mycal");

    // This calendar doesn't do anything really, just testing that calendars work
    scheduler.addCalendar("mycal", new BaseCalendar(), false, true);
    scheduler.addJob(jobDetail, false);
    scheduler.scheduleJob(trigger);

    SimpleJob.localBarrier.await();
    System.out.println("Done runng testjob, waiting for client2 to assert");
    barrier.await();
  }

  private void assertThreadShutdown(Set<SimpleThreadInfo> dump) throws Exception {
    filterKnownThreads(dump);
    if (dump.size() > 0) { throw new AssertionError("Threads still running: " + dump); }
  }

  private static String getThreadDump() {
    final String newline = System.getProperty("line.separator", "\n");
    StringBuffer rv = new StringBuffer();
    ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
    for (long id : tbean.getAllThreadIds()) {
      ThreadInfo tinfo = tbean.getThreadInfo(id, Integer.MAX_VALUE);
      rv.append("Thread name: " + tinfo.getThreadName()).append("-" + id).append(newline);
      for (StackTraceElement e : tinfo.getStackTrace()) {
        rv.append("    at " + e).append(newline);
      }
      rv.append(newline);
    }
    return rv.toString();
  }

  private Set<SimpleThreadInfo> filterKnownThreads(Set<SimpleThreadInfo> dump) {
    List<ThreadIgnore> ignores = Arrays.asList(new ThreadIgnore("http-", "org.apache.tomcat."),
                                               new ThreadIgnore("Attach Listener-", ""),
                                               new ThreadIgnore("Poller SunPKCS11", "sun.security.pkcs11."),
                                               new ThreadIgnore("(Attach Listener)-", ""),
                                               new ThreadIgnore("JFR request timer-", ""),
                                               new ThreadIgnore("JMAPI event thread-", ""));

    for (Iterator<SimpleThreadInfo> it = dump.iterator(); it.hasNext();) {
      SimpleThreadInfo threadInfo = it.next();
      for (ThreadIgnore ignore : ignores) {
        if (ignore.canIgnore(threadInfo)) {
          it.remove();
        }
      }
    }
    return dump;
  }

  private void storeL1ClassLoaderWeakReferences(Scheduler scheduler) throws Exception {
    ClassLoader clusteredStateLoader = getRealJobStore(scheduler).getClass().getClassLoader();

    System.out.println("XXX: clusteredStateLoader: " + clusteredStateLoader);
    Assert.assertNotNull(clusteredStateLoader);

    CLASS_LOADER_LIST.add(new WeakReference<ClassLoader>(clusteredStateLoader));
  }

  private Object getRealJobStore(Scheduler scheduler) throws Exception {
    Object sched = getPrivateFieldValue(scheduler, "sched");
    Object resources = getPrivateFieldValue(sched, "resources");
    Object jobStore = getPrivateFieldValue(resources, "jobStore");
    Object realJobStore = getPrivateFieldValue(AbstractTerracottaJobStore.class, jobStore, "realJobStore");
    return realJobStore;
  }

  private Object getPrivateFieldValue(Object target, String fieldName) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }

  private Object getPrivateFieldValue(Class clazz, Object target, String fieldName) throws Exception {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }

  private static void dumpHeap(String dumpName) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      String hotSpotDiagName = "com.sun.management:type=HotSpotDiagnostic";
      ObjectName name = new ObjectName(hotSpotDiagName);
      String operationName = "dumpHeap";

      new File("heapDumps").mkdirs();
      File tempFile = new File("heapDumps/" + dumpName + "_" + (System.currentTimeMillis()) + ".hprof");
      tempFile.delete();
      String dumpFilename = tempFile.getAbsolutePath();

      Object[] params = new Object[] { dumpFilename, Boolean.TRUE };
      String[] signature = new String[] { String.class.getName(), boolean.class.getName() };
      mbs.invoke(name, operationName, params, signature);

      System.out.println("dumped heap in file " + dumpFilename);
    } catch (Exception e) {
      // ignore
    }
  }

}
