/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * 
 * @author jhouse
 */
public class DumbJobListener implements JobListener {

        String name;

        public DumbJobListener() {
        }

        public DumbJobListener(String name) {
            setName(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /** 
         * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
         */
        public void jobToBeExecuted(JobExecutionContext context) {
            System.err.println("Listener " + name + " says: \"Job '"+context.getJobDetail().getFullName()+"' will execute.\"");
        }

        /** 
         * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
         */
        public void jobExecutionVetoed(JobExecutionContext context) {
             // TODO Auto-generated method stub
        }

        /** 
         * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)
         */
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            System.err.println("Listener " + name + " says: \"Job '"+context.getJobDetail().getFullName()+"' executed.\"");
        }

}
