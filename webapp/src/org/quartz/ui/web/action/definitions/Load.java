/*
 * Created on Jan 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.quartz.ui.web.action.definitions;
 
import org.quartz.ui.web.base.BaseWebWork;
import org.quartz.ui.web.model.JobDefinition;

/**
 * @author sergei
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Load  extends BaseWebWork {

	 
   private String definitionName;
   JobDefinition _definition = new JobDefinition();
	 
   public String execute()  {

		   if (definitionName == null  || definitionName.length() < 1) {
				// this is fine.  No definition loaded(new)
				return INPUT;
		   } else {
			
			   _definition = BaseWebWork.getDefinitionManager().getDefinition(definitionName);
			
			   return SUCCESS;	 
		   }
		
		
	   }

   
   public String list()  {
   		return SUCCESS;	
   }
   


	/**
	 * @return
	 */
	public JobDefinition getDefinition() {
		return _definition;
	}


	/**
	 * @return
	 */
	public void setDefinitionName(String definitionName) {
		this.definitionName = definitionName;
	}


}
