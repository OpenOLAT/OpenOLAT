package org.olat.modules.lecture.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
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

}
