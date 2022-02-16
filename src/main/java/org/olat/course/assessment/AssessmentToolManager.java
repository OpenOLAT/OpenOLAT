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
package org.olat.course.assessment;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.course.assessment.model.AssessedBusinessGroup;
import org.olat.course.assessment.model.AssessedCurriculumElement;
import org.olat.course.assessment.model.AssessmentScoreStatistic;
import org.olat.course.assessment.model.AssessmentStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentMembersStatistics;
import org.olat.modules.grading.GradingAssignment;
import org.olat.repository.RepositoryEntry;

/**
 * The manager taylored for the assessment tool.
 * 
 * 
 * Initial date: 22.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentToolManager {
	
	public AssessmentStatistics getStatistics(Identity coach, SearchAssessedIdentityParams params);
	
	/**
	 * Return the number of participants to the course
	 * 
	 * @param coach
	 * @param params
	 * @param courseInfoLaunch get the launch infos from the course infos or the assessment entry
	 * @return
	 */
	public AssessmentMembersStatistics getNumberOfParticipants(Identity coach, SearchAssessedIdentityParams params, boolean courseInfoLaunch);
	
	public List<AssessmentScoreStatistic> getScoreStatistics(Identity coach, SearchAssessedIdentityParams params);
	
	public List<AssessedBusinessGroup> getBusinessGroupStatistics(Identity coach, SearchAssessedIdentityParams params);
	
	public List<AssessedCurriculumElement> getCurriculumElementStatistics(Identity coach, SearchAssessedIdentityParams params);
	
	public List<Identity> getAssessedIdentities(Identity coach, SearchAssessedIdentityParams params);
	
	public List<AssessmentEntry> getAssessmentEntries(Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status);
	
	public List<GradingAssignment> getGradingAssignments(Identity coach, SearchAssessedIdentityParams params, AssessmentEntryStatus status) ;
	
	/**
	 * 
	 * @param assessedIdentity
	 * @param entry
	 * @param subIdent
	 * @return
	 */
	public AssessmentEntry getAssessmentEntries(IdentityRef assessedIdentity, RepositoryEntry entry, String subIdent);

	public List<CoachingAssessmentEntry> getCoachingEntries(CoachingAssessmentSearchParams params);

}
