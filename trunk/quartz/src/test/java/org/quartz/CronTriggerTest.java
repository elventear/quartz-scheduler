/* 
 * Copyright 2007-2009 Terracotta, Inc. 
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

import java.text.ParseException;

/**
 * Unit test for CronTrigger.
 */
public class CronTriggerTest extends SerializationTestSupport {

    private static final String[] VERSIONS = new String[] {"1.5.2"};

    /**
     * Get the Quartz versions for which we should verify
     * serialization backwards compatibility.
     */
    protected String[] getVersions() {
        return VERSIONS;
    }
    
    /**
     * Get the object to serialize when generating serialized file for future
     * tests, and against which to validate deserialized object.
     */
    protected Object getTargetObject() throws Exception {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("A", "B");
        
        CronTrigger t = new CronTrigger("test", "testgroup", "0 0 12 * * ?");
        t.setCalendarName("MyCalendar");
        t.setDescription("CronTriggerDesc");
        t.setJobDataMap(jobDataMap);
        t.setVolatility(true);

        t.addTriggerListener("L1");
        t.addTriggerListener("L2");
        
        return t;
    }
    
        
    /**
     * Verify that the target object and the object we just deserialized 
     * match.
     */
    protected void verifyMatch(Object target, Object deserialized) {
        CronTrigger targetCronTrigger = (CronTrigger)target;
        CronTrigger deserializedCronTrigger = (CronTrigger)deserialized;

        assertNotNull(deserializedCronTrigger);
        assertEquals(targetCronTrigger.getName(), deserializedCronTrigger.getName());
        assertEquals(targetCronTrigger.getGroup(), deserializedCronTrigger.getGroup());
        assertEquals(targetCronTrigger.getJobName(), deserializedCronTrigger.getJobName());
        assertEquals(targetCronTrigger.getJobGroup(), deserializedCronTrigger.getJobGroup());
//        assertEquals(targetCronTrigger.getStartTime(), deserializedCronTrigger.getStartTime());
        assertEquals(targetCronTrigger.getEndTime(), deserializedCronTrigger.getEndTime());
        assertEquals(targetCronTrigger.getCalendarName(), deserializedCronTrigger.getCalendarName());
        assertEquals(targetCronTrigger.getDescription(), deserializedCronTrigger.getDescription());
        assertEquals(targetCronTrigger.getJobDataMap(), deserializedCronTrigger.getJobDataMap());
        assertTrue(targetCronTrigger.isVolatile());
        assertEquals(2, deserializedCronTrigger.getTriggerListenerNames().length);
    }
        
    
    public void testClone() throws ParseException {
        CronTrigger trigger = new CronTrigger("test", "testgroup", "0 0 12 * * ?");
        CronTrigger trigger2 = (CronTrigger) trigger.clone();

        assertEquals( "Cloning failed", trigger, trigger2 );

        // equals() doesn't test the cron expression
        assertEquals( "Cloning failed for the cron expression", 
                      "0 0 12 * * ?", trigger2.getCronExpression()
                    );
    }

    // http://jira.opensymphony.com/browse/QUARTZ-558
    public void testQuartz558() throws ParseException {
        CronTrigger trigger = new CronTrigger("test", "testgroup");
        CronTrigger trigger2 = (CronTrigger) trigger.clone();

        assertEquals( "Cloning failed", trigger, trigger2 );
    }

}
