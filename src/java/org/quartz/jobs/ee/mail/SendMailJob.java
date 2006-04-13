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
package org.quartz.jobs.ee.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <p>
 * A Job which sends an e-mail with the configured content to the configured
 * recipient.
 * </p>
 * 
 * @author James House
 */
public class SendMailJob implements Job {

    private final Log log = LogFactory.getLog(getClass());

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * The host name of the smtp server. REQUIRED.
     */
    public static final String PROP_SMTP_HOST = "smtp_host";

    /**
     * The e-mail address to send the mail to. REQUIRED.
     */
    public static final String PROP_RECIPIENT = "recipient";

    /**
     * The e-mail address to cc the mail to. Optional.
     */
    public static final String PROP_CC_RECIPIENT = "cc_recipient";

    /**
     * The e-mail address to claim the mail is from. REQUIRED.
     */
    public static final String PROP_SENDER = "sender";

    /**
     * The e-mail address the message should say to reply to. Optional.
     */
    public static final String PROP_REPLY_TO = "reply_to";

    /**
     * The subject to place on the e-mail. REQUIRED.
     */
    public static final String PROP_SUBJECT = "subject";

    /**
     * The e-mail message body. REQUIRED.
     */
    public static final String PROP_MESSAGE = "message";

    /**
     * The message content type. For example, "text/html". Optional.
     */
    public static final String PROP_CONTENT_TYPE = "content_type";

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        JobDataMap data = context.getMergedJobDataMap();

        MailInfo mailInfo = populateMailInfo(data, createMailInfo());
        
        getLog().info("Sending message " + mailInfo);

        try {
            MimeMessage mimeMessage = prepareMimeMessage(mailInfo);
            
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            throw new JobExecutionException("Unable to send mail: " + mailInfo,
                    e, false);
        }

    }

    protected Log getLog() {
        return log;
    }

    protected MimeMessage prepareMimeMessage(MailInfo mailInfo)
        throws MessagingException {
        Session session = getMailSession(mailInfo);

        MimeMessage mimeMessage = new MimeMessage(session);

        Address[] toAddresses = InternetAddress.parse(mailInfo.getTo());
        mimeMessage.setRecipients(Message.RecipientType.TO, toAddresses);

        if (mailInfo.getCc() != null) {
            Address[] ccAddresses = InternetAddress.parse(mailInfo.getCc());
            mimeMessage.setRecipients(Message.RecipientType.CC, ccAddresses);
        }

        mimeMessage.setFrom(new InternetAddress(mailInfo.getFrom()));
        
        if (mailInfo.getReplyTo() != null) {
            mimeMessage.setReplyTo(new InternetAddress[]{new InternetAddress(mailInfo.getReplyTo())});
        }
        
        mimeMessage.setSubject(mailInfo.getSubject());
        
        mimeMessage.setSentDate(new Date());

        setMimeMessageContent(mimeMessage, mailInfo);

        return mimeMessage;
    }
    
    protected void setMimeMessageContent(MimeMessage mimeMessage, MailInfo mailInfo) 
        throws MessagingException {
        if (mailInfo.getContentType() == null) {
            mimeMessage.setText(mailInfo.getMessage());
        } else {
            mimeMessage.setContent(mailInfo.getMessage(), mailInfo.getContentType());
        }
    }

    protected Session getMailSession(MailInfo mailInfo) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", mailInfo.getSmtpHost());
        
        return Session.getDefaultInstance(properties, null);
    }
    
    protected MailInfo createMailInfo() {
        return new MailInfo();
    }
    
    protected MailInfo populateMailInfo(JobDataMap data, MailInfo mailInfo) {
        // Required parameters
        mailInfo.setSmtpHost(getRequiredParm(data, PROP_SMTP_HOST, "PROP_SMTP_HOST"));
        mailInfo.setTo(getRequiredParm(data, PROP_RECIPIENT, "PROP_RECIPIENT"));
        mailInfo.setFrom(getRequiredParm(data, PROP_SENDER, "PROP_SENDER"));
        mailInfo.setSubject(getRequiredParm(data, PROP_SUBJECT, "PROP_SUBJECT"));
        mailInfo.setMessage(getRequiredParm(data, PROP_MESSAGE, "PROP_MESSAGE"));
        
        // Optional parameters
        mailInfo.setReplyTo(getOptionalParm(data, PROP_REPLY_TO));
        mailInfo.setCc(getOptionalParm(data, PROP_CC_RECIPIENT));
        mailInfo.setContentType(getOptionalParm(data, PROP_CONTENT_TYPE));
        
        return mailInfo;
    }
    
    
    protected String getRequiredParm(JobDataMap data, String property, String constantName) {
        String value = getOptionalParm(data, property);
        
        if (value == null) {
            throw new IllegalArgumentException(constantName + " not specified.");
        }
        
        return value;
    }
    
    protected String getOptionalParm(JobDataMap data, String property) {
        String value = data.getString(property);
        
        if ((value != null) && (value.trim().length() == 0)) {
            return null;
        }
        
        return value;
    }
    
    protected static class MailInfo {
        private String smtpHost;
        private String to;
        private String from;
        private String subject;
        private String message;
        private String replyTo;
        private String cc;
        private String contentType;

        public String toString() {
            return "'" + getSubject() + "' to: " + getTo();
        }
        
        public String getCc() {
            return cc;
        }

        public void setCc(String cc) {
            this.cc = cc;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }

        public String getSmtpHost() {
            return smtpHost;
        }

        public void setSmtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }
}
