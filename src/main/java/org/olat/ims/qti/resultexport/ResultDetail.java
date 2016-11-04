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
package org.olat.ims.qti.resultexport;

import java.util.Date;

public class ResultDetail {
	
	private String assessmentID;
	private Date assessmentDate;
	private String dateCaption;
	private long duration;
	private String durationCaption;
	private float score;
	private String scoreCaption;
	private String passed;
	private String link;
	
	public ResultDetail() {
	}

	public ResultDetail(String assessmentID, Date assessmentDate, long duration, float score, String passed, String link) {
		super();
		this.assessmentID = assessmentID;
		this.assessmentDate = assessmentDate;
		this.duration = duration;
		this.score = score;
		this.passed = passed;
		this.link = link;
	}



	public ResultDetail(String assessmentID, String dateCaption, String durationCaption, String scoreCaption, String passed) {
		super();
		this.assessmentID = assessmentID;
		this.dateCaption = dateCaption;
		this.durationCaption = durationCaption;
		this.scoreCaption = scoreCaption;
		this.passed = passed;
	}
	
	

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDateCaption() {
		return dateCaption;
	}

	public void setDateCaption(String dateCaption) {
		this.dateCaption = dateCaption;
	}

	public String getDurationCaption() {
		return durationCaption;
	}

	public void setDurationCaption(String durationCaption) {
		this.durationCaption = durationCaption;
	}

	public String getScoreCaption() {
		return scoreCaption;
	}

	public void setScoreCaption(String scoreCaption) {
		this.scoreCaption = scoreCaption;
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public Date getAssessmentDate() {
		return assessmentDate;
	}

	public void setAssessmentDate(Date assessmentDate) {
		this.assessmentDate = assessmentDate;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public String getPassed() {
		return passed;
	}

	public void setPassed(String passed) {
		this.passed = passed;
	}
	
	

}
