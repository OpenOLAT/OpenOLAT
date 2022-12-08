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
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.basesecurity.model.SearchOrganisationParameters;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.webdav.manager.WebDAVAuthManager;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Encoder.Algorithm;
import org.olat.core.util.mail.MailPackage;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.ldap.LDAPError;
import org.olat.ldap.LDAPLoginManager;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.LDAPSyncConfiguration;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.restapi.RestConnection;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
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
public class LDAPLoginManagerTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private LDAPLoginModule ldapLoginModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private LDAPSyncConfiguration syncConfiguration;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@ClassRule
	public static final EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
	        .newInstance()
	        .usingDomainDsn("dc=olattest,dc=org")
	        .importingLdifs("org/olat/ldap/junittestdata/olattest.ldif")
	        .bindingToAddress("localhost")
	        .bindingToPort(1389)
	        .build();
	
	@Before
	public void resetSynchronizationSettings() {
		syncConfiguration.setLdapGroupBases(List.of());
		syncConfiguration.setLdapOrganisationsGroupBases(List.of());
		syncConfiguration.setCoachedGroupAttribute(null);
		syncConfiguration.setCoachedGroupAttributeSeparator(null);
		syncConfiguration.setCoachRoleAttribute(null);
		syncConfiguration.setCoachRoleValue(null);
		syncConfiguration.setGroupCoachAsParticipant("false");
		syncConfiguration.setAuthorRoleAttribute(null);
		syncConfiguration.setAuthorRoleValue(null);
		syncConfiguration.setQpoolManagerRoleAttribute(null);
		syncConfiguration.setQpoolManagerRoleValue(null);
		syncConfiguration.setUserManagerRoleAttribute(null);
		syncConfiguration.setUserManagerRoleValue(null);
		
		syncConfiguration.setUserManagersGroupBase(List.of());
		
		securityModule.setIdentityName("auto");
	}
	
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
	
	/**
	 * Synchronize the member of a LDAP group as participants.
	 */
	@Test
	public void syncGroupsParticipantsOnly() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("ldaplearning");
		List<BusinessGroup> ldapGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		Assert.assertEquals(1, ldapGroups.size());
		
		BusinessGroup ldapGroup = ldapGroups.get(0);
		Assert.assertEquals("ldaplearning", ldapGroup.getName());
		Assert.assertEquals("ldaplearning", ldapGroup.getExternalId());
		Assert.assertEquals("ldaplearning", ldapGroup.getDescription());
		Assert.assertEquals("membersmanagement,delete", ldapGroup.getManagedFlagsString());
		Assert.assertEquals(Boolean.FALSE, ldapGroup.getWaitingListEnabled());
		
		List<Identity> participants = businessGroupService.getMembers(ldapGroup, GroupRoles.participant.name());
		Assert.assertEquals(3, participants.size());
		assertThat(participants)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("halpen", "LP7AFreimann", "hcoulter");
		List<Identity> coaches = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name());		
		Assert.assertEquals(0, coaches.size());	
	}
	
	/**
	 * Synchronize the members of a LDAP group as participants,
	 * a member has an attribute with the list of the groups he
	 * coaches.
	 */
	@Test
	public void syncGroupsParticipantsAndCoachByAttribute() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setCoachedGroupAttribute("employeeType");
		syncConfiguration.setCoachedGroupAttributeSeparator(",");
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("ldaplearning");
		List<BusinessGroup> ldapGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		Assert.assertEquals(1, ldapGroups.size());
		
		BusinessGroup ldapGroup = ldapGroups.get(0);
		Assert.assertEquals("ldaplearning", ldapGroup.getName());
		Assert.assertEquals("ldaplearning", ldapGroup.getExternalId());
		Assert.assertEquals("ldaplearning", ldapGroup.getDescription());
		Assert.assertEquals("membersmanagement,delete", ldapGroup.getManagedFlagsString());
		Assert.assertEquals(Boolean.FALSE, ldapGroup.getWaitingListEnabled());
		
		List<Identity> participants = businessGroupService.getMembers(ldapGroup, GroupRoles.participant.name());
		Assert.assertEquals(2, participants.size());
		assertThat(participants)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("halpen", "hcoulter");
		
		List<Identity> coaches = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name());
		Assert.assertEquals(1, coaches.size());
		assertThat(coaches)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("LP7AFreimann");
	}
	
	/**
	 * Synchronize the members of the LDAP group as participants.
	 * Synchronization check the flag employeeNumber = 234 to find
	 * the coaches.
	 */
	@Test
	public void syncGroupsParticipantsAndCoachByRole() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setCoachRoleAttribute("employeeNumber");
		syncConfiguration.setCoachRoleValue("234");
		syncConfiguration.setGroupCoachAsParticipant("false");
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("ldaplearning");
		List<BusinessGroup> ldapGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		Assert.assertEquals(1, ldapGroups.size());
		
		BusinessGroup ldapGroup = ldapGroups.get(0);
		List<Identity> participants = businessGroupService.getMembers(ldapGroup, GroupRoles.participant.name());
		Assert.assertEquals(2, participants.size());
		assertThat(participants)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("LP7AFreimann", "hcoulter");
		
		List<Identity> coaches = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name());
		Assert.assertEquals(1, coaches.size());
		assertThat(coaches)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("halpen");
	}
	
	/**
	 * Synchronize the members of the LDAP group as participants.
	 * Synchronization check the flag employeeNumber = 234 to find
	 * the coaches. But with the setting: coach are automatically
	 * participant too.
	 */
	@Test
	public void syncGroupsParticipantsAndCoachAsParticipantByRole() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setLdapOrganisationsGroupBases(List.of());
		syncConfiguration.setCoachRoleAttribute("employeeNumber");
		syncConfiguration.setCoachRoleValue("234");
		syncConfiguration.setGroupCoachAsParticipant("true");
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("ldaplearning");
		List<BusinessGroup> ldapGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		Assert.assertEquals(1, ldapGroups.size());
		
		BusinessGroup ldapGroup = ldapGroups.get(0);
		List<Identity> participants = businessGroupService.getMembers(ldapGroup, GroupRoles.participant.name());
		Assert.assertEquals(3, participants.size());
		assertThat(participants)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("LP7AFreimann", "halpen", "hcoulter");
		
		List<Identity> coaches = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name());
		Assert.assertEquals(1, coaches.size());
		assertThat(coaches)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("halpen");
	}
	
	/**
	 * Synchronize the members of the LDAP group as participants.
	 * Members based on member attribute of the group (default) and
	 * memberOf attribute of the person.
	 */
	@Test
	public void syncGroupsParticipantsByAttribute() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setGroupAttribute("memberOf");
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("ldapteaching");
		List<BusinessGroup> ldapGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		Assert.assertEquals(1, ldapGroups.size());
		
		BusinessGroup ldapGroup = ldapGroups.get(0);
		List<Identity> participants = businessGroupService.getMembers(ldapGroup, GroupRoles.participant.name());
		Assert.assertEquals(2, participants.size());
		assertThat(participants)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("ahentschel", "hcoulter");
		
		List<Identity> coaches = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name());
		Assert.assertEquals(0, coaches.size());
	}
	
	/**
	 * Synchronize the members of the LDAP group as participants.
	 * Members based on member attribute of the group (default) and
	 * memberOf attribute of the person.
	 */
	@Test
	public void gSyncGroupsAuthorRole() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setAuthorRoleAttribute("employeeNumber");
		syncConfiguration.setAuthorRoleValue("author");
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		Identity author = securityManager.findIdentityByLogin("kmeier");
		Roles roles = securityManager.getRoles(author);
		// meier is author
		Assert.assertTrue(roles.isAuthor());
		// but nothing else
		Assert.assertFalse(roles.isAdministrator());
		Assert.assertFalse(roles.isCurriculumManager());
		Assert.assertFalse(roles.isGroupManager());
		Assert.assertFalse(roles.isLearnResourceManager());
		Assert.assertFalse(roles.isLectureManager());
		Assert.assertFalse(roles.isLineManager());
		Assert.assertFalse(roles.isPoolManager());
		Assert.assertFalse(roles.isPrincipal());
		Assert.assertFalse(roles.isQualityManager());
		Assert.assertFalse(roles.isRolesManager());
		Assert.assertFalse(roles.isSystemAdmin());
		Assert.assertFalse(roles.isUserManager());
	}
	
	@Test
	public void syncWithoutEmail() throws LDAPException {
		boolean emailMandatory = userModule.isEmailMandatory();
		userModule.setEmailMandatory(false);
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		Identity idWithoutEmail = securityManager.findIdentityByLogin("afrommenwiler");
		Assert.assertNotNull(idWithoutEmail);
		Assert.assertEquals("Annabelle", idWithoutEmail.getUser().getFirstName());
		Assert.assertEquals("Frommenwiler", idWithoutEmail.getUser().getLastName());
		Assert.assertNull(idWithoutEmail.getUser().getEmail());
		
		// make a change
		String dn = "uid=afrommenwiler,ou=person,dc=olattest,dc=org";
		List<Modification> modifications = new ArrayList<>();
		modifications.add(new Modification(ModificationType.REPLACE, "givenname", "Annabella"));
		embeddedLdapRule.ldapConnection().modify(dn, modifications);
		
		// simple sync
		ldapManager.doSyncSingleUserWithLoginAttribute(idWithoutEmail);
		dbInstance.commitAndCloseSession();
		
		Identity id = securityManager.loadIdentityByKey(idWithoutEmail.getKey());
		Assert.assertNotNull(id);
		Assert.assertEquals("Annabella", id.getUser().getFirstName());
		Assert.assertEquals("Frommenwiler", id.getUser().getLastName());
		Assert.assertNull(id.getUser().getEmail());

		userModule.setEmailMandatory(emailMandatory);
	}
	
	/**
	 * The email is written in OpenOlat and doesn't come from LDAP
	 * 
	 * @throws LDAPException
	 */
	@Test
	public void syncWithoutEmailOlatEmail() throws LDAPException {
		boolean emailMandatory = userModule.isEmailMandatory();
		userModule.setEmailMandatory(false);
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		Identity idWithoutEmail = securityManager.findIdentityByLogin("mbuchholz");
		Assert.assertNotNull(idWithoutEmail);
		Assert.assertEquals("Michel", idWithoutEmail.getUser().getFirstName());
		Assert.assertEquals("Buchholz", idWithoutEmail.getUser().getLastName());
		Assert.assertNull(idWithoutEmail.getUser().getEmail());
		
		User user = idWithoutEmail.getUser();
		user.setProperty(UserConstants.EMAIL, "michel.b@openolat.org");
		userManager.updateUserFromIdentity(idWithoutEmail);
		dbInstance.commitAndCloseSession();
		
		Identity id =  securityManager.loadIdentityByKey(idWithoutEmail.getKey());
		
		// simple sync
		ldapManager.doSyncSingleUserWithLoginAttribute(id);
		dbInstance.commitAndCloseSession();
		
		Identity reloadedId = securityManager.loadIdentityByKey(id.getKey());
		Assert.assertNotNull(reloadedId);
		Assert.assertEquals("Michel", reloadedId.getUser().getFirstName());
		Assert.assertEquals("Buchholz", reloadedId.getUser().getLastName());
		Assert.assertEquals("michel.b@openolat.org", reloadedId.getUser().getEmail());
		
		// batch sync
		boolean allOkTwice = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOkTwice);
		
		Identity resynchedId = securityManager.loadIdentityByKey(id.getKey());
		Assert.assertNotNull(resynchedId);
		Assert.assertEquals("Michel", resynchedId.getUser().getFirstName());
		Assert.assertEquals("Buchholz", resynchedId.getUser().getLastName());
		Assert.assertEquals("michel.b@openolat.org", resynchedId.getUser().getEmail());
		
		resynchedId.getUser().setProperty(UserConstants.EMAIL, null);
		userManager.updateUserFromIdentity(resynchedId);
		dbInstance.commitAndCloseSession();
		
		userModule.setEmailMandatory(emailMandatory);
	}
	
	@Test
	public void syncMultiValues() throws LDAPException {
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		Identity id = securityManager.findIdentityByLogin("vferro");
		Assert.assertNotNull(id);
		Assert.assertEquals("Valeria", id.getUser().getFirstName());
		Assert.assertEquals("Ferro", id.getUser().getLastName());
		Assert.assertEquals("0791234567,0797654321,0787654321", id.getUser().getProperty("genericTextProperty", null));
	}
	
	@Test
	public void getIdentitiesDeletedInLdap() {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		Identity orphan = JunitTestHelper.createAndPersistIdentityAsRndUser("ldap-orphan");
		securityManager.createAndPersistAuthentication(orphan, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER,
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
	public void syncUserGroups() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("admin-ldap");
		
		// sync the groups first (sync by user only doesn't creates the groups)
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		// first remove all members of the LDAP group
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("ldapcoaching");
		List<BusinessGroup> ldapGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		Assert.assertEquals(1, ldapGroups.size());

		BusinessGroup ldapGroup = ldapGroups.get(0);
		List<Identity> currentMembers = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name(), GroupRoles.participant.name());
		businessGroupService.removeMembers(admin, currentMembers, ldapGroup.getResource(), new MailPackage(false), true);
		dbInstance.commitAndCloseSession();
		
		int numOfMembers = businessGroupService.countMembers(ldapGroup, GroupRoles.coach.name(), GroupRoles.participant.name());
		Assert.assertEquals(0, numOfMembers);
		
		// set synchronization settings
		syncConfiguration.setCoachedGroupAttribute("memberOf");
		syncConfiguration.setCoachedGroupAttributeSeparator(",");
		
		Identity id1 = securityManager.findIdentityByLogin("dforster");
		ldapManager.syncUserGroups(id1);
		dbInstance.commitAndCloseSession();
		
		// has additional coach attribute
		Identity id2 = securityManager.findIdentityByLogin("gstieger");
		ldapManager.syncUserGroups(id2);
		dbInstance.commitAndCloseSession();
		
		List<Identity> participants = businessGroupService.getMembers(ldapGroup, GroupRoles.participant.name());
		Assert.assertEquals(1, participants.size());
		assertThat(participants)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("dforster");
		
		List<Identity> coaches = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name());
		Assert.assertEquals(1, coaches.size());
		assertThat(coaches)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("gstieger");
	}
	
	@Test
	public void syncUserGroupsCoachAsParticipantsToo() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("admin-ldap");
		
		// sync the groups first (sync by user only doesn't creates the groups)
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		// first remove all members of the LDAP group
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("ldapopenolat");
		List<BusinessGroup> ldapGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		Assert.assertEquals(1, ldapGroups.size());

		BusinessGroup ldapGroup = ldapGroups.get(0);
		List<Identity> currentMembers = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name(), GroupRoles.participant.name());
		businessGroupService.removeMembers(admin, currentMembers, ldapGroup.getResource(), new MailPackage(false), true);
		dbInstance.commitAndCloseSession();
		
		int numOfMembers = businessGroupService.countMembers(ldapGroup, GroupRoles.coach.name(), GroupRoles.participant.name());
		Assert.assertEquals(0, numOfMembers);
		
		// set synchronization settings
		syncConfiguration.setCoachedGroupAttribute("memberOf");
		syncConfiguration.setCoachedGroupAttributeSeparator(",");
		syncConfiguration.setGroupCoachAsParticipant("true");
		
		Identity id1 = securityManager.findIdentityByLogin("dforster");
		ldapManager.syncUserGroups(id1);
		dbInstance.commitAndCloseSession();
		
		// has additional coach attribute
		Identity id2 = securityManager.findIdentityByLogin("gstieger");
		ldapManager.syncUserGroups(id2);
		dbInstance.commitAndCloseSession();
		
		List<Identity> participants = businessGroupService.getMembers(ldapGroup, GroupRoles.participant.name());
		Assert.assertEquals(2, participants.size());
		assertThat(participants)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("dforster", "gstieger");
		
		List<Identity> coaches = businessGroupService.getMembers(ldapGroup, GroupRoles.coach.name());
		Assert.assertEquals(1, coaches.size());
		assertThat(coaches)
			.extracting(id -> id.getUser())
			.extracting(user -> user.getNickName())
			.containsExactlyInAnyOrder("gstieger");
	}
	

	@Test
	public void syncUserGroupsRemove() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("admin-ldap");
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setExactName("ldapopenolat");
		List<BusinessGroup> ldapGroups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		Assert.assertEquals(1, ldapGroups.size());

		BusinessGroup ldapGroup = ldapGroups.get(0);
		
		String externalId = UUID.randomUUID().toString();
		BusinessGroup managedGroup = businessGroupService.createBusinessGroup(admin, "External LDAP Group", "", BusinessGroup.BUSINESS_TYPE,
				externalId, null, null, null, false, false, null);
		Identity id1 = securityManager.findIdentityByLogin("dforster");
		businessGroupRelationDao.addRole(id1, managedGroup, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// set synchronization settings
		syncConfiguration.setCoachedGroupAttribute("memberOf");
		syncConfiguration.setCoachedGroupAttributeSeparator(",");
		syncConfiguration.setGroupCoachAsParticipant("true");
		
		id1 = securityManager.findIdentityByLogin("dforster");
		ldapManager.syncUserGroups(id1);
		dbInstance.commitAndCloseSession();
		
		// check the user is still in the real LDAP group
		List<Identity> participants = businessGroupService.getMembers(ldapGroup, GroupRoles.participant.name());
		Assert.assertFalse(participants.isEmpty());
		Assert.assertTrue(participants.contains(id1));
		// check the user was removed from the mocked managed group
		List<Identity> managedParticipants = businessGroupService.getMembers(managedGroup, GroupRoles.participant.name());
		Assert.assertTrue(managedParticipants.isEmpty());
		Assert.assertFalse(managedParticipants.contains(id1));
	}
	
	@Test
	public void syncUserOrganisationsRolesByAttribute() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapOrganisationsGroupFilter("(&(objectClass=groupOfNames)(|(cn=ldaporganisation)(cn=ldaplearning)))");
		syncConfiguration.setLdapOrganisationsGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setQpoolManagerRoleAttribute("employeeNumber");
		syncConfiguration.setQpoolManagerRoleValue("poolmanager");

		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchOrganisationParameters params = new SearchOrganisationParameters();
		params.setExternalId("ldaporganisation");
		List<Organisation> ldapOrganisations = organisationDao.findOrganisations(params);
		assertThat(ldapOrganisations)
			.isNotNull()
			.hasSize(1)
			.map(Organisation::getDisplayName)
			.containsExactlyInAnyOrder("ldaporganisation");
		
		List<Identity> poolManagers = organisationDao
				.getNonInheritedMembersIdentity(ldapOrganisations.get(0), OrganisationRoles.poolmanager.name());
		assertThat(poolManagers)
			.isNotNull()
			.hasSize(1)
			.map(Identity::getUser)
			.map(User::getLastName)
			.containsExactlyInAnyOrder("Martin");
	}
	
	@Test
	public void syncUserOrganisationsRolesByAdditionalGroups() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapOrganisationsGroupFilter("(&(objectClass=groupOfNames)(|(cn=ldaporganisation)(cn=ldapempty)))");
		syncConfiguration.setLdapOrganisationsGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setLdapGroupFilter("(&(objectClass=groupOfNames)(cn=ldapcoaching))");
		syncConfiguration.setUserManagersGroupBase(List.of("ou=groups,dc=olattest,dc=org"));

		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchOrganisationParameters params = new SearchOrganisationParameters();
		params.setExternalId("ldaporganisation");
		List<Organisation> ldapOrganisations = organisationDao.findOrganisations(params);
		assertThat(ldapOrganisations)
			.isNotNull()
			.hasSize(1)
			.map(Organisation::getDisplayName)
			.containsExactlyInAnyOrder("ldaporganisation");
		
		List<Identity> userManagers = organisationDao
				.getNonInheritedMembersIdentity(ldapOrganisations.get(0), OrganisationRoles.usermanager.name());
		assertThat(userManagers)
			.isNotNull()
			.hasSize(1)
			.map(Identity::getUser)
			.map(User::getLastName)
			.containsExactlyInAnyOrder("Forster");
	}
	
	/**
	 * Configure user manager synchronization by attribute and than
	 * with a dummy value to see if the managers are veicted.
	 * 
	 * @throws LDAPException
	 */
	@Test
	public void syncUserOrganisationsRolesEviction() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapOrganisationsGroupFilter("(&(objectClass=groupOfNames)(|(cn=ldaporganisation)(cn=ldapempty)))");
		syncConfiguration.setLdapOrganisationsGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setLdapGroupFilter("(&(objectClass=groupOfNames)(cn=ldapcoaching))");
		syncConfiguration.setUserManagerRoleAttribute("employeeNumber");
		syncConfiguration.setUserManagerRoleValue("usermanager");

		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchOrganisationParameters params = new SearchOrganisationParameters();
		params.setExternalId("ldaporganisation");
		List<Organisation> ldapOrganisations = organisationDao.findOrganisations(params);
		assertThat(ldapOrganisations)
			.isNotNull()
			.hasSize(1)
			.map(Organisation::getDisplayName)
			.containsExactlyInAnyOrder("ldaporganisation");
		
		// At least one user manager
		List<Identity> userManagers = organisationDao
				.getNonInheritedMembersIdentity(ldapOrganisations.get(0), OrganisationRoles.usermanager.name());
		assertThat(userManagers)
			.isNotNull()
			.map(Identity::getUser)
			.map(User::getLastName)
			.containsAnyOf("Ferro");
		
		// A configuration which match nobody
		syncConfiguration.setUserManagerRoleValue("notreallymanager");
		
		allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		List<Identity> notReallyManagers = organisationDao
				.getNonInheritedMembersIdentity(ldapOrganisations.get(0), OrganisationRoles.usermanager.name());
		assertThat(notReallyManagers)
			.isNotNull()
			.isEmpty();
	}
	
	/**
	 * Synchronize organizations with LDAP groups and check that users
	 * which are not member of a group, go in the "Lost and found" organization.
	 * 
	 * @throws LDAPException
	 */
	@Test
	public void syncUserOrganisationsLostAndFound() throws LDAPException {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		syncConfiguration.setLdapOrganisationsGroupFilter("(&(objectClass=groupOfNames)(|(cn=ldaporganisation)(cn=ldapopenolat)))");
		syncConfiguration.setLdapOrganisationsGroupBases(List.of("ou=groups,dc=olattest,dc=org"));
		syncConfiguration.setLdapGroupFilter("(&(objectClass=groupOfNames)(cn=ldapcoaching))");

		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
		
		SearchOrganisationParameters params = new SearchOrganisationParameters();
		params.setExternalId("Lost and found");
		List<Organisation> lostAndFoundOrganisations = organisationDao.findOrganisations(params);
		assertThat(lostAndFoundOrganisations)
			.isNotNull()
			.hasSize(1)
			.map(Organisation::getDisplayName)
			.containsExactlyInAnyOrder("Lost and found");
		
		// At least some lost users
		List<Identity> users = organisationDao
				.getMembersIdentity(lostAndFoundOrganisations.get(0), OrganisationRoles.user.name());
		assertThat(users)
			.isNotNull()
			.map(Identity::getUser)
			.map(User::getLastName)
			.containsAnyOf("Meier", "Rohrer")
			.doesNotContain("Ferro", "Forster", "Martin", "Stieger");
		
		// Add an additional LDAP group, stieger must disappear from the organization
		syncConfiguration.setLdapOrganisationsGroupFilter("(&(objectClass=groupOfNames)(|(cn=ldaporganisation)(cn=ldapopenolat)(cn=ldapcoaching)))");
		
		allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);
	
		List<Identity> updateUsers = organisationDao
				.getMembersIdentity(lostAndFoundOrganisations.get(0), OrganisationRoles.user.name());
		assertThat(updateUsers)
			.isNotNull()
			.map(Identity::getUser)
			.map(User::getLastName)
			.containsAnyOf("Meier", "Rohrer", "Stieger")
			.doesNotContain("Ferro", "Forster", "Martin");
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
		Authentication ha1Authentication = securityManager.findAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(ha1Authentication);
		Assert.assertEquals("hadigest", ha1Authentication.getAuthusername());
		
		Authentication ha1EAuthentication = securityManager.findAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1_EMAIL, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(ha1EAuthentication);
		Assert.assertEquals("ha1digest@openolat.com", ha1EAuthentication.getAuthusername());

		Authentication ldapAuthentication = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(ldapAuthentication);
		
		// sync user
		String dn = "uid=hadigest,ou=person,dc=olattest,dc=org";
		Modification mod = new Modification(ModificationType.REPLACE, "mail", "haedigest@frentix.com");
		embeddedLdapRule.ldapConnection().modify(dn, mod);
		
		boolean updateAllOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(updateAllOk);
		
		// check authentications
		Authentication validHa1Authentication = securityManager.findAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(validHa1Authentication);
		Assert.assertEquals("hadigest", validHa1Authentication.getAuthusername());
		
		Authentication invalidHa1EAuthentication = securityManager.findAuthentication(identity, WebDAVAuthManager.PROVIDER_HA1_EMAIL, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNull(invalidHa1EAuthentication);
	
		Authentication validLdapAuthentication = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
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
		Authentication authentication = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(authentication);
		securityManager.deleteAuthentication(authentication);
		securityManager.createAndPersistAuthentication(identity, "OLAT", BaseSecurity.DEFAULT_ISSUER, "cguerrera", "secret", Algorithm.sha512);
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
	public void findIdentityByLdapAuthenticationConvertFromOlat2Logins() {
		LDAPError ldapError = new LDAPError();
		
		// Take an LDAP user, delete her LDAP authentication
		List<String> firstLastName = Collections.singletonList("Ursula Schelling");
		List<FindNamedIdentity> foundIdentities = securityManager.findIdentitiesBy(firstLastName);
		Assert.assertEquals(1, foundIdentities.size());
		
		Identity identity = foundIdentities.get(0).getIdentity();
		Authentication authentication = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(authentication);
		Assert.assertTrue(authentication.getAuthusername().contains("chelling"));
		securityManager.deleteAuthentication(authentication);
		securityManager.createAndPersistAuthentication(identity, "OLAT", BaseSecurity.DEFAULT_ISSUER, "uschelling", "secret", Algorithm.sha512);
		dbInstance.commitAndCloseSession();
		
		// convert users to LDAP
		boolean currentConvertExistingLocalUsers = ldapLoginModule.isConvertExistingLocalUsersToLDAPUsers();
		ldapLoginModule.setConvertExistingLocalUsersToLDAPUsers(true);
		String currentLoginAttribute = syncConfiguration.getLdapUserLoginAttribute();
		syncConfiguration.setLdapUserLoginAttribute("sn," + currentLoginAttribute);
		
		// use uid as login attribute	
		Attributes attrs = ldapManager.bindUser("uschelling", "olat", ldapError);
		Assert.assertNotNull(attrs);

		LDAPError errors = new LDAPError();
		Identity convertedIdentity = ldapManager.findIdentityByLdapAuthentication(attrs, errors);
		Assert.assertNotNull(convertedIdentity);
		Assert.assertEquals("Ursula", convertedIdentity.getUser().getFirstName());
		Assert.assertEquals("Schelling", convertedIdentity.getUser().getLastName());

		Authentication updatedAuthentication = securityManager.findAuthentication(identity, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(updatedAuthentication);
		Assert.assertEquals("Schelling", updatedAuthentication.getAuthusername());

		// revert configuration
		ldapLoginModule.setConvertExistingLocalUsersToLDAPUsers(currentConvertExistingLocalUsers);
		syncConfiguration.setLdapUserLoginAttribute(currentLoginAttribute);
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
		
		securityModule.setIdentityName("manual");
		
		// Create the user without OLAT login
		User user = userManager.createUser("Leyla", "Salathe", "lsalathe@openolat.com");
		Identity identity = securityManager.createAndPersistIdentityAndUser("lsalathe", null, null, user, null, null, null, null, null);
		
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
		
		// check
		Assert.assertEquals(identity, convertedIdentity);
		Authentication authentication = securityManager.findAuthentication(convertedIdentity, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(authentication);
	}
	
	@Test
	public void renameLDAPViaRest() throws Exception {
		Assume.assumeTrue(ldapLoginModule.isLDAPEnabled());
		
		LDAPError errors = new LDAPError();
		boolean allOk = ldapManager.doBatchSync(errors);
		Assert.assertTrue(allOk);

		// try
		Attributes attrs = ldapManager.bindUser("cbraben", "olat", errors);
		Assert.assertNotNull(attrs);
		Assert.assertEquals("cbraben", attrs.get("uid").get());
		Assert.assertTrue(errors.isEmpty());
		
		// change the username of the LDAP token and the nick name
		Authentication ldapAuthentication = securityManager.findAuthenticationByAuthusername("cbraben", LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		Identity id = ldapAuthentication.getIdentity();
		ldapAuthentication.setAuthusername("bbraben");
		authenticationDao.updateAuthentication(ldapAuthentication);
		id.getUser().setProperty(UserConstants.NICKNAME, "bbraben");
		userManager.updateUserFromIdentity(id);
		dbInstance.commitAndCloseSession();
		
		// check our changes
		Authentication changedAuth = securityManager.findAuthentication(id, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertEquals("bbraben", changedAuth.getAuthusername());
		dbInstance.commitAndCloseSession();
		
		// change it back via REST
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		String newUsername = "cbraben";
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(id.getKey().toString())
				.path("username").queryParam("username", newUsername).build();
		
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		Identity renamedId = securityManager.loadIdentityByKey(id.getKey());
		Assert.assertEquals(newUsername, renamedId.getUser().getNickName());
		
		Authentication auth = securityManager.findAuthentication(id, LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(auth);
		Assert.assertEquals(newUsername, auth.getAuthusername());
	}
}
