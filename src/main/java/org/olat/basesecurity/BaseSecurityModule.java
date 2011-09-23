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
* <p>
*/ 

package org.olat.basesecurity;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;

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
	
	/**
	 * default values
	 */
	public static Boolean USERMANAGER_CAN_CREATE_USER = false;
	public static Boolean USERMANAGER_CAN_DELETE_USER = true;
	public static Boolean USERMANAGER_CAN_CREATE_PWD = true;
	public static Boolean USERMANAGER_CAN_MODIFY_PWD = true;
	public static Boolean USERMANAGER_CAN_START_GROUPS = true;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void initDefaultProperties() {
		USERMANAGER_CAN_CREATE_USER = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_CREATE_USER, USERMANAGER_CAN_CREATE_USER);
		USERMANAGER_CAN_DELETE_USER = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_DELETE_USER, USERMANAGER_CAN_DELETE_USER);
		USERMANAGER_CAN_CREATE_PWD = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_CREATE_PWD, USERMANAGER_CAN_CREATE_PWD);
		USERMANAGER_CAN_MODIFY_PWD = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_MODIFY_PWD, USERMANAGER_CAN_MODIFY_PWD);
		USERMANAGER_CAN_START_GROUPS = getBooleanConfigParameter(CONFIG_USERMANAGER_CAN_START_GROUPS, USERMANAGER_CAN_START_GROUPS);
		
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
		
		
	}

	@Override
	protected void initFromChangedProperties() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}


}