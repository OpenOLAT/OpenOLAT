/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.teams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 21 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public enum TeamsRecordingsPublishedRoles {
	
	coach,
	participant,
	guest,
	none,
	all;
	
	public static final TeamsRecordingsPublishedRoles secureValueOf(String val) {
		TeamsRecordingsPublishedRoles publishing = TeamsRecordingsPublishedRoles.none;
		if(StringHelper.containsNonWhitespace(val)) {
			for(TeamsRecordingsPublishedRoles l:values()) {
				if(l.name().equals(val)) {
					publishing = l;
				}
			}
		}
		return publishing;
	}
	
	public static final boolean has(TeamsRecordingsPublishedRoles[] roles, TeamsRecordingsPublishedRoles roleToHave) {
		if(roles == null || roles.length == 0) return false;
		
		for(TeamsRecordingsPublishedRoles role:roles) {
			if(role == roleToHave) {
				return true;
			}
		}
		return false;
	}
	
	public static TeamsRecordingsPublishedRoles[] toArray(String roles) {
		TeamsRecordingsPublishedRoles[] rolesEnum;
		if(StringHelper.containsNonWhitespace(roles)) {
			String[] roleArr = roles.split(",");
			rolesEnum = new TeamsRecordingsPublishedRoles[roleArr.length];
			for(int i=roleArr.length; i-->0; ) {
				rolesEnum[i] = secureValueOf(roleArr[i]);
			}
		} else {
			rolesEnum = new TeamsRecordingsPublishedRoles[0];
		}
		return rolesEnum;
	}
	
	public static TeamsRecordingsPublishedRoles[] toArray(Collection<String> roles) {
		TeamsRecordingsPublishedRoles[] rolesEnum;
		if(roles != null && !roles.isEmpty()) {
			List<TeamsRecordingsPublishedRoles> rolesList = new ArrayList<>(roles.size());
			for(String role:roles) {
				rolesList.add(secureValueOf(role));
			}
			rolesEnum = rolesList.toArray(new TeamsRecordingsPublishedRoles[rolesList.size()]);
		} else {
			rolesEnum = new TeamsRecordingsPublishedRoles[0];
		}
		return rolesEnum;
	}

	public static String toString(TeamsRecordingsPublishedRoles[] roles) {
		StringBuilder sb = new StringBuilder(32);
		if(roles != null && roles.length > 0) {
			for(TeamsRecordingsPublishedRoles role:roles) {
				if(role != null) {
					if(sb.length() > 0) sb.append(",");
					sb.append(role.name());
				}
			}
		}
		return sb.toString();
	}
}
