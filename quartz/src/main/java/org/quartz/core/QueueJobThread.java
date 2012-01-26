
/*
 * Copyright 2012 Terracotta, Inc.
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

package org.quartz.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.quartz.JobPersistenceException;
import org.quartz.QueueJobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.spi.TriggerFiredResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread to process QueueJob from a job store.
 * 
 * @author Zemian Deng
 */
public class QueueJobThread extends Thread {
    private QuartzScheduler qs;

    private QuartzSchedulerResources qsRsrcs;

    private final Object sigLock = new Object();
    
    private boolean paused;

    private AtomicBoolean halted;

    private Random random = new Random(System.currentTimeMillis());

    // When the scheduler finds there is no current trigger to fire, how long
    // it should wait until checking again...
    private static long DEFAULT_IDLE_WAIT_TIME = 30L * 1000L;

    private long idleWaitTime = DEFAULT_IDLE_WAIT_TIME;

    private int idleWaitVariablness = 7 * 1000;

    private final static Logger logger = LoggerFactory.getLogger(QueueJobThread.class);
    
    /**
     * <p>
     * Construct a new <code>QueueJobThread</code> for the given
     * <code>QuartzScheduler</code> as a non-daemon <code>Thread</code>
     * with normal priority.
     * </p>
     */
    QueueJobThread(QuartzScheduler qs, QuartzSchedulerResources qsRsrcs) {
        this(qs, qsRsrcs, qsRsrcs.getMakeSchedulerThreadDaemon(), Thread.NORM_PRIORITY);
    }

    /**
     * <p>
     * Construct a new <code>QueueJobThread</code> for the given
     * <code>QuartzScheduler</code> as a <code>Thread</code> with the given
     * attributes.
     * </p>
     */
    QueueJobThread(QuartzScheduler qs, QuartzSchedulerResources qsRsrcs, boolean setDaemon, int threadPrio) {
        super(qs.getSchedulerThreadGroup(), qsRsrcs.getThreadName());
        this.qs = qs;
        this.qsRsrcs = qsRsrcs;
        this.setDaemon(setDaemon);
        if(qsRsrcs.isThreadsInheritInitializersClassLoadContext()) {
            logger.info("QueueJobThread Inheriting ContextClassLoader of thread: " + Thread.currentThread().getName());
            this.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        }

        this.setPriority(threadPrio);

        // start the underlying thread, but put this object into the 'paused'
        // state
        // so processing doesn't start yet...
        paused = true;
        halted = new AtomicBoolean(false);
    }

    void setIdleWaitTime(long waitTime) {
        idleWaitTime = waitTime;
        idleWaitVariablness = (int) (waitTime * 0.2);
    }

    private long getRandomizedIdleWaitTime() {
        return idleWaitTime - random.nextInt(idleWaitVariablness);
    }

    /**
     * <p>
     * Signals the main processing loop to pause at the next possible point.
     * </p>
     */
    void togglePause(boolean pause) {
        synchronized (sigLock) {
            paused = pause;

            if (!paused) {
                sigLock.notifyAll();
            }
        }
    }

    /**
     * <p>
     * Signals the main processing loop to pause at the next possible point.
     * </p>
     */
    void halt() {
        synchronized (sigLock) {
            halted.set(true);

            if (paused) {
                sigLock.notifyAll();
            }
        }
    }

    boolean isPaused() {
        return paused;
    }

    private void pauseThreadIfSet() {
    	// check if we're supposed to pause...
        synchronized (sigLock) {
            while (paused && !halted.get()) {
                try {
                    // wait until togglePause(false) is called...
                    sigLock.wait(1000L);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }
    
    private void pauseBetweenRunLoop() {
        long now = System.currentTimeMillis();
        long waitTime = now + getRandomizedIdleWaitTime();
        long timeUntilContinue = waitTime - now;
        synchronized(sigLock) {
            try {
            	if(!halted.get())
            		sigLock.wait(timeUntilContinue);
            } catch (InterruptedException ignore) {
            }
        }
    }
    
    private void processQueueJobs() {
    	try {
            List<QueueJobDetail> jobs = qsRsrcs.getJobStore().getQueueJobDetails();
            if (logger.isDebugEnabled()) 
                logger.debug("Processing queue jobs: " + jobs.size());
        } catch (JobPersistenceException jpe) {
            logger.error("Problem processing queue jobs with data store problem.", jpe);
        } catch (RuntimeException e) {
            logger.error("Problem processing queue jobs.", e);
        }
    }
    
    /**
     * <p>
     * The main processing loop of the <code>QueueJobThread</code>.
     * </p>
     */
    @Override
    public void run() {
        while (!halted.get()) {
            try {
                pauseThreadIfSet();
                if (halted.get())
                    break;
                
                processQueueJobs();
                
                // We slow down between run so not not thrashing the system.
                pauseBetweenRunLoop();
            } catch(RuntimeException re) {
                logger.error("Runtime error occurred in main scheduler thread run loop.", re);
            }
        } // while (!halted)

        // drop references to scheduler stuff to aid garbage collection...
        qs = null;
        qsRsrcs = null;
    }
}
