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
package org.olat.ims.qti21.model.jpa;

import java.math.BigDecimal;
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

import org.olat.core.id.Persistable;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;

/**
 * 
 * Initial date: 02.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qtiassessmentitemsession")
@Table(name="o_qti_assessmentitem_session")
public class AssessmentItemSessionImpl implements AssessmentItemSession, Persistable {

	private static final long serialVersionUID = 404608933232435117L;

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
	
    @Column(name="q_itemidentifier", nullable=false, insertable=true, updatable=false)
    private String assessmentItemIdentifier;
    
    @Column(name="q_sectionidentifier", nullable=true, insertable=true, updatable=false)
    private String sectionIdentifier;
    @Column(name="q_testpartidentifier", nullable=true, insertable=true, updatable=false)
    private String testPartIdentifier;
    
    @Column(name="q_externalrefidentifier", nullable=true, insertable=true, updatable=false)
    private String externalRefIdentifier;
    
    @Column(name="q_duration", nullable=true, insertable=true, updatable=true)
    private Long duration;
    @Column(name="q_passed", nullable=true, insertable=true, updatable=true)
    private Boolean passed;
    @Column(name="q_score", nullable=true, insertable=true, updatable=true)
    private BigDecimal score;
    @Column(name="q_attempts", nullable=true, insertable=true, updatable=true)
    private Integer attempts;
    
    @Column(name="q_manual_score", nullable=true, insertable=true, updatable=true)
    private BigDecimal manualScore;
    @Column(name="q_to_review", nullable=true, insertable=true, updatable=true)
    private boolean toReview;
    @Column(name="q_coach_comment", nullable=true, insertable=true, updatable=true)
    private String coachComment;
    
	@ManyToOne(targetEntity=AssessmentTestSessionImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_assessmenttest_session", nullable=false, insertable=true, updatable=false)
	private AssessmentTestSession assessmentTestSession;
    

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
	public Long getDuration() {
		return duration;
	}

	@Override
	public void setDuration(Long duration) {
		this.duration = duration;
	}

	@Override
	public Boolean getPassed() {
		return passed;
	}

	@Override
	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	@Override
	public BigDecimal getScore() {
		return score;
	}

	@Override
	public void setScore(BigDecimal score) {
		this.score = score;
	}

	@Override
	public BigDecimal getManualScore() {
		return manualScore;
	}

	@Override
	public void setManualScore(BigDecimal manualScore) {
		this.manualScore = manualScore;
	}

	@Override
	public Integer getAttempts() {
		return attempts;
	}

	@Override
	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}

	@Override
	public String getCoachComment() {
		return coachComment;
	}

	@Override
	public void setCoachComment(String coachComment) {
		this.coachComment = coachComment;
	}

	@Override
	public boolean isToReview() {
		return toReview;
	}

	@Override
	public void setToReview(boolean toReview) {
		this.toReview = toReview;
	}

	@Override
	public String getAssessmentItemIdentifier() {
		return assessmentItemIdentifier;
	}

	public void setAssessmentItemIdentifier(String assessmentItemIdentifier) {
		this.assessmentItemIdentifier = assessmentItemIdentifier;
	}

	@Override
	public String getExternalRefIdentifier() {
		return externalRefIdentifier;
	}

	public void setExternalRefIdentifier(String externalRefIdentifier) {
		this.externalRefIdentifier = externalRefIdentifier;
	}

	@Override
	public String getSectionIdentifier() {
		return sectionIdentifier;
	}

	public void setSectionIdentifier(String sectionIdentifier) {
		this.sectionIdentifier = sectionIdentifier;
	}

	@Override
	public String getTestPartIdentifier() {
		return testPartIdentifier;
	}

	public void setTestPartIdentifier(String testPartIdentifier) {
		this.testPartIdentifier = testPartIdentifier;
	}

	@Override
	public AssessmentTestSession getAssessmentTestSession() {
		return assessmentTestSession;
	}

	public void setAssessmentTestSession(AssessmentTestSession assessmentTestSession) {
		this.assessmentTestSession = assessmentTestSession;
	}
	
	@Override
	public int hashCode() {
		return key == null ? -86534687 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssessmentItemSessionImpl) {
			AssessmentItemSessionImpl session = (AssessmentItemSessionImpl)obj;
			return getKey() != null && getKey().equals(session.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}