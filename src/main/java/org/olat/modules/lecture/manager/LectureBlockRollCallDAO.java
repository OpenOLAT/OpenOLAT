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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.AggregatedLectureBlocksStatistics;
import org.olat.modules.lecture.model.LectureBlockAndRollCall;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureBlockRollCallAndCoach;
import org.olat.modules.lecture.model.LectureBlockRollCallImpl;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureBlockRollCallDAO {

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureBlockAuditLogDAO auditLogDao;
	
	public LectureBlockRollCall createAndPersistRollCall(LectureBlock lectureBlock, Identity identity,
			Boolean authorizedAbsence, String absenceReason, AbsenceCategory absenceCategory, AbsenceNotice absenceNotice,
			String comment, List<Integer> absences) {
		LectureBlockRollCallImpl rollCall = new LectureBlockRollCallImpl();
		rollCall.setCreationDate(new Date());
		rollCall.setLastModified(rollCall.getCreationDate());
		rollCall.setIdentity(identity);
		rollCall.setLectureBlock(lectureBlock);
		rollCall.setAbsenceAuthorized(authorizedAbsence);
		rollCall.setAbsenceReason(absenceReason);
		rollCall.setAbsenceCategory(absenceCategory);
		rollCall.setAbsenceNotice(absenceNotice);
		rollCall.setComment(comment);
		addInternalLecture(lectureBlock, rollCall, absences);
		dbInstance.getCurrentEntityManager().persist(rollCall);
		return rollCall;
	}
	
	public LectureBlockRollCall addLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		addInternalLecture(lectureBlock, rollCall, absences);
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	private void addInternalLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		if(absences != null) {
			LectureBlockRollCallImpl call = (LectureBlockRollCallImpl)rollCall;

			List<Integer> currentAbsentList = call.getLecturesAbsentList();
			for(int i=absences.size(); i-->0; ) {
				Integer absence = absences.get(i);
				if(absence != null && !currentAbsentList.contains(absence)) {
					currentAbsentList.add(absence);
				}
			}
			call.setLecturesAbsentList(currentAbsentList);
			call.setLecturesAbsentNumber(currentAbsentList.size());
			
			int numOfLectures = lectureBlock.getEffectiveLecturesNumber();
			if(numOfLectures <= 0 && lectureBlock.getStatus() != LectureBlockStatus.cancelled) {
				numOfLectures = lectureBlock.getPlannedLecturesNumber();
			}
			
			List<Integer> attendedList = new ArrayList<>();
			for(int i=0; i<numOfLectures; i++) {
				if(!currentAbsentList.contains(i)) {
					attendedList.add(i);
				}
			}
			call.setLecturesAttendedList(attendedList);
			call.setLecturesAttendedNumber(numOfLectures - currentAbsentList.size());
		}
	}
	
	public LectureBlockRollCall removeLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		removeInternalLecture(lectureBlock, rollCall, absences);
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	private void removeInternalLecture(LectureBlock lectureBlock, LectureBlockRollCall rollCall, List<Integer> absences) {
		if(absences != null) {
			LectureBlockRollCallImpl call = (LectureBlockRollCallImpl)rollCall;
			
			List<Integer> currentAbsentList = call.getLecturesAbsentList();
			for(int i=absences.size(); i-->0; ) {
				currentAbsentList.remove(absences.get(i));
			}
			call.setLecturesAbsentList(currentAbsentList);
			call.setLecturesAbsentNumber(currentAbsentList.size());

			int numOfLectures = lectureBlock.getEffectiveLecturesNumber();
			if(numOfLectures <= 0 && lectureBlock.getStatus() != LectureBlockStatus.cancelled) {
				numOfLectures = lectureBlock.getPlannedLecturesNumber();
			}

			List<Integer> attendedList = new ArrayList<>();
			for(int i=0; i<numOfLectures; i++) {
				if(!currentAbsentList.contains(i)) {
					attendedList.add(i);
				}
			}
			call.setLecturesAttendedList(attendedList);
			call.setLecturesAttendedNumber(numOfLectures - currentAbsentList.size());
			
			if(currentAbsentList.isEmpty()) {
				call.setAbsenceAuthorized(null);
			}
		}
	}
	
	public LectureBlockRollCall adaptLecture(LectureBlock lectureBlock,
			LectureBlockRollCall rollCall, int numberOfLectures, Identity author) {
		LectureBlockRollCallImpl call = (LectureBlockRollCallImpl)rollCall;
		List<Integer> currentAbsentList = call.getLecturesAbsentList();
		List<Integer> currentAttendedList = call.getLecturesAttendedList();
		
		if((currentAbsentList != null && currentAbsentList.size() > 0) || (currentAttendedList != null && currentAttendedList.size() > 0)) {
			String before = auditLogDao.toXml(rollCall);
			
			int currentLectures = currentAbsentList.size() + currentAttendedList.size();
			if(currentLectures > numberOfLectures) {
				// need to reduce
				List<Integer> absentList = new ArrayList<>();
				for(Integer absence:currentAbsentList) {
					if(absence.intValue() < numberOfLectures) {
						absentList.add(absence);
					}
				}
				call.setLecturesAbsentList(absentList);
				call.setLecturesAbsentNumber(absentList.size());
				
				List<Integer> attendedList = new ArrayList<>();
				for(Integer attended:currentAttendedList) {
					if(attended.intValue() < numberOfLectures) {
						attendedList.add(attended);
					}
				}
				call.setLecturesAttendedList(attendedList);
				call.setLecturesAttendedNumber(numberOfLectures - absentList.size());
				call = (LectureBlockRollCallImpl)update(call);
			} else if(currentLectures < numberOfLectures) {
				//need to add some lecture

				List<Integer> attendedList = new ArrayList<>(currentAttendedList);
				for(int i=currentLectures; i<numberOfLectures; i++) {
					attendedList.add(i);
				}

				call.setLecturesAttendedList(attendedList);
				call.setLecturesAttendedNumber(attendedList.size());
				call = (LectureBlockRollCallImpl)update(call);
			}
			
			String after = auditLogDao.toXml(rollCall);
			if(before == null || !before.equals(after)) {
				auditLogDao.auditLog(LectureBlockAuditLog.Action.adaptRollCall, before, after,
						null, lectureBlock, rollCall, lectureBlock.getEntry(), rollCall.getIdentity(), author);
			}
		}
		return call;
	}
	
	public LectureBlockRollCall loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" where rollcall.key=:rollCallKey");
		
		List<LectureBlockRollCall> rollCalls = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("rollCallKey", key)
				.getResultList();
		return rollCalls == null || rollCalls.isEmpty() ? null : rollCalls.get(0);
	}
	
	public LectureBlockRollCall update(LectureBlockRollCall rollCall) {
		return dbInstance.getCurrentEntityManager().merge(rollCall);
	}
	
	public int deleteRollCalls(Identity identity) {
		String del = "delete from lectureblockrollcall rollcall where rollcall.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(del)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRef block) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" left join fetch rollcall.absenceNotice notice")
		  .append(" where rollcall.lectureBlock.key=:blockKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("blockKey", block.getKey())
				.getResultList();
	}
	
	public List<LectureBlockRollCall> getRollCalls(List<? extends LectureBlockRef> blocks) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" left join fetch rollcall.absenceNotice notice")
		  .append(" where rollcall.lectureBlock.key in (:blockKeys)");
		
		List<Long> blockKeys = blocks.stream()
				.map(LectureBlockRef::getKey).collect(Collectors.toList());
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("blockKeys", blockKeys)
				.getResultList();
	}
	
	/**
	 * The query doesn't fetch anything.
	 * 
	 * @param block The lecture block
	 * @param identity The attendee's identity
	 * @return A lecture block roll cal or null if not found
	 */
	public LectureBlockRollCall getRollCall(LectureBlockRef block, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" where rollcall.lectureBlock.key=:blockKey and rollcall.identity.key=:identityKey");
		List<LectureBlockRollCall> rollCalls = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("blockKey", block.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return rollCalls != null && !rollCalls.isEmpty() ? rollCalls.get(0) : null;
	}
	
	public List<LectureBlockRollCallAndCoach> getLectureBlockAndRollCalls(LectureBlockRollCallSearchParameters searchParams) {
		List<LectureBlockRollCall> rollCalls = getRollCalls(searchParams);
		Map<Long,String> coaches = getCoaches(searchParams.getEntry(), ", ");
		List<LectureBlockRollCallAndCoach> blockAndRollCalls = new ArrayList<>(rollCalls.size());
		for(LectureBlockRollCall rollCall:rollCalls) {
			LectureBlock block = rollCall.getLectureBlock();
			AbsenceNotice absenceNotice = rollCall.getAbsenceNotice();
			String coach = coaches.get(block.getKey());
			blockAndRollCalls.add(new LectureBlockRollCallAndCoach(coach, block, rollCall, absenceNotice));
		}
		return blockAndRollCalls;
	}
	
	private Map<Long,String> getCoaches(RepositoryEntryRef entry, String teacherSeaparator) {
		if(entry == null) {
			return Collections.emptyMap();
		}
		
		// append the coaches
		StringBuilder sc = new StringBuilder(256);
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
		Map<Long,String> coaches = new HashMap<>();
		for(Object[] rawCoach:rawCoachs) {
			Long blockKey = (Long)rawCoach[0];
			Identity coach = (Identity)rawCoach[1];
			String fullname = userManager.getUserDisplayName(coach);
			if(coaches.containsKey(blockKey)) {
				fullname = coaches.get(blockKey) + " " + teacherSeaparator + " " + fullname;
			}
			coaches.put(blockKey, fullname);
		}
		return coaches;
	}
	
	public List<LectureBlockRollCall> getRollCalls(LectureBlockRollCallSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select rollcall from lectureblockrollcall rollcall")
		  .append(" inner join fetch rollcall.identity ident")
		  .append(" inner join fetch ident.user user")
		  .append(" inner join fetch rollcall.lectureBlock block")
		  .append(" left join fetch rollcall.absenceNotice notice");

		if(searchParams.getHasAbsence() != null) {
			sb.and();
			if(searchParams.getHasAbsence().booleanValue()) {
				sb.append("rollcall.lecturesAbsentNumber>0");
			} else {
				sb.append("(rollcall.lecturesAbsentNumber = 0 or rollcall.lecturesAbsentNumber is null)");
			}
		}
		
		if(searchParams.getCalledIdentity() != null) {
			sb.and().append(" ident.key=:calledIdentityKey");
		}
		
		if(searchParams.getManager() != null || searchParams.getMasterCoach() != null) {
			sb.and()
			  .append(" exists (select membership.key from repoentrytogroup as rel, bgroupmember as membership")
	          .append("    where rel.entry.key=block.entry.key and rel.group.key=membership.group.key and membership.identity.key=:managerKey")
	          .append("    and membership.role");
			if(searchParams.getManager() != null && searchParams.getMasterCoach() != null) {
				sb.in(OrganisationRoles.administrator, OrganisationRoles.lecturemanager, CurriculumRoles.mastercoach, GroupRoles.owner.name());
			} else if(searchParams.getManager() != null ) {
				sb.in(OrganisationRoles.administrator, OrganisationRoles.lecturemanager, GroupRoles.owner.name());
			} else if(searchParams.getMasterCoach() != null) {
				sb.in(CurriculumRoles.mastercoach);
			}
	        sb.append("  )");
		}
		
		if(searchParams.getTeacher() != null) {
			sb.and()
			  .append(" exists (select teachership.key from bgroup as teacherGroup")
			  .append("  inner join teacherGroup.members as teachership")
			  .append("  where teacherGroup.key=block.teacherGroup.key and teachership.identity.key=:teacherKey")
			  .append(" )");
		}
		
		if(searchParams.getHasSupervisorNotificationDate() != null) {
			sb.and();
			if(searchParams.getHasSupervisorNotificationDate().booleanValue()) {
				sb.append("rollcall.absenceSupervisorNotificationDate is not null");
			} else {
				sb.append("rollcall.absenceSupervisorNotificationDate is null");
			}
		}

		if(searchParams.getLectureBlockRefs() != null && !searchParams.getLectureBlockRefs().isEmpty()) {
			sb.and().append(" block.key in (:lectureBlockKeys)");
		}
		
		if(searchParams.getClosed() != null) {
			sb.and();
			if(searchParams.getClosed().booleanValue()) {
				sb.append("(block.statusString='").append(LectureBlockStatus.done.name()).append("'")
				  .append(" or block.rollCallStatusString='").append(LectureRollCallStatus.closed.name()).append("'")
				  .append(" or block.rollCallStatusString='").append(LectureRollCallStatus.autoclosed.name()).append("')");
			} else {
				sb.append("(block.statusString!='").append(LectureBlockStatus.done.name()).append("'")
				  .append(" and block.rollCallStatusString!='").append(LectureRollCallStatus.closed.name()).append("'")
				  .append(" and block.rollCallStatusString!='").append(LectureRollCallStatus.autoclosed.name()).append("')");
			}
		}

		String fuzzyRef = null;
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			fuzzyRef = PersistenceHelper.makeFuzzyQueryString(searchParams.getSearchString());
			sb.and().append("(")
			  .likeFuzzy("block.title", "fuzzyRef", dbInstance.getDbVendor())
			  .append(" or ")
			  .likeFuzzy("block.externalId", "fuzzyRef", dbInstance.getDbVendor())
			  .append(" or ")
			  .likeFuzzy("user.firstName", "fuzzyRef", dbInstance.getDbVendor())
			  .append(" or ")
			  .likeFuzzy("user.lastName", "fuzzyRef", dbInstance.getDbVendor())
			  .append(")");
			//TODO curriculum, course, reasons...
		}
		
		if(searchParams.getStartDate() != null && searchParams.getEndDate() != null) {
			sb.and().append(" (block.startDate>=:startDate and block.endDate<=:endDate)");
		} else if(searchParams.getStartDate() != null) {
			sb.and().append(" block.startDate>=:startDate");
		} else if(searchParams.getEndDate() != null) {
			sb.and().append(" block.endDate<=:endDate");
		}
		
		if(searchParams.getRollCallKey() != null) {
			sb.and().append("rollcall.key=:rollCallKey");
		}
		if(searchParams.getLectureBlockKey() != null) {
			sb.and().append("block.key=:lectureBlockKey");
		}
		if(searchParams.getEntry() != null) {
			sb.and().append("block.entry.key=:entryKey");
		}
		if(searchParams.getAppealStatus() != null && !searchParams.getAppealStatus().isEmpty()) {
			sb.and().append("rollcall.appealStatusString in (:appealStatus)");
		}

		TypedQuery<LectureBlockRollCall> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class);
		if(searchParams.getRollCallKey() != null) {
			query.setParameter("rollCallKey", searchParams.getRollCallKey());
		}
		if(searchParams.getCalledIdentity() != null) {
			query.setParameter("calledIdentityKey", searchParams.getCalledIdentity().getKey());
		}
		if(searchParams.getLectureBlockKey() != null) {
			query.setParameter("lectureBlockKey", searchParams.getLectureBlockKey());
		}
		if(searchParams.getStartDate() != null) {
			query.setParameter("startDate", searchParams.getStartDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getEndDate() != null) {
			query.setParameter("endDate", searchParams.getEndDate(), TemporalType.TIMESTAMP);
		}
		if(fuzzyRef != null) {
			query.setParameter("fuzzyRef", fuzzyRef);
		}
		if(searchParams.getEntry() != null) {
			query.setParameter("entryKey", searchParams.getEntry().getKey());
		}
		if(searchParams.getLectureBlockRefs() != null && !searchParams.getLectureBlockRefs().isEmpty()) {
			List<Long> lectureBlockKeys = searchParams.getLectureBlockRefs().stream()
					.map(LectureBlockRef::getKey).collect(Collectors.toList());
			query.setParameter("lectureBlockKeys", lectureBlockKeys);
		}
		if(searchParams.getAppealStatus() != null && !searchParams.getAppealStatus().isEmpty()) {
			List<String> appealStatus = searchParams.getAppealStatus().stream()
					.map(LectureBlockAppealStatus::name).collect(Collectors.toList());
			query.setParameter("appealStatus", appealStatus);
		}
		if(searchParams.getTeacher() != null) {
			query.setParameter("teacherKey", searchParams.getTeacher().getKey());
		}
		if(searchParams.getManager() != null) {
			query.setParameter("managerKey", searchParams.getManager().getKey());
		} else if(searchParams.getMasterCoach() != null) {
			query.setParameter("managerKey", searchParams.getMasterCoach().getKey());
		}
		
		
		return query.getResultList();
	}
	
	public List<LectureBlockAndRollCall> getParticipantLectureBlockAndRollCalls(RepositoryEntryRef entry, IdentityRef identity,
			String teacherSeaparator) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block, call, notice, re.displayname")
		  .append(" from lectureblock block")
		  .append(" inner join block.entry re")
		  .append(" inner join block.groups blockToGroup")
		  .append(" inner join blockToGroup.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" inner join lectureparticipantsummary as summary on (summary.identity.key=membership.identity.key and summary.entry.key=block.entry.key)")
		  .append(" left join lectureblockrollcall as call on (call.identity.key=membership.identity.key and call.lectureBlock.key=block.key)")
		  .append(" left join call.absenceNotice notice")
		  .append(" where membership.identity.key=:identityKey and membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" and block.entry.key=:repoEntryKey and block.endDate>=summary.firstAdmissionDate");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repoEntryKey", entry.getKey())
				.getResultList();
		Map<Long,LectureBlockAndRollCall> blockToRollCallMap = new HashMap<>(); 
		for(Object[] objects:rawObjects) {
			int pos = 0;
			LectureBlock block = (LectureBlock)objects[pos++];
			LectureBlockRollCall rollCall = (LectureBlockRollCall)objects[pos++];
			AbsenceNotice notice = (AbsenceNotice)objects[pos++];
			String displayname = (String)objects[pos];
			blockToRollCallMap.put(block.getKey(), new LectureBlockAndRollCall(displayname, block, rollCall, notice));
		}
		
		appendCoaches(entry, blockToRollCallMap, teacherSeaparator);
		return new ArrayList<>(blockToRollCallMap.values());
	}
	
	private void appendCoaches(RepositoryEntryRef entry, Map<Long,LectureBlockAndRollCall> blockToRollCallMap, String teacherSeaparator) {
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
			LectureBlockAndRollCall rollCall = blockToRollCallMap.get(blockKey);
			if(rollCall != null) {
				Identity coach = (Identity)rawCoach[1];
				String fullname = userManager.getUserDisplayName(coach);
				if(rollCall.getCoach() == null) {
					rollCall.setCoach(fullname);
				} else {
					rollCall.setCoach(rollCall.getCoach() + " " + teacherSeaparator + " " + fullname);
				}
			}
		}
	}
	
	public List<LectureBlockStatistics> getStatistics(IdentityRef identity, RepositoryEntryStatusEnum[] entryStatus,
			boolean authorizedAbsenceEnabled, boolean absenceDefaultAuthorized, boolean countAuthorizedAbsenceAsAttendant,
			boolean calculateAttendanceRate, double requiredAttendanceRateDefault) {
		QueryBuilder sb = new QueryBuilder(5000);
		sb.append("select call.key as callKey, ")
		  .append("  call.lecturesAttendedNumber as attendedLectures,")
		  .append("  call.lecturesAbsentNumber as absentLectures,")
		  .append("  call.absenceAuthorized as absenceAuthorized,")
		  .append("  notice.key as absenceNoticeKey,")
		  .append("  notice.absenceAuthorized as absenceNoticeAuthorized,")
		  .append("  block.key as blockKey,")
		  .append("  block.compulsory as compulsory,")
		  .append("  block.plannedLecturesNumber as blockPlanned,")
		  .append("  block.effectiveLecturesNumber as blockEffective,")
		  .append("  block.statusString as status,")
		  .append("  block.rollCallStatusString as rollCallStatus,")
		  .append("  block.endDate as rollCallEndDate,")
		  .append("  re.key as repoKey,")
		  .append("  re.displayname as repoDisplayName,")
		  .append("  re.externalRef as repoExternalRef,")
		  .append("  config.overrideModuleDefault as overrideDef,")
		  .append("  config.calculateAttendanceRate as calculateRate,")
		  .append("  config.requiredAttendanceRate as repoConfigRate,")//rate enabled
		  .append("  summary.firstAdmissionDate as firstAdmissionDate,")
		  .append("  summary.requiredAttendanceRate as summaryRate")
		  .append(" from lectureblock block")
		  .append(" inner join block.entry re")
		  .append(" inner join block.groups blockToGroup")
		  .append(" inner join blockToGroup.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" inner join lectureentryconfig as config on (re.key=config.entry.key)")
		  .append(" left join lectureparticipantsummary as summary on (summary.identity.key=membership.identity.key and summary.entry.key=block.entry.key)")
		  .append(" left join lectureblockrollcall as call on (call.identity.key=membership.identity.key and call.lectureBlock.key=block.key)")
		  .append(" left join absencenotice as notice on (call.absenceNotice.key=notice.key)")
		  .append(" where config.lectureEnabled=true and membership.identity.key=:identityKey")
		  .append(" and membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" and re.status ").in(entryStatus);
		
		//take in account: requiredAttendanceRateDefault, from repo config requiredAttendanceRate
		//take in account: authorized absence
		//take in account: firstAddmissionDate and null
		
		Date now = new Date();

		Map<Long,LectureBlockStatistics> stats = new HashMap<>();
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey())
				.getResultStream().forEach(rawObject -> {
			int pos = 1;//jump roll call key
			Long lecturesAttended = PersistenceHelper.extractLong(rawObject, pos++);
			Long lecturesAbsent = PersistenceHelper.extractLong(rawObject, pos++);
			Boolean absenceAuthorized;
			if(authorizedAbsenceEnabled) {
				absenceAuthorized = (Boolean)rawObject[pos++];
			} else {
				absenceAuthorized = null;
				pos++;
			}
			Long absenceNoticeKey = (Long)rawObject[pos++];
			Boolean absenceNoticeAuthorized = (Boolean)rawObject[pos++];
			
			Long lectureBlockKey = (Long)rawObject[pos++];
			boolean compulsory = PersistenceHelper.extractBoolean(rawObject, pos++, true);
			Long plannedLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			Long effectiveLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			if(effectiveLecturesNumber == null) {
				effectiveLecturesNumber = plannedLecturesNumber;
			}
			String status = (String)rawObject[pos++];
			String rollCallStatus = (String)rawObject[pos++];
			Date rollCallEndDate = (Date)rawObject[pos++];

			Long repoKey = PersistenceHelper.extractLong(rawObject, pos++);
			String repoDisplayname = (String)rawObject[pos++];
			String repoExternalRef = (String)rawObject[pos++];

			Boolean overrideDefault = (Boolean)rawObject[pos++];
			Boolean repoCalculateRate = (Boolean)rawObject[pos++];
			Double repoRequiredRate = (Double)rawObject[pos++];
			Date firstAdmissionDate = (Date)rawObject[pos++];
			Double persoRequiredRate = (Double)rawObject[pos++];
			
			LectureBlockStatistics entryStatistics;
			if(stats.containsKey(repoKey)) {
				entryStatistics = stats.get(repoKey);
			} else {
				entryStatistics = create(identity.getKey(), lectureBlockKey, repoKey, repoDisplayname, repoExternalRef,
						overrideDefault, repoCalculateRate,  repoRequiredRate,
						persoRequiredRate, calculateAttendanceRate, requiredAttendanceRateDefault);
				stats.put(repoKey, entryStatistics);
			}
			
			appendStatistics(entryStatistics, compulsory, status,
					rollCallEndDate, rollCallStatus,
					lecturesAttended, lecturesAbsent, absenceAuthorized,
					absenceNoticeKey, absenceNoticeAuthorized, absenceDefaultAuthorized,
					plannedLecturesNumber, effectiveLecturesNumber,
					firstAdmissionDate, now);
		});

		List<LectureBlockStatistics> statisticsList = new ArrayList<>(stats.values());
		calculateAttendanceRate(statisticsList, countAuthorizedAbsenceAsAttendant);
		return statisticsList;
	}
	
	public List<LectureBlockIdentityStatistics> getStatistics(LectureStatisticsSearchParameters params,
			List<UserPropertyHandler> userPropertyHandlers, Identity identity,
			boolean authorizedAbsenceEnabled,
			boolean absenceDefaultAuthorized, boolean countAuthorizedAbsenceAsAttendant,
			boolean calculateAttendanceRate, double requiredAttendanceRateDefault) {
		
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select ident.key as participantKey, ident.name as participantName,")
		  .append("  call.lecturesAttendedNumber as attendedLectures,")
		  .append("  call.lecturesAbsentNumber as absentLectures,")
		  .append("  call.absenceAuthorized as absenceAuthorized,")
		  .append("  notice.key as absenceNoticeKey,")
		  .append("  notice.absenceAuthorized as absenceNoticeAuthorized,")
		  .append("  block.key as blockKey,")
		  .append("  block.compulsory as compulsory,")
		  .append("  block.plannedLecturesNumber as blockPlanned,")
		  .append("  block.effectiveLecturesNumber as blockEffective,")
		  .append("  block.statusString as status,")
		  .append("  block.rollCallStatusString as rollCallStatus,")
		  .append("  block.endDate as rollCallEndDate,")
		  .append("  re.key as repoKey,")
		  .append("  re.displayname as repoDisplayName,")
		  .append("  re.externalRef as repoExternalRef,")
		  .append("  config.overrideModuleDefault as overrideDef,")
		  .append("  config.calculateAttendanceRate as calculateRate,")
		  .append("  config.requiredAttendanceRate as repoConfigRate,")//rate enabled
		  .append("  summary.firstAdmissionDate as firstAdmissionDate,")
		  .append("  summary.requiredAttendanceRate as summaryRate");
		for(UserPropertyHandler handler:userPropertyHandlers) {
			sb.append(", user.").append(handler.getName()).append(" as ").append("p_").append(handler.getName());
		} 
		sb.append(" from lectureblock block")
		  .append(" inner join block.entry re")
		  .append(" inner join block.groups blockToGroup")
		  .append(" inner join blockToGroup.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" inner join ident.user user")
		  .append(" inner join lectureentryconfig as config on (re.key=config.entry.key)")
		  .append(" left join lectureblockrollcall as call on (call.identity.key=membership.identity.key and call.lectureBlock.key=block.key)")
		  .append(" left join absencenotice as notice on (call.absenceNotice.key=notice.key)")
		  .append(" left join lectureparticipantsummary as summary on (summary.identity.key=membership.identity.key and summary.entry.key=block.entry.key)")
		  .append(" where config.lectureEnabled=true and membership.role='").append(GroupRoles.participant.name()).append("'");
	
		// check access permission
		sb.append(" and (exists (select rel from repoentrytogroup as rel, bgroupmember as membership ")
		  .append("     where re.key=rel.entry.key and membership.group.key=rel.group.key and membership.identity.key=:identityKey")
		  .append("     and membership.role in ('").append(OrganisationRoles.administrator.name()).append("','").append(OrganisationRoles.lecturemanager.name()).append("','").append(GroupRoles.owner.name()).append("')")
		  .append("     and re.status ").in(RepositoryEntryStatusEnum.publishedAndClosed())
		  .append(" ) or exists (select membership.key from bgroupmember as membership ")
		  .append("     where block.teacherGroup.key=membership.group.key and membership.identity.key=:identityKey")
		  .append("     and re.status ").in(RepositoryEntryStatusEnum.publishedAndClosed())
		  .append(" ))");

		if(params.getLifecycle() != null) {
			sb.append(" and re.lifecycle.key=:lifecycleKey");
		}
		if(params.getStartDate() != null) {
			sb.append(" and block.startDate>=:startDate");
		}
		if(params.getEndDate() != null) {
			sb.append(" and block.endDate<=:endDate");
		}
		if(params.getBulkIdentifiers() != null && !params.getBulkIdentifiers().isEmpty()) {
			sb.append(" and (")
			  .append("  lower(ident.name) in (:bulkIdentifiers)")
			  .append("  or lower(ident.externalId) in (:bulkIdentifiers)")
			  .append("  or lower(user.email) in (:bulkIdentifiers)")
			  .append("  or lower(user.institutionalEmail) in (:bulkIdentifiers)")
			  .append("  or lower(user.institutionalUserIdentifier) in (:bulkIdentifiers)")
			  .append(")");
		}
		if(params.hasEntries()) {
			sb.append(" and re.key in (:repoEntryKeys)");
		}
		
		Map<String,Object> queryParams = new HashMap<>();
		appendUsersStatisticsSearchParams(params, queryParams, userPropertyHandlers, sb);

		TypedQuery<Object[]> rawQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("identityKey", identity.getKey());
		if(StringHelper.containsNonWhitespace(params.getLogin())) {
			rawQuery.setParameter("login", params.getLogin());
		}
		if(params.getLifecycle() != null) {
			rawQuery.setParameter("lifecycleKey", params.getLifecycle().getKey());
		}
		if(params.getStartDate() != null) {
			rawQuery.setParameter("startDate", params.getStartDate(), TemporalType.TIMESTAMP);
		}
		if(params.getEndDate() != null) {
			rawQuery.setParameter("endDate", params.getEndDate(), TemporalType.TIMESTAMP);
		}
		if(params.getBulkIdentifiers() != null && !params.getBulkIdentifiers().isEmpty()) {
			rawQuery.setParameter("bulkIdentifiers", params.getBulkIdentifiers());
		}
		if(params.hasEntries()) {
			List<Long> repoEntryKeys = params.getEntries().stream()
					.map(RepositoryEntryRef::getKey).collect(Collectors.toList());
			rawQuery.setParameter("repoEntryKeys", repoEntryKeys);
		}
		for(Map.Entry<String, Object> entry:queryParams.entrySet()) {
			rawQuery.setParameter(entry.getKey(), entry.getValue());
		}

		Date now = new Date();
		Map<Membership,LectureBlockIdentityStatistics> stats = new HashMap<>();
		rawQuery.getResultStream().forEach(rawObject -> {
			int pos = 0;//jump roll call key
			Long identityKey = (Long)rawObject[pos++];
			String identityName = (String)rawObject[pos++];
			Long lecturesAttended = PersistenceHelper.extractLong(rawObject, pos++);
			Long lecturesAbsent = PersistenceHelper.extractLong(rawObject, pos++);
			Boolean absenceAuthorized;
			if(authorizedAbsenceEnabled) {
				absenceAuthorized = (Boolean)rawObject[pos++];
			} else {
				absenceAuthorized = null;
				pos++;
			}
			Long absenceNoticeKey = (Long)rawObject[pos++];
			Boolean absenceNoticeAuthorized = (Boolean)rawObject[pos++];

			Long lectureBlockKey = (Long)rawObject[pos++];
			boolean compulsory = PersistenceHelper.extractBoolean(rawObject, pos++, true);
			Long plannedLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			Long effectiveLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			if(effectiveLecturesNumber == null) {
				effectiveLecturesNumber = plannedLecturesNumber;
			}
			String status = (String)rawObject[pos++];
			String rollCallStatus = (String)rawObject[pos++];
			Date rollCallEndDate = (Date)rawObject[pos++];
			
			//entry and config
			Long repoKey = PersistenceHelper.extractLong(rawObject, pos++);
			String repoDisplayname = (String)rawObject[pos++];
			String repoExternalRef = (String)rawObject[pos++];
			boolean overrideDefault = PersistenceHelper.extractBoolean(rawObject, pos++, false);
			Boolean repoCalculateRate = (Boolean)rawObject[pos++];
			Double repoRequiredRate = (Double)rawObject[pos++];
			
			//summary
			Date firstAdmissionDate = (Date)rawObject[pos++];
			Double persoRequiredRate = (Double)rawObject[pos++];
			
			LectureBlockIdentityStatistics entryStatistics;
			
			Membership memberKey = new Membership(identityKey, repoKey);
			if(stats.containsKey(memberKey)) {
				entryStatistics = stats.get(memberKey);
			} else {
				
				//user data
				int numOfProperties = userPropertyHandlers.size();
				String[] identityProps = new String[numOfProperties];
				for(int i=0; i<numOfProperties; i++) {
					identityProps[i] = (String)rawObject[pos++];
				}
				
				entryStatistics = createIdentityStatistics(identityKey, identityName, identityProps,
						lectureBlockKey, repoKey, repoDisplayname, repoExternalRef,
						overrideDefault, repoCalculateRate,  repoRequiredRate,
						persoRequiredRate, calculateAttendanceRate, requiredAttendanceRateDefault);
				stats.put(memberKey, entryStatistics);
			}

			appendStatistics(entryStatistics, compulsory, status,
					rollCallEndDate, rollCallStatus,
					lecturesAttended, lecturesAbsent, absenceAuthorized,
					absenceNoticeKey, absenceNoticeAuthorized, absenceDefaultAuthorized,
					plannedLecturesNumber, effectiveLecturesNumber,
					firstAdmissionDate, now);
		});
		
		List<LectureBlockIdentityStatistics> statisticsList = new ArrayList<>(stats.values());
		calculateAttendanceRate(statisticsList, countAuthorizedAbsenceAsAttendant);
		return statisticsList;
	}
	
	private void appendUsersStatisticsSearchParams(LectureStatisticsSearchParameters params, Map<String,Object> queryParams,
			List<UserPropertyHandler> userPropertyHandlers, QueryBuilder sb) {
		if(StringHelper.containsNonWhitespace(params.getLogin())) {
			String login = PersistenceHelper.makeFuzzyQueryString(params.getLogin());
			if (login.contains("_") && dbInstance.isOracle()) {
				//oracle needs special ESCAPE sequence to search for escaped strings
				sb.append(" and lower(ident.name) like :login ESCAPE '\\'");
			} else if (dbInstance.isMySQL()) {
				sb.append(" and ident.name like :login");
			} else {
				sb.append(" and lower(ident.name) like :login");
			}
			queryParams.put("login", login);
		}
		
		if(params.getUserProperties() != null && params.getUserProperties().size() > 0) {
			Map<String,String> searchParams = new HashMap<>(params.getUserProperties());
	
			int count = 0;
			for(Map.Entry<String, String> entry:searchParams.entrySet()) {
				String propName = entry.getKey();
				String propValue = entry.getValue();
				String qName = "p_" + ++count;
				
				UserPropertyHandler handler = userManager.getUserPropertiesConfig().getPropertyHandler(propName);
				if(handler == null) {// fallback if the haandler is disabled
					for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
						if(propName.equals(userPropertyHandler.getName())) {
							handler = userPropertyHandler;
						}
					}
				}
				
				if(dbInstance.isMySQL()) {
					sb.append(" and user.").append(handler.getName()).append(" like :").append(qName);
				} else {
					sb.append(" and lower(user.").append(handler.getName()).append(") like :").append(qName);
					if(dbInstance.isOracle()) {
						sb.append(" escape '\\'");
					}
				}
				queryParams.put(qName, PersistenceHelper.makeFuzzyQueryString(propValue));
			}
		}
		
		if(params.hasOrganisations()) {
			sb.append(" and exists (select orgtomember.key from bgroupmember as orgtomember")
			  .append("  inner join organisation as org on (org.group.key=orgtomember.group.key)")
			  .append("  where orgtomember.identity.key=ident.key and org.key in (:organisationKey))");
			
			Set<Long> organisationKeys = params.getOrganisations().stream().map(OrganisationRef::getKey).collect(Collectors.toSet());
			queryParams.put("organisationKey", organisationKeys);
		}
	}
	

	public List<LectureBlockStatistics> getStatistics(RepositoryEntry entry,
			RepositoryEntryLectureConfiguration config, boolean authorizedAbsenceEnabled,
			boolean absenceDefaultAuthorized, boolean countAuthorizedAbsenceAsAttendant,
			boolean calculateAttendanceRate, double requiredAttendanceRateDefault) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select ident.key as participantKey, ")
		  .append("  call.lecturesAttendedNumber as attendedLectures,")
		  .append("  call.lecturesAbsentNumber as absentLectures,")
		  .append("  call.absenceAuthorized as absenceAuthorized,")
		  .append("  notice.key as absenceNoticeKey,")
		  .append("  notice.absenceAuthorized as absenceNoticeAuthorized,")
		  .append("  block.key as blockKey,")
		  .append("  block.compulsory as compulsory,")
		  .append("  block.plannedLecturesNumber as blockPlanned,")
		  .append("  block.effectiveLecturesNumber as blockEffective,")
		  .append("  block.statusString as status,")
		  .append("  block.rollCallStatusString as rollCallStatus,")
		  .append("  block.endDate as rollCallEndDate,")
		  .append("  summary.firstAdmissionDate as firstAdmissionDate,")
		  .append("  summary.requiredAttendanceRate as summaryRate")
		  .append(" from lectureblock block")
		  .append(" inner join block.groups blockToGroup")
		  .append(" inner join blockToGroup.group bGroup")
		  .append(" inner join bGroup.members membership")
		  .append(" inner join membership.identity ident")
		  .append(" left join lectureblockrollcall as call on (call.identity.key=membership.identity.key and call.lectureBlock.key=block.key)")
		  .append(" left join absencenotice as notice on (call.absenceNotice.key=notice.key)")
		  .append(" left join lectureparticipantsummary as summary on (summary.identity.key=membership.identity.key and summary.entry.key=block.entry.key)")
		  .append(" where block.entry.key=:entryKey and membership.role='").append(GroupRoles.participant.name()).append("'");

		final Date now = new Date();
		final Boolean repoCalculateRate;
		final Double repoRequiredRate;
		if(config != null && config.isOverrideModuleDefault()) {
			repoCalculateRate = config.getCalculateAttendanceRate();
			repoRequiredRate = config.getRequiredAttendanceRate();
		} else {
			repoCalculateRate = null;
			repoRequiredRate = null;
		}

		Map<Long,LectureBlockStatistics> stats = new HashMap<>();
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("entryKey", entry.getKey())
				.getResultStream().forEach(rawObject ->  {
			int pos = 0;//jump roll call key
			Long identityKey = (Long)rawObject[pos++];
			Long lecturesAttended = PersistenceHelper.extractLong(rawObject, pos++);
			Long lecturesAbsent = PersistenceHelper.extractLong(rawObject, pos++);
			Boolean absenceAuthorized;
			if(authorizedAbsenceEnabled) {
				absenceAuthorized = (Boolean)rawObject[pos++];
			} else {
				absenceAuthorized = null;
				pos++;
			}
			Long absenceNoticeKey = (Long)rawObject[pos++];
			Boolean absenceNoticeAuthorized = (Boolean)rawObject[pos++];

			Long lectureBlockKey = (Long)rawObject[pos++];
			boolean compulsory = PersistenceHelper.extractBoolean(rawObject, pos++, true);
			Long plannedLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			Long effectiveLecturesNumber = PersistenceHelper.extractLong(rawObject, pos++);
			if(effectiveLecturesNumber == null) {
				effectiveLecturesNumber = plannedLecturesNumber;
			}
			String status = (String)rawObject[pos++];
			String rollCallStatus = (String)rawObject[pos++];
			Date rollCallEndDate = (Date)rawObject[pos++];
			
			Date firstAdmissionDate = (Date)rawObject[pos++];
			Double persoRequiredRate = (Double)rawObject[pos++];
			
			LectureBlockStatistics entryStatistics;
			if(stats.containsKey(identityKey)) {
				entryStatistics = stats.get(identityKey);
			} else {
				entryStatistics = create(identityKey, lectureBlockKey,
						entry.getKey(), entry.getDisplayname(), entry.getExternalRef(),
						config.isOverrideModuleDefault(), repoCalculateRate,  repoRequiredRate,
						persoRequiredRate, calculateAttendanceRate, requiredAttendanceRateDefault);
				stats.put(identityKey, entryStatistics);
			}

			appendStatistics(entryStatistics, compulsory, status,
					rollCallEndDate, rollCallStatus,
					lecturesAttended, lecturesAbsent,
					absenceAuthorized, absenceNoticeKey, absenceNoticeAuthorized, absenceDefaultAuthorized,
					plannedLecturesNumber, effectiveLecturesNumber,
					firstAdmissionDate, now);
		});
		
		List<LectureBlockStatistics> statisticsList = new ArrayList<>(stats.values());
		calculateAttendanceRate(statisticsList, countAuthorizedAbsenceAsAttendant);
		return statisticsList;
	}
	
	private void calculateAttendanceRate(List<? extends LectureBlockStatistics> statisticsList, boolean countAuthorizedAbsenceAsAttendant) {
		for(LectureBlockStatistics statistics:statisticsList) {
			calculateAttendanceRate(statistics, countAuthorizedAbsenceAsAttendant);
		}
	}

	protected void calculateAttendanceRate(LectureBlockStatistics statistics, boolean countAuthorizedAbsenceAsAttendant) {
		long totalAttendedLectures = statistics.getTotalAttendedLectures();
		long totalAbsentLectures = statistics.getTotalAbsentLectures();
		if(countAuthorizedAbsenceAsAttendant) {
			totalAttendedLectures += statistics.getTotalAuthorizedAbsentLectures();
		} else {
			totalAbsentLectures += statistics.getTotalAuthorizedAbsentLectures();
		}
		if(totalAbsentLectures < 0) {
			totalAbsentLectures = 0;
		}

		long totalLectures = totalAbsentLectures + totalAttendedLectures;
		double rate;
		if(totalLectures == 0 || totalAttendedLectures == 0) {
			rate = 0.0d;
		} else {
			rate = (double)totalAttendedLectures / (double)totalLectures;
		}
		statistics.setAttendanceRate(rate);
	}
	
	private LectureBlockStatistics create(Long identityKey, Long lectureBlockKey, Long entryKey, String displayName, String externalRef,
			Boolean overrideDefault, Boolean repoCalculateRate, Double repoRequiredRate,
			Double persoRequiredRate, boolean calculateAttendanceRate, double requiredAttendanceRateDefault) {

		RequiredRate requiredRate = calculateRequiredRate(overrideDefault, repoCalculateRate, repoRequiredRate,
				persoRequiredRate, calculateAttendanceRate, requiredAttendanceRateDefault);
		return new LectureBlockStatistics(identityKey, lectureBlockKey, entryKey, displayName, externalRef,
				requiredRate.isCalculateRate(), requiredRate.getRequiredRate());
	}
	
	private LectureBlockIdentityStatistics createIdentityStatistics(Long identityKey, String identityName, String[] identityProps,
			Long lectureBlockKey, Long entryKey, String displayName, String externalRef,
			Boolean overrideDefault, Boolean repoCalculateRate, Double repoRequiredRate,
			Double persoRequiredRate, boolean calculateAttendanceRate, double requiredAttendanceRateDefault) {

		RequiredRate requiredRate = calculateRequiredRate(overrideDefault, repoCalculateRate, repoRequiredRate,
				persoRequiredRate, calculateAttendanceRate, requiredAttendanceRateDefault);
		return new LectureBlockIdentityStatistics(identityKey, identityName, identityProps, lectureBlockKey,
				entryKey, displayName, externalRef, requiredRate.isCalculateRate(), requiredRate.getRequiredRate());
	}
	
	private RequiredRate calculateRequiredRate(Boolean overrideDefault, Boolean repoCalculateRate, Double repoRequiredRate,
			Double persoRequiredRate, boolean calculateAttendanceRate, double requiredAttendanceRateDefault) {
		
		final boolean calculateRate;
		if(repoCalculateRate != null && overrideDefault != null && !overrideDefault.booleanValue()) {
			calculateRate = repoCalculateRate.booleanValue();
		} else {
			calculateRate = calculateAttendanceRate;
		}

		double requiredRate = -1.0d;
		if(calculateRate) {
			if(persoRequiredRate != null && persoRequiredRate.doubleValue() >= 0.0d) {
				requiredRate = persoRequiredRate.doubleValue();
			} else if(repoRequiredRate != null && repoRequiredRate.doubleValue() >= 0.0d) {
				requiredRate = repoRequiredRate.doubleValue();
			} else {
				requiredRate = requiredAttendanceRateDefault;
			}
		}
		
		return new RequiredRate(calculateRate, requiredRate);
	}
	
	private void appendStatistics(LectureBlockStatistics statistics, boolean compulsory, String blockStatus,
			Date rollCallEndDate, String rollCallStatus,
			Long lecturesAttended, Long lecturesAbsent, Boolean absenceAuthorized,
			Long absenceNoticeKey, Boolean absenceNoticeAuthorized, boolean absenceDefaultAuthorized,
			Long plannedLecturesNumber, Long effectiveLecturesNumber,
			Date firstAdmissionDate, Date now) {
		if(!compulsory) return;// not compulsory blocks are simply ignored
		
		if(absenceNoticeAuthorized != null) {// notice override roll call
			absenceAuthorized = absenceNoticeAuthorized;
		}
		
		//only count closed roll call after the end date
		if(rollCallEndDate != null && rollCallEndDate.before(now)
				&& firstAdmissionDate != null && firstAdmissionDate.before(rollCallEndDate)
				&& blockStatus != null && !LectureBlockStatus.cancelled.name().equals(blockStatus)
				&& rollCallStatus != null && (LectureRollCallStatus.closed.name().equals(rollCallStatus) || LectureRollCallStatus.autoclosed.name().equals(rollCallStatus))) {
		
			if(absenceNoticeKey != null) {
				long numOfLectures = 0l;
				if(effectiveLecturesNumber != null) {
					numOfLectures = effectiveLecturesNumber.longValue();
				} else if(plannedLecturesNumber != null) {
					numOfLectures = plannedLecturesNumber.longValue();
				}
				
				if(absenceNoticeAuthorized != null) {
					if(absenceAuthorized.booleanValue()) {
						statistics.addTotalAuthorizedAbsentLectures(numOfLectures);
					} else {
						statistics.addTotalAbsentLectures(numOfLectures);
					}
				} else if(absenceDefaultAuthorized) {
					statistics.addTotalAuthorizedAbsentLectures(numOfLectures);
				} else {
					statistics.addTotalAbsentLectures(numOfLectures);
				}
			} else {
			
				if(lecturesAbsent != null) {
					if(absenceAuthorized != null) {
						if(absenceAuthorized.booleanValue()) {
							statistics.addTotalAuthorizedAbsentLectures(lecturesAbsent.longValue());
						} else {
							statistics.addTotalAbsentLectures(lecturesAbsent.longValue());
						}
					} else if(absenceDefaultAuthorized) {
						statistics.addTotalAuthorizedAbsentLectures(lecturesAbsent.longValue());
					} else {
						statistics.addTotalAbsentLectures(lecturesAbsent.longValue());
					}
				}
				if(lecturesAttended != null) {
					statistics.addTotalAttendedLectures(lecturesAttended.longValue());
				}
			}

			if(effectiveLecturesNumber != null) {
				statistics.addTotalEffectiveLectures(effectiveLecturesNumber.longValue());
			} else if(plannedLecturesNumber != null) {
				statistics.addTotalEffectiveLectures(plannedLecturesNumber.longValue());
			}
		}
		
		if(firstAdmissionDate != null && firstAdmissionDate.before(rollCallEndDate)
				&& (blockStatus == null || !LectureBlockStatus.cancelled.name().equals(blockStatus))) {
			// apply the effective lectures number only if the roll call is closed
			if(effectiveLecturesNumber != null && rollCallStatus != null 
					&& (LectureRollCallStatus.closed.name().equals(rollCallStatus) || LectureRollCallStatus.autoclosed.name().equals(rollCallStatus))) {
				statistics.addTotalPersonalPlannedLectures(effectiveLecturesNumber.longValue());
			} else if(plannedLecturesNumber != null) {
				statistics.addTotalPersonalPlannedLectures(plannedLecturesNumber.longValue());
			}
		}
	}
	
	public AggregatedLectureBlocksStatistics aggregatedStatistics(List<? extends LectureBlockStatistics> statisticsList,
			boolean countAuthorizedAbsenceAsAttendant) {
		
		long totalPersonalPlannedLectures = 0;
		long totalAttendedLectures = 0;
		long totalAuthorizedAbsentLectures = 0;
		long totalAbsentLectures = 0;
		
		long attendedForRate = 0;
		long absentForRate = 0;

		for(LectureBlockStatistics statistics:statisticsList) {
			totalPersonalPlannedLectures += statistics.getTotalPersonalPlannedLectures();
			totalAuthorizedAbsentLectures += statistics.getTotalAuthorizedAbsentLectures();
			totalAttendedLectures += statistics.getTotalAttendedLectures();
			totalAbsentLectures += statistics.getTotalAbsentLectures();
			
			attendedForRate += statistics.getTotalAttendedLectures();
			absentForRate += statistics.getTotalAbsentLectures();
			if(countAuthorizedAbsenceAsAttendant) {
				attendedForRate += statistics.getTotalAuthorizedAbsentLectures();
			} else {
				absentForRate += statistics.getTotalAuthorizedAbsentLectures();
			}
		}
		
		long totalLectures = attendedForRate + absentForRate;
		double rate;
		if(totalLectures == 0 || attendedForRate == 0) {
			rate = 0.0d;
		} else {
			rate = (double)attendedForRate / (double)totalLectures;
		}
		
		double currentRate;
		if(attendedForRate == 0) {
			currentRate = 0.0d;
		} else {
			currentRate = attendedForRate / ((double)attendedForRate + (double)absentForRate);
		}
		
		return new AggregatedLectureBlocksStatistics(totalPersonalPlannedLectures, totalAttendedLectures, totalAuthorizedAbsentLectures, totalAbsentLectures,
				rate, currentRate);
	}
	
	private static class Membership {
		private final Long identityKey;
		private final Long repoEntryKey;
		
		public Membership(Long identityKey, Long repoEntryKey) {
			this.identityKey = identityKey;
			this.repoEntryKey = repoEntryKey;
		}

		@Override
		public int hashCode() {
			return identityKey.hashCode() + repoEntryKey.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof Membership) {
				Membership membership = (Membership)obj;
				return identityKey != null && identityKey.equals(membership.identityKey)
						&& repoEntryKey != null && repoEntryKey.equals(membership.repoEntryKey);
			}
			return false;
		}
	}
	
	private static class RequiredRate {
		
		private final boolean calculateRate;
		private final double requiredRate;
		
		public RequiredRate(boolean calculateRate, double requiredRate) {
			this.calculateRate = calculateRate;
			this.requiredRate = requiredRate;
		}

		public boolean isCalculateRate() {
			return calculateRate;
		}

		public double getRequiredRate() {
			return requiredRate;
		}
	}
}
