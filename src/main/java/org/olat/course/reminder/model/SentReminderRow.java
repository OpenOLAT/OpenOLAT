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
package org.olat.course.reminder.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.SentReminder;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 08.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SentReminderRow extends UserPropertiesRow {
	
	private final Long reminderKey;
	private final String description;
	private final Date sendDate;
	private final String status;
	
	public SentReminderRow(Reminder reminder, SentReminder sentReminder,
			Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.description = reminder.getDescription();
		this.sendDate = sentReminder.getCreationDate();
		this.status = sentReminder.getStatus();
		this.reminderKey = reminder.getKey();
	}
	
	public Long getReminderKey() {
		return reminderKey;
	}

	public String getStatus() {
		return status;
	}

	public String getReminderDescription() {
		return description;
	}
	
	public Date getSendDate() {
		return sendDate;
	}
}
