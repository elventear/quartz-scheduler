package org.quartz.core.jmx;

import java.util.ArrayList;

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
import javax.management.openmbean.TabularData;

import org.quartz.JobDetail;
import org.quartz.Trigger;

public class JobDetailSupport {
	private static final String COMPOSITE_TYPE_NAME = "JobDetail";
	private static final String COMPOSITE_TYPE_DESCRIPTION = "Job Execution Details";
	private static final String[] ITEM_NAMES = new String[] { "name", "group",
			"description", "jobClass", "jobDataMap", "volatility",
			"durability", "shouldRecover",};
	private static final String[] ITEM_DESCRIPTIONS = new String[] { "name",
			"group", "description", "jobClass", "jobDataMap", "volatility",
			"durability", "shouldRecover",};
	private static final OpenType[] ITEM_TYPES = new OpenType[] { STRING,
			STRING, STRING, STRING, JobDataMapSupport.TABULAR_TYPE, BOOLEAN,
			BOOLEAN, BOOLEAN, };
	private static final CompositeType COMPOSITE_TYPE;
	private static final String TABULAR_TYPE_NAME = "JobDetail collection";
	private static final String TABULAR_TYPE_DESCRIPTION = "JobDetail collection";
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

	/**
	 * @param cData
	 * @return JobDetail
	 */
	public static JobDetail newJobDetail(CompositeData cData) {
		JobDetail jobDetail = new JobDetail();

		int i = 0;
		jobDetail.setName((String) cData.get(ITEM_NAMES[i++]));
		jobDetail.setGroup((String) cData.get(ITEM_NAMES[i++]));
		jobDetail.setDescription((String) cData.get(ITEM_NAMES[i++]));
		try {
			Class c = Class.forName((String) cData.get(ITEM_NAMES[i++]));
			jobDetail.setJobClass(c);
		} catch (ClassNotFoundException cnfe) {
			/**/
		}
		jobDetail.setJobDataMap(JobDataMapSupport
				.newJobDataMap((TabularData) cData.get(ITEM_NAMES[i++])));
		jobDetail.setVolatility((Boolean) cData.get(ITEM_NAMES[i++]));
		jobDetail.setDurability((Boolean) cData.get(ITEM_NAMES[i++]));
		jobDetail.setRequestsRecovery((Boolean) cData.get(ITEM_NAMES[i++]));

		return jobDetail;
	}

	/**
	 * @param jobDetail
	 * @return CompositeData
	 */
	public static CompositeData toCompositeData(JobDetail jobDetail) {
		try {
			return new CompositeDataSupport(COMPOSITE_TYPE, ITEM_NAMES,
					new Object[] {
							jobDetail.getName(),
							jobDetail.getGroup(),
							jobDetail.getDescription(),
							jobDetail.getJobClass().getName(),
							JobDataMapSupport.toTabularData(jobDetail
									.getJobDataMap()), jobDetail.isVolatile(),
							jobDetail.isDurable(),
							jobDetail.requestsRecovery(), });
		} catch (OpenDataException e) {
			throw new RuntimeException(e);
		}
	}

	public static TabularData toTabularData(JobDetail[] jobDetails) {
		TabularData tData = new TabularDataSupport(TABULAR_TYPE);
		if (jobDetails != null) {
			ArrayList<CompositeData> list = new ArrayList<CompositeData>();
			for (JobDetail jobDetail : jobDetails) {
				list.add(toCompositeData(jobDetail));
			}
			tData.putAll(list.toArray(new CompositeData[list.size()]));
		}
		return tData;
	}

}
