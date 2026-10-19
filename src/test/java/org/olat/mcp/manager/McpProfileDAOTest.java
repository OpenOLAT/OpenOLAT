/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.mcp.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.model.McpProfileImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpProfileDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpProfileDAO mcpProfileDao;
	@Autowired
	private OrganisationService organisationService;

	private final List<McpProfile> createdProfiles = new ArrayList<>();

	@After
	public void deleteProfiles() {
		for(McpProfile profile:createdProfiles) {
			mcpProfileDao.deleteProfile(profile);
		}
		createdProfiles.clear();
		dbInstance.commitAndCloseSession();
	}

	private McpProfile createProfile(McpProfileType type, boolean enabled, Set<String> tools, List<OrganisationRoles> roles) {
		return createProfile(type, enabled, tools, roles, JunitTestHelper.getDefaultOrganisation());
	}

	private McpProfile createProfile(McpProfileType type, boolean enabled, Set<String> tools, List<OrganisationRoles> roles,
			Organisation organisation) {
		McpProfile profile = mcpProfileDao.createProfile(randomIdentifier(), "DAO test", type);
		mcpProfileDao.createOrganisationRelation(profile, organisation);
		profile.setEnabled(enabled);
		profile.setTools(tools);
		profile.setOrganisationRoles(roles);
		profile = mcpProfileDao.updateProfile(profile);
		createdProfiles.add(profile);
		return profile;
	}

	private static String randomIdentifier() {
		return "dao-" + UUID.randomUUID();
	}

	@Test
	public void createProfile() {
		String identifier = randomIdentifier();
		McpProfile profile = mcpProfileDao.createProfile(identifier, "Create", McpProfileType.user);
		createdProfiles.add(profile);
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(profile.getKey());
		McpProfile reloaded = mcpProfileDao.loadByKey(profile.getKey());
		Assert.assertNotNull(reloaded);
		Assert.assertNotNull(reloaded.getCreationDate());
		Assert.assertNotNull(reloaded.getLastModified());
		Assert.assertEquals(identifier, reloaded.getIdentifier());
		Assert.assertEquals("Create", reloaded.getName());
		Assert.assertNull(reloaded.getDescription());
		Assert.assertFalse(reloaded.isEnabled());
		Assert.assertEquals(McpProfileType.user, reloaded.getType());
		Assert.assertTrue(reloaded.getTools().isEmpty());
		Assert.assertTrue(reloaded.getOrganisationRoles().isEmpty());
		Assert.assertNull(reloaded.getRateLimitRequestsPerMinute());
		Assert.assertEquals("[]", ((McpProfileImpl)reloaded).getToolsJson());
	}

	@Test
	public void updateProfile_allFields() {
		McpProfile profile = mcpProfileDao.createProfile(randomIdentifier(), "Before", McpProfileType.system);
		createdProfiles.add(profile);
		dbInstance.commitAndCloseSession();

		profile.setName("After");
		profile.setDescription("A long description\nwith two lines");
		profile.setEnabled(true);
		profile.setTools(Set.of("todo_query", "course_query", " lecture_query ", "", " course_query"));
		profile.setOrganisationRoles(List.of(OrganisationRoles.author, OrganisationRoles.user, OrganisationRoles.author));
		profile.setRateLimitRequestsPerMinute(Integer.valueOf(240));
		mcpProfileDao.updateProfile(profile);
		dbInstance.commitAndCloseSession();

		McpProfile reloaded = mcpProfileDao.loadByKey(profile.getKey());
		Assert.assertEquals("After", reloaded.getName());
		Assert.assertEquals("A long description\nwith two lines", reloaded.getDescription());
		Assert.assertTrue(reloaded.isEnabled());
		Assert.assertEquals(McpProfileType.system, reloaded.getType());
		assertThat(reloaded.getTools())
			.containsExactly("course_query", "lecture_query", "todo_query");
		Assert.assertEquals("[\"course_query\",\"lecture_query\",\"todo_query\"]", ((McpProfileImpl)reloaded).getToolsJson());
		// enumeration order, no duplicates
		assertThat(reloaded.getOrganisationRoles())
			.containsExactly(OrganisationRoles.author, OrganisationRoles.user);
		Assert.assertEquals("author,user", ((McpProfileImpl)reloaded).getOrganisationRolesString());
		Assert.assertEquals(Integer.valueOf(240), reloaded.getRateLimitRequestsPerMinute());

		// reset the optional fields
		reloaded.setDescription(null);
		reloaded.setTools(null);
		reloaded.setOrganisationRoles(null);
		reloaded.setRateLimitRequestsPerMinute(null);
		mcpProfileDao.updateProfile(reloaded);
		dbInstance.commitAndCloseSession();

		McpProfile reset = mcpProfileDao.loadByKey(profile.getKey());
		Assert.assertNull(reset.getDescription());
		Assert.assertTrue(reset.getTools().isEmpty());
		Assert.assertTrue(reset.getOrganisationRoles().isEmpty());
		Assert.assertNull(((McpProfileImpl)reset).getOrganisationRolesString());
		Assert.assertNull(reset.getRateLimitRequestsPerMinute());
	}

	@Test
	public void tools_unmodifiable() {
		McpProfile profile = createProfile(McpProfileType.user, false, Set.of("course_query"), null);
		dbInstance.commitAndCloseSession();

		McpProfile reloaded = mcpProfileDao.loadByKey(profile.getKey());
		Assert.assertThrows(UnsupportedOperationException.class, () -> reloaded.getTools().add("todo_modify"));
		Assert.assertThrows(UnsupportedOperationException.class, () -> reloaded.getOrganisationRoles().add(OrganisationRoles.user));
	}

	@Test
	public void parseTolerant() {
		McpProfileImpl profile = new McpProfileImpl();
		profile.setToolsJson("not json");
		Assert.assertTrue(profile.getTools().isEmpty());
		profile.setToolsJson(null);
		Assert.assertTrue(profile.getTools().isEmpty());
		profile.setToolsJson("[\"todo_query\",null,\"\"]");
		assertThat(profile.getTools()).containsExactly("todo_query");

		profile.setOrganisationRolesString("user, unknown ,author,");
		assertThat(profile.getOrganisationRoles())
			.containsExactly(OrganisationRoles.author, OrganisationRoles.user);
		Assert.assertNull(McpProfileImpl.toRolesString(List.of()));
		Assert.assertNull(McpProfileImpl.toRolesString(null));
	}

	@Test
	public void loadByIdentifier() {
		McpProfile profile = createProfile(McpProfileType.user, false, null, null);
		dbInstance.commitAndCloseSession();

		McpProfile loaded = mcpProfileDao.loadByIdentifier(profile.getIdentifier());
		Assert.assertEquals(profile, loaded);
		Assert.assertNull(mcpProfileDao.loadByIdentifier("dao-does-not-exist-" + UUID.randomUUID()));
		Assert.assertNull(mcpProfileDao.loadByKey(Long.valueOf(-1l)));
	}

	@Test
	public void loadProfiles() {
		McpProfile enabledProfile = createProfile(McpProfileType.user, true, null, null);
		McpProfile disabledProfile = createProfile(McpProfileType.system, false, null, null);
		dbInstance.commitAndCloseSession();

		assertThat(mcpProfileDao.loadProfiles())
			.contains(enabledProfile, disabledProfile);
		Assert.assertTrue(mcpProfileDao.countProfiles() >= 2);
	}

	@Test
	public void grants() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-2");
		McpProfile profile = createProfile(McpProfileType.user, true, null, null);
		McpProfile otherProfile = createProfile(McpProfileType.user, false, null, null);
		mcpProfileDao.createGrant(profile, id1);
		mcpProfileDao.createGrant(profile, id2);
		mcpProfileDao.createGrant(otherProfile, id1);
		dbInstance.commitAndCloseSession();

		Assert.assertTrue(mcpProfileDao.hasGrant(profile, id1));
		Assert.assertTrue(mcpProfileDao.hasGrant(profile, id2));
		Assert.assertTrue(mcpProfileDao.hasGrant(otherProfile, id1));
		Assert.assertFalse(mcpProfileDao.hasGrant(otherProfile, id2));
		Assert.assertEquals(2, mcpProfileDao.countGrants(id1));
		Assert.assertEquals(1, mcpProfileDao.countGrants(id2));
		Assert.assertEquals(2, mcpProfileDao.countGrantedIdentities(profile));

		List<Identity> identities = mcpProfileDao.loadGrantedIdentities(profile, 0, -1);
		assertThat(identities).containsExactly(id1, id2);
		// the user is fetched
		Assert.assertEquals(id1.getUser().getLastName(), identities.get(0).getUser().getLastName());

		List<Identity> firstPage = mcpProfileDao.loadGrantedIdentities(profile, 0, 1);
		assertThat(firstPage).containsExactly(id1);
		List<Identity> secondPage = mcpProfileDao.loadGrantedIdentities(profile, 1, 1);
		assertThat(secondPage).containsExactly(id2);

		Assert.assertEquals(1, mcpProfileDao.deleteGrant(profile, id1));
		Assert.assertEquals(0, mcpProfileDao.deleteGrant(profile, id1));
		dbInstance.commitAndCloseSession();

		Assert.assertFalse(mcpProfileDao.hasGrant(profile, id1));
		Assert.assertTrue(mcpProfileDao.hasGrant(otherProfile, id1));
		Assert.assertEquals(1, mcpProfileDao.countGrants(id1));
		assertThat(mcpProfileDao.loadGrantedIdentities(profile, 0, -1)).containsExactly(id2);
	}

	@Test
	public void deleteGrantsOfIdentity() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-3");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-4");
		McpProfile profile1 = createProfile(McpProfileType.user, true, null, null);
		McpProfile profile2 = createProfile(McpProfileType.user, true, null, null);
		mcpProfileDao.createGrant(profile1, id);
		mcpProfileDao.createGrant(profile2, id);
		mcpProfileDao.createGrant(profile1, otherId);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(2, mcpProfileDao.deleteGrants(id));
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(0, mcpProfileDao.countGrants(id));
		Assert.assertTrue(mcpProfileDao.hasGrant(profile1, otherId));
	}

	@Test
	public void deleteProfile_deletesGrants() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-5");
		McpProfile profile = createProfile(McpProfileType.user, true, null, null);
		McpProfile otherProfile = createProfile(McpProfileType.user, true, null, null);
		mcpProfileDao.createGrant(profile, id);
		mcpProfileDao.createGrant(otherProfile, id);
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(1, mcpProfileDao.deleteProfile(profile));
		dbInstance.commitAndCloseSession();

		Assert.assertNull(mcpProfileDao.loadByKey(profile.getKey()));
		Assert.assertEquals(0, mcpProfileDao.countGrantedIdentities(profile));
		Assert.assertEquals(1, mcpProfileDao.countGrants(id));
		Assert.assertTrue(mcpProfileDao.hasGrant(otherProfile, id));
	}

	@Test
	public void loadEnabledProfilesOf() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-6");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-7");
		organisationService.addMember(JunitTestHelper.getDefaultOrganisation(), id, OrganisationRoles.linemanager, null);
		// user profiles
		McpProfile granted = createProfile(McpProfileType.user, true, Set.of("todo_query"), null);
		McpProfile grantedDisabled = createProfile(McpProfileType.user, false, Set.of("todo_modify"), null);
		McpProfile notGranted = createProfile(McpProfileType.user, true, Set.of("project_query"), null);
		// system profiles
		McpProfile qualityManagers = createProfile(McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.qualitymanager, OrganisationRoles.linemanager));
		McpProfile qualityManagersDisabled = createProfile(McpProfileType.system, false, Set.of("course_query"),
				List.of(OrganisationRoles.qualitymanager));
		McpProfile educationManagers = createProfile(McpProfileType.system, true, Set.of("lecture_query"),
				List.of(OrganisationRoles.educationmanager));
		McpProfile noRoles = createProfile(McpProfileType.system, true, Set.of("lecture_query"), null);
		// a grant on a system profile has no effect
		mcpProfileDao.createGrant(educationManagers, id);
		mcpProfileDao.createGrant(granted, id);
		mcpProfileDao.createGrant(grantedDisabled, id);
		mcpProfileDao.createGrant(notGranted, otherId);
		dbInstance.commitAndCloseSession();

		List<McpProfile> profiles = mcpProfileDao.loadEnabledProfilesOf(id);
		assertThat(profiles)
			.contains(granted, qualityManagers)
			.doesNotContain(grantedDisabled, notGranted, qualityManagersDisabled, educationManagers, noRoles)
			.allMatch(McpProfile::isEnabled)
			.isSortedAccordingTo((p1, p2) -> p1.getIdentifier().compareTo(p2.getIdentifier()));

		List<McpProfile> otherProfiles = mcpProfileDao.loadEnabledProfilesOf(otherId);
		assertThat(otherProfiles)
			.contains(notGranted)
			.doesNotContain(granted, grantedDisabled, qualityManagers, qualityManagersDisabled, educationManagers, noRoles);
	}

	/**
	 * A role is not matched as a part of another role name: the profile of
	 * the user managers does not match the role user.
	 */
	@Test
	public void loadEnabledProfilesOf_roleNameBoundaries() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-8");
		McpProfile userManagers = createProfile(McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.usermanager));
		McpProfile users = createProfile(McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.author, OrganisationRoles.user));
		dbInstance.commitAndCloseSession();

		assertThat(mcpProfileDao.loadEnabledProfilesOf(id))
			.contains(users)
			.doesNotContain(userManagers);
	}

	/**
	 * Organisation tree: the default organisation (root), A below the root,
	 * A1 below A and B below the root.
	 */
	@Test
	public void loadEnabledProfilesOf_organisationScope() {
		Organisation root = JunitTestHelper.getDefaultOrganisation();
		Organisation orgA = organisationService.createOrganisation("MCP A", "mcp-a-" + UUID.randomUUID(), null, root, null, null);
		Organisation orgA1 = organisationService.createOrganisation("MCP A1", "mcp-a1-" + UUID.randomUUID(), null, orgA, null, null);
		Organisation orgB = organisationService.createOrganisation("MCP B", "mcp-b-" + UUID.randomUUID(), null, root, null, null);
		Identity authorA1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-9");
		Identity authorB = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-10");
		Identity userA1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-11");
		Identity userB = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-12");
		organisationService.addMember(orgA1, authorA1, OrganisationRoles.author, null);
		organisationService.addMember(orgB, authorB, OrganisationRoles.author, null);
		organisationService.addMember(orgA1, userA1, OrganisationRoles.user, null);
		organisationService.addMember(orgB, userB, OrganisationRoles.user, null);

		McpProfile authorsA = createProfile(McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.author), orgA);
		McpProfile pilotsA = createProfile(McpProfileType.user, true, Set.of("project_query"), null, orgA);
		mcpProfileDao.createGrant(pilotsA, userA1);
		mcpProfileDao.createGrant(pilotsA, userB);
		dbInstance.commitAndCloseSession();

		// a system profile on A matches the role in A1, not in B
		assertThat(mcpProfileDao.loadEnabledProfilesOf(authorA1)).contains(authorsA).doesNotContain(pilotsA);
		assertThat(mcpProfileDao.loadEnabledProfilesOf(authorB)).doesNotContain(authorsA, pilotsA);
		// a user profile on A needs the role user in A or below
		assertThat(mcpProfileDao.loadEnabledProfilesOf(userA1)).contains(pilotsA).doesNotContain(authorsA);
		assertThat(mcpProfileDao.loadEnabledProfilesOf(userB)).doesNotContain(pilotsA, authorsA);
	}

	@Test
	public void organisationRelations() {
		Organisation root = JunitTestHelper.getDefaultOrganisation();
		Organisation orgA = organisationService.createOrganisation("MCP rel A", "mcp-rel-a-" + UUID.randomUUID(), null, root, null, null);
		McpProfile profile = createProfile(McpProfileType.user, false, null, null, orgA);
		McpProfile otherProfile = createProfile(McpProfileType.system, false, null, null);
		mcpProfileDao.createOrganisationRelation(profile, root);
		dbInstance.commitAndCloseSession();

		assertThat(mcpProfileDao.loadOrganisations(profile)).containsExactlyInAnyOrder(root, orgA);
		Assert.assertEquals(2, mcpProfileDao.countOrganisations(profile));
		assertThat(mcpProfileDao.loadProfiles(orgA)).containsExactly(profile);
		assertThat(mcpProfileDao.loadProfiles(root)).contains(profile, otherProfile);
		assertThat(mcpProfileDao.loadOrganisationRelations())
			.anyMatch(rel -> rel.getProfile().equals(profile) && rel.getOrganisation().equals(orgA));

		Assert.assertEquals(1, mcpProfileDao.deleteOrganisationRelation(profile, root));
		Assert.assertEquals(1, mcpProfileDao.deleteOrganisationRelations(orgA));
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(0, mcpProfileDao.countOrganisations(profile));

		// the delete of a profile removes its relations
		Assert.assertEquals(1, mcpProfileDao.deleteProfile(otherProfile));
		dbInstance.commitAndCloseSession();
		assertThat(mcpProfileDao.loadProfiles(root)).doesNotContain(otherProfile);
	}

	@Test
	public void loadUserProfilesVisibleFor() {
		Organisation root = JunitTestHelper.getDefaultOrganisation();
		Organisation orgA = organisationService.createOrganisation("MCP vis A", "mcp-vis-a-" + UUID.randomUUID(), null, root, null, null);
		Organisation orgA1 = organisationService.createOrganisation("MCP vis A1", "mcp-vis-a1-" + UUID.randomUUID(), null, orgA, null, null);
		Organisation orgB = organisationService.createOrganisation("MCP vis B", "mcp-vis-b-" + UUID.randomUUID(), null, root, null, null);
		McpProfile pilotsA = createProfile(McpProfileType.user, false, null, null, orgA);
		McpProfile pilotsRoot = createProfile(McpProfileType.user, true, null, null, root);
		McpProfile pilotsA1 = createProfile(McpProfileType.user, true, null, null, orgA1);
		McpProfile systemA = createProfile(McpProfileType.system, true, null, List.of(OrganisationRoles.user), orgA);
		dbInstance.commitAndCloseSession();

		assertThat(mcpProfileDao.loadUserProfilesVisibleFor(List.of(orgA)))
			.contains(pilotsA, pilotsRoot)
			.doesNotContain(pilotsA1, systemA);
		assertThat(mcpProfileDao.loadUserProfilesVisibleFor(List.of(orgA1)))
			.contains(pilotsA, pilotsRoot, pilotsA1)
			.doesNotContain(systemA);
		assertThat(mcpProfileDao.loadUserProfilesVisibleFor(List.of(orgB)))
			.contains(pilotsRoot)
			.doesNotContain(pilotsA, pilotsA1, systemA);
		assertThat(mcpProfileDao.loadUserProfilesVisibleFor(List.of(orgB, orgA1)))
			.contains(pilotsA, pilotsRoot, pilotsA1)
			.doesNotHaveDuplicates();
		assertThat(mcpProfileDao.loadUserProfilesVisibleFor(List.of())).isEmpty();
	}

	@Test
	public void loadUserProfilesOf() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-dao-13");
		McpProfile enabled = createProfile(McpProfileType.user, true, null, null);
		McpProfile disabled = createProfile(McpProfileType.user, false, null, null);
		McpProfile notGranted = createProfile(McpProfileType.user, true, null, null);
		McpProfile system = createProfile(McpProfileType.system, true, null, List.of(OrganisationRoles.user));
		mcpProfileDao.createGrant(enabled, id);
		mcpProfileDao.createGrant(disabled, id);
		mcpProfileDao.createGrant(system, id);
		dbInstance.commitAndCloseSession();

		assertThat(mcpProfileDao.loadUserProfilesOf(id))
			.containsExactlyInAnyOrder(enabled, disabled)
			.doesNotContain(notGranted, system);
	}
}
