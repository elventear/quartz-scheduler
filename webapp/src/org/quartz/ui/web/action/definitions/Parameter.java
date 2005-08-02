package org.quartz.ui.web.action.definitions;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.ui.web.base.BaseWebWork;
import org.quartz.ui.web.model.JobDefinition;
import org.quartz.ui.web.model.JobParameter;

public class Parameter extends BaseWebWork {
 
	  private static final Log log = LogFactory.getLog(Parameter.class);
	  
	
		private JobDefinition _definition;
		JobParameter parameter = new JobParameter();
		String definitionName="";   
    
 
	public String execute()  { 
	  		_definition = BaseWebWork.getDefinitionManager().getDefinition(definitionName);
		
			if (_definition==null) {
				log.debug("def was null");
				addActionError("specified definiton is null");
				return ERROR;
			} 
	
			_definition.addParameter(parameter);
			return SUCCESS;		
	
	}
		
	
	public String removeParameter()  { 

		_definition = BaseWebWork.getDefinitionManager().getDefinition(definitionName);
		Iterator itr = _definition.getParameters().iterator();
		
	    while (itr.hasNext()) {
	   		JobParameter param = (JobParameter) itr.next();
	    	if (parameter.getName().equals(param.getName())) {
	    		_definition.getParameters().remove(param);
	    		addActionMessage("removed parameter " + param);
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
