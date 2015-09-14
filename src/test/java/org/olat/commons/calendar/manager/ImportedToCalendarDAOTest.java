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
package org.olat.commons.calendar.manager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportedToCalendarDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ImportedToCalendarDAO importedToCalendarDao;
	
	@Test
	public void createImportedToCalendar() {
		String toCalendarId = UUID.randomUUID().toString();
		String toType = "to-rnd-1";
		String url = "http://www.openolat.org/importedto/calendar1.ics";
		ImportedToCalendar importToCalendar = importedToCalendarDao
				.createImportedToCalendar(toCalendarId, toType, url, new Date());
		dbInstance.commit();
		
		Assert.assertNotNull(importToCalendar);
		Assert.assertNotNull(importToCalendar.getKey());
		Assert.assertNotNull(importToCalendar.getCreationDate());
		Assert.assertNotNull(importToCalendar.getLastModified());
		Assert.assertNotNull(importToCalendar.getLastUpdate());
		
		Assert.assertEquals(toCalendarId, importToCalendar.getToCalendarId());
		Assert.assertEquals(toType, importToCalendar.getToType());
		Assert.assertEquals(url, importToCalendar.getUrl());
	}
	
	@Test
	public void getImportedToCalendars_byCalendarIdTypeAndUrl() {
		String toCalendarId = UUID.randomUUID().toString();
		String toType = "to-rnd-2";
		String url = "http://www.openolat.org/importedto/calendar2.ics";
		ImportedToCalendar importToCalendar = importedToCalendarDao
				.createImportedToCalendar(toCalendarId, toType, url, new Date());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(importToCalendar);
		
		List<ImportedToCalendar> loadedToCalendars = importedToCalendarDao
				.getImportedToCalendars(toCalendarId, toType, url);
		Assert.assertNotNull(loadedToCalendars);
		Assert.assertEquals(1, loadedToCalendars.size());
		Assert.assertTrue(loadedToCalendars.contains(importToCalendar));
		
		//paranoia check
		ImportedToCalendar loadedToCalendar = loadedToCalendars.get(0);
		Assert.assertEquals(importToCalendar, loadedToCalendar);
		Assert.assertEquals(importToCalendar.getKey(), loadedToCalendar.getKey());
		Assert.assertNotNull(loadedToCalendar.getCreationDate());
		Assert.assertNotNull(loadedToCalendar.getLastModified());
		Assert.assertNotNull(loadedToCalendar.getLastUpdate());
		
		Assert.assertEquals(toCalendarId, loadedToCalendar.getToCalendarId());
		Assert.assertEquals(toType, loadedToCalendar.getToType());
		Assert.assertEquals(url, loadedToCalendar.getUrl());
	}

	@Test
	public void getImportedToCalendars() {
		String toCalendarId = UUID.randomUUID().toString();
		String toType = "to-rnd-3";
		String url = "http://www.openolat.org/importedto/calendar3.ics";
		ImportedToCalendar importToCalendar = importedToCalendarDao
				.createImportedToCalendar(toCalendarId, toType, url, new Date());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(importToCalendar);
		
		//load all calendars
		List<ImportedToCalendar> allCalendars = importedToCalendarDao.getImportedToCalendars();
		Assert.assertNotNull(allCalendars);
		Assert.assertFalse(allCalendars.isEmpty());
		Assert.assertTrue(allCalendars.contains(importToCalendar));	
	}
}
