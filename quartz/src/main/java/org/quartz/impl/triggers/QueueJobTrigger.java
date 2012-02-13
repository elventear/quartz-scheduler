package org.quartz.impl.triggers;
import java.util.Date;

import org.quartz.Calendar;
import org.quartz.ScheduleBuilder;
import org.quartz.TriggerKey;

public class QueueJobTrigger extends AbstractTrigger<QueueJobTrigger> {
		
	private static final long serialVersionUID = -5081309930198081728L;

	public QueueJobTrigger() {
		setKey(TriggerKey.triggerKey("QueueJobTrigger"));
	}
	
	public void setNextFireTime(Date nextFireTime) {
	}

	public void setPreviousFireTime(Date previousFireTime) {
	}

	@Override
	public void triggered(Calendar calendar) {
	}

	@Override
	public Date computeFirstFireTime(Calendar calendar) {
		return null;
	}

	@Override
	public boolean mayFireAgain() {
		return false;
	}

	@Override
	public Date getStartTime() {
		return null;
	}

	@Override
	public void setStartTime(Date startTime) {
	}

	@Override
	public void setEndTime(Date endTime) {
	}

	@Override
	public Date getEndTime() {
		return null;
	}

	@Override
	public Date getNextFireTime() {
		return null;
	}

	@Override
	public Date getPreviousFireTime() {
		return null;
	}

	@Override
	public Date getFireTimeAfter(Date afterTime) {
		return null;
	}

	@Override
	public Date getFinalFireTime() {
		return null;
	}

	@Override
	protected boolean validateMisfireInstruction(int candidateMisfireInstruction) {
		return false;
	}

	@Override
	public void updateAfterMisfire(Calendar cal) {
	}

	@Override
	public void updateWithNewCalendar(Calendar cal, long misfireThreshold) {
	}

	@Override
	public ScheduleBuilder<QueueJobTrigger> getScheduleBuilder() {
		return null;
	}
}