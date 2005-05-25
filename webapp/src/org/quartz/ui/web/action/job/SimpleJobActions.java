package org.quartz.ui.web.action.job;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;

import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.ui.web.Util;
import org.quartz.ui.web.action.schedule.ScheduleBase;
import org.quartz.ui.web.base.BaseWebWork;
import org.quartz.ui.web.form.JobDetailForm;
import org.quartz.ui.web.form.ListenerForm;
import org.quartz.ui.web.form.TriggerForm;

public class SimpleJobActions extends BaseWebWork {

    String jobName = "";

    String jobGroup = "";

    JobDetail jobDetail = new JobDetail();

    JobDetailForm form = new JobDetailForm();
    
    //Trigger[] jobTriggers=null;
    
    private ArrayList jobTriggers = new ArrayList();

    public String runNow() {
        
    	Scheduler scheduler = ScheduleBase.getCurrentScheduler();
     
        try {
            scheduler.triggerJob(jobName, jobGroup);
        } catch (SchedulerException e) {
        	LOG.error("error executing job", e);
            return ERROR;
        }
        return SUCCESS;
    }

    public String delete() {
        Scheduler scheduler = ScheduleBase.getCurrentScheduler();
        try {
            scheduler.deleteJob(jobName, jobGroup);
        } catch (SchedulerException e) {
            //error.job.notFound
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ERROR;
        }
        return SUCCESS;
    }

    public String view() throws Exception {
        if (hasFieldErrors()) {
            LOG.info("this thing has errors");
            return ERROR;
        }
        Scheduler scheduler = ScheduleBase.getCurrentScheduler();
        
        try {
        
            if (jobDetail.getName() == null) {
                jobDetail = scheduler.getJobDetail(jobName, jobGroup);
            
            } else {
                /*
                 * we sort of have a job, after a "create" we may not have out
                 * jobClass populated, so we need to re-fetch it
                 */
                jobDetail = scheduler.getJobDetail(jobDetail.getName(),
                        jobDetail.getGroup());
            }
        } catch (SchedulerException e) {
            //error.job.notFound
            throw new Exception("When reading the jobs", e);
        }
        populateForm(jobDetail, form, scheduler);
        return SUCCESS;
    }

    private void populateForm(JobDetail jobDetail, JobDetailForm form,
            Scheduler scheduler) throws ServletException {
        
        Trigger[] triggers = Util.getTriggersFromJob(scheduler, jobDetail
                .getName(), jobDetail.getGroup());
      
        for (int i = 0; i < triggers.length; i++) {
            Trigger trigger = triggers[i];
            TriggerForm tForm = new TriggerForm();
            tForm.setDescription(trigger.getDescription());
            tForm.setJobGroup(trigger.getJobGroup());
            tForm.setJobName(trigger.getJobName());
            tForm.setMisFireInstruction(trigger.getMisfireInstruction());
            tForm.setStartTime(Util.getDateAsString(trigger.getStartTime()));
            tForm.setStopTime(Util.getDateAsString(trigger.getEndTime()));
            tForm.setTriggerGroup(trigger.getGroup());
            tForm.setTriggerName(trigger.getName());
            tForm.setNextFireTime(Util.getDateAsString(trigger
                    .getNextFireTime()));
            tForm.setPreviousFireTime(Util.getDateAsString(trigger
                    .getPreviousFireTime()));
            tForm.setType(Util.getTriggerType(trigger));
            this.jobTriggers.add(tForm);
        }
      
        try {
            String[] jobListenerNames = jobDetail.getJobListenerNames();
            for (Iterator iter = scheduler.getJobListenerNames().iterator(); iter
                    .hasNext();) {
                String name = (String) iter.next();
                JobListener jobListener = scheduler.getJobListener(name);
                for (int i = 0; i < jobListenerNames.length; i++) {
                    if (jobListener.getName().equals(jobListenerNames[i])) {
                        ListenerForm listenerForm = new ListenerForm();
                        listenerForm.setListenerName(jobListener.getName());
                        listenerForm.setListenerClass(jobListener.getClass()
                                .getName());
                        form.getJobListeners().add(listenerForm);
                    }
                }
            }
        } catch (SchedulerException e) {
        }
    }

    /**
     * @return Returns the jobDetail.
     */
    public JobDetail getJobDetail() {
        return jobDetail;
    }

    /**
     * @param jobDetail
     *            The jobDetail to set.
     */
    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    /**
     * @return Returns the jobGroup.
     */
    public String getJobGroup() {
        return jobGroup;
    }

    /**
     * @param jobGroup
     *            The jobGroup to set.
     */
    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    /**
     * @return Returns the jobName.
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * @param jobName
     *            The jobName to set.
     */
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
    /**
     * @return Returns the jobTriggers.
     */
    public ArrayList getJobTriggers() {
        return jobTriggers;
    }
    /**
     * @param jobTriggers The jobTriggers to set.
     */
    public void setJobTriggers(ArrayList jobTriggers) {
        this.jobTriggers = jobTriggers;
    }
}
