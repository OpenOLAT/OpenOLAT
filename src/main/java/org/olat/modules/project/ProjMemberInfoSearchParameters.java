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
package org.olat.modules.project;

import java.util.List;
import java.util.Map;

import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMemberInfoSearchParameters {
	
	private ProjProject project;
	private String login;
	private List<ProjectRole> roles;
	private String searchString;
	private List<UserPropertyHandler> userProperties;
	private Map<String, String> userPropertiesSearch;
	
	public ProjProject getProject() {
		return project;
	}

	public void setProject(ProjProject project) {
		this.project = project;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
	public List<UserPropertyHandler> getUserProperties() {
		return userProperties;
	}

	public void setUserProperties(List<UserPropertyHandler> userProperties) {
		this.userProperties = userProperties;
	}

	public List<ProjectRole> getRoles() {
		return roles;
	}

	public void setRoles(List<ProjectRole> roles) {
		this.roles = roles;
	}

	public Map<String, String> getUserPropertiesSearch() {
		return userPropertiesSearch;
	}

	public void setUserPropertiesSearch(Map<String, String> userPropertiesSearch) {
		this.userPropertiesSearch = userPropertiesSearch;
	}
	
}
