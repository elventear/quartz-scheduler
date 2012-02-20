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
 
package org.quartz.utils.weblogic;

import org.quartz.utils.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <p>
 * Provides connections via Weblogic's JTS driver.
 * </p>
 * 
 * @see org.quartz.utils.ConnectionProvider
 * @see org.quartz.utils.DBConnectionManager
 * @author Mohammad Rezaei
 * @author James House
 */
public class WeblogicConnectionProvider implements ConnectionProvider {

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Data members.
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  private final String             poolName;

  private weblogic.jdbc.jts.Driver driver;

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Constructors.
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  public WeblogicConnectionProvider(String poolName) {
    this.poolName = poolName;
  }

  /*
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Interface.
   * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   */

  public Connection getConnection() throws SQLException {
    try {
      if (driver == null) {
        driver = weblogic.jdbc.jts.Driver.class.newInstance();
      }

      java.sql.Connection con = null;
      con = driver.connect("jdbc:weblogic:jts:" + poolName, (java.util.Properties) null);

      return con;
    } catch (Exception e) {
      throw new SQLException("Could not get weblogic pool connection with name '" + poolName + "': "
                             + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  public void shutdown() throws SQLException {
    // do nothing
  }
}
