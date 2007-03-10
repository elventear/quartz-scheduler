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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/**
 * Holds a List of references to TriggerListener instances and broadcasts all
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
 * will only take place if the event applies to a trigger who's name/group
 * matches one or more of the patterns.</p>
 *
 * @see #addListener(org.quartz.TriggerListener)
 * @see #removeListener(org.quartz.TriggerListener)
 * @see #removeListener(String)
 * @see #addTriggerNamePattern(String)
 * @see #addTriggerGroupPattern(String)
 *
 * @author James House (jhouse AT revolition DOT net)
 */
public class FilterAndBroadcastTriggerListener implements TriggerListener {

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
    public FilterAndBroadcastTriggerListener(String name) {
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
     * @param listeners the initial List of TriggerListeners to broadcast to.
     */
    public FilterAndBroadcastTriggerListener(String name, List listeners) {
        this(name);
        this.listeners.addAll(listeners);
    }

    public String getName() {
        return name;
    }

    public void addListener(TriggerListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(TriggerListener listener) {
        return listeners.remove(listener);
    }

    public boolean removeListener(String listenerName) {
        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            TriggerListener l = (TriggerListener) itr.next();
            if(l.getName().equals(listenerName)) {
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
     * triggers who's name matches the given regular expression pattern
     * will be dispatched to the delegate listeners.
     *
     * @param regularExpression
     */
    public void addTriggerNamePattern(String regularExpression) {
        if(regularExpression == null) {
            throw new IllegalArgumentException("Expression cannot be null!");
        }

        namePatterns.add(regularExpression);
    }

    public List getTriggerNamePatterns() {
        return namePatterns;
    }

    /**
     * If one or more group patterns are specified, only events relating to
     * triggers who's group matches the given regular expression pattern
     * will be dispatched to the delegate listeners.
     *
     * @param regularExpression
     */
    public void addTriggerGroupPattern(String regularExpression) {
        if(regularExpression == null) {
            throw new IllegalArgumentException("Expression cannot be null!");
        }

        groupPatterns.add(regularExpression);
    }

    public List getTriggerGroupPatterns() {
        return namePatterns;
    }

    protected boolean shouldDispatch(Trigger trigger) {

        if(namePatterns.size() == 0 && groupPatterns.size() == 0) {
            return true;
        }

        Iterator itr = groupPatterns.iterator();
        while(itr.hasNext()) {
            String pat = (String) itr.next();
            if(trigger.getGroup().matches(pat)) {
                return true;
            }
        }

        itr = namePatterns.iterator();
        while(itr.hasNext()) {
            String pat = (String) itr.next();
            if(trigger.getName().matches(pat)) {
                return true;
            }
        }

        return false;
    }

    public void triggerFired(Trigger trigger, JobExecutionContext context) {

        if(!shouldDispatch(trigger)) {
            return;
        }

        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            TriggerListener l = (TriggerListener) itr.next();
            l.triggerFired(trigger, context);
        }
    }

    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {

        if(!shouldDispatch(trigger)) {
            return false;
        }

        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            TriggerListener l = (TriggerListener) itr.next();
            if(l.vetoJobExecution(trigger, context)) {
                return true;
            }
        }
        return false;
    }

    public void triggerMisfired(Trigger trigger) {

        if(!shouldDispatch(trigger)) {
            return;
        }

        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            TriggerListener l = (TriggerListener) itr.next();
            l.triggerMisfired(trigger);
        }
    }

    public void triggerComplete(Trigger trigger, JobExecutionContext context, int triggerInstructionCode) {

        if(!shouldDispatch(trigger)) {
            return;
        }

        Iterator itr = listeners.iterator();
        while(itr.hasNext()) {
            TriggerListener l = (TriggerListener) itr.next();
            l.triggerComplete(trigger, context, triggerInstructionCode);
        }
    }

}
