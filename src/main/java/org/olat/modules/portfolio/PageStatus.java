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
 * Define the status for a page / entry. And some utilitiy methods.
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PageStatus {
	
	draft("o_icon_pf_entry_draft"),
	published("o_icon_pf_entry_published"),
	inRevision("o_icon_pf_entry_revision"),
	closed("o_icon_pf_entry_closed"),
	deleted("o_icon_pf_entry_deleted");
	
	private final String cssClass;
	
	private PageStatus(String cssClass) {
		this.cssClass = cssClass;
	}
	
	public String cssClass() {
		return cssClass;
	}

	public static final boolean isValueOf(String val) {
		if(val == null) return false;
		for(PageStatus value:values()) {
			if(val.equals(value.name())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isClosed(Page page) {
		if(page.getPageStatus() == PageStatus.closed) {
			return true;
		}
		if(page.getSection() != null) {
			Section section = page.getSection();
			Date now = new Date();
			if(section.getEndDate() != null && section.getEndDate().compareTo(now) < 0) {
				return true;
			}
		}
		return false;
	}
}