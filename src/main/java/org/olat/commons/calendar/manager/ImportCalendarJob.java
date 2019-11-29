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
import java.net.URLConnection;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date:  21 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportCalendarJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(ImportCalendarJob.class);
	
	private static final Random random = new Random();
	
	@Override
	public void executeWithDB(JobExecutionContext context) {
		try {
			Scheduler scheduler = CoreSpringFactory.getImpl(Scheduler.class);
			jitter(scheduler);
			if(!scheduler.isShutdown()) {
				updateCalendarIn(scheduler);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private boolean updateCalendarIn(Scheduler scheduler) {
		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		ImportToCalendarManager importToCalendarManager = CoreSpringFactory.getImpl(ImportToCalendarManager.class);
		
		List<ImportedToCalendar> importedToCalendars = importToCalendarManager.getImportedToCalendars();
		log.info(Tracing.M_AUDIT, "Begin to update {} calendars.", importedToCalendars.size() );
		
		//make a full check only every 10 runs
		boolean check = importToCalendarManager.check();

		for(ImportedToCalendar importedToCalendar:importedToCalendars) {
			String type = importedToCalendar.getToType();
			String id = importedToCalendar.getToCalendarId();
			String importUrl = importedToCalendar.getUrl();
			if(check || importToCalendarManager.check(importedToCalendar)) {
				URLConnection connection = calendarManager.getURLConnection(importUrl);
				if(connection != null) {
					try(InputStream in = connection.getInputStream()) {
						Kalendar cal = calendarManager.getCalendar(type, id);
						if(calendarManager.synchronizeCalendarFrom(in, importUrl, cal)) {
							log.info(Tracing.M_AUDIT, "Updated successfully calendar: {} / {}", type, id);
						} else {
							log.info(Tracing.M_AUDIT, "Failed to update calendar: {} / {}", type, id);
						}
					} catch(Exception ex) {
						log.warn("Cannot synchronize calendar ({}) from url: {}", importedToCalendar.getKey() , importUrl, ex);
					}
				}
			} else {
				log.info(Tracing.M_AUDIT, "Delete imported calendar because of missing resource: {} / {} with URL: {}", type, id, importUrl);
				importToCalendarManager.deleteImportedCalendars(type, id);
			}
			
			DBFactory.getInstance().commit();
	
			try {
				if(scheduler.isShutdown()) {
					return false;
				}
				Thread.sleep(1000);// sleep to don't overload the system
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return false;
	}
	
	private void jitter(Scheduler scheduler)  {
		try {
			double numOfWaitingLoops =  random.nextDouble() * 180.0d;
			long wait = Math.round(numOfWaitingLoops);
			for(int i=0; i<wait; i++) {
				if(scheduler.isShutdown()) {
					return;
				}
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
}
