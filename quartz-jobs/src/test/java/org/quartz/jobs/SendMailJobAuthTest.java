package org.quartz.jobs;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.jobs.ee.mail.SendMailJob;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.PlainAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

public class SendMailJobAuthTest {
    private SMTPServer smtpServer;
    private Scheduler scheduler;
    private SimpleValidator simpleValidator;
    private int port;

    @Before
    public void setUp() throws Exception {
        SimpleMessageListener messageListener = new SimpleMessageListener() {
            @Override
            public boolean accept(String sender, String receiver) {
                System.out.println("SimpleMessageListener: accept from "
                        + sender + " to " + receiver);
                return true;
            }

            @Override
            public void deliver(String sender, String receiver,
                    InputStream inputstream) throws TooMuchDataException,
                    IOException {
                System.out.println("SimpleMessageListener: deliver from "
                        + sender + " to " + receiver + " message: "
                        + IOUtils.toString(inputstream));
            }
        };

        simpleValidator = new SimpleValidator();

        smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(
                messageListener));
        smtpServer
                .setAuthenticationHandlerFactory(new PlainAuthenticationHandlerFactory(
                        simpleValidator));

        smtpServer.setHostName("localhost");

        scheduler = new StdSchedulerFactory().getScheduler();
    }

    @After
    public void tearDown() throws Exception {
        smtpServer.stop();
        scheduler.shutdown();
    }

    @Test
    public void testWithCorrectCredentials() throws Exception {
        port = 2500;
        smtpServer.setPort(port);
        smtpServer.start();
        
        JobDetail job = newJob(SendMailJob.class)
                .withIdentity("job1", "group1").build();

        configureSendMailJob(job, "real@host.com", "myusername", "mypassword", port);

        Trigger trigger = newTrigger().withIdentity("trigger1", "group1")
                .startNow().build();

        scheduler.scheduleJob(job, trigger);
        scheduler.start();

        Thread.sleep(1 * 1000L);
        assertNull(simpleValidator.error);
        scheduler.deleteJob(job.getKey());
    }

    @Test
    public void testWithFalseCredentials() throws Exception {
        port = 2501;
        smtpServer.setPort(port);
        smtpServer.start();
        
        JobDetail job = newJob(SendMailJob.class)
                .withIdentity("job2", "group1").build();

        configureSendMailJob(job, "fake@host.com", "blahblah", "1234", port);

        Trigger trigger = newTrigger().withIdentity("trigger1", "group1")
                .startNow().build();

        scheduler.scheduleJob(job, trigger);
        scheduler.start();

        Thread.sleep(1 * 1000L);

        assertThat(simpleValidator.error,
                instanceOf(LoginFailedException.class));
    }

    private void configureSendMailJob(JobDetail job, String sender,
            String username, String password, int port) {
        JobDataMap jobData = job.getJobDataMap();
        jobData.put(SendMailJob.PROP_SMTP_HOST, "localhost");
        jobData.put(SendMailJob.PROP_SENDER, sender);
        jobData.put(SendMailJob.PROP_RECIPIENT, "receiver@host.com");
        jobData.put(SendMailJob.PROP_SUBJECT, "test subject");
        jobData.put(SendMailJob.PROP_MESSAGE, "do not reply");
        jobData.put(SendMailJob.PROP_USERNAME, username);
        jobData.put(SendMailJob.PROP_PASSWORD, password);
        jobData.put("mail.smtp.port", String.valueOf(port));
        jobData.put("mail.smtp.auth", "true");
    }

    private static class SimpleValidator implements UsernamePasswordValidator {
        public LoginFailedException error;

        public SimpleValidator() {
            System.out.println("SimpleValidator: " + error);
        }

        @Override
        public void login(String username, String password)
                throws LoginFailedException {
            System.out.println("UsernamePasswordValidator: login username '"
                    + username + "' password '" + password + "'");
            try {
                assertThat(username, equalTo("myusername"));
                assertThat(password, equalTo("mypassword"));
            } catch (Throwable e) {
                error = new LoginFailedException(e.getMessage());
                throw error;
            }
        }
    }

}
