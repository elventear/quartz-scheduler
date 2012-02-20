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
