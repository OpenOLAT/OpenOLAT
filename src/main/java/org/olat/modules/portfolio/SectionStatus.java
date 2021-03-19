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

import java.util.Date;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum SectionStatus {
	
	notStarted("o_icon_pf_section_draft", "o_portfolio_section_draft", "status.not.started"),
	inProgress("o_icon_pf_section_progress", "o_portfolio_section_progress", "status.in.progress"),
	closed("o_icon_pf_section_closed", "o_portfolio_section_closed", "status.closed");
	
	private final String iconClass;
	private final String statusClass;
	private final String i18nKey;
	
	private SectionStatus(String iconClass, String statusClass, String i18nKey) {
		this.iconClass = iconClass;
		this.statusClass = statusClass;
		this.i18nKey = i18nKey;
	}
	
	public String iconClass() {
		return iconClass;
	}
	
	public String statusClass() {
		return statusClass;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public static final SectionStatus secureValueOf(String val) {
		if(val == null) return null;
		for(SectionStatus value:values()) {
			if(val.equals(value.name())) {
				return value;
			}
		}
		return null;
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
	
	public static boolean isClosed(Section section) {
		if(section.getSectionStatus() == SectionStatus.closed) {
			return true;
		}

		Date now = new Date();
		if(section.getEndDate() != null && section.getEndDate().compareTo(now) < 0) {
			return true;
		}
		return false;
	}
}