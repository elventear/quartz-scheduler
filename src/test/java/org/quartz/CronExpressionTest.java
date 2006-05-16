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

import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

public class CronExpressionTest extends SerializationTestSupport {
    private static final String[] VERSIONS = new String[] {"1.5.2"};

    private static final TimeZone EST_TIME_ZONE = TimeZone.getTimeZone("US/Eastern"); 

    /**
     * Get the object to serialize when generating serialized file for future
     * tests, and against which to validate deserialized object.
     */
    protected Object getTargetObject() throws ParseException {
        CronExpression cronExpression = new CronExpression("0 15 10 * * ? 2005");
        cronExpression.setTimeZone(EST_TIME_ZONE);
        
        return cronExpression;
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
        CronExpression targetCronExpression = (CronExpression)target;
        CronExpression deserializedCronExpression = (CronExpression)deserialized;
        
        assertNotNull(deserializedCronExpression);
        assertEquals(targetCronExpression.getCronExpression(), deserializedCronExpression.getCronExpression());
        assertEquals(targetCronExpression.getTimeZone(), deserializedCronExpression.getTimeZone());
    }
    
    /*
     * Test method for 'org.quartz.CronExpression.isSatisfiedBy(Date)'.
     */
    public void testIsSatisfiedBy() throws Exception {
        CronExpression cronExpression = new CronExpression("0 15 10 * * ? 2005");
        
        Calendar cal = Calendar.getInstance();
        
        cal.set(2005, Calendar.JUNE, 1, 10, 15, 0);
        assertTrue(cronExpression.isSatisfiedBy(cal.getTime()));
        
        cal.set(Calendar.YEAR, 2006);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));

        cal = Calendar.getInstance();
        cal.set(2005, Calendar.JUNE, 1, 10, 16, 0);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));

        cal = Calendar.getInstance();
        cal.set(2005, Calendar.JUNE, 1, 10, 14, 0);
        assertFalse(cronExpression.isSatisfiedBy(cal.getTime()));
    }
}
