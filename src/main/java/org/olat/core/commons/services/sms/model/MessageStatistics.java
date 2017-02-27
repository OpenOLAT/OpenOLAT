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
package org.olat.core.commons.services.sms.model;

import java.util.Date;

/**
 * 
 * Initial date: 7 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MessageStatistics {
	
	private final Date date;
	private final String service;
	private final long numOfMessages;

	public MessageStatistics(String service, Date date, long numOfMessages) {
		this.service = service;
		this.date = date;
		this.numOfMessages = numOfMessages;
	}

	public Date getDate() {
		return date;
	}

	public String getService() {
		return service;
	}

	public long getNumOfMessages() {
		return numOfMessages;
	}
}
