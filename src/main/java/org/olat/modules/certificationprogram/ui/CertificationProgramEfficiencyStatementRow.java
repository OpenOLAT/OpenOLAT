/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.math.BigDecimal;

import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 11 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramEfficiencyStatementRow {
	
	private final AssessmentEntry assessmentEntry;
	private final RepositoryEntry repositoryEntry;
	
	public CertificationProgramEfficiencyStatementRow(AssessmentEntry assessmentEntry) {
		this.assessmentEntry = assessmentEntry;
		repositoryEntry = assessmentEntry.getRepositoryEntry();
	}
	
	public Long getRepositoryEntryKey() {
		return repositoryEntry.getKey();
	}
	
	public String getRepositoryEntryDisplayname() {
		return repositoryEntry.getDisplayname();
	}
	
	public String getRepositoryEntryExternalRef() {
		return repositoryEntry.getExternalRef();
	}
	
	public Double getCurrentRunCompletion() {
		return assessmentEntry.getCurrentRunCompletion();
	}
	
	public BigDecimal getScore() {
		BigDecimal score = assessmentEntry.getWeightedScore();
		if(score == null) {
			score = assessmentEntry.getScore();
		}
		return score;
	}
	
	public BigDecimal getMaxScore() {
		BigDecimal score = assessmentEntry.getWeightedMaxScore();
		if(score == null) {
			score = assessmentEntry.getMaxScore();
		}
		return score;
	}
	
	public ScoreInfos getScoreInfos() {
		BigDecimal score = getScore();
		BigDecimal maxScore = getMaxScore();
		return new ScoreInfos(score, maxScore);
	}
	
	public Boolean getPassed() {
		return assessmentEntry.getPassed();
	}

}
