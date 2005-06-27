package org.quartz.ui.web.action.schedule;

import java.util.Date;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.ui.web.base.BaseWebWork;
import com.opensymphony.xwork.ActionContext;

import com.opensymphony.webwork.ServletActionContext;

public class ScheduleBase extends BaseWebWork {

	String jobName="";
	String jobGroup="";

	  

		protected String triggerGroup = "";
		protected String description = new String();
		protected String triggerName = new String();
		protected String startTime = new String();
		protected Date startTimeAsDate = new Date();
		protected String stopTime = new String();
		protected Date stopTimeAsDate = new Date();
	
	
	public static final String CURRENT_SCHEDULER_PROP = "currentScheduler";

	public static Scheduler createSchedulerAndUpdateApplicationContext(String schedulerName) {
		Scheduler currentScheduler = null;
		
		   try {
				if 	(schedulerName != null && schedulerName.length() > 0)
					currentScheduler = new StdSchedulerFactory().getScheduler(schedulerName);
				else {
					currentScheduler = StdSchedulerFactory.getDefaultScheduler();
				}

				ActionContext.getContext().getApplication().put(CURRENT_SCHEDULER_PROP, currentScheduler);	
		   } catch (SchedulerException e) {
			   LOG.error("Problem creating scheduler",e);
		   }
		
		return currentScheduler;
	}
	
	public static Scheduler getCurrentScheduler(String schedulerName)    {
		
		Scheduler currentScheduler = (Scheduler)ActionContext.getContext().getApplication().get(CURRENT_SCHEDULER_PROP);
		   if (currentScheduler == null)   {
			   currentScheduler = createSchedulerAndUpdateApplicationContext(schedulerName);
		   }
		   return currentScheduler;
	   }


	public static Scheduler getCurrentScheduler()    {
		   return getCurrentScheduler(null);
    }

	/**
	 * @return
	 */
	public String getJobGroup() {
		return jobGroup;
	}

	/**
	 * @return
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param string
	 */
	public void setJobGroup(String string) {
		jobGroup = string;
	}

	/**
	 * @param string
	 */
	public void setJobName(String string) {
		jobName = string;
	}
	
	/**
     * returns the value of the triggerName
     *
     * @return the triggerName
     */
   public String getTriggerName() {
         return triggerName;
   }

   /**
     * sets the value of the triggerName
     *
     * @param triggerName
     */
   public void setTriggerName(String triggerName) {
         this.triggerName = triggerName;
   }

   /**
     * returns the value of the triggerGroup
     *
     * @return the triggerGroup
     */
   public String getTriggerGroup() {
         return triggerGroup;
   }

   /**
     * sets the value of the triggerGroup
     *
     * @param triggerGroup
     */
   public void setTriggerGroup(String triggerGroup) {
         this.triggerGroup = triggerGroup;
   }

   /**
    * returns the value of the description
    *
    * @return the description
    */
  public String getDescription() {
        return description;
  }

  /**
    * sets the value of the description
    *
    * @param description
    */
  public void setDescription(String description) {
        this.description = description;
  }
  
  /**
   * returns the value of the startTime
   *
   * @return the startTime
   */
 public String getStartTime() {
       return startTime;
 }

 /**
   * sets the value of the startTime
   *
   * @param startTime
   */
 public void setStartTime(String startTime) {
       this.startTime = startTime;
 }

 /**
   * returns the value of the startTimeAsDate
   *
   * @return the startTimeAsDate
   */
 public Date getStartTimeAsDate() {
       return startTimeAsDate;
 }

 /**
   * sets the value of the startTimeAsDate
   *
   * @param startTimeAsDate
   */
 public void setStartTimeAsDate(Date startTimeAsDate) {
       this.startTimeAsDate = startTimeAsDate;
 }

 /**
   * returns the value of the stopTime
   *
   * @return the stopTime
   */
 public String getStopTime() {
       return stopTime;
 }

 /**
   * sets the value of the stopTime
   *
   * @param stopTime
   */
 public void setStopTime(String stopTime) {
       this.stopTime = stopTime;
 }

 /**
   * returns the value of the stopTimeAsDate
   *
   * @return the stopTimeAsDate
   */
 public Date getStopTimeAsDate() {
       return stopTimeAsDate;
 }

 /**
   * sets the value of the stopTimeAsDate
   *
   * @param stopTimeAsDate
   */
 public void setStopTimeAsDate(Date stopTimeAsDate) {
       this.stopTimeAsDate = stopTimeAsDate;
 }


}
