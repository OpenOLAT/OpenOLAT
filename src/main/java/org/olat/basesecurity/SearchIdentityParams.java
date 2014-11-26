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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchIdentityParams {
	private String login;
	private Map<String, String> userProperties;
	private boolean userPropertiesAsIntersectionSearch;
	private SecurityGroup[] groups;
	private PermissionOnResourceable[] permissionOnResources;
	private String[] authProviders;
	private Date createdAfter;
	private Date createdBefore;
	private Date userLoginAfter;
	private Date userLoginBefore;
	private Integer status;
	private Collection<Long> identityKeys;
	private Boolean managed;
	
	public SearchIdentityParams() {
		//
	}
	
	public SearchIdentityParams(String login, Map<String, String> userproperties, boolean userPropertiesAsIntersectionSearch,
			SecurityGroup[] groups, PermissionOnResourceable[] permissionOnResources, String[] authProviders,
			Date createdAfter, Date createdBefore, Date userLoginAfter, Date userLoginBefore, Integer status) {
		this.login = login;
		this.userProperties = userproperties;
		this.userPropertiesAsIntersectionSearch = userPropertiesAsIntersectionSearch;
		this.groups = groups;
		this.permissionOnResources = permissionOnResources;
		this.authProviders = authProviders;
		this.createdAfter = createdAfter;
		this.createdBefore = createdBefore;
		this.userLoginAfter = userLoginAfter;
		this.userLoginBefore = userLoginBefore;
		this.status = status;
	}
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
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
	
	public SecurityGroup[] getGroups() {
		return groups;
	}
	
	public void setGroups(SecurityGroup[] groups) {
		this.groups = groups;
	}
	
	public PermissionOnResourceable[] getPermissionOnResources() {
		return permissionOnResources;
	}
	
	public void setPermissionOnResources(PermissionOnResourceable[] permissionOnResources) {
		this.permissionOnResources = permissionOnResources;
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
	
	public Collection<Long> getIdentityKeys() {
		return identityKeys;
	}
	
	public void setIdentityKeys(Collection<Long> identityKeys) {
		this.identityKeys = identityKeys;
	}
}
