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

import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 15.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Rule {
	
	public static final String AUTHOR = "author";
	public static final String USER_MGR = "userManager";
	public static final String GROUP_MGR = "groupManager";
	public static final String RSRC_MGR = "institutionalResourceManager";
	public static final String POOL_MGR = "poolAdmin";
	public static final String ADMIN = "olatAdmin";
	
	private String role;
	private String userAttributeKey;
	private String userAttributeValue;
	private String landingPath;
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	public String getUserAttributeKey() {
		return userAttributeKey;
	}
	
	public void setUserAttributeKey(String userAttributeKey) {
		this.userAttributeKey = userAttributeKey;
	}
	
	public String getUserAttributeValue() {
		return userAttributeValue;
	}
	
	public void setUserAttributeValue(String userAttributeValue) {
		this.userAttributeValue = userAttributeValue;
	}
	
	public String getLandingPath() {
		return landingPath;
	}
	
	public void setLandingPath(String landingPath) {
		this.landingPath = landingPath;
	}
	
	public boolean match(UserSession userSession) {
		if(userSession == null || userSession.getRoles() == null || userSession.getIdentity() == null) {
			return false;
		}
		
		boolean match = true;
		
		//match the role?
		if(!"none".equals(role) && StringHelper.containsNonWhitespace(role)) {
			Roles roles = userSession.getRoles();
			switch(role) {
				case AUTHOR: match &= roles.isAuthor(); break;
				case USER_MGR: match &= roles.isUserManager(); break;
				case GROUP_MGR: match &= roles.isGroupManager(); break;
				case RSRC_MGR: match &= roles.isLearnResourceManager(); break;
				case POOL_MGR: match &= roles.isPoolAdmin(); break;
				case ADMIN: match &= roles.isOLATAdmin(); break;
				default: {
					match &= false;
				}
			}
		}
		
		if(StringHelper.containsNonWhitespace(userAttributeKey)) {
			User user = userSession.getIdentity().getUser();
			String value = user.getProperty(userAttributeKey, null);
			if(!StringHelper.containsNonWhitespace(value) && !StringHelper.containsNonWhitespace(userAttributeValue)) {
				// ok, both are null or empty
			} else if(StringHelper.containsNonWhitespace(value) && StringHelper.containsNonWhitespace(userAttributeValue)) {
				match &= userAttributeValue.trim().equalsIgnoreCase(value.trim());
			} else {
				match &= false;
			}
		}
		return match;
	}
}
