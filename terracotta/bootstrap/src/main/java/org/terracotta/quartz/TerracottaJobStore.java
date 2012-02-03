/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.terracotta.quartz;

public class TerracottaJobStore extends AbstractTerracottaJobStore {

  @Override
  String getRealStoreClassName() {
    return "org.terracotta.quartz.PlainTerracottaJobStore";
  }

}
