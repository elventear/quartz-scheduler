package org.quartz.ui.web.action.schedule;

/**
 * Action for scheduling a job via the standard cron trigger
 * @since Tue Feb 17 22:27:33 EST 2004
 * @version Revision:
 * @author Matthew Payne
 *  TODO
 */

import java.text.ParseException;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import com.opensymphony.xwork.Action;


public class CronTriggerAction extends ScheduleBase   implements Action {

	/* (non-Javadoc)
	 * @see com.opensymphony.xwork.Action#execute()
	 */
 	
	  // the misfireinstruction
      private int misFireInstruction;
      private JobDetail detail = new JobDetail();
      private String type =  new String();

      private String cronExpression = "";
    
    public String start() throws SchedulerException {
    	System.out.println("jobname is "+ super.jobName);
    	this.detail  = ScheduleBase.getCurrentScheduler().getJobDetail(jobName, jobGroup);
    	return INPUT;
    }
    
    
	public String execute() throws ParseException  {

		if (hasErrors()) {
		   LOG.info("this thing has errors");
			return ERROR;
		}

		boolean startTimeHasValue =
			((startTime != null) && (startTime.length() > 0));
		boolean stopTimeHasValue =
			((stopTime != null) && (stopTime.length() > 0));
			
		
			CronTrigger trigger = null;
			//all weird constraints are handled by the validate method
			
				if (cronExpression.length() > 2) {
					trigger = new CronTrigger( 
							getTriggerName(),
							getTriggerGroup(),
							getJobName(),
							getJobGroup(),
							getCronExpression());
					
				}
				
				// test for parse expression erro error.cronExpression.parseError
				
				trigger.setDescription(this.getDescription());

				if (startTimeHasValue) {
					trigger.setStartTime(getStartTimeAsDate());
				}
				
				
				if (stopTimeHasValue) {
					trigger.setEndTime(getStopTimeAsDate());
				}
				
				// todo look at volativily later
				trigger.setVolatility(false);
			
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
}
