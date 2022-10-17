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
package org.olat.course.disclaimer.model;

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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.course.disclaimer.CourseDisclaimerConsent;
import org.olat.repository.RepositoryEntry;

/* 
 * Date: 16 Mar 2020<br>
 * @author Alexander Boeckle
 */
@Entity(name = "coursedisclaimerconsent")
@Table(name = "o_course_disclaimer_consent")
public class CourseDisclaimerConsentImpl implements CourseDisclaimerConsent {
	
	private static final long serialVersionUID = 4304769402805394739L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Column(name = "disc_1_accepted", nullable = false, insertable = true, updatable = true)
	private boolean disc1Accepted; 
	
	@Column(name = "disc_2_accepted", nullable = false, insertable = true, updatable = true)
	private boolean disc2Accepted;
	
	@ManyToOne(targetEntity = RepositoryEntry.class, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_repository_entry", nullable = false, insertable = true, updatable = false)
	private RepositoryEntry repositoryEntry;
	
	@ManyToOne(targetEntity = IdentityImpl.class, optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "fk_identity", nullable = false, insertable = true, updatable = false)
	private Identity identity;
	
	@CreationTimestamp
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@UpdateTimestamp
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Override
	public Date getCreationDate() {
		return creationDate;
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
	public Long getKey() {
		return key;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return super.equals(persistable);
	}

	@Override
	public boolean isDisc1Accepted() {
		return disc1Accepted;
	}

	@Override
	public void setDisc1(boolean accepted) {
		this.disc1Accepted = accepted;
	}

	@Override
	public boolean isDisc2Accepted() {
		return disc2Accepted;
	}

	@Override
	public void setDisc2(boolean accepted) {
		this.disc2Accepted = accepted;
		
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	@Override
	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
		
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Override
	public Date getConsentDate() {
		if (disc1Accepted || disc2Accepted) {
			return lastModified;
		} else {
			return null;
		}
	}
}
