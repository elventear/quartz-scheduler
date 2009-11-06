/* 
 * Copyright 2001-2009 James House 
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
package org.quartz.impl.jdbcjobstore;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.core.SchedulingContext;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.utils.DBConnectionManager;
import org.quartz.utils.Key;
import org.quartz.utils.TriggerStatus;


/**
 * <p>
 * Contains base functionality for JDBC-based JobStore implementations.
 * </p>
 * 
 * @author <a href="mailto:jeff@binaryfeed.org">Jeffrey Wescott</a>
 * @author James House
 */
public abstract class JobStoreSupport implements JobStore, Constants {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected static final String LOCK_TRIGGER_ACCESS = "TRIGGER_ACCESS";

    protected static final String LOCK_JOB_ACCESS = "JOB_ACCESS";

    protected static final String LOCK_CALENDAR_ACCESS = "CALENDAR_ACCESS";

    protected static final String LOCK_STATE_ACCESS = "STATE_ACCESS";

    protected static final String LOCK_MISFIRE_ACCESS = "MISFIRE_ACCESS";

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected String dsName;

    protected String tablePrefix = DEFAULT_TABLE_PREFIX;

    protected boolean useProperties = false;

    protected String instanceId;

    protected String instanceName;
    
    protected String delegateClassName;
    protected Class delegateClass = StdJDBCDelegate.class;

    protected HashMap calendarCache = new HashMap();

    private DriverDelegate delegate;

    private long misfireThreshold = 60000L; // one minute

    private boolean dontSetAutoCommitFalse = false;

    private boolean isClustered = false;

    private boolean useDBLocks = false;
    
    private boolean lockOnInsert = true;

    private Semaphore lockHandler = null; // set in initialize() method...

    private String selectWithLockSQL = null;

    private long clusterCheckinInterval = 7500L;

    private ClusterManager clusterManagementThread = null;

    private MisfireHandler misfireHandler = null;

    private ClassLoadHelper classLoadHelper;

    private SchedulerSignaler signaler;

    protected int maxToRecoverAtATime = 20;
    
    private boolean setTxIsolationLevelSequential = false;
    
    private boolean acquireTriggersWithinLock = false;
    
    private long dbRetryInterval = 10000;
    
    private boolean makeThreadsDaemons = false;

    private boolean threadsInheritInitializersClassLoadContext = false;
    private ClassLoader initializersLoader = null;
    
    private boolean doubleCheckLockMisfireHandler = true;
    
    private final Log log = LogFactory.getLog(getClass());
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Set the name of the <code>DataSource</code> that should be used for
     * performing database functions.
     * </p>
     */
    public void setDataSource(String dsName) {
        this.dsName = dsName;
    }

    /**
     * <p>
     * Get the name of the <code>DataSource</code> that should be used for
     * performing database functions.
     * </p>
     */
    public String getDataSource() {
        return dsName;
    }

    /**
     * <p>
     * Set the prefix that should be pre-pended to all table names.
     * </p>
     */
    public void setTablePrefix(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        this.tablePrefix = prefix;
    }

    /**
     * <p>
     * Get the prefix that should be pre-pended to all table names.
     * </p>
     */
    public String getTablePrefix() {
        return tablePrefix;
    }

    /**
     * <p>
     * Set whether String-only properties will be handled in JobDataMaps.
     * </p>
     */
    public void setUseProperties(String useProp) {
        if (useProp == null) {
            useProp = "false";
        }

        this.useProperties = Boolean.valueOf(useProp).booleanValue();
    }

    /**
     * <p>
     * Get whether String-only properties will be handled in JobDataMaps.
     * </p>
     */
    public boolean canUseProperties() {
        return useProperties;
    }

    /**
     * <p>
     * Set the instance Id of the Scheduler (must be unique within a cluster).
     * </p>
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * <p>
     * Get the instance Id of the Scheduler (must be unique within a cluster).
     * </p>
     */
    public String getInstanceId() {

        return instanceId;
    }

    /**
     * Set the instance name of the Scheduler (must be unique within this server instance).
     */
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    /**
     * Get the instance name of the Scheduler (must be unique within this server instance).
     */
    public String getInstanceName() {

        return instanceName;
    }

    /**
     * <p>
     * Set whether this instance is part of a cluster.
     * </p>
     */
    public void setIsClustered(boolean isClustered) {
        this.isClustered = isClustered;
    }

    /**
     * <p>
     * Get whether this instance is part of a cluster.
     * </p>
     */
    public boolean isClustered() {
        return isClustered;
    }

    /**
     * <p>
     * Get the frequency (in milliseconds) at which this instance "checks-in"
     * with the other instances of the cluster. -- Affects the rate of
     * detecting failed instances.
     * </p>
     */
    public long getClusterCheckinInterval() {
        return clusterCheckinInterval;
    }

    /**
     * <p>
     * Set the frequency (in milliseconds) at which this instance "checks-in"
     * with the other instances of the cluster. -- Affects the rate of
     * detecting failed instances.
     * </p>
     */
    public void setClusterCheckinInterval(long l) {
        clusterCheckinInterval = l;
    }

    /**
     * <p>
     * Get the maximum number of misfired triggers that the misfire handling
     * thread will try to recover at one time (within one transaction).  The
     * default is 20.
     * </p>
     */
    public int getMaxMisfiresToHandleAtATime() {
        return maxToRecoverAtATime;
    }

    /**
     * <p>
     * Set the maximum number of misfired triggers that the misfire handling
     * thread will try to recover at one time (within one transaction).  The
     * default is 20.
     * </p>
     */
    public void setMaxMisfiresToHandleAtATime(int maxToRecoverAtATime) {
        this.maxToRecoverAtATime = maxToRecoverAtATime;
    }

    /**
     * @return Returns the dbRetryInterval.
     */
    public long getDbRetryInterval() {
        return dbRetryInterval;
    }
    /**
     * @param dbRetryInterval The dbRetryInterval to set.
     */
    public void setDbRetryInterval(long dbRetryInterval) {
        this.dbRetryInterval = dbRetryInterval;
    }
    
    /**
     * <p>
     * Set whether this instance should use database-based thread
     * synchronization.
     * </p>
     */
    public void setUseDBLocks(boolean useDBLocks) {
        this.useDBLocks = useDBLocks;
    }

    /**
     * <p>
     * Get whether this instance should use database-based thread
     * synchronization.
     * </p>
     */
    public boolean getUseDBLocks() {
        return useDBLocks;
    }

    public boolean isLockOnInsert() {
        return lockOnInsert;
    }
    
    /**
     * Whether or not to obtain locks when inserting new jobs/triggers.  
     * Defaults to <code>true</code>, which is safest - some db's (such as 
     * MS SQLServer) seem to require this to avoid deadlocks under high load,
     * while others seem to do fine without.  
     * 
     * <p>Setting this property to <code>false</code> will provide a 
     * significant performance increase during the addition of new jobs 
     * and triggers.</p>
     * 
     * @param lockOnInsert
     */
    public void setLockOnInsert(boolean lockOnInsert) {
        this.lockOnInsert = lockOnInsert;
    }
    
    public long getMisfireThreshold() {
        return misfireThreshold;
    }

    /**
     * The the number of milliseconds by which a trigger must have missed its
     * next-fire-time, in order for it to be considered "misfired" and thus
     * have its misfire instruction applied.
     * 
     * @param misfireThreshold
     */
    public void setMisfireThreshold(long misfireThreshold) {
        if (misfireThreshold < 1) {
            throw new IllegalArgumentException(
                    "Misfirethreshold must be larger than 0");
        }
        this.misfireThreshold = misfireThreshold;
    }

    public boolean isDontSetAutoCommitFalse() {
        return dontSetAutoCommitFalse;
    }

    /**
     * Don't call set autocommit(false) on connections obtained from the
     * DataSource. This can be helpfull in a few situations, such as if you
     * have a driver that complains if it is called when it is already off.
     * 
     * @param b
     */
    public void setDontSetAutoCommitFalse(boolean b) {
        dontSetAutoCommitFalse = b;
    }

    public boolean isTxIsolationLevelSerializable() {
        return setTxIsolationLevelSequential;
    }

	/**
     * Set the transaction isolation level of DB connections to sequential.
     * 
     * @param b
     */
    public void setTxIsolationLevelSerializable(boolean b) {
        setTxIsolationLevelSequential = b;
    }

    /**
     * Whether or not the query and update to acquire a Trigger for firing
     * should be performed after obtaining an explicit DB lock (to avoid 
     * possible race conditions on the trigger's db row).  This is the
     * behavior prior to Quartz 1.6.3, but is considered unnecessary for most
     * databases (due to the nature of the SQL update that is performed), 
     * and therefore a superfluous performance hit.     
     */
    public boolean isAcquireTriggersWithinLock() {
		return acquireTriggersWithinLock;
	}

    /**
     * Whether or not the query and update to acquire a Trigger for firing
     * should be performed after obtaining an explicit DB lock.  This is the
     * behavior prior to Quartz 1.6.3, but is considered unnecessary for most
     * databases, and therefore a superfluous performance hit.     
     */
	public void setAcquireTriggersWithinLock(boolean acquireTriggersWithinLock) {
		this.acquireTriggersWithinLock = acquireTriggersWithinLock;
	}

    
    /**
     * <p>
     * Set the JDBC driver delegate class.
     * </p>
     * 
     * @param delegateClassName
     *          the delegate class name
     */
    public void setDriverDelegateClass(String delegateClassName)
        throws InvalidConfigurationException {
        this.delegateClassName = delegateClassName;
    }

    /**
     * <p>
     * Get the JDBC driver delegate class name.
     * </p>
     * 
     * @return the delegate class name
     */
    public String getDriverDelegateClass() {
        return delegateClassName;
    }

    public String getSelectWithLockSQL() {
        return selectWithLockSQL;
    }

    /**
     * <p>
     * set the SQL statement to use to select and lock a row in the "locks"
     * table.
     * </p>
     * 
     * @see StdRowLockSemaphore
     */
    public void setSelectWithLockSQL(String string) {
        selectWithLockSQL = string;
    }

    protected ClassLoadHelper getClassLoadHelper() {
        return classLoadHelper;
    }

    /**
     * Get whether the threads spawned by this JobStore should be
     * marked as daemon.  Possible threads include the <code>MisfireHandler</code> 
     * and the <code>ClusterManager</code>.
     * 
     * @see Thread#setDaemon(boolean)
     */
    public boolean getMakeThreadsDaemons() {
        return makeThreadsDaemons;
    }

    /**
     * Set whether the threads spawned by this JobStore should be
     * marked as daemon.  Possible threads include the <code>MisfireHandler</code> 
     * and the <code>ClusterManager</code>.
     *
     * @see Thread#setDaemon(boolean)
     */
    public void setMakeThreadsDaemons(boolean makeThreadsDaemons) {
        this.makeThreadsDaemons = makeThreadsDaemons;
    }
    
    /**
     * Get whether to set the class load context of spawned threads to that
     * of the initializing thread.
     */
    public boolean isThreadsInheritInitializersClassLoadContext() {
		return threadsInheritInitializersClassLoadContext;
	}

    /**
     * Set whether to set the class load context of spawned threads to that
     * of the initializing thread.
     */
	public void setThreadsInheritInitializersClassLoadContext(
			boolean threadsInheritInitializersClassLoadContext) {
		this.threadsInheritInitializersClassLoadContext = threadsInheritInitializersClassLoadContext;
	}

	/**
     * Get whether to check to see if there are Triggers that have misfired
     * before actually acquiring the lock to recover them.  This should be 
     * set to false if the majority of the time, there are are misfired
     * Triggers.
     */
    public boolean getDoubleCheckLockMisfireHandler() {
        return doubleCheckLockMisfireHandler;
    }

    /**
     * Set whether to check to see if there are Triggers that have misfired
     * before actually acquiring the lock to recover them.  This should be 
     * set to false if the majority of the time, there are are misfired
     * Triggers.
     */
    public void setDoubleCheckLockMisfireHandler(
            boolean doubleCheckLockMisfireHandler) {
        this.doubleCheckLockMisfireHandler = doubleCheckLockMisfireHandler;
    }
    
    //---------------------------------------------------------------------------
    // interface methods
    //---------------------------------------------------------------------------

    protected Log getLog() {
        return log;
    }

    /**
     * <p>
     * Called by the QuartzScheduler before the <code>JobStore</code> is
     * used, in order to give it a chance to initialize.
     * </p>
     */
    public void initialize(ClassLoadHelper loadHelper,
            SchedulerSignaler signaler) throws SchedulerConfigException {

        if (dsName == null) { 
            throw new SchedulerConfigException("DataSource name not set."); 
        }

        classLoadHelper = loadHelper;
        if(isThreadsInheritInitializersClassLoadContext()) {
        	log.info("JDBCJobStore threads will inherit ContextClassLoader of thread: " + Thread.currentThread().getName());
        	initializersLoader = Thread.currentThread().getContextClassLoader();
        }
        
        this.signaler = signaler;

        // If the user hasn't specified an explicit lock handler, then 
        // choose one based on CMT/Clustered/UseDBLocks.
        if (getLockHandler() == null) {
            
            // If the user hasn't specified an explicit lock handler, 
            // then we *must* use DB locks with clustering
            if (isClustered()) {
                setUseDBLocks(true);
            }
            
            if (getUseDBLocks()) {
                getLog().info(
                    "Using db table-based data access locking (synchronization).");
                setLockHandler(
                    new StdRowLockSemaphore(getTablePrefix(), getSelectWithLockSQL()));
            } else {
                getLog().info(
                    "Using thread monitor-based data access locking (synchronization).");
                setLockHandler(new SimpleSemaphore());
            }
        }

        if (!isClustered()) {
            try {
                cleanVolatileTriggerAndJobs();
            } catch (SchedulerException se) {
                throw new SchedulerConfigException(
                        "Failure occured during job recovery.", se);
            }
        }
    }
   
    /**
     * @see org.quartz.spi.JobStore#schedulerStarted()
     */
    public void schedulerStarted() throws SchedulerException {

        if (isClustered()) {
            clusterManagementThread = new ClusterManager();
            if(initializersLoader != null)
            	clusterManagementThread.setContextClassLoader(initializersLoader);
            clusterManagementThread.initialize();
        } else {
            try {
                recoverJobs();
            } catch (SchedulerException se) {
                throw new SchedulerConfigException(
                        "Failure occured during job recovery.", se);
            }
        }

        misfireHandler = new MisfireHandler();
        if(initializersLoader != null)
        	misfireHandler.setContextClassLoader(initializersLoader);
        misfireHandler.initialize();
    }
    
    /**
     * <p>
     * Called by the QuartzScheduler to inform the <code>JobStore</code> that
     * it should free up all of it's resources because the scheduler is
     * shutting down.
     * </p>
     */
    public void shutdown() {
        if (clusterManagementThread != null) {
            clusterManagementThread.shutdown();
        }

        if (misfireHandler != null) {
            misfireHandler.shutdown();
        }
        
        try {
            DBConnectionManager.getInstance().shutdown(getDataSource());
        } catch (SQLException sqle) {
            getLog().warn("Database connection shutdown unsuccessful.", sqle);
        }        
    }

    public boolean supportsPersistence() {
        return true;
    }

    //---------------------------------------------------------------------------
    // helper methods for subclasses
    //---------------------------------------------------------------------------

    protected abstract Connection getNonManagedTXConnection()
        throws JobPersistenceException;

    /**
     * Wrap the given <code>Connection</code> in a Proxy such that attributes 
     * that might be set will be restored before the connection is closed 
     * (and potentially restored to a pool).
     */
    protected Connection getAttributeRestoringConnection(Connection conn) {
        return (Connection)Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] { Connection.class },
                new AttributeRestoringConnectionInvocationHandler(conn));
    }
    
    protected Connection getConnection() throws JobPersistenceException {
        Connection conn = null;
        try {
            conn = DBConnectionManager.getInstance().getConnection(
                    getDataSource());
        } catch (SQLException sqle) {
            throw new JobPersistenceException(
                    "Failed to obtain DB connection from data source '"
                    + getDataSource() + "': " + sqle.toString(), sqle);
        } catch (Throwable e) {
            throw new JobPersistenceException(
                    "Failed to obtain DB connection from data source '"
                    + getDataSource() + "': " + e.toString(), e,
                    JobPersistenceException.ERR_PERSISTENCE_CRITICAL_FAILURE);
        }

        if (conn == null) { 
            throw new JobPersistenceException(
                "Could not get connection from DataSource '"
                + getDataSource() + "'"); 
        }

        // Protect connection attributes we might change.
        conn = getAttributeRestoringConnection(conn);

        // Set any connection connection attributes we are to override.
        try {
            if (!isDontSetAutoCommitFalse()) {
                conn.setAutoCommit(false);
            }

            if(isTxIsolationLevelSerializable()) {
                conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            }
        } catch (SQLException sqle) {
            getLog().warn("Failed to override connection auto commit/transaction isolation.", sqle);
        } catch (Throwable e) {
            try { conn.close(); } catch(Throwable tt) {}
            
            throw new JobPersistenceException(
                "Failure setting up connection.", e);
        }
    
        return conn;
    }
    
    protected void releaseLock(Connection conn, String lockName, boolean doIt) {
        if (doIt && conn != null) {
            try {
                getLockHandler().releaseLock(conn, lockName);
            } catch (LockException le) {
                getLog().error("Error returning lock: " + le.getMessage(), le);
            }
        }
    }
    
    /**
     * Removes all volatile data.
     * 
     * @throws JobPersistenceException If jobs could not be recovered.
     */
    protected void cleanVolatileTriggerAndJobs()
        throws JobPersistenceException {
        executeInNonManagedTXLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    cleanVolatileTriggerAndJobs(conn);
                }
            });
    }
    
    /**
     * <p>
     * Removes all volatile data.
     * </p>
     * 
     * @throws JobPersistenceException
     *           if jobs could not be recovered
     */
    protected void cleanVolatileTriggerAndJobs(Connection conn)
        throws JobPersistenceException {
        try {
            // find volatile jobs & triggers...
            Key[] volatileTriggers = getDelegate().selectVolatileTriggers(conn);
            Key[] volatileJobs = getDelegate().selectVolatileJobs(conn);

            for (int i = 0; i < volatileTriggers.length; i++) {
                removeTrigger(conn, null, volatileTriggers[i].getName(),
                        volatileTriggers[i].getGroup());
            }
            getLog().info(
                    "Removed " + volatileTriggers.length
                            + " Volatile Trigger(s).");

            for (int i = 0; i < volatileJobs.length; i++) {
                removeJob(conn, null, volatileJobs[i].getName(),
                        volatileJobs[i].getGroup(), true);
            }
            getLog().info(
                    "Removed " + volatileJobs.length + " Volatile Job(s).");

            // clean up any fired trigger entries
            getDelegate().deleteVolatileFiredTriggers(conn);

        } catch (Exception e) {
            throw new JobPersistenceException("Couldn't clean volatile data: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Recover any failed or misfired jobs and clean up the data store as
     * appropriate.
     * 
     * @throws JobPersistenceException if jobs could not be recovered
     */
    protected void recoverJobs() throws JobPersistenceException {
        executeInNonManagedTXLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    recoverJobs(conn);
                }
            });
    }
    
    /**
     * <p>
     * Will recover any failed or misfired jobs and clean up the data store as
     * appropriate.
     * </p>
     * 
     * @throws JobPersistenceException
     *           if jobs could not be recovered
     */
    protected void recoverJobs(Connection conn) throws JobPersistenceException {
        try {
            // update inconsistent job states
            int rows = getDelegate().updateTriggerStatesFromOtherStates(conn,
                    STATE_WAITING, STATE_ACQUIRED, STATE_BLOCKED);

            rows += getDelegate().updateTriggerStatesFromOtherStates(conn,
                        STATE_PAUSED, STATE_PAUSED_BLOCKED, STATE_PAUSED_BLOCKED);
            
            getLog().info(
                    "Freed " + rows
                            + " triggers from 'acquired' / 'blocked' state.");

            // clean up misfired jobs
            recoverMisfiredJobs(conn, true);
            
            // recover jobs marked for recovery that were not fully executed
            Trigger[] recoveringJobTriggers = getDelegate()
                    .selectTriggersForRecoveringJobs(conn);
            getLog()
                    .info(
                            "Recovering "
                                    + recoveringJobTriggers.length
                                    + " jobs that were in-progress at the time of the last shut-down.");

            for (int i = 0; i < recoveringJobTriggers.length; ++i) {
                if (jobExists(conn, recoveringJobTriggers[i].getJobName(),
                        recoveringJobTriggers[i].getJobGroup())) {
                    recoveringJobTriggers[i].computeFirstFireTime(null);
                    storeTrigger(conn, null, recoveringJobTriggers[i], null, false,
                            STATE_WAITING, false, true);
                }
            }
            getLog().info("Recovery complete.");

            // remove lingering 'complete' triggers...
            Key[] ct = getDelegate().selectTriggersInState(conn, STATE_COMPLETE);
            for(int i=0; ct != null && i < ct.length; i++) {
                removeTrigger(conn, null, ct[i].getName(), ct[i].getGroup());
            }
            getLog().info(
                "Removed " + (ct != null ? ct.length : 0)
                + " 'complete' triggers.");
            
            // clean up any fired trigger entries
            int n = getDelegate().deleteFiredTriggers(conn);
            getLog().info("Removed " + n + " stale fired job entries.");
        } catch (JobPersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new JobPersistenceException("Couldn't recover jobs: "
                    + e.getMessage(), e);
        }
    }

    protected long getMisfireTime() {
        long misfireTime = System.currentTimeMillis();
        if (getMisfireThreshold() > 0) {
            misfireTime -= getMisfireThreshold();
        }

        return (misfireTime > 0) ? misfireTime : 0;
    }

    /**
     * Helper class for returning the composite result of trying
     * to recover misfired jobs.
     */
    protected static class RecoverMisfiredJobsResult {
        public static final RecoverMisfiredJobsResult NO_OP =
            new RecoverMisfiredJobsResult(false, 0, Long.MAX_VALUE);
        
        private boolean _hasMoreMisfiredTriggers;
        private int _processedMisfiredTriggerCount;
        private long _earliestNewTime;
        
        public RecoverMisfiredJobsResult(
            boolean hasMoreMisfiredTriggers, int processedMisfiredTriggerCount, long earliestNewTime) {
            _hasMoreMisfiredTriggers = hasMoreMisfiredTriggers;
            _processedMisfiredTriggerCount = processedMisfiredTriggerCount;
            _earliestNewTime = earliestNewTime;
        }
        
        public boolean hasMoreMisfiredTriggers() {
            return _hasMoreMisfiredTriggers;
        }
        public int getProcessedMisfiredTriggerCount() {
            return _processedMisfiredTriggerCount;
        } 
        public long getEarliestNewTime() {
            return _earliestNewTime;
        } 
    }
    
    protected RecoverMisfiredJobsResult recoverMisfiredJobs(
        Connection conn, boolean recovering)
        throws JobPersistenceException, SQLException {

        // If recovering, we want to handle all of the misfired
        // triggers right away.
        int maxMisfiresToHandleAtATime = 
            (recovering) ? -1 : getMaxMisfiresToHandleAtATime();
        
        List misfiredTriggers = new ArrayList();
        long earliestNewTime = Long.MAX_VALUE;
        // We must still look for the MISFIRED state in case triggers were left 
        // in this state when upgrading to this version that does not support it. 
        boolean hasMoreMisfiredTriggers =
            getDelegate().selectMisfiredTriggersInStates(
                conn, STATE_MISFIRED, STATE_WAITING, getMisfireTime(), 
                maxMisfiresToHandleAtATime, misfiredTriggers);

        if (hasMoreMisfiredTriggers) {
            getLog().info(
                "Handling the first " + misfiredTriggers.size() +
                " triggers that missed their scheduled fire-time.  " +
                "More misfired triggers remain to be processed.");
        } else if (misfiredTriggers.size() > 0) { 
            getLog().info(
                "Handling " + misfiredTriggers.size() + 
                " trigger(s) that missed their scheduled fire-time.");
        } else {
            getLog().debug(
                "Found 0 triggers that missed their scheduled fire-time.");
            return RecoverMisfiredJobsResult.NO_OP; 
        }

        for (Iterator misfiredTriggerIter = misfiredTriggers.iterator(); misfiredTriggerIter.hasNext();) {
            Key triggerKey = (Key) misfiredTriggerIter.next();
            
            Trigger trig = 
                retrieveTrigger(conn, triggerKey.getName(), triggerKey.getGroup());

            if (trig == null) {
                continue;
            }

            doUpdateOfMisfiredTrigger(conn, null, trig, false, STATE_WAITING, recovering);

            if(trig.getNextFireTime() != null && trig.getNextFireTime().getTime() < earliestNewTime)
            	earliestNewTime = trig.getNextFireTime().getTime();
            
            signaler.notifyTriggerListenersMisfired(trig);
        }

        return new RecoverMisfiredJobsResult(
                hasMoreMisfiredTriggers, misfiredTriggers.size(), earliestNewTime);
    }

    protected boolean updateMisfiredTrigger(Connection conn,
            SchedulingContext ctxt, String triggerName, String groupName,
            String newStateIfNotComplete, boolean forceState) // TODO: probably
            // get rid of
            // this
        throws JobPersistenceException {
        try {

            Trigger trig = retrieveTrigger(conn, triggerName, groupName);

            long misfireTime = System.currentTimeMillis();
            if (getMisfireThreshold() > 0) {
                misfireTime -= getMisfireThreshold();
            }

            if (trig.getNextFireTime().getTime() > misfireTime) {
                return false;
            }

            doUpdateOfMisfiredTrigger(conn, ctxt, trig, forceState, newStateIfNotComplete, false);
            
            signaler.notifySchedulerListenersFinalized(trig);

            return true;

        } catch (Exception e) {
            throw new JobPersistenceException(
                    "Couldn't update misfired trigger '" + groupName + "."
                            + triggerName + "': " + e.getMessage(), e);
        }
    }

    private void doUpdateOfMisfiredTrigger(Connection conn, SchedulingContext ctxt, Trigger trig, boolean forceState, String newStateIfNotComplete, boolean recovering) throws JobPersistenceException {
        Calendar cal = null;
        if (trig.getCalendarName() != null) {
            cal = retrieveCalendar(conn, ctxt, trig.getCalendarName());
        }

        signaler.notifyTriggerListenersMisfired(trig);

        trig.updateAfterMisfire(cal);

        if (trig.getNextFireTime() == null) {
            storeTrigger(conn, ctxt, trig,
                null, true, STATE_COMPLETE, forceState, recovering);
        } else {
            storeTrigger(conn, ctxt, trig, null, true, newStateIfNotComplete,
                    forceState, false);
        }
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.JobDetail}</code> and <code>{@link org.quartz.Trigger}</code>.
     * </p>
     * 
     * @param newJob
     *          The <code>JobDetail</code> to be stored.
     * @param newTrigger
     *          The <code>Trigger</code> to be stored.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Job</code> with the same name/group already
     *           exists.
     */
    public void storeJobAndTrigger(final SchedulingContext ctxt, final JobDetail newJob,
            final Trigger newTrigger) 
        throws ObjectAlreadyExistsException, JobPersistenceException {
        executeInLock(
            (isLockOnInsert()) ? LOCK_TRIGGER_ACCESS : null,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    if (newJob.isVolatile() && !newTrigger.isVolatile()) {
                        JobPersistenceException jpe = 
                            new JobPersistenceException(
                                "Cannot associate non-volatile trigger with a volatile job!");
                        jpe.setErrorCode(SchedulerException.ERR_CLIENT_ERROR);
                        throw jpe;
                    }

                    storeJob(conn, ctxt, newJob, false);
                    storeTrigger(conn, ctxt, newTrigger, newJob, false,
                            Constants.STATE_WAITING, false, false);
                }
            });
    }
    
    /**
     * <p>
     * Store the given <code>{@link org.quartz.JobDetail}</code>.
     * </p>
     * 
     * @param newJob
     *          The <code>JobDetail</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Job</code> existing in the
     *          <code>JobStore</code> with the same name & group should be
     *          over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Job</code> with the same name/group already
     *           exists, and replaceExisting is set to false.
     */
    public void storeJob(final SchedulingContext ctxt, final JobDetail newJob,
        final boolean replaceExisting) throws ObjectAlreadyExistsException, JobPersistenceException {
        executeInLock(
            (isLockOnInsert() || replaceExisting) ? LOCK_TRIGGER_ACCESS : null,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    storeJob(conn, ctxt, newJob, replaceExisting);
                }
            });
    }
    
    /**
     * <p>
     * Insert or update a job.
     * </p>
     */
    protected void storeJob(Connection conn, SchedulingContext ctxt,
            JobDetail newJob, boolean replaceExisting)
        throws ObjectAlreadyExistsException, JobPersistenceException {
        if (newJob.isVolatile() && isClustered()) {
            getLog().info(
                "note: volatile jobs are effectively non-volatile in a clustered environment.");
        }

        boolean existingJob = jobExists(conn, newJob.getName(), newJob
                .getGroup());
        try {
            if (existingJob) {
                if (!replaceExisting) { 
                    throw new ObjectAlreadyExistsException(newJob); 
                }
                getDelegate().updateJobDetail(conn, newJob);
            } else {
                getDelegate().insertJobDetail(conn, newJob);
            }
        } catch (IOException e) {
            throw new JobPersistenceException("Couldn't store job: "
                    + e.getMessage(), e);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't store job: "
                    + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Check existence of a given job.
     * </p>
     */
    protected boolean jobExists(Connection conn, String jobName,
            String groupName) throws JobPersistenceException {
        try {
            return getDelegate().jobExists(conn, jobName, groupName);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't determine job existence (" + groupName + "."
                            + jobName + "): " + e.getMessage(), e);
        }
    }


    /**
     * <p>
     * Store the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     * 
     * @param newTrigger
     *          The <code>Trigger</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Trigger</code> existing in
     *          the <code>JobStore</code> with the same name & group should
     *          be over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Trigger</code> with the same name/group already
     *           exists, and replaceExisting is set to false.
     */
    public void storeTrigger(final SchedulingContext ctxt, final Trigger newTrigger,
        final boolean replaceExisting) throws ObjectAlreadyExistsException,
            JobPersistenceException {
        executeInLock(
            (isLockOnInsert() || replaceExisting) ? LOCK_TRIGGER_ACCESS : null,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    storeTrigger(conn, ctxt, newTrigger, null, replaceExisting,
                        STATE_WAITING, false, false);
                }
            });
    }
    
    /**
     * <p>
     * Insert or update a trigger.
     * </p>
     */
    protected void storeTrigger(Connection conn, SchedulingContext ctxt,
            Trigger newTrigger, JobDetail job, boolean replaceExisting, String state,
            boolean forceState, boolean recovering)
        throws ObjectAlreadyExistsException, JobPersistenceException {
        if (newTrigger.isVolatile() && isClustered()) {
            getLog().info(
                "note: volatile triggers are effectively non-volatile in a clustered environment.");
        }

        boolean existingTrigger = triggerExists(conn, newTrigger.getName(),
                newTrigger.getGroup());

        if ((existingTrigger) && (!replaceExisting)) { 
            throw new ObjectAlreadyExistsException(newTrigger); 
        }
        
        try {

            boolean shouldBepaused = false;

            if (!forceState) {
                shouldBepaused = getDelegate().isTriggerGroupPaused(
                        conn, newTrigger.getGroup());

                if(!shouldBepaused) {
                    shouldBepaused = getDelegate().isTriggerGroupPaused(conn,
                            ALL_GROUPS_PAUSED);

                    if (shouldBepaused) {
                        getDelegate().insertPausedTriggerGroup(conn, newTrigger.getGroup());
                    }
                }

                if (shouldBepaused && (state.equals(STATE_WAITING) || state.equals(STATE_ACQUIRED))) {
                    state = STATE_PAUSED;
                }
            }

            if(job == null) {
                job = getDelegate().selectJobDetail(conn,
                    newTrigger.getJobName(), newTrigger.getJobGroup(),
                    getClassLoadHelper());
            }
            if (job == null) {
                throw new JobPersistenceException("The job ("
                        + newTrigger.getFullJobName()
                        + ") referenced by the trigger does not exist.");
            }
            if (job.isVolatile() && !newTrigger.isVolatile()) {
                throw new JobPersistenceException(
                        "It does not make sense to "
                                + "associate a non-volatile Trigger with a volatile Job!");
            }

            if (job.isStateful() && !recovering) { 
                state = checkBlockedState(conn, ctxt, job.getName(), 
                        job.getGroup(), state);
            }
            
            if (existingTrigger) {
                if (newTrigger instanceof SimpleTrigger && ((SimpleTrigger)newTrigger).hasAdditionalProperties() == false ) {
                    getDelegate().updateSimpleTrigger(conn,
                            (SimpleTrigger) newTrigger);
                } else if (newTrigger instanceof CronTrigger && ((CronTrigger)newTrigger).hasAdditionalProperties() == false ) {
                    getDelegate().updateCronTrigger(conn,
                            (CronTrigger) newTrigger);
                } else {
                    getDelegate().updateBlobTrigger(conn, newTrigger);
                }
                getDelegate().updateTrigger(conn, newTrigger, state, job);
            } else {
                getDelegate().insertTrigger(conn, newTrigger, state, job);
                if (newTrigger instanceof SimpleTrigger && ((SimpleTrigger)newTrigger).hasAdditionalProperties() == false ) {
                    getDelegate().insertSimpleTrigger(conn,
                            (SimpleTrigger) newTrigger);
                } else if (newTrigger instanceof CronTrigger && ((CronTrigger)newTrigger).hasAdditionalProperties() == false ) {
                    getDelegate().insertCronTrigger(conn,
                            (CronTrigger) newTrigger);
                } else {
                    getDelegate().insertBlobTrigger(conn, newTrigger);
                }
            }
        } catch (Exception e) {
            throw new JobPersistenceException("Couldn't store trigger '" + newTrigger.getName() + "' for '" 
                    + newTrigger.getJobName() + "' job:" + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Check existence of a given trigger.
     * </p>
     */
    protected boolean triggerExists(Connection conn, String triggerName,
            String groupName) throws JobPersistenceException {
        try {
            return getDelegate().triggerExists(conn, triggerName, groupName);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't determine trigger existence (" + groupName + "."
                            + triggerName + "): " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Job}</code> with the given
     * name, and any <code>{@link org.quartz.Trigger}</code> s that reference
     * it.
     * </p>
     * 
     * <p>
     * If removal of the <code>Job</code> results in an empty group, the
     * group should be removed from the <code>JobStore</code>'s list of
     * known group names.
     * </p>
     * 
     * @param jobName
     *          The name of the <code>Job</code> to be removed.
     * @param groupName
     *          The group name of the <code>Job</code> to be removed.
     * @return <code>true</code> if a <code>Job</code> with the given name &
     *         group was found and removed from the store.
     */
    public boolean removeJob(final SchedulingContext ctxt, final String jobName,
        final String groupName) throws JobPersistenceException {
        return ((Boolean)executeInLock(
                LOCK_TRIGGER_ACCESS,
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        return removeJob(conn, ctxt, jobName, groupName, true) ? 
                                Boolean.TRUE : Boolean.FALSE;
                    }
                })).booleanValue();
    }
    
    protected boolean removeJob(Connection conn, SchedulingContext ctxt,
            String jobName, String groupName, boolean activeDeleteSafe)
        throws JobPersistenceException {

        try {
            Key[] jobTriggers = getDelegate().selectTriggerNamesForJob(conn,
                    jobName, groupName);
            for (int i = 0; i < jobTriggers.length; ++i) {
                deleteTriggerAndChildren(
                    conn, jobTriggers[i].getName(), jobTriggers[i].getGroup());
            }

            return deleteJobAndChildren(conn, ctxt, jobName, groupName);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't remove job: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Delete a job and its listeners.
     * 
     * @see #removeJob(Connection, SchedulingContext, String, String, boolean)
     * @see #removeTrigger(Connection, SchedulingContext, String, String)
     */
    private boolean deleteJobAndChildren(Connection conn, 
            SchedulingContext ctxt, String jobName, String groupName)
        throws NoSuchDelegateException, SQLException {
        getDelegate().deleteJobListeners(conn, jobName, groupName);

        return (getDelegate().deleteJobDetail(conn, jobName, groupName) > 0);
    }
    
    /**
     * Delete a trigger, its listeners, and its Simple/Cron/BLOB sub-table entry.
     * 
     * @see #removeJob(Connection, SchedulingContext, String, String, boolean)
     * @see #removeTrigger(Connection, SchedulingContext, String, String)
     * @see #replaceTrigger(Connection, SchedulingContext, String, String, Trigger)
     */
    private boolean deleteTriggerAndChildren(
            Connection conn, String triggerName, String triggerGroupName)
        throws SQLException, NoSuchDelegateException {
        DriverDelegate delegate = getDelegate();

        // Once it succeeds in deleting one sub-table entry it will not try the others.
        if ((delegate.deleteSimpleTrigger(conn, triggerName, triggerGroupName) == 0) && 
            (delegate.deleteCronTrigger(conn, triggerName, triggerGroupName) == 0)) {
            delegate.deleteBlobTrigger(conn, triggerName, triggerGroupName);
        }
        
        delegate.deleteTriggerListeners(conn, triggerName, triggerGroupName);
        
        return (delegate.deleteTrigger(conn, triggerName, triggerGroupName) > 0);
    }
    
    /**
     * <p>
     * Retrieve the <code>{@link org.quartz.JobDetail}</code> for the given
     * <code>{@link org.quartz.Job}</code>.
     * </p>
     * 
     * @param jobName
     *          The name of the <code>Job</code> to be retrieved.
     * @param groupName
     *          The group name of the <code>Job</code> to be retrieved.
     * @return The desired <code>Job</code>, or null if there is no match.
     */
    public JobDetail retrieveJob(final SchedulingContext ctxt, final String jobName,
            final String groupName) throws JobPersistenceException {
        return (JobDetail)executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return retrieveJob(conn, ctxt, jobName, groupName);
                }
            });
    }
    
    protected JobDetail retrieveJob(Connection conn, SchedulingContext ctxt,
            String jobName, String groupName) throws JobPersistenceException {
        try {
            JobDetail job = getDelegate().selectJobDetail(conn, jobName,
                    groupName, getClassLoadHelper());
            if (job != null) {
                String[] listeners = getDelegate().selectJobListeners(conn,
                        jobName, groupName);
                for (int i = 0; i < listeners.length; ++i) {
                    job.addJobListener(listeners[i]);
                }
            }

            return job;
        } catch (ClassNotFoundException e) {
            throw new JobPersistenceException(
                    "Couldn't retrieve job because a required class was not found: "
                            + e.getMessage(), e,
                    SchedulerException.ERR_PERSISTENCE_JOB_DOES_NOT_EXIST);
        } catch (IOException e) {
            throw new JobPersistenceException(
                    "Couldn't retrieve job because the BLOB couldn't be deserialized: "
                            + e.getMessage(), e,
                    SchedulerException.ERR_PERSISTENCE_JOB_DOES_NOT_EXIST);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't retrieve job: "
                    + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Trigger}</code> with the
     * given name.
     * </p>
     * 
     * <p>
     * If removal of the <code>Trigger</code> results in an empty group, the
     * group should be removed from the <code>JobStore</code>'s list of
     * known group names.
     * </p>
     * 
     * <p>
     * If removal of the <code>Trigger</code> results in an 'orphaned' <code>Job</code>
     * that is not 'durable', then the <code>Job</code> should be deleted
     * also.
     * </p>
     * 
     * @param triggerName
     *          The name of the <code>Trigger</code> to be removed.
     * @param groupName
     *          The group name of the <code>Trigger</code> to be removed.
     * @return <code>true</code> if a <code>Trigger</code> with the given
     *         name & group was found and removed from the store.
     */
    public boolean removeTrigger(final SchedulingContext ctxt, final String triggerName,
        final String groupName) throws JobPersistenceException {
        return ((Boolean)executeInLock(
                LOCK_TRIGGER_ACCESS,
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        return removeTrigger(conn, ctxt, triggerName, groupName) ? 
                                Boolean.TRUE : Boolean.FALSE;
                    }
                })).booleanValue();
    }
    
    protected boolean removeTrigger(Connection conn, SchedulingContext ctxt,
            String triggerName, String groupName)
        throws JobPersistenceException {
        boolean removedTrigger = false;
        try {
            // this must be called before we delete the trigger, obviously
            JobDetail job = getDelegate().selectJobForTrigger(conn,
                    triggerName, groupName, getClassLoadHelper());

            removedTrigger = 
                deleteTriggerAndChildren(conn, triggerName, groupName);

            if (null != job && !job.isDurable()) {
                int numTriggers = getDelegate().selectNumTriggersForJob(conn,
                        job.getName(), job.getGroup());
                if (numTriggers == 0) {
                    // Don't call removeJob() because we don't want to check for
                    // triggers again.
                    deleteJobAndChildren(conn, ctxt, job.getName(), job.getGroup());
                }
            }
        } catch (ClassNotFoundException e) {
            throw new JobPersistenceException("Couldn't remove trigger: "
                    + e.getMessage(), e);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't remove trigger: "
                    + e.getMessage(), e);
        }

        return removedTrigger;
    }

    /** 
     * @see org.quartz.spi.JobStore#replaceTrigger(org.quartz.core.SchedulingContext, java.lang.String, java.lang.String, org.quartz.Trigger)
     */
    public boolean replaceTrigger(final SchedulingContext ctxt, final String triggerName, 
            final String groupName, final Trigger newTrigger) throws JobPersistenceException {
        return ((Boolean)executeInLock(
                LOCK_TRIGGER_ACCESS,
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        return replaceTrigger(conn, ctxt, triggerName, groupName, newTrigger) ? 
                                Boolean.TRUE : Boolean.FALSE;
                    }
                })).booleanValue();
    }
    
    protected boolean replaceTrigger(Connection conn, SchedulingContext ctxt,
            String triggerName, String groupName, Trigger newTrigger)
        throws JobPersistenceException {
        try {
            // this must be called before we delete the trigger, obviously
            JobDetail job = getDelegate().selectJobForTrigger(conn,
                    triggerName, groupName, getClassLoadHelper());

            if (job == null) {
                return false;
            }
            
            if (!newTrigger.getJobName().equals(job.getName()) || 
                !newTrigger.getJobGroup().equals(job.getGroup())) {
                throw new JobPersistenceException("New trigger is not related to the same job as the old trigger.");
            }
            
            boolean removedTrigger = 
                deleteTriggerAndChildren(conn, triggerName, groupName);
            
            storeTrigger(conn, ctxt, newTrigger, job, false, STATE_WAITING, false, false);

            return removedTrigger;
        } catch (ClassNotFoundException e) {
            throw new JobPersistenceException("Couldn't remove trigger: "
                    + e.getMessage(), e);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't remove trigger: "
                    + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     * 
     * @param triggerName
     *          The name of the <code>Trigger</code> to be retrieved.
     * @param groupName
     *          The group name of the <code>Trigger</code> to be retrieved.
     * @return The desired <code>Trigger</code>, or null if there is no
     *         match.
     */
    public Trigger retrieveTrigger(final SchedulingContext ctxt, final String triggerName,
        final String groupName) throws JobPersistenceException {
        return (Trigger)executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return retrieveTrigger(conn, ctxt, triggerName, groupName);
                }
            });
    }
    
    protected Trigger retrieveTrigger(Connection conn, SchedulingContext ctxt,
            String triggerName, String groupName)
        throws JobPersistenceException {
        return retrieveTrigger(conn, triggerName, groupName);
    }
    
    protected Trigger retrieveTrigger(Connection conn, String triggerName, String groupName)
        throws JobPersistenceException {
        try {
            Trigger trigger = getDelegate().selectTrigger(conn, triggerName,
                    groupName);
            if (trigger == null) {
                return null;
            }
            
            // In case Trigger was BLOB, clear out any listeners that might 
            // have been serialized.
            trigger.clearAllTriggerListeners();
            
            String[] listeners = getDelegate().selectTriggerListeners(conn,
                    triggerName, groupName);
            for (int i = 0; i < listeners.length; ++i) {
                trigger.addTriggerListener(listeners[i]);
            }

            return trigger;
        } catch (Exception e) {
            throw new JobPersistenceException("Couldn't retrieve trigger: "
                    + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Get the current state of the identified <code>{@link Trigger}</code>.
     * </p>
     * 
     * @see Trigger#STATE_NORMAL
     * @see Trigger#STATE_PAUSED
     * @see Trigger#STATE_COMPLETE
     * @see Trigger#STATE_ERROR
     * @see Trigger#STATE_NONE
     */
    public int getTriggerState(final SchedulingContext ctxt, final String triggerName,
            final String groupName) throws JobPersistenceException {
        return ((Integer)executeWithoutLock( // no locks necessary for read...
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        return new Integer(getTriggerState(conn, ctxt, triggerName, groupName));
                    }
                })).intValue();
    }
    
    public int getTriggerState(Connection conn, SchedulingContext ctxt,
            String triggerName, String groupName)
        throws JobPersistenceException {
        try {
            String ts = getDelegate().selectTriggerState(conn, triggerName,
                    groupName);

            if (ts == null) {
                return Trigger.STATE_NONE;
            }

            if (ts.equals(STATE_DELETED)) {
                return Trigger.STATE_NONE;
            }

            if (ts.equals(STATE_COMPLETE)) {
                return Trigger.STATE_COMPLETE;
            }

            if (ts.equals(STATE_PAUSED)) {
                return Trigger.STATE_PAUSED;
            }

            if (ts.equals(STATE_PAUSED_BLOCKED)) {
                return Trigger.STATE_PAUSED;
            }

            if (ts.equals(STATE_ERROR)) {
                return Trigger.STATE_ERROR;
            }

            if (ts.equals(STATE_BLOCKED)) {
                return Trigger.STATE_BLOCKED;
            }

            return Trigger.STATE_NORMAL;

        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't determine state of trigger (" + groupName + "."
                            + triggerName + "): " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.Calendar}</code>.
     * </p>
     * 
     * @param calName
     *          The name of the calendar.
     * @param calendar
     *          The <code>Calendar</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Calendar</code> existing
     *          in the <code>JobStore</code> with the same name & group
     *          should be over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Calendar</code> with the same name already
     *           exists, and replaceExisting is set to false.
     */
    public void storeCalendar(final SchedulingContext ctxt, final String calName,
        final Calendar calendar, final boolean replaceExisting, final boolean updateTriggers)
        throws ObjectAlreadyExistsException, JobPersistenceException {
        executeInLock(
            (isLockOnInsert() || updateTriggers) ? LOCK_TRIGGER_ACCESS : null,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    storeCalendar(conn, ctxt, calName, calendar, replaceExisting, updateTriggers);
                }
            });
    }
    
    protected void storeCalendar(Connection conn, SchedulingContext ctxt,
            String calName, Calendar calendar, boolean replaceExisting, boolean updateTriggers)
        throws ObjectAlreadyExistsException, JobPersistenceException {
        try {
            boolean existingCal = calendarExists(conn, calName);
            if (existingCal && !replaceExisting) { 
                throw new ObjectAlreadyExistsException(
                    "Calendar with name '" + calName + "' already exists."); 
            }

            if (existingCal) {
                if (getDelegate().updateCalendar(conn, calName, calendar) < 1) { 
                    throw new JobPersistenceException(
                        "Couldn't store calendar.  Update failed."); 
                }
                
                if(updateTriggers) {
                    Trigger[] trigs = getDelegate().selectTriggersForCalendar(conn, calName);
                    
                    for(int i=0; i < trigs.length; i++) {
                        trigs[i].updateWithNewCalendar(calendar, getMisfireThreshold());
                        storeTrigger(conn, ctxt, trigs[i], null, true, STATE_WAITING, false, false);
                    }
                }
            } else {
                if (getDelegate().insertCalendar(conn, calName, calendar) < 1) { 
                    throw new JobPersistenceException(
                        "Couldn't store calendar.  Insert failed."); 
                }
            }

            if (isClustered == false) {
                calendarCache.put(calName, calendar); // lazy-cache
            }

        } catch (IOException e) {
            throw new JobPersistenceException(
                    "Couldn't store calendar because the BLOB couldn't be serialized: "
                            + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new JobPersistenceException("Couldn't store calendar: "
                    + e.getMessage(), e);
        }catch (SQLException e) {
            throw new JobPersistenceException("Couldn't store calendar: "
                    + e.getMessage(), e);
        }
    }
    
    protected boolean calendarExists(Connection conn, String calName)
        throws JobPersistenceException {
        try {
            return getDelegate().calendarExists(conn, calName);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't determine calendar existence (" + calName + "): "
                            + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Calendar}</code> with the
     * given name.
     * </p>
     * 
     * <p>
     * If removal of the <code>Calendar</code> would result in
     * <code.Trigger</code>s pointing to non-existent calendars, then a
     * <code>JobPersistenceException</code> will be thrown.</p>
     *       *
     * @param calName The name of the <code>Calendar</code> to be removed.
     * @return <code>true</code> if a <code>Calendar</code> with the given name
     * was found and removed from the store.
     */
    public boolean removeCalendar(final SchedulingContext ctxt, final String calName)
        throws JobPersistenceException {
        return ((Boolean)executeInLock(
                LOCK_TRIGGER_ACCESS,
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        return removeCalendar(conn, ctxt, calName) ? 
                                Boolean.TRUE : Boolean.FALSE;
                    }
                })).booleanValue();
    }
    
    protected boolean removeCalendar(Connection conn, SchedulingContext ctxt,
            String calName) throws JobPersistenceException {
        try {
            if (getDelegate().calendarIsReferenced(conn, calName)) { 
                throw new JobPersistenceException(
                    "Calender cannot be removed if it referenced by a trigger!"); 
            }

            if (isClustered == false) {
                calendarCache.remove(calName);
            }

            return (getDelegate().deleteCalendar(conn, calName) > 0);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't remove calendar: "
                    + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     * 
     * @param calName
     *          The name of the <code>Calendar</code> to be retrieved.
     * @return The desired <code>Calendar</code>, or null if there is no
     *         match.
     */
    public Calendar retrieveCalendar(final SchedulingContext ctxt, final String calName)
        throws JobPersistenceException {
        return (Calendar)executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return retrieveCalendar(conn, ctxt, calName);
                }
            });
    }
    
    protected Calendar retrieveCalendar(Connection conn,
            SchedulingContext ctxt, String calName)
        throws JobPersistenceException {
        // all calendars are persistent, but we can lazy-cache them during run
        // time as long as we aren't running clustered.
        Calendar cal = (isClustered) ? null : (Calendar) calendarCache.get(calName);
        if (cal != null) {
            return cal;
        }

        try {
            cal = getDelegate().selectCalendar(conn, calName);
            if (isClustered == false) {
                calendarCache.put(calName, cal); // lazy-cache...
            }
            return cal;
        } catch (ClassNotFoundException e) {
            throw new JobPersistenceException(
                    "Couldn't retrieve calendar because a required class was not found: "
                            + e.getMessage(), e);
        } catch (IOException e) {
            throw new JobPersistenceException(
                    "Couldn't retrieve calendar because the BLOB couldn't be deserialized: "
                            + e.getMessage(), e);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't retrieve calendar: "
                    + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.Job}</code> s that are
     * stored in the <code>JobStore</code>.
     * </p>
     */
    public int getNumberOfJobs(final SchedulingContext ctxt)
        throws JobPersistenceException {
        return ((Integer)executeWithoutLock( // no locks necessary for read...
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        return new Integer(getNumberOfJobs(conn, ctxt));
                    }
                })).intValue();
    }
    
    protected int getNumberOfJobs(Connection conn, SchedulingContext ctxt)
        throws JobPersistenceException {
        try {
            return getDelegate().selectNumJobs(conn);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't obtain number of jobs: " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.Trigger}</code> s that are
     * stored in the <code>JobsStore</code>.
     * </p>
     */
    public int getNumberOfTriggers(final SchedulingContext ctxt)
        throws JobPersistenceException {
        return ((Integer)executeWithoutLock( // no locks necessary for read...
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        return new Integer(getNumberOfTriggers(conn, ctxt));
                    }
                })).intValue();
    }
    
    protected int getNumberOfTriggers(Connection conn, SchedulingContext ctxt)
        throws JobPersistenceException {
        try {
            return getDelegate().selectNumTriggers(conn);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't obtain number of triggers: " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.Calendar}</code> s that are
     * stored in the <code>JobsStore</code>.
     * </p>
     */
    public int getNumberOfCalendars(final SchedulingContext ctxt)
        throws JobPersistenceException {
        return ((Integer)executeWithoutLock( // no locks necessary for read...
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        return new Integer(getNumberOfCalendars(conn, ctxt));
                    }
                })).intValue();
    }
    
    protected int getNumberOfCalendars(Connection conn, SchedulingContext ctxt)
        throws JobPersistenceException {
        try {
            return getDelegate().selectNumCalendars(conn);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't obtain number of calendars: " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Job}</code> s that
     * have the given group name.
     * </p>
     * 
     * <p>
     * If there are no jobs in the given group name, the result should be a
     * zero-length array (not <code>null</code>).
     * </p>
     */
    public String[] getJobNames(final SchedulingContext ctxt, final String groupName)
        throws JobPersistenceException {
        return (String[])executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return getJobNames(conn, ctxt, groupName);
                }
            });
    }
    
    protected String[] getJobNames(Connection conn, SchedulingContext ctxt,
            String groupName) throws JobPersistenceException {
        String[] jobNames = null;

        try {
            jobNames = getDelegate().selectJobsInGroup(conn, groupName);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't obtain job names: "
                    + e.getMessage(), e);
        }

        return jobNames;
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Trigger}</code> s
     * that have the given group name.
     * </p>
     * 
     * <p>
     * If there are no triggers in the given group name, the result should be a
     * zero-length array (not <code>null</code>).
     * </p>
     */
    public String[] getTriggerNames(final SchedulingContext ctxt, final String groupName)
        throws JobPersistenceException {
        return (String[])executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return getTriggerNames(conn, ctxt, groupName);
                }
            });
    }
    
    protected String[] getTriggerNames(Connection conn, SchedulingContext ctxt,
            String groupName) throws JobPersistenceException {

        String[] trigNames = null;

        try {
            trigNames = getDelegate().selectTriggersInGroup(conn, groupName);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't obtain trigger names: "
                    + e.getMessage(), e);
        }

        return trigNames;
    }


    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Job}</code>
     * groups.
     * </p>
     * 
     * <p>
     * If there are no known group names, the result should be a zero-length
     * array (not <code>null</code>).
     * </p>
     */
    public String[] getJobGroupNames(final SchedulingContext ctxt)
        throws JobPersistenceException {
        return (String[])executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return getJobGroupNames(conn, ctxt);
                }
            });
    }
    
    protected String[] getJobGroupNames(Connection conn, SchedulingContext ctxt)
        throws JobPersistenceException {

        String[] groupNames = null;

        try {
            groupNames = getDelegate().selectJobGroups(conn);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't obtain job groups: "
                    + e.getMessage(), e);
        }

        return groupNames;
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Trigger}</code>
     * groups.
     * </p>
     * 
     * <p>
     * If there are no known group names, the result should be a zero-length
     * array (not <code>null</code>).
     * </p>
     */
    public String[] getTriggerGroupNames(final SchedulingContext ctxt)
        throws JobPersistenceException {
        return (String[])executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return getTriggerGroupNames(conn, ctxt);
                }
            });        
    }
    
    protected String[] getTriggerGroupNames(Connection conn,
            SchedulingContext ctxt) throws JobPersistenceException {

        String[] groupNames = null;

        try {
            groupNames = getDelegate().selectTriggerGroups(conn);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't obtain trigger groups: " + e.getMessage(), e);
        }

        return groupNames;
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Calendar}</code> s
     * in the <code>JobStore</code>.
     * </p>
     * 
     * <p>
     * If there are no Calendars in the given group name, the result should be
     * a zero-length array (not <code>null</code>).
     * </p>
     */
    public String[] getCalendarNames(final SchedulingContext ctxt)
        throws JobPersistenceException {
        return (String[])executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return getCalendarNames(conn, ctxt);
                }
            });      
    }
    
    protected String[] getCalendarNames(Connection conn, SchedulingContext ctxt)
        throws JobPersistenceException {
        try {
            return getDelegate().selectCalendars(conn);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't obtain trigger groups: " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Get all of the Triggers that are associated to the given Job.
     * </p>
     * 
     * <p>
     * If there are no matches, a zero-length array should be returned.
     * </p>
     */
    public Trigger[] getTriggersForJob(final SchedulingContext ctxt, final String jobName,
        final String groupName) throws JobPersistenceException {
        return (Trigger[])executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return getTriggersForJob(conn, ctxt, jobName, groupName);
                }
            });
    }
    
    protected Trigger[] getTriggersForJob(Connection conn,
            SchedulingContext ctxt, String jobName, String groupName)
        throws JobPersistenceException {
        Trigger[] array = null;

        try {
            array = getDelegate()
                    .selectTriggersForJob(conn, jobName, groupName);
        } catch (Exception e) {
            throw new JobPersistenceException(
                    "Couldn't obtain triggers for job: " + e.getMessage(), e);
        }

        return array;
    }

    /**
     * <p>
     * Pause the <code>{@link org.quartz.Trigger}</code> with the given name.
     * </p>
     * 
     * @see #resumeTrigger(SchedulingContext, String, String)
     */
    public void pauseTrigger(final SchedulingContext ctxt, final String triggerName,
            final String groupName) throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    pauseTrigger(conn, ctxt, triggerName, groupName);
                }
            });
    }
    
    /**
     * <p>
     * Pause the <code>{@link org.quartz.Trigger}</code> with the given name.
     * </p>
     * 
     * @see #resumeTrigger(Connection, SchedulingContext, String, String)
     */
    public void pauseTrigger(Connection conn, SchedulingContext ctxt,
            String triggerName, String groupName)
        throws JobPersistenceException {

        try {
            String oldState = getDelegate().selectTriggerState(conn,
                    triggerName, groupName);

            if (oldState.equals(STATE_WAITING)
                    || oldState.equals(STATE_ACQUIRED)) {

                getDelegate().updateTriggerState(conn, triggerName,
                        groupName, STATE_PAUSED);
            } else if (oldState.equals(STATE_BLOCKED)) {
                getDelegate().updateTriggerState(conn, triggerName,
                        groupName, STATE_PAUSED_BLOCKED);
            }
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't pause trigger '"
                    + groupName + "." + triggerName + "': " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Pause the <code>{@link org.quartz.Job}</code> with the given name - by
     * pausing all of its current <code>Trigger</code>s.
     * </p>
     * 
     * @see #resumeJob(SchedulingContext, String, String)
     */
    public void pauseJob(final SchedulingContext ctxt, final String jobName,
            final String groupName) throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    Trigger[] triggers = getTriggersForJob(conn, ctxt, jobName, groupName);
                    for (int j = 0; j < triggers.length; j++) {
                        pauseTrigger(conn, ctxt, triggers[j].getName(), triggers[j].getGroup());
                    }
                }
            });
    }
    
    /**
     * <p>
     * Pause all of the <code>{@link org.quartz.Job}s</code> in the given
     * group - by pausing all of their <code>Trigger</code>s.
     * </p>
     * 
     * @see #resumeJobGroup(SchedulingContext, String)
     */
    public void pauseJobGroup(final SchedulingContext ctxt, final String groupName)
        throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    String[] jobNames = getJobNames(conn, ctxt, groupName);

                    for (int i = 0; i < jobNames.length; i++) {
                        Trigger[] triggers = getTriggersForJob(conn, ctxt, jobNames[i], groupName);
                        for (int j = 0; j < triggers.length; j++) {
                            pauseTrigger(conn, ctxt, triggers[j].getName(), triggers[j].getGroup());
                        }
                    }
                }
            });
    }
    
    /**
     * Determines if a Trigger for the given job should be blocked.  
     * State can only transition to STATE_PAUSED_BLOCKED/STATE_BLOCKED from 
     * STATE_PAUSED/STATE_WAITING respectively.
     * 
     * @return STATE_PAUSED_BLOCKED, STATE_BLOCKED, or the currentState. 
     */
    protected String checkBlockedState(
            Connection conn, SchedulingContext ctxt, String jobName, 
            String jobGroupName, String currentState)
        throws JobPersistenceException {

        // State can only transition to BLOCKED from PAUSED or WAITING.
        if ((currentState.equals(STATE_WAITING) == false) && 
            (currentState.equals(STATE_PAUSED) == false)) {
            return currentState;
        }
        
        try {
            List lst = getDelegate().selectFiredTriggerRecordsByJob(conn,
                    jobName, jobGroupName);

            if (lst.size() > 0) {
                FiredTriggerRecord rec = (FiredTriggerRecord) lst.get(0);
                if (rec.isJobIsStateful()) { // TODO: worry about
                    // failed/recovering/volatile job
                    // states?
                    return (STATE_PAUSED.equals(currentState)) ? STATE_PAUSED_BLOCKED : STATE_BLOCKED;
                }
            }

            return currentState;
        } catch (SQLException e) {
            throw new JobPersistenceException(
                "Couldn't determine if trigger should be in a blocked state '"
                    + jobGroupName + "."
                    + jobName + "': "
                    + e.getMessage(), e);
        }

    }

    /*
     * private List findTriggersToBeBlocked(Connection conn, SchedulingContext
     * ctxt, String groupName) throws JobPersistenceException {
     * 
     * try { List blockList = new LinkedList();
     * 
     * List affectingJobs =
     * getDelegate().selectStatefulJobsOfTriggerGroup(conn, groupName);
     * 
     * Iterator itr = affectingJobs.iterator(); while(itr.hasNext()) { Key
     * jobKey = (Key) itr.next();
     * 
     * List lst = getDelegate().selectFiredTriggerRecordsByJob(conn,
     * jobKey.getName(), jobKey.getGroup());
     * 
     * This logic is BROKEN...
     * 
     * if(lst.size() > 0) { FiredTriggerRecord rec =
     * (FiredTriggerRecord)lst.get(0); if(rec.isJobIsStateful()) // TODO: worry
     * about failed/recovering/volatile job states? blockList.add(
     * rec.getTriggerKey() ); } }
     * 
     * 
     * return blockList; } catch (SQLException e) { throw new
     * JobPersistenceException ("Couldn't determine states of resumed triggers
     * in group '" + groupName + "': " + e.getMessage(), e); } }
     */

    /**
     * <p>
     * Resume (un-pause) the <code>{@link org.quartz.Trigger}</code> with the
     * given name.
     * </p>
     * 
     * <p>
     * If the <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     * 
     * @see #pauseTrigger(SchedulingContext, String, String)
     */
    public void resumeTrigger(final SchedulingContext ctxt, final String triggerName,
            final String groupName) throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    resumeTrigger(conn, ctxt, triggerName, groupName);
                }
            });
    }
    
    /**
     * <p>
     * Resume (un-pause) the <code>{@link org.quartz.Trigger}</code> with the
     * given name.
     * </p>
     * 
     * <p>
     * If the <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     * 
     * @see #pauseTrigger(Connection, SchedulingContext, String, String)
     */
    public void resumeTrigger(Connection conn, SchedulingContext ctxt,
            String triggerName, String groupName)
        throws JobPersistenceException {
        try {

            TriggerStatus status = getDelegate().selectTriggerStatus(conn,
                    triggerName, groupName);

            if (status == null || status.getNextFireTime() == null) {
                return;
            }

            boolean blocked = false;
            if(STATE_PAUSED_BLOCKED.equals(status.getStatus())) {
                blocked = true;
            }

            String newState = checkBlockedState(conn, ctxt, status.getJobKey().getName(), 
                    status.getJobKey().getGroup(), STATE_WAITING);

            boolean misfired = false;

            if (status.getNextFireTime().before(new Date())) {
                misfired = updateMisfiredTrigger(conn, ctxt, triggerName, groupName,
                    newState, true);
            }

            if(!misfired) {
                if(blocked) {
                    getDelegate().updateTriggerStateFromOtherState(conn,
                            triggerName, groupName, newState, STATE_PAUSED_BLOCKED);
                } else {
                    getDelegate().updateTriggerStateFromOtherState(conn,
                            triggerName, groupName, newState, STATE_PAUSED);
                }
            } 

        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't resume trigger '"
                    + groupName + "." + triggerName + "': " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Resume (un-pause) the <code>{@link org.quartz.Job}</code> with the
     * given name.
     * </p>
     * 
     * <p>
     * If any of the <code>Job</code>'s<code>Trigger</code> s missed one
     * or more fire-times, then the <code>Trigger</code>'s misfire
     * instruction will be applied.
     * </p>
     * 
     * @see #pauseJob(SchedulingContext, String, String)
     */
    public void resumeJob(final SchedulingContext ctxt, final String jobName,
        final String groupName) throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    Trigger[] triggers = getTriggersForJob(conn, ctxt, jobName, groupName);
                    for (int j = 0; j < triggers.length; j++) {
                        resumeTrigger(conn, ctxt, triggers[j].getName(), triggers[j].getGroup());
                    }
                }
            });
    }
    
    /**
     * <p>
     * Resume (un-pause) all of the <code>{@link org.quartz.Job}s</code> in
     * the given group.
     * </p>
     * 
     * <p>
     * If any of the <code>Job</code> s had <code>Trigger</code> s that
     * missed one or more fire-times, then the <code>Trigger</code>'s
     * misfire instruction will be applied.
     * </p>
     * 
     * @see #pauseJobGroup(SchedulingContext, String)
     */
    public void resumeJobGroup(final SchedulingContext ctxt, final String groupName)
        throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    String[] jobNames = getJobNames(conn, ctxt, groupName);

                    for (int i = 0; i < jobNames.length; i++) {
                        Trigger[] triggers = getTriggersForJob(conn, ctxt, jobNames[i], groupName);
                        for (int j = 0; j < triggers.length; j++) {
                            resumeTrigger(conn, ctxt, triggers[j].getName(), triggers[j].getGroup());
                        }
                    }
                }
            });
    }
    
    /**
     * <p>
     * Pause all of the <code>{@link org.quartz.Trigger}s</code> in the
     * given group.
     * </p>
     * 
     * @see #resumeTriggerGroup(SchedulingContext, String)
     */
    public void pauseTriggerGroup(final SchedulingContext ctxt, final String groupName)
        throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    pauseTriggerGroup(conn, ctxt, groupName);
                }
            });
    }
    
    /**
     * <p>
     * Pause all of the <code>{@link org.quartz.Trigger}s</code> in the
     * given group.
     * </p>
     * 
     * @see #resumeTriggerGroup(Connection, SchedulingContext, String)
     */
    public void pauseTriggerGroup(Connection conn, SchedulingContext ctxt,
            String groupName) throws JobPersistenceException {

        try {

            getDelegate().updateTriggerGroupStateFromOtherStates(
                    conn, groupName, STATE_PAUSED, STATE_ACQUIRED,
                    STATE_WAITING, STATE_WAITING);

            getDelegate().updateTriggerGroupStateFromOtherState(
                    conn, groupName, STATE_PAUSED_BLOCKED, STATE_BLOCKED);
            
            if (!getDelegate().isTriggerGroupPaused(conn, groupName)) {
                getDelegate().insertPausedTriggerGroup(conn, groupName);
            }

        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't pause trigger group '"
                    + groupName + "': " + e.getMessage(), e);
        }
    }

    public Set getPausedTriggerGroups(final SchedulingContext ctxt) 
        throws JobPersistenceException {
        return (Set)executeWithoutLock( // no locks necessary for read...
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    return getPausedTriggerGroups(conn, ctxt);
                }
            });
    }    
    
    /**
     * <p>
     * Pause all of the <code>{@link org.quartz.Trigger}s</code> in the
     * given group.
     * </p>
     * 
     * @see #resumeTriggerGroup(Connection, SchedulingContext, String)
     */
    public Set getPausedTriggerGroups(Connection conn, SchedulingContext ctxt) 
        throws JobPersistenceException {

        try {
            return getDelegate().selectPausedTriggerGroups(conn);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't determine paused trigger groups: " + e.getMessage(), e);
        }
    }
    
    /**
     * <p>
     * Resume (un-pause) all of the <code>{@link org.quartz.Trigger}s</code>
     * in the given group.
     * </p>
     * 
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     * 
     * @see #pauseTriggerGroup(SchedulingContext, String)
     */
    public void resumeTriggerGroup(final SchedulingContext ctxt, final String groupName)
        throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    resumeTriggerGroup(conn, ctxt, groupName);
                }
            });
    }
    
    /**
     * <p>
     * Resume (un-pause) all of the <code>{@link org.quartz.Trigger}s</code>
     * in the given group.
     * </p>
     * 
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     * 
     * @see #pauseTriggerGroup(Connection, SchedulingContext, String)
     */
    public void resumeTriggerGroup(Connection conn, SchedulingContext ctxt,
            String groupName) throws JobPersistenceException {

        try {

            getDelegate().deletePausedTriggerGroup(conn, groupName);

            String[] trigNames = getDelegate().selectTriggersInGroup(conn,
                    groupName);

            for (int i = 0; i < trigNames.length; i++) {
                resumeTrigger(conn, ctxt, trigNames[i], groupName);
            }

            // TODO: find an efficient way to resume triggers (better than the
            // above)... logic below is broken because of
            // findTriggersToBeBlocked()
            /*
             * int res =
             * getDelegate().updateTriggerGroupStateFromOtherState(conn,
             * groupName, STATE_WAITING, STATE_PAUSED);
             * 
             * if(res > 0) {
             * 
             * long misfireTime = System.currentTimeMillis();
             * if(getMisfireThreshold() > 0) misfireTime -=
             * getMisfireThreshold();
             * 
             * Key[] misfires =
             * getDelegate().selectMisfiredTriggersInGroupInState(conn,
             * groupName, STATE_WAITING, misfireTime);
             * 
             * List blockedTriggers = findTriggersToBeBlocked(conn, ctxt,
             * groupName);
             * 
             * Iterator itr = blockedTriggers.iterator(); while(itr.hasNext()) {
             * Key key = (Key)itr.next();
             * getDelegate().updateTriggerState(conn, key.getName(),
             * key.getGroup(), STATE_BLOCKED); }
             * 
             * for(int i=0; i < misfires.length; i++) {               String
             * newState = STATE_WAITING;
             * if(blockedTriggers.contains(misfires[i])) newState =
             * STATE_BLOCKED; updateMisfiredTrigger(conn, ctxt,
             * misfires[i].getName(), misfires[i].getGroup(), newState, true); } }
             */

        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't pause trigger group '"
                    + groupName + "': " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Pause all triggers - equivalent of calling <code>pauseTriggerGroup(group)</code>
     * on every group.
     * </p>
     * 
     * <p>
     * When <code>resumeAll()</code> is called (to un-pause), trigger misfire
     * instructions WILL be applied.
     * </p>
     * 
     * @see #resumeAll(SchedulingContext)
     * @see #pauseTriggerGroup(SchedulingContext, String)
     */
    public void pauseAll(final SchedulingContext ctxt) throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    pauseAll(conn, ctxt);
                }
            });
    }
    
    /**
     * <p>
     * Pause all triggers - equivalent of calling <code>pauseTriggerGroup(group)</code>
     * on every group.
     * </p>
     * 
     * <p>
     * When <code>resumeAll()</code> is called (to un-pause), trigger misfire
     * instructions WILL be applied.
     * </p>
     * 
     * @see #resumeAll(SchedulingContext)
     * @see #pauseTriggerGroup(SchedulingContext, String)
     */
    public void pauseAll(Connection conn, SchedulingContext ctxt)
        throws JobPersistenceException {

        String[] names = getTriggerGroupNames(conn, ctxt);

        for (int i = 0; i < names.length; i++) {
            pauseTriggerGroup(conn, ctxt, names[i]);
        }

        try {
            if (!getDelegate().isTriggerGroupPaused(conn, ALL_GROUPS_PAUSED)) {
                getDelegate().insertPausedTriggerGroup(conn, ALL_GROUPS_PAUSED);
            }

        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't pause all trigger groups: " + e.getMessage(), e);
        }

    }

    /**
     * <p>
     * Resume (un-pause) all triggers - equivalent of calling <code>resumeTriggerGroup(group)</code>
     * on every group.
     * </p>
     * 
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     * 
     * @see #pauseAll(SchedulingContext)
     */
    public void resumeAll(final SchedulingContext ctxt)
        throws JobPersistenceException {
        executeInLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    resumeAll(conn, ctxt);
                }
            });
    }
    
    /**
     * protected
     * <p>
     * Resume (un-pause) all triggers - equivalent of calling <code>resumeTriggerGroup(group)</code>
     * on every group.
     * </p>
     * 
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     * 
     * @see #pauseAll(SchedulingContext)
     */
    public void resumeAll(Connection conn, SchedulingContext ctxt)
        throws JobPersistenceException {

        String[] names = getTriggerGroupNames(conn, ctxt);

        for (int i = 0; i < names.length; i++) {
            resumeTriggerGroup(conn, ctxt, names[i]);
        }

        try {
            getDelegate().deletePausedTriggerGroup(conn, ALL_GROUPS_PAUSED);
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't resume all trigger groups: " + e.getMessage(), e);
        }
    }

    private static long ftrCtr = System.currentTimeMillis();

    protected synchronized String getFiredTriggerRecordId() {
        return getInstanceId() + ftrCtr++;
    }

    /**
     * <p>
     * Get a handle to the next N triggers to be fired, and mark them as 'reserved'
     * by the calling scheduler.
     * </p>
     * 
     * @see #releaseAcquiredTrigger(SchedulingContext, Trigger)
     */
    public Trigger acquireNextTrigger(final SchedulingContext ctxt, final long noLaterThan)
        throws JobPersistenceException {
    	
    	if(isAcquireTriggersWithinLock()) { // behavior before Quartz 1.6.3 release
	        return (Trigger)executeInNonManagedTXLock(
	                LOCK_TRIGGER_ACCESS,
	                new TransactionCallback() {
	                    public Object execute(Connection conn) throws JobPersistenceException {
	                        return acquireNextTrigger(conn, ctxt, noLaterThan);
	                    }
	                });
    	}
    	else { // default behavior since Quartz 1.6.3 release
	        return (Trigger)executeInNonManagedTXLock(
	                null, /* passing null as lock name causes no lock to be made */
	                new TransactionCallback() {
	                    public Object execute(Connection conn) throws JobPersistenceException {
	                        return acquireNextTrigger(conn, ctxt, noLaterThan);
	                    }
	                });
    	}
    }
    
    // TODO: this really ought to return something like a FiredTriggerBundle,
    // so that the fireInstanceId doesn't have to be on the trigger...
    protected Trigger acquireNextTrigger(Connection conn, SchedulingContext ctxt, long noLaterThan)
        throws JobPersistenceException {
        do {
            try {
            	Trigger nextTrigger = null;
            	
            	List keys = getDelegate().selectTriggerToAcquire(conn, noLaterThan, getMisfireTime());

            	// No trigger is ready to fire yet.
            	if (keys == null || keys.size() == 0)
            		return null;
            	
            	Iterator itr = keys.iterator();
            	while(itr.hasNext()) {
	                Key triggerKey = (Key) itr.next();
	
	                int rowsUpdated = 
	                    getDelegate().updateTriggerStateFromOtherState(
	                        conn,
	                        triggerKey.getName(), triggerKey.getGroup(), 
	                        STATE_ACQUIRED, STATE_WAITING);
	
	                // If our trigger was no longer in the expected state, try a new one.
	                if (rowsUpdated <= 0) {
	                    continue;
	                }
	
	                nextTrigger = 
	                    retrieveTrigger(conn, ctxt, triggerKey.getName(), triggerKey.getGroup());
	
	                // If our trigger is no longer available, try a new one.
	                if(nextTrigger == null) {
	                    continue;
	                }
	                
	                break;
            	}

            	// if we didn't end up with a trigger to fire from that first
            	// batch, try again for another batch
            	if(nextTrigger == null) {
                    continue;
                }
            	
                nextTrigger.setFireInstanceId(getFiredTriggerRecordId());
                getDelegate().insertFiredTrigger(conn, nextTrigger, STATE_ACQUIRED, null);
                
                return nextTrigger;
            } catch (Exception e) {
                throw new JobPersistenceException(
                          "Couldn't acquire next trigger: " + e.getMessage(), e);
            }
        } while (true);
    }
    
    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler no longer plans to
     * fire the given <code>Trigger</code>, that it had previously acquired
     * (reserved).
     * </p>
     */
    public void releaseAcquiredTrigger(final SchedulingContext ctxt, final Trigger trigger)
        throws JobPersistenceException {
        executeInNonManagedTXLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    releaseAcquiredTrigger(conn, ctxt, trigger);
                }
            });
    }
    
    protected void releaseAcquiredTrigger(Connection conn,
            SchedulingContext ctxt, Trigger trigger)
        throws JobPersistenceException {
        try {
            getDelegate().updateTriggerStateFromOtherState(conn,
                    trigger.getName(), trigger.getGroup(), STATE_WAITING,
                    STATE_ACQUIRED);
            getDelegate().deleteFiredTrigger(conn, trigger.getFireInstanceId());
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't release acquired trigger: " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler is now firing the
     * given <code>Trigger</code> (executing its associated <code>Job</code>),
     * that it had previously acquired (reserved).
     * </p>
     * 
     * @return null if the trigger or its job or calendar no longer exist, or
     *         if the trigger was not successfully put into the 'executing'
     *         state.
     */
    public TriggerFiredBundle triggerFired(
            final SchedulingContext ctxt, final Trigger trigger) throws JobPersistenceException {
        return 
            (TriggerFiredBundle)executeInNonManagedTXLock(
                LOCK_TRIGGER_ACCESS,
                new TransactionCallback() {
                    public Object execute(Connection conn) throws JobPersistenceException {
                        try {
                            return triggerFired(conn, ctxt, trigger);
                        } catch (JobPersistenceException jpe) {
                            // If job didn't exisit, we still want to commit our work and return null.
                            if (jpe.getErrorCode() == SchedulerException.ERR_PERSISTENCE_JOB_DOES_NOT_EXIST) {
                                return null;
                            } else {
                                throw jpe;
                            }
                        }
                    }
                });
    }

    protected TriggerFiredBundle triggerFired(Connection conn,
            SchedulingContext ctxt, Trigger trigger)
        throws JobPersistenceException {
        JobDetail job = null;
        Calendar cal = null;

        // Make sure trigger wasn't deleted, paused, or completed...
        try { // if trigger was deleted, state will be STATE_DELETED
            String state = getDelegate().selectTriggerState(conn,
                    trigger.getName(), trigger.getGroup());
            if (!state.equals(STATE_ACQUIRED)) {
                return null;
            }
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't select trigger state: "
                    + e.getMessage(), e);
        }

        try {
            job = retrieveJob(conn, ctxt, trigger.getJobName(), trigger
                    .getJobGroup());
            if (job == null) { return null; }
        } catch (JobPersistenceException jpe) {
            try {
                getLog().error("Error retrieving job, setting trigger state to ERROR.", jpe);
                getDelegate().updateTriggerState(conn, trigger.getName(),
                        trigger.getGroup(), STATE_ERROR);
            } catch (SQLException sqle) {
                getLog().error("Unable to set trigger state to ERROR.", sqle);
            }
            throw jpe;
        }

        if (trigger.getCalendarName() != null) {
            cal = retrieveCalendar(conn, ctxt, trigger.getCalendarName());
            if (cal == null) { return null; }
        }

        try {
            getDelegate().deleteFiredTrigger(conn, trigger.getFireInstanceId());
            getDelegate().insertFiredTrigger(conn, trigger, STATE_EXECUTING,
                    job);
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't insert fired trigger: "
                    + e.getMessage(), e);
        }

        Date prevFireTime = trigger.getPreviousFireTime();

        // call triggered - to update the trigger's next-fire-time state...
        trigger.triggered(cal);

        String state = STATE_WAITING;
        boolean force = true;
        
        if (job.isStateful()) {
            state = STATE_BLOCKED;
            force = false;
            try {
                getDelegate().updateTriggerStatesForJobFromOtherState(conn, job.getName(),
                        job.getGroup(), STATE_BLOCKED, STATE_WAITING);
                getDelegate().updateTriggerStatesForJobFromOtherState(conn, job.getName(),
                        job.getGroup(), STATE_BLOCKED, STATE_ACQUIRED);
                getDelegate().updateTriggerStatesForJobFromOtherState(conn, job.getName(),
                        job.getGroup(), STATE_PAUSED_BLOCKED, STATE_PAUSED);
            } catch (SQLException e) {
                throw new JobPersistenceException(
                        "Couldn't update states of blocked triggers: "
                                + e.getMessage(), e);
            }
        } 
            
        if (trigger.getNextFireTime() == null) {
            state = STATE_COMPLETE;
            force = true;
        }

        storeTrigger(conn, ctxt, trigger, job, true, state, force, false);

        job.getJobDataMap().clearDirtyFlag();

        return new TriggerFiredBundle(job, trigger, cal, trigger.getGroup()
                .equals(Scheduler.DEFAULT_RECOVERY_GROUP), new Date(), trigger
                .getPreviousFireTime(), prevFireTime, trigger.getNextFireTime());
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler has completed the
     * firing of the given <code>Trigger</code> (and the execution its
     * associated <code>Job</code>), and that the <code>{@link org.quartz.JobDataMap}</code>
     * in the given <code>JobDetail</code> should be updated if the <code>Job</code>
     * is stateful.
     * </p>
     */
    public void triggeredJobComplete(final SchedulingContext ctxt, final Trigger trigger,
            final JobDetail jobDetail, final int triggerInstCode)
        throws JobPersistenceException {
        executeInNonManagedTXLock(
            LOCK_TRIGGER_ACCESS,
            new VoidTransactionCallback() {
                public void execute(Connection conn) throws JobPersistenceException {
                    triggeredJobComplete(conn, ctxt, trigger, jobDetail,triggerInstCode);
                }
            });    
    }
    
    protected void triggeredJobComplete(Connection conn,
            SchedulingContext ctxt, Trigger trigger, JobDetail jobDetail,
            int triggerInstCode) throws JobPersistenceException {
        try {
            if (triggerInstCode == Trigger.INSTRUCTION_DELETE_TRIGGER) {
                if(trigger.getNextFireTime() == null) { 
                    // double check for possible reschedule within job 
                    // execution, which would cancel the need to delete...
                    TriggerStatus stat = getDelegate().selectTriggerStatus(
                            conn, trigger.getName(), trigger.getGroup());
                    if(stat != null && stat.getNextFireTime() == null) {
                        removeTrigger(conn, ctxt, trigger.getName(), trigger.getGroup());
                    }
                } else{
                    removeTrigger(conn, ctxt, trigger.getName(), trigger.getGroup());
                    signaler.signalSchedulingChange(0L);
                }
            } else if (triggerInstCode == Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE) {
                getDelegate().updateTriggerState(conn, trigger.getName(),
                        trigger.getGroup(), STATE_COMPLETE);
                signaler.signalSchedulingChange(0L);
            } else if (triggerInstCode == Trigger.INSTRUCTION_SET_TRIGGER_ERROR) {
                getLog().info("Trigger " + trigger.getFullName() + " set to ERROR state.");
                getDelegate().updateTriggerState(conn, trigger.getName(),
                        trigger.getGroup(), STATE_ERROR);
                signaler.signalSchedulingChange(0L);
            } else if (triggerInstCode == Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE) {
                getDelegate().updateTriggerStatesForJob(conn,
                        trigger.getJobName(), trigger.getJobGroup(),
                        STATE_COMPLETE);
                signaler.signalSchedulingChange(0L);
            } else if (triggerInstCode == Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_ERROR) {
                getLog().info("All triggers of Job " + 
                        trigger.getFullJobName() + " set to ERROR state.");
                getDelegate().updateTriggerStatesForJob(conn,
                        trigger.getJobName(), trigger.getJobGroup(),
                        STATE_ERROR);
                signaler.signalSchedulingChange(0L);
            }

            if (jobDetail.isStateful()) {
                getDelegate().updateTriggerStatesForJobFromOtherState(conn,
                        jobDetail.getName(), jobDetail.getGroup(),
                        STATE_WAITING, STATE_BLOCKED);

                getDelegate().updateTriggerStatesForJobFromOtherState(conn,
                        jobDetail.getName(), jobDetail.getGroup(),
                        STATE_PAUSED, STATE_PAUSED_BLOCKED);

                signaler.signalSchedulingChange(0L);

                try {
                    if (jobDetail.getJobDataMap().isDirty()) {
                        getDelegate().updateJobData(conn, jobDetail);
                    }
                } catch (IOException e) {
                    throw new JobPersistenceException(
                            "Couldn't serialize job data: " + e.getMessage(), e);
                } catch (SQLException e) {
                    throw new JobPersistenceException(
                            "Couldn't update job data: " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            throw new JobPersistenceException(
                    "Couldn't update trigger state(s): " + e.getMessage(), e);
        }

        try {
            getDelegate().deleteFiredTrigger(conn, trigger.getFireInstanceId());
        } catch (SQLException e) {
            throw new JobPersistenceException("Couldn't delete fired trigger: "
                    + e.getMessage(), e);
        }
    }

    /**
     * <P>
     * Get the driver delegate for DB operations.
     * </p>
     */
    protected DriverDelegate getDelegate() throws NoSuchDelegateException {
        if (null == delegate) {
            try {
                if(delegateClassName != null) {
                    delegateClass = 
                        getClassLoadHelper().loadClass(delegateClassName);
                }
                
                Constructor ctor = null;
                Object[] ctorParams = null;
                if (canUseProperties()) {
                    Class[] ctorParamTypes = new Class[]{
                        Log.class, String.class, String.class, Boolean.class};
                    ctor = delegateClass.getConstructor(ctorParamTypes);
                    ctorParams = new Object[]{
                        getLog(), tablePrefix,
                        instanceId, new Boolean(canUseProperties())};
                } else {
                    Class[] ctorParamTypes = new Class[]{
                        Log.class, String.class, String.class};
                    ctor = delegateClass.getConstructor(ctorParamTypes);
                    ctorParams = new Object[]{getLog(), tablePrefix, instanceId};
                }

                delegate = (DriverDelegate) ctor.newInstance(ctorParams);
            } catch (NoSuchMethodException e) {
                throw new NoSuchDelegateException(
                        "Couldn't find delegate constructor: " + e.getMessage());
            } catch (InstantiationException e) {
                throw new NoSuchDelegateException("Couldn't create delegate: "
                        + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new NoSuchDelegateException("Couldn't create delegate: "
                        + e.getMessage());
            } catch (InvocationTargetException e) {
                throw new NoSuchDelegateException("Couldn't create delegate: "
                        + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new NoSuchDelegateException("Couldn't load delegate class: "
                        + e.getMessage());
            }
        }

        return delegate;
    }

    protected Semaphore getLockHandler() {
        return lockHandler;
    }

    public void setLockHandler(Semaphore lockHandler) {
        this.lockHandler = lockHandler;
    }

    //---------------------------------------------------------------------------
    // Management methods
    //---------------------------------------------------------------------------

    protected RecoverMisfiredJobsResult doRecoverMisfires() throws JobPersistenceException {
        boolean transOwner = false;
        Connection conn = getNonManagedTXConnection();
        try {
            RecoverMisfiredJobsResult result = RecoverMisfiredJobsResult.NO_OP;
            
            // Before we make the potentially expensive call to acquire the 
            // trigger lock, peek ahead to see if it is likely we would find
            // misfired triggers requiring recovery.
            int misfireCount = (getDoubleCheckLockMisfireHandler()) ?
                getDelegate().countMisfiredTriggersInStates(
                    conn, STATE_MISFIRED, STATE_WAITING, getMisfireTime()) : 
                Integer.MAX_VALUE;
            
            if (misfireCount == 0) {
                getLog().debug(
                    "Found 0 triggers that missed their scheduled fire-time.");
            } else {
                transOwner = getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
                
                result = recoverMisfiredJobs(conn, false);
            }
            
            commitConnection(conn);
            return result;
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (SQLException e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("Database error recovering from misfires.", e);
        } catch (RuntimeException e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("Unexpected runtime exception: "
                    + e.getMessage(), e);
        } finally {
            try {
                releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            } finally {
                cleanupConnection(conn);
            }
        }
    }

    protected void signalSchedulingChange(long candidateNewNextFireTime) {
        signaler.signalSchedulingChange(candidateNewNextFireTime);
    }

    //---------------------------------------------------------------------------
    // Cluster management methods
    //---------------------------------------------------------------------------

    protected boolean firstCheckIn = true;

    protected long lastCheckin = System.currentTimeMillis();
    
    protected boolean doCheckin() throws JobPersistenceException {
        boolean transOwner = false;
        boolean transStateOwner = false;
        boolean recovered = false;

        Connection conn = getNonManagedTXConnection();
        try {
            // Other than the first time, always checkin first to make sure there is 
            // work to be done before we acquire the lock (since that is expensive, 
            // and is almost never necessary).  This must be done in a separate
            // transaction to prevent a deadlock under recovery conditions.
            List failedRecords = null;
            if (firstCheckIn == false) {
                failedRecords = clusterCheckIn(conn);
                commitConnection(conn);
            }
            
            if (firstCheckIn || (failedRecords.size() > 0)) {
                getLockHandler().obtainLock(conn, LOCK_STATE_ACCESS);
                transStateOwner = true;
    
                // Now that we own the lock, make sure we still have work to do. 
                // The first time through, we also need to make sure we update/create our state record
                failedRecords = (firstCheckIn) ? clusterCheckIn(conn) : findFailedInstances(conn);
    
                if (failedRecords.size() > 0) {
                    getLockHandler().obtainLock(conn, LOCK_TRIGGER_ACCESS);
                    //getLockHandler().obtainLock(conn, LOCK_JOB_ACCESS);
                    transOwner = true;
    
                    clusterRecover(conn, failedRecords);
                    recovered = true;
                }
            }
            
            commitConnection(conn);
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } finally {
            try {
                releaseLock(conn, LOCK_TRIGGER_ACCESS, transOwner);
            } finally {
                try {
                    releaseLock(conn, LOCK_STATE_ACCESS, transStateOwner);
                } finally {
                    cleanupConnection(conn);
                }
            }
        }

        firstCheckIn = false;

        return recovered;
    }

    /**
     * Get a list of all scheduler instances in the cluster that may have failed.
     * This includes this scheduler if it is checking in for the first time.
     */
    protected List findFailedInstances(Connection conn)
        throws JobPersistenceException {
        try {
            List failedInstances = new LinkedList();
            boolean foundThisScheduler = false;
            long timeNow = System.currentTimeMillis();
            
            List states = getDelegate().selectSchedulerStateRecords(conn, null);

            for (Iterator itr = states.iterator(); itr.hasNext();) {
                SchedulerStateRecord rec = (SchedulerStateRecord) itr.next();
        
                // find own record...
                if (rec.getSchedulerInstanceId().equals(getInstanceId())) {
                    foundThisScheduler = true;
                    if (firstCheckIn) {
                        failedInstances.add(rec);
                    }
                } else {
                    // find failed instances...
                    if (calcFailedIfAfter(rec) < timeNow) {
                        failedInstances.add(rec);
                    }
                }
            }
            
            // The first time through, also check for orphaned fired triggers.
            if (firstCheckIn) {
                failedInstances.addAll(findOrphanedFailedInstances(conn, states));
            }
            
            // If not the first time but we didn't find our own instance, then
            // Someone must have done recovery for us.
            if ((foundThisScheduler == false) && (firstCheckIn == false)) {
                // TODO: revisit when handle self-failed-out implied (see TODO in clusterCheckIn() below)
                getLog().warn(
                    "This scheduler instance (" + getInstanceId() + ") is still " + 
                    "active but was recovered by another instance in the cluster.  " +
                    "This may cause inconsistent behavior.");
            }
            
            return failedInstances;
        } catch (Exception e) {
            lastCheckin = System.currentTimeMillis();
            throw new JobPersistenceException("Failure identifying failed instances when checking-in: "
                    + e.getMessage(), e);
        }
    }
    
    /**
     * Create dummy <code>SchedulerStateRecord</code> objects for fired triggers
     * that have no scheduler state record.  Checkin timestamp and interval are
     * left as zero on these dummy <code>SchedulerStateRecord</code> objects.
     * 
     * @param schedulerStateRecords List of all current <code>SchedulerStateRecords</code>
     */
    private List findOrphanedFailedInstances(
            Connection conn, 
            List schedulerStateRecords) 
        throws SQLException, NoSuchDelegateException {
        List orphanedInstances = new ArrayList();
        
        Set allFiredTriggerInstanceNames = getDelegate().selectFiredTriggerInstanceNames(conn);
        if (allFiredTriggerInstanceNames.isEmpty() == false) {
            for (Iterator schedulerStateIter = schedulerStateRecords.iterator(); 
                 schedulerStateIter.hasNext();) {
                SchedulerStateRecord rec = (SchedulerStateRecord)schedulerStateIter.next();
                
                allFiredTriggerInstanceNames.remove(rec.getSchedulerInstanceId());
            }
            
            for (Iterator orphanIter = allFiredTriggerInstanceNames.iterator(); 
                 orphanIter.hasNext();) {
                
                SchedulerStateRecord orphanedInstance = new SchedulerStateRecord();
                orphanedInstance.setSchedulerInstanceId((String)orphanIter.next());
                
                orphanedInstances.add(orphanedInstance);
                
                getLog().warn(
                    "Found orphaned fired triggers for instance: " + orphanedInstance.getSchedulerInstanceId());
            }
        }
        
        return orphanedInstances;
    }
    
    protected long calcFailedIfAfter(SchedulerStateRecord rec) {
        return rec.getCheckinTimestamp() +
            Math.max(rec.getCheckinInterval(), 
                    (System.currentTimeMillis() - lastCheckin)) +
            7500L;
    }
    
    protected List clusterCheckIn(Connection conn)
        throws JobPersistenceException {

        List failedInstances = findFailedInstances(conn);
        
        try {
            // TODO: handle self-failed-out

            // check in...
            lastCheckin = System.currentTimeMillis();
            if(getDelegate().updateSchedulerState(conn, getInstanceId(), lastCheckin) == 0) {
                getDelegate().insertSchedulerState(conn, getInstanceId(),
                        lastCheckin, getClusterCheckinInterval());
            }
            
        } catch (Exception e) {
            throw new JobPersistenceException("Failure updating scheduler state when checking-in: "
                    + e.getMessage(), e);
        }

        return failedInstances;
    }

    protected void clusterRecover(Connection conn, List failedInstances)
        throws JobPersistenceException {

        if (failedInstances.size() > 0) {

            long recoverIds = System.currentTimeMillis();

            logWarnIfNonZero(failedInstances.size(),
                    "ClusterManager: detected " + failedInstances.size()
                            + " failed or restarted instances.");
            try {
                Iterator itr = failedInstances.iterator();
                while (itr.hasNext()) {
                    SchedulerStateRecord rec = (SchedulerStateRecord) itr
                            .next();

                    getLog().info(
                            "ClusterManager: Scanning for instance \""
                                    + rec.getSchedulerInstanceId()
                                    + "\"'s failed in-progress jobs.");

                    List firedTriggerRecs = getDelegate()
                            .selectInstancesFiredTriggerRecords(conn,
                                    rec.getSchedulerInstanceId());

                    int acquiredCount = 0;
                    int recoveredCount = 0;
                    int otherCount = 0;

                    Set triggerKeys = new HashSet();
                    
                    Iterator ftItr = firedTriggerRecs.iterator();
                    while (ftItr.hasNext()) {
                        FiredTriggerRecord ftRec = (FiredTriggerRecord) ftItr
                                .next();

                        Key tKey = ftRec.getTriggerKey();
                        Key jKey = ftRec.getJobKey();

                        triggerKeys.add(tKey);
                        
                        // release blocked triggers..
                        if (ftRec.getFireInstanceState().equals(STATE_BLOCKED)) {
                            getDelegate()
                                    .updateTriggerStatesForJobFromOtherState(
                                            conn, jKey.getName(),
                                            jKey.getGroup(), STATE_WAITING,
                                            STATE_BLOCKED);
                        } else if (ftRec.getFireInstanceState().equals(STATE_PAUSED_BLOCKED)) {
                            getDelegate()
                                    .updateTriggerStatesForJobFromOtherState(
                                            conn, jKey.getName(),
                                            jKey.getGroup(), STATE_PAUSED,
                                            STATE_PAUSED_BLOCKED);
                        }

                        // release acquired triggers..
                        if (ftRec.getFireInstanceState().equals(STATE_ACQUIRED)) {
                            getDelegate().updateTriggerStateFromOtherState(
                                    conn, tKey.getName(), tKey.getGroup(),
                                    STATE_WAITING, STATE_ACQUIRED);
                            acquiredCount++;
                        } else if (ftRec.isJobRequestsRecovery()) {
                            // handle jobs marked for recovery that were not fully
                            // executed..
                            if (jobExists(conn, jKey.getName(), jKey.getGroup())) {
                                SimpleTrigger rcvryTrig = new SimpleTrigger(
                                        "recover_"
                                                + rec.getSchedulerInstanceId()
                                                + "_"
                                                + String.valueOf(recoverIds++),
                                        Scheduler.DEFAULT_RECOVERY_GROUP,
                                        new Date(ftRec.getFireTimestamp()));
                                rcvryTrig.setVolatility(ftRec.isTriggerIsVolatile());
                                rcvryTrig.setJobName(jKey.getName());
                                rcvryTrig.setJobGroup(jKey.getGroup());
                                rcvryTrig.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                                rcvryTrig.setPriority(ftRec.getPriority());
                                JobDataMap jd = getDelegate().selectTriggerJobDataMap(conn, tKey.getName(), tKey.getGroup());
                                jd.put(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_NAME, tKey.getName());
                                jd.put(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_GROUP, tKey.getGroup());
                                jd.put(Scheduler.FAILED_JOB_ORIGINAL_TRIGGER_FIRETIME_IN_MILLISECONDS, String.valueOf(ftRec.getFireTimestamp()));
                                rcvryTrig.setJobDataMap(jd);

                                rcvryTrig.computeFirstFireTime(null);
                                storeTrigger(conn, null, rcvryTrig, null, false,
                                        STATE_WAITING, false, true);
                                recoveredCount++;
                            } else {
                                getLog()
                                        .warn(
                                                "ClusterManager: failed job '"
                                                        + jKey
                                                        + "' no longer exists, cannot schedule recovery.");
                                otherCount++;
                            }
                        } else {
                            otherCount++;
                        }

                        // free up stateful job's triggers
                        if (ftRec.isJobIsStateful()) {
                            getDelegate()
                                .updateTriggerStatesForJobFromOtherState(
                                        conn, jKey.getName(),
                                        jKey.getGroup(), STATE_WAITING,
                                        STATE_BLOCKED);
                            getDelegate()
                                .updateTriggerStatesForJobFromOtherState(
                                        conn, jKey.getName(),
                                        jKey.getGroup(), STATE_PAUSED,
                                        STATE_PAUSED_BLOCKED);
                        }
                    }

                    getDelegate().deleteFiredTriggers(conn,
                            rec.getSchedulerInstanceId());

                    // Check if any of the fired triggers we just deleted were the last fired trigger
                    // records of a COMPLETE trigger.
                    int completeCount = 0;
                    for (Iterator triggerKeyIter = triggerKeys.iterator(); triggerKeyIter.hasNext();) {
                        Key triggerKey = (Key)triggerKeyIter.next();
                        
                        if (getDelegate().selectTriggerState(conn, triggerKey.getName(), triggerKey.getGroup()).
                                equals(STATE_COMPLETE)) {
                            List firedTriggers = 
                                getDelegate().selectFiredTriggerRecords(conn, triggerKey.getName(), triggerKey.getGroup());
                            if (firedTriggers.isEmpty()) {
                                SchedulingContext schedulingContext = new SchedulingContext();
                                schedulingContext.setInstanceId(instanceId);
                                
                                if (removeTrigger(conn, schedulingContext, triggerKey.getName(), triggerKey.getGroup())) {
                                    completeCount++;
                                }
                            }
                        }
                    }
                    
                    logWarnIfNonZero(acquiredCount,
                            "ClusterManager: ......Freed " + acquiredCount
                                    + " acquired trigger(s).");
                    logWarnIfNonZero(completeCount,
                            "ClusterManager: ......Deleted " + completeCount
                                    + " complete triggers(s).");
                    logWarnIfNonZero(recoveredCount,
                            "ClusterManager: ......Scheduled " + recoveredCount
                                    + " recoverable job(s) for recovery.");
                    logWarnIfNonZero(otherCount,
                            "ClusterManager: ......Cleaned-up " + otherCount
                                    + " other failed job(s).");

                    if (rec.getSchedulerInstanceId().equals(getInstanceId()) == false) {
                        getDelegate().deleteSchedulerState(conn,
                                rec.getSchedulerInstanceId());
                    }
                }
            } catch (Exception e) {
                throw new JobPersistenceException("Failure recovering jobs: "
                        + e.getMessage(), e);
            }
        }
    }

    protected void logWarnIfNonZero(int val, String warning) {
        if (val > 0) {
            getLog().info(warning);
        } else {
            getLog().debug(warning);
        }
    }

    /**
     * <p>
     * Cleanup the given database connection.  This means restoring
     * any modified auto commit or transaction isolation connection
     * attributes, and then closing the underlying connection.
     * </p>
     * 
     * <p>
     * This is separate from closeConnection() because the Spring 
     * integration relies on being able to overload closeConnection() and
     * expects the same connection back that it originally returned
     * from the datasource. 
     * </p>
     * 
     * @see #closeConnection(Connection)
     */
    protected void cleanupConnection(Connection conn) {
        if (conn != null) {
            if (conn instanceof Proxy) {
                Proxy connProxy = (Proxy)conn;
                
                InvocationHandler invocationHandler = 
                    Proxy.getInvocationHandler(connProxy);
                if (invocationHandler instanceof AttributeRestoringConnectionInvocationHandler) {
                    AttributeRestoringConnectionInvocationHandler connHandler =
                        (AttributeRestoringConnectionInvocationHandler)invocationHandler;
                        
                    connHandler.restoreOriginalAtributes();
                    closeConnection(connHandler.getWrappedConnection());
                    return;
                }
            }
            
            // Wan't a Proxy, or was a Proxy, but wasn't ours.
            closeConnection(conn);
        }
    }
    
    
    /**
     * Closes the supplied <code>Connection</code>.
     * <p>
     * Ignores a <code>null Connection</code>.  
     * Any exception thrown trying to close the <code>Connection</code> is
     * logged and ignored.  
     * </p>
     * 
     * @param conn The <code>Connection</code> to close (Optional).
     */
    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                getLog().error("Failed to close Connection", e);
            } catch (Throwable e) {
                getLog().error(
                    "Unexpected exception closing Connection." +
                    "  This is often due to a Connection being returned after or during shutdown.", e);
            }
        }
    }

    /**
     * Rollback the supplied connection.
     * 
     * <p>  
     * Logs any SQLException it gets trying to rollback, but will not propogate
     * the exception lest it mask the exception that caused the caller to 
     * need to rollback in the first place.
     * </p>
     *
     * @param conn (Optional)
     */
    protected void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                getLog().error(
                    "Couldn't rollback jdbc connection. "+e.getMessage(), e);
            }
        }
    }
    
    /**
     * Commit the supplied connection
     *
     * @param conn (Optional)
     * @throws JobPersistenceException thrown if a SQLException occurs when the
     * connection is committed
     */
    protected void commitConnection(Connection conn)
        throws JobPersistenceException {

        if (conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                throw new JobPersistenceException(
                    "Couldn't commit jdbc connection. "+e.getMessage(), e);
            }
        }
    }
    
    /**
     * Implement this interface to provide the code to execute within
     * the a transaction template.  If no return value is required, execute
     * should just return null.
     * 
     * @see JobStoreSupport#executeInNonManagedTXLock(String, TransactionCallback)
     * @see JobStoreSupport#executeInLock(String, TransactionCallback)
     * @see JobStoreSupport#executeWithoutLock(TransactionCallback)
     */
    protected interface TransactionCallback {
        Object execute(Connection conn) throws JobPersistenceException;
    }

    /**
     * Implement this interface to provide the code to execute within
     * the a transaction template that has no return value.
     * 
     * @see JobStoreSupport#executeInNonManagedTXLock(String, TransactionCallback)
     */
    protected interface VoidTransactionCallback {
        void execute(Connection conn) throws JobPersistenceException;
    }

    /**
     * Execute the given callback in a transaction. Depending on the JobStore, 
     * the surrounding transaction may be assumed to be already present 
     * (managed).  
     * 
     * <p>
     * This method just forwards to executeInLock() with a null lockName.
     * </p>
     * 
     * @see #executeInLock(String, TransactionCallback)
     */
    public Object executeWithoutLock(
        TransactionCallback txCallback) throws JobPersistenceException {
        return executeInLock(null, txCallback);
    }

    /**
     * Execute the given callback having aquired the given lock.  
     * Depending on the JobStore, the surrounding transaction may be 
     * assumed to be already present (managed).  This version is just a 
     * handy wrapper around executeInLock that doesn't require a return
     * value.
     * 
     * @param lockName The name of the lock to aquire, for example 
     * "TRIGGER_ACCESS".  If null, then no lock is aquired, but the
     * lockCallback is still executed in a transaction. 
     * 
     * @see #executeInLock(String, TransactionCallback)
     */
    protected void executeInLock(
            final String lockName, 
            final VoidTransactionCallback txCallback) throws JobPersistenceException {
        executeInLock(
            lockName,
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    txCallback.execute(conn);
                    return null;
                }
            });
    }
    
    /**
     * Execute the given callback having aquired the given lock.  
     * Depending on the JobStore, the surrounding transaction may be 
     * assumed to be already present (managed).
     * 
     * @param lockName The name of the lock to aquire, for example 
     * "TRIGGER_ACCESS".  If null, then no lock is aquired, but the
     * lockCallback is still executed in a transaction. 
     */
    protected abstract Object executeInLock(
        String lockName, 
        TransactionCallback txCallback) throws JobPersistenceException;
    
    /**
     * Execute the given callback having optionally aquired the given lock.
     * This uses the non-managed transaction connection.  This version is just a 
     * handy wrapper around executeInNonManagedTXLock that doesn't require a return
     * value.
     * 
     * @param lockName The name of the lock to aquire, for example 
     * "TRIGGER_ACCESS".  If null, then no lock is aquired, but the
     * lockCallback is still executed in a non-managed transaction. 
     * 
     * @see #executeInNonManagedTXLock(String, TransactionCallback)
     */
    protected void executeInNonManagedTXLock(
            final String lockName, 
            final VoidTransactionCallback txCallback) throws JobPersistenceException {
        executeInNonManagedTXLock(
            lockName,
            new TransactionCallback() {
                public Object execute(Connection conn) throws JobPersistenceException {
                    txCallback.execute(conn);
                    return null;
                }
            });
    }
    
    /**
     * Execute the given callback having optionally aquired the given lock.
     * This uses the non-managed transaction connection.
     * 
     * @param lockName The name of the lock to aquire, for example 
     * "TRIGGER_ACCESS".  If null, then no lock is aquired, but the
     * lockCallback is still executed in a non-managed transaction. 
     */
    protected Object executeInNonManagedTXLock(
            String lockName, 
            TransactionCallback txCallback) throws JobPersistenceException {
        boolean transOwner = false;
        Connection conn = null;
        try {
            if (lockName != null) {
                // If we aren't using db locks, then delay getting DB connection 
                // until after aquiring the lock since it isn't needed.
                if (getLockHandler().requiresConnection()) {
                    conn = getNonManagedTXConnection();
                }
                
                transOwner = getLockHandler().obtainLock(conn, lockName);
            }
            
            if (conn == null) {
                conn = getNonManagedTXConnection();
            }
            
            Object result = txCallback.execute(conn);
            commitConnection(conn);
            return result;
        } catch (JobPersistenceException e) {
            rollbackConnection(conn);
            throw e;
        } catch (RuntimeException e) {
            rollbackConnection(conn);
            throw new JobPersistenceException("Unexpected runtime exception: "
                    + e.getMessage(), e);
        } finally {
            try {
                releaseLock(conn, lockName, transOwner);
            } finally {
                cleanupConnection(conn);
            }
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////
    //
    // ClusterManager Thread
    //
    /////////////////////////////////////////////////////////////////////////////

    class ClusterManager extends Thread {

        private boolean shutdown = false;

        private int numFails = 0;
        
        ClusterManager() {
            this.setPriority(Thread.NORM_PRIORITY + 2);
            this.setName("QuartzScheduler_" + instanceName + "-" + instanceId + "_ClusterManager");
            this.setDaemon(getMakeThreadsDaemons());
        }

        public void initialize() {
            this.manage();
            this.start();
        }

        public void shutdown() {
            shutdown = true;
            this.interrupt();
        }

        private boolean manage() {
            boolean res = false;
            try {

                res = doCheckin();

                numFails = 0;
                getLog().debug("ClusterManager: Check-in complete.");
            } catch (Exception e) {
                if(numFails % 4 == 0) {
                    getLog().error(
                        "ClusterManager: Error managing cluster: "
                                + e.getMessage(), e);
                }
                numFails++;
            }
            return res;
        }

        public void run() {
            while (!shutdown) {

                if (!shutdown) {
                    long timeToSleep = getClusterCheckinInterval();
                    long transpiredTime = (System.currentTimeMillis() - lastCheckin);
                    timeToSleep = timeToSleep - transpiredTime;
                    if (timeToSleep <= 0) {
                        timeToSleep = 100L;
                    }

                    if(numFails > 0) {
                        timeToSleep = Math.max(getDbRetryInterval(), timeToSleep);
                    }
                    
                    try {
                        Thread.sleep(timeToSleep);
                    } catch (Exception ignore) {
                    }
                }

                if (!shutdown && this.manage()) {
                    signalSchedulingChange(0L);
                }

            }//while !shutdown
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    //
    // MisfireHandler Thread
    //
    /////////////////////////////////////////////////////////////////////////////

    class MisfireHandler extends Thread {

        private boolean shutdown = false;

        private int numFails = 0;
        

        MisfireHandler() {
            this.setName("QuartzScheduler_" + instanceName + "-" + instanceId + "_MisfireHandler");
            this.setDaemon(getMakeThreadsDaemons());
        }

        public void initialize() {
            //this.manage();
            this.start();
        }

        public void shutdown() {
            shutdown = true;
            this.interrupt();
        }

        private RecoverMisfiredJobsResult manage() {
            try {
                getLog().debug("MisfireHandler: scanning for misfires...");

                RecoverMisfiredJobsResult res = doRecoverMisfires();
                numFails = 0;
                return res;
            } catch (Exception e) {
                if(numFails % 4 == 0) {
                    getLog().error(
                        "MisfireHandler: Error handling misfires: "
                                + e.getMessage(), e);
                }
                numFails++;
            }
            return RecoverMisfiredJobsResult.NO_OP;
        }

        public void run() {
            
            while (!shutdown) {

                long sTime = System.currentTimeMillis();

                RecoverMisfiredJobsResult recoverMisfiredJobsResult = manage();

                if (recoverMisfiredJobsResult.getProcessedMisfiredTriggerCount() > 0) {
                    signalSchedulingChange(recoverMisfiredJobsResult.getEarliestNewTime());
                }

                if (!shutdown) {
                    long timeToSleep = 50l;  // At least a short pause to help balance threads
                    if (!recoverMisfiredJobsResult.hasMoreMisfiredTriggers()) {
                        timeToSleep = getMisfireThreshold() - (System.currentTimeMillis() - sTime);
                        if (timeToSleep <= 0) {
                            timeToSleep = 50l;
                        }

                        if(numFails > 0) {
                            timeToSleep = Math.max(getDbRetryInterval(), timeToSleep);
                        }
                    }
                    
                    try {
                        Thread.sleep(timeToSleep);
                    } catch (Exception ignore) {
                    }
                }//while !shutdown
            }
        }
    }
}

// EOF
