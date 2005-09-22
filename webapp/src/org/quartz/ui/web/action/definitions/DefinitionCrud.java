/*
 * @since Jan 12, 2004
 * @version Revision: 
 * @author Matthew Payne
 *  TODO
 */
package org.quartz.ui.web.action.definitions;
 
import java.util.Iterator;
import java.util.Map;

import org.quartz.ui.web.base.BaseWebWork;
import org.quartz.ui.web.model.JobDefinition;

import com.opensymphony.xwork.Action;

/**
 * @author Matthew Payne
 * Save update operations for a JobDefinition
 */
public class DefinitionCrud extends BaseWebWork  implements Action {

	/* (non-Javadoc)
	 * @see com.opensymphony.xwork.Action#execute() 
	 */

 	JobDefinition _definition = new JobDefinition();
	Map paramMap; 
	 
	public String execute()  {

		JobDefinition def = BaseWebWork.getDefinitionManager().getDefinition(_definition.getName());
			 if (def!=null) {
				this._definition = def;
							 	
			 } else {
				//save for a new
				 if (paramMap !=null) {
					 /* Iterator itr=paramMap.keySet().iterator();
					 while (itr.hasNext()) {
						 String key = itr.next();
						 paramMap.get(key);
					 } */
					 
					 _definition.getParameters().addAll(paramMap.values());
					 
				 }
				 
				 BaseWebWork.getDefinitionManager().addDefinition(_definition.getName(), _definition);
			 }
			
			return SUCCESS;	 
		
	}

	
	/**
	 * @return JobDefinition
	 */
	public JobDefinition getDefinition() {
		return _definition;
	}

	/**
	 * @param definition
	 */
	public void setDefinition(JobDefinition definition) {
		this._definition = definition;
	}


	/**
	 * @return Returns the paramMap.
	 */
	public Map getParamMap() {
		return paramMap;
	}


	/**
	 * @param paramMap The paramMap to set.
	 */
	public void setParamMap(Map paramMap) {
		this.paramMap = paramMap;
	}

}
