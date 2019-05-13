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
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
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

	private AtomicInteger counter = new AtomicInteger(0);
	private static final Logger log = Tracing.createLoggerFor(ImportToCalendarManager.class);

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private ImportedToCalendarDAO importedToCalendarDao;
	
	/**
	 * Method used by the cron job
	 * @return
	 */
	public boolean updateCalendarIn() {
		List<ImportedToCalendar> importedToCalendars = importedToCalendarDao.getImportedToCalendars();
		log.info(Tracing.M_AUDIT, "Begin to update " + importedToCalendars.size() + " calendars.");
		
		//make a full check only every 10 runs
		boolean check = counter.incrementAndGet() % 10 == 0;

		int count = 0;
		for(ImportedToCalendar importedToCalendar:importedToCalendars) {
			String type = importedToCalendar.getToType();
			String id = importedToCalendar.getToCalendarId();
			String importUrl = importedToCalendar.getUrl();
			if(check || check(importedToCalendar)) {
				try(InputStream in = new URL(importUrl).openStream()) {
					Kalendar cal = calendarManager.getCalendar(type, id);
					if(calendarManager.synchronizeCalendarFrom(in, importUrl, cal)) {
						log.info(Tracing.M_AUDIT, "Updated successfully calendar: " + type + " / " + id);
					} else {
						log.info(Tracing.M_AUDIT, "Failed to update calendar: " + type + " / " + id);
					}
				} catch(Exception ex) {
					log.warn("Cannot synchronize calendar (" + importedToCalendar.getKey() + ") from url: " + importUrl, ex);
				}
			} else {
				log.info(Tracing.M_AUDIT, "Delete imported calendar because of missing resource: " + type + " " + id + " with URL: " + importUrl);
				deleteImportedCalendars(type, id);
			}
			
			if(count++ % 20 == 0) {
				DBFactory.getInstance().commit();
				
				try {
					Thread.sleep(1000);// sleep to don't overload the system
				} catch (InterruptedException e) {
					log.error("", e);
				}
			}
		}
		return false;
	}
	
	
	private boolean check(ImportedToCalendar importedToCalendar) {
		String id = importedToCalendar.getToCalendarId();
		String type = importedToCalendar.getToType();
		if(CalendarManager.TYPE_USER.equals(type)) {
			Identity identity = securityManager.findIdentityByNameCaseInsensitive(id);
			return identity != null && identity.getStatus() != null && identity.getStatus().intValue() < Identity.STATUS_DELETED;
		}
		if(CalendarManager.TYPE_COURSE.equals(type)) {
			Long resourceId = new Long(id);
			RepositoryEntry entry = repositoryEntryDao.loadByResourceId("CourseModule", resourceId);
			return entry != null;
		}
		if(CalendarManager.TYPE_GROUP.equals(type)) {
			Long resourceId = new Long(id);
			BusinessGroup group = businessGroupDao.loadByResourceId(resourceId);
			return group != null;
		}
		return true;
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
	
	public void deletePersonalImportedCalendars(Identity identity) {
		deleteImportedCalendars(CalendarManager.TYPE_USER, identity.getName());
	}
	
	public void deleteGroupImportedCalendars(BusinessGroup businessGroup) {
		deleteImportedCalendars(CalendarManager.TYPE_GROUP, businessGroup.getResourceableId().toString());
	}
	
	public void deleteCourseImportedCalendars(OLATResourceable course) {
		deleteImportedCalendars(CalendarManager.TYPE_COURSE, course.getResourceableId().toString());
	}

	private void deleteImportedCalendars(String type, String id) {
		List<ImportedToCalendar> importedToCalendars = importedToCalendarDao.getImportedToCalendars(id, type);
		for(ImportedToCalendar importedToCalendar:importedToCalendars) {
			importedToCalendarDao.delete(importedToCalendar);	
		}
	}
}
