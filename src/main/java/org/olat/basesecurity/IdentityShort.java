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
package org.olat.basesecurity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;

/**
 * 
 * Description:<br>
 * This is an immutable version of identity with a limited set of fields.
 * 
 * <P>
 * Initial Date:  14 juil. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="bidentityshort")
@Table(name="o_bs_identity_short_v")
@NamedQuery(name="selectAllIdentitiesShortUnordered", query="select ident from bidentityshort as ident")
@NamedQuery(name="getIdentityShortById", query="select identity from bidentityshort as identity where identity.key=:identityKey")
@NamedQuery(name="getIdentityShortByKeys", query="select ident from bidentityshort as ident where ident.key in (:keys)")
public class IdentityShort implements Persistable, IdentityNames {

	private static final long serialVersionUID = -9039644291427632379L;
	
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
	@Column(name="id_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Column(name="us_id", nullable=true, unique=false, insertable=false, updatable=false)
	private Long userKey;

	@Column(name="id_name", nullable=true, unique=false, insertable=false, updatable=false)
	private String name;
	@Column(name="id_lastlogin", nullable=true, unique=false, insertable=false, updatable=false)
	private Date lastLogin;
	@Column(name="id_status", nullable=true, unique=false, insertable=false, updatable=false)
	private int status;
	@Column(name="id_external", nullable=true, unique=false, insertable=false, updatable=false)
	private String externalId;
	@Column(name="first_name", nullable=true, unique=false, insertable=false, updatable=false)
	private String firstName;
	@Column(name="last_name", nullable=true, unique=false, insertable=false, updatable=false)
	private String lastName;
	@Column(name="nick_name", nullable=true, unique=false, insertable=false, updatable=false)
	private String nickName;
	@Column(name="email", nullable=true, unique=false, insertable=false, updatable=false)
	private String email;

	@Override
	public Long getKey() {
		return key;
	}

	public Long getUserKey() {
		return userKey;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getExternalId() {
		return externalId;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	public String getNickName() {
		return nickName;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "IdentityShort[name=" + name + "], " + super.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 3482601 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof IdentityShort) {
			IdentityShort id = (IdentityShort)obj;
			return getKey() != null && getKey().equals(id.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
