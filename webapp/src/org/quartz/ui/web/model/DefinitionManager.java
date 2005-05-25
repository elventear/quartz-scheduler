package org.quartz.ui.web.model;

import java.util.Map;
import java.util.TreeMap;

/**
 *  Created on Oct 9, 2003
  * @author Matthew Payne
 *  DefinitionManager = maintain a list of JobDefinitions
 */
public class DefinitionManager {


	private Map definitionMap = new TreeMap();
	/**
	 * 
	 */
	public DefinitionManager() {
	
	}
	
	
	public void addDefinition(String name, JobDefinition def) {
		this.definitionMap.put(name, def);
    }
	
	public void removeDefinition(String name) {
				if (definitionMap.containsKey(name)) {
					definitionMap.remove(name);
				}
		}
	
	
	
	public JobDefinition getDefinition (String jobName) {
		return (JobDefinition)definitionMap.get(jobName);
	}

   /* public void setDefinitionList(Map defs)   {
        this.definitionMap = defs;
    }*/

	public Map getDefinitions() {
		return this.definitionMap;
	}
	

}
