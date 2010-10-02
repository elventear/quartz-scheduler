
/* 
 * Copyright 2001-2009 Terracotta, Inc. 
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

/**
 * <p>
 * An exception that is thrown to indicate that an attempt to retrieve or update an
 * object (i.e. <code>{@link org.quartz.JobDetail}</code>,<code>{@link Trigger}</code>
 * or <code>{@link Calendar}</code>) in a <code>{@link Scheduler}</code>
 * failed, because one with the given identifier does not exists.
 * </p>
 * 
 * @author James House
 */
public class ObjectDoesNotExistException extends JobPersistenceException {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a <code>ObjectDoesNotExistException</code> with the given
     * message.
     * </p>
     */
    public ObjectDoesNotExistException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * <p>
     * Create a <code>ObjectDoesNotExistException</code> and auto-generate a
     * message using the name/group from the given <code>JobDetail</code>.
     * </p>
     */
    public ObjectDoesNotExistException(JobDetail offendingJob) {
        super("Job with identifier of name: '" + offendingJob.getName()
                + "' and group: '" + offendingJob.getGroup()
                + "', does not exist.");
    }

    /**
     * <p>
     * Create a <code>ObjectAlreadyExistsException</code> and auto-generate a
     * message using the name/group from the given <code>Trigger</code>.
     * </p>
     */
    public ObjectDoesNotExistException(Trigger offendingTrigger) {
        super("Trigger with identifier of name: '"
                + offendingTrigger.getKey().getName() + "' and group: '"
                + offendingTrigger.getKey().getGroup()
                + "', does not exist.");
    }

}
