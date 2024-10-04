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
package org.olat.core.commons.services.notifications.ui;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

/**
 * Initial date: Aug 27, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ContextualSubscriptionListRow {

	private final String description;
	private final String iconCssClass;
	private final String link;
	private final Date date;
	private final Locale locale;

	public ContextualSubscriptionListRow(String description, String iconCssClass,
										 String link, Date date, Locale locale) {
		this.description = description;
		this.iconCssClass = iconCssClass;
		this.link = link;
		this.date = date;
		this.locale = locale;
	}

	public String getDescription() {
		return new OWASPAntiSamyXSSFilter().filter(description.trim());
	}

	public String getIconCssClass() {
		return iconCssClass;
	}

	public String getLink() {
		return link;
	}

	public String getFormattedDate() {
		Formatter form = Formatter.getInstance(locale);
		return form.formatDateAndTime(date);
	}
}
