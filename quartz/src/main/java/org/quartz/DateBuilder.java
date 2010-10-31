/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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
import java.util.TimeZone;

/**
 * <code>DateBuilder</code> is used to conveniently create 
 * <code>java.util.Date</code> instances that meet particular criteria.
 *  
 * <p>Quartz provides a builder-style API for constructing scheduling-related
 * entities via a Domain-Specific Language (DSL).  The DSL can best be
 * utilized through the usage of static imports of the methods on the classes
 * <code>TriggerBuilder</code>, <code>JobBuilder</code>, 
 * <code>DateBuilder</code>, <code>JobKey</code>, <code>TriggerKey</code> 
 * and the various <code>ScheduleBuilder</code> implementations.</p>
 * 
 * <p>Client code can then use the DSL to write code such as this:</p>
 * <pre>
 *         JobDetail job = newJob(MyJob.class)
 *             .withIdentity("myJob")
 *             .build();
 *             
 *         Trigger trigger = newTrigger() 
 *             .withIdentity(triggerKey("myTrigger", "myTriggerGroup"))
 *             .withSchedule(simpleSchedule()
 *                 .withIntervalInHours(1)
 *                 .repeatForever())
 *             .startAt(futureDate(10, MINUTES))
 *             .build();
 *         
 *         scheduler.scheduleJob(job, trigger);
 * <pre>
 *  
 * @see TriggerBuilder
 * @see JobBuilder 
 */
public class DateBuilder {

    public enum IntervalUnit { MILLISECOND, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR };
    
    public static final int SUNDAY = 1;

    public static final int MONDAY = 2;

    public static final int TUESDAY = 3;

    public static final int WEDNESDAY = 4;

    public static final int THURSDAY = 5;

    public static final int FRIDAY = 6;

    public static final int SATURDAY = 7;

    public static final long MILLISECONDS_IN_MINUTE = 60l * 1000l;

    public static final long MILLISECONDS_IN_HOUR = 60l * 60l * 1000l;

    public static final long SECONDS_IN_MOST_DAYS = 24l * 60l * 60L;

    public static final long MILLISECONDS_IN_DAY = SECONDS_IN_MOST_DAYS * 1000l;
    
    
    private DateBuilder() {
    }
    
    public static Date futureDate(int interval, IntervalUnit unit) {
        
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.setLenient(true);
        
        c.add(translate(unit), interval);

        return c.getTime();
    }
    

    private static int translate(IntervalUnit unit) {
        switch(unit) {
            case DAY : return Calendar.DAY_OF_YEAR;
            case HOUR : return Calendar.HOUR_OF_DAY;
            case MINUTE : return Calendar.MINUTE;
            case MONTH : return Calendar.MONTH;
            case SECOND : return Calendar.SECOND;
            case MILLISECOND : return Calendar.MILLISECOND;
            case WEEK : return Calendar.WEEK_OF_YEAR;
            case YEAR : return Calendar.YEAR;
            default : throw new IllegalArgumentException("Unknown IntervalUnit");
        }
    }

    /**
     * <p>
     * Get a <code>Date</code> object that represents the given time, on
     * today's date.
     * </p>
     * 
     * @param second
     *          The value (0-59) to give the seconds field of the date
     * @param minute
     *          The value (0-59) to give the minutes field of the date
     * @param hour
     *          The value (0-23) to give the hours field of the date
     * @return the new date
     */
    public static Date dateOf(int hour, int minute, int second) {
        validateSecond(second);
        validateMinute(minute);
        validateHour(hour);

        Date date = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.setLenient(true);

        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    /**
     * <p>
     * Get a <code>Date</code> object that represents the given time, on the
     * given date.
     * </p>
     * 
     * @param second
     *          The value (0-59) to give the seconds field of the date
     * @param minute
     *          The value (0-59) to give the minutes field of the date
     * @param hour
     *          The value (0-23) to give the hours field of the date
     * @param dayOfMonth
     *          The value (1-31) to give the day of month field of the date
     * @param month
     *          The value (1-12) to give the month field of the date
     * @return the new date
     */
    public static Date dateOf(int hour, int minute, int second,
            int dayOfMonth, int month) {
        validateSecond(second);
        validateMinute(minute);
        validateHour(hour);
        validateDayOfMonth(dayOfMonth);
        validateMonth(month);

        Date date = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    /**
     * <p>
     * Get a <code>Date</code> object that represents the given time, on the
     * given date.
     * </p>
     * 
     * @param second
     *          The value (0-59) to give the seconds field of the date
     * @param minute
     *          The value (0-59) to give the minutes field of the date
     * @param hour
     *          The value (0-23) to give the hours field of the date
     * @param dayOfMonth
     *          The value (1-31) to give the day of month field of the date
     * @param month
     *          The value (1-12) to give the month field of the date
     * @param year
     *          The value (1970-2099) to give the year field of the date
     * @return the new date
     */
    public static Date dateOf(int hour, int minute, int second,
            int dayOfMonth, int month, int year) {
        validateSecond(second);
        validateMinute(minute);
        validateHour(hour);
        validateDayOfMonth(dayOfMonth);
        validateMonth(month);
        validateYear(year);

        Date date = new Date();

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, second);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }


    /**
     * <p>
     * Returns a date that is rounded to the next even hour after the current time.
     * </p>
     * 
     * <p>
     * For example a current time of 08:13:54 would result in a date
     * with the time of 09:00:00. If the date's time is in the 23rd hour, the
     * date's 'day' will be promoted, and the time will be set to 00:00:00.
     * </p>
     * 
     * @return the new rounded date
     */
    public static Date evenHourDateAfterNow() {
        return evenHourDate(null);
    }
    /**
     * <p>
     * Returns a date that is rounded to the next even hour above the given
     * date.
     * </p>
     * 
     * <p>
     * For example an input date with a time of 08:13:54 would result in a date
     * with the time of 09:00:00. If the date's time is in the 23rd hour, the
     * date's 'day' will be promoted, and the time will be set to 00:00:00.
     * </p>
     * 
     * @param date
     *          the Date to round, if <code>null</code> the current time will
     *          be used
     * @return the new rounded date
     */
    public static Date evenHourDate(Date date) {
        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.setLenient(true);

        c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    /**
     * <p>
     * Returns a date that is rounded to the previous even hour below the given
     * date.
     * </p>
     * 
     * <p>
     * For example an input date with a time of 08:13:54 would result in a date
     * with the time of 08:00:00.
     * </p>
     * 
     * @param date
     *          the Date to round, if <code>null</code> the current time will
     *          be used
     * @return the new rounded date
     */
    public static Date evenHourDateBefore(Date date) {
        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    /**
     * <p>
     * Returns a date that is rounded to the next even minute after the current time.
     * </p>
     * 
     * <p>
     * For example a current time of 08:13:54 would result in a date
     * with the time of 08:14:00. If the date's time is in the 59th minute,
     * then the hour (and possibly the day) will be promoted.
     * </p>
     * 
     * @return the new rounded date
     */
    public static Date evenMinuteDateAfterNow() {
        return evenMinuteDate(null);
    }
    
    /**
     * <p>
     * Returns a date that is rounded to the next even minute above the given
     * date.
     * </p>
     * 
     * <p>
     * For example an input date with a time of 08:13:54 would result in a date
     * with the time of 08:14:00. If the date's time is in the 59th minute,
     * then the hour (and possibly the day) will be promoted.
     * </p>
     * 
     * @param date
     *          the Date to round, if <code>null</code> the current time will
     *          be used
     * @return the new rounded date
     */
    public static Date evenMinuteDate(Date date) {
        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.setLenient(true);

        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 1);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    /**
     * <p>
     * Returns a date that is rounded to the previous even minute below the 
     * given date.
     * </p>
     * 
     * <p>
     * For example an input date with a time of 08:13:54 would result in a date
     * with the time of 08:13:00.
     * </p>
     * 
     * @param date
     *          the Date to round, if <code>null</code> the current time will
     *          be used
     * @return the new rounded date
     */
    public static Date evenMinuteDateBefore(Date date) {
        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    /**
     * <p>
     * Returns a date that is rounded to the next even second after the current time.
     * </p>
     * 
     * @return the new rounded date
     */
    public static Date evenSecondDateAfterNow() {
        return evenSecondDate(null);
    }
    /**
     * <p>
     * Returns a date that is rounded to the next even second above the given
     * date.
     * </p>
     * 
     * @param date
     *          the Date to round, if <code>null</code> the current time will
     *          be used
     * @return the new rounded date
     */
    public static Date evenSecondDate(Date date) {
        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.setLenient(true);

        c.set(Calendar.SECOND, c.get(Calendar.SECOND) + 1);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }

    /**
     * <p>
     * Returns a date that is rounded to the previous even second below the
     * given date.
     * </p>
     * 
     * <p>
     * For example an input date with a time of 08:13:54.341 would result in a
     * date with the time of 08:13:00.000.
     * </p>
     * 
     * @param date
     *          the Date to round, if <code>null</code> the current time will
     *          be used
     * @return the new rounded date
     */
    public static Date evenSecondDateBefore(Date date) {
        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);

        c.set(Calendar.MILLISECOND, 0);

        return c.getTime();
    }
    
    /**
     * <p>
     * Returns a date that is rounded to the next even multiple of the given
     * minute.
     * </p>
     * 
     * <p>
     * For example an input date with a time of 08:13:54, and an input
     * minute-base of 5 would result in a date with the time of 08:15:00. The
     * same input date with an input minute-base of 10 would result in a date
     * with the time of 08:20:00. But a date with the time 08:53:31 and an
     * input minute-base of 45 would result in 09:00:00, because the even-hour
     * is the next 'base' for 45-minute intervals.
     * </p>
     * 
     * <p>
     * More examples: <table>
     * <tr>
     * <th>Input Time</th>
     * <th>Minute-Base</th>
     * <th>Result Time</th>
     * </tr>
     * <tr>
     * <td>11:16:41</td>
     * <td>20</td>
     * <td>11:20:00</td>
     * </tr>
     * <tr>
     * <td>11:36:41</td>
     * <td>20</td>
     * <td>11:40:00</td>
     * </tr>
     * <tr>
     * <td>11:46:41</td>
     * <td>20</td>
     * <td>12:00:00</td>
     * </tr>
     * <tr>
     * <td>11:26:41</td>
     * <td>30</td>
     * <td>11:30:00</td>
     * </tr>
     * <tr>
     * <td>11:36:41</td>
     * <td>30</td>
     * <td>12:00:00</td>
     * </tr>
     * <td>11:16:41</td>
     * <td>17</td>
     * <td>11:17:00</td>
     * </tr>
     * </tr>
     * <td>11:17:41</td>
     * <td>17</td>
     * <td>11:34:00</td>
     * </tr>
     * </tr>
     * <td>11:52:41</td>
     * <td>17</td>
     * <td>12:00:00</td>
     * </tr>
     * </tr>
     * <td>11:52:41</td>
     * <td>5</td>
     * <td>11:55:00</td>
     * </tr>
     * </tr>
     * <td>11:57:41</td>
     * <td>5</td>
     * <td>12:00:00</td>
     * </tr>
     * </tr>
     * <td>11:17:41</td>
     * <td>0</td>
     * <td>12:00:00</td>
     * </tr>
     * </tr>
     * <td>11:17:41</td>
     * <td>1</td>
     * <td>11:08:00</td>
     * </tr>
     * </table>
     * </p>
     * 
     * @param date
     *          the Date to round, if <code>null</code> the current time will
     *          be used
     * @param minuteBase
     *          the base-minute to set the time on
     * @return the new rounded date
     * 
     * @see #nextGivenSecondDate(Date, int)
     */
    public static Date nextGivenMinuteDate(Date date, int minuteBase) {
        if (minuteBase < 0 || minuteBase > 59) {
            throw new IllegalArgumentException(
                    "minuteBase must be >=0 and <= 59");
        }

        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.setLenient(true);

        if (minuteBase == 0) {
            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            return c.getTime();
        }

        int minute = c.get(Calendar.MINUTE);

        int arItr = minute / minuteBase;

        int nextMinuteOccurance = minuteBase * (arItr + 1);

        if (nextMinuteOccurance < 60) {
            c.set(Calendar.MINUTE, nextMinuteOccurance);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            return c.getTime();
        } else {
            c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) + 1);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            return c.getTime();
        }
    }
    
    /**
     * <p>
     * Returns a date that is rounded to the next even multiple of the given
     * minute.
     * </p>
     * 
     * <p>
     * The rules for calculating the second are the same as those for
     * calculating the minute in the method 
     * <code>getNextGivenMinuteDate(..)<code>.
     * </p>
     *
     * @param date the Date to round, if <code>null</code> the current time will
     * be used
     * @param secondBase the base-second to set the time on
     * @return the new rounded date
     * 
     * @see #nextGivenMinuteDate(Date, int)
     */
    public static Date nextGivenSecondDate(Date date, int secondBase) {
        if (secondBase < 0 || secondBase > 59) {
            throw new IllegalArgumentException(
                    "secondBase must be >=0 and <= 59");
        }

        if (date == null) {
            date = new Date();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.setLenient(true);

        if (secondBase == 0) {
            c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 1);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            return c.getTime();
        }

        int second = c.get(Calendar.SECOND);

        int arItr = second / secondBase;

        int nextSecondOccurance = secondBase * (arItr + 1);

        if (nextSecondOccurance < 60) {
            c.set(Calendar.SECOND, nextSecondOccurance);
            c.set(Calendar.MILLISECOND, 0);

            return c.getTime();
        } else {
            c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) + 1);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            return c.getTime();
        }
    }

    /**
     * Translate a date & time from a users time zone to the another
     * (probably server) time zone to assist in creating a simple trigger with 
     * the right date & time.
     * 
     * @param date the date to translate
     * @param src the original time-zone
     * @param dest the destination time-zone
     * @return the translated date
     */
    public static Date translateTime(Date date, TimeZone src, TimeZone dest) {

        Date newDate = new Date();

        int offset = (dest.getOffset(date.getTime()) - src.getOffset(date.getTime()));

        newDate.setTime(date.getTime() - offset);

        return newDate;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static void validateDayOfWeek(int dayOfWeek) {
        if (dayOfWeek < SUNDAY || dayOfWeek > SATURDAY) {
            throw new IllegalArgumentException("Invalid day of week.");
        }
    }

    public static void validateHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(
                    "Invalid hour (must be >= 0 and <= 23).");
        }
    }

    public static void validateMinute(int minute) {
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(
                    "Invalid minute (must be >= 0 and <= 59).");
        }
    }

    public static void validateSecond(int second) {
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException(
                    "Invalid second (must be >= 0 and <= 59).");
        }
    }

    public static void validateDayOfMonth(int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Invalid day of month.");
        }
    }

    public static void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Invalid month (must be >= 1 and <= 12.");
        }
    }

    public static void validateYear(int year) {
        if (year < 1970 || year > 2099) {
            throw new IllegalArgumentException(
                    "Invalid year (must be >= 1970 and <= 2099.");
        }
    }

}
