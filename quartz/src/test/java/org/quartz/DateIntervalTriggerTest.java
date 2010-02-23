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
 */
package org.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * Unit tests for DateIntervalTrigger.
 */
public class DateIntervalTriggerTest  extends TestCase {
    
    public void testYearlyIntervalGetFireTimeAfter() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger yearlyTrigger = new DateIntervalTrigger();
        yearlyTrigger.setStartTime(startCalendar.getTime());
        yearlyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.YEAR);
        yearlyTrigger.setRepeatInterval(2); // every two years;
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2009, Calendar.JUNE, 1, 9, 30, 17); // jump 4 years (2 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        List fireTimes = TriggerUtils.computeFireTimes(yearlyTrigger, null, 4);
        Date secondTime = (Date) fireTimes.get(2); // get the third fire time
        
        assertEquals("Year increment result not as expected.", targetCalendar.getTime(), secondTime);
    }

    
    public void testMonthlyIntervalGetFireTimeAfter() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger yearlyTrigger = new DateIntervalTrigger();
        yearlyTrigger.setStartTime(startCalendar.getTime());
        yearlyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.MONTH);
        yearlyTrigger.setRepeatInterval(5); // every five months
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        targetCalendar.setLenient(true);
        targetCalendar.add(Calendar.MONTH, 25); // jump 25 five months (5 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        List fireTimes = TriggerUtils.computeFireTimes(yearlyTrigger, null, 6);
        Date fifthTime = (Date) fireTimes.get(5); // get the sixth fire time

        assertEquals("Month increment result not as expected.", targetCalendar.getTime(), fifthTime);
    }

    public void testWeeklyIntervalGetFireTimeAfter() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger yearlyTrigger = new DateIntervalTrigger();
        yearlyTrigger.setStartTime(startCalendar.getTime());
        yearlyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.WEEK);
        yearlyTrigger.setRepeatInterval(6); // every six weeks
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        targetCalendar.setLenient(true);
        targetCalendar.add(Calendar.DAY_OF_YEAR, 7 * 6 * 4); // jump 24 weeks (4 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        List fireTimes = TriggerUtils.computeFireTimes(yearlyTrigger, null, 7);
        Date fifthTime = (Date) fireTimes.get(4); // get the fifth fire time

        System.out.println("targetCalendar:" + targetCalendar.getTime());
        System.out.println("fifthTimee" + fifthTime);
        
        assertEquals("Week increment result not as expected.", targetCalendar.getTime(), fifthTime);
    }
    
    public void testDailyIntervalGetFireTimeAfter() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger dailyTrigger = new DateIntervalTrigger();
        dailyTrigger.setStartTime(startCalendar.getTime());
        dailyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.DAY);
        dailyTrigger.setRepeatInterval(90); // every ninety days
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        targetCalendar.setLenient(true);
        targetCalendar.add(Calendar.DAY_OF_YEAR, 360); // jump 360 days (4 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        List fireTimes = TriggerUtils.computeFireTimes(dailyTrigger, null, 6);
        Date fifthTime = (Date) fireTimes.get(4); // get the fifth fire time

        assertEquals("Day increment result not as expected.", targetCalendar.getTime(), fifthTime);
    }
    
    public void testHourlyIntervalGetFireTimeAfter() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger yearlyTrigger = new DateIntervalTrigger();
        yearlyTrigger.setStartTime(startCalendar.getTime());
        yearlyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.HOUR);
        yearlyTrigger.setRepeatInterval(100); // every 100 hours
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        targetCalendar.setLenient(true);
        targetCalendar.add(Calendar.HOUR, 400); // jump 400 hours (4 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        List fireTimes = TriggerUtils.computeFireTimes(yearlyTrigger, null, 6);
        Date fifthTime = (Date) fireTimes.get(4); // get the fifth fire time

        assertEquals("Hour increment result not as expected.", targetCalendar.getTime(), fifthTime);
    }

    public void testMinutelyIntervalGetFireTimeAfter() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger yearlyTrigger = new DateIntervalTrigger();
        yearlyTrigger.setStartTime(startCalendar.getTime());
        yearlyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.MINUTE);
        yearlyTrigger.setRepeatInterval(100); // every 100 minutes
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        targetCalendar.setLenient(true);
        targetCalendar.add(Calendar.MINUTE, 400); // jump 400 minutes (4 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        List fireTimes = TriggerUtils.computeFireTimes(yearlyTrigger, null, 6);
        Date fifthTime = (Date) fireTimes.get(4); // get the fifth fire time

        assertEquals("Minutes increment result not as expected.", targetCalendar.getTime(), fifthTime);
    }

    public void testSecondlyIntervalGetFireTimeAfter() {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger yearlyTrigger = new DateIntervalTrigger();
        yearlyTrigger.setStartTime(startCalendar.getTime());
        yearlyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.SECOND);
        yearlyTrigger.setRepeatInterval(100); // every 100 seconds
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        targetCalendar.setLenient(true);
        targetCalendar.add(Calendar.SECOND, 400); // jump 400 seconds (4 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        List fireTimes = TriggerUtils.computeFireTimes(yearlyTrigger, null, 6);
        Date fifthTime = (Date) fireTimes.get(4); // get the third fire time

        assertEquals("Seconds increment result not as expected.", targetCalendar.getTime(), fifthTime);
    }

    public void testDaylightSavingsTransitions() {

        // Pick a day before a daylight savings transition...
        
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2010, Calendar.MARCH, 12, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger dailyTrigger = new DateIntervalTrigger();
        dailyTrigger.setStartTime(startCalendar.getTime());
        dailyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.DAY);
        dailyTrigger.setRepeatInterval(5); // every 5 days
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(startCalendar.getTime());
        targetCalendar.setLenient(true);
        targetCalendar.add(Calendar.DAY_OF_YEAR, 10); // jump 10 days (2 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        List fireTimes = TriggerUtils.computeFireTimes(dailyTrigger, null, 6);
        Date testTime = (Date) fireTimes.get(2); // get the third fire time

        assertEquals("Day increment result not as expected over spring daylight savings transition.", targetCalendar.getTime(), testTime);

        
        // Pick a day before a daylight savings transition...
        
        startCalendar = Calendar.getInstance();
        startCalendar.set(2010, Calendar.OCTOBER, 31, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);

        dailyTrigger = new DateIntervalTrigger();
        dailyTrigger.setStartTime(startCalendar.getTime());
        dailyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.DAY);
        dailyTrigger.setRepeatInterval(5); // every 5 days
        
        targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(startCalendar.getTime());
        targetCalendar.setLenient(true);
        targetCalendar.add(Calendar.DAY_OF_YEAR, 15); // jump 15 days (3 intervals)
        targetCalendar.clear(Calendar.MILLISECOND);

        fireTimes = TriggerUtils.computeFireTimes(dailyTrigger, null, 6);
        testTime = (Date) fireTimes.get(3); // get the fourth fire time

        assertEquals("Day increment result not as expected over fall daylight savings transition.", targetCalendar.getTime(), testTime);
    }
 
    
    public void testFinalFireTimes() {

        
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2010, Calendar.MARCH, 12, 9, 0, 0);
        startCalendar.clear(Calendar.MILLISECOND);

        DateIntervalTrigger dailyTrigger = new DateIntervalTrigger();
        dailyTrigger.setStartTime(startCalendar.getTime());
        dailyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.DAY);
        dailyTrigger.setRepeatInterval(5); // every 5 days
        
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(startCalendar.getTime());
        endCalendar.setLenient(true);
        endCalendar.add(Calendar.DAY_OF_YEAR, 10); // jump 10 days (2 intervals)
        endCalendar.clear(Calendar.MILLISECOND);
        dailyTrigger.setEndTime(endCalendar.getTime());

        Date testTime = dailyTrigger.getFinalFireTime();

        assertEquals("Final fire time not computed correctly for day interval.", endCalendar.getTime(), testTime);

        
        startCalendar = Calendar.getInstance();
        startCalendar.set(2010, Calendar.MARCH, 12, 9, 0, 0);
        startCalendar.clear(Calendar.MILLISECOND);

        dailyTrigger = new DateIntervalTrigger();
        dailyTrigger.setStartTime(startCalendar.getTime());
        dailyTrigger.setRepeatIntervalUnit(DateIntervalTrigger.IntervalUnit.MINUTE);
        dailyTrigger.setRepeatInterval(5); // every 5 minutes
        
        endCalendar = Calendar.getInstance();
        endCalendar.setTime(startCalendar.getTime());
        endCalendar.setLenient(true);
        endCalendar.add(Calendar.DAY_OF_YEAR, 15); // jump 15 days 
        endCalendar.add(Calendar.MINUTE,-2); // back up two minutes
        endCalendar.clear(Calendar.MILLISECOND);
        dailyTrigger.setEndTime(endCalendar.getTime());

        testTime = dailyTrigger.getFinalFireTime();

        assertTrue("Final fire time not computed correctly for minutely interval.", (endCalendar.getTime().after(testTime)));

        endCalendar.add(Calendar.MINUTE,-3); // back up three more minutes
        
        assertTrue("Final fire time not computed correctly for minutely interval.", (endCalendar.getTime().equals(testTime)));
    }
    
}
