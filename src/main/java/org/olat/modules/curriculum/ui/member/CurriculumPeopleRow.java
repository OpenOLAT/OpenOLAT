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
package org.olat.modules.curriculum.ui.member;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityOrganisationsRow;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 17 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumPeopleRow extends UserPropertiesRow implements IdentityOrganisationsRow {
	
	private FormLink toolsLink;
	private int numOfReservations;
	private final int numOfImplementations;
	private final List<CurriculumRoles> roles;
	private List<OrganisationWithParents> organisations;
	
	public CurriculumPeopleRow(Identity member, List<CurriculumRoles> roles, int numOfImplementations,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(member, userPropertyHandlers, locale);
		this.roles = roles;
		this.numOfImplementations = numOfImplementations;
	}

	public List<CurriculumRoles> getRoles() {
		return roles;
	}

	public int getNumOfImplementations() {
		return numOfImplementations;
	}

	public int getNumOfReservations() {
		return numOfReservations;
	}

	public void addNumOfReservations(long reservations) {
		if(reservations > 0) {
			numOfReservations += reservations;
		}
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public List<OrganisationWithParents> getOrganisations() {
		return organisations;
	}

	@Override
	public void setOrganisations(List<OrganisationWithParents> organisations) {
		this.organisations = organisations;
	}
}
