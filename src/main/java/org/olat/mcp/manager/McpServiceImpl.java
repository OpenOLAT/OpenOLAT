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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.mcp.McpGrants;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileChangedEvent;
import org.olat.mcp.McpProfileRef;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.McpService;
import org.olat.mcp.model.McpProfileImpl;
import org.olat.mcp.model.McpProfileToOrganisationImpl;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Profiles and grant resolution of the MCP server.
 * <p>
 * Every change of a profile or of a grant is committed before the
 * {@link McpProfileChangedEvent} is fired. The event reaches the other
 * nodes asynchronously, a node which reloads the grants after the event
 * must see the committed state.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class McpServiceImpl implements McpService, UserDataDeletable, OrganisationDataDeletable, InitializingBean {

	private static final Logger log = Tracing.createLoggerFor(McpServiceImpl.class);

	private static final Pattern IDENTIFIER = Pattern.compile(McpProfile.IDENTIFIER_PATTERN);

	public static final String DEFAULT_LEARNER = "default-learner";
	public static final String DEFAULT_LECTURE_MANAGER = "default-lecturemanager";
	public static final String DEFAULT_AUTHOR = "default-author";
	public static final String DEFAULT_PROJECT_PILOT = "default-project-pilot";

	@Autowired
	private DB dbInstance;
	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpProfileDAO mcpProfileDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private OrganisationService organisationService;

	/**
	 * Seeds the four disabled default profiles once per instance: only if the
	 * flag of the module is not set and the table is empty. A failure, for
	 * example a parallel seeding on a second cluster node which hits the
	 * unique identifier, never stops the startup.
	 */
	@Override
	public void afterPropertiesSet() {
		if(!mcpModule.isDefaultProfilesCreated()) {
			try {
				if(mcpProfileDao.countProfiles() == 0) {
					createMissingDefaultProfiles();
				}
				dbInstance.commitAndCloseSession();
				mcpModule.setDefaultProfilesCreated(true);
			} catch (Exception e) {
				log.error("Cannot create the default MCP profiles", e);
				dbInstance.rollbackAndCloseSession();
			}
		}
	}

	/**
	 * Creates the default profiles whose identifier does not exist yet. The
	 * caller commits. At startup it runs only on an empty table, see
	 * {@link #afterPropertiesSet()}.
	 *
	 * @return The number of created profiles
	 */
	int createMissingDefaultProfiles() {
		Set<String> readTools = Set.of("openolat_whoami", "course_query", "lecture_query", "todo_query", "news_query");
		Set<String> authorTools = new HashSet<>(readTools);
		authorTools.add("project_query");
		authorTools.add("note_query");
		authorTools.add("file_query");
		authorTools.add("timeline_query");

		int created = 0;
		created += createDefaultProfile(DEFAULT_LEARNER, "Learner", McpProfileType.system,
				List.of(OrganisationRoles.user), readTools);
		created += createDefaultProfile(DEFAULT_LECTURE_MANAGER, "Lecture manager", McpProfileType.system,
				List.of(OrganisationRoles.lecturemanager), readTools);
		created += createDefaultProfile(DEFAULT_AUTHOR, "Author", McpProfileType.system,
				List.of(OrganisationRoles.author), authorTools);
		created += createDefaultProfile(DEFAULT_PROJECT_PILOT, "Project pilot", McpProfileType.user,
				List.of(), Set.of("openolat_whoami", "project_query", "project_modify", "timeline_query", "note_query", "note_modify",
						"file_query", "file_modify", "appointment_query", "appointment_modify", "milestone_query", "milestone_modify",
						"decision_query", "decision_modify", "todo_query", "todo_modify", "news_query"));
		if(created > 0) {
			log.info("{} default MCP profiles created (disabled) and linked to the default organisation", created);
		}
		return created;
	}

	private int createDefaultProfile(String identifier, String name, McpProfileType type,
			List<OrganisationRoles> roles, Set<String> tools) {
		if(mcpProfileDao.loadByIdentifier(identifier) != null) {
			return 0;
		}
		McpProfile profile = mcpProfileDao.createProfile(identifier, name, type);
		profile.setOrganisationRoles(roles);
		profile.setTools(tools);
		mcpProfileDao.updateProfile(profile);
		mcpProfileDao.createOrganisationRelation(profile, organisationService.getDefaultOrganisation());
		return 1;
	}

	@Override
	public McpProfile createProfile(String identifier, String name, McpProfileType type,
			Collection<? extends OrganisationRef> organisations) {
		validateIdentifier(identifier);
		List<OrganisationRef> uniqueOrganisations = validateOrganisations(organisations);
		if(type == null) {
			throw new IllegalArgumentException("The type of an MCP profile is mandatory");
		}
		if(!StringHelper.containsNonWhitespace(name)) {
			throw new IllegalArgumentException("The name of an MCP profile is mandatory");
		}
		if(name.length() > McpProfileImpl.NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("The name of an MCP profile is too long");
		}
		if(mcpProfileDao.loadByIdentifier(identifier) != null) {
			throw new IllegalArgumentException("The identifier of an MCP profile is already in use: " + identifier);
		}

		McpProfile profile = mcpProfileDao.createProfile(identifier, name, type);
		for(OrganisationRef organisation:uniqueOrganisations) {
			mcpProfileDao.createOrganisationRelation(profile, organisation);
		}
		dbInstance.commit();
		fireProfileChanged(profile.getKey());
		return profile;
	}

	/**
	 * @param organisations The organisations of a profile
	 * @return The organisations without duplicates
	 * @throws IllegalArgumentException if no organisation is given
	 */
	private static List<OrganisationRef> validateOrganisations(Collection<? extends OrganisationRef> organisations) {
		Map<Long,OrganisationRef> uniqueOrganisations = new LinkedHashMap<>();
		if(organisations != null) {
			for(OrganisationRef organisation:organisations) {
				if(organisation != null && organisation.getKey() != null) {
					uniqueOrganisations.putIfAbsent(organisation.getKey(), organisation);
				}
			}
		}
		if(uniqueOrganisations.isEmpty()) {
			throw new IllegalArgumentException("An MCP profile needs at least one organisation");
		}
		return new ArrayList<>(uniqueOrganisations.values());
	}

	/**
	 * @param identifier The identifier to check
	 * @return true if the identifier matches {@link McpProfile#IDENTIFIER_PATTERN}
	 */
	public static boolean isValidIdentifier(String identifier) {
		return identifier != null && IDENTIFIER.matcher(identifier).matches();
	}

	private void validateIdentifier(String identifier) {
		if(!isValidIdentifier(identifier)) {
			throw new IllegalArgumentException("Invalid identifier of an MCP profile: " + identifier);
		}
	}

	@Override
	public McpProfile updateProfile(McpProfile profile) {
		if(!StringHelper.containsNonWhitespace(profile.getName())) {
			throw new IllegalArgumentException("The name of an MCP profile is mandatory");
		}
		if(profile.getName().length() > McpProfileImpl.NAME_MAX_LENGTH) {
			throw new IllegalArgumentException("The name of an MCP profile is too long");
		}
		Integer rpm = profile.getRateLimitRequestsPerMinute();
		if(rpm != null && rpm.intValue() <= 0) {
			throw new IllegalArgumentException("The rate limit of an MCP profile must be positive or null");
		}
		if(profile.getType() == McpProfileType.user && !profile.getOrganisationRoles().isEmpty()) {
			// a user profile applies through grants only
			profile.setOrganisationRoles(List.of());
		}
		if(profile.isEnabled() && mcpProfileDao.countOrganisations(profile) == 0) {
			throw new IllegalArgumentException("An MCP profile without organisation cannot be enabled: " + profile.getIdentifier());
		}

		McpProfile mergedProfile = mcpProfileDao.updateProfile(profile);
		dbInstance.commit();
		fireProfileChanged(mergedProfile.getKey());
		return mergedProfile;
	}

	@Override
	public void deleteProfile(McpProfileRef profile) {
		if(profile == null || profile.getKey() == null) {
			return;
		}
		mcpProfileDao.deleteProfile(profile);
		dbInstance.commit();
		fireProfileChanged(profile.getKey());
	}

	@Override
	public List<Organisation> getOrganisations(McpProfileRef profile) {
		if(profile == null || profile.getKey() == null) {
			return new ArrayList<>();
		}
		return mcpProfileDao.loadOrganisations(profile);
	}

	@Override
	public Map<Long,List<Organisation>> getOrganisationsByProfileKey() {
		Map<Long,List<Organisation>> organisationsByProfileKey = new HashMap<>();
		for(McpProfileToOrganisationImpl rel:mcpProfileDao.loadOrganisationRelations()) {
			organisationsByProfileKey.computeIfAbsent(rel.getProfile().getKey(), key -> new ArrayList<>())
				.add(rel.getOrganisation());
		}
		return organisationsByProfileKey;
	}

	@Override
	public void setOrganisations(McpProfile profile, Collection<? extends OrganisationRef> organisations) {
		List<OrganisationRef> newOrganisations = validateOrganisations(organisations);
		Set<Long> newKeys = new HashSet<>();
		for(OrganisationRef organisation:newOrganisations) {
			newKeys.add(organisation.getKey());
		}

		boolean changed = false;
		Set<Long> currentKeys = new HashSet<>();
		for(Organisation current:mcpProfileDao.loadOrganisations(profile)) {
			currentKeys.add(current.getKey());
			if(!newKeys.contains(current.getKey())) {
				changed |= mcpProfileDao.deleteOrganisationRelation(profile, current) > 0;
			}
		}
		for(OrganisationRef organisation:newOrganisations) {
			if(!currentKeys.contains(organisation.getKey())) {
				mcpProfileDao.createOrganisationRelation(profile, organisation);
				changed = true;
			}
		}
		dbInstance.commit();
		if(changed) {
			fireProfileChanged(profile.getKey());
		}
	}

	@Override
	public McpProfile getProfile(McpProfileRef profile) {
		if(profile == null || profile.getKey() == null) {
			return null;
		}
		return mcpProfileDao.loadByKey(profile.getKey());
	}

	@Override
	public McpProfile getProfileByIdentifier(String identifier) {
		if(!StringHelper.containsNonWhitespace(identifier)) {
			return null;
		}
		return mcpProfileDao.loadByIdentifier(identifier);
	}

	@Override
	public List<McpProfile> getProfiles() {
		return mcpProfileDao.loadProfiles();
	}

	@Override
	public void addGrant(McpProfile profile, IdentityRef identity) {
		if(mcpProfileDao.hasGrant(profile, identity)) {
			return;
		}
		mcpProfileDao.createGrant(profile, identity);
		dbInstance.commit();
		fireProfileChanged(profile.getKey());
	}

	@Override
	public void removeGrant(McpProfile profile, IdentityRef identity) {
		int deleted = mcpProfileDao.deleteGrant(profile, identity);
		dbInstance.commit();
		if(deleted > 0) {
			fireProfileChanged(profile.getKey());
		}
	}

	@Override
	public boolean hasGrant(McpProfileRef profile, IdentityRef identity) {
		return mcpProfileDao.hasGrant(profile, identity);
	}

	@Override
	public List<Identity> getGrantedIdentities(McpProfileRef profile, int firstResult, int maxResults) {
		return mcpProfileDao.loadGrantedIdentities(profile, firstResult, maxResults);
	}

	@Override
	public int countGrantedIdentities(McpProfileRef profile) {
		return mcpProfileDao.countGrantedIdentities(profile);
	}

	@Override
	public int countGrants(IdentityRef identity) {
		return mcpProfileDao.countGrants(identity);
	}

	@Override
	public List<McpProfile> getUserProfilesOf(IdentityRef identity) {
		if(identity == null || identity.getKey() == null) {
			return new ArrayList<>();
		}
		return mcpProfileDao.loadUserProfilesOf(identity);
	}

	@Override
	public List<McpProfile> getManageableUserProfiles(Roles managerRoles, IdentityRef user) {
		if(managerRoles == null || user == null || user.getKey() == null) {
			return new ArrayList<>();
		}
		Roles userRoles = securityManager.getRoles(user);
		List<OrganisationRef> organisations = new ArrayList<>();
		for(OrganisationRef organisation:userRoles.getOrganisationsWithRole(OrganisationRoles.user)) {
			if(managerRoles.isSystemAdmin()
					|| managerRoles.hasSomeRoles(organisation, OrganisationRoles.administrator, OrganisationRoles.usermanager)) {
				organisations.add(organisation);
			}
		}
		return mcpProfileDao.loadUserProfilesVisibleFor(organisations);
	}

	@Override
	public boolean canManageMembers(Roles managerRoles, IdentityRef user) {
		if(managerRoles == null || user == null || user.getKey() == null) {
			return false;
		}
		if(managerRoles.isSystemAdmin()) {
			return true;
		}
		Roles userRoles = securityManager.getRoles(user);
		return managerRoles.isManagerOf(OrganisationRoles.administrator, userRoles)
				|| managerRoles.isManagerOf(OrganisationRoles.usermanager, userRoles);
	}

	@Override
	public List<McpProfile> getProfilesOf(IdentityRef identity) {
		if(identity == null || identity.getKey() == null) {
			return List.of();
		}
		return mcpProfileDao.loadEnabledProfilesOf(identity);
	}

	@Override
	public McpGrants resolveGrants(IdentityRef identity) {
		List<McpProfile> profiles = getProfilesOf(identity);
		return toGrants(profiles);
	}

	/**
	 * The union of the tools, the highest rate limit override and the sorted
	 * identifiers of the profiles.
	 *
	 * @param profiles The matching profiles
	 * @return The grants, {@link McpGrants#EMPTY} if the list is empty
	 */
	static McpGrants toGrants(List<McpProfile> profiles) {
		if(profiles == null || profiles.isEmpty()) {
			return McpGrants.EMPTY;
		}

		Set<String> tools = new HashSet<>();
		Integer requestsPerMinute = null;
		List<String> identifiers = new ArrayList<>(profiles.size());
		for(McpProfile profile:profiles) {
			tools.addAll(profile.getTools());
			Integer rpm = profile.getRateLimitRequestsPerMinute();
			if(rpm != null && (requestsPerMinute == null || rpm.intValue() > requestsPerMinute.intValue())) {
				requestsPerMinute = rpm;
			}
			identifiers.add(profile.getIdentifier());
		}
		identifiers.sort(null);
		return new McpGrants(tools, requestsPerMinute, identifiers);
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		int deleted = mcpProfileDao.deleteGrants(identity);
		dbInstance.commit();
		if(deleted > 0) {
			fireProfileChanged(null);
		}
	}

	/**
	 * Removes the links of the deleted organisation. With a replacement
	 * organisation, every affected profile is linked to the replacement
	 * organisation, unless it is already linked to it. Without a replacement
	 * organisation, a profile which loses its last organisation is disabled:
	 * it applies nowhere, and an administrator must give it an organisation
	 * before it is enabled again.
	 */
	@Override
	public boolean deleteOrganisationData(Organisation organisation, Organisation replacementOrganisation) {
		List<McpProfile> profiles = mcpProfileDao.loadProfiles(organisation);
		if(profiles.isEmpty()) {
			return true;
		}
		mcpProfileDao.deleteOrganisationRelations(organisation);
		boolean replace = replacementOrganisation != null
				&& !replacementOrganisation.getKey().equals(organisation.getKey());
		for(McpProfile profile:profiles) {
			if(replace) {
				boolean linked = mcpProfileDao.loadOrganisations(profile).stream()
						.anyMatch(org -> replacementOrganisation.getKey().equals(org.getKey()));
				if(!linked) {
					mcpProfileDao.createOrganisationRelation(profile, replacementOrganisation);
					log.info("MCP profile {} linked to the organisation {}: the organisation {} was deleted",
							profile.getIdentifier(), replacementOrganisation.getKey(), organisation.getKey());
				}
			} else if(profile.isEnabled() && mcpProfileDao.countOrganisations(profile) == 0) {
				McpProfile reloadedProfile = mcpProfileDao.loadByKey(profile.getKey());
				reloadedProfile.setEnabled(false);
				mcpProfileDao.updateProfile(reloadedProfile);
				log.info("MCP profile {} disabled: its last organisation {} was deleted",
						profile.getIdentifier(), organisation.getKey());
			}
		}
		dbInstance.commit();
		fireProfileChanged(null);
		return true;
	}

	private void fireProfileChanged(Long profileKey) {
		coordinatorManager.getCoordinator().getEventBus()
			.fireEventToListenersOf(new McpProfileChangedEvent(profileKey), McpProfileChangedEvent.ORES);
	}
}
