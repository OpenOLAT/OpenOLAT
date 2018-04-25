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

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchIdentityParams {
	private String login;
	private Map<String, String> userProperties;
	private boolean userPropertiesAsIntersectionSearch;
	
	private OrganisationRoles[] roles;
	private OrganisationRoles[] excludedRoles;
	private GroupRoles repositoryEntryRole;
	private GroupRoles businessGroupRole;
	private boolean authorAndCoAuthor;
	
	private String[] authProviders;
	private Date createdAfter;
	private Date createdBefore;
	private Date userLoginAfter;
	private Date userLoginBefore;
	private Integer status;
	private Collection<Long> identityKeys;
	private Boolean managed;
	
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
			OrganisationRoles[] roles, String[] authProviders,
			Date createdAfter, Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status) {
		this.login = login;
		this.userProperties = userproperties;
		this.userPropertiesAsIntersectionSearch = userPropertiesAsIntersectionSearch;
		this.roles = roles;
		this.authProviders = authProviders;
		this.createdAfter = createdAfter;
		this.createdBefore = createdBefore;
		this.userLoginAfter = userLoginAfter;
		this.userLoginBefore = userLoginBefore;
		this.status = status;
	}
	
	public static SearchIdentityParams params(Date createdAfter, Date createdBefore, Integer status) {
		return new SearchIdentityParams(null, null, true, null, null, createdAfter, createdBefore, null, null, status);
	}
	
	public static SearchIdentityParams params(OrganisationRoles[] roles, Integer status) {
		return new SearchIdentityParams(null, null, true, roles, null, null, null, null, null, status);
	}
	
	public static SearchIdentityParams authenticationProviders(String[] authProviders, Integer status) {
		return new SearchIdentityParams(null, null, true, null, authProviders, null, null, null, null, status);
	}
	
	public static SearchIdentityParams resources(GroupRoles repositoryEntryRole, GroupRoles businessGroupRole,
			OrganisationRoles[] roles, OrganisationRoles[] excludedRoles, Integer status) {
		SearchIdentityParams params = new SearchIdentityParams(null, null, true, null, null, null, null, null, null, status);
		params.setRepositoryEntryRole(repositoryEntryRole);
		params.setBusinessGroupRole(businessGroupRole);
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
	
	/**
	 * 
	 * @return A set of parameters to search authors along co-authors
	 */
	public static SearchIdentityParams authorsAndCoAuthors() {
		SearchIdentityParams params = new SearchIdentityParams(null, null, true, null, null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		params.setRepositoryEntryRole(GroupRoles.owner);
		params.setRoles(new OrganisationRoles[] { OrganisationRoles.author } );
		params.setAuthorAndCoAuthor(true);
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

	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
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

	public void setRepositoryEntryRole(GroupRoles repositoryEntryRole) {
		this.repositoryEntryRole = repositoryEntryRole;
	}

	public GroupRoles getBusinessGroupRole() {
		return businessGroupRole;
	}

	public void setBusinessGroupRole(GroupRoles businessGroupRole) {
		this.businessGroupRole = businessGroupRole;
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
	
	public boolean isAuthorAndCoAuthor() {
		return authorAndCoAuthor;
	}

	private void setAuthorAndCoAuthor(boolean authorAndCoAuthor) {
		this.authorAndCoAuthor = authorAndCoAuthor;
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
	
	public boolean hasIdentityKeys() {
		return identityKeys != null && !identityKeys.isEmpty();
	}
	
	public Collection<Long> getIdentityKeys() {
		return identityKeys;
	}
	
	public void setIdentityKeys(Collection<Long> identityKeys) {
		this.identityKeys = identityKeys;
	}
}
