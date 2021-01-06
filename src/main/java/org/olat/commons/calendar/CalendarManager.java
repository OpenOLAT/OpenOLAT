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
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.model.CalendarKey;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;

import net.fortuna.ical4j.model.Calendar;

public interface CalendarManager {

	public static final String TYPE_USER = "user";
	public static final String TYPE_GROUP = "group";
	public static final String TYPE_COURSE = "course";
	public static final String TYPE_CURRICULUM_EL_AGGREGATED = "claggregated";
	public static final String TYPE_USER_AGGREGATED = "paggregated";
	
	public static final String ICAL_X_OLAT_LINK = "X-OLAT-LINK";
	public static final String ICAL_X_OLAT_COMMENT = "X-OLAT-COMMENT";
	public static final String ICAL_X_OLAT_NUMPARTICIPANTS = "X-OLAT-NUMPARTICIPANTS";
	public static final String ICAL_X_OLAT_PARTICIPANTS = "X-OLAT-PARTICIPANTS";
	public static final String ICAL_X_OLAT_SOURCENODEID = "X-OLAT-SOURCENODEID";
	public static final String ICAL_X_OLAT_MANAGED = "X-OLAT-MANAGED";
	public static final String ICAL_X_OLAT_EXTERNAL_ID = "X-OLAT-EXTERNAL-ID";
	public static final String ICAL_X_OLAT_EXTERNAL_SOURCE = "X-OLAT-EXTERNAL-SOURCE";
	public static final String ICAL_X_OLAT_VIDEO_STREAM_URL = "X-OLAT-VIDEO-STREAM-URL";
	public static final String ICAL_X_OLAT_VIDEO_STREAM_URL_TEMPLATE_KEY = "X-OLAT-VIDEO-STREAM-URL_TEMPLATE_KEY";

	/** path prefix for personal iCal feed **/
	public static final String ICAL_PREFIX_AGGREGATED = "/paggregated/";
	/** path prefix for personal iCal feed **/
	public static final String ICAL_PREFIX_PERSONAL = "/user/";
	/** path prefix for course iCal feed **/
	public static final String ICAL_PREFIX_COURSE = "/course/";
	/** path prefix for group iCal feed **/
	public static final String ICAL_PREFIX_GROUP = "/group/";
	
	/** Expected number of tokens in the course/group calendar link **/
	public static final int ICAL_PATH_TOKEN_LENGTH = 4;
	/** Expected number of tokens in the personal calendar link **/
	public static final int ICAL_PERSONAL_PATH_TOKEN_LENGTH = 3;
	
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
	 * Return the calendar file if it exists or null.
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
	public KalendarRenderWrapper getImportedCalendar(Identity identity, String calendarID);
	
	
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
	
	public Calendar readCalendar(File calendarFile);
	
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
	public CalendarUserConfiguration findCalendarConfigForIdentity(Kalendar calendar, IdentityRef identity);
	
	/**
	 * Save the calendar configuration for a specific calendar for
	 * a specific identity.
	 * 
	 * @param kalendarConfig
	 * @param calendar
	 * @param ureq
	 */
	public void saveCalendarConfigForIdentity(KalendarRenderWrapper calendar, Identity identity);
	
	public CalendarUserConfiguration saveCalendarConfig(CalendarUserConfiguration configuration);
	
	/**
	 * 
	 * @param identity The user which want a token
	 * @return A configuration with a security token
	 */
	public CalendarUserConfiguration createAggregatedCalendarConfig(String calendarType, Long calendarId, Identity identity);
	
	public CalendarUserConfiguration createCalendarConfig(Identity identity, Kalendar calendar);
	
	public List<CalendarUserConfiguration> getCalendarUserConfigurationsList(IdentityRef identity, String calendarType, String calendarId);
	
	public CalendarUserConfiguration getCalendarUserConfiguration(IdentityRef identity, Kalendar calendar);
	
	/**
	 * @param key The primary key of the configuration
	 * @return The configuration
	 */
	public CalendarUserConfiguration getCalendarUserConfiguration(Long key);

	/**
	 * Retrieve the settings for a specific user.
	 * 
	 * @param identity The user
	 * @param types The types of calendars (optional)
	 * @return
	 */
	public Map<CalendarKey,CalendarUserConfiguration> getCalendarUserConfigurationsMap(IdentityRef identity, String... types);
	
	/**
	 * Retrieve the token if it exists.
	 * 
	 * @param calendarType
	 * @param calendarID
	 * @param userName
	 * @return
	 */
	public String getCalendarToken(String calendarType, String calendarID, String userName);
	
	public KalendarEvent createKalendarEventRecurringOccurence(KalendarRecurEvent parentEvent);

	/**
	 * Add an event to given calendar and save calendar.
	 * @param cal
	 * @param kalendarEvent
	 * @return true if success
	 */
	public boolean addEventTo(Kalendar cal, KalendarEvent kalendarEvent);
	
	/**
	 * Add a list of events to a given calendar and save it.
	 * 
	 * @param cal
	 * @param kalendarEvents
	 * @return
	 */
	public boolean addEventTo(Kalendar cal, List<KalendarEvent> kalendarEvents);

	/**
	 * Remove an event from given calendar and save calendar. In the case of
	 * a recurring event, all the recurrences while be deleted with the exceptions.
	 * In the case of an exception to a recurring event, only the exception will
	 * be deleted.
	 * 
	 * @param cal
	 * @param kalendarEvent
	 * @return true if success
	 */
	public boolean removeEventFrom(Kalendar cal, KalendarEvent kalendarEvent);
	
	public boolean removeEventsFrom(Kalendar cal, List<KalendarEvent> kalendarEvents);
	
	/**
	 * Remove an occurence of a recurring event.
	 * 
	 * @param cal
	 * @param kalendarEvent
	 * @return
	 */
	public boolean removeOccurenceOfEvent(Kalendar cal, KalendarRecurEvent kalendarEvent);
	
	/**
	 * Truncate the recurrence rule and adjust the unitl date.
	 * 
	 * @param cal
	 * @param kalendarEvent
	 * @return
	 */
	public boolean removeFutureOfEvent(Kalendar cal, KalendarRecurEvent kalendarEvent);
	
	
	/**
	 * Update an event of given calendar and save calendar.
	 * @param cal
	 * @param kalendarEvent
	 * @return true if success
	 */
	public boolean updateEventFrom(Kalendar cal, KalendarEvent kalendarEvent);
	
	/**
	 * Update a list of events fron a given calendar and save it.
	 * 
	 * @param cal
	 * @param kalendarEvent
	 * @return
	 */
	public boolean updateEventsFrom(Kalendar cal, List<KalendarEvent> kalendarEvents);
	
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
	

	public List<KalendarEvent> getEvents(Kalendar calendar, Date from, Date to, boolean privateEventsVisible);

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
	 * Return the last date (until) of a recurrence rule.
	 * 
	 * @param rule
	 * @return
	 */
	public Date getRecurrenceEndDate(String rule);
	
	/**
	 * The method set the recurrence rule until the end of the day.
	 * 
	 * @param recurrence
	 * @param recurrenceEnd
	 * @return
	 */
	public String getRecurrenceRule(String recurrence, Date recurrenceEnd);
	
	/**
	 * Build a Calendar object from String object.
	 * @param calendarContent
	 * @return
	 */
	public Kalendar buildKalendarFrom(InputStream calendarContent, String calType,  String calId);
	
	/**
	 * Create an URL connection with default settings like time out.
	 * 
	 * @param url The URL as string
	 * @return A connection or null
	 */
	public URLConnection getURLConnection(String url);
	
	/**
	 * Synchronize the event of the calendar stream to the target calendar,
	 * set the synchronized events as managed.
	 * 
	 * @param in
	 * @param targetCalendar
	 * @return
	 */
	public boolean synchronizeCalendarFrom(InputStream in, String source, Kalendar targetCalendar);

	/**
	 * Create Ores Helper object.
	 * @param cal
	 * @return OLATResourceable for given Kalendar
	 */
	public OLATResourceable getOresHelperFor(Kalendar cal);
	
	
}
