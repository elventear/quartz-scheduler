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
package org.quartz.jobs.ee.jms;

import org.quartz.SchedulerException;

/**
 * The JmsJobException is used to indicate an error during sending of a 
 * <code>javax.jms.Messaage</code>.
 * 
 * @author Weston M. Price
 * 
 *
 */
public class JmsJobException extends SchedulerException {

    private static final long serialVersionUID = 3045647075496522093L;

    public JmsJobException() {
    }

    public JmsJobException(String message) {
        super(message);
    }

    public JmsJobException(Throwable cause) {
        super(cause);
    }

    public JmsJobException(String message, Throwable cause) {
        super(message, cause);
    }

}
