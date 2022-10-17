/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.registration;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;

/**
 *  Description:
 * 
 * 
 * @author Sabina Jeger
 */
@Entity(name="otemporarykey")
@Table(name="o_temporarykey")
@NamedQuery(name="loadTemporaryKeyByRegAction", query="select r from otemporarykey r where r.regAction=:action")
@NamedQuery(name="loadTemporaryKeyByRegKey", query="select r from otemporarykey r where r.registrationKey=:regkey")
@NamedQuery(name="loadTemporaryKeyByEmailAddress", query="select r from otemporarykey r where lower(r.emailAddress)=:email")
@NamedQuery(name="loadTemporaryKeyByIdentity", query="select r from otemporarykey r where r.identityKey=:identityKey and r.regAction=:action")
@NamedQuery(name="loadAll", query="select r from otemporarykey r")
@NamedQuery(name="deleteTemporaryKeyByIdentityAndAction", query="delete from otemporarykey r where r.identityKey=:identityKey and r.regAction=:action")
@NamedQuery(name="deleteTemporaryKeyByIdentity", query="delete from otemporarykey r where r.identityKey=:identityKey")
public class TemporaryKeyImpl implements Persistable, CreateInfo, TemporaryKey {

	private static final long serialVersionUID = 2617181963956081372L;
	
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
	@Column(name="reglist_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="email", nullable=false, unique=true, insertable=true, updatable=true)
	private String emailAddress;
	@Column(name="ip", nullable=false, unique=true, insertable=true, updatable=true)
	private String ipAddress;
	@Column(name="regkey", nullable=false, unique=true, insertable=true, updatable=true)
	private String registrationKey;
	@Column(name="valid_until", nullable=true, unique=false, insertable=true, updatable=true)
	private Date validUntil;
	@Column(name="action", nullable=false, unique=true, insertable=true, updatable=true)
	private String regAction;
	@Column(name="mailsent", nullable=false, unique=true, insertable=true, updatable=true)
	private boolean mailSent = false;
	
	@Column(name="fk_identity_id", nullable=true, unique=true, insertable=true, updatable=true)
	private Long identityKey;
	
	public TemporaryKeyImpl() {
		//
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public void setCreationDate(Date date) {
		creationDate = date;
	}

	@Override
	public String getEmailAddress() {
		return emailAddress;
	}

	@Override
	public void setEmailAddress(String string) {
		emailAddress = string;
	}

	@Override
	public String getIpAddress() {
		return ipAddress;
	}

	@Override
	public void setIpAddress(String string) {
		ipAddress = string;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getRegistrationKey() {
		return registrationKey;
	}

	@Override
	public void setRegistrationKey(String string) {
		registrationKey = string;
	}

	@Override
	public Date getValidUntil() {
		return validUntil;
	}

	@Override
	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	@Override
	public boolean isMailSent() {
		return mailSent;
	}

	@Override
	public void setMailSent(boolean b) {
		mailSent = b;
	}

	@Override
	public String getRegAction() {
		return regAction;
	}

	@Override
	public void setRegAction(String string) {
		regAction = string;
	}

	@Override
	public Long getIdentityKey() {
		return identityKey;
	}

	@Override
	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 8742558 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TemporaryKeyImpl) {
			TemporaryKeyImpl tmpKey = (TemporaryKeyImpl)obj;
			return getKey() != null && getKey().equals(tmpKey.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
