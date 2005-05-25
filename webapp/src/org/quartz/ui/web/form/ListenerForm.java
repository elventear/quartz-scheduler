/*
 *  Copyright James House (c) 2001-2004
 *
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *
 *
 * This product uses and includes within its distribution, 
 * software developed by the Apache Software Foundation 
 *     (http://www.apache.org/)
 *
 */
package org.quartz.ui.web.form;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * 
 * @author Erick Romson
 * @author Rene Eigenheer
 */
public class ListenerForm  {
	public static String FORM_NAME = "listenerForm";

	public static String SCHEDULER_LISTENER = "schedulerListener";
	public static String GLOBAL_JOB_LISTENER = "globalJobListener";
	public static String GLOBAL_TRIGGER_LISTENER = "globalTriggerListener";
	public static String REGISTERED_JOB_LISTENER = "registeredJobListener";
	public static String REGISTERED_TRIGGER_LISTENER = "registeredTriggerListener";

	private String listenerType;
	private String listenerClass;
	private String listenerName;

	private boolean selected;
	private String saveAction;
	private String cancelAction;
	
	
/*	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {

		ActionErrors errors = new ActionErrors();
		this.listenerClass = this.listenerClass.trim();
//		ActionErrors errors = super.validate(mapping, request);
		if ((this.listenerClass != null) && (this.listenerClass.length() > 0)) {
			try {
				Class.forName(this.listenerClass).newInstance();
			} catch (InstantiationException e) {
				errors.add("listenerClass",new ActionError("error.class.noEmptyConstructor",this.listenerClass));
			} catch (IllegalAccessException e) {
				errors.add("listenerClass",new ActionError("error.class.noPublicConstructor",this.listenerClass));
			} catch (ClassNotFoundException e) {
				errors.add("listenerClass",new ActionError("error.class.does_not_exist",this.listenerClass));
			}
		}

		return errors;
	}
*/
	
	
			
	/**
	 * Returns the listenerClass.
	 * @return String
	 */
	public String getListenerClass() {
		return listenerClass;
	}

	/**
	 * Sets the listenerClass.
	 * @param listenerClass The listenerClass to set
	 */
	public void setListenerClass(String listenerClass) {
		this.listenerClass = listenerClass;
	}

	public static Log getLog() {
		return LogFactory.getLog(ListenerForm.class);
	}


	/**
	 * Returns the listenerName.
	 * @return String
	 */
	public String getListenerName() {
		return listenerName;
	}

	/**
	 * Sets the listenerName.
	 * @param listenerName The listenerName to set
	 */
	public void setListenerName(String listenerName) {
		this.listenerName = listenerName;
	}

	/**
	 * Returns the listenerType.
	 * @return String
	 */
	public String getListenerType() {
		return listenerType;
	}

	/**
	 * Sets the listenerType.
	 * @param listenerType The listenerType to set
	 */
	public void setListenerType(String listenerType) {
		this.listenerType = listenerType;
	}

	/**
	 * Returns the cancelAction.
	 * @return String
	 */
	public String getCancelAction() {
		return cancelAction;
	}

	/**
	 * Returns the saveAction.
	 * @return String
	 */
	public String getSaveAction() {
		return saveAction;
	}

	/**
	 * Sets the cancelAction.
	 * @param cancelAction The cancelAction to set
	 */
	public void setCancelAction(String cancelAction) {
		this.cancelAction = cancelAction;
	}

	/**
	 * Sets the saveAction.
	 * @param saveAction The saveAction to set
	 */
	public void setSaveAction(String saveAction) {
		this.saveAction = saveAction;
	}

	/**
	 * Returns the selected.
	 * @return boolean
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the selected.
	 * @param selected The selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
