package org.olat.ldap.manager;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.Attributes;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.id.Identity;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.ui.LDAPAuthenticationController;
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
public class LDAPLoginManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private BaseSecurity securityManager;
	
	@Rule
	public EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
	        .newInstance()
	        .usingDomainDsn("dc=olattest,dc=org")
	        .importingLdifs("org/olat/ldap/junittestdata/olattest.ldif")
	        .bindingToAddress("localhost")
	        .bindingToPort(1389)
	        .build();
	
	@Test
	public void syncUsers() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		Identity identity = userManager.findUniqueIdentityByEmail("hhuerlimann@openolat.com");
		Assert.assertNotNull(identity);
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

}
