/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */
package org.quartz.osgi.tests;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class HelloJob implements Job {
	private int count;
	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		System.out.println("Hello world!");
		count++;
	}

	public int getCount() {
		return count;
	}
}

