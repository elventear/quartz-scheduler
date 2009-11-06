/* 
 * Copyright 2001-2009 James House 
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

/*
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.jobs;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Inspects a file and compares whether it's "last modified date" has changed
 * since the last time it was inspected.  If the file has been updated, the
 * job invokes a "call-back" method on an identified 
 * <code>FileScanListener</code> that can be found in the 
 * <code>SchedulerContext</code>.
 * 
 * @author jhouse
 * @author pl47ypus
 * @see org.quartz.jobs.FileScanListener
 */
public class FileScanJob implements StatefulJob {

    public static final String FILE_NAME = "FILE_NAME";
    public static final String FILE_SCAN_LISTENER_NAME = "FILE_SCAN_LISTENER_NAME";
    private static final String LAST_MODIFIED_TIME = "LAST_MODIFIED_TIME";
    
    private final Log log = LogFactory.getLog(getClass());

    public FileScanJob() {
    }

    /** 
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
        SchedulerContext schedCtxt = null;
        try {
            schedCtxt = context.getScheduler().getContext();
        } catch (SchedulerException e) {
            throw new JobExecutionException("Error obtaining scheduler context.", e, false);
        }
        
        String fileName = mergedJobDataMap.getString(FILE_NAME);
        String listenerName = mergedJobDataMap.getString(FILE_SCAN_LISTENER_NAME);
        
        if(fileName == null) {
            throw new JobExecutionException("Required parameter '" + 
                    FILE_NAME + "' not found in merged JobDataMap");
        }
        if(listenerName == null) {
            throw new JobExecutionException("Required parameter '" + 
                    FILE_SCAN_LISTENER_NAME + "' not found in merged JobDataMap");
        }

        FileScanListener listener = (FileScanListener)schedCtxt.get(listenerName);
        
        if(listener == null) {
            throw new JobExecutionException("FileScanListener named '" + 
                    listenerName + "' not found in SchedulerContext");
        }
        
        long lastDate = -1;
        if(mergedJobDataMap.containsKey(LAST_MODIFIED_TIME)) {
            lastDate = mergedJobDataMap.getLong(LAST_MODIFIED_TIME);
        }
        
        long newDate = getLastModifiedDate(fileName);

        if(newDate < 0) {
            log.warn("File '"+fileName+"' does not exist.");
            return;
        }
        
        if(lastDate > 0 && (newDate != lastDate)) {
            // notify call back...
            log.info("File '"+fileName+"' updated, notifying listener.");
            listener.fileUpdated(fileName); 
        } else if (log.isDebugEnabled()) {
            log.debug("File '"+fileName+"' unchanged.");
        }
        
        // It is the JobDataMap on the JobDetail which is actually stateful
        context.getJobDetail().getJobDataMap().put(LAST_MODIFIED_TIME, newDate);
    }
    
    protected long getLastModifiedDate(String fileName) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(fileName);
        
        // Get the absolute path.
        String filePath = (resource == null) ? fileName : URLDecoder.decode(resource.getFile()); ;
        
        // If the jobs file is inside a jar point to the jar file (to get it modification date).
        // Otherwise continue as usual.
        int jarIndicator = filePath.indexOf('!');
        
        if (jarIndicator > 0) {
            filePath = filePath.substring(5, filePath.indexOf('!'));
        }

        File file = new File(filePath);
        
        if(!file.exists()) {
            return -1;
        } else {
            return file.lastModified();
        }
    }
}
