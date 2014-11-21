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
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementCourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.EfficiencyStatementGroupStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementStudentStatEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
public class CoachingDAO extends BasicManager {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;

	public boolean isCoach(Identity coach) {
		try {
			return repositoryManager.hasLearningResourcesAsTeacher(coach);
		} catch (Exception e) {
			logError("isCoach: ", e);
			return false;
		}
	}

	public EfficiencyStatementEntry getEfficencyStatementEntry(UserEfficiencyStatement statement) {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(statement.getCourseRepoKey(), false);
		Identity identity = statement.getIdentity();
		EfficiencyStatementEntry entry = new EfficiencyStatementEntry(identity, re, statement);
		return entry;
	}

	public List<EfficiencyStatementEntry> getEfficencyStatementEntriesAlt(List<Identity> students, List<RepositoryEntry> courses) {
		if(students.isEmpty() || courses.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Long> studentsKey = new ArrayList<Long>();
		for(Identity student:students) {
			studentsKey.add(student.getKey());
		}
		
		List<UserEfficiencyStatement> statements = getEfficiencyStatementByStudentKeys(studentsKey, courses);
		List<EfficiencyStatementEntry> entries = new ArrayList<EfficiencyStatementEntry>(students.size() * courses.size());
		for(RepositoryEntry course:courses) {
			for(Identity student:students) {
				UserEfficiencyStatement statement = getUserEfficiencyStatementFor(student.getKey(), course, statements);
				EfficiencyStatementEntry entry = new EfficiencyStatementEntry(student, course, statement);
				entries.add(entry);
			}
		}
		return entries;
	}

	public List<EfficiencyStatementEntry> getEfficencyStatementEntries(List<IdentityShort> students, List<RepositoryEntry> courses) {
		if(students.isEmpty() || courses.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<Long> studentsKey = new ArrayList<Long>();
		for(IdentityShort student:students) {
			studentsKey.add(student.getKey());
		}
		
		List<UserEfficiencyStatement> statements = getEfficiencyStatementByStudentKeys(studentsKey, courses);
		List<EfficiencyStatementEntry> entries = new ArrayList<EfficiencyStatementEntry>(students.size() * courses.size());
		for(RepositoryEntry course:courses) {
			for(IdentityShort student:students) {
				UserEfficiencyStatement statement = getUserEfficiencyStatementFor(student.getKey(), course, statements);
				EfficiencyStatementEntry entry = new EfficiencyStatementEntry(student, course, statement);
				entries.add(entry);
			}
		}
		return entries;
	}
	
	public List<UserEfficiencyStatement> getEfficencyStatementEntries(Identity student) {
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from ").append(UserEfficiencyStatementLight.class.getName()).append(" as statement ")
		  .append(" where statement.identity.key=:studentKey");

		TypedQuery<UserEfficiencyStatement> dbQuery = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), UserEfficiencyStatement.class);
		dbQuery.setParameter("studentKey", student.getKey());
		List<UserEfficiencyStatement> props = dbQuery.getResultList();
		return props;
	}
	
	private UserEfficiencyStatement getUserEfficiencyStatementFor(Long studentKey, RepositoryEntry course, List<UserEfficiencyStatement> statements) {
		for(UserEfficiencyStatement statement:statements) {
			if(studentKey.equals(statement.getIdentity().getKey()) && course.getKey().equals(statement.getCourseRepoKey())) {
				return statement;
			}
		}
		return null;
	}
	
	private List<UserEfficiencyStatement> getEfficiencyStatementByStudentKeys(List<Long> studentKeys, List<RepositoryEntry> courses) {
		if(studentKeys == null || studentKeys.isEmpty() || courses == null || courses.isEmpty()) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select statement from ").append(UserEfficiencyStatementLight.class.getName()).append(" as statement ")
		  .append(" where statement.identity.key in (:studentsKey) and statement.resource.key in (:courseResourcesKey)");

		TypedQuery<UserEfficiencyStatement> dbQuery = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), UserEfficiencyStatement.class);
		
		List<Long> coursesKey = new ArrayList<Long>();
		for(RepositoryEntry course:courses) {
			coursesKey.add(course.getOlatResource().getKey());
		}
		dbQuery.setParameter("courseResourcesKey",coursesKey);
		dbQuery.setParameter("studentsKey", studentKeys);

		List<UserEfficiencyStatement> props = dbQuery.getResultList();
		return props;
	}

	public List<GroupStatEntry> getGroupsStatistics(Identity coach) {
		try {
			StringBuilder query = new StringBuilder();
			query.append("select s.groupKey, s.groupName, count(distinct s.repoKey), count(distinct s.studentKey), count(s.studentKey),")
			     .append("sum(s.passed), sum(s.failed), sum(s.notAttempted), avg(s.score), count(s.initialLaunchKey) ")
			     .append(" from ").append(EfficiencyStatementGroupStatEntry.class.getName()).append(" as s ")
	             .append(" where s.tutorKey=:coachKey")
	             .append(" group by s.groupKey, s.groupName");
			
			Query dbQuery = dbInstance.getCurrentEntityManager().createQuery(query.toString());
			dbQuery.setParameter("coachKey", coach.getKey());

			@SuppressWarnings("unchecked")
			List<Object[]> rawStats = dbQuery.getResultList();
			List<GroupStatEntry> stats = new ArrayList<GroupStatEntry>();
			for(Object[] rawStat:rawStats) {
				GroupStatEntry entry = new GroupStatEntry();
				entry.setGroupKey((Long)rawStat[0]);
				entry.setGroupName((String)rawStat[1]);
				entry.setCountCourses(((Number)rawStat[2]).intValue());
				entry.setCountStudents(((Number)rawStat[3]).intValue());
				entry.setCountStudents(((Number)rawStat[4]).intValue());
				entry.setCountPassed(((Number)rawStat[5]).intValue());
				entry.setCountFailed(((Number)rawStat[6]).intValue());
				entry.setCountNotAttempted(((Number)rawStat[7]).intValue());
				if(rawStat[8] != null) {
					entry.setAverageScore(((Number)rawStat[8]).floatValue());
				}
				entry.setInitialLaunch(((Number)rawStat[9]).intValue());
				stats.add(entry);
			}
			return stats;
		} catch (Exception e) {
			logError("getGroupsStatistics takes (ms): ", e);
			return Collections.emptyList();
		}
	}

	public List<CourseStatEntry> getCoursesStatistics(Identity coach) {
		//course has members or groups but not both, we can add without check
		
		StringBuilder query = new StringBuilder();
		//                      0             1                    2                             3
		query.append("select s.repoKey, s.repoDisplayName, count(distinct s.studentKey), count(s.studentKey),")
		//                      4             5              6                 7                 8
		     .append(" sum(s.passed), sum(s.failed), sum(s.notAttempted), avg(s.score), count(s.initialLaunchKey) ")
		     .append(" from ").append(EfficiencyStatementCourseStatEntry.class.getName()).append(" as s ")
             .append(" where s.tutorKey=:tutorKey")
             .append(" group by s.repoKey, s.repoDisplayName");

		List<Object[]> rawStats = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Object[].class)
				.setParameter("tutorKey", coach.getKey())
				.getResultList();
		
		List<CourseStatEntry> stats = new ArrayList<CourseStatEntry>();
		for(Object[] rawStat:rawStats) {
			CourseStatEntry entry = new CourseStatEntry();
			entry.setRepoKey((Long)rawStat[0]);
			entry.setRepoDisplayName((String)rawStat[1]);
			entry.setCountStudents(((Number)rawStat[2]).intValue());
			entry.setCountDistinctStudents(((Number)rawStat[3]).intValue());
			entry.setCountPassed(((Number)rawStat[4]).intValue());
			entry.setCountFailed(((Number)rawStat[5]).intValue());
			entry.setCountNotAttempted(((Number)rawStat[6]).intValue());
			if(rawStat[7] != null) {
				entry.setAverageScore(((Number)rawStat[7]).floatValue());
			}
			if(rawStat[8] != null) {
				entry.setInitialLaunch(((Number)rawStat[8]).intValue());
			} else {
				entry.setInitialLaunch(0);
			}
			stats.add(entry);
		}
		return stats;
	}

	public List<StudentStatEntry> getStudentsStatistics(Identity coach) {
		StringBuilder query = new StringBuilder();
		//                     0          1                 2               3              4                             5
		query.append("select s.studentKey, count(s.repoKey), sum(s.passed), sum(s.failed), sum(s.notAttempted), count(s.initialLaunchKey)")
		     .append(" from ").append(EfficiencyStatementStudentStatEntry.class.getName()).append(" as s ")
             .append(" where s.tutorKey=:coachKey")
             .append(" group by s.studentKey");
		
		List<Object[]> rawStats = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Object[].class)
				.setParameter("coachKey", coach.getKey())
				.getResultList();

		List<StudentStatEntry> stats = new ArrayList<StudentStatEntry>();
		for(Object[] rawStat:rawStats) {
			StudentStatEntry entry = new StudentStatEntry();
			entry.setStudentKey((Long)rawStat[0]);
			entry.setCountRepo(((Number)rawStat[1]).intValue());
			entry.setCountPassed(((Number)rawStat[2]).intValue());
			entry.setCountFailed(((Number)rawStat[3]).intValue());
			entry.setCountNotAttempted(((Number)rawStat[4]).intValue());
			entry.setInitialLaunch(((Number)rawStat[5]).intValue());
			stats.add(entry);
		}
		return stats;
	}
	
	public List<StudentStatEntry> getUsersStatistics(List<? extends IdentityRef> identities) {
		if(identities == null || identities.isEmpty()) return Collections.emptyList();
		
		StringBuilder query = new StringBuilder();
		//                     0          1                 2               3              4                             5
		query.append("select s.studentKey, count(s.repoKey), sum(s.passed), sum(s.failed), sum(s.notAttempted), count(s.initialLaunchKey)")
		     .append(" from coachstatisticsidentity as s ")
             .append(" where s.studentKey in (:identitiesKey)")
             .append(" group by s.studentKey");
		
		List<Long> identityKeys = getIdentityKeys(identities);
		List<Object[]> rawStats = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), Object[].class)
				.setParameter("identitiesKey", identityKeys)
				.getResultList();

		List<StudentStatEntry> stats = new ArrayList<StudentStatEntry>();
		for(Object[] rawStat:rawStats) {
			StudentStatEntry entry = new StudentStatEntry();
			entry.setStudentKey((Long)rawStat[0]);
			entry.setCountRepo(((Number)rawStat[1]).intValue());
			entry.setCountPassed(((Number)rawStat[2]).intValue());
			entry.setCountFailed(((Number)rawStat[3]).intValue());
			entry.setCountNotAttempted(((Number)rawStat[4]).intValue());
			entry.setInitialLaunch(((Number)rawStat[5]).intValue());
			stats.add(entry);
		}
		return stats;
	}
	
	private List<Long> getIdentityKeys(List<? extends IdentityRef> identities) {
		List<Long> identityKeys = new ArrayList<>(identities.size());
		for(IdentityRef ref:identities) {
			identityKeys.add(ref.getKey());
		}
		return identityKeys;
	}
	
	public List<Long> getStudents(Identity coach, RepositoryEntry entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(participant.identity.key) from repoentrytogroup as relGroup ")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as coach on coach.role='coach'")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
          .append(" where coach.identity.key=:coachKey and relGroup.entry.key=:repoKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("repoKey", entry.getKey())
				.getResultList();
	}

	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(re) from ").append(RepositoryEntry.class.getName()).append(" as re ")
		  .append(" inner join re.groups as relGroup ")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as coach on coach.role='coach'")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" where coach.identity.key=:coachKey and participant.identity.key=:studentKey");

		TypedQuery<RepositoryEntry> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("studentKey", student.getKey());
		if(firstResult >= 0) {
			dbQuery.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}

		List<RepositoryEntry> courses = dbQuery.getResultList();
		return courses;
	}
	
	public List<RepositoryEntry> getUserCourses(IdentityRef student, int firstResult, int maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(re) from ").append(RepositoryEntry.class.getName()).append(" as re ")
		  .append(" inner join re.groups as relGroup ")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" where participant.identity.key=:studentKey");

		TypedQuery<RepositoryEntry> dbQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("studentKey", student.getKey());
		if(firstResult >= 0) {
			dbQuery.setFirstResult(firstResult);
		}
		if(maxResults > 0) {
			dbQuery.setMaxResults(maxResults);
		}

		List<RepositoryEntry> courses = dbQuery.getResultList();
		return courses;
	}
}