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
import org.olat.resource.OLATResource;

/**
 * Description:
 * This mapping of the efficiency statement table don't contains some attributes as the whole XML
 * and the long title... The mapping is also NOT mutable.
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserEfficiencyStatementLight extends PersistentObject implements UserEfficiencyStatement, ModifiedInfo {

	private static final long serialVersionUID = 2996458434418813284L;
	
	private Float score;
	private Boolean passed;
	private Integer totalNodes;
	private Integer attemptedNodes;
	private Integer passedNodes;
	
	private Identity identity;
	private OLATResource resource;

	private String shortTitle;
	private Long courseRepoKey;
	private Long archivedResourceKey;
	
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

	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
		
	public OLATResource getResource() {
		return resource;
	}
		
	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public Long getArchivedResourceKey() {
		return archivedResourceKey;
	}

	public void setArchivedResourceKey(Long archivedResourceKey) {
		this.archivedResourceKey = archivedResourceKey;
	}

	@Override
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
		if(obj instanceof UserEfficiencyStatementLight) {
			UserEfficiencyStatementLight statement = (UserEfficiencyStatementLight)obj;
			return getKey() != null && getKey().equals(statement.getKey());
		}
		return false;
	}
}
