/**
 *  Copyright 2003-2009 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.quartz.utils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A fail-safe timer in the sense that if the runtime environment restricts
 * creating new threads, it doesn't blow up with exception. TimerTasks that are
 * scheduled will run at least once (inline when they are scheduled) if creating
 * threads is not allowed.
 * For example, Google App Engine does not allow creation of new threads.
 * 
 * @author <a href="mailto:asanoujam@terracottatech.com">Abhishek Sanoujam</a>
 * @since 1.7
 * 
 */
public class FailSafeTimer {

    private final Timer timer;
    private final boolean timerThreadRunning;

    /**
     * Constructor accepting a name for the timer. The scheduling thread is
     * created as a daemon
     * 
     * @param name
     */
    public FailSafeTimer(String name) {
        boolean threadRunning;
        Timer localTimer = null;
        try {
            localTimer = new Timer(name, true);
            threadRunning = true;
        } catch (Exception e) {
            localTimer = null;
            threadRunning = false;
        }
        this.timerThreadRunning = threadRunning;
        this.timer = localTimer;
    }

    /**
     * If the runtime environment restricts thread creation, this method does
     * nothing.
     * 
     * @see java.util.Timer#cancel()
     */
    public void cancel() {
        if (timerThreadRunning) {
            timer.cancel();
        }
    }

    /**
     * If the runtime environment restricts thread creation, this method does
     * nothing.
     * 
     * @see java.util.Timer#purge()
     */
    public int purge() {
        if (timerThreadRunning) {
            return timer.purge();
        } else {
            return 0;
        }
    }

    /**
     * If the runtime environment restricts thread creation, the task is run
     * inline for only one time. No further repeated execution happens for the
     * task
     * 
     * @see java.util.Timer#schedule(java.util.TimerTask, java.util.Date, long)
     */
    public void schedule(TimerTask task, Date firstTime, long period) {
        if (timerThreadRunning) {
            timer.schedule(task, firstTime, period);
        } else {
            task.run();
        }
    }

    /**
     * If the runtime environment restricts thread creation, the task is run
     * inline for only one time. No further repeated execution happens for the
     * task
     * 
     * @see java.util.Timer#schedule(java.util.TimerTask, java.util.Date)
     */
    public void schedule(TimerTask task, Date time) {
        if (timerThreadRunning) {
            timer.schedule(task, time);
        } else {
            task.run();
        }
    }

    /**
     * If the runtime environment restricts thread creation, the task is run
     * inline for only one time. No further repeated execution happens for the
     * task
     * 
     * @see java.util.Timer#schedule(java.util.TimerTask, long, long)
     */
    public void schedule(TimerTask task, long delay, long period) {
        if (timerThreadRunning) {
            timer.schedule(task, delay, period);
        } else {
            task.run();
        }
    }

    /**
     * If the runtime environment restricts thread creation, the task is run
     * inline for only one time. No further repeated execution happens for the
     * task
     * 
     * @see java.util.Timer#schedule(java.util.TimerTask, long)
     */
    public void schedule(TimerTask task, long delay) {
        if (timerThreadRunning) {
            timer.schedule(task, delay);
        } else {
            task.run();
        }
    }

    /**
     * If the runtime environment restricts thread creation, the task is run
     * inline for only one time. No further repeated execution happens for the
     * task
     * 
     * @see java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, java.util.Date, long)
     */
    public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
        if (timerThreadRunning) {
            timer.scheduleAtFixedRate(task, firstTime, period);
        } else {
            task.run();
        }
    }

    /**
     * If the runtime environment restricts thread creation, the task is run
     * inline for only one time. No further repeated execution happens for the
     * task
     * 
     * @see java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, long, long)
     */
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        if (timerThreadRunning) {
            timer.scheduleAtFixedRate(task, delay, period);
        } else {
            task.run();
        }
    }

}
