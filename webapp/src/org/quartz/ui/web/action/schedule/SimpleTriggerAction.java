package org.quartz.ui.web.action.schedule;

/**
 * @since Tue Feb 17 22:27:33 EST 2004
 * @version Revision:
 * @author Matthew Payne
 *  TODO
 */


import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

import com.opensymphony.xwork.Action;

public class SimpleTriggerAction extends ScheduleBase   implements Action {

	/* (non-Javadoc)
	 * @see com.opensymphony.xwork.Action#execute()
	 */
    
	  // the misfireinstruction
      private int misFireInstruction;
      private JobDetail detail = new JobDetail();
      private String type =  new String();

	private Integer repeatInterval;
	private Integer repeatCount;
    
    
    public String start() throws SchedulerException {
    	LOG.debug("jobname is"+ super.jobName);
		this.detail  = ScheduleBase.getCurrentScheduler().getJobDetail(jobName, jobGroup);
		
    	return INPUT;
    }
    
    
	public String execute()  {

			boolean startTimeHasValue =
			((startTime != null) && (startTime.length() > 0));
		boolean stopTimeHasValue =
			((stopTime != null) && (stopTime.length() > 0));
		boolean repeatCountHasValue = (repeatCount != null);
		boolean repeatIntervalHasValue = (repeatInterval != null);
			
			

			SimpleTrigger simpleTrigger = null;

			//all weird constraints are handled by the validate method
			
				int repeatCount = 0;
				long repeatInterval = 0;
				try {
					repeatCount = getRepeatCount().intValue();
				} catch (Exception e) {}
				try {
					repeatInterval = getRepeatInterval().intValue();
				} catch (Exception e) {}

				if (startTimeHasValue && (!stopTimeHasValue)) {
					simpleTrigger =
						new SimpleTrigger(
							getTriggerName(),
							getTriggerGroup(),
							getStartTimeAsDate());
				} else if (!startTimeHasValue) {
					simpleTrigger =
						new SimpleTrigger(
							triggerName,
							triggerGroup,
							repeatCount,
							repeatInterval);
				} else {
					simpleTrigger =
						new SimpleTrigger(
							getTriggerName(),
							getTriggerGroup(),
							getStartTimeAsDate(),
							getStopTimeAsDate(),
							repeatCount,
							repeatInterval);
				}

			simpleTrigger.setJobName(jobName);
			simpleTrigger.setJobGroup(jobGroup);
			simpleTrigger.setVolatility(false);
			
			try {
				
				ScheduleBase.getCurrentScheduler().scheduleJob(simpleTrigger);
			} catch (SchedulerException e) {
				this.addActionError("Could not schedule the trigger " + simpleTrigger);
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
	 * Returns the repeatCount.
	 * @return String
	 */
	public Integer getRepeatCount() {
		return repeatCount;
	}

	/**
	 * Returns the repeatInterval.
	 * @return repeatInterval
	 */
	public Integer getRepeatInterval() {
		return repeatInterval;
	}

	/**
	 * Sets the repeatCount.
	 * @param repeatCount The repeatCount to set
	 */
	public void setRepeatCount(Integer repeatCount) {
		this.repeatCount = repeatCount;
	}

	/**
	 * Sets the repeatInterval.
	 * @param repeatInterval The repeatInterval to set
	 */
	public void setRepeatInterval(Integer repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
    

}
