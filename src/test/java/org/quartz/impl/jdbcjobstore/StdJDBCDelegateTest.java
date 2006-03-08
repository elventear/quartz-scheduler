/*
 * Copyright NLG 2006
 */
package org.quartz.impl.jdbcjobstore;

import java.io.IOException;
import java.io.NotSerializableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;

import junit.framework.TestCase;

public class StdJDBCDelegateTest extends TestCase {

    public void testSerializeJobData() throws IOException {
        StdJDBCDelegate delegate = new StdJDBCDelegate(LogFactory.getLog(getClass()), "QRTZ_", "INSTANCE");
        
        JobDataMap jdm = new JobDataMap();
        delegate.serializeJobData(jdm).close();

        jdm.clear();
        jdm.put("key", "value");
        jdm.put("key2", null);
        delegate.serializeJobData(jdm).close();

        jdm.clear();
        jdm.put("key1", "value");
        jdm.put("key2", null);
        jdm.put("key3", new Object());
        try {
            delegate.serializeJobData(jdm);
            fail();
        } catch (NotSerializableException e) {
            assertTrue(e.getMessage().contains("key3"));
        }
    }
}
