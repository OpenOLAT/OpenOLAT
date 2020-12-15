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
package org.olat.modules.teams.manager;

import org.jgroups.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.teams.TeamsUser;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsUserDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TeamsUserDAO teamsUserDao;
	
	@Test
	public void createUser() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-user-1");
		String identifier = UUID.randomUUID().toString();
		String displayName = "Teams User 1";
		TeamsUser user = teamsUserDao.createUser(id, identifier, displayName);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(user);
		Assert.assertNotNull(user.getKey());
		Assert.assertNotNull(user.getCreationDate());
		Assert.assertNotNull(user.getLastModified());
		Assert.assertEquals(id, user.getIdentity());
		Assert.assertEquals(identifier, user.getIdentifier());
		Assert.assertEquals(displayName, user.getDisplayName());
	}
	
	@Test
	public void getUser() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("teams-user-2");
		String identifier = UUID.randomUUID().toString();
		String displayName = "Teams User 2";
		TeamsUser user = teamsUserDao.createUser(id, identifier, displayName);
		dbInstance.commitAndCloseSession();
		
		TeamsUser reloadedUser = teamsUserDao.getUser(id);
		
		Assert.assertNotNull(reloadedUser);
		Assert.assertNotNull(reloadedUser.getCreationDate());
		Assert.assertNotNull(reloadedUser.getLastModified());
		Assert.assertEquals(user, reloadedUser);
		Assert.assertEquals(id, reloadedUser.getIdentity());
		Assert.assertEquals(identifier, reloadedUser.getIdentifier());
		Assert.assertEquals(displayName, reloadedUser.getDisplayName());
	}

}
