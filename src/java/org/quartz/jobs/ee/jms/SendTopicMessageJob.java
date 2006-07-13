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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
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
* <code>javax.jms.Topic</code>.
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
public class SendTopicMessageJob implements Job {

    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        TopicConnectionFactory tcf = null;
        TopicConnection connection = null;
        TopicSession session = null;
        Topic topic = null;
        TopicPublisher publisher = null;
        InitialContext ctx = null;
        String user = null;
        String pw = null;

        final JobDetail detail = context.getJobDetail();
        final JobDataMap jobDataMap = detail.getJobDataMap();

        try {

            ctx = JmsHelper.getInitialContext(jobDataMap);

            tcf = (TopicConnectionFactory) ctx
                    .lookup(JmsHelper.JMS_CONNECTION_FACTORY_JNDI);

            if (JmsHelper.isDestinationSecure(jobDataMap)) {

                user = jobDataMap.getString(JmsHelper.JMS_USER);
                pw = jobDataMap.getString(JmsHelper.JMS_PASSWORD);
                connection = tcf.createTopicConnection(user, pw);

            } else {
    
                connection = tcf.createTopicConnection();

            }

            session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = (Topic) ctx.lookup(JmsHelper.JMS_DESTINATION_JNDI);
            publisher = session.createPublisher(topic);
            String factoryClassName = jobDataMap.getString(JmsHelper.JMS_MSG_FACTORY_CLASS_NAME);
            JmsMessageFactory factory = JmsHelper.getMessageFactory(factoryClassName);
            Message m = factory.createMessage(jobDataMap, session);
            publisher.publish(m);

        } catch (NamingException e) {

            throw new JobExecutionException(e);

        } catch (JMSException e) {

            throw new JobExecutionException(e);

        } catch (JmsJobException e) {

            throw new JobExecutionException(e);

        } finally {

            JmsHelper.closeResource(publisher);
            JmsHelper.closeResource(session);
            JmsHelper.closeResource(connection);

        }

    }

}
