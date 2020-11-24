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
package org.olat.resource.accesscontrol.provider.auto.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.resource.accesscontrol.model.AbstractAccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;

/**
 *
 * Initial date: 14.08.2017<br>
 *
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="advanceOrder")
@Table(name="o_ac_auto_advance_order")
@NamedQueries({
	@NamedQuery(name="exists", query=
			  "select count(*) "
			+ "from advanceOrder ao "
			+ "where ao.identity.key =:identityKey "
			+ "and ao.identifierKey =:identifierKey "
			+ "and ao.identifierValue =:identifierValue "
			+ "and ao.method.key =:methodKey"),
	@NamedQuery(name="deleteByKey", query =
			  "delete from advanceOrder ao where ao.key=:key"),
	@NamedQuery(name="deleteByIdentity", query =
			  "delete from advanceOrder ao"
			+ " where ao.identity.key=:identityKey")
})
public class AdvanceOrderImpl implements Persistable, AdvanceOrder {

	private static final long serialVersionUID = -536425559285612562L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Enumerated(EnumType.STRING)
	@Column(name="a_identifier_key", nullable=false, insertable=true, updatable=false)
	private IdentifierKey identifierKey;
	@Column(name="a_identifier_value", nullable=false, insertable=true, updatable=false)
	private String identifierValue;

	@Enumerated(EnumType.STRING)
	@Column(name="a_status", nullable=false, insertable=true, updatable=true)
	private Status status;
	@Column(name="a_status_modified", nullable=false, insertable=true, updatable=true)
	private Date statusModified;

	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Identity identity;

	@ManyToOne(targetEntity=AbstractAccessMethod.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_method", nullable=false, insertable=true, updatable=false)
	private AccessMethod method;

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
	public void setLastModified(Date date) {
		lastModified = date;
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public AccessMethod getMethod() {
		return method;
	}

	public void setMethod(AccessMethod method) {
		this.method = method;
	}

	@Override
	public IdentifierKey getIdentifierKey() {
		return identifierKey;
	}

	public void setIdentifierKey(IdentifierKey key) {
		this.identifierKey = key;
	}

	@Override
	public String getIdentifierValue() {
		return identifierValue;
	}

	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
		setStatusModified(new Date());
	}

	@Override
	public Date getStatusModified() {
		return statusModified;
	}

	public void setStatusModified(Date statusModified) {
		this.statusModified = statusModified;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifierKey == null) ? 0 : identifierKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if(obj instanceof AdvanceOrderImpl) {
			AdvanceOrderImpl other = (AdvanceOrderImpl)obj;
			return getKey() != null && getKey().equals(other.getKey());
		}
		return false;
	}

}
