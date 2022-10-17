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

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Table(name="o_re_membership_v")
@Entity(name="repoentrymembership")
public class RepositoryEntryMembership implements Persistable, ModifiedInfo, CreateInfo {

	private static final long serialVersionUID = -5404538852842562897L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="membership_id", nullable=false, unique=true, insertable=false, updatable=false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=false, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=false, updatable=false)
	private Date lastModified;
	@Column(name="fk_identity_id", nullable=false, unique=false, insertable=false, updatable=false)
	private Long identityKey;
	@Column(name="fk_entry_id", nullable=false, unique=false, insertable=false, updatable=false)
	private Long repoKey;
	@Column(name="g_role", nullable=false, unique=false, insertable=false, updatable=false)
	private String role;

	@Transient
	private boolean owner;
	@Transient
	private boolean coach;
	@Transient
	private boolean participant;
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Long getRepoKey() {
		return repoKey;
	}

	public void setRepoKey(Long repoKey) {
		this.repoKey = repoKey;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		if(GroupRoles.owner.name().equals(role)) {
			owner = true;
		} else if(GroupRoles.coach.name().equals(role)) {
			coach = true;
		} else if(GroupRoles.participant.name().equals(role)) {
			participant = true;
		}
		this.role = role;
	}

	public boolean isOwner() {
		return owner || GroupRoles.owner.name().equals(role);
	}

	public void setOwner(boolean owner) {
		this.owner = owner;
	}

	public boolean isCoach() {
		return coach || GroupRoles.coach.name().equals(role);
	}

	public void setCoach(boolean coach) {
		this.coach = coach;
	}

	public boolean isParticipant() {
		return participant || GroupRoles.participant.name().equals(role);
	}

	public void setParticipant(boolean participant) {
		this.participant = participant;
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
		} else if (obj instanceof RepositoryEntryMembership) {
			RepositoryEntryMembership bg = (RepositoryEntryMembership)obj;
			return getKey() != null && getKey().equals(bg.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
