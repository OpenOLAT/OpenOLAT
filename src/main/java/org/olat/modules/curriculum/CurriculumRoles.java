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
package org.olat.modules.curriculum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CurriculumRoles {
	
	curriculummanager,
	curriculumowner,// own a piece of a curriculum
	curriculumelementowner,// own a piece of curriculum
	owner, //same as GroupRoles
	mastercoach,
	coach, //same as GroupRoles
	participant; //same as GroupRoles
	
	
	public static List<String> toList(String... roles) {
		if(roles != null && roles.length > 0 && !(roles.length == 1 && roles[0] == null)) {
			List<String> roleList = new ArrayList<>(roles.length);
			for(String role:roles) {
				roleList.add(role);
			}
			return roleList;
		}
		return Collections.emptyList();
	}
	
	public static final boolean isValueOf(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			for(CurriculumRoles role:values()) {
				if(role.name().equals(val)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param role The role to check
	 * @return true if the role is by default inherited in the curriculum tree
	 */
	public static boolean isInheritedByDefault(CurriculumRoles role) {
		return role == CurriculumRoles.mastercoach;
	}
}
