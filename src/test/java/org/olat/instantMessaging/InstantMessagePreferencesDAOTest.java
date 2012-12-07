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
package org.olat.instantMessaging;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.instantMessaging.manager.InstantMessagePreferencesDAO;
import org.olat.instantMessaging.model.ImPreferencesImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessagePreferencesDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private InstantMessagePreferencesDAO imDao;
	
	@Test
	public void testCreateMessage() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-prefs-1-" + UUID.randomUUID().toString());
		ImPreferencesImpl prefs = imDao.createPreferences(id);
		Assert.assertNotNull(prefs);
		Assert.assertNotNull(prefs.getKey());
		Assert.assertNotNull(prefs.getCreationDate());
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testLoadMessage() {
		//create a message
		Identity id = JunitTestHelper.createAndPersistIdentityAsAdmin("im-prefs-2-" + UUID.randomUUID().toString());
		ImPreferencesImpl prefs = imDao.createPreferences(id);
		Assert.assertNotNull(prefs);
		dbInstance.commitAndCloseSession();
		
		//load the message
		ImPreferencesImpl reloadedPrefs = imDao.getPreferences(id);
		Assert.assertNotNull(reloadedPrefs);
		Assert.assertEquals(prefs.getKey(), reloadedPrefs.getKey());
	}



}
