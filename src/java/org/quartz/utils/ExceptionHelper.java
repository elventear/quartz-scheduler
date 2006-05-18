/* 
 * Copyright 2004-2006 OpenSymphony 
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
package org.quartz.utils;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Helper class for handling exception nesting, which is only supported in JDKs
 * which are version 1.4 or later. 
 * </p>
 * 
 * Sample usage:
 * <pre>
 * try {
 *     // Validate arguments
 * } catch (Exception e) {
 *     Exception myException = new IllegalArgumentException("Doh!");
 *     ExceptionHelper.setCause(myException, e);
 *     throw myException;
 * }
 * </pre>
 */
public class ExceptionHelper {
    private static Boolean supportsNestedThrowable = null;
    
    private ExceptionHelper() {
    }
    
    /**
     * Set the given <code>Throwable<code>'s cause if this JDK supports
     * the <code>Throwable#initCause(Throwable)</code> method. 
     */
    public static Throwable setCause(Throwable exception, Throwable cause) {
        if (exception != null) {
            if (supportsNestedThrowable()) {
                try {
                    Method initCauseMethod = 
                        exception.getClass().getMethod("initCause", new Class[] {Throwable.class});
                    initCauseMethod.invoke(exception, new Object[] {cause});
                } catch (Exception e) {
                    getLog().warn(
                        "Unable to invoke initCause() method on class: " + 
                        exception.getClass().getName(), e);
                }
            }
        }        
        return exception;
    }
    
    /**
     * Get the underlying cause <code>Throwable</code> of the given exception
     * if this JDK supports the <code>Throwable#getCause()</code> method. 
     */
    public static Throwable getCause(Throwable exception) {
        if (supportsNestedThrowable()) {
            try {
                Method getCauseMethod = 
                    exception.getClass().getMethod("getCause", (Class[])null);
                return (Throwable)getCauseMethod.invoke(exception, (Object[])null);
            } catch (Exception e) {
                getLog().warn(
                    "Unable to invoke getCause() method on class: " + 
                    exception.getClass().getName(), e);
            }
        }
        
        return null;
    }
    
    /**
     * Get whether the Throwable hierarchy for this JDK supports 
     * initCause()/getCause(). 
     */
    public static synchronized boolean supportsNestedThrowable() {
        if (supportsNestedThrowable == null) {
            try {
                Throwable.class.getMethod("initCause", new Class[] {Throwable.class});
                Throwable.class.getMethod("getCause", (Class[])null);
                supportsNestedThrowable = Boolean.TRUE;
                getLog().debug("Detected JDK support for nested exceptions.");
            } catch (NoSuchMethodException e) {
                supportsNestedThrowable = Boolean.FALSE;
                getLog().debug("Nested exceptions are not supported by this JDK.");
            }
        }
        
        return supportsNestedThrowable.booleanValue();
    }
    
    private static Log getLog() {
        return LogFactory.getLog(ExceptionHelper.class);
    }
}
