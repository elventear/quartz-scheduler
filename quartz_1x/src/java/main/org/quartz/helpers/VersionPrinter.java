/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.helpers;

import org.quartz.core.QuartzScheduler;

/**
 * <p>
 * Prints the version of Quartz on stdout.
 * </p>
 * 
 * @author James House
 */
public class VersionPrinter {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public static void main(String[] args) {
        System.out.println("Quartz version: " + QuartzScheduler.VERSION_MAJOR
                + "." + QuartzScheduler.VERSION_MINOR + "."
                + QuartzScheduler.VERSION_ITERATION);
    }
}
