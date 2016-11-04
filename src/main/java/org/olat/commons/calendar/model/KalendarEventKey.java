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

/**
 * 
 * Initial date: 25 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class KalendarEventKey {
	
	private String eventId;
	private String recurrenceId;
	
	public KalendarEventKey(String eventId, String recurrenceId) {
		this.eventId = eventId;
		this.recurrenceId = recurrenceId;
	}
	
	public KalendarEventKey(KalendarEvent kEvent) {
		eventId = kEvent.getID();
		recurrenceId = kEvent.getRecurrenceID();
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getRecurrenceId() {
		return recurrenceId;
	}

	public void setRecurrenceId(String recurrenceId) {
		this.recurrenceId = recurrenceId;
	}

	@Override
	public int hashCode() {
		return (eventId == null ? 289297 : eventId.hashCode())
				+ (recurrenceId == null ? 85062 : recurrenceId.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof KalendarEventKey) {
			KalendarEventKey key = (KalendarEventKey)obj;
			return eventId != null && eventId.equals(key.eventId)
					&& ((recurrenceId == null && key.recurrenceId == null) || (recurrenceId != null && recurrenceId.equals(key.recurrenceId)));
		}
		return false;
	}
}
