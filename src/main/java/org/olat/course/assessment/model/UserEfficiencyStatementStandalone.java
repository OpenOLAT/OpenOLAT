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

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.course.assessment.UserEfficiencyStatement;

/**
 * 
 * Mapping used to save efficiency statement in import process where
 * the course / resource doesn't exists.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserEfficiencyStatementStandalone extends PersistentObject implements UserEfficiencyStatement, ModifiedInfo {

	private static final long serialVersionUID = 2996458434418813284L;
	
	private Float score;
	private Boolean passed;
	private Integer totalNodes;
	private Integer attemptedNodes;
	private Integer passedNodes;
	
	private Identity identity;
	private Long resourceKey;
	
	private String title;
	private String shortTitle;
	private Long courseRepoKey;
	
	private String statementXml;
	
	private Date lastModified;
	private Date lastUserModified;
	private Date lastCoachModified;
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public Date getLastUserModified() {
		return lastUserModified;
	}

	public void setLastUserModified(Date lastUserModified) {
		this.lastUserModified = lastUserModified;
	}

	public Date getLastCoachModified() {
		return lastCoachModified;
	}

	public void setLastCoachModified(Date lastCoachModified) {
		this.lastCoachModified = lastCoachModified;
	}

	@Override
	public Float getScore() {
		return score;
	}
		
	public void setScore(Float score) {
		this.score = score;
	}

	@Override
	public Boolean getPassed() {
		return passed;
	}
		
	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	@Override
	public Integer getTotalNodes() {
		return totalNodes;
	}

	public void setTotalNodes(Integer totalNodes) {
		this.totalNodes = totalNodes;
	}

	@Override
	public Integer getAttemptedNodes() {
		return attemptedNodes;
	}

	public void setAttemptedNodes(Integer attemptedNodes) {
		this.attemptedNodes = attemptedNodes;
	}

	@Override
	public Integer getPassedNodes() {
		return passedNodes;
	}

	public void setPassedNodes(Integer passedNodes) {
		this.passedNodes = passedNodes;
	}

	public String getStatementXml() {
		return statementXml;
	}

	public void setStatementXml(String statementXml) {
		this.statementXml = statementXml;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public Long getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(Long resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public Long getCourseRepoKey() {
		return courseRepoKey;
	}

	public void setCourseRepoKey(Long courseRepoKey) {
		this.courseRepoKey = courseRepoKey;
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -82654 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UserEfficiencyStatementStandalone) {
			UserEfficiencyStatementStandalone statement = (UserEfficiencyStatementStandalone)obj;
			return getKey() != null && getKey().equals(statement.getKey());
		}
		return false;
	}
}
