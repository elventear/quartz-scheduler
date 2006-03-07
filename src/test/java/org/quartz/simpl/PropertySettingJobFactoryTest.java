/*
 * Copyright NLG 2006
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
    
    public void testSetBeanPropsUnknownProperty() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("bogusValue", new Integer(1));
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (Exception ignore) {}
    }
    
    public void testSetBeanPropsNullPrimative() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("intValue", null);
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (Exception ignore) {}
    }
    
    public void testSetBeanPropsNullNonPrimative() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("mapValue", null);
        TestBean testBean = new TestBean();
        testBean.setMapValue(Collections.singletonMap("A", "B"));
        factory.setBeanProps(testBean, jobDataMap);
        assertNull(testBean.getMapValue());
    }
    
    public void testSetBeanPropsWrongPrimativeType() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("intValue", new Float(7));
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (Exception ignore) {}
    }

    public void testSetBeanPropsWrongNonPrimativeType() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("mapValue", new Float(7));
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (Exception ignore) {}
    }

    public void testSetBeanPropsCharStringTooShort() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("charValue", "");
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (Exception ignore) {}
    }

    public void testSetBeanPropsCharStringTooLong() throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("charValue", "abba");
        try {
            factory.setBeanProps(new TestBean(), jobDataMap);
            fail();
        } catch (Exception ignore) {}
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
