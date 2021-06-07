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

import java.util.Date;

/**
 * 
 * Initial date: 10.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderInfos {
	
	private final Long key;
	private final String description;
	private final String configuration;
	private final String sendTime;
	private final Long creatorKey;
	private final String creator;
	private final Date creationDate;
	private final Date lastModified;
	private final int numOfRemindersSent;
	
	public ReminderInfos(Long key, Date creationDate, Date lastModified, String description, String configuration,
			String sendTime, Long creatorKey, String creator, int numOfRemindersSent) {
		this.key = key;
		this.creationDate = creationDate;
		this.lastModified = lastModified;
		this.description = description;
		this.configuration = configuration;
		this.sendTime = sendTime;
		this.creatorKey = creatorKey;
		this.creator = creator;
		this.numOfRemindersSent = numOfRemindersSent;
	}
	
	public Long getKey() {
		return key;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getConfiguration() {
		return configuration;
	}

	public String getSendTime() {
		return sendTime;
	}
	
	public Long getCreatorKey() {
		return creatorKey;
	}
	
	public String getCreator() {
		return creator;
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	public Date getLastModified() {
		return lastModified;
	}

	public int getNumOfRemindersSent() {
		return numOfRemindersSent;
	}
}
