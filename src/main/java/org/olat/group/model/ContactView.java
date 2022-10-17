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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * The view list all visible owners
 * Initial date: 24.1.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity
@Table(name="o_gp_contactkey_v")
public class ContactView implements Persistable {

	private static final long serialVersionUID = 5125563005863650603L;

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

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="member_id", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	@Column(name="membership_role", nullable=false, insertable=false, updatable=false)
	private String role;

	@Column(name="me_id", nullable=false, insertable=false, updatable=false)
	private Long meKey;
	
	@Column(name="bg_id", nullable=false, insertable=false, updatable=false)
	private Long businessGroupKey;

	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Long getMeKey() {
		return meKey;
	}

	public void setMeKey(Long meKey) {
		this.meKey = meKey;
	}

	public Long getBusinessGroupKey() {
		return businessGroupKey;
	}

	public void setBusinessGroupKey(Long businessGroupKey) {
		this.businessGroupKey = businessGroupKey;
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
		if(obj instanceof ContactView) {
			ContactView msg = (ContactView)obj;
			return key != null && key.equals(msg.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
