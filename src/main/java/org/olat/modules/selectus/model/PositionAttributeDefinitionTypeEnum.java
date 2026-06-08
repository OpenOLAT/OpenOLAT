/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
