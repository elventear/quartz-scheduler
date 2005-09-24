/*
 * @since Jan 12, 2004
 * @version Revision: 
 * @author Matthew Payne
 *  TODO
 */
package org.quartz.ui.web.action.definitions;
 
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
	 
	 
	public String execute()  {

		JobDefinition def = BaseWebWork.getDefinitionManager().getDefinition(_definition.getName());
			 if (def!=null) {
						//save for an edit /update
						def.setDescription(_definition.getDescription());
						def.setClassName(_definition.getClassName());
						this._definition = def;
							 	
			 } else {
				//save for a new
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

}
