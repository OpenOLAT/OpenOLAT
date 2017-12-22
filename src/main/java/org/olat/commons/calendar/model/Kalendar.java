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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kalendar implements Serializable {

	private static final long serialVersionUID = -2179014489859413340L;
	
	private final String calendarID;
	private final String type;
	private final Map<KalendarEventKey, KalendarEvent> events = new HashMap<>();
	
	private int managedEvent;
	private int notManagedEvent;
	
	public Kalendar(String calendarID, String type) {
		this.calendarID = calendarID;
		this.type = type;
	}
	
	/**
	 * Return this calendar's ID.
	 * @return
	 */
	public String getCalendarID() {
		return calendarID;
	}
	
	public int size() {
		return events.size();
	}
	
	public boolean hasManagedEvents() {
		return managedEvent > 0;
	}
	
	public boolean hasNotManagedEvents() {
		return notManagedEvent > 0;
	}
	
	/**
	 * Add a new event.
	 * @param event
	 */
	public void addEvent(KalendarEvent event) {
		if(event.isManaged()) {
			managedEvent++;
		} else {
			notManagedEvent++;
		}
		event.setKalendar(this);
		events.put(new KalendarEventKey(event.getID(), event.getRecurrenceID()), event);
	}
	
	/**
	 * Remove an event from this calendar.
	 * @param event
	 */
	public void removeEvent(KalendarEvent event) {
		if(event.isManaged()) {
			managedEvent--;
		} else {
			notManagedEvent--;
		}
		events.remove(new KalendarEventKey(event.getID(), event.getRecurrenceID()));
	}
	
	/**
	 * Get a specific event.
	 * 
	 * @param eventID
	 * @return
	 */
	public KalendarEvent getEvent(String eventID, String recurenceID) {
		return events.get(new KalendarEventKey(eventID, recurenceID));
	}
	
	/**
	 * Return all events associated with this calendar.
	 * @return
	 */
	public List<KalendarEvent> getEvents() {
		return new ArrayList<>(events.values());
	}

	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "Kalendar[type=" + getType() + ", id=" + getCalendarID() + "]";
	}
}