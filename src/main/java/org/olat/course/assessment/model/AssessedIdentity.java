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
package org.olat.course.assessment.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.User;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.user.UserImpl;

/**
 * The instance is immutable
 * 
 * Initial date: 01.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="asidentity")
@Table(name="o_bs_identity")
public class AssessedIdentity implements Persistable, Identity {

	private static final long serialVersionUID = 5437094042277941568L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=false, updatable=false)
	protected Date creationDate;

	@Column(name="name", nullable=false, unique=false, insertable=false, updatable=false)
	private String name;
	@Column(name="lastlogin", nullable=false, unique=false, insertable=false, updatable=false)
	private Date lastLogin;
	@Column(name="external_id", nullable=false, unique=false, insertable=false, updatable=false)
	private String externalId;
	@Column(name="status", nullable=false, unique=false, insertable=false, updatable=false)
	private Integer status;

	@ManyToOne(targetEntity=UserImpl.class,fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_user_id", nullable=false, insertable=false, updatable=false)
	private User user;

	@OneToMany(targetEntity=AssessmentEntryImpl.class, mappedBy="identity")
	private Set<AssessmentEntry> assessmentEntries;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Date getLastLogin() {
		return lastLogin;
	}

	@Override
	public Integer getStatus() {
		return status;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public User getUser() {
		return user;
	}
	
	public Set<AssessmentEntry> getAssessmentEntries() {
		return assessmentEntries;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 34801 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssessedIdentity) {
			AssessedIdentity id = (AssessedIdentity)obj;
			return getKey() != null && getKey().equals(id.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
