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
package org.olat.ims.qti21.model;

import java.math.BigDecimal;
import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InMemoryAssessmentTestSession implements AssessmentTestSession {

	private final Long key;
	private final Date creationDate;
	private Date lastModified;
    private String storage;
    private Date finishTime;
    private Date terminationTime;
    private Integer extraTime;
    private Integer compensationExtraTime;
   
    private Long duration;
    private Boolean passed;
    private BigDecimal score;
    private BigDecimal manualScore;
    private BigDecimal maxScore;
	
	private Integer numOfQuestions;
	private Integer numOfAnsweredQuestions;
    
    private boolean exploded;
    private boolean cancelled;
    
    private Identity identity;
    private String anonymousIdentifier;
    
    public InMemoryAssessmentTestSession() {
		key = -1l;
		creationDate = new Date();
		lastModified = creationDate;
    }

    @Override
	public Long getKey() {
		return key;
	}

    @Override
	public Date getCreationDate() {
		return creationDate;
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
	public boolean isExploded() {
		return exploded;
	}

	@Override
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

	@Override
	public boolean isAuthorMode() {
		return false;
	}

	@Override
	public AssessmentEntry getAssessmentEntry() {
		return null;
	}

	@Override
	public RepositoryEntry getTestEntry() {
		return null;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return null;
	}

	@Override
	public String getSubIdent() {
		return null;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj);
	}
}
