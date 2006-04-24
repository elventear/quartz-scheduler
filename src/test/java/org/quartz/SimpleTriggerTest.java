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



/**
 * Unit test for SimpleTrigger serialization backwards compatibility.
 */
public class SimpleTriggerTest extends SerializationTestSupport {
    private static final String[] VERSIONS = new String[] {"1.5.2"};

    private static final Calendar START_TIME = Calendar.getInstance();
    private static final Calendar END_TIME = Calendar.getInstance();
    
    static
    {
        START_TIME.clear();
        START_TIME.set(2006, Calendar.JUNE, 1, 10, 5, 15);
        END_TIME.clear();
        END_TIME.set(2008, Calendar.MAY, 2, 20, 15, 30);
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
        assertEquals(0, deserializedSimpleTrigger.getTriggerListenerNames().length);
    }
}
