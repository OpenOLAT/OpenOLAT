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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service
public class CoachingService extends BasicManager {
	
	@Autowired
	private CoachingManager coachingManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	
	public boolean isCoach(Identity coach) {
		return coachingManager.isCoach(coach);
	}
	
	public Map<Long, String> getIdentities(Collection<Long> identityNames) {
		Map<Long,String> identityMap = new HashMap<Long,String>();
		
		List<IdentityShort> identities = securityManager.findShortIdentitiesByKey(identityNames);
		for(IdentityShort identity:identities) {
			String fullName = identity.getFirstName()	+ " " + identity.getLastName();
			identityMap.put(identity.getKey(), fullName);
		}
		return identityMap;
	}

	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student, int firstResult, int maxResults) {
		return coachingManager.getStudentsCourses(coach, student, firstResult, maxResults);
	}
	
	public List<StudentStatEntry> getStudentsStatistics(Identity coach) {
		return coachingManager.getStudentsStatistics(coach);
	}
	
	public List<CourseStatEntry> getCoursesStatistics(Identity coach) {
		return coachingManager.getCoursesStatistics(coach);
	}
	
	public List<GroupStatEntry> getGroupsStatistics(Identity coach) {
		return coachingManager.getGroupsStatistics(coach);
	}
	
	public List<EfficiencyStatementEntry> getGroup(BusinessGroup group) {
		List<SecurityGroup> secGroups = Collections.singletonList(group.getPartipiciantGroup());
		List<IdentityShort> students = securityManager.getIdentitiesShortOfSecurityGroups(secGroups, 0 , -1);
		List<RepositoryEntry> courses = businessGroupService.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		return coachingManager.getEfficencyStatementEntries(students, courses);
	}
	
	public List<EfficiencyStatementEntry> getCourse(Identity coach, RepositoryEntry entry, int firstResult, int maxResults) {
		List<Long> studentKeys = coachingManager.getStudents(coach, entry, firstResult, maxResults);
		List<IdentityShort> students = securityManager.findShortIdentitiesByKey(studentKeys);
		return coachingManager.getEfficencyStatementEntries(students, Collections.singletonList(entry));
	}
	
	public EfficiencyStatementEntry getEfficencyStatement(UserEfficiencyStatement statement) {
		return coachingManager.getEfficencyStatementEntry(statement);
	}
	
	public List<EfficiencyStatementEntry> getEfficencyStatements(Identity student, List<RepositoryEntry> courses) {
		IdentityShort identity = securityManager.loadIdentityShortByKey(student.getKey());
		List<IdentityShort> students = Collections.singletonList(identity);
		return coachingManager.getEfficencyStatementEntries(students, courses);
	}
	
	public List<UserEfficiencyStatement> getEfficencyStatements(Identity student) {
		return coachingManager.getEfficencyStatementEntries(student);
	}

}
