/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
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
