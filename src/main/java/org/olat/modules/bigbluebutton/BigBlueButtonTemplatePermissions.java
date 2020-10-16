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
import java.util.Collections;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 24 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum BigBlueButtonTemplatePermissions {
	
	group,
	coach,
	owner,
	author,
	administrator;
	
	public boolean accept(BigBlueButtonTemplatePermissions role) {
		if(role == null) return false;
		if(this == role) {
			return true;
		}
		if(role == administrator) {
			return true;
		}
		if(role == author) {
			return (this == author || this == owner || this == coach);
		}
		return false;
	}
	
	public static List<BigBlueButtonTemplatePermissions> valuesAsList() {
		List<BigBlueButtonTemplatePermissions> roles = new ArrayList<>();
		Collections.addAll(roles, BigBlueButtonTemplatePermissions.values());
		return roles;
	}
	
	public static List<BigBlueButtonTemplatePermissions> toList(String val) {
		List<BigBlueButtonTemplatePermissions> roles = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(val)) {
			String[] valArray = val.split("[,]");
			if(valArray != null && valArray.length > 0) {
				for(String v:valArray) {
					BigBlueButtonTemplatePermissions role = BigBlueButtonTemplatePermissions.valueOfSecure(v);
					if(role != null) {
						roles.add(role);
					}
				}
			}	
		}
		return roles;
	}
	
	public static BigBlueButtonTemplatePermissions valueOfSecure(String val) {
		for(BigBlueButtonTemplatePermissions role:BigBlueButtonTemplatePermissions.values()) {
			if(role.name().equals(val)) {
				return role;
			}
		}
		return null;
	}
	
	public static String toString(List<BigBlueButtonTemplatePermissions> roles) {
		StringBuilder sb = new StringBuilder();
		for(BigBlueButtonTemplatePermissions role:roles) {
			if(sb.length() > 0) sb.append(",");
			sb.append(role.name());
		}
		return sb.toString();
	}
}
