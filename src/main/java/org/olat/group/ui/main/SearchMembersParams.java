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
package org.olat.group.ui.main;

import java.util.Map;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.control.Event;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchMembersParams extends Event {
	private static final long serialVersionUID = -8842738563007496141L;
	
	private GroupRoles[] roles;
	private boolean pending;
	
	private Origin origin;
	
	private String login;
	private Map<String, String> userPropertiesSearch;
	
	public SearchMembersParams() {
		super("search_members");
	}
	
	public SearchMembersParams(boolean pending, GroupRoles... roles) {
		this();
		this.pending = pending;
		this.roles = roles;
	}
	
	public GroupRoles[] getRoles() {
		return roles;
	}
	
	public void setRole(GroupRoles role) {
		if(role == null) {
			roles = null;
		} else {
			roles = new GroupRoles[] { role };
		}
	}
	
	public void setRoles(GroupRoles[] roles) {
		this.roles = roles;
	}
	
	public boolean isRole(GroupRoles role) {
		if(roles != null) {
			for(GroupRoles r:roles) {
				if(r == role) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public Origin getOrigin() {
		return origin == null ? Origin.all : origin;
	}

	public void setOrigin(Origin origin) {
		this.origin = origin;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Map<String, String> getUserPropertiesSearch() {
		return userPropertiesSearch;
	}

	public void setUserPropertiesSearch(Map<String, String> userPropertiesSearch) {
		this.userPropertiesSearch = userPropertiesSearch;
	}
	
	public enum Origin {
		all,
		repositoryEntry,
		businessGroup,
		curriculum
	}
}