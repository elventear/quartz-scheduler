/* 
 * Copyright 2005 - 2009 Terracotta, Inc. 
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

package org.quartz.examples.example15;

import org.quartz.StatefulJob;
import org.quartz.examples.example13.SimpleRecoveryJob;

/**
 * This job has the same functionality of SimpleRecoveryJob
 * except that this job implements the StatefulJob interface
 *
 * @author Bill Kratzer
 */
public class SimpleRecoveryStatefulJob
    extends SimpleRecoveryJob
    implements StatefulJob {

    public SimpleRecoveryStatefulJob() {
        super();
    }
}