/* 
 * Copyright 2001-2009 Terracotta, Inc. 
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
 */
package org.quartz.impl.jdbcjobstore;

import java.io.IOException;
import java.io.NotSerializableException;

import org.slf4j.LoggerFactory;
import org.quartz.JobDataMap;

import junit.framework.TestCase;

public class StdJDBCDelegateTest extends TestCase {

    public void testSerializeJobData() throws IOException {
        StdJDBCDelegate delegate = new StdJDBCDelegate(LoggerFactory.getLogger(getClass()), "QRTZ_", "INSTANCE");
        
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
            assertTrue(e.getMessage().indexOf("key3") >= 0);
        }
    }
}
