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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

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
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.modules.lecture.model.LectureBlockToGroupImpl;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LectureReportRow;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
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
	
	public LectureBlock createLectureBlock(RepositoryEntry entry) {
		LectureBlockImpl block = new LectureBlockImpl();
		block.setCreationDate(new Date());
		block.setLastModified(block.getCreationDate());
		block.setStatus(LectureBlockStatus.active);
		block.setRollCallStatus(LectureRollCallStatus.open);
		block.setCompulsory(true);
		block.setEntry(entry);
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
	
	public LectureBlock loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block from lectureblock block")
		  .append(" left join fetch block.reasonEffectiveEnd reason")
		  .append(" inner join fetch block.entry entry")
		  .append(" where block.key=:blockKey");

		List<LectureBlock> blocks = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlock.class)
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
	
	public List<LectureBlock> searchLectureBlocks(LecturesBlockSearchParameters searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block from lectureblock block")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" inner join tGroup.members membership")
		  .append(" inner join fetch block.entry entry");
		boolean where = addSearchParametersToQuery(sb, false, searchParams);
		where = PersistenceHelper.appendAnd(sb, where);
		sb.append(" exists (select config.key from lectureentryconfig config")
		  .append("   where config.entry.key=entry.key and config.lectureEnabled=true")
		  .append(" )");

		TypedQuery<LectureBlock> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlock.class);
		addSearchParametersToQuery(query, searchParams);
		return query.getResultList();
	}
	
	public List<LectureBlock> loadByTeacher(IdentityRef identityRef, LecturesBlockSearchParameters searchParams) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block from lectureblock block")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" inner join tGroup.members membership")
		  .append(" inner join fetch block.entry entry")
		  .append(" where membership.identity.key=:teacherKey");
		addSearchParametersToQuery(sb, true, searchParams);
		sb.append(" and exists (select config.key from lectureentryconfig config")
		  .append("   where config.entry.key=entry.key and config.lectureEnabled=true")
		  .append(" )");

		TypedQuery<LectureBlock> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlock.class)
				.setParameter("teacherKey", identityRef.getKey());
		addSearchParametersToQuery(query, searchParams);
		return query.getResultList();
	}
	
	public List<LectureBlockRef> loadAssessedByTeacher(IdentityRef identityRef, LecturesBlockSearchParameters searchParams) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select distinct block.key from lectureblock block")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" inner join tGroup.members membership")
		  .append(" inner join block.entry entry")
		  .append(" inner join courseassessmentmode mode on (mode.lectureBlock.key=block.key)")
		  .append(" where membership.identity.key=:teacherKey");
		addSearchParametersToQuery(sb, true, searchParams);
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("teacherKey", identityRef.getKey());
		addSearchParametersToQuery(query, searchParams);
		List<Long> blockKeys = query.getResultList();
		return blockKeys.stream().map(LectureBlockRefImpl::new).collect(Collectors.toList());
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
	 * 
	 * @param entry The course (mandatory)
	 * @param teacher The teacher (optional)
	 * @return
	 */
	public List<LectureBlockWithTeachers> getLecturesBlockWithTeachers(RepositoryEntryRef entry) {
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
			blockMap.put(block.getKey(), new  LectureBlockWithTeachers(block, assessedBlockKeySet.contains(block.getKey())));
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
	 * 
	 * @param entry The course (mandatory)
	 * @param teacher The teacher (mandatory)
	 * @return
	 */
	public List<LectureBlockWithTeachers> getLecturesBlockWithTeachers(RepositoryEntryRef entry,
			IdentityRef teacher, LecturesBlockSearchParameters searchParams) {
		StringBuilder sc = new StringBuilder();
		sc.append("select block, coach, mode.key")
		  .append(" from lectureblock block")
		  .append(" inner join block.entry entry")
		  .append(" inner join block.teacherGroup tGroup")
		  .append(" inner join tGroup.members membership")
		  .append(" inner join membership.identity coach")
		  .append(" inner join fetch coach.user usercoach")
		  .append(" left join courseassessmentmode mode on (mode.lectureBlock.key=block.key)")
		  .append(" where membership.role='").append("teacher").append("' and block.entry.key=:repoEntryKey");
		addSearchParametersToQuery(sc, true, searchParams);
		if(teacher != null) {
			sc.append(" and exists (select teachership.key from bgroupmember teachership where")
			  .append("  teachership.group.key=tGroup.key and teachership.identity.key=:teacherKey")
			  .append(" )");
		}
		
		//get all, it's quick
		TypedQuery<Object[]> coachQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sc.toString(), Object[].class)
				.setParameter("repoEntryKey", entry.getKey());
		if(teacher != null) {
			coachQuery.setParameter("teacherKey", teacher.getKey());
		}
		addSearchParametersToQuery(coachQuery, searchParams);
		
		List<Object[]> rawCoachs = coachQuery.getResultList();
		Map<Long,LectureBlockWithTeachers> blockMap = new HashMap<>();
		for(Object[] rawCoach:rawCoachs) {
			LectureBlock block = (LectureBlock)rawCoach[0];
			Identity coach = (Identity)rawCoach[1];
			Long assessmentModeKey = (Long)rawCoach[2];
			
			LectureBlockWithTeachers blockWith = blockMap.get(block.getKey());
			if(blockWith == null) {
				blockWith = new LectureBlockWithTeachers(block, assessmentModeKey != null);
				blockMap.put(block.getKey(), blockWith);
			}
			blockWith.getTeachers().add(coach);
		}
		return new ArrayList<>(blockMap.values());
	}
	
	private boolean addSearchParametersToQuery(StringBuilder sb, boolean where, LecturesBlockSearchParameters searchParams) {
		if(searchParams == null) return where;
		
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" (entry.externalRef=:searchString or ");
			PersistenceHelper.appendFuzzyLike(sb, "entry.displayname", "fuzzySearchString", dbInstance.getDbVendor());
			sb.append(" or ");
			PersistenceHelper.appendFuzzyLike(sb, "block.title", "fuzzySearchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		if(searchParams.getStartDate() != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" block.startDate>=:startDate");
		}
		if(searchParams.getEndDate() != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" block.endDate<=:endDate");
		}
		if(searchParams.getManager() != null) {
			where = PersistenceHelper.appendAnd(sb, where);
			sb.append(" exists (select membership.key from repoentrytogroup as rel, bgroupmember as membership")
	         .append("    where rel.entry.key=entry.key and rel.group.key=membership.group.key and membership.identity.key=:managerKey")
	         .append("      and membership.role in ('").append(OrganisationRoles.administrator.name()).append("','").append(OrganisationRoles.learnresourcemanager.name()).append("','").append(OrganisationRoles.lecturemanager.name()).append("','").append(GroupRoles.owner.name()).append("')")
	         .append("  )");
		}
		return where;
	}
	
	private void addSearchParametersToQuery(TypedQuery<?> query, LecturesBlockSearchParameters searchParams) {
		if(searchParams == null) return;
		
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			String searchString = searchParams.getSearchString();
			query.setParameter("searchString", searchString);
			String fuzzySearchString = PersistenceHelper.makeFuzzyQueryString(searchString);
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
				.setParameter("entryKey", entry.getKey())
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return firstKey != null && !firstKey.isEmpty()
				&& firstKey.get(0) != null && firstKey.get(0).longValue() > 0;
	}
	
	public List<LectureBlock> getRollCallAsTeacher(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block from lectureblock block")
		  .append(" inner join block.teacherGroup teacherGroup")
		  .append(" inner join teacherGroup.members teachers")
		  .append(" inner join fetch block.entry entry")
		  .append(" where teachers.identity.key=:identityKey")
		  .append(" and block.startDate<=:now and block.endDate>=:now")
		  .append(" and block.statusString not in ('").append(LectureBlockStatus.cancelled.name()).append("','").append(LectureBlockStatus.done.name()).append("')")
		  .append(" and block.rollCallStatusString not in ('").append(LectureRollCallStatus.closed.name()).append("','").append(LectureRollCallStatus.autoclosed.name()).append("')")
		  .append(" and exists (select config.key from lectureentryconfig config")
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
