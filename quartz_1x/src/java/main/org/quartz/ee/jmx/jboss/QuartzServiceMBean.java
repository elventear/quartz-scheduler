/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.ee.jmx.jboss;

import org.jboss.system.ServiceMBean;

/**
 * 
 * See org/quartz/ee/jmx/jboss/doc-files/quartz-service.xml for an example
 * service mbean deployment descriptor.
 * 
 * @author Andrew Collins
 */
public interface QuartzServiceMBean extends ServiceMBean {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public void setJndiName(String jndiName) throws Exception;

    public String getJndiName();

    public void setProperties(String properties);

    public void setPropertiesFile(String propertiesFile);

}
