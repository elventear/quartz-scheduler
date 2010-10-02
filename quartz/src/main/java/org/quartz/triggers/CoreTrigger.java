package org.quartz.triggers;

import org.quartz.Trigger;

public interface CoreTrigger extends Trigger {

    public boolean hasAdditionalProperties();
    
}
