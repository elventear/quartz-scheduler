
/* 
 * Copyright 2001-2009 Terracotta, Inc. 
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

package org.quartz;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

import org.quartz.utils.Key;



/**
 * The base interface implemented by all <code>Trigger</code>s.
 * 
 * <p>
 * <code>Triggers</code>s have a name and group associated with them, which
 * should uniquely identify them within a single <code>{@link Scheduler}</code>.
 * </p>
 * 
 * <p>
 * <code>Trigger</code>s are the 'mechanism' by which <code>Job</code>s
 * are scheduled. Many <code>Trigger</code>s can point to the same <code>Job</code>,
 * but a single <code>Trigger</code> can only point to one <code>Job</code>.
 * </p>
 * 
 * <p>
 * Triggers can 'send' parameters/data to <code>Job</code>s by placing contents
 * into the <code>JobDataMap</code> on the <code>Trigger</code>.
 * </p>
 * 
 * @see SimpleTrigger
 * @see CronTrigger
 * @see NthIncludedDayTrigger
 * @see TriggerUtils
 * @see JobDataMap
 * @see JobExecutionContext
 * 
 * @author James House
 */
public interface Trigger extends Serializable, Cloneable, Comparable<Trigger> {

    public static final long serialVersionUID = -3904243490805975570L;
    
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that the <code>{@link Trigger}</code>
     * has no further instructions.
     * </p>
     */
    public static final int INSTRUCTION_NOOP = 0;
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that the <code>{@link Trigger}</code>
     * wants the <code>{@link org.quartz.JobDetail}</code> to re-execute
     * immediately. If not in a 'RECOVERING' or 'FAILED_OVER' situation, the
     * execution context will be re-used (giving the <code>Job</code> the
     * ability to 'see' anything placed in the context by its last execution).
     * </p>
     */
    public static final int INSTRUCTION_RE_EXECUTE_JOB = 1;
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that the <code>{@link Trigger}</code>
     * should be put in the <code>COMPLETE</code> state.
     * </p>
     */
    public static final int INSTRUCTION_SET_TRIGGER_COMPLETE = 2;
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that the <code>{@link Trigger}</code>
     * wants itself deleted.
     * </p>
     */
    public static final int INSTRUCTION_DELETE_TRIGGER = 3;
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that all <code>Trigger</code>
     * s referencing the same <code>{@link org.quartz.JobDetail}</code> as
     * this one should be put in the <code>COMPLETE</code> state.
     * </p>
     */
    public static final int INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE = 4;
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that all <code>Trigger</code>
     * s referencing the same <code>{@link org.quartz.JobDetail}</code> as
     * this one should be put in the <code>ERROR</code> state.
     * </p>
     */
    public static final int INSTRUCTION_SET_TRIGGER_ERROR = 5;
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that the <code>Trigger</code>
     * should be put in the <code>ERROR</code> state.
     * </p>
     */
    public static final int INSTRUCTION_SET_ALL_JOB_TRIGGERS_ERROR = 6;
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>updateAfterMisfire()</code> method will be called
     * on the <code>Trigger</code> to determine the mis-fire instruction.
     * </p>
     * 
     * <p>
     * In order to see if this instruction fits your needs, you should look at
     * the documentation for the <code>getSmartMisfirePolicy()</code> method
     * on the particular <code>Trigger</code> implementation you are using.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_SMART_POLICY = 0;
    /**
     * <p>
     * Indicates that the <code>Trigger</code> is in the "normal" state.
     * </p>
     */
    public static final int STATE_NORMAL = 0;
    /**
     * <p>
     * Indicates that the <code>Trigger</code> is in the "paused" state.
     * </p>
     */
    public static final int STATE_PAUSED = 1;
    /**
     * <p>
     * Indicates that the <code>Trigger</code> is in the "complete" state.
     * </p>
     * 
     * <p>
     * "Complete" indicates that the trigger has not remaining fire-times in
     * its schedule.
     * </p>
     */
    public static final int STATE_COMPLETE = 2;
    /**
     * <p>
     * Indicates that the <code>Trigger</code> is in the "error" state.
     * </p>
     * 
     * <p>
     * A <code>Trigger</code> arrives at the error state when the scheduler
     * attempts to fire it, but cannot due to an error creating and executing
     * its related job. Often this is due to the <code>Job</code>'s
     * class not existing in the classpath.
     * </p>
     * 
     * <p>
     * When the trigger is in the error state, the scheduler will make no
     * attempts to fire it.
     * </p>
     */
    public static final int STATE_ERROR = 3;
    /**
     * <p>
     * Indicates that the <code>Trigger</code> is in the "blocked" state.
     * </p>
     * 
     * <p>
     * A <code>Trigger</code> arrives at the blocked state when the job that
     * it is associated with is a <code>StatefulJob</code> and it is 
     * currently executing.
     * </p>
     *
     * @see StatefulJob
     */
    public static final int STATE_BLOCKED = 4;
    /**
     * <p>
     * Indicates that the <code>Trigger</code> does not exist.
     * </p>
     */
    public static final int STATE_NONE = -1;
    /**
     * The default value for priority.
     */
    public static final int DEFAULT_PRIORITY = 5;

    public TriggerKey getKey();

    public JobKey getJobKey();
    
    /**
     * <p>
     * Return the description given to the <code>Trigger</code> instance by
     * its creator (if any).
     * </p>
     * 
     * @return null if no description was set.
     */
    public String getDescription();

    /**
     * <p>
     * Get the name of the <code>{@link Calendar}</code> associated with this
     * Trigger.
     * </p>
     * 
     * @return <code>null</code> if there is no associated Calendar.
     */
    public String getCalendarName();

    /**
     * <p>
     * Get the <code>JobDataMap</code> that is associated with the 
     * <code>Trigger</code>.
     * </p>
     * 
     * <p>
     * Changes made to this map during job execution are not re-persisted, and
     * in fact typically result in an <code>IllegalStateException</code>.
     * </p>
     */
    public JobDataMap getJobDataMap();

    /**
     * <p>
     * Whether or not the <code>Trigger</code> should be persisted in the
     * <code>{@link org.quartz.spi.JobStore}</code> for re-use after program
     * restarts.
     * </p>
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> if the <code>Trigger</code> should be
     *         garbage collected along with the <code>{@link Scheduler}</code>.
     */
    public boolean isVolatile();

    /**
     * The priority of a <code>Trigger</code> acts as a tiebreaker such that if 
     * two <code>Trigger</code>s have the same scheduled fire time, then the
     * one with the higher priority will get first access to a worker
     * thread.
     * 
     * <p>
     * If not explicitly set, the default value is <code>5</code>.
     * </p>
     * 
     * @see #DEFAULT_PRIORITY
     */
    public int getPriority();

    /**
     * <p>
     * Used by the <code>{@link Scheduler}</code> to determine whether or not
     * it is possible for this <code>Trigger</code> to fire again.
     * </p>
     * 
     * <p>
     * If the returned value is <code>false</code> then the <code>Scheduler</code>
     * may remove the <code>Trigger</code> from the <code>{@link org.quartz.spi.JobStore}</code>.
     * </p>
     */
    public boolean mayFireAgain();

    /**
     * <p>
     * Get the time at which the <code>Trigger</code> should occur.
     * </p>
     */
    public Date getStartTime();

    /**
     * <p>
     * Get the time at which the <code>Trigger</code> should quit repeating -
     * regardless of any remaining repeats (based on the trigger's particular 
     * repeat settings). 
     * </p>
     * 
     * @see #getFinalFireTime()
     */
    public Date getEndTime();

    /**
     * <p>
     * Returns the next time at which the <code>Trigger</code> is scheduled to fire. If
     * the trigger will not fire again, <code>null</code> will be returned.  Note that
     * the time returned can possibly be in the past, if the time that was computed
     * for the trigger to next fire has already arrived, but the scheduler has not yet
     * been able to fire the trigger (which would likely be due to lack of resources
     * e.g. threads).
     * </p>
     *
     * <p>The value returned is not guaranteed to be valid until after the <code>Trigger</code>
     * has been added to the scheduler.
     * </p>
     *
     * @see TriggerUtils#computeFireTimesBetween(Trigger, Calendar, Date, Date)
     */
    public Date getNextFireTime();

    /**
     * <p>
     * Returns the previous time at which the <code>Trigger</code> fired.
     * If the trigger has not yet fired, <code>null</code> will be returned.
     */
    public Date getPreviousFireTime();

    /**
     * <p>
     * Returns the next time at which the <code>Trigger</code> will fire,
     * after the given time. If the trigger will not fire after the given time,
     * <code>null</code> will be returned.
     * </p>
     */
    public Date getFireTimeAfter(Date afterTime);

    /**
     * <p>
     * Returns the last time at which the <code>Trigger</code> will fire, if
     * the Trigger will repeat indefinitely, null will be returned.
     * </p>
     * 
     * <p>
     * Note that the return time *may* be in the past.
     * </p>
     */
    public Date getFinalFireTime();

    /**
     * <p>
     * Get the instruction the <code>Scheduler</code> should be given for
     * handling misfire situations for this <code>Trigger</code>- the
     * concrete <code>Trigger</code> type that you are using will have
     * defined a set of additional <code>MISFIRE_INSTRUCTION_XXX</code>
     * constants that may be passed to this method.
     * </p>
     * 
     * <p>
     * If not explicitly set, the default value is <code>MISFIRE_INSTRUCTION_SMART_POLICY</code>.
     * </p>
     * 
     * @see #MISFIRE_INSTRUCTION_SMART_POLICY
     * @see #updateAfterMisfire(Calendar)
     * @see SimpleTrigger
     * @see CronTrigger
     */
    public int getMisfireInstruction();

    /**
     * <p>
     * Compare the next fire time of this <code>Trigger</code> to that of
     * another.
     * </p>
     */
    public int compareTo(Trigger other);

}
