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
import org.olat.modules.reminder.model.ReminderInfos;

/**
 * 
 * Initial date: 07.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderRow {
	
	private final ReminderInfos reminder;
	private final FormLink toolsLink;
	private final FormLink emailLink;
	private final FormLink sendLink;
	private final String rulesComponentName;
	
	public ReminderRow(ReminderInfos reminder, FormLink toolsLink, FormLink emailLink, FormLink sendLink, String rulesComponentName) {
		this.reminder = reminder;
		this.toolsLink = toolsLink;
		this.emailLink = emailLink;
		this.sendLink = sendLink;
		this.rulesComponentName = rulesComponentName;
	}
	
	public Long getKey() {
		return reminder.getKey();
	}
	
	public String getCreator() {
		return reminder.getCreator();
	}
	
	public String getDescription() {
		return reminder.getDescription();
	}
	
	public String getSendTime() {
		return reminder.getSendTime();
	}
	
	public Date getCreationDate() {
		return reminder.getCreationDate();
	}
	
	public Date getLastModified() {
		return reminder.getLastModified();
	}
	
	public int getSend() {
		return reminder.getNumOfRemindersSent();
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public FormLink getEmailLink() {
		return emailLink;
	}
	
	public FormLink getSendLink() {
		return sendLink;
	}

	public String getRulesComponentName() {
		return rulesComponentName;
	}

}
