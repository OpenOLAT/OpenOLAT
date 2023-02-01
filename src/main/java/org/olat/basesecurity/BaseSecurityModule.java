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

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.admin.user.UserAdminContextEntryControllerCreator;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
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
	
	private static final Logger log = Tracing.createLoggerFor(BaseSecurityModule.class);
	
	private static final OrganisationRoles[] privacyRoles = new OrganisationRoles[]{
			OrganisationRoles.user, OrganisationRoles.author,
			OrganisationRoles.usermanager, OrganisationRoles.rolesmanager,
			OrganisationRoles.groupmanager, OrganisationRoles.learnresourcemanager,
			OrganisationRoles.poolmanager, OrganisationRoles.curriculummanager,
			OrganisationRoles.lecturemanager, OrganisationRoles.projectmanager, OrganisationRoles.qualitymanager,
			OrganisationRoles.linemanager, OrganisationRoles.principal,
			OrganisationRoles.administrator, OrganisationRoles.sysadmin
	};

	private static final String USERSEARCH_ADMINPROPS_USERS = "userSearchAdminPropsForUsers";
	private static final String USERSEARCH_ADMINPROPS_AUTHORS = "userSearchAdminPropsForAuthors";
	private static final String USERSEARCH_ADMINPROPS_USERMANAGERS = "userSearchAdminPropsForUsermanagers";
	private static final String USERSEARCH_ADMINPROPS_ROLESMANAGERS = "userSearchAdminPropsForRolesmanagers";
	private static final String USERSEARCH_ADMINPROPS_GROUPMANAGERS = "userSearchAdminPropsForGroupmanagers";
	private static final String USERSEARCH_ADMINPROPS_LEARNRESOURCEMANAGERS = "userSearchAdminPropsForLearnresourcemanagers";
	private static final String USERSEARCH_ADMINPROPS_POOLMANAGERS = "userSearchAdminPropsForPoolmanagers";
	private static final String USERSEARCH_ADMINPROPS_CURRICULUMMANAGERS = "userSearchAdminPropsForCurriculummanagers";
	private static final String USERSEARCH_ADMINPROPS_LECTUREMANAGERS = "userSearchAdminPropsForLecturemanagers";
	private static final String USERSEARCH_ADMINPROPS_PROJECTMANAGERS = "userSearchAdminPropsForProjectmanagers";
	private static final String USERSEARCH_ADMINPROPS_QUALITYMANAGERS = "userSearchAdminPropsForQualitymanagers";
	private static final String USERSEARCH_ADMINPROPS_LINEMANAGERS = "userSearchAdminPropsForLinemanagers";
	private static final String USERSEARCH_ADMINPROPS_PRINCIPALS = "userSearchAdminPropsForPrincipals";
	private static final String USERSEARCH_ADMINPROPS_ADMINISTRATORS = "userSearchAdminPropsForAdministrators";
	private static final String USERSEARCH_ADMINPROPS_SYSTEMADMINS = "userSearchAdminPropsForSystemAdmins";
	
	private static final String USER_LASTLOGIN_VISIBLE_USERS = "userLastLoginVisibleForUsers";
	private static final String USER_LASTLOGIN_VISIBLE_AUTHORS = "userLastLoginVisibleForAuthors";
	private static final String USER_LASTLOGIN_VISIBLE_USERMANAGERS = "userLastLoginVisibleForUsermanagers";
	private static final String USER_LASTLOGIN_VISIBLE_ROLESMANAGERS = "userLastLoginVisibleForRolesmanagers";
	private static final String USER_LASTLOGIN_VISIBLE_GROUPMANAGERS = "userLastLoginVisibleForGroupmanagers";
	private static final String USER_LASTLOGIN_VISIBLE_LEARNRESOURCEMANAGERS = "userLastLoginVisibleForLearnresourcemanagers";
	private static final String USER_LASTLOGIN_VISIBLE_POOLMANAGERS = "userLastLoginVisibleForPoolmanagers";
	private static final String USER_LASTLOGIN_VISIBLE_CURRICULUMMANAGERS = "userLastLoginVisibleForCurriculummanagers";
	private static final String USER_LASTLOGIN_VISIBLE_LECTUREMANAGERS = "userLastLoginVisibleForLecturemanagers";
	private static final String USER_LASTLOGIN_VISIBLE_PROJECTMANAGERS = "userLastLoginVisibleForProjectmanagers";
	private static final String USER_LASTLOGIN_VISIBLE_QUALITYMANAGERS = "userLastLoginVisibleForQualitymanagers";
	private static final String USER_LASTLOGIN_VISIBLE_LINEMANAGERS = "userLastLoginVisibleForLinemanagers";
	private static final String USER_LASTLOGIN_VISIBLE_PRINCIPALS = "userLastLoginVisibleForPrincipals";
	private static final String USER_LASTLOGIN_VISIBLE_ADMINISTRATORS = "userLastLoginVisibleForAdministrators";
	private static final String USER_LASTLOGIN_VISIBLE_SYSTEMADMINS = "userSearchAdminPropsForSystemAdmins";

	private static final String USERSEARCHAUTOCOMPLETE_USERS = "userSearchAutocompleteForUsers";
	private static final String USERSEARCHAUTOCOMPLETE_AUTHORS = "userSearchAutocompleteForAuthors";
	private static final String USERSEARCHAUTOCOMPLETE_USERMANAGERS = "userSearchAutocompleteForUsermanagers";
	private static final String USERSEARCHAUTOCOMPLETE_ROLESMANAGERS = "userSearchAutocompleteForRolesmanagers";
	private static final String USERSEARCHAUTOCOMPLETE_GROUPMANAGERS = "userSearchAutocompleteForGroupmanagers";
	private static final String USERSEARCHAUTOCOMPLETE_LEARNRESOURCEMANAGERS = "userSearchAutocompleteForLearnresourcemanagers";
	private static final String USERSEARCHAUTOCOMPLETE_POOLMANAGERS = "userSearchAutocompleteForPoolmanagers";
	private static final String USERSEARCHAUTOCOMPLETE_CURRICULUMMANAGERS = "userSearchAutocompleteForCurriculummanagers";
	private static final String USERSEARCHAUTOCOMPLETE_LECTUREMANAGERS = "userSearchAutocompleteForLecturemanagers";
	private static final String USERSEARCHAUTOCOMPLETE_PROJECTMANAGERS = "userSearchAutocompleteForProjectmanagers";
	private static final String USERSEARCHAUTOCOMPLETE_QUALITYMANAGERS = "userSearchAutocompleteForQualitymanagers";
	private static final String USERSEARCHAUTOCOMPLETE_LINEMANAGERS = "userSearchAutocompleteForLinemanagers";
	private static final String USERSEARCHAUTOCOMPLETE_PRINCIPALS = "userSearchAutocompleteForPrincipals";
	private static final String USERSEARCHAUTOCOMPLETE_ADMINISTRATORS = "userSearchAutocompleteForAdministrators";
	private static final String USERSEARCHAUTOCOMPLETE_SYSTEMADMINS = "userSearchAdminPropsForSystemAdmins";
	private static final String USERSEARCH_MAXRESULTS = "userSearchMaxResults";
	
	private static final String RELATION_ROLE_MANAGED = "relationRoleManaged";
	private static final String RELATION_ROLE_ENABLED = "relationRoleEnabled";

	private static final String USERINFOS_TUNNEL_CBB = "userInfosTunnelCourseBuildingBlock";
	/** The feature is enabled, always */
	
	private static final String WIKI_ENABLED = "wiki";
	
	private static final String IDENTITY_NAME = "identity.name";
	
	/**
	 * default values
	 */
	public static final Boolean USERMANAGER_CAN_DELETE_USER = false;// fx -> true
	public static final Boolean USERMANAGER_CAN_MODIFY_SUBSCRIPTIONS = true;
	public static final Boolean USERMANAGER_CAN_MANAGE_POOLMANAGERS = true;
	public static final Boolean USERMANAGER_CAN_MANAGE_CURRICULUMMANAGERS = true;
	public static final Boolean USERMANAGER_CAN_MANAGE_STATUS = true;
	
	private static String defaultAuthProviderIdentifier;

	@Value("${usersearch.adminProps.users:disabled}")
	private String userSearchAdminPropsForUsers;
	@Value("${usersearch.adminProps.authors:enabled}")
	private String userSearchAdminPropsForAuthors;
	@Value("${usersearch.adminProps.usermanagers:enabled}")
	private String userSearchAdminPropsForUsermanagers;
	@Value("${usersearch.adminProps.rolesmanagers:enabled}")
	private String userSearchAdminPropsForRolesmanagers;
	@Value("${usersearch.adminProps.groupmanagers:enabled}")
	private String userSearchAdminPropsForGroupmanagers;
	@Value("${usersearch.adminProps.learnresourcemanagers:enabled}")
	private String userSearchAdminPropsForLearnresourcemanagers;
	@Value("${usersearch.adminProps.poolmanagers:enabled}")
	private String userSearchAdminPropsForPoolmanagers;
	@Value("${usersearch.adminProps.curriculummanagers:enabled}")
	private String userSearchAdminPropsForCurriculummanagers;
	@Value("${usersearch.adminProps.lecturemanagers:enabled}")
	private String userSearchAdminPropsForLecturemanagers;
	@Value("${usersearch.adminProps.projectmanagers:enabled}")
	private String userSearchAdminPropsForProjectmanagers;
	@Value("${usersearch.adminProps.qualitymanagers:enabled}")
	private String userSearchAdminPropsForQualitymanagers;
	@Value("${usersearch.adminProps.linemanagers:enabled}")
	private String userSearchAdminPropsForLinemanagers;
	@Value("${usersearch.adminProps.principals:enabled}")
	private String userSearchAdminPropsForPrincipals;
	@Value("${usersearch.adminProps.administrators:enabled}")
	private String userSearchAdminPropsForAdministrators;
	@Value("${usersearch.adminProps.systemadmins:enabled}")
	private String userSearchAdminPropsForSystemAdmins;

	@Value("${user.lastlogin.visible.users:disabled}")
	private String userLastLoginVisibleForUsers;
	@Value("${user.lastlogin.visible.authors:enabled}")
	private String userLastLoginVisibleForAuthors;
	@Value("${user.lastlogin.visible.usermanagers:enabled}")
	private String userLastLoginVisibleForUsermanagers;
	@Value("${user.lastlogin.visible.rolesmanagers:enabled}")
	private String userLastLoginVisibleForRolesmanagers;
	@Value("${user.lastlogin.visible.groupmanagers:enabled}")
	private String userLastLoginVisibleForGroupmanagers;
	@Value("${user.lastlogin.visible.learnresourcemanagers:enabled}")
	private String userLastLoginVisibleForLearnresourcemanagers;
	@Value("${user.lastlogin.visible.poolmanagers:enabled}")
	private String userLastLoginVisibleForPoolmanagers;
	@Value("${user.lastlogin.visible.curriculummanagers:enabled}")
	private String userLastLoginVisibleForCurriculummanagers;
	@Value("${user.lastlogin.visible.lecturemanagers:enabled}")
	private String userLastLoginVisibleForLecturemanagers;
	@Value("${user.lastlogin.visible.projectmanagers:enabled}")
	private String userLastLoginVisibleForProjectmanagers;
	@Value("${user.lastlogin.visible.qualitymanagers:enabled}")
	private String userLastLoginVisibleForQualitymanagers;
	@Value("${user.lastlogin.visible.linemanagers:enabled}")
	private String userLastLoginVisibleForLinemanagers;
	@Value("${user.lastlogin.visible.principals:enabled}")
	private String userLastLoginVisibleForPrincipals;
	@Value("${user.lastlogin.visible.administrators:enabled}")
	private String userLastLoginVisibleForAdministrators;
	@Value("${user.lastlogin.visible.systemadmins:enabled}")
	private String userLastLoginVisibleForSystemAdmins;
	
	
	@Value("${usersearch.maxResults:-1}")
	private String userSearchMaxResults;
	@Value("${usersearch.autocomplete.users:enabled}")
	private String userSearchAutocompleteForUsers;
	@Value("${usersearch.autocomplete.authors:enabled}")
	private String userSearchAutocompleteForAuthors;
	@Value("${usersearch.autocomplete.usermanagers:enabled}")
	private String userSearchAutocompleteForUsermanagers;
	@Value("${usersearch.autocomplete.rolesmanagers:enabled}")
	private String userSearchAutocompleteForRolesmanagers;
	@Value("${usersearch.autocomplete.groupmanagers:enabled}")
	private String userSearchAutocompleteForGroupmanagers;
	@Value("${usersearch.autocomplete.learnresourcemanagers:enabled}")
	private String userSearchAutocompleteForLearnresourcemanagers;
	@Value("${usersearch.autocomplete.poolmanagers:enabled}")
	private String userSearchAutocompleteForPoolmanagers;
	@Value("${usersearch.autocomplete.curriculummanagers:enabled}")
	private String userSearchAutocompleteForCurriculummanagers;
	@Value("${usersearch.autocomplete.lecturemanagers:enabled}")
	private String userSearchAutocompleteForLecturemanagers;
	@Value("${usersearch.autocomplete.projectmanagers:enabled}")
	private String userSearchAutocompleteForProjectmanagers;
	@Value("${usersearch.autocomplete.qualitymanagers:enabled}")
	private String userSearchAutocompleteForQualitymanagers;
	@Value("${usersearch.autocomplete.linemanagers:enabled}")
	private String userSearchAutocompleteForLinemanagers;
	@Value("${usersearch.autocomplete.principals:enabled}")
	private String userSearchAutocompleteForPrincipals;
	@Value("${usersearch.autocomplete.administrators:enabled}")
	private String userSearchAutocompleteForAdministrators;
	@Value("${usersearch.autocomplete.systemadmins:enabled}")
	private String userSearchAutocompleteForSystemAdmins;
	
	@Value("${relation.role.enabled:enabled}")
	private String relationRoleEnabled;
	@Value("${managed.relation.role:enabled}")
	private String relationRoleManaged;
	
	@Value("${userinfos.tunnelcoursebuildingblock}")
	private String userInfosTunnelCourseBuildingBlock;
	
	@Value("${base.security.wiki:enabled}")
	private String wikiEnabled;
	
	@Value("${identity.name:auto}")
	private String identityName;
	
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
		// view admin. properties
		userSearchAdminPropsForUsers = getStringPropertyValue(USERSEARCH_ADMINPROPS_USERS, userSearchAdminPropsForUsers);
		userSearchAdminPropsForAuthors = getStringPropertyValue(USERSEARCH_ADMINPROPS_AUTHORS, userSearchAdminPropsForAuthors);
		userSearchAdminPropsForUsermanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_USERMANAGERS, userSearchAdminPropsForUsermanagers);
		userSearchAdminPropsForRolesmanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_ROLESMANAGERS, userSearchAdminPropsForRolesmanagers);
		userSearchAdminPropsForGroupmanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_GROUPMANAGERS, userSearchAdminPropsForGroupmanagers);
		userSearchAdminPropsForLearnresourcemanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_LEARNRESOURCEMANAGERS, userSearchAdminPropsForLearnresourcemanagers);
		userSearchAdminPropsForPoolmanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_POOLMANAGERS, userSearchAdminPropsForPoolmanagers);
		userSearchAdminPropsForCurriculummanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_CURRICULUMMANAGERS, userSearchAdminPropsForCurriculummanagers);
		userSearchAdminPropsForLecturemanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_LECTUREMANAGERS, userSearchAdminPropsForLecturemanagers);
		userSearchAdminPropsForQualitymanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_QUALITYMANAGERS, userSearchAdminPropsForQualitymanagers);
		userSearchAdminPropsForLinemanagers = getStringPropertyValue(USERSEARCH_ADMINPROPS_LINEMANAGERS, userSearchAdminPropsForLinemanagers);
		userSearchAdminPropsForPrincipals = getStringPropertyValue(USERSEARCH_ADMINPROPS_PRINCIPALS, userSearchAdminPropsForPrincipals);
		userSearchAdminPropsForAdministrators = getStringPropertyValue(USERSEARCH_ADMINPROPS_ADMINISTRATORS, userSearchAdminPropsForAdministrators);
		userSearchAdminPropsForSystemAdmins = getStringPropertyValue(USERSEARCH_ADMINPROPS_SYSTEMADMINS, userSearchAdminPropsForSystemAdmins);

		// view last login
		userLastLoginVisibleForUsers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_USERS, userLastLoginVisibleForUsers);
		userLastLoginVisibleForAuthors = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_AUTHORS, userLastLoginVisibleForAuthors);
		userLastLoginVisibleForUsermanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_USERMANAGERS, userLastLoginVisibleForUsermanagers);
		userLastLoginVisibleForRolesmanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_ROLESMANAGERS, userLastLoginVisibleForRolesmanagers);
		userLastLoginVisibleForGroupmanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_GROUPMANAGERS, userLastLoginVisibleForGroupmanagers);
		userLastLoginVisibleForLearnresourcemanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_LEARNRESOURCEMANAGERS, userLastLoginVisibleForLearnresourcemanagers);
		userLastLoginVisibleForPoolmanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_POOLMANAGERS, userLastLoginVisibleForPoolmanagers);
		userLastLoginVisibleForCurriculummanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_CURRICULUMMANAGERS, userLastLoginVisibleForCurriculummanagers);
		userLastLoginVisibleForLecturemanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_LECTUREMANAGERS, userLastLoginVisibleForLecturemanagers);
		userLastLoginVisibleForQualitymanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_QUALITYMANAGERS, userLastLoginVisibleForQualitymanagers);
		userLastLoginVisibleForLinemanagers = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_LINEMANAGERS, userLastLoginVisibleForLinemanagers);
		userLastLoginVisibleForAdministrators = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_ADMINISTRATORS, userLastLoginVisibleForAdministrators);
		userLastLoginVisibleForPrincipals = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_PRINCIPALS, userLastLoginVisibleForPrincipals);
		userLastLoginVisibleForSystemAdmins = getStringPropertyValue(USER_LASTLOGIN_VISIBLE_SYSTEMADMINS, userLastLoginVisibleForSystemAdmins);

		// autocompletion
		userSearchAutocompleteForUsers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_USERS, userSearchAutocompleteForUsers);
		userSearchAutocompleteForAuthors = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_AUTHORS, userSearchAutocompleteForAuthors);
		userSearchAutocompleteForUsermanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_USERMANAGERS, userSearchAutocompleteForUsermanagers);
		userSearchAutocompleteForRolesmanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_ROLESMANAGERS, userSearchAutocompleteForRolesmanagers);
		userSearchAutocompleteForGroupmanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_GROUPMANAGERS, userSearchAutocompleteForGroupmanagers);
		userSearchAutocompleteForLearnresourcemanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_LEARNRESOURCEMANAGERS, userSearchAutocompleteForLearnresourcemanagers);
		userSearchAutocompleteForPoolmanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_POOLMANAGERS, userSearchAutocompleteForPoolmanagers);
		userSearchAutocompleteForCurriculummanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_CURRICULUMMANAGERS, userSearchAutocompleteForCurriculummanagers);
		userSearchAutocompleteForLecturemanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_LECTUREMANAGERS, userSearchAutocompleteForLecturemanagers);
		userSearchAutocompleteForQualitymanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_QUALITYMANAGERS, userSearchAutocompleteForQualitymanagers);
		userSearchAutocompleteForLinemanagers = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_LINEMANAGERS, userSearchAutocompleteForLinemanagers);
		userSearchAutocompleteForPrincipals = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_PRINCIPALS, userSearchAutocompleteForPrincipals);
		userSearchAutocompleteForAdministrators = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_ADMINISTRATORS, userSearchAutocompleteForAdministrators);
		userSearchAutocompleteForSystemAdmins = getStringPropertyValue(USERSEARCHAUTOCOMPLETE_SYSTEMADMINS, userSearchAutocompleteForSystemAdmins);

		// other stuff
		relationRoleManaged = getStringPropertyValue(RELATION_ROLE_MANAGED, relationRoleManaged);
		relationRoleEnabled = getStringPropertyValue(RELATION_ROLE_ENABLED, relationRoleEnabled);

		userSearchMaxResults = getStringPropertyValue(USERSEARCH_MAXRESULTS, userSearchMaxResults);
		userInfosTunnelCourseBuildingBlock = getStringPropertyValue(USERINFOS_TUNNEL_CBB, userInfosTunnelCourseBuildingBlock);
		wikiEnabled = getStringPropertyValue(WIKI_ENABLED, wikiEnabled);
		
		identityName = getStringPropertyValue(IDENTITY_NAME, identityName);
		log.info("Identity.name generator: {}", identityName);
	}
	
	public static final OrganisationRoles[] getUserAllowedRoles() {
		OrganisationRoles[] copy = new OrganisationRoles[privacyRoles.length];
		System.arraycopy(privacyRoles, 0, copy, 0, privacyRoles.length);
		return copy;
	}
	
	public boolean isIdentityNameAutoGenerated() {
		return "auto".equals(identityName);
	}
	
	public String getIdentityName() {
		return identityName;
	}

	public void setIdentityName(String identityName) {
		this.identityName = identityName;
		setStringProperty(IDENTITY_NAME, identityName, true);
	}

	public boolean isUserAllowedAdminProps(Roles roles) {
		if(roles == null || roles.isInvitee() || roles.isGuestOnly()) return false;

		boolean allowed = false;
		for(OrganisationRoles role:privacyRoles) {
			if(roles.hasRole(role) && "enabled".equals(getUserSearchAdminPropsFor(role))) {
				allowed = true;
				break;
			}
		}
		return allowed;
	}

	public String getUserSearchAdminPropsFor(OrganisationRoles role) {
		switch(role) {
			case user: return userSearchAdminPropsForUsers;
			case author: return userSearchAdminPropsForAuthors;
			case usermanager: return userSearchAdminPropsForUsermanagers;
			case rolesmanager: return userSearchAdminPropsForRolesmanagers;
			case groupmanager: return userSearchAdminPropsForGroupmanagers;
			case learnresourcemanager: return userSearchAdminPropsForLearnresourcemanagers;
			case poolmanager: return userSearchAdminPropsForPoolmanagers;
			case curriculummanager: return userSearchAdminPropsForCurriculummanagers;
			case lecturemanager: return userSearchAdminPropsForLecturemanagers;
			case projectmanager: return userSearchAdminPropsForProjectmanagers;
			case qualitymanager: return userSearchAdminPropsForQualitymanagers;
			case linemanager: return userSearchAdminPropsForLinemanagers;
			case principal: return userSearchAdminPropsForPrincipals;
			case administrator: return userSearchAdminPropsForAdministrators;
			case sysadmin: return userSearchAdminPropsForSystemAdmins;
			default: return "disabled";
		}
	}
	
	public void setUserSearchAdminPropsFor(OrganisationRoles role, String enable) {
		switch(role) {
			case user:
				userSearchAdminPropsForUsers = setStringProperty(USERSEARCH_ADMINPROPS_USERS, enable, true);
				break;
			case author:
				userSearchAdminPropsForAuthors = setStringProperty(USERSEARCH_ADMINPROPS_AUTHORS, enable, true);
				break;
			case usermanager:
				userSearchAdminPropsForUsermanagers = setStringProperty(USERSEARCH_ADMINPROPS_USERMANAGERS, enable, true);
				break;
			case rolesmanager:
				userSearchAdminPropsForRolesmanagers = setStringProperty(USERSEARCH_ADMINPROPS_ROLESMANAGERS, enable, true);
				break;
			case groupmanager:
				userSearchAdminPropsForGroupmanagers = setStringProperty(USERSEARCH_ADMINPROPS_GROUPMANAGERS, enable, true);
				break;
			case learnresourcemanager:
				userSearchAdminPropsForLearnresourcemanagers = setStringProperty(USERSEARCH_ADMINPROPS_LEARNRESOURCEMANAGERS, enable, true);
				break;
			case poolmanager:
				userSearchAdminPropsForPoolmanagers = setStringProperty(USERSEARCH_ADMINPROPS_POOLMANAGERS, enable, true);
				break;
			case curriculummanager:
				userSearchAdminPropsForCurriculummanagers = setStringProperty(USERSEARCH_ADMINPROPS_CURRICULUMMANAGERS, enable, true);
				break;
			case lecturemanager:
				userSearchAdminPropsForLecturemanagers = setStringProperty(USERSEARCH_ADMINPROPS_LECTUREMANAGERS, enable, true);
				break;
			case projectmanager:
				userSearchAdminPropsForProjectmanagers = setStringProperty(USERSEARCH_ADMINPROPS_PROJECTMANAGERS, enable, true);
			case qualitymanager:
				userSearchAdminPropsForQualitymanagers = setStringProperty(USERSEARCH_ADMINPROPS_QUALITYMANAGERS, enable, true);
				break;
			case linemanager:
				userSearchAdminPropsForLinemanagers = setStringProperty(USERSEARCH_ADMINPROPS_LINEMANAGERS, enable, true);
				break;
			case principal:
				userSearchAdminPropsForPrincipals = setStringProperty(USERSEARCH_ADMINPROPS_PRINCIPALS, enable, true);
				break;
			case administrator:
				userSearchAdminPropsForAdministrators = setStringProperty(USERSEARCH_ADMINPROPS_ADMINISTRATORS, enable, true);
				break;
			case sysadmin:
				userSearchAdminPropsForSystemAdmins = setStringProperty(USERSEARCH_ADMINPROPS_SYSTEMADMINS, enable, true);
				break;
			default: /* Ignore the other roles */
		}
	}
	
	public boolean isUserLastVisitVisible(Roles roles) {
		if(roles == null || roles.isGuestOnly() || roles.isInvitee()) return false;
		
		boolean allowed = false;
		for(OrganisationRoles role:privacyRoles) {
			if(roles.hasRole(role) && "enabled".equals(getUserLastLoginVisibleFor(role))) {
				allowed = true;
				break;
			}
		}
		return allowed;
	}
	
	public String getUserLastLoginVisibleFor(OrganisationRoles role) {
		switch(role) {
			case user: return userLastLoginVisibleForUsers;
			case author: return userLastLoginVisibleForAuthors;
			case usermanager: return userLastLoginVisibleForUsermanagers;
			case rolesmanager: return userLastLoginVisibleForRolesmanagers;
			case groupmanager: return userLastLoginVisibleForGroupmanagers;
			case learnresourcemanager: return userLastLoginVisibleForLearnresourcemanagers;
			case poolmanager: return userLastLoginVisibleForPoolmanagers;
			case curriculummanager: return userLastLoginVisibleForCurriculummanagers;
			case lecturemanager: return userLastLoginVisibleForLecturemanagers;
			case projectmanager: return userLastLoginVisibleForProjectmanagers;
			case qualitymanager: return userLastLoginVisibleForQualitymanagers;
			case linemanager: return userLastLoginVisibleForLinemanagers;
			case principal: return userLastLoginVisibleForPrincipals;
			case administrator: return userLastLoginVisibleForAdministrators;
			case sysadmin: return userLastLoginVisibleForSystemAdmins;
			default: return "disabled";
		}
	}
	
	public void setUserLastLoginVisibleFor(OrganisationRoles role, String enable) {
		switch(role) {
			case user:
				userLastLoginVisibleForUsers = setStringProperty(USER_LASTLOGIN_VISIBLE_USERS, enable, true);
				break;
			case author:
				userLastLoginVisibleForAuthors = setStringProperty(USER_LASTLOGIN_VISIBLE_AUTHORS, enable, true);
				break;
			case usermanager:
				userLastLoginVisibleForUsermanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_USERMANAGERS, enable, true);
				break;
			case rolesmanager:
				userLastLoginVisibleForRolesmanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_ROLESMANAGERS, enable, true);
				break;
			case groupmanager:
				userLastLoginVisibleForGroupmanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_GROUPMANAGERS, enable, true);
				break;
			case learnresourcemanager:
				userLastLoginVisibleForLearnresourcemanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_LEARNRESOURCEMANAGERS, enable, true);
				break;
			case poolmanager:
				userLastLoginVisibleForPoolmanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_POOLMANAGERS, enable, true);
				break;
			case curriculummanager:
				userLastLoginVisibleForCurriculummanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_CURRICULUMMANAGERS, enable, true);
				break;
			case lecturemanager:
				userLastLoginVisibleForLecturemanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_LECTUREMANAGERS, enable, true);
				break;
			case projectmanager:
				userLastLoginVisibleForProjectmanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_PROJECTMANAGERS, enable, true);
				break;
			case qualitymanager:
				userLastLoginVisibleForQualitymanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_QUALITYMANAGERS, enable, true);
				break;
			case linemanager:
				userLastLoginVisibleForLinemanagers = setStringProperty(USER_LASTLOGIN_VISIBLE_LINEMANAGERS, enable, true);
				break;
			case principal:
				userLastLoginVisibleForPrincipals = setStringProperty(USER_LASTLOGIN_VISIBLE_PRINCIPALS, enable, true);
				break;
			case administrator:
				userLastLoginVisibleForAdministrators = setStringProperty(USER_LASTLOGIN_VISIBLE_ADMINISTRATORS, enable, true);
				break;
			case sysadmin:
				userLastLoginVisibleForSystemAdmins = setStringProperty(USER_LASTLOGIN_VISIBLE_SYSTEMADMINS, enable, true);
				break;
			default: /* Ignore the other roles */
		}
	}

	public boolean isUserAllowedAutoComplete(Roles roles) {
		if(roles == null || roles.isGuestOnly() || roles.isInvitee()) return false;
		
		boolean allowed = false;
		for(OrganisationRoles role:privacyRoles) {
			if(roles.hasRole(role) && "enabled".equals(getUserSearchAutocompleteFor(role))) {
				allowed = true;
				break;
			}
		}
		return allowed;
	}
	
	public String getUserSearchAutocompleteFor(OrganisationRoles role) {
		switch(role) {
			case user: return userSearchAutocompleteForUsers;
			case author: return userSearchAutocompleteForAuthors;
			case usermanager: return userSearchAutocompleteForUsermanagers;
			case rolesmanager: return userSearchAutocompleteForRolesmanagers;
			case groupmanager: return userSearchAutocompleteForGroupmanagers;
			case learnresourcemanager: return userSearchAutocompleteForLearnresourcemanagers;
			case poolmanager: return userSearchAutocompleteForPoolmanagers;
			case curriculummanager: return userSearchAutocompleteForCurriculummanagers;
			case lecturemanager: return userSearchAutocompleteForLecturemanagers;
			case projectmanager: return userSearchAutocompleteForProjectmanagers;
			case qualitymanager: return userSearchAutocompleteForQualitymanagers;
			case linemanager: return userSearchAutocompleteForLinemanagers;
			case principal: return userSearchAutocompleteForPrincipals;
			case administrator: return userSearchAutocompleteForAdministrators;
			case sysadmin: return userSearchAutocompleteForSystemAdmins;
			default: return "disabled";
		}
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
	
	public boolean isRelationRoleManaged() {
		return "enabled".equals(relationRoleManaged);
	}

	public void setRelationRoleManaged(boolean managed) {
		relationRoleManaged = managed ? "enabled" : "disabled";
		setStringProperty(RELATION_ROLE_MANAGED, relationRoleManaged, true);
	}
	
	public boolean isRelationRoleEnabled() {
		return "enabled".equals(relationRoleEnabled);
	}

	public void setRelationRoleEnabled(boolean enabled) {
		relationRoleEnabled = enabled ? "enabled" : "disabled";
		setStringProperty(RELATION_ROLE_ENABLED, relationRoleEnabled, true);
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