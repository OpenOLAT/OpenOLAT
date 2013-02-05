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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.EfficiencyStatementGroupGroupedStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementGroupStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementMemberStatEntry;
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
public class CoachingManagerImpl extends BasicManager implements CoachingManager {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;

	@Override
	public boolean isCoach(Identity coach) {
		try {
			return repositoryManager.hasLearningResourcesAsTeacher(coach);
		} catch (Exception e) {
			logError("isCoach: ", e);
			return false;
		}
	}

	@Override
	public EfficiencyStatementEntry getEfficencyStatementEntry(UserEfficiencyStatement statement) {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(statement.getCourseRepoKey(), false);
		Identity identity = statement.getIdentity();
		EfficiencyStatementEntry entry = new EfficiencyStatementEntry(identity, re, statement);
		return entry;
	}

	@Override
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
	
	@Override
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

	@Override
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

	@Override
	public List<CourseStatEntry> getCoursesStatistics(Identity coach) {
		//course has members or groups but not both, we can add without check
		List<CourseStatEntry> groupStats = getCoursesStatisticsPart(coach, false);
		List<CourseStatEntry> repoStats = getCoursesStatisticsPart(coach, true);
		
		List<CourseStatEntry> stats = new ArrayList<CourseStatEntry>(groupStats.size() + repoStats.size());
		stats.addAll(groupStats);
		Map<Long,CourseStatEntry> courseMaps = new HashMap<Long,CourseStatEntry>();
		for(CourseStatEntry stat:groupStats) {
			courseMaps.put(stat.getRepoKey(), stat);
		}

		for(CourseStatEntry stat:repoStats) {
			if(courseMaps.containsKey(stat.getRepoKey())) {
				CourseStatEntry currentStat = courseMaps.get(stat.getRepoKey());
				currentStat.add(stat);
			} else {
				stats.add(stat);
			}
		}
		return stats;
	}
		
	private List<CourseStatEntry> getCoursesStatisticsPart(Identity coach, boolean members) {
		try {
			StringBuilder query = new StringBuilder();
			
			String target = (members ? EfficiencyStatementMemberStatEntry.class.getName()
					: EfficiencyStatementGroupGroupedStatEntry.class.getName());
			
			//                      0             1                    2                             3
			query.append("select s.repoKey, s.repoDisplayName, count(distinct s.studentKey), count(s.studentKey),")
			//                      4             5              6                 7                 8
			     .append(" sum(s.passed), sum(s.failed), sum(s.notAttempted), avg(s.score), count(s.initialLaunchKey) ")
			     .append(" from ").append(target).append(" as s ")
	         .append(" where s.tutorKey=:tutorKey")
	         .append(" group by s.repoKey, s.repoDisplayName");

			Query dbQuery = dbInstance.getCurrentEntityManager().createQuery(query.toString());
			dbQuery.setParameter("tutorKey", coach.getKey());

			@SuppressWarnings("unchecked")
			List<Object[]> rawStats = dbQuery.getResultList();
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
		} catch (Exception e) {
			logError("getCourseStatistics " + (members ? "members" : "groups") + " takes (ms): ", e);
			return Collections.emptyList();
		}
	}

	@Override
	public List<StudentStatEntry> getStudentsStatistics(Identity coach) {
		List<StudentStatEntry> studentsInGroups = getStudentsStatisticsPart(coach, false);
		Map<Long,StudentStatEntry> studentsMap = new HashMap<Long,StudentStatEntry>((studentsInGroups.size() * 2) + 1);
		for(StudentStatEntry studentInGroups:studentsInGroups) {
			studentsMap.put(studentInGroups.getStudentKey(), studentInGroups);
		}

		List<StudentStatEntry> studentsMembers = getStudentsStatisticsPart(coach, true);
		for(StudentStatEntry studentsMember:studentsMembers) {
			if(studentsMap.containsKey(studentsMember.getStudentKey())) {
				StudentStatEntry current = studentsMap.get(studentsMember.getStudentKey());
				current.add(studentsMember);
			} else {
				studentsInGroups.add(studentsMember);
			}
		}
		return studentsInGroups;
	}
	
	private List<StudentStatEntry> getStudentsStatisticsPart(Identity coach, boolean members) {
		try {
			
			String target = (members ? EfficiencyStatementMemberStatEntry.class.getName()
					: EfficiencyStatementGroupGroupedStatEntry.class.getName());
			
			StringBuilder query = new StringBuilder();
			//                          0          1                 2               3              4                   5                6
			query.append("select s.studentKey, count(s.repoKey), sum(s.passed), sum(s.failed), sum(s.notAttempted), avg(s.score), count(s.initialLaunchKey)")
			     .append(" from ").append(target).append(" as s ")
	         .append(" where s.tutorKey=:coachKey")
	         .append(" group by s.studentKey");
			
			Query dbQuery = dbInstance.getCurrentEntityManager().createQuery(query.toString());
			dbQuery.setParameter("coachKey", coach.getKey());

			@SuppressWarnings("unchecked")
			List<Object[]> rawStats = dbQuery.getResultList();
			List<StudentStatEntry> stats = new ArrayList<StudentStatEntry>();
			for(Object[] rawStat:rawStats) {
				StudentStatEntry entry = new StudentStatEntry();
				entry.setStudentKey((Long)rawStat[0]);
				entry.setCountRepo(((Number)rawStat[1]).intValue());
				entry.setCountPassed(((Number)rawStat[2]).intValue());
				entry.setCountFailed(((Number)rawStat[3]).intValue());
				entry.setCountNotAttempted(((Number)rawStat[4]).intValue());
				//6
				entry.setInitialLaunch(((Number)rawStat[6]).intValue());
				stats.add(entry);
			}
			return stats;
		} catch (Exception e) {
			logError("getStudentsStatisticsPart " + (members ? "members" : "groups") + " takes (ms): ", e);
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<Long> getStudents(Identity coach, RepositoryEntry entry, int firstResult, int maxResults) {
		try {
			StringBuilder queryG = new StringBuilder();
			queryG.append("select distinct(s.studentKey) from ").append(EfficiencyStatementGroupStatEntry.class.getName()).append(" as s ")
	          .append(" where s.tutorKey=:coachKey and s.repoKey=:repoKey");
			
			StringBuilder queryM = new StringBuilder();
			queryM.append("select distinct(s.studentKey) from ").append(EfficiencyStatementMemberStatEntry.class.getName()).append(" as s ")
	          .append(" where s.tutorKey=:coachKey and s.repoKey=:repoKey");

			TypedQuery<Long> dbQueryG = dbInstance.getCurrentEntityManager().createQuery(queryG.toString(), Long.class);
			dbQueryG.setParameter("coachKey", coach.getKey());
			dbQueryG.setParameter("repoKey", entry.getKey());
			List<Long> studentKeyGs = dbQueryG.getResultList();
			
			TypedQuery<Long> dbQueryM = dbInstance.getCurrentEntityManager().createQuery(queryM.toString(), Long.class);
			dbQueryM.setParameter("coachKey", coach.getKey());
			dbQueryM.setParameter("repoKey", entry.getKey());
			List<Long> studentKeyMs = dbQueryM.getResultList();

			Set<Long> distinctStudentKeys = new HashSet<Long>(((studentKeyGs.size() + studentKeyMs.size()) * 2) + 1);
			distinctStudentKeys.addAll(studentKeyGs);
			distinctStudentKeys.addAll(studentKeyMs);
			
			List<Long> distinctStudentKeyList = new ArrayList<Long>(distinctStudentKeys);
			return distinctStudentKeyList;
		} catch (Exception e) {
			logError("getStudents: ", e);
			return Collections.emptyList();
		}
	}

	@Override
	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student, int firstResult, int maxResults) {
		try {
			StringBuilder query = new StringBuilder();
			query.append("select distinct(re) from ").append(RepositoryEntry.class.getName()).append(" as re ")
			     .append(" where re.key in ( ")     
			     .append("   select s.repoKey from ").append(EfficiencyStatementGroupStatEntry.class.getName()).append(" as s ")
	         .append("   where s.tutorKey=:coachKey and s.studentKey=:studentKey")
	         .append(" ) or re.key in ( ")     
			     .append("   select s.repoKey from ").append(EfficiencyStatementMemberStatEntry.class.getName()).append(" as s ")
	         .append("   where s.tutorKey=:coachKey and s.studentKey=:studentKey")
	         .append(" )");
			
			TypedQuery<RepositoryEntry> dbQuery = dbInstance.getCurrentEntityManager().createQuery(query.toString(), RepositoryEntry.class);
			dbQuery.setParameter("coachKey", coach.getKey());
			dbQuery.setParameter("studentKey", student.getKey());
			if(firstResult >= 0) {
				dbQuery.setFirstResult(firstResult);
			}
			if(maxResults > 0) {
				dbQuery.setMaxResults(maxResults);
			}

			List<RepositoryEntry> courses = dbQuery.getResultList();
			return courses;
		} catch (Exception e) {
			logError("getCourseStatistics: ", e);
			return Collections.emptyList();
		}
	}
}