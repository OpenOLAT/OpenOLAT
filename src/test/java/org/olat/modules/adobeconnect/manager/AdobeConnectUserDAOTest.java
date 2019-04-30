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
package org.olat.modules.adobeconnect.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.modules.adobeconnect.AdobeConnectUser;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 17 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectUserDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AdobeConnectUserDAO adobeConnectUserDao;
	
	@Test
	public void createAdobeConnectUser() {
		String principalId = String.valueOf(CodeHelper.getForeverUniqueID());
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("aconnect-1");
		AdobeConnectUser user = adobeConnectUserDao.createUser(principalId, "meet2345.frentix.com", identity);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(user);
		Assert.assertEquals(identity, user.getIdentity());
		Assert.assertEquals(principalId, user.getPrincipalId());
	}
	
	@Test
	public void getAdobeConnectUser() {
		String principalId = String.valueOf(CodeHelper.getForeverUniqueID());
		String envName = "meet2345.frentix.com";
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("aconnect-1");
		AdobeConnectUser user = adobeConnectUserDao.createUser(principalId, envName, identity);
		dbInstance.commitAndCloseSession();
		
		AdobeConnectUser loadedUser = adobeConnectUserDao.getUser(identity, envName);
		
		Assert.assertNotNull(loadedUser);
		Assert.assertEquals(identity, loadedUser.getIdentity());
		Assert.assertEquals(principalId, loadedUser.getPrincipalId());
		Assert.assertEquals(user, loadedUser);
	}
	
	@Test
	public void deleteAdobeConnectUser() {
		// create 2 users
		String envName = "meet2346.frentix.com";
		String principalId1 = String.valueOf(CodeHelper.getForeverUniqueID());
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("aconnect-3");
		AdobeConnectUser user1 = adobeConnectUserDao.createUser(principalId1, envName, identity1);
		
		String principalId2 = String.valueOf(CodeHelper.getForeverUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("aconnect-4");
		AdobeConnectUser user2 = adobeConnectUserDao.createUser(principalId2, envName, identity2);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(user1);
		Assert.assertNotNull(user2);
		
		// delete data of user 1
		int rows = adobeConnectUserDao.deleteAdobeConnectUser(identity1);
		Assert.assertEquals(1, rows);
		
		// check user 1
		AdobeConnectUser loadedUser1 = adobeConnectUserDao.getUser(identity1, envName);
		Assert.assertNull(loadedUser1);
		// check user 2 has not lost its adobe connect principal
		AdobeConnectUser loadedUser2 = adobeConnectUserDao.getUser(identity2, envName);
		Assert.assertNotNull(loadedUser2);
	}
}
