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
* @author Alain Marion
* @author Frank Van Uffelen
*
*/
public class SendDestinationMessageJob implements Job {

    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        ConnectionFactory qcf = null;
        Connection conn = null;
        Session session = null;
        Destination queue = null;
        MessageProducer sender = null;
        InitialContext ctx = null;

        final JobDetail detail = context.getJobDetail();
        final JobDataMap jobDataMap = detail.getJobDataMap();

        try {
            ctx = JmsHelper.getInitialContext(jobDataMap);

            qcf = (ConnectionFactory) ctx.lookup(
                jobDataMap.getString(JmsHelper.JMS_CONNECTION_FACTORY_JNDI)
            );

            if(JmsHelper.isDestinationSecure(jobDataMap)) {
                String user = jobDataMap.getString(JmsHelper.JMS_USER);
                String pw = jobDataMap.getString(JmsHelper.JMS_PASSWORD);
                conn = qcf.createConnection(user, pw);
            } else {
                conn = qcf.createConnection();
            }

            boolean useTransactions = JmsHelper.useTransaction(jobDataMap);
            int ackMode = Session.AUTO_ACKNOWLEDGE; // A sensible default value
            try {
                ackMode = jobDataMap.getInt(JmsHelper.JMS_ACK_MODE);
            } catch (ClassCastException e) {
                ackMode = jobDataMap.getIntFromString(JmsHelper.JMS_ACK_MODE);
            }
            session = conn.createSession(useTransactions, ackMode);
            String queueName = jobDataMap.getString(JmsHelper.JMS_DESTINATION_JNDI);
            queue = (Destination)ctx.lookup(queueName);
            sender = session.createProducer(queue);
            String factoryClass = jobDataMap.getString(JmsHelper.JMS_MSG_FACTORY_CLASS_NAME);
            JmsMessageFactory factory = JmsHelper.getMessageFactory(factoryClass);
            Message m = factory.createMessage(jobDataMap, session);
            sender.send(m);
        } catch (NamingException e) {
            throw new JobExecutionException(e.getMessage());
        } catch (JMSException e) {
            throw new JobExecutionException(e.getMessage());
        } catch (JmsJobException e) {
            throw new JobExecutionException(e.getMessage());
        } finally {
            JmsHelper.closeResource(sender);
            JmsHelper.closeResource(session);
            JmsHelper.closeResource(conn);
        }
    }

}
