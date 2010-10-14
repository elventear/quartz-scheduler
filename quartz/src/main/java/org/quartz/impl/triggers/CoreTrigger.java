package org.quartz.impl.triggers;

import org.quartz.Trigger;

/**
 * internal interface preserved for backward compatibility 
 */
public interface CoreTrigger extends Trigger {

    public boolean hasAdditionalProperties();
    
}
