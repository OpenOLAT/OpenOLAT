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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.naming.ldap.LdapContext;

import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.model.LDAPGroup;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

/**
 * 
 * Initial date: 23 nov. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LDAPDAOTest extends OlatTestCase {
	
	@Autowired
	private LDAPDAO ldapDao;
	@Autowired
	private LDAPLoginManager ldapManager;
	
	@ClassRule
	public static final EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
	        .newInstance()
	        .usingDomainDsn("dc=olattest,dc=org")
	        .importingLdifs("org/olat/ldap/junittestdata/olattest.ldif")
	        .bindingToAddress("localhost")
	        .bindingToPort(1389)
	        .build();
	
	@Test
	public void searchGroups() {	
		LdapContext ctx = ldapManager.bindSystem();
		List<String> bases = List.of("ou=groups,dc=olattest,dc=org");
		List<LDAPGroup> onlyGroups = ldapDao.searchGroups(ctx, bases);
		assertThat(onlyGroups)
			.isNotNull()
			.hasSize(4)
			.map(LDAPGroup::getCommonName)
			.containsExactlyInAnyOrder("ldaplearning", "ldapteaching", "ldapcoaching", "ldapopenolat");
	}

	@Test
	public void searchAllGroupsWithFilter() {	
		LdapContext ctx = ldapManager.bindSystem();
		List<String> bases = List.of("ou=groups,dc=olattest,dc=org");
		String filter = "(objectClass=groupOfNames)";
		List<LDAPGroup> onlyGroups = ldapDao.searchGroups(ctx, bases, filter);
		assertThat(onlyGroups)
			.isNotNull()
			.hasSize(4)
			.map(LDAPGroup::getCommonName)
			.containsExactlyInAnyOrder("ldaplearning", "ldapteaching", "ldapcoaching", "ldapopenolat");
	}
	
	@Test
	public void searchAllGroupsExcludedSomeCns() {	
		LdapContext ctx = ldapManager.bindSystem();
		List<String> bases = List.of("ou=groups,dc=olattest,dc=org");
		String filter = "(&(objectClass=groupOfNames)(!(cn=ldapteaching))(!(cn=ldaplearning)))";
		List<LDAPGroup> onlyGroups = ldapDao.searchGroups(ctx, bases, filter);
		assertThat(onlyGroups)
			.isNotNull()
			.hasSize(2)
			.map(LDAPGroup::getCommonName)
			.containsExactlyInAnyOrder("ldapcoaching", "ldapopenolat");
	}
	
	@Test
	public void searchGroupsWithSpecificMember() {	
		LdapContext ctx = ldapManager.bindSystem();
		List<String> bases = List.of("ou=groups,dc=olattest,dc=org");
		String filter = "(&(objectClass=groupOfNames)(member=uid=dforster,ou=person,dc=olattest,dc=org))";
		List<LDAPGroup> onlyGroups = ldapDao.searchGroups(ctx, bases, filter);
		assertThat(onlyGroups)
			.isNotNull()
			.hasSize(2)
			.map(LDAPGroup::getCommonName)
			.containsExactlyInAnyOrder("ldapcoaching", "ldapopenolat");
	}

	@Test
	public void searchGroupsWithSpecificMemberAndExcludeGroups() {	
		LdapContext ctx = ldapManager.bindSystem();
		List<String> bases = List.of("ou=groups,dc=olattest,dc=org");
		String filter = "(&(objectClass=groupOfNames)(!(cn=ldapteaching))(!(cn=ldapcoaching))(member=uid=dforster,ou=person,dc=olattest,dc=org))";
		List<LDAPGroup> onlyGroups = ldapDao.searchGroups(ctx, bases, filter);
		assertThat(onlyGroups)
			.isNotNull()
			.hasSize(1)
			.map(LDAPGroup::getCommonName)
			.containsExactlyInAnyOrder("ldapopenolat");
	}
}
