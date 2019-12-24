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
package org.olat.admin.user.bulkChange;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.groups.GroupChanges;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 20 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserBulkChanges extends GroupChanges {
	
	private final Map<OrganisationRoles, String> roleChangeMap = new EnumMap<>(OrganisationRoles.class);
	private final Map<String, String> attributeChangeMap = new HashMap<>();
	
	private List<Long> mailGroups;

	private List<Identity> identitiesToEdit;
	
	private Integer status;
	private boolean sendLoginDeniedEmail;
	
	private OrganisationRef organisation;
	
	public UserBulkChanges(OrganisationRef organisation) {
		this.organisation = organisation;
	}
	
	public OrganisationRef getOrganisation() {
		return organisation;
	}
	
	public void setOrganisation(OrganisationRef organisation) {
		this.organisation = organisation;
	}
	
	public Map<OrganisationRoles, String> getRoleChangeMap() {
		return roleChangeMap;
	}

	public Map<String, String> getAttributeChangeMap() {
		return attributeChangeMap;
	}

	public List<Identity> getIdentitiesToEdit() {
		return identitiesToEdit;
	}

	public void setIdentitiesToEdit(List<Identity> identitiesToEdit) {
		this.identitiesToEdit = identitiesToEdit;
	}

	public List<Long> getMailGroups() {
		return mailGroups;
	}

	public void setMailGroups(List<Long> mailGroups) {
		this.mailGroups = mailGroups;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public boolean isSendLoginDeniedEmail() {
		return sendLoginDeniedEmail;
	}

	public void setSendLoginDeniedEmail(boolean sendLoginDeniedEmail) {
		this.sendLoginDeniedEmail = sendLoginDeniedEmail;
	}


}
