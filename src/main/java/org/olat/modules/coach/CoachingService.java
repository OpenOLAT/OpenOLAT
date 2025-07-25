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
package org.olat.modules.coach;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.group.BusinessGroup;
import org.olat.modules.coach.model.CoachingSecurity;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GeneratedReport;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.ParticipantStatisticsEntry;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.SearchParticipantsStatisticsParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 27.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CoachingService {

	public CoachingSecurity isCoach(Identity identity, Roles roles);
	
	public boolean isMasterCoach(Identity identity);
	

	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student);
	
	public List<ParticipantStatisticsEntry> getParticipantsStatistics(SearchParticipantsStatisticsParams params, List<UserPropertyHandler> userPropertyHandlers, Locale locale);
	
	

	public List<StudentStatEntry> getUsersStatistics(SearchCoachedIdentityParams params, List<UserPropertyHandler> userPropertyHandlers, Locale locale);


	public List<StudentStatEntry> getUsersByOrganization(List<UserPropertyHandler> userPropertyHandlers, Identity identity, List<Organisation> organisations, OrganisationRoles organisationRole, Locale locale);
	
	public List<RepositoryEntry> getUserCourses(Identity student);

	/**
	 * @param identity The user
	 * @param role The role to search for, coach or owner only
	 * @return A list of courses statistics
	 */
	public List<CourseStatEntry> getCoursesStatistics(Identity identity, GroupRoles role);
	
	public List<GroupStatEntry> getGroupsStatistics(Identity coach);
	
	public List<EfficiencyStatementEntry> getGroup(BusinessGroup group, List<UserPropertyHandler> userPropertyHandlers, Locale locale);
	
	public List<EfficiencyStatementEntry> getCourse(Identity coach, RepositoryEntry entry, List<UserPropertyHandler> userPropertyHandlers, Locale locale);
	
	public EfficiencyStatementEntry getEfficencyStatement(UserEfficiencyStatement statement, List<UserPropertyHandler> userPropertyHandlers, Locale locale);
	
	public List<EfficiencyStatementEntry> getEfficencyStatements(Identity student, List<RepositoryEntry> courses, List<UserPropertyHandler> userPropertyHandlers, Locale locale);
	
	public List<UserEfficiencyStatement> getEfficencyStatements(Identity student);
	
	public List<GeneratedReport> getGeneratedReports(Identity coach);

	public LocalFolderImpl getGeneratedReportsFolder(Identity coach);

	public void setGeneratedReport(Identity coach, String name, String fileName);
	
	public void deleteGeneratedReport(Identity coach, VFSMetadata vfsMetadata);

	public VFSLeaf getGeneratedReportLeaf(Identity identity, VFSMetadata metadata);
}
