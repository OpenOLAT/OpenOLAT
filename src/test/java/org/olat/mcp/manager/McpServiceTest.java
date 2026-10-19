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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileChangedEvent;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.McpService;
import org.olat.mcp.McpTokenCreation;
import org.olat.mcp.McpTokenService;
import org.olat.mcp.McpTokenValidation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserLifecycleManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;
	@Autowired
	private McpServiceImpl mcpServiceImpl;
	@Autowired
	private McpProfileDAO mcpProfileDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private McpTokenService mcpTokenService;
	@Autowired
	private UserLifecycleManager userLifecycleManager;

	private final List<McpProfile> createdProfiles = new ArrayList<>();

	@After
	public void deleteProfiles() {
		for(McpProfile profile:createdProfiles) {
			mcpService.deleteProfile(profile);
		}
		createdProfiles.clear();
		dbInstance.commitAndCloseSession();
	}

	private McpProfile createProfile(String prefix, McpProfileType type, boolean enabled, Set<String> tools,
			List<OrganisationRoles> roles, Integer rpm) {
		return createProfile(prefix, type, enabled, tools, roles, rpm, JunitTestHelper.getDefaultOrganisation());
	}

	private McpProfile createProfile(String prefix, McpProfileType type, boolean enabled, Set<String> tools,
			List<OrganisationRoles> roles, Integer rpm, Organisation organisation) {
		McpProfile profile = mcpService.createProfile(randomIdentifier(prefix), "Service test", type, List.of(organisation));
		createdProfiles.add(profile);
		profile.setEnabled(enabled);
		profile.setTools(tools);
		profile.setOrganisationRoles(roles);
		profile.setRateLimitRequestsPerMinute(rpm);
		profile = mcpService.updateProfile(profile);
		return profile;
	}

	private static String randomIdentifier(String prefix) {
		return prefix + "-" + UUID.randomUUID();
	}

	private static List<Organisation> defaultOrganisations() {
		return List.of(JunitTestHelper.getDefaultOrganisation());
	}

	@Test
	public void createProfile() {
		String identifier = randomIdentifier("svc");
		McpProfile profile = mcpService.createProfile(identifier, "My profile", McpProfileType.system, defaultOrganisations());
		createdProfiles.add(profile);
		dbInstance.commitAndCloseSession();

		McpProfile reloaded = mcpService.getProfile(profile);
		Assert.assertEquals(identifier, reloaded.getIdentifier());
		Assert.assertEquals("My profile", reloaded.getName());
		Assert.assertEquals(McpProfileType.system, reloaded.getType());
		Assert.assertFalse(reloaded.isEnabled());
		Assert.assertTrue(reloaded.getTools().isEmpty());
		Assert.assertEquals(reloaded, mcpService.getProfileByIdentifier(identifier));
		Assert.assertNull(mcpService.getProfileByIdentifier(null));
		Assert.assertNull(mcpService.getProfileByIdentifier(" "));
		Assert.assertNull(mcpService.getProfile(null));
	}

	@Test
	public void createProfile_identifierPattern() {
		List<String> invalidIdentifiers = new ArrayList<>();
		invalidIdentifiers.add(null);
		invalidIdentifiers.add("");
		invalidIdentifiers.add("ab");
		invalidIdentifiers.add("Upper-case");
		invalidIdentifiers.add("with_underscore");
		invalidIdentifiers.add("with space");
		invalidIdentifiers.add("-leading-hyphen");
		invalidIdentifiers.add("umlaut-ä");
		invalidIdentifiers.add("a".repeat(65));
		for(String identifier:invalidIdentifiers) {
			Assert.assertFalse(identifier, McpServiceImpl.isValidIdentifier(identifier));
			Assert.assertThrows(identifier, IllegalArgumentException.class,
					() -> mcpService.createProfile(identifier, "Invalid", McpProfileType.user, defaultOrganisations()));
		}

		Assert.assertTrue(McpServiceImpl.isValidIdentifier("abc"));
		Assert.assertTrue(McpServiceImpl.isValidIdentifier("0-a"));
		Assert.assertTrue(McpServiceImpl.isValidIdentifier("default-project-pilot"));
		String longest = "x" + UUID.randomUUID().toString().replace("-", "") + "y".repeat(31);
		Assert.assertEquals(64, longest.length());
		McpProfile profile = mcpService.createProfile(longest, "Longest", McpProfileType.user, defaultOrganisations());
		createdProfiles.add(profile);
		Assert.assertNotNull(profile.getKey());
	}

	@Test
	public void createProfile_identifierUnique() {
		String identifier = randomIdentifier("svc");
		McpProfile profile = mcpService.createProfile(identifier, "First", McpProfileType.user, defaultOrganisations());
		createdProfiles.add(profile);
		dbInstance.commitAndCloseSession();

		Assert.assertThrows(IllegalArgumentException.class,
				() -> mcpService.createProfile(identifier, "Second", McpProfileType.system, defaultOrganisations()));
		dbInstance.commitAndCloseSession();
		assertThat(mcpService.getProfiles())
			.filteredOn(p -> identifier.equals(p.getIdentifier()))
			.hasSize(1);
	}

	@Test
	public void createProfile_mandatoryFields() {
		Assert.assertThrows(IllegalArgumentException.class,
				() -> mcpService.createProfile(randomIdentifier("svc"), null, McpProfileType.user, defaultOrganisations()));
		Assert.assertThrows(IllegalArgumentException.class,
				() -> mcpService.createProfile(randomIdentifier("svc"), "  ", McpProfileType.user, defaultOrganisations()));
		Assert.assertThrows(IllegalArgumentException.class,
				() -> mcpService.createProfile(randomIdentifier("svc"), "x".repeat(256), McpProfileType.user, defaultOrganisations()));
		Assert.assertThrows(IllegalArgumentException.class,
				() -> mcpService.createProfile(randomIdentifier("svc"), "No type", null, defaultOrganisations()));
	}

	@Test
	public void updateProfile() {
		McpProfile profile = createProfile("svc", McpProfileType.system, true, Set.of("course_query", "todo_query"),
				List.of(OrganisationRoles.author), Integer.valueOf(300));
		dbInstance.commitAndCloseSession();

		McpProfile reloaded = mcpService.getProfile(profile);
		Assert.assertTrue(reloaded.isEnabled());
		assertThat(reloaded.getTools()).containsExactlyInAnyOrder("course_query", "todo_query");
		assertThat(reloaded.getOrganisationRoles()).containsExactly(OrganisationRoles.author);
		Assert.assertEquals(Integer.valueOf(300), reloaded.getRateLimitRequestsPerMinute());

		reloaded.setEnabled(false);
		reloaded.setDescription("Disabled");
		mcpService.updateProfile(reloaded);
		dbInstance.commitAndCloseSession();

		McpProfile disabled = mcpService.getProfile(profile);
		Assert.assertFalse(disabled.isEnabled());
		Assert.assertEquals("Disabled", disabled.getDescription());
		assertThat(mcpService.getProfiles()).contains(disabled);
	}

	@Test
	public void updateProfile_invalid() {
		McpProfile profile = createProfile("svc", McpProfileType.user, false, null, null, null);
		dbInstance.commitAndCloseSession();

		profile.setRateLimitRequestsPerMinute(Integer.valueOf(0));
		Assert.assertThrows(IllegalArgumentException.class, () -> mcpService.updateProfile(profile));
		profile.setRateLimitRequestsPerMinute(Integer.valueOf(-5));
		Assert.assertThrows(IllegalArgumentException.class, () -> mcpService.updateProfile(profile));
		profile.setRateLimitRequestsPerMinute(null);
		profile.setName(" ");
		Assert.assertThrows(IllegalArgumentException.class, () -> mcpService.updateProfile(profile));
		dbInstance.rollbackAndCloseSession();
	}

	@Test
	public void updateProfile_userProfileHasNoRoles() {
		McpProfile profile = createProfile("svc", McpProfileType.user, true, null,
				List.of(OrganisationRoles.user), null);
		dbInstance.commitAndCloseSession();

		McpProfile reloaded = mcpService.getProfile(profile);
		Assert.assertTrue(reloaded.getOrganisationRoles().isEmpty());
	}

	@Test
	public void grants() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-2");
		McpProfile profile = createProfile("svc", McpProfileType.user, true, null, null, null);
		dbInstance.commitAndCloseSession();

		Assert.assertFalse(mcpService.hasGrant(profile, id1));
		Assert.assertEquals(0, mcpService.countGrants(id1));

		mcpService.addGrant(profile, id1);
		mcpService.addGrant(profile, id2);
		// adding twice is a no-op
		mcpService.addGrant(profile, id1);
		dbInstance.commitAndCloseSession();

		Assert.assertTrue(mcpService.hasGrant(profile, id1));
		Assert.assertTrue(mcpService.hasGrant(profile, id2));
		Assert.assertEquals(1, mcpService.countGrants(id1));
		assertThat(mcpService.getGrantedIdentities(profile, 0, -1)).containsExactly(id1, id2);

		mcpService.removeGrant(profile, id1);
		// removing twice is a no-op
		mcpService.removeGrant(profile, id1);
		dbInstance.commitAndCloseSession();

		Assert.assertFalse(mcpService.hasGrant(profile, id1));
		Assert.assertEquals(0, mcpService.countGrants(id1));
		assertThat(mcpService.getGrantedIdentities(profile, 0, -1)).containsExactly(id2);
	}

	@Test
	public void deleteProfile_removesGrants() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-3");
		McpProfile profile = mcpService.createProfile(randomIdentifier("svc"), "To delete", McpProfileType.user, defaultOrganisations());
		mcpService.addGrant(profile, id);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(1, mcpService.countGrants(id));

		mcpService.deleteProfile(profile);
		dbInstance.commitAndCloseSession();

		Assert.assertNull(mcpService.getProfile(profile));
		Assert.assertEquals(0, mcpService.countGrants(id));
		// null is tolerated
		mcpService.deleteProfile(null);
	}

	@Test
	public void getProfilesOf_userProfileGrant() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-4");
		McpProfile profile = createProfile("svc", McpProfileType.user, true, Set.of("todo_query"), null, null);
		mcpService.addGrant(profile, id);
		dbInstance.commitAndCloseSession();

		assertThat(mcpService.getProfilesOf(id)).containsExactly(profile);
	}

	@Test
	public void getProfilesOf_systemProfileByRole() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-svc-5");
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-6");
		McpProfile authorProfile = createProfile("svc", McpProfileType.system, true, Set.of("project_query"),
				List.of(OrganisationRoles.author), null);
		McpProfile poolManagerProfile = createProfile("svc", McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.poolmanager), null);
		dbInstance.commitAndCloseSession();

		// the roles of the identities in the database
		assertThat(mcpService.getProfilesOf(author))
			.contains(authorProfile)
			.doesNotContain(poolManagerProfile);
		assertThat(mcpService.getProfilesOf(user))
			.doesNotContain(authorProfile, poolManagerProfile);

		// a role in a second organisation counts
		Organisation second = organisationService.createOrganisation("MCP second", "mcp-second-" + UUID.randomUUID(),
				null, JunitTestHelper.getDefaultOrganisation(), null, null);
		organisationService.addMember(second, user, OrganisationRoles.poolmanager, null);
		dbInstance.commitAndCloseSession();
		assertThat(mcpService.getProfilesOf(user))
			.contains(poolManagerProfile)
			.doesNotContain(authorProfile);
	}

	@Test
	public void getProfilesOf_union() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-7");
		McpProfile userProfile = createProfile("svc", McpProfileType.user, true, Set.of("todo_modify"), null, null);
		McpProfile systemProfile = createProfile("svc", McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.curriculummanager), null);
		mcpService.addGrant(userProfile, id);
		organisationService.addMember(JunitTestHelper.getDefaultOrganisation(), id, OrganisationRoles.curriculummanager, null);
		dbInstance.commitAndCloseSession();

		List<McpProfile> profiles = mcpService.getProfilesOf(id);
		assertThat(profiles)
			.contains(userProfile, systemProfile)
			.isSortedAccordingTo((p1, p2) -> p1.getIdentifier().compareTo(p2.getIdentifier()));

		McpGrants grants = mcpService.resolveGrants(id);
		Assert.assertTrue(grants.isGranted("todo_modify"));
		Assert.assertTrue(grants.isGranted("course_query"));
		assertThat(grants.profileIdentifiers()).contains(userProfile.getIdentifier(), systemProfile.getIdentifier());
	}

	@Test
	public void getProfilesOf_disabledExcluded() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-8");
		McpProfile userProfile = createProfile("svc", McpProfileType.user, false, Set.of("todo_modify"), null, null);
		McpProfile systemProfile = createProfile("svc", McpProfileType.system, false, Set.of("course_query"),
				List.of(OrganisationRoles.selectusmanager), null);
		mcpService.addGrant(userProfile, id);
		dbInstance.commitAndCloseSession();

		assertThat(mcpService.getProfilesOf(id))
			.doesNotContain(userProfile, systemProfile);
		assertThat(mcpService.getProfilesOf(id)).isEmpty();
		Assert.assertSame(McpGrants.EMPTY, mcpService.resolveGrants(id));
	}

	@Test
	public void getProfilesOf_nothing() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-9");
		dbInstance.commitAndCloseSession();

		assertThat(mcpService.getProfilesOf(id)).isEmpty();
		assertThat(mcpService.getProfilesOf(null)).isEmpty();

		McpGrants grants = mcpService.resolveGrants(id);
		Assert.assertSame(McpGrants.EMPTY, grants);
		Assert.assertTrue(grants.isEmpty());
	}

	@Test
	public void resolveGrants() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-10");
		McpProfile profileB = createProfile("svc-b", McpProfileType.user, true,
				Set.of("course_query", "todo_query"), null, Integer.valueOf(150));
		McpProfile profileA = createProfile("svc-a", McpProfileType.user, true,
				Set.of("todo_query", "todo_modify"), null, null);
		McpProfile profileC = createProfile("svc-c", McpProfileType.user, true,
				Set.of("project_query"), null, Integer.valueOf(400));
		McpProfile profileDisabled = createProfile("svc-d", McpProfileType.user, false,
				Set.of("lecture_query"), null, Integer.valueOf(1000));
		mcpService.addGrant(profileB, id);
		mcpService.addGrant(profileA, id);
		mcpService.addGrant(profileC, id);
		mcpService.addGrant(profileDisabled, id);
		dbInstance.commitAndCloseSession();

		McpGrants grants = mcpService.resolveGrants(id);
		Assert.assertFalse(grants.isEmpty());
		assertThat(grants.tools())
			.containsExactlyInAnyOrder("course_query", "todo_query", "todo_modify", "project_query");
		Assert.assertFalse(grants.isGranted("lecture_query"));
		Assert.assertEquals(Integer.valueOf(400), grants.requestsPerMinute());
		assertThat(grants.profileIdentifiers())
			.containsExactly(profileA.getIdentifier(), profileB.getIdentifier(), profileC.getIdentifier());
	}

	@Test
	public void resolveGrants_rateLimitNull() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-11");
		McpProfile profile1 = createProfile("svc", McpProfileType.user, true, Set.of("todo_query"), null, null);
		McpProfile profile2 = createProfile("svc", McpProfileType.user, true, Set.of(), null, null);
		mcpService.addGrant(profile1, id);
		mcpService.addGrant(profile2, id);
		dbInstance.commitAndCloseSession();

		McpGrants grants = mcpService.resolveGrants(id);
		Assert.assertNull(grants.requestsPerMinute());
		assertThat(grants.tools()).containsExactly("todo_query");
		assertThat(grants.profileIdentifiers()).hasSize(2);
	}

	@Test
	public void toGrants() {
		Assert.assertSame(McpGrants.EMPTY, McpServiceImpl.toGrants(null));
		Assert.assertSame(McpGrants.EMPTY, McpServiceImpl.toGrants(List.of()));
	}

	@Test
	public void deleteUserData() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-12");
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-13");
		McpProfile profile1 = createProfile("svc", McpProfileType.user, true, Set.of("todo_query"), null, null);
		McpProfile profile2 = createProfile("svc", McpProfileType.user, false, Set.of("todo_query"), null, null);
		mcpService.addGrant(profile1, id);
		mcpService.addGrant(profile2, id);
		mcpService.addGrant(profile1, otherId);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(2, mcpService.countGrants(id));

		mcpServiceImpl.deleteUserData(id, "deleted-mcp");
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(0, mcpService.countGrants(id));
		Assert.assertTrue(mcpService.resolveGrants(id).isEmpty());
		Assert.assertTrue(mcpService.hasGrant(profile1, otherId));
	}

	/**
	 * The real deletion path: the lifecycle manager finds the MCP service as a
	 * UserDataDeletable bean, and the core deletes the token rows of provider
	 * MCP in o_bs_authentication.
	 */
	@Test
	public void deleteIdentity_cascadesGrantsAndTokens() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-del");
		McpProfile profile = createProfile("svc", McpProfileType.user, true, Set.of("todo_query"), null, null);
		mcpService.addGrant(profile, id);
		int currentTokenMax = mcpModule.getUserTokenMax();
		McpTokenCreation creation;
		try {
			mcpModule.setUserTokenMax(5);
			creation = mcpTokenService.generateToken(id, "Deletion test");
		} finally {
			mcpModule.setUserTokenMax(currentTokenMax);
		}
		dbInstance.commitAndCloseSession();

		String plaintext = creation.plaintext();
		Assert.assertEquals(1, mcpService.countGrants(id));
		Assert.assertEquals(1, mcpTokenService.countTokens(id));
		Assert.assertTrue(mcpTokenService.validateToken(plaintext).isValid());

		boolean deleted = userLifecycleManager.deleteIdentity(id, null);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(deleted);

		// the grant is gone, the profile stays
		Assert.assertEquals(0, mcpService.countGrants(id));
		Assert.assertTrue(mcpService.resolveGrants(id).isEmpty());
		McpProfile reloadedProfile = mcpService.getProfile(profile);
		Assert.assertNotNull(reloadedProfile);
		Assert.assertTrue(reloadedProfile.isEnabled());
		Assert.assertEquals(0, mcpService.countGrantedIdentities(reloadedProfile));

		// the token is gone
		Assert.assertEquals(0, mcpTokenService.countTokens(id));
		assertThat(mcpTokenService.getTokens(id)).isEmpty();
		Assert.assertNull(mcpTokenService.getToken(creation.token().tokenId()));
		List<Authentication> authentications = securityManager.getAuthentications(id);
		assertThat(authentications)
			.extracting(Authentication::getProvider)
			.doesNotContain(McpTokenService.MCP_AUTH_PROVIDER);

		// the old plaintext is unknown, not only refused for the inactive identity
		McpTokenValidation validation = mcpTokenService.validateToken(plaintext);
		Assert.assertFalse(validation.isValid());
		Assert.assertEquals(McpTokenValidation.Status.unknown, validation.status());
		Assert.assertNull(validation.token());
	}

	@Test
	public void events() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-14");
		dbInstance.commitAndCloseSession();

		// The event bus is asynchronous: events of earlier tests in the same JVM
		// arrive late. Let them pass before the listener is registered, and count
		// only the events of the profile of this test (and the null key of the
		// user data deletion below).
		waitMessageAreConsumed();
		List<McpProfileChangedEvent> events = new CopyOnWriteArrayList<>();
		AtomicReference<Long> profileKeyRef = new AtomicReference<>();
		GenericEventListener listener = event -> {
			if(event instanceof McpProfileChangedEvent changedEvent
					&& (changedEvent.getProfileKey() == null || changedEvent.getProfileKey().equals(profileKeyRef.get()))) {
				events.add(changedEvent);
			}
		};
		coordinatorManager.getCoordinator().getEventBus().registerFor(listener, null, McpProfileChangedEvent.ORES);
		try {
			String identifier = randomIdentifier("svc");
			McpProfile profile = mcpService.createProfile(identifier, "Events", McpProfileType.user, defaultOrganisations());
			profileKeyRef.set(profile.getKey());
			createdProfiles.add(profile);
			assertEvents(events, 1, profile.getKey());

			profile.setEnabled(true);
			profile = mcpService.updateProfile(profile);
			assertEvents(events, 2, profile.getKey());

			mcpService.addGrant(profile, id);
			assertEvents(events, 3, profile.getKey());
			// no change, no event
			mcpService.addGrant(profile, id);

			mcpService.removeGrant(profile, id);
			assertEvents(events, 4, profile.getKey());
			// no change, no event
			mcpService.removeGrant(profile, id);

			mcpService.addGrant(profile, id);
			assertEvents(events, 5, profile.getKey());
			mcpServiceImpl.deleteUserData(id, "deleted-mcp-events");
			assertEvents(events, 6, null);

			mcpService.deleteProfile(profile);
			createdProfiles.remove(profile);
			assertEvents(events, 7, profile.getKey());
			Assert.assertEquals(McpProfileChangedEvent.COMMAND, events.get(6).getCommand());
		} finally {
			coordinatorManager.getCoordinator().getEventBus().deregisterFor(listener, McpProfileChangedEvent.ORES);
		}
	}

	private void assertEvents(List<McpProfileChangedEvent> events, int expectedSize, Long expectedProfileKey) {
		waitForCondition(() -> events.size() >= expectedSize, 5000);
		Assert.assertEquals(expectedSize, events.size());
		Assert.assertEquals(expectedProfileKey, events.get(expectedSize - 1).getProfileKey());
	}

	@Test
	public void createProfile_organisationMandatory() {
		Assert.assertThrows(IllegalArgumentException.class,
				() -> mcpService.createProfile(randomIdentifier("svc"), "No organisation", McpProfileType.user, null));
		Assert.assertThrows(IllegalArgumentException.class,
				() -> mcpService.createProfile(randomIdentifier("svc"), "No organisation", McpProfileType.user, List.of()));
		dbInstance.rollbackAndCloseSession();
	}

	@Test
	public void setOrganisations() {
		Organisation root = JunitTestHelper.getDefaultOrganisation();
		Organisation orgA = organisationService.createOrganisation("MCP set A", "mcp-set-a-" + UUID.randomUUID(), null, root, null, null);
		Organisation orgB = organisationService.createOrganisation("MCP set B", "mcp-set-b-" + UUID.randomUUID(), null, root, null, null);
		McpProfile profile = createProfile("svc", McpProfileType.user, false, null, null, null);
		dbInstance.commitAndCloseSession();
		assertThat(mcpService.getOrganisations(profile)).containsExactly(root);

		mcpService.setOrganisations(profile, List.of(orgA, orgB, orgA));
		dbInstance.commitAndCloseSession();
		assertThat(mcpService.getOrganisations(profile)).containsExactlyInAnyOrder(orgA, orgB);
		assertThat(mcpService.getOrganisationsByProfileKey().get(profile.getKey())).containsExactlyInAnyOrder(orgA, orgB);

		mcpService.setOrganisations(profile, List.of(orgB));
		dbInstance.commitAndCloseSession();
		assertThat(mcpService.getOrganisations(profile)).containsExactly(orgB);

		Assert.assertThrows(IllegalArgumentException.class, () -> mcpService.setOrganisations(profile, List.of()));
		Assert.assertThrows(IllegalArgumentException.class, () -> mcpService.setOrganisations(profile, null));
		dbInstance.commitAndCloseSession();
		assertThat(mcpService.getOrganisations(profile)).containsExactly(orgB);
	}

	@Test
	public void updateProfile_enableWithoutOrganisation() {
		McpProfile profile = mcpProfileDao.createProfile(randomIdentifier("svc"), "No organisation", McpProfileType.system);
		createdProfiles.add(profile);
		dbInstance.commitAndCloseSession();

		profile.setEnabled(true);
		Assert.assertThrows(IllegalArgumentException.class, () -> mcpService.updateProfile(profile));
		dbInstance.rollbackAndCloseSession();

		// disabled, the update is accepted
		McpProfile reloaded = mcpService.getProfile(profile);
		Assert.assertFalse(reloaded.isEnabled());
		reloaded.setDescription("Still disabled");
		mcpService.updateProfile(reloaded);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("Still disabled", mcpService.getProfile(profile).getDescription());
	}

	/**
	 * Organisation tree: the default organisation (root), A below the root,
	 * A1 below A and B below the root. The users are members of A1 or B only.
	 */
	@Test
	public void resolveGrants_organisationTree() {
		OrganisationTree tree = createOrganisationTree("res");
		Identity userA1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-15", tree.orgA1(), JunitTestHelper.PWD);
		Identity userB = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-16", tree.orgB(), JunitTestHelper.PWD);
		organisationService.addMember(tree.orgA1(), userA1, OrganisationRoles.lecturemanager, null);
		organisationService.addMember(tree.orgB(), userB, OrganisationRoles.lecturemanager, null);

		// a system profile on A
		McpProfile lectureManagersA = createProfile("svc", McpProfileType.system, true, Set.of("lecture_query"),
				List.of(OrganisationRoles.lecturemanager), null, tree.orgA());
		// a user profile on A with both users as members
		McpProfile pilotsA = createProfile("svc", McpProfileType.user, true, Set.of("project_modify"), null, null, tree.orgA());
		mcpService.addGrant(pilotsA, userA1);
		mcpService.addGrant(pilotsA, userB);
		// a profile like the default learner profile: role user on the root
		McpProfile learners = createProfile("svc", McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.user), null, tree.root());
		dbInstance.commitAndCloseSession();

		McpGrants grantsA1 = mcpService.resolveGrants(userA1);
		assertThat(grantsA1.profileIdentifiers())
			.contains(lectureManagersA.getIdentifier(), pilotsA.getIdentifier(), learners.getIdentifier());
		Assert.assertTrue(grantsA1.isGranted("lecture_query"));
		Assert.assertTrue(grantsA1.isGranted("project_modify"));
		Assert.assertTrue(grantsA1.isGranted("course_query"));

		// the role in B matches neither the system nor the user profile on A
		McpGrants grantsB = mcpService.resolveGrants(userB);
		assertThat(grantsB.profileIdentifiers())
			.contains(learners.getIdentifier())
			.doesNotContain(lectureManagersA.getIdentifier(), pilotsA.getIdentifier());
	}

	@Test
	public void defaultProfiles_resolveBelowTheRoot() {
		OrganisationTree tree = createOrganisationTree("def");
		Identity userA1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-17", tree.orgA1(), JunitTestHelper.PWD);
		dbInstance.commitAndCloseSession();

		McpProfile learner = mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_LEARNER);
		Assert.assertNotNull(learner);
		boolean wasEnabled = learner.isEnabled();
		try {
			learner.setEnabled(true);
			mcpService.updateProfile(learner);
			dbInstance.commitAndCloseSession();

			assertThat(mcpService.getProfilesOf(userA1)).contains(mcpService.getProfile(learner));
		} finally {
			McpProfile reloaded = mcpService.getProfile(learner);
			reloaded.setEnabled(wasEnabled);
			mcpService.updateProfile(reloaded);
			dbInstance.commitAndCloseSession();
		}
	}

	@Test
	public void canManageMembers() {
		OrganisationTree tree = createOrganisationTree("man");
		Identity userA1 = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-18", tree.orgA1(), JunitTestHelper.PWD);
		Identity userB = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-19", tree.orgB(), JunitTestHelper.PWD);
		Identity managerA = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-20", tree.orgA(), JunitTestHelper.PWD);
		organisationService.addMember(tree.orgA(), managerA, OrganisationRoles.usermanager, null);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("mcp-svc-21");
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("mcp-svc-22");
		McpProfile pilotsA = createProfile("svc", McpProfileType.user, true, null, null, null, tree.orgA());
		McpProfile pilotsB = createProfile("svc", McpProfileType.user, true, null, null, null, tree.orgB());
		dbInstance.commitAndCloseSession();

		Roles managerRoles = securityManager.getRoles(managerA);
		// the user manager of A manages the users of A1, not the users of B
		Assert.assertTrue(mcpService.canManageMembers(managerRoles, userA1));
		Assert.assertFalse(mcpService.canManageMembers(managerRoles, userB));
		assertThat(mcpService.getManageableUserProfiles(managerRoles, userA1))
			.contains(pilotsA)
			.doesNotContain(pilotsB);
		assertThat(mcpService.getManageableUserProfiles(managerRoles, userB))
			.doesNotContain(pilotsA, pilotsB);

		// an author manages nobody
		Roles authorRoles = securityManager.getRoles(author);
		Assert.assertFalse(mcpService.canManageMembers(authorRoles, userA1));
		assertThat(mcpService.getManageableUserProfiles(authorRoles, userA1)).doesNotContain(pilotsA, pilotsB);

		// the administrator of the root manages everybody
		Roles adminRoles = securityManager.getRoles(admin);
		Assert.assertTrue(mcpService.canManageMembers(adminRoles, userA1));
		Assert.assertTrue(mcpService.canManageMembers(adminRoles, userB));
		assertThat(mcpService.getManageableUserProfiles(adminRoles, userB)).contains(pilotsB).doesNotContain(pilotsA);

		Assert.assertFalse(mcpService.canManageMembers(managerRoles, null));
	}

	@Test
	public void getUserProfilesOf() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-23");
		McpProfile enabled = createProfile("svc", McpProfileType.user, true, null, null, null);
		McpProfile disabled = createProfile("svc", McpProfileType.user, false, null, null, null);
		mcpService.addGrant(enabled, id);
		mcpService.addGrant(disabled, id);
		dbInstance.commitAndCloseSession();

		assertThat(mcpService.getUserProfilesOf(id)).containsExactlyInAnyOrder(enabled, disabled);
		assertThat(mcpService.getUserProfilesOf(null)).isEmpty();
	}

	@Test
	public void deleteOrganisation_disablesProfile() {
		OrganisationTree tree = createOrganisationTree("del");
		McpProfile onlyB = createProfile("svc", McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.author), null, tree.orgB());
		McpProfile bAndA = createProfile("svc", McpProfileType.user, true, Set.of("course_query"), null, null, tree.orgB());
		mcpService.setOrganisations(bAndA, List.of(tree.orgA(), tree.orgB()));
		dbInstance.commitAndCloseSession();

		organisationService.deleteOrganisation(tree.orgB(), null, null);
		dbInstance.commitAndCloseSession();

		McpProfile reloadedOnlyB = mcpService.getProfile(onlyB);
		Assert.assertFalse(reloadedOnlyB.isEnabled());
		assertThat(mcpService.getOrganisations(reloadedOnlyB)).isEmpty();
		McpProfile reloadedBAndA = mcpService.getProfile(bAndA);
		Assert.assertTrue(reloadedBAndA.isEnabled());
		assertThat(mcpService.getOrganisations(reloadedBAndA)).containsExactly(tree.orgA());
	}

	/**
	 * The deleted organisation has a replacement: the profiles are linked to
	 * the replacement organisation, once, and stay enabled.
	 */
	@Test
	public void deleteOrganisation_linksProfileToReplacement() {
		OrganisationTree tree = createOrganisationTree("rpl");
		McpProfile onlyB = createProfile("svc", McpProfileType.system, true, Set.of("course_query"),
				List.of(OrganisationRoles.author), null, tree.orgB());
		McpProfile bAndA = createProfile("svc", McpProfileType.user, true, Set.of("course_query"), null, null, tree.orgB());
		mcpService.setOrganisations(bAndA, List.of(tree.orgA(), tree.orgB()));
		dbInstance.commitAndCloseSession();

		organisationService.deleteOrganisation(tree.orgB(), tree.orgA(), null);
		dbInstance.commitAndCloseSession();

		McpProfile reloadedOnlyB = mcpService.getProfile(onlyB);
		Assert.assertTrue(reloadedOnlyB.isEnabled());
		assertThat(mcpService.getOrganisations(reloadedOnlyB)).containsExactly(tree.orgA());
		// already linked to the replacement: no second relation
		McpProfile reloadedBAndA = mcpService.getProfile(bAndA);
		Assert.assertTrue(reloadedBAndA.isEnabled());
		assertThat(mcpService.getOrganisations(reloadedBAndA)).containsExactly(tree.orgA());
	}

	/**
	 * A user grant on a profile whose only organisation is deleted: the profile
	 * is disabled, the grant stays, and resolveGrants ignores it without error.
	 */
	@Test
	public void deleteOrganisation_userGrantResolvesNothing() {
		OrganisationTree tree = createOrganisationTree("grt");
		Identity onlyB = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-grt-1");
		Identity bAndA = JunitTestHelper.createAndPersistIdentityAsRndUser("mcp-svc-grt-2");
		// a user profile applies to the users of its organisation and below
		organisationService.addMember(tree.orgB(), onlyB, OrganisationRoles.user, null);
		organisationService.addMember(tree.orgB(), bAndA, OrganisationRoles.user, null);
		organisationService.addMember(tree.orgA(), bAndA, OrganisationRoles.user, null);
		McpProfile profileB = createProfile("svc", McpProfileType.user, true, Set.of("lecture_query"), null, null, tree.orgB());
		McpProfile profileA = createProfile("svc", McpProfileType.user, true, Set.of("todo_query"), null, null, tree.orgA());
		mcpService.addGrant(profileB, onlyB);
		mcpService.addGrant(profileB, bAndA);
		mcpService.addGrant(profileA, bAndA);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(mcpService.resolveGrants(onlyB).isGranted("lecture_query"));
		assertThat(mcpService.resolveGrants(bAndA).tools()).containsExactlyInAnyOrder("lecture_query", "todo_query");

		organisationService.deleteOrganisation(tree.orgB(), null, null);
		dbInstance.commitAndCloseSession();

		McpProfile reloadedB = mcpService.getProfile(profileB);
		Assert.assertFalse(reloadedB.isEnabled());
		assertThat(mcpService.getOrganisations(reloadedB)).isEmpty();
		// the grants stay
		Assert.assertEquals(1, mcpService.countGrants(onlyB));
		Assert.assertEquals(2, mcpService.countGrants(bAndA));
		assertThat(mcpService.getUserProfilesOf(onlyB)).containsExactly(reloadedB);

		// but the disabled profile yields nothing
		McpGrants onlyBGrants = mcpService.resolveGrants(onlyB);
		Assert.assertSame(McpGrants.EMPTY, onlyBGrants);
		Assert.assertFalse(onlyBGrants.isGranted("lecture_query"));
		McpGrants bAndAGrants = mcpService.resolveGrants(bAndA);
		assertThat(bAndAGrants.tools()).containsExactly("todo_query");
		assertThat(bAndAGrants.profileIdentifiers()).containsExactly(profileA.getIdentifier());
	}

	private OrganisationTree createOrganisationTree(String prefix) {
		Organisation root = JunitTestHelper.getDefaultOrganisation();
		Organisation orgA = organisationService.createOrganisation("MCP A", "mcp-" + prefix + "-a-" + UUID.randomUUID(), null, root, null, null);
		Organisation orgA1 = organisationService.createOrganisation("MCP A1", "mcp-" + prefix + "-a1-" + UUID.randomUUID(), null, orgA, null, null);
		Organisation orgB = organisationService.createOrganisation("MCP B", "mcp-" + prefix + "-b-" + UUID.randomUUID(), null, root, null, null);
		dbInstance.commitAndCloseSession();
		return new OrganisationTree(root, orgA, orgA1, orgB);
	}

	private record OrganisationTree(Organisation root, Organisation orgA, Organisation orgA1, Organisation orgB) {
		//
	}

	/**
	 * Tests the seeding against the four default identifiers only. The other
	 * rows of o_mcp_profile survive, tests of other classes may run in parallel.
	 */
	@Test
	public void defaultProfiles() {
		McpProfile otherProfile = createProfile("svc", McpProfileType.user, false, Set.of("todo_query"), null, null);
		dbInstance.commitAndCloseSession();
		deleteDefaultProfiles();

		try {
			Assert.assertEquals(4, mcpServiceImpl.createMissingDefaultProfiles());
			dbInstance.commitAndCloseSession();
			assertDefaultProfiles();

			// second run: nothing new
			Assert.assertEquals(0, mcpServiceImpl.createMissingDefaultProfiles());
			dbInstance.commitAndCloseSession();
			assertDefaultProfiles();

			// one missing: only this one is created
			mcpService.deleteProfile(mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_PROJECT_PILOT));
			dbInstance.commitAndCloseSession();
			Assert.assertEquals(1, mcpServiceImpl.createMissingDefaultProfiles());
			dbInstance.commitAndCloseSession();
			assertDefaultProfiles();

			// the other row survives
			Assert.assertNotNull(mcpService.getProfile(otherProfile));
		} finally {
			mcpServiceImpl.createMissingDefaultProfiles();
			dbInstance.commitAndCloseSession();
		}
	}

	/**
	 * At startup the seeding runs only if the flag is unset and the table is
	 * empty. The table holds at least one other profile here.
	 */
	@Test
	public void defaultProfiles_afterPropertiesSet() {
		McpProfile otherProfile = createProfile("svc", McpProfileType.user, false, null, null, null);
		dbInstance.commitAndCloseSession();
		deleteDefaultProfiles();

		try {
			// flag set: nothing happens
			mcpModule.setDefaultProfilesCreated(true);
			mcpServiceImpl.afterPropertiesSet();
			assertNoDefaultProfiles();

			// flag unset, table not empty: nothing is created, the flag is set
			mcpModule.setDefaultProfilesCreated(false);
			mcpServiceImpl.afterPropertiesSet();
			assertNoDefaultProfiles();
			Assert.assertTrue(mcpModule.isDefaultProfilesCreated());
			Assert.assertNotNull(mcpService.getProfile(otherProfile));
		} finally {
			mcpServiceImpl.createMissingDefaultProfiles();
			dbInstance.commitAndCloseSession();
			mcpModule.setDefaultProfilesCreated(true);
		}
	}

	private void deleteDefaultProfiles() {
		for(String identifier:List.of(McpServiceImpl.DEFAULT_LEARNER, McpServiceImpl.DEFAULT_LECTURE_MANAGER,
				McpServiceImpl.DEFAULT_AUTHOR, McpServiceImpl.DEFAULT_PROJECT_PILOT)) {
			McpProfile profile = mcpService.getProfileByIdentifier(identifier);
			if(profile != null) {
				mcpService.deleteProfile(profile);
			}
		}
		dbInstance.commitAndCloseSession();
	}

	private void assertNoDefaultProfiles() {
		Assert.assertNull(mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_LEARNER));
		Assert.assertNull(mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_LECTURE_MANAGER));
		Assert.assertNull(mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_AUTHOR));
		Assert.assertNull(mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_PROJECT_PILOT));
	}

	private void assertDefaultProfiles() {
		List<McpProfile> defaults = mcpService.getProfiles().stream()
				.filter(p -> p.getIdentifier().startsWith("default-"))
				.toList();
		Assert.assertEquals(4, defaults.size());
		assertThat(defaults).noneMatch(McpProfile::isEnabled);
		for(McpProfile defaultProfile:defaults) {
			assertThat(mcpService.getOrganisations(defaultProfile))
				.containsExactly(JunitTestHelper.getDefaultOrganisation());
		}

		McpProfile learner = mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_LEARNER);
		Assert.assertEquals(McpProfileType.system, learner.getType());
		Assert.assertEquals("Learner", learner.getName());
		assertThat(learner.getOrganisationRoles()).containsExactly(OrganisationRoles.user);
		assertThat(learner.getTools())
			.containsExactlyInAnyOrder("openolat_whoami", "course_query", "lecture_query", "todo_query", "news_query");

		McpProfile lectureManager = mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_LECTURE_MANAGER);
		Assert.assertEquals(McpProfileType.system, lectureManager.getType());
		Assert.assertEquals("Lecture manager", lectureManager.getName());
		assertThat(lectureManager.getOrganisationRoles()).containsExactly(OrganisationRoles.lecturemanager);
		assertThat(lectureManager.getTools())
			.containsExactlyInAnyOrder("openolat_whoami", "course_query", "lecture_query", "todo_query", "news_query");

		McpProfile author = mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_AUTHOR);
		Assert.assertEquals(McpProfileType.system, author.getType());
		assertThat(author.getOrganisationRoles()).containsExactly(OrganisationRoles.author);
		assertThat(author.getTools())
			.containsExactlyInAnyOrder("openolat_whoami", "course_query", "lecture_query", "project_query", "note_query", "file_query", "timeline_query", "todo_query", "news_query");

		McpProfile pilot = mcpService.getProfileByIdentifier(McpServiceImpl.DEFAULT_PROJECT_PILOT);
		Assert.assertEquals(McpProfileType.user, pilot.getType());
		Assert.assertTrue(pilot.getOrganisationRoles().isEmpty());
		Assert.assertEquals(0, mcpProfileDao.countGrantedIdentities(pilot));
		assertThat(pilot.getTools())
			.containsExactlyInAnyOrder("openolat_whoami", "project_query", "project_modify", "timeline_query", "note_query", "note_modify",
					"file_query", "file_modify", "appointment_query", "appointment_modify", "milestone_query", "milestone_modify",
					"decision_query", "decision_modify", "todo_query", "todo_modify", "news_query");
	}
}
