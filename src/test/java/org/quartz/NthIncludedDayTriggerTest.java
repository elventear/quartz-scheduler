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
    
    public void testTimeZone() throws Exception {
        
        TimeZone GMT = TimeZone.getTimeZone("GMT-0:00");
        TimeZone EST = TimeZone.getTimeZone("GMT-5:00");
        
        Calendar startTime = Calendar.getInstance(EST);
        startTime.set(2006, Calendar.MARCH, 7, 7, 0, 0);
        
        // Same timezone, so should just get back 8:00 that day
        {
            NthIncludedDayTrigger t = new NthIncludedDayTrigger("name", "group");
            t.setIntervalType(NthIncludedDayTrigger.INTERVAL_TYPE_WEEKLY);
            t.setN(3);
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
            t.setN(3);
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
        t.setN(3);
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
