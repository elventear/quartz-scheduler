/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz;

/**
 * <p>
 * The interface to be implemented by classes that want to be informed when a
 * <code>{@link Trigger}</code> fires. In general, applications that use a
 * <code>Scheduler</code> will not have use for this mechanism.
 * </p>
 * 
 * @see Scheduler
 * @see Trigger
 * @see JobListener
 * @see JobExecutionContext
 * 
 * @author James House
 */
public interface TriggerListener {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Get the name of the <code>TriggerListener</code>.
     * </p>
     */
    public String getName();

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has fired, and it's associated <code>{@link org.quartz.JobDetail}</code>
     * is about to be executed.
     * </p>
     * 
     * <p>
     * It is called before the <code>vetoJobExecution(..)</code> method of this
     * interface.
     * </p>
     * 
     * @param trigger
     *          The <code>Trigger</code> that has fired.
     * @param context
     *          The <code>JobExecutionContext</code> that will be passed to
     *          the <code>Job</code>'s<code>execute(xx)</code> method.
     */
    public void triggerFired(Trigger trigger, JobExecutionContext context);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has fired, and it's associated <code>{@link org.quartz.JobDetail}</code>
     * is about to be executed.
     * </p>
     * 
     * <p>
     * It is called after the <code>triggerFired(..)</code> method of this
     * interface.
     * </p>
     * 
     * @param trigger
     *          The <code>Trigger</code> that has fired.
     * @param context
     *          The <code>JobExecutionContext</code> that will be passed to
     *          the <code>Job</code>'s<code>execute(xx)</code> method.
     */
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context);

    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has misfired.
     * </p>
     * 
     * @param trigger
     *          The <code>Trigger</code> that has misfired.
     */
    public void triggerMisfired(Trigger trigger);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has fired, it's associated <code>{@link org.quartz.JobDetail}</code>
     * has been executed, and it's <code>triggered(xx)</code> method has been
     * called.
     * </p>
     * 
     * @param trigger
     *          The <code>Trigger</code> that was fired.
     * @param context
     *          The <code>JobExecutionContext</code> that was passed to the
     *          <code>Job</code>'s<code>execute(xx)</code> method.
     * @param triggerIntructionCode
     *          the result of the call on the <code>Trigger</code>'s<code>triggered(xx)</code>
     *          method.
     */
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            int triggerInstructionCode);

}
