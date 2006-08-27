package org.quartz.jobs;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A Job executing ANT scripts.<br>
 * Brief usage example:
 *
 * <p><blockquote><pre>
 * ...
 *
 * JobDetail job = new JobDetail();
 * job.setGroup("ANT");
 * job.setName("Example");
 * job.setJobClass(AntJob.class);
 *
 * Map dataMap = job.getJobDataMap();
 * dataMap.put("execdir", "pathToYourAntScripts");
 * dataMap.put("buildfile", "yourAntScript.xml"); //default is build.xml
 * dataMap.put("target", "targetToExecute"); //default is script's dafault target
 *
 * //set the 'lib.dir' ant property to './lib'
 * dataMap.put("$P_lib.dir", "./lib");
 *
 * //set the 'debug' ant (user) property to 'true'
 * dataMap.put("$U_debug", "true");
 *
 * ...
 * </pre></blockquote>
 * <p>
 *
 * @author Dimitar Marinov
 */
public class AntJob implements Job {

    /**
     * Creates a new <code>AntJob</code>.
     */
    public AntJob() {
        super();
    }

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        JobDataMap dataMap = context.getMergedJobDataMap();
        String execDir = dataMap.getString("execdir");
        String buildFileName = dataMap.getString("buildfile");
        String target = dataMap.getString("target");

        // Create a new project, and perform some default initialization
        Project project = new Project();
        try {
            project.init();
        } catch (BuildException e) {
            throw new JobExecutionException(e.getMessage());
        }
        
        // Prepare the buildfile. If none is given, "build.xml" is used.
        if (buildFileName == null) {
            buildFileName = "build.xml";
        }
        File buildFile = new File(execDir + File.separator + buildFileName);
        
        // Set system properties
        Properties sysProps = System.getProperties();
        Iterator it = sysProps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            project.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        
        // Loop through the parameter property map
        it = dataMap.keySet().iterator();
        while (it.hasNext()) {
            // Get the property's name and value
            String propName = (String) it.next();
            String propValue = dataMap.getString(propName);
            if (propName.startsWith("$P_")) {
                project.setProperty(propName.substring(3), propValue);
            } else if (propName.startsWith("$U_")) {
                project.setUserProperty(propName.substring(3), propValue);
            }
        }
        
        try {
            ProjectHelper.getProjectHelper().parse(project, buildFile);
            
            // Set ANT built-in properties
            project.setProperty("basedir", project.getBaseDir().getPath());
            project.setProperty("ant.project.name", project.getName());
            project.setProperty("ant.file", buildFile.getPath());
            
            // If no target is specified, run the default one.
            if (target == null) {
                target = project.getDefaultTarget();
            }
            
            // Run the target
            project.executeTarget(target);
            
        } catch (BuildException e) {
            throw new JobExecutionException(e.getMessage());
        }
    }
}