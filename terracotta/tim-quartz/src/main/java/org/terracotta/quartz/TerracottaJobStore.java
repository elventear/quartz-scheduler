/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TerracottaJobStore extends PlainTerracottaJobStore {
  // This is the public facing job store type name. When used as a TIM (as opposed to express) we don't need to do
  // anything special. Express has a class of the same name which will override this one (depending on classpath of
  // course)

  public void setTcConfig(String tcConfig) {
    getLog().warn("tcConfig property ignored for non-express usage");
  }

  public void setTcConfigUrl(String tcConfigUrl) {
    getLog().warn("tcConfigUrl property ignored for non-express usage");
  }

  private Log getLog() {
    return LogFactory.getLog(getClass());
  }

}
