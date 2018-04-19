/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.basesecurity;

import org.olat.NewControllerFactory;
import org.olat.admin.user.UserAdminContextEntryControllerCreator;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial Date: May 4, 2004
 * @author Mike Stock 
 * @author guido
 * Comment:
 */
@Service("baseSecurityModule")
public class BaseSecurityModule extends AbstractSpringModule {
	
	private static final OLog log = Tracing.createLoggerFor(BaseSecurityModule.class);

	private static final String USERSEARCH_ADMINPROPS_USERS = "userSearchAdminPropsForUsers";
	private static final String USERSEARCH_ADMINPROPS_AUTHORS = "userSearchAdminPropsForAuthors";
	private static final String USERSEARCH_ADMINPROPS_USERMANAGERS = "userSearchAdminPropsForUsermanagers";
	private static final String USERSEARCH_ADMINPROPS_GROUPMANAGERS = "userSearchAdminPropsForGroupmanagers";
	private static final String USERSEARCH_ADMINPROPS_ADMINISTRATORS = "userSearchAdminPropsForAdministrators";
	
	private static final String USER_LASTLOGIN_VISIBLE_USERS = "userLastLoginVisibleForUsers";
	private static final String USER_LASTLOGIN_VISIBLE_AUTHORS = "userLastLoginVisibleForAuthors";
	private static final String USER_LASTLOGIN_VISIBLE_USERMANAGERS = "userLastLoginVisibleForUsermanagers";
	private static final String USER_LASTLOGIN_VISIBLE_GROUPMANAGERS = "userLastLoginVisibleForGroupmanagers";
	private static final String USER_LASTLOGIN_VISIBLE_ADMINISTRATORS = "userLastLoginVisibleForAdministrators";

	private static final String USERSEARCHAUTOCOMPLETE_USERS = "userSearchAutocompleteForUsers";
	private static final String USERSEARCHAUTOCOMPLETE_AUTHORS = "userSearchAutocompleteForAuthors";
	private static final String USERSEARCHAUTOCOMPLETE_USERMANAGERS = "userSearchAutocompleteForUsermanagers";
	private static final String USERSEARCHAUTOCOMPLETE_GROUPMANAGERS = "userSearchAutocompleteForGroupmanagers";
	private static final String USERSEARCHAUTOCOMPLETE_ADMINISTRATORS = "userSearchAutocompleteForAdministrators";
	private static final String USERSEARCH_MAXRESULTS = "userSearchMaxResults";
	

	private static final String USERINFOS_TUNNEL_CBB = "userInfosTunnelCourseBuildingBlock";
	/** The feature is enabled, always */
	
	private static final String WIKI_ENABLED = "wiki";
	
	/**
	 * default values
	 */
	public static final Boolean USERMANAGER_CAN_CREATE_USER = true;
	public static final Boolean USERMANAGER_CAN_DELETE_USER = false;
	public static final Boolean USERMANAGER_CAN_CREATE_PWD = true;
	public static final Boolean USERMANAGER_CAN_MODIFY_PWD = true;
	public static final Boolean USERMANAGER_CAN_START_GROUPS = true;
	public static final Boolean USERMANAGER_CAN_MODIFY_SUBSCRIPTIONS = true;
	public static final Boolean USERMANAGER_ACCESS_TO_QUOTA = true;
	public static final Boolean USERMANAGER_ACCESS_TO_PROP = false;
	public static final Boolean USERMANAGER_ACCESS_TO_AUTH = false;
	public static final Boolean USERMANAGER_CAN_MANAGE_POOLMANAGERS = true;
	public static final Boolean USERMANAGER_CAN_MANAGE_GROUPMANAGERS = true;
	public static final Boolean USERMANAGER_CAN_MANAGE_CURRICULUMMANAGERS = true;
	public static final Boolean USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER = true;
	public static final Boolean USERMANAGER_CAN_MANAGE_AUTHORS = true;
	public static final Boolean USERMANAGER_CAN_MANAGE_GUESTS = false;
	public static final Boolean USERMANAGER_CAN_MANAGE_STATUS = true;
	public static final Boolean USERMANAGER_CAN_BYPASS_EMAILVERIFICATION = true;
	public static final Boolean USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS = true;
	
	private static String defaultAuthProviderIdentifier;

	@Value("${usersearch.adminProps.users:disabled}")
	private String userSearchAdminPropsForUsers;
	@Value("${usersearch.adminProps.authors:enabled}")
	private String userSearchAdminPropsForAuthors;
	@Value("${usersearch.adminProps.usermanagers:enabled}")
	private String userSearchAdminPropsForUsermanagers;
	@Value("${usersearch.adminProps.groupmanagers:enabled}")
	private String userSearchAdminPropsForGroupmanagers;
	@Value("${usersearch.adminProps.administrators:enabled}")
	private String userSearchAdminPropsForAdministrators;

	@Value("${user.lastlogin.visible.users:disabled}")
	private String userLastLoginVisibleForUsers;
	@Value("${user.lastlogin.visible.authors:enabled}")
	private String userLastLoginVisibleForAuthors;
	@Value("${user.lastlogin.visible.usermanagers:enabled}")
	private String userLastLoginVisibleForUsermanagers;
	@Value("${user.lastlogin.visible.groupmanagers:enabled}")
	private String userLastLoginVisibleForGroupmanagers;
	@Value("${user.lastlogin.visible.administrators:enabled}")
	private String userLastLoginVisibleForAdministrators;
	
	@Value("${usersearch.maxResults:-1}")
	private String userSearchMaxResults;
	@Value("${usersearch.autocomplete.users:enabled}")
	private String userSearchAutocompleteForUsers;
	@Value("${usersearch.autocomplete.authors:enabled}")
	private String userSearchAutocompleteForAuthors;
	@Value("${usersearch.autocomplete.usermanagers:enabled}")
	private String userSearchAutocompleteForUsermanagers;
	@Value("${usersearch.autocomplete.groupmanagers:enabled}")
	private String userSearchAutocompleteForGroupmanagers;
	@Value("${usersearch.autocomplete.administrators:enabled}")
	private String userSearchAutocompleteForAdministrators;
	
	@Value("${userinfos.tunnelcoursebuildingblock}")
	private String userInfosTunnelCourseBuildingBlock;
	
	@Value("${base.security.wiki:enabled}")
	private String wikiEnabled;
	
	@Autowired
	public BaseSecurityModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
		BaseSecurityModule.defaultAuthProviderIdentifier = "OLAT";
	}
	
	/**
	 * 
	 * @return the string which identifies the credentials on the database
	 */
	public static String getDefaultAuthProviderIdentifier() {
		return defaultAuthProviderIdentifier;
	}

	@Override
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator(User.class.getSimpleName(),
				new UserAdminContextEntryControllerCreator());
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabled = getStringPropertyValue(USERSEARCH_ADMINPROPS_USERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAdminPropsForUsers = enabled;
		}
		enabled = getStringPropertyValue(USERSEARCH_ADMINPROPS_AUTHORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAdminPropsForAuthors = enabled;
		}
		enabled = getStringPropertyValue(USERSEARCH_ADMINPROPS_USERMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAdminPropsForUsermanagers = enabled;
		}
		enabled = getStringPropertyValue(USERSEARCH_ADMINPROPS_GROUPMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAdminPropsForGroupmanagers = enabled;
		}
		enabled = getStringPropertyValue(USERSEARCH_ADMINPROPS_ADMINISTRATORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAdminPropsForAdministrators = enabled;
		}
		
		enabled = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_USERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userLastLoginVisibleForUsers = enabled;
		}
		enabled = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_AUTHORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userLastLoginVisibleForAuthors = enabled;
		}
		enabled = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_USERMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userLastLoginVisibleForUsermanagers = enabled;
		}
		enabled = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_GROUPMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userLastLoginVisibleForGroupmanagers = enabled;
		}
		enabled = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_ADMINISTRATORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userLastLoginVisibleForAdministrators = enabled;
		}

		enabled = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_USERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAutocompleteForUsers = enabled;
		}
		enabled = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_AUTHORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAutocompleteForAuthors = enabled;
		}
		enabled = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_USERMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAutocompleteForUsermanagers = enabled;
		}
		enabled = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_GROUPMANAGERS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAutocompleteForGroupmanagers = enabled;
		}
		enabled = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_ADMINISTRATORS, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userSearchAutocompleteForAdministrators = enabled;
		}
		
		String maxResults = getStringPropertyValue(USERSEARCH_MAXRESULTS, true);
		if(StringHelper.containsNonWhitespace(maxResults)) {
			userSearchMaxResults = maxResults;
		}
		
		enabled = getStringPropertyValue(USERINFOS_TUNNEL_CBB, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			userInfosTunnelCourseBuildingBlock = enabled;
		}

		enabled = getStringPropertyValue(WIKI_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			wikiEnabled = enabled;
		}
	}
	
	public boolean isUserAllowedAdminProps(Roles roles) {
		if(roles == null) return false;
		if(roles.isOLATAdmin()) {
			return "enabled".equals(userSearchAdminPropsForAdministrators);
		}
		if(roles.isGroupManager()) {
			return "enabled".equals(userSearchAdminPropsForGroupmanagers);
		}
		if(roles.isUserManager()) {
			return "enabled".equals(userSearchAdminPropsForUsermanagers);
		}
		if(roles.isAuthor()) {
			return "enabled".equals(userSearchAdminPropsForAuthors);
		}
		if(roles.isInvitee()) {
			return false;
		}
		return "enabled".equals(userSearchAdminPropsForUsers);
	}

	public String getUserSearchAdminPropsForUsers() {
		return userSearchAdminPropsForUsers;
	}

	public void setUserSearchAdminPropsForUsers(String enable) {
		setStringProperty(USERSEARCH_ADMINPROPS_USERS, enable, true);
	}

	public String getUserSearchAdminPropsForAuthors() {
		return userSearchAdminPropsForAuthors;
	}

	public void setUserSearchAdminPropsForAuthors(String enable) {
		setStringProperty(USERSEARCH_ADMINPROPS_AUTHORS, enable, true);
	}

	public String getUserSearchAdminPropsForUsermanagers() {
		return userSearchAdminPropsForUsermanagers;
	}

	public void setUserSearchAdminPropsForUsermanagers(String enable) {
		setStringProperty(USERSEARCH_ADMINPROPS_USERMANAGERS, enable, true);
	}

	public String getUserSearchAdminPropsForGroupmanagers() {
		return userSearchAdminPropsForGroupmanagers;
	}

	public void setUserSearchAdminPropsForGroupmanagers(String enable) {
		setStringProperty(USERSEARCH_ADMINPROPS_GROUPMANAGERS, enable, true);
	}

	public String getUserSearchAdminPropsForAdministrators() {
		return userSearchAdminPropsForAdministrators;
	}

	public void setUserSearchAdminPropsForAdministrators(String enable) {
		setStringProperty(USERSEARCH_ADMINPROPS_ADMINISTRATORS, enable, true);
	}
	
	public boolean isUserLastVisitVisible(Roles roles) {
		if(roles == null) return false;
		if(roles.isOLATAdmin()) {
			return "enabled".equals(userLastLoginVisibleForAdministrators);
		}
		if(roles.isGroupManager()) {
			return "enabled".equals(userLastLoginVisibleForGroupmanagers);
		}
		if(roles.isUserManager()) {
			return "enabled".equals(userLastLoginVisibleForUsermanagers);
		}
		if(roles.isAuthor()) {
			return "enabled".equals(userLastLoginVisibleForAuthors);
		}
		if(roles.isInvitee()) {
			return false;
		}
		return "enabled".equals(userLastLoginVisibleForUsers);
	}

	public String getUserLastLoginVisibleForUsers() {
		return userLastLoginVisibleForUsers;
	}

	public void setUserLastLoginVisibleForUsers(String enable) {
		setStringProperty(USER_LASTLOGIN_VISIBLE_USERS, enable, true);
	}

	public String getUserLastLoginVisibleForAuthors() {
		return userLastLoginVisibleForAuthors;
	}

	public void setUserLastLoginVisibleForAuthors(String enable) {
		setStringProperty(USER_LASTLOGIN_VISIBLE_AUTHORS, enable, true);
	}

	public String getUserLastLoginVisibleForUsermanagers() {
		return userLastLoginVisibleForUsermanagers;
	}

	public void setUserLastLoginVisibleForUsermanagers(String enable) {
		setStringProperty(USER_LASTLOGIN_VISIBLE_USERMANAGERS, enable, true);
	}

	public String getUserLastLoginVisibleForGroupmanagers() {
		return userLastLoginVisibleForGroupmanagers;
	}

	public void setUserLastLoginVisibleForGroupmanagers(String enable) {
		setStringProperty(USER_LASTLOGIN_VISIBLE_GROUPMANAGERS, enable, true);
	}

	public String getUserLastLoginVisibleForAdministrators() {
		return userLastLoginVisibleForAdministrators;
	}

	public void setUserLastLoginVisibleForAdministrators(String enable) {
		setStringProperty(USER_LASTLOGIN_VISIBLE_ADMINISTRATORS, enable, true);
	}

	public boolean isUserAllowedAutoComplete(Roles roles) {
		if(roles == null) return false;
		if(roles.isOLATAdmin()) {
			return "enabled".equals(userSearchAutocompleteForAdministrators);
		}
		if(roles.isGroupManager()) {
			return "enabled".equals(userSearchAutocompleteForGroupmanagers);
		}
		if(roles.isUserManager()) {
			return "enabled".equals(userSearchAutocompleteForUsermanagers);
		}
		if(roles.isAuthor()) {
			return "enabled".equals(userSearchAutocompleteForAuthors);
		}
		if(roles.isInvitee()) {
			return false;
		}
		return "enabled".equals(userSearchAutocompleteForUsers);
	}
	
	public String isUserSearchAutocompleteForUsers() {
		return userSearchAutocompleteForUsers;
	}

	public void setUserSearchAutocompleteForUsers(String enable) {
		setStringProperty(USERSEARCHAUTOCOMPLETE_USERS, enable, true);
	}

	public String isUserSearchAutocompleteForAuthors() {
		return userSearchAutocompleteForAuthors;
	}

	public void setUserSearchAutocompleteForAuthors(String enable) {
		setStringProperty(USERSEARCHAUTOCOMPLETE_AUTHORS, enable, true);
	}

	public String isUserSearchAutocompleteForUsermanagers() {
		return userSearchAutocompleteForUsermanagers;
	}

	public void setUserSearchAutocompleteForUsermanagers(String enable) {
		setStringProperty(USERSEARCHAUTOCOMPLETE_USERMANAGERS, enable, true);
	}

	public String isUserSearchAutocompleteForGroupmanagers() {
		return userSearchAutocompleteForGroupmanagers;
	}

	public void setUserSearchAutocompleteForGroupmanagers(String enable) {
		setStringProperty(USERSEARCHAUTOCOMPLETE_GROUPMANAGERS, enable, true);
	}

	public String isUserSearchAutocompleteForAdministrators() {
		return userSearchAutocompleteForAdministrators;
	}

	public void setUserSearchAutocompleteForAdministrators(String enable) {
		setStringProperty(USERSEARCHAUTOCOMPLETE_ADMINISTRATORS, enable, true);
	}
	
	public int getUserSearchMaxResultsValue() {
		if(StringHelper.containsNonWhitespace(userSearchMaxResults)) {
			try {
				return Integer.parseInt(userSearchMaxResults);
			} catch (NumberFormatException e) {
				log.error("userSearchMaxResults as the wrong format", e);
			}
		}
		return -1;
	}

	public String getUserSearchMaxResults() {
		return userSearchMaxResults;
	}

	public void setUserSearchMaxResults(String maxResults) {
		setStringProperty(USERSEARCH_MAXRESULTS, maxResults, true);
	}

	public String getUserInfosTunnelCourseBuildingBlock() {
		return userInfosTunnelCourseBuildingBlock;
	}

	public void setUserInfosTunnelCourseBuildingBlock(String enable) {
		setStringProperty(USERINFOS_TUNNEL_CBB, enable, true);
	}

	public boolean isWikiEnabled() {
		return "enabled".equals(wikiEnabled);
	}

	public void setWikiEnabled(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		wikiEnabled = enabled;
		setStringProperty(WIKI_ENABLED, enabled, true);
	}
}