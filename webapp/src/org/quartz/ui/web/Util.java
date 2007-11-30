/*
 *  Copyright James House (c) 2001-2004
 *
 *  All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *
 *
 * This product uses and includes within its distribution, 
 * software developed by the Apache Software Foundation 
 *     (http://www.apache.org/)
 *
 */
package org.quartz.ui.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  FIXME: Document class no.ezone.quartz.web.Util
 *
 * @since Feb 2, 2003
 * @version $Revision$
 * @author Erick Romson
 * @author Rene Eigenheer
 */

public class Util
{
	public static String JOB_DEFINITIONS_PROP="definitionManager";

	private static transient final Log logger = LogFactory.getLog( Util.class );	
	
    public static final String DATE_FORMAT_PATTERN = "yy.MM.dd hh:mm";
    static SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);

    public static final String CURRENT_SCHEDULER_PROP = "currentScheduler";

    /** The field's value is $Id$ */
    public static final String CVS_INFO = "$Id$";

    public static Scheduler getCurrentScheduler(HttpServletRequest request)
    {
        Scheduler currentScheduler = (Scheduler) request.getSession(true).getAttribute(CURRENT_SCHEDULER_PROP);
        if (currentScheduler == null)
        {
//            currentScheduler = SchedulerFactoryRepository.getDefaultScheduler();
			try {
				currentScheduler = StdSchedulerFactory.getDefaultScheduler();
			} catch (SchedulerException e) {
			}
            request.getSession().setAttribute(CURRENT_SCHEDULER_PROP, currentScheduler);
        }
        return currentScheduler;
    }

    /**
     *
     * @param date
     * @return
     */
    public static String getDateAsString(Date date)
    {
        if (date==null)
        {
            return null;
        }
        return dateFormatter.format(date);
    }

    /**
     *
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date parseStringToDate(String dateStr) throws ParseException
    {
        if (dateStr==null)
        {
            return null;
        }
        return dateFormatter.parse(dateStr);
    }

    /**
     *
     * @param trigger
     * @return
     */
    public static String getTriggerType(Trigger trigger)
    {
        String type = null;
        if (trigger == null)
        {
            return null;
        }
        
        if (trigger instanceof SimpleTrigger && ((SimpleTrigger)trigger).hasAdditionalProperties() == false ) {
        {
            type = "simple";
        }
        else if (trigger instanceof CronTrigger && ((CronTrigger)trigger).hasAdditionalProperties() == false ) {
        {
            type = "cron";
        }
        else
        {
            type = trigger.getClass().getName();
        }
        return type;
    }

    /**
     * the method scheduler.getTriggersForJob not implemented
     * @param scheduler
     * @param jobName
     * @param jobGroup
     * @return
     * @throws ServletException
     */
    public static Trigger[] getTriggersFromJob(Scheduler scheduler, String jobName, String jobGroup) throws ServletException
    {
        List triggerList = new ArrayList(5);
        String[] groups = null;
        try
        {
            groups = scheduler.getTriggerGroupNames();
        }
        catch (SchedulerException e)
        {
            logger.error("When getting all trigger groups", e);
            throw new ServletException("When getting all trigger groups", e);
        }

        for (int i = 0; i < groups.length; i++)
        {
            String group = groups[i];
            String[] names = null;
            try
            {
                names = scheduler.getTriggerNames(group);
            }
            catch (SchedulerException e)
            {
                logger.error("When getting all trigger in group groups " + group, e);
                throw new ServletException("When getting all trigger in group groups " + group, e);
            }
            for (int j = 0; j < names.length; j++)
            {
                String name = names[j];
                Trigger trigger = null;
                try
                {
                    trigger = scheduler.getTrigger(name, group);
                }
                catch (SchedulerException e)
                {
                    logger.error("When getting trigger " + name + " in group " + group, e);
                    throw new ServletException("When getting trigger " + name + " in group " + group, e);
                }

                if (trigger==null)
                {
                    logger.warn("The trigger "+name+" in group "+group+" was null");
                    continue;
                }

                if (trigger.getJobName().equals(jobName) && trigger.getJobGroup().equals(jobGroup))
                {
                    triggerList.add(trigger);
                }
            }
        }
        Trigger[] retArr = new Trigger[triggerList.size()];
        triggerList.toArray(retArr);
        return retArr;
    }
}

