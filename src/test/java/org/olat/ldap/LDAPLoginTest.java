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
import org.junit.Rule;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;


/**
 * Description:<br>
 * LDAP junit tests
 * 
 * please import "olattest.ldif" into your configured LDAP directory
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
	
	@Rule
	public EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
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
	public void testUserBind() throws Exception {
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
}
