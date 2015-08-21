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

package org.olat.commons.calendar;

import java.io.File;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.model.Calendar;

import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;

public interface CalendarManager {

	public static final String TYPE_USER = "user";
	public static final String TYPE_GROUP = "group";
	public static final String TYPE_COURSE = "course";
	
	public static final String ICAL_X_OLAT_LINK = "X-OLAT-LINK";
	public static final String ICAL_X_OLAT_COMMENT = "X-OLAT-COMMENT";
	public static final String ICAL_X_OLAT_NUMPARTICIPANTS = "X-OLAT-NUMPARTICIPANTS";
	public static final String ICAL_X_OLAT_PARTICIPANTS = "X-OLAT-PARTICIPANTS";
	public static final String ICAL_X_OLAT_SOURCENODEID = "X-OLAT-SOURCENODEID";
	public static final String ICAL_X_OLAT_MANAGED = "X-OLAT-MANAGED";
	public static final String ICAL_X_OLAT_EXTERNAL_ID = "X-OLAT-EXTERNAL-ID";
	
	public static final int MAX_SUBJECT_DISPLAY_LENGTH = 30;

	/**
	 * Create a new calendar with the given id.
	 * @param calendarID
	 * @param type
	 * @return
	 */
	public Kalendar createCalendar(String calendarType, String calendarID);

	/**
	 * Check if a calendar already exists for the given id.
	 * @param calendarID
	 * @param type
	 * @return
	 */
	public boolean calendarExists(String calendarType, String calendarID);
	
	/**
	 * Save a calendar.
	 * 
	 * @param calendar
	 */
	public boolean persistCalendar(Kalendar calendar);
	
	/**
	 * Delete a calendar.
	 * 
	 * @param calendarType
	 * @param calendarID
	 * @return
	 */
	public boolean deleteCalendar(String calendarType, String calendarID);
	
	/**
	 * Get a calendar as iCalendar file.
	 * 
	 * @param calendarType
	 * @param calendarID
	 * @return
	 */
	public File getCalendarICalFile(String calendarType, String calendarID);
	
	/**
	 * Get an identity's personal calendar. If the calendar does not exist yet,
	 * a new calendar will be created.
	 * The calendar will be configured with defaults for calendar config.
	 * 
	 * @param identity
	 * @return
	 */
	public KalendarRenderWrapper getPersonalCalendar(Identity identity);
	
	/**
	 * Get an identity's personal calendar. If the calendar does not exist yet,
	 * a new calendar will be created.
	 * The calendar will be configured with defaults for calendar config.
	 * 
	 * @param identity
	 * @return
	 */
	public KalendarRenderWrapper getImportedCalendar(Identity identity, String calendarName);
	
	
	/**
	 * Delete the personal calendar of an identity.
	 * 
	 * @param identity
	 */
	public void deletePersonalCalendar(Identity identity);
	
	/**
	 * Get a group's calendar. If the calendar does not yet exist, a 
	 * new calendar will be created.
	 * The calendar will be configured with defaults for calendar config.
	 * 
	 * @param businessGroup
	 * @return
	 */
	public KalendarRenderWrapper getGroupCalendar(BusinessGroup businessGroup);

	/**
	 * Delete the calendar of the given business group.
	 * @param businessGroup
	 */
	public void deleteGroupCalendar(BusinessGroup businessGroup);
	
	/**
	 * Get calendar for course. If the calendar does not yet exist, a
	 * new calendar will be created.
	 * The calendar will be configured with defaults for calendar config.
	 * 
	 * @param course
	 * @return
	 */
	public KalendarRenderWrapper getCourseCalendar(ICourse course);
	
	/**
	 * Get calendar for resource. If the calendar does not yet exist, it will not be created.
	 * 
	 * @param course
	 * @return
	 */
	public KalendarRenderWrapper getCalendarForDeletion(OLATResourceable resource);
	
	/**
	 * get the calendar file name from type and id 
	 * @param type
	 * @param calendarID
	 * @return
	 */
	public File getCalendarFile(String type, String calendarID);
	
	/**
	 * Read the calendar file (.ics) from the olatdata section. 
	 * @param type
	 * @param calendarID
	 * @return
	 */
	public Calendar readCalendar(String type, String calendarID);
	
	/**
	 * Delete the calendar of the given course.
	 * @param course
	 */
	public void deleteCourseCalendar(ICourse course);
	
	/**
	 * Delete the calendar of the given resource.
	 * @param course
	 */
	public void deleteCourseCalendar(OLATResourceable resource);
	
	/**
	 * Get the individual calendar configuration for a specific
	 * calendar for a specific identity.
	 * If no individual calendar config exists, null is returned.
	 * 
	 * @param calendar
	 * @param ureq
	 * @return
	 */
	public KalendarConfig findKalendarConfigForIdentity(Kalendar calendar, UserRequest ureq);
	
	/**
	 * Save the calendar configuration for a specific calendar for
	 * a specific identity.
	 * 
	 * @param kalendarConfig
	 * @param calendar
	 * @param ureq
	 */
	public void saveKalendarConfigForIdentity(KalendarConfig kalendarConfig, Kalendar calendar, UserRequest ureq);

	/**
	 * Add an event to given calendar and save calendar.
	 * @param cal
	 * @param kalendarEvent
	 * @return true if success
	 */
	public boolean addEventTo(Kalendar cal, KalendarEvent kalendarEvent);

	/**
	 * Remove an event from given calendar and save calendar.
	 * @param cal
	 * @param kalendarEvent
	 * @return true if success
	 */
	public boolean removeEventFrom(Kalendar cal, KalendarEvent kalendarEvent);
	
	/**
	 * Update an event of given calendar and save calendar.
	 * @param cal
	 * @param kalendarEvent
	 * @return true if success
	 */
	public boolean updateEventFrom(Kalendar cal, KalendarEvent kalendarEvent);
	
	/**
	 * Update an event of given calendar and save calendar. Use this method if the Kalendar is already in a doInSync.
	 * @param cal
	 * @param kalendarEvent
	 * @return true if success
	 */
	public boolean updateEventAlreadyInSync(final Kalendar cal, final KalendarEvent kalendarEvent);
	
	/**
	 * Update a calendar with the events from an other calendar
	 * @param cal
	 * @param importedCal
	 * @return true if success
	 */
	public boolean updateCalendar(final Kalendar cal, final Kalendar importedCal);

	/**
	 * Get a calendar by type and id.
	 * @param type
	 * @param calendarID
	 * @return 
	 */
	public Kalendar getCalendar(String type, String calendarID);

	/**
	 * Get the recurring event
	 * @param today
	 * @param kEvent
	 * @return affected <code>KalendarEvent</code> or <code>null</code> if not recurring in period
	 */
	public KalendarEvent getRecurringInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent);
	
	/**
	 * Check if the event recurs within the given period
	 * @param periodStart
	 * @param periodEnd
	 * @param kEvent
	 * @return <code>true</code> if event recurs in the given period, otherwise <code>false</code>
	 */
	public boolean isRecurringInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent);
	
	/**
	 * Get all recurrings of an event within the given period
	 * @param periodStart
	 * @param periodEnd
	 * @param kEvent
	 * @return list with <code>KalendarRecurEvent</code>
	 */
	public List<KalendarRecurEvent> getRecurringDatesInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent);

	/**
	 * Build a Calendar object from String object.
	 * @param calendarContent
	 * @return
	 */
	public Kalendar buildKalendarFrom(String calendarContent, String calType,  String calId);

	/**
	 * Create Ores Helper object.
	 * @param cal
	 * @return OLATResourceable for given Kalendar
	 */
	public OLATResourceable getOresHelperFor(Kalendar cal);
}
