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

import org.olat.core.id.Identity;
import org.olat.course.assessment.CoachingAssessmentEntry;

/**
 * 
 * Initial date: 24 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CoachingAssessmentEntryImpl implements CoachingAssessmentEntry {
	
	private final Long assessmentEntryKey;
	private final Long assessedIdentityKey;
	private Identity assessedIdentity;
	private final Long repositoryEntryKey;
	private String repositoryEntryName;
	private final String subIdent;
	private final String courseElementType;
	private final String courseElementShortTitle;
	private final String courseElementLongTitle;
	private final Date lastUserModified;
	private final Long statusDoneByKey;
	private Identity statusDoneBy;
	private final Date statusDoneAt;
	private final boolean owner;
	private final boolean coach;
	
	public CoachingAssessmentEntryImpl(Long assessmentEntryKey, Long assessedIdentityKey, Long repositoryEntryKey,
			String subIdent, String courseElementType, String courseElementShortTitle, String courseElementLongTitle,
			Date lastUserModified, Long statusDoneByKey, Date statusDoneAt, boolean owner, boolean coach) {
		this.assessmentEntryKey = assessmentEntryKey;
		this.assessedIdentityKey = assessedIdentityKey;
		this.repositoryEntryKey = repositoryEntryKey;
		this.subIdent = subIdent;
		this.courseElementType = courseElementType;
		this.courseElementShortTitle = courseElementShortTitle;
		this.courseElementLongTitle = courseElementLongTitle;
		this.lastUserModified = lastUserModified;
		this.statusDoneByKey = statusDoneByKey;
		this.statusDoneAt = statusDoneAt;
		this.owner = owner;
		this.coach = coach;
	}

	@Override
	public Long getAssessmentEntryKey() {
		return assessmentEntryKey;
	}

	public Long getAssessedIdentityKey() {
		return assessedIdentityKey;
	}

	@Override
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public void setAssessedIdentity(Identity assessedIdentity) {
		this.assessedIdentity = assessedIdentity;
	}

	@Override
	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	@Override
	public String getRepositoryEntryName() {
		return repositoryEntryName;
	}

	public void setRepositoryEntryName(String repositoryEntryName) {
		this.repositoryEntryName = repositoryEntryName;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	@Override
	public String getCourseElementType() {
		return courseElementType;
	}

	@Override
	public String getCourseElementShortTitle() {
		return courseElementShortTitle;
	}

	@Override
	public String getCourseElementLongTitle() {
		return courseElementLongTitle;
	}
	
	@Override
	public Date getLastUserModified() {
		return lastUserModified;
	}

	public Long getStatusDoneByKey() {
		return statusDoneByKey;
	}

	@Override
	public Identity getStatusDoneBy() {
		return statusDoneBy;
	}

	public void setStatusDoneBy(Identity statusDoneBy) {
		this.statusDoneBy = statusDoneBy;
	}

	@Override
	public Date getStatusDoneAt() {
		return statusDoneAt;
	}

	@Override
	public boolean isOwner() {
		return owner;
	}

	@Override
	public boolean isCoach() {
		return coach;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assessmentEntryKey == null) ? 0 : assessmentEntryKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CoachingAssessmentEntryImpl other = (CoachingAssessmentEntryImpl) obj;
		if (assessmentEntryKey == null) {
			if (other.assessmentEntryKey != null)
				return false;
		} else if (!assessmentEntryKey.equals(other.assessmentEntryKey))
			return false;
		return true;
	}
	

}
