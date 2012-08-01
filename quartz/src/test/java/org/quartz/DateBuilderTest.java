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

import java.util.Date;
import java.util.Calendar;

import static org.quartz.DateBuilder.*;

import junit.framework.TestCase;

/**
 * Unit test for JobDetail.
 */
public class DateBuilderTest extends TestCase {
    
    public void testBasicBuilding() {
    	
    	
    	Date t = dateOf(10, 30, 0, 1, 7, 2013);  // july 1 10:30:00 am
    	
    	Calendar vc = Calendar.getInstance();
    	vc.set(Calendar.YEAR, 2013);
    	vc.set(Calendar.MONTH, Calendar.JULY);
    	vc.set(Calendar.DAY_OF_MONTH, 1);
    	vc.set(Calendar.HOUR_OF_DAY, 10);
    	vc.set(Calendar.MINUTE, 30);
    	vc.set(Calendar.SECOND, 0);
    	vc.set(Calendar.MILLISECOND, 0);
    	
    	Date v = vc.getTime();
    	
        assertEquals("DateBuilder-produced date is not as expected.", t, v);
    }
}
