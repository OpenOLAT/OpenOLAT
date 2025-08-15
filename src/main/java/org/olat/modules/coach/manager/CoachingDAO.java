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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.RelationRole;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.NativeQueryBuilder;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.modules.coach.model.CompletionStats;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.ParticipantStatisticsEntry;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.Certificates;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.Entries;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.SuccessStatus;
import org.olat.modules.coach.model.SearchCoachedIdentityParams;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.GenericSelectionPropertyHandler;
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
public class CoachingDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;

	public boolean isCoach(IdentityRef coach) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select v.key from repositoryentry v")
		  .append(" inner join v.olatResource as res on res.resName='CourseModule'")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership on (membership.identity.key=:identityKey and membership.role ").in(GroupRoles.owner, GroupRoles.coach.name()).append(")")
		  .append(" where v.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed());
		
		List<Long> firstKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFlushMode(FlushModeType.COMMIT)
				.setParameter("identityKey", coach.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return !firstKey.isEmpty();
	}
	
	public boolean isTeacher(IdentityRef coach) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select block.key from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members membership")
		  .append(" where membership.identity.key=:identityKey and membership.role ").in(LectureRoles.teacher);
		
		List<Long> firstKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFlushMode(FlushModeType.COMMIT)
				.setParameter("identityKey", coach.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return !firstKey.isEmpty();
	}
	
	public boolean isMasterCoach(IdentityRef coach) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select curEl.key from curriculumelement as curEl")
		  .append(" inner join curEl.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" where membership.identity.key=:identityKey and membership.role ").in(LectureRoles.mastercoach);
		
		List<Long> firstKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFlushMode(FlushModeType.COMMIT)
				.setParameter("identityKey", coach.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return !firstKey.isEmpty();
	}

	public EfficiencyStatementEntry getEfficencyStatementEntry(UserEfficiencyStatement statement,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(statement.getCourseRepoKey(), false);
		Identity identity = statement.getIdentity();
		return new EfficiencyStatementEntry(identity, re, statement, userPropertyHandlers, locale);
	}
	
	protected List<GroupStatEntry> getGroupsStatisticsNative(Identity coach) {
		Map<Long,GroupStatEntry> map = new HashMap<>();
		boolean hasGroups = getGroups(coach, map);
		if(hasGroups) {
			boolean hasCoachedGroups = getGroupsStatisticsInfosForCoach(coach, map);
			boolean hasOwnedGroups = getGroupsStatisticsInfosForOwner(coach, map);
			for(GroupStatEntry entry:map.values()) {
				entry.getRepoIds().clear();
				entry.setCountStudents(entry.getCountDistinctStudents() * entry.getCountCourses());
			}
			if(hasOwnedGroups) {
				getGroupsStatisticsStatementForOwner(coach, map);
			}
			if(hasCoachedGroups) {
				getGroupsStatisticsStatementForCoach(coach, map);
			}
			
			for(Iterator<Map.Entry<Long, GroupStatEntry>> it=map.entrySet().iterator(); it.hasNext() ; ) {
				Map.Entry<Long, GroupStatEntry> entry = it.next();
				GroupStatEntry groupEntry = entry.getValue();
				if(groupEntry.getCountStudents() == 0) {
					it.remove();
				} else {
					groupEntry.setRepoIds(null);
					int attempted = groupEntry.getCountPassed() + groupEntry.getCountFailed();
					groupEntry.setCountNotAttempted(groupEntry.getCountStudents() - attempted);
					if(groupEntry.getCountScore() > 0) {
						float averageScore = (float)groupEntry.getSumScore() / groupEntry.getCountScore();
						groupEntry.setAverageScore(averageScore);
					}
				}
			}
		}
		return new ArrayList<>(map.values());
	}
	
	private boolean getGroups(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select ")
		  .append(" infos.group_id as grp_id, ")
		  .append(" infos.fk_group_id as bgrp_id, ")
		  .append(" infos.groupname as grp_name, ")
		  .append(" (select count(sg_participant.fk_identity_id) from o_bs_group_member sg_participant ")
		  .append("   where infos.fk_group_id = sg_participant.fk_group_id and sg_participant.g_role='participant' ")
		  .append(" ) as num_of_participant ")
		  .append(" from o_gp_business infos where infos.fk_group_id in ( select ")
		  .append("   togroup.fk_group_id ")
		  .append("  from o_re_to_group togroup ")
		  .append("  inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role = 'coach') ")
		  .append("  inner join o_repositoryentry sg_re on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append("  inner join o_olatresource sg_res on (sg_res.resource_id = sg_re.fk_olatresource and sg_res.resname = 'CourseModule') ")
		  .append("  where sg_coach.fk_identity_id=:coachKey and sg_re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .append("  union  ")
		  .append("   select togroup.fk_group_id")
		  .append("  from o_re_to_group togroup")
		  .append("  inner join o_repositoryentry sg_re on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append("  inner join o_olatresource sg_res on (sg_res.resource_id = sg_re.fk_olatresource and sg_res.resname = 'CourseModule') ")
		  .append("  inner join o_re_to_group owngroup on (owngroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append("  inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id and sg_owner.g_role ")
		     .in(GroupRoles.owner).append(")")
		  .append("  where togroup.r_defgroup=").appendFalse().append(" and sg_owner.fk_identity_id=:coachKey and sg_re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .append(" ) ");

		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();

		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			Long groupKey = ((Number)rawStat[0]).longValue();
			Long baseGroupKey = ((Number)rawStat[1]).longValue();
			String title = (String)rawStat[2];
			GroupStatEntry entry = new GroupStatEntry(groupKey, title);
			entry.setCountDistinctStudents(((Number)rawStat[3]).intValue());
			map.put(baseGroupKey, entry);
		}
		return !rawList.isEmpty();
	}
	
	private boolean getGroupsStatisticsInfosForCoach(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select ")
		  .append("  togroup.fk_group_id as basegr_id, ")
		  .append("  togroup.fk_entry_id as re_id, ")
		  .append("  count(distinct pg_initial_launch.id) as pg_id ")
		  .append(" from o_repositoryentry sg_re  ")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role = 'coach') ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" left join o_as_user_course_infos pg_initial_launch ")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id) ")
		  .append(" where sg_coach.fk_identity_id=:coachKey")
		  .append(" and sg_re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed()).append(" and sg_coach.g_role ").in(GroupRoles.coach)//BAR
		  .append(" group by togroup.fk_group_id, togroup.fk_entry_id ");
		
		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long baseGroupKey = ((Number)rawStats[0]).longValue();
			GroupStatEntry entry = map.get(baseGroupKey);
			if(entry != null) {
				Long repoKey = ((Number)rawStats[1]).longValue();
				if(!entry.getRepoIds().contains(repoKey)) {
					int initalLaunch = ((Number)rawStats[2]).intValue();
					entry.setInitialLaunch(initalLaunch + entry.getInitialLaunch());
					entry.setCountCourses(entry.getCountCourses() + 1);
					entry.getRepoIds().add(repoKey);
				}
			}
		}
		return !rawList.isEmpty();
	}
	
	private boolean getGroupsStatisticsInfosForOwner(Identity coach, Map<Long,GroupStatEntry> map) {
		// Get the groups of the owner
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select ")
		  .append(" togroup.fk_group_id as basegr_id, ")
		  .append(" togroup.fk_entry_id as re_id ")
		  .append(" from o_repositoryentry sg_re  ")
		  .append(" inner join o_re_to_group owngroup on (owngroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id and sg_owner.g_role = 'owner')")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" where sg_owner.fk_identity_id=:coachKey and sg_re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .append(" group by togroup.fk_group_id, togroup.fk_entry_id ");
		
		List<?> entryKeysRaw = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		if (entryKeysRaw.isEmpty()) return false;
		
		List<Long> entryKeys = new ArrayList<>(entryKeysRaw.size());
		List<GroupRepoKey> groupRepoKeys = new ArrayList<>(entryKeysRaw.size());
		for(Object rawObject:entryKeysRaw) {
			Object[] rawStats = (Object[])rawObject;
			Long groupKey = ((Number)rawStats[0]).longValue();
			Long repoKey = ((Number)rawStats[1]).longValue();
			groupRepoKeys.add(new GroupRepoKey(groupKey, repoKey));
			entryKeys.add(repoKey);
		}
		
		// Get the statistics of the groups
		NativeQueryBuilder sbFirstLaunch = new NativeQueryBuilder(1024, dbInstance);
		sbFirstLaunch.append("select ")
		  .append(" togroup.fk_group_id as basegr_id, ")
		  .append(" togroup.fk_entry_id as re_id, ")
		  .append(" count(distinct pg_initial_launch.id) as pg_id ")
		  .append(" from o_repositoryentry sg_re  ")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_as_user_course_infos pg_initial_launch ")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id) ")
		  .append(" where togroup.fk_entry_id in (:repoKeys)")
		  .append(" group by togroup.fk_group_id, togroup.fk_entry_id ");
		
		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sbFirstLaunch.toString())
				.setParameter("repoKeys", entryKeys)
				.getResultList();
		
		Map<GroupRepoKey, Integer> keysToInitialLaunch = new HashMap<>();
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long groupKey = ((Number)rawStats[0]).longValue();
			Long repoKey = ((Number)rawStats[1]).longValue();
			Integer initalLaunch = Integer.valueOf(((Number)rawStats[2]).intValue());
			GroupRepoKey groupRepoKey = new GroupRepoKey(groupKey, repoKey);
			keysToInitialLaunch.put(groupRepoKey, initalLaunch);
		}
		
		for (GroupRepoKey groupRepoKey : groupRepoKeys) {
			GroupStatEntry entry = map.get(groupRepoKey.getGroupKey());
			if (entry != null) {
				if(!entry.getRepoIds().contains(groupRepoKey.getRepoKey())) {
					Integer initalLaunch = keysToInitialLaunch.getOrDefault(groupRepoKey, Integer.valueOf(0));
					entry.setInitialLaunch(initalLaunch.intValue() + entry.getInitialLaunch());
					entry.setCountCourses(entry.getCountCourses() + 1);
					entry.getRepoIds().add(groupRepoKey.getRepoKey());
				}
			}
		}
		
		return !rawList.isEmpty();
	}
	
	private boolean getGroupsStatisticsStatementForCoach(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append(" fin_statement.bgp_id,")
		  .append(" fin_statement.re_id,")
		  .append(" sum(case when fin_statement.passed=").appendTrue().append(" then 1 else 0 end) as num_of_passed,")
		  .append(" sum(case when fin_statement.passed=").appendFalse().append(" then 1 else 0 end) as num_of_failed,")
		  .append(" sum(case when fin_statement.score is not null then 1 else 0 end) as num_score,")
		  .append(" sum(fin_statement.score) as avg_score ")
		  .append("from ( select ")
		  .append("  distinct sg_statement.id as id,")
		  .append("  togroup.fk_group_id as bgp_id,")
		  .append("  togroup.fk_entry_id as re_id,")
		  .append("  sg_statement.passed as passed,")
		  .append("  sg_statement.score as score ")
		  .append(" from o_repositoryentry sg_re ")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role = 'coach') ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id ")
		  .append("   and sg_statement.fk_resource_id = sg_re.fk_olatresource and sg_statement.last_statement=").appendTrue().append(")")
		  .append(" where sg_coach.fk_identity_id=:coachKey and sg_coach.g_role = 'coach'")
		  .append("  and sg_re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .append(") ").appendAs().append(" fin_statement ")
		  .append("group by fin_statement.bgp_id, fin_statement.re_id ");
		
		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long baseGroupKey = ((Number)rawStats[0]).longValue();
			Long repoKey = ((Number)rawStats[1]).longValue();
			GroupStatEntry entry = map.get(baseGroupKey);
			if(entry != null && !entry.getRepoIds().contains(repoKey)) {
				int passed = ((Number)rawStats[2]).intValue();
				int failed = ((Number)rawStats[3]).intValue();
				entry.setCountFailed(failed + entry.getCountFailed());
				entry.setCountPassed(passed + entry.getCountPassed());
				int countScore = ((Number)rawStats[4]).intValue();
				entry.setCountScore(entry.getCountScore() + countScore);
				if(rawStats[5] != null) {
					entry.setSumScore(entry.getSumScore() + ((Number)rawStats[5]).floatValue());
				}
				entry.getRepoIds().add(repoKey);
			}
		}
		return !rawList.isEmpty();
	}
	
	private boolean getGroupsStatisticsStatementForOwner(Identity coach, Map<Long,GroupStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		sb.append("select")
		  .append(" fin_statement.bgp_id,")
		  .append(" fin_statement.re_id,")
		  .append(" sum(case when fin_statement.passed=").appendTrue().append(" then 1 else 0 end) as num_of_passed,")
		  .append(" sum(case when fin_statement.passed=").appendFalse().append(" then 1 else 0 end) as num_of_failed,")
		  .append(" sum(case when fin_statement.score is not null then 1 else 0 end) as num_score,")
		  .append(" sum(fin_statement.score) as avg_score ")
		  .append("from ( select ")
		  .append("  distinct sg_statement.id as id,")
		  .append("  togroup.fk_group_id as bgp_id,")
		  .append("  togroup.fk_entry_id as re_id,")
		  .append("  sg_statement.passed as passed,")
		  .append("  sg_statement.score as score")
		  .append(" from o_repositoryentry sg_re")
		  .append(" inner join o_re_to_group owngroup on (owngroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_owner on (sg_owner.fk_group_id=owngroup.fk_group_id and sg_owner.g_role ")
		  		.in(GroupRoles.owner).append(")")
		  .append(" inner join o_re_to_group togroup on (togroup.r_defgroup=").appendFalse().append(" and togroup.fk_entry_id = sg_re.repositoryentry_id)")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant')")
		  .append(" inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource")
		  .append("   and sg_statement.last_statement=").appendTrue().append(")")
		  .append(" where sg_owner.fk_identity_id=:coachKey and sg_re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .append(") ").appendAs().append(" fin_statement ")
		  .append("group by fin_statement.bgp_id, fin_statement.re_id");
		
		List<?> rawList = dbInstance.getCurrentEntityManager()
				.createNativeQuery(sb.toString())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		for(Object rawObject:rawList) {
			Object[] rawStats = (Object[])rawObject;
			Long baseGroupKey = ((Number)rawStats[0]).longValue();
			Long repoKey = ((Number)rawStats[1]).longValue();
			GroupStatEntry entry = map.get(baseGroupKey);
			if(entry != null && !entry.getRepoIds().contains(repoKey)) {
				int passed = ((Number)rawStats[2]).intValue();
				int failed = ((Number)rawStats[3]).intValue();
				entry.setCountFailed(failed + entry.getCountFailed());
				entry.setCountPassed(passed + entry.getCountPassed());
				int countScore = ((Number)rawStats[4]).intValue();
				entry.setCountScore(entry.getCountScore() + countScore);
				if(rawStats[5] != null) {
					entry.setSumScore(entry.getSumScore() + ((Number)rawStats[5]).floatValue());
				}
				entry.getRepoIds().add(repoKey);
			}
		}
		return !rawList.isEmpty();
	}
	
	protected List<CourseStatEntry> getCoursesStatistics(Identity identity, GroupRoles role) {
		Map<Long,CourseStatEntry> map = getCourses(identity, role);
		if(!map.isEmpty()) {
			loadCoursesStatistics(identity, role, map);
			loadCoursesStatisticsStatements(identity, role, map);
			loadCoursesCompletions(identity, role, map);
		}
		return new ArrayList<>(map.values());
	}
	
	private Map<Long,CourseStatEntry> getCourses(IdentityRef coach, GroupRoles role) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select v.key, lifecycle.key, v.displayname, v.technicalType, v.externalId, v.externalRef, v.status,")
		  .append("  v.teaser, v.location, v.authors, res.resId, lifecycle.validFrom, lifecycle.validTo,")
		  .append("  stats.rating, stats.numOfRatings, stats.numOfComments, v.educationalType.key")
		  .append(" from repositoryentry v")
		  .append(" inner join v.olatResource as res")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as participantGroup")
		  .append(" left join v.lifecycle as lifecycle")
		  .append(" left join v.statistics as stats")
		  .where()
		  .append(" res.resName='CourseModule' and v.status ").in(role == GroupRoles.owner
		  		? RepositoryEntryStatusEnum.preparationToClosed() : RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .and();
		
		if(role == GroupRoles.owner) {
			sb.append("v.key in (select reToOwnerGroup.entry.key from repoentrytogroup as reToOwnerGroup")
			  .append("  inner join bgroupmember as owner on (owner.role='owner' and owner.group.key=reToOwnerGroup.group.key)")
			  .append("  where owner.identity.key=:coachKey")
			  .append(")");
		} else {
			sb.append("participantGroup.key in (select coach.group.key from bgroupmember as coach")
			  .append("  where coach.role='coach' and coach.identity.key=:coachKey")
			  .append(")");
		}
		
		List<Object[]> rawList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		Map<Long,CourseStatEntry> map = new HashMap<>();
		for(Object[] rawStat:rawList) {
			Long repoKey = ((Number)rawStat[0]).longValue();
			map.computeIfAbsent(repoKey, key -> {
				CourseStatEntry entry = new CourseStatEntry();
				entry.setRepoKey(key);
				entry.setRepoDisplayName((String)rawStat[2]);
				entry.setRepoTechnicalType((String)rawStat[3]);
				entry.setRepoExternalId((String)rawStat[4]);
				entry.setRepoExternalRef((String)rawStat[5]);
				entry.setRepoStatus(RepositoryEntryStatusEnum.valueOf((String)rawStat[6]));
				entry.setRepoTeaser((String)rawStat[7]);
				entry.setRepoLocation((String)rawStat[8]);
				entry.setRepoAuthors((String)rawStat[9]);
				entry.setResourceId(PersistenceHelper.extractLong(rawStat, 10));
				entry.setLifecycleStartDate((Date)rawStat[11]);
				entry.setLifecycleEndDate((Date)rawStat[12]);
				entry.setAverageRating(PersistenceHelper.extractDouble(rawStat, 13));
				entry.setNumOfRatings(PersistenceHelper.extractPrimitiveLong(rawStat, 14));
				entry.setNumOfComments(PersistenceHelper.extractPrimitiveInt(rawStat, 15));
				entry.setEducationalTypeKey(PersistenceHelper.extractLong(rawStat, 16));
				return entry;
			});
		}
		return map;
	}
	
	private void loadCoursesStatistics(IdentityRef coach, GroupRoles role, Map<Long,CourseStatEntry> map) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select v.key,")
		  .append("  count(distinct participantMembers.identity.key) as numOfParticipants,")
		  .append("  count(distinct courseInfos.key) as numOfVisited,")
		  .append("  max(courseInfos.recentLaunch) as lastvisit,")
		  .append("  sum(case when certificateConfig.automaticCertificationEnabled=true or certificateConfig.manualCertificationEnabled=true then 1 else 0 end) as numOfCoursesWithCertificate,")
		  .append("  count(certificate.key) as numOfCertificates, ")
		  .append("  sum(case when certificate.nextRecertificationDate<:now then 1 else 0 end) as numOfInvalidCertificates")
		  .append(" from repositoryentry v")
		  .append(" inner join v.olatResource as res")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as participantGroup")
		  .append(" inner join participantGroup.members as participantMembers on (participantMembers.role='participant')")
		  .append(" left join usercourseinfos as courseInfos on (courseInfos.identity.key=participantMembers.identity.key and courseInfos.resource.key=res.key)")
		  .append(" left join certificateentryconfig as certificateConfig on (certificateConfig.entry.key=v.key)")
		  .append(" left join certificate as certificate on (certificate.identity.key=participantMembers.identity.key and certificate.last=true and certificate.olatResource.key=res.key)")
		  .where()
		  .append(" res.resName='CourseModule' and v.status ").in(role == GroupRoles.owner
			  		? RepositoryEntryStatusEnum.preparationToClosed() : RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .and();
		
		if(role == GroupRoles.owner) {
			sb.append("v.key in (select reToOwnerGroup.entry.key from repoentrytogroup as reToOwnerGroup")
			  .append("  inner join bgroupmember as owner on (owner.role='owner' and owner.group.key=reToOwnerGroup.group.key)")
			  .append("  where owner.identity.key=:coachKey")
			  .append(")");
		} else {
			sb.append("participantGroup.key in (select coach.group.key from bgroupmember as coach")
			  .append("  where coach.role='coach' and coach.identity.key=:coachKey")
			  .append(")");
		}
		sb.append(" group by v.key, lifecycle.key");
		
		List<Object[]> rawList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("now", new Date(), TemporalType.TIMESTAMP)
				.getResultList();

		for(Object[] rawStat:rawList) {
			Long repoKey = ((Number)rawStat[0]).longValue();
			
			CourseStatEntry entry = map.get(repoKey);
			if(entry != null) {
				entry.setParticipants(PersistenceHelper.extractPrimitiveInt(rawStat, 1));
				entry.setParticipantsVisited(PersistenceHelper.extractPrimitiveInt(rawStat, 2));
				entry.setParticipantsNotVisited(entry.getParticipants() - entry.getParticipantsVisited());
				entry.setLastVisit((Date)rawStat[3]);
				
				long withCertificate = PersistenceHelper.extractPrimitiveLong(rawStat, 4);
				if(withCertificate > 0l) {
					long numOfCertificates = PersistenceHelper.extractPrimitiveLong(rawStat, 5);
					long numOfInvalidCertificates = PersistenceHelper.extractPrimitiveLong(rawStat, 6);
					entry.setCertificates(new Certificates(numOfCertificates, withCertificate, numOfInvalidCertificates));
				}
			}
		}
	}
	
	private void loadCoursesStatisticsStatements(Identity coach, GroupRoles role, Map<Long,CourseStatEntry> map) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  ae.repositoryEntry.key,")
		  .append("  sum(case when ae.passed=true then 1 else 0 end) as numPassed,")
		  .append("  sum(case when ae.passed=false then 1 else 0 end) as numFailed,")
		  .append("  count(ae.key) as total,")
		  .append("  avg(ae.score) as averageScore")
		  .append(" from assessmententry as ae")
		  .append(" where ae.key in (select distinct ae2.key from repositoryentry as re")
		  .append("  inner join re.groups as reToParticipantGroup")
		  .append("  inner join reToParticipantGroup.group as participantGroup")
		  .append("  inner join participantGroup.members as participant on (participant.role='participant')")
		  .append("  inner join assessmententry as ae2 on (participant.identity.key=ae2.identity.key and ae2.repositoryEntry.key=re.key)")
		  .append("  inner join courseelement rootElement on (rootElement.repositoryEntry.key=re.key and rootElement.subIdent=ae2.subIdent)")
		  .where()
		  .append("  ae2.entryRoot=true and rootElement.passedMode<>'none' and re.status").in(role == GroupRoles.owner
			  		? RepositoryEntryStatusEnum.preparationToClosed() : RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .and();
		
		if(role == GroupRoles.owner) {
			sb.append("re.key in (select reToOwnerGroup.entry.key from repoentrytogroup as reToOwnerGroup")
			  .append("  inner join bgroupmember as owner on (owner.role='owner' and owner.group.key=reToOwnerGroup.group.key)")
			  .append("  where owner.identity.key=:coachKey")
			  .append(" )");
		} else {
			sb.append("participantGroup.key in (select coach.group.key from bgroupmember as coach")
			  .append("  where coach.role='coach' and coach.identity.key=:coachKey")
			  .append(")");
		}
		sb.append(" )")
		  .append(" group by ae.repositoryEntry.key");
		
		List<Object[]> rawList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		for(Object[] rawObjects:rawList) {
			Long entryKey = ((Number)rawObjects[0]).longValue();
			CourseStatEntry stats = map.get(entryKey);
			if(stats != null) {
				long numPassed = PersistenceHelper.extractPrimitiveLong(rawObjects, 1);
				long numFailed = PersistenceHelper.extractPrimitiveLong(rawObjects, 2);
				long total = PersistenceHelper.extractPrimitiveLong(rawObjects, 3);
				long numUndefined = total - numFailed - numPassed;
				stats.setSuccessStatus(new SuccessStatus(numPassed, numFailed, numUndefined, total));

				Double averageScore = PersistenceHelper.extractDouble(rawObjects, 4);
				stats.setAverageScore(averageScore);
			}
		}
	}
	
	private void loadCoursesCompletions(Identity coach, GroupRoles role, Map<Long,CourseStatEntry> map) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  ae.repositoryEntry.key,")
		  .append("  avg(ae.completion) as completion")
		  .append(" from assessmententry as ae")
		  .append(" where ae.key in (select distinct ae2.key from repositoryentry as re")
		  .append("  inner join re.groups as reToParticipantGroup")
		  .append("  inner join reToParticipantGroup.group as participantGroup")
		  .append("  inner join bgroupmember as participant on (participant.role='participant' and participant.group.key=participantGroup.key)")
		  .append("  inner join assessmententry as ae2 on (ae2.repositoryEntry.key=re.key and participant.identity.key=ae2.identity.key and ae2.entryRoot=true)")
		  .where()
		  .append("  re.status").in(role == GroupRoles.owner
			  		? RepositoryEntryStatusEnum.preparationToClosed() : RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .and();
		
		if(role == GroupRoles.owner) {
			sb.append("re.key in (select reToOwnerGroup.entry.key from repoentrytogroup as reToOwnerGroup")
			  .append("  inner join bgroupmember as owner on (owner.role='owner' and owner.group.key=reToOwnerGroup.group.key)")
			  .append("  where owner.identity.key=:coachKey")
			  .append(")");
		} else {
			sb.append("participantGroup.key in (select coach.group.key from bgroupmember as coach")
			  .append("  where coach.role='coach' and coach.identity.key=:coachKey")
			  .append(" )");
		}
		
		sb.append(" )")
		  .append(" group by ae.repositoryEntry.key");
		
		List<Object[]> rawList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		for(Object[] rawObjects:rawList) {
			Long entryKey = ((Number)rawObjects[0]).longValue();
			CourseStatEntry stats = map.get(entryKey);
			if(stats != null) {
				Double completion = PersistenceHelper.extractDouble(rawObjects, 1);
				stats.setAverageCompletion(completion);
			}
		}
	}

	
	
	protected void loadOrganisationsMembers(List<Group> organisationsGroups, List<OrganisationRoles> excludedRoles,
			final List<ParticipantStatisticsEntry> statsEntries, final Map<Long,ParticipantStatisticsEntry> statisticsEntries,
			final List<UserPropertyHandler> userPropertyHandlers, final Locale locale) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  member.key,")
		  .append("  memberUser.key,");
		writeUserProperties("memberUser", sb, userPropertyHandlers);
		sb.append(" 0 as numOf")
		  .append(" from organisation as org")
		  .append(" inner join org.group as memberGroup")
		  .append(" inner join memberGroup.members as members")
		  .append(" inner join members.identity as member on (members.role='user')")
		  .append(" inner join member.user as memberUser")
		  .where()
		  .append(" memberGroup.key in (:organisationGroupKeys)");
		if(excludedRoles != null && !excludedRoles.isEmpty()) {
			sb.and().append("member.key not in (select managerMember.identity.key from bgroupmember as managerMember")
			  .append("  where managerMember.role in (:excludedRoles)")
			  .append(")");
		}
		sb.append(" group by member.key, memberUser.key");
		
		List<Long> groupKeys = organisationsGroups.stream()
				.map(Group::getKey)
				.toList();
		final TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("organisationGroupKeys", groupKeys);
		if(excludedRoles != null && !excludedRoles.isEmpty()) {
			List<String> rolesStrings = excludedRoles.stream()
					.map(OrganisationRoles::name)
					.toList();
			query.setParameter("excludedRoles", rolesStrings);
		}
		
		final List<Object[]> rawList = query.getResultList();
		final int numOfProperties = userPropertyHandlers.size();
		
		for(Object rawObject:rawList) {
			final Object[] rawStat = (Object[])rawObject;
			Long identityKey = ((Number)rawStat[0]).longValue();
			statisticsEntries.computeIfAbsent(identityKey, idKey -> {
				int pos = 2;
				String[] userProperties = new String[numOfProperties];
				for(int i=0; i<numOfProperties; i++) {
					userProperties[i] = (String)rawStat[pos++];
				}
				ParticipantStatisticsEntry entry = new ParticipantStatisticsEntry(idKey, userPropertyHandlers, userProperties, locale);
				statsEntries.add(entry);
				return entry;
			});
		}
	}
	
	protected void loadRelationUsers(Identity identity, RelationRole relationRole,
			final List<ParticipantStatisticsEntry> statsEntries, final Map<Long,ParticipantStatisticsEntry> statisticsEntries,
			final List<UserPropertyHandler> userPropertyHandlers, final Locale locale) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  member.key,")
		  .append("  memberUser.key,");
		writeUserProperties("memberUser", sb, userPropertyHandlers);
		sb.append(" 0 as numOf")
		  .append(" from identitytoidentity as relation")
		  .append(" inner join relation.target as member")
		  .append(" inner join member.user as memberUser")
		  .where()
		  .append(" relation.role.key = :roleKey and relation.source.key=:coachKey")
		  .append(" group by member.key, memberUser.key");
		
		final List<Object[]> rawList = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Object[].class)
				.setParameter("roleKey", relationRole.getKey())
				.setParameter("coachKey", identity.getKey())
				.getResultList();
		final int numOfProperties = userPropertyHandlers.size();
		
		for(Object rawObject:rawList) {
			final Object[] rawStat = (Object[])rawObject;
			Long identityKey = ((Number)rawStat[0]).longValue();
			statisticsEntries.computeIfAbsent(identityKey, idKey -> {
				int pos = 2;
				String[] userProperties = new String[numOfProperties];
				for(int i=0; i<numOfProperties; i++) {
					userProperties[i] = (String)rawStat[pos++];
				}
				ParticipantStatisticsEntry entry = new ParticipantStatisticsEntry(idKey, userPropertyHandlers, userProperties, locale);
				statsEntries.add(entry);
				return entry;
			});
		}
	}
	
	protected List<ParticipantStatisticsEntry> loadParticipantsCoursesStatistics(Identity coach, GroupRoles role,
			List<Group> organisationsGroups, RelationRole userRelation, List<OrganisationRoles> excludedRoles,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  participant.key,")
		  .append("  participantUser.key,");
		writeUserProperties("participantUser", sb, userPropertyHandlers);
		sb.append("  count(distinct re.key) as numOfCourse,")
		  .append("  count(distinct courseInfos.key) as numOfVisited,")
		  .append("  max(courseInfos.recentLaunch) as lastvisit,")
		  .append("  sum(case when certificateConfig.automaticCertificationEnabled=true or certificateConfig.manualCertificationEnabled=true then 1 else 0 end) as numOfCoursesWithCertificate,")
		  .append("  count(certificate.key) as numOfCertificates,")
		  .append("  sum(case when certificate.nextRecertificationDate<:now then 1 else 0 end) as numOfInvalidCertificates")
		  .append(" from repositoryentry as re")
		  .append(" inner join re.groups as reToParticipantGroup")
		  .append(" inner join reToParticipantGroup.group as participantGroup")
		  .append(" inner join participantGroup.members as participantMembers on (participantMembers.role='participant')")
		  .append(" inner join participantMembers.identity as participant")
		  .append(" inner join participant.user as participantUser")
		  .append(" left join usercourseinfos as courseInfos on (courseInfos.identity.key=participant.key and courseInfos.resource.key=re.olatResource.key)")
		  .append(" left join certificateentryconfig as certificateConfig on (certificateConfig.entry.key=re.key)")
		  .append(" left join certificate as certificate on (certificate.identity.key=participant.key and certificate.last=true and certificate.olatResource.key=re.olatResource.key)")
		  .where();
		if(role == GroupRoles.coach) {
			sb.append("participantGroup.key in (select coach.group.key from bgroupmember as coach")
			  .append("  where coach.role='coach' and coach.identity.key=:coachKey")
			  .append(")");
		} else if(role == GroupRoles.owner) {
			sb.append("re.key in (select reToOwnerGroup.entry.key from repoentrytogroup as reToOwnerGroup")
			  .append("  inner join bgroupmember as owner on (owner.role='owner' and owner.group.key=reToOwnerGroup.group.key)")
			  .append("  where owner.identity.key=:coachKey")
			  .append(")");
		} else if(organisationsGroups != null && !organisationsGroups.isEmpty()) {
			sb.append("participant.key in (select organisationMember.identity.key from bgroupmember as organisationMember")
			  .append("  where organisationMember.group.key in (:organisationGroupKeys) and organisationMember.role='user'");
			sb.append(")");
		} else if(userRelation != null) {
			sb.append("participant.key in (select relation.target.key from identitytoidentity as relation")
			  .append("  where relation.source.key=:coachKey and relation.role.key=:roleKey")
			  .append(")");
		}
		
		if(excludedRoles != null && !excludedRoles.isEmpty()) {
			sb.and().append("participant.key not in (select managerMember.identity.key from bgroupmember as managerMember")
			  .append("  where managerMember.role in (:excludedRoles)")
			  .append(")");
		}
		
		sb.append(" group by participant.key, participantUser.key") ;
		
		final TypedQuery<Object[]> query = buildTypedQuery(sb, coach, role,
				organisationsGroups, userRelation);
		if(excludedRoles != null && !excludedRoles.isEmpty()) {
			List<String> rolesStrings = excludedRoles.stream()
					.map(OrganisationRoles::name)
					.toList();
			query.setParameter("excludedRoles", rolesStrings);
		}
		query.setParameter("now", new Date(), TemporalType.TIMESTAMP);
		final List<Object[]> rawList = query.getResultList();
		final List<ParticipantStatisticsEntry> list = new ArrayList<>(rawList.size());
		final int numOfProperties = userPropertyHandlers.size();
		
		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			Long identityKey = ((Number)rawStat[0]).longValue();
			
			int pos = 2;
			String[] userProperties = new String[numOfProperties];
			for(int i=0; i<numOfProperties; i++) {
				userProperties[i] = (String)rawStat[pos++];
			}
	
			long numOfCourses = PersistenceHelper.extractPrimitiveLong(rawStat, pos++);
			long numOfVisited = PersistenceHelper.extractPrimitiveLong(rawStat, pos++);
			Date lastVisit = (Date)rawStat[pos++];
			
			long numOfCoursesWithCertificate = PersistenceHelper.extractPrimitiveLong(rawStat, pos++);
			long numOfCertificates = PersistenceHelper.extractPrimitiveLong(rawStat, pos++);
			long numOfInvalidCertificates = PersistenceHelper.extractPrimitiveLong(rawStat, pos);

			ParticipantStatisticsEntry entry = new ParticipantStatisticsEntry(identityKey, userPropertyHandlers, userProperties, locale);
			entry.setEntries(new Entries(numOfCourses, numOfVisited, numOfCourses - numOfVisited));
			entry.setLastVisit(lastVisit);
			entry.setCertificates(new Certificates(numOfCertificates, numOfCoursesWithCertificate, numOfInvalidCertificates));
			list.add(entry);
		}
		return list;
	}
	
	private TypedQuery<Object[]> buildTypedQuery(QueryBuilder sb, Identity coach, GroupRoles role,
			List<Group> organisationsGroups, RelationRole userRelation) {
		
		final TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(role == GroupRoles.coach || role == GroupRoles.owner) {
			query.setParameter("coachKey", coach.getKey());
		} else if(organisationsGroups != null && !organisationsGroups.isEmpty()) {
			List<Long> groupKeys = organisationsGroups.stream()
					.map(Group::getKey)
					.toList();
			query.setParameter("organisationGroupKeys", groupKeys);
		} else if(userRelation != null) {
			query.setParameter("coachKey", coach.getKey());
			query.setParameter("roleKey", userRelation.getKey());
		}
		return query;
	}
	
	private void writeUserProperties(String user, QueryBuilder sb, List<UserPropertyHandler> userPropertyHandlers) {
		for(UserPropertyHandler handler:userPropertyHandlers) {
			sb.append(" ").append(user).append(".").append(handler.getName()).append(" as ")
			  .append("ident_user_").append(handler.getDatabaseColumnName()).append(",");
		}	
	}
	
	protected void processParticipantsPassedFailedStatistics(Identity coach, GroupRoles role,
			List<Group> organisationsGroups, RelationRole userRelation,
			Map<Long,ParticipantStatisticsEntry> statEntries) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  ae.identity.key,")
		  .append("  sum(case when ae.passed=true then 1 else 0 end) as numPassed,")
		  .append("  sum(case when ae.passed=false then 1 else 0 end) as numFailed,")
		  .append("  count(ae.key) as total")
		  .append(" from assessmententry as ae")
		  .append(" where ae.key in (select distinct ae2.key from repositoryentry as re")
		  .append("  inner join re.groups as reToParticipantGroup")
		  .append("  inner join reToParticipantGroup.group as participantGroup")
		  .append("  inner join participantGroup.members as participant on (participant.role='participant')")
		  .append("  inner join assessmententry as ae2 on (participant.identity.key=ae2.identity.key and ae2.repositoryEntry.key=re.key)")
		  .append("  inner join courseelement rootElement on (rootElement.repositoryEntry.key=re.key and rootElement.subIdent=ae2.subIdent)")
		  .where()
		  .append("  ae2.entryRoot=true and ae2.completion is not null and rootElement.passedMode<>'none' and re.status").in(RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .and();
		
		if(role == GroupRoles.coach) {
			sb.append("participantGroup.key in (select coach.group.key from bgroupmember as coach")
			  .append("  where coach.role='coach' and coach.identity.key=:coachKey")
			  .append(" )");
		} else if(role == GroupRoles.owner) {
			sb.append("re.key in (select reToOwnerGroup.entry.key from repoentrytogroup as reToOwnerGroup")
			  .append("  inner join bgroupmember as owner on (owner.role='owner' and owner.group.key=reToOwnerGroup.group.key)")
			  .append("  where owner.identity.key=:coachKey")
			  .append(" )");
		} else if(!organisationsGroups.isEmpty()) {
			sb.append("participant.identity.key in (select organisationMember.identity.key from bgroupmember as organisationMember")
			  .append("  where organisationMember.group.key in (:organisationGroupKeys) and organisationMember.role='user'")
			  .append(")");
		} else if(userRelation != null) {
			sb.append("participant.identity.key in (select relation.target.key from identitytoidentity as relation")
			  .append("  where relation.source.key=:coachKey and relation.role.key=:roleKey")
			  .append(")");
		}
		sb.append(" )")
		  .append(" group by ae.identity.key");
		
		final TypedQuery<Object[]> query = buildTypedQuery(sb, coach, role,
				organisationsGroups, userRelation);
		final List<Object[]> rawList = query.getResultList();
		for(Object[] rawObjects:rawList) {
			Long identityKey = ((Number)rawObjects[0]).longValue();
			ParticipantStatisticsEntry stats = statEntries.get(identityKey);
			if(stats != null) {
				long numPassed = PersistenceHelper.extractPrimitiveLong(rawObjects, 1);
				long numFailed = PersistenceHelper.extractPrimitiveLong(rawObjects, 2);
				long total = PersistenceHelper.extractPrimitiveLong(rawObjects, 3);
				long numUndefined = total - numFailed - numPassed;
				stats.setSuccessStatus(new SuccessStatus(numPassed, numFailed, numUndefined, total));
			}
		}
	}
	
	protected void processParticipantsCompletionStatistics(Identity identity, GroupRoles role,
			List<Group> organisationsGroups, RelationRole userRelation,
			Map<Long,ParticipantStatisticsEntry> statEntries) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  ae.identity.key,")
		  .append("  avg(ae.completion) as completion")
		  .append(" from assessmententry as ae")
		  .append(" where ae.key in (select distinct ae2.key from repositoryentry as re")
		  .append("  inner join re.groups as reToParticipantGroup")
		  .append("  inner join reToParticipantGroup.group as participantGroup")
		  .append("  inner join bgroupmember as participant on (participant.role='participant' and participant.group.key=participantGroup.key)")
		  .append("  inner join assessmententry as ae2 on (ae2.repositoryEntry.key=re.key and participant.identity.key=ae2.identity.key and ae2.entryRoot=true)")
		  .where()
		  .append("  re.status").in(RepositoryEntryStatusEnum.coachPublishedToClosed())
		  .and();

		if(role == GroupRoles.coach) {
			sb.append("participantGroup.key in (select distinct coach.group.key from bgroupmember as coach")
			  .append("  where coach.role='coach' and coach.identity.key=:coachKey")
			  .append(" )");
		} else if(role == GroupRoles.owner) {
			sb.append("re.key in (select distinct reToOwnerGroup.entry.key from repoentrytogroup as reToOwnerGroup")
			  .append("  inner join bgroupmember as owner on (owner.role='owner' and owner.group.key=reToOwnerGroup.group.key)")
			  .append("  where owner.identity.key=:coachKey")
			  .append(" )");
		} else if(!organisationsGroups.isEmpty()) {
			sb.append("participant.identity.key in (select organisationMember.identity.key from bgroupmember as organisationMember")
			  .append("  where organisationMember.group.key in (:organisationGroupKeys) and organisationMember.role='user'")
			  .append(")");
		} else if(userRelation != null) {
			sb.append("participant.identity.key in (select relation.target.key from identitytoidentity as relation")
			  .append("  where relation.source.key=:coachKey and relation.role.key=:roleKey")
			  .append(")");
		}
		sb.append(" )")
		  .append(" group by ae.identity.key");
		
		final TypedQuery<Object[]> query = buildTypedQuery(sb, identity, role,
				organisationsGroups, userRelation);
		final List<Object[]> rawList = query.getResultList();
		for(Object[] rawObjects:rawList) {
			Long identityKey = ((Number)rawObjects[0]).longValue();
			ParticipantStatisticsEntry stats = statEntries.get(identityKey);
			if(stats != null) {
				Double completion = PersistenceHelper.extractDouble(rawObjects, 1);
				stats.setAverageCompletion(completion);
			}
		}
	}
	
	protected void loadRepositoryEntryReservation(Identity identity, GroupRoles role,
			List<Group> organisationsGroups, RelationRole userRelation,
			Map<Long,ParticipantStatisticsEntry> statEntries) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  reservation.identity.key,")
		  .append("  count(reservation.key) as numOfReservations,")
		  .append("  sum(case when reservation.userConfirmable=true then 1 else 0 end) as confirmedByUser,")
		  .append("  sum(case when reservation.userConfirmable=false then 1 else 0 end) as confirmedByOther")
		  .append(" from resourcereservation as reservation")
		  .append(" inner join reservation.resource as resource")
		  .append(" inner join repositoryentry as re on (re.olatResource.key=resource.key)")
		  .where();
		
		if(role == GroupRoles.coach) {
			sb.append("re.key in (select distinct reToCoachGroup.entry.key from repoentrytogroup as reToCoachGroup")
			  .append("  inner join bgroupmember as coach on (coach.role='coach' and coach.group.key=reToCoachGroup.group.key)")
			  .append("  where reToCoachGroup.defaultGroup=true and coach.identity.key=:coachKey")
			  .append(" )");
		} else if(role == GroupRoles.owner) {
			sb.append("re.key in (select distinct reToOwnerGroup.entry.key from repoentrytogroup as reToOwnerGroup")
			  .append("  inner join bgroupmember as owner on (owner.role='owner' and owner.group.key=reToOwnerGroup.group.key)")
			  .append("  where owner.identity.key=:coachKey")
			  .append(" )");
		} else if(!organisationsGroups.isEmpty()) {
			sb.append("reservation.identity.key in (select organisationMember.identity.key from bgroupmember as organisationMember")
			  .append("  where organisationMember.group.key in (:organisationGroupKeys) and organisationMember.role='user'")
			  .append(")");
		} else if(userRelation != null) {
			sb.append("reservation.identity.key in (select relation.target.key from identitytoidentity as relation")
			  .append("  where relation.source.key=:coachKey and relation.role.key=:roleKey")
			  .append(")");
		}
		
		sb.append(" group by reservation.identity.key");
		
		final TypedQuery<Object[]> query = buildTypedQuery(sb, identity, role,
				organisationsGroups, userRelation);
		final List<Object[]> rawList = query.getResultList();
		for(Object[] rawObjects:rawList) {
			Long identityKey = ((Number)rawObjects[0]).longValue();
			ParticipantStatisticsEntry stats = statEntries.get(identityKey);
			if(stats != null) {
				long reservations = PersistenceHelper.extractPrimitiveLong(rawObjects, 1);
				long confirmedByUser = PersistenceHelper.extractPrimitiveLong(rawObjects, 2);
				long confirmedByAdmin = PersistenceHelper.extractPrimitiveLong(rawObjects, 3);
				stats.addReservations(reservations);
				stats.addReservationsConfirmedByUser(confirmedByUser);
				stats.addReservationsConfirmedByAdmin(confirmedByAdmin);
			}
		}
	}
	
	protected void loadCurriculumElementsReservations(Identity identity, GroupRoles role,
			List<Group> organisationsGroups, RelationRole userRelation,
			Map<Long,ParticipantStatisticsEntry> statEntries) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select")
		  .append("  reservation.identity.key,")
		  .append("  count(reservation.key) as numOfReservations,")
		  .append("  sum(case when reservation.userConfirmable=true then 1 else 0 end) as confirmedByUser,")
		  .append("  sum(case when reservation.userConfirmable=false then 1 else 0 end) as confirmedByOther")
		  .append(" from resourcereservation as reservation")
		  .append(" inner join reservation.resource as resource")
		  .append(" inner join curriculumelement as curEl on (curEl.resource.key=resource.key)")
		  .append(" inner join curEl.group as baseGroup")
		  .where();

		if(role == GroupRoles.coach) {
			sb.append("baseGroup.key in (select distinct coach.group.key from bgroupmember as coach")
			  .append("  where coach.role='coach' and coach.identity.key=:coachKey")
			  .append(")");
		} else if(role == GroupRoles.owner) {
			sb.append("baseGroup.key in (select distinct owner.group.key from bgroupmember as owner")
			  .append("  where owner.role='owner' and owner.identity.key=:coachKey")
			  .append(")");
		} else if(!organisationsGroups.isEmpty()) {
			sb.append("reservation.identity.key in (select organisationMember.identity.key from bgroupmember as organisationMember")
			  .append("  where organisationMember.group.key in (:organisationGroupKeys) and organisationMember.role='user'")
			  .append(")");
		} else if(userRelation != null) {
			sb.append("reservation.identity.key in (select relation.target.key from identitytoidentity as relation")
			  .append("  where relation.source.key=:coachKey and relation.role.key=:roleKey")
			  .append(")");
		}
		
		sb.append(" group by reservation.identity.key");
		
		final TypedQuery<Object[]> query = buildTypedQuery(sb, identity, role, organisationsGroups, userRelation);
		final List<Object[]> rawList = query.getResultList();
		for(Object[] rawObjects:rawList) {
			Long identityKey = ((Number)rawObjects[0]).longValue();
			ParticipantStatisticsEntry stats = statEntries.get(identityKey);
			if(stats != null) {
				long reservations = PersistenceHelper.extractPrimitiveLong(rawObjects, 1);
				long confirmedByUser = PersistenceHelper.extractPrimitiveLong(rawObjects, 2);
				long confirmedByAdmin = PersistenceHelper.extractPrimitiveLong(rawObjects, 3);
				stats.addReservations(reservations);
				stats.addReservationsConfirmedByUser(confirmedByUser);
				stats.addReservationsConfirmedByAdmin(confirmedByAdmin);
			}
		}
	}
	
	private void writeUserProperties(String user, NativeQueryBuilder sb, List<UserPropertyHandler> userPropertyHandlers) {
		for(UserPropertyHandler handler:userPropertyHandlers) {
			sb.append(" ").append(user).append(".").append(handler.getDatabaseColumnName()).append(" as ")
			  .append("p_").append(handler.getDatabaseColumnName()).append(",");
		}	
	}

	private void writeUserPropertiesGroupBy(String user, NativeQueryBuilder sb, List<UserPropertyHandler> userPropertyHandlers) {
		for(UserPropertyHandler handler:userPropertyHandlers) {
			sb.append(", ").append(user).append(".").append(handler.getDatabaseColumnName());
		}	
	}
	
	public boolean getStudentsCompletionStatement(List<? extends CurriculumElementRef> elements, Map<Long, ? extends CompletionStats> statistics) {
		if(elements == null || elements.isEmpty() || statistics == null || statistics.isEmpty()) return false;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae.identity.key, avg(ae.completion)")
		  .append(" from assessmententry as ae")
		  .append(" where ae.key in (select distinct asge.key")
		  .append(" from curriculumelement el")
		  .append(" inner join el.group bGroup")
		  .append(" inner join bGroup.members as membership")
		  .append(" inner join repoentrytogroup as rel on (bGroup.key=rel.group.key)")
		  .append(" inner join assessmententry as asge on (asge.repositoryEntry.key=rel.entry.key and asge.identity.key=membership.identity.key)")
		  .append(" where membership.role ").in(CurriculumRoles.participant).append(" and el.key in (:elementKeys)")
		  .append(" and asge.entryRoot=true and asge.completion is not null")
		  .append(")")
		  .append(" group by ae.identity.key");
		
		List<Long> elementKeys = elements.stream()
				.map(CurriculumElementRef::getKey)
				.collect(Collectors.toList());
		
		List<Object[]> rawList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("elementKeys", elementKeys)
				.getResultList();

		for(Object[] rawStat:rawList) {
			Long identityKey = ((Number)rawStat[0]).longValue();
			Double completion = PersistenceHelper.extractDouble(rawStat, 1);
			
			CompletionStats stats = statistics.get(identityKey);
			if(stats != null) {
				stats.setAverageCompletion(completion);
			}
		}
		return !rawList.isEmpty();
	}

	public List<StudentStatEntry> getUsersByOrganization(List<UserPropertyHandler> userPropertyHandlers,
														 Identity identity, List<Organisation> organisations,
														 OrganisationRoles organisationRole, Locale locale) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct ident.key");
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			sb.append(", user.").append(userPropertyHandler.getName()).append(" as p_").append(userPropertyHandler.getName());
		}
		sb.append(" from organisation org");
		sb.append(" inner join org.group orgGroup");
		sb.append(" inner join orgGroup.members mgmtMembership");
		sb.append(" inner join orgGroup.members userMembership");
		sb.append(" inner join userMembership.identity ident");
		sb.append(" inner join ident.user user");
		sb.and().append("org.key in (:orgKeys)");
		sb.and().append("mgmtMembership.identity.key = :identityKey");
		sb.and().append("mgmtMembership.role").in(organisationRole);
		sb.and().append("userMembership.inheritanceModeString").in(GroupMembershipInheritance.root, GroupMembershipInheritance.none);
		sb.and().append("userMembership.identity.key <> :identityKey");
		sb.and().append("userMembership.role = 'user'");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("orgKeys", organisations.stream().map(Organisation::getKey).toList())
				.getResultList().stream()
				.map(objects -> mapToUser(objects, userPropertyHandlers, locale))
				.toList();
	}

	private StudentStatEntry mapToUser(Object[] objects, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		int srcIdx = 0;
		int nbProperties = userPropertyHandlers.size();
		String[] userProperties = new String[nbProperties];

		Long identityKey = (Long) objects[srcIdx++];
		for (int propIdx = 0; propIdx < nbProperties; propIdx++) {
			userProperties[propIdx] = (String) objects[srcIdx++];
		}
		return new StudentStatEntry(identityKey, userPropertyHandlers, userProperties, locale);
	}

	/**
	 * Search all participants without restrictions on coach or owner relations.
	 * 
	 * @param params
	 * @return The list of statistics
	 */
	protected List<StudentStatEntry> getUsersStatisticsNative(SearchCoachedIdentityParams params, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		Map<Long,StudentStatEntry> map = new HashMap<>();
		boolean hasUsers = getUsersStatisticsInfos(params, map, userPropertyHandlers, locale);
		if(hasUsers) {
			getUsersStatisticsStatements(params, map);
		}
		return new ArrayList<>(map.values());
	}
	
	private boolean getUsersStatisticsInfos(SearchCoachedIdentityParams params, Map<Long, StudentStatEntry> map,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		Map<String,Object> queryParams = new HashMap<>();
		sb.append("select ")
		  .append("  sg_participant_id.id as part_id,")
		  .append("  sg_participant_user.user_id as part_user_id,");
		writeUserProperties("sg_participant_user",  sb, userPropertyHandlers);
		sb.append("  count(distinct sg_re.repositoryentry_id) as re_count, ")
		  .append("  count(distinct pg_initial_launch.id) as pg_id ")
		  .append("  from o_repositoryentry sg_re ")
		  .append(" inner join o_olatresource sg_res on (sg_res.resource_id = sg_re.fk_olatresource and sg_res.resname = 'CourseModule')")
		  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_bs_identity id_participant on (sg_participant.fk_identity_id = id_participant.id) ")
		  .append(" inner join o_bs_identity sg_participant_id on (sg_participant_id.id=sg_participant.fk_identity_id)")
		  .append(" inner join o_user sg_participant_user on (sg_participant_user.fk_identity=sg_participant_id.id)")
		  .append(" left join o_as_user_course_infos pg_initial_launch ")
		  .append("   on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = id_participant.id) ")
		  .append(" inner join o_user user_participant on (user_participant.fk_identity=id_participant.id)")
		  .append(" where sg_re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed()).append(" ");
		appendUsersStatisticsSearchParams(params, queryParams, sb)
		  .append(" group by sg_participant_id.id, sg_participant_user.user_id");
		if(dbInstance.isOracle()) {
			writeUserPropertiesGroupBy("sg_participant_user", sb, userPropertyHandlers);
		}

		Query query = dbInstance.getCurrentEntityManager().createNativeQuery(sb.toString());
		for(Map.Entry<String, Object> entry:queryParams.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		List<?> rawList = query.getResultList();

		int numOfProperties = userPropertyHandlers.size();
		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			
			int pos = 0;
			Long identityKey = ((Number)rawStat[pos++]).longValue();
			((Number)rawStat[pos++]).longValue();//user key
			
			String[] userProperties = new String[numOfProperties];
			for(int i=0; i<numOfProperties; i++) {
				userProperties[i] = (String)rawStat[pos++];
			}
			StudentStatEntry entry = new StudentStatEntry(identityKey, userPropertyHandlers, userProperties, locale);
			entry.setCountRepo(((Number)rawStat[pos++]).intValue());
			entry.setInitialLaunch(((Number)rawStat[pos++]).intValue());
			map.put(identityKey, entry);
		}
		return !rawList.isEmpty();
	}
	
	private boolean getUsersStatisticsStatements(SearchCoachedIdentityParams params, Map<Long,StudentStatEntry> map) {
		NativeQueryBuilder sb = new NativeQueryBuilder(1024, dbInstance);
		Map<String,Object> queryParams = new HashMap<>();
		sb.append("select ")
		  .append(" fin_statement.fk_identity, ")
		  .append(" sum(case when fin_statement.passed=").appendTrue().append(" then 1 else 0 end) as num_of_passed, ")
		  .append(" sum(case when fin_statement.passed=").appendFalse().append(" then 1 else 0 end) as num_of_failed ")
		  .append("from o_as_eff_statement fin_statement ")
		  .append("where fin_statement.id in ( select ")
		  .append("  distinct sg_statement.id as st_id ")
		  .append(" from o_repositoryentry sg_re ")
 		  .append(" inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id) ")
		  .append(" inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant') ")
		  .append(" inner join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource")
		  .append("   and sg_statement.last_statement=").appendTrue().append(")")
		  .append(" inner join o_bs_identity id_participant on (sg_participant.fk_identity_id = id_participant.id) ");
		appendUsersStatisticsJoins(params, sb)
		  .append(" where  sg_re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed());
		appendUsersStatisticsSearchParams(params, queryParams, sb)
		  .append(")")
		  .append(" group by fin_statement.fk_identity ");
		
		Query query = dbInstance.getCurrentEntityManager().createNativeQuery(sb.toString());
		for(Map.Entry<String, Object> entry:queryParams.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		List<?> rawList = query.getResultList();
		for(Object rawObject:rawList) {
			Object[] rawStat = (Object[])rawObject;
			Long userKey = ((Number)rawStat[0]).longValue();
			StudentStatEntry entry = map.get(userKey);
			if(entry != null) {
				int passed = ((Number)rawStat[1]).intValue();
				int failed = ((Number)rawStat[2]).intValue();
				entry.setCountPassed(passed);
				entry.setCountFailed(failed);
				int notAttempted = entry.getCountRepo() - passed - failed;
				entry.setCountNotAttempted(notAttempted);
			}
		}
		return !rawList.isEmpty();
	}
	
	private NativeQueryBuilder appendUsersStatisticsJoins(SearchCoachedIdentityParams params, NativeQueryBuilder sb) {
		if(StringHelper.containsNonWhitespace(params.getLogin()) || (params != null && params.getUserProperties() != null && params.getUserProperties().size() > 0)) {
			sb.append(" inner join o_user user_participant on (user_participant.fk_identity=id_participant.id)");
		}
		return sb;
	}
	
	private NativeQueryBuilder appendUsersStatisticsSearchParams(SearchCoachedIdentityParams params, Map<String,Object> queryParams, NativeQueryBuilder sb) {
		if(params == null) return sb;
		
		if(params.getIdentityKey() != null) {
			sb.append(" and id_participant.id=:identityKey");
			queryParams.put("identityKey", params.getIdentityKey());
		}
		
		if(StringHelper.containsNonWhitespace(params.getLogin())) {
			String login = PersistenceHelper.makeFuzzyQueryString(params.getLogin());
			sb.append(" and (");
			
			if (login.contains("_") && dbInstance.isOracle()) {
				//oracle needs special ESCAPE sequence to search for escaped strings
				sb.append(" lower(id_participant.name) like :login ESCAPE '\\'");
			} else if (dbInstance.isMySQL()) {
				sb.append(" id_participant.name like :login");
			} else {
				sb.append(" lower(id_participant.name) like :login");
			}
			sb.append(" or ");
			if (login.contains("_") && dbInstance.isOracle()) {
				//oracle needs special ESCAPE sequence to search for escaped strings
				sb.append(" lower(user_participant.u_nickname) like :login ESCAPE '\\'");
			} else if (dbInstance.isMySQL()) {
				sb.append(" user_participant.u_nickname like :login");
			} else {
				sb.append(" lower(user_participant.u_nickname) like :login");
			}
			sb.append(")");

			queryParams.put("login", login);
		}
		
		if(params.getStatus() != null) {
			Integer status = params.getStatus();
			if (status.equals(Identity.STATUS_VISIBLE_LIMIT)) {
				sb.append(" and id_participant.status<").append(Identity.STATUS_VISIBLE_LIMIT);
			} else {
				sb.append(" and id_participant.status=:status");
				queryParams.put("status", params.getStatus());
			}
		}
		
		if(params.getUserProperties() != null && params.getUserProperties().size() > 0) {
			Map<String,String> searchParams = new HashMap<>(params.getUserProperties());
	
			int count = 0;
	
			for(Map.Entry<String, String> entry:searchParams.entrySet()) {
				String propName = entry.getKey();
				String propValue = entry.getValue();
				String qName = "p_" + ++count;
				
				UserPropertyHandler handler = userManager.getUserPropertiesConfig().getPropertyHandler(propName);
				if(handler instanceof GenericSelectionPropertyHandler && ((GenericSelectionPropertyHandler)handler).isMultiSelect()) {
					List<String> propValueList = GenericSelectionPropertyHandler.splitMultipleValues(propValue);
					if(!propValueList.isEmpty()) {
						sb.append(" and (");
						for(int i=0; i<propValueList.size(); i++) {
							String val = propValueList.get(i);
							String operand = (i > 0) ? "or" : "";
							appendUsersStatisticsSearchParams(handler, qName + "_" + i, val, operand, queryParams, sb);
						}
						sb.append(") ");
					}
					
				} else {
					appendUsersStatisticsSearchParams(handler, qName, propValue, "and", queryParams, sb);
				}
			}
		}
		
		if(params.hasOrganisations()) {
			sb.append(" and exists (select orgtomember.id from o_bs_group_member as orgtomember ")
			  .append("  inner join o_org_organisation as org on (org.fk_group=orgtomember.fk_group_id)")
			  .append("  where orgtomember.fk_identity_id=id_participant.id and org.id in (:organisationKey)");
			
			if (params.isIgnoreInheritedOrgMemberships()) {
				sb.append("  and orgtomember.g_inheritance_mode").in(GroupMembershipInheritance.root, GroupMembershipInheritance.none);
			}
			sb.append(")");
			Set<Long> organisationKeys = params.getOrganisations().stream().map(OrganisationRef::getKey).collect(Collectors.toSet());
			queryParams.put("organisationKey", organisationKeys);
		}

		return sb;
	}
	
	private void appendUsersStatisticsSearchParams(UserPropertyHandler handler, String qName, String propValue, String operand,
			Map<String,Object> queryParams, NativeQueryBuilder sb) {
		
		sb.append(" ").append(operand).append(" ");
		if(dbInstance.isMySQL()) {
			sb.append("user_participant.").append(handler.getDatabaseColumnName()).append(" like :").append(qName);
		} else {
			sb.append("lower(user_participant.").append(handler.getDatabaseColumnName()).append(") like :").append(qName);
			if(dbInstance.isOracle()) {
				sb.append(" escape '\\'");
			}
		}
		queryParams.put(qName, PersistenceHelper.makeFuzzyQueryString(propValue));
	}
	
	public List<Identity> getStudents(Identity coach) {
		StringBuilder sc = new StringBuilder();
		sc.append("select participantIdent from repositoryentry as re")
		  .append(" inner join re.groups as ownedRelGroup on ownedRelGroup.defaultGroup=true")
		  .append(" inner join ownedRelGroup.group as ownedGroup")
		  .append(" inner join ownedGroup.members as owner on owner.role='owner'")
		  .append(" inner join re.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" inner join participant.identity as participantIdent")
		  .append(" inner join fetch participantIdent.user as participantUser")
          .append(" where owner.identity.key=:coachKey and re.key=:repoKey");

		List<Identity> identityKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Identity.class)
				.setParameter("coachKey", coach.getKey())
				.getResultList();
		
		//owner see all participants
		if(identityKeys.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("select participantIdent from repoentrytogroup as relGroup ")
			  .append(" inner join relGroup.group as baseGroup")
			  .append(" inner join baseGroup.members as coach on coach.role = 'coach'")
			  .append(" inner join baseGroup.members as participant on participant.role='participant'")
			  .append(" inner join participant.identity as participantIdent")
			  .append(" inner join fetch participantIdent.user as participantUser")
	          .append(" where coach.identity.key=:coachKey and relGroup.entry.key=:repoKey");
	
			identityKeys = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Identity.class)
					.setParameter("coachKey", coach.getKey())
					.getResultList();
		}
		return new ArrayList<>(new HashSet<>(identityKeys));
	}
	
	public List<Identity> getStudents(Identity coach, RepositoryEntry entry) {
		StringBuilder sc = new StringBuilder();
		sc.append("select participantIdent from repositoryentry as re")
		  .append(" inner join re.groups as ownedRelGroup on ownedRelGroup.defaultGroup=true")
		  .append(" inner join ownedRelGroup.group as ownedGroup")
		  .append(" inner join ownedGroup.members as owner on owner.role='owner'")
		  .append(" inner join re.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" inner join participant.identity as participantIdent")
		  .append(" inner join fetch participantIdent.user as participantUser")
          .append(" where owner.identity.key=:coachKey and re.key=:repoKey");

		List<Identity> identityKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Identity.class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("repoKey", entry.getKey())
				.getResultList();
		
		//owner see all participants
		if(identityKeys.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("select participantIdent from repoentrytogroup as relGroup ")
			  .append(" inner join relGroup.group as baseGroup")
			  .append(" inner join baseGroup.members as coach on coach.role = 'coach'")
			  .append(" inner join baseGroup.members as participant on participant.role='participant'")
			  .append(" inner join participant.identity as participantIdent")
			  .append(" inner join fetch participantIdent.user as participantUser")
	          .append(" where coach.identity.key=:coachKey and relGroup.entry.key=:repoKey");
	
			identityKeys = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Identity.class)
					.setParameter("coachKey", coach.getKey())
					.setParameter("repoKey", entry.getKey())
					.getResultList();
		}
		return new ArrayList<>(new HashSet<>(identityKeys));
	}

	public List<RepositoryEntry> getStudentsCourses(Identity coach, Identity student, boolean fetch) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select re from repositoryentry as re")
		  .append(" inner join fetch re.olatResource res")
		  .append(" inner join fetch re.statistics as statistics", fetch)
		  .append(" left join fetch re.lifecycle as lifecycle", fetch)
		  .append(" left join fetch re.educationalType", fetch)
		  .append(" inner join re.groups as relGroup ")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as coach on coach.role='coach'")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" where coach.identity.key=:coachKey and participant.identity.key=:studentKey")
		  .append(" and res.resName='CourseModule' and re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed());

		List<RepositoryEntry> coachedEntries = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("studentKey", student.getKey())
				.getResultList();
		
		QueryBuilder sc = new QueryBuilder(1024);
		sc.append("select re from ").append(RepositoryEntry.class.getName()).append(" as re ")
		  .append(" inner join fetch re.olatResource res")
		  .append(" inner join fetch re.statistics as statistics", fetch)
		  .append(" left join fetch re.lifecycle as lifecycle", fetch)
		  .append(" left join fetch re.educationalType", fetch)
		  .append(" inner join re.groups as ownedRelGroup on ownedRelGroup.defaultGroup=true ")
		  .append(" inner join ownedRelGroup.group as ownedGroup")
		  .append(" inner join ownedGroup.members as owner on owner.role='owner'")
		  .append(" inner join re.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" where owner.identity.key=:coachKey and participant.identity.key=:studentKey")
		  .append(" and res.resName='CourseModule' and re.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed());

		List<RepositoryEntry> ownedEntries = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), RepositoryEntry.class)
				.setParameter("coachKey", coach.getKey())
				.setParameter("studentKey", student.getKey())
				.getResultList();
		
		Set<RepositoryEntry> uniqueRes = new HashSet<>(coachedEntries);
		uniqueRes.addAll(ownedEntries);
		return new ArrayList<>(uniqueRes);
	}
	
	public List<RepositoryEntry> getUserCourses(IdentityRef student, boolean fetch) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct(v) from repositoryentry as v ")
		  .append(" inner join fetch v.olatResource res")
		  .append(" inner join fetch v.statistics as statistics", fetch)
		  .append(" left join fetch v.lifecycle as lifecycle", fetch)
		  .append(" left join fetch v.educationalType", fetch)
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as participant on participant.role='participant'")
		  .append(" where res.resName='CourseModule' and v.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed()).append(" and participant.identity.key=:studentKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("studentKey", student.getKey())
				.getResultList();
	}

	private static final class GroupRepoKey {
		
		private final Long groupKey;
		private final Long repoKey;
		
		
		public GroupRepoKey(Long groupKey, Long repoKey) {
			super();
			this.groupKey = groupKey;
			this.repoKey = repoKey;
		}

		private Long getGroupKey() {
			return groupKey;
		}
		
		private Long getRepoKey() {
			return repoKey;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((groupKey == null) ? 0 : groupKey.hashCode());
			result = prime * result + ((repoKey == null) ? 0 : repoKey.hashCode());
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
			GroupRepoKey other = (GroupRepoKey) obj;
			if (groupKey == null) {
				if (other.groupKey != null)
					return false;
			} else if (!groupKey.equals(other.groupKey))
				return false;
			if (repoKey == null) {
				if (other.repoKey != null)
					return false;
			} else if (!repoKey.equals(other.repoKey))
				return false;
			return true;
		}
		
	}
}
