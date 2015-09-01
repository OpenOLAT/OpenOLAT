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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.model.ImportedCalendar;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportedCalendarDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ImportedCalendarDAO importedCalendarDao;
	
	@Test
	public void createImportedCalendar() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Imp-cal-1");
		String calendarId = UUID.randomUUID().toString();
		String type = "imported-test";
		String url = "http://www.openolat.org/calendar.ics";
		
		ImportedCalendar importedCalendar = importedCalendarDao
				.createImportedCalendar(user, "Imported calendar", calendarId, type, url, new Date());
		Assert.assertNotNull(importedCalendar);
		dbInstance.commit();
		
		Assert.assertNotNull(importedCalendar.getKey());
		Assert.assertNotNull(importedCalendar.getCreationDate());
		Assert.assertNotNull(importedCalendar.getLastModified());
		Assert.assertNotNull(importedCalendar.getLastUpdate());
		
		Assert.assertEquals("Imported calendar", importedCalendar.getDisplayName());
		Assert.assertEquals(user, importedCalendar.getIdentity());
		Assert.assertEquals(calendarId, importedCalendar.getCalendarId());
		Assert.assertEquals(type, importedCalendar.getType());
		Assert.assertEquals(url, importedCalendar.getUrl());
	}
	
	@Test
	public void getImportedCalendar() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Imp-cal-2");
		String calendarId = UUID.randomUUID().toString();
		String type = "imported-test-2";
		String url = "http://www.openolat.org/calendar1.ics";
		
		ImportedCalendar importedCalendar = importedCalendarDao
				.createImportedCalendar(user, "Imported calendar 2", calendarId, type, url, new Date());
		Assert.assertNotNull(importedCalendar);
		dbInstance.commit();
		
		List<ImportedCalendar> calendars = importedCalendarDao.getImportedCalendar(user, calendarId, type);
		Assert.assertNotNull(calendars);
		Assert.assertEquals(1, calendars.size());
		Assert.assertTrue(calendars.contains(importedCalendar));
		
		//paranoia check
		ImportedCalendar reloadedCalendar = calendars.get(0);
		Assert.assertNotNull(reloadedCalendar.getCreationDate());
		Assert.assertNotNull(reloadedCalendar.getLastModified());
		Assert.assertNotNull(reloadedCalendar.getLastUpdate());

		Assert.assertEquals(importedCalendar.getKey(), reloadedCalendar.getKey());
		Assert.assertEquals("Imported calendar 2", reloadedCalendar.getDisplayName());
		Assert.assertEquals(user, reloadedCalendar.getIdentity());
		Assert.assertEquals(calendarId, reloadedCalendar.getCalendarId());
		Assert.assertEquals(type, reloadedCalendar.getType());
		Assert.assertEquals(url, reloadedCalendar.getUrl());
	}
	
	@Test
	public void updateImportedCalendar() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Imp-cal-1");
		String calendarId = UUID.randomUUID().toString();
		String type = "imported-test";
		String url = "http://www.openolat.org/calendar.ics";
		
		ImportedCalendar importedCalendar = importedCalendarDao
				.createImportedCalendar(user, "Imported calendar up", calendarId, type, url, new Date());
		Assert.assertNotNull(importedCalendar);
		dbInstance.commit();
		
		//update
		importedCalendar.setDisplayName("Imported calendar updated");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		importedCalendar.setLastUpdate(cal.getTime());
		ImportedCalendar updatedCalendar = importedCalendarDao.update(importedCalendar);
		dbInstance.commit();
		
		//check
		Assert.assertEquals(importedCalendar, updatedCalendar);
		Assert.assertEquals(importedCalendar.getKey(), updatedCalendar.getKey());

		Assert.assertTrue(new Date().before(updatedCalendar.getLastUpdate()));
		Assert.assertEquals("Imported calendar updated", updatedCalendar.getDisplayName());
		Assert.assertEquals(user, updatedCalendar.getIdentity());
		Assert.assertEquals(calendarId, updatedCalendar.getCalendarId());
		Assert.assertEquals(type, updatedCalendar.getType());
		Assert.assertEquals(url, updatedCalendar.getUrl());
		
		//reload and check
		List<ImportedCalendar> reloadedCalendars = importedCalendarDao.getImportedCalendar(user, calendarId, type);
		Assert.assertNotNull(reloadedCalendars);
		Assert.assertEquals(1, reloadedCalendars.size());
		Assert.assertTrue(reloadedCalendars.contains(importedCalendar));
		
		//paranoia check
		ImportedCalendar reloadedCalendar = reloadedCalendars.get(0);
		Assert.assertNotNull(reloadedCalendar.getCreationDate());
		Assert.assertNotNull(reloadedCalendar.getLastModified());
		Assert.assertTrue(new Date().before(reloadedCalendar.getLastUpdate()));

		Assert.assertEquals(importedCalendar.getKey(), reloadedCalendar.getKey());
		Assert.assertEquals("Imported calendar updated", reloadedCalendar.getDisplayName());
		Assert.assertEquals(user, reloadedCalendar.getIdentity());
		Assert.assertEquals(calendarId, reloadedCalendar.getCalendarId());
		Assert.assertEquals(type, reloadedCalendar.getType());
		Assert.assertEquals(url, reloadedCalendar.getUrl());
	}
	
	@Test
	public void getImportedCalendars() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Imp-cal-4");
		String calendarId = UUID.randomUUID().toString();
		ImportedCalendar importedCalendar1 = importedCalendarDao
				.createImportedCalendar(user, "Imported calendar 3", calendarId, "rnd-3", null, new Date());
		ImportedCalendar importedCalendar2 = importedCalendarDao
				.createImportedCalendar(user, "Imported calendar 4", UUID.randomUUID().toString(), "rnd-4",
						"http://www.openolat.org/calendar4.ics", new Date());
		
		//noise
		Identity noiseUser = JunitTestHelper.createAndPersistIdentityAsRndUser("Imp-cal-4");
		ImportedCalendar noiseImportedCalendar = importedCalendarDao
				.createImportedCalendar(noiseUser, "Imported calendar 3", calendarId, "rnd-3", null, new Date());
		
		dbInstance.commit();
		
		List<ImportedCalendar> importedCalendars = importedCalendarDao.getImportedCalendars(user);
		Assert.assertNotNull(importedCalendars);
		Assert.assertEquals(2, importedCalendars.size());
		Assert.assertTrue(importedCalendars.contains(importedCalendar1));
		Assert.assertTrue(importedCalendars.contains(importedCalendar2));
		Assert.assertFalse(importedCalendars.contains(noiseImportedCalendar));
	}
	
	@Test
	public void deleteImportedCalendar() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Imp-cal-5");
		String calendarId = UUID.randomUUID().toString();
		ImportedCalendar importedCalendar1 = importedCalendarDao
				.createImportedCalendar(user, "Imported calendar 5", calendarId, "rnd-3", null, new Date());
		ImportedCalendar importedCalendar2 = importedCalendarDao
				.createImportedCalendar(user, "Imported calendar 6", UUID.randomUUID().toString(), "rnd-4",
						"http://www.openolat.org/calendar5.ics", new Date());
		
		//noise
		Identity noiseUser = JunitTestHelper.createAndPersistIdentityAsRndUser("Imp-cal-6");
		ImportedCalendar noiseImportedCalendar = importedCalendarDao
				.createImportedCalendar(noiseUser, "Imported calendar 7", calendarId, "rnd-3", null, new Date());
		
		dbInstance.commit();
		
		//check
		List<ImportedCalendar> importedCalendars = importedCalendarDao.getImportedCalendars(user);
		Assert.assertEquals(2, importedCalendars.size());
		List<ImportedCalendar> noiseImportedCalendars = importedCalendarDao.getImportedCalendars(noiseUser);
		Assert.assertEquals(1, noiseImportedCalendars.size());
		
		//delete
		importedCalendarDao.deleteImportedCalendar(user, calendarId, "rnd-3");
		dbInstance.commitAndCloseSession();
		
		//check the the first imported calendar is really deleted
		List<ImportedCalendar> reloadedCalendars = importedCalendarDao.getImportedCalendars(user);
		Assert.assertEquals(1, reloadedCalendars.size());
		Assert.assertFalse(reloadedCalendars.contains(importedCalendar1));
		Assert.assertTrue(reloadedCalendars.contains(importedCalendar2));
		Assert.assertFalse(reloadedCalendars.contains(noiseImportedCalendar));
		
		//noise must still have its calendar
		List<ImportedCalendar> noiseReloadedCalendars = importedCalendarDao.getImportedCalendars(noiseUser);
		Assert.assertEquals(1, noiseReloadedCalendars.size());
		Assert.assertFalse(noiseReloadedCalendars.contains(importedCalendar1));
		Assert.assertFalse(noiseReloadedCalendars.contains(importedCalendar2));
		Assert.assertTrue(noiseReloadedCalendars.contains(noiseImportedCalendar));
	}
}
