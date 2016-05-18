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
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;

/**
 * Initial Date: May 4, 2004
 * @author Mike Stock 
 * @author guido
 * Comment:
 */
public class BaseSecurityModule extends AbstractOLATModule {
	
	private static final String CONFIG_USERMANAGER_CAN_CREATE_USER = "sysGroupUsermanager.canCreateUser";
	private static final String CONFIG_USERMANAGER_CAN_DELETE_USER = "sysGroupUsermanager.canDeleteUser";
	private static final String CONFIG_USERMANAGER_CAN_CREATE_PWD = "sysGroupUsermanager.canCreatePassword";
	private static final String CONFIG_USERMANAGER_CAN_MODIFY_PWD = "sysGroupUsermanager.canModifyPassword";
	private static final String CONFIG_USERMANAGER_CAN_START_GROUPS = "sysGroupUsermanager.canStartGroups";
	private static final String CONFIG_USERMANAGER_CAN_MODIFY_SUBSCRIPTIONS = "sysGroupUsermanager.canModifySubscriptions";
	private static final String CONFIG_USERMANAGER_ACCESS_TO_QUOTA = "sysGroupUsermanager.accessToQuota";
	private static final String CONFIG_USERMANAGER_ACCESS_TO_PROP = "sysGroupUsermanager.accessToProperties";
	private static final String CONFIG_USERMANAGER_ACCESS_TO_AUTH = "sysGroupUsermanager.accessToAuthentications";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_POOLMANAGERS = "sysGroupUsermanager.canManagePoolmanagers";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_GROUPMANAGERS = "sysGroupUsermanager.canManageGroupmanagers";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER = "sysGroupUsermanager.canManageInstitutionalResourceManager";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_AUTHORS = "sysGroupUsermanager.canManageAuthors";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_GUESTS = "sysGroupUsermanager.canManageGuests";
	private static final String CONFIG_USERMANAGER_CAN_BYPASS_EMAILVERIFICATION = "sysGroupUsermanager.canBypassEmailverification";
	private static final String CONFIG_USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS = "sysGroupUsermanager.canEditAllProfileFields";

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
	private static final String FORCE_TOP_FRAME = "forceTopFrame";
	private static final String WIKI_ENABLED = "wiki";

	/**
	 * default values
	 */
	public static Boolean USERMANAGER_CAN_CREATE_USER = false;
	public static Boolean USERMANAGER_CAN_DELETE_USER = true;
	public static Boolean USERMANAGER_CAN_CREATE_PWD = true;
	public static Boolean USERMANAGER_CAN_MODIFY_PWD = true;
	public static Boolean USERMANAGER_CAN_START_GROUPS = true;
	public static Boolean USERMANAGER_CAN_MODIFY_SUBSCRIPTIONS = true;
	public static Boolean USERMANAGER_ACCESS_TO_QUOTA = true;
	public static Boolean USERMANAGER_ACCESS_TO_PROP = false;
	public static Boolean USERMANAGER_ACCESS_TO_AUTH = false;
	public static Boolean USERMANAGER_CAN_MANAGE_POOLMANAGERS = true;
	public static Boolean USERMANAGER_CAN_MANAGE_GROUPMANAGERS = true;
	public static Boolean USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER = true;
	public static Boolean USERMANAGER_CAN_MANAGE_AUTHORS = true;
	public static Boolean USERMANAGER_CAN_MANAGE_GUESTS = false;
	public static Boolean USERMANAGER_CAN_BYPASS_EMAILVERIFICATION = true;
	public static Boolean USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS = true;
	private static String defaultAuthProviderIdentifier;

	private String userSearchAdminPropsForUsers;
	private String userSearchAdminPropsForAuthors;
	private String userSearchAdminPropsForUsermanagers;
	private String userSearchAdminPropsForGroupmanagers;
	private String userSearchAdminPropsForAdministrators;
	
	private String userLastLoginVisibleForUsers;
	private String userLastLoginVisibleForAuthors;
	private String userLastLoginVisibleForUsermanagers;
	private String userLastLoginVisibleForGroupmanagers;
	private String userLastLoginVisibleForAdministrators;
	
	private String userSearchMaxResults;
	private String userSearchAutocompleteForUsers;
	private String userSearchAutocompleteForAuthors;
	private String userSearchAutocompleteForUsermanagers;
	private String userSearchAutocompleteForGroupmanagers;
	private String userSearchAutocompleteForAdministrators;
	
	private String userInfosTunnelCourseBuildingBlock;
	
	private String forceTopFrame;
	private String wikiEnabled;


	private BaseSecurityModule(String defaultAuthProviderIdentifier) {
		BaseSecurityModule.defaultAuthProviderIdentifier = defaultAuthProviderIdentifier;
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
		// fxdiff: Add controller factory extension point to launch user admin site
		NewControllerFactory.getInstance().addContextEntryControllerCreator(User.class.getSimpleName(),
				new UserAdminContextEntryControllerCreator());
		updateProperties();
	}

	@Override
	protected void initDefaultProperties() {
		USERMANAGER_CAN_CREATE_USER = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_CREATE_USER, USERMANAGER_CAN_CREATE_USER);
		USERMANAGER_CAN_DELETE_USER = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_DELETE_USER, USERMANAGER_CAN_DELETE_USER);
		USERMANAGER_CAN_CREATE_PWD = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_CREATE_PWD, USERMANAGER_CAN_CREATE_PWD);
		USERMANAGER_CAN_MODIFY_PWD = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MODIFY_PWD, USERMANAGER_CAN_MODIFY_PWD);
		USERMANAGER_CAN_START_GROUPS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_START_GROUPS, USERMANAGER_CAN_START_GROUPS);
		USERMANAGER_CAN_MODIFY_SUBSCRIPTIONS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MODIFY_SUBSCRIPTIONS, USERMANAGER_CAN_MODIFY_SUBSCRIPTIONS);
		
		USERMANAGER_ACCESS_TO_QUOTA = getBooleanConfigParameter(CONFIG_USERMANAGER_ACCESS_TO_QUOTA, USERMANAGER_ACCESS_TO_QUOTA);
		USERMANAGER_ACCESS_TO_PROP = getBooleanConfigParameter(CONFIG_USERMANAGER_ACCESS_TO_PROP, USERMANAGER_ACCESS_TO_PROP);
		USERMANAGER_ACCESS_TO_AUTH = getBooleanConfigParameter(CONFIG_USERMANAGER_ACCESS_TO_AUTH, USERMANAGER_ACCESS_TO_AUTH);
		
		USERMANAGER_CAN_MANAGE_GROUPMANAGERS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_GROUPMANAGERS, USERMANAGER_CAN_MANAGE_GROUPMANAGERS);
		USERMANAGER_CAN_MANAGE_POOLMANAGERS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_POOLMANAGERS, USERMANAGER_CAN_MANAGE_POOLMANAGERS);
		USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER, USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER);
		USERMANAGER_CAN_MANAGE_AUTHORS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_AUTHORS, USERMANAGER_CAN_MANAGE_AUTHORS);
		USERMANAGER_CAN_MANAGE_GUESTS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_GUESTS, USERMANAGER_CAN_MANAGE_GUESTS);
		
		USERMANAGER_CAN_BYPASS_EMAILVERIFICATION = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_BYPASS_EMAILVERIFICATION, USERMANAGER_CAN_BYPASS_EMAILVERIFICATION);
		USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS, USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS);

		userSearchAdminPropsForUsers = getStringConfigParameter(USERSEARCH_ADMINPROPS_USERS, "disabled", true);
		userSearchAdminPropsForAuthors = getStringConfigParameter(USERSEARCH_ADMINPROPS_AUTHORS, "enabled", true);
		userSearchAdminPropsForUsermanagers = getStringConfigParameter(USERSEARCH_ADMINPROPS_USERMANAGERS, "enabled", true);
		userSearchAdminPropsForGroupmanagers = getStringConfigParameter(USERSEARCH_ADMINPROPS_GROUPMANAGERS, "enabled", true);
		userSearchAdminPropsForAdministrators = getStringConfigParameter(USERSEARCH_ADMINPROPS_ADMINISTRATORS, "enabled", true);

		userLastLoginVisibleForUsers = getStringConfigParameter(USER_LASTLOGIN_VISIBLE_USERS, "disabled", true);
		userLastLoginVisibleForAuthors = getStringConfigParameter(USER_LASTLOGIN_VISIBLE_AUTHORS, "enabled", true);
		userLastLoginVisibleForUsermanagers = getStringConfigParameter(USER_LASTLOGIN_VISIBLE_USERMANAGERS, "enabled", true);
		userLastLoginVisibleForGroupmanagers = getStringConfigParameter(USER_LASTLOGIN_VISIBLE_GROUPMANAGERS, "enabled", true);
		userLastLoginVisibleForAdministrators = getStringConfigParameter(USER_LASTLOGIN_VISIBLE_ADMINISTRATORS, "enabled", true);

		userSearchAutocompleteForUsers = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_USERS, "enabled", true);
		userSearchAutocompleteForAuthors = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_AUTHORS, "enabled", true);
		userSearchAutocompleteForUsermanagers = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_USERMANAGERS, "enabled", true);
		userSearchAutocompleteForGroupmanagers = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_GROUPMANAGERS, "enabled", true);
		userSearchAutocompleteForAdministrators = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_ADMINISTRATORS, "enabled", true);
		userSearchMaxResults = getStringConfigParameter(USERSEARCH_MAXRESULTS, "-1", true);

		userInfosTunnelCourseBuildingBlock = getStringConfigParameter(USERINFOS_TUNNEL_CBB, "disabled", true);

		forceTopFrame = getStringConfigParameter(FORCE_TOP_FRAME, "disabled", true);
		wikiEnabled = getStringConfigParameter(WIKI_ENABLED, "enabled", true);
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
		
		enabled = getStringPropertyValue(FORCE_TOP_FRAME, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			forceTopFrame = enabled;
		}
		enabled = getStringPropertyValue(WIKI_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabled)) {
			wikiEnabled = enabled;
		}
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
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
				logError("userSearchMaxResults as the wrong format", e);
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

	public boolean isForceTopFrame() {
		return true;//"enabled".equals(forceTopFrame);
	}

	public void setForceTopFrame(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		setStringProperty(FORCE_TOP_FRAME, enabled, true);
	}

	public boolean isWikiEnabled() {
		return "enabled".equals(wikiEnabled);
	}

	public void setWikiEnabled(boolean enable) {
		String enabled = enable ? "enabled" : "disabled";
		setStringProperty(WIKI_ENABLED, enabled, true);
	}
}