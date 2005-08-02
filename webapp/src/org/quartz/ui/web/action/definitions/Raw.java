package org.quartz.ui.web.action.definitions;

import java.io.StringWriter;

import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.ui.web.base.BaseWebWork;

import com.opensymphony.xwork.ActionSupport;

public class Raw extends BaseWebWork {

    protected transient static final Log log = LogFactory.getLog(Raw.class);

	String xmlResult; 

	public String execute()  {
		StringWriter outputWriter = new StringWriter(); 
        // Betwixt just writes out the bean as a fragment
		// So if we want well-formed xml, we need to add the prolog
		outputWriter.write("<?xml version='1.0' ?><JobDefinitions>");
        
       BeanWriter writer = new BeanWriter(outputWriter);
       try {
		writer.write("definitions", super.getDefinitionManager().getDefinitions());
		
		xmlResult = outputWriter.toString() + "</JobDefinitions>";
		
	} catch (Exception e) {
		log.error("Problem generating definition output", e);
	} 

	return SUCCESS;

	}


	/**
	 * @param string
	 */
	public String getXmlResult() {
		return xmlResult;
	}

}
