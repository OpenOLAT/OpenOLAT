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

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntryStrictMember extends PersistentObject {

	private static final long serialVersionUID = 3795108974268603941L;
	
	private Long repoParticipantKey;
	private Long repoTutorKey;
	private Long repoOwnerKey;
	private Long groupParticipantKey;
	private Long groupOwnerKey;

	public Long getRepoParticipantKey() {
		return repoParticipantKey;
	}
	
	public void setRepoParticipantKey(Long repoParticipantKey) {
		this.repoParticipantKey = repoParticipantKey;
	}
	
	public Long getRepoTutorKey() {
		return repoTutorKey;
	}

	public void setRepoTutorKey(Long repoTutorKey) {
		this.repoTutorKey = repoTutorKey;
	}

	public Long getRepoOwnerKey() {
		return repoOwnerKey;
	}
	
	public void setRepoOwnerKey(Long repoOwnerKey) {
		this.repoOwnerKey = repoOwnerKey;
	}
	
	public Long getGroupParticipantKey() {
		return groupParticipantKey;
	}
	
	public void setGroupParticipantKey(Long groupParticipantKey) {
		this.groupParticipantKey = groupParticipantKey;
	}
	
	public Long getGroupOwnerKey() {
		return groupOwnerKey;
	}
	
	public void setGroupOwnerKey(Long groupOwnerKey) {
		this.groupOwnerKey = groupOwnerKey;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 3768 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		return false;
	}
}
