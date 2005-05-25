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
package org.quartz.ui.web.form;

import java.util.ArrayList;

/**
 *  FIXME: Document class no.ezone.quartz.web.form.ChooseSchedulerForm
 *
 * @since Feb 2, 2003
 * @version $Revision$
 * @author Erick Romson
 * @author Rene Eigenheer
 */

public class ChooseSchedulerForm 
{

    private String choosenSchedulerName;
    private ArrayList schedulers;
    private SchedulerDTO scheduler;
    private ArrayList executingJobs;

    private String btnSetSchedulerAsCurrent;
  


    public String getChoosenSchedulerName()
    {
        return choosenSchedulerName;
    }

    public void setChoosenSchedulerName(String choosenSchedulerName)
    {
        this.choosenSchedulerName = choosenSchedulerName;
    }


	/**
	 * Returns the schedulers.
	 * @return ArrayList
	 */
	public ArrayList getSchedulers() {
		return schedulers;
	}

	/**
	 * Sets the schedulers.
	 * @param schedulers The schedulers to set
	 */
	public void setSchedulers(ArrayList schedulers) {
		this.schedulers = schedulers;
	}

	/**
	 * Returns the scheduler.
	 * @return SchedulerForm
	 */
	public SchedulerDTO getScheduler() {
		return scheduler;
	}

	/**
	 * Sets the scheduler.
	 * @param scheduler The scheduler to set
	 */
//	public void setScheduler(SchedulerForm scheduler) {
//		this.scheduler = scheduler;
//	}

	/**
	 * Sets the scheduler.
	 * @param scheduler The scheduler to set
	 */
	public void setScheduler(SchedulerDTO scheduler) {
		this.scheduler = scheduler;
	}
	
	/**
	 * Returns the executingJobs.
	 * @return ArrayList
	 */
	public ArrayList getExecutingJobs() {
		return executingJobs;
	}

	/**
	 * Sets the executingJobs.
	 * @param executingJobs The executingJobs to set
	 */
	public void setExecutingJobs(ArrayList executingJobs) {
		this.executingJobs = executingJobs;
	}



	/**
	 * Returns the btnSetSchedulerAsCurrent.
	 * @return String
	 */
	public String getBtnSetSchedulerAsCurrent() {
		return btnSetSchedulerAsCurrent;
	}

	/**
	 * Sets the btnSetSchedulerAsCurrent.
	 * @param btnSetSchedulerAsCurrent The btnSetSchedulerAsCurrent to set
	 */
	public void setBtnSetSchedulerAsCurrent(String btnSetSchedulerAsCurrent) {
		this.btnSetSchedulerAsCurrent = btnSetSchedulerAsCurrent;
	}

	

}

