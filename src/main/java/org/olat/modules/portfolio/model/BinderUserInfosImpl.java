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
package org.olat.modules.portfolio.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderUserInformations;


@Entity(name="pfbinderuserinfos")
@Table(name="o_pf_binder_user_infos")
@NamedQuery(name="loadBinderUserInfosByBinderAndIdentity", query="select infos from pfbinderuserinfos as infos where infos.identity.key=:identityKey and infos.binder.key=:binderKey")
@NamedQuery(name="updateBinderLaunchDates", query="update pfbinderuserinfos set visit=visit+1, recentLaunch=:now, lastModified=:now where identity.key=:identityKey and binder.key=:binderKey")
public class BinderUserInfosImpl implements BinderUserInformations, Persistable {

	private static final long serialVersionUID = 3837878861740933933L;

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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_initiallaunchdate", nullable=false, insertable=true, updatable=true)
	private Date initialLaunch;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_recentlaunchdate", nullable=false, insertable=true, updatable=true)
	private Date recentLaunch;

	@Column(name="p_visit", nullable=false, insertable=true, updatable=true)
	private int visit;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=BinderImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_binder", nullable=false, updatable=false)
	private Binder binder;
	
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
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
		this.lastModified = date;
	}

	@Override
	public Date getInitialLaunch() {
		return initialLaunch;
	}
	
	public void setInitialLaunch(Date initialLaunch) {
		this.initialLaunch = initialLaunch;
	}
	
	@Override
	public Date getRecentLaunch() {
		return recentLaunch;
	}

	public void setRecentLaunch(Date recentLaunch) {
		this.recentLaunch = recentLaunch;
	}

	@Override
	public int getVisit() {
		return visit;
	}

	public void setVisit(int visit) {
		this.visit = visit;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public Binder getBinder() {
		return binder;
	}

	public void setBinder(Binder binder) {
		this.binder = binder;
	}

	@Override
	public int hashCode() {
		return key == null ? 601410 : super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BinderUserInfosImpl) {
			BinderUserInfosImpl infos = (BinderUserInfosImpl)obj;
			return key != null && key.equals(infos.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}