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
package org.olat.basesecurity.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.restapi.security.RestSecurityBeanImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 18 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthenticationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private AuthenticationDAO authenticationDao;
	
	@Test
	public void updateCredential() {
		String token = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-1-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident, RestSecurityBeanImpl.REST_AUTH_PROVIDER, ident.getName(), token, null);
		dbInstance.commitAndCloseSession();

		String newToken = UUID.randomUUID().toString();
		authenticationDao.updateCredential(auth, newToken);
		
		//check if the new token was saved
		Authentication updatedAuth = securityManager.findAuthentication(ident, RestSecurityBeanImpl.REST_AUTH_PROVIDER);
		Assert.assertEquals(newToken, updatedAuth.getCredential());
	}
	
	@Test
	public void getIdentitiesWithAuthentication() {
		String token = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-6-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident, "SPECAUTH", ident.getName(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<Identity> identities = authenticationDao.getIdentitiesWithAuthentication("SPECAUTH");
		Assert.assertTrue(identities.contains(ident));
	}

	@Test
	public void hasAuthentication() {
		String token = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident, "OLAT", ident.getName(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		boolean hasOlatAuthentication = authenticationDao.hasAuthentication(ident, "OLAT");
		Assert.assertTrue(hasOlatAuthentication);
		boolean hasLdapAuthentication = authenticationDao.hasAuthentication(ident, "LDAP");
		Assert.assertFalse(hasLdapAuthentication);
	}

	@Test
	public void hasValidOlatAuthentication() {
		String token = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-3-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident, "OLAT", ident.getName(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);
		
		List<String> fullProviders = new ArrayList<>();
		fullProviders.add(LDAPAuthenticationController.PROVIDER_LDAP);

		//check nothing at the end
		boolean valid = authenticationDao.hasValidOlatAuthentication(ident, false, 0, fullProviders);
		Assert.assertTrue(valid);
		//check if the authentication is new
		boolean brandNew = authenticationDao.hasValidOlatAuthentication(ident, true, 0, fullProviders);
		Assert.assertFalse(brandNew);
		//check if the authentication is new
		boolean fresh = authenticationDao.hasValidOlatAuthentication(ident, false, 60, fullProviders);
		Assert.assertTrue(fresh);
	}
	
	@Test
	public void hasValidOlatAuthentication_tooOld() {
		String token = UUID.randomUUID().toString();
		Identity ident = JunitTestHelper.createAndPersistIdentityAsRndUser("authdao-3-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident, "OLAT", ident.getName(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);
		
		// fake the last modified date
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -120);
		((AuthenticationImpl)auth).setLastModified(cal.getTime());
		auth = dbInstance.getCurrentEntityManager().merge(auth);
		dbInstance.commitAndCloseSession();

		//check if the authentication is new
		List<String> fullProviders = new ArrayList<>();
		fullProviders.add(LDAPAuthenticationController.PROVIDER_LDAP);
		boolean tooOld = authenticationDao.hasValidOlatAuthentication(ident, false, 60, fullProviders);
		Assert.assertFalse(tooOld);
	}
}
