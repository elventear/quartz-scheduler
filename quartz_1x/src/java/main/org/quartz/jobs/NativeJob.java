/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */

package org.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/*
 * <p> Built in job for executing native executables in a separate process.</p> 
 * 
 * @author Matthew payne
 * @author James House
 * @date Sep 17, 2003 @Time: 11:27:13 AM
 */
public class NativeJob implements Job {

    /*
     *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constants.
     *  
     *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
        
    /**
     * Required parameter that specifies the name of the command (executable) 
     * to be ran.
     */
    public static final String PROP_COMMAND = "command";
    
    /**
     * Optional parameter that specifies the parameters to be passed to the
     * executed command.
     */
    public static final String PROP_PARAMETERS = "parameters";
    
    
    /**
     * Optional parameter (value should be 'true' or 'false') that specifies 
     * whether the job should wait for the execution of the native process to 
     * complete before it completes.
     * 
     * <p>Defaults to <code>true</code>.</p>  
     */
    public static final String PROP_WAIT_FOR_PROCESS = "waitForProcess";
    
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public void execute(JobExecutionContext context)
    throws JobExecutionException {

        JobDataMap data = context.getJobDetail().getJobDataMap();
        
        String command = data.getString(PROP_COMMAND);

        String parameters = data.getString(PROP_PARAMETERS);

        if (parameters == null) {
            parameters = "";
        }

        boolean wait = true;
        data.containsKey(PROP_WAIT_FOR_PROCESS);
            wait = data.getBoolean(PROP_WAIT_FOR_PROCESS);
        
        this.runNativeCommand(command, parameters, wait);
    }

    private static Log getLog()
    {
        return LogFactory.getLog(NativeJob.class);
    }
    
    private void runNativeCommand(String command, String parameters, boolean wait) throws JobExecutionException {

        String[] cmd = null;
        String[] args = new String[2];
        args[0] = command;
        args[1] = parameters;

        try {
            //with this variable will be done the swithcing
            String osName = System.getProperty("os.name");

            //only will work with Windows NT
            if (osName.equals("Windows NT")) {
                if (cmd == null) cmd = new String[args.length + 2];
                cmd[0] = "cmd.exe";
                cmd[1] = "/C";
                for (int i = 0; i < args.length; i++)
                    cmd[i + 2] = args[i];
            }
            //only will work with Windows 95
            else if (osName.equals("Windows 95")) {
                if (cmd == null) cmd = new String[args.length + 2];
                cmd[0] = "command.com";
                cmd[1] = "/C";
                for (int i = 0; i < args.length; i++)
                    cmd[i + 2] = args[i];
            }
            //only will work with Windows 2000
            else if (osName.equals("Windows 2000")) {
                if (cmd == null) cmd = new String[args.length + 2];
                cmd[0] = "cmd.exe";
                cmd[1] = "/C";

                for (int i = 0; i < args.length; i++)
                    cmd[i + 2] = args[i];
            }
            //only will work with Windows XP
            else if (osName.equals("Windows XP")) {
                if (cmd == null) cmd = new String[args.length + 2];
                cmd[0] = "cmd.exe";
                cmd[1] = "/C";

                for (int i = 0; i < args.length; i++)
                    cmd[i + 2] = args[i];
            }
            //only will work with Linux
            else if (osName.equals("Linux")) {
                if (cmd == null) cmd = new String[args.length];
                cmd = args;
            }
            //will work with the rest
            else {
                if (cmd == null) cmd = new String[args.length];
                cmd = args;
            }

            Runtime rt = Runtime.getRuntime();
            // Executes the command
            getLog().info("About to run" + cmd[0] + cmd[1]);
            Process proc = rt.exec(cmd);
            if(wait)
                proc.waitFor(); 
            // any error message?

            
        } catch (Exception x) {
            System.out.println("error happened in native job");
            throw new JobExecutionException("Error launching native command: ", x, false);
        }
    }

}
