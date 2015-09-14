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
package org.olat.commons.calendar.model;

import java.io.Serializable;

/**
 * Useable in HashMap and co.
 * 
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarKey implements Serializable {
	
	private static final long serialVersionUID = 4402127033060370317L;
	private final String type;
	private final String calendarId;
	
	public CalendarKey(String calendarId, String type) {
		this.type = type;
		this.calendarId = calendarId;
	}
	
	public String getType() {
		return type;
	}
	
	public String getCalendarId() {
		return calendarId;
	}

	@Override
	public int hashCode() {
		return type.hashCode()
				+ calendarId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CalendarKey) {
			CalendarKey key = (CalendarKey)obj;
			return key.calendarId.equals(calendarId)
					&& key.type.equals(type);
		}
		return false;
	}

}
