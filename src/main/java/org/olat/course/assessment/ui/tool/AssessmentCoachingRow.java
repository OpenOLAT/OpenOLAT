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
package org.olat.course.assessment.ui.tool;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.course.assessment.CoachingAssessmentEntry;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 23 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCoachingRow extends UserPropertiesRow implements IndentedCourseNode {
	
	private final Long repositoryEntryKey;
	private final String repositoryEntryName;
	private final String subIdent;
	private final String type;
	private final String shortTitle;
	private final String longTitle;
	private final Date lastUserModified;
	private final Identity statusDoneBy;
	private final Date statusDoneAt;
	
	public AssessmentCoachingRow(CoachingAssessmentEntry entry, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(entry.getAssessedIdentity(), userPropertyHandlers, locale);
		this.repositoryEntryKey = entry.getRepositoryEntryKey();
		this.repositoryEntryName = entry.getRepositoryEntryName();
		this.subIdent = entry.getSubIdent();
		this.type = entry.getCourseElementType();
		this.shortTitle = entry.getCourseElementShortTitle();
		this.longTitle = entry.getCourseElementLongTitle();
		this.lastUserModified = entry.getLastUserModified();
		this.statusDoneBy = entry.getStatusDoneBy();
		this.statusDoneAt = entry.getStatusDoneAt();
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public String getRepositoryEntryName() {
		return repositoryEntryName;
	}

	public String getSubIdent() {
		return subIdent;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	@Override
	public String getLongTitle() {
		return longTitle;
	}

	@Override
	public int getRecursionLevel() {
		return 0;
	}

	public Date getLastUserModified() {
		return lastUserModified;
	}

	public Identity getStatusDoneBy() {
		return statusDoneBy;
	}

	public Date getStatusDoneAt() {
		return statusDoneAt;
	}
	
}
