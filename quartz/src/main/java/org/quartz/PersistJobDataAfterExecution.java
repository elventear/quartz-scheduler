package org.quartz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks a {@link Job} class as one that makes updates to its
 * {@link JobDataMap} during execution, and wishes the scheduler to re-store the
 * <code>JobDataMap</code> when execution completes. 
 *   
 * <p>This can be used in lieu of implementing the StatefulJob marker interface that 
 * was used prior to Quartz 2.0</p>
 *
 * @see DisallowConcurrentExecution
 * 
 * @author jhouse
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistJobDataAfterExecution {

}
