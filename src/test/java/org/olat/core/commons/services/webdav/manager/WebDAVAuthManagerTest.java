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
package org.olat.core.commons.services.webdav.manager;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Encoder;
import org.olat.login.auth.OLATAuthManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVAuthManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private OLATAuthManager authManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private WebDAVAuthManager webdavAuthManager;
	
	@Test
	public void updatePassword() {
		// create an identity
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("update-wedbav-1");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(id);
		id.getUser().setProperty(UserConstants.INSTITUTIONALEMAIL, "inst_" + id.getUser().getEmail());
		userManager.updateUser(id, id.getUser());
		dbInstance.commitAndCloseSession();
		
		// update its password
		webdavAuthManager.upgradePassword(id.getIdentity(), id.getLogin(), "secret");
		
		// check digest providers
		Authentication ha1Authentication = securityManager.findAuthentication(id.getIdentity(),
				WebDAVAuthManager.PROVIDER_HA1_EMAIL, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(ha1Authentication);
		String digestEmailToken = Encoder.md5hash(id.getUser().getEmail() + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":secret");
		Assert.assertEquals(digestEmailToken, ha1Authentication.getCredential());
		
		Authentication ha1InstAuthentication = securityManager.findAuthentication(id.getIdentity(),
				WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(ha1InstAuthentication);
		String digestInstEmailToken = Encoder.md5hash(id.getUser().getInstitutionalEmail() + ":" + WebDAVManagerImpl.BASIC_AUTH_REALM + ":secret");
		Assert.assertEquals(digestInstEmailToken, ha1InstAuthentication.getCredential());
	}
	
	/**
	 * Check the case of bad data quality and duplicate institutional email
	 * adresss.
	 */
	@Test
	public void updatePassword_duplicate() {
		// create an identity
		IdentityWithLogin id1 = JunitTestHelper.createAndPersistRndUser("update-wedbav-2");
		IdentityWithLogin id2 = JunitTestHelper.createAndPersistRndUser("update-wedbav-3");
		dbInstance.commit();

		String uuid = UUID.randomUUID().toString();
		id1.getUser().setProperty(UserConstants.INSTITUTIONALEMAIL, uuid);
		id2.getUser().setProperty(UserConstants.INSTITUTIONALEMAIL, uuid);
		userManager.updateUser(id1, id1.getUser());
		userManager.updateUser(id2, id2.getUser());
		dbInstance.commitAndCloseSession();
		
		// update  password id 1
		webdavAuthManager.upgradePassword(id1.getIdentity(), id1.getLogin(), "secret");
		dbInstance.commitAndCloseSession();
		
		// update  password id 2
		//this one will have a problem to update the password, but it need to be silent
		webdavAuthManager.upgradePassword(id2.getIdentity(), id2.getLogin(), "secret");
		
		//check the authentication
		//check the connection is useable
		Authentication ha1InstAuthentication1 = securityManager.findAuthentication(id1.getIdentity(),
				WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(ha1InstAuthentication1);
		Authentication ha1InstAuthentication2 = securityManager.findAuthentication(id2.getIdentity(),
				WebDAVAuthManager.PROVIDER_HA1_INSTITUTIONAL_EMAIL, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNull(ha1InstAuthentication2);
		
		//check the connection is clean
		dbInstance.commit();
	}
	
	/**
	 * this reproduce the workflow of a LDAP login where someone switch
	 * the email addresses of users without updating their HA1-E credentials.
	 * 
	 */
	@Test
	public void updatePassword_wrongHA1Email() {
		IdentityWithLogin id1 = JunitTestHelper.createAndPersistRndUser("email-thief-1");
		IdentityWithLogin id2 = JunitTestHelper.createAndPersistRndUser("email-thief-2");
		
		String emailId1 = id1.getUser().getEmail();
		String emailId2 = id2.getUser().getEmail();
		
		// update  passwords
		webdavAuthManager.upgradePassword(id1.getIdentity(), id1.getLogin(), "secret");
		webdavAuthManager.upgradePassword(id2.getIdentity(), id2.getLogin(), "secret");
		dbInstance.commitAndCloseSession();
		
		// reproduce switch of email adress
		id1.getUser().setProperty(UserConstants.EMAIL, emailId2);
		id2.getUser().setProperty(UserConstants.EMAIL, emailId1);
		userManager.updateUserFromIdentity(id1.getIdentity());
		userManager.updateUserFromIdentity(id2.getIdentity());
		dbInstance.commitAndCloseSession();

		Identity reloadedId1 = userManager.findUniqueIdentityByEmail(emailId2);
		Assert.assertEquals(id1.getIdentity(), reloadedId1);

		CoreSpringFactory.getImpl(OLATAuthManager.class)
			.synchronizeOlatPasswordAndUsername(id2.getIdentity(), id2.getIdentity(), emailId2, "new-secret");
		
		// simulate login error
		securityManager.setIdentityLastLogin(id2.getIdentity());
		dbInstance.commit();
	}
	
	@Test
	public void authenticationByName() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("webdav-user-1");
		Identity reloadedUser = authManager.authenticate(id.getLogin(), id.getPassword());
		Assert.assertNotNull(reloadedUser);
		dbInstance.commitAndCloseSession();

		// login successful
		Identity authenticatedByLogin = webdavAuthManager.authenticate(id.getLogin(), id.getPassword());
		Assert.assertNotNull(authenticatedByLogin);
		Assert.assertEquals(id.getIdentity(), authenticatedByLogin);
	}
	
	@Test
	public void authenticationByName_failed() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("webdav-usser-2");
		Identity reloadedUser = authManager.authenticate(id.getLogin(), id.getPassword());
		Assert.assertNotNull(reloadedUser);
		dbInstance.commitAndCloseSession();
		
		// login successful
		Identity authenticatedId = webdavAuthManager.authenticate(id.getLogin(), "ooops");
		Assert.assertNull(authenticatedId);
	}
	
	@Test
	public void authenticationByName_denied() {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("webdav-usser-2");
		Identity reloadedUser = authManager.authenticate(id.getLogin(), id.getPassword());
		Assert.assertNotNull(reloadedUser);
		dbInstance.commitAndCloseSession();
		
		// login successful
		Identity authenticatedId = webdavAuthManager.authenticate(id.getLogin(), id.getPassword());
		Assert.assertNotNull(authenticatedId);
		
		// denied login
		securityManager.saveIdentityStatus(authenticatedId, Identity.STATUS_LOGIN_DENIED, id.getIdentity());
		dbInstance.commitAndCloseSession();
		
		// login failed
		Identity deniedId = webdavAuthManager.authenticate(id.getLogin(), id.getPassword());
		Assert.assertNull(deniedId);
	}
}
