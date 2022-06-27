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
package org.olat.course.assessment.model;

import java.util.Date;

import org.olat.course.assessment.UserEfficiencyStatement;

/**
 * 
 * Initial date: 17.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EfficiencyStatementVO {

	private Long identityKey;
	
	private Float score;
	private String grade;
	private String gradeSystemIdent;
	private String performanceClassIdent;
	private Boolean passed;
	private Date creationDate;
	private String courseTitle;
	
	public EfficiencyStatementVO() {
		//make JAX-RS happy
	}
	
	public EfficiencyStatementVO(UserEfficiencyStatement efficiencyStatement) {
		courseTitle = efficiencyStatement.getShortTitle();
		creationDate = efficiencyStatement.getCreationDate();
		passed = efficiencyStatement.getPassed();
		score = efficiencyStatement.getScore();
		grade = efficiencyStatement.getGrade();
		gradeSystemIdent = efficiencyStatement.getGradeSystemIdent();
		performanceClassIdent = efficiencyStatement.getPerformanceClassIdent();
		identityKey = efficiencyStatement.getIdentity().getKey();
	}
	
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getGradeSystemIdent() {
		return gradeSystemIdent;
	}

	public void setGradeSystemIdent(String gradeSystemIdent) {
		this.gradeSystemIdent = gradeSystemIdent;
	}

	public String getPerformanceClassIdent() {
		return performanceClassIdent;
	}

	public void setPerformanceClassIdent(String performanceClassIdent) {
		this.performanceClassIdent = performanceClassIdent;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}
}
