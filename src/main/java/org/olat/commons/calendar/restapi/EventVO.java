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
package org.olat.commons.calendar.restapi;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.model.KalendarEvent;

import io.swagger.v3.oas.annotations.media.Schema;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "eventVO")
public class EventVO {
	
	private String id;
	private String recurrenceId;
	private String subject;
	private String description;
	private String location;
	private String calendarId;
	
	private Date begin;
	private Date end;
	private boolean allDayEvent;
	
	private String liveStreamUrl;
	
	@Schema(required = true, description = "Action to be performed on managedFlags", allowableValues = { 
			"all",
			"details(all)",
			"subject(details, all)",
			"description(details, all)",
			"location(details, all)",
			"dates(details, all)",
			"classification(all)",
			"links(all)",
			"liveStreamUrl(details, all)"})
	private String managedFlags;
	private String externalId;
	private String externalSource;
	
	public EventVO() {
		//
	}
	
	public EventVO(KalendarEvent event) {
		id = event.getID();
		recurrenceId = event.getRecurrenceID();
		subject = event.getSubject();
		description = event.getDescription();
		location = event.getLocation();
		begin = event.getBegin();
		end = event.getEnd();
		allDayEvent = event.isAllDayEvent();
		liveStreamUrl = event.getLiveStreamUrl();
		calendarId = event.getCalendar().getType() + "_" + event.getCalendar().getCalendarID();
		managedFlags = CalendarManagedFlag.toString(event.getManagedFlags());
		externalId = event.getExternalId();
		externalSource = event.getExternalSource();
	}

	public String getId() {
		return id;
	}
	
	public String getRecurrenceId() {
		return recurrenceId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public boolean isAllDayEvent() {
		return allDayEvent;
	}

	public void setAllDayEvent(boolean allDayEvent) {
		this.allDayEvent = allDayEvent;
	}

	public String getLiveStreamUrl() {
		return liveStreamUrl;
	}

	public void setLiveStreamUrl(String liveStreamUrl) {
		this.liveStreamUrl = liveStreamUrl;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(String calendarId) {
		this.calendarId = calendarId;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalSource() {
		return externalSource;
	}

	public void setExternalSource(String externalSource) {
		this.externalSource = externalSource;
	}
}
