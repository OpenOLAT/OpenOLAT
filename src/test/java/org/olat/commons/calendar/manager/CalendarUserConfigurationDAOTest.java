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

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarUserConfigurationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarUserConfigurationDAO calendarDao;
	
	@Test
	public void createConfiguration() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Cal-1");
		String calendarId = UUID.randomUUID().toString();
		Kalendar calendar = new Kalendar(calendarId, CalendarManager.TYPE_USER);
		
		CalendarUserConfiguration config = calendarDao.createCalendarUserConfiguration(calendar, user);
		dbInstance.commit();
		Assert.assertNotNull(config);
		Assert.assertNotNull(config.getKey());
		Assert.assertNotNull(config.getCreationDate());
		Assert.assertNotNull(config.getLastModified());
		Assert.assertEquals(calendarId, config.getCalendarId());
		Assert.assertEquals(CalendarManager.TYPE_USER, config.getType());
		Assert.assertEquals(user, config.getIdentity());
	}
	
	@Test
	public void getCalendarUserConfigurations() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Cal-2");
		String calendarId = UUID.randomUUID().toString();
		Kalendar calendar = new Kalendar(calendarId, CalendarManager.TYPE_COURSE);
		
		CalendarUserConfiguration config = calendarDao.createCalendarUserConfiguration(calendar, user);
		dbInstance.commit();
		Assert.assertNotNull(config);
		
		//retrieve
		List<CalendarUserConfiguration> configList = calendarDao.getCalendarUserConfigurations(user);
		Assert.assertNotNull(configList);
		Assert.assertEquals(1, configList.size());
		Assert.assertEquals(config, configList.get(0));
		//paranoia check
		CalendarUserConfiguration loadedConfig = configList.get(0);
		Assert.assertNotNull(loadedConfig.getCreationDate());
		Assert.assertNotNull(loadedConfig.getLastModified());
		Assert.assertEquals(config.getKey(), loadedConfig.getKey());
		Assert.assertEquals(calendarId, loadedConfig.getCalendarId());
		Assert.assertEquals(CalendarManager.TYPE_COURSE, loadedConfig.getType());
		Assert.assertEquals(user, loadedConfig.getIdentity());
		Assert.assertTrue(loadedConfig.isVisible());
		Assert.assertTrue(loadedConfig.isInAggregatedFeed());
	}
	
	@Test
	public void getCalendarUserConfigurations_byTypes() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("Cal-3");
		String calendarId = UUID.randomUUID().toString();
		
		Kalendar courseCalendar = new Kalendar(calendarId, CalendarManager.TYPE_COURSE);
		CalendarUserConfiguration courseCalConfig = calendarDao.createCalendarUserConfiguration(courseCalendar, user);
		Kalendar groupCalendar = new Kalendar(calendarId, CalendarManager.TYPE_GROUP);
		CalendarUserConfiguration groupCalConfig = calendarDao.createCalendarUserConfiguration(groupCalendar, user);	
		Kalendar personalCalendar = new Kalendar(user.getName(), CalendarManager.TYPE_USER);
		CalendarUserConfiguration personalCalConfig = calendarDao.createCalendarUserConfiguration(personalCalendar, user);
		dbInstance.commit();
		Assert.assertNotNull(courseCalConfig);
		
		//get all
		List<CalendarUserConfiguration> configList = calendarDao.getCalendarUserConfigurations(user);
		Assert.assertNotNull(configList);
		Assert.assertEquals(3, configList.size());
		Assert.assertTrue(configList.contains(courseCalConfig));
		Assert.assertTrue(configList.contains(groupCalConfig));
		Assert.assertTrue(configList.contains(personalCalConfig));
		
		//get course
		List<CalendarUserConfiguration> courseConfigList = calendarDao.getCalendarUserConfigurations(user, CalendarManager.TYPE_COURSE);
		Assert.assertNotNull(courseConfigList);
		Assert.assertEquals(1, courseConfigList.size());
		Assert.assertTrue(courseConfigList.contains(courseCalConfig));
		
		//null check
		List<CalendarUserConfiguration> nullConfigList = calendarDao.getCalendarUserConfigurations(user, (String)null);
		Assert.assertNotNull(nullConfigList);
		Assert.assertEquals(3, nullConfigList.size());
	}
	

}
