package org.quartz.ui.web.action.definitions;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.betwixt.io.BeanWriter;

import org.quartz.ui.web.base.BaseWebWork;
import org.xml.sax.SAXException;

import com.opensymphony.xwork.util.LocalizedTextUtil;

public class Raw extends BaseWebWork {


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
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IntrospectionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}


	  	   if (hasFieldErrors()) {
			   LOG.info("this thing has errors");
			return ERROR;
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
