
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
package org.quartz.ee.jmx.jboss;

import org.jboss.system.ServiceMBean;

/**
 * Interface exposed via JMX for MBean for configuring, starting, and
 * binding to JNDI a Quartz Scheduler instance.
 *  
 * <p> 
 * Sample MBean deployment descriptor: 
 * <a href="doc-files/quartz-service.xml" type="text/plain">quartz-service.xml</a>
 * </p>
 * 
 * @see org.quartz.ee.jmx.jboss.QuartzService
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
