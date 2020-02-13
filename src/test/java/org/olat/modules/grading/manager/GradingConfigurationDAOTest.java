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
package org.olat.modules.grading.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.grading.GradingAssessedIdentityVisibility;
import org.olat.modules.grading.GradingNotificationType;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingConfigurationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GradingConfigurationDAO gradingConfigurationDao;
	
	@Test
	public void createConfiguration() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		Assert.assertNotNull(config.getCreationDate());
		Assert.assertNotNull(config.getCreationDate());
		Assert.assertEquals(entry, config.getEntry());
		Assert.assertEquals(GradingAssessedIdentityVisibility.anonymous, config.getIdentityVisibilityEnum());
		Assert.assertEquals(GradingNotificationType.afterTestSubmission, config.getNotificationTypeEnum());
		Assert.assertFalse(config.isGradingEnabled());
		dbInstance.commit();
	}
	
	@Test
	public void updateConfiguration() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		dbInstance.commitAndCloseSession();
		
		config.setGradingEnabled(true);
		config.setGradingPeriod(10);
		config.setIdentityVisibilityEnum(GradingAssessedIdentityVisibility.nameVisible);
		config.setNotificationTypeEnum(GradingNotificationType.onceDay);
		config.setNotificationSubject("Notification subject");
		config.setNotificationBody("Notification body");
		config.setFirstReminder(8);
		config.setFirstReminderSubject("First subject");
		config.setFirstReminderBody("First body");
		config.setSecondReminder(6);
		config.setSecondReminderSubject("Second subject");
		config.setSecondReminderBody("Second body");
		
		config = gradingConfigurationDao.updateConfiguration(config);
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals(entry, config.getEntry());
		Assert.assertTrue(config.isGradingEnabled());
		Assert.assertEquals(Integer.valueOf(10), config.getGradingPeriod());
		Assert.assertEquals(GradingAssessedIdentityVisibility.nameVisible, config.getIdentityVisibilityEnum());
		Assert.assertEquals(GradingNotificationType.onceDay, config.getNotificationTypeEnum());
		Assert.assertEquals("Notification subject", config.getNotificationSubject());
		Assert.assertEquals("Notification body", config.getNotificationBody());
		Assert.assertEquals(Integer.valueOf(8), config.getFirstReminder());
		Assert.assertEquals("First subject", config.getFirstReminderSubject());
		Assert.assertEquals("First body", config.getFirstReminderBody());
		Assert.assertEquals(Integer.valueOf(6), config.getSecondReminder());
		Assert.assertEquals("Second subject", config.getSecondReminderSubject());
		Assert.assertEquals("Second body", config.getSecondReminderBody());
	}
	
	@Test
	public void reloadConfiguration() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		dbInstance.commitAndCloseSession();
		
		config.setGradingEnabled(true);
		config.setGradingPeriod(10);
		config.setIdentityVisibilityEnum(GradingAssessedIdentityVisibility.nameVisible);
		config.setNotificationTypeEnum(GradingNotificationType.onceDay);
		config.setNotificationSubject("The notification subject");
		config.setNotificationBody("The notification body");
		config.setFirstReminder(7);
		config.setFirstReminderSubject("My first subject");
		config.setFirstReminderBody("My first body");
		config.setSecondReminder(9);
		config.setSecondReminderSubject("My second subject");
		config.setSecondReminderBody("My second body");
		
		config = gradingConfigurationDao.updateConfiguration(config);
		dbInstance.commitAndCloseSession();
		RepositoryEntryGradingConfiguration reloadConfig = gradingConfigurationDao.getConfiguration(entry);

		Assert.assertEquals(entry, reloadConfig.getEntry());
		Assert.assertTrue(reloadConfig.isGradingEnabled());
		Assert.assertEquals(Integer.valueOf(10), reloadConfig.getGradingPeriod());
		Assert.assertEquals(GradingAssessedIdentityVisibility.nameVisible, reloadConfig.getIdentityVisibilityEnum());
		Assert.assertEquals(GradingNotificationType.onceDay, reloadConfig.getNotificationTypeEnum());
		Assert.assertEquals("The notification subject", reloadConfig.getNotificationSubject());
		Assert.assertEquals("The notification body", reloadConfig.getNotificationBody());
		Assert.assertEquals(Integer.valueOf(7), reloadConfig.getFirstReminder());
		Assert.assertEquals("My first subject", reloadConfig.getFirstReminderSubject());
		Assert.assertEquals("My first body", reloadConfig.getFirstReminderBody());
		Assert.assertEquals(Integer.valueOf(9), reloadConfig.getSecondReminder());
		Assert.assertEquals("My second subject", reloadConfig.getSecondReminderSubject());
		Assert.assertEquals("My second body", reloadConfig.getSecondReminderBody());
	}
	
	@Test
	public void getConfiguration_byEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-4");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntryGradingConfiguration> reloadedConfigs = gradingConfigurationDao.getConfiguration(entry, null);
		Assert.assertNotNull(reloadedConfigs);
		Assert.assertEquals(1, reloadedConfigs.size());
		Assert.assertEquals(config, reloadedConfigs.get(0));
	}
	
	@Test
	public void getConfiguration_bySoftKey() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-5");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntryGradingConfiguration> reloadedConfigs = gradingConfigurationDao.getConfiguration(null, entry.getSoftkey());
		Assert.assertNotNull(reloadedConfigs);
		Assert.assertEquals(1, reloadedConfigs.size());
		Assert.assertEquals(config, reloadedConfigs.get(0));
	}
	
	@Test
	public void getConfiguration_byBoth() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-5");
		RepositoryEntry entry1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry2 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntryGradingConfiguration config1 = gradingConfigurationDao.createConfiguration(entry1);
		RepositoryEntryGradingConfiguration config2 = gradingConfigurationDao.createConfiguration(entry2);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(config1);
		Assert.assertNotNull(config2);
		
		List<RepositoryEntryGradingConfiguration> reloadedConfigs = gradingConfigurationDao.getConfiguration(entry1, entry2.getSoftkey());
		Assert.assertNotNull(reloadedConfigs);
		Assert.assertTrue(reloadedConfigs.isEmpty());
	}
	
	@Test
	public void getIdentityVisibility() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-5");
		RepositoryEntry entryAnonymous = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entryNameVisible = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		
		RepositoryEntryGradingConfiguration configAnonymous = gradingConfigurationDao.createConfiguration(entryAnonymous);
		RepositoryEntryGradingConfiguration configNameVisible = gradingConfigurationDao.createConfiguration(entryNameVisible);
		dbInstance.commit();
		
		configAnonymous.setGradingEnabled(true);
		configAnonymous.setIdentityVisibilityEnum(GradingAssessedIdentityVisibility.anonymous);
		gradingConfigurationDao.updateConfiguration(configAnonymous);
		configNameVisible.setGradingEnabled(true);
		configNameVisible.setIdentityVisibilityEnum(GradingAssessedIdentityVisibility.nameVisible);
		gradingConfigurationDao.updateConfiguration(configNameVisible);
		dbInstance.commit();
		
		List<RepositoryEntryRef> entries = new ArrayList<>();
		entries.add(entry);
		entries.add(entryNameVisible);
		entries.add(entryAnonymous);
		Map<Long,GradingAssessedIdentityVisibility> configMap = gradingConfigurationDao.getIdentityVisibility(entries);
		Assert.assertNotNull(configMap);
		Assert.assertEquals(GradingAssessedIdentityVisibility.anonymous, configMap.get(entryAnonymous.getKey()));
		Assert.assertEquals(GradingAssessedIdentityVisibility.nameVisible, configMap.get(entryNameVisible.getKey()));
	}
}
