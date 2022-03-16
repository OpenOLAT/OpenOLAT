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
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.course.assessment.UserEfficiencyStatementShort;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  9 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EfficiencyStatementEntry extends UserPropertiesRow {
	
	private final RepositoryEntry course;
	
	private final Long efficiencyStatementKey;
	private final Float score;
	private final String grade;
	private final String performanceClassIdent;
	private final Boolean passed;
	private final Integer totalNodes;
	private final Integer attemptedNodes;
	private final Date lastModified;
	private final Date lastUserModified;
	private final Date lastCoachModified;
	
	public EfficiencyStatementEntry(Identity student, RepositoryEntry course, UserEfficiencyStatementShort efficiencyStatement,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(student, userPropertyHandlers, locale);
		
		this.course = course;
		if(efficiencyStatement == null) {
			passed = null;
			score = null;
			grade = null;
			performanceClassIdent = null;
			totalNodes = null;
			attemptedNodes = null;
			lastModified = null;
			lastUserModified = null;
			lastCoachModified = null;
			efficiencyStatementKey = null;
		} else {
			efficiencyStatementKey = efficiencyStatement.getKey();
			passed = efficiencyStatement.getPassed();
			score = efficiencyStatement.getScore();
			grade = efficiencyStatement.getGrade();
			performanceClassIdent = efficiencyStatement.getPerformanceClassIdent();
			totalNodes = efficiencyStatement.getTotalNodes();
			attemptedNodes = efficiencyStatement.getAttemptedNodes();
			lastModified = efficiencyStatement.getLastModified();
			lastUserModified = efficiencyStatement.getLastUserModified();
			lastCoachModified = efficiencyStatement.getLastCoachModified();
		}
	}

	public String getCourseDisplayName() {
		return course == null ? null : course.getDisplayname();
	}
	
	public RepositoryEntry getCourse() {
		return course;
	}
	
	public Long getUserEfficiencyStatementKey() {
		return efficiencyStatementKey;
	}
	
	public Float getScore() {
		return score;
	}
	
	public String getGrade() {
		return grade;
	}

	public String getPerformanceClassIdent() {
		return performanceClassIdent;
	}

	public Boolean getPassed() {
		return passed;
	}
	
	public Integer getTotalNodes() {
		return totalNodes;
	}
	
	public Integer getAttemptedNodes() {
		return attemptedNodes;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public Date getLastUserModified() {
		return lastUserModified;
	}
	
	public Date getLastCoachModified() {
		return lastCoachModified;
	}
}
