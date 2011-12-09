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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;

import net.fortuna.ical4j.model.Recur;

public class KalendarEvent implements Cloneable, Comparable {

	public static final int CLASS_PRIVATE = 0;
	public static final int CLASS_X_FREEBUSY = 1;
	public static final int CLASS_PUBLIC = 2;
	
	public static final String DAILY = Recur.DAILY;
	public static final String WEEKLY = Recur.WEEKLY;
	public static final String MONTHLY = Recur.MONTHLY;
	public static final String YEARLY = Recur.YEARLY;
	public static final String WORKDAILY = "WORKDAILY";
	public static final String BIWEEKLY = "BIWEEKLY";
	
	public static final String UNTIL = "UNTIL";
	public static final String COUNT = "COUNT";
	
	private String id;
	transient private Kalendar kalendar;
	private String subject;
	private String description;
	private Date begin, end;
	private boolean isAllDayEvent;
	private String location;
	private List kalendarEventLinks;
	private long created, lastModified;
	private String createdBy;
	private int classification;
	
	private String comment;
	private Integer numParticipants;
	private String[] participants;
	private String sourceNodeId;

	private String recurrenceRule;
	private String recurrenceExc;

	private KalendarEvent() {
		// save no-args constructor for XStream
	}
	
	/**
	 * Create a new calendar event with the given subject and
	 * given start and end times as UNIX timestamps.
	 * 
	 * @param subject
	 * @param begin
	 * @param end
	 */
	public KalendarEvent(String id, String subject, Date begin, Date end) {
		this.id = id;
		this.subject = subject;
		this.begin = begin;
		this.end = end;
		this.isAllDayEvent = false;
		this.kalendarEventLinks = new ArrayList();
	}
	
	/**
	 * Create a new calendar entry with the given subject, starting at
	 * <begin> and with a duration of <duration> milliseconds.
	 * @param subject
	 * @param begin
	 * @param duration
	 */
	public KalendarEvent(String id, String subject, Date begin, int duration) {
		this.id = id;
		this.subject = subject;
		this.begin = begin;
		this.end = new Date(begin.getTime() + duration);
		this.isAllDayEvent = false;
		this.kalendarEventLinks = new ArrayList();
	}
	
	/**
	 * Create a new calendar entry with the given start, a duration and a recurrence
	 * @param id
	 * @param subject
	 * @param begin
	 * @param duration
	 * @param recurrenceRule
	 */
	public KalendarEvent(String id, String subject, Date begin, int duration, String recurrenceRule) {
		this(id, subject, begin, duration);
		this.recurrenceRule = recurrenceRule;
	}
	
	/**
	 * Create a new calendar entry with the given start and end
	 * @param id
	 * @param subject
	 * @param begin
	 * @param end
	 * @param recurrenceRule
	 */
	public KalendarEvent(String id, String subject, Date begin, Date end, String recurrenceRule) {
		this(id, subject, begin, end);
		this.recurrenceRule = recurrenceRule;
	}
	
	protected void setKalendar(Kalendar kalendar) {
		this.kalendar = kalendar;
	}
	
	public String getID() {
		return id;
	}
	public Date getBegin() {
		return begin;
	}
	public void setBegin(Date begin) {
		this.begin = begin;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getClassification() {
		return classification;
	}

	public void setClassification(int classification) {
		this.classification = classification;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Kalendar getCalendar() {
		return kalendar;
	}

	public boolean isAllDayEvent() {
		return isAllDayEvent;
	}

	public void setAllDayEvent(boolean isAllDayEvent) {
		this.isAllDayEvent = isAllDayEvent;
	}
	
	public boolean isToday() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(begin);
		int startDay = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTime(end);
		int endDay = cal.get(Calendar.DAY_OF_YEAR);
		int todayDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		return (todayDay - startDay == 0) && ((todayDay - endDay == 0));
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isWithinOneDay() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(begin);
		int startDay = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTime(end);
		int endDay = cal.get(Calendar.DAY_OF_YEAR);
		return (endDay - startDay == 0);
	}

	/**
	 * @return Returns the uRI.
	 */
	public List getKalendarEventLinks() {
		return kalendarEventLinks;
	}

	/**
	 * @param uri The uRI to set.
	 */
	public void setKalendarEventLinks(List kalendarEventLinks) {
		this.kalendarEventLinks = kalendarEventLinks;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getNumParticipants() {
		return numParticipants;
	}

	public void setNumParticipants(int numParticipants) {
		this.numParticipants = numParticipants;
	}

	public String[] getParticipants() {
		return participants;
	}

	public void setParticipants(String[] participants) {
		this.participants = participants;
	}

	public String getSourceNodeId() {
		return sourceNodeId;
	}

	public void setSourceNodeId(String sourceNodeId) {
		this.sourceNodeId = sourceNodeId;
	}

	public String getRecurrenceRule() {
		return recurrenceRule;
	}
	
	public void setRecurrenceRule(String recurrenceRule) {
		this.recurrenceRule = recurrenceRule;
	}

	public KalendarEvent clone() {
		Object c = null;
		try {
			c = super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		return (KalendarEvent)c;
	}

	public void setRecurrenceExc(String recurrenceExc) {
		this.recurrenceExc = recurrenceExc;
	}

	public String getRecurrenceExc() {
		return recurrenceExc;
	}
	
	public void addRecurrenceExc(Date excDate) {
		List<Date> excDates = CalendarUtils.getRecurrenceExcludeDates(recurrenceExc);
		excDates.add(excDate);
		String excRule = CalendarUtils.getRecurrenceExcludeRule(excDates);
		setRecurrenceExc(excRule);
	}
	
	public int compareTo(Object o1) {
		if ( !(o1 instanceof KalendarEvent)) {
			return -1;
		}
		KalendarEvent event1 = (KalendarEvent)o1;
		return this.getBegin().compareTo(event1.getBegin());
	}

}
