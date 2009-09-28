/* 
 * Copyright 2005-2009 James House 
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

package threadlock;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>
 * A dumb implementation of Job, for unit testing purposes.
 * </p>
 * 
 * @author James House
 */
public class SimpleLongJob implements Job {

    private static Log _log = LogFactory.getLog(SimpleLongJob.class);
    private static Log _crazy = LogFactory.getLog("crazy");

    private static final String COUNT = "count";
    
    private static int c = 0;
    
    /**
     * Quartz requires a public empty constructor so that the
     * scheduler can instantiate the class whenever it needs.
     */
    public SimpleLongJob() {
    }

    /**
     * <p>
     * Called by the <code>{@link org.quartz.Scheduler}</code> when a
     * <code>{@link org.quartz.Trigger}</code> fires that is associated with
     * the <code>Job</code>.
     * </p>
     * 
     * @throws JobExecutionException
     *             if there is an exception while executing the job.
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        String jobName = context.getJobDetail().getFullName();

        // if the job is recovering print a message
        if (context.isRecovering()) {
            _log.info("SimpleLongJob: " + jobName + " RECOVERING at " + new Date());
        } else {
            _log.info("SimpleLongJob: " + jobName + " starting at " + new Date());
        }
        
        c++;

        try {
            Thread.sleep(5); // delay in millis
        } catch (Exception e) {
        }

        JobDataMap data = context.getJobDetail().getJobDataMap();
        int count;
        if (data.containsKey(COUNT)) {
            count = data.getInt(COUNT);
        } else {
            count = 0;
        }
        count++;
        data.put(COUNT, count);
        
        _crazy.info("SimpleLongJob: " + jobName + "." + c + 
                " done at " + new Date() + 
                "\n Execution #" + count);
         
    }

    

}
