/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.simpl;

import org.quartz.spi.ClassLoadHelper;

/**
 * A <code>ClassLoadHelper</code> that uses either the current thread's
 * context class loader (<code>Thread.currentThread().getContextClassLoader().loadClass( .. )</code>).
 * 
 * @see org.quartz.spi.ClassLoadHelper
 * @see org.quartz.simpl.InitThreadContextClassLoadHelper
 * @see org.quartz.simpl.SimpleClassLoadHelper
 * @see org.quartz.simpl.CascadingClassLoadHelper
 * 
 * @author jhouse
 */
public class ThreadContextClassLoadHelper implements ClassLoadHelper {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Called to give the ClassLoadHelper a chance to initialize itself,
     * including the oportunity to "steal" the class loader off of the calling
     * thread, which is the thread that is initializing Quartz.
     */
    public void initialize() {
    }

    /**
     * Return the class with the given name.
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(name);
    }

}
