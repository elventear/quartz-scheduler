/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */

package org.quartz.jobs;

/**
 * Interface for objects wishing to receive a 'call-back' from a 
 * <code>FileScanJob</code>.
 * 
 * @author jhouse
 * @see org.quartz.jobs.FileScanJob
 */
public interface FileScanListener {

    public void fileUpdated(String fileName);
}
