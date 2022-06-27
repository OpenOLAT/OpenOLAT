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

import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 *  Dummy bean to transport statistic values about course
 *  
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseStatEntry {
	// s.repoKey, 
	private Long repoKey;
	private String repoDisplayName;
	private String repoExternalId;
	private String repoExternalRef;
	private RepositoryEntryStatusEnum status;
	private int countStudents;
	private int countPassed;
	private int countFailed;
	private int countNotAttempted;
	private Float averageScore;
	private int initialLaunch;
	private Double averageCompletion;
	
	private double totalScore;
	private int totalScoredStudents;
	
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

	public int getCountStudents() {
		return countStudents;
	}
	
	public void setCountStudents(int countStudents) {
		this.countStudents = countStudents;
	}

	public int getCountPassed() {
		return countPassed;
	}
	
	public void setCountPassed(int countPassed) {
		this.countPassed = countPassed;
	}
	
	public int getCountFailed() {
		return countFailed;
	}
	
	public void setCountFailed(int countFailed) {
		this.countFailed = countFailed;
	}
	
	public int getCountNotAttempted() {
		return countNotAttempted;
	}
	
	public void setCountNotAttempted(int countNotAttempted) {
		this.countNotAttempted = countNotAttempted;
	}
	
	public Float getAverageScore() {
		return averageScore;
	}
	
	public void setAverageScore(Float averageScore) {
		this.averageScore = averageScore;
	}

	public int getInitialLaunch() {
		return initialLaunch;
	}

	public void setInitialLaunch(int initialLaunch) {
		this.initialLaunch = initialLaunch;
	}

	public Double getAverageCompletion() {
		return averageCompletion;
	}

	public void setAverageCompletion(Double averageCompletion) {
		this.averageCompletion = averageCompletion;
	}
}
