
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

import java.util.Calendar;
import java.util.Date;


/**
 * <p>A concrete <code>{@link Trigger}</code> that is used to fire a <code>{@link org.quartz.JobDetail}</code>
 * based upon repeating calendar time intervals.</p>
 * 
 * <p>The trigger will fire every N (see {@link #setRepeatInterval(int)} ) units of calendar time
 * (see {@link #setRepeatIntervalUnit(IntervalUnit)}) as specified in the trigger's definition.  
 * This trigger can achieve schedules that are not possible with {@link SimpleTrigger} (e.g 
 * because months are not a fixed number of seconds) or {@link CronTrigger} (e.g. because
 * "every 5 months" is not an even divisor of 12).</p>
 * 
 * @see Trigger
 * @see CronTrigger
 * @see SimpleTrigger
 * @see NthIncludedDayTrigger
 * @see TriggerUtils
 * 
 * @since 1.7
 * 
 * @author James House
 */
public class DateIntervalTrigger extends Trigger {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private static final long serialVersionUID = -2635982274232850343L;

    
    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link DateIntervalTrigger}</code> wants to be 
     * fired now by <code>Scheduler</code>.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_FIRE_ONCE_NOW = 1;

    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link DateIntervalTrigger}</code> wants to have it's
     * next-fire-time updated to the next time in the schedule after the
     * current time (taking into account any associated <code>{@link Calendar}</code>,
     * but it does not want to be fired now.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_DO_NOTHING = 2;

    private static final int YEAR_TO_GIVEUP_SCHEDULING_AT = 2299;
    
    public enum IntervalUnit { SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR };
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    
    private Date startTime = null;

    private Date endTime = null;

    private Date nextFireTime = null;

    private Date previousFireTime = null;

    private  int repeatInterval = 0;
    
    private IntervalUnit repeatIntervalUnit = IntervalUnit.DAY;

    private int timesTriggered = 0;

    private boolean complete = false;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a <code>DateIntervalTrigger</code> with no settings.
     * </p>
     */
    public DateIntervalTrigger() {
        super();
    }

    /**
     * <p>
     * Create a <code>DateIntervalTrigger</code> that will occur immediately, and
     * repeat at the the given interval.
     * </p>
     */
    public DateIntervalTrigger(String name, IntervalUnit intervalUnit,  int repeatInterval) {
        this(name, null, intervalUnit, repeatInterval);
    }

    /**
     * <p>
     * Create a <code>DateIntervalTrigger</code> that will occur immediately, and
     * repeat at the the given interval.
     * </p>
     */
    public DateIntervalTrigger(String name, String group, IntervalUnit intervalUnit,
            int repeatInterval) {
        this(name, group, new Date(), null, intervalUnit, repeatInterval);
    }
    
    /**
     * <p>
     * Create a <code>DateIntervalTrigger</code> that will occur at the given time,
     * and repeat at the the given interval until the given end time.
     * </p>
     * 
     * @param startTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to fire.
     * @param endTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to quit repeat firing.
     * @param intervalUnit
     *          The repeat interval unit (minutes, days, months, etc).
     * @param repeatInterval
     *          The number of milliseconds to pause between the repeat firing.
     */
    public DateIntervalTrigger(String name, Date startTime,
            Date endTime, IntervalUnit intervalUnit,  int repeatInterval) {
        this(name, null, startTime, endTime, intervalUnit, repeatInterval);
    }
    
    /**
     * <p>
     * Create a <code>DateIntervalTrigger</code> that will occur at the given time,
     * and repeat at the the given interval until the given end time.
     * </p>
     * 
     * @param startTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to fire.
     * @param endTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to quit repeat firing.
     * @param intervalUnit
     *          The repeat interval unit (minutes, days, months, etc).
     * @param repeatInterval
     *          The number of milliseconds to pause between the repeat firing.
     */
    public DateIntervalTrigger(String name, String group, Date startTime,
            Date endTime, IntervalUnit intervalUnit,  int repeatInterval) {
        super(name, group);

        setStartTime(startTime);
        setEndTime(endTime);
        setRepeatIntervalUnit(intervalUnit);
        setRepeatInterval(repeatInterval);
    }

    /**
     * <p>
     * Create a <code>DateIntervalTrigger</code> that will occur at the given time,
     * fire the identified <code>Job</code> and repeat at the the given
     * interval until the given end time.
     * </p>
     * 
     * @param startTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to fire.
     * @param endTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to quit repeat firing.
     * @param intervalUnit
     *          The repeat interval unit (minutes, days, months, etc).
     * @param repeatInterval
     *          The number of milliseconds to pause between the repeat firing.
     */
    public DateIntervalTrigger(String name, String group, String jobName,
            String jobGroup, Date startTime, Date endTime,  
            IntervalUnit intervalUnit,  int repeatInterval) {
        super(name, group, jobName, jobGroup);

        setStartTime(startTime);
        setEndTime(endTime);
        setRepeatIntervalUnit(intervalUnit);
        setRepeatInterval(repeatInterval);
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
     * Get the time at which the <code>DateIntervalTrigger</code> should occur.
     * </p>
     */
    @Override
    public Date getStartTime() {
        if(startTime == null)
            startTime = new Date();
        return startTime;
    }

    /**
     * <p>
     * Set the time at which the <code>DateIntervalTrigger</code> should occur.
     * </p>
     * 
     * @exception IllegalArgumentException
     *              if startTime is <code>null</code>.
     */
    @Override
    public void setStartTime(Date startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }

        Date eTime = getEndTime();
        if (eTime != null && startTime != null && eTime.before(startTime)) {
            throw new IllegalArgumentException(
                "End time cannot be before start time");    
        }

        this.startTime = startTime;
    }

    /**
     * <p>
     * Get the time at which the <code>DateIntervalTrigger</code> should quit
     * repeating.
     * </p>
     * 
     * @see #getFinalFireTime()
     */
    @Override
    public Date getEndTime() {
        return endTime;
    }

    /**
     * <p>
     * Set the time at which the <code>DateIntervalTrigger</code> should quit
     * repeating (and be automatically deleted).
     * </p>
     * 
     * @exception IllegalArgumentException
     *              if endTime is before start time.
     */
    @Override
    public void setEndTime(Date endTime) {
        Date sTime = getStartTime();
        if (sTime != null && endTime != null && sTime.after(endTime)) {
            throw new IllegalArgumentException(
                    "End time cannot be before start time");
        }

        this.endTime = endTime;
    }

    /**
     * <p>Get the interval unit - the time unit on with the interval applies.</p>
     */
    public IntervalUnit getRepeatIntervalUnit() {
        return repeatIntervalUnit;
    }

    /**
     * <p>Set the interval unit - the time unit on with the interval applies.</p>
     */
    public void setRepeatIntervalUnit(IntervalUnit intervalUnit) {
        this.repeatIntervalUnit = intervalUnit;
    }

    /**
     * <p>
     * Get the the time interval that will be added to the <code>DateIntervalTrigger</code>'s
     * fire time (in the set repeat interval unit) in order to calculate the time of the 
     * next trigger repeat.
     * </p>
     */
    public int getRepeatInterval() {
        return repeatInterval;
    }

    /**
     * <p>
     * set the the time interval that will be added to the <code>DateIntervalTrigger</code>'s
     * fire time (in the set repeat interval unit) in order to calculate the time of the 
     * next trigger repeat.
     * </p>
     * 
     * @exception IllegalArgumentException
     *              if repeatInterval is < 1
     */
    public void setRepeatInterval( int repeatInterval) {
        if (repeatInterval < 0) {
            throw new IllegalArgumentException(
                    "Repeat interval must be >= 1");
        }

        this.repeatInterval = repeatInterval;
    }

    /**
     * <p>
     * Get the number of times the <code>DateIntervalTrigger</code> has already
     * fired.
     * </p>
     */
    public int getTimesTriggered() {
        return timesTriggered;
    }

    /**
     * <p>
     * Set the number of times the <code>DateIntervalTrigger</code> has already
     * fired.
     * </p>
     */
    public void setTimesTriggered(int timesTriggered) {
        this.timesTriggered = timesTriggered;
    }

    @Override
    protected boolean validateMisfireInstruction(int misfireInstruction) {
        if (misfireInstruction < MISFIRE_INSTRUCTION_SMART_POLICY) {
            return false;
        }

        if (misfireInstruction > MISFIRE_INSTRUCTION_DO_NOTHING) {
            return false;
        }

        return true;
    }


    /**
     * <p>
     * Updates the <code>DateIntervalTrigger</code>'s state based on the
     * MISFIRE_INSTRUCTION_XXX that was selected when the <code>DateIntervalTrigger</code>
     * was created.
     * </p>
     * 
     * <p>
     * If the misfire instruction is set to MISFIRE_INSTRUCTION_SMART_POLICY,
     * then the following scheme will be used: <br>
     * <ul>
     * <li>The instruction will be interpreted as <code>MISFIRE_INSTRUCTION_FIRE_ONCE_NOW</code>
     * </ul>
     * </p>
     */
    @Override
    public void updateAfterMisfire(org.quartz.Calendar cal) {
        int instr = getMisfireInstruction();

        if (instr == MISFIRE_INSTRUCTION_SMART_POLICY) {
            instr = MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        }

        if (instr == MISFIRE_INSTRUCTION_DO_NOTHING) {
            Date newFireTime = getFireTimeAfter(new Date());
            while (newFireTime != null && cal != null
                    && !cal.isTimeIncluded(newFireTime.getTime())) {
                newFireTime = getFireTimeAfter(newFireTime);
            }
            setNextFireTime(newFireTime);
        } else if (instr == MISFIRE_INSTRUCTION_FIRE_ONCE_NOW) { // TODO: JHOUSE: think this out, reset will change time-of-day for day/month/year intervals...
            //setNextFireTime(new Date());
        }
    }

    /**
     * <p>
     * Called when the <code>{@link Scheduler}</code> has decided to 'fire'
     * the trigger (execute the associated <code>Job</code>), in order to
     * give the <code>Trigger</code> a chance to update itself for its next
     * triggering (if any).
     * </p>
     * 
     * @see #executionComplete(JobExecutionContext, JobExecutionException)
     */
    @Override
    public void triggered(org.quartz.Calendar calendar) {
        timesTriggered++;
        previousFireTime = nextFireTime;
        nextFireTime = getFireTimeAfter(nextFireTime);

        while (nextFireTime != null && calendar != null
                && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            
            nextFireTime = getFireTimeAfter(nextFireTime);

            if(nextFireTime == null)
                break;
            
            //avoid infinite loop
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(nextFireTime);
            if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
                nextFireTime = null;
            }
        }
    }


    /**
     *  
     * @see org.quartz.Trigger#updateWithNewCalendar(org.quartz.Calendar, long)
     */
    @Override
    public void updateWithNewCalendar(org.quartz.Calendar calendar, long misfireThreshold)
    {
        nextFireTime = getFireTimeAfter(previousFireTime);

        if (nextFireTime == null || calendar == null) {
            return;
        }
        
        Date now = new Date();
        while (nextFireTime != null && !calendar.isTimeIncluded(nextFireTime.getTime())) {

            nextFireTime = getFireTimeAfter(nextFireTime);

            if(nextFireTime == null)
                break;
            
            //avoid infinite loop
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(nextFireTime);
            if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
                nextFireTime = null;
            }

            if(nextFireTime != null && nextFireTime.before(now)) {
                long diff = now.getTime() - nextFireTime.getTime();
                if(diff >= misfireThreshold) {
                    nextFireTime = getFireTimeAfter(nextFireTime);
                }
            }
        }
    }

    /**
     * <p>
     * Called by the scheduler at the time a <code>Trigger</code> is first
     * added to the scheduler, in order to have the <code>Trigger</code>
     * compute its first fire time, based on any associated calendar.
     * </p>
     * 
     * <p>
     * After this method has been called, <code>getNextFireTime()</code>
     * should return a valid answer.
     * </p>
     * 
     * @return the first time at which the <code>Trigger</code> will be fired
     *         by the scheduler, which is also the same value <code>getNextFireTime()</code>
     *         will return (until after the first firing of the <code>Trigger</code>).
     *         </p>
     */
    @Override
    public Date computeFirstFireTime(org.quartz.Calendar calendar) {
        nextFireTime = getStartTime();

        while (nextFireTime != null && calendar != null
                && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            
            nextFireTime = getFireTimeAfter(nextFireTime);
            
            if(nextFireTime == null)
                break;

            //avoid infinite loop
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(nextFireTime);
            if (c.get(java.util.Calendar.YEAR) > YEAR_TO_GIVEUP_SCHEDULING_AT) {
                return null;
            }
        }
        
        return nextFireTime;
    }

    /**
     * <p>
     * Called after the <code>{@link Scheduler}</code> has executed the
     * <code>{@link org.quartz.JobDetail}</code> associated with the <code>Trigger</code>
     * in order to get the final instruction code from the trigger.
     * </p>
     * 
     * @param context
     *          is the <code>JobExecutionContext</code> that was used by the
     *          <code>Job</code>'s<code>execute(xx)</code> method.
     * @param result
     *          is the <code>JobExecutionException</code> thrown by the
     *          <code>Job</code>, if any (may be null).
     * @return one of the Trigger.INSTRUCTION_XXX constants.
     * 
     * @see #INSTRUCTION_NOOP
     * @see #INSTRUCTION_RE_EXECUTE_JOB
     * @see #INSTRUCTION_DELETE_TRIGGER
     * @see #INSTRUCTION_SET_TRIGGER_COMPLETE
     * @see #triggered(Calendar)
     */
    @Override
    public int executionComplete(JobExecutionContext context,
            JobExecutionException result) {
        if (result != null && result.refireImmediately()) {
            return INSTRUCTION_RE_EXECUTE_JOB;
        }

        if (result != null && result.unscheduleFiringTrigger()) {
            return INSTRUCTION_SET_TRIGGER_COMPLETE;
        }

        if (result != null && result.unscheduleAllTriggers()) {
            return INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE;
        }

        if (!mayFireAgain()) {
            return INSTRUCTION_DELETE_TRIGGER;
        }

        return INSTRUCTION_NOOP;
    }

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
     */
    @Override
    public Date getNextFireTime() {
        return nextFireTime;
    }

    /**
     * <p>
     * Returns the previous time at which the <code>DateIntervalTrigger</code> 
     * fired. If the trigger has not yet fired, <code>null</code> will be
     * returned.
     */
    @Override
    public Date getPreviousFireTime() {
        return previousFireTime;
    }

    /**
     * <p>
     * Set the next time at which the <code>DateIntervalTrigger</code> should fire.
     * </p>
     * 
     * <p>
     * <b>This method should not be invoked by client code.</b>
     * </p>
     */
    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    /**
     * <p>
     * Set the previous time at which the <code>DateIntervalTrigger</code> fired.
     * </p>
     * 
     * <p>
     * <b>This method should not be invoked by client code.</b>
     * </p>
     */
    public void setPreviousFireTime(Date previousFireTime) {
        this.previousFireTime = previousFireTime;
    }

    /**
     * <p>
     * Returns the next time at which the <code>DateIntervalTrigger</code> will
     * fire, after the given time. If the trigger will not fire after the given
     * time, <code>null</code> will be returned.
     * </p>
     */
    public Date getFireTimeAfter(Date afterTime) {
        if (complete) {
            return null;
        }

        // increment afterTme by a second, so that we are 
        // comparing against a time after it!
        if (afterTime == null) {
            afterTime = new Date(System.currentTimeMillis() + 1000L);
        }
        else {
            afterTime = new Date(afterTime.getTime() + 1000L);
        }

        long startMillis = getStartTime().getTime();
        long afterMillis = afterTime.getTime();
        long endMillis = (getEndTime() == null) ? Long.MAX_VALUE : getEndTime()
                .getTime();

        if (endMillis <= afterMillis) {
            return null;
        }

        if (afterMillis < startMillis) {
            return new Date(startMillis);
        }

        
        long secondsAfterStart = (afterMillis - startMillis) / 1000L;

        Date time = null;
        long repeatLong = getRepeatInterval();
        
        Calendar sTime = Calendar.getInstance();
        sTime.setTime(getStartTime());
        sTime.setLenient(true);
        
        if(getRepeatIntervalUnit().equals(IntervalUnit.SECOND)) {
            long jumpCount = secondsAfterStart / repeatLong;
            if(secondsAfterStart % repeatLong != 0)
                jumpCount++;
            sTime.add(Calendar.SECOND, getRepeatInterval() * (int)jumpCount);
            time = sTime.getTime();
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.MINUTE)) {
            long jumpCount = secondsAfterStart / (repeatLong * 60L);
            if(secondsAfterStart % (repeatLong * 60L) != 0)
                jumpCount++;
            sTime.add(Calendar.MINUTE, getRepeatInterval() * (int)jumpCount);
            time = sTime.getTime();
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.HOUR)) {
            long jumpCount = secondsAfterStart / (repeatLong * 60L * 60L);
            if(secondsAfterStart % (repeatLong * 60L * 60L) != 0)
                jumpCount++;
            sTime.add(Calendar.HOUR, getRepeatInterval() * (int)jumpCount);
            time = sTime.getTime();
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.DAY)) {
            long jumpCount = secondsAfterStart / (repeatLong * 24L * 60L * 60L);
            if(secondsAfterStart % (repeatLong * 24L * 60L * 60L) != 0)
                jumpCount++;
            sTime.add(Calendar.DAY_OF_YEAR, getRepeatInterval() * (int)jumpCount);
            time = sTime.getTime();
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.WEEK)) {
            long jumpCount = secondsAfterStart / (repeatLong * 7L * 24L * 60L * 60L);
            if(secondsAfterStart % (repeatLong * 7L * 24L * 60L * 60L) != 0)
                jumpCount++;
            sTime.add(Calendar.DAY_OF_YEAR, getRepeatInterval() * (int)jumpCount * 7);
            time = sTime.getTime();
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.MONTH)) {
            while(sTime.getTime().before(afterTime) && 
                    (sTime.get(java.util.Calendar.YEAR) < YEAR_TO_GIVEUP_SCHEDULING_AT)) {            
                sTime.setLenient(true);
                sTime.add(java.util.Calendar.MONTH, getRepeatInterval());
            }
            time = sTime.getTime();
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.YEAR)) {
            while(sTime.getTime().before(afterTime) && 
                    (sTime.get(java.util.Calendar.YEAR) < YEAR_TO_GIVEUP_SCHEDULING_AT)) {            
                sTime.add(java.util.Calendar.YEAR, getRepeatInterval());
            }
            time = sTime.getTime();
        }

        if (endMillis <= time.getTime()) {
            return null;
        }

        return time;
    }

    /**
     * <p>
     * Returns the final time at which the <code>DateIntervalTrigger</code> will
     * fire, if there is no end time set, null will be returned.
     * </p>
     * 
     * <p>
     * Note that the return time may be in the past.
     * </p>
     */
    public Date getFinalFireTime() {
        if (complete || getEndTime() == null) {
            return null;
        }

        Date beforeTime = new Date(getEndTime().getTime() - 1000L);

        long startMillis = getStartTime().getTime();
        long beforeMillis = beforeTime.getTime();

        if (beforeMillis < startMillis) {
            return new Date(startMillis);
        }
        
        long secondsAfterStart = (beforeMillis - startMillis) / 1000L;

        Date time = null;
        long repeatLong = getRepeatInterval();
        
        if(getRepeatIntervalUnit().equals(IntervalUnit.SECOND)) {
            long jumpCount = secondsAfterStart / repeatLong;
            if(secondsAfterStart % repeatLong != 0)
                jumpCount++;
            jumpCount --; // backup to time before
            time = new Date(startMillis + (repeatLong * jumpCount * 1000L));
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.MINUTE)) {
            long jumpCount = secondsAfterStart / (repeatLong * 60L);
            if(secondsAfterStart % (repeatLong * 60L) != 0)
                jumpCount++;
            jumpCount --; // backup to time before
            time = new Date(startMillis + (repeatLong * jumpCount * 60L * 1000L));
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.HOUR)) {
            long jumpCount = secondsAfterStart / (repeatLong * 60L * 60L);
            if(secondsAfterStart % (repeatLong * 60L * 60L) != 0)
                jumpCount++;
            jumpCount --; // backup to time before
            time = new Date(startMillis + (repeatLong * jumpCount * 60L * 60L * 1000L));
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.DAY)) {
            long jumpCount = secondsAfterStart / (repeatLong * 24L * 60L * 60L);
            if(secondsAfterStart % (repeatLong * 24L * 60L * 60L) != 0)
                jumpCount++;
            jumpCount --; // backup to time before
            time = new Date(startMillis + (repeatLong * jumpCount * 24L * 60L * 60L * 1000L));
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.WEEK)) {
            long jumpCount = secondsAfterStart / (repeatLong * 7L * 24L * 60L * 60L);
            if(secondsAfterStart % (repeatLong * 7L * 24L * 60L * 60L) != 0)
                jumpCount++;
            jumpCount --; // backup to time before
            time = new Date(startMillis + (repeatLong * jumpCount * 7L * 24L * 60L * 60L * 1000L));
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.MONTH)) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(getStartTime());
            cal.setLenient(true);
            while(cal.getTime().before(beforeTime) && 
                    (cal.get(java.util.Calendar.YEAR) < YEAR_TO_GIVEUP_SCHEDULING_AT)) {            
                cal.add(java.util.Calendar.MONTH, getRepeatInterval());
            }
            cal.add(java.util.Calendar.MONTH, -getRepeatInterval()); // backup to time before
            time = cal.getTime();
        }
        else if(getRepeatIntervalUnit().equals(IntervalUnit.YEAR)) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(getStartTime());
            cal.setLenient(true);
            while(cal.getTime().before(beforeTime) && 
                    (cal.get(java.util.Calendar.YEAR) < YEAR_TO_GIVEUP_SCHEDULING_AT)) {            
                cal.add(java.util.Calendar.YEAR, getRepeatInterval());
            }
            cal.add(java.util.Calendar.YEAR, -getRepeatInterval()); // backup to time before
            time = cal.getTime();
        }

        if (time.getTime() < startMillis) {
            return startTime;
        }

        return time;
    }

    /**
     * <p>
     * Determines whether or not the <code>DateIntervalTrigger</code> will occur
     * again.
     * </p>
     */
    public boolean mayFireAgain() {
        return (getNextFireTime() != null);
    }

    /**
     * <p>
     * Validates whether the properties of the <code>JobDetail</code> are
     * valid for submission into a <code>Scheduler</code>.
     * 
     * @throws IllegalStateException
     *           if a required property (such as Name, Group, Class) is not
     *           set.
     */
    public void validate() throws SchedulerException {
        super.validate();
        
        if (repeatInterval < 1) {
            throw new SchedulerException("Repeat Interval cannot be zero.",
                    SchedulerException.ERR_CLIENT_ERROR);
        }
    }

    public static void main(String[] args) {

//        DateIntervalTrigger dt = new DateIntervalTrigger("foo", IntervalUnit.SECOND, 30);
//        dt.setStartTime(TriggerUtils.getEvenMinuteDate(new Date()));
        
//        DateIntervalTrigger dt = new DateIntervalTrigger("foo", IntervalUnit.MINUTE, 75);
//        dt.setStartTime(TriggerUtils.getEvenMinuteDate(new Date()));

//        DateIntervalTrigger dt = new DateIntervalTrigger("foo", IntervalUnit.HOUR, 27);
//        dt.setStartTime(TriggerUtils.getEvenMinuteDate(new Date()));

//        DateIntervalTrigger dt = new DateIntervalTrigger("foo", IntervalUnit.DAY, 5);
//        dt.setStartTime(TriggerUtils.getEvenMinuteDate(new Date()));
        
//        DateIntervalTrigger dt = new DateIntervalTrigger("foo", IntervalUnit.MONTH, 5);
//        dt.setStartTime(TriggerUtils.getEvenMinuteDate(new Date()));

        DateIntervalTrigger dt = new DateIntervalTrigger("foo", IntervalUnit.YEAR, 5);
        dt.setStartTime(TriggerUtils.getEvenMinuteDate(new Date()));

        java.util.List times = TriggerUtils.computeFireTimes(dt, null, 25);
        
        for (int i = 0; i < times.size(); i++) {
            System.err.println("firetime = " + times.get(i));
        }
    }
}
