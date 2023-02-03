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
package org.olat.modules.video.model;

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
import jakarta.persistence.Transient;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.video.VideoTaskSession;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="videotasksession")
@Table(name="o_vid_task_session")
public class VideoTaskSessionImpl implements VideoTaskSession, Persistable {
	
	private static final long serialVersionUID = -9016122208389649903L;

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
    private RepositoryEntry videoEntry;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_entry", nullable=true, insertable=true, updatable=false)
    private RepositoryEntry repositoryEntry;

    @Column(name="v_subident", nullable=true, insertable=true, updatable=false)
	private String subIdent;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity", nullable=true, insertable=true, updatable=false)
    private Identity identity;
	
    @Column(name="v_anon_identifier", nullable=true, insertable=true, updatable=false)
	private String anonymousIdentifier;
    
    /** Is this session running in author mode? (I.e. providing debugging information) */
    @Column(name="v_author_mode", nullable=false, insertable=true, updatable=true)
    private boolean authorMode;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="v_finish_time", nullable=true, insertable=true, updatable=true)
    private Date finishTime;
    
    @Column(name="v_passed", nullable=true, insertable=true, updatable=true)
    private Boolean passed;
    
    @Column(name="v_score", nullable=true, insertable=true, updatable=true)
    private BigDecimal score;
    @Column(name="v_max_score", nullable=true, insertable=true, updatable=true)
    private BigDecimal maxScore;

    @Column(name="v_result", nullable=true, insertable=true, updatable=true)
    private BigDecimal result;
    @Column(name="v_segments", nullable=true, insertable=true, updatable=true)
    private int segments;
    
    @Column(name="v_attempt", nullable=true, insertable=true, updatable=true)
    private long attempt;
    
    /**
     * Flag to indicate if this session was cancelled and will be ignored in
     * assessment but the results are available.
     */
    @Column(name="v_cancelled", nullable=false, insertable=true, updatable=true)
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
	public boolean isAuthorMode() {
		return authorMode;
	}

	public void setAuthorMode(boolean authorMode) {
		this.authorMode = authorMode;
	}

	@Override
	public RepositoryEntry getVideoEntry() {
		return videoEntry;
	}

	public void setVideoEntry(RepositoryEntry videoEntry) {
		this.videoEntry = videoEntry;
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
	public AssessmentEntry getAssessmentEntry() {
		return assessmentEntry;
	}

	public void setAssessmentEntry(AssessmentEntry assessmentEntry) {
		this.assessmentEntry = assessmentEntry;
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

	public void setAnonymousIdentifier(String anonymousIdentifier) {
		this.anonymousIdentifier = anonymousIdentifier;
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
	@Transient
	public long getDuration() {
		if(getFinishTime() == null) {
			return -1;
		}
		return getFinishTime().getTime() - getCreationDate().getTime();
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
	public BigDecimal getMaxScore() {
		return maxScore;
	}

	@Override
	public void setMaxScore(BigDecimal maxScore) {
		this.maxScore = maxScore;
	}

	@Override
	public BigDecimal getResult() {
		return result;
	}

	@Override
	public void setResult(BigDecimal result) {
		this.result = result;
	}

	@Override
	public BigDecimal getResultInPercent() {
		return result == null ? null : result.multiply(BigDecimal.valueOf(100l));
	}

	@Override
	public int getSegments() {
		return segments;
	}

	@Override
	public void setSegments(int segments) {
		this.segments = segments;
	}

	@Override
	public long getAttempt() {
		return attempt;
	}

	public void setAttempt(long attempt) {
		this.attempt = attempt;
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
	public int hashCode() {
		return key == null ? -48927 : key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof VideoTaskSessionImpl session) {
			return getKey() != null && getKey().equals(session.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
