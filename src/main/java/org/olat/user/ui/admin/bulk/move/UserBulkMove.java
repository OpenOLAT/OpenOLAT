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
package org.olat.user.ui.admin.bulk.move;

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 27 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserBulkMove {
	
	private final List<Identity> identities;
	private final Organisation organisation;
	private Organisation targetOrganisation;
	private List<OrganisationRoles> roles;
	private List<Identity> identitiesToMove;
	
	public UserBulkMove(Organisation organisation, List<Identity> identities) {
		this.organisation = organisation;
		this.identities = identities;
	}

	public List<Identity> getIdentities() {
		return identities;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public Organisation getTargetOrganisation() {
		return targetOrganisation;
	}

	public void setTargetOrganisation(Organisation targetOrganisation) {
		this.targetOrganisation = targetOrganisation;
	}

	public List<OrganisationRoles> getRoles() {
		return roles;
	}

	public void setRoles(List<OrganisationRoles> roles) {
		this.roles = roles;
	}

	public List<Identity> getIdentitiesToMove() {
		return identitiesToMove;
	}
	
	public void setIdentitiesToMove(List<Identity> identitiesToMove) {
		this.identitiesToMove = identitiesToMove;
	}
}
