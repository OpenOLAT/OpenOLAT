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
	private static final String CONFIG_USERMANAGER_ACCESS_TO_POLICIES = "sysGroupUsermanager.accessToPolicies";
	private static final String CONFIG_USERMANAGER_ACCESS_TO_AUTH = "sysGroupUsermanager.accessToAuthentications";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_GROUPMANAGERS = "sysGroupUsermanager.canManageGroupmanagers";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER = "sysGroupUsermanager.canManageInstitutionalResourceManager";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_AUTHORS = "sysGroupUsermanager.canManageAuthors";
	private static final String CONFIG_USERMANAGER_CAN_MANAGE_GUESTS = "sysGroupUsermanager.canManageGuests";
	private static final String CONFIG_USERMANAGER_CAN_BYPASS_EMAILVERIFICATION = "sysGroupUsermanager.canBypassEmailverification";
	private static final String CONFIG_USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS = "sysGroupUsermanager.canEditAllProfileFields";
	
	private static final String USERSEARCHAUTOCOMPLETE_USERS = "userSearchAutocompleteForUsers";
	private static final String USERSEARCHAUTOCOMPLETE_AUTHORS = "userSearchAutocompleteForAuthors";
	private static final String USERSEARCHAUTOCOMPLETE_USERMANAGERS = "userSearchAutocompleteForUsermanagers";
	private static final String USERSEARCHAUTOCOMPLETE_GROUPMANAGERS = "userSearchAutocompleteForUsermanagers";
	private static final String USERSEARCHAUTOCOMPLETE_ADMINISTRATORS = "userSearchAutocompleteForAdministrators";
	private static final String USERSEARCH_MAXRESULTS = "userSearchMaxResults";
	
	
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
	public static Boolean USERMANAGER_ACCESS_TO_POLICIES = false;
	public static Boolean USERMANAGER_ACCESS_TO_AUTH = false;
	public static Boolean USERMANAGER_CAN_MANAGE_GROUPMANAGERS = true;
	public static Boolean USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER = true;
	public static Boolean USERMANAGER_CAN_MANAGE_AUTHORS = true;
	public static Boolean USERMANAGER_CAN_MANAGE_GUESTS = false;
	public static Boolean USERMANAGER_CAN_BYPASS_EMAILVERIFICATION = true;
	public static Boolean USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS = true;
	private static String defaultAuthProviderIdentifier;

	private String userSearchMaxResults;
	private String userSearchAutocompleteForUsers;
	private String userSearchAutocompleteForAuthors;
	private String userSearchAutocompleteForUsermanagers;
	private String userSearchAutocompleteForGroupmanagers;
	private String userSearchAutocompleteForAdministrators;


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
		USERMANAGER_ACCESS_TO_POLICIES = getBooleanConfigParameter(CONFIG_USERMANAGER_ACCESS_TO_POLICIES, USERMANAGER_ACCESS_TO_POLICIES);
		USERMANAGER_ACCESS_TO_AUTH = getBooleanConfigParameter(CONFIG_USERMANAGER_ACCESS_TO_AUTH, USERMANAGER_ACCESS_TO_AUTH);
		
		USERMANAGER_CAN_MANAGE_GROUPMANAGERS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_GROUPMANAGERS, USERMANAGER_CAN_MANAGE_GROUPMANAGERS);
		USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER, USERMANAGER_CAN_MANAGE_INSTITUTIONAL_RESOURCE_MANAGER);
		USERMANAGER_CAN_MANAGE_AUTHORS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_AUTHORS, USERMANAGER_CAN_MANAGE_AUTHORS);
		USERMANAGER_CAN_MANAGE_GUESTS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MANAGE_GUESTS, USERMANAGER_CAN_MANAGE_GUESTS);
		
		USERMANAGER_CAN_BYPASS_EMAILVERIFICATION = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_BYPASS_EMAILVERIFICATION, USERMANAGER_CAN_BYPASS_EMAILVERIFICATION);
		USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS, USERMANAGER_CAN_EDIT_ALL_PROFILE_FIELDS);
		
		userSearchAutocompleteForUsers = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_USERS, "enable", true);
		userSearchAutocompleteForAuthors = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_AUTHORS, "enable", true);
		userSearchAutocompleteForUsermanagers = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_USERMANAGERS, "enable", true);
		userSearchAutocompleteForGroupmanagers = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_GROUPMANAGERS, "enable", true);
		userSearchAutocompleteForAdministrators = getStringConfigParameter(USERSEARCHAUTOCOMPLETE_ADMINISTRATORS, "enable", true);
		userSearchMaxResults = getStringConfigParameter(USERSEARCH_MAXRESULTS, "-1", true);
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabled = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_USERS, true);
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
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
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
}