/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/**
 * 
 * @author jhouse
 */
public class DumbTriggerListener implements TriggerListener {

        String name;

        public DumbTriggerListener() {
        }

        public DumbTriggerListener(String name) {
            setName(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /** 
         * @see org.quartz.TriggerListener#triggerFired(org.quartz.Trigger, org.quartz.JobExecutionContext)
         */
        public void triggerFired(Trigger trigger, JobExecutionContext context) {
             System.err.println("Listener " + name + " says: \"Trigger '"+trigger.getFullName()+"' Fired!\"");
        }

        /** 
         * @see org.quartz.TriggerListener#vetoJobExecution(org.quartz.Trigger, org.quartz.JobExecutionContext)
         */
        public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
            return false; 
        }

        /** 
         * @see org.quartz.TriggerListener#triggerMisfired(org.quartz.Trigger)
         */
        public void triggerMisfired(Trigger trigger) {
        }

        /** 
         * @see org.quartz.TriggerListener#triggerComplete(org.quartz.Trigger, org.quartz.JobExecutionContext, int)
         */
        public void triggerComplete(Trigger trigger, JobExecutionContext context, int triggerInstructionCode) {
        }

}
