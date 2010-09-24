package org.quartz;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public class CronScheduleBuilder extends ScheduleBuilder {

    private String cronExpression;
    private TimeZone tz;
    private int misfireInstruction = CronTrigger.MISFIRE_INSTRUCTION_SMART_POLICY;
    
    private CronScheduleBuilder(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    public MutableTrigger build() {

        CronTrigger ct = new CronTrigger();
        
        try {
            ct.setCronExpression(cronExpression);
        } catch (ParseException e) {
            // all methods of construction ensure the expression is valid by this point...
            throw new RuntimeException("CronExpression '" + cronExpression + 
                    "' is invalid, which should not be possible, please report bug to Quartz developers.");
        }
        ct.setTimeZone(tz);
        ct.setMisfireInstruction(misfireInstruction);
        
        return ct;
    }

    public static CronScheduleBuilder cronSchedule(String cronExpression) throws ParseException {
        CronExpression.validateExpression(cronExpression);
        return new CronScheduleBuilder(cronExpression);
    }
    
    public static CronScheduleBuilder cronScheduleDaily(int hour, int minute) {
        DateBuilder.validateHour(hour);
        DateBuilder.validateMinute(minute);

        String cronExpression = String.format("0 %d %d ? * *", minute, hour);

        return new CronScheduleBuilder(cronExpression);
    }

    public static CronScheduleBuilder cronScheduleDailyWeekly(int dayOfWeek, int hour, int minute) {
        DateBuilder.validateDayOfWeek(dayOfWeek);
        DateBuilder.validateHour(hour);
        DateBuilder.validateMinute(minute);

        String cronExpression = String.format("0 %d %d ? * %d", minute, hour, dayOfWeek);

        return new CronScheduleBuilder(cronExpression);
    }

    public static CronScheduleBuilder cronScheduleDailyMonthly(int dayOfMonth, int hour, int minute) {
        DateBuilder.validateDayOfMonth(dayOfMonth);
        DateBuilder.validateHour(hour);
        DateBuilder.validateMinute(minute);

        String cronExpression = String.format("0 %d %d %d * ?", minute, hour, dayOfMonth);

        return new CronScheduleBuilder(cronExpression);
    }
    
    // TODO: add other cronScheduleXXX() methods
    
    public CronScheduleBuilder inTimeZone(TimeZone tz) {
        this.tz = tz;
        return this;
    }
    
    public CronScheduleBuilder withMisfireHandlingInstructionDoNothing() {
        misfireInstruction = CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING;
        return this;
    }
    
    public CronScheduleBuilder withMisfireHandlingInstructionFireAndProceed() {
        misfireInstruction = CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
        return this;
    }
}
