package org.quartz.ui.web.action.schedule;

/**
 * @since Tue Feb 17 22:27:33 EST 2004
 * @version Revision:
 * @author Matthew Payne
 *  TODO
 */

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.UICronTrigger;

import com.opensymphony.xwork.Action;

public class UICronTriggerAction extends ScheduleBase implements Action {

	
    protected transient static final Log log = LogFactory.getLog(UICronTriggerAction.class);

	
	/* (non-Javadoc)
	 * @see com.opensymphony.xwork.Action#execute()
	 */
    
	//TODO validate parse of cron expression
	
	  // the misfireinstruction
      private int misFireInstruction;
      private JobDetail jobDetail = new JobDetail();
      private String type =  new String();
      UICronTrigger trigger = new UICronTrigger();

      Integer[] daysOfMonth = new Integer[31];
      
    private String cronExpression = "";
    
    public String start() throws SchedulerException {
   
    	UICronTrigger cronTrigger = new UICronTrigger();
      	log.debug("jobname is"+ super.jobName);
		this.jobDetail  = ScheduleBase.getCurrentScheduler().getJobDetail(jobName, jobGroup);
		
    	return INPUT;
    }
    
    
	public String execute() throws ParseException  {

		
		boolean startTimeHasValue =
			((startTime != null) && (startTime.length() > 0));
		boolean stopTimeHasValue =
			((stopTime != null) && (stopTime.length() > 0));

			//all weird constraints are handled by the validate method
			
			trigger.setJobGroup(this.getJobGroup());
			trigger.setJobName(this.getJobName());
				
				// test for parse expression erro error.cronExpression.parseError
			
				trigger.setDescription(this.getDescription());
				
				// todo look at volativily later
				trigger.setVolatility(false);
				LOG.info(jobDetail.getFullName() + " scheduled with" + trigger.getExpressionSummary());
				
				
			try {
				
				ScheduleBase.getCurrentScheduler().scheduleJob(trigger);
			
			} catch (SchedulerException e) {
				this.addActionError("SchedulerException, Could not schedule the trigger " + trigger + " " + e.getLocalizedMessage());
				return ERROR;
			}   catch (UnsupportedOperationException ue) {
	
			    LOG.error("UnsupportedOperation in CronSchedule", ue);
			    this.addActionError("Could not schedule the trigger " + trigger + " " + ue.getLocalizedMessage());
				return ERROR;
			}

			return SUCCESS;
		}
    

      /**
        * returns the value of the misFireInstruction
        *
        * @return the misFireInstruction
        */
      public int getMisFireInstruction() {
            return misFireInstruction;
      }

      /**
        * sets the value of the misFireInstruction
        *
        * @param misFireInstruction
        */
      public void setMisFireInstruction(int misFireInstruction) {
            this.misFireInstruction = misFireInstruction;
      }

   
      /**
        * returns the value of the type
        *
        * @return the type
        */
      public String getType() {
            return type;
      }

      /**
        * sets the value of the type
        *
        * @param type
        */
      public void setType(String type) {
            this.type = type;
      }

	/**
	 * @return Returns the cronExpression.
	 */
	public String getCronExpression() {
		return cronExpression;
	}
	/**
	 * @param cronExpression The cronExpression to set.
	 */
	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
	/**
	 * @return Returns the jobDetail.
	 */
	public JobDetail getJobDetail() {
		return jobDetail;
	}
	/**
	 * @param jobDetail The jobDetail to set.
	 */
	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}
    /**
     * @return Returns the daysOfMonth.
     */
    public Integer[] getDaysOfMonth() {
        return daysOfMonth;
    }
    /**
     * @param daysOfMonth The daysOfMonth to set.
     */
    public void setDaysOfMonth(Integer[] daysOfMonth) {
        this.daysOfMonth = daysOfMonth;
    }
    /**
     * @return Returns the trigger.
     */
    public UICronTrigger getTrigger() {
        return trigger;
    }
    /**
     * @param trigger The trigger to set.
     */
    public void setTrigger(UICronTrigger trigger) {
        this.trigger = trigger;
    }
}
