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

import org.olat.core.gui.control.Event;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SearchMembersParams extends Event {
	private static final long serialVersionUID = -8842738563007496141L;
	
	private boolean repoOwners;
	private boolean repoTutors;
	private boolean repoParticipants;
	private boolean groupTutors;
	private boolean groupParticipants;
	private boolean groupWaitingList;
	private boolean pending;
	
	private boolean repoOrigin = true;
	private boolean groupOrigin = true;
	
	private String login;
	private String searchString;
	private Map<String, String> userPropertiesSearch;
	
	public SearchMembersParams() {
		super("search_members");
	}
	
	public SearchMembersParams(boolean repoOwners, boolean repoTutors, boolean repoParticipants,
			boolean groupTutors, boolean groupParticipants, boolean groupWaitingList,
			boolean pending) {
		this();
		this.repoOwners = repoOwners;
		this.repoTutors = repoTutors;
		this.repoParticipants = repoParticipants;
		this.groupTutors = groupTutors;
		this.groupParticipants = groupParticipants;
		this.groupWaitingList = groupWaitingList;
		this.pending = pending;
	}
	
	public boolean isRepoOwners() {
		return repoOwners;
	}
	
	public void setRepoOwners(boolean repoOwners) {
		this.repoOwners = repoOwners;
	}
	
	public boolean isRepoTutors() {
		return repoTutors;
	}
	
	public void setRepoTutors(boolean repoTutors) {
		this.repoTutors = repoTutors;
	}
	
	public boolean isRepoParticipants() {
		return repoParticipants;
	}
	
	public void setRepoParticipants(boolean repoParticipants) {
		this.repoParticipants = repoParticipants;
	}
	
	public boolean isGroupTutors() {
		return groupTutors;
	}
	
	public void setGroupTutors(boolean groupTutors) {
		this.groupTutors = groupTutors;
	}
	
	public boolean isGroupParticipants() {
		return groupParticipants;
	}
	
	public void setGroupParticipants(boolean groupParticipants) {
		this.groupParticipants = groupParticipants;
	}
	
	public boolean isGroupWaitingList() {
		return groupWaitingList;
	}
	
	public void setGroupWaitingList(boolean groupWaitingList) {
		this.groupWaitingList = groupWaitingList;
	}

	public boolean isPending() {
		return pending;
	}

	public void setPending(boolean pending) {
		this.pending = pending;
	}

	public boolean isRepoOrigin() {
		return repoOrigin;
	}

	public void setRepoOrigin(boolean repoOrigin) {
		this.repoOrigin = repoOrigin;
	}

	public boolean isGroupOrigin() {
		return groupOrigin;
	}

	public void setGroupOrigin(boolean groupOrigin) {
		this.groupOrigin = groupOrigin;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Map<String, String> getUserPropertiesSearch() {
		return userPropertiesSearch;
	}

	public void setUserPropertiesSearch(Map<String, String> userPropertiesSearch) {
		this.userPropertiesSearch = userPropertiesSearch;
	}
}