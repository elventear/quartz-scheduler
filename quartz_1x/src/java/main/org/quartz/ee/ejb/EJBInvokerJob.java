/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.ee.ejb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>
 * A <code>Job</code> that invokes a method on an EJB.
 * </p>
 * 
 * <p>
 * Expects the properties corresponding to the following keys to be in the
 * <code>JobDataMap</code> when it executes:
 * <ul>
 * <li><code>EJB_JNDI_NAME_KEY</code>- the JNDI name (location) of the
 * EJB's home interface.</li>
 * <li><code>EJB_METHOD_KEY</code>- the name of the method to invoke on the
 * EJB.</li>
 * <li><code>EJB_ARGS_KEY</code>- an Object[] of the args to pass to the
 * method.</li>
 * </ul>
 * </p>
 * 
 * @deprecated This class has been repackaged at <code>org.quartz.jobs.ee.ejb</code>.
 * 
 * @author Andrew Collins
 * @author James House
 */
public class EJBInvokerJob implements Job {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public static final String EJB_JNDI_NAME_KEY = "ejb";

    public static final String EJB_METHOD_KEY = "method";

    public static final String EJB_ARGS_KEY = "args";

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public EJBInvokerJob() {
        // nothing
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        JobDetail detail = context.getJobDetail();

        JobDataMap dataMap = detail.getJobDataMap();

        String ejb = dataMap.getString(EJB_JNDI_NAME_KEY);
        String method = dataMap.getString(EJB_METHOD_KEY);
        Object[] arguments = (Object[]) dataMap.get(EJB_ARGS_KEY);

        if (ejb == null) { 
        // must specify remote home
        throw new JobExecutionException(); }

        InitialContext jndiContext = null;

        // get initial context
        try {
            jndiContext = new InitialContext();
        } catch (NamingException ne) {
            throw new JobExecutionException(ne);
        }

        Object value = null;

        // locate home interface
        try {
            value = jndiContext.lookup(ejb);
        } catch (NamingException ne) {
            throw new JobExecutionException(ne);
        }

        // get home interface
        EJBHome ejbHome = (EJBHome) PortableRemoteObject.narrow(value,
                EJBHome.class);

        // get meta data
        EJBMetaData metaData = null;

        try {
            metaData = ejbHome.getEJBMetaData();
        } catch (RemoteException re) {
            throw new JobExecutionException(re);
        }

        // get home interface class
        Class homeClass = metaData.getHomeInterfaceClass();

        // get remote interface class
        Class remoteClass = metaData.getRemoteInterfaceClass();

        // get home interface
        ejbHome = (EJBHome) PortableRemoteObject.narrow(ejbHome, homeClass);

        Method methodCreate = null;

        try {
            // create method 'create()' on home interface
            methodCreate = homeClass.getMethod("create", null);
        } catch (NoSuchMethodException nsme) {
            throw new JobExecutionException(nsme);
        }

        // create remote object
        EJBObject remoteObj = null;

        try {
            // invoke 'create()' method on home interface
            remoteObj = (EJBObject) methodCreate.invoke(ejbHome, null);
        } catch (IllegalAccessException iae) {
            throw new JobExecutionException(iae);
        } catch (InvocationTargetException ite) {
            throw new JobExecutionException(ite);
        }

        // execute user-specified method on remote object
        Method methodExecute = null;

        try {
            // create method signature
            Class[] argTypes = new Class[arguments.length];
            for (int i = 0; i < arguments.length; i++)
                argTypes[i] = arguments[i].getClass();

            // get method on remote object
            methodExecute = remoteClass.getMethod(method, argTypes);
        } catch (NoSuchMethodException nsme) {
            throw new JobExecutionException(nsme);
        }

        try {
            // invoke user-specified method on remote object
            methodExecute.invoke(remoteObj, arguments);
        } catch (IllegalAccessException iae) {
            throw new JobExecutionException(iae);
        } catch (InvocationTargetException ite) {
            throw new JobExecutionException(ite);
        }
    }
}