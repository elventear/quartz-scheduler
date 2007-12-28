/*
 * Copyright 2004-2006 OpenSymphony
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
 */
package org.quartz.listeners;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

import org.quartz.JobListener;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDetail;

/**
 * Holds a List of references to JobListener instances and broadcasts all
 * events to them (in order) - if the event is not excluded via filtering
 * (read on).
 *
 * <p>The broadcasting behavior of this listener to delegate listeners may be
 * more convenient than registering all of the listeners directly with the
 * Trigger, and provides the flexibility of easily changing which listeners
 * get notified.</p>
 *
 * <p>You may also register a number of Regular Expression patterns to match
 * the events against. If one or more patterns are registered, the broadcast
 * will only take place if the event applies to a job who's name/group
 * matches one or more of the patterns.</p>
 *
 * @see #addListener(org.quartz.JobListener)
 * @see #removeListener(org.quartz.JobListener)
 * @see #removeListener(String)
 * @see #addJobNamePattern(String)
 * @see #addJobGroupPattern(String)
 *
 * @author James House (jhouse AT revolition DOT net)
 */
public class FilterAndBroadcastJobListener implements JobListener {

    private String name;
    private List listeners;
    private List namePatterns = new LinkedList();
    private List groupPatterns = new LinkedList();

    /**
     * Construct an instance with the given name.
     *
     * (Remember to add some delegate listeners!)
     *
     * @param name the name of this instance
     */
    public FilterAndBroadcastJobListener(String name) {
        if(name == null) {
            throw new IllegalArgumentException("Listener name cannot be null!");
        }
        this.name = name;
        listeners = new LinkedList();
    }

    /**
     * Construct an instance with the given name, and List of listeners.
     *
     * @param name the name of this instance
     * @param listeners the initial List of JobListeners to broadcast to.
     */
    public FilterAndBroadcastJobListener(String name, List listeners) {
        this(name);
        this.listeners.addAll(listeners);
    }

    public String getName() {
        return name;
    }

    public void addListener(JobListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(JobListener listener) {
        return listeners.remove(listener);
    }

    public boolean removeListener(String listenerName) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            JobListener jl = (JobListener) itr.next();
            if(jl.getName().equals(listenerName)) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    public List getListeners() {
        return java.util.Collections.unmodifiableList(listeners);
    }

    /**
     * If one or more name patterns are specified, only events relating to
     * jobs who's name matches the given regular expression pattern
     * will be dispatched to the delegate listeners.
     *
     * @param regularExpression
     */
    public void addJobNamePattern(String regularExpression) {
        if(regularExpression == null) {
            throw new IllegalArgumentException("Expression cannot be null!");
        }

        namePatterns.add(regularExpression);
    }

    public List getJobNamePatterns() {
        return namePatterns;
    }

    /**
     * If one or more group patterns are specified, only events relating to
     * jobs who's group matches the given regular expression pattern
     * will be dispatched to the delegate listeners.
     *
     * @param regularExpression
     */
    public void addJobGroupPattern(String regularExpression) {
        if(regularExpression == null) {
            throw new IllegalArgumentException("Expression cannot be null!");
        }

        groupPatterns.add(regularExpression);
    }

    public List getJobGroupPatterns() {
        return namePatterns;
    }

    protected boolean shouldDispatch(JobExecutionContext context) {
        JobDetail job = context.getJobDetail();

        if(namePatterns.size() == 0 && groupPatterns.size() == 0) {
            return true;
        }

        Iterator itr = groupPatterns.iterator();
        while(itr.hasNext()) {
            String pat = (String) itr.next();
            if(job.getGroup().matches(pat)) {
                return true;
            }
        }

        itr = namePatterns.iterator();
        while(itr.hasNext()) {
            String pat = (String) itr.next();
            if(job.getName().matches(pat)) {
                return true;
            }
        }

        return false;
    }

    public void jobToBeExecuted(JobExecutionContext context) {

        if(!shouldDispatch(context)) {
            return;
        }

        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            JobListener jl = (JobListener) itr.next();
            jl.jobToBeExecuted(context);
        }
    }

    public void jobExecutionVetoed(JobExecutionContext context) {

        if(!shouldDispatch(context)) {
            return;
        }

        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            JobListener jl = (JobListener) itr.next();
            jl.jobExecutionVetoed(context);
        }
    }

    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

        if(!shouldDispatch(context)) {
            return;
        }

        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            JobListener jl = (JobListener) itr.next();
            jl.jobWasExecuted(context, jobException);
        }
    }

}
