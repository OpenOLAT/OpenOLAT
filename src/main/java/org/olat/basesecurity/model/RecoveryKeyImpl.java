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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.RecoveryKey;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.Encoder;

/**
 * 
 * Initial date: 11 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="recoverykey")
@Table(name="o_bs_recovery_key")
public class RecoveryKeyImpl  implements Persistable, RecoveryKey {

	private static final long serialVersionUID = -7621387649492859554L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="r_recovery_key_hash", nullable=true, insertable=true, updatable=true)
	private String recoveryKeyHash;
	@Column(name="r_recovery_salt", nullable=true, insertable=true, updatable=true)
	private String recoverySalt;
	@Column(name="r_recovery_algorithm", nullable=true, insertable=true, updatable=true)
	private String recoveryAlgorithm;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_expiration_date", nullable=true, insertable=true, updatable=true)
	private Date expirationDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="r_use_date", nullable=true, insertable=false, updatable=true)
	private Date useDate;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	
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
	public String getRecoveryKeyHash() {
		return recoveryKeyHash;
	}

	public void setRecoveryKeyHash(String hash) {
		this.recoveryKeyHash = hash;
	}

	public String getRecoverySalt() {
		return recoverySalt;
	}

	public void setRecoverySalt(String recoverySalt) {
		this.recoverySalt = recoverySalt;
	}

	public String getRecoveryAlgorithm() {
		return recoveryAlgorithm;
	}

	public void setRecoveryAlgorithm(String recoveryAlgorithm) {
		this.recoveryAlgorithm = recoveryAlgorithm;
	}

	@Override
	public boolean isSame(String key) {
		Encoder.Algorithm algorithm = Encoder.Algorithm.valueOf(getRecoveryAlgorithm());
		String salt = algorithm.isSalted() ? getRecoverySalt() : null;
		String hash = Encoder.encrypt(key, salt, algorithm);
		return getRecoveryKeyHash().equals(hash);
	}

	@Override
	public Date getUseDate() {
		return useDate;
	}

	@Override
	public void setUseDate(Date useDate) {
		this.useDate = useDate;
	}

	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 147518 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RecoveryKeyImpl rKey) {
			return getKey() != null && getKey().equals(rKey.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
