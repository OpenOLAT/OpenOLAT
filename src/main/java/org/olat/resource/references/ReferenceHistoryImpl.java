/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.references;

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
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;


/**
 * 
 * Initial date: 2 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="referenceshistory")
@Table(name="o_references_history")
public class ReferenceHistoryImpl implements Persistable, ReferenceHistory {

	private static final long serialVersionUID = -6861263748211168112L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_source", nullable=false, insertable=true, updatable=false)
	private OLATResource source;
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_target", nullable=false, insertable=true, updatable=false)
	private OLATResource target;
	@Column(name="userdata", nullable=true, insertable=true, updatable=false)
	private String userdata;
	
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
	public OLATResource getSource() {
		return source;
	}

	public void setSource(OLATResource source) {
		this.source = source;
	}

	@Override
	public OLATResource getTarget() {
		return target;
	}

	public void setTarget(OLATResource target) {
		this.target = target;
	}

	@Override
	public String getUserdata() {
		return userdata;
	}

	public void setUserdata(String userdata) {
		this.userdata = userdata;
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
		return getKey() == null ? -37463223 : getKey().hashCode();
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ReferenceHistoryImpl ref) {
			return getKey() != null && getKey().equals(ref.getKey());	
		}
		return false;
	}
}