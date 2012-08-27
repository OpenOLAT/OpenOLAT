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
package org.olat.group.model;

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupMembershipViewImpl extends PersistentObject {

	private static final long serialVersionUID = -5404538852842562897L;
	
	private Long identityKey;
	private Date lastModified;
	private Long ownerGroupKey;
	private Long participantGroupKey;
	private Long waitingGroupKey;

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Long getOwnerGroupKey() {
		return ownerGroupKey;
	}

	public void setOwnerGroupKey(Long ownerGroupKey) {
		this.ownerGroupKey = ownerGroupKey;
	}

	public Long getParticipantGroupKey() {
		return participantGroupKey;
	}

	public void setParticipantGroupKey(Long participantGroupKey) {
		this.participantGroupKey = participantGroupKey;
	}

	public Long getWaitingGroupKey() {
		return waitingGroupKey;
	}

	public void setWaitingGroupKey(Long waitingGroupKey) {
		this.waitingGroupKey = waitingGroupKey;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 2901 : getKey().hashCode();
	}

	/**
	 * Compares the keys.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof BusinessGroupMembershipViewImpl) {
			BusinessGroupMembershipViewImpl bg = (BusinessGroupMembershipViewImpl)obj;
			return getKey() != null && getKey().equals(bg.getKey());
		}
		return false;
	}
}
