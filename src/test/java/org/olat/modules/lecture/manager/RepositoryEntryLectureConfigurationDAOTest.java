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
package org.olat.modules.lecture.manager;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryLectureConfigurationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private RepositoryEntryLectureConfigurationDAO lectureConfigurationDao;
	
	@Test
	public void createLectureConfiguration() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.createConfiguration(entry);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(config);
		Assert.assertNotNull(config.getKey());
		Assert.assertNotNull(config.getCreationDate());
		Assert.assertNotNull(config.getLastModified());
		Assert.assertEquals(entry, config.getEntry());
		Assert.assertFalse(config.isLectureEnabled());
		Assert.assertFalse(config.isOverrideModuleDefault());
	}
	
	@Test
	public void createAndConfigureLectureConfiguration() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.createConfiguration(entry);
		dbInstance.commitAndCloseSession();

		config.setLectureEnabled(true);
		config.setOverrideModuleDefault(true);
		config.setCalculateAttendanceRate(Boolean.TRUE);
		config.setRequiredAttendanceRate(0.75d);
		config.setParticipantCalendarSyncEnabled(Boolean.TRUE);
		config.setTeacherCalendarSyncEnabled(Boolean.TRUE);
		
		RepositoryEntryLectureConfiguration mergedConfig = lectureConfigurationDao.update(config);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryLectureConfiguration reloadedConfig = lectureConfigurationDao.getConfiguration(entry);
		Assert.assertEquals(config, reloadedConfig);
		Assert.assertEquals(mergedConfig, reloadedConfig);
		Assert.assertEquals(true, reloadedConfig.isLectureEnabled());
		Assert.assertEquals(true, reloadedConfig.isOverrideModuleDefault());
		Assert.assertEquals(Boolean.TRUE, reloadedConfig.getCalculateAttendanceRate());
		Assert.assertEquals(0.75d, reloadedConfig.getRequiredAttendanceRate(), 0.0001);
		Assert.assertEquals(Boolean.TRUE, reloadedConfig.getParticipantCalendarSyncEnabled());
		Assert.assertEquals(Boolean.TRUE, reloadedConfig.getTeacherCalendarSyncEnabled());
	}
	
	@Test
	public void getRepositoryEntryLectureConfiguration_lectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.createConfiguration(entry);
		dbInstance.commit();
		
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		//get the configuration
		RepositoryEntryLectureConfiguration reloadedConfig = lectureConfigurationDao.getConfiguration(lectureBlock);
		Assert.assertEquals(config, reloadedConfig);
	}
	
	@Test
	public void cloneConfigureLectureConfiguration() {
		//create a configuration
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry cloneEntry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.createConfiguration(entry);
		config.setLectureEnabled(true);
		config.setOverrideModuleDefault(true);
		config.setCalculateAttendanceRate(Boolean.TRUE);
		config.setRequiredAttendanceRate(0.75d);
		config.setParticipantCalendarSyncEnabled(Boolean.TRUE);
		config.setTeacherCalendarSyncEnabled(Boolean.TRUE);
		RepositoryEntryLectureConfiguration mergedConfig = lectureConfigurationDao.update(config);
		dbInstance.commitAndCloseSession();
		
		//clone it
		RepositoryEntryLectureConfiguration clonedConfig = lectureConfigurationDao.cloneConfiguration(mergedConfig, cloneEntry);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(clonedConfig);
		
		
		RepositoryEntryLectureConfiguration reloadedClonedConfig = lectureConfigurationDao.getConfiguration(cloneEntry);
		Assert.assertEquals(clonedConfig, reloadedClonedConfig);
		Assert.assertEquals(true, reloadedClonedConfig.isLectureEnabled());
		Assert.assertEquals(true, reloadedClonedConfig.isOverrideModuleDefault());
		Assert.assertEquals(Boolean.TRUE, reloadedClonedConfig.getCalculateAttendanceRate());
		Assert.assertEquals(0.75d, reloadedClonedConfig.getRequiredAttendanceRate(), 0.0001);
		Assert.assertEquals(Boolean.TRUE, reloadedClonedConfig.getParticipantCalendarSyncEnabled());
		Assert.assertEquals(Boolean.TRUE, reloadedClonedConfig.getTeacherCalendarSyncEnabled());
	}
	
	@Test
	public void isConfigurationEnabledFor() {
		//create a configuration
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.createConfiguration(entry);
		config.setLectureEnabled(true);
		RepositoryEntryLectureConfiguration mergedConfig = lectureConfigurationDao.update(config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mergedConfig);
		
		//check that the configuration enables the lectures feature
		boolean enabled = lectureConfigurationDao.isConfigurationEnabledFor(entry);
		Assert.assertTrue(enabled);
	}
	
	@Test
	public void deleteConfiguration_byRepositoryEntry() {
		//create a configuration
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryLectureConfiguration config = lectureConfigurationDao.createConfiguration(entry);
		config.setLectureEnabled(true);
		RepositoryEntryLectureConfiguration mergedConfig = lectureConfigurationDao.update(config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(mergedConfig);
		
		//delete the configuration
		lectureConfigurationDao.deleteConfiguration(entry);
		dbInstance.commitAndCloseSession();
		
		//check that the configuration is really deleted
		RepositoryEntryLectureConfiguration deletedConfig = lectureConfigurationDao.getConfiguration(entry);
		Assert.assertNull(deletedConfig);
	}
}
