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

import org.olat.group.BusinessGroupMembership;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupMembershipImpl implements BusinessGroupMembership {
	
	private Long identityKey;
	private Long groupKey;
	
	private Date creationDate;
	private Date lastModified;
	private boolean owner;
	private boolean participant;
	private boolean waiting;
	
	public BusinessGroupMembershipImpl() {
		//
	}
	
	public BusinessGroupMembershipImpl(Long identityKey, Long groupKey) {
		this.identityKey = identityKey;
		this.groupKey = groupKey;
	}

	@Override
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public Long getGroupKey() {
		return groupKey;
	}

	public void setGroupKey(Long groupKey) {
		this.groupKey = groupKey;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		if(creationDate == null) return;
		if(this.creationDate == null || this.creationDate.compareTo(creationDate) > 0) {
			this.creationDate = creationDate;
		}
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		if(lastModified == null) return;
		if(this.lastModified == null || this.lastModified.compareTo(lastModified) > 0) {
			this.lastModified = lastModified;
		}	
	}

	@Override
	public boolean isOwner() {
		return owner;
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	@Override
	public boolean isParticipant() {
		return participant;
	}

	public void setParticipant(boolean participant) {
		this.participant = participant;
	}

	@Override
	public boolean isWaiting() {
		return waiting;
	}

	public void setWaiting(boolean waiting) {
		this.waiting = waiting;
	}

	@Override
	public int hashCode() {
		return (getGroupKey() == null ? 2901 : getGroupKey().hashCode())
				+ (getIdentityKey() == null ? -301 : getIdentityKey().hashCode());
	}

	/**
	 * Compares the keys.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof BusinessGroupMembershipImpl) {
			BusinessGroupMembershipImpl bg = (BusinessGroupMembershipImpl)obj;
			return getGroupKey() != null && getGroupKey().equals(bg.getGroupKey())
					&& getIdentityKey() != null && getIdentityKey().equals(bg.getIdentityKey());
		}
		return false;
	}
}
