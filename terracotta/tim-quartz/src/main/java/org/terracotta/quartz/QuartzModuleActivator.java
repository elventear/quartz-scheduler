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
 package org.terracotta.quartz;

import org.terracotta.modules.configuration.BundleContext;
import org.terracotta.modules.configuration.TerracottaConfiguratorModule;

public class QuartzModuleActivator extends TerracottaConfiguratorModule {

  @Override
  protected void addInstrumentation(BundleContext context) {
    super.addInstrumentation(context);

    // mbeans
    config.addTunneledMBeanDomain("quartz");

    // includes
    config.addIncludePattern("org.terracotta.quartz.DefaultClusteredJobStore", true);
    config.addIncludePattern("org.terracotta.quartz.JobWrapper", true);
    config.addIncludePattern("org.terracotta.quartz.TriggerWrapper", true);
    config.addIncludePattern("org.terracotta.quartz.TimeTriggerSet", true);
    config.addIncludePattern("org.terracotta.quartz.TimeTriggerSet$TimeTrigger", true);
    config.addIncludePattern("org.terracotta.quartz.CalendarWrapper", true);
    config.addIncludePattern("org.terracotta.quartz.ClusteredTriggerKey", true);
    config.addIncludePattern("org.terracotta.quartz.ClusteredJobKey", true);
    config.addIncludePattern("org.terracotta.quartz.ClusteredKey", true);
    config.addIncludePattern("org.terracotta.quartz.FiredTrigger", true);
    config.addIncludePattern("org.terracotta.quartz.Serializer", true);
  }
}
