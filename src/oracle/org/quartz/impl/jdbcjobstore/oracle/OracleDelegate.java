/* 
 * Copyright 2004-2005 OpenSymphony 
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

/*
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.impl.jdbcjobstore.oracle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate;

/**
 * <p>
 * This is a driver delegate for the Oracle JDBC driver. To use this delegate,
 * <code>jdbcDriverVendor</code> should be configured as 'Oracle' with any
 * <code>jdbcDriverVersion</code>.
 * </p>
 * 
 * @see org.quartz.impl.jdbcjobstore.WebLogicDelegate
 * @see org.quartz.impl.jdbcjobstore.oracle.weblogic.WebLogicOracleDelegate
 * @author James House
 * @author Patrick Lightbody
 * @author Eric Mueller
 */
public class OracleDelegate extends StdJDBCDelegate {
    /**
     * <p>
     * Create new OrcaleDelegate instance.
     * </p>
     * 
     * @param logger
     *          the logger to use during execution
     * @param tablePrefix
     *          the prefix of all table names
     */
    public OracleDelegate(Log logger, String tablePrefix, String instanceId) {
        super(logger, tablePrefix, instanceId);
    }

    /**
     * <p>
     * Create new OrcaleDelegate instance.
     * </p>
     * 
     * @param logger
     *          the logger to use during execution
     * @param tablePrefix
     *          the prefix of all table names
     * @param useProperties
     *          use java.util.Properties for storage
     */
    public OracleDelegate(Log logger, String tablePrefix, String instanceId,
            Boolean useProperties) {
        super(logger, tablePrefix, instanceId, useProperties);
    }

    public static final String UPDATE_ORACLE_JOB_DETAIL = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " SET "
            + COL_DESCRIPTION + " = ?, " + COL_JOB_CLASS + " = ?, "
            + COL_IS_DURABLE + " = ?, " + COL_IS_VOLATILE + " = ?, "
            + COL_IS_STATEFUL + " = ?, " + COL_REQUESTS_RECOVERY + " = ? "
            + " WHERE " + COL_JOB_NAME + " = ? AND " + COL_JOB_GROUP + " = ?";

    public static final String UPDATE_ORACLE_JOB_DETAIL_BLOB = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " SET "
            + COL_JOB_DATAMAP + " = ? " + " WHERE " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";

    public static final String UPDATE_ORACLE_JOB_DETAIL_EMPTY_BLOB = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_JOB_DETAILS + " SET "
            + COL_JOB_DATAMAP + " = EMPTY_BLOB() " + " WHERE " + COL_JOB_NAME
            + " = ? AND " + COL_JOB_GROUP + " = ?";

    public static final String SELECT_ORACLE_JOB_DETAIL_BLOB = "SELECT "
            + COL_JOB_DATAMAP + " FROM " + TABLE_PREFIX_SUBST
            + TABLE_JOB_DETAILS + " WHERE " + COL_JOB_NAME + " = ? AND "
            + COL_JOB_GROUP + " = ? FOR UPDATE";

    public static final String UPDATE_ORACLE_TRIGGER = "UPDATE "  
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " SET " + COL_JOB_NAME  
        + " = ?, " + COL_JOB_GROUP + " = ?, " + COL_IS_VOLATILE + " = ?, "
        + COL_DESCRIPTION + " = ?, " + COL_NEXT_FIRE_TIME + " = ?, "
        + COL_PREV_FIRE_TIME + " = ?, " + COL_TRIGGER_STATE + " = ?, "
        + COL_TRIGGER_TYPE + " = ?, " + COL_START_TIME + " = ?, "
        + COL_END_TIME + " = ?, " + COL_CALENDAR_NAME + " = ?, "
        + COL_MISFIRE_INSTRUCTION + " = ?, "
        + COL_PRIORITY + " = ? WHERE " 
        + COL_TRIGGER_NAME + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    
    public static final String SELECT_ORACLE_TRIGGER_JOB_DETAIL_BLOB = "SELECT "
        + COL_JOB_DATAMAP + " FROM " + TABLE_PREFIX_SUBST
        + TABLE_TRIGGERS + " WHERE " + COL_TRIGGER_NAME + " = ? AND "
        + COL_TRIGGER_GROUP + " = ? FOR UPDATE";

    public static final String UPDATE_ORACLE_TRIGGER_JOB_DETAIL_BLOB = "UPDATE "
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " SET "
        + COL_JOB_DATAMAP + " = ? " + " WHERE " + COL_TRIGGER_NAME
        + " = ? AND " + COL_TRIGGER_GROUP + " = ?";

    public static final String UPDATE_ORACLE_TRIGGER_JOB_DETAIL_EMPTY_BLOB = "UPDATE "
        + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " SET "
        + COL_JOB_DATAMAP + " = EMPTY_BLOB() " + " WHERE " + COL_TRIGGER_NAME
        + " = ? AND " + COL_TRIGGER_GROUP + " = ?";
    
    
    public static final String INSERT_ORACLE_CALENDAR = "INSERT INTO "
            + TABLE_PREFIX_SUBST + TABLE_CALENDARS + " (" + COL_CALENDAR_NAME
            + ", " + COL_CALENDAR + ") " + " VALUES(?, EMPTY_BLOB())";

    public static final String SELECT_ORACLE_CALENDAR_BLOB = "SELECT "
            + COL_CALENDAR + " FROM " + TABLE_PREFIX_SUBST + TABLE_CALENDARS
            + " WHERE " + COL_CALENDAR_NAME + " = ? FOR UPDATE";

    public static final String UPDATE_ORACLE_CALENDAR_BLOB = "UPDATE "
            + TABLE_PREFIX_SUBST + TABLE_CALENDARS + " SET " + COL_CALENDAR
            + " = ? " + " WHERE " + COL_CALENDAR_NAME + " = ?";

    //---------------------------------------------------------------------------
    // protected methods that can be overridden by subclasses
    //---------------------------------------------------------------------------

    protected Object getObjectFromBlob(ResultSet rs, String colName)
        throws ClassNotFoundException, IOException, SQLException {
        
        Object obj = null;
        InputStream binaryInput = rs.getBinaryStream(colName);
        if (binaryInput != null) {
            ObjectInputStream in = new ObjectInputStream(binaryInput);
            try {
                obj = in.readObject();
            } finally {
                in.close();
            }
        }

        return obj;
    }

    public int insertJobDetail(Connection conn, JobDetail job)
        throws IOException, SQLException {

        ByteArrayOutputStream baos = serializeJobData(job.getJobDataMap());
        byte[] data = baos.toByteArray();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(rtp(INSERT_JOB_DETAIL));
            ps.setString(1, job.getName());
            ps.setString(2, job.getGroup());
            ps.setString(3, job.getDescription());
            ps.setString(4, job.getJobClass().getName());
            setBoolean(ps, 5, job.isDurable());
            setBoolean(ps, 6, job.isVolatile());
            setBoolean(ps, 7, job.isStateful());
            setBoolean(ps, 8, job.requestsRecovery());

            ps.setBinaryStream(9, null, 0);
            ps.executeUpdate();
            ps.close();

            ps = conn
                    .prepareStatement(rtp(UPDATE_ORACLE_JOB_DETAIL_EMPTY_BLOB));
            ps.setString(1, job.getName());
            ps.setString(2, job.getGroup());
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(rtp(SELECT_ORACLE_JOB_DETAIL_BLOB));
            ps.setString(1, job.getName());
            ps.setString(2, job.getGroup());

            rs = ps.executeQuery();

            int res = 0;

            Blob dbBlob = null;
            if (rs.next()) {
                dbBlob = writeDataToBlob(rs, 1, data);
            } else {
                return res;
            }

            rs.close();
            ps.close();

            ps = conn.prepareStatement(rtp(UPDATE_ORACLE_JOB_DETAIL_BLOB));
            ps.setBlob(1, dbBlob);
            ps.setString(2, job.getName());
            ps.setString(3, job.getGroup());

            res = ps.executeUpdate();

            if (res > 0) {
                String[] jobListeners = job.getJobListenerNames();
                for (int i = 0; jobListeners != null && i < jobListeners.length; i++) {
                    insertJobListener(conn, job, jobListeners[i]);
                }
            }

            return res;
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }

    }

    protected Object getJobDetailFromBlob(ResultSet rs, String colName)
        throws ClassNotFoundException, IOException, SQLException {
        
        if (canUseProperties()) {
            InputStream binaryInput = rs.getBinaryStream(colName);
            return binaryInput;
        }

        return getObjectFromBlob(rs, colName);
    }

    public int updateJobDetail(Connection conn, JobDetail job)
        throws IOException, SQLException {
        
        ByteArrayOutputStream baos = serializeJobData(job.getJobDataMap());
        byte[] data = baos.toByteArray();

        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(rtp(UPDATE_ORACLE_JOB_DETAIL));
            ps.setString(1, job.getDescription());
            ps.setString(2, job.getJobClass().getName());
            setBoolean(ps, 3, job.isDurable());
            setBoolean(ps, 4, job.isVolatile());
            setBoolean(ps, 5, job.isStateful());
            setBoolean(ps, 6, job.requestsRecovery());
            ps.setString(7, job.getName());
            ps.setString(8, job.getGroup());

            ps.executeUpdate();
            ps.close();

            ps = conn
                    .prepareStatement(rtp(UPDATE_ORACLE_JOB_DETAIL_EMPTY_BLOB));
            ps.setString(1, job.getName());
            ps.setString(2, job.getGroup());
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(rtp(SELECT_ORACLE_JOB_DETAIL_BLOB));
            ps.setString(1, job.getName());
            ps.setString(2, job.getGroup());

            rs = ps.executeQuery();

            int res = 0;

            if (rs.next()) {
                Blob dbBlob = writeDataToBlob(rs, 1, data);
                ps2 = conn.prepareStatement(rtp(UPDATE_ORACLE_JOB_DETAIL_BLOB));

                ps2.setBlob(1, dbBlob);
                ps2.setString(2, job.getName());
                ps2.setString(3, job.getGroup());

                res = ps2.executeUpdate();
            }

            if (res > 0) {
                deleteJobListeners(conn, job.getName(), job.getGroup());
                String[] jobListeners = job.getJobListenerNames();
                for (int i = 0; jobListeners != null && i < jobListeners.length; i++) {
                    insertJobListener(conn, job, jobListeners[i]);
                }
            }

            return res;

        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeStatement(ps2);
        }
    }

    public int insertTrigger(Connection conn, Trigger trigger, String state,
            JobDetail jobDetail) throws SQLException, IOException {

        byte[] data = null;
        if (trigger.getJobDataMap().size() > 0) {
            data = serializeJobData(trigger.getJobDataMap()).toByteArray();
        }
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        int insertResult = 0;

        try {
            ps = conn.prepareStatement(rtp(INSERT_TRIGGER));
            ps.setString(1, trigger.getName());
            ps.setString(2, trigger.getGroup());
            ps.setString(3, trigger.getJobName());
            ps.setString(4, trigger.getJobGroup());
            setBoolean(ps, 5, trigger.isVolatile());
            ps.setString(6, trigger.getDescription());
            ps.setBigDecimal(7, new BigDecimal(String.valueOf(trigger
                    .getNextFireTime().getTime())));
            long prevFireTime = -1;
            if (trigger.getPreviousFireTime() != null) {
                prevFireTime = trigger.getPreviousFireTime().getTime();
            }
            ps.setBigDecimal(8, new BigDecimal(String.valueOf(prevFireTime)));
            ps.setString(9, state);
            if (trigger instanceof SimpleTrigger && ((SimpleTrigger)trigger).hasAdditionalProperties() == false ) {
                ps.setString(10, TTYPE_SIMPLE);
            } else if (trigger instanceof CronTrigger && ((CronTrigger)trigger).hasAdditionalProperties() == false ) {
                ps.setString(10, TTYPE_CRON);
            } else {
                ps.setString(10, TTYPE_BLOB);
            }
            ps.setBigDecimal(11, new BigDecimal(String.valueOf(trigger
                    .getStartTime().getTime())));
            long endTime = 0;
            if (trigger.getEndTime() != null) {
                endTime = trigger.getEndTime().getTime();
            }
            ps.setBigDecimal(12, new BigDecimal(String.valueOf(endTime)));
            ps.setString(13, trigger.getCalendarName());
            ps.setInt(14, trigger.getMisfireInstruction());
            ps.setBinaryStream(15, null, 0);
            ps.setInt(16, trigger.getPriority());

            insertResult = ps.executeUpdate();

            if(data != null) {
                ps.close();

                ps = conn
                    .prepareStatement(rtp(UPDATE_ORACLE_TRIGGER_JOB_DETAIL_EMPTY_BLOB));
                ps.setString(1, trigger.getName());
                ps.setString(2, trigger.getGroup());
                ps.executeUpdate();
                ps.close();
        
                ps = conn.prepareStatement(rtp(SELECT_ORACLE_TRIGGER_JOB_DETAIL_BLOB));
                ps.setString(1, trigger.getName());
                ps.setString(2, trigger.getGroup());
        
                rs = ps.executeQuery();
        
                int res = 0;
        
                Blob dbBlob = null;
                if (rs.next()) {
                    dbBlob = writeDataToBlob(rs, 1, data);
                } else {
                    return res;
                }
        
                rs.close();
                ps.close();
        
                ps = conn.prepareStatement(rtp(UPDATE_ORACLE_TRIGGER_JOB_DETAIL_BLOB));
                ps.setBlob(1, dbBlob);
                ps.setString(2, trigger.getName());
                ps.setString(3, trigger.getGroup());
        
                res = ps.executeUpdate();
            }
            
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }

        if (insertResult > 0) {
            String[] trigListeners = trigger.getTriggerListenerNames();
            for (int i = 0; trigListeners != null && i < trigListeners.length; i++) {
                insertTriggerListener(conn, trigger, trigListeners[i]);
            }
        }

        return insertResult;
    }

    public int updateTrigger(Connection conn, Trigger trigger, String state,
            JobDetail jobDetail) throws SQLException, IOException {

        // save some clock cycles by unnecessarily writing job data blob ...
        boolean updateJobData = trigger.getJobDataMap().isDirty();
        byte[] data = null;
        if (updateJobData && trigger.getJobDataMap().size() > 0) {
            data = serializeJobData(trigger.getJobDataMap()).toByteArray();
        }
                
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        
        int insertResult = 0;


        try {
            ps = conn.prepareStatement(rtp(UPDATE_ORACLE_TRIGGER));
                
            ps.setString(1, trigger.getJobName());
            ps.setString(2, trigger.getJobGroup());
            setBoolean(ps, 3, trigger.isVolatile());
            ps.setString(4, trigger.getDescription());
            long nextFireTime = -1;
            if (trigger.getNextFireTime() != null) {
                nextFireTime = trigger.getNextFireTime().getTime();
            }
            ps.setBigDecimal(5, new BigDecimal(String.valueOf(nextFireTime)));
            long prevFireTime = -1;
            if (trigger.getPreviousFireTime() != null) {
                prevFireTime = trigger.getPreviousFireTime().getTime();
            }
            ps.setBigDecimal(6, new BigDecimal(String.valueOf(prevFireTime)));
            ps.setString(7, state);
            if (trigger instanceof SimpleTrigger && ((SimpleTrigger)trigger).hasAdditionalProperties() == false ) {
                //                updateSimpleTrigger(conn, (SimpleTrigger)trigger);
                ps.setString(8, TTYPE_SIMPLE);
            } else if (trigger instanceof CronTrigger && ((CronTrigger)trigger).hasAdditionalProperties() == false ) {
                //                updateCronTrigger(conn, (CronTrigger)trigger);
                ps.setString(8, TTYPE_CRON);
            } else {
                //                updateBlobTrigger(conn, trigger);
                ps.setString(8, TTYPE_BLOB);
            }
            ps.setBigDecimal(9, new BigDecimal(String.valueOf(trigger
                    .getStartTime().getTime())));
            long endTime = 0;
            if (trigger.getEndTime() != null) {
                endTime = trigger.getEndTime().getTime();
            }
            ps.setBigDecimal(10, new BigDecimal(String.valueOf(endTime)));
            ps.setString(11, trigger.getCalendarName());
            ps.setInt(12, trigger.getMisfireInstruction());
            ps.setInt(13, trigger.getPriority());
            ps.setString(14, trigger.getName());
            ps.setString(15, trigger.getGroup());

            insertResult = ps.executeUpdate();

            if(updateJobData) {
                ps.close();

                ps = conn
                        .prepareStatement(rtp(UPDATE_ORACLE_TRIGGER_JOB_DETAIL_EMPTY_BLOB));
                ps.setString(1, trigger.getName());
                ps.setString(2, trigger.getGroup());
                ps.executeUpdate();
                ps.close();

                ps = conn.prepareStatement(rtp(SELECT_ORACLE_TRIGGER_JOB_DETAIL_BLOB));
                ps.setString(1, trigger.getName());
                ps.setString(2, trigger.getGroup());

                rs = ps.executeQuery();

                int res = 0;

                if (rs.next()) {
                    Blob dbBlob = writeDataToBlob(rs, 1, data);
                    ps2 = conn.prepareStatement(rtp(UPDATE_ORACLE_TRIGGER_JOB_DETAIL_BLOB));

                    ps2.setBlob(1, dbBlob);
                    ps2.setString(2, trigger.getName());
                    ps2.setString(3, trigger.getGroup());

                    res = ps2.executeUpdate();
                }
            }

        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeStatement(ps2);
        }

        if (insertResult > 0) {
            deleteTriggerListeners(conn, trigger.getName(), trigger.getGroup());

            String[] trigListeners = trigger.getTriggerListenerNames();
            for (int i = 0; trigListeners != null && i < trigListeners.length; i++) {
                insertTriggerListener(conn, trigger, trigListeners[i]);
            }
        }

        return insertResult;
    }
    
    public int insertCalendar(Connection conn, String calendarName,
            Calendar calendar) throws IOException, SQLException {
        ByteArrayOutputStream baos = serializeObject(calendar);

        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(rtp(INSERT_ORACLE_CALENDAR));
            ps.setString(1, calendarName);

            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(rtp(SELECT_ORACLE_CALENDAR_BLOB));
            ps.setString(1, calendarName);

            rs = ps.executeQuery();

            if (rs.next()) {
                Blob dbBlob = writeDataToBlob(rs, 1, baos.toByteArray());
                ps2 = conn.prepareStatement(rtp(UPDATE_ORACLE_CALENDAR_BLOB));

                ps2.setBlob(1, dbBlob);
                ps2.setString(2, calendarName);

                return ps2.executeUpdate();
            }

            return 0;

        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeStatement(ps2);
        }
    }

    public int updateCalendar(Connection conn, String calendarName,
            Calendar calendar) throws IOException, SQLException {
        ByteArrayOutputStream baos = serializeObject(calendar);

        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(rtp(SELECT_ORACLE_CALENDAR_BLOB));
            ps.setString(1, calendarName);

            rs = ps.executeQuery();

            if (rs.next()) {
                Blob dbBlob = writeDataToBlob(rs, 1, baos.toByteArray());
                ps2 = conn.prepareStatement(rtp(UPDATE_ORACLE_CALENDAR_BLOB));

                ps2.setBlob(1, dbBlob);
                ps2.setString(2, calendarName);

                return ps2.executeUpdate();
            }

            return 0;

        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeStatement(ps2);
        }
    }

    public int updateJobData(Connection conn, JobDetail job)
        throws IOException, SQLException {
        
        ByteArrayOutputStream baos = serializeJobData(job.getJobDataMap());
        byte[] data = baos.toByteArray();

        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(rtp(SELECT_ORACLE_JOB_DETAIL_BLOB));
            ps.setString(1, job.getName());
            ps.setString(2, job.getGroup());

            rs = ps.executeQuery();

            int res = 0;

            if (rs.next()) {
                Blob dbBlob = writeDataToBlob(rs, 1, data);
                ps2 = conn.prepareStatement(rtp(UPDATE_ORACLE_JOB_DETAIL_BLOB));

                ps2.setBlob(1, dbBlob);
                ps2.setString(2, job.getName());
                ps2.setString(3, job.getGroup());

                res = ps2.executeUpdate();
            }

            return res;
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeStatement(ps2);
        }
    }

    protected Blob writeDataToBlob(ResultSet rs, int column, byte[] data) throws SQLException {

        Blob blob = rs.getBlob(column); // get blob

        if (blob == null) { 
            throw new SQLException("Driver's Blob representation is null!");
        }
        
        if (blob instanceof oracle.sql.BLOB) { // is it an oracle blob?
            ((oracle.sql.BLOB) blob).putBytes(1, data);
            return blob;
        } else {
            throw new SQLException(
                    "Driver's Blob representation is of an unsupported type: "
                            + blob.getClass().getName());
        }
    }
}

// EOF
