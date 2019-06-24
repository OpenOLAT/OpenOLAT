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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementForCoaching;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.user.propertyhandlers.UserPropertyHandler;
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
public class CoachingServiceImpl implements CoachingService {
	
	@Autowired
	private CoachingDAO coachingDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;

	@Override
	public boolean isCoach(Identity coach) {
		return coachingDao.isCoach(coach);
	}

	@Override
	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student) {
		return coachingDao.getStudentsCourses(coach, student);
	}
	
	@Override
	public List<StudentStatEntry> getUsersStatistics(SearchCoachedIdentityParams params,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		return coachingDao.getUsersStatisticsNative(params, userPropertyHandlers, locale);
	}

	@Override
	public List<StudentStatEntry> getStudentsStatistics(Identity coach, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		return coachingDao.getStudentsStatisticsNative(coach, userPropertyHandlers, locale);
	}

	@Override
	public List<RepositoryEntry> getUserCourses(Identity student) {
		return coachingDao.getUserCourses(student);
	}

	@Override
	public List<CourseStatEntry> getCoursesStatistics(Identity coach) {
		return coachingDao.getCoursesStatisticsNative(coach);
	}

	@Override
	public List<GroupStatEntry> getGroupsStatistics(Identity coach) {
		return coachingDao.getGroupsStatisticsNative(coach);
	}

	@Override
	public List<EfficiencyStatementEntry> getGroup(BusinessGroup group, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		List<Identity> students = businessGroupService.getMembers(group, GroupRoles.participant.name());
		List<RepositoryEntry> courses = businessGroupService.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		List<UserEfficiencyStatementForCoaching> statements = efficiencyStatementManager.getUserEfficiencyStatementForCoaching(group);
		
		Map<IdentityRepositoryEntryKey,UserEfficiencyStatementForCoaching> identityToStatements = new HashMap<>();
		for(UserEfficiencyStatementForCoaching statement:statements) {
			IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(statement.getIdentityKey(), statement.getCourseRepoKey());
			identityToStatements.put(key, statement);
		}
		
		List<EfficiencyStatementEntry> entries = new ArrayList<>(students.size() * courses.size());
		for(RepositoryEntry course:courses) {
			for(Identity student:students) {
				IdentityRepositoryEntryKey key = new IdentityRepositoryEntryKey(student.getKey(), course.getKey());
				UserEfficiencyStatementForCoaching statement = identityToStatements.get(key);
				entries.add(new EfficiencyStatementEntry(student, course, statement, userPropertyHandlers, locale));
			}
		}
		return entries;
	}

	@Override
	public List<EfficiencyStatementEntry> getCourse(Identity coach, RepositoryEntry entry, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		List<Identity> students = coachingDao.getStudents(coach, entry);

		List<UserEfficiencyStatementForCoaching> statements = efficiencyStatementManager.getUserEfficiencyStatementForCoaching(entry);
		Map<Long,UserEfficiencyStatementForCoaching> identityToStatements = new HashMap<>();
		for(UserEfficiencyStatementForCoaching statement:statements) {
			identityToStatements.put(statement.getIdentityKey(), statement);
		}
		
		List<EfficiencyStatementEntry> entries = new ArrayList<>(students.size());
		for(Identity student:students) {
			UserEfficiencyStatementForCoaching statement = identityToStatements.get(student.getKey());
			entries.add(new EfficiencyStatementEntry(student, entry, statement, userPropertyHandlers, locale));
		}
		return entries;
	}

	@Override
	public EfficiencyStatementEntry getEfficencyStatement(UserEfficiencyStatement statement, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		return coachingDao.getEfficencyStatementEntry(statement, userPropertyHandlers, locale);
	}

	@Override
	public List<EfficiencyStatementEntry> getEfficencyStatements(Identity student, List<RepositoryEntry> courses, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		List<UserEfficiencyStatement> statements = efficiencyStatementManager.getUserEfficiencyStatementLight(student, courses);
		Map<Long,UserEfficiencyStatement> courseKeyToStatements = new HashMap<>();
		for(UserEfficiencyStatement statement:statements) {
			courseKeyToStatements.put(statement.getCourseRepoKey(), statement);
		}
		
		List<EfficiencyStatementEntry> entries = new ArrayList<>(courses.size());
		for(RepositoryEntry course:courses) {
			UserEfficiencyStatement statement = courseKeyToStatements.get(course.getKey());
			entries.add(new EfficiencyStatementEntry(student, course, statement, userPropertyHandlers, locale));
		}
		return entries;
	}
	
	@Override
	public List<UserEfficiencyStatement> getEfficencyStatements(Identity student) {
		return efficiencyStatementManager.getUserEfficiencyStatementLight(student);
	}
	
	private static class IdentityRepositoryEntryKey {
		private final Long identityKey;
		private final Long repositoryEntryKey;

		public IdentityRepositoryEntryKey(Long identityKey, Long repositoryEntryKey) {
			this.identityKey = identityKey;
			this.repositoryEntryKey = repositoryEntryKey;
		}
		
		@Override
		public int hashCode() {
			return (identityKey == null ? 365784 : identityKey.hashCode())
					+ (repositoryEntryKey == null ? 234 : repositoryEntryKey.hashCode());
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof IdentityRepositoryEntryKey) {
				IdentityRepositoryEntryKey key = (IdentityRepositoryEntryKey)obj;
				return identityKey != null && identityKey.equals(key.identityKey)
						&& repositoryEntryKey != null && repositoryEntryKey.equals(key.repositoryEntryKey);
			}
			return false;
		}
	}
}
