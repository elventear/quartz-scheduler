package org.quartz;

import java.util.Properties;

import org.quartz.impl.StdSchedulerFactory;

public class RAMSchedulerTest extends AbstractSchedulerTest {

    @Override
    protected Scheduler createScheduler(String name, int threadPoolSize) throws SchedulerException {
        Properties config = new Properties();
        config.setProperty("org.quartz.scheduler.instanceName", name + "Scheduler");
        config.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        config.setProperty("org.quartz.threadPool.threadCount", Integer.toString(threadPoolSize));
        config.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        return new StdSchedulerFactory(config).getScheduler();
    }
}
