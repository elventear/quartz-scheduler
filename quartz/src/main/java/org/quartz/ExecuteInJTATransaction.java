package org.quartz;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks a {@link Job} class as one that will have its 
 * execution wrapped by a JTA Transaction. 
 *   
 * <p>If this annotation is present, Quartz will begin a JTA transaction 
 * before calling the <code>execute()</code> method, and will commit
 * the transaction if the method does not throw an exception and the
 * transaction has not had <code>setRollbackOnly()</code> called on it 
 * (otherwise the transaction will be rolled-back by Quartz).</p>
 * 
 * <p>This is essentially the same behavior as setting the configuration
 * property <code>org.quartz.scheduler.wrapJobExecutionInUserTransaction</code>
 * to <code>true</code> - except that it only affects the job that has
 * the annotation, rather than all jobs (as the property does).  If the
 * property is set to <code>true</code> and the annotation is also set,
 * then of course the annotation becomes redundant.</p> 
 * 
 * @author jhouse
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExecuteInJTATransaction {

}
