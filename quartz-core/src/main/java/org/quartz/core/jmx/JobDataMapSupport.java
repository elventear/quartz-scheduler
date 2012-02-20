/* 
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved. 
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
  package org.quartz.core.jmx;

import static javax.management.openmbean.SimpleType.STRING;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.quartz.JobDataMap;

public class JobDataMapSupport {
  private static final String typeName = "JobDataMap";
  private static final String[] keyValue = new String[] { "key", "value" };
  private static final OpenType[] openTypes = new OpenType[] { STRING, STRING };
  private static final CompositeType rowType;
  public static final TabularType TABULAR_TYPE;

  static {
    try {
      rowType = new CompositeType(typeName, typeName, keyValue, keyValue,
          openTypes);
      TABULAR_TYPE = new TabularType(typeName, typeName, rowType,
          new String[] { "key" });
    } catch (OpenDataException e) {
      throw new RuntimeException(e);
    }
  }

  public static JobDataMap newJobDataMap(TabularData tabularData) {
    JobDataMap jobDataMap = new JobDataMap();

    if(tabularData != null) {
      for (final Iterator<?> pos = tabularData.values().iterator(); pos.hasNext();) {
        CompositeData cData = (CompositeData) pos.next();
        jobDataMap.put((String) cData.get("key"), (String) cData.get("value"));
      }
    }
    
    return jobDataMap;
  }

  public static JobDataMap newJobDataMap(Map<String, Object> map) {
    JobDataMap jobDataMap = new JobDataMap();

    if(map != null) {
      for (final Iterator<String> pos = map.keySet().iterator(); pos.hasNext();) {
        String key = pos.next();
        jobDataMap.put(key, map.get(key));
      }
    }
    
    return jobDataMap;
  }
  
  /**
   * @return composite data
   */
  public static CompositeData toCompositeData(String key, String value) {
    try {
      return new CompositeDataSupport(rowType, keyValue, new Object[] {
          key, value });
    } catch (OpenDataException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param jobDataMap
   * @return TabularData
   */
  public static TabularData toTabularData(JobDataMap jobDataMap) {
    TabularData tData = new TabularDataSupport(TABULAR_TYPE);
    ArrayList<CompositeData> list = new ArrayList<CompositeData>();
    Iterator<String> iter = jobDataMap.keySet().iterator();
    while (iter.hasNext()) {
      String key = iter.next();
      list.add(toCompositeData(key, String.valueOf(jobDataMap.get(key))));
    }
    tData.putAll(list.toArray(new CompositeData[list.size()]));
    return tData;
  }

}
