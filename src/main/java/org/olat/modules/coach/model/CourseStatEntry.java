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
package org.olat.modules.coach.model;

import java.util.Date;

import org.olat.modules.coach.model.ParticipantStatisticsEntry.Certificates;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.SuccessStatus;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 *  Dummy bean to transport statistic values about course
 *  
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseStatEntry {
	
	private static final Certificates NO_CERTIFICATES = new Certificates(0l, 0l, 0l);
	private static final SuccessStatus NO_SUCCESS = new SuccessStatus(0l, 0l, 0l, 0l);
	
	// s.repoKey, 
	private Long repoKey;
	private String repoDisplayName;
	private String repoExternalId;
	private String repoExternalRef;
	private String repoTechnicalType;
	private String repoTeaser;
	private String repoLocation;
	private String repoAuthors;
	private RepositoryEntryStatusEnum status;
	private Date lifecycleStartDate;
	private Date lifecycleEndDate;
	
	private Long educationalTypeKey;
	
	private Long resourceId;
	
	private Double averageRating;
	
	private int participants;
	private int participantsVisited;
	private int participantsNotVisited;
	private Date lastVisit;
	
	private Certificates certificates = NO_CERTIFICATES;

	private double totalScore;
	private Double averageScore;
	private int totalScoredStudents;
	private Double averageCompletion;
	private SuccessStatus successStatus = NO_SUCCESS;
	
	public CourseStatEntry() {
		//
	}
	
	public double getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(double totalScore) {
		this.totalScore = totalScore;
	}

	public int getTotalScoredStudents() {
		return totalScoredStudents;
	}

	public void setTotalScoredStudents(int totalScoredStudents) {
		this.totalScoredStudents = totalScoredStudents;
	}

	public Long getRepoKey() {
		return repoKey;
	}
	
	public void setRepoKey(Long repoKey) {
		this.repoKey = repoKey;
	}
	
	public String getRepoDisplayName() {
		return repoDisplayName;
	}
	
	public void setRepoDisplayName(String repoDisplayName) {
		this.repoDisplayName = repoDisplayName;
	}

	public String getRepoTechnicalType() {
		return repoTechnicalType;
	}

	public void setRepoTechnicalType(String repoTechnicalType) {
		this.repoTechnicalType = repoTechnicalType;
	}

	public Long getEducationalTypeKey() {
		return educationalTypeKey;
	}

	public void setEducationalTypeKey(Long educationalTypeKey) {
		this.educationalTypeKey = educationalTypeKey;
	}

	public String getRepoExternalId() {
		return repoExternalId;
	}

	public void setRepoExternalId(String repoExternalId) {
		this.repoExternalId = repoExternalId;
	}

	public String getRepoExternalRef() {
		return repoExternalRef;
	}

	public void setRepoExternalRef(String repoExternalRef) {
		this.repoExternalRef = repoExternalRef;
	}

	public RepositoryEntryStatusEnum getRepoStatus() {
		return status;
	}

	public void setRepoStatus(RepositoryEntryStatusEnum status) {
		this.status = status;
	}

	public String getRepoTeaser() {
		return repoTeaser;
	}

	public void setRepoTeaser(String repoTeaser) {
		this.repoTeaser = repoTeaser;
	}

	public String getRepoLocation() {
		return repoLocation;
	}

	public void setRepoLocation(String repoLocation) {
		this.repoLocation = repoLocation;
	}

	public String getRepoAuthors() {
		return repoAuthors;
	}

	public void setRepoAuthors(String repoAuthors) {
		this.repoAuthors = repoAuthors;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public Date getLifecycleStartDate() {
		return lifecycleStartDate;
	}

	public void setLifecycleStartDate(Date lifecycleStartDate) {
		this.lifecycleStartDate = lifecycleStartDate;
	}

	public Date getLifecycleEndDate() {
		return lifecycleEndDate;
	}

	public void setLifecycleEndDate(Date lifecycleEndDate) {
		this.lifecycleEndDate = lifecycleEndDate;
	}

	public int getParticipants() {
		return participants;
	}

	public void setParticipants(int participants) {
		this.participants = participants;
	}

	public int getParticipantsVisited() {
		return participantsVisited;
	}

	public void setParticipantsVisited(int participantsVisited) {
		this.participantsVisited = participantsVisited;
	}

	public int getParticipantsNotVisited() {
		return participantsNotVisited;
	}

	public void setParticipantsNotVisited(int participantsNotVisited) {
		this.participantsNotVisited = participantsNotVisited;
	}

	public Date getLastVisit() {
		return lastVisit;
	}

	public void setLastVisit(Date lastVisit) {
		this.lastVisit = lastVisit;
	}

	public SuccessStatus getSuccessStatus() {
		return successStatus;
	}
	
	public void setSuccessStatus(SuccessStatus successStatus) {
		this.successStatus = successStatus;
	}

	public Double getAverageScore() {
		return averageScore;
	}
	
	public void setAverageScore(Double averageScore) {
		this.averageScore = averageScore;
	}

	public Double getAverageCompletion() {
		return averageCompletion;
	}

	public void setAverageCompletion(Double averageCompletion) {
		this.averageCompletion = averageCompletion;
	}

	public Certificates getCertificates() {
		return certificates;
	}

	public void setCertificates(Certificates certificates) {
		if(certificates == null) {
			this.certificates = NO_CERTIFICATES;
		} else {
			this.certificates = certificates;
		}
	}

	public Double getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(Double averageRating) {
		this.averageRating = averageRating;
	}
}
