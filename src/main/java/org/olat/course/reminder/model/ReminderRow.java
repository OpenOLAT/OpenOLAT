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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.reminder.Reminder;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderRow {
	
	private final Reminder reminder;
	private final Integer send;
	private final FormLink toolsLink;
	
	public ReminderRow(Reminder reminder, Integer send, FormLink toolsLink) {
		this.reminder = reminder;
		this.send = send;
		this.toolsLink = toolsLink;
	}
	
	public Long getKey() {
		return reminder.getKey();
	}
	
	public String getDescription() {
		return reminder.getDescription();
	}
	
	public Date getCreationDate() {
		return reminder.getCreationDate();
	}
	
	public Date getLastModified() {
		return reminder.getLastModified();
	}
	
	public Reminder getReminder() {
		return reminder;
	}
	
	public Integer getSend() {
		return send;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}
}
