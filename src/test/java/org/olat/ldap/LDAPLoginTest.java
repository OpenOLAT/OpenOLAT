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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.ldap.manager.LDAPDAO;
import org.olat.ldap.model.LDAPUser;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
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
	private LDAPDAO ldapDao;
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
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
	
	@Test
	public void testSystemBind() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		
		LdapContext ctx = ldapManager.bindSystem();
		Assert.assertNotNull(ctx);
	}
	
	@Test
	public void testUserBind() throws NamingException {
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
	
	@Test @Ignore //need to sync the user
	public void testCheckUser() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());

		LDAPError errors = new LDAPError();

		//should create error entry
		String uid = "Administrator";
		Attributes attrs = ldapManager.bindUser(uid, "olat", errors);
		Identity identity = ldapManager.findIdentityByLdapAuthentication(attrs, errors);
		Assert.assertEquals("findIdentyByLdapAuthentication: attrs::null", errors.get());

		//should return identity, since is existing in OLAT and Managed by LDAP
		uid = "mrohrer";
		attrs = ldapManager.bindUser(uid, "olat", errors);
		identity = ldapManager.findIdentityByLdapAuthentication(attrs, errors);
		Assert.assertEquals(uid, identity.getName());
		Assert.assertTrue(errors.isEmpty());
	}
	
	@Test @Ignore //need to sync the user
	public void testSyncUser(){
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());

		Map<String,String> changedMap = new HashMap<String,String>();
		LDAPError errors = new LDAPError();
		
		changedMap.put("userID", "kmeier");
		changedMap.put("firstName", "Klaus");
		changedMap.put("email", "kmeier@olat.org");
		changedMap.put("institutionalName", "Informatik");
		changedMap.put("homepage", "http://www.olat.org");
		Identity identity = securityManager.findIdentityByName("kmeier");
		ldapManager.syncUser(changedMap, identity);
		
		
		changedMap.put("userID", "kmeier");
		Attributes attrs = ldapManager.bindUser("kmeier", "olat", errors);
		changedMap = ldapManager.prepareUserPropertyForSync(attrs, identity);
		assertEquals(true, (changedMap==null));
	}

	@Test  @Ignore
	public void testIdentityDeletedInLDAP(){
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());

		List<Identity> deletList;
		
		//should be empty
		LdapContext ctx = ldapManager.bindSystem(); 
		deletList = ldapManager.getIdentitysDeletedInLdap(ctx);
		assertEquals(0, (deletList.size()));
		
		// simulate closed session (user adding from startup job)
		DBFactory.getInstance().intermediateCommit();
		
		//create some users in LDAPSecurityGroup
		User user = UserManager.getInstance().createUser("grollia", "wa", "gorrila@olat.org");
		Identity identity = securityManager.createAndPersistIdentityAndUser("gorilla", null,user, "LDAP", "gorrila");
		SecurityGroup secGroup1 = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
		securityManager.addIdentityToSecurityGroup(identity, secGroup1);
		user = UserManager.getInstance().createUser("wer", "immer", "immer@olat.org");
		identity = securityManager.createAndPersistIdentityAndUser("der", null, user, "LDAP", "der");
		securityManager.addIdentityToSecurityGroup(identity, secGroup1);
		user = UserManager.getInstance().createUser("die", "da", "chaspi@olat.org");
		identity = securityManager.createAndPersistIdentityAndUser("das", null, user, "LDAP", "das");
		securityManager.addIdentityToSecurityGroup(identity, secGroup1);
		
		// simulate closed session
		DBFactory.getInstance().intermediateCommit();
				
		//3 members in LDAP group but not existing in OLAT
		deletList = ldapManager.getIdentitysDeletedInLdap(ctx);
		assertEquals(3, (deletList.size()));
		
		//delete user in OLAT
		securityManager.removeIdentityFromSecurityGroup(identity, secGroup1);
		UserDeletionManager.getInstance().deleteIdentity(identity, null);

		// simulate closed session
		DBFactory.getInstance().intermediateCommit();

		//2 members in LDAP group but not existing in OLAT
		deletList = ldapManager.getIdentitysDeletedInLdap(ctx);
		assertEquals(2, (deletList.size()));
	}
	
	@Test @Ignore
	public void testCreateUser() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());

		String uid = "mrohrer";
		String userPW = "olat";
		LDAPError errors = new LDAPError();
		
		boolean usersSyncedAtStartup = ldapLoginModule.isLdapSyncOnStartup();
		//user should not exits in OLAT when not synced during startup
		assertEquals(usersSyncedAtStartup, (securityManager.findIdentityByName(uid) != null));
		// bind user 
		Attributes attrs = ldapManager.bindUser(uid, userPW, errors);
		assertEquals(usersSyncedAtStartup, (securityManager.findIdentityByName(uid) != null));
		//user should be created
		ldapManager.createAndPersistUser(attrs);
		assertEquals(true, (securityManager.findIdentityByName(uid) != null));

		//should fail, user is existing
		ldapManager.createAndPersistUser(attrs);
		assertEquals(true, (securityManager.findIdentityByName(uid) != null));
	}
	
	@Test  @Ignore
	public void testCreateChangedAttrMap() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());

		// simulate closed session (user adding from startup job)
		DBFactory.getInstance().intermediateCommit();

		String uid = "kmeier";
		String pwd = "olat";
		LDAPError errors = new LDAPError();

		boolean usersSyncedAtStartup = ldapLoginModule.isLdapSyncOnStartup();
		if (usersSyncedAtStartup) {
			try {
				//create user but with different attributes - must fail since user already exists
				User user = UserManager.getInstance().createUser("klaus", "Meier", "klaus@meier.ch");
				Identity identity = securityManager.createAndPersistIdentityAndUser("kmeier", null, user, "LDAP", "kmeier");
				SecurityGroup secGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
				securityManager.addIdentityToSecurityGroup(identity, secGroup);
				
				// simulate closed session (user adding from startup job)
				DBFactory.getInstance().intermediateCommit();
				fail("Expected constrant violation becaus of doupliate entry");			
			} catch (Exception e) {
				// success, this is what we expected
			}
			//changedAttrMap empty since already synchronized
			Attributes attrs = ldapManager.bindUser(uid, pwd, errors);
			Identity identitys = securityManager.findIdentityByName(uid);
			Map<String, String> changedAttrMap = ldapManager.prepareUserPropertyForSync(attrs, identitys);
			// map is empty - no attributes to sync
			assertNull(changedAttrMap);
		} else {
			//create user but with different attributes - must fail since user already exists
			User user = UserManager.getInstance().createUser("klaus", "Meier", "klaus@meier.ch");
			Identity identity = securityManager.createAndPersistIdentityAndUser("kmeier", null, user, "LDAP", "kmeier");
			SecurityGroup secGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
			securityManager.addIdentityToSecurityGroup(identity, secGroup);
			// simulate closed session (user adding from startup job)
			DBFactory.getInstance().intermediateCommit();
			
			//changedAttrMap has 2 changes and uid as entrys (Klaus!=klaus, klaus@olat.org!=klaus@meier.ch)
			Attributes attrs = ldapManager.bindUser(uid, pwd, errors);
			Identity identitys = securityManager.findIdentityByName(uid);
			Map<String, String> changedAttrMap = ldapManager.prepareUserPropertyForSync(attrs, identitys);
			// result must be 3: 2 changed plus the user ID which is always in the map
			assertEquals(3, changedAttrMap.keySet().size());
		}
		
		//nothing to change for this user
		uid= "mrohrer";
		Attributes attrs = ldapManager.bindUser(uid, pwd, errors);
		Identity identitys = securityManager.findIdentityByName(uid);
		Map<String, String> changedAttrMap = ldapManager.prepareUserPropertyForSync(attrs, identitys);
		assertEquals(true, (changedAttrMap==null));
	}
	
	@Test  @Ignore
	public void testCronSync() throws Exception {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());

		LdapContext ctx;
		List<LDAPUser> ldapUserList;
		List<Attributes> newLdapUserList;
		Map<Identity, Map<String, String>> changedMapIdenityMap;
		List<Identity> deletedUserList;
		
		LDAPError errors = new LDAPError();

		//find user changed after 2010,01,09,00,00
		ctx = ldapManager.bindSystem();
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 0, 10, 0, 0, 0);
		Date syncDate = cal.getTime();
		ldapUserList = ldapDao.getUserAttributesModifiedSince(syncDate, ctx);
		assertEquals(1, ldapUserList.size());
		
		//find all users
		syncDate = null;
		ldapUserList = ldapDao.getUserAttributesModifiedSince(syncDate, ctx);
		assertEquals(6, ldapUserList.size());

		//prepare create- and sync-Lists for each user from defined syncTime
		Identity idenity;
		Map<String,String> changedAttrMap;
		newLdapUserList = new LinkedList<Attributes>();
		changedMapIdenityMap = new HashMap<Identity, Map<String, String>>();
		for (int i = 0; i < ldapUserList.size(); i++) {
			Attributes userAttrs = ldapUserList.get(i).getAttributes();
			String user = getAttributeValue(userAttrs.get(syncConfiguration.getOlatPropertyToLdapAttribute("userID")));
			idenity = ldapManager.findIdentityByLdapAuthentication(userAttrs, errors);
			if (idenity != null) {
				changedAttrMap = ldapManager.prepareUserPropertyForSync(userAttrs, idenity);
				if(changedAttrMap!= null) changedMapIdenityMap.put(idenity, changedAttrMap);
			} else  {
				if(errors.isEmpty()) {
				String[] reqAttrs = syncConfiguration.checkRequestAttributes(userAttrs);
				if(reqAttrs==null) newLdapUserList.add(userAttrs);
				else System.out.println("Cannot create User " + user + " required Attributes are missing");
				}
				else System.out.println(errors.get());
			} 
		}
		

		//create Users in LDAP Group only existing in OLAT 
		User user1 = UserManager.getInstance().createUser("hansi", "hürlima", "hansi@hansli.com");
		Identity identity1 = securityManager.createAndPersistIdentityAndUser("hansi", null, user1, "LDAP", "hansi");
		SecurityGroup secGroup1 = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
		securityManager.addIdentityToSecurityGroup(identity1, secGroup1);
		user1 = UserManager.getInstance().createUser("chaspi", "meier", "chaspi@hansli.com");
		identity1 = securityManager.createAndPersistIdentityAndUser("chaspi", null, user1, "LDAP", "chaspi");
		securityManager.addIdentityToSecurityGroup(identity1, secGroup1);

		//create User to Delete List
		deletedUserList = ldapManager.getIdentitysDeletedInLdap(ctx);
		assertEquals(4, (deletedUserList.size()));


		//sync users
		Iterator<Identity> itrIdent = changedMapIdenityMap.keySet().iterator();
		while(itrIdent.hasNext()){
			Identity ident = itrIdent.next();
			ldapManager.syncUser(changedMapIdenityMap.get(ident), ident);
		}


		//create all users
		for (int i = 0; i < newLdapUserList.size(); i++) {
			ldapManager.createAndPersistUser(newLdapUserList.get(i));
		}

		//delete all users
		ldapManager.deleteIdentities(deletedUserList, null);
		
		//check if users are deleted
		deletedUserList = ldapManager.getIdentitysDeletedInLdap(ctx);
		assertEquals(0, (deletedUserList.size()));

	}
	

	private String getAttributeValue(Attribute attribute)
	throws NamingException {
		String attrValue = (String)attribute.get();
		return attrValue;
	}
}
