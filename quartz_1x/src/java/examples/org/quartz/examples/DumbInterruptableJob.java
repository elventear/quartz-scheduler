/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.examples;

import java.util.Date;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import com.adventnet.utils.Scheduler;

/**
 * <p>
 * A dumb implementation of an InterruptableJob, for unittesting purposes.
 * </p>
 * 
 * @author <a href="mailto:bonhamcm@thirdeyeconsulting.com">Chris Bonham</a>
 * @author James House
 */
public class DumbInterruptableJob implements InterruptableJob {
    
    private boolean interrupted = false;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public DumbInterruptableJob() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called by the <code>{@link org.quartz.Scheduler}</code> when a <code>{@link org.quartz.Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     * </p>
     * 
     * @throws JobExecutionException
     *           if there is an exception while executing the job.
     */
    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        System.err.println("--->" + context.getJobDetail().getFullName()
                + " executing.[" + new Date() + "]");

        try {
            
            // main job loop... see the JavaDOC for InterruptableJob for discussion...
            
            // do some work... in this example we are 'simulating' work by sleeping... :)
            try {
                Thread.sleep(1000L);
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
            
            // periodically check if we've been interrupted...
            if(interrupted) {
                System.err.println("--- " + context.getJobDetail().getFullName() + "  -- Interrupted... bailing out!");
                
                return; // could also choose to throw a JobExecutionException 
                         // if that made for sense based on the particular  
                         // job's responsibilities/behaviors
            }

            // do some further work... 
            try {
                Thread.sleep(1000L);
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
            
            // periodically check if we've been interrupted...
            if(interrupted) {
                System.err.println("--- " + context.getJobDetail().getFullName() + "  -- Interrupted... bailing out!");
                
                return; // could also choose to throw a JobExecutionException 
                         // if that made for sense based on the particular  
                         // job's responsibilities/behaviors
            }
            
            // do even more fun work... 
            try {
                Thread.sleep(1000L);
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
            

            // finish up the job...
            try {
                Thread.sleep(1000L);
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }

            // periodically check if we've been interrupted...
            if(interrupted) {
                System.err.println("--- " + context.getJobDetail().getFullName() + "  -- Interrupted... bailing out!");
                
                return; // could also choose to throw a JobExecutionException 
                         // if that made for sense based on the particular  
                         // job's responsibilities/behaviors
            }
            
            
        }
        finally {
            System.err.println("--- " + context.getJobDetail().getFullName()
                    + " complete.[" + new Date() + "]");
        }
    }
    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a user
     * interrupts the <code>Job</code>.
     * </p>
     * 
     * @return void (nothing) if job interrupt is successful.
     * @throws JobExecutionException
     *           if there is an exception while interrupting the job.
     */
    public void interrupt() throws UnableToInterruptJobException {
        System.err.println("---" + "  -- INTERRUPTING --");
        this.interrupted = true;
    }

}