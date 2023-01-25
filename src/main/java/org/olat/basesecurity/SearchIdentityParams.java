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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchIdentityParams {
	private String idAndExternalIds;
	private String externalId;
	private String login;
	private String searchString;
	private Map<String, String> userProperties;
	private boolean userPropertiesAsIntersectionSearch;
	
	private OrganisationRoles[] roles;
	private OrganisationRoles[] excludedRoles;
	private GroupMembershipInheritance[] roleInheritence;
	private GroupRoles repositoryEntryRole;
	private boolean repositoryEntryRoleInDefaultOnly;
	private GroupRoles businessGroupRole;
	private CurriculumRoles curriculumRole;
	
	private String[] authProviders;
	private Date createdAfter;
	private Date createdBefore;
	private Date userLoginAfter;
	private Date userLoginBefore;
	private Integer status;
	private List<Integer> exactStatusList;
	private List<Integer> excludeStatusList;
	private Collection<Long> identityKeys;
	private Boolean managed;
	private boolean withoutBusinessGroup;
	private boolean withoutResources;
	private boolean withoutEfficiencyStatements;
	private boolean withoutOrganisation;
	
	private List<Organisation> organisationParents;
	private List<OrganisationRef> organisations;
	
	public SearchIdentityParams() {
		//
	}
	
	public SearchIdentityParams(OrganisationRoles[] roles, Integer status) {
		this.roles = roles;
		this.status = status;
	}
	
	public SearchIdentityParams(String login, Map<String, String> userproperties, boolean userPropertiesAsIntersectionSearch,
			OrganisationRoles[] roles, GroupMembershipInheritance[] roleInheritence, String[] authProviders,
			Date createdAfter, Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status) {
		setLogin(login);
		this.userProperties = userproperties;
		this.userPropertiesAsIntersectionSearch = userPropertiesAsIntersectionSearch;
		this.roles = roles;
		this.roleInheritence = roleInheritence;
		this.authProviders = authProviders;
		this.createdAfter = createdAfter;
		this.createdBefore = createdBefore;
		this.userLoginAfter = userLoginAfter;
		this.userLoginBefore = userLoginBefore;
		this.status = status;
	}
	
	public static SearchIdentityParams created(Date createdAfter, Date createdBefore, Integer status) {
		return new SearchIdentityParams(null, null, true, null, null, null, createdAfter, createdBefore, null, null, status);
	}
	
	public static SearchIdentityParams roles(OrganisationRoles[] roles, GroupMembershipInheritance[] roleInheritence, Integer status) {
		return new SearchIdentityParams(null, null, true, roles, roleInheritence, null, null, null, null, null, status);
	}
	
	public static SearchIdentityParams authenticationProviders(String[] authProviders, Integer status) {
		return new SearchIdentityParams(null, null, true, null, null, authProviders, null, null, null, null, status);
	}
	
	public static SearchIdentityParams resources(GroupRoles repositoryEntryRole, boolean defOnly,
			GroupRoles businessGroupRole, CurriculumRoles curriculumRole,
			OrganisationRoles[] roles, OrganisationRoles[] excludedRoles, Integer status) {
		SearchIdentityParams params = new SearchIdentityParams(null, null, true, null, null, null, null, null, null, null, status);
		params.setRepositoryEntryRole(repositoryEntryRole, defOnly);
		params.setBusinessGroupRole(businessGroupRole);
		params.setCurriculumRole(curriculumRole);
		params.setRoles(roles);
		params.setExcludedRoles(excludedRoles);
		return params;
	}
	
	public static SearchIdentityParams organisation(Organisation organisation, Integer status) {
		SearchIdentityParams params = new SearchIdentityParams();
		params.setOrganisations(Collections.singletonList(organisation));
		params.setStatus(status);
		return params;
	}
	
	public static SearchIdentityParams withoutOrganisation() {
		SearchIdentityParams params = new SearchIdentityParams();
		params.setWithoutOrganisation(true);
		params.setExcludeStatusList(List.of(Identity.STATUS_DELETED));
		return params;
	}
	
	public static SearchIdentityParams withBusinesGroups() {
		SearchIdentityParams params = new SearchIdentityParams();
		params.setWithoutBusinessGroup(true);
		params.setStatus(Identity.STATUS_VISIBLE_LIMIT);
		return params;
	}
	
	public boolean hasOrganisationParents() {
		return organisationParents != null && !organisationParents.isEmpty();
	}
	
	public List<Organisation> getOrganisationParents() {
		return organisationParents;
	}

	public void setOrganisationParents(List<Organisation> organisationParents) {
		this.organisationParents = organisationParents;
	}
	
	public boolean hasOrganisations() {
		return organisations != null && !organisations.isEmpty();
	}

	public List<OrganisationRef> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(List<? extends OrganisationRef> organisations) {
		if(organisations == null) {
			this.organisations = null;
		} else {
			this.organisations = new ArrayList<>(organisations);
		}
	}

	public boolean isWithoutOrganisation() {
		return withoutOrganisation;
	}

	/**
	 * This parameter overwrite roles and organisations parameters.
	 * 
	 * @param withoutOrganisation true if you want the list of user without a user memberhsip to at least one organisation
	 */
	public void setWithoutOrganisation(boolean withoutOrganisation) {
		this.withoutOrganisation = withoutOrganisation;
	}

	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		if(StringHelper.containsNonWhitespace(login)) {
			this.login = login;
		} else {
			this.login = null;
		}
	}
	
	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		if(StringHelper.containsNonWhitespace(searchString)) {
			this.searchString = searchString;
		} else {
			this.searchString = null;
		}
	}

	public boolean hasUserProperties() {
		return userProperties != null && !userProperties.isEmpty();  
	}

	public Map<String, String> getUserProperties() {
		return userProperties;
	}
	
	public void setUserProperties(Map<String, String> userProperties) {
		this.userProperties = userProperties;
	}
	
	public boolean isUserPropertiesAsIntersectionSearch() {
		return userPropertiesAsIntersectionSearch;
	}
	
	public void setUserPropertiesAsIntersectionSearch(boolean userPropertiesAsIntersectionSearch) {
		this.userPropertiesAsIntersectionSearch = userPropertiesAsIntersectionSearch;
	}
	
	public boolean hasRoles() {
		return roles != null && roles.length > 0;
	}
	
	public OrganisationRoles[] getRoles() {
		return roles;
	}

	public void setRoles(OrganisationRoles[] roles) {
		this.roles = roles;
	}
	
	public boolean hasRoleInheritence() {
		return roleInheritence != null && roleInheritence.length > 0;
	}
	
	public GroupMembershipInheritance[] getRoleInheritence() {
		return roleInheritence;
	}

	public void setRoleInheritence(GroupMembershipInheritance[] roleInheritence) {
		this.roleInheritence = roleInheritence;
	}

	public boolean hasExcludedRoles() {
		return excludedRoles != null && excludedRoles.length > 0;
	}
	
	public OrganisationRoles[] getExcludedRoles() {
		return excludedRoles;
	}

	public void setExcludedRoles(OrganisationRoles[] excludedRoles) {
		this.excludedRoles = excludedRoles;
	}

	public GroupRoles getRepositoryEntryRole() {
		return repositoryEntryRole;
	}

	public void setRepositoryEntryRole(GroupRoles repositoryEntryRole, boolean defaultOnly) {
		this.repositoryEntryRole = repositoryEntryRole;
		this.repositoryEntryRoleInDefaultOnly = defaultOnly;
	}

	public GroupRoles getBusinessGroupRole() {
		return businessGroupRole;
	}

	public void setBusinessGroupRole(GroupRoles businessGroupRole) {
		this.businessGroupRole = businessGroupRole;
	}

	public boolean isRepositoryEntryRoleInDefaultOnly() {
		return repositoryEntryRoleInDefaultOnly;
	}

	public void setRepositoryEntryRoleInDefaultOnly(boolean repositoryEntryRoleInDefaultOnly) {
		this.repositoryEntryRoleInDefaultOnly = repositoryEntryRoleInDefaultOnly;
	}

	public CurriculumRoles getCurriculumRole() {
		return curriculumRole;
	}

	public void setCurriculumRole(CurriculumRoles curriculumRole) {
		this.curriculumRole = curriculumRole;
	}

	public boolean hasAuthProviders() {
		return authProviders != null && authProviders.length > 0;
	}
	
	public String[] getAuthProviders() {
		return authProviders;
	}
	
	public void setAuthProviders(String[] authProviders) {
		this.authProviders = authProviders;
	}

	public Boolean getManaged() {
		return managed;
	}

	public void setManaged(Boolean managed) {
		this.managed = managed;
	}

	public Date getCreatedAfter() {
		return createdAfter;
	}
	
	public void setCreatedAfter(Date createdAfter) {
		this.createdAfter = createdAfter;
	}
	
	public Date getCreatedBefore() {
		return createdBefore;
	}
	
	public void setCreatedBefore(Date createdBefore) {
		this.createdBefore = createdBefore;
	}
	
	public Date getUserLoginAfter() {
		return userLoginAfter;
	}
	
	public void setUserLoginAfter(Date userLoginAfter) {
		this.userLoginAfter = userLoginAfter;
	}
	
	public Date getUserLoginBefore() {
		return userLoginBefore;
	}
	
	public void setUserLoginBefore(Date userLoginBefore) {
		this.userLoginBefore = userLoginBefore;
	}
	
	public Integer getStatus() {
		return status;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public String getIdAndExternalIds() {
		return idAndExternalIds;
	}

	public void setIdAndExternalIds(String idAndExternalIds) {
		this.idAndExternalIds = idAndExternalIds;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public List<Integer> getExactStatusList() {
		return exactStatusList;
	}

	/**
	 * These status will override the one set with setStatus()
	 * 
	 * @param statusList A list of status
	 */
	public void setExactStatusList(List<Integer> statusList) {
		this.exactStatusList = statusList;
	}

	public List<Integer> getExcludeStatusList() {
		return excludeStatusList;
	}

	public void setExcludeStatusList(List<Integer> excludeStatusList) {
		this.excludeStatusList = excludeStatusList;
	}

	public boolean hasIdentityKeys() {
		return identityKeys != null && !identityKeys.isEmpty();
	}
	
	public Collection<Long> getIdentityKeys() {
		return identityKeys;
	}
	
	public void setIdentityKeys(Collection<Long> identityKeys) {
		this.identityKeys = identityKeys;
	}

	public boolean isWithoutBusinessGroup() {
		return withoutBusinessGroup;
	}

	public void setWithoutBusinessGroup(boolean withoutBusinessGroup) {
		this.withoutBusinessGroup = withoutBusinessGroup;
	}

	public boolean isWithoutResources() {
		return withoutResources;
	}

	public void setWithoutResources(boolean withoutResources) {
		this.withoutResources = withoutResources;
	}

	public boolean isWithoutEfficiencyStatements() {
		return withoutEfficiencyStatements;
	}

	public void setWithoutEfficiencyStatements(boolean withoutEfficiencyStatements) {
		this.withoutEfficiencyStatements = withoutEfficiencyStatements;
	}
}
