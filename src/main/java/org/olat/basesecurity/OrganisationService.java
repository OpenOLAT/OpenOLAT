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
package org.olat.basesecurity;

import java.util.List;

import org.olat.basesecurity.model.OrganisationMember;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface OrganisationService {
	
	public static final String DEFAULT_ORGANISATION_IDENTIFIER = "default-org";
	
	/**
	 * Create and persist a brand new organisation.
	 * 
	 * @param displayName The display name
	 * @param identifier The identifier
	 * @return The persisted organisation
	 */
	public Organisation createOrganisation(String displayName, String identifier, String description,
			Organisation parentOrganisation, OrganisationType type);
	
	/**
	 * 
	 * @param organisation A reference of the organisation
	 * @return A reloaded organisation
	 */
	public Organisation getOrganisation(OrganisationRef organisation);
	
	public Organisation updateOrganisation(Organisation organisation);
	
	public List<Organisation> getOrganisations();
	
	/**
	 * @param member The user (mandatory)
	 * @param role The roles (mandatory)
	 * @return A list of organization where the user has the specified roles
	 */
	public List<Organisation> getOrganisations(IdentityRef member, OrganisationRoles... role);
	
	/**
	 * 
	 * @param member
	 * @param roles
	 * @return
	 */
	public List<Organisation> getSearchableOrganisations(IdentityRef member, Roles roles);
	
	public Organisation getDefaultOrganisation();

	public void addMember(Organisation organisation, Identity member, OrganisationRoles role);
	
	/**
	 * 
	 * 
	 * @param member The identity
	 * @param role The role in the organisation
	 */
	public void addMember(Identity member, OrganisationRoles role);
	
	public void removeMember(IdentityRef member, OrganisationRoles role);

	public void removeMember(Organisation organisation, IdentityRef member);
	
	public void removeMember(Organisation organisation, IdentityRef member, OrganisationRoles role);
	
	public List<OrganisationMember> getMembers(Organisation organisation);

	
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
	
	public boolean hasRole(IdentityRef identity, OrganisationRoles role);
	
	public List<Identity> getIdentitiesWithRole(OrganisationRoles role);

}
