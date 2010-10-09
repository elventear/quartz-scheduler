package org.quartz;

import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.impl.triggers.DateIntervalTriggerImpl;
import org.quartz.impl.triggers.SimpleTriggerImpl;


public class DateIntervalScheduleBuilder extends ScheduleBuilder {

    private int interval;
    private IntervalUnit intervalUnit;

    private int misfireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_SMART_POLICY;
    
    private DateIntervalScheduleBuilder() {
    }
    
    public static DateIntervalScheduleBuilder simpleSchedule() {
        return new DateIntervalScheduleBuilder();
    }
    
    public MutableTrigger build() {

        DateIntervalTriggerImpl st = new DateIntervalTriggerImpl();
        st.setRepeatInterval(interval);
        st.setRepeatIntervalUnit(intervalUnit);
        
        return st;
    }

    public DateIntervalScheduleBuilder withInterval(int interval, IntervalUnit unit) {
        if(unit == null)
            throw new IllegalArgumentException("TimeUnit must be specified.");
        validateInterval(interval);
        this.interval = interval;
        this.intervalUnit = unit;
        return this;
    }

    public DateIntervalScheduleBuilder withIntervalInSeconds(int intervalInSeconds) {
        validateInterval(intervalInSeconds);
        this.interval = intervalInSeconds;
        this.intervalUnit = IntervalUnit.SECOND;
        return this;
    }
    
    public DateIntervalScheduleBuilder withIntervalInMinutes(int intervalInMinutes) {
        validateInterval(intervalInMinutes);
        this.interval = intervalInMinutes;
        this.intervalUnit = IntervalUnit.MINUTE;
        return this;
    }

    public DateIntervalScheduleBuilder withIntervalInHours(int intervalInHours) {
        validateInterval(intervalInHours);
        this.interval = intervalInHours;
        this.intervalUnit = IntervalUnit.HOUR;
        return this;
    }
    
    public DateIntervalScheduleBuilder withIntervalInDays(int intervalInDays) {
        validateInterval(intervalInDays);
        this.interval = intervalInDays;
        this.intervalUnit = IntervalUnit.DAY;
        return this;
    }

    public DateIntervalScheduleBuilder withIntervalInWeeks(int intervalInWeeks) {
        validateInterval(intervalInWeeks);
        this.interval = intervalInWeeks;
        this.intervalUnit = IntervalUnit.WEEK;
        return this;
    }

    public DateIntervalScheduleBuilder withIntervalInMonths(int intervalInMonths) {
        validateInterval(intervalInMonths);
        this.interval = intervalInMonths;
        this.intervalUnit = IntervalUnit.MONTH;
        return this;
    }

    public DateIntervalScheduleBuilder withIntervalInYears(int intervalInYears) {
        validateInterval(intervalInYears);
        this.interval = intervalInYears;
        this.intervalUnit = IntervalUnit.YEAR;
        return this;
    }
    
    public DateIntervalScheduleBuilder withMisfireHandlingInstructionDoNothing() {
        misfireInstruction = DateIntervalTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
        return this;
    }
    
    public DateIntervalScheduleBuilder withMisfireHandlingInstructionFireAndProceed() {
        misfireInstruction = DateIntervalTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        return this;
    }

    private void validateInterval(int interval) {
        if(interval <= 0)
            throw new IllegalArgumentException("Interval must be a positive value.");
    }
}
