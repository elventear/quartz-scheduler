/*
 * Copyright James House (c) 2001-2004
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package org.quartz.impl.jdbcjobstore.oracle;

import org.apache.commons.logging.Log;

import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handle Blobs correctly when Oracle is being used inside of Weblogic 8.1,
 * as discussed at: http://edocs.bea.com/wls/docs81/jdbc/thirdparty.html#1043705
 *  
 * @see org.quartz.impl.jdbcjobstore.WebLogicDelegate
 * @author James House
 * @author Igor Fedulov <a href="mailto:igor@fedulov.com">igor@fedulov.com</a>
 */
public class WebLogicOracleDelegate extends OracleDelegate {

    /**
     * <p>
     * Create new WebLogicOracleDelegate instance.
     * </p>
     * 
     * @param logger
     *            the logger to use during execution
     * @param tablePrefix
     *            the prefix of all table names
     */
    public WebLogicOracleDelegate(Log logger, String tablePrefix,
            String instanceId) {
        super(logger, tablePrefix, instanceId);
    }

    /**
     * <p>
     * Create new WebLogicOracleDelegate instance.
     * </p>
     * 
     * @param logger
     *            the logger to use during execution
     * @param tablePrefix
     *            the prefix of all table names
     * @param useProperties
     *            use java.util.Properties for storage
     */
    public WebLogicOracleDelegate(Log logger, String tablePrefix,
            String instanceId, Boolean useProperties) {
        super(logger, tablePrefix, instanceId, useProperties);
    }

    /**
     * Check for the Weblogic Blob wrapper, and handle accordingly...
     */
    protected Blob writeDataToBlob(ResultSet rs, int column, byte[] data) throws SQLException {
        Blob blob = rs.getBlob(column);
        
        if(blob == null) 
            throw new SQLException("Driver's Blob representation is null!");
        
        // handle thin driver's blob
        if (blob instanceof weblogic.jdbc.vendor.oracle.OracleThinBlob) { 
            ((weblogic.jdbc.vendor.oracle.OracleThinBlob) blob).putBytes(1, data);
            return blob;
        }
        // (more slowly) handle blob for wrappers of other variations of drivers...
        else if(blob.getClass().getPackage().getName().startsWith("weblogic.")) { 
            try {
                // try to find putBytes method...
                Method m = blob.getClass().getMethod("putBytes", new Class[] {long.class, byte[].class});
                m.invoke(blob, new Object[] {new Long(1), data});
            } catch (Exception e) {
                throw new SQLException("Unable to find putBytes(long,byte[]) method on blob: " + e);
            }
            return blob;
        }
        else {
            return super.writeDataToBlob(rs, column, data);
        }
    }
}