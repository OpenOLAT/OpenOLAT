/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.RecruitingAuditLogUserSettings;

/**
 * 
 * 
 * Initial date: 23 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingAuditLogUserSettingsDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingAuditLogUserSettingsDAO recruitingAuditLogUserSettingsDAO;
	
	@Test
	public void create() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("settings-1");
		RecruitingAuditLogUserSettings settings = recruitingAuditLogUserSettingsDAO.createAndPersist(id);
		dbInstance.commit();
		
		Assert.assertNotNull(settings);
		Assert.assertNotNull(settings.getKey());
		Assert.assertNotNull(settings.getCreationDate());
		Assert.assertEquals(id, settings.getIdentity());
		Assert.assertFalse(settings.isEnabled());
		Assert.assertNotNull(settings.getInterval());
	}
	
	@Test
	public void findSettings() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("settings-1");
		RecruitingAuditLogUserSettings settings = recruitingAuditLogUserSettingsDAO.createAndPersist(id);
		dbInstance.commit();
		
		RecruitingAuditLogUserSettings reloadSettings =  recruitingAuditLogUserSettingsDAO.findSettings(id);
		
		Assert.assertNotNull(reloadSettings);
		Assert.assertEquals(settings.getKey(), reloadSettings.getKey());
		Assert.assertEquals(settings, reloadSettings);
		Assert.assertEquals(id, reloadSettings.getIdentity());
		Assert.assertEquals(settings.isEnabled(), reloadSettings.isEnabled());
		Assert.assertEquals(settings.getInterval(), reloadSettings.getInterval());
	}
	
	@Test
	public void createAndUpdate() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("settings-1");
		RecruitingAuditLogUserSettings settings = recruitingAuditLogUserSettingsDAO.createAndPersist(id);
		dbInstance.commit();
		
		// update
		settings.setEnabled(true);
		settings.setInterval("monthly");
		RecruitingAuditLogUserSettings updateSettings = recruitingAuditLogUserSettingsDAO.update(settings);
		
		// reload
		RecruitingAuditLogUserSettings reloadSettings =  recruitingAuditLogUserSettingsDAO.findSettings(id);
		
		Assert.assertNotNull(reloadSettings);
		Assert.assertEquals(settings, reloadSettings);
		Assert.assertEquals(updateSettings, reloadSettings);
		Assert.assertEquals(id, reloadSettings.getIdentity());
		Assert.assertTrue(reloadSettings.isEnabled());
		Assert.assertEquals("monthly", reloadSettings.getInterval());
	}
	
	@Test
	public void findEnabledNotifications() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("settings-4");
		RecruitingAuditLogUserSettings settings = recruitingAuditLogUserSettingsDAO.createAndPersist(id);
		dbInstance.commit();
		// update
		settings.setEnabled(true);
		settings.setInterval("monthly");
		RecruitingAuditLogUserSettings updatedSettings = recruitingAuditLogUserSettingsDAO.update(settings);
		dbInstance.commit();
		
		// reload
		List<RecruitingAuditLogUserSettings> enabledSettings =  recruitingAuditLogUserSettingsDAO.findEnabledNotifications();
		Assert.assertNotNull(enabledSettings);
		Assert.assertTrue(enabledSettings.contains(updatedSettings));
	}
}
