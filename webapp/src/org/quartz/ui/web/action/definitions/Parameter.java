package org.quartz.ui.web.action.definitions;

import java.util.Iterator;

import org.quartz.ui.web.base.BaseWebWork;
import org.quartz.ui.web.model.JobDefinition;
import org.quartz.ui.web.model.JobParameter;

public class Parameter extends BaseWebWork {
 
		private JobDefinition _definition;
	JobParameter parameter = new JobParameter();
		String definitionName="";   
    
 
	public String execute()  { 
	  
	 
		_definition = BaseWebWork.getDefinitionManager().getDefinition(definitionName);
		System.out.println("entered action"); 
	
	  	   if (hasFieldErrors()) {
					   LOG.info("this thing has errors");
					   System.out.println("this has errors");
						return ERROR;
			}
	 
			 
			if (_definition==null) {
				System.out.println("def was null");
	
				return ERROR;
			} else {
						_definition.addParameter(parameter);
				return SUCCESS;		
			}
			
		}
		
	
	public String removeParameter()  { 

		_definition = BaseWebWork.getDefinitionManager().getDefinition(definitionName);
		Iterator itr = _definition.getParameters().iterator();
		
	    while (itr.hasNext()) {
	   		JobParameter param = (JobParameter) itr.next();
	    	if (parameter.getName().equals(param.getName())) {
	    		_definition.getParameters().remove(param);
	  		
	  			this.setFieldErrors(new java.util.HashMap()); // hack ww 2.1 will not need this since validation can be set on methods
	  			return SUCCESS;
	    	}
	    }
		
		return SUCCESS;		

	}

		
		public JobDefinition getDefinition() {
			return this._definition;
		}
		


		/**
		 * @return
		 */
		public JobParameter getParameter() {
			return parameter;
		}

		/**
		 * @return
		 */
		public String getDefinitionName() {
			return definitionName;
		}

		/**
		 * @param parameter
		 */
		public void setParameter(JobParameter parameter) {
			this.parameter = parameter;
		}

		/**
		 * @param string
		 */
		public void setDefinitionName(String string) {
			definitionName = string;
		}

}
