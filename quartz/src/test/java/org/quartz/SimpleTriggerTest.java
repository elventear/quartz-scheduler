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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;


/**
 * Unit test for SimpleTrigger serialization backwards compatibility.
 */
public class SimpleTriggerTest extends SerializationTestSupport {
    private static final String[] VERSIONS = new String[] {"1.5.2"};

    private static final TimeZone EST_TIME_ZONE = TimeZone.getTimeZone("US/Eastern"); 
    private static final Calendar START_TIME = Calendar.getInstance();
    private static final Calendar END_TIME = Calendar.getInstance();
    
    static
    {
        START_TIME.clear();
        START_TIME.set(2006, Calendar.JUNE, 1, 10, 5, 15);
        START_TIME.setTimeZone(EST_TIME_ZONE);
        END_TIME.clear();
        END_TIME.set(2008, Calendar.MAY, 2, 20, 15, 30);
        END_TIME.setTimeZone(EST_TIME_ZONE);
    }
    
    /**
     * Get the object to serialize when generating serialized file for future
     * tests, and against which to validate deserialized object.
     */
    protected Object getTargetObject() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("A", "B");
        
        SimpleTrigger t = new SimpleTrigger("SimpleTrigger", "SimpleGroup",
                "JobName", "JobGroup", START_TIME.getTime(),
                END_TIME.getTime(), 5, 1000);
        t.setCalendarName("MyCalendar");
        t.setDescription("SimpleTriggerDesc");
        t.setJobDataMap(jobDataMap);
        t.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
        t.setVolatility(true);

        t.addTriggerListener("L1");
        t.addTriggerListener("L2");
        
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
        SimpleTrigger targetSimpleTrigger = (SimpleTrigger)target;
        SimpleTrigger deserializedSimpleTrigger = (SimpleTrigger)deserialized;
        
        assertNotNull(deserializedSimpleTrigger);
        assertEquals(targetSimpleTrigger.getName(), deserializedSimpleTrigger.getName());
        assertEquals(targetSimpleTrigger.getGroup(), deserializedSimpleTrigger.getGroup());
        assertEquals(targetSimpleTrigger.getJobName(), deserializedSimpleTrigger.getJobName());
        assertEquals(targetSimpleTrigger.getJobGroup(), deserializedSimpleTrigger.getJobGroup());
        assertEquals(targetSimpleTrigger.getStartTime(), deserializedSimpleTrigger.getStartTime());
        assertEquals(targetSimpleTrigger.getEndTime(), deserializedSimpleTrigger.getEndTime());
        assertEquals(targetSimpleTrigger.getRepeatCount(), deserializedSimpleTrigger.getRepeatCount());
        assertEquals(targetSimpleTrigger.getRepeatInterval(), deserializedSimpleTrigger.getRepeatInterval());
        assertEquals(targetSimpleTrigger.getCalendarName(), deserializedSimpleTrigger.getCalendarName());
        assertEquals(targetSimpleTrigger.getDescription(), deserializedSimpleTrigger.getDescription());
        assertEquals(targetSimpleTrigger.getJobDataMap(), deserializedSimpleTrigger.getJobDataMap());
        assertEquals(targetSimpleTrigger.getMisfireInstruction(), deserializedSimpleTrigger.getMisfireInstruction());
        assertTrue(targetSimpleTrigger.isVolatile());
        assertEquals(2, deserializedSimpleTrigger.getTriggerListenerNames().length);
    }
    
    public void testUpdateAfterMisfire() {
        
        Calendar startTime = Calendar.getInstance();
        startTime.set(2005, Calendar.JULY, 5, 9, 0, 0);
        
        Calendar endTime = Calendar.getInstance();
        endTime.set(2005, Calendar.JULY, 5, 10, 0, 0);
        
        SimpleTrigger simpleTrigger = new SimpleTrigger();
        simpleTrigger.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT);
        simpleTrigger.setRepeatCount(5);
        simpleTrigger.setStartTime(startTime.getTime());
        simpleTrigger.setEndTime(endTime.getTime());
        
        Date currentTime = new Date();
        simpleTrigger.updateAfterMisfire(null);
        assertEquals(startTime.getTime(), simpleTrigger.getStartTime());
        assertEquals(endTime.getTime(), simpleTrigger.getEndTime());
        assertNull(simpleTrigger.getNextFireTime());
    }
    
    public void testGetFireTimeAfter() {
        SimpleTrigger simpleTrigger = new SimpleTrigger();

        simpleTrigger.setStartTime(new Date(0));
        simpleTrigger.setRepeatInterval(10);
        simpleTrigger.setRepeatCount(4);
        
        Date fireTimeAfter = simpleTrigger.getFireTimeAfter(new Date(34));
        assertEquals(40, fireTimeAfter.getTime());
    }
    
    public void testAddTriggerListener() {
        String[] listenerNames = new String[] {"X", "A", "B"};
        
        // Verify that a HashSet shuffles order, so we know that order test
        // below is actually testing something
        HashSet hashSet = new HashSet(Arrays.asList(listenerNames));
        assertFalse(Arrays.asList(listenerNames).equals(new ArrayList(hashSet)));
        
        SimpleTrigger simpleTrigger = new SimpleTrigger();
        for (int i = 0; i < listenerNames.length; i++) {
            simpleTrigger.addTriggerListener(listenerNames[i]);
        }

        // Make sure order was maintained
        assertEquals(Arrays.asList(listenerNames),
                     Arrays.asList(simpleTrigger.getTriggerListenerNames()));
        
        // Make sure uniqueness is enforced
        for (int i = 0; i < listenerNames.length; i++) {
            try {
                simpleTrigger.addTriggerListener(listenerNames[i]);
                fail();
            } catch (IllegalArgumentException e) {
            }
        }
    }
    
    public void testClone() {
        SimpleTrigger simpleTrigger = new SimpleTrigger();
        
        // Make sure empty sub-objects are cloned okay
        Trigger clone = (Trigger)simpleTrigger.clone();
        assertEquals(0, clone.getTriggerListenerNames().length);
        assertEquals(0, clone.getJobDataMap().size());
        
        // Make sure non-empty sub-objects are cloned okay
        simpleTrigger.addTriggerListener("L1");
        simpleTrigger.addTriggerListener("L2");
        simpleTrigger.getJobDataMap().put("K1", "V1");
        simpleTrigger.getJobDataMap().put("K2", "V2");
        clone = (Trigger)simpleTrigger.clone();
        assertEquals(2, clone.getTriggerListenerNames().length);
        assertEquals(Arrays.asList(new String[] {"L1", "L2"}), Arrays.asList(clone.getTriggerListenerNames()));
        assertEquals(2, clone.getJobDataMap().size());
        assertEquals("V1", clone.getJobDataMap().get("K1"));
        assertEquals("V2", clone.getJobDataMap().get("K2"));
        
        // Make sure sub-object collections have really been cloned by ensuring 
        // their modification does not change the source Trigger 
        clone.removeTriggerListener("L2");
        assertEquals(1, clone.getTriggerListenerNames().length);
        assertEquals(Arrays.asList(new String[] {"L1"}), Arrays.asList(clone.getTriggerListenerNames()));
        clone.getJobDataMap().remove("K1");
        assertEquals(1, clone.getJobDataMap().size());
        
        assertEquals(2, simpleTrigger.getTriggerListenerNames().length);
        assertEquals(Arrays.asList(new String[] {"L1", "L2"}), Arrays.asList(simpleTrigger.getTriggerListenerNames()));
        assertEquals(2, simpleTrigger.getJobDataMap().size());
        assertEquals("V1", simpleTrigger.getJobDataMap().get("K1"));
        assertEquals("V2", simpleTrigger.getJobDataMap().get("K2"));
    }
    
    // NPE in equals()
    public void testQuartz665() {
        new SimpleTrigger().equals(new SimpleTrigger());
    }    
}
