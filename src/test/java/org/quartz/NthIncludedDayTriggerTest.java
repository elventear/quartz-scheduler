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
package org.quartz;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;



/**
 * Unit test for NthIncludedDayTrigger serialization backwards compatibility.
 */
public class NthIncludedDayTriggerTest extends SerializationTestSupport {
    private static final String[] VERSIONS = new String[] {"1.5.2"};
    
    public void testGetFireTimeAfter() {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2005, Calendar.JUNE, 1, 9, 30, 17);
        startCalendar.clear(Calendar.MILLISECOND);
        
        // Test yearly
        NthIncludedDayTrigger yearlyTrigger = new NthIncludedDayTrigger();
        yearlyTrigger.setIntervalType(NthIncludedDayTrigger.INTERVAL_TYPE_YEARLY);
        yearlyTrigger.setStartTime(startCalendar.getTime());
        yearlyTrigger.setN(10);
        yearlyTrigger.setFireAtTime("14:35:15");
        
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(2006, Calendar.JANUARY, 10, 14, 35, 15);
        targetCalendar.clear(Calendar.MILLISECOND);
        Date nextFireTime = yearlyTrigger.getFireTimeAfter(new Date(startCalendar.getTime().getTime() + 1000));
        assertEquals(targetCalendar.getTime(), nextFireTime);
        
        // Test monthly
        NthIncludedDayTrigger monthlyTrigger = new NthIncludedDayTrigger();
        monthlyTrigger.setIntervalType(NthIncludedDayTrigger.INTERVAL_TYPE_MONTHLY);
        monthlyTrigger.setStartTime(startCalendar.getTime());
        monthlyTrigger.setN(5);
        monthlyTrigger.setFireAtTime("14:35:15");
        
        targetCalendar.set(2005, Calendar.JUNE, 5, 14, 35, 15);
        nextFireTime = monthlyTrigger.getFireTimeAfter(new Date(startCalendar.getTime().getTime() + 1000));
        assertEquals(targetCalendar.getTime(), nextFireTime);
        
        // Test weekly
        NthIncludedDayTrigger weeklyTrigger = new NthIncludedDayTrigger();
        weeklyTrigger.setIntervalType(NthIncludedDayTrigger.INTERVAL_TYPE_WEEKLY);
        weeklyTrigger.setStartTime(startCalendar.getTime());
        weeklyTrigger.setN(3);
        weeklyTrigger.setFireAtTime("14:35:15");

        targetCalendar.set(2005, Calendar.JUNE, 7, 14, 35, 15);
        nextFireTime = weeklyTrigger.getFireTimeAfter(new Date(startCalendar.getTime().getTime() + 1000));
        assertEquals(targetCalendar.getTime(), nextFireTime);
    }
    
    public void testSetGetFireAtTime() {
        NthIncludedDayTrigger trigger = new NthIncludedDayTrigger();
        
        // Make sure a bad fire at time doesn't reset fire time
        trigger.setFireAtTime("14:30:10");
        try {
            trigger.setFireAtTime("blah");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        assertEquals("14:30:10", trigger.getFireAtTime());
        
        trigger.setFireAtTime("4:03:15");
        assertEquals("04:03:15", trigger.getFireAtTime());
        
        try {
            trigger.setFireAtTime("4:3");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        
        try {
            trigger.setFireAtTime("4:3:15");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        
        trigger.setFireAtTime("23:17");
        assertEquals("23:17:00", trigger.getFireAtTime());
        
        try {
            trigger.setFireAtTime("24:3:15");
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            trigger.setFireAtTime("-1:3:15");
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            trigger.setFireAtTime("23:60:15");
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            trigger.setFireAtTime("23:-1:15");
            fail();
        } catch (IllegalArgumentException ignore) {
        }

        try {
            trigger.setFireAtTime("23:17:60");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
        
        try {
            trigger.setFireAtTime("23:17:-1");
            fail();
        } catch (IllegalArgumentException ignore) {
        }
    }
    
    public void testTimeZone() throws Exception {
        
        TimeZone GMT = TimeZone.getTimeZone("GMT-0:00");
        TimeZone EST = TimeZone.getTimeZone("GMT-5:00");
        
        Calendar startTime = Calendar.getInstance(EST);
        startTime.set(2006, Calendar.MARCH, 7, 7, 0, 0);
        
        // Same timezone, so should just get back 8:00 that day
        {
            NthIncludedDayTrigger t = new NthIncludedDayTrigger("name", "group");
            t.setIntervalType(NthIncludedDayTrigger.INTERVAL_TYPE_WEEKLY);
            t.setN(TriggerUtils.TUESDAY);
            t.setStartTime(startTime.getTime());
            t.setFireAtTime("8:00");
            t.setTimeZone(EST);
            
            Date firstTime = t.computeFirstFireTime(null);
            Calendar firstTimeCal = Calendar.getInstance(EST);
            firstTimeCal.setTime(firstTime);
            assertEquals(7, firstTimeCal.get(Calendar.DATE));
        }

        // Timezone is 5 hours later, so should just get back 8:00 a week later
        {
            NthIncludedDayTrigger t = new NthIncludedDayTrigger("name", "group");
            t.setIntervalType(NthIncludedDayTrigger.INTERVAL_TYPE_WEEKLY);
            t.setN(TriggerUtils.TUESDAY);
            t.setStartTime(startTime.getTime());
            t.setFireAtTime("8:00");
            t.setTimeZone(GMT);
            
            Date firstTime = t.computeFirstFireTime(null);
            Calendar firstTimeCal = Calendar.getInstance(EST);
            firstTimeCal.setTime(firstTime);
            assertEquals(14, firstTimeCal.get(Calendar.DATE));
        }
    }
    
    
    /**
     * Get the object to serialize when generating serialized file for future
     * tests, and against which to validate deserialized object.
     */
    protected Object getTargetObject() {
        Calendar startTime = Calendar.getInstance();
        startTime.set(2005, 6, 1, 11, 30, 0);
        
        NthIncludedDayTrigger t = new NthIncludedDayTrigger("name", "group");
        t.setIntervalType(NthIncludedDayTrigger.INTERVAL_TYPE_MONTHLY);
        t.setN(TriggerUtils.TUESDAY);
        t.setStartTime(startTime.getTime());
        t.setFireAtTime("12:15");
        t.setNextFireCutoffInterval(13);
        
        return t;
    }
    
    /**
     * Get the Quartz versions for which we should verify
     * serialization backwards compatibility.
     */
    protected String[] getVersions() {
        return VERSIONS;
    }
    
    /**
     * Verify that the target object and the object we just deserialized 
     * match.
     */
    protected void verifyMatch(Object target, Object deserialized) {
        NthIncludedDayTrigger targetTrigger = (NthIncludedDayTrigger)target;
        NthIncludedDayTrigger deserializedTrigger = (NthIncludedDayTrigger)deserialized;
        
        assertNotNull(deserializedTrigger);
        assertEquals(deserializedTrigger.getName(), deserializedTrigger.getName());
        assertEquals(deserializedTrigger.getGroup(), deserializedTrigger.getGroup());
        assertEquals(deserializedTrigger.getIntervalType(), deserializedTrigger.getIntervalType());
        assertEquals(deserializedTrigger.getN(), deserializedTrigger.getN());
        assertEquals(deserializedTrigger.getStartTime(), deserializedTrigger.getStartTime());
        assertNull(deserializedTrigger.getEndTime());
        assertEquals(deserializedTrigger.getFireAtTime(), deserializedTrigger.getFireAtTime());
        assertEquals(deserializedTrigger.getNextFireCutoffInterval(), deserializedTrigger.getNextFireCutoffInterval());
        assertEquals(TimeZone.getDefault(), deserializedTrigger.getTimeZone());
    }
}
