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
import java.util.List;

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
	
	KalendarEvent sourceEvent;

	public KalendarRecurEvent(String id, String subject, Date begin, Date end) {
		super(id, subject, begin, end);
	}
	
	public KalendarRecurEvent(String id, String subject, Date begin, int duration) {
		super(id, subject, begin, duration);
	}
	
	public KalendarRecurEvent(String id, String subject, Date begin, int duration, String recurrenceRule) {
		super(id, subject, begin, duration, recurrenceRule);
	}
	
	public KalendarRecurEvent(String id, String subject, Date begin, Date end, String recurrenceRule) {
		super(id, subject, begin, end, recurrenceRule);
	}
	
	/**
	 * @return source event for this recurrence
	 */
	public KalendarEvent getSourceEvent() {
		return sourceEvent;
	}
	
	/**
	 * @param source event for this recurrence
	 */
	public void setSourceEvent(KalendarEvent sourceEvent) {
		this.sourceEvent = sourceEvent;
	}
	
	public Kalendar getCalendar() {
		return sourceEvent.getCalendar();
	}

	public int getClassification() {
		return sourceEvent.getClassification();
	}

	public String getComment() {
		return sourceEvent.getComment();
	}

	public long getCreated() {
		return sourceEvent.getCreated();
	}

	public String getCreatedBy() {
		return sourceEvent.getCreatedBy();
	}

	public String getDescription() {
		return sourceEvent.getDescription();
	}

	public String getID() {
		return sourceEvent.getID();
	}

	public List getKalendarEventLinks() {
		return sourceEvent.getKalendarEventLinks();
	}

	public long getLastModified() {
		return sourceEvent.getLastModified();
	}

	public String getLocation() {
		return sourceEvent.getLocation();
	}

	public Integer getNumParticipants() {
		return sourceEvent.getNumParticipants();
	}

	public String[] getParticipants() {
		return sourceEvent.getParticipants();
	}

	public String getRecurrenceRule() {
		return sourceEvent.getRecurrenceRule();
	}

	public String getSourceNodeId() {
		return sourceEvent.getSourceNodeId();
	}

	public String getSubject() {
		return sourceEvent.getSubject();
	}

	public boolean isAllDayEvent() {
		return sourceEvent.isAllDayEvent();
	}
}
