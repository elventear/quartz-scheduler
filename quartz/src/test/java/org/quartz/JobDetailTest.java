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
import java.util.HashSet;

import junit.framework.TestCase;

/**
 * Unit test for JobDetail.
 */
public class JobDetailTest extends TestCase {
    public void testAddJobListener() {
        String[] listenerNames = new String[] {"X", "A", "B"};
        
        // Verify that a HashSet shuffles order, so we know that order test
        // below is actually testing something
        HashSet hashSet = new HashSet(Arrays.asList(listenerNames));
        assertFalse(Arrays.asList(listenerNames).equals(new ArrayList(hashSet)));
        
        JobDetail jobDetail = new JobDetail();
        for (int i = 0; i < listenerNames.length; i++) {
            jobDetail.addJobListener(listenerNames[i]);
        }

        // Make sure order was maintained
        assertEquals(Arrays.asList(listenerNames),
                     Arrays.asList(jobDetail.getJobListenerNames()));
        
        // Make sure uniqueness is enforced
        for (int i = 0; i < listenerNames.length; i++) {
            try {
                jobDetail.addJobListener(listenerNames[i]);
                fail();
            } catch (IllegalArgumentException e) {
            }
        }
    }
    
    public void testClone() {
        JobDetail jobDetail = new JobDetail();
        jobDetail.addJobListener("A");

        // verify order (see #QUARTZ-553)
        for (int i = 0; i < 10; i++) {
            jobDetail.addJobListener("A" + i);
        }
        
        JobDetail clonedJobDetail = (JobDetail)jobDetail.clone();
        assertEquals(Arrays.asList(clonedJobDetail.getJobListenerNames()),
                     Arrays.asList(jobDetail.getJobListenerNames()));
        
        jobDetail.addJobListener("B");
        
        // Verify deep clone of jobListenerNames 
        assertTrue(Arrays.asList(jobDetail.getJobListenerNames()).contains("A"));
        assertTrue(Arrays.asList(jobDetail.getJobListenerNames()).contains("B"));
        assertTrue(Arrays.asList(clonedJobDetail.getJobListenerNames()).contains("A"));
        assertFalse(Arrays.asList(clonedJobDetail.getJobListenerNames()).contains("B"));
    }
}
