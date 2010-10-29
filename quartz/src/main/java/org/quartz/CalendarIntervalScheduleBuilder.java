/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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
 * 
 */

package org.quartz;

import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.impl.triggers.CalendarIntervalTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;


public class CalendarIntervalScheduleBuilder extends ScheduleBuilder {

    private int interval;
    private IntervalUnit intervalUnit;

    private int misfireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_SMART_POLICY;
    
    private CalendarIntervalScheduleBuilder() {
    }
    
    public static CalendarIntervalScheduleBuilder calendarIntervalSchedule() {
        return new CalendarIntervalScheduleBuilder();
    }
    
    public MutableTrigger build() {

        CalendarIntervalTriggerImpl st = new CalendarIntervalTriggerImpl();
        st.setRepeatInterval(interval);
        st.setRepeatIntervalUnit(intervalUnit);
        
        return st;
    }

    public CalendarIntervalScheduleBuilder withInterval(int interval, IntervalUnit unit) {
        if(unit == null)
            throw new IllegalArgumentException("TimeUnit must be specified.");
        validateInterval(interval);
        this.interval = interval;
        this.intervalUnit = unit;
        return this;
    }

    public CalendarIntervalScheduleBuilder withIntervalInSeconds(int intervalInSeconds) {
        validateInterval(intervalInSeconds);
        this.interval = intervalInSeconds;
        this.intervalUnit = IntervalUnit.SECOND;
        return this;
    }
    
    public CalendarIntervalScheduleBuilder withIntervalInMinutes(int intervalInMinutes) {
        validateInterval(intervalInMinutes);
        this.interval = intervalInMinutes;
        this.intervalUnit = IntervalUnit.MINUTE;
        return this;
    }

    public CalendarIntervalScheduleBuilder withIntervalInHours(int intervalInHours) {
        validateInterval(intervalInHours);
        this.interval = intervalInHours;
        this.intervalUnit = IntervalUnit.HOUR;
        return this;
    }
    
    public CalendarIntervalScheduleBuilder withIntervalInDays(int intervalInDays) {
        validateInterval(intervalInDays);
        this.interval = intervalInDays;
        this.intervalUnit = IntervalUnit.DAY;
        return this;
    }

    public CalendarIntervalScheduleBuilder withIntervalInWeeks(int intervalInWeeks) {
        validateInterval(intervalInWeeks);
        this.interval = intervalInWeeks;
        this.intervalUnit = IntervalUnit.WEEK;
        return this;
    }

    public CalendarIntervalScheduleBuilder withIntervalInMonths(int intervalInMonths) {
        validateInterval(intervalInMonths);
        this.interval = intervalInMonths;
        this.intervalUnit = IntervalUnit.MONTH;
        return this;
    }

    public CalendarIntervalScheduleBuilder withIntervalInYears(int intervalInYears) {
        validateInterval(intervalInYears);
        this.interval = intervalInYears;
        this.intervalUnit = IntervalUnit.YEAR;
        return this;
    }
    
    public CalendarIntervalScheduleBuilder withMisfireHandlingInstructionDoNothing() {
        misfireInstruction = CalendarIntervalTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
        return this;
    }
    
    public CalendarIntervalScheduleBuilder withMisfireHandlingInstructionFireAndProceed() {
        misfireInstruction = CalendarIntervalTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        return this;
    }

    private void validateInterval(int interval) {
        if(interval <= 0)
            throw new IllegalArgumentException("Interval must be a positive value.");
    }
}
