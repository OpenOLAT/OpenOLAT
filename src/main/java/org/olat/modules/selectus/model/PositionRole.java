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
package org.olat.modules.selectus.model;

import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  16 feb. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public enum PositionRole {
	
	member("role.member"),
	head("role.head"),
	secretary("role.secretary"),
	exofficio("role.exofficio");

	private final String role;
	public static final PositionRole[] EMPTY = new PositionRole[0];
	
	private PositionRole(String role) {
		this.role = role;
	}
	
	public String role() {
		return role;
	}
	
	public static PositionRole role(String key) {
		for(PositionRole value : values()) {
			if(key.equals(value.role())) {
				return value;
			}
		}
		return null;
	}
	
	public static PositionRole[] valueOfArray(String roles) {
		PositionRole[] rolesEnum;
		if(StringHelper.containsNonWhitespace(roles)) {
			String[] roleArr = roles.split(",");
			rolesEnum = new PositionRole[roleArr.length];
			for(int i=roleArr.length; i-->0; ) {
				rolesEnum[i] = PositionRole.valueOf(roleArr[i]);
			}
		} else {
			rolesEnum = new PositionRole[0];
		}
		return rolesEnum;
	}
	
	public static String[] roles() {
		String[] roles = new String[values().length];
		for(int i=0; i<roles.length; i++) {
			roles[i] = values()[i].role();
		}
		return roles;
	}
	
	public static String toString(PositionRole[] roles) {
		StringBuilder sb = new StringBuilder(32);
		if(roles != null && roles.length > 0) {
			for(PositionRole role:roles) {
				if(role != null) {
					if(sb.length() > 0) sb.append(",");
					sb.append(role.name());
				}
			}
		}
		return sb.toString();
	}
}
