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

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Description:<BR>
 * Constants and helper methods for the OLAT iCal feeds
 * 
 * <P>
 * Initial Date:  July 22, 2008
 *
 * @author Udit Sajjanhar
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class ImportToCalendarManager {
	
	private static final OLog log = Tracing.createLoggerFor(ImportToCalendarManager.class);

	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private ImportedToCalendarDAO importedToCalendarDao;
	
	/**
	 * Method used by the cron job
	 * @return
	 */
	public boolean updateCalendarIn() {
		List<ImportedToCalendar> importedToCalendars = importedToCalendarDao.getImportedToCalendars();
		log.audit("Begin to update " + importedToCalendars.size() + " calendars.");
		
		int count = 0;
		for(ImportedToCalendar importedToCalendar:importedToCalendars) {
			String type = importedToCalendar.getToType();
			String id = importedToCalendar.getToCalendarId();
			String importUrl = importedToCalendar.getUrl();
	
			Kalendar cal = calendarManager.getCalendar(type, id);
			try(InputStream in = new URL(importUrl).openStream()) {
				if(calendarManager.synchronizeCalendarFrom(in, importUrl, cal)) {
					log.audit("Updated successfully calendar: " + type + " / " + id);
				} else {
					log.audit("Failed to update calendar: " + type + " / " + id);
				}
			} catch(Exception ex) {
				log.error("Cannot synchronize calendar from url: " + importUrl, ex);
			}
			
			if(count++ % 20 == 0) {
				DBFactory.getInstance().commit();
			}
		}
		return false;
	}
	
	/**
	 * Append the stream of events to the specified calendar.
	 * 
	 * @param calenderWrapper The target calendar.
	 * @param in An iCal stream.
	 * @return true if successfully imported
	 */
	public boolean importCalendarIn(KalendarRenderWrapper calenderWrapper, InputStream in) {
		try {
			Kalendar cal = calenderWrapper.getKalendar();
			Kalendar importedCal = calendarManager.buildKalendarFrom(in, cal.getType(), cal.getCalendarID());
			return calendarManager.updateCalendar(cal, importedCal);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	/**
	 * Import an external calendar.
	 * 
	 * @param cal
	 * @param importUrl
	 * @return
	 */
	public boolean importCalendarIn(Kalendar cal, String importUrl) {
		try (InputStream in = new URL(importUrl).openStream()){
			Kalendar importedCal = calendarManager.buildKalendarFrom(in, cal.getType(), cal.getCalendarID());
			boolean imported = calendarManager.updateCalendar(cal, importedCal);
			if(imported) {
				List<ImportedToCalendar> importedToCalendars = importedToCalendarDao.getImportedToCalendars(cal.getCalendarID(), cal.getType(), importUrl);
				if(importedToCalendars.isEmpty()) {
					importedToCalendarDao.createImportedToCalendar(cal.getCalendarID(), cal.getType(), importUrl, new Date());
				} else {
					ImportedToCalendar importedToCalendar = importedToCalendars.get(0);
					importedToCalendar.setLastUpdate(new Date());
					importedToCalendar = importedToCalendarDao.update(importedToCalendar);
				}
			}
			return imported;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
}
