/* 
 * Copyright 2001-2010 Terracotta, Inc. 
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

package org.quartz.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.ClassLoadHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Parses an XML file that declares Jobs and their schedules (Triggers).
 * 
 * The xml document must conform to the format defined in
 * "job_scheduling_data_1_8.xsd"
 * 
 * After creating an instance of this class, you should call one of the <code>processFile()</code>
 * functions, after which you may call the <code>getLoadedJobs()</code> and <code>getLoadedTriggers()</code>
 * function to get a handle to the defined Jobs and Triggers, which can then be
 * scheduled with the <code>Scheduler</code>. Alternatively, you could call
 * the <code>processFileAndScheduleJobs()</code> function to do all of this
 * in one step.
 * 
 * The same instance can be used again and again, with the list of defined Jobs
 * being cleared each time you call a <code>processFile</code> method,
 * however a single instance is not thread-safe.
 * 
 * @author James House
 * @author Past contributions from <a href="mailto:bonhamcm@thirdeyeconsulting.com">Chris Bonham</a>
 * @author Past contributions from pl47ypus
 * 
 * @since Quartz 1.8
 */
public class JobSchedulingDataLoader implements ErrorHandler {
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public static final String QUARTZ_NS = "http://www.quartz-scheduler.org/xml/JobSchedulingData";

    public static final String QUARTZ_SCHEMA_WEB_URL = "http://www.quartz-scheduler.org/xml/job_scheduling_data_1_8.xsd";
    
    public static final String QUARTZ_XSD_PATH_IN_JAR = "org/quartz/xml/job_scheduling_data_1_8.xsd";

    public static final String QUARTZ_XML_DEFAULT_FILE_NAME = "quartz_data.xml";

    public static final String QUARTZ_SYSTEM_ID_JAR_PREFIX = "jar:";
    
    /**
     * XML Schema dateTime datatype format.
     * <p>
     * See <a
     * href="http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#dateTime">
     * http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#dateTime</a>
     */
    protected static final String XSD_DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss";

    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat(XSD_DATE_FORMAT);


    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected List<JobDetail> loadedJobs = new LinkedList<JobDetail>();
    protected List<Trigger> loadedTriggers = new LinkedList<Trigger>();
    
    protected Collection validationExceptions = new ArrayList();
    
    protected ClassLoadHelper classLoadHelper;

    private boolean overWriteExistingData = true;
    
    private DocumentBuilder docBuilder = null;
    private XPath xpath = null;
    
    private ThreadLocal schedLocal = new ThreadLocal();
    
    private final Log log = LogFactory.getLog(getClass());

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
     
    /**
     * Constructor for JobSchedulingDataLoader.
     * 
     * @param clh               class-loader helper to share with digester.
     * @throws ParserConfigurationException if the XML parser cannot be configured as needed. 
     */
    public JobSchedulingDataLoader(ClassLoadHelper clh) throws ParserConfigurationException {
        this.classLoadHelper = clh;
        initDocumentParser();
    }

    /**
     * Initializes the XML parser.
     * @throws ParserConfigurationException 
     */
    protected void initDocumentParser() throws ParserConfigurationException  {

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        docBuilderFactory.setNamespaceAware(true);
        docBuilderFactory.setValidating(true);
        
        docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        
        docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", resolveSchemaSource());
        
        docBuilder = docBuilderFactory.newDocumentBuilder();
        
        docBuilder.setErrorHandler(this);
        
        NamespaceContext nsContext = new NamespaceContext()
        {
          public String getNamespaceURI(String prefix)
          {
              if (prefix == null)
                  throw new IllegalArgumentException("Null prefix");
              if (XMLConstants.XML_NS_PREFIX.equals(prefix))
                  return XMLConstants.XML_NS_URI;
              if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))
                  return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        
              if ("q".equals(prefix))
                  return QUARTZ_NS;
        
              return XMLConstants.NULL_NS_URI;
          }
        
          public Iterator getPrefixes(String namespaceURI)
          {
              // This method isn't necessary for XPath processing.
              throw new UnsupportedOperationException();
          }
        
          public String getPrefix(String namespaceURI)
          {
              // This method isn't necessary for XPath processing.
              throw new UnsupportedOperationException();
          }
        
        }; 
        
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(nsContext);
    }
    
    public Object resolveSchemaSource() {
        InputSource inputSource = null;

        InputStream is = null;

        URL url = null;

        try {
            is = classLoadHelper.getResourceAsStream(QUARTZ_XSD_PATH_IN_JAR);
        }  finally {
            if (is != null) {
                inputSource = new InputSource(is);
                inputSource.setSystemId(QUARTZ_SCHEMA_WEB_URL);
                log.debug("Utilizing schema packaged in local quartz distribution jar.");
            }
            else {
                log.info("Unable to load local schema packaged in quartz distribution jar. Utilizing schema online at " + QUARTZ_SCHEMA_WEB_URL);
                return QUARTZ_SCHEMA_WEB_URL;
            }
                
        }

        return inputSource;
    }

    /**
     * Returns whether to overwrite existing scheduling data.
     * 
     * @return whether to overwrite existing scheduling data.
     */
    public boolean getOverWriteExistingData() {
        return overWriteExistingData;
    }
    
    /**
     * Sets whether to overwrite existing scheduling data.
     * 
     * @param overWriteExistingData boolean.
     */
    protected void setOverWriteExistingData(boolean overWriteExistingData) {
        this.overWriteExistingData = overWriteExistingData;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Process the xml file in the default location (a file named
     * "quartz_jobs.xml" in the current working directory).
     *  
     */
    public void processFile() throws Exception {
        processFile(QUARTZ_XML_DEFAULT_FILE_NAME);
    }

    /**
     * Process the xml file named <code>fileName</code>.
     * 
     * @param fileName
     *          meta data file name.
     */
    public void processFile(String fileName) throws Exception {
        processFile(fileName, getSystemIdForFileName(fileName));
    }

    /**
     * For the given <code>fileName</code>, attempt to expand it to its full path
     * for use as a system id.
     * 
     * @see #getURL(String)
     * @see #processFile()
     * @see #processFile(String)
     * @see #processFileAndScheduleJobs(Scheduler, boolean)
     * @see #processFileAndScheduleJobs(String, Scheduler, boolean)
     */
    protected String getSystemIdForFileName(String fileName) {
        InputStream fileInputStream = null;
        try {
            String urlPath = null;
            
            File file = new File(fileName); // files in filesystem
            if (!file.exists()) {
                URL url = getURL(fileName);
                if (url != null) {
                    try {
                        urlPath = URLDecoder.decode(url.getPath(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        log.warn("Unable to decode file path URL", e);
                    } 
                    try {
                        if(url != null)
                            fileInputStream = url.openStream();
                    } catch (IOException ignore) {
                    }
                }        
            } else {
                try {              
                    fileInputStream = new FileInputStream(file);
                }catch (FileNotFoundException ignore) {
                }
            }
            
            if (fileInputStream == null) {
                log.debug("Unable to resolve '" + fileName + "' to full path, so using it as is for system id.");
                return fileName;
            } else {
                return (urlPath != null) ? urlPath : file.getAbsolutePath();
            }
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException ioe) {
                log.warn("Error closing jobs file: " + fileName, ioe);
            }
        }
    }

    /**
     * Returns an <code>URL</code> from the fileName as a resource.
     * 
     * @param fileName
     *          file name.
     * @return an <code>URL</code> from the fileName as a resource.
     */
    protected URL getURL(String fileName) {
        return classLoadHelper.getResource(fileName); 
    }

    protected void prepForProcessing()
    {
        clearValidationExceptions();
        
        setOverWriteExistingData(true);

        loadedJobs.clear();
        loadedTriggers.clear();
    }
    
    /**
     * Process the xmlfile named <code>fileName</code> with the given system
     * ID.
     * 
     * @param fileName
     *          meta data file name.
     * @param systemId
     *          system ID.
     */
    public void processFile(String fileName, String systemId)
        throws ValidationException, ParserConfigurationException,
            SAXException, IOException, SchedulerException,
            ClassNotFoundException, ParseException, XPathException {

        prepForProcessing();
        
        log.info("Parsing XML file: " + fileName + 
                " with systemId: " + systemId);
        InputSource is = new InputSource(getInputStream(fileName));
        is.setSystemId(systemId);
        
        process(is);
        
        maybeThrowValidationException();
    }
    
    /**
     * Process the xmlfile named <code>fileName</code> with the given system
     * ID.
     * 
     * @param stream
     *          an input stream containing the xml content.
     * @param systemId
     *          system ID.
     */
    public void processStream(InputStream stream, String systemId)
        throws ValidationException, ParserConfigurationException,
            SAXException, XPathException, IOException, SchedulerException,
            ClassNotFoundException, ParseException {

        prepForProcessing();

        log.info("Parsing XML from stream with systemId: " + systemId);

        InputSource is = new InputSource(stream);
        is.setSystemId(systemId);

        process(is);

        maybeThrowValidationException();
    }
    
    protected void process(InputSource is) throws SAXException, IOException, ParseException, XPathException, ClassNotFoundException {
        
        // load the document 
        Document document = docBuilder.parse(is);
        
        // TODO: FIXME:  get overwrite data attribute!
        
        //
        // Extract Job definitions...
        //

        NodeList jobNodes = (NodeList) xpath.evaluate("/q:job-scheduling-data/q:job",
                document, XPathConstants.NODESET);

        log.debug("Found " + jobNodes.getLength() + " job definitions.");

        for (int i = 0; i < jobNodes.getLength(); i++) {
            Node jobDetailNode = jobNodes.item(i);
            String t = null;

            String jobName = getTrimmedToNullString(xpath, "q:name", jobDetailNode);
            String jobGroup = getTrimmedToNullString(xpath, "q:group", jobDetailNode);
            String jobDescription = getTrimmedToNullString(xpath, "q:description", jobDetailNode);
            String jobClassName = getTrimmedToNullString(xpath, "q:job-class", jobDetailNode);
            t = getTrimmedToNullString(xpath, "q:volatility", jobDetailNode);
            boolean jobVolatility = (t != null) && t.equals("true");
            t = getTrimmedToNullString(xpath, "q:durability", jobDetailNode);
            boolean jobDurability = (t != null) && t.equals("true");
            t = getTrimmedToNullString(xpath, "q:recover", jobDetailNode);
            boolean jobRecoveryRequested = (t != null) && t.equals("true");

            Class jobClass = classLoadHelper.loadClass(jobClassName);

            JobDetail jobDetail = new JobDetail(jobName, jobGroup,
                    jobClass, jobVolatility, jobDurability,
                    jobRecoveryRequested);
            jobDetail.setDescription(jobDescription);

            NodeList jobListenerEntries = (NodeList) xpath.evaluate(
                    "q:job-listener-ref", jobDetailNode,
                    XPathConstants.NODESET);
            for (int j = 0; j < jobListenerEntries.getLength(); j++) {
                Node listenerRefNode = jobListenerEntries.item(j);
                String ref = listenerRefNode.getTextContent();
                if(ref != null && (ref = ref.trim()).length() == 0)
                    ref = null;
                if(ref == null)
                    continue;
                jobDetail.addJobListener(ref);
            }

            NodeList jobDataEntries = (NodeList) xpath.evaluate(
                    "q:job-data-map/q:entry", jobDetailNode,
                    XPathConstants.NODESET);
            
            for (int k = 0; k < jobDataEntries.getLength(); k++) {
                Node entryNode = jobDataEntries.item(k);
                String key = getTrimmedToNullString(xpath, "q:key", entryNode);
                String value = getTrimmedToNullString(xpath, "q:value", entryNode);
                jobDetail.getJobDataMap().put(key, value);
            }
            
            if(log.isDebugEnabled())
                log.debug("Parsed job definition: " + jobDetail);

            addJobToSchedule(jobDetail);
        }
        
        //
        // Extract Trigger definitions...
        //

        NodeList triggerEntries = (NodeList) xpath.evaluate(
                "/q:job-scheduling-data/q:trigger/*", document, XPathConstants.NODESET);

        log.debug("Found " + triggerEntries.getLength() + " trigger definitions.");

        for (int j = 0; j < triggerEntries.getLength(); j++) {
            Node triggerNode = triggerEntries.item(j);
            String triggerName = getTrimmedToNullString(xpath, "q:name", triggerNode);
            String triggerGroup = getTrimmedToNullString(xpath, "q:group", triggerNode);
            String triggerDescription = getTrimmedToNullString(xpath, "q:description", triggerNode);
            String triggerMisfireInstructionConst = getTrimmedToNullString(xpath, "q:misfire-instruction", triggerNode);
            String triggerCalendarRef = getTrimmedToNullString(xpath, "q:calendar-name", triggerNode);
            String triggerJobName = getTrimmedToNullString(xpath, "q:job-name", triggerNode);
            String triggerJobGroup = getTrimmedToNullString(xpath, "q:job-group", triggerNode);
            String t = getTrimmedToNullString(xpath, "q:volatility", triggerNode);
            boolean triggerVolatility = (t != null) && t.equals("true");

            String startTimeString = getTrimmedToNullString(xpath, "q:start-time", triggerNode);
            String endTimeString = getTrimmedToNullString(xpath, "q:end-time", triggerNode);

            Date triggerStartTime = startTimeString == null || startTimeString.length() == 0 ? new Date() : dateFormat.parse(startTimeString);
            Date triggerEndTime = endTimeString == null || endTimeString.length() == 0 ? null : dateFormat.parse(endTimeString);

            Trigger trigger = null;

            if (triggerNode.getNodeName().equals("simple")) {
                String repeatCountString = getTrimmedToNullString(xpath, "q:repeat-count", triggerNode);
                String repeatIntervalString = getTrimmedToNullString(xpath, "q:repeat-interval", triggerNode);

                int repeatCount = repeatCountString == null ? SimpleTrigger.REPEAT_INDEFINITELY : Integer.parseInt(repeatCountString);
                long repeatInterval = repeatIntervalString == null ? 0 : Long.parseLong(repeatIntervalString);

                trigger = new SimpleTrigger(triggerName, triggerGroup,
                        triggerJobName, triggerJobGroup,
                        triggerStartTime, triggerEndTime, 
                        repeatCount, repeatInterval);
            } else if (triggerNode.getNodeName().equals("cron")) {
                String cronExpression = getTrimmedToNullString(xpath, "q:cron-expression", triggerNode);
                String timezoneString = getTrimmedToNullString(xpath, "q:time-zone", triggerNode);

                
                TimeZone tz = timezoneString == null ? null : TimeZone.getTimeZone(timezoneString);

                trigger = new CronTrigger(triggerName, triggerGroup,
                        triggerJobName, triggerJobGroup,
                        triggerStartTime, triggerEndTime,
                        cronExpression, tz);
            } else {
                throw new ParseException("Unknown trigger type: " + triggerNode.getNodeName(), -1);
            }

            trigger.setVolatility(triggerVolatility);
            trigger.setDescription(triggerDescription);
            trigger.setCalendarName(triggerCalendarRef);

            if (triggerMisfireInstructionConst != null && triggerMisfireInstructionConst.length() != 0) {
                Class clazz = trigger.getClass();
                java.lang.reflect.Field field;
                try {
                    field = clazz.getField(triggerMisfireInstructionConst);
                    int misfireInst = field.getInt(trigger);
                    trigger.setMisfireInstruction(misfireInst);
                } catch (Exception e) {
                    throw new ParseException("Unexpected/Unhandlable Misfire Instruction encountered '" + triggerMisfireInstructionConst + "', for trigger: " + trigger.getFullName(), -1);
                }
            }

            if(log.isDebugEnabled())
                log.debug("Parsed trigger definition: " + trigger);
            
            addTriggerToSchedule(trigger);
        }
    }
    
    protected String getTrimmedToNullString(XPath xpath, String elementName, Node parentNode) throws XPathExpressionException {
        String str = (String) xpath.evaluate(elementName,
                parentNode, XPathConstants.STRING);
        
        if(str != null)
            str = str.trim();
        
        if(str != null && str.length() == 0)
            str = null;
        
        return str;
    }

    /**
     * Process the xml file in the default location, and schedule all of the
     * jobs defined within it.
     *  
     */
    public void processFileAndScheduleJobs(Scheduler sched,
            boolean overWriteExistingJobs) throws SchedulerException, Exception {
        processFileAndScheduleJobs(QUARTZ_XML_DEFAULT_FILE_NAME, sched,
                overWriteExistingJobs);
    }

    /**
     * Process the xml file in the given location, and schedule all of the
     * jobs defined within it.
     * 
     * @param fileName
     *          meta data file name.
     */
    public void processFileAndScheduleJobs(String fileName, Scheduler sched,
            boolean overWriteExistingJobs) throws Exception {
        processFileAndScheduleJobs(fileName, getSystemIdForFileName(fileName), sched, overWriteExistingJobs);
    }
    
    /**
     * Process the xml file in the given location, and schedule all of the
     * jobs defined within it.
     * 
     * @param fileName
     *          meta data file name.
     */
    public void processFileAndScheduleJobs(String fileName, String systemId,
            Scheduler sched, boolean overWriteExistingJobs) throws Exception {
        schedLocal.set(sched);
        try {
            processFile(fileName, systemId);
            scheduleJobs(getLoadedJobs(), getLoadedTriggers(), sched, overWriteExistingJobs);
        } finally {
            schedLocal.set(null);
        }
    }

    /**
     * Returns a <code>List</code> of jobs loaded from the xml file.
     * <p/>
     * 
     * @return a <code>List</code> of jobs.
     */
    public List<JobDetail> getLoadedJobs() {
        return Collections.unmodifiableList(loadedJobs);
    }
    
    /**
     * Returns a <code>List</code> of triggers loaded from the xml file.
     * <p/>
     * 
     * @return a <code>List</code> of triggers.
     */
    public List<Trigger> getLoadedTriggers() {
        return Collections.unmodifiableList(loadedTriggers);
    }

    /**
     * Returns an <code>InputStream</code> from the fileName as a resource.
     * 
     * @param fileName
     *          file name.
     * @return an <code>InputStream</code> from the fileName as a resource.
     */
    protected InputStream getInputStream(String fileName) {
        return this.classLoadHelper.getResourceAsStream(fileName);
    }
    
    public void addJobToSchedule(JobDetail job) {
        loadedJobs.add(job);
    }
    
    public void addTriggerToSchedule(Trigger trigger) {
        loadedTriggers.add(trigger);
    }

    public Map<String, List<Trigger>> buildTriggersByFQJobNameMap(List<Trigger> triggers) {
        
        Map<String, List<Trigger>> triggersByFQJobName = new HashMap<String, List<Trigger>>();
        
        for(Trigger trigger: triggers) {
            List<Trigger> triggersOfJob = triggersByFQJobName.get(trigger.getFullJobName());
            if(triggersOfJob == null) {
                triggersOfJob = new LinkedList<Trigger>();
                triggersByFQJobName.put(trigger.getFullJobName(), triggersOfJob);
            }
            triggersOfJob.add(trigger);
        }

        return triggersByFQJobName;
    }
    
    

    /**
     * Schedules a given job and trigger (both wrapped by a <code>JobSchedulingBundle</code>).
     * 
     * @param job
     *          job wrapper.
     * @param sched
     *          job scheduler.
     * @param localOverWriteExistingJobs
     *          locally overwrite existing jobs.
     * @exception SchedulerException
     *              if the Job or Trigger cannot be added to the Scheduler, or
     *              there is an internal Scheduler error.
     */
    public void scheduleJobs(List<JobDetail> jobs, List<Trigger> triggers, Scheduler sched, boolean localOverWriteExistingJobs)
        throws SchedulerException {
        
        log.info("Adding " + jobs.size() + " jobs, " + triggers.size() + " triggers.");     
        
        jobs = new LinkedList(jobs);
        triggers = new LinkedList(triggers);
        
        Map<String, List<Trigger>> triggersByFQJobName = buildTriggersByFQJobNameMap(triggers);
        
        // add each job, and it's associated triggers
        Iterator<JobDetail> itr = jobs.iterator();
        while(itr.hasNext()) {
            JobDetail detail = itr.next();
            itr.remove(); // remove jobs as we handle them...
            
            JobDetail dupeJ = sched.getJobDetail(detail.getName(), detail.getGroup());

            if ((dupeJ != null) && !localOverWriteExistingJobs) {
                log.info("Not overwriting existing job: " + dupeJ.getFullName());
                return;
            }
            
            if (dupeJ != null) {
                log.info("Replacing job: " + detail.getFullName());
            } else {
                log.info("Adding job: " + detail.getFullName());
            }
            
            List<Trigger> triggersOfJob = triggersByFQJobName.get(detail.getFullName());
            
            if (!detail.isDurable() && (triggersOfJob == null || triggersOfJob.size() == 0)) {
                if (dupeJ == null) {
                    throw new SchedulerException(
                        "A new job defined without any triggers must be durable: " + 
                        detail.getFullName());
                }
                
                if ((dupeJ.isDurable() && 
                    (sched.getTriggersOfJob(
                        detail.getName(), detail.getGroup()).length == 0))) {
                    throw new SchedulerException(
                        "Can't change existing durable job without triggers to non-durable: " + 
                        detail.getFullName());
                }
            }
            
            boolean addJobWithFirstSchedule = false;
            
            if(dupeJ != null || detail.isDurable()) {
                sched.addJob(detail, true); // add the job if a replacement or durable
            }
            else {
                addJobWithFirstSchedule = true;
            }

            // Add triggers related to the job...
            Iterator<Trigger> titr = triggersOfJob.iterator();
            while(titr.hasNext()) {
                Trigger trigger = titr.next(); 
                triggers.remove(trigger);  // remove triggers as we handle them...

                if(trigger.getStartTime() == null) {
                    trigger.setStartTime(new Date());
                }
                
                boolean addedTrigger = false;
                while (addedTrigger == false) {
                    Trigger dupeT = sched.getTrigger(trigger.getName(), trigger.getGroup());
                    if (dupeT != null) {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                "Rescheduling job: " + trigger.getFullJobName() + " with updated trigger: " + trigger.getFullName());
                        }
                        if(!dupeT.getJobGroup().equals(trigger.getJobGroup()) || !dupeT.getJobName().equals(trigger.getJobName())) {
                            log.warn("Possibly duplicately named triggers in jobs xml file!");
                        }
                        sched.rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                "Scheduling job: " + trigger.getFullJobName() + " with trigger: " + trigger.getFullName());
                        }
    
                        try {
                            if(addJobWithFirstSchedule) {
                                sched.scheduleJob(detail, trigger); // add the job if it's not in yet...
                                addJobWithFirstSchedule = false;
                            }
                            else {
                                sched.scheduleJob(trigger);
                            }
                        } catch (ObjectAlreadyExistsException e) {
                            if (log.isDebugEnabled()) {
                                log.debug(
                                    "Adding trigger: " + trigger.getFullName() + " for job: " + detail.getFullName() + 
                                    " failed because the trigger already existed.  " +
                                    "This is likely due to a race condition between multiple instances " + 
                                    "in the cluster.  Will try to reschedule instead.");
                            }
                            continue;
                        }
                    }
                    addedTrigger = true;
                }
            }
        }
        
        // add triggers that weren't associated with a new job... (those we already handled were removed above)
        for(Trigger trigger: triggers) {
            
            if(trigger.getStartTime() == null) {
                trigger.setStartTime(new Date());
            }
            
            boolean addedTrigger = false;
            while (addedTrigger == false) {
                Trigger dupeT = sched.getTrigger(trigger.getName(), trigger.getGroup());
                if (dupeT != null) {
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "Rescheduling job: " + trigger.getFullJobName() + " with updated trigger: " + trigger.getFullName());
                    }
                    if(!dupeT.getJobGroup().equals(trigger.getJobGroup()) || !dupeT.getJobName().equals(trigger.getJobName())) {
                        log.warn("Possibly duplicately named triggers in jobs xml file!");
                    }
                    sched.rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(
                            "Scheduling job: " + trigger.getFullJobName() + " with trigger: " + trigger.getFullName());
                    }

                    try {
                        sched.scheduleJob(trigger);
                    } catch (ObjectAlreadyExistsException e) {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                "Adding trigger: " + trigger.getFullName() + " for job: " +trigger.getFullJobName() + 
                                " failed because the trigger already existed.  " +
                                "This is likely due to a race condition between multiple instances " + 
                                "in the cluster.  Will try to reschedule instead.");
                        }
                        continue;
                    }
                }
                addedTrigger = true;
            }
        }

    }

    /**
     * ErrorHandler interface.
     * 
     * Receive notification of a warning.
     * 
     * @param e
     *          The error information encapsulated in a SAX parse exception.
     * @exception SAXException
     *              Any SAX exception, possibly wrapping another exception.
     */
    public void warning(SAXParseException e) throws SAXException {
        addValidationException(e);
    }

    /**
     * ErrorHandler interface.
     * 
     * Receive notification of a recoverable error.
     * 
     * @param e
     *          The error information encapsulated in a SAX parse exception.
     * @exception SAXException
     *              Any SAX exception, possibly wrapping another exception.
     */
    public void error(SAXParseException e) throws SAXException {
        addValidationException(e);
    }

    /**
     * ErrorHandler interface.
     * 
     * Receive notification of a non-recoverable error.
     * 
     * @param e
     *          The error information encapsulated in a SAX parse exception.
     * @exception SAXException
     *              Any SAX exception, possibly wrapping another exception.
     */
    public void fatalError(SAXParseException e) throws SAXException {
        addValidationException(e);
    }

    /**
     * Adds a detected validation exception.
     * 
     * @param e
     *          SAX exception.
     */
    protected void addValidationException(SAXException e) {
        validationExceptions.add(e);
    }

    /**
     * Resets the the number of detected validation exceptions.
     */
    protected void clearValidationExceptions() {
        validationExceptions.clear();
    }

    /**
     * Throws a ValidationException if the number of validationExceptions
     * detected is greater than zero.
     * 
     * @exception ValidationException
     *              DTD validation exception.
     */
    protected void maybeThrowValidationException() throws ValidationException {
        if (validationExceptions.size() > 0) {
            throw new ValidationException("Encountered " + validationExceptions.size() + " validation exceptions.", validationExceptions);
        }
    }
}
