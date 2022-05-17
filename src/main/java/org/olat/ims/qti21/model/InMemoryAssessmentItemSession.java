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

import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;

/**
 * 
 * Initial date: 12.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InMemoryAssessmentItemSession implements AssessmentItemSession {

	private final Long key;
	private final Date creationDate;
	private Date lastModified;
   
    private Long duration;
    private Boolean passed;
    private BigDecimal score;
    private BigDecimal manualScore;
    private Integer attempts;

    private String externalRefIdentifier;
    private final String assessmentItemIdentifier;
    private final AssessmentTestSession assessmentTestSession;

    public InMemoryAssessmentItemSession(AssessmentTestSession assessmentTestSession, String assessmentItemIdentifier) {
		key = -1l;
		creationDate = new Date();
		lastModified = creationDate;
		this.assessmentTestSession = assessmentTestSession;
		this.assessmentItemIdentifier = assessmentItemIdentifier;
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
	public String getAssessmentItemIdentifier() {
		return assessmentItemIdentifier;
	}

	@Override
	public String getSectionIdentifier() {
		return null;
	}

	@Override
	public String getTestPartIdentifier() {
		return null;
	}

	@Override
	public String getExternalRefIdentifier() {
		return  externalRefIdentifier;
	}

	public void setExternalRefIdentifier(String externalRefIdentifier) {
		this.externalRefIdentifier = externalRefIdentifier;
	}

	@Override
	public String getCoachComment() {
		return null;
	}

	@Override
	public void setCoachComment(String comment) {
		//
	}

	@Override
	public boolean isToReview() {
		return false;
	}

	@Override
	public void setToReview(boolean toReview) {
		//
	}

	@Override
	public AssessmentTestSession getAssessmentTestSession() {
		return assessmentTestSession;
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
	public Long getDuration() {
		return duration;
	}

	@Override
	public void setDuration(Long duration) {
		this.duration = duration;
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
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj);
	}
}
