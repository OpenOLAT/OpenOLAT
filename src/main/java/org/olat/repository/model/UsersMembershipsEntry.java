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
package org.olat.repository.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 1 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UsersMembershipsEntry extends UserPropertiesRow {
	
	private final Long repositoryEntryKey;
	private final String repositoryEntryDisplayname;
	private final String repositoryEntryInitialAuthor;
	private final String repositoryEntryExternalId;
	private final String repositoryEntryExternalRef;
	private final RepositoryEntryStatusEnum repositoryEntryStatus;
	private final Boolean repositoryEntryPublicVisible;
	private final Date lifecycleFrom;
	private final Date lifecycleTo;
	private final GroupRoles role;
	private final Date registrationDate;
	private final int identityStatus;
	private final Date identityLastLogin;
	private final Date identityCreationDate;
	private List<String> organisations;
	private List<String> taxonomyLevels;
	
	public UsersMembershipsEntry(Long identityKey, String externalId, List<UserPropertyHandler> userPropertyHandlers, String[] identityProps, Locale locale,
			int identityStatus, Date identityCreationDate, Date identityLastLogin,
			Long repositoryEntryKey, String repositoryEntryDisplayname, String repositoryEntryInitialAuthor,
			String repositoryEntryExternalId, String repositoryEntryExternalRef,
			RepositoryEntryStatusEnum repositoryEntryStatus, Boolean repositoryEntryPublicVisible, Date lifecycleFrom, Date lifecycleTo,
			GroupRoles role, Date registrationDate) {
		super(identityKey, externalId, userPropertyHandlers, identityProps, locale);
		this.repositoryEntryKey = repositoryEntryKey;
		this.repositoryEntryDisplayname = repositoryEntryDisplayname;
		this.repositoryEntryInitialAuthor = repositoryEntryInitialAuthor;
		this.repositoryEntryExternalId = repositoryEntryExternalId;
		this.repositoryEntryExternalRef = repositoryEntryExternalRef;
		this.repositoryEntryStatus = repositoryEntryStatus;
		this.repositoryEntryPublicVisible = repositoryEntryPublicVisible;
		this.lifecycleFrom = lifecycleFrom;
		this.lifecycleTo = lifecycleTo;
		this.role = role;
		this.registrationDate = registrationDate;
		this.identityStatus = identityStatus;
		this.identityLastLogin = identityLastLogin;
		this.identityCreationDate = identityCreationDate;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public String getRepositoryEntryDisplayname() {
		return repositoryEntryDisplayname;
	}

	public String getRepositoryEntryInitialAuthor() {
		return repositoryEntryInitialAuthor;
	}

	public String getRepositoryEntryExternalId() {
		return repositoryEntryExternalId;
	}

	public String getRepositoryEntryExternalRef() {
		return repositoryEntryExternalRef;
	}

	public RepositoryEntryStatusEnum getRepositoryEntryStatus() {
		return repositoryEntryStatus;
	}

	public Boolean getRepositoryEntryPublicVisible() {
		return repositoryEntryPublicVisible;
	}

	public Date getLifecycleFrom() {
		return lifecycleFrom;
	}

	public Date getLifecycleTo() {
		return lifecycleTo;
	}

	public GroupRoles getRole() {
		return role;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public int getIdentityStatus() {
		return identityStatus;
	}

	public Date getIdentityLastLogin() {
		return identityLastLogin;
	}

	public Date getIdentityCreationDate() {
		return identityCreationDate;
	}

	public List<String> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<String> organisations) {
		this.organisations = organisations;
	}

	public List<String> getTaxonomyLevels() {
		return taxonomyLevels;
	}

	public void setTaxonomyLevels(List<String> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}
}
