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
package org.olat.course.nodes.gta.model;

import java.util.Date;

/**
 * 
 * Initial date: 3 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DueDate {
	
	private final Date dueDate;
	private final boolean relative; 
	private final String messageKey;
	private final String messageArg;
	
	public DueDate(boolean relative, String messageKey, String messageArg) {
		this(relative, null, messageKey, messageArg);
	}
	
	public DueDate(boolean relative, Date dueDate) {
		this(relative, dueDate, null, null);
	}
	
	public DueDate(boolean relative, Date dueDate, String messageKey, String messageArg) {
		this.dueDate = dueDate;
		this.relative = relative;
		this.messageKey = messageKey;
		this.messageArg = messageArg;
	}

	public Date getDueDate() {
		return dueDate;
	}
	
	public boolean isRelative() {
		return relative;
	}

	public String getMessageKey() {
		return messageKey;
	}
	
	public String getMessageArg() {
		return messageArg;
	}

}
