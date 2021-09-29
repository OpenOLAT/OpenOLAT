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
package org.olat.modules.bigbluebutton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 7 août 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum BigBlueButtonRecordingsPublishedRoles {
	coach,
	participant,
	guest,
	none,
	all;
	
	public static final BigBlueButtonRecordingsPublishedRoles[] defaultValues() {
		return new BigBlueButtonRecordingsPublishedRoles[] { BigBlueButtonRecordingsPublishedRoles.all };
	}
	
	public static final boolean has(BigBlueButtonRecordingsPublishedRoles[] roles, BigBlueButtonRecordingsPublishedRoles roleToHave) {
		if(roles == null || roles.length == 0) return false;
		
		for(BigBlueButtonRecordingsPublishedRoles role:roles) {
			if(role == roleToHave) {
				return true;
			}
		}
		return false;
	}
	
	public static final BigBlueButtonRecordingsPublishedRoles secureValueOf(String val) {
		BigBlueButtonRecordingsPublishedRoles publishing = BigBlueButtonRecordingsPublishedRoles.none;
		if(StringHelper.containsNonWhitespace(val)) {
			if("auto".equals(val)) {
				publishing = BigBlueButtonRecordingsPublishedRoles.none;
			} else {
				for(BigBlueButtonRecordingsPublishedRoles l:values()) {
					if(l.name().equals(val)) {
						publishing = l;
					}
				}
			}
		}
		return publishing;
	}
	
	public static BigBlueButtonRecordingsPublishedRoles[] toArray(String roles) {
		BigBlueButtonRecordingsPublishedRoles[] rolesEnum;
		if(StringHelper.containsNonWhitespace(roles)) {
			String[] roleArr = roles.split(",");
			rolesEnum = new BigBlueButtonRecordingsPublishedRoles[roleArr.length];
			for(int i=roleArr.length; i-->0; ) {
				rolesEnum[i] = secureValueOf(roleArr[i]);
			}
		} else {
			rolesEnum = new BigBlueButtonRecordingsPublishedRoles[0];
		}
		return rolesEnum;
	}
	
	public static BigBlueButtonRecordingsPublishedRoles[] toArray(Collection<String> roles) {
		BigBlueButtonRecordingsPublishedRoles[] rolesEnum;
		if(roles != null && !roles.isEmpty()) {
			List<BigBlueButtonRecordingsPublishedRoles> rolesList = new ArrayList<>(roles.size());
			for(String role:roles) {
				rolesList.add(secureValueOf(role));
			}
			rolesEnum = rolesList.toArray(new BigBlueButtonRecordingsPublishedRoles[rolesList.size()]);
		} else {
			rolesEnum = new BigBlueButtonRecordingsPublishedRoles[0];
		}
		return rolesEnum;
	}
	
	public static String toString(BigBlueButtonRecordingsPublishedRoles[] roles) {
		StringBuilder sb = new StringBuilder(32);
		if(roles != null && roles.length > 0) {
			for(BigBlueButtonRecordingsPublishedRoles role:roles) {
				if(role != null) {
					if(sb.length() > 0) sb.append(",");
					sb.append(role.name());
				}
			}
		}
		return sb.toString();
	}
}
