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
import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileRef;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.model.McpProfileImpl;
import org.olat.mcp.model.McpProfileToIdentityImpl;
import org.olat.mcp.model.McpProfileToOrganisationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Persistence of the MCP profiles (table o_mcp_profile), of the explicit
 * grants of the user profiles (table o_mcp_profile_to_identity) and of the
 * organisations of the profiles (table o_mcp_profile_to_organisation). The number
 * of profiles is small and defined by the administrators, the number of
 * grants can be large: the lists of grants are paginated.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
@Service
public class McpProfileDAO {

	@Autowired
	private DB dbInstance;

	/**
	 * Creates a disabled profile without tools and roles.
	 */
	public McpProfile createProfile(String identifier, String name, McpProfileType type) {
		McpProfileImpl profile = new McpProfileImpl();
		Date now = new Date();
		profile.setCreationDate(now);
		profile.setLastModified(now);
		profile.setIdentifier(identifier);
		profile.setName(name);
		profile.setType(type);
		profile.setEnabled(false);
		profile.setTools(null);
		dbInstance.getCurrentEntityManager().persist(profile);
		return profile;
	}

	public McpProfile updateProfile(McpProfile profile) {
		profile.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(profile);
	}

	/**
	 * Deletes the grants of the profile and the profile.
	 *
	 * @return The number of deleted profiles, 0 or 1
	 */
	public int deleteProfile(McpProfileRef profile) {
		deleteGrants(profile);
		deleteOrganisationRelations(profile);
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from mcpprofile profile where profile.key=:profileKey")
				.setParameter("profileKey", profile.getKey())
				.executeUpdate();
	}

	public McpProfile loadByKey(Long key) {
		List<McpProfile> profiles = dbInstance.getCurrentEntityManager()
				.createQuery("select profile from mcpprofile profile where profile.key=:profileKey", McpProfile.class)
				.setParameter("profileKey", key)
				.getResultList();
		return profiles.isEmpty() ? null : profiles.get(0);
	}

	public McpProfile loadByIdentifier(String identifier) {
		List<McpProfile> profiles = dbInstance.getCurrentEntityManager()
				.createQuery("select profile from mcpprofile profile where profile.identifier=:identifier", McpProfile.class)
				.setParameter("identifier", identifier)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return profiles.isEmpty() ? null : profiles.get(0);
	}

	public long countProfiles() {
		Long count = dbInstance.getCurrentEntityManager()
				.createQuery("select count(profile.key) from mcpprofile profile", Long.class)
				.getSingleResult();
		return count == null ? 0l : count.longValue();
	}

	/**
	 * @return All profiles ordered by name and key
	 */
	public List<McpProfile> loadProfiles() {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select profile from mcpprofile profile order by profile.name asc, profile.key asc", McpProfile.class)
				.getResultList();
	}

	/**
	 * The enabled profiles which apply to the identity, in one query. A profile
	 * applies to its organisations and to every organisation below them (the
	 * materialized path of the organisation starts with the path of the linked
	 * organisation). In this scope:
	 * <ul>
	 *  <li>a system profile matches if the identity has one of the roles of the
	 *      profile in an organisation of the scope; the roles are stored
	 *      comma-separated and compared as <code>,role,</code></li>
	 *  <li>a user profile matches if the identity has a grant and the role
	 *      <code>user</code> in an organisation of the scope</li>
	 * </ul>
	 * Deleted organisations are ignored.
	 *
	 * @param identity The identity
	 * @return The matching enabled profiles ordered by identifier
	 */
	public List<McpProfile> loadEnabledProfilesOf(IdentityRef identity) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("select profile from mcpprofile profile")
		  .append(" where profile.enabled=true and exists (select membership.key from mcpprofiletoorganisation rel")
		  .append("  inner join rel.organisation profileOrg,")
		  .append("  organisation memberOrg")
		  .append("  inner join memberOrg.group memberGroup")
		  .append("  inner join memberGroup.members membership")
		  .append("  where rel.profile.key=profile.key and membership.identity.key=:identityKey")
		  .append("  and memberOrg.materializedPathKeys like concat(profileOrg.materializedPathKeys, '%')")
		  .append("  and (profileOrg.status is null or profileOrg.status<>:deletedStatus)")
		  .append("  and (memberOrg.status is null or memberOrg.status<>:deletedStatus)")
		  .append("  and (")
		  .append("   (profile.type=:systemType and concat(',', profile.organisationRolesString, ',') like concat('%,', membership.role, ',%'))")
		  .append("   or (profile.type=:userType and membership.role=:userRole and exists (select grantRel.key from mcpprofiletoidentity grantRel")
		  .append("     where grantRel.profile.key=profile.key and grantRel.identity.key=:identityKey))")
		  .append("  )")
		  .append(" ) order by profile.identifier asc");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), McpProfile.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("deletedStatus", OrganisationStatus.deleted.name())
				.setParameter("systemType", McpProfileType.system)
				.setParameter("userType", McpProfileType.user)
				.setParameter("userRole", OrganisationRoles.user.name())
				.getResultList();
	}

	/**
	 * The user profiles, enabled or not, visible for the organisations: a
	 * profile is visible for an organisation if it is linked to it or to one
	 * of its ancestors.
	 *
	 * @param organisations The organisations
	 * @return The user profiles ordered by name and key
	 */
	public List<McpProfile> loadUserProfilesVisibleFor(Collection<? extends OrganisationRef> organisations) {
		if(organisations == null || organisations.isEmpty()) {
			return new ArrayList<>();
		}
		List<Long> organisationKeys = organisations.stream()
				.map(OrganisationRef::getKey)
				.distinct()
				.toList();

		StringBuilder sb = new StringBuilder(512);
		sb.append("select profile from mcpprofile profile")
		  .append(" where profile.type=:userType and exists (select rel.key from mcpprofiletoorganisation rel")
		  .append("  inner join rel.organisation profileOrg,")
		  .append("  organisation targetOrg")
		  .append("  where rel.profile.key=profile.key and targetOrg.key in (:organisationKeys)")
		  .append("  and targetOrg.materializedPathKeys like concat(profileOrg.materializedPathKeys, '%')")
		  .append("  and (profileOrg.status is null or profileOrg.status<>:deletedStatus)")
		  .append(" ) order by profile.name asc, profile.key asc");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), McpProfile.class)
				.setParameter("userType", McpProfileType.user)
				.setParameter("organisationKeys", organisationKeys)
				.setParameter("deletedStatus", OrganisationStatus.deleted.name())
				.getResultList();
	}

	/**
	 * @param identity The identity
	 * @return The user profiles, enabled or not, with a grant for the identity, ordered by name and key
	 */
	public List<McpProfile> loadUserProfilesOf(IdentityRef identity) {
		String query = """
				select profile from mcpprofile profile
				where profile.type=:userType and exists (select rel.key from mcpprofiletoidentity rel
				 where rel.profile.key=profile.key and rel.identity.key=:identityKey)
				order by profile.name asc, profile.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, McpProfile.class)
				.setParameter("userType", McpProfileType.user)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	public McpProfileToOrganisationImpl createOrganisationRelation(McpProfileRef profile, OrganisationRef organisation) {
		McpProfileToOrganisationImpl rel = new McpProfileToOrganisationImpl();
		rel.setCreationDate(new Date());
		rel.setProfile(dbInstance.getCurrentEntityManager().getReference(McpProfileImpl.class, profile.getKey()));
		rel.setOrganisation(dbInstance.getCurrentEntityManager().getReference(OrganisationImpl.class, organisation.getKey()));
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}

	/**
	 * @return The number of deleted relations
	 */
	public int deleteOrganisationRelation(McpProfileRef profile, OrganisationRef organisation) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from mcpprofiletoorganisation rel where rel.profile.key=:profileKey and rel.organisation.key=:organisationKey")
				.setParameter("profileKey", profile.getKey())
				.setParameter("organisationKey", organisation.getKey())
				.executeUpdate();
	}

	/**
	 * @return The number of deleted relations of the profile
	 */
	public int deleteOrganisationRelations(McpProfileRef profile) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from mcpprofiletoorganisation rel where rel.profile.key=:profileKey")
				.setParameter("profileKey", profile.getKey())
				.executeUpdate();
	}

	/**
	 * @return The number of deleted relations of the organisation
	 */
	public int deleteOrganisationRelations(OrganisationRef organisation) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from mcpprofiletoorganisation rel where rel.organisation.key=:organisationKey")
				.setParameter("organisationKey", organisation.getKey())
				.executeUpdate();
	}

	/**
	 * @param profile The profile
	 * @return The organisations linked to the profile, ordered by display name and key
	 */
	public List<Organisation> loadOrganisations(McpProfileRef profile) {
		String query = """
				select org from mcpprofiletoorganisation rel
				inner join rel.organisation org
				where rel.profile.key=:profileKey
				order by org.displayName asc, org.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Organisation.class)
				.setParameter("profileKey", profile.getKey())
				.getResultList();
	}

	/**
	 * @return Every relation of a profile to an organisation, with the organisation
	 */
	public List<McpProfileToOrganisationImpl> loadOrganisationRelations() {
		String query = """
				select rel from mcpprofiletoorganisation rel
				inner join fetch rel.organisation org
				order by org.displayName asc, org.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, McpProfileToOrganisationImpl.class)
				.getResultList();
	}

	/**
	 * @param organisation The organisation
	 * @return The profiles linked to the organisation
	 */
	public List<McpProfile> loadProfiles(OrganisationRef organisation) {
		String query = """
				select profile from mcpprofiletoorganisation rel
				inner join rel.profile profile
				where rel.organisation.key=:organisationKey
				order by profile.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, McpProfile.class)
				.setParameter("organisationKey", organisation.getKey())
				.getResultList();
	}

	public int countOrganisations(McpProfileRef profile) {
		Long count = dbInstance.getCurrentEntityManager()
				.createQuery("select count(rel.key) from mcpprofiletoorganisation rel where rel.profile.key=:profileKey", Long.class)
				.setParameter("profileKey", profile.getKey())
				.getSingleResult();
		return count == null ? 0 : count.intValue();
	}

	public McpProfileToIdentityImpl createGrant(McpProfileRef profile, IdentityRef identity) {
		McpProfileToIdentityImpl rel = new McpProfileToIdentityImpl();
		rel.setCreationDate(new Date());
		rel.setProfile(dbInstance.getCurrentEntityManager().getReference(McpProfileImpl.class, profile.getKey()));
		rel.setIdentity(dbInstance.getCurrentEntityManager().getReference(IdentityImpl.class, identity.getKey()));
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}

	/**
	 * @return The number of deleted grants
	 */
	public int deleteGrant(McpProfileRef profile, IdentityRef identity) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from mcpprofiletoidentity rel where rel.profile.key=:profileKey and rel.identity.key=:identityKey")
				.setParameter("profileKey", profile.getKey())
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}

	/**
	 * @return The number of deleted grants of the profile
	 */
	public int deleteGrants(McpProfileRef profile) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from mcpprofiletoidentity rel where rel.profile.key=:profileKey")
				.setParameter("profileKey", profile.getKey())
				.executeUpdate();
	}

	/**
	 * @return The number of deleted grants of the identity
	 */
	public int deleteGrants(IdentityRef identity) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from mcpprofiletoidentity rel where rel.identity.key=:identityKey")
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}

	public boolean hasGrant(McpProfileRef profile, IdentityRef identity) {
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery("select rel.key from mcpprofiletoidentity rel where rel.profile.key=:profileKey and rel.identity.key=:identityKey", Long.class)
				.setParameter("profileKey", profile.getKey())
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return !keys.isEmpty();
	}

	/**
	 * @param profile The profile
	 * @param firstResult The first result
	 * @param maxResults The maximum number of identities, -1 for all
	 * @return The identities with a grant, with their user, ordered by key
	 */
	public List<Identity> loadGrantedIdentities(McpProfileRef profile, int firstResult, int maxResults) {
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery("select ident from mcpprofiletoidentity rel inner join rel.identity ident inner join fetch ident.user identUser where rel.profile.key=:profileKey order by ident.key asc", Identity.class)
				.setParameter("profileKey", profile.getKey());
		if(firstResult > 0) {
			query.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			query.setMaxResults(maxResults);
		}
		return query.getResultList();
	}

	public int countGrantedIdentities(McpProfileRef profile) {
		Long count = dbInstance.getCurrentEntityManager()
				.createQuery("select count(rel.key) from mcpprofiletoidentity rel where rel.profile.key=:profileKey", Long.class)
				.setParameter("profileKey", profile.getKey())
				.getSingleResult();
		return count == null ? 0 : count.intValue();
	}

	/**
	 * @return The number of profiles, enabled or not, with a grant for the identity
	 */
	public int countGrants(IdentityRef identity) {
		Long count = dbInstance.getCurrentEntityManager()
				.createQuery("select count(rel.key) from mcpprofiletoidentity rel where rel.identity.key=:identityKey", Long.class)
				.setParameter("identityKey", identity.getKey())
				.getSingleResult();
		return count == null ? 0 : count.intValue();
	}
}
