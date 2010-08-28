/* 
 * Copyright 2001-2009 Terracotta, Inc. 
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

package org.quartz.simpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.spi.TriggerFiredResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class implements a <code>{@link org.quartz.spi.JobStore}</code> that
 * utilizes RAM as its storage device.
 * </p>
 * 
 * <p>
 * As you should know, the ramification of this is that access is extrememly
 * fast, but the data is completely volatile - therefore this <code>JobStore</code>
 * should not be used if true persistence between program shutdowns is
 * required.
 * </p>
 * 
 * @author James House
 * @author Sharada Jambula
 * @author Eric Mueller
 */
public class RAMJobStore implements JobStore {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected HashMap<String, JobWrapper> jobsByFQN = new HashMap<String, JobWrapper>(1000);

    protected HashMap<String, TriggerWrapper> triggersByFQN = new HashMap<String, TriggerWrapper>(1000);

    protected HashMap<String, HashMap<String, JobWrapper>> jobsByGroup = new HashMap<String, HashMap<String, JobWrapper>>(25);

    protected HashMap<String, HashMap<String, TriggerWrapper>> triggersByGroup = new HashMap<String, HashMap<String, TriggerWrapper>>(25);

    protected TreeSet<TriggerWrapper> timeTriggers = new TreeSet<TriggerWrapper>(new TriggerComparator());

    protected HashMap<String, Calendar> calendarsByName = new HashMap<String, Calendar>(25);

    protected ArrayList<TriggerWrapper> triggers = new ArrayList<TriggerWrapper>(1000);

    protected final Object lock = new Object();

    protected HashSet<String> pausedTriggerGroups = new HashSet<String>();

    protected HashSet<String> pausedJobGroups = new HashSet<String>();

    protected HashSet<String> blockedJobs = new HashSet<String>();
    
    protected long misfireThreshold = 5000l;

    protected SchedulerSignaler signaler;

    private final Logger log = LoggerFactory.getLogger(getClass());

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Create a new <code>RAMJobStore</code>.
     * </p>
     */
    public RAMJobStore() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected Logger getLog() {
        return log;
    }

    /**
     * <p>
     * Called by the QuartzScheduler before the <code>JobStore</code> is
     * used, in order to give the it a chance to initialize.
     * </p>
     */
    public void initialize(ClassLoadHelper loadHelper,
            SchedulerSignaler signaler) {

        this.signaler = signaler;

        getLog().info("RAMJobStore initialized.");
    }

    public void schedulerStarted() throws SchedulerException {
        // nothing to do
    }

    public long getMisfireThreshold() {
        return misfireThreshold;
    }

    /**
     * The number of milliseconds by which a trigger must have missed its
     * next-fire-time, in order for it to be considered "misfired" and thus
     * have its misfire instruction applied.
     * 
     * @param misfireThreshold
     */
    public void setMisfireThreshold(long misfireThreshold) {
        if (misfireThreshold < 1) {
            throw new IllegalArgumentException("Misfirethreashold must be larger than 0");
        }
        this.misfireThreshold = misfireThreshold;
    }

    /**
     * <p>
     * Called by the QuartzScheduler to inform the <code>JobStore</code> that
     * it should free up all of it's resources because the scheduler is
     * shutting down.
     * </p>
     */
    public void shutdown() {
    }

    public boolean supportsPersistence() {
        return false;
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.JobDetail}</code> and <code>{@link org.quartz.Trigger}</code>.
     * </p>
     * 
     * @param newJob
     *          The <code>JobDetail</code> to be stored.
     * @param newTrigger
     *          The <code>Trigger</code> to be stored.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Job</code> with the same name/group already
     *           exists.
     */
    public void storeJobAndTrigger(JobDetail newJob,
            Trigger newTrigger) throws JobPersistenceException {
        storeJob(newJob, false);
        storeTrigger(newTrigger, false);
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.Job}</code>.
     * </p>
     * 
     * @param newJob
     *          The <code>Job</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Job</code> existing in the
     *          <code>JobStore</code> with the same name & group should be
     *          over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Job</code> with the same name/group already
     *           exists, and replaceExisting is set to false.
     */
    public void storeJob(JobDetail newJob,
            boolean replaceExisting) throws ObjectAlreadyExistsException {
        JobWrapper jw = new JobWrapper((JobDetail)newJob.clone());

        boolean repl = false;

        synchronized (lock) {
            if (jobsByFQN.get(jw.key) != null) {
                if (!replaceExisting) {
                    throw new ObjectAlreadyExistsException(newJob);
                }
                repl = true;
            }

            if (!repl) {
                // get job group
                HashMap grpMap = (HashMap) jobsByGroup.get(newJob.getGroup());
                if (grpMap == null) {
                    grpMap = new HashMap(100);
                    jobsByGroup.put(newJob.getGroup(), grpMap);
                }
                // add to jobs by group
                grpMap.put(newJob.getName(), jw);
                // add to jobs by FQN map
                jobsByFQN.put(jw.key, jw);
            } else {
                // update job detail
                JobWrapper orig = (JobWrapper) jobsByFQN.get(jw.key);
                orig.jobDetail = jw.jobDetail; // already cloned
            }
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Job}</code> with the given
     * name, and any <code>{@link org.quartz.Trigger}</code> s that reference
     * it.
     * </p>
     *
     * @param jobName
     *          The name of the <code>Job</code> to be removed.
     * @param groupName
     *          The group name of the <code>Job</code> to be removed.
     * @return <code>true</code> if a <code>Job</code> with the given name &
     *         group was found and removed from the store.
     */
    public boolean removeJob(String jobName,
            String groupName) {
        String key = JobWrapper.getJobNameKey(jobName, groupName);

        boolean found = false;

        synchronized (lock) {
            List<Trigger> triggers = getTriggersForJob(jobName,
                    groupName);
            for (Trigger trig: triggers) {
                this.removeTrigger(trig.getName(), trig.getGroup());
                found = true;
            }
            
            found = (jobsByFQN.remove(key) != null) | found;
            if (found) {

                HashMap grpMap = (HashMap) jobsByGroup.get(groupName);
                if (grpMap != null) {
                    grpMap.remove(jobName);
                    if (grpMap.size() == 0) {
                        jobsByGroup.remove(groupName);
                    }
                }
            }
        }

        return found;
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     *
     * @param newTrigger
     *          The <code>Trigger</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Trigger</code> existing in
     *          the <code>JobStore</code> with the same name & group should
     *          be over-written.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Trigger</code> with the same name/group already
     *           exists, and replaceExisting is set to false.
     *
     * @see #pauseTriggerGroup(SchedulingContext, String)
     */
    public void storeTrigger(Trigger newTrigger,
            boolean replaceExisting) throws JobPersistenceException {
        TriggerWrapper tw = new TriggerWrapper((Trigger)newTrigger.clone());

        synchronized (lock) {
            if (triggersByFQN.get(tw.key) != null) {
                if (!replaceExisting) {
                    throw new ObjectAlreadyExistsException(newTrigger);
                }
    
                removeTrigger(newTrigger.getName(), newTrigger.getGroup(), false);
            }
    
            if (retrieveJob(newTrigger.getJobName(), newTrigger.getJobGroup()) == null) {
                throw new JobPersistenceException("The job ("
                        + newTrigger.getFullJobName()
                        + ") referenced by the trigger does not exist.");
            }

            // add to triggers array
            triggers.add(tw);
            // add to triggers by group
            HashMap grpMap = (HashMap) triggersByGroup.get(newTrigger
                    .getGroup());
            if (grpMap == null) {
                grpMap = new HashMap(100);
                triggersByGroup.put(newTrigger.getGroup(), grpMap);
            }
            grpMap.put(newTrigger.getName(), tw);
            // add to triggers by FQN map
            triggersByFQN.put(tw.key, tw);

            if (pausedTriggerGroups.contains(newTrigger.getGroup())
            		|| pausedJobGroups.contains(newTrigger.getJobGroup())) {
                tw.state = TriggerWrapper.STATE_PAUSED;
                if (blockedJobs.contains(tw.jobKey)) {
                    tw.state = TriggerWrapper.STATE_PAUSED_BLOCKED;
                }
            } else if (blockedJobs.contains(tw.jobKey)) {
                tw.state = TriggerWrapper.STATE_BLOCKED;
            } else {
                timeTriggers.add(tw);
            }
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Trigger}</code> with the
     * given name.
     * </p>
     *
     * @param triggerName
     *          The name of the <code>Trigger</code> to be removed.
     * @param groupName
     *          The group name of the <code>Trigger</code> to be removed.
     * @return <code>true</code> if a <code>Trigger</code> with the given
     *         name & group was found and removed from the store.
     */
    public boolean removeTrigger(String triggerName,
            String groupName) {
        return removeTrigger(triggerName, groupName, true);
    }
    private boolean removeTrigger(String triggerName,
            String groupName, boolean removeOrphanedJob) {
        String key = TriggerWrapper.getTriggerNameKey(triggerName, groupName);

        boolean found = false;

        synchronized (lock) {
            // remove from triggers by FQN map
            found = (triggersByFQN.remove(key) == null) ? false : true;
            if (found) {
                TriggerWrapper tw = null;
                // remove from triggers by group
                HashMap grpMap = (HashMap) triggersByGroup.get(groupName);
                if (grpMap != null) {
                    grpMap.remove(triggerName);
                    if (grpMap.size() == 0) {
                        triggersByGroup.remove(groupName);
                    }
                }
                // remove from triggers array
                Iterator tgs = triggers.iterator();
                while (tgs.hasNext()) {
                    tw = (TriggerWrapper) tgs.next();
                    if (key.equals(tw.key)) {
                        tgs.remove();
                        break;
                    }
                }
                timeTriggers.remove(tw);

                if (removeOrphanedJob) {
                    JobWrapper jw = (JobWrapper) jobsByFQN.get(JobWrapper
                            .getJobNameKey(tw.trigger.getJobName(), tw.trigger
                                    .getJobGroup()));
                    List<Trigger> trigs = getTriggersForJob(tw.trigger
                            .getJobName(), tw.trigger.getJobGroup());
                    if ((trigs == null || trigs.size() == 0) && !jw.jobDetail.isDurable()) {
                        removeJob(tw.trigger.getJobName(), tw.trigger
                                .getJobGroup());
                    }
                }
            }
        }

        return found;
    }


    /**
     * @see org.quartz.spi.JobStore#replaceTrigger(org.quartz.core.SchedulingContext, java.lang.String, java.lang.String, org.quartz.Trigger)
     */
    public boolean replaceTrigger(String triggerName, String groupName, Trigger newTrigger) throws JobPersistenceException {
        String key = TriggerWrapper.getTriggerNameKey(triggerName, groupName);

        boolean found = false;

        synchronized (lock) {
            // remove from triggers by FQN map
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.remove(key);
            found = ( tw == null) ? false : true;

            if (found) {

                if (!tw.getTrigger().getJobName().equals(newTrigger.getJobName()) ||
                    !tw.getTrigger().getJobGroup().equals(newTrigger.getJobGroup())) {
                    throw new JobPersistenceException("New trigger is not related to the same job as the old trigger.");
                }

                tw = null;
                // remove from triggers by group
                HashMap grpMap = (HashMap) triggersByGroup.get(groupName);
                if (grpMap != null) {
                    grpMap.remove(triggerName);
                    if (grpMap.size() == 0) {
                        triggersByGroup.remove(groupName);
                    }
                }
                // remove from triggers array
                Iterator tgs = triggers.iterator();
                while (tgs.hasNext()) {
                    tw = (TriggerWrapper) tgs.next();
                    if (key.equals(tw.key)) {
                        tgs.remove();
                        break;
                    }
                }
                timeTriggers.remove(tw);

                try {
                    storeTrigger(newTrigger, false);
                } catch(JobPersistenceException jpe) {
                    storeTrigger(tw.getTrigger(), false); // put previous trigger back...
                    throw jpe;
                }
            }
        }

        return found;
    }

    /**
     * <p>
     * Retrieve the <code>{@link org.quartz.JobDetail}</code> for the given
     * <code>{@link org.quartz.Job}</code>.
     * </p>
     *
     * @param jobName
     *          The name of the <code>Job</code> to be retrieved.
     * @param groupName
     *          The group name of the <code>Job</code> to be retrieved.
     * @return The desired <code>Job</code>, or null if there is no match.
     */
    public JobDetail retrieveJob(String jobName,
            String groupName) {
        synchronized(lock) {
            JobWrapper jw = (JobWrapper) jobsByFQN.get(JobWrapper.getJobNameKey(
                jobName, groupName));
            return (jw != null) ? (JobDetail)jw.jobDetail.clone() : null;
        }
    }

    /**
     * <p>
     * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     *
     * @param triggerName
     *          The name of the <code>Trigger</code> to be retrieved.
     * @param groupName
     *          The group name of the <code>Trigger</code> to be retrieved.
     * @return The desired <code>Trigger</code>, or null if there is no
     *         match.
     */
    public Trigger retrieveTrigger(String triggerName,
            String groupName) {
        synchronized(lock) {
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                    .getTriggerNameKey(triggerName, groupName));
    
            return (tw != null) ? (Trigger)tw.getTrigger().clone() : null;
        }
    }

    /**
     * <p>
     * Get the current state of the identified <code>{@link Trigger}</code>.
     * </p>
     *
     * @see Trigger#STATE_NORMAL
     * @see Trigger#STATE_PAUSED
     * @see Trigger#STATE_COMPLETE
     * @see Trigger#STATE_ERROR
     * @see Trigger#STATE_BLOCKED
     * @see Trigger#STATE_NONE
     */
    public int getTriggerState(String triggerName,
            String groupName) throws JobPersistenceException {
        synchronized(lock) {
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                .getTriggerNameKey(triggerName, groupName));
            
            if (tw == null) {
                return Trigger.STATE_NONE;
            }
    
            if (tw.state == TriggerWrapper.STATE_COMPLETE) {
                return Trigger.STATE_COMPLETE;
            }
    
            if (tw.state == TriggerWrapper.STATE_PAUSED) {
                return Trigger.STATE_PAUSED;
            }
    
            if (tw.state == TriggerWrapper.STATE_PAUSED_BLOCKED) {
                return Trigger.STATE_PAUSED;
            }
    
            if (tw.state == TriggerWrapper.STATE_BLOCKED) {
                return Trigger.STATE_BLOCKED;
            }
    
            if (tw.state == TriggerWrapper.STATE_ERROR) {
                return Trigger.STATE_ERROR;
            }
    
            return Trigger.STATE_NORMAL;
        }
    }

    /**
     * <p>
     * Store the given <code>{@link org.quartz.Calendar}</code>.
     * </p>
     *
     * @param calendar
     *          The <code>Calendar</code> to be stored.
     * @param replaceExisting
     *          If <code>true</code>, any <code>Calendar</code> existing
     *          in the <code>JobStore</code> with the same name & group
     *          should be over-written.
     * @param updateTriggers
     *          If <code>true</code>, any <code>Trigger</code>s existing
     *          in the <code>JobStore</code> that reference an existing
     *          Calendar with the same name with have their next fire time
     *          re-computed with the new <code>Calendar</code>.
     * @throws ObjectAlreadyExistsException
     *           if a <code>Calendar</code> with the same name already
     *           exists, and replaceExisting is set to false.
     */
    public void storeCalendar(String name,
            Calendar calendar, boolean replaceExisting, boolean updateTriggers)
        throws ObjectAlreadyExistsException {

        calendar = (Calendar) calendar.clone();
        
        synchronized (lock) {
    
            Object obj = calendarsByName.get(name);
    
            if (obj != null && replaceExisting == false) {
                throw new ObjectAlreadyExistsException(
                    "Calendar with name '" + name + "' already exists.");
            } else if (obj != null) {
                calendarsByName.remove(name);
            }
    
            calendarsByName.put(name, calendar);
    
            if(obj != null && updateTriggers) {
                Iterator trigs = getTriggerWrappersForCalendar(name).iterator();
                while (trigs.hasNext()) {
                    TriggerWrapper tw = (TriggerWrapper) trigs.next();
                    Trigger trig = tw.getTrigger();
                    boolean removed = timeTriggers.remove(tw);

                    trig.updateWithNewCalendar(calendar, getMisfireThreshold());

                    if(removed) {
                        timeTriggers.add(tw);
                    }
                }
            }
        }
    }

    /**
     * <p>
     * Remove (delete) the <code>{@link org.quartz.Calendar}</code> with the
     * given name.
     * </p>
     *
     * <p>
     * If removal of the <code>Calendar</code> would result in
     * <code>Trigger</code>s pointing to non-existent calendars, then a
     * <code>JobPersistenceException</code> will be thrown.</p>
     *       *
     * @param calName The name of the <code>Calendar</code> to be removed.
     * @return <code>true</code> if a <code>Calendar</code> with the given name
     * was found and removed from the store.
     */
    public boolean removeCalendar(String calName)
        throws JobPersistenceException {
        int numRefs = 0;

        synchronized (lock) {
            Iterator itr = triggers.iterator();
            while (itr.hasNext()) {
                Trigger trigg = ((TriggerWrapper) itr.next()).trigger;
                if (trigg.getCalendarName() != null
                        && trigg.getCalendarName().equals(calName)) {
                    numRefs++;
                }
            }
        }

        if (numRefs > 0) {
            throw new JobPersistenceException(
                    "Calender cannot be removed if it referenced by a Trigger!");
        }

        return (calendarsByName.remove(calName) != null);
    }

    /**
     * <p>
     * Retrieve the given <code>{@link org.quartz.Trigger}</code>.
     * </p>
     *
     * @param calName
     *          The name of the <code>Calendar</code> to be retrieved.
     * @return The desired <code>Calendar</code>, or null if there is no
     *         match.
     */
    public Calendar retrieveCalendar(String calName) {
        synchronized (lock) {
            Calendar cal = (Calendar) calendarsByName.get(calName);
            if(cal != null)
                return (Calendar) cal.clone();
            return null;
        }
    }

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.JobDetail}</code> s that are
     * stored in the <code>JobsStore</code>.
     * </p>
     */
    public int getNumberOfJobs() {
        synchronized (lock) {
            return jobsByFQN.size();
        }
    }

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.Trigger}</code> s that are
     * stored in the <code>JobsStore</code>.
     * </p>
     */
    public int getNumberOfTriggers() {
        synchronized (lock) {
            return triggers.size();
        }
    }

    /**
     * <p>
     * Get the number of <code>{@link org.quartz.Calendar}</code> s that are
     * stored in the <code>JobsStore</code>.
     * </p>
     */
    public int getNumberOfCalendars() {
        synchronized (lock) {
            return calendarsByName.size();
        }
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Job}</code> s that
     * have the given group name.
     * </p>
     */
    public List<String> getJobNames(String groupName) {
        List<String> outList = null;
        synchronized (lock) {
            HashMap<String, JobWrapper> grpMap = jobsByGroup.get(groupName);
            if (grpMap != null) {
                outList = new LinkedList<String>();

                for (JobWrapper jw : grpMap.values()) {

                    if (jw != null) {
                        outList.add(jw.jobDetail.getName());
                    }
                }
            } else {
                outList = java.util.Collections.emptyList();
            }
        }

        return outList;
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Calendar}</code> s
     * in the <code>JobStore</code>.
     * </p>
     *
     * <p>
     * If there are no Calendars in the given group name, the result should be
     * a zero-length array (not <code>null</code>).
     * </p>
     */
    public List<String> getCalendarNames() {
        synchronized(lock) {
            return new LinkedList<String>(calendarsByName.keySet());
        }
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Trigger}</code> s
     * that have the given group name.
     * </p>
     */
    public List<String> getTriggerNames(String groupName) {
        List<String> outList = null;
        synchronized (lock) {
            HashMap<String, TriggerWrapper> grpMap = triggersByGroup.get(groupName);
            if (grpMap != null) {
                outList = new LinkedList<String>();

                for(TriggerWrapper tw: grpMap.values()) {

                    if (tw != null) {
                        outList.add(tw.trigger.getName());
                    }
                }
            } else {
                outList = java.util.Collections.emptyList();
            }
        }

        return outList;
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Job}</code>
     * groups.
     * </p>
     */
    public List<String> getJobGroupNames() {
        List<String> outList = null;

        synchronized (lock) {
            outList = new LinkedList<String>(jobsByGroup.keySet());
        }

        return outList;
    }

    /**
     * <p>
     * Get the names of all of the <code>{@link org.quartz.Trigger}</code>
     * groups.
     * </p>
     */
    public List<String> getTriggerGroupNames() {
        LinkedList<String> outList = null;

        synchronized (lock) {
            outList = new LinkedList<String>(triggersByGroup.keySet());
        }

        return outList;
    }

    /**
     * <p>
     * Get all of the Triggers that are associated to the given Job.
     * </p>
     *
     * <p>
     * If there are no matches, a zero-length array should be returned.
     * </p>
     */
    public List<Trigger> getTriggersForJob(String jobName,
            String groupName) {
        ArrayList<Trigger> trigList = new ArrayList<Trigger>();

        String jobKey = JobWrapper.getJobNameKey(jobName, groupName);
        synchronized (lock) {
            for (int i = 0; i < triggers.size(); i++) {
                TriggerWrapper tw = (TriggerWrapper) triggers.get(i);
                if (tw.jobKey.equals(jobKey)) {
                    trigList.add((Trigger) tw.trigger.clone());
                }
            }
        }

        return trigList;
    }

    protected ArrayList getTriggerWrappersForJob(String jobName, String groupName) {
        ArrayList trigList = new ArrayList();

        String jobKey = JobWrapper.getJobNameKey(jobName, groupName);
        synchronized (lock) {
            for (int i = 0; i < triggers.size(); i++) {
                TriggerWrapper tw = (TriggerWrapper) triggers.get(i);
                if (tw.jobKey.equals(jobKey)) {
                    trigList.add(tw);
                }
            }
        }

        return trigList;
    }

    protected ArrayList getTriggerWrappersForCalendar(String calName) {
        ArrayList trigList = new ArrayList();

        synchronized (lock) {
            for (int i = 0; i < triggers.size(); i++) {
                TriggerWrapper tw = (TriggerWrapper) triggers.get(i);
                String tcalName = tw.getTrigger().getCalendarName();
                if (tcalName != null && tcalName.equals(calName)) {
                    trigList.add(tw);
                }
            }
        }

        return trigList;
    }

    /**
     * <p>
     * Pause the <code>{@link Trigger}</code> with the given name.
     * </p>
     *
     */
    public void pauseTrigger(String triggerName,
            String groupName) {

        synchronized (lock) {
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                    .getTriggerNameKey(triggerName, groupName));
    
            // does the trigger exist?
            if (tw == null || tw.trigger == null) {
                return;
            }
    
            // if the trigger is "complete" pausing it does not make sense...
            if (tw.state == TriggerWrapper.STATE_COMPLETE) {
                return;
            }

            if(tw.state == TriggerWrapper.STATE_BLOCKED) {
                tw.state = TriggerWrapper.STATE_PAUSED_BLOCKED;
            } else {
                tw.state = TriggerWrapper.STATE_PAUSED;
            }

            timeTriggers.remove(tw);
        }
    }

    /**
     * <p>
     * Pause all of the <code>{@link Trigger}s</code> in the given group.
     * </p>
     *
     * <p>
     * The JobStore should "remember" that the group is paused, and impose the
     * pause on any new triggers that are added to the group while the group is
     * paused.
     * </p>
     *
     */
    public void pauseTriggerGroup(String groupName) {

        synchronized (lock) {
            if (pausedTriggerGroups.contains(groupName)) {
                return;
            }

            pausedTriggerGroups.add(groupName);
            List<String> names = getTriggerNames(groupName);

            for (String name: names) {
                pauseTrigger(name, groupName);
            }
        }
    }

    /**
     * <p>
     * Pause the <code>{@link org.quartz.JobDetail}</code> with the given
     * name - by pausing all of its current <code>Trigger</code>s.
     * </p>
     *
     */
    public void pauseJob(String jobName,
            String groupName) {
        synchronized (lock) {
            List<Trigger> triggers = getTriggersForJob(jobName, groupName);
            for (Trigger trigger: triggers) {
                pauseTrigger(trigger.getName(), trigger.getGroup());
            }
        }
    }

    /**
     * <p>
     * Pause all of the <code>{@link org.quartz.JobDetail}s</code> in the
     * given group - by pausing all of their <code>Trigger</code>s.
     * </p>
     *
     *
     * <p>
     * The JobStore should "remember" that the group is paused, and impose the
     * pause on any new jobs that are added to the group while the group is
     * paused.
     * </p>
     */
    public void pauseJobGroup(String groupName) {
        synchronized (lock) {
            if (!pausedJobGroups.contains(groupName)) {
        	    pausedJobGroups.add(groupName);
            }
            
            List<String> jobNames = getJobNames(groupName);

            for (String name: jobNames) {
                List<Trigger> triggers = getTriggersForJob(name,
                        groupName);
                for (Trigger trigger: triggers) {
                    pauseTrigger(trigger.getName(), trigger.getGroup());
                }
            }
        }
    }

    /**
     * <p>
     * Resume (un-pause) the <code>{@link Trigger}</code> with the given
     * name.
     * </p>
     *
     * <p>
     * If the <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     */
    public void resumeTrigger(String triggerName,
            String groupName) {

        synchronized (lock) {
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                    .getTriggerNameKey(triggerName, groupName));
    
            // does the trigger exist?
            if (tw == null || tw.trigger == null) {
                return;
            }
    
            Trigger trig = tw.getTrigger();
    
            // if the trigger is not paused resuming it does not make sense...
            if (tw.state != TriggerWrapper.STATE_PAUSED &&
                    tw.state != TriggerWrapper.STATE_PAUSED_BLOCKED) {
                return;
            }

            if(blockedJobs.contains( JobWrapper.getJobNameKey(trig.getJobName(), trig.getJobGroup()) )) {
                tw.state = TriggerWrapper.STATE_BLOCKED;
            } else {
                tw.state = TriggerWrapper.STATE_WAITING;
            }

            applyMisfire(tw);

            if (tw.state == TriggerWrapper.STATE_WAITING) {
                timeTriggers.add(tw);
            }
        }
    }

    /**
     * <p>
     * Resume (un-pause) all of the <code>{@link Trigger}s</code> in the
     * given group.
     * </p>
     *
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     */
    public void resumeTriggerGroup(String groupName) {

        synchronized (lock) {
            List<String> names = getTriggerNames(groupName);

            for (String name: names) {
            	String key = TriggerWrapper.getTriggerNameKey(name, groupName);
            	if(triggersByFQN.get(key) != null) {
            		String jobGroup = ((TriggerWrapper) triggersByFQN.get(key)).getTrigger().getJobGroup();
            		if(pausedJobGroups.contains(jobGroup)) {
            			continue;
            		}
            	}
                resumeTrigger(name, groupName);
            }
            pausedTriggerGroups.remove(groupName);
        }
    }

    /**
     * <p>
     * Resume (un-pause) the <code>{@link org.quartz.JobDetail}</code> with
     * the given name.
     * </p>
     *
     * <p>
     * If any of the <code>Job</code>'s<code>Trigger</code> s missed one
     * or more fire-times, then the <code>Trigger</code>'s misfire
     * instruction will be applied.
     * </p>
     *
     */
    public void resumeJob(String jobName,
            String groupName) {

        synchronized (lock) {
            List<Trigger> triggers = getTriggersForJob(jobName, groupName);
            for (Trigger trigger: triggers) {
                resumeTrigger(trigger.getName(), trigger.getGroup());
            }
        }
    }

    /**
     * <p>
     * Resume (un-pause) all of the <code>{@link org.quartz.JobDetail}s</code>
     * in the given group.
     * </p>
     *
     * <p>
     * If any of the <code>Job</code> s had <code>Trigger</code> s that
     * missed one or more fire-times, then the <code>Trigger</code>'s
     * misfire instruction will be applied.
     * </p>
     *
     */
    public void resumeJobGroup(String groupName) {
        synchronized (lock) {
            List<String> jobNames = getJobNames(groupName);

            if(pausedJobGroups.contains(groupName)) {
            	pausedJobGroups.remove(groupName);
            }
            
            for (String name: jobNames) {
                List<Trigger> triggers = getTriggersForJob(name, groupName);
                for (Trigger trigger: triggers) {
                    resumeTrigger(trigger.getName(), trigger.getGroup());
                }
            }
        }
    }

    /**
     * <p>
     * Pause all triggers - equivalent of calling <code>pauseTriggerGroup(group)</code>
     * on every group.
     * </p>
     *
     * <p>
     * When <code>resumeAll()</code> is called (to un-pause), trigger misfire
     * instructions WILL be applied.
     * </p>
     *
     * @see #resumeAll(SchedulingContext)
     * @see #pauseTriggerGroup(SchedulingContext, String)
     */
    public void pauseAll() {

        synchronized (lock) {
            List<String> names = getTriggerGroupNames();

            for (String name: names) {
                pauseTriggerGroup(name);
            }
        }
    }

    /**
     * <p>
     * Resume (un-pause) all triggers - equivalent of calling <code>resumeTriggerGroup(group)</code>
     * on every group.
     * </p>
     *
     * <p>
     * If any <code>Trigger</code> missed one or more fire-times, then the
     * <code>Trigger</code>'s misfire instruction will be applied.
     * </p>
     *
     * @see #pauseAll(SchedulingContext)
     */
    public void resumeAll() {

        synchronized (lock) {
        	pausedJobGroups.clear();
            List<String> names = getTriggerGroupNames();

            for (String name: names) {
                resumeTriggerGroup(name);
            }
        }
    }

    protected boolean applyMisfire(TriggerWrapper tw) {

        long misfireTime = System.currentTimeMillis();
        if (getMisfireThreshold() > 0) {
            misfireTime -= getMisfireThreshold();
        }

        Date tnft = tw.trigger.getNextFireTime();
        if (tnft == null || tnft.getTime() > misfireTime) { 
            return false; 
        }

        Calendar cal = null;
        if (tw.trigger.getCalendarName() != null) {
            cal = retrieveCalendar(tw.trigger.getCalendarName());
        }

        signaler.notifyTriggerListenersMisfired((Trigger)tw.trigger.clone());

        tw.trigger.updateAfterMisfire(cal);

        if (tw.trigger.getNextFireTime() == null) {
            tw.state = TriggerWrapper.STATE_COMPLETE;
            signaler.notifySchedulerListenersFinalized(tw.trigger);
            synchronized (lock) {
                timeTriggers.remove(tw);
            }
        } else if (tnft.equals(tw.trigger.getNextFireTime())) {
            return false;
        }

        return true;
    }

    private static AtomicLong ftrCtr = new AtomicLong(System.currentTimeMillis());

    protected String getFiredTriggerRecordId() {
        return String.valueOf(ftrCtr.incrementAndGet());
    }

    /**
     * <p>
     * Get a handle to the next trigger to be fired, and mark it as 'reserved'
     * by the calling scheduler.
     * </p>
     *
     * @see #releaseAcquiredTrigger(SchedulingContext, Trigger)
     */
    public List<Trigger> acquireNextTriggers(long noLaterThan, int maxCount, long timeWindow) {
        synchronized (lock) {
            List<Trigger> result = new ArrayList<Trigger>();

            while (true) {
                TriggerWrapper tw;

                try {
                    tw = (TriggerWrapper) timeTriggers.first();
                    if (tw == null) return result;
                    timeTriggers.remove(tw);
                } catch (java.util.NoSuchElementException nsee) {
                    return result;
                }

                if (tw.trigger.getNextFireTime() == null) {
                    continue;
                }

                if (applyMisfire(tw)) {
                    if (tw.trigger.getNextFireTime() != null) {
                        timeTriggers.add(tw);
                    }
                    continue;
                }

                if (tw.getTrigger().getNextFireTime().getTime() > noLaterThan + timeWindow) {
                    timeTriggers.add(tw);
                    return result;
                }

                tw.state = TriggerWrapper.STATE_ACQUIRED;

                tw.trigger.setFireInstanceId(getFiredTriggerRecordId());
                Trigger trig = (Trigger) tw.trigger.clone();
                result.add(trig);

                if (result.size() == maxCount)
                  return result;
            }
        }
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler no longer plans to
     * fire the given <code>Trigger</code>, that it had previously acquired
     * (reserved).
     * </p>
     */
    public void releaseAcquiredTrigger(Trigger trigger) {
        synchronized (lock) {
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                .getTriggerNameKey(trigger));
            if (tw != null && tw.state == TriggerWrapper.STATE_ACQUIRED) {
                tw.state = TriggerWrapper.STATE_WAITING;
                timeTriggers.add(tw);
            }
        }
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler is now firing the
     * given <code>Trigger</code> (executing its associated <code>Job</code>),
     * that it had previously acquired (reserved).
     * </p>
     */
    public List<TriggerFiredResult> triggersFired(List<Trigger> triggers) {

        synchronized (lock) {
            List<TriggerFiredResult> results = new ArrayList<TriggerFiredResult>();

            for (Trigger trigger : triggers) {
                TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                        .getTriggerNameKey(trigger));
                // was the trigger deleted since being acquired?
                if (tw == null || tw.trigger == null) {
                    return null;
                }
                // was the trigger completed, paused, blocked, etc. since being acquired?
                if (tw.state != TriggerWrapper.STATE_ACQUIRED) {
                    return null;
                }

                Calendar cal = null;
                if (tw.trigger.getCalendarName() != null) {
                    cal = retrieveCalendar(tw.trigger.getCalendarName());
                    if(cal == null)
                        return null;
                }
                Date prevFireTime = trigger.getPreviousFireTime();
                // in case trigger was replaced between acquiring and firering
                timeTriggers.remove(tw);
                // call triggered on our copy, and the scheduler's copy
                tw.trigger.triggered(cal);
                trigger.triggered(cal);
                //tw.state = TriggerWrapper.STATE_EXECUTING;
                tw.state = TriggerWrapper.STATE_WAITING;

                TriggerFiredBundle bndle = new TriggerFiredBundle(retrieveJob(
                        trigger.getJobName(), trigger.getJobGroup()), trigger, cal,
                        false, new Date(), trigger.getPreviousFireTime(), prevFireTime,
                        trigger.getNextFireTime());

                JobDetail job = bndle.getJobDetail();

                if (job.isStateful()) {
                    ArrayList trigs = getTriggerWrappersForJob(job.getName(), job
                            .getGroup());
                    Iterator itr = trigs.iterator();
                    while (itr.hasNext()) {
                        TriggerWrapper ttw = (TriggerWrapper) itr.next();
                        if(ttw.state == TriggerWrapper.STATE_WAITING) {
                            ttw.state = TriggerWrapper.STATE_BLOCKED;
                        }
                        if(ttw.state == TriggerWrapper.STATE_PAUSED) {
                            ttw.state = TriggerWrapper.STATE_PAUSED_BLOCKED;
                        }
                        timeTriggers.remove(ttw);
                    }
                    blockedJobs.add(JobWrapper.getJobNameKey(job));
                } else if (tw.trigger.getNextFireTime() != null) {
                    synchronized (lock) {
                        timeTriggers.add(tw);
                    }
                }

                results.add(new TriggerFiredResult(bndle));
            }
            return results;
        }
    }

    /**
     * <p>
     * Inform the <code>JobStore</code> that the scheduler has completed the
     * firing of the given <code>Trigger</code> (and the execution its
     * associated <code>Job</code>), and that the <code>{@link org.quartz.JobDataMap}</code>
     * in the given <code>JobDetail</code> should be updated if the <code>Job</code>
     * is stateful.
     * </p>
     */
    public void triggeredJobComplete(Trigger trigger,
            JobDetail jobDetail, int triggerInstCode) {

        synchronized (lock) {

            String jobKey = JobWrapper.getJobNameKey(jobDetail.getName(), jobDetail
                    .getGroup());
            JobWrapper jw = (JobWrapper) jobsByFQN.get(jobKey);
            TriggerWrapper tw = (TriggerWrapper) triggersByFQN.get(TriggerWrapper
                    .getTriggerNameKey(trigger));

            // It's possible that the job is null if:
            //   1- it was deleted during execution
            //   2- RAMJobStore is being used only for volatile jobs / triggers
            //      from the JDBC job store
            if (jw != null) {
                JobDetail jd = jw.jobDetail;

                if (jd.isStateful()) {
                    JobDataMap newData = jobDetail.getJobDataMap();
                    if (newData != null) {
                        newData = (JobDataMap)newData.clone();
                        newData.clearDirtyFlag();
                    }
                    jd.setJobDataMap(newData);
                    blockedJobs.remove(JobWrapper.getJobNameKey(jd));
                    ArrayList trigs = getTriggerWrappersForJob(jd.getName(), jd
                            .getGroup());
                    Iterator itr = trigs.iterator();
                    while (itr.hasNext()) {
                        TriggerWrapper ttw = (TriggerWrapper) itr.next();
                        if (ttw.state == TriggerWrapper.STATE_BLOCKED) {
                            ttw.state = TriggerWrapper.STATE_WAITING;
                            timeTriggers.add(ttw);
                        }
                        if (ttw.state == TriggerWrapper.STATE_PAUSED_BLOCKED) {
                            ttw.state = TriggerWrapper.STATE_PAUSED;
                        }
                    }
                    signaler.signalSchedulingChange(0L);
                }
            } else { // even if it was deleted, there may be cleanup to do
                blockedJobs.remove(JobWrapper.getJobNameKey(jobDetail));
            }
    
            // check for trigger deleted during execution...
            if (tw != null) {
                if (triggerInstCode == Trigger.INSTRUCTION_DELETE_TRIGGER) {
                    
                    if(trigger.getNextFireTime() == null) {
                        // double check for possible reschedule within job 
                        // execution, which would cancel the need to delete...
                        if(tw.getTrigger().getNextFireTime() == null) {
                            removeTrigger(trigger.getName(), trigger.getGroup());
                        }
                    } else {
                        removeTrigger(trigger.getName(), trigger.getGroup());
                        signaler.signalSchedulingChange(0L);
                    }
                } else if (triggerInstCode == Trigger.INSTRUCTION_SET_TRIGGER_COMPLETE) {
                    tw.state = TriggerWrapper.STATE_COMPLETE;
                    timeTriggers.remove(tw);
                    signaler.signalSchedulingChange(0L);
                } else if(triggerInstCode == Trigger.INSTRUCTION_SET_TRIGGER_ERROR) {
                    getLog().info("Trigger " + trigger.getFullName() + " set to ERROR state.");
                    tw.state = TriggerWrapper.STATE_ERROR;
                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_ERROR) {
                    getLog().info("All triggers of Job " 
                            + trigger.getFullJobName() + " set to ERROR state.");
                    setAllTriggersOfJobToState(
                            trigger.getJobName(), 
                            trigger.getJobGroup(),
                            TriggerWrapper.STATE_ERROR);
                    signaler.signalSchedulingChange(0L);
                } else if (triggerInstCode == Trigger.INSTRUCTION_SET_ALL_JOB_TRIGGERS_COMPLETE) {
                    setAllTriggersOfJobToState(
                            trigger.getJobName(), 
                            trigger.getJobGroup(),
                            TriggerWrapper.STATE_COMPLETE);
                    signaler.signalSchedulingChange(0L);
                }
            }
        }
    }

    protected void setAllTriggersOfJobToState(String jobName, String jobGroup, int state) {
        ArrayList tws = getTriggerWrappersForJob(jobName, jobGroup);
        Iterator itr = tws.iterator();
        while (itr.hasNext()) {
            TriggerWrapper tw = (TriggerWrapper) itr.next();
            tw.state = state;
            if(state != TriggerWrapper.STATE_WAITING) {
                timeTriggers.remove(tw);
            }
        }
    }
    
    protected String peekTriggers() {

        StringBuffer str = new StringBuffer();
        TriggerWrapper tw = null;
        synchronized (lock) {
            for (Iterator valueIter = triggersByFQN.values().iterator(); valueIter.hasNext();) {
                tw = (TriggerWrapper)valueIter.next();
                str.append(tw.trigger.getName());
                str.append("/");
            }
        }
        str.append(" | ");

        synchronized (lock) {
            Iterator itr = timeTriggers.iterator();
            while (itr.hasNext()) {
                tw = (TriggerWrapper) itr.next();
                str.append(tw.trigger.getName());
                str.append("->");
            }
        }

        return str.toString();
    }

    /** 
     * @see org.quartz.spi.JobStore#getPausedTriggerGroups(org.quartz.core.SchedulingContext)
     */
    public Set getPausedTriggerGroups() throws JobPersistenceException {
        HashSet set = new HashSet();
        
        set.addAll(pausedTriggerGroups);
        
        return set;
    }

    public void setInstanceId(String schedInstId) {
        //
    }

    public void setInstanceName(String schedName) {
        //
    }

    public long getEstimatedTimeToReleaseAndAcquireTrigger() {
        return 5;
    }

    public boolean isClustered() {
        return false;
    }

}

/*******************************************************************************
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Helper Classes. * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

class TriggerComparator implements Comparator {

    public int compare(Object obj1, Object obj2) {
        TriggerWrapper trig1 = (TriggerWrapper) obj1;
        TriggerWrapper trig2 = (TriggerWrapper) obj2;

        int comp = trig1.trigger.compareTo(trig2.trigger);
        if (comp != 0) {
            return comp;
        }

        comp = trig2.trigger.getPriority() - trig1.trigger.getPriority();
        if (comp != 0) {
            return comp;
        }
        
        return trig1.trigger.getFullName().compareTo(trig2.trigger.getFullName());
    }

    public boolean equals(Object obj) {
        return (obj instanceof TriggerComparator);
    }
}

class JobWrapper {

    public String key;

    public JobDetail jobDetail;

    JobWrapper(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
        key = getJobNameKey(jobDetail);
    }

    JobWrapper(JobDetail jobDetail, String key) {
        this.jobDetail = jobDetail;
        this.key = key;
    }

    static String getJobNameKey(JobDetail jobDetail) {
        return jobDetail.getGroup() + "_$x$x$_" + jobDetail.getName();
    }

    static String getJobNameKey(String jobName, String groupName) {
        return groupName + "_$x$x$_" + jobName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof JobWrapper) {
            JobWrapper jw = (JobWrapper) obj;
            if (jw.key.equals(this.key)) {
                return true;
            }
        }

        return false;
    }
    
    public int hashCode() {
        return key.hashCode(); 
    }
    

}

class TriggerWrapper {

    public String key;

    public String jobKey;

    public Trigger trigger;

    public int state = STATE_WAITING;

    public static final int STATE_WAITING = 0;

    public static final int STATE_ACQUIRED = 1;

    public static final int STATE_EXECUTING = 2;

    public static final int STATE_COMPLETE = 3;

    public static final int STATE_PAUSED = 4;

    public static final int STATE_BLOCKED = 5;

    public static final int STATE_PAUSED_BLOCKED = 6;

    public static final int STATE_ERROR = 7;
    
    TriggerWrapper(Trigger trigger) {
        this.trigger = trigger;
        key = getTriggerNameKey(trigger);
        this.jobKey = JobWrapper.getJobNameKey(trigger.getJobName(), trigger
                .getJobGroup());
    }

    static String getTriggerNameKey(Trigger trigger) {
        return trigger.getGroup() + "_$x$x$_" + trigger.getName();
    }

    static String getTriggerNameKey(String triggerName, String groupName) {
        return groupName + "_$x$x$_" + triggerName;
    }

    public boolean equals(Object obj) {
        if (obj instanceof TriggerWrapper) {
            TriggerWrapper tw = (TriggerWrapper) obj;
            if (tw.key.equals(this.key)) {
                return true;
            }
        }

        return false;
    }

    public int hashCode() {
        return key.hashCode(); 
    }

    
    public Trigger getTrigger() {
        return this.trigger;
    }
}
