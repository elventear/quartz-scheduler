package org.quartz;

import java.io.Serializable;

public interface JobDetail extends Serializable, Cloneable {

    public JobKey getKey();

    /**
     * <p>
     * Return the description given to the <code>Job</code> instance by its
     * creator (if any).
     * </p>
     * 
     * @return null if no description was set.
     */
    public String getDescription();

    /**
     * <p>
     * Get the instance of <code>Job</code> that will be executed.
     * </p>
     */
    public Class<? extends Job> getJobClass();

    /**
     * <p>
     * Get the <code>JobDataMap</code> that is associated with the <code>Job</code>.
     * </p>
     */
    public JobDataMap getJobDataMap();

    /**
     * <p>
     * Whether or not the <code>Job</code> should remain stored after it is
     * orphaned (no <code>{@link Trigger}s</code> point to it).
     * </p>
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     * 
     * @return <code>true</code> if the Job should remain persisted after
     *         being orphaned.
     */
    public boolean isDurable();

    /**
     * @see PersistJobDataAfterExecution
     * @return whether the associated Job class carries the {@link PersistJobDataAfterExecution} annotation.
     */
    public boolean isPersistJobDataAfterExecution();

    /**
     * @see DisallowConcurrentExecution
     * @return whether the associated Job class carries the {@link DisallowConcurrentExecution} annotation.
     */
    public boolean isConcurrentExectionDisallowed();

    /**
     * <p>
     * Instructs the <code>Scheduler</code> whether or not the <code>Job</code>
     * should be re-executed if a 'recovery' or 'fail-over' situation is
     * encountered.
     * </p>
     * 
     * <p>
     * If not explicitly set, the default value is <code>false</code>.
     * </p>
     * 
     * @see JobExecutionContext#isRecovering()
     */
    public boolean requestsRecovery();

    public Object clone();
    
    /**
     * Get a {@link JobBuilder} that is configured to produce a 
     * <code>JobDetail</code> identical to this one.
     */
    public JobBuilder getJobBuilder();

}