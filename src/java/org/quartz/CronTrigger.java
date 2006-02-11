/* 
 * Copyright 2004-2005 OpenSymphony 
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

/*
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeSet;


/**
 * <p>
 * A concrete <code>{@link Trigger}</code> that is used to fire a <code>{@link org.quartz.JobDetail}</code>
 * at given moments in time, defined with Unix 'cron-like' definitions.
 * </p>
 * 
 * <p>
 * For those unfamiliar with "cron", this means being able to create a firing
 * schedule such as: "At 8:00am every Monday through Friday" or "At 1:30am
 * every last Friday of the month".
 * </p>
 * 
 * <p>
 * A "Cron-Expression" is a string comprised of 6 or 7 fields separated by
 * white space. The 6 mandatory and 1 optional fields are as follows: <br>
 * 
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L W C</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-12 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L C #</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>empty, 1970-2099</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
 * </p>
 * 
 * <p>
 * The '*' character is used to specify all values. For example, "*" in the
 * minute field means "every minute".
 * </p>
 * 
 * <p>
 * The '?' character is allowed for the day-of-month and day-of-week fields. It
 * is used to specify 'no specific value'. This is useful when you need to
 * specify something in one of the two fileds, but not the other. See the
 * examples below for clarification.
 * </p>
 * 
 * <p>
 * The '-' character is used to specify ranges For example "10-12" in the hour
 * field means "the hours 10, 11 and 12".
 * </p>
 * 
 * <p>
 * The ',' character is used to specify additional values. For example
 * "MON,WED,FRI" in the day-of-week field means "the days Monday, Wednesday,
 * and Friday".
 * </p>
 * 
 * <p>
 * The '/' character is used to specify increments. For example "0/15" in the
 * seconds field means "the seconds 0, 15, 30, and 45". And "5/15" in the
 * seconds field means "the seconds 5, 20, 35, and 50".  Specifying '*' before 
 * the  '/' is equivalent to specifying 0 is the value to start with.  
 * Essentially, for each field in the expression, there is a set of numbers 
 * that can be turned on or off. For seconds and minutes, the numbers range 
 * from 0 to 59. For hours 0 to 23, for days of the month 0 to 31, and for 
 * months 1 to 12. The "/" character simply helps you turn on every "nth" 
 * value in the given set. Thus "7/6" in the month field only turns on month 
 * "7", it does NOT mean every 6th month, please note that subtlety.  
 * </p>
 * 
 * <p>
 * The 'L' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for "last", but it has different meaning in
 * each of the two fields. For example, the value "L" in the day-of-month field
 * means "the last day of the month" - day 31 for January, day 28 for February
 * on non-leap years. If used in the day-of-week field by itself, it simply
 * means "7" or "SAT". But if used in the day-of-week field after another
 * value, it means "the last xxx day of the month" - for example "6L" means
 * "the last friday of the month". When using the 'L' option, it is important
 * not to specify lists, or ranges of values, as you'll get confusing results.
 * </p>
 * 
 * <p>
 * The 'W' character is allowed for the day-of-month field.  This character 
 * is used to specify the weekday (Monday-Friday) nearest the given day.  As an 
 * example, if you were to specify "15W" as the value for the day-of-month 
 * field, the meaning is: "the nearest weekday to the 15th of the month".  So
 * if the 15th is a Saturday, the trigger will fire on Friday the 14th.  If the
 * 15th is a Sunday, the trigger will fire on Monday the 16th.  If the 15th is
 * a Tuesday, then it will fire on Tuesday the 15th.  However if you specify
 * "1W" as the value for day-of-month, and the 1st is a Saturday, the trigger
 * will fire on Monday the 3rd, as it will not 'jump' over the boundary of a 
 * month's days.  The 'W' character can only be specified when the day-of-month 
 * is a single day, not a range or list of days.  
 * </p>
 * 
 * <p>
 * The 'L' and 'W' characters can also be combined for the day-of-month 
 * expression to yield 'LW', which translates to "last weekday of the month".
 * </p>
 * 
 * <p>
 * The '#' character is allowed for the day-of-week field. This character is
 * used to specify "the nth" XXX day of the month. For example, the value of
 * "6#3" in the day-of-week field means the third Friday of the month (day 6 =
 * Friday and "#3" = the 3rd one in the month). Other examples: "2#1" = the
 * first Monday of the month and "4#5" = the fifth Wednesday of the month. Note
 * that if you specify "#5" and there is not 5 of the given day-of-week in the
 * month, then no firing will occur that month.
 * </p>
 * 
 * <p>
 * The 'C' character is allowed for the day-of-month and day-of-week fields.
 * This character is short-hand for "calendar". This means values are
 * calculated against the associated calendar, if any. If no calendar is
 * associated, then it is equivalent to having an all-inclusive calendar. A
 * value of "5C" in the day-of-month field means "the first day included by the
 * calendar on or after the 5th". A value of "1C" in the day-of-week field
 * means "the first day included by the calendar on or after sunday".
 * </p>
 * 
 * <p>
 * The legal characters and the names of months and days of the week are not
 * case sensitive.
 * </p>
 * 
 * <p>
 * Here are some full examples: <br><table cellspacing="8">
 * <tr>
 * <th align="left">Expression</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Meaning</th>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 0 12 * * ?"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 12pm (noon) every day</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * *"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 * * ?"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 * * ? *"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am every day</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 * * ? 2005"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am every day during the year 2005</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 * 14 * * ?"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire every minute starting at 2pm and ending at 2:59pm, every day</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 0/5 14 * * ?"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire every 5 minutes starting at 2pm and ending at 2:55pm, every day</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 0/5 14,18 * * ?"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire every 5 minutes starting at 2pm and ending at 2:55pm, AND fire every 5 minutes starting at 6pm and ending at 6:55pm, every day</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 0-5 14 * * ?"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire every minute starting at 2pm and ending at 2:05pm, every day</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 10,44 14 ? 3 WED"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 2:10pm and at 2:44pm every Wednesday in the month of March.</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * MON-FRI"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am every Monday, Tuesday, Wednesday, Thursday and Friday</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 15 * ?"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am on the 15th day of every month</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 L * ?"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am on the last day of every month</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * 6L"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am on the last Friday of every month</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * 6L"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am on the last Friday of every month</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * 6L 2002-2005"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am on every last friday of every month during the years 2002, 2003, 2004 and 2005</code>
 * </td>
 * </tr>
 * <tr>
 * <td align="left"><code>"0 15 10 ? * 6#3"</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>Fire at 10:15am on the third Friday of every month</code>
 * </td>
 * </tr>
 * </table>
 * </p>
 * 
 * <p>
 * Pay attention to the effects of '?' and '*' in the day-of-week and
 * day-of-month fields!
 * </p>
 * 
 * <p>
 * <b>NOTES:</b>
 * <ul>
 * <li>Support for the features described for the 'C' character is not
 * complete.</li>
 * <li>Support for specifying both a day-of-week and a day-of-month value is
 * not complete (you'll need to use the '?' character in on of these fields).
 * </li>
 * <li>Be careful when setting fire times between mid-night and 1:00 AM -
 * "daylight savings" can cause a skip or a repeat depending on whether the
 * time moves back or jumps forward.</li>
 * </ul>
 * </p>
 * 
 * @see Trigger
 * @see SimpleTrigger
 * @see TriggerUtils
 * 
 * @author Sharada Jambula, James House
 * @author Contributions from Mads Henderson
 */
public class CronTrigger extends Trigger {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link CronTrigger}</code> wants to be fired now
     * by <code>Scheduler</code>.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_FIRE_ONCE_NOW = 1;

    /**
     * <p>
     * Instructs the <code>{@link Scheduler}</code> that upon a mis-fire
     * situation, the <code>{@link CronTrigger}</code> wants to have it's
     * next-fire-time updated to the next time in the schedule after the
     * current time (taking into account any associated <code>{@link Calendar}</code>,
     * but it does not want to be fired now.
     * </p>
     */
    public static final int MISFIRE_INSTRUCTION_DO_NOTHING = 2;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected static final int SECOND = 0;

    protected static final int MINUTE = 1;

    protected static final int HOUR = 2;

    protected static final int DAY_OF_MONTH = 3;

    protected static final int MONTH = 4;

    protected static final int DAY_OF_WEEK = 5;

    protected static final int YEAR = 6;

    protected static final int ALL_SPEC_INT = 99; // '*'

    protected static final int NO_SPEC_INT = 98; // '?'

    protected static final Integer ALL_SPEC = new Integer(ALL_SPEC_INT);

    protected static final Integer NO_SPEC = new Integer(NO_SPEC_INT);

    protected static Map monthMap = new HashMap(20);

    protected static Map dayMap = new HashMap(60);

    static {
        monthMap.put("JAN", new Integer(0));
        monthMap.put("FEB", new Integer(1));
        monthMap.put("MAR", new Integer(2));
        monthMap.put("APR", new Integer(3));
        monthMap.put("MAY", new Integer(4));
        monthMap.put("JUN", new Integer(5));
        monthMap.put("JUL", new Integer(6));
        monthMap.put("AUG", new Integer(7));
        monthMap.put("SEP", new Integer(8));
        monthMap.put("OCT", new Integer(9));
        monthMap.put("NOV", new Integer(10));
        monthMap.put("DEC", new Integer(11));

        dayMap.put("SUN", new Integer(1));
        dayMap.put("MON", new Integer(2));
        dayMap.put("TUE", new Integer(3));
        dayMap.put("WED", new Integer(4));
        dayMap.put("THU", new Integer(5));
        dayMap.put("FRI", new Integer(6));
        dayMap.put("SAT", new Integer(7));
    }

    private String cronExpression = null;

    private Date startTime = null;

    private Date endTime = null;

    private Date nextFireTime = null;

    private Date previousFireTime = null;

    private TimeZone timeZone = null;

    protected transient TreeSet seconds;

    protected transient TreeSet minutes;

    protected transient TreeSet hours;

    protected transient TreeSet daysOfMonth;

    protected transient TreeSet months;

    protected transient TreeSet daysOfWeek;

    protected transient TreeSet years;

    protected transient boolean lastdayOfWeek = false;

    protected transient int nthdayOfWeek = 0;

    protected transient boolean lastdayOfMonth = false;

    protected transient boolean nearestWeekday = false;

    protected transient boolean calendardayOfWeek = false;

    protected transient boolean calendardayOfMonth = false;

    protected transient boolean expressionParsed = false;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a <code>CronTrigger</code> with no settings.
     * </p>
     * 
     * <p>
     * The start-time will also be set to the current time, and the time zone
     * will be set the the system's default time zone.
     * </p>
     */
    public CronTrigger() {
        super();
        setStartTime(new Date());
        setTimeZone(TimeZone.getDefault());
    }

    /**
     * <p>
     * Create a <code>CronTrigger</code> with the given name and group.
     * </p>
     * 
     * <p>
     * The start-time will also be set to the current time, and the time zone
     * will be set the the system's default time zone.
     * </p>
     */
    public CronTrigger(String name, String group) {
        super(name, group);
        setStartTime(new Date());
        setTimeZone(TimeZone.getDefault());
    }

    /**
     * <p>
     * Create a <code>CronTrigger</code> with the given name, group and
     * expression.
     * </p>
     * 
     * <p>
     * The start-time will also be set to the current time, and the time zone
     * will be set the the system's default time zone.
     * </p>
     */
    public CronTrigger(String name, String group, String cronExpression)
            throws ParseException {
        super(name, group);

        setCronExpression(cronExpression);

        setStartTime(new Date());
        setTimeZone(TimeZone.getDefault());
    }

    /**
     * <p>
     * Create a <code>CronTrigger</code> with the given name and group, and
     * associated with the identified <code>{@link org.quartz.JobDetail}</code>.
     * </p>
     * 
     * <p>
     * The start-time will also be set to the current time, and the time zone
     * will be set the the system's default time zone.
     * </p>
     */
    public CronTrigger(String name, String group, String jobName,
            String jobGroup) {
        super(name, group, jobName, jobGroup);
        setStartTime(new Date());
        setTimeZone(TimeZone.getDefault());
    }

    /**
     * <p>
     * Create a <code>CronTrigger</code> with the given name and group,
     * associated with the identified <code>{@link org.quartz.JobDetail}</code>,
     * and with the given "cron" expression.
     * </p>
     * 
     * <p>
     * The start-time will also be set to the current time, and the time zone
     * will be set the the system's default time zone.
     * </p>
     */
    public CronTrigger(String name, String group, String jobName,
            String jobGroup, String cronExpression) throws ParseException {
        this(name, group, jobName, jobGroup, null, null, cronExpression,
                TimeZone.getDefault());
    }

    /**
     * <p>
     * Create a <code>CronTrigger</code> with the given name and group,
     * associated with the identified <code>{@link org.quartz.JobDetail}</code>,
     * and with the given "cron" expression resolved with respect to the <code>TimeZone</code>.
     * </p>
     */
    public CronTrigger(String name, String group, String jobName,
            String jobGroup, String cronExpression, TimeZone timeZone)
            throws ParseException {
        this(name, group, jobName, jobGroup, null, null, cronExpression,
                timeZone);
    }

    /**
     * <p>
     * Create a <code>CronTrigger</code> that will occur at the given time,
     * until the given end time.
     * </p>
     * 
     * <p>
     * If null, the start-time will also be set to the current time, the time
     * zone will be set the the system's default.
     * </p>
     * 
     * @param startTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to fire.
     * @param endTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to quit repeat firing.
     */
    public CronTrigger(String name, String group, String jobName,
            String jobGroup, Date startTime, Date endTime, String cronExpression)
            throws ParseException {
        super(name, group, jobName, jobGroup);

        setCronExpression(cronExpression);

        if (startTime == null) startTime = new Date();
        setStartTime(startTime);
        if (endTime != null) setEndTime(endTime);
        setTimeZone(TimeZone.getDefault());

    }

    /**
     * <p>
     * Create a <code>CronTrigger</code> with fire time dictated by the
     * <code>cronExpression</code> resolved with respect to the specified
     * <code>timeZone</code> occuring from the <code>startTime</code> until
     * the given <code>endTime</code>.
     * </p>
     * 
     * <p>
     * If null, the start-time will also be set to the current time. If null,
     * the time zone will be set to the system's default.
     * </p>
     * 
     * @param name
     *          of the <code>Trigger</code>
     * @param group
     *          of the <code>Trigger</code>
     * @param jobName,
     *          name of the <code>{@link org.quartz.JobDetail}</code>
     *          executed on firetime
     * @param jobGroup,
     *          group of the <code>{@link org.quartz.JobDetail}</code>
     *          executed on firetime
     * @param startTime
     *          A <code>Date</code> set to the earliest time for the <code>Trigger</code>
     *          to start firing.
     * @param endTime
     *          A <code>Date</code> set to the time for the <code>Trigger</code>
     *          to quit repeat firing.
     * @param cronExpression,
     *          A cron expression dictating the firing sequence of the <code>Trigger</code>
     * @param timeZone,
     *          Specifies for which time zone the <code>cronExpression</code>
     *          should be interprted, i.e. the expression 0 0 10 * * ?, is
     *          resolved to 10:00 am in this time zone.
     * @throws ParseException
     *           if the <code>cronExpression</code> is invalid.
     */
    public CronTrigger(String name, String group, String jobName,
            String jobGroup, Date startTime, Date endTime,
            String cronExpression, TimeZone timeZone) throws ParseException {
        super(name, group, jobName, jobGroup);

        setCronExpression(cronExpression);

        if (startTime == null) startTime = new Date();
        setStartTime(startTime);
        if (endTime != null) setEndTime(endTime);
        if (timeZone == null) {
            setTimeZone(TimeZone.getDefault());
        } else {
            setTimeZone(timeZone);
        }
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private void readObject(java.io.ObjectInputStream stream)
            throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        try {
            buildExpression(cronExpression);
        } catch (Exception ignore) {
        } // never happens
    }

    public void setCronExpression(String cronExpression) throws ParseException {
        if (cronExpression == null)
                throw new IllegalArgumentException(
                        "Cron time expression cannot be null");

        // Clear out values from last expression...
        nextFireTime = null;
        seconds = null;
        minutes = null;
        hours = null;
        daysOfMonth = null;
        months = null;
        daysOfWeek = null;
        years = null;
        lastdayOfWeek = false;
        nthdayOfWeek = 0;
        lastdayOfMonth = false;
        calendardayOfWeek = false;
        calendardayOfMonth = false;

        buildExpression(cronExpression.toUpperCase(Locale.US));
        
        this.cronExpression = cronExpression;
    }

    public String getCronExpression() {
        return this.cronExpression;
    }

    /**
     * <p>
     * Get the time at which the <code>CronTrigger</code> should occur.
     * </p>
     */
    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        if (startTime == null)
                throw new IllegalArgumentException("Start time cannot be null");

        Date eTime = getEndTime();
        if (eTime != null && startTime != null && eTime.before(startTime))
            throw new IllegalArgumentException(
            "End time cannot be before start time");
        
        // round off millisecond...
        // Note timeZone is not needed here as parameter for
        // Calendar.getInstance(),
        // since time zone is implicit when using a Date in the setTime method.
        Calendar cl = Calendar.getInstance();
        cl.setTime(startTime);
        cl.set(Calendar.MILLISECOND, 0);

        this.startTime = cl.getTime();
    }

    /**
     * <p>
     * Get the time at which the <code>CronTrigger</code> should quit
     * repeating - even if repeastCount isn't yet satisfied.
     * </p>
     * 
     * @see #getFinalFireTime()
     */
    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        Date sTime = getStartTime();
        if (sTime != null && endTime != null && sTime.after(endTime))
                throw new IllegalArgumentException(
                        "End time cannot be before start time");

        this.endTime = endTime;
    }

    /**
     * <p>
     * Returns the next time at which the <code>CronTrigger</code> will fire.
     * If the trigger will not fire again, <code>null</code> will be
     * returned. The value returned is not guaranteed to be valid until after
     * the <code>Trigger</code> has been added to the scheduler.
     * </p>
     */
    public Date getNextFireTime() {
        return this.nextFireTime;
    }

    /**
     * <p>
     * Returns the previous time at which the <code>CronTrigger</code> will
     * fire. If the trigger has not yet fired, <code>null</code> will be
     * returned.
     */
    public Date getPreviousFireTime() {
        return this.previousFireTime;
    }

    /**
     * <p>
     * Sets the next time at which the <code>CronTrigger</code> will fire.
     * <b>This method should not be invoked by client code.</b>
     * </p>
     */
    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    /**
     * <p>
     * Set the previous time at which the <code>SimpleTrigger</code> fired.
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
     * Returns the time zone for which the <code>cronExpression</code> of
     * this <code>CronTrigger</code> will be resolved.
     * </p>
     */
    public TimeZone getTimeZone() {
        if (timeZone == null) timeZone = TimeZone.getDefault();

        return timeZone;
    }

    /**
     * <p>
     * Sets the time zone for which the <code>cronExpression</code> of this
     * <code>CronTrigger</code> will be resolved.
     * </p>
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * <p>
     * Returns the next time at which the <code>CronTrigger</code> will fire,
     * after the given time. If the trigger will not fire after the given time,
     * <code>null</code> will be returned.
     * </p>
     * 
     * <p>
     * Note that the date returned is NOT validated against the related
     * org.quartz.Calendar (if any)
     * </p>
     */
    public Date getFireTimeAfter(Date afterTime) {
        if (afterTime == null) afterTime = new Date();

        if (startTime.after(afterTime))
                afterTime = new Date(startTime.getTime() - 1000l);

        Date pot = getTimeAfter(afterTime);
        if (endTime != null && pot != null && pot.after(endTime)) return null;

        return pot;
    }

    /**
     * <p>
     * Returns the final time at which the <code>CronTrigger</code> will
     * fire.
     * </p>
     * 
     * <p>
     * Note that the return time *may* be in the past. and the date returned is
     * not validated against org.quartz.calendar
     * </p>
     */
    public Date getFinalFireTime() {
        if (this.endTime != null) return getTimeBefore(this.endTime);
        else
            return null;
    }

    /**
     * <p>
     * Determines whether or not the <code>CronTrigger</code> will occur
     * again.
     * </p>
     */
    public boolean mayFireAgain() {
        return (getNextFireTime() != null);
    }

    protected boolean validateMisfireInstruction(int misfireInstruction) {
        if (misfireInstruction < MISFIRE_INSTRUCTION_SMART_POLICY)
                return false;

        if (misfireInstruction > MISFIRE_INSTRUCTION_DO_NOTHING) return false;

        return true;
    }

    /**
     * <p>
     * Updates the <code>CronTrigger</code>'s state based on the
     * MISFIRE_INSTRUCTION_XXX that was selected when the <code>SimpleTrigger</code>
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
    public void updateAfterMisfire(org.quartz.Calendar cal) {
        int instr = getMisfireInstruction();

        if (instr == MISFIRE_INSTRUCTION_SMART_POLICY)
                instr = MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;

        if (instr == MISFIRE_INSTRUCTION_DO_NOTHING) {
            Date newFireTime = getFireTimeAfter(new Date());
            while (newFireTime != null && cal != null
                    && !cal.isTimeIncluded(newFireTime.getTime())) {
                newFireTime = getFireTimeAfter(newFireTime);
            }
            setNextFireTime(newFireTime);
        } else if (instr == MISFIRE_INSTRUCTION_FIRE_ONCE_NOW) {
            setNextFireTime(new Date());
        }
    }

    /**
     * <p>
     * Determines whether the date and (optionally) time of the given Calendar 
     * instance falls on a scheduled fire-time of this trigger.
     * </p>
     * 
     * <p>
     * Equivalent to calling <code>willFireOn(cal, false)</code>.
     * </p>
     * 
     * @param test the date to compare
     * 
     * @see #willFireOn(Calendar, boolean)
     */
    public boolean willFireOn(Calendar test) {
        return willFireOn(test, false);
    }
    
    /**
     * <p>
     * Determines whether the date and (optionally) time of the given Calendar 
     * instance falls on a scheduled fire-time of this trigger.
     * </p>
     * 
     * <p>
     * Note that the value returned is NOT validated against the related
     * org.quartz.Calendar (if any)
     * </p>
     * 
     * @param test the date to compare
     * @param dayOnly if set to true, the method will only determine if the
     * trigger will fire during the day represented by the given Calendar
     * (hours, minutes and seconds will be ignored).
     * @see #willFireOn(Calendar)
     */
    public boolean willFireOn(Calendar test, boolean dayOnly) {

    	test = (Calendar) test.clone();
    	
        test.set(Calendar.MILLISECOND, 0); // don't compare millis.
        
        if(dayOnly) {
            test.set(Calendar.HOUR, 0); 
            test.set(Calendar.MINUTE, 0); 
            test.set(Calendar.SECOND, 0); 
        }
        
        Date testTime = test.getTime();
        
        Date fta = getFireTimeAfter(new Date(test.getTime().getTime() - 1000));

        Calendar p = Calendar.getInstance(test.getTimeZone());
        p.setTime(fta);
        
        int year = p.get(Calendar.YEAR);
        int month = p.get(Calendar.MONTH);
        int day = p.get(Calendar.DATE);
        
        if(dayOnly) {
            return (year == test.get(Calendar.YEAR) 
                    && month == test.get(Calendar.MONTH) 
                    && day == test.get(Calendar.DATE));
        }
        
        while(fta.before(testTime)) {
            fta = getFireTimeAfter(fta);
        }
        
        if(fta.equals(testTime))
            return true;

        return false;
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
    public int executionComplete(JobExecutionContext context,
            JobExecutionException result) {
        if (result != null && result.refireImmediately())
                return INSTRUCTION_RE_EXECUTE_JOB;

        if (result != null && result.unscheduleFiringTrigger())
                return INSTRUCTION_SET_TRIGGER_COMPLETE;

        if (result != null && result.unscheduleAllTriggers())
                return INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE;

        if (!mayFireAgain()) return INSTRUCTION_DELETE_TRIGGER;

        return INSTRUCTION_NOOP;
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
    public void triggered(org.quartz.Calendar calendar) {
        previousFireTime = nextFireTime;
        nextFireTime = getFireTimeAfter(nextFireTime);

        while (nextFireTime != null && calendar != null
                && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime);
        }
    }

    /**
     *  
     * @see org.quartz.Trigger#updateWithNewCalendar(org.quartz.Calendar, long)
     */
    public void updateWithNewCalendar(org.quartz.Calendar calendar, long misfireThreshold)
    {
        nextFireTime = getFireTimeAfter(previousFireTime);
        
        Date now = new Date();
        do {
            while (nextFireTime != null && calendar != null
                    && !calendar.isTimeIncluded(nextFireTime.getTime())) {
                nextFireTime = getFireTimeAfter(nextFireTime);
            }
            
            if(nextFireTime != null && nextFireTime.before(now)) {
                long diff = now.getTime() - nextFireTime.getTime();
                if(diff >= misfireThreshold) {
                    nextFireTime = getFireTimeAfter(nextFireTime);
                    continue;
                }
            }
        }while(false);
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
    public Date computeFirstFireTime(org.quartz.Calendar calendar) {
        nextFireTime = getFireTimeAfter(new Date(startTime.getTime() - 1000l));

        while (nextFireTime != null && calendar != null
                && !calendar.isTimeIncluded(nextFireTime.getTime())) {
            nextFireTime = getFireTimeAfter(nextFireTime);
        }

        return nextFireTime;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Expression Parsing Functions
    //
    ////////////////////////////////////////////////////////////////////////////

    protected void buildExpression(String expression) throws ParseException {
        expressionParsed = true;

        try {

            if (seconds == null) seconds = new TreeSet();
            if (minutes == null) minutes = new TreeSet();
            if (hours == null) hours = new TreeSet();
            if (daysOfMonth == null) daysOfMonth = new TreeSet();
            if (months == null) months = new TreeSet();
            if (daysOfWeek == null) daysOfWeek = new TreeSet();
            if (years == null) years = new TreeSet();

            int exprOn = SECOND;

            StringTokenizer exprsTok = new StringTokenizer(expression, " \t",
                    false);

            while (exprsTok.hasMoreTokens() && exprOn <= YEAR) {
                String expr = exprsTok.nextToken().trim();
                StringTokenizer vTok = new StringTokenizer(expr, ",");
                while (vTok.hasMoreTokens()) {
                    String v = vTok.nextToken();
                    storeExpressionVals(0, v, exprOn);
                }

                exprOn++;
            }

            if (exprOn <= DAY_OF_WEEK)
                    throw new ParseException("Unexpected end of expression.",
                            expression.length());

            if (exprOn <= YEAR) storeExpressionVals(0, "*", YEAR);

        } catch (ParseException pe) {
            throw pe;
        } catch (Exception e) {
            throw new ParseException("Illegal cron expression format ("
                    + e.toString() + ")", 0);
        }
    }

    protected int storeExpressionVals(int pos, String s, int type)
            throws ParseException {
        int incr = 0;
        int i = skipWhiteSpace(pos, s);
        if (i >= s.length()) return i;
        char c = s.charAt(i);
        if ((c >= 'A') && (c <= 'Z') && (!s.equals("L")) && (!s.equals("LW"))) {
            String sub = s.substring(i, i + 3);
            int sval = -1;
            int eval = -1;
            if (type == MONTH) {
                sval = getMonthNumber(sub) + 1;
                if (sval < 0)
                        throw new ParseException("Invalid Month value: '" + sub
                                + "'", i);
                if (s.length() > i + 3) {
                    c = s.charAt(i + 3);
                    if (c == '-') {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getMonthNumber(sub) + 1;
                        if (eval < 0)
                                throw new ParseException(
                                        "Invalid Month value: '" + sub + "'", i);
                    }
                }
            } else if (type == DAY_OF_WEEK) {
                sval = getDayOfWeekNumber(sub);
                if (sval < 0)
                        throw new ParseException("Invalid Day-of-Week value: '"
                                + sub + "'", i);
                if (s.length() > i + 3) {
                    c = s.charAt(i + 3);
                    if (c == '-') {
                        i += 4;
                        sub = s.substring(i, i + 3);
                        eval = getDayOfWeekNumber(sub);
                        if (eval < 0)
                            throw new ParseException(
                                    "Invalid Day-of-Week value: '" + sub
                                        + "'", i);
                        if (sval > eval)
                            throw new ParseException(
                                    "Invalid Day-of-Week sequence: " + sval 
                                        + " > " + eval, i);
                        
                    } else if (c == '#') {
                        try {
                            i += 4;
                            nthdayOfWeek = Integer.parseInt(s.substring(i));
                            if (nthdayOfWeek < 1 || nthdayOfWeek > 5)
                                    throw new Exception();
                        } catch (Exception e) {
                            throw new ParseException(
                                    "A numeric value between 1 and 5 must follow the '#' option",
                                    i);
                        }
                    } else if (c == 'L') {
                        lastdayOfWeek = true;
                        i++;
                    }
                }

            } else {
                throw new ParseException(
                        "Illegal characters for this position: '" + sub + "'",
                        i);
            }
            if (eval != -1) incr = 1;
            addToSet(sval, eval, incr, type);
            return (i + 3);
        }

        if (c == '?') {
            i++;
            if ((i + 1) < s.length()
                    && (s.charAt(i) != ' ' && s.charAt(i + 1) != '\t'))
                    throw new ParseException("Illegal character after '?': "
                            + s.charAt(i), i);
            if (type != DAY_OF_WEEK && type != DAY_OF_MONTH)
                    throw new ParseException(
                            "'?' can only be specfied for Day-of-Month or Day-of-Week.",
                            i);
            if (type == DAY_OF_WEEK && !lastdayOfMonth) {
                int val = ((Integer) daysOfMonth.last()).intValue();
                if (val == NO_SPEC_INT)
                        throw new ParseException(
                                "'?' can only be specfied for Day-of-Month -OR- Day-of-Week.",
                                i);
            }

            addToSet(NO_SPEC_INT, -1, 0, type);
            return i;
        }

        if (c == '*' || c == '/') {
            if (c == '*' && (i + 1) >= s.length()) {
                addToSet(ALL_SPEC_INT, -1, incr, type);
                return i + 1;
            } else if (c == '/'
                    && ((i + 1) >= s.length() || s.charAt(i + 1) == ' ' || s
                            .charAt(i + 1) == '\t')) throw new ParseException(
                    "'/' must be followed by an integer.", i);
            else if (c == '*') i++;
            c = s.charAt(i);
            if (c == '/') { // is an increment specified?
                i++;
                if (i >= s.length())
                        throw new ParseException("Unexpected end of string.", i);

                incr = getNumericValue(s, i);

                i++;
                if (incr > 10) i++;
                if (incr > 59 && (type == SECOND || type == MINUTE)) throw new ParseException(
                        "Increment > 60 : " + incr, i);
                else if (incr > 23 && (type == HOUR)) throw new ParseException(
                        "Increment > 24 : " + incr, i);
                else if (incr > 31 && (type == DAY_OF_MONTH)) throw new ParseException(
                        "Increment > 31 : " + incr, i);
                else if (incr > 7 && (type == DAY_OF_WEEK)) throw new ParseException(
                        "Increment > 7 : " + incr, i);
                else if (incr > 12 && (type == MONTH))
                        throw new ParseException("Increment > 12 : " + incr, i);
            } else
                incr = 1;

            addToSet(ALL_SPEC_INT, -1, incr, type);
            return i;
        } else if (c == 'L') {
            i++;
            if (type == DAY_OF_MONTH) lastdayOfMonth = true;
            if (type == DAY_OF_WEEK) addToSet(7, 7, 0, type);
            if(type == DAY_OF_MONTH && s.length() > i) {
                c = s.charAt(i);
                if(c == 'W') {
                    nearestWeekday = true;
                    i++;
                }
            }
            return i;
        } else if (c >= '0' && c <= '9') {
            int val = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) addToSet(val, -1, -1, type);
            else {
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(val, s, i);
                    val = vs.value;
                    i = vs.pos;
                }
                i = checkNext(i, s, val, type);
                return i;
            }
        } else
            throw new ParseException("Unexpected character: " + c, i);

        return i;
    }

    protected int checkNext(int pos, String s, int val, int type)
            throws ParseException {
        int end = -1;
        int i = pos;

        if (i >= s.length()) {
            addToSet(val, end, -1, type);
            return i;
        }

        char c = s.charAt(pos);

        if (c == 'L') {
            if (type == DAY_OF_WEEK) lastdayOfWeek = true;
            else
                throw new ParseException("'L' option is not valid here. (pos="
                        + i + ")", i);
            TreeSet set = getSet(type);
            set.add(new Integer(val));
            i++;
            return i;
        }
        
        if (c == 'W') {
            if(type == DAY_OF_MONTH) nearestWeekday = true;
            else
                throw new ParseException("'W' option is not valid here. (pos="
                        + i + ")", i);
            TreeSet set = getSet(type);
            set.add(new Integer(val));
            i++;
            return i;
        }

        if (c == '#') {
            if (type != DAY_OF_WEEK)
                    throw new ParseException(
                            "'#' option is not valid here. (pos=" + i + ")", i);
            i++;
            try {
                nthdayOfWeek = Integer.parseInt(s.substring(i));
                if (nthdayOfWeek < 1 || nthdayOfWeek > 5)
                        throw new Exception();
            } catch (Exception e) {
                throw new ParseException(
                        "A numeric value between 1 and 5 must follow the '#' option",
                        i);
            }

            TreeSet set = getSet(type);
            set.add(new Integer(val));
            i++;
            return i;
        }

        if (c == 'C') {
            if (type == DAY_OF_WEEK) calendardayOfWeek = true;
            else if (type == DAY_OF_MONTH) calendardayOfMonth = true;
            else
                throw new ParseException("'C' option is not valid here. (pos="
                        + i + ")", i);
            TreeSet set = getSet(type);
            set.add(new Integer(val));
            i++;
            return i;
        }

        if (c == '-') {
            i++;
            c = s.charAt(i);
            int v = Integer.parseInt(String.valueOf(c));
            end = v;
            i++;
            if (i >= s.length()) {
                addToSet(val, end, 1, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v, s, i);
                int v1 = vs.value;
                end = v1;
                i = vs.pos;
            }
            if (i < s.length() && ((c = s.charAt(i)) == '/')) {
                i++;
                c = s.charAt(i);
                int v2 = Integer.parseInt(String.valueOf(c));
                i++;
                if (i >= s.length()) {
                    addToSet(val, end, v2, type);
                    return i;
                }
                c = s.charAt(i);
                if (c >= '0' && c <= '9') {
                    ValueSet vs = getValue(v2, s, i);
                    int v3 = vs.value;
                    addToSet(val, end, v3, type);
                    i = vs.pos;
                    return i;
                } else {
                    addToSet(val, end, v2, type);
                    return i;
                }
            } else {
                addToSet(val, end, 1, type);
                return i;
            }
        }

        if (c == '/') {
            i++;
            c = s.charAt(i);
            int v2 = Integer.parseInt(String.valueOf(c));
            i++;
            if (i >= s.length()) {
                addToSet(val, end, v2, type);
                return i;
            }
            c = s.charAt(i);
            if (c >= '0' && c <= '9') {
                ValueSet vs = getValue(v2, s, i);
                int v3 = vs.value;
                addToSet(val, end, v3, type);
                i = vs.pos;
                return i;
            } else
                throw new ParseException("Unexpected character '" + c
                        + "' after '/'", i);
        }

        addToSet(val, end, 0, type);
        i++;
        return i;
    }

    public String getExpressionSummary() {
        StringBuffer buf = new StringBuffer();

        buf.append("seconds: ");
        buf.append(getExpressionSetSummary(seconds));
        buf.append("\n");
        buf.append("minutes: ");
        buf.append(getExpressionSetSummary(minutes));
        buf.append("\n");
        buf.append("hours: ");
        buf.append(getExpressionSetSummary(hours));
        buf.append("\n");
        buf.append("daysOfMonth: ");
        buf.append(getExpressionSetSummary(daysOfMonth));
        buf.append("\n");
        buf.append("months: ");
        buf.append(getExpressionSetSummary(months));
        buf.append("\n");
        buf.append("daysOfWeek: ");
        buf.append(getExpressionSetSummary(daysOfWeek));
        buf.append("\n");
        buf.append("lastdayOfWeek: ");
        buf.append(lastdayOfWeek);
        buf.append("\n");
        buf.append("nearestWeekday: ");
        buf.append(nearestWeekday);
        buf.append("\n");
        buf.append("NthDayOfWeek: ");
        buf.append(nthdayOfWeek);
        buf.append("\n");
        buf.append("lastdayOfMonth: ");
        buf.append(lastdayOfMonth);
        buf.append("\n");
        buf.append("calendardayOfWeek: ");
        buf.append(calendardayOfWeek);
        buf.append("\n");
        buf.append("calendardayOfMonth: ");
        buf.append(calendardayOfMonth);
        buf.append("\n");
        buf.append("years: ");
        buf.append(getExpressionSetSummary(years));
        buf.append("\n");

        return buf.toString();
    }

    protected String getExpressionSetSummary(java.util.Set set) {

        if (set.contains(NO_SPEC)) return "?";
        if (set.contains(ALL_SPEC)) return "*";

        StringBuffer buf = new StringBuffer();

        Iterator itr = set.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = (Integer) itr.next();
            String val = iVal.toString();
            if (!first) buf.append(",");
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    protected String getExpressionSetSummary(java.util.ArrayList list) {

        if (list.contains(NO_SPEC)) return "?";
        if (list.contains(ALL_SPEC)) return "*";

        StringBuffer buf = new StringBuffer();

        Iterator itr = list.iterator();
        boolean first = true;
        while (itr.hasNext()) {
            Integer iVal = (Integer) itr.next();
            String val = iVal.toString();
            if (!first) buf.append(",");
            buf.append(val);
            first = false;
        }

        return buf.toString();
    }

    protected int skipWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t'); i++)
            ;

        return i;
    }

    protected int findNextWhiteSpace(int i, String s) {
        for (; i < s.length() && (s.charAt(i) != ' ' || s.charAt(i) != '\t'); i++)
            ;

        return i;
    }

    protected void addToSet(int val, int end, int incr, int type)
            throws ParseException {
        TreeSet set = getSet(type);

        if (type == SECOND || type == MINUTE) {
            if ((val < 0 || val > 59 || end > 59) && (val != ALL_SPEC_INT))
                    throw new ParseException(
                            "Minute and Second values must be between 0 and 59",
                            -1);
        } else if (type == HOUR) {
            if ((val < 0 || val > 23 || end > 23) && (val != ALL_SPEC_INT))
                    throw new ParseException(
                            "Hour values must be between 0 and 23", -1);
        } else if (type == DAY_OF_MONTH) {
            if ((val < 1 || val > 31 || end > 31) && (val != ALL_SPEC_INT)
                    && (val != NO_SPEC_INT))
                    throw new ParseException(
                            "Day of month values must be between 1 and 31", -1);
        } else if (type == MONTH) {
            if ((val < 1 || val > 12 || end > 12) && (val != ALL_SPEC_INT))
                    throw new ParseException(
                            "Month values must be between 1 and 12", -1);
        } else if (type == DAY_OF_WEEK) {
            if ((val == 0 || val > 7 || end > 7) && (val != ALL_SPEC_INT)
                    && (val != NO_SPEC_INT))
                    throw new ParseException(
                            "Day-of-Week values must be between 1 and 7", -1);
        }

        if ((incr == 0 || incr == -1) && val != ALL_SPEC_INT) {
            if (val != -1) set.add(new Integer(val));
            else
                set.add(NO_SPEC);
            return;
        }

        int startAt = val;
        int stopAt = end;

        if (val == ALL_SPEC_INT && incr <= 0) {
            incr = 1;
            set.add(ALL_SPEC); // put in a marker, but also fill values
        }

        if (type == SECOND || type == MINUTE) {
            if (stopAt == -1) stopAt = 59;
            if (startAt == -1 || startAt == ALL_SPEC_INT) startAt = 0;
        } else if (type == HOUR) {
            if (stopAt == -1) stopAt = 23;
            if (startAt == -1 || startAt == ALL_SPEC_INT) startAt = 0;
        } else if (type == DAY_OF_MONTH) {
            if (stopAt == -1) stopAt = 31;
            if (startAt == -1 || startAt == ALL_SPEC_INT) startAt = 1;
        } else if (type == MONTH) {
            if (stopAt == -1) stopAt = 12;
            if (startAt == -1 || startAt == ALL_SPEC_INT) startAt = 1;
        } else if (type == DAY_OF_WEEK) {
            if (stopAt == -1) stopAt = 7;
            if (startAt == -1 || startAt == ALL_SPEC_INT) startAt = 1;
        } else if (type == YEAR) {
            if (stopAt == -1) stopAt = 2099;
            if (startAt == -1 || startAt == ALL_SPEC_INT) startAt = 1970;
        }

        for (int i = startAt; i <= stopAt; i += incr)
            set.add(new Integer(i));
    }

    protected TreeSet getSet(int type) {
        switch (type) {
        case SECOND:
            return seconds;
        case MINUTE:
            return minutes;
        case HOUR:
            return hours;
        case DAY_OF_MONTH:
            return daysOfMonth;
        case MONTH:
            return months;
        case DAY_OF_WEEK:
            return daysOfWeek;
        case YEAR:
            return years;
        default:
            return null;
        }
    }

    protected ValueSet getValue(int v, String s, int i) {
        char c = s.charAt(i);
        String s1 = String.valueOf(v);
        while (c >= '0' && c <= '9') {
            s1 += c;
            i++;
            if (i >= s.length()) break;
            c = s.charAt(i);
        }
        ValueSet val = new ValueSet();
        if (i < s.length()) val.pos = i;
        else
            val.pos = i + 1;
        val.value = Integer.parseInt(s1);
        return val;
    }

    protected int getNumericValue(String s, int i) {
        int endOfVal = findNextWhiteSpace(i, s);
        String val = s.substring(i, endOfVal);
        return Integer.parseInt(val);
    }

    protected int getMonthNumber(String s) {
        Integer integer = (Integer) monthMap.get(s);

        if (integer == null) return -1;

        return integer.intValue();
    }

    protected int getDayOfWeekNumber(String s) {
        Integer integer = (Integer) dayMap.get(s);

        if (integer == null) return -1;

        return integer.intValue();
    }

    protected Date getTime(int sc, int mn, int hr, int dayofmn, int mon) {
        try {
            Calendar cl = Calendar.getInstance(getTimeZone());
            //cl.add(Calendar.DAY_OF_MONTH,);
            if (hr >= 0 && hr <= 12) cl.set(Calendar.AM_PM, Calendar.AM);
            if (hr >= 13 && hr <= 23) cl.set(Calendar.AM_PM, Calendar.PM);
            cl.setLenient(false);
            if (sc != -1) cl.set(Calendar.SECOND, sc);
            if (mn != -1) cl.set(Calendar.MINUTE, mn);
            if (hr != -1) cl.set(Calendar.HOUR_OF_DAY, hr);
            if (dayofmn != -1) cl.set(Calendar.DAY_OF_MONTH, dayofmn);
            if (mon != -1) cl.set(Calendar.MONTH, mon);
            return cl.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Computation Functions
    //
    ////////////////////////////////////////////////////////////////////////////

    protected Date getTimeAfter(Date afterTime) {

        Calendar cl = Calendar.getInstance(getTimeZone());

        // move ahead one second, since we're computing the time *after* the
        // given time
        afterTime = new Date(afterTime.getTime() + 1000);
        // CronTrigger does not deal with milliseconds
        cl.setTime(afterTime);
        cl.set(Calendar.MILLISECOND, 0);

        boolean gotOne = false;
        // loop until we've computed the next time, or we've past the endTime
        while (!gotOne) {

            if (endTime != null && cl.getTime().after(endTime)) return null;

            SortedSet st = null;
            int t = 0;

            int sec = cl.get(Calendar.SECOND);
            int min = cl.get(Calendar.MINUTE);

            // get second.................................................
            st = seconds.tailSet(new Integer(sec));
            if (st != null && st.size() != 0) {
                sec = ((Integer) st.first()).intValue();
            } else {
                sec = ((Integer) seconds.first()).intValue();
                min++;
                cl.set(Calendar.MINUTE, min);
            }
            cl.set(Calendar.SECOND, sec);

            min = cl.get(Calendar.MINUTE);
            int hr = cl.get(Calendar.HOUR_OF_DAY);
            t = -1;

            // get minute.................................................
            st = minutes.tailSet(new Integer(min));
            if (st != null && st.size() != 0) {
                t = min;
                min = ((Integer) st.first()).intValue();
            } else {
                min = ((Integer) minutes.first()).intValue();
                hr++;
            }
            if (min != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, min);
                setCalendarHour(cl, hr);
                continue;
            }
            cl.set(Calendar.MINUTE, min);

            hr = cl.get(Calendar.HOUR_OF_DAY);
            int day = cl.get(Calendar.DAY_OF_MONTH);
            t = -1;

            // get hour...................................................
            st = hours.tailSet(new Integer(hr));
            if (st != null && st.size() != 0) {
                t = hr;
                hr = ((Integer) st.first()).intValue();
            } else {
                hr = ((Integer) hours.first()).intValue();
                day++;
            }
            if (hr != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.DAY_OF_MONTH, day);
                setCalendarHour(cl, hr);
                continue;
            }
            cl.set(Calendar.HOUR_OF_DAY, hr);

            day = cl.get(Calendar.DAY_OF_MONTH);
            int mon = cl.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            t = -1;
            int tmon = mon;
            
            // get day...................................................
            boolean dayOfMSpec = !daysOfMonth.contains(NO_SPEC);
            boolean dayOfWSpec = !daysOfWeek.contains(NO_SPEC);
            if (dayOfMSpec && !dayOfWSpec) { // get day by day of month rule
                st = daysOfMonth.tailSet(new Integer(day));
                if (lastdayOfMonth) {
                    if(!nearestWeekday) {
                        t = day;
                        day = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    }
                    else {
                        t = day;
                        day = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        
                        java.util.Calendar tcal = java.util.Calendar.getInstance();
                        tcal.set(Calendar.SECOND, 0);
                        tcal.set(Calendar.MINUTE, 0);
                        tcal.set(Calendar.HOUR_OF_DAY, 0);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));
                        
                        int ldom = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                        int dow = tcal.get(Calendar.DAY_OF_WEEK);

                        if(dow == Calendar.SATURDAY && day == 1)
                            day += 2;
                        else if(dow == Calendar.SATURDAY)
                            day -= 1;
                        else if(dow == Calendar.SUNDAY && day == ldom) 
                            day -= 2;
                        else if(dow == Calendar.SUNDAY) 
                            day += 1;
                    
                        tcal.set(Calendar.SECOND, sec);
                        tcal.set(Calendar.MINUTE, min);
                        tcal.set(Calendar.HOUR_OF_DAY, hr);
                        tcal.set(Calendar.DAY_OF_MONTH, day);
                        tcal.set(Calendar.MONTH, mon - 1);
                        Date nTime = tcal.getTime();
                        if(nTime.before(afterTime)) {
                            day = 1;
                            mon++;
                        }
                    }
                } else if(nearestWeekday) {
                    t = day;
                    day = ((Integer) daysOfMonth.first()).intValue();

                    java.util.Calendar tcal = java.util.Calendar.getInstance();
                    tcal.set(Calendar.SECOND, 0);
                    tcal.set(Calendar.MINUTE, 0);
                    tcal.set(Calendar.HOUR_OF_DAY, 0);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    tcal.set(Calendar.YEAR, cl.get(Calendar.YEAR));
                    
                    int ldom = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));
                    int dow = tcal.get(Calendar.DAY_OF_WEEK);

                    if(dow == Calendar.SATURDAY && day == 1)
                        day += 2;
                    else if(dow == Calendar.SATURDAY)
                        day -= 1;
                    else if(dow == Calendar.SUNDAY && day == ldom) 
                        day -= 2;
                    else if(dow == Calendar.SUNDAY) 
                        day += 1;
                        
                
                    tcal.set(Calendar.SECOND, sec);
                    tcal.set(Calendar.MINUTE, min);
                    tcal.set(Calendar.HOUR_OF_DAY, hr);
                    tcal.set(Calendar.DAY_OF_MONTH, day);
                    tcal.set(Calendar.MONTH, mon - 1);
                    Date nTime = tcal.getTime();
                    if(nTime.before(afterTime)) {
                        day = ((Integer) daysOfMonth.first()).intValue();;
                        mon++;
                    }
                }
                else if (st != null && st.size() != 0) {
                    t = day;
                    day = ((Integer) st.first()).intValue();
                } else {
                    day = ((Integer) daysOfMonth.first()).intValue();
                    mon++;
                }
                
                if (day != t || mon != tmon) {
                    cl.set(Calendar.SECOND, 0);
                    cl.set(Calendar.MINUTE, 0);
                    cl.set(Calendar.HOUR_OF_DAY, 0);
                    cl.set(Calendar.DAY_OF_MONTH, day);
                    cl.set(Calendar.MONTH, mon - 1);
                    // '- 1' because calendar is 0-based for this field, and we
                    // are 1-based
                    continue;
                }
            } else if (dayOfWSpec && !dayOfMSpec) { // get day by day of week rule
                if (lastdayOfWeek) { // are we looking for the last XXX day of
                    // the month?
                    int dow = ((Integer) daysOfWeek.first()).intValue(); // desired
                    // d-o-w
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) daysToAdd = dow - cDow;
                    if (cDow > dow) daysToAdd = dow + (7 - cDow);

                    int lDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // did we already miss the
                        // last one?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    }

                    // find date of last occurance of this day in this month...
                    while ((day + daysToAdd + 7) <= lDay)
                        daysToAdd += 7;

                    day += daysToAdd;

                    if (daysToAdd > 0) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are not promoting the month
                        continue;
                    }

                } else if (nthdayOfWeek != 0) {
                    // are we looking for the Nth XXX day in the month?
                    int dow = ((Integer) daysOfWeek.first()).intValue(); // desired
                    // d-o-w
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int daysToAdd = 0;
                    if (cDow < dow) daysToAdd = dow - cDow;
                    else if (cDow > dow) daysToAdd = dow + (7 - cDow);

                    boolean dayShifted = false;
                    if (daysToAdd > 0) dayShifted = true;

                    day += daysToAdd;
                    int weekOfMonth = day / 7;
                    if (day % 7 > 0) weekOfMonth++;

                    daysToAdd = (nthdayOfWeek - weekOfMonth) * 7;
                    day += daysToAdd;
                    if (daysToAdd < 0
                            || day > getLastDayOfMonth(mon, cl
                                    .get(Calendar.YEAR))) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0 || dayShifted) {
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' here because we are NOT promoting the month
                        continue;
                    }
                } else {
                    int cDow = cl.get(Calendar.DAY_OF_WEEK); // current d-o-w
                    int dow = ((Integer) daysOfWeek.first()).intValue(); // desired
                    // d-o-w
                    st = daysOfWeek.tailSet(new Integer(cDow));
                    if (st != null && st.size() > 0) {
                        dow = ((Integer) st.first()).intValue();
                    }

                    int daysToAdd = 0;
                    if (cDow < dow) daysToAdd = dow - cDow;
                    if (cDow > dow) daysToAdd = dow + (7 - cDow);

                    int lDay = getLastDayOfMonth(mon, cl.get(Calendar.YEAR));

                    if (day + daysToAdd > lDay) { // will we pass the end of
                        // the month?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, 1);
                        cl.set(Calendar.MONTH, mon);
                        // no '- 1' here because we are promoting the month
                        continue;
                    } else if (daysToAdd > 0) { // are we swithing days?
                        cl.set(Calendar.SECOND, 0);
                        cl.set(Calendar.MINUTE, 0);
                        cl.set(Calendar.HOUR_OF_DAY, 0);
                        cl.set(Calendar.DAY_OF_MONTH, day + daysToAdd);
                        cl.set(Calendar.MONTH, mon - 1);
                        // '- 1' because calendar is 0-based for this field,
                        // and we are 1-based
                        continue;
                    }
                }
            } else { // dayOfWSpec && !dayOfMSpec
                throw new UnsupportedOperationException(
                        "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented.");
                // TODO:
            }
            cl.set(Calendar.DAY_OF_MONTH, day);

            mon = cl.get(Calendar.MONTH) + 1;
            // '+ 1' because calendar is 0-based for this field, and we are
            // 1-based
            int year = cl.get(Calendar.YEAR);
            t = -1;

            // test for expressions that never generate a valid fire date,
            // but keep looping...
            if (year > 2099) return null;

            // get month...................................................
            st = months.tailSet(new Integer(mon));
            if (st != null && st.size() != 0) {
                t = mon;
                mon = ((Integer) st.first()).intValue();
            } else {
                mon = ((Integer) months.first()).intValue();
                year++;
            }
            if (mon != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.HOUR_OF_DAY, 0);
                cl.set(Calendar.DAY_OF_MONTH, 1);
                cl.set(Calendar.MONTH, mon - 1);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                cl.set(Calendar.YEAR, year);
                continue;
            }
            cl.set(Calendar.MONTH, mon - 1);
            // '- 1' because calendar is 0-based for this field, and we are
            // 1-based

            year = cl.get(Calendar.YEAR);
            t = -1;

            // get year...................................................
            st = years.tailSet(new Integer(year));
            if (st != null && st.size() != 0) {
                t = year;
                year = ((Integer) st.first()).intValue();
            } else
                return null; // ran out of years...

            if (year != t) {
                cl.set(Calendar.SECOND, 0);
                cl.set(Calendar.MINUTE, 0);
                cl.set(Calendar.HOUR_OF_DAY, 0);
                cl.set(Calendar.DAY_OF_MONTH, 1);
                cl.set(Calendar.MONTH, 0);
                // '- 1' because calendar is 0-based for this field, and we are
                // 1-based
                cl.set(Calendar.YEAR, year);
                continue;
            }
            cl.set(Calendar.YEAR, year);

            gotOne = true;
        } // while( !done )

        return cl.getTime();
    }

    /**
     * Advance the calendar to the particular hour paying particular attention
     * to daylight saving problems.
     * 
     * @param cal
     * @param hour
     */
    protected void setCalendarHour(Calendar cal, int hour) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
        if (cal.get(java.util.Calendar.HOUR_OF_DAY) != hour && hour != 24) {
            cal.set(java.util.Calendar.HOUR_OF_DAY, hour + 1);
        }
    }

    protected Date getTimeBefore(Date endTime) // TODO: implement
    {
        return null;
    }

    protected boolean isLeapYear(int year) {
        if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) return true;
        else
            return false;
    }

    public int getLastDayOfMonth(int monthNum, int year) {

        switch (monthNum) {
        case 1:
            return 31;
        case 2:
            return (isLeapYear(year)) ? 29 : 28;
        case 3:
            return 31;
        case 4:
            return 30;
        case 5:
            return 31;
        case 6:
            return 30;
        case 7:
            return 31;
        case 8:
            return 31;
        case 9:
            return 30;
        case 10:
            return 31;
        case 11:
            return 30;
        case 12:
            return 31;
        default:
            throw new IllegalArgumentException("Illegal month number: "
                    + monthNum);
        }
    }

    public static void main(String[] args) // TODO: remove method after good
            // unit testing
            throws Exception {

            String expr = "0 0 9-14 ? * 1,2,3,4,7";
            if(args != null && args.length > 0 && args[0] != null)
              expr = args[0];
        
            CronTrigger ct = new CronTrigger("t", "g", "j", "g", new Date(), null, expr);

//            ct.setTimeZone(TimeZone.getTimeZone("Africa/Harare"));

            System.err.println(ct.getExpressionSummary());
            System.err.println("tz=" + ct.getTimeZone().getID());
            System.err.println();
        
            java.util.List times = TriggerUtils.computeFireTimes(ct, null, 25);
        
            for (int i = 0; i < times.size(); i++) {
              System.err.println("firetime = " + times.get(i));
            }

            
            Calendar tt = Calendar.getInstance();
            tt.set(Calendar.DATE, 17);
            tt.set(Calendar.MONTH, 5 - 1);
            tt.set(Calendar.HOUR, 11);
            tt.set(Calendar.MINUTE, 0);
            tt.set(Calendar.SECOND, 7);
            
            System.err.println("\nWill fire on: " + tt.getTime() + " -- " + ct.willFireOn(tt, false));
            
          
//            CRON Expression: 0 0 9 * * ?
//
//                    TimeZone.getDefault().getDisplayName() = Central African Time
//                    TimeZone.getDefault().getID() = Africa/Harare            
        //
////        System.err.println();
////        System.err.println();
////        System.err.println();
////        System.err.println("Daylight test:");
////
////        CronTrigger trigger = new CronTrigger();
////
////        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
////        //    TimeZone timeZone = TimeZone.getDefault();
////
////        trigger.setTimeZone(timeZone);
////        trigger.setCronExpression("0 0 1 ? 4 *");
////
////        Date start = new Date(1056319200000L);
////        Date end = new Date(1087682399000L);
////
////        trigger.setStartTime(start);
////        trigger.setEndTime(end);
////
////        Date next = new Date(1056232800000L);
////        while (next != null) {
////            next = trigger.getFireTimeAfter(next);
////            if (next != null) {
////                Calendar cal = Calendar.getInstance();
////                cal.setTimeZone(timeZone);
////                cal.setTime(next);
////                System.err.println(cal.get(Calendar.MONTH) + "/"
////                        + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR)
////                        + " - " + cal.get(Calendar.HOUR_OF_DAY) + ":"
////                        + cal.get(Calendar.MINUTE));
////            }
////        }
////
////        System.err.println();
////        System.err.println();
////        System.err.println();
////        System.err.println("Midnight test:");
////
////        trigger = new CronTrigger();
////
////        timeZone = TimeZone.getTimeZone("Asia/Jerusalem");
////        //    timeZone = TimeZone.getTimeZone("America/Los_Angeles");
////        //    TimeZone timeZone = TimeZone.getDefault();
////
////        trigger.setTimeZone(timeZone);
////        trigger.setCronExpression("0 /15 * ? 4 *");
////
////        start = new Date(1056319200000L);
////        end = new Date(1087682399000L);
////
////        trigger.setStartTime(start);
////        trigger.setEndTime(end);
////
////        next = new Date(1056232800000L);
////        while (next != null) {
////            next = trigger.getFireTimeAfter(next);
////            if (next != null) {
////                Calendar cal = Calendar.getInstance();
////                cal.setTimeZone(timeZone);
////                cal.setTime(next);
////                System.err.println(cal.get(Calendar.MONTH) + "/"
////                        + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR)
////                        + " - " + cal.get(Calendar.HOUR_OF_DAY) + ":"
////                        + cal.get(Calendar.MINUTE));
////            }
////        }

    }
}

class ValueSet {
    public int value;

    public int pos;
}
