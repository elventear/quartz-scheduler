/*
 * @since Jan 12, 2004
 * @version Revision: 
 * @author Matthew Payne
 *  TODO
 */
package org.quartz.ui.web.action.definitions;
 
import org.quartz.ui.web.base.BaseWebWork;

import com.opensymphony.xwork.Action;

/**
 * @author Matthew Payne
 * Save update operations for a JobDefinition
 */
public class Delete extends BaseWebWork  implements Action {

	/* (non-Javadoc)
	 * @see com.opensymphony.xwork.Action#execute() 
	 */
	 
	 private String definitionName = "";
 	 
	public String execute()  {
			
			BaseWebWork.getDefinitionManager().removeDefinition(definitionName);
			return SUCCESS;	 
		}
		

	/**
	 * @return
	 */
	public String getDefinitionName() {
		return definitionName;
	}

	/**
	 * @param string
	 */
	public void setDefinitionName(String string) {
		definitionName = string;
	}

}
