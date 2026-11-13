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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.manager.NotificationsManagerImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.DateUtils;
import org.olat.test.JunitTestHelper;
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
	@Autowired
	private NotificationsManagerImpl notificationsManager;
	
	
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
	
	@Test
	public void updatePreferences() {
		String defaultInterval = notificationsManager.getDefaultNotificationInterval();
		
		//create a user
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();

		User user = id.getUser();
		user.getPreferences().setInformSessionTimeout(true);
		Assert.assertEquals(defaultInterval, user.getPreferences().getNotificationInterval());
		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();
		
		notificationsManager.setDefaultNotificationInterval("weekly");
		
		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assert.assertEquals(true, reloadedUser.getPreferences().getInformSessionTimeout());
		Assert.assertEquals("weekly", reloadedUser.getPreferences().getNotificationInterval());

		notificationsManager.setDefaultNotificationInterval(defaultInterval);
	}
	
	@Test
	public void updateUserProperty() {
		//Create a user
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		Date lastModifiedDate = id.getUser().getLastModified();
		Assert.assertNotNull(lastModifiedDate);

		User user = id.getUser();
		user.setProperty(UserConstants.FIRSTNAME, "New first name");
		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();

		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assert.assertEquals("New first name", reloadedUser.getFirstName());
		Assert.assertNotEquals(lastModifiedDate, reloadedUser.getLastModified());
	}
	
	@Test
	public void lastModifiedAtCreation() {
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		User reloadedUser = userManager.loadUserByKey(id.getUser().getKey());
		Assert.assertNotNull(reloadedUser.getLastModified());
		Assert.assertEquals(reloadedUser.getCreationDate().getTime(), reloadedUser.getLastModified().getTime());
	}
	
	@Test
	public void updateUserWithoutChanges() {
		Identity id = createUserWithOldLastModified();
		int version = ((UserImpl)id.getUser()).getVersion();

		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();

		UserImpl reloadedUser = (UserImpl)userManager.loadUserByKey(id.getUser().getKey());
		Assertions.assertThat(reloadedUser.getLastModified()).isBefore(oneDayAgo());
		Assert.assertEquals(version, reloadedUser.getVersion());
	}
	
	@Test
	public void updateUserWithSameValue() {
		Identity id = createUserWithOldLastModified();
		
		User user = id.getUser();
		user.setProperty(UserConstants.FIRSTNAME, user.getFirstName());
		user.setProperty(UserConstants.LASTNAME, user.getLastName());
		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();

		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assertions.assertThat(reloadedUser.getLastModified()).isBefore(oneDayAgo());
	}
	
	@Test
	public void updateUserEmptyStringForNull() {
		Identity id = createUserWithOldLastModified();
		Assert.assertNull(id.getUser().getProperty(UserConstants.TELMOBILE));
		
		// Set directly, without the normalization of the property handler
		UserImpl user = (UserImpl)id.getUser();
		user.setUserProperty(UserConstants.TELMOBILE, "");
		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();

		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assertions.assertThat(reloadedUser.getLastModified()).isBefore(oneDayAgo());
	}
	
	@Test
	public void updateUserPreferences() {
		Identity id = createUserWithOldLastModified();
		
		User user = id.getUser();
		String language = "fr".equals(user.getPreferences().getLanguage()) ? "de" : "fr";
		user.getPreferences().setLanguage(language);
		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();

		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assert.assertEquals(language, reloadedUser.getPreferences().getLanguage());
		Assertions.assertThat(reloadedUser.getLastModified()).isBefore(oneDayAgo());
	}
	
	@Test
	public void updateUserTechnicalFields() {
		Identity id = createUserWithOldLastModified();
		
		UserImpl user = (UserImpl)id.getUser();
		user.setPortraitPath("portrait.png");
		user.setLogoPath("logo.png");
		user.setInitialsCssClass("o_user_initials_light_blue");
		user.setUserProperty("emchangeKey", UUID.randomUUID().toString());
		user.setUserProperty("emailDisabled", "true");
		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();

		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assert.assertEquals("portrait.png", reloadedUser.getPortraitPath());
		Assertions.assertThat(reloadedUser.getLastModified()).isBefore(oneDayAgo());
	}
	
	@Test
	public void updateUserPropertyMovesLastModified() {
		Identity id = createUserWithOldLastModified();
		
		User user = id.getUser();
		user.setProperty(UserConstants.LASTNAME, "New last name");
		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();

		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assert.assertEquals("New last name", reloadedUser.getLastName());
		Assertions.assertThat(reloadedUser.getLastModified()).isAfter(oneDayAgo());
	}
	
	@Test
	public void updateManagedUserAtCommit() {
		Identity id = createUserWithOldLastModified();
		
		// Change the managed user without calling updateUser
		User user = userManager.loadUserByKey(id.getUser().getKey());
		user.setProperty(UserConstants.FIRSTNAME, "Managed first name");
		dbInstance.commitAndCloseSession();

		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assert.assertEquals("Managed first name", reloadedUser.getFirstName());
		Assertions.assertThat(reloadedUser.getLastModified()).isAfter(oneDayAgo());
	}
	
	@Test
	public void updateUserCannotMoveLastModifiedBack() {
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		UserImpl user = (UserImpl)userManager.loadUserByKey(id.getUser().getKey());
		user.setLastModified(DateUtils.addDays(new Date(), -10));
		user.setInitialsCssClass("o_user_initials_light_blue");
		dbInstance.commitAndCloseSession();

		User reloadedUser = userManager.loadUserByKey(user.getKey());
		Assertions.assertThat(reloadedUser.getLastModified()).isAfter(oneDayAgo());
	}
	
	@Test
	public void getUserDisplayNameByIdentity() {
		//create a user
		String uuid = UUID.randomUUID().toString();
		Identity id = createUser(uuid);
		dbInstance.commitAndCloseSession();
		
		String fullname = userManager.getUserDisplayName(id);
		Assert.assertTrue(fullname.contains("firstcreateid-" +uuid));
	}
	
	@Test
	public void getUserDisplayNameByLazyIdentity() {
		//create a user
		String uuid = UUID.randomUUID().toString();
		Identity id = createUser(uuid);
		dbInstance.commitAndCloseSession();
		
		// Load lazy reference of identity
		Identity lazyId = dbInstance.getCurrentEntityManager().getReference(IdentityImpl.class, id.getKey());
		dbInstance.commitAndCloseSession();
		
		String fullname = userManager.getUserDisplayName(lazyId);
		Assert.assertTrue(fullname.contains("firstcreateid-" +uuid));
	}
	
	/**
	 * @return A user with a last modified date 2 days ago, detached
	 */
	private Identity createUserWithOldLastModified() {
		Identity id = createUser(UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		JunitTestHelper.setUserLastModified(id, DateUtils.addDays(new Date(), -2));
		Identity reloadedId = securityManager.loadIdentityByKey(id.getKey());
		dbInstance.commitAndCloseSession();
		Assertions.assertThat(reloadedId.getUser().getLastModified()).isBefore(oneDayAgo());
		return reloadedId;
	}
	
	private Date oneDayAgo() {
		return DateUtils.addDays(new Date(), -1);
	}
	
	private Identity createUser(String uuid) {
		String username = "createid-" + uuid;
		String email = username + "@frentix.com";
		String institutEmail = username + "@openolat.com";
		User user = userManager.createUser("first" + username, "last" + username, email);
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, institutEmail);
		Identity identity = securityManager.createAndPersistIdentityAndUser(null, username, null, user,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, null, username, "secret", null);
		Assert.assertNotNull(identity);
		return identity;
	}
}
