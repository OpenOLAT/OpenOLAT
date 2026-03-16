/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 12 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PositionAttributeDefinitionTypeEnum {
	
	question("custom.attribute.question", true), // single line text
	select("custom.attribute.select", true),
	number("custom.attribute.number", true),
	percentage("custom.attribute.percentage", true),
	date("custom.attribute.date", true),
	heading("custom.attribute.heading", false),
	separator("custom.attribute.separator", false),
	text("custom.attribute.text", false)
	;
	
	private final String i18nKey;
	private final boolean valueType;
	
	private PositionAttributeDefinitionTypeEnum(String i18nKey, boolean valueType) {
		this.i18nKey = i18nKey;
		this.valueType = valueType;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public boolean valueType() {
		return valueType;
	}
	

}
