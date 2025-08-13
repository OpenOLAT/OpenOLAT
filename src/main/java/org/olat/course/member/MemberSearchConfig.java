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
package org.olat.course.member;

import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberSearchConfig {

	private boolean singleSelection = true;
	private boolean multiSelection = true;
	private boolean showSelectButton = true;
	
	private GroupRoles[] roles;
	private boolean runningTestSession;
	private String runningTestSessionSubIdent;

	private final String prefsId;
	private final GroupRoles asRole;
	private final RepositoryEntry repositoryEntry;
	
	private List<Identity> identitiesList;
	private Collection<Long> preselectedIdentitiesKeys;
	
	private MemberSearchConfig(RepositoryEntry repositoryEntry, GroupRoles asRole, String prefsId) {
		this.asRole = asRole;
		this.prefsId = prefsId;
		this.repositoryEntry = repositoryEntry;
	}
	
	public static MemberSearchConfig defaultConfig(RepositoryEntry repositoryEntry, GroupRoles asRole, String prefsId) {
		return new MemberSearchConfig(repositoryEntry, asRole, prefsId);
	}
	
	public boolean singleSelection() {
		return singleSelection;
	}
	
	public MemberSearchConfig singleSelection( boolean enable) {
		singleSelection = enable;
		return this;
	}
	
	public boolean multiSelection() {
		return multiSelection;
	}
	
	public MemberSearchConfig multiSelection( boolean enable) {
		multiSelection = enable;
		return this;
	}
	
	public boolean showSelectButton() {
		return showSelectButton;
	}
	
	public MemberSearchConfig showSelectButton(boolean show) {
		this.showSelectButton = show;
		return this;
	}

	public String prefsId() {
		return prefsId;
	}

	public RepositoryEntry repositoryEntry() {
		return repositoryEntry;
	}
	
	public boolean runningTestSession() {
		return runningTestSession;
	}
	
	public String runningTestSessionSubIdent() {
		return runningTestSessionSubIdent;
	}
	
	public MemberSearchConfig onlyRunningTestSessions(boolean onlyRunningTestSession, String runningTestSessionSubIdent) {
		this.runningTestSession = onlyRunningTestSession;
		this.runningTestSessionSubIdent = runningTestSessionSubIdent;
		return this;
	}
	
	public GroupRoles searchAsRole() {
		return asRole;
	}
	
	public MemberSearchConfig searchForRoles(GroupRoles[] roles) {
		this.roles = roles;
		return this;
	}
	
	public boolean withOwners() {
		return hasRoles(GroupRoles.owner);
	}
	
	public boolean withCoaches() {
		return hasRoles(GroupRoles.coach);
	}
	
	public boolean withParticipants() {
		return hasRoles(GroupRoles.participant);
	}
	
	public boolean withWaiting() {
		return hasRoles(GroupRoles.waiting);
	}
	
	private boolean hasRoles(GroupRoles role) {
		if(roles == null) return true; // all allowed

		for(GroupRoles r:roles ) {
			if(r == role) {
				return true;
			}
		}
		return false;
	}
	
	public List<Identity> identitiesList() {
		return identitiesList;
	}
	
	public MemberSearchConfig identitiesList(List<Identity> list) {
		identitiesList = list;
		return this;
	}
	
	public Collection<Long> preselectedIdentitiesKeys() {
		return preselectedIdentitiesKeys;
	}
	
	public MemberSearchConfig preselectedIdentitiesKeys(Collection<Long> list) {
		preselectedIdentitiesKeys = list;
		return this;
	}
}
