/*
 * Created on Mar 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.quartz.examples.example13;

import org.quartz.StatefulJob;

/**
 * This job has the same functionality of SimpleRecoveryJob
 * except that this job implements the StatefulJob interface
 * 
 * @author Bill Kratzer
 */
public class SimpleRecoveryStatefulJob
	extends SimpleRecoveryJob
	implements StatefulJob {

	/**
	 * 
	 */
	public SimpleRecoveryStatefulJob() {
		super();
	}

}
