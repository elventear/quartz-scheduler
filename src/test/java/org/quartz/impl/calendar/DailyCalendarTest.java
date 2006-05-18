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
package org.quartz.impl.calendar;

import junit.framework.TestCase;

/**
 * Unit test for DailyCalendar.
 */
public class DailyCalendarTest extends TestCase {
    public void testStringStartEndTimes() {
        DailyCalendar dailyCalendar = new DailyCalendar("TestCal", "1:20", "14:50");
        assertTrue(dailyCalendar.toString().indexOf("01:20:00:000 - 14:50:00:000") > 0);
        
        dailyCalendar = new DailyCalendar("TestCal", "1:20:1:456", "14:50:15:2");
        assertTrue(dailyCalendar.toString().indexOf("01:20:01:456 - 14:50:15:002") > 0);
    }
    
    public void testStringInvertTimeRange() {
        DailyCalendar dailyCalendar = new DailyCalendar("TestCal", "1:20", "14:50");
        dailyCalendar.setInvertTimeRange(true);
        assertTrue(dailyCalendar.toString().indexOf("inverted: true") > 0);

        dailyCalendar.setInvertTimeRange(false);
        assertTrue(dailyCalendar.toString().indexOf("inverted: false") > 0);
    }
}
