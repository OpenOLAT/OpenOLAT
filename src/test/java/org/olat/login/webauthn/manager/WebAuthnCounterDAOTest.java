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
package org.olat.login.webauthn.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.login.webauthn.WebAuthnStatistics;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class WebAuthnCounterDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private WebAuthnCounterDAO webAuthnCounterDao;
	
	@Test
	public void createStatistics() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("webauthn-1");
		WebAuthnStatistics wStatistics = webAuthnCounterDao.createStatistics(id, 1l);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(wStatistics);
		Assert.assertNotNull(wStatistics.getKey());
		Assert.assertNotNull(wStatistics.getCreationDate());
		Assert.assertNotNull(wStatistics.getLastModified());
		Assert.assertEquals(id, wStatistics.getIdentity());
		Assert.assertEquals(1l, wStatistics.getLaterCounter());
	}
	
	@Test
	public void loadStatistics() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("webauthn-2");
		WebAuthnStatistics wStatistics = webAuthnCounterDao.createStatistics(id, 3l);
		dbInstance.commitAndCloseSession();
		
		List<WebAuthnStatistics> identityStatistics = webAuthnCounterDao.getStatistics(id);
		Assert.assertNotNull(identityStatistics);
		Assert.assertEquals(1l, identityStatistics.size());
		
		WebAuthnStatistics identityStatistic = identityStatistics.get(0);
		Assert.assertNotNull(identityStatistic.getKey());
		Assert.assertNotNull(identityStatistic.getCreationDate());
		Assert.assertNotNull(identityStatistic.getLastModified());
		Assert.assertEquals(id, identityStatistic.getIdentity());
		Assert.assertEquals(3l, identityStatistic.getLaterCounter());
		Assert.assertEquals(wStatistics, identityStatistic);
	}

	@Test
	public void deleteUser() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("webauthn-3");
		WebAuthnStatistics wStatistics = webAuthnCounterDao.createStatistics(id, 3l);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(wStatistics);
		
		webAuthnCounterDao.deleteStatistics(id);
		dbInstance.commitAndCloseSession();
		
		List<WebAuthnStatistics> identityStatistics = webAuthnCounterDao.getStatistics(id);
		Assert.assertTrue(identityStatistics.isEmpty());
	}
	
}
