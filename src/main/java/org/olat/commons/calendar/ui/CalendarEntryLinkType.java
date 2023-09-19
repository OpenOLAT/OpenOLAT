/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.commons.calendar.ui;

/**
 * Initial date: Aug 29, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum CalendarEntryLinkType {
	LINK_TO_COURSE("cal.entry.link.title.course"),
	LINK_TO_COURSE_ELEMENT("cal.entry.link.title.course.el"),
	LINK_TO_GROUP("cal.entry.link.title.group"),
	LINK_TO_LIBRARY("cal.entry.link.title.library"),
	LINK_EXTERNAL("cal.entry.link.title.external");

	private final String i18nKey;

	CalendarEntryLinkType(String i18nKey) {
		this.i18nKey = i18nKey;
	}

	public String i18nKey() {
		return i18nKey;
	}
}
