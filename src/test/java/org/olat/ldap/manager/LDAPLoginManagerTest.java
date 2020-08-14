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
package org.olat.ldap.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.LDAPSyncConfiguration;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;

/**
 * 
 * Initial date: 22 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPLoginManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private LDAPSyncConfiguration syncConfiguration;
	
	@Rule
	public EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
	        .newInstance()
	        .usingDomainDsn("dc=olattest,dc=org")
	        .importingLdifs("org/olat/ldap/junittestdata/olattest.ldif")
	        .bindingToAddress("localhost")
	        .bindingToPort(1389)
	        .build();
	
	/**
	 * a to be the first to be called.
	 */
	@Test
	public void aSyncUsers() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
	
		// historic
		Identity identity = userManager.findUniqueIdentityByEmail("hhuerlimann@openolat.com");
		Assert.assertNotNull(identity);
	}
	
	@Test
	public void getIdentitiesDeletedInLdap() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		Identity orphan = JunitTestHelper.createAndPersistIdentityAsRndUser("ldap-orphan");
		securityManager.createAndPersistAuthentication(orphan, LDAPAuthenticationController.PROVIDER_LDAP,
				UUID.randomUUID().toString(), null, null);
		dbInstance.commitAndCloseSession();

		LdapContext ctx = ldapManager.bindSystem();
		List<Identity> identities = ldapManager.getIdentitiesDeletedInLdap(ctx);
		
		Assert.assertNotNull(identities);
		Assert.assertTrue(identities.contains(orphan));

		// historic
		Identity identity1 = userManager.findUniqueIdentityByEmail("hhuerlimann@openolat.com");
		Assert.assertFalse(identities.contains(identity1));
		Identity identity2 = userManager.findUniqueIdentityByEmail("ahentschel@openolat.com");
		Assert.assertFalse(identities.contains(identity2));
	}
	
	@Test
	public void doSyncSingleUserWithLoginAttribute() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());

		// sync single user
		Identity identity = userManager.findUniqueIdentityByEmail("ahentschel@openolat.com");
		Assert.assertNotNull(identity);
		Assert.assertNotEquals("ahentschel", identity.getName());
		
		// make a change
		String dn = "uid=ahentschel,ou=person,dc=olattest,dc=org";
		List<Modification> modifications = new ArrayList<>();
		modifications.add(new Modification(ModificationType.REPLACE, "sn", "Herschell"));
		embeddedLdapRule.ldapConnection().modify(dn, modifications);
		
		// simple sync
		ldapManager.doSyncSingleUserWithLoginAttribute(identity);
		dbInstance.commitAndCloseSession();
		
		Identity reloadIdentity = securityManager.loadIdentityByKey(identity.getKey());
		Assert.assertNotNull(reloadIdentity);
		Assert.assertNotEquals("ahentschel", reloadIdentity.getName());
		Assert.assertEquals(identity, reloadIdentity);
		Assert.assertEquals("Alessandro", reloadIdentity.getUser().getFirstName());
		Assert.assertEquals("Herschell", reloadIdentity.getUser().getLastName());
		Assert.assertEquals("ahentschel@openolat.com", reloadIdentity.getUser().getEmail());
		Assert.assertEquals("ahentschel", reloadIdentity.getUser().getProperty(UserConstants.NICKNAME, null));
	}
	
	@Test
	public void testUserBindDigest() throws Exception {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		
		LDAPError errors = new LDAPError();
		String uid = "hadigest";
		String userPW = "olat";
		
		//normal bind, should work
		Attributes attrs = ldapManager.bindUser(uid, userPW, errors);
		Assert.assertNotNull(attrs);
		Assert.assertEquals("Digest", attrs.get("sn").get());
	}
	
	@Test
	public void updateIdentity() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		Identity identity = userManager.findUniqueIdentityByEmail("john.doe@openolat.com");
		Assert.assertNotNull(identity);
		
		// authentication
		LDAPError ldapError = new LDAPError();
		Identity johnDoeId = ldapManager.authenticate("updateme", "olat", ldapError);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(johnDoeId);
		// check user properties
		Assert.assertEquals("john.doe@openolat.com", johnDoeId.getUser().getEmail());
		Assert.assertEquals("John", johnDoeId.getUser().getFirstName());
		Assert.assertEquals("Doe", johnDoeId.getUser().getLastName());
		
		// make some changes and sync users
		String dn = "uid=updateme,ou=person,dc=olattest,dc=org";
		List<Modification> modifications = new ArrayList<>();
		
		modifications.add(new Modification(ModificationType.REPLACE, "mail", "michel.dupont@frentix.com"));
		modifications.add(new Modification(ModificationType.REPLACE, "givenname", "Michel"));
		modifications.add(new Modification(ModificationType.REPLACE, "sn", "Dupont"));
		embeddedLdapRule.ldapConnection().modify(dn, modifications);
		
		boolean updateAllOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(updateAllOk);
		Assert.assertTrue(errors.isEmpty());
		
		// check the identity
		Identity updatedIdentity = securityManager.loadIdentityByKey(identity.getKey());
		Assert.assertNotNull(updatedIdentity);
		Assert.assertEquals("michel.dupont@frentix.com", updatedIdentity.getUser().getEmail());
		Assert.assertEquals("Michel", updatedIdentity.getUser().getFirstName());
		Assert.assertEquals("Dupont", updatedIdentity.getUser().getLastName());
	}
	
	@Test
	public void updateDigestAutentications() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		// authentication to create HA1 authentication hashes
		LDAPError ldapError = new LDAPError();
		Identity identity = ldapManager.authenticate("hadigest", "olat", ldapError);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(identity);

		Identity loadedIdentity = userManager.findUniqueIdentityByEmail("ha1digest@openolat.com");
		Assert.assertNotNull(loadedIdentity);
		
		// check authentications
		Authentication ha1Authentication = securityManager.findAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1);
		Assert.assertNotNull(ha1Authentication);
		Assert.assertEquals("hadigest", ha1Authentication.getAuthusername());
		
		Authentication ha1EAuthentication = securityManager.findAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1_EMAIL);
		Assert.assertNotNull(ha1EAuthentication);
		Assert.assertEquals("ha1digest@openolat.com", ha1EAuthentication.getAuthusername());

		Authentication ldapAuthentication = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP);
		Assert.assertNotNull(ldapAuthentication);
		
		// sync user
		String dn = "uid=hadigest,ou=person,dc=olattest,dc=org";
		Modification mod = new Modification(ModificationType.REPLACE, "mail", "haedigest@frentix.com");
		embeddedLdapRule.ldapConnection().modify(dn, mod);
		
		boolean updateAllOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(updateAllOk);
		
		// check authentications
		Authentication validHa1Authentication = securityManager.findAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1);
		Assert.assertNotNull(validHa1Authentication);
		Assert.assertEquals("hadigest", validHa1Authentication.getAuthusername());
		
		Authentication invalidHa1EAuthentication = securityManager.findAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1_EMAIL);
		Assert.assertNull(invalidHa1EAuthentication);
	
		Authentication validLdapAuthentication = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP);
		Assert.assertNotNull(validLdapAuthentication);
	}
	
	@Test
	public void findIdentityByLdapAuthentication() {
		LDAPError ldapError = new LDAPError();
		
		// use SN as login attribute
		String currentLoginAttr = syncConfiguration.getLdapUserLoginAttribute();
		syncConfiguration.setLdapUserLoginAttribute("sn");
		
		Attributes attrs = ldapManager.bindUser("Ramljak", "olat", ldapError);
		Assert.assertNotNull(attrs);

		LDAPError errors = new LDAPError();
		Identity identity = ldapManager.findIdentityByLdapAuthentication(attrs, errors);
		Assert.assertNotNull(identity);
		Assert.assertEquals("Ramljak", identity.getUser().getLastName());
		
		syncConfiguration.setLdapUserLoginAttribute(currentLoginAttr);
	}
	
	@Test
	public void findIdentityByLdapAuthenticationConvertFromOlat() {
		LDAPError ldapError = new LDAPError();
		
		// Take an LDAP user, delete her LDAP authentication
		List<String> firstLastName = Collections.singletonList("Carmen Guerrera");
		List<FindNamedIdentity> foundIdentities = securityManager.findIdentitiesBy(firstLastName);
		Assert.assertEquals(1, foundIdentities.size());
		
		Identity identity = foundIdentities.get(0).getIdentity();
		Authentication authentication = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP);
		Assert.assertNotNull(authentication);
		securityManager.deleteAuthentication(authentication);
		securityManager.createAndPersistAuthentication(identity, "OLAT", "cguerrera", "secret", Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		
		// convert users to LDAP
		boolean currentConvertExistingLocalUsers = ldapLoginModule.isConvertExistingLocalUsersToLDAPUsers();
		ldapLoginModule.setConvertExistingLocalUsersToLDAPUsers(true);

		// use uid as login attribute	
		Attributes attrs = ldapManager.bindUser("cguerrera", "olat", ldapError);
		Assert.assertNotNull(attrs);

		LDAPError errors = new LDAPError();
		Identity convertedIdentity = ldapManager.findIdentityByLdapAuthentication(attrs, errors);
		Assert.assertNotNull(convertedIdentity);
		Assert.assertEquals("Guerrera", convertedIdentity.getUser().getLastName());

		// revert configuration
		ldapLoginModule.setConvertExistingLocalUsersToLDAPUsers(currentConvertExistingLocalUsers);
	}
	
	@Test
	public void findIdentityByLdapAuthenticationConvertFromIdentityName() {
		// Take an LDAP user, delete her LDAP authentication
		List<String> firstLastName = Collections.singletonList("Leyla Salathe");
		List<FindNamedIdentity> foundIdentities = securityManager.findIdentitiesBy(firstLastName);
		List<Identity> deleteList = foundIdentities.stream()
				.map(FindNamedIdentity::getIdentity)
				.collect(Collectors.toList());
		ldapManager.deleteIdentities(deleteList, null);
		dbInstance.commitAndCloseSession();
		
		String currentIdentityNameGenerator = securityModule.getIdentityName();
		securityModule.setIdentityName("manual");
		
		// Create the user without OLAT login
		User user = userManager.createUser("Leyla", "Salathe", "lsalathe@openolat.com");
		Identity identity = securityManager.createAndPersistIdentityAndUser("lsalathe", null, null, user, null, null, null);
		
		// convert users to LDAP
		boolean currentConvertExistingLocalUsers = ldapLoginModule.isConvertExistingLocalUsersToLDAPUsers();
		ldapLoginModule.setConvertExistingLocalUsersToLDAPUsers(true);
		
		// use uid as login attribute
		LDAPError ldapError = new LDAPError();
		Attributes attrs = ldapManager.bindUser("lsalathe", "olat", ldapError);
		Assert.assertNotNull(attrs);

		LDAPError errors = new LDAPError();
		Identity convertedIdentity = ldapManager.findIdentityByLdapAuthentication(attrs, errors);
		Assert.assertNotNull(convertedIdentity);
		Assert.assertEquals("Salathe", convertedIdentity.getUser().getLastName());

		// revert configuration
		ldapLoginModule.setConvertExistingLocalUsersToLDAPUsers(currentConvertExistingLocalUsers);
		securityModule.setIdentityName(currentIdentityNameGenerator);
		
		// check
		Assert.assertEquals(identity, convertedIdentity);
		Authentication authentication = securityManager.findAuthentication(convertedIdentity, LDAPAuthenticationController.PROVIDER_LDAP);
		Assert.assertNotNull(authentication);
	}

}
