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

import org.quartz.impl.triggers.SimpleTriggerImpl;

public class SimpleScheduleBuilder extends ScheduleBuilder {

    private long interval;
    private int repeatCount;
    private int misfireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_SMART_POLICY;
    
    private SimpleScheduleBuilder() {
    }
    
    public static SimpleScheduleBuilder simpleSchedule() {
        return new SimpleScheduleBuilder();
    }
    
    public MutableTrigger build() {

        SimpleTriggerImpl st = new SimpleTriggerImpl();
        st.setRepeatInterval(interval);
        st.setRepeatCount(repeatCount);
        
        return st;
    }

    public SimpleScheduleBuilder withIntervalInMilliseconds(long intervalInMillis) {
        this.interval = intervalInMillis;
        return this;
    }
    
    public SimpleScheduleBuilder withIntervalInSeconds(int intervalInSeconds) {
        this.interval = intervalInSeconds * 1000L;
        return this;
    }
    
    public SimpleScheduleBuilder withIntervalInMinutes(int intervalInMinutes) {
        this.interval = intervalInMinutes * DateBuilder.MILLISECONDS_IN_MINUTE;
        return this;
    }

    public SimpleScheduleBuilder withIntervalInHours(int intervalInHours) {
        this.interval = intervalInHours * DateBuilder.MILLISECONDS_IN_HOUR;
        return this;
    }
    
    public SimpleScheduleBuilder withRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
        return this;
    }
    
    public SimpleScheduleBuilder repeatForever() {
        this.repeatCount = SimpleTrigger.REPEAT_INDEFINITELY;
        return this;
    }

    public SimpleScheduleBuilder withMisfireHandlingInstructionFireNow() {
        misfireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW;
        return this;
    }

    public SimpleScheduleBuilder withMisfireHandlingInstructionNextWithExistingCount() {
        misfireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT;
        return this;
    }
    
    public SimpleScheduleBuilder withMisfireHandlingInstructionNextWithRemainingCount() {
        misfireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT;
        return this;
    }

    public SimpleScheduleBuilder withMisfireHandlingInstructionNowWithExistingCount() {
        misfireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT;
        return this;
    }
    
    public SimpleScheduleBuilder withMisfireHandlingInstructionNowWithRemainingCount() {
        misfireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT;
        return this;
    }

}
