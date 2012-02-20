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
