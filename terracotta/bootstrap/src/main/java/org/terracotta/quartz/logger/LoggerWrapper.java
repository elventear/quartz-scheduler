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
package org.terracotta.quartz.logger;

public interface LoggerWrapper {

  void info(String msg);

  void info(String msg, Throwable t);

  void warn(String msg);

  void warn(String msg, Throwable t);

  void debug(String msg);

  void debug(String msg, Throwable t);

  void error(String msg);

  void error(String msg, Throwable t);

  void trace(String msg);

  void trace(String msg, Throwable t);

  boolean isDebugEnabled();

  boolean isErrorEnabled();

  boolean isInfoEnabled();

  boolean isWarnEnabled();

  boolean isTraceEnabled();
}
