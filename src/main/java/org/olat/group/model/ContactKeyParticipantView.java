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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.Persistable;

/**
 * The view list all visible owners
 * Initial date: 24.1.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity
@Table(name="o_gp_contactkey_participant_v")
public class ContactKeyParticipantView implements Persistable {

	private static final long serialVersionUID = 5125563005863650603L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="membership_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Column(name="bg_part_member_id", nullable=false, insertable=true, updatable=false)
	private Long identityKey;
	@Column(name="bg_owner_sec_id", nullable=false, insertable=true, updatable=false)
	private Long ownerSecGroupKey;
	@Column(name="bg_part_sec_id", nullable=false, insertable=true, updatable=false)
	private Long participantSecGroupKey;


	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getOwnerSecGroupKey() {
		return ownerSecGroupKey;
	}

	public void setOwnerSecGroupKey(Long ownerSecGroupKey) {
		this.ownerSecGroupKey = ownerSecGroupKey;
	}

	public Long getParticipantSecGroupKey() {
		return participantSecGroupKey;
	}

	public void setParticipantSecGroupKey(Long participantSecGroupKey) {
		this.participantSecGroupKey = participantSecGroupKey;
	}

	@Override
	public int hashCode() {
		return key == null ? 925867 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ContactKeyParticipantView) {
			ContactKeyParticipantView msg = (ContactKeyParticipantView)obj;
			return key != null && key.equals(msg.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}