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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * Initial date: 28.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="coachstatisticsidentity")
@Table(name="o_as_eff_statement_identity_v")
public class EfficiencyStatementIdentityStatEntry {
	
	@Id
	@Column(name="st_id", nullable=false, unique=true, insertable=false, updatable=false)
	private Long statementKey;

	@Column(name="re_id", nullable=false, unique=false, insertable=false, updatable=false)
	private Long repoKey;
	@Column(name="student_id", nullable=false, unique=false, insertable=false, updatable=false)
	private Long studentKey;

	@Column(name="st_score", nullable=false, unique=false, insertable=false, updatable=false)
	private Float score;
	@Column(name="st_passed", nullable=false, unique=false, insertable=false, updatable=false)
	private int passed;
	@Column(name="st_failed", nullable=false, unique=false, insertable=false, updatable=false)
	private int failed;
	@Column(name="st_not_attempted", nullable=false, unique=false, insertable=false, updatable=false)
	private int notAttempted;

	@Column(name="pg_id", nullable=false, unique=false, insertable=false, updatable=false)
	private Long initialLaunchKey;

	public Long getStatementKey() {
		return statementKey;
	}

	public void setStatementKey(Long statementKey) {
		this.statementKey = statementKey;
	}

	public Long getRepoKey() {
		return repoKey;
	}

	public void setRepoKey(Long repoKey) {
		this.repoKey = repoKey;
	}

	public Long getStudentKey() {
		return studentKey;
	}

	public void setStudentKey(Long studentKey) {
		this.studentKey = studentKey;
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

	public Long getInitialLaunchKey() {
		return initialLaunchKey;
	}

	public void setInitialLaunchKey(Long initialLaunchKey) {
		this.initialLaunchKey = initialLaunchKey;
	}
}
