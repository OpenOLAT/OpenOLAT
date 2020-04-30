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
* <p>
*/ 

package org.olat.commons.calendar.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.CalendarFileInfos;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.ImportedCalendar;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarComparator;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.fortuna.ical4j.model.Calendar;


/**
 * Description:<BR>
 * Constants and helper methods for the OLAT iCal feeds
 * 
 * <P>
 * Initial Date:  July 22, 2008
 *
 * @author Udit Sajjanhar
 */
@Service
public class ImportCalendarManager {
	private static final Logger log = Tracing.createLoggerFor(ImportCalendarManager.class);
	
	private static final int RELOAD_INTERVAL = 3600000;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private ImportedCalendarDAO importedCalendarDao;

	public KalendarRenderWrapper importCalendar(Identity identity, String calendarName, String type, String url) throws IOException {
		File tmpFile = new File(WebappHelper.getTmpDir(), UUID.randomUUID() + ".ics");
		try(InputStream in = new URL(url).openStream()) {
			Files.copy(in, tmpFile.toPath());
		} catch(IOException e) {
			log.error("Cannot copy {}", tmpFile, e);
			throw e;
		}
		
		KalendarRenderWrapper calendarWrapper = null;
		Calendar calendar = calendarManager.readCalendar(tmpFile);
		if(calendar != null) {
			String calendarID = getImportedCalendarID(identity, calendarName);
			File calendarFile = calendarManager.getCalendarFile(type, calendarID);
			if(!tmpFile.renameTo(calendarFile)) {
				Files.copy(tmpFile.toPath(), calendarFile.toPath());
			}
			
			importedCalendarDao.createImportedCalendar(identity, calendarName, calendarID, type, url, new Date());
			calendarWrapper = calendarManager.getImportedCalendar(identity, calendarID);
			calendarWrapper.setDisplayName(calendarName);
			calendarWrapper.setPrivateEventsVisible(true);
		}
		return calendarWrapper;
	}
	
	public KalendarRenderWrapper importCalendar(Identity identity, String calendarName, String type, File file)
	throws IOException {
		KalendarRenderWrapper calendarWrapper = null;
		Calendar calendar = calendarManager.readCalendar(file);
		if(calendar != null) {
			String calendarID = getImportedCalendarID(identity, calendarName);
			File calendarFile = calendarManager.getCalendarFile(type, calendarID);
			if(!file.renameTo(calendarFile)) {
				Files.copy(file.toPath(), calendarFile.toPath());
			}
			
			importedCalendarDao.createImportedCalendar(identity, calendarName, calendarID, type, null, new Date());
			calendarWrapper = calendarManager.getImportedCalendar(identity, calendarID);
			calendarWrapper.setDisplayName(calendarName);
			calendarWrapper.setPrivateEventsVisible(true);
		}
		return calendarWrapper;
	}
	
	/**
	 * Delete an imported calendar
	 *  1. remove the entry from the database
	 *  2. delete the calendar file
	 * @param calendarID
	 * @param ureq
	 * @return
	 */
	public void deleteCalendar(Identity identity, Kalendar calendar) {
		importedCalendarDao.deleteImportedCalendar(identity, calendar.getCalendarID(), calendar.getType());
		calendarManager.deleteCalendar(calendar.getType(), calendar.getCalendarID());
	}
	
	/**
	 * Get imported calendars for a user.
	 * @param ureq
	 * @return
	 */
	public List<KalendarRenderWrapper> getImportedCalendarsForIdentity(Identity identity, boolean reload) {
		// initialize the calendars list

		List<KalendarRenderWrapper> calendars = new ArrayList<>();
		if(calendarModule.isEnabled() && calendarModule.isEnablePersonalCalendar()) {
			long timestamp = System.currentTimeMillis();

			List<ImportedCalendar> importedCalendars = importedCalendarDao.getImportedCalendars(identity);
			KalendarEventFilter filter = new KalendarEventFilter(identity, importedCalendars);
			for (ImportedCalendar importedCalendar: importedCalendars) {
				try {
					if(reload) {
						reloadImportCalendar(importedCalendar, timestamp, filter);
					}

					String calendarId = importedCalendar.getCalendarId();
					KalendarRenderWrapper calendarWrapper = calendarManager.getImportedCalendar(identity, calendarId);
					calendarWrapper.setDisplayName(importedCalendar.getDisplayName());
					calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
					calendarWrapper.setImported(true);
					CalendarUserConfiguration config = calendarManager.findCalendarConfigForIdentity(calendarWrapper.getKalendar(), identity);
					if (config != null) {
						calendarWrapper.setConfiguration(config);
					}
					calendars.add(calendarWrapper);
				} catch (Exception e) {
					log.error("Cannot read an imported file", e);
				}
			}
			Collections.sort(calendars, KalendarComparator.getInstance());
		}
		return calendars;
	}
	
	public List<CalendarFileInfos> getImportedCalendarInfosForIdentity(Identity identity, boolean reload) {
		List<CalendarFileInfos> calendars = new ArrayList<>();
		if(calendarModule.isEnabled() && calendarModule.isEnablePersonalCalendar()) {
			long timestamp = System.currentTimeMillis();

			List<ImportedCalendar> importedCalendars = importedCalendarDao.getImportedCalendars(identity);
			KalendarEventFilter filter = new KalendarEventFilter(identity, importedCalendars);
			for (ImportedCalendar importedCalendar: importedCalendars) {
				if(reload) {
					reloadImportCalendar(importedCalendar, timestamp, filter);
				}
				String calendarId = importedCalendar.getCalendarId();
				File calendarFile = calendarManager.getCalendarFile(CalendarManager.TYPE_USER, calendarId);
				CalendarFileInfos calendarInfos = new CalendarFileInfos(calendarId, CalendarManager.TYPE_USER, calendarFile);
				calendars.add(calendarInfos);
			}
		}
		return calendars;
	}
	
	private void reloadImportCalendar(ImportedCalendar importedCalendar, long timestamp, KalendarEventFilter filter) {
		String url = importedCalendar.getUrl();
		Date lastUpdate = importedCalendar.getLastUpdate();
		if (url != null && (timestamp - lastUpdate.getTime() > RELOAD_INTERVAL)) {
			log.info("Calendar reload started from url={}", url);
			importedCalendar.setLastUpdate(new Date());
			importedCalendar = importedCalendarDao.update(importedCalendar);
			dbInstance.commit();
			
			String calendarId = importedCalendar.getCalendarId();
			reloadCalendarFromUrl(url, CalendarManager.TYPE_USER, calendarId, filter);
			log.info("Calendar reloaded from url={}", url);
		}
	}

	/**
	 * Reload calendar from url and store calendar file locally.
	 * @param importUrl
	 * @param calType
	 * @param calId
	 */
	private void reloadCalendarFromUrl(String importUrl, String calType, String calId, KalendarEventFilter filter) {
		try (InputStream in = new URL(importUrl).openStream()){
			String targetId = "-" + CalendarManager.TYPE_USER + "-" + calId;
			Kalendar kalendar = calendarManager.buildKalendarFrom(in, calType, calId);
			Collection<KalendarEvent> events = kalendar.getEvents();
			for(KalendarEvent event:events) {
				if(!filter.test(event) || event.getID().contains(targetId)) {
					kalendar.removeEvent(event);
				}
			}
			calendarManager.persistCalendar(kalendar);
		} catch (Exception e) {
		  	log.info("Could not reload calendar from url={}", importUrl, e);
		}
	}
	
	/**
	 * Get ID of a imported calendar 
	 * @param identity
	 * @param calendarName
	 * @return
	 */
	public static String getImportedCalendarID(Identity identity, String calendarName) {
		return identity.getName() + "_" + sanitize(calendarName);
	}

	private static String sanitize(String name) {
		// delete the preceding and trailing whitespaces
		name = name.trim();
		
		// replace every other character other than alphabets and numbers by underscore
		Pattern specialChars = Pattern.compile("([^a-zA-z0-9])");
		return specialChars.matcher(name).replaceAll("_").toLowerCase();
	}
	
	/**
	 * The filter refuse all events where the ID
	 * contains the calendar ID of the personal
	 * calendar of the identity and the calendar IDs
	 * of its imported calendars. It prevents cross
	 * import or recursive import of its own calendars.
	 * 
	 * Initial date: 18 oct. 2017<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static class KalendarEventFilter implements Predicate<KalendarEvent> {
		
		private final String id;
		private final List<String> calendarIds = new ArrayList<>();
		
		public KalendarEventFilter(Identity identity, List<ImportedCalendar> importedCalendars) {
			id = "-" + CalendarManager.TYPE_USER + "-" + identity.getName() + "-";
			if(importedCalendars != null && !importedCalendars.isEmpty()) {
				for(ImportedCalendar importedCalendar:importedCalendars) {
					String calendarId = CalendarManager.TYPE_USER + "-" + importedCalendar.getCalendarId();
					calendarIds.add(calendarId);
				}	
			}
		}

		@Override
		public boolean test(KalendarEvent event) {
			String eventId = event.getID();
			if(eventId == null || eventId.contains(id)) {
				return false;
			}
			for(String calendarId:calendarIds) {
				if(eventId.contains(calendarId)) {
					return false;
				}
			}
			return true;
		}
	}
}
