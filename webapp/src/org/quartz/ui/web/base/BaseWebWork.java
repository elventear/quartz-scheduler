/*
 * @since Jan 12, 2004
 * @version Revision: 
 * @author Matthew Payne
 *  
 * Basic Action for quartz actions
 */
package org.quartz.ui.web.base;
 

import org.quartz.ui.web.Util;
import org.quartz.ui.web.model.DefinitionManager;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport; 


/**
 * @author Matthew Payne
 *
 * Add job definition
 */
public class BaseWebWork extends ActionSupport  {

	public static DefinitionManager getDefinitionManager() {
            DefinitionManager manager = null;
			manager =(DefinitionManager) ActionContext.getContext().getApplication().get(Util.JOB_DEFINITIONS_PROP);
			
			return manager;
		}

	
	public String logout() {
		ActionContext.getContext().getSession().clear();
		return SUCCESS;
		
	}
	
}
