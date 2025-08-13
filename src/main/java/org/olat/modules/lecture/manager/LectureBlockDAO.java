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
package org.olat.modules.lecture.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.manager.AssessmentModeDAO;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.modules.lecture.model.LectureBlockToGroupImpl;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LectureCurriculumElementInfos;
import org.olat.modules.lecture.model.LectureCurriculumElementSearchParameters;
import org.olat.modules.lecture.model.LectureReportRow;
import org.olat.modules.lecture.model.LectureRepositoryEntryInfos;
import org.olat.modules.lecture.model.LectureRepositoryEntrySearchParameters;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.LecturesMemberSearchParameters;
import org.olat.modules.lecture.model.Reference;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.component.LectureBlockStatusCellRenderer.LectureBlockVirtualStatus;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureBlockDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private AssessmentModeDAO assessmentModeDao;
	
	public LectureBlock createLectureBlock(RepositoryEntry entry, CurriculumElement curriculumElement) {
		LectureBlockImpl block = new LectureBlockImpl();
		block.setCreationDate(new Date());
		block.setLastModified(block.getCreationDate());
		block.setStatus(LectureBlockStatus.active);
		block.setRollCallStatus(LectureRollCallStatus.open);
		block.setCompulsory(true);
		block.setEntry(entry);
		block.setCurriculumElement(curriculumElement);
		block.setGroups(new HashSet<>());
		block.setTaxonomyLevels(new HashSet<>());
		return block;
	}
	
	public LectureBlock update(LectureBlock block) {
		if(block.getKey() == null) {
			((LectureBlockImpl)block).setTeacherGroup(groupDao.createGroup());
			dbInstance.getCurrentEntityManager().persist(block);
		} else {
			block.setLastModified(new Date());
			block = dbInstance.getCurrentEntityManager().merge(block);
		}
		return block;
	}
	
	public List<LectureBlock> loadByKeys(List<Long> keys) {
		if(keys == null || keys.isEmpty()) return new ArrayList<>();
		
		String sb = """
				select block from lectureblock block
				left join fetch block.reasonEffectiveEnd reason
				inner join fetch block.entry entry
				where block.key in (:blockKeys)""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, LectureBlock.class)
				.setParameter("blockKeys", keys)
				.getResultList();
	}
	
	public LectureBlock loadByKey(Long key) {
		List<LectureBlock> blocks = dbInstance.getCurrentEntityManager()
				.createNamedQuery("lectureBlockByKey", LectureBlock.class)
				.setParameter("blockKey", key)
				.getResultList();
		return blocks == null || blocks.isEmpty() ? null : blocks.get(0);
	}
	
	/**
	 * Delete the relation to group, the roll call, the reminders and at the
	 * end the lecture block itself.
	 * 
	 * @param lectureBlock The block to delete
	 * @return The number of rows deleted
	 */
	public int delete(LectureBlock lectureBlock) {
		LectureBlock reloadedBlock = dbInstance.getCurrentEntityManager()
			.getReference(LectureBlockImpl.class, lectureBlock.getKey());
		
		AssessmentMode assessmentMode = assessmentModeDao.getAssessmentModeByLecture(reloadedBlock);
		if(assessmentMode != null) {
			assessmentModeDao.delete(assessmentMode);
		}
		
		//delete lecture block to group
		String deleteToGroup = "delete from lectureblocktogroup blocktogroup where blocktogroup.lectureBlock.key=:lectureBlockKey";
		int rows = dbInstance.getCurrentEntityManager()
			.createQuery(deleteToGroup)
			.setParameter("lectureBlockKey", reloadedBlock.getKey())
			.executeUpdate();
		
		//delete LectureBlockRollCallImpl
		String deleteRollCall = "delete from lectureblockrollcall rollcall where rollcall.lectureBlock.key=:lectureBlockKey";
		rows += dbInstance.getCurrentEntityManager()
			.createQuery(deleteRollCall)
			.setParameter("lectureBlockKey", reloadedBlock.getKey())
			.executeUpdate();
		
		//delete LectureBlockReminderImpl
		String deleteReminder = "delete from lecturereminder reminder where reminder.lectureBlock.key=:lectureBlockKey";
		rows += dbInstance.getCurrentEntityManager()
			.createQuery(deleteReminder)
			.setParameter("lectureBlockKey", reloadedBlock.getKey())
			.executeUpdate();
		
		//delete LectureBlockToTaxonomyLevelImpl
		String deleteTaxanomyLevels = "delete from lectureblocktotaxonomylevel relTax where relTax.lectureBlock.key=:lectureBlockKey";
		rows += dbInstance.getCurrentEntityManager()
			.createQuery(deleteTaxanomyLevels)
			.setParameter("lectureBlockKey", reloadedBlock.getKey())
			.executeUpdate();

		dbInstance.getCurrentEntityManager()
			.remove(reloadedBlock);
		rows++;

		return rows;
	}
	
	public List<LectureBlock> getLectureBlocks() {
		StringBuilder sb = new StringBuilder();
		sb.append("select block from lectureblock block")
		  .append(" inner join fetch block.entry entry");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlock.class)
				.getResultList();
	}
	
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("lectureBlocksByRepositoryEntry", LectureBlock.class)
				.setParameter("repoEntryKey", entry.getKey())
				.getResultList();
	}
	
	public boolean hasLectureBlocks(RepositoryEntryRef entry) {
		String query = "select block.key from lectureblock block where block.entry.key=:repoEntryKey";	
		List<Long> firstBlock = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("repoEntryKey", entry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return firstBlock != null && !firstBlock.isEmpty()
				&& firstBlock.get(0) != null && firstBlock.get(0).longValue() > 0;
	}
	
	public List<LectureBlock> getLectureBlocksUpToRepositoryEntries(CurriculumElementRef curriculumElement) {
		String query = """
				select block from lectureblock block
				where block.curriculumElement.key=:curriculumElementKey
				or block.entry.key in (select v.key from repositoryentry v
				  inner join v.groups as baseGroups
				  inner join baseGroups.group as baseGroup
				  inner join curriculumelement as curEl on (curEl.group.key=baseGroup.key)
				  where curEl.key = :curriculumElementKey
				)""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, LectureBlock.class)
				.setParameter("curriculumElementKey", curriculumElement.getKey())
				.getResultList();
	}
	
	public List<LectureBlock> getLectureBlocks(CurriculumElementRef curriculumElement) {
		String query = """
				select block from lectureblock block
				where block.curriculumElement.key=:curriculumElementKey
				or block.entry.key in (select v.key from repositoryentry v
				  inner join v.groups as baseGroups
				  inner join baseGroups.group as baseGroup
				  inner join curriculumelement as curEl on (curEl.group.key=baseGroup.key)
				  where curEl.key = :curriculumElementKey
				)""";
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, LectureBlock.class)
				.setParameter("curriculumElementKey", curriculumElement.getKey())
				.getResultList();
	}
	
	public long countLectureBlocks(LecturesBlockSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select count(distinct block.key) from lectureblock block")
		  .append(" ").append("inner", "left", searchParams.isLectureConfiguredRepositoryEntry()).append(" join block.entry entry")
		  .append(" ").append("inner", "left", searchParams.isLectureConfiguredRepositoryEntry()).append(" join entry.olatResource oRes")
		  .append(" left join block.teacherGroup tGroup")
		  .append(" left join block.curriculumElement curEl")
		  .append(" left join curEl.curriculum cur")
		  .append(" left join cur.organisation organis")
		  .append(" left join lectureentryconfig as config on (config.entry.key=entry.key)");
		addSearchParametersToQuery(sb, searchParams);
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addSearchParametersToQuery(query, searchParams);
		List<Long> count = query.getResultList();
		return count == null || count.isEmpty() || count.get(0) == null ? 0l : count.get(0).longValue();
	}
	
	public List<LectureBlock> searchLectureBlocks(LecturesBlockSearchParameters searchParams, int maxResults, Boolean orderAsc) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select distinct block from lectureblock block")
		  .append(" ").append("inner", "left", searchParams.isLectureConfiguredRepositoryEntry()).append(" join fetch block.entry entry")
		  .append(" ").append("inner", "left", searchParams.isLectureConfiguredRepositoryEntry()).append(" join fetch entry.olatResource oRes")
		  .append(" left join fetch block.teacherGroup tGroup")
		  .append(" left join block.curriculumElement curEl")
		  .append(" left join curEl.curriculum cur")
		  .append(" left join cur.organisation organis")
		  .append(" left join lectureentryconfig as config on (config.entry.key=entry.key)");

		addSearchParametersToQuery(sb, searchParams);
		if(orderAsc != null) {
			sb.append(" order by block.startDate ").append("asc", "desc", orderAsc.booleanValue());
		}

		TypedQuery<LectureBlock> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlock.class);
		addSearchParametersToQuery(query, searchParams);
		if(maxResults > 0) {
			query.setFirstResult(0)
			     .setMaxResults(maxResults);
		}
		return query.getResultList();
	}
	
	public List<LectureBlockRef> searchAssessedLectureBlocks(LecturesBlockSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select distinct block.key from lectureblock block")
		  .append(" inner join courseassessmentmode mode on (mode.lectureBlock.key=block.key)")
		  .append(" inner join block.entry entry")
		  .append(" left join block.curriculumElement curEl")
		  .append(" left join curEl.curriculum cur")
		  .append(" left join cur.organisation organis")
		  .append(" left join block.teacherGroup tGroup");
		addSearchParametersToQuery(sb, searchParams);
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class);
		addSearchParametersToQuery(query, searchParams);
		List<Long> blockKeys = query.getResultList();
		return blockKeys.stream().map(LectureBlockRefImpl::new).collect(Collectors.toList());
	}
	
	public List<LectureRepositoryEntryInfos> searchRepositoryEntries(LectureRepositoryEntrySearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select v,")
		  .append(" (select count(distinct participants.key) from repoentrytogroup as rel, bgroupmember as participants")
		  .append("  where v.key=rel.entry.key and rel.group.key=participants.group.key and participants.role='").append(GroupRoles.participant.name()).append("'")
		  .append("  and exists (select blockToGroup.key from lectureblocktogroup blockToGroup where blockToGroup.group.key=rel.group.key) ")
		  .append(" ) as numOfParticipants")
		  .append(" from lectureentryconfig config")
		  .append(" inner join config.entry as v")
		  .append(" inner join fetch v.olatResource as res" )
		  .append(" left join fetch v.lifecycle as lifecycle")
		  .append(" where config.lectureEnabled=true");
		
		if(searchParams.getTeacher() != null) {
			sb.append(" and exists (select block.key from lectureblock block")
			  .append("   inner join block.teacherGroup tGroup")
			  .append("   inner join tGroup.members membership")
			  .append("   where block.entry.key=v.key and membership.identity.key=:teacherKey")
			  .append(" )");
		}
		
		//quick search in repository entry infos
		Long quickId = null;
		String quickRefs = null;
		String quickText = null;
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			quickRefs = searchParams.getSearchString();
			sb.append(" and (v.externalId=:quickRef or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.externalRef", "quickText", dbInstance.getDbVendor());
			sb.append(" or v.softkey=:quickRef or ");
			quickText = PersistenceHelper.makeFuzzyQueryString(quickRefs);
			PersistenceHelper.appendFuzzyLike(sb, "v.displayname", "quickText", dbInstance.getDbVendor());
			if(StringHelper.isLong(quickRefs)) {
				try {
					quickId = Long.parseLong(quickRefs);
					sb.append(" or v.key=:quickVKey or res.resId=:quickVKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}
		
		//TODO absences coach
		if(searchParams.getManager() != null || searchParams.getMasterCoach() != null) {
			sb.append(" and exists (select membership.key from repoentrytogroup as rel, bgroupmember as membership")
	          .append("    where v.key=rel.entry.key and rel.group.key=membership.group.key and membership.identity.key=:managerKey")
	          .append("    and membership.role");
			if(searchParams.getManager() != null && searchParams.getMasterCoach() != null) {
				sb.in(OrganisationRoles.administrator, OrganisationRoles.lecturemanager, CurriculumRoles.mastercoach, GroupRoles.owner, GroupRoles.coach);
			} else if(searchParams.getManager() != null ) {
				sb.in(OrganisationRoles.administrator, OrganisationRoles.lecturemanager, GroupRoles.owner, GroupRoles.coach);
			} else if(searchParams.getMasterCoach() != null) {
				sb.in(CurriculumRoles.mastercoach);
			}
	        sb.append("  )");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(searchParams.getTeacher() != null) {
			query.setParameter("teacherKey", searchParams.getTeacher().getKey());
		}
		if(searchParams.getManager() != null) {
			query.setParameter("managerKey", searchParams.getManager().getKey());
		} else if(searchParams.getMasterCoach() != null) {
			query.setParameter("managerKey", searchParams.getMasterCoach().getKey());
		}
		
		if(quickId != null) {
			query.setParameter("quickVKey", quickId);
		}
		if(quickRefs != null) {
			query.setParameter("quickRef", quickRefs);
		}
		if(quickText != null) {
			query.setParameter("quickText", quickText);
		}
		
		List<Object[]> rawObjects = query.getResultList();
		return rawObjects.stream()
			.map(objects -> new LectureRepositoryEntryInfos((RepositoryEntry)objects[0], PersistenceHelper.extractPrimitiveLong(objects, 1)))
			.collect(Collectors.toList());
	}
	
	public List<LectureCurriculumElementInfos> searchCurriculumElements(LectureCurriculumElementSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select curEl, curElType,")
		  .append(" (select count(distinct participants.key) from bgroupmember as participants")
		  .append("   where curEl.group.key=participants.group.key and participants.role='").append(GroupRoles.participant.name()).append("'")
		  .append("   and exists (select blockToGroup.key from lectureblocktogroup blockToGroup where blockToGroup.group.key=curEl.group.key) ")
		  .append(" ) as numOfParticipants")
		  .append(" from curriculumelement curEl")
		  .append(" left join curEl.type curElType")
		  .append(" inner join fetch curEl.group curElGroup")
		  .append(" inner join fetch curEl.curriculum cur")
		  .append(" left join fetch cur.organisation organis")
		  .append(" where (exists (select v.key from repositoryentry as v")
		  .append("   inner join v.groups as relGroup")
		  .append("   inner join lectureentryconfig config on (config.entry.key=v.key)")
		  .append("   where relGroup.group.key=curElGroup.key and config.lectureEnabled=true")
		  .append("  )")
		  .append("  or curEl.lecturesEnabledString='enabled' or curElType.lecturesEnabledString='enabled'")
		  .append(" )");
		// generic search
		Long key = null;
		String ref = null;
		String fuzzyRef = null;
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			ref = searchParams.getSearchString();
			fuzzyRef = PersistenceHelper.makeFuzzyQueryString(ref);
			
			sb.append(" and (cur.externalId=:ref or curEl.externalId=:ref or ")
			  .likeFuzzy("cur.displayName", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("cur.identifier", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("curEl.displayName", "fuzzyRef")
			  .append(" or ")
			  .likeFuzzy("curEl.identifier", "fuzzyRef");
			if(StringHelper.isLong(ref)) {
				key = Long.valueOf(ref);
				sb.append(" or cur.key=:cKey or curEl.key=:cKey");
			}
			sb.append(")");	
		}
		
		if(searchParams.getManager() != null) {
			sb.append(" and exists (select membership.key from bgroupmember as membership")
			  .append("  where membership.identity.key=:managerKey")
			  .append("  and membership.role").in(OrganisationRoles.administrator, LectureRoles.lecturemanager)
			  .append("  and (membership.group.key=curElGroup.key or membership.group.key=organis.group.key)")
			  .append(" )");
		}
		
		if(searchParams.getMasterCoach() != null) {
			sb.append(" and exists (select masterCoachMembership.key from bgroupmember as masterCoachMembership")
			  .append("  where curElGroup.key=masterCoachMembership.group.key and masterCoachMembership.identity.key=:masterCoachKey")
			  .append("  and masterCoachMembership.role ").in(LectureRoles.mastercoach)
			  .append(" )");
		}
		
		if(searchParams.getTeacher() != null) {
			sb.append(" and exists (select teacherMembership.key from lectureblock as block")
			  .append(" inner join block.teacherGroup tGroup")
			  .append(" inner join tGroup.members teacherMembership")
			  .append(" inner join repoentrytogroup as rel on (block.entry.key=rel.entry.key)")
			  .append(" where curElGroup.key=rel.group.key and teacherMembership.identity.key=:teacherKey")
			  .append("  and teacherMembership.role ").in(LectureRoles.teacher)
			  .append(" )");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(key != null) {
			query.setParameter("cKey", key);
		}
		if(ref != null) {
			query.setParameter("ref", ref);
		}
		if(fuzzyRef != null) {
			query.setParameter("fuzzyRef", fuzzyRef);
		}
		if(searchParams.getManager() != null) {
			query.setParameter("managerKey", searchParams.getManager().getKey());
		}
		if(searchParams.getMasterCoach() != null) {
			query.setParameter("masterCoachKey", searchParams.getMasterCoach().getKey());
		}
		if(searchParams.getTeacher() != null) {
			query.setParameter("teacherKey", searchParams.getTeacher().getKey());
		}
		
		List<Object[]> rawObjects = query.getResultList();
		return rawObjects.stream().map(objects
				-> new LectureCurriculumElementInfos((CurriculumElement)objects[0], (CurriculumElementType)objects[1], PersistenceHelper.extractPrimitiveLong(objects, 2)))
				.collect(Collectors.toList());
	}
	
	public List<LectureReportRow> getLecturesBlocksReport(Date from, Date to, List<LectureRollCallStatus> status) {
		// search blocks and coaches
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select block, teacher")
		  .append(" from lectureblock block")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" left join tGroup.members membership on (membership.role='").append("teacher").append("')")
		  .append(" left join membership.identity teacher")
		  .append(" left join fetch teacher.user userteacher")
		  .append(" inner join lectureentryconfig config on (config.entry.key=block.entry.key)")
		  .append(" where config.lectureEnabled=true");
		
		if(from != null) {
			sb.append(" and block.startDate>=:startDate");
		}
		if(to != null) {
			sb.append(" and block.endDate<=:endDate");
		}
		if(status != null && !status.isEmpty()) {
			sb.append(" and block.rollCallStatusString in (:status)");
		}

		//get all, it's quick
		TypedQuery<Object[]> rawCoachQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(from != null) {
			rawCoachQuery.setParameter("startDate", from, TemporalType.TIMESTAMP);
		}
		if(to != null) {
			rawCoachQuery.setParameter("endDate", to, TemporalType.TIMESTAMP);
		}
		if(status != null && !status.isEmpty()) {
			List<String> statusStrings = status.stream()
					.map(LectureRollCallStatus::name).collect(Collectors.toList());
			rawCoachQuery.setParameter("status", statusStrings);
		}

		List<Object[]> rawCoachs = rawCoachQuery.getResultList();
		Map<Long,LectureReportRow> blockMap = new HashMap<>();
		for(Object[] rawCoach:rawCoachs) {
			LectureBlock block = (LectureBlock)rawCoach[0];
			Identity teacher = (Identity)rawCoach[1];
			LectureReportRow row = blockMap
					.computeIfAbsent(block.getKey(), b -> new LectureReportRow(block));
			if(teacher != null && !row.getTeachers().contains(teacher)) {
				row.getTeachers().add(teacher);
			}
		}
		
		enrichLecturesBlocksReport(from, to, status, blockMap);
		return new ArrayList<>(blockMap.values());
	}
	
	private void enrichLecturesBlocksReport(Date from, Date to, List<LectureRollCallStatus> status, Map<Long,LectureReportRow> blockMap) {
		// search blocks and coaches
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select block.key, entry.externalRef, owner")
		  .append(" from lectureblock block")
		  .append(" inner join block.entry as entry")
		  .append(" inner join entry.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" left join baseGroup.members as membership on (membership.role='").append(GroupRoles.owner).append("')")
		  .append(" left join membership.identity owner")
		  .append(" left join fetch owner.user userowner");
		if(from != null) {
			sb.and().append(" block.startDate>=:startDate");
		}
		if(to != null) {
			sb.and().append(" block.endDate<=:endDate");
		}
		if(status != null && !status.isEmpty()) {
			sb.and().append(" block.rollCallStatusString in (:status)");
		}

		//get all, it's quick
		TypedQuery<Object[]> rawCoachQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(from != null) {
			rawCoachQuery.setParameter("startDate", from, TemporalType.TIMESTAMP);
		}
		if(to != null) {
			rawCoachQuery.setParameter("endDate", to, TemporalType.TIMESTAMP);
		}
		if(status != null && !status.isEmpty()) {
			List<String> statusStrings = status.stream()
					.map(LectureRollCallStatus::name).collect(Collectors.toList());
			rawCoachQuery.setParameter("status", statusStrings);
		}

		List<Object[]> rawRepos = rawCoachQuery.getResultList();
		for(Object[] rawRepo:rawRepos) {
			Long blockKey = (Long)rawRepo[0];
			String externalRef = (String)rawRepo[1];
			Identity owner = (Identity)rawRepo[2];
			LectureReportRow row = blockMap.get(blockKey);
			if(row != null) {
				row.setExternalRef(externalRef);
				if(owner != null && !row.getOwners().contains(owner)) {
					row.getOwners().add(owner);
				}
			}
		}
	}
	
	/**
	 * The method doesn't fill the configuration.
	 * 
	 * @param entry The course (mandatory)
	 * @return
	 */
	public List<LectureBlockWithTeachers> getLecturesBlockWithTeachers(RepositoryEntryRef entry, RepositoryEntryLectureConfiguration config) {
		List<LectureBlock> blocks = getLectureBlocks(entry);
		
		// assessed lectures blocks
		StringBuilder sb = new StringBuilder(256);
		sb.append("select distinct block.key from lectureblock block")
		  .append(" inner join courseassessmentmode mode on (mode.lectureBlock.key=block.key)")
		  .append(" where block.entry.key=:entryKey");

		List<Long> assessedBlockKeys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
		Set<Long> assessedBlockKeySet = new HashSet<>(assessedBlockKeys);
		
		Map<Long,LectureBlockWithTeachers> blockMap = new HashMap<>();
		for(LectureBlock block:blocks) {
			blockMap.put(block.getKey(), new LectureBlockWithTeachers(block, config,
					null, null, -1l, -1l, -1l, assessedBlockKeySet.contains(block.getKey())));
		}
		
		// append the coaches
		StringBuilder sc = new StringBuilder();
		sc.append("select block.key, coach")
		  .append(" from lectureblock block")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" inner join tGroup.members membership")
		  .append(" inner join membership.identity coach")
		  .append(" inner join fetch coach.user usercoach")
		  .append(" where membership.role='").append("teacher").append("' and block.entry.key=:repoEntryKey");

		//get all, it's quick
		List<Object[]> rawCoachs = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Object[].class)
				.setParameter("repoEntryKey", entry.getKey())
				.getResultList();
		for(Object[] rawCoach:rawCoachs) {
			Long blockKey = (Long)rawCoach[0];
			Identity coach = (Identity)rawCoach[1];
			LectureBlockWithTeachers block = blockMap.get(blockKey);
			if(block != null) {
				block.getTeachers().add(coach);
			}
		}
		return new ArrayList<>(blockMap.values());
	}
	
	/**
	 * Search lecture blocks with their teachers. Presence of a teacher is mandatory
	 * to return a lecture block. Lecture block without are not returned.
	 * 
	 * @param searchParams The search parameters
	 * @return Lecture blocks with their teachers
	 */
	public List<LectureBlockWithTeachers> getLecturesBlockWithTeachers(LecturesBlockSearchParameters searchParams) {
		QueryBuilder sc = new QueryBuilder(2048);
		sc.append("select block, coach, config, mode.key, mode.leadTime, mode.followupTime,")
		  .append(" entry.key as vKey,")
		  .append(" entry.displayname as vDisplayName,")
		  .append(" entry.externalRef as vExternalRef,")
		  .append(" curEl.key as cKey,")
		  .append(" curEl.displayName as cDisplayName,")
		  .append(" curEl.identifier as cIdentifier,")
		  .append(" (select count(distinct participant.identity.key) from lectureblocktogroup as ltogroup")
		  .append("   inner join ltogroup.group lgroup")
		  .append("   inner join lgroup.members as participant")
		  .append("   where ltogroup.lectureBlock.key=block.key and participant.role='").append(GroupRoles.participant).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" teamsMeeting.leadTime, teamsMeeting.followupTime, bbbMeeting.leadTime, bbbMeeting.followupTime")
		  .append(" from lectureblock block")
		  .append(" inner join fetch block.entry entry")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" inner join tGroup.members membership")
		  .append(" inner join membership.identity coach")
		  .append(" inner join fetch coach.user usercoach")
		  .append(" left join block.curriculumElement curEl")
		  .append(" left join curEl.curriculum cur")
		  .append(" left join cur.organisation organis")
		  .append(" left join courseassessmentmode mode on (mode.lectureBlock.key=block.key)")
		  .append(" inner join lectureentryconfig as config on (config.entry.key=entry.key)")
		  .append(" left join block.teamsMeeting teamsMeeting")
		  .append(" left join block.bbbMeeting bbbMeeting")
		  .where().append(" membership.role='").append("teacher").append("'")
		  .and().append(" config.lectureEnabled=true");
		addSearchParametersToQuery(sc, searchParams);

		//get all, it's quick
		TypedQuery<Object[]> coachQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Object[].class);
		
		addSearchParametersToQuery(coachQuery, searchParams);
		
		List<Object[]> rawCoachs = coachQuery.getResultList();
		Map<Long,LectureBlockWithTeachers> blockMap = new HashMap<>();
		for(Object[] rawCoach:rawCoachs) {
			LectureBlock block = (LectureBlock)rawCoach[0];
			Identity coach = (Identity)rawCoach[1];
			RepositoryEntryLectureConfiguration lectureConfiguration = (RepositoryEntryLectureConfiguration)rawCoach[2];
			boolean assessmentMode = rawCoach[3] != null;
			Long assessmentModeLeadTime = PersistenceHelper.extractLong(rawCoach, 4);
			Long assessmentModeFollowUpTime = PersistenceHelper.extractLong(rawCoach, 5);
			Reference entryRef = new Reference((Long)rawCoach[6], (String)rawCoach[7], (String)rawCoach[8]);
			Reference elementRef = new Reference((Long)rawCoach[9], (String)rawCoach[10], (String)rawCoach[11]);
			long numOfParticipants = PersistenceHelper.extractPrimitiveLong(rawCoach, 12);
			Long teamsMeetingLeadTime = PersistenceHelper.extractLong(rawCoach, 13);
			Long teamsMeetingFollowUpTime = PersistenceHelper.extractLong(rawCoach, 14);
			Long bigBlueButtonMeetingLeadTime = PersistenceHelper.extractLong(rawCoach, 15);
			Long bigBlueButtonMeetingFollowUpTime = PersistenceHelper.extractLong(rawCoach, 16);
			long leadTime = time(assessmentModeLeadTime, teamsMeetingLeadTime, bigBlueButtonMeetingLeadTime);
			long followupTime = time(assessmentModeFollowUpTime, teamsMeetingFollowUpTime, bigBlueButtonMeetingFollowUpTime);
			
			LectureBlockWithTeachers blockWith = blockMap.computeIfAbsent(block.getKey(), key ->
				new LectureBlockWithTeachers(block, lectureConfiguration,
						elementRef, entryRef, numOfParticipants, leadTime, followupTime, assessmentMode));
			blockWith.getTeachers().add(coach);
		}
		return new ArrayList<>(blockMap.values());
	}
	
	public LectureBlockRef getNextScheduledLectureBlock(LecturesBlockSearchParameters searchParams) {
		QueryBuilder sc = new QueryBuilder(2048);
		sc.append("select block.key")
		  .append(" from lectureblock block")
		  .append(" left join block.teacherGroup tGroup")
		  .append(" left join tGroup.members membership")
		  .append(" left join block.entry entry")
		  .append(" left join block.curriculumElement curEl")
		  .append(" left join curEl.curriculum cur")
		  .append(" left join cur.organisation organis")
		  .append(" left join lectureentryconfig as config on (config.entry.key=entry.key)");
		addSearchParametersToQuery(sc, searchParams);
		sc.append(" order by block.startDate asc");
		
		TypedQuery<Long> nextQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Long.class);
		
		addSearchParametersToQuery(nextQuery, searchParams);
		
		List<Long> nextKeys = nextQuery
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		if(nextKeys != null && !nextKeys.isEmpty() && nextKeys.get(0) != null && nextKeys.get(0).longValue() > 0) {
			return new LectureBlockRefImpl(nextKeys.get(0));
		}
		return null;
	}
	
	/**
	 * 
	 * 
	 * @param searchParams The search parameters
	 * @return The lecture blocks and their coaches if any
	 */
	public List<LectureBlockWithTeachers> getLecturesBlockWithOptionalTeachers(LecturesBlockSearchParameters searchParams) {
		QueryBuilder sc = new QueryBuilder(2048);
		sc.append("select block, coach, config, mode.key, mode.leadTime, mode.followupTime,")
		  .append(" entry.key as vKey,")
		  .append(" entry.displayname as vDisplayName,")
		  .append(" entry.externalRef as vExternalRef,")
		  .append(" curEl.key as cKey,")
		  .append(" curEl.displayName as cDisplayName,")
		  .append(" curEl.identifier as cIdentifier,")
		  .append(" (select count(distinct participant.identity.key) from lectureblocktogroup as ltogroup")
		  .append("   inner join ltogroup.group lgroup")
		  .append("   inner join lgroup.members as participant")
		  .append("   where ltogroup.lectureBlock.key=block.key and participant.role='").append(GroupRoles.participant).append("'")
		  .append(" ) as numOfParticipants,")
		  .append(" teamsMeeting.leadTime, teamsMeeting.followupTime, bbbMeeting.leadTime, bbbMeeting.followupTime")
		  .append(" from lectureblock block")
		  .append(" left join block.teacherGroup tGroup")
		  .append(" left join tGroup.members membership")
		  .append(" left join bidentity as coach on (membership.identity.key=coach.key and membership.role='").append("teacher").append("')")
		  .append(" left join fetch coach.user usercoach")
		  .append(" left join courseassessmentmode mode on (mode.lectureBlock.key=block.key)")
		  .append(" left join fetch block.entry entry")
		  .append(" left join fetch block.curriculumElement curEl")
		  .append(" left join curEl.curriculum cur")
		  .append(" left join cur.organisation organis")
		  .append(" left join lectureentryconfig as config on (config.entry.key=entry.key)")
		  .append(" left join block.teamsMeeting teamsMeeting")
		  .append(" left join block.bbbMeeting bbbMeeting");
		addSearchParametersToQuery(sc, searchParams);

		//get all, it's quick
		TypedQuery<Object[]> coachQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Object[].class);
		
		addSearchParametersToQuery(coachQuery, searchParams);
		
		List<Object[]> rawCoachs = coachQuery.getResultList();
		Map<Long,LectureBlockWithTeachers> blockMap = new HashMap<>();
		for(Object[] rawCoach:rawCoachs) {
			LectureBlock block = (LectureBlock)rawCoach[0];
			Identity coach = (Identity)rawCoach[1];
			RepositoryEntryLectureConfiguration lectureConfiguration = (RepositoryEntryLectureConfiguration)rawCoach[2];
			boolean assessmentMode = rawCoach[3] != null;
			Long assessmentModeLeadTime = PersistenceHelper.extractLong(rawCoach, 4);
			Long assessmentModeFollowUpTime = PersistenceHelper.extractLong(rawCoach, 5);
			Reference entryRef = new Reference((Long)rawCoach[6], (String)rawCoach[7], (String)rawCoach[8]);
			Reference elementRef = new Reference((Long)rawCoach[9], (String)rawCoach[10], (String)rawCoach[11]);
			long numOfParticipants = PersistenceHelper.extractPrimitiveLong(rawCoach, 12);
			Long teamsMeetingLeadTime = PersistenceHelper.extractLong(rawCoach, 13);
			Long teamsMeetingFollowUpTime = PersistenceHelper.extractLong(rawCoach, 14);
			Long bigBlueButtonMeetingLeadTime = PersistenceHelper.extractLong(rawCoach, 15);
			Long bigBlueButtonMeetingFollowUpTime = PersistenceHelper.extractLong(rawCoach, 16);
			long leadTime = time(assessmentModeLeadTime, teamsMeetingLeadTime, bigBlueButtonMeetingLeadTime);
			long followupTime = time(assessmentModeFollowUpTime, teamsMeetingFollowUpTime, bigBlueButtonMeetingFollowUpTime);
			
			LectureBlockWithTeachers blockWith = blockMap.computeIfAbsent(block.getKey(), key ->
					new LectureBlockWithTeachers(block, lectureConfiguration,
							elementRef, entryRef, numOfParticipants, leadTime, followupTime, assessmentMode));
			if(coach != null) {
				blockWith.getTeachers().add(coach);
			}
		}
		return new ArrayList<>(blockMap.values());
	}
	
	private long time(Long assessmentModeTime, Long teamsMeetingTime, Long bigBlueButtonMeetingTime) {
		if(assessmentModeTime != null && assessmentModeTime.longValue() > 0l) {
			return assessmentModeTime.longValue();
		}
		if(bigBlueButtonMeetingTime != null && bigBlueButtonMeetingTime.longValue() > 0l) {
			return bigBlueButtonMeetingTime.longValue();
		}
		if(teamsMeetingTime != null && teamsMeetingTime.longValue() > 0l) {
			return teamsMeetingTime.longValue();
		}
		return 0;
	}
	
	private void addSearchParametersToQuery(QueryBuilder sb, LecturesBlockSearchParameters searchParams) {
		if(searchParams == null) return;

		if(searchParams.getCurriculumElement() != null) {
			sb.and().append(" (curEl.key=:curriculumElementKey or entry.key in (select rel.entry.key from repoentrytogroup as rel")
			  .append(" inner join curriculumelement entryEl on (entryEl.group.key=rel.group.key)")
			  .append(" where entryEl.key=:curriculumElementKey")
			  .append("))");
		} else if(StringHelper.containsNonWhitespace(searchParams.getCurriculumElementPath())) {
			sb.and().append(" (curEl.materializedPathKeys like :curriculumElementPath or entry.key in (select rel.entry.key from repoentrytogroup as rel")
			  .append(" inner join curriculumelement entryEl on (entryEl.group.key=rel.group.key)")
			  .append(" where entryEl.materializedPathKeys like :curriculumElementPath")
			  .append("))");
		} else if(searchParams.getRepositoryEntry() != null) {
			sb.and().append(" entry.key=:repoEntryKey and config.lectureEnabled=true");
		} else {
			sb.and().append("(");
			if(!searchParams.isLectureConfiguredRepositoryEntry()) {
				sb.append("entry.key is null or ");
			}
			sb.append("entry.status not ").in(RepositoryEntryStatusEnum.deleted()).append(")");
		}
		
		if(searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) {
			sb.and()
			  .append("((cur.key in (:curriculumKeys) and curEl.status ").in(CurriculumElementStatus.notDeleted()).append(")")
			  .append(" or entry.key in (select crel.entry.key from repoentrytogroup as crel")
			  .append(" inner join curriculumelement centryEl on (centryEl.group.key=crel.group.key)")
			  .append(" where centryEl.curriculum.key in (:curriculumKeys)")
			  .append(" and centryEl.status ").in(CurriculumElementStatus.notDeleted())
			  .append("))");
		}
		
		if(searchParams.isInSomeCurriculum()) {
			sb.and()
			  .append("((curEl.key is not null and curEl.status ").in(CurriculumElementStatus.notDeleted()).append(")")
			  .append(" or entry.key in (select screl.entry.key from repoentrytogroup as screl")
			  .append("  inner join curriculumelement scentryEl on (scentryEl.group.key=screl.group.key)")
			  .append("  and scentryEl.status ").in(CurriculumElementStatus.notDeleted())
			  .append("))");
		}
		
		if(searchParams.isLectureConfiguredRepositoryEntry()) {
			sb.and()
			  .append(" exists (select config.key from lectureentryconfig config")
			  .append("   where config.entry.key=entry.key and config.lectureEnabled=true")
			  .append(" )");
		}
		
		if(searchParams.getLectureBlocks() != null && !searchParams.getLectureBlocks().isEmpty()) {
			sb.and().append(" block.key in (:lectureBlockKeys)");
		}
		
		if(searchParams.getLectureBlockStatus() != null && !searchParams.getLectureBlockStatus().isEmpty()) {
			sb.and().append(" block.statusString in (:lectureBlockStatus)");
		}
		
		if(searchParams.getRollCallStatus() != null && !searchParams.getRollCallStatus().isEmpty()) {
			sb.and().append(" block.rollCallStatusString in (:rollCallStatus)");
		}
		
		if(searchParams.getVirtualStatus() != null && !searchParams.getVirtualStatus().isEmpty()) {
			sb.and().append("(");
			for(int i=0; i<searchParams.getVirtualStatus().size(); i++) {
				if(i > 0) {
					sb.append(" or ");
				}
				LectureBlockVirtualStatus status = searchParams.getVirtualStatus().get(i);
				addSearchVirtualStatusToQuery(sb, status);
			}
			sb.append(")");
		}

		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			sb.and()
			  .append("(")
			  .likeFuzzy("entry.externalRef", "fuzzySearchString")
			  .append(" or ")
			  .likeFuzzy("entry.displayname", "fuzzySearchString")
			  .append(" or ")
			  .likeFuzzy("block.title", "fuzzySearchString")
			  .append(")");
		}
		
		if(searchParams.getStartDate() != null) {
			sb.and().append(" block.startDate>=:startDate");
		}
		if(searchParams.getEndDate() != null) {
			sb.and().append(" block.endDate<=:endDate");
		}
		if(searchParams.getManager() != null) {
			sb.and()
			// Course
			  .append(" (exists (select managerMembership.key from repoentrytogroup as rel, bgroupmember as managerMembership")
	          .append("   where rel.entry.key=entry.key and rel.group.key=managerMembership.group.key and managerMembership.identity.key=:managerKey")
	          .append("   and managerMembership.role ").in(OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.lecturemanager, GroupRoles.owner.name())
	          .append(" )");
			// Curriculum element
			sb.append(" or exists (select elementOwner.key from bgroupmember as elementOwner")
	          .append("   where curEl.group.key=elementOwner.group.key and elementOwner.identity.key=:managerKey")
	          .append("   and elementOwner.role ").in(CurriculumRoles.curriculumelementowner)
	          .append(" )");
			// Curriculum
			sb.append(" or exists (select curriculumOwner.key from bgroupmember as curriculumOwner")
	          .append("   where cur.group.key=curriculumOwner.group.key and curriculumOwner.identity.key=:managerKey")
	          .append("   and curriculumOwner.role ").in(CurriculumRoles.curriculumowner)
	          .append(" )");
			// Curriculum organisation
			sb.append(" or exists (select curOrgManager.key from bgroupmember as curOrgManager")
	          .append("   where organis.group.key=curOrgManager.group.key and curOrgManager.identity.key=:managerKey")
	          .append("   and curOrgManager.role ").in(OrganisationRoles.curriculummanager, OrganisationRoles.administrator, OrganisationRoles.lecturemanager)
	          .append(" ))");
		}
		if(searchParams.getMasterCoach() != null) {
			sb.and()
			  .append(" exists (select mcMembership.key from lectureblocktogroup as blockToCoachGroup, bgroupmember as mcMembership")
	          .append("   where block.key=blockToCoachGroup.lectureBlock.key and blockToCoachGroup.group.key=mcMembership.group.key and mcMembership.identity.key=:masterCoachKey")
	          .append("   and mcMembership.role ").in(CurriculumRoles.mastercoach)
	          .append(" )");
		}
		if(searchParams.getParticipant() != null) {
			sb.and()
			  .append(" exists (select participantship.key from lectureblocktogroup as blockToParticipantGroup, bgroupmember as participantship")
	          .append("   where block.key=blockToParticipantGroup.lectureBlock.key and blockToParticipantGroup.group.key=participantship.group.key and participantship.identity.key=:participantKey")
	          .append("   and participantship.role ").in(GroupRoles.participant)
	          .append(" )");
		}
		if(searchParams.getTeacher() != null) {
			sb.and()
			  .append(" exists (select teachership.key from bgroupmember teachership where")
			  .append("  teachership.group.key=block.teacherGroup.key and teachership.identity.key=:teacherKey")
			  .append(" )");
		}

		if((searchParams.getTeachersList() != null && !searchParams.getTeachersList().isEmpty()) || searchParams.getWithTeachers() != null) {
			sb.and().append(" (");
			
			if(searchParams.getWithTeachers() != null) {
				if(!searchParams.getWithTeachers().booleanValue()) {
					sb.append(" not ");
				}
				sb.append(" exists (select teacherlist.key from bgroupmember teacherlist where")
				  .append("  teacherlist.group.key=tGroup.key")
				  .append(" )");
			}
			
			if(searchParams.getTeachersList() != null && !searchParams.getTeachersList().isEmpty()) {
				if(searchParams.getWithTeachers() != null) {
					sb.append(" or ");
				}
				sb.append(" exists (select teacherlist.key from bgroupmember teacherlist where")
				  .append("  teacherlist.group.key=tGroup.key and teacherlist.identity.key in (:teachersList)")
				  .append(" )");
			}
			
			sb.append(")"); 
		}

		if (searchParams.getTaxonomyLevels() != null && !searchParams.getTaxonomyLevels().isEmpty()) {
			sb.and().append(" exists (")
					.append(" select bToTax.key from lectureblocktotaxonomylevel as bToTax")
					.append(" where bToTax.lectureBlock.key = block.key")
					.append(" and bToTax.taxonomyLevel.key in (:taxonomyLevelKeys)")
					.append(")");
		}
	}
	
	private void addSearchVirtualStatusToQuery(QueryBuilder sb, LectureBlockVirtualStatus status) {
		if(status == LectureBlockVirtualStatus.CANCELLED) {
			sb.append(" (block.statusString ").in(LectureBlockStatus.cancelled).append(")");
		} else if(status == LectureBlockVirtualStatus.PLANNED) {
			sb.append(" (block.statusString ").in(LectureBlockStatus.active).append (" and block.startDate>:now)");
		} else if(status == LectureBlockVirtualStatus.RUNNING) {
			sb.append(" (block.statusString ").in(LectureBlockStatus.active).append (" and block.startDate<:now and block.endDate>:now)");
		} else if(status == LectureBlockVirtualStatus.DONE) {
			sb.append(" (block.statusString ").in(LectureBlockStatus.active, LectureBlockStatus.done).append (" and block.endDate<:now)");
		}
	}
	
	private void addSearchParametersToQuery(TypedQuery<?> query, LecturesBlockSearchParameters searchParams) {
		if(searchParams == null) return;
		
		if(searchParams.getCurriculumElement() != null) {
			query.setParameter("curriculumElementKey", searchParams.getCurriculumElement().getKey());
		} else if(StringHelper.containsNonWhitespace(searchParams.getCurriculumElementPath())) {
			query.setParameter("curriculumElementPath", searchParams.getCurriculumElementPath() + "%");
		} else if(searchParams.getRepositoryEntry() != null) {
			query.setParameter("repoEntryKey", searchParams.getRepositoryEntry().getKey());
		}
		
		if(searchParams.getCurriculums() != null && !searchParams.getCurriculums().isEmpty()) {
			List<Long> keys = searchParams.getCurriculums().stream()
					.map(CurriculumRef::getKey)
					.toList();
			query.setParameter("curriculumKeys", keys);
		}
		
		if(searchParams.getLectureBlocks() != null && !searchParams.getLectureBlocks().isEmpty()) {
			List<Long> lectureBlockKeys = searchParams.getLectureBlocks().stream()
					.map(LectureBlockRef::getKey).toList();
			query.setParameter("lectureBlockKeys", lectureBlockKeys);
		}
		
		if(searchParams.getLectureBlockStatus() != null && !searchParams.getLectureBlockStatus().isEmpty()) {
			List<String> lectureBlockStatus = searchParams.getLectureBlockStatus()
					.stream().map(LectureBlockStatus::name).toList();
			query.setParameter("lectureBlockStatus", lectureBlockStatus);
		}
		
		if(searchParams.getRollCallStatus() != null && !searchParams.getRollCallStatus().isEmpty()) {
			List<String> rollCallStatus = searchParams.getRollCallStatus()
					.stream().map(LectureRollCallStatus::name).toList();
			query.setParameter("rollCallStatus", rollCallStatus);
		}

		if(searchParams.getVirtualStatus() != null && !searchParams.getVirtualStatus().isEmpty()) {
			List<LectureBlockVirtualStatus> statusList = searchParams.getVirtualStatus();
			if(statusList.contains(LectureBlockVirtualStatus.PLANNED)
					|| statusList.contains(LectureBlockVirtualStatus.RUNNING)
					|| statusList.contains(LectureBlockVirtualStatus.DONE)) {
				query.setParameter("now", new Date(), TemporalType.TIMESTAMP);
			}
		}
		
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			String fuzzySearchString = PersistenceHelper.makeFuzzyQueryString(searchParams.getSearchString());
			query.setParameter("fuzzySearchString", fuzzySearchString);
		}
		if(searchParams.getStartDate() != null) {
			query.setParameter("startDate", searchParams.getStartDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getEndDate() != null) {
			query.setParameter("endDate", searchParams.getEndDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getManager() != null) {
			query.setParameter("managerKey", searchParams.getManager().getKey());
		}
		if(searchParams.getMasterCoach() != null) {
			query.setParameter("masterCoachKey", searchParams.getMasterCoach().getKey());
		}
		if(searchParams.getTeacher() != null) {
			query.setParameter("teacherKey", searchParams.getTeacher().getKey());
		}
		if(searchParams.getParticipant() != null) {
			query.setParameter("participantKey", searchParams.getParticipant().getKey());
		}
		if(searchParams.getTeachersList() != null && !searchParams.getTeachersList().isEmpty()) {
			List<Long> teachersKeys = searchParams.getTeachersList().stream()
					.map(IdentityRef::getKey).toList();
			query.setParameter("teachersList", teachersKeys);
		}
		if (searchParams.getTaxonomyLevels() != null && !searchParams.getTaxonomyLevels().isEmpty()) {
			List<Long> taxonomyLevelKeys = searchParams.getTaxonomyLevels().stream().map(TaxonomyLevelRef::getKey)
					.collect(Collectors.toList());
			query.setParameter("taxonomyLevelKeys", taxonomyLevelKeys);
		}
	}

	public LectureBlock addGroupToLectureBlock(LectureBlock block, Group group) {
		LectureBlockImpl reloadedBlock = (LectureBlockImpl)loadByKey(block.getKey());
		LectureBlockToGroupImpl blockToGroup = new LectureBlockToGroupImpl();
		blockToGroup.setGroup(group);
		blockToGroup.setLectureBlock(block);
		dbInstance.getCurrentEntityManager().persist(blockToGroup);
		return reloadedBlock;
	}
	
	public boolean hasLecturesAsTeacher(RepositoryEntryRef entry, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block.key from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members teachers")
		  .append(" where block.entry.key=:entryKey and teachers.identity.key=:identityKey")
		  .append(" and exists (select config.key from lectureentryconfig config")
		  .append("   where config.entry.key=:entryKey and config.lectureEnabled=true")
		  .append(" )");
		
		List<Long> firstKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setFlushMode(FlushModeType.COMMIT)
				.setParameter("entryKey", entry.getKey())
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return firstKey != null && !firstKey.isEmpty()
				&& firstKey.get(0) != null && firstKey.get(0).longValue() > 0;
	}
	
	public List<LectureBlock> getRollCallAsTeacher(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select block from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members teachers")
		  .append(" inner join fetch block.entry entry")
		  .where().append(" teachers.identity.key=:identityKey")
		  .and().append(" entry.status not ").in(RepositoryEntryStatusEnum.deleted())
		  .and().append(" block.startDate<=:now and block.endDate>=:now")
		  .and().append(" block.statusString not in ('").append(LectureBlockStatus.cancelled.name()).append("','").append(LectureBlockStatus.done.name()).append("')")
		  .and().append(" block.rollCallStatusString not in ('").append(LectureRollCallStatus.closed.name()).append("','").append(LectureRollCallStatus.autoclosed.name()).append("')")
		  .and().append(" exists (select config.key from lectureentryconfig config")
		  .append("   where config.entry.key=entry.key and config.lectureEnabled=true")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlock.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("now", new Date(), TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public List<LectureBlock> getLecturesAsTeacher(RepositoryEntryRef entry, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members teachers")
		  .append(" where block.entry.key=:entryKey and teachers.identity.key=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlock.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<Identity> getTeachers(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ident from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where block.entry.key=:repoKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repoKey", entry.getKey())
				.getResultList();
	}
	
	public List<Identity> getTeachers(List<LectureBlock> blocks) {
		if(blocks == null || blocks.isEmpty()) return new ArrayList<>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ident from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where block.key in (:lectureBlockKeys)");
		
		List<Long> lectureBlockKeys = blocks.stream()
				.map(LectureBlock::getKey).collect(Collectors.toList());
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("lectureBlockKeys", lectureBlockKeys)
				.getResultList();
	}
	
	public List<Identity> getTeachers(Identity participant, List<LectureBlock> blocks, List<RepositoryEntry> entries, Date start, Date end) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct ident from lectureblock block")
		  .append(" inner join block.groups as blockToGroup")
		  .append(" inner join blockToGroup.group as bGroup")
		  .append(" inner join bGroup.members participants on (participants.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where participants.identity.key=:participantKey")
		  .append(" and block.startDate>=:startDate and block.endDate<=:endDate");
		
		if(blocks != null && !blocks.isEmpty()) {
			sb.append(" and block.key in (:blockKeys)");
		}
		if(entries != null && !entries.isEmpty()) {
			sb.append(" and block.entry.key in (:entryKeys)");
		}
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("startDate", start, TemporalType.TIMESTAMP)
				.setParameter("endDate", end, TemporalType.TIMESTAMP)
				.setParameter("participantKey", participant.getKey());
		if(blocks != null && !blocks.isEmpty()) {
			List<Long> blockKeys = blocks.stream()
					.map(LectureBlock::getKey).collect(Collectors.toList());
			query.setParameter("blockKeys", blockKeys);
		}
		if(entries != null && !entries.isEmpty()) {
			List<Long> entryKeys = entries.stream()
					.map(RepositoryEntry::getKey).collect(Collectors.toList());
			query.setParameter("entryKeys", entryKeys);
		}
		
		return query.getResultList();
	}
	
	public boolean isMasterCoach(LectureBlockRef block, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mastercoachs.key from lectureblock block")
		  .append(" inner join block.groups as blockToGroup")
		  .append(" inner join blockToGroup.group as bGroup")
		  .append(" inner join bGroup.members mastercoachs on (mastercoachs.role='").append(CurriculumRoles.mastercoach.name()).append("')")
		  .append(" where block.key=:blockKey and mastercoachs.identity.key=:identityKey");
		
		List<Long> masterCoachs = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("blockKey", block.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return masterCoachs != null && masterCoachs.size() > 0 && masterCoachs.get(0) != null;
	}
	
	public List<Identity> getMasterCoaches(IdentityRef participant, List<LectureBlock> blocks, List<RepositoryEntry> entries, Date start, Date end) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct mcIdent from lectureblock block")
		  .append(" inner join block.groups as blockToGroup")
		  .append(" inner join blockToGroup.group as bGroup")
		  .append(" inner join bGroup.members participants on (participants.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join bGroup.members mastercoachs on (mastercoachs.role='").append(CurriculumRoles.mastercoach.name()).append("')")
		  .append(" inner join mastercoachs.identity mcIdent")
		  .append(" inner join mcIdent.user mcUser")
		  .append(" where participants.identity.key=:participantKey")
		  .append(" and block.startDate>=:startDate and block.endDate<=:endDate");
		
		if(blocks != null && !blocks.isEmpty()) {
			sb.append(" and block.key in (:blockKeys)");
		}
		if(entries != null && !entries.isEmpty()) {
			sb.append(" and block.entry.key in (:entryKeys)");
		}
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("startDate", start, TemporalType.TIMESTAMP)
				.setParameter("endDate", end, TemporalType.TIMESTAMP)
				.setParameter("participantKey", participant.getKey());
		if(blocks != null && !blocks.isEmpty()) {
			List<Long> blockKeys = blocks.stream()
					.map(LectureBlock::getKey).collect(Collectors.toList());
			query.setParameter("blockKeys", blockKeys);
		}
		if(entries != null && !entries.isEmpty()) {
			List<Long> entryKeys = entries.stream()
					.map(RepositoryEntry::getKey).collect(Collectors.toList());
			query.setParameter("entryKeys", entryKeys);
		}
		
		return query.getResultList();
	}
	
	public List<Identity> getParticipants(LectureBlockRef block) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ident from lectureblock block")
		  .append(" inner join block.groups as blockToGroup")
		  .append(" inner join blockToGroup.group as bGroup")
		  .append(" inner join bGroup.members participants on (participants.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join participants.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where block.key=:blockKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("blockKey", block.getKey())
				.getResultList();
	}
	
	public List<Identity> getParticipants(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ident from lectureblock block")
		  .append(" inner join block.groups as blockToGroup")
		  .append(" inner join blockToGroup.group as bGroup")
		  .append(" inner join bGroup.members participants on (participants.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join participants.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where block.entry.key=:repoKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repoKey", entry.getKey())
				.getResultList();
	}
	
	public List<Identity> getParticipants(RepositoryEntryRef entry, IdentityRef teacher) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ident from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members teachers on (teachers.identity.key=:teacherKey)")
		  .append(" inner join block.groups as blockToGroup")
		  .append(" inner join blockToGroup.group as bGroup")
		  .append(" inner join bGroup.members participants on (participants.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join participants.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where block.entry.key=:repoKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repoKey", entry.getKey())
				.setParameter("teacherKey", teacher.getKey())
				.getResultList();
	}
	
	public List<Identity> getParticipants(LecturesMemberSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct ident from lectureblock block")
		  .append(" inner join block.groups as blockToGroup")
		  .append(" inner join blockToGroup.group as bGroup")
		  .append(" inner join bGroup.members participants on (participants.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" inner join participants.identity ident")
		  .append(" inner join fetch ident.user identUser");
		
		if(searchParams.getTeacher() != null) {
			sb.and()
			  .append(" exists (select teacherMembership from bgroupmember as teacherMembership")
			  .append("   where teacherMembership.group.key=block.teacherGroup.key and teacherMembership.identity.key=:teacherKey and teacherMembership.role").in(LectureRoles.teacher)
			  .append(" )");
		}
		if(searchParams.getMasterCoach() != null) {
			sb.and()
			  .append(" exists (select teacherMembership from bgroupmember as teacherMembership")
			  .append("   where teacherMembership.group.key=bGroup.key and teacherMembership.identity.key=:masterCoachKey")
			  .append("   and teacherMembership.role").in(CurriculumRoles.mastercoach)
			  .append(" )");
		}
		if(searchParams.getManager() != null) {
			sb.and()
			  .append(" exists (select rel.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("   where rel.entry.key=block.entry.key and rel.group.key=membership.group.key and membership.identity.key=:managerKey")
			  .append("   and membership.role ").in(OrganisationRoles.administrator, OrganisationRoles.lecturemanager, GroupRoles.owner)
			  .append(" )");
		}
		
		if(searchParams.getRepositoryEntry() != null) {
			sb.and().append(" block.entry.key=:repositoryEntryKey");
		}
		if(searchParams.getCurriculumElement() != null) {
			sb.and().append(" exists (select curEl.key from curriculumelement as curEl")
			  .append("  where curEl.group.key=bGroup.key and curEl.key=:curriculumElementKey")
			  .append(")");
		}
		
		Long refId = null;
		String fuzzyString = null;
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			sb.append(" and (");
			fuzzyString = PersistenceHelper.makeFuzzyQueryString(searchParams.getSearchString());
			if(StringHelper.isLong(searchParams.getSearchString())) {
				refId = Long.valueOf(searchParams.getSearchString());
				sb.append("ident.key=:idKey or identUser.key=:idKey or ");
			}
			sb.likeFuzzy(" ident.externalId", "fuzzyString")
			  .append(" or ")
			  .likeFuzzy(" identUser.firstName", "fuzzyString")
			  .append(" or ")
			  .likeFuzzy(" identUser.lastName", "fuzzyString")
			  .append(" or ")
			  .likeFuzzy(" identUser.email", "fuzzyString")
			  .append(" or ")
			  .likeFuzzy(" identUser.nickName", "fuzzyString")
			  .append(")");
		}
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class);
		if(searchParams.getTeacher() != null) {
			query.setParameter("teacherKey", searchParams.getTeacher().getKey());
		}
		if(searchParams.getMasterCoach() != null) {
			query.setParameter("masterCoachKey", searchParams.getMasterCoach().getKey());
		}
		if(searchParams.getManager() != null) {
			query.setParameter("managerKey", searchParams.getManager().getKey());
		}
		if(refId != null) {
			query.setParameter("idKey", refId);
		}
		if(StringHelper.containsNonWhitespace(fuzzyString)) {
			query.setParameter("fuzzyString", fuzzyString);
		}
		if(searchParams.getRepositoryEntry() != null) {
			query.setParameter("repositoryEntryKey", searchParams.getRepositoryEntry().getKey());
		}
		if(searchParams.getCurriculumElement() != null) {
			query.setParameter("curriculumElementKey", searchParams.getCurriculumElement().getKey());
		}
		
		return query.getResultList();
	}
	
	public List<Identity> getTeachers(LecturesMemberSearchParameters searchParams) {
		if(searchParams.getTeacher() != null) {
			return new ArrayList<>();// teacher cannot search teachers
		}
		
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select distinct ident from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members teachers on (teachers.role='").append(LectureRoles.teacher).append("')")
		  .append(" inner join teachers.identity ident")
		  .append(" inner join fetch ident.user identUser");
		
		if(searchParams.getMasterCoach() != null) {
			sb.and()
			  .append(" exists (select teacherMembership from bgroupmember as teacherMembership")
			  .append("   where teacherMembership.group.key=teacherGroup.key and teacherMembership.identity.key=:masterCoachKey")
			  .append("   and teacherMembership.role").in(CurriculumRoles.mastercoach)
			  .append(" )");
		}
		
		if(searchParams.getManager() != null) {
			sb.and()
			  .append(" exists (select rel.key from repoentrytogroup as rel, bgroupmember as membership")
			  .append("   where rel.entry.key=block.entry.key and rel.group.key=membership.group.key and membership.identity.key=:managerKey")
			  .append("   and membership.role ").in(OrganisationRoles.administrator, OrganisationRoles.lecturemanager, GroupRoles.owner)
			  .append(" )");
		}
		
		if(searchParams.getCurriculum() != null) {
			sb.and()
			  .append(" exists (select blockToCur.key from lectureblocktogroup as blockToCur")
			  .append("  inner join curriculumelement curriculumEl on (curriculumEl.group.key=blockToCur.group.key)")
			  .append("  where blockToCur.lectureBlock.key=block.key and curriculumEl.curriculum.key=:curriculumKey")
			  .append(")");
		}
		
		if(searchParams.getCurriculumElement() != null) {
			sb.and()
			  .append(" exists (select blockToCurEl.key from lectureblocktogroup as blockToCurEl")
			  .append("  inner join curriculumelement curEl on (curEl.group.key=blockToCurEl.group.key)")
			  .append("  where blockToCurEl.lectureBlock.key=block.key and curEl.key=:curriculumElementKey")
			  .append(")");
		}
		
		if(searchParams.getRepositoryEntry() != null) {
			sb.and().append(" block.entry.key=:repositoryEntryKey");
		}
		
		Long refId = null;
		String fuzzyString = null;
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			sb.append(" and (");
			fuzzyString = PersistenceHelper.makeFuzzyQueryString(searchParams.getSearchString());
			if(StringHelper.isLong(searchParams.getSearchString())) {
				refId = Long.valueOf(searchParams.getSearchString());
				sb.append("ident.key=:idKey or identUser.key=:idKey or ");
			}
			sb.likeFuzzy(" ident.externalId", "fuzzyString")
			  .append(" or ")
			  .likeFuzzy(" identUser.firstName", "fuzzyString")
			  .append(" or ")
			  .likeFuzzy(" identUser.lastName", "fuzzyString")
			  .append(" or ")
			  .likeFuzzy(" identUser.email", "fuzzyString")
			  .append(" or ")
			  .likeFuzzy(" identUser.nickName", "fuzzyString")
			  .append(")");
		}
		
		TypedQuery<Identity> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class);
		if(searchParams.getMasterCoach() != null) {
			query.setParameter("masterCoachKey", searchParams.getMasterCoach().getKey());
		}
		if(searchParams.getManager() != null) {
			query.setParameter("managerKey", searchParams.getManager().getKey());
		}
		if(searchParams.getCurriculum() != null) {
			query.setParameter("curriculumKey", searchParams.getCurriculum().getKey());
		}
		if(searchParams.getCurriculumElement() != null) {
			query.setParameter("curriculumElementKey", searchParams.getCurriculumElement().getKey());
		}
		if(searchParams.getRepositoryEntry() != null) {
			query.setParameter("repositoryEntryKey", searchParams.getRepositoryEntry().getKey());
		}
		if(refId != null) {
			query.setParameter("idKey", refId);
		}
		if(StringHelper.containsNonWhitespace(fuzzyString)) {
			query.setParameter("fuzzyString", fuzzyString);
		}
		
		return query.getResultList();
	}
	
	public Map<Long,Long> getNumOfParticipants(List<LectureBlock> lectureBlocks) {
		if(lectureBlocks == null || lectureBlocks.isEmpty()) {
			return Collections.emptyMap();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select block.key, count(participants.identity.key) from lectureblock block")
		  .append(" inner join block.groups as blockToGroup")
		  .append(" inner join blockToGroup.group as bGroup")
		  .append(" inner join bGroup.members as participants on (participants.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" where block.key in (:lectureBlockKeys)")
		  .append(" group by block.key");
		
		List<Long> lectureBlockKeys = lectureBlocks.stream()
				.map(LectureBlockRef::getKey).collect(Collectors.toList());

		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("lectureBlockKeys", lectureBlockKeys)
				.getResultList();
		Map<Long,Long> numOfParticipantMap = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			Long lectureBlockKey = (Long)rawObject[0];
			Long numOfParticipants = PersistenceHelper.extractLong(rawObject, 1);
			if(numOfParticipants == null) {
				numOfParticipants = Long.valueOf(0l);
			}
			numOfParticipantMap.put(lectureBlockKey, numOfParticipants);
		}
		return numOfParticipantMap;
	}
	
	public List<LectureBlockImpl> loadOpenBlocksBefore(Date endDate) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block from lectureblock block")
		  .append(" left join fetch block.reasonEffectiveEnd reason")
		  .append(" inner join fetch block.entry entry")
		  .append(" where block.endDate<=:endDate and block.rollCallStatusString in ('").append(LectureRollCallStatus.open.name()).append("')");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockImpl.class)
				.setParameter("endDate", endDate, TemporalType.TIMESTAMP)
				.getResultList();
	}
}
