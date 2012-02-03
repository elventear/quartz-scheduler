package org.quartz.utils;

import java.util.Properties;

import junit.framework.TestCase;

/**
 * Unit tests for PropertiesParser.
 */
public class PropertiesParserTest extends TestCase {

    /**
     * Unit test for full getPropertyGroup() method.
     */
    public void testGetPropertyGroupStringBooleanStringArray() {
        // Test that an empty property does not cause an exception
        Properties props = new Properties();
        props.put("x.y.z", "");
        
        PropertiesParser propertiesParser = new PropertiesParser(props);
        Properties propGroup = propertiesParser.getPropertyGroup("x.y", true, new String[] {});
        assertEquals("", propGroup.getProperty("z"));
    }
}
