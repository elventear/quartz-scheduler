/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Reports QuartzMetaDataProcessor validation exceptions.
 * 
 * @author <a href="mailto:bonhamcm@thirdeyeconsulting.com">Chris Bonham</a>
 */
public class ValidationException extends Exception {
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private Collection validationExceptions = new ArrayList();

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Constructor for ValidationException.
     */
    public ValidationException() {
        super();
    }

    /**
     * Constructor for ValidationException.
     * 
     * @param message
     *          exception message.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructor for ValidationException.
     * 
     * @param validationExceptions
     *          collection of validation exceptions.
     */
    public ValidationException(Collection errors) {
        this();
        this.validationExceptions = Collections
                .unmodifiableCollection(validationExceptions);
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Returns collection of errors.
     * 
     * @return collection of errors.
     */
    public Collection getValidationExceptions() {
        return validationExceptions;
    }

    /**
     * Returns the detail message string.
     * 
     * @return the detail message string.
     */
    public String getMessage() {
        if (getValidationExceptions().size() == 0) { return super.getMessage(); }

        StringBuffer sb = new StringBuffer();

        boolean first = true;

        for (Iterator iter = getValidationExceptions().iterator(); iter
                .hasNext(); ) {
            Exception e = (Exception) iter.next();

            if (!first) {
                sb.append('\n');
                first = false;
            }

            sb.append(e.getMessage());
        }

        return sb.toString();
    }
}
