/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.mail;

/**
 * 
 * Initial date: 12 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LetterPlaceholder {
	
	private final String id;
	private final String type;
	private final String label;
	private final String variable;
	private final boolean mandatory;
	
	private String defaultValue;
	
	public LetterPlaceholder(String id, String variable, String type, String label, boolean mandatory) {
		this.id = id;
		this.type = type;
		this.label = label;
		this.variable = variable;
		this.mandatory = mandatory;
	}

	public String getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public String getVariable() {
		return variable;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public void appendDefaultValue(String text) {
		if(defaultValue == null) {
			defaultValue = text;
		} else {
			defaultValue += text;
		}
	}
}
