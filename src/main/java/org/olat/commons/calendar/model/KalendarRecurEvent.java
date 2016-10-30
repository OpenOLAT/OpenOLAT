/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.commons.calendar.model;

import java.util.Date;

/**
 * 
 * Description:<br>
 * Kalendar Event for recurring events
 * 
 * <P>
 * Initial Date:  08.04.2009 <br>
 * @author skoeber
 */
public class KalendarRecurEvent extends KalendarEvent {

	private boolean original;

	public KalendarRecurEvent(String id, boolean original, String subject, Date begin, Date end) {
		super(id, null, subject, begin, end);
		this.original = original;
	}
	
	public boolean isOriginal() {
		return original;
	}
	
	/**
	 * @param source event for this recurrence
	 */
	public void setSourceEvent(KalendarEvent sourceEvent) {
		fillEvent(sourceEvent);
	}
	
	public void setRecurrenceEvent(KalendarEvent recurrenceEvent) {
		setRecurrenceID(recurrenceEvent.getRecurrenceID());
		setBegin(recurrenceEvent.getBegin());
		setEnd(recurrenceEvent.getEnd());
		fillEvent(recurrenceEvent);
	}

	private void fillEvent(KalendarEvent event) {
		super.setAllDayEvent(event.isAllDayEvent());
		super.setClassification(event.getClassification());
		super.setComment(event.getComment());
		super.setCreated(event.getCreated());
		super.setCreatedBy(event.getCreatedBy());
		super.setDescription(event.getDescription());
		super.setKalendarEventLinks(event.getKalendarEventLinks());
		super.setLastModified(event.getLastModified());
		super.setKalendar(event.getCalendar());
		super.setLocation(event.getLocation());
		super.setManagedFlags(event.getManagedFlags());
		super.setNumParticipants(event.getNumParticipants());
		super.setParticipants(event.getParticipants());
		super.setRecurrenceExc(event.getRecurrenceExc());
		super.setRecurrenceRule(event.getRecurrenceRule());
		super.setSourceNodeId(event.getSourceNodeId());
		super.setSubject(event.getSubject());
	}
}
