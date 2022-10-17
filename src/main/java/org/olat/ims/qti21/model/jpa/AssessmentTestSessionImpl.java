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

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.repository.RepositoryEntry;

/**
 * This a custom implementation of CandidateSession
 * 
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qtiassessmenttestsession")
@Table(name="o_qti_assessmenttest_session")
public class AssessmentTestSessionImpl implements AssessmentTestSession, Persistable {

	private static final long serialVersionUID = -6069133323360142500L;

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
	
	@ManyToOne(targetEntity=AssessmentEntryImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_assessment_entry", nullable=false, insertable=true, updatable=false)
    private AssessmentEntry assessmentEntry;

	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_reference_entry", nullable=false, insertable=true, updatable=false)
    private RepositoryEntry testEntry;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_entry", nullable=true, insertable=true, updatable=false)
    private RepositoryEntry repositoryEntry;

    @Column(name="q_subident", nullable=true, insertable=true, updatable=false)
	private String subIdent;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity", nullable=true, insertable=true, updatable=false)
    private Identity identity;
	
    @Column(name="q_anon_identifier", nullable=true, insertable=true, updatable=false)
	private String anonymousIdentifier;

    /** Is this session running in author mode? (I.e. providing debugging information) */
    @Column(name="q_author_mode", nullable=false, insertable=true, updatable=true)
    private boolean authorMode;
    
    @Column(name="q_storage", nullable=false, insertable=true, updatable=false)
    private String storage;

    /**
     * Timestamp indicating when the session has been <strong>finished</strong>.
     * This is a QTIWorks specific concept with the following meaning:
     * <ul>
     *   <li>
     *     A test is marked as finished once the candidate gets to the end of the last
     *     enterable testPart. At this time, the outcome variables are finalised and will
     *     be sent back to the LTI TC (if appropriate). A test only finishes once.
     *   </li>
     *   <li>
     *     A standalone item is marked as finished once the item session ends. At this time,
     *     the outcome variables are sent back to the LTI TC (if appropriate). These variables
     *     are normally final, but it is currently possible for items to reopen. The session can
     *     finish again in this case.
     *   </li>
     * </ul>
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="q_finish_time", nullable=true, insertable=true, updatable=true)
    private Date finishTime;

    /**
     * Timestamp indicating when the session has been terminated. Session termination can
     * occur in two ways:
     * <ul>
     *   <li>When the candidate naturally exits the session</li>
     *   <li>When the instructor explicitly terminates the session</li>
     * </ul>
     * Once terminated, a session is no longer available to the candidate.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="q_termination_time", nullable=true, insertable=true, updatable=true)
    private Date terminationTime;
    
    @Column(name="q_duration", nullable=true, insertable=true, updatable=true)
    private Long duration;
    
    @Column(name="q_passed", nullable=true, insertable=true, updatable=true)
    private Boolean passed;

    @Column(name="q_score", nullable=true, insertable=true, updatable=true)
    private BigDecimal score; 
    @Column(name="q_manual_score", nullable=true, insertable=true, updatable=true)
    private BigDecimal manualScore;
    @Column(name="q_max_score", nullable=true, insertable=true, updatable=true)
    private BigDecimal maxScore; 

    @Column(name="q_num_questions", nullable=true, insertable=true, updatable=true)
    private Integer numOfQuestions;
    @Column(name="q_num_answered_questions", nullable=true, insertable=true, updatable=true)
    private Integer numOfAnsweredQuestions;
    
    /**
     * Can only be updated via the service
     */
    @Column(name="q_extra_time", nullable=true, insertable=false, updatable=false)
    private Integer extraTime;
    /**
     * Can only be inserted, after need to be updated via the service
     */
    @Column(name="q_compensation_extra_time", nullable=true, insertable=true, updatable=false)
    private Integer compensationExtraTime;

    /**
     * Flag to indicate if this session blew up while running, either because
     * the assessment was not runnable, or because of a logic error.
     */
    @Column(name="q_exploded", nullable=false, insertable=true, updatable=true)
    private boolean exploded;
    
    /**
     * Flag to indicate if this session was cancelled and will be ignored in
     * assessment but the results are available.
     */
    @Column(name="q_cancelled", nullable=false, insertable=true, updatable=true)
    private boolean cancelled;
    

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
	public AssessmentEntry getAssessmentEntry() {
		return assessmentEntry;
	}

	public void setAssessmentEntry(AssessmentEntry assessmentEntry) {
		this.assessmentEntry = assessmentEntry;
	}

	@Override
	public RepositoryEntry getTestEntry() {
		return testEntry;
	}

	public void setTestEntry(RepositoryEntry testEntry) {
		this.testEntry = testEntry;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public String getAnonymousIdentifier() {
		return anonymousIdentifier;
	}

	public void setAnonymousIdentifier(String identifier) {
		this.anonymousIdentifier = identifier;
	}

	@Override
	public boolean isAuthorMode() {
		return authorMode;
	}

	public void setAuthorMode(boolean authorMode) {
		this.authorMode = authorMode;
	}

	@Override
	public boolean isExploded() {
		return exploded;
	}

	public void setExploded(boolean exploded) {
		this.exploded = exploded;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	@Override
	public Date getFinishTime() {
		return finishTime;
	}

	@Override
	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	@Override
	public Date getTerminationTime() {
		return terminationTime;
	}

	@Override
	public void setTerminationTime(Date terminationTime) {
		this.terminationTime = terminationTime;
	}

	@Override
	public Integer getExtraTime() {
		return extraTime;
	}

	public void setExtraTime(Integer extraTime) {
		this.extraTime = extraTime;
	}

	@Override
	public Integer getCompensationExtraTime() {
		return compensationExtraTime;
	}

	public void setCompensationExtraTime(Integer compensationExtraTime) {
		this.compensationExtraTime = compensationExtraTime;
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
	public BigDecimal getMaxScore() {
		return maxScore;
	}

	@Override
	public void setMaxScore(BigDecimal maxScore) {
		this.maxScore = maxScore;
	}

	@Override
	public Integer getNumOfQuestions() {
		return numOfQuestions;
	}

	@Override
	public void setNumOfQuestions(Integer numOfQuestions) {
		this.numOfQuestions = numOfQuestions;
	}

	@Override
	public Integer getNumOfAnsweredQuestions() {
		return numOfAnsweredQuestions;
	}

	@Override
	public void setNumOfAnsweredQuestions(Integer numOfAnsweredQuestions) {
		this.numOfAnsweredQuestions = numOfAnsweredQuestions;
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
	public int hashCode() {
		return key == null ? -86534687 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AssessmentTestSessionImpl) {
			AssessmentTestSessionImpl session = (AssessmentTestSessionImpl)obj;
			return getKey() != null && getKey().equals(session.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
