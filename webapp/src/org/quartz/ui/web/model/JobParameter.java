package org.quartz.ui.web.model;
 
/*
 * @author Matthew Payne
 * JobParameter (child of JobDefinition)
 * JobParameter describes an input parameter for a JobDefinition
 * and the rules for that parameter (required/mask)
 */
public class JobParameter {
	
	private String name;
	private String description;
	private boolean required;
	private String inputMask;


	public JobParameter () {
		
	}


    public JobParameter(String name, String desciption, String inputMask) {
        this.name = name;
        this.description = desciption;
        this.inputMask = inputMask;
    }

    /**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return
	 */
	public String getInputMask() {
		return inputMask;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param string
	 */
	public void setDescription(String string) {
		description = string;
	}

	/**
	 * @param string
	 */
	public void setInputMask(String string) {
		inputMask = string;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param b
	 */
	public void setRequired(boolean b) {
		required = b;
	}

}
