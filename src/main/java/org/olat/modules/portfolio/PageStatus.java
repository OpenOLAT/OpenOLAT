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

import org.olat.core.util.StringHelper;

/**
 * Define the status for a page / entry. And some utilitiy methods.
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum PageStatus {
	
	draft("o_icon_pf_entry_draft", "o_portfolio_entry_draft", "status.draft"),
	published("o_icon_pf_entry_published", "o_portfolio_entry_published", "status.published"),
	inRevision("o_icon_pf_entry_revision", "o_portfolio_entry_revision", "status.in.revision"),
	closed("o_icon_pf_entry_closed", "o_portfolio_entry_closed", "status.closed"),
	deleted("o_icon_pf_entry_deleted", "o_portfolio_entry_deleted", "status.deleted");
	
	private final String iconClass;
	private final String statusCssClass;
	private final String i18nKey;

	private PageStatus(String iconClass, String statusCssClass, String i18nKey) {
		this.iconClass = iconClass;
		this.i18nKey = i18nKey;
		this.statusCssClass = statusCssClass;
	}
	
	public String iconClass() {
		return iconClass;
	}
	
	public String statusClass() {
		return statusCssClass;
	}

	public String i18nKey() {
		return i18nKey;
	}
	
	public static final PageStatus valueOfOrNull(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			return PageStatus.valueOf(val);
		}
		return null;
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
	
	public static boolean isClosed(PageStatus status, Date sectionEndDate, Date now) {
		if(status == PageStatus.closed) {
			return true;
		}

		if(sectionEndDate != null && sectionEndDate.compareTo(now) < 0) {
			return true;
		}
		return false;
	}
}