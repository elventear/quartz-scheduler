/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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

import org.quartz.utils.Key;

public final class JobKey extends Key<JobKey> {

    public JobKey(String name) {
        super(name, null);
    }

    public JobKey(String name, String group) {
        super(name, group);
    }

    public static JobKey jobKey(String name) {
        return new JobKey(name, null);
    }
    
    public static JobKey jobKey(String name, String group) {
        return new JobKey(name, group);
    }

}
