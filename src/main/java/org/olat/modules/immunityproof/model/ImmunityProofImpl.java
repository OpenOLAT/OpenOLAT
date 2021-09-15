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
package org.olat.modules.immunityproof.model;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.immunityproof.ImmunityProof;

/**
 * Initial date: 08.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Entity(name = "immunityProof")
@Table(name = "o_immunity_proof")
public class ImmunityProofImpl implements ImmunityProof {
	
	private static final long serialVersionUID = 548721632485749985L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
	private Date creationDate;
	
	@ManyToOne(targetEntity = IdentityImpl.class, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="fk_user", nullable=false, insertable=true, updatable=false)
    private Identity identity;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "safedate", nullable = false, insertable = true, updatable = false)
	private Date safeDate;
	
	@Column(name = "validated", nullable = false, insertable = true, updatable = false)
	private boolean validated;
	
	@Column(name = "send_mail", nullable = false, insertable = true, updatable = false)
	private boolean sendMail;
	
	@Column(name = "email_sent", nullable = false, insertable = true, updatable = true)
	private boolean mailSent;
	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public Date getSafeDate() {
		return safeDate;
	}
	
	public void setSafeDate(Date safeDate) {
		this.safeDate = safeDate;
	}

	@Override
	public boolean isValidated() {
		return validated;
	}
	
	public void setValidated(boolean validated) {
		this.validated = validated;
	}
	
	@Override
	public boolean isMailSent() {
		return mailSent;
	}
	
	public void setMailSent(boolean mailSent) {
		this.mailSent = mailSent;
	}
	
	@Override
	public boolean isSendMail() {
		return sendMail;
	}
	
	public void setSendMail(boolean sendMail) {
		this.sendMail = sendMail;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		
		if (obj instanceof ImmunityProof) {
			return getKey() != null && getKey().equals(((ImmunityProof) obj).getKey());
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

}
