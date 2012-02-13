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

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.quartz.Calendar;
import org.quartz.JobPersistenceException;
import org.quartz.QueueJobDetail;
import org.quartz.SchedulerException;
import org.quartz.impl.triggers.QueueJobTrigger;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.ThreadPool;
import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread to process Job's from a queue store. This will poll the queue store (eg: QRTZ_QUEUE_JOB_DETAIL table), execute the job, and then
 * remove it from the store.
 * 
 * @author Zemian Deng
 */
public class QueueJobThread extends Thread {

    private final static Logger logger = LoggerFactory.getLogger(QueueJobThread.class);
    
    // When the scheduler finds there is no current trigger to fire, how long it should wait until checking again...
    private static long DEFAULT_IDLE_WAIT_TIME = 30L * 1000L;

    private QuartzScheduler qs;

    private QuartzSchedulerResources qsRsrcs;
    
    private AtomicBoolean paused;

    private AtomicBoolean halted;

    private Random random = new Random(System.currentTimeMillis());

    private long idleWaitTime = DEFAULT_IDLE_WAIT_TIME;

    private int idleWaitMaxVariance = 7 * 1000;
    
    /**
     * Constructor make this thread daemon and use normal priority.
     */
    public QueueJobThread(QuartzScheduler qs, QuartzSchedulerResources qsRsrcs) {
        this(qs, qsRsrcs, qsRsrcs.getMakeSchedulerThreadDaemon(), Thread.NORM_PRIORITY);
    }

    /**
     * Create this thread with scheduler resources such as store store and polling interval etc.
     */
    public QueueJobThread(QuartzScheduler qs, QuartzSchedulerResources qsRsrcs, boolean setDaemon, int threadPrio) {
        super(qs.getSchedulerThreadGroup(), "QuartzQueueJobThread");
        this.qs = qs;
        this.qsRsrcs = qsRsrcs;
        this.setDaemon(setDaemon);
        if(qsRsrcs.isThreadsInheritInitializersClassLoadContext()) {
            logger.info("QueueJobThread Inheriting ContextClassLoader of thread: " + Thread.currentThread().getName());
            this.setContextClassLoader(Thread.currentThread().getContextClassLoader());
        }

        this.setPriority(threadPrio);

        // Initialize fields so to put this thread into the 'paused' state so processing doesn't start until scheduler toogle it.
        paused = new AtomicBoolean(true);;
        halted = new AtomicBoolean(false);
    }

    void setIdleWaitTime(long waitTime) {
        idleWaitTime = waitTime;
        idleWaitMaxVariance = (int) (waitTime * 0.2);
    }

    private long getRandomizedIdleWaitTime() {
        return idleWaitTime - random.nextInt(idleWaitMaxVariance);
    }

    /**
     * Pause the run() execution of this thread that process the jobs from the queue.
     */
    void togglePause(boolean pause) {
        paused.set(pause);

        if (!paused.get()) {
            synchronized (this) {
                notify();
            }
        }
    }

    /**
     * Halt and signal this thread to end the run() execution.
     */
    void halt() {
        halted.set(true);
        if (paused.get()) {
            synchronized (this) {
            	notify();
            }
        }
    }

    boolean isPaused() {
        return paused.get();
    }

    /** 
     * Place this thread in wait/pause state if the {@link #paused} field has toggled to 'true'. It can be unpause by
     * toggle it with 'false' or calling halt() to end the thread. 
     */
    private void pauseThreadIfSet() {
    	while (paused.get() && !halted.get()) {
            try {
                // Wait until togglePause(false) or halt() is called.
                synchronized (this) {
                	wait(1000L);
                }
            } catch (InterruptedException ignore) {
            	// Ignore exception and continue to wait again.
            }
        }
    }
    
    /**
     * Pause the thread briefly before the next cycle of run() execution loop starts over again (polling).
     */
    private void pauseBetweenRunLoop() {
        long now = System.currentTimeMillis();
        long waitTime = now + getRandomizedIdleWaitTime();
        long timeUntilContinue = waitTime - now;
        try {
        	if(!halted.get()) {
                synchronized (this) {
                	logger.debug("Will wait for {} ms before polling queued jobs again.", timeUntilContinue);
                	wait(timeUntilContinue);
                }
        	}
        } catch (InterruptedException ignore) {
        }
    }
        
    /**
     * The main execution of this thread, which will run #processQueueJobs() in a loop until {@link #halt()} is called. User may
     * pause the execution by calling {@link #togglePause(boolean)}. 
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

        // We are done, let's drop references to scheduler stuff to aid garbage collection.
        qs = null;
        qsRsrcs = null;
    }
    
    /**
     * Process jobs from a stored queue. This is the main work of this thread per each execution cycle.
     */
    private void processQueueJobs() {
    	try {
            List<QueueJobDetail> jobs = qsRsrcs.getJobStore().getQueueJobDetails();
            if (logger.isDebugEnabled()) logger.debug("Processing {} jobs from queue.", jobs.size());
            for (QueueJobDetail job : jobs) {
                if (logger.isDebugEnabled()) logger.debug("Processing job: {}", job);
                TriggerFiredBundle bundle = createTriggerFiredBundle(job);
                runQueueJobInThreadPool(bundle);
                if (logger.isDebugEnabled()) logger.debug("Queue job {} has been processed.", job);
            }
        } catch (JobPersistenceException jpe) {
            logger.error("Problem processing QueueJob's with data store.", jpe);
        } catch (SchedulerException se) {
            logger.error("Problem processing QueueJob", se);
		} catch (RuntimeException e) {
            logger.error("Problem processing QueueJob's.", e);
		}        
    }
    
	private void runQueueJobInThreadPool(TriggerFiredBundle bundle) throws SchedulerException {
        if (logger.isDebugEnabled()) logger.debug("Executing queue job as trigger bundle: {} in a thread pool.", bundle);
        JobRunShellFactory jobShellFactory = qsRsrcs.getJobRunShellFactory(); 
        if (logger.isDebugEnabled()) logger.debug("Using jobShellFactory {}", jobShellFactory);
        JobRunShell jobRunShell = jobShellFactory.createJobRunShell(bundle);
        jobRunShell.initialize(qs);
        ThreadPool threadPool = qsRsrcs.getQueueJobThreadPool();
        boolean added = threadPool.runInThread(jobRunShell);
        if (!added) {
        	throw new SchedulerException("Not able to add queue job into thread pool.");
        }
        logger.info("Queue job {} has been added to thread pool.", bundle.getJobDetail().getKey());
	}

	private TriggerFiredBundle createTriggerFiredBundle(QueueJobDetail job) {
        if (logger.isDebugEnabled()) logger.debug("Creating trigger bundle for queue job: {}", job);
        OperableTrigger trigger = createQueueJobTrigger();
        Calendar cal = null;
        boolean jobIsRecovering = false;
        Date fireTime = new Date();
        Date scheduledFireTime = new Date();
        Date prevFireTime = null;
        Date nextFireTime = null;
        TriggerFiredBundle result = new TriggerFiredBundle(job, trigger, cal, jobIsRecovering, fireTime, scheduledFireTime, prevFireTime, nextFireTime);
		return result;
	}

	private OperableTrigger createQueueJobTrigger() {
		QueueJobTrigger result = new QueueJobTrigger();
		return result;
	}
}
