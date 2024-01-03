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
package org.olat.course.certificate.model;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreScalingHelper;

/**
 * 
 * Initial date: 24.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateInfos {
	
	private final Float score;
	private final Float maxScore;
	private final Boolean passed;
	private final Double progress;
	private final Identity assessedIdentity;
	
	private Date creationDate;
	private String externalId;
	
	public CertificateInfos(Identity assessedIdentity, Float score, Float maxScore, Boolean passed, Double progress) {
		this.score = score;
		this.maxScore = maxScore;
		this.passed = passed;
		this.progress = progress;
		this.assessedIdentity = assessedIdentity;
	}
	
	public static final CertificateInfos valueOf(Identity assessedIdentity,  AssessmentEvaluation scoreEval, CourseEnvironment courseEnvironment) {
		Float score = null;
		Float maxScore = null;
		Boolean passed = null;
		Double completion = null;
		if(scoreEval != null) {
			passed = scoreEval.getPassed();
			completion = scoreEval.getCompletion();
			if(ScoreScalingHelper.isEnabled(courseEnvironment)) {
				score = scoreEval.getWeightedScore();
				maxScore =  scoreEval.getWeightedMaxScore();
			} else {
				score = scoreEval.getScore();
				maxScore = scoreEval.getMaxScore();
			}
		}
		return new CertificateInfos(assessedIdentity, score, maxScore, passed, completion);
	}
	
	public Float getScore() {
		return score;
	}

	public Float getMaxScore() {
		return maxScore;
	}

	public Boolean getPassed() {
		return passed;
	}
	
	public Double getProgress() {
		return progress;
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public int hashCode() {
		return assessedIdentity == null ? 88121 : assessedIdentity.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CertificateInfos) {
			CertificateInfos infos = (CertificateInfos)obj;
			return assessedIdentity != null && assessedIdentity.equals(infos.getAssessedIdentity());
		}
		
		return super.equals(obj);
	}
}