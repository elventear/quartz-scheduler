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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
* <p>
* A <code>Job</code> that sends a <code>javax.jms.Message</code> to a 
* <code>javax.jms.Destination</code>. Note, this class can only be used in a JMS 1.1
* compliant environment.
* 
* <p>
* The following properties are expected to be provided in the <code>JobDataMap</code>:
* 
* <ul>
* <li><code>JMS_CONNECTION_FACTORY_JNDI</code> - The JNDI name of the JMS Connection Factory.</li>
* <li><code>JMS_DESTINATION_JNDI</code> - The JNDI name of the JMS destination.</li>
* <li><code>JMS_USE_TXN</code> - Whether or not to use a transacted <code>javax.jms.Session</code>.</li>
* <li><code>JMS_ACK_MODE</code> - The acknowledgement mode for the <code>javax.jms.Session</code>.</li>
* <li><code>JMS_MSG_FACTORY_CLASS_NAME</code> - The implementation class name for the <code>JmsMessageFactory</code>.</li>
* </ul>
* 
* <p>
* The following properties are optional
* 
* <ul>
* <li><code>JMS_USER</code> - The JMS user for secure destinations.
* <li><code>JMS_PASSWORD</code> - The JMS password for secure destinations.
* </ul>
* 
* <p>
* The following properties can be used for JNDI support:
* <ul>
* <li><code>INITIAL_CONTEXT_FACTORY</code> - The java.naming.factory.initial setting for JNDI.
* <li><code>PROVIDER_URL</code> - The java.naming.provider.url for JNDI.
* </ul>
* 
* 
* @see JmsMessageFactory
* 
* @author Weston M. Price 
* 
*
*/
public class SendDestinationMessageJob implements Job {

    public void execute(JobExecutionContext context)
        throws JobExecutionException {
        
        ConnectionFactory factory = null;
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        Destination destination = null;
        InitialContext initCtx = null;
        
        JobDetail detail = context.getJobDetail();
        JobDataMap jobDataMap = detail.getJobDataMap();
        
        try {
            
            initCtx = org.quartz.jobs.ee.jms.JmsHelper.getInitialContext(jobDataMap);
            factory = (ConnectionFactory) initCtx.lookup(org.quartz.jobs.ee.jms.JmsHelper.JMS_CONNECTION_FACTORY_JNDI);
            
            if(org.quartz.jobs.ee.jms.JmsHelper.isDestinationSecure(jobDataMap))
            {
                String user = jobDataMap.getString(org.quartz.jobs.ee.jms.JmsHelper.JMS_USER);
                String pw = jobDataMap.getString(org.quartz.jobs.ee.jms.JmsHelper.JMS_PASSWORD);
                connection = factory.createConnection(user, pw);
            } else {
                connection = factory.createConnection();
                    
            }
                        
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = (Destination) initCtx.lookup(org.quartz.jobs.ee.jms.JmsHelper.JMS_DESTINATION_JNDI);
            producer = session.createProducer(destination);
            String factoryClass = jobDataMap.getString(org.quartz.jobs.ee.jms.JmsHelper.JMS_MSG_FACTORY_CLASS_NAME);
            org.quartz.jobs.ee.jms.JmsMessageFactory messageFactory = org.quartz.jobs.ee.jms.JmsHelper.getMessageFactory(factoryClass);
            Message m = messageFactory.createMessage(jobDataMap, session);
            producer.send(m);

        } catch (NamingException e) {
            
            throw new JobExecutionException(e);
            
        } catch (JMSException e) {

            throw new JobExecutionException(e);

        } catch (JmsJobException e) {

            throw new JobExecutionException(e);
        
        }finally{
            
            JmsHelper.closeResource(producer);
            JmsHelper.closeResource(session);
            JmsHelper.closeResource(connection);
        }

        
    }

}
