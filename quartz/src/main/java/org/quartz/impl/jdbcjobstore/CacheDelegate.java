/* 
 * Copyright 2001-2012 Terracotta, Inc. 
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

package org.quartz.impl.jdbcjobstore;

import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.quartz.spi.ClassLoadHelper;
import org.slf4j.Logger;

/**
 * <p>
 * This is a driver delegate for Intersystems Caché database.
 * </p>
 * 
 * <p>
 * Works with the Oracle table creation scripts / schema.
 * </p>
 * 
 * @author Franck Routier
 * @author <a href="mailto:alci@mecadu.org">Franck Routier</a>
 */
public class CacheDelegate extends StdJDBCDelegate {
		
    /**
     * <p>
     * Create new CacheDelegate instance.
     * </p>
     * 
     * @param log
     *          the logger to use during execution
     * @param tablePrefix
     *          the prefix of all table names
     */
    public CacheDelegate(Logger log, String tablePrefix, String schedName, String instanceId, ClassLoadHelper classLoadHelper) {
        super(log, tablePrefix, schedName, instanceId, classLoadHelper);
    }

    /**
     * <p>
     * Create new CacheDelegate instance.
     * </p>
     * 
     * @param log
     *          the logger to use during execution
     * @param tablePrefix
     *          the prefix of all table names
     * @param useProperties
     *          use java.util.Properties for storage
     */
    public CacheDelegate(Logger log, String tablePrefix, String schedName, String instanceId, ClassLoadHelper classLoadHelper,
            Boolean useProperties) {
        super(log, tablePrefix, schedName, instanceId, classLoadHelper, useProperties);
    }

    //---------------------------------------------------------------------------
    // protected methods that can be overridden by subclasses
    //---------------------------------------------------------------------------
  
    /**
     * Sets the designated parameter to the byte array of the given
     * <code>ByteArrayOutputStream</code>. Will set parameter value to null if the
     * <code>ByteArrayOutputStream</code> is null.
     * This just wraps <code>{@link PreparedStatement#setBytes(int, byte[])}</code>
     * by default, but it can be overloaded by subclass delegates for databases that
     * don't explicitly support storing bytes in this way.
     */
    @Override
    protected void setBytes(PreparedStatement ps, int index, ByteArrayOutputStream baos) throws SQLException {
    	ps.setObject(index, ((baos == null) ? null : baos.toByteArray()), java.sql.Types.BLOB);
    } 
}

