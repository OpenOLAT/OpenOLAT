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
package org.olat.modules.coach.manager;

import java.util.List;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.id.Identity;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface CoachingManager {
	
	public boolean isCoach(Identity coach);

	public List<GroupStatEntry> getGroupsStatistics(Identity coach);
	
	public List<CourseStatEntry> getCoursesStatistics(Identity coach);

	public List<StudentStatEntry> getStudentsStatistics(Identity coach);
	
	public List<UserEfficiencyStatement> getEfficencyStatementEntries(Identity student);
	
	public EfficiencyStatementEntry getEfficencyStatementEntry(UserEfficiencyStatement statement);

	public List<EfficiencyStatementEntry> getEfficencyStatementEntries(List<IdentityShort> students, List<RepositoryEntry> courses);
	
	public List<Long> getStudents(Identity coach, RepositoryEntry entry, int firstResult, int maxResults);
	
	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student, int firstResult, int maxResults);
}
