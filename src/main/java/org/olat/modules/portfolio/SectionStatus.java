/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.portfolio;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum SectionStatus {
	
	notStarted("o_icon_warn", "status.not.started"),
	inProgress("o_icon_refresh", "status.in.progress"),
	submitted("o_icon_refresh", "status.submitted"),
	closed("o_icon_locked", "status.closed");
	
	private final String cssClass;
	private final String i18nKey;
	
	private SectionStatus(String cssClass, String i18nKey) {
		this.cssClass = cssClass;
		this.i18nKey = i18nKey;
	}
	
	public String cssClass() {
		return cssClass;
	}
	
	public String i18nKey() {
		return i18nKey;
	}

	public static final boolean isValueOf(String val) {
		if(val == null) return false;
		for(SectionStatus value:values()) {
			if(val.equals(value.name())) {
				return true;
			}
		}
		return false;
	}
}