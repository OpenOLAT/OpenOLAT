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
package org.olat.user.model;

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
import org.olat.user.AbsenceLeave;

/**
 * 
 * Initial date: 14 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="userabsenceleave")
@Table(name="o_user_absence_leave")
public class AbsenceLeaveImpl implements AbsenceLeave, Persistable {

	private static final long serialVersionUID = 2209932091989638214L;

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
	@Column(name="u_absent_from", nullable=true, insertable=true, updatable=true)
	private Date absentFrom;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="u_absent_to", nullable=true, insertable=true, updatable=true)
	private Date absentTo;

	@Column(name="u_resname", nullable=true, insertable=true, updatable=false)
	private String resName;
	@Column(name="u_resid", nullable=true, insertable=true, updatable=false)
	private Long resId;
	@Column(name="u_sub_ident", nullable=true, insertable=true, updatable=false)
	private String subIdent;
	
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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Override
	public Date getAbsentFrom() {
		return absentFrom;
	}
	@Override
	public void setAbsentFrom(Date from) {
		absentFrom = from;
	}
	@Override
	public Date getAbsentTo() {
		return absentTo;
	}
	@Override
	public void setAbsentTo(Date to) {
		absentTo = to;
	}

	@Override
	public String getResName() {
		return resName;
	}
	
	public void setResName(String resName) {
		this.resName = resName;
	}

	@Override
	public Long getResId() {
		return resId;
	}
	
	public void setResId(Long resId) {
		this.resId = resId;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}
	
	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 623159867 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AbsenceLeaveImpl) {
			AbsenceLeaveImpl leave = (AbsenceLeaveImpl)obj;
			return getKey() != null && getKey().equals(leave.getKey());
		}
		return super.equals(obj);
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
