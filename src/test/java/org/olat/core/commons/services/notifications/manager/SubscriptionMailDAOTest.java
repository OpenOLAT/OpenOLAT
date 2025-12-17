/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.notifications.manager;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.SubscriptionMail;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SubscriptionMailDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private SubscriptionMailDAO subscriptionMailDao;
	
	@Test
	public void createSubscriptionMail() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("sub-1");
		
		Date mail = new Date();
		Date next = DateUtils.addDays(mail, 5);
		SubscriptionMail smail = subscriptionMailDao.create(id, mail, next);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(smail);
		Assert.assertNotNull(smail.getCreationDate());
		Assert.assertNotNull(smail.getLastModified());
		Assert.assertNotNull(smail.getLastMail());
		Assert.assertNotNull(smail.getNextMail());
		Assert.assertEquals(id, smail.getIdentity());
	}
	
	@Test
	public void loadByIdentity() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("sub-2");
		
		Date mail = new Date();
		Date next = DateUtils.addDays(mail, 5);
		SubscriptionMail smail = subscriptionMailDao.create(id, mail, next);
		dbInstance.commitAndCloseSession();
		
		SubscriptionMail loadedMail = subscriptionMailDao.loadByIdentity(id);
		Assert.assertNotNull(loadedMail);
		Assert.assertEquals(smail, loadedMail);
		Assert.assertNotNull(loadedMail.getCreationDate());
		Assert.assertNotNull(loadedMail.getLastModified());
		Assert.assertNotNull(loadedMail.getLastMail());
		Assert.assertNotNull(loadedMail.getNextMail());
		Assert.assertEquals(id, loadedMail.getIdentity());
	}
}
