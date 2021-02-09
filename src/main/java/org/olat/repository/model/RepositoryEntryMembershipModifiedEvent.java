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
package org.olat.repository.model;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 09.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryMembershipModifiedEvent extends MultiUserEvent {

	private static final long serialVersionUID = -8624039692057985920L;
	
	public static final String IDENTITY_REMOVED = "identity.removed.re";
	public static final String ROLE_PARTICIPANT_ADDED = "identity.role.participant.added";
	/** event: an identity has been added to the group but needs a user confirmation */
	public static final String ROLE_PARTICIPANT_ADD_PENDING = "identity.role.participant.add.pending";
	private Long identityKey;
	private Long repositoryEntryKey;

	
	/**
	 * @param command one of the class constants
	 * @param group
	 * @param identity
	 */
	public RepositoryEntryMembershipModifiedEvent(String command, Long identityKey, Long repositoryEntryKey) {
		super(command);
		this.identityKey = identityKey;
		this.repositoryEntryKey = repositoryEntryKey;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}
	
	public static RepositoryEntryMembershipModifiedEvent removed(IdentityRef identity, RepositoryEntryRef re) {
		return new RepositoryEntryMembershipModifiedEvent(IDENTITY_REMOVED, identity.getKey(), re.getKey());
	}

	public static RepositoryEntryMembershipModifiedEvent roleParticipantAdded(Identity identity, RepositoryEntry re) {
		return new RepositoryEntryMembershipModifiedEvent(ROLE_PARTICIPANT_ADDED, identity.getKey(), re.getKey());
	}
	
	public static RepositoryEntryMembershipModifiedEvent roleParticipantAddPending(Identity identity, RepositoryEntry re) {
		return new RepositoryEntryMembershipModifiedEvent(ROLE_PARTICIPANT_ADD_PENDING, identity.getKey(), re.getKey());
	}
}
