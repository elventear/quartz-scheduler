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

import org.quartz.impl.calendar.AnnualCalendar;


/**
 * Unit test for AnnualCalendar serialization backwards compatibility.
 */
public class AnnualCalendarTest extends SerializationTestSupport {
    private static final String[] VERSIONS = new String[] {"1.5.1"};
    
    /**
     * Get the object to serialize when generating serialized file for future
     * tests, and against which to validate deserialized object.
     */
    protected Object getTargetObject() {
        AnnualCalendar c = new AnnualCalendar();
        
        c.setDescription("description");
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2005, Calendar.JANUARY, 20, 10, 5, 15);
        
        c.setDayExcluded(cal, true);
        
        return c;
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
        AnnualCalendar targetCalendar = (AnnualCalendar)target;
        AnnualCalendar deserializedCalendar = (AnnualCalendar)deserialized;
        
        assertNotNull(deserializedCalendar);
        assertEquals(deserializedCalendar.getDescription(), deserializedCalendar.getDescription());
        assertEquals(deserializedCalendar.getDaysExcluded(), deserializedCalendar.getDaysExcluded());
        assertNull(deserializedCalendar.getTimeZone());
    }
}
