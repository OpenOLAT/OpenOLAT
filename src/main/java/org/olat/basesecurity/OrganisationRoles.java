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
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum OrganisationRoles {

	sysadmin,
	administrator,
	usermanager,
	rolesmanager,
	learnresourcemanager,
	lecturemanager,
	groupmanager,
	poolmanager,
	curriculummanager,
	qualitymanager,
	projectmanager,
	linemanager,
	principal,
	author,
	user,
	invitee,
	guest;
	
	public static final OrganisationRoles[] EMPTY_ROLES = new OrganisationRoles[0];
	
	public static boolean isValue(String value) {
		boolean isValue = false;
		if(StringHelper.containsNonWhitespace(value)) {
			for(OrganisationRoles role:OrganisationRoles.values()) {
				if(role.name().equals(value)) {
					isValue = true;
				}
			}
		}
		return isValue;
	}
	
	public static boolean valid(String value) {
		if(!StringHelper.containsNonWhitespace(value)) {
			return false;
		}

		return OrganisationRoles.isValue(value);
	}
	
	public static List<OrganisationRoles> toValues(List<String> roles) {
		List<OrganisationRoles> roleList = new ArrayList<>();
		if(roles != null && !roles.isEmpty()) {
			for(String role:roles) {
				if(valid(role)) {
					roleList.add(OrganisationRoles.valueOf(role));
				}
			}
		}
		return roleList;
	}
	
	public static List<String> toList(OrganisationRoles... roles) {
		List<String> roleList = new ArrayList<>();
		if(roles != null && roles.length > 0 && roles[0] != null) {
			for(int i=0; i<roles.length; i++) {
				if(roles[i] != null) {
					roleList.add(roles[i].name());
				}
			}
		}
		return roleList;
	}
	
	public static OrganisationRoles[] valuesWithoutGuestAndInvitee() {
		return new OrganisationRoles[]{
				sysadmin,
				administrator,
				usermanager,
				rolesmanager,
				learnresourcemanager,
				lecturemanager,
				groupmanager,
				poolmanager,
				curriculummanager,
				projectmanager,
				qualitymanager,
				linemanager,
				principal,
				author,
				user };
	}
	
	public static OrganisationRoles[] managersRoles() {
		return new OrganisationRoles[]{
				sysadmin,
				administrator,
				usermanager,
				rolesmanager,
				learnresourcemanager,
				lecturemanager,
				groupmanager,
				poolmanager,
				curriculummanager,
				qualitymanager,
				linemanager,
				principal,
				author,
			};
	}
	
	/**
	 * @param role The role to check
	 * @return true if the role is by default inherited in the organisation tree
	 */
	public static boolean isInheritedByDefault(OrganisationRoles role) {
		return role == OrganisationRoles.author
				|| role == OrganisationRoles.usermanager || role == OrganisationRoles.rolesmanager
				|| role == OrganisationRoles.groupmanager || role == OrganisationRoles.learnresourcemanager
				|| role == OrganisationRoles.poolmanager || role == OrganisationRoles.curriculummanager
				|| role == OrganisationRoles.lecturemanager || role == projectmanager || role == OrganisationRoles.qualitymanager
				|| role == OrganisationRoles.linemanager || role == OrganisationRoles.principal
				|| role == OrganisationRoles.administrator || role == OrganisationRoles.sysadmin;
	}
}
