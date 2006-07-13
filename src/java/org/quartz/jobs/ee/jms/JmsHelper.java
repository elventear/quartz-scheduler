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
package org.quartz.jobs.ee.jms;

import org.quartz.JobDataMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

/**
 * Utility class that aids in the processing of JMS based jobs and sending
 * of <code>javax.jms.Message</code>
 *
 * @author Weston M. Price
 */
public class JmsHelper {
    
    public static final String INITIAL_CONTEXT_FACTORY = "java.naming.factory.initial";
    
    public static final String PROVIDER_URL = "java.naming.provider.url";

    public static final String PRINCIPAL = "java.naming.security.principal";

    public static final String CREDENTIALS = "java.naming.security.credentials";

    public static final String JMS_CONNECTION_FACTORY_JNDI = "jms.connection.factory";

    public static final String JMS_DESTINATION_JNDI = "jms.destination";

    public static final String JMS_USER = "jms.user";
    
    public static final String JMS_PASSWORD = "jms.password";
    
    public static final String JMS_ACK_MODE = "jms.acknowledge";
        
    public static final String JMS_USE_TXN = "jms.use.transaction";
        
    public static final String JMS_MSG_FACTORY_CLASS_NAME = "jms.message.factory.class.name";
    
    private JmsHelper() {
    }
    
    public static InitialContext getInitialContext(final JobDataMap jobDataMap) throws NamingException
    {
        
        Hashtable params = new Hashtable(2);
        
        String initialContextFactory =
            jobDataMap.getString(INITIAL_CONTEXT_FACTORY);
    
        if (initialContextFactory != null) {
            params.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        }
        
        String providerUrl = jobDataMap.getString(PROVIDER_URL);
        if (providerUrl != null) {
            params.put(Context.PROVIDER_URL, providerUrl);
        }

        String principal = jobDataMap.getString(PRINCIPAL);
        if ( principal != null ) {
            params.put( Context.SECURITY_PRINCIPAL, principal );
        }

        String credentials = jobDataMap.getString(CREDENTIALS);
        if ( credentials != null ) {
            params.put( Context.SECURITY_CREDENTIALS, credentials );
        }

        if (params.size() == 0) {
            return new InitialContext();
        } else {
            return new InitialContext(params);
        }
        
    }
        
    public static boolean isDestinationSecure(JobDataMap jobDataMap) {
        String user = jobDataMap.getString(JmsHelper.JMS_USER);
        String pw = jobDataMap.getString(JmsHelper.JMS_PASSWORD);
        
        return (user != null && pw != null);
        
    }
    
    /**
     * Closes a resource that has a <code>close()</code> method.
     * 
     * @param resource the resource to close.
     */
    public static void closeResource(Object resource) {
        //TODO determine if vargs can be used in Quartz (1.5 features)
        if(resource == null) {
            return;
        }
        
        try {
            
            final Method m = resource.getClass().getMethod("close", new Class[0]);
            m.invoke(resource, new Object[0]);
        
        } catch (SecurityException e) {
        } catch (IllegalArgumentException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        
    }

    public static boolean useTransaction(JobDataMap jobDataMap) {
        return false;
    }
    
    /**
     * Creates the <code>JmsMessageFactory</code>
     * @param factoryName
     * @return
     * @throws JmsJobException
     */
    public static JmsMessageFactory getMessageFactory(String factoryName) throws JmsJobException
    {
        
        try {
        
            Class clazz = Class.forName(factoryName);
            JmsMessageFactory messageFactory = (JmsMessageFactory)clazz.newInstance();
            return messageFactory;
        
        } catch (ClassNotFoundException e) {
            
            throw new JmsJobException(e.getMessage(), e);
            
        } catch (InstantiationException e) {

            throw new JmsJobException(e.getMessage(), e);
        
        } catch (IllegalAccessException e) {

            throw new JmsJobException(e.getMessage(), e);
        }
        
        
    }
    
}
