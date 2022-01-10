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
package org.olat.ims.qti21.resultexport;

import java.math.BigDecimal;

import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;

/**
 * 
 * Initial date: 28 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResultDetail {
	
	private final String assessmentID;
	private final String assessmentDate;
	private final String duration;
	private final BigDecimal score;
	private final BigDecimal manualScore;
	private final String passed;
	private final String link;
	private final String linkPdf;

	public ResultDetail(String assessmentID, String assessmentDate, String duration,
			BigDecimal score, BigDecimal manualScore, String passed, String link, String linkPdf) {
		this.assessmentID = assessmentID;
		this.assessmentDate = assessmentDate;
		this.duration = duration;
		this.score = score;
		this.manualScore = manualScore;
		this.passed = passed;
		this.link = link;
		this.linkPdf = linkPdf;
	}

	public String getLink() {
		return link;
	}
	
	public String getLinkPdf() {
		return linkPdf;
	}
	
	public boolean hasLinkPdf() {
		return StringHelper.containsNonWhitespace(linkPdf);
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public String getAssessmentDate() {
		return assessmentDate;
	}

	public String getDuration() {
		return duration;
	}

	public String getScore() {
		return score == null ? "" : AssessmentHelper.getRoundedScore(score);
	}
	
	public String getManualScore() {
		return manualScore == null ? "" : AssessmentHelper.getRoundedScore(manualScore);
	}
	
	public String getFinalScore() {
		BigDecimal finalScore = score;
		if(finalScore == null) {
			finalScore = manualScore;
		} else if(manualScore != null) {
			finalScore = finalScore.add(manualScore);
		}
		return finalScore == null ? "" : AssessmentHelper.getRoundedScore(finalScore);
	}

	public String getPassed() {
		return passed;
	}
}
