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
package org.olat.course.assessment.restapi;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.course.assessment.model.UserEfficiencyStatementImpl;

/**
 * 
 * Initial date: 13 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "userEfficiencyStatementVO")
public class UserEfficiencyStatementVO {

	private Long statementKey;
	private Long identityKey;
	
	private Float score;
	private String grade;
	private String gradeSystemIdent;
	private String perfromanceClassIdent;
	private Boolean passed;
	private Integer totalNodes;
	private Integer attemptedNodes;
	private Integer passedNodes;
	
	private Date creationDate;
	private String courseTitle;
	private Long courseRepoKey;
	
	private String statementXml;
	
	public UserEfficiencyStatementVO() {
		//make JAX-RS happy
	}
	
	public UserEfficiencyStatementVO(UserEfficiencyStatementImpl efficiencyStatement) {
		statementKey = efficiencyStatement.getKey();
		identityKey = efficiencyStatement.getIdentity().getKey();
		
		passed = efficiencyStatement.getPassed();
		score = efficiencyStatement.getScore();
		grade = efficiencyStatement.getGrade();
		gradeSystemIdent = efficiencyStatement.getGradeSystemIdent();
		perfromanceClassIdent = efficiencyStatement.getPerformanceClassIdent();
		totalNodes = efficiencyStatement.getTotalNodes();
		passedNodes = efficiencyStatement.getPassedNodes();
		attemptedNodes = efficiencyStatement.getAttemptedNodes();
		
		courseTitle = efficiencyStatement.getShortTitle();
		courseRepoKey = efficiencyStatement.getCourseRepoKey();
		
		creationDate = efficiencyStatement.getCreationDate();

		statementXml = efficiencyStatement.getStatementXml();
	}
	
	public Long getStatementKey() {
		return statementKey;
	}

	public void setStatementKey(Long statementKey) {
		this.statementKey = statementKey;
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

	public String getPerfromanceClassIdent() {
		return perfromanceClassIdent;
	}

	public void setPerfromanceClassIdent(String perfromanceClassIdent) {
		this.perfromanceClassIdent = perfromanceClassIdent;
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

	public Integer getTotalNodes() {
		return totalNodes;
	}

	public void setTotalNodes(Integer totalNodes) {
		this.totalNodes = totalNodes;
	}

	public Integer getAttemptedNodes() {
		return attemptedNodes;
	}

	public void setAttemptedNodes(Integer attemptedNodes) {
		this.attemptedNodes = attemptedNodes;
	}

	public Integer getPassedNodes() {
		return passedNodes;
	}

	public void setPassedNodes(Integer passedNodes) {
		this.passedNodes = passedNodes;
	}

	public Long getCourseRepoKey() {
		return courseRepoKey;
	}

	public void setCourseRepoKey(Long courseRepoKey) {
		this.courseRepoKey = courseRepoKey;
	}

	public String getStatementXml() {
		return statementXml;
	}

	public void setStatementXml(String statementXml) {
		this.statementXml = statementXml;
	}
	
	@Override
	public String toString() {
		return "EfficiencyStatementFullVO[key=" + statementKey + ":identityKey=" + identityKey + ":courseKey=" + courseRepoKey + "]";
	}
	
	@Override
	public int hashCode() {
		return statementKey == null ? 24348 : statementKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof UserEfficiencyStatementVO) {
			UserEfficiencyStatementVO statement = (UserEfficiencyStatementVO)obj;
			return statementKey != null && statementKey.equals(statement.getStatementKey());
		}
		return super.equals(obj);
	}
}
