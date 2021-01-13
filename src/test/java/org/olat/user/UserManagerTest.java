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
package org.olat.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 14.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	
	
	@Test
	public void findIdentityByEmail_email() {
		//create a user
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		//get the identity by email
		String email = id.getUser().getProperty(UserConstants.EMAIL, null);
		Identity foundIdentity = userManager.findUniqueIdentityByEmail(email);
		Assert.assertNotNull(foundIdentity);
		Assert.assertEquals(id, foundIdentity);
	}
	
	@Test
	public void findIdentityByEmail_institutionalEmail() {
		//create a user
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		//get the identity by email
		String email = id.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		Identity foundIdentity = userManager.findUniqueIdentityByEmail(email);
		Assert.assertNotNull(foundIdentity);
		Assert.assertEquals(id, foundIdentity);
	}
	
	@Test
	public void findIdentitiesByEmail_email() {
		//create a user
		Identity id1 = createUser(UUID.randomUUID().toString());
		Identity id2 = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		//get empty (must survive)
		List<Identity> emptyIdentities = userManager.findIdentitiesByEmail(Collections.<String>emptyList());
		Assert.assertNotNull(emptyIdentities);
		Assert.assertTrue(emptyIdentities.isEmpty());
		
		//get the identities by emails
		List<String> emails = new ArrayList<>();
		emails.add(id1.getUser().getProperty(UserConstants.EMAIL, null));
		emails.add(id2.getUser().getProperty(UserConstants.EMAIL, null));
		
		List<Identity> identities = userManager.findIdentitiesByEmail(emails);
		Assert.assertNotNull(identities);
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
	}
	
	@Test
	public void findIdentitiesByEmail_institutionalEmail() {
		//create a user
		Identity id1 = createUser(UUID.randomUUID().toString());
		Identity id2 = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		//get the identities by emails
		List<String> emails = new ArrayList<>();
		emails.add(id1.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null));
		emails.add(id2.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null));
		
		List<Identity> identities = userManager.findIdentitiesByEmail(emails);
		Assert.assertNotNull(identities);
		Assert.assertEquals(2, identities.size());
		Assert.assertTrue(identities.contains(id1));
		Assert.assertTrue(identities.contains(id2));
	}
	
	@Test
	public void findUserKeyWithProperty() {
		//create a user
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		String institutionalEmail = id.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		List<Long> identityKeys = userManager.findUserKeyWithProperty(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		Assert.assertNotNull(identityKeys);
		Assert.assertEquals(1, identityKeys.size());
		Assert.assertEquals(id.getUser().getKey(), identityKeys.get(0));
	}
	
	@Test
	public void findIdentitiesWithProperty() {
		//create a user
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		String institutionalEmail = id.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
		List<Identity> identities = userManager.findIdentitiesWithProperty(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		Assert.assertNotNull(identities);
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(id, identities.get(0));
	}
	
	private Identity createUser(String uuid) {
		String username = "createid-" + uuid;
		String email = username + "@frentix.com";
		String institutEmail = username + "@openolat.com";
		User user = userManager.createUser("first" + username, "last" + username, email);
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, institutEmail);
		Identity identity = securityManager.createAndPersistIdentityAndUser(null, username, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), username, "secret", null);
		Assert.assertNotNull(identity);
		return identity;
	}
}
