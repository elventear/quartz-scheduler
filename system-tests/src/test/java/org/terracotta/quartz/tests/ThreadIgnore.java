/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz.tests;

public class ThreadIgnore {
  private final String firstFramePackage;
  private final String threadNamePrefix;

  public ThreadIgnore(String threadNamePrefix, String firstFramePackage) {
    this.threadNamePrefix = threadNamePrefix;
    this.firstFramePackage = firstFramePackage;
  }

  public boolean canIgnore(SimpleThreadInfo info) {
    if (info.getName().startsWith(threadNamePrefix)) {

      String[] stack = info.getStackTraceArray();
      if (stack.length > 1) {
        String frame = stack[stack.length - 2].trim().replaceFirst("at ", "");
        if (frame.startsWith(firstFramePackage)) { return true; }
      }
    }

    return false;
  }
}