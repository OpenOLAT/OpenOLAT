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
import java.util.Collections;
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
import org.olat.test.JunitTestHelper.IdentityWithLogin;
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
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-1-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), RestSecurityBeanImpl.REST_AUTH_PROVIDER, BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();

		String newToken = UUID.randomUUID().toString();
		authenticationDao.updateCredential(auth, newToken);
		
		//check if the new token was saved
		Authentication updatedAuth = securityManager.findAuthentication(ident.getIdentity(), RestSecurityBeanImpl.REST_AUTH_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertEquals(newToken, updatedAuth.getCredential());
	}
	
	@Test
	public void getAuthenticationIdentityProvider() {
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-1-");
		dbInstance.commitAndCloseSession();
		
		//check if the new token was saved
		Authentication authentication = authenticationDao.getAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER);
		Assert.assertEquals(ident.getIdentity(), authentication.getIdentity());
		Assert.assertEquals(ident.getLogin(), authentication.getAuthusername());
	}
	
	@Test
	public void getIdentitiesWithAuthentication() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-6-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "SPECAUTH", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<Identity> identities = authenticationDao.getIdentitiesWithAuthentication("SPECAUTH");
		Assert.assertTrue(identities.contains(ident.getIdentity()));
	}
	
	@Test
	public void loadByKey() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-26-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "SPECAUTH", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		Authentication reloadedAuth = authenticationDao.loadByKey(auth.getKey());
		Assert.assertEquals(auth, reloadedAuth);
	}
	
	@Test
	public void countIdentitiesWithAuthentication() {
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-16-");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ident);

		long numOfOlatAuthentication = authenticationDao.countIdentitiesWithAuthentication("OLAT");
		Assert.assertTrue(numOfOlatAuthentication > 0);
		
		List<Identity> identities = authenticationDao.getIdentitiesWithAuthentication("OLAT");
		Assert.assertEquals((int)numOfOlatAuthentication, identities.size());
	}
	
	@Test
	public void getIdentitiesWithLogin() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<Identity> identities = authenticationDao.getIdentitiesWithLogin(ident.getLogin());
		Assert.assertNotNull(identities);
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(ident.getIdentity(), identities.get(0));
	}
	
	@Test
	public void getIdentitiesWithCamelCaseLogin() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<Identity> lowerIdentities = authenticationDao.getIdentitiesWithLogin(ident.getLogin().toLowerCase());
		Assert.assertNotNull(lowerIdentities);
		Assert.assertEquals(1, lowerIdentities.size());
		Assert.assertEquals(ident.getIdentity(), lowerIdentities.get(0));

		List<Identity> upperIdentities = authenticationDao.getIdentitiesWithLogin(ident.getLogin().toUpperCase());
		Assert.assertNotNull(upperIdentities);
		Assert.assertEquals(1, upperIdentities.size());
		Assert.assertEquals(ident.getIdentity(), upperIdentities.get(0));
	}
	
	@Test
	public void getAuthentication() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		Authentication olatAuthentication = authenticationDao.getAuthentication(ident.getLogin(), "OLAT");
		Assert.assertNotNull(olatAuthentication);
		Assert.assertEquals(ident.getIdentity(), olatAuthentication.getIdentity());
		
		Authentication ldapAuthentication = authenticationDao.getAuthentication(ident.getLogin(), "LDAP");
		Assert.assertNull(ldapAuthentication);
	}
	
	@Test
	public void getAuthenticationByAuthusername() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		Authentication olatAuthentication = authenticationDao.getAuthenticationByAuthusername(ident.getLogin(), "OLAT", BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(olatAuthentication);
		Assert.assertEquals(ident.getIdentity(), olatAuthentication.getIdentity());
		
		Authentication ldapAuthentication = authenticationDao.getAuthentication(ident.getLogin(), "LDAP");
		Assert.assertNull(ldapAuthentication);
	}
	
	@Test
	public void getAuthenticationByAuthusernameCamelCase() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2low-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		Authentication lowerAuthentication = authenticationDao.getAuthenticationByAuthusername(ident.getLogin().toLowerCase(), "OLAT", BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(lowerAuthentication);
		Assert.assertEquals(ident.getIdentity(), lowerAuthentication.getIdentity());
		
		Authentication upperAuthentication = authenticationDao.getAuthenticationByAuthusername(ident.getLogin().toUpperCase(), "OLAT", BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(upperAuthentication);
		Assert.assertEquals(ident.getIdentity(), upperAuthentication.getIdentity());
	}
	
	@Test
	public void getAuthenticationsByAuthusername() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<Authentication> olatAuthentications = authenticationDao.getAuthenticationsByAuthusername(ident.getLogin());
		Assert.assertNotNull(olatAuthentications);
		Assert.assertEquals(1, olatAuthentications.size());
		Assert.assertEquals(ident.getIdentity(), olatAuthentications.get(0).getIdentity());
	}
	
	@Test
	public void getAuthenticationsByAuthusernameCamelCase() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<Authentication> lowerAuthentications = authenticationDao.getAuthenticationsByAuthusername(ident.getLogin().toLowerCase());
		Assert.assertNotNull(lowerAuthentications);
		Assert.assertEquals(1, lowerAuthentications.size());
		Assert.assertEquals(ident.getIdentity(), lowerAuthentications.get(0).getIdentity());
		
		List<Authentication> upperAuthentications = authenticationDao.getAuthenticationsByAuthusername(ident.getLogin().toUpperCase());
		Assert.assertNotNull(upperAuthentications);
		Assert.assertEquals(1, upperAuthentications.size());
		Assert.assertEquals(ident.getIdentity(), upperAuthentications.get(0).getIdentity());
	}
	
	@Test
	public void getAuthenticationsByProvider() {
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(ident);

		List<Authentication> olatAuthentications = authenticationDao.getAuthentications("OLAT");
		Assert.assertNotNull(olatAuthentications);
		for(Authentication authentication:olatAuthentications) {
			Assert.assertEquals("OLAT", authentication.getProvider());
		}
	}
	
	@Test
	public void getAuthenticationsByAuthusername_providersList() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<String> olatProviderList = Collections.singletonList("OLAT");
		List<Authentication> olatAuthentications = authenticationDao.getAuthenticationsByAuthusername(ident.getLogin(), olatProviderList);
		Assert.assertNotNull(olatAuthentications);
		Assert.assertEquals(1, olatAuthentications.size());
		Assert.assertEquals(ident.getIdentity(), olatAuthentications.get(0).getIdentity());
		
		// negative test
		List<String> ldapProviderList = Collections.singletonList("LDAP");
		List<Authentication> ldapAuthentications = authenticationDao.getAuthenticationsByAuthusername(ident.getLogin(), ldapProviderList);
		Assert.assertNotNull(ldapAuthentications);
		Assert.assertTrue(ldapAuthentications.isEmpty());
	}
	
	@Test
	public void getAuthenticationsByAuthusername_providersCamelList() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<String> providerList = Collections.singletonList("OLAT");
		List<Authentication> lowerAuthentications = authenticationDao.getAuthenticationsByAuthusername(ident.getLogin().toLowerCase(), providerList);
		Assert.assertNotNull(lowerAuthentications);
		Assert.assertEquals(1, lowerAuthentications.size());
		Assert.assertEquals(ident.getIdentity(), lowerAuthentications.get(0).getIdentity());
		
		// negative test
		List<Authentication> upperAuthentications = authenticationDao.getAuthenticationsByAuthusername(ident.getLogin().toUpperCase(), providerList);
		Assert.assertNotNull(upperAuthentications);
		Assert.assertEquals(1, upperAuthentications.size());
		Assert.assertEquals(ident.getIdentity(), upperAuthentications.get(0).getIdentity());
	}
	
	@Test
	public void getAuthentications() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		List<Authentication> authentications = authenticationDao.getAuthentications(ident.getIdentity());
		Assert.assertNotNull(authentications);
		Assert.assertEquals(1, authentications.size());
		Assert.assertEquals(auth, authentications.get(0));
	}
	
	@Test
	public void hasAuthentication() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-2-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);

		boolean hasOlatAuthentication = authenticationDao.hasAuthentication(ident.getIdentity(), "OLAT");
		Assert.assertTrue(hasOlatAuthentication);
		boolean hasLdapAuthentication = authenticationDao.hasAuthentication(ident.getIdentity(), "LDAP");
		Assert.assertFalse(hasLdapAuthentication);
	}

	@Test
	public void hasValidOlatAuthentication() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-3-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(auth);
		
		List<String> fullProviders = new ArrayList<>();
		fullProviders.add(LDAPAuthenticationController.PROVIDER_LDAP);

		//check nothing at the end
		boolean valid = authenticationDao.hasValidOlatAuthentication(ident.getIdentity(), false, 0, fullProviders);
		Assert.assertTrue(valid);
		//check if the authentication is new
		boolean brandNew = authenticationDao.hasValidOlatAuthentication(ident.getIdentity(), true, 0, fullProviders);
		Assert.assertFalse(brandNew);
		//check if the authentication is new
		boolean fresh = authenticationDao.hasValidOlatAuthentication(ident.getIdentity(), false, 60, fullProviders);
		Assert.assertTrue(fresh);
	}
	
	@Test
	public void hasValidOlatAuthentication_tooOld() {
		String token = UUID.randomUUID().toString();
		IdentityWithLogin ident = JunitTestHelper.createAndPersistRndUser("authdao-3-");
		Authentication auth = securityManager.createAndPersistAuthentication(ident.getIdentity(), "OLAT", BaseSecurity.DEFAULT_ISSUER,
				ident.getLogin(), token, null);
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
		boolean tooOld = authenticationDao.hasValidOlatAuthentication(ident.getIdentity(), false, 60, fullProviders);
		Assert.assertFalse(tooOld);
	}
}
