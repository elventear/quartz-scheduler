package org.quartz.listeners;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;

/**
 * Holds a List of references to SchedulerListener instances and broadcasts all
 * events to them (in order).
 *
 * <p>This may be more convenient than registering all of the listeners
 * directly with the Scheduler, and provides the flexibility of easily changing
 * which listeners get notified.</p>
 *
 * @see #addListener(org.quartz.SchedulerListener)
 * @see #removeListener(org.quartz.SchedulerListener)
 *
 * @author James House (jhouse AT revolition DOT net)
 */
public class BroadcastSchedulerListener implements SchedulerListener {

    private List listeners;

    public BroadcastSchedulerListener() {
        listeners = new LinkedList();
    }

    /**
     * Construct an instance with the given List of listeners.
     *
     * @param listeners the initial List of SchedulerListeners to broadcast to.
     */
    public BroadcastSchedulerListener(List listeners) {
        this();
        this.listeners.addAll(listeners);
    }


    public void addListener(SchedulerListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(SchedulerListener listener) {
        return listeners.remove(listener);
    }

    public List getListeners() {
        return java.util.Collections.unmodifiableList(listeners);
    }

    public void jobScheduled(Trigger trigger) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.jobScheduled(trigger);
        }
    }

    public void jobUnscheduled(String triggerName, String triggerGroup) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.jobUnscheduled(triggerName, triggerGroup);
        }
    }

    public void triggerFinalized(Trigger trigger) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.triggerFinalized(trigger);
        }
    }

    public void triggersPaused(String triggerName, String triggerGroup) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.triggersPaused(triggerName, triggerGroup);
        }
    }

    public void triggersResumed(String triggerName, String triggerGroup) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.triggersResumed(triggerName, triggerGroup);
        }
    }

    public void jobsPaused(String jobName, String jobGroup) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.jobsPaused(jobName, jobGroup);
        }
    }

    public void jobsResumed(String jobName, String jobGroup) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.jobsResumed(jobName, jobGroup);
        }
    }

    public void schedulerError(String msg, SchedulerException cause) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.schedulerError(msg, cause);
        }
    }

    public void schedulerShutdown() {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            SchedulerListener l = (SchedulerListener) itr.next();
            l.schedulerShutdown();
        }
    }
}
