/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples;

import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @author James House
 */
public class InitJobsTest {

    public static void schedTest(SchedulerFactory sf) throws Exception {
        Log lg = LogFactory.getLog(InitJobsTest.class);

        lg.info("------- Initializing -------------------");

        Scheduler sched = sf.getScheduler();

        lg.info("------- Initialization Complete -----------");

        lg
                .info("------- (Not Scheduling any Jobs - relying on XML definitions --");

        lg.info("------- Starting Scheduler ----------------");

        // jobs don't start firing until start() has been called...

        sched.start();

        lg.info("------- Started Scheduler -----------------");

        lg.info("------- Waiting... -----------------------");

        try {
            Thread.sleep(300L * 1000L); // wait five minutes to show jobs
                                        // executing...
        } catch (Exception e) {
        }

        lg.info("------- Shutting Down ---------------------");

        sched.shutdown(true);

        lg.info("------- Shutdown Complete -----------------");

        SchedulerMetaData metaData = sched.getMetaData();
        lg.info("Executed " + metaData.numJobsExecuted() + " jobs.");

    }

    public static void main(String[] args) throws Exception {

        // Configure Log4J
        // org.apache.log4j.PropertyConfigurator.configure(System.getProperty("log4jConfigFile",
        // "log4j.properties"));

        try {
            schedTest(new org.quartz.impl.StdSchedulerFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}