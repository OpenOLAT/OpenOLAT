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

import org.olat.core.commons.persistence.PersistentObject;

/**
 * The object is immutable. It's like EfficiencyStatementGroupStatEntry
 * but with a group by to melt the different groups.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EfficiencyStatementGroupGroupedStatEntry extends PersistentObject {

	private static final long serialVersionUID = -5632894869497135693L;
	
	private Long repoKey;
	private Long statementKey;
	private Long tutorKey;
	private Long studentKey;
	
	private String repoDisplayName;
	private Long repoResourceId;
	private int repoAccess;
	private int repoStatusCode;
	
	private String tutorName;
	private String studentName;

	private Float score;
	private int passed;
	private int failed;
	private int notAttempted;
	private Date lastModified;
	
	private Long initialLaunchKey;
	
	public Long getRepoKey() {
		return repoKey;
	}
	
	public void setRepoKey(Long repoKey) {
		this.repoKey = repoKey;
	}


	public Long getStatementKey() {
		return statementKey;
	}

	public void setStatementKey(Long statementKey) {
		this.statementKey = statementKey;
	}

	public Long getTutorKey() {
		return tutorKey;
	}

	public void setTutorKey(Long tutorKey) {
		this.tutorKey = tutorKey;
	}

	public Long getStudentKey() {
		return studentKey;
	}

	public void setStudentKey(Long studentKey) {
		this.studentKey = studentKey;
	}

	public String getRepoDisplayName() {
		return repoDisplayName;
	}
	
	public void setRepoDisplayName(String repoDisplayName) {
		this.repoDisplayName = repoDisplayName;
	}
	
	public Long getRepoResourceId() {
		return repoResourceId;
	}

	public void setRepoResourceId(Long repoResourceId) {
		this.repoResourceId = repoResourceId;
	}

	public int getRepoAccess() {
		return repoAccess;
	}

	public void setRepoAccess(int repoAccess) {
		this.repoAccess = repoAccess;
	}

	public int getRepoStatusCode() {
		return repoStatusCode;
	}

	public void setRepoStatusCode(int repoStatusCode) {
		this.repoStatusCode = repoStatusCode;
	}

	public String getTutorName() {
		return tutorName;
	}
	
	public void setTutorName(String tutorName) {
		this.tutorName = tutorName;
	}
	
	public String getStudentName() {
		return studentName;
	}
	
	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}
	
	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public int getPassed() {
		return passed;
	}

	public void setPassed(int passed) {
		this.passed = passed;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(int failed) {
		this.failed = failed;
	}

	public int getNotAttempted() {
		return notAttempted;
	}

	public void setNotAttempted(int notAttempted) {
		this.notAttempted = notAttempted;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Long getInitialLaunchKey() {
		return initialLaunchKey;
	}

	public void setInitialLaunchKey(Long initialLaunchKey) {
		this.initialLaunchKey = initialLaunchKey;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("efficiencyStatementStatEntry[statementId=")
		  .append(getKey()).append(":repositoryEntry=")
		  .append(repoDisplayName).append(":tutorName=")
		  .append(tutorName).append(":studentName=")
		  .append(studentName);
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	
}
