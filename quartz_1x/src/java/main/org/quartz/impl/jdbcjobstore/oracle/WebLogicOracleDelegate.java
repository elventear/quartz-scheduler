/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
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