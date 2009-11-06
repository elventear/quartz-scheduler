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
package org.quartz.plugins.xml;


/**
 * This plugin loads XML files to add jobs and schedule them with triggers
 * as the scheduler is initialized, and can optionally periodically scan the
 * file for changes.
 *
 * @deprecated Use the <code>{@link org.quartz.plugins.xml.JobInitializationPlugin}</code>
 *  instead now that it supports multiple files.  <b>NOTE:</b> 
 *  <code>JobInitializationPluginMultiple</code> has different defaults for two properties: 
 *  "overWriteExistingJobs" and "validating" both default to true for 
 *  <code>JobInitializationPluginMultiple</code> but false for <code>JobInitializationPlugin}</code>.
 * 
 * @see org.quartz.plugins.xml.JobInitializationPlugin
 * 
 * @author Brooke Hedrick
 */
public class JobInitializationPluginMultiple extends JobInitializationPlugin {

    public JobInitializationPluginMultiple() {
        setOverWriteExistingJobs(true);
        setValidating(true);
    }
}

// EOF
