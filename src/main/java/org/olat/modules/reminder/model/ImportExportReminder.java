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
package org.olat.modules.reminder.model;

import java.io.Serializable;

import org.olat.modules.reminder.Reminder;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportExportReminder implements Serializable {

	private static final long serialVersionUID = -6548172969534189234L;
	
	private String description;
	private String configuration;
	private String emailSubject;	// added in OO 12.0
	private String emailBody;
	
	public ImportExportReminder() {
		//
	}
	
	public ImportExportReminder(Reminder reminder) {
		description = reminder.getDescription();
		configuration = reminder.getConfiguration();
		emailSubject = reminder.getEmailSubject();
		emailBody = reminder.getEmailBody();
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}
	
	public String getEmailSubject() {
		return emailSubject;
	}
	
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}
	
	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

}
