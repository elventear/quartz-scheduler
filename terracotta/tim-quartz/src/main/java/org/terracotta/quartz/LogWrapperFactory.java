/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz;

import org.apache.commons.logging.Log;
import org.slf4j.Logger;

class LogWrapperFactory {

  private static final boolean USE_SLF4J;

  static {
    boolean useSlf4j = false;
    try {
      Class.forName("org.slf4j.Logger");
      useSlf4j = true;
    } catch (Exception e) {
      //
    }

    USE_SLF4J = useSlf4j;
  }

  private LogWrapperFactory() {
    //
  }

  static LoggerWrapper getLogger(Class<?> c) {
    return getLogger(c.getName());
  }

  static LoggerWrapper getLogger(String name) {
    if (USE_SLF4J) {
      return new Slf4jImpl(name);
    } else {
      return new CommonsLoggingImpl(name);
    }
  }

  private static class Slf4jImpl implements LoggerWrapper {

    private final Logger log;

    Slf4jImpl(String name) {
      this.log = org.slf4j.LoggerFactory.getLogger(name);
    }

    public void debug(String s, Throwable throwable) {
      log.debug(s, throwable);
    }

    public void debug(String s) {
      log.debug(s);
    }

    public void error(String s, Throwable throwable) {
      log.error(s, throwable);
    }

    public void error(String s) {
      log.error(s);
    }

    public void info(String s, Throwable throwable) {
      log.info(s, throwable);
    }

    public void info(String s) {
      log.info(s);
    }

    public boolean isDebugEnabled() {
      return log.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
      return log.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
      return log.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
      return log.isTraceEnabled();
    }

    public boolean isWarnEnabled() {
      return log.isWarnEnabled();
    }

    public void trace(String s, Throwable throwable) {
      log.trace(s, throwable);
    }

    public void trace(String s) {
      log.trace(s);
    }

    public void warn(String s, Throwable throwable) {
      log.warn(s, throwable);
    }

    public void warn(String s) {
      log.warn(s);
    }

  }

  private static class CommonsLoggingImpl implements LoggerWrapper {

    private final Log log;

    CommonsLoggingImpl(String name) {
      this.log = org.apache.commons.logging.LogFactory.getLog(name);
    }

    public void debug(String arg0, Throwable arg1) {
      log.debug(arg0, arg1);
    }

    public void debug(String arg0) {
      log.debug(arg0);
    }

    public void error(String arg0, Throwable arg1) {
      log.error(arg0, arg1);
    }

    public void error(String arg0) {
      log.error(arg0);
    }

    public void info(String arg0, Throwable arg1) {
      log.info(arg0, arg1);
    }

    public void info(String arg0) {
      log.info(arg0);
    }

    public boolean isDebugEnabled() {
      return log.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
      return log.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
      return log.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
      return log.isWarnEnabled();
    }

    public void warn(String arg0, Throwable arg1) {
      log.warn(arg0, arg1);
    }

    public void warn(String arg0) {
      log.warn(arg0);
    }

    public boolean isTraceEnabled() {
      return log.isTraceEnabled();
    }

    public void trace(String msg) {
      log.trace(msg);
    }

    public void trace(String msg, Throwable t) {
      log.trace(msg, t);
    }

  }

}
