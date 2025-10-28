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
package org.olat.restapi.support.vo;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

/**
 * Value object representing an individual user's results on an
 * {@link org.olat.course.nodes.AssessableCourseNode}.
 * 
 * @author federico, srosse, stephane.rosse@frentix.com
 * @version 1.0
 * @since Feb 24, 2010
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "assessableResultsVO")
public class AssessableResultsVO {
	
	private Long identityKey;
	private String identityExternalId;
	private String nodeIdent;

	private Boolean passed;
	
	private Float score;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The weighted score will be calculated from the score")
	private Float weightedScore;
	private Float maxScore;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The weighted max. score will be calculated from the score")
	private Float weightedMaxScore;
	
	private Boolean userVisible;

	@Schema(accessMode = AccessMode.AUTO, description = "The grade will be calculated from the score if course element is in auto mode")
	private String grade;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The class will be calculated from the score")
	private String performanceClassIdent;
	
	private Double completion;
	private Integer attempts;
	private String assessmentStatus;

	@Schema(accessMode = AccessMode.READ_ONLY, description = "The date will be automatically updated")
	private Date lastModifiedDate;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The date will be automatically updated")
	private Date lastUserModified;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The date will be automatically updated")
	private Date lastCoachModified;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The date will be automatically updated")
	private Date assessmentDone;
	private Boolean fullyAssessed;
	private Date fullyAssessedDate;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The date will be automatically updated")
	private Date firstVisit;
	@Schema(accessMode = AccessMode.READ_ONLY, description = "The date will be automatically updated")
	private Date lastVisit;

	public AssessableResultsVO() {
	//make jaxb happy
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public String getIdentityExternalId() {
		return identityExternalId;
	}

	public void setIdentityExternalId(String identityExternalId) {
		this.identityExternalId = identityExternalId;
	}

	public String getNodeIdent() {
		return nodeIdent;
	}

	public void setNodeIdent(String nodeIdent) {
		this.nodeIdent = nodeIdent;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}
	
	public Float getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Float maxScore) {
		this.maxScore = maxScore;
	}

	public Float getWeightedScore() {
		return weightedScore;
	}

	public void setWeightedScore(Float weightedScore) {
		this.weightedScore = weightedScore;
	}

	public Float getWeightedMaxScore() {
		return weightedMaxScore;
	}

	public void setWeightedMaxScore(Float weightedMaxScore) {
		this.weightedMaxScore = weightedMaxScore;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getPerformanceClassIdent() {
		return performanceClassIdent;
	}

	public void setPerformanceClassIdent(String performanceClassIdent) {
		this.performanceClassIdent = performanceClassIdent;
	}

	public Double getCompletion() {
		return completion;
	}

	public void setCompletion(Double completion) {
		this.completion = completion;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}

	public String getAssessmentStatus() {
		return assessmentStatus;
	}

	public void setAssessmentStatus(String assessmentStatus) {
		this.assessmentStatus = assessmentStatus;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Date getLastUserModified() {
		return lastUserModified;
	}

	public void setLastUserModified(Date lastUserModified) {
		this.lastUserModified = lastUserModified;
	}

	public Date getLastCoachModified() {
		return lastCoachModified;
	}

	public void setLastCoachModified(Date lastCoachModified) {
		this.lastCoachModified = lastCoachModified;
	}

	public Date getAssessmentDone() {
		return assessmentDone;
	}

	public void setAssessmentDone(Date assessmentDone) {
		this.assessmentDone = assessmentDone;
	}

	public Boolean getFullyAssessed() {
		return fullyAssessed;
	}

	public void setFullyAssessed(Boolean fullyAssessed) {
		this.fullyAssessed = fullyAssessed;
	}

	public Date getFullyAssessedDate() {
		return fullyAssessedDate;
	}

	public void setFullyAssessedDate(Date fullyAssessedDate) {
		this.fullyAssessedDate = fullyAssessedDate;
	}

	public Date getFirstVisit() {
		return firstVisit;
	}

	public void setFirstVisit(Date firstVisit) {
		this.firstVisit = firstVisit;
	}

	public Date getLastVisit() {
		return lastVisit;
	}

	public void setLastVisit(Date lastVisit) {
		this.lastVisit = lastVisit;
	}

	public Boolean getUserVisible() {
		return userVisible;
	}

	public void setUserVisible(Boolean userVisible) {
		this.userVisible = userVisible;
	}
}
