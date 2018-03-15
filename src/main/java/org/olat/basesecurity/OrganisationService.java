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
	
	public void addMember(Organisation organisation, Identity member, GroupRoles role);

	public void removeMember(Organisation organisation, IdentityRef member);
	
	public void removeMember(Organisation organisation, IdentityRef member, GroupRoles role);
	
	public List<OrganisationMember> getMembers(Organisation organisation);

}
