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
package org.olat.mcp;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;

/**
 * Profiles and grant resolution of the MCP server. Every change of a profile
 * or of a grant fires a {@link McpProfileChangedEvent} on the event bus, the
 * identity cache of every node drops its entries.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface McpService {

	/**
	 * Creates a disabled profile without tools, linked to the organisations.
	 *
	 * @param identifier The unique slug, see {@link McpProfile#IDENTIFIER_PATTERN}
	 * @param name The display name
	 * @param type system or user
	 * @param organisations The organisations of the profile, at least one
	 * @return The persisted profile
	 * @throws IllegalArgumentException if the identifier is invalid or taken,
	 *         or if no organisation is given
	 */
	McpProfile createProfile(String identifier, String name, McpProfileType type,
			Collection<? extends OrganisationRef> organisations);

	/**
	 * Persists the changes of name, description, enabled, tools, roles and rate limit.
	 *
	 * @param profile The profile with the changes
	 * @return The merged profile
	 * @throws IllegalArgumentException if a value is invalid or if the profile
	 *         is enabled and has no organisation; the UI validates first
	 */
	McpProfile updateProfile(McpProfile profile);

	/**
	 * @param profile The profile
	 * @return The organisations linked to the profile, ordered by display name
	 */
	List<Organisation> getOrganisations(McpProfileRef profile);

	/**
	 * For the list of profiles: the organisations of every profile in one query.
	 *
	 * @return The organisations ordered by display name, by key of the profile
	 */
	Map<Long,List<Organisation>> getOrganisationsByProfileKey();

	/**
	 * Replaces the organisations of the profile. A profile applies to its
	 * organisations and to every organisation below them.
	 *
	 * @param profile The profile
	 * @param organisations The new organisations, at least one
	 * @throws IllegalArgumentException if the list is null or empty
	 */
	void setOrganisations(McpProfile profile, Collection<? extends OrganisationRef> organisations);

	/**
	 * Deletes the profile and its grants.
	 *
	 * @param profile The profile
	 */
	void deleteProfile(McpProfileRef profile);

	McpProfile getProfile(McpProfileRef profile);

	McpProfile getProfileByIdentifier(String identifier);

	/**
	 * @return All profiles ordered by name
	 */
	List<McpProfile> getProfiles();

	/**
	 * Adds an explicit grant to a user profile. A grant on a system profile
	 * is accepted but has no effect on the resolution.
	 *
	 * @param profile The profile
	 * @param identity The identity
	 */
	void addGrant(McpProfile profile, IdentityRef identity);

	void removeGrant(McpProfile profile, IdentityRef identity);

	boolean hasGrant(McpProfileRef profile, IdentityRef identity);

	/**
	 * @param profile The profile
	 * @param firstResult The offset
	 * @param maxResults The page size
	 * @return The identities with an explicit grant, with their user, ordered by key
	 */
	List<Identity> getGrantedIdentities(McpProfileRef profile, int firstResult, int maxResults);
	
	int countGrantedIdentities(McpProfileRef profile);

	/**
	 * @param identity The identity
	 * @return The number of enabled and disabled profiles with an explicit grant for the identity
	 */
	int countGrants(IdentityRef identity);

	/**
	 * The user profiles, enabled or not, with an explicit grant for the identity.
	 * For the MCP segment of the user administration.
	 *
	 * @param identity The identity
	 * @return The user profiles ordered by name
	 */
	List<McpProfile> getUserProfilesOf(IdentityRef identity);

	/**
	 * The user profiles whose members the manager may change for the user: the
	 * profiles visible for an organisation in which the user has the role
	 * <code>user</code> and the manager is administrator or user manager
	 * (inherited roles included).
	 *
	 * @param managerRoles The roles of the manager
	 * @param user The user
	 * @return The user profiles ordered by name
	 */
	List<McpProfile> getManageableUserProfiles(Roles managerRoles, IdentityRef user);

	/**
	 * The rule of the user administration (UserAdminController): the manager
	 * is administrator or user manager of an organisation in which the user
	 * has the role <code>user</code>, or a system administrator.
	 *
	 * @param managerRoles The roles of the manager
	 * @param user The user
	 * @return true if the manager may add and remove the user as member of the
	 *         user profiles of {@link #getManageableUserProfiles(Roles, IdentityRef)}
	 */
	boolean canManageMembers(Roles managerRoles, IdentityRef user);

	/**
	 * The enabled profiles that apply to the identity: a profile applies to
	 * its organisations and every organisation below them. In this scope a
	 * system profile matches a role
	 * of the identity, a user profile an explicit grant plus the role
	 * <code>user</code>. The memberships are read from the database.
	 *
	 * @param identity The identity
	 * @return The matching enabled profiles ordered by identifier, empty for no access
	 */
	List<McpProfile> getProfilesOf(IdentityRef identity);

	/**
	 * The union of the matching enabled profiles, see {@link #getProfilesOf(IdentityRef)}.
	 *
	 * @param identity The identity
	 * @return The grants, {@link McpGrants#EMPTY} for no access, never null
	 */
	McpGrants resolveGrants(IdentityRef identity);

}
