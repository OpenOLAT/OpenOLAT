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
package org.olat.ldap;

import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;


/**
 * Low level LDAP authentication unit tests. this test must happend
 * before LDAPLoginManagerTest (it will do a sync).
 * 
 * <P>
 * Initial Date:  June 30, 2008 <br>
 * @author Maurus Rohrer
 */
public class LDAPLoginTest extends OlatTestCase {
	
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private LDAPSyncConfiguration syncConfiguration;
	
	@ClassRule
	public static final EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
	        .newInstance()
	        .usingDomainDsn("dc=olattest,dc=org")
	        .importingLdifs("org/olat/ldap/junittestdata/olattest.ldif")
	        .bindingToAddress("localhost")
	        .bindingToPort(1389)
	        .build();
	
	@Test
	public void testSystemBind() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		
		LdapContext ctx = ldapManager.bindSystem();
		Assert.assertNotNull(ctx);
	}
	
	@Test
	public void userBind() throws Exception {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());

		LDAPError errors = new LDAPError();
		String uid = "mrohrer";
		String userPW = "olat";
		
		//normal bind, should work
		Attributes attrs = ldapManager.bindUser(uid, userPW, errors);
		Assert.assertNotNull(attrs);
		Assert.assertEquals("Rohrer", attrs.get("sn").get());

		//wrong password, should fail
		userPW = "haha";
		attrs = ldapManager.bindUser(uid, userPW, errors);
		Assert.assertNull(attrs);
		Assert.assertEquals("Username or password incorrect", errors.get());

		//wrong username, should fail
		uid = "ruedisueli";
		userPW = "olat";
		attrs = ldapManager.bindUser(uid, userPW, errors);
		Assert.assertNull(attrs);
		Assert.assertEquals("Username or password incorrect", errors.get());

		//no password, should fail
		uid = "mrohrer";
		userPW = null;
		attrs = ldapManager.bindUser(uid, userPW, errors);
		Assert.assertNull(attrs);
		Assert.assertEquals("Username and password must be selected", errors.get());
	}
	
	@Test
	public void userBindLoginWithAlternativeLoginAttribute() throws Exception {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		String currentLoginAttr = syncConfiguration.getLdapUserLoginAttribute();
		
		// use SN as login attribute
		syncConfiguration.setLdapUserLoginAttribute("sn");

		LDAPError errors = new LDAPError();
		Attributes attrs = ldapManager.bindUser("Forster", "olat", errors);
		Assert.assertNotNull(attrs);
		Assert.assertEquals("Forster", attrs.get("sn").get());
		Assert.assertTrue(errors.isEmpty());
		
		syncConfiguration.setLdapUserLoginAttribute(currentLoginAttr);
	}
	
	@Test
	public void userBindLoginWithTwoLoginAttributes() throws Exception {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		String currentLoginAttr = syncConfiguration.getLdapUserLoginAttribute();

		// use SN as login attribute
		syncConfiguration.setLdapUserLoginAttribute("sn," + currentLoginAttr);

		// try sn attribute
		LDAPError errors = new LDAPError();
		Attributes attrs = ldapManager.bindUser("Forster", "olat", errors);
		Assert.assertNotNull(attrs);
		Assert.assertEquals("dforster", attrs.get("uid").get());
		Assert.assertTrue(errors.isEmpty());
		
		// try uid attribute
		LDAPError altErrors = new LDAPError();
		Attributes altAttrs = ldapManager.bindUser("dforster", "olat", altErrors);
		Assert.assertNotNull(altAttrs);
		Assert.assertEquals("Forster", altAttrs.get("sn").get());
		Assert.assertEquals("dforster", altAttrs.get("uid").get());
		Assert.assertTrue(altErrors.isEmpty());

		syncConfiguration.setLdapUserLoginAttribute(currentLoginAttr);
	}
}
