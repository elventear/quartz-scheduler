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
package org.quartz.simpl;

import java.util.Collections;
import java.util.Map;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import junit.framework.TestCase;

/**
 * Unit test for PropertySettingJobFactory.
 */
public class PropertySettingJobFactoryTest extends TestCase {
    
    private PropertySettingJobFactory factory;
    
    protected void setUp() throws Exception {
        factory = new PropertySettingJobFactory();
        factory.setThrowIfPropertyNotFound(true);    
    }
    
    public void testSetBeanPropsPrimatives() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("intValue", new Integer(1));
        jobDataMap.put("longValue", new Long(2l));
        jobDataMap.put("floatValue", new Float(3.0f));
        jobDataMap.put("doubleValue", new Double(4.0));
        jobDataMap.put("booleanValue", Boolean.TRUE);
        jobDataMap.put("shortValue", new Short(((short)5)));
        jobDataMap.put("charValue", 'a');
        jobDataMap.put("byteValue", new Byte((byte)6));
        jobDataMap.put("stringValue", "S1");
        jobDataMap.put("mapValue", Collections.singletonMap("A", "B"));
        
        TestBean myBean = new TestBean();
        factory.setBeanProps(myBean, jobDataMap);
        
        assertEquals(1, myBean.getIntValue());
        assertEquals(2l, myBean.getLongValue());
        assertEquals(3.0f, myBean.getFloatValue(), 0.0001);
        assertEquals(4.0, myBean.getDoubleValue(), 0.0001);
        assertTrue(myBean.getBooleanValue());
        assertEquals(5, myBean.getShortValue());
        assertEquals('a', myBean.getCharValue());
        assertEquals((byte)6, myBean.getByteValue());
        assertEquals("S1", myBean.getStringValue());
        assertTrue(myBean.getMapValue().containsKey("A"));
    }
    
    public void testSetBeanPropsUnknownProperty() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("bogusValue", new Integer(1));
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (SchedulerException ignore) {}
    }
    
    public void testSetBeanPropsNullPrimative() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("intValue", null);
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (SchedulerException ignore) {}
    }
    
    public void testSetBeanPropsNullNonPrimative() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("mapValue", null);
        TestBean testBean = new TestBean();
        testBean.setMapValue(Collections.singletonMap("A", "B"));
        factory.setBeanProps(testBean, jobDataMap);
        assertNull(testBean.getMapValue());
    }
    
    public void testSetBeanPropsWrongPrimativeType() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("intValue", new Float(7));
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (SchedulerException ignore) {}
    }

    public void testSetBeanPropsWrongNonPrimativeType() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("mapValue", new Float(7));
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (SchedulerException ignore) {}
    }

    public void testSetBeanPropsCharStringTooShort() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("charValue", "");
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (SchedulerException ignore) {}
    }

    public void testSetBeanPropsCharStringTooLong() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("charValue", "abba");
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (SchedulerException ignore) {}
    }

    public void testSetBeanPropsFromStrings() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("intValue", "1");
        jobDataMap.put("longValue", "2");
        jobDataMap.put("floatValue", "3.0");
        jobDataMap.put("doubleValue", "4.0");
        jobDataMap.put("booleanValue", "true");
        jobDataMap.put("shortValue", "5");
        jobDataMap.put("charValue", "a");
        jobDataMap.put("byteValue", "6");
        
        TestBean myBean = new TestBean();
        factory.setBeanProps(myBean, jobDataMap);
        
        assertEquals(1, myBean.getIntValue());
        assertEquals(2l, myBean.getLongValue());
        assertEquals(3.0f, myBean.getFloatValue(), 0.0001);
        assertEquals(4.0, myBean.getDoubleValue(), 0.0001);
        assertEquals(true, myBean.getBooleanValue());
        assertEquals(5, myBean.getShortValue());
        assertEquals('a', myBean.getCharValue());
        assertEquals((byte)6, myBean.getByteValue());
    }

    private static final class TestBean {
        private int intValue;
        private long longValue;
        private float floatValue;
        private double doubleValue;
        private boolean booleanValue;
        private byte byteValue;
        private short shortValue;
        private char charValue;
        private String stringValue;
        private Map mapValue;
    
        public boolean getBooleanValue() {
            return booleanValue;
        }
    
        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }
    
        public double getDoubleValue() {
            return doubleValue;
        }
    
        public void setDoubleValue(double doubleValue) {
            this.doubleValue = doubleValue;
        }
    
        public float getFloatValue() {
            return floatValue;
        }
    
        public void setFloatValue(float floatValue) {
            this.floatValue = floatValue;
        }
   
        public int getIntValue() {
            return intValue;
        }
    
        public void setIntValue(int intValue) {
            this.intValue = intValue;
        }
    
        public long getLongValue() {
            return longValue;
        }
    
        public void setLongValue(long longValue) {
            this.longValue = longValue;
        }

        public Map getMapValue() {
            return mapValue;
        }
    
        public void setMapValue(Map mapValue) {
            this.mapValue = mapValue;
        }
    
        public String getStringValue() {
            return stringValue;
        }
    
        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }

        public byte getByteValue() {
            return byteValue;
        }

        public void setByteValue(byte byteValue) {
            this.byteValue = byteValue;
        }

        public char getCharValue() {
            return charValue;
        }

        public void setCharValue(char charValue) {
            this.charValue = charValue;
        }

        public short getShortValue() {
            return shortValue;
        }

        public void setShortValue(short shortValue) {
            this.shortValue = shortValue;
        }
    }
}
