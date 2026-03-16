/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.model.log.RecruitingAuditLogUserNotificationsImpl;

/**
 * 
 * Initial date: 23 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingAuditLogUserNotificationsDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingAuditLogUserNotificationsDAO userNotificationsDao;
	
	@Test
	public void create() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("noti-1");
		RecruitingAuditLogUserNotificationsImpl userNotificationInfos = userNotificationsDao.create(id, new Date());
		dbInstance.commit();
		
		Assert.assertNotNull(userNotificationInfos);
		Assert.assertNotNull(userNotificationInfos.getKey());
	}
	
	@Test
	public void getUserNotifications() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("noti-1");
		RecruitingAuditLogUserNotificationsImpl userNotificationInfos = userNotificationsDao.create(id, new Date());
		dbInstance.commit();
		
		List<RecruitingAuditLogUserNotificationsImpl> userNotifications = userNotificationsDao.getUserNotifications();
		Assert.assertNotNull(userNotifications);
		Assert.assertTrue(userNotifications.contains(userNotificationInfos));
	}

}
