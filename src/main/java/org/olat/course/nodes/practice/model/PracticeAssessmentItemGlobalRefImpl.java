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
package org.olat.course.nodes.practice.model;

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
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;

/**
 * 
 * Initial date: 10 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="practiceglobalitemref")
@Table(name="o_practice_global_item_ref")
public class PracticeAssessmentItemGlobalRefImpl implements Persistable, PracticeAssessmentItemGlobalRef {
	
	private static final long serialVersionUID = 709628875168816537L;

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

	@Column(name="p_identifier", nullable=false, insertable=true, updatable=false)
	private String identifier;

	@Column(name="p_level", nullable=false, insertable=true, updatable=true)
	private int level;
	@Column(name="p_attempts", nullable=false, insertable=true, updatable=true)
	private int attempts;
	@Column(name="p_correct_answers", nullable=false, insertable=true, updatable=true)
	private int correctAnswers;
	@Column(name="p_incorrect_answers", nullable=false, insertable=true, updatable=true)
	private int incorrectAnswers;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_last_attempt_date", nullable=true, insertable=true, updatable=true)
	private Date lastAttempts;
	@Column(name="p_last_attempt_passed", nullable=true, insertable=true, updatable=true)
	private Boolean lastAttemptsPassed;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
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
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public Date getLastAttempts() {
		return lastAttempts;
	}

	@Override
	public void setLastAttempts(Date lastAttempts) {
		this.lastAttempts = lastAttempts;
	}

	@Override
	public Boolean getLastAttemptsPassed() {
		return lastAttemptsPassed;
	}

	@Override
	public void setLastAttemptsPassed(Boolean lastAttemptsPassed) {
		this.lastAttemptsPassed = lastAttemptsPassed;
	}

	@Override
	public int getAttempts() {
		return attempts;
	}

	@Override
	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	@Override
	public int getCorrectAnswers() {
		return correctAnswers;
	}

	@Override
	public void setCorrectAnswers(int correctAnswers) {
		this.correctAnswers = correctAnswers;
	}

	@Override
	public int getIncorrectAnswers() {
		return incorrectAnswers;
	}

	@Override
	public void setIncorrectAnswers(int incorrectAnswers) {
		this.incorrectAnswers = incorrectAnswers;
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
		return key == null ? 458932 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof PracticeAssessmentItemGlobalRefImpl) {
			PracticeAssessmentItemGlobalRefImpl ref = (PracticeAssessmentItemGlobalRefImpl)obj;
			return getKey() != null && getKey().equals(ref.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
