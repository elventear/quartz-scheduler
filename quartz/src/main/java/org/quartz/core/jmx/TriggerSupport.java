package org.quartz.core.jmx;

import java.util.ArrayList;
import java.util.Iterator;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import static javax.management.openmbean.SimpleType.STRING;
import static javax.management.openmbean.SimpleType.BOOLEAN;
import static javax.management.openmbean.SimpleType.INTEGER;
import static javax.management.openmbean.SimpleType.DATE;
import javax.management.openmbean.TabularData;

import org.quartz.Trigger;

public class TriggerSupport {
	private static final String COMPOSITE_TYPE_NAME = "Trigger";
	private static final String COMPOSITE_TYPE_DESCRIPTION = "Trigger Details";
	private static final String[] ITEM_NAMES = new String[] { "name", "group",
			"jobName", "jobGroup", "description", "jobDataMap", "volatility",
			"calendarName", "fireInstanceId", "misfireInstruction", "priority",
			"startTime", "endTime", "nextFireTime", "previousFireTime",
			"finalFireTime" };
	private static final String[] ITEM_DESCRIPTIONS = new String[] { "name",
			"group", "jobName", "jobGroup", "description", "jobDataMap",
			"volatility", "calendarName", "fireInstanceId",
			"misfireInstruction", "priority", "startTime", "endTime",
			"nextFireTime", "previousFireTime", "finalFireTime" };
	private static final OpenType[] ITEM_TYPES = new OpenType[] { STRING,
			STRING, STRING, STRING, STRING, JobDataMapSupport.TABULAR_TYPE,
			BOOLEAN, STRING, STRING, INTEGER, INTEGER, DATE, DATE, DATE, DATE,
			DATE };
	private static final CompositeType COMPOSITE_TYPE;
	private static final String TABULAR_TYPE_NAME = "Trigger collection";
	private static final String TABULAR_TYPE_DESCRIPTION = "Trigger collection";
	private static final String[] INDEX_NAMES = new String[] { "name", "group" };
	private static final TabularType TABULAR_TYPE;

	static {
		try {
			COMPOSITE_TYPE = new CompositeType(COMPOSITE_TYPE_NAME,
					COMPOSITE_TYPE_DESCRIPTION, ITEM_NAMES, ITEM_DESCRIPTIONS,
					ITEM_TYPES);
			TABULAR_TYPE = new TabularType(TABULAR_TYPE_NAME,
					TABULAR_TYPE_DESCRIPTION, COMPOSITE_TYPE, INDEX_NAMES);
		} catch (OpenDataException e) {
			throw new RuntimeException(e);
		}
	}

	public static CompositeData toCompositeData(Trigger trigger) {
		try {
			return new CompositeDataSupport(COMPOSITE_TYPE, ITEM_NAMES,
					new Object[] {
							trigger.getName(),
							trigger.getGroup(),
							trigger.getJobName(),
							trigger.getJobGroup(),
							trigger.getDescription(),
							JobDataMapSupport.toTabularData(trigger
									.getJobDataMap()), trigger.isVolatile(),
							trigger.getCalendarName(),
							trigger.getFireInstanceId(),
							trigger.getMisfireInstruction(),
							trigger.getPriority(), trigger.getStartTime(),
							trigger.getEndTime(), trigger.getNextFireTime(),
							trigger.getPreviousFireTime(),
							trigger.getFinalFireTime() });
		} catch (OpenDataException e) {
			throw new RuntimeException(e);
		}
	}

	public static TabularData toTabularData(Trigger[] triggers) {
		TabularData tData = new TabularDataSupport(TABULAR_TYPE);
		if (triggers != null) {
			ArrayList<CompositeData> list = new ArrayList<CompositeData>();
			for (Trigger trigger : triggers) {
				list.add(toCompositeData(trigger));
			}
			tData.putAll(list.toArray(new CompositeData[list.size()]));
		}
		return tData;
	}
}
