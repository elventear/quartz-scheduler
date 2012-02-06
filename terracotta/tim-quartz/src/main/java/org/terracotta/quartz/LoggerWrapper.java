/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz;

interface LoggerWrapper {

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
