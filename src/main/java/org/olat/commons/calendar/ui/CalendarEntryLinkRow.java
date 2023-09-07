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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;

/**
 * Initial date: Aug 29, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CalendarEntryLinkRow {

	private final String key;
	private final CalendarEntryLinkType calendarEntryLinkType;
	private TextElement linkTitleEl;
	private StaticTextElement linkUrlEl;
	private FormLink deleteLink;

	public CalendarEntryLinkRow(String key, CalendarEntryLinkType calendarEntryLinkType) {
		this.key = key;
		this.calendarEntryLinkType = calendarEntryLinkType;
	}

	public String getKey() {
		return key;
	}

	public CalendarEntryLinkType getCalendarEntryLinkType() {
		return calendarEntryLinkType;
	}

	public TextElement getLinkTitleEl() {
		return linkTitleEl;
	}

	public void setLinkTitleEl(TextElement linkTitleEl) {
		this.linkTitleEl = linkTitleEl;
	}

	public StaticTextElement getLinkUrlEl() {
		return linkUrlEl;
	}

	public void setLinkUrlEl(StaticTextElement linkUrlEl) {
		this.linkUrlEl = linkUrlEl;
	}

	public String getDeleteLinkName() {
		return deleteLink != null ? deleteLink.getName() : null;
	}

	public void setDeleteLinkEl(FormLink deleteLink) {
		this.deleteLink = deleteLink;
	}
}
