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
package org.olat.basesecurity.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 18.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="bidentitylastlogin")
@Table(name="o_bs_identity")
@NamedQuery(name="updateIdentityLastLogin", query="update bidentitylastlogin set lastLogin=:now, inactivationEmailDate=null where key=:identityKey")
public class IdentityLastLoginImpl implements Persistable, IdentityRef {

	private static final long serialVersionUID = 2090002193918262648L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastlogin", nullable=false, insertable=true, updatable=true)
	private Date lastLogin;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="inactivationemaildate", nullable=false, insertable=true, updatable=true)
	private Date inactivationEmailDate;
	
	public IdentityLastLoginImpl() {
		//
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return key == null ? 3975 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof IdentityLastLoginImpl) {
			IdentityLastLoginImpl id = (IdentityLastLoginImpl)obj;
			return key != null && key.equals(id.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
