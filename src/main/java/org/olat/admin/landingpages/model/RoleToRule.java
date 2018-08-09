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
package org.olat.admin.landingpages.model;

import org.olat.basesecurity.OrganisationRoles;

/**
 * 
 * Initial date: 8 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum RoleToRule {
	
	AUTHOR("author", OrganisationRoles.author),
	USER_MGR("userManager", OrganisationRoles.usermanager),
	ROLE_MGR(OrganisationRoles.rolesmanager),
	GROUP_MGR("groupManager", OrganisationRoles.groupmanager),
	RSRC_MGR("institutionalResourceManager", OrganisationRoles.learnresourcemanager),
	POOL_MGR("poolAdmin", OrganisationRoles.poolmanager),
	LECTURE_MGR(OrganisationRoles.lecturemanager),
	QUALITY_MGR(OrganisationRoles.qualitymanager),
	LINE_MGR(OrganisationRoles.linemanager),
	PRINCIPAL(OrganisationRoles.principal),
	ADMIN("olatAdmin", OrganisationRoles.administrator),
	SYS_ADMIN( OrganisationRoles.sysadmin);
	
	private final String roleName;
	private final OrganisationRoles role;
	
	private RoleToRule(OrganisationRoles role) {
		this.roleName = role.name();
		this.role = role;
	}
	
	private RoleToRule(String roleName, OrganisationRoles role) {
		this.roleName = roleName;
		this.role = role;
	}
	
	public final String ruleName() {
		return roleName;
	}
	
	public final OrganisationRoles role() {
		return role;
	}
	
	public static RoleToRule valueOfConfiguration(String string) {
		for(RoleToRule roleToRule:values()) {
			if(string.equals(roleToRule.name()) || string.equals(roleToRule.roleName)) {
				return roleToRule;
			}
		}
		return null;
	}
}
