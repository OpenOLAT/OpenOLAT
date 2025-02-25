/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.basesecurity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.model.OrganisationMember;
import org.olat.basesecurity.model.OrganisationMembershipStats;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.basesecurity.model.SearchMemberParameters;
import org.olat.basesecurity.model.SearchOrganisationParameters;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface OrganisationService {
	
	public static final String DEFAULT_ORGANISATION_IDENTIFIER = "default-org";
	public static final OLATResourceable ORGANISATIONS_CHANGED_EVENT_CHANNEL = OresHelper
			.createOLATResourceableType("OrganisationChangedChannel");

	/**
	 * Create a brand new organization. The membership inheritance
	 * will be automatically calculated and propagated.
	 * 
	 * @param displayName The name of the organization
	 * @param identifier The identifier
	 * @param description The description
	 * @param parentOrganisation The Parent organization if any
	 * @param type The type
	 * @return The persisted organization
	 */
	public Organisation createOrganisation(String displayName, String identifier, String description,
			Organisation parentOrganisation, OrganisationType type, Identity doer);
	
	/**
	 * The default organization.
	 * 
	 * @return The default organization.
	 */
	public Organisation getDefaultOrganisation();
	
	/**
	 * 
	 * @param organisation A reference of the organization
	 * @return A reloaded organization
	 */
	public Organisation getOrganisation(OrganisationRef organisation);
	
	public List<Organisation> getOrganisation(Collection<? extends OrganisationRef> organisations);
	
	public List<Organisation> findOrganisationByIdentifier(String identifier);
	
	public List<Organisation> findOrganisations(SearchOrganisationParameters searchparams);
	
	public List<Organisation> getOrganisationParentLine(Organisation organisation);
	
	public List<OrganisationRef> getParentLineRefs(List<Organisation> organisations);
	
	public Organisation updateOrganisation(Organisation organisation);
	
	/**
	 * Delete or mark as deleted the organization and its children.
	 * 
	 * @param organisation
	 */
	public void deleteOrganisation(OrganisationRef organisation, OrganisationRef organisationAlt, Identity doer);
	
	/**
	 * Move an organization to a new place in the organization structure.
	 * 
	 * @param organisationToMove The organization to move
	 * @param newParent The new parent
	 */
	public void moveOrganisation(OrganisationRef organisationToMove, OrganisationRef newParent, Identity doer);
	
	/**
	 * 
	 * @return A list of active or inactive organisations.
	 */
	public List<Organisation> getOrganisations();
	
	/**
	 * 
	 * @return Returns an immutable list of organisations, ordered by its tree structure,
	 *   default organisation first and alphabetically. The list is cached.
	 */
	public List<OrganisationWithParents> getOrderedTreeOrganisationsWithParents();
	
	/**
	 * 
	 * @return true if an other organisation as the default is available
	 */
	public boolean isMultiOrganisations();
	
	/**
	 * Search the organisations by status.
	 * 
	 * @param status The status (mandatory)
	 * @return A list of organisations
	 */
	public List<Organisation> getOrganisations(OrganisationStatus[] status);
	
	/**
	 * Create a new organization type.
	 * 
	 * @param displayName The name
	 * @param identifier The identifier of the type
	 * @param description The description
	 * @return A persisted organization type
	 */
	public OrganisationType createOrganisationType(String displayName, String identifier, String description);
	
	/**
	 * Reload an organization type.
	 * 
	 * @param type The primary key
	 * @return A fresh organization type
	 */
	public OrganisationType getOrganisationType(OrganisationTypeRef type);
	
	/**
	 * Update the type without touching to the sub-types.
	 * 
	 * @param type
	 * @param allowedSubTypes
	 * @return
	 */
	public OrganisationType updateOrganisationType(OrganisationType type);
	
	/**
	 * Update the type and the relation to sub-types.
	 * 
	 * @param type The type to update
	 * @param allowedSubTypes The sub-types allowed
	 * @return The merged type
	 */
	public OrganisationType updateOrganisationType(OrganisationType type, List<OrganisationType> allowedSubTypes);
	
	public void allowOrganisationSubType(OrganisationType parentType, OrganisationType allowedSubType);
	
	public void disallowOrganisationSubType(OrganisationType parentType, OrganisationType allowedSubType);
	
	/**
	 * @return The list of organization types
	 */
	public List<OrganisationType> getOrganisationTypes();
	
	public OrganisationEmailDomain createOrganisationEmailDomain(Organisation organisation, String domain);

	public OrganisationEmailDomain updateOrganisationEmailDomain(OrganisationEmailDomain emailDomain);

	public void deleteOrganisationEmailDomain(OrganisationEmailDomain organisationEmailDomain);
	
	public List<OrganisationEmailDomain> getEmailDomains(OrganisationEmailDomainSearchParams searchParams);

	public Map<Long, Integer> getEmailDomainKeyToUsersCount(List<OrganisationEmailDomain> emailDomains);

	public List<OrganisationEmailDomain> getEnabledEmailDomains(OrganisationRef organisation);
	
	public boolean isEmailDomainAllowed(List<OrganisationEmailDomain> emailDomains, String emailAddress);

	/**
	 * Retrieves a list of matching email domains based on the given mail domain.
	 * The method follows these rules:
	 * <ul>
	 *   <li>First, it checks for exact matches in the provided email domain list.</li>
	 *   <li>If exact matches exist, they are returned.</li>
	 *   <li>If no exact match is found, it retrieves all wildcard domains (domains marked as "*").</li>
	 *   <li>If neither exact matches nor wildcard domains exist, an empty list is returned.</li>
	 * </ul>
	 *
	 * @param emailDomains A list of {@link OrganisationEmailDomain} objects to search within.
	 * @param mailDomain The email domain to match against the list.
	 * @return A list of matching {@link OrganisationEmailDomain} objects, or an empty list if no matches are found.
	 */
	public List<OrganisationEmailDomain> getMatchingEmailDomains(List<OrganisationEmailDomain> emailDomains, String mailDomain);
	
	public VFSContainer getLegalContainer(OrganisationRef organisation);

	/**
	 * The list of all organizations where the user has the specified roles,
	 * with inheritance in the organization structure dependent of the role.
	 * 
	 * @param member The user (mandatory)
	 * @param role The roles (mandatory)
	 * @return A list of organization where the user has the specified roles
	 */
	public List<Organisation> getOrganisations(IdentityRef member, OrganisationRoles... role);
	
	public List<OrganisationRef> getOrganisationsWithParentLines(IdentityRef member, OrganisationRoles... role);
	
	/**
	 * The list of organizations where the user has the specified roles,
	 * without any inheritance in the organization structure.
	 * 
	 * @param member The user (mandatory)
	 * @param role The roles (mandatory)
	 * @return A list of organization where the user has the specified roles
	 */
	public List<Organisation> getOrganisationsNotInherited(IdentityRef member, OrganisationRoles... role);
	
	/**
	 * Return the organization the specified user is allow to see. 
	 * An OpenOLAT admin. can see all organizations.
	 * 
	 * 
	 * @param member The user 
	 * @param roles The roles of the specified user
	 * @param managerRole The additional manager
	 * @return A list of organizations a user can manage
	 */
	public List<Organisation> getOrganisations(IdentityRef member, Roles roles, OrganisationRoles... managerRole);
	
	/**
	 * 
	 * @param identities A list of identities
	 * @return A map with the identity key and the associated names of organisation where the user is a user.
	 */
	public Map<Long,List<String>> getUsersOrganisationsNames(List<IdentityRef> identities);
	
	/**
	 * Add a membership without inheritance on the default organization.
	 * 
	 * @param member The identity
	 * @param role The role in the organization
	 */
	public void addMember(Identity member, OrganisationRoles role, Identity doer);
	
	/**
	 * Add a membership on the specified organization. The inheritance mode "root"
	 * will be automatically applied to learn resource manager, author and user manager.
	 * This role will be propagated to child-organizations as "inherithed".
	 * 
	 * @param organisation The organization
	 * @param member The new member of the organization
	 * @param role The role in the organization
	 */
	public void addMember(Organisation organisation, Identity member, OrganisationRoles role, Identity doer);
	
	/**
	 * A method to fine set role. 
	 * 
	 * @param organisation The organization
	 * @param member The new member of the organization
	 * @param role The role in the organization
	 * @param inheritanceMode The inheritance mode (none, root)
	 */
	public void addMember(Organisation organisation, Identity member, OrganisationRoles role, GroupMembershipInheritance inheritanceMode, Identity doer);
	
	/**
	 * Remove the role on the default organisation.
	 * 
	 * @param member The user
	 * @param role The role to remove
	 */
	public void removeMember(IdentityRef member, OrganisationRoles role, Identity doer);

	/**
	 * Remove all roles the user has from the specified organization. The method
	 * will recursively remove the root and inherited roles.
	 * 
	 * @param organisation The organization
	 * @param member The member
	 */
	public void removeMember(Organisation organisation, IdentityRef member, Identity doer);
	
	/**
	 * Remove the specified user's role from the organization. The method will
	 * recursively remove root and inherited roles but only membership
	 * 
	 * @param organisation The organization
	 * @param member The user
	 * @param role The role to remove
	 * @param excludeInherited true if you want to ignore inherited membership
	 */
	public boolean removeMember(Organisation organisation, IdentityRef member, OrganisationRoles role, boolean excludeInherited, Identity doer);
	
	public void setAsGuest(Identity identity, Identity doer);
	
	public List<OrganisationMember> getMembers(Organisation organisation, SearchMemberParameters params);
	
	public List<Identity> getMembersIdentity(Organisation organisation, OrganisationRoles role);
	
	public boolean hasRole(IdentityRef identity, OrganisationRoles role);
	

	/**
	 * 
	 * @param SourceOrganisation
	 * @param targetOrganisation
	 * @param identities
	 * @param roles
	 */
	public void moveMembers(OrganisationRef sourceOrganisation, OrganisationRef targetOrganisation,
			List<Identity> identities, List<OrganisationRoles> roles, Identity doer);
	
	/**
	 * Check if the specified user has the list of roles in the organization.
	 * 
	 * @param organisation The organization
	 * @param identity The identity
	 * @param roles The roles (at least one)
	 * @return true if at least a role is found
	 */
	public boolean hasRole(IdentityRef identity, OrganisationRef organisation, OrganisationRoles... roles);
	
	/**
	 * Return true if the specified user has a role in the list of specified roles
	 * for an organization with the specified identifier.
	 * 
	 * @param organisationIdentifier An organization identifier (exact match)
	 * @param identity The identity
	 * @param roles A list of roles (need at least one)
	 * @return true if a role was found for the user and an organization with the given identifier
	 */
	public boolean hasRole(String organisationIdentifier, IdentityRef identity, OrganisationRoles... roles);
	
	
	public List<Identity> getDefaultsSystemAdministator();
	
	public List<Identity> getIdentitiesWithRole(OrganisationRoles role);
	
	public List<Long> getMemberKeys(OrganisationRef organisation, OrganisationRoles... roles);
	
	/**
	 * 
	 * @param organisation The organization (mandatory)
	 * @param identities A list of identities, not null, not empty.
	 * @return The number of user with a role in the organization
	 */
	public List<OrganisationMembershipStats> getOrganisationStatistics(OrganisationRef organisation, List<IdentityRef> identities);

	/**
	 * @param organisation The organisation
	 * @param role Organisation role
	 * @return A list of rights for the role of a given organisation
	 */
	public List<RightProvider> getGrantedOrganisationRights(Organisation organisation, OrganisationRoles role);

	/**
	 * @param roles Given organisation role
	 * @return A list of all rights selectable for organisation rights
	 */
	public List<RightProvider> getAllOrganisationRights(OrganisationRoles roles);

	/**
	 * Save the given rights for a specific role in an organisation
	 *
	 * @param organisation
	 * @param role
	 * @param rights
	 */
	public void setGrantedOrganisationRights(Organisation organisation, OrganisationRoles role, Collection<String> rights);

}
