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
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeRef;
import org.olat.modules.lecture.AbsenceNoticeSearchParameters;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.model.AbsenceNoticeImpl;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.modules.lecture.model.AbsenceNoticeRefImpl;
import org.olat.modules.lecture.model.LectureBlockWithNotice;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AbsenceNoticeDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AbsenceNotice createAbsenceNotice(Identity identity, AbsenceNoticeType type, AbsenceNoticeTarget target,
			Date start, Date end, AbsenceCategory category, String absenceRason, Boolean authorized,
			Identity authorizer, Identity notifier) {
		AbsenceNoticeImpl notice = new AbsenceNoticeImpl();
		notice.setCreationDate(new Date());
		notice.setLastModified(notice.getCreationDate());
		notice.setNoticeType(type);
		notice.setStartDate(start);
		notice.setEndDate(end);
		notice.setAbsenceCategory(category);
		notice.setAbsenceReason(absenceRason);
		notice.setNoticeTarget(target);
		notice.setAbsenceAuthorized(authorized);
		notice.setIdentity(identity);
		notice.setNotifier(notifier);
		notice.setAuthorizer(authorizer);
		dbInstance.getCurrentEntityManager().persist(notice);
		return notice;
	}
	
	public AbsenceNotice updateAbsenceNotice(AbsenceNotice notice) {
		((AbsenceNoticeImpl)notice).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(notice);
	}
	
	public void deleteAbsenceNotice(AbsenceNotice notice) {
		dbInstance.getCurrentEntityManager().remove(notice);
	}
	
	public List<LectureBlockRollCall> getRollCalls(AbsenceNotice notice) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rollCall from lectureblockrollcall rollCall")
		  .append(" inner join fetch rollCall.absenceNotice as notice")
		  .append(" left join fetch rollCall.lectureBlock as lectureBlock")
		  .append(" where notice.key=:noticeKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LectureBlockRollCall.class)
				.setParameter("noticeKey", notice.getKey())
				.getResultList();
	}
	
	public List<LectureBlockWithNotice> loadLectureBlocksOf(List<AbsenceNotice> notices, AbsenceNoticeTarget target) {
		if(notices.isEmpty()) return new ArrayList<>();
		
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select block, entry, notice.key")
		  .append(" from lectureblock block")
		  .append(" inner join block.entry as entry");
		
		if(target == AbsenceNoticeTarget.lectureblocks) {
			sb.append(" inner join absencenoticetolectureblock as noticeToBlock on (noticeToBlock.lectureBlock.key=block.key)")
			  .append(" inner join noticeToBlock.absenceNotice as notice")
			  .append(" where notice.key in (:noticeKeys)");
		} else if(target == AbsenceNoticeTarget.entries) {
			sb.append(" inner join absencenoticetoentry as noticeToEntry on (noticeToEntry.entry.key=entry.key)")
			  .append(" inner join noticeToEntry.absenceNotice as notice")
			  .append(" where notice.key in (:noticeKeys)");
		} else if(target == AbsenceNoticeTarget.allentries) {
			sb.append(" inner join block.groups as blockToGroup")
			  .append(" inner join blockToGroup.group as bGroup")
			  .append(" inner join bGroup.members participants on (participants.role='").append(GroupRoles.participant.name()).append("')")
			  .append(" inner join absencenotice notice on (participants.identity.key=notice.identity.key)")
			  .append(" where notice.key in (:noticeKeys)");
		}
		
		if(target == AbsenceNoticeTarget.entries || target == AbsenceNoticeTarget.allentries) {
			// date constraints
			sb.append(" and ");
			noticeBlockDates(sb);
		}
		
		List<Long> noticeKeys = notices.stream()
				.map(AbsenceNotice::getKey).collect(Collectors.toList());
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("noticeKeys", noticeKeys)
			.getResultList();
		
		List<LectureBlockWithNotice> blockWithNotices = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			LectureBlock block = (LectureBlock)rawObject[0];
			RepositoryEntry entry = (RepositoryEntry)rawObject[1];
			Long noticeKey = (Long)rawObject[2];
			AbsenceNoticeRef noticeRef = AbsenceNoticeRefImpl.valueOf(noticeKey);
			blockWithNotices.add(new LectureBlockWithNotice(block, entry, noticeRef));
		}
		return blockWithNotices;
	}
	
	protected static QueryBuilder noticeBlockDates(QueryBuilder sb) {
		sb.append("(")
		  .append(" (notice.startDate<=block.startDate and notice.endDate>=block.endDate)")
		  .append(" or ")
		  .append(" (notice.startDate>=block.startDate and notice.endDate<=block.startDate)")
		  .append(" or ")
		  .append(" (notice.startDate>=block.endDate and notice.endDate<=block.endDate)")
		  .append(")");
		return sb;
	}
	
	public AbsenceNotice loadAbsenceNotice(Long noticeKey) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select notice from absencenotice as notice")
		  .append(" left join fetch notice.absenceCategory as category")
		  .append(" inner join fetch notice.identity as aIdent")
		  .append(" inner join fetch aIdent.user as aUser")
		  .append(" where notice.key=:noticeKey");
		
		List<AbsenceNotice> notices = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AbsenceNotice.class)
				.setParameter("noticeKey", noticeKey)
				.getResultList();
		return notices != null && !notices.isEmpty() ? notices.get(0) : null;
	}
	
	public List<AbsenceNotice> detectCollision(Identity identity, AbsenceNoticeRef noticeToIgnore, Date start, Date end) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select notice from absencenotice as notice")
		  .append(" inner join fetch notice.identity as aIdent")
		  .append(" inner join fetch aIdent.user as aUser")
		  .append(" where aIdent.key=:identityKey")
		  .append(" and (")
		  .append("  (notice.startDate<=:start and notice.endDate>=:end)")
		  .append("  or (notice.startDate>=:start and notice.endDate<=:end)")
		  .append("  or (notice.startDate>=:start and notice.startDate<=:end)")
		  .append("  or (notice.endDate>=:start and notice.endDate<=:end)")
		  .append(" )");
		if(noticeToIgnore != null) {
			sb.append(" and notice.key<>:noticeToIgnoreKey");
		}
		
		TypedQuery<AbsenceNotice> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AbsenceNotice.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("start", start, TemporalType.TIMESTAMP)
			.setParameter("end", end, TemporalType.TIMESTAMP);
		if(noticeToIgnore != null) {
			query.setParameter("noticeToIgnoreKey", noticeToIgnore.getKey());
		}
		
		return query.getResultList();
	}
	
	public List<AbsenceNoticeInfos> search(AbsenceNoticeSearchParameters searchParams, boolean absenceDefaultAuthorized) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select notice from absencenotice as notice")
		  .append(" left join fetch notice.absenceCategory as category")
		  .append(" inner join fetch notice.identity as aIdent")
		  .append(" inner join fetch aIdent.user as aUser");
		if(!searchParams.getTypes().isEmpty()) {
			sb.and().append(" notice.type in (:types)");
		}
		if(searchParams.isLinkedToRollCall()) {
			sb.and()
			  .append("exists (select rollCall.key from lectureblockrollcall as rollCall")
			  .append("  where rollCall.absenceNotice.key=notice.key")
			  .append(")");
		}
		
		if(searchParams.getStartDate() != null && searchParams.getEndDate() != null) {
			sb.and().append("(")
			  .append("(notice.startDate<=:startDate and notice.endDate>=:endDate)")
			  .append(" or ")
			  .append("(notice.startDate>=:startDate and notice.endDate<=:startDate)")
			  .append(" or ")
			  .append("(notice.startDate>=:startDate and notice.startDate<=:endDate)")
			  .append(" or ")
			  .append("(notice.endDate>=:startDate and notice.endDate<=:endDate)")
			  .append(")");
		} else if(searchParams.getStartDate() != null) {
			sb.and().append(" notice.startDate>=:startDate");
		} else if(searchParams.getEndDate() != null) {
			sb.and().append(" notice.endDate<=:endDate");
		}
		
		if(searchParams.getParticipant() != null) {
			sb.and().append(" aIdent.key=:participantKey");
		}
		
		if(searchParams.getAuthorized() != null) {
			sb.and().append(" (notice.absenceAuthorized=").append(searchParams.getAuthorized().booleanValue());
			if((searchParams.getAuthorized().booleanValue() && absenceDefaultAuthorized)
					|| (!searchParams.getAuthorized().booleanValue() && !absenceDefaultAuthorized)) {
				sb.append(" or notice.absenceAuthorized is null");
			}
			sb.append(")");
		}
		
		if(searchParams.getAbsenceCategory() != null) {
			sb.and().append(" notice.absenceCategory.key=:absenceCategoryKey");
		}
		
		if(searchParams.getTeacher() != null) {
			sb.and().append(" exists (select block.key from lectureblock as block")
			  .append("  inner join block.teacherGroup tGroup")
			  .append("  inner join tGroup.members tMembership")
			  .append("  inner join block.groups as blockToGroup")
			  .append("  inner join blockToGroup.group as bGroup")
			  .append("  inner join bGroup.members participants") 
			  .append("  where tMembership.identity.key=:teacherKey")
			  .append("  and aIdent.key=participants.identity.key and participants.role ").in(GroupRoles.participant.name())
			  .append(")");
		}
		
		if(searchParams.getMasterCoach() != null) {
			sb.and().append(" exists (select curEl.key from curriculumelement as curEl")
			  .append("  inner join curEl.group as curElGroup")
			  .append("  inner join curElGroup.members participants")
			  .append("  inner join curElGroup.members masterCoaches")
			  .append("  where masterCoaches.identity.key=:masterCoachKey and masterCoaches.role ").in(CurriculumRoles.mastercoach.name())
			  .append("  and aIdent.key=participants.identity.key and participants.role ").in(GroupRoles.participant.name())
			  .append(")");
		}
		
		if(searchParams.getManagedOrganisations() != null && !searchParams.getManagedOrganisations().isEmpty()) {
			sb.and().append(" exists (select orgtomember.key from bgroupmember as orgtomember ")
			  .append("  inner join organisation as org on (org.group.key=orgtomember.group.key)")
			  .append("  where orgtomember.identity.key=aIdent.key and org.key in (:organisationKey)")
			  .append(")");
		}
		
		TypedQuery<AbsenceNotice> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AbsenceNotice.class);
		if(!searchParams.getTypes().isEmpty()) {
			List<String> types = searchParams.getTypes()
					.stream().map(AbsenceNoticeType::name).collect(Collectors.toList());
			query.setParameter("types", types);
		}
		if(searchParams.getStartDate() != null) {
			query.setParameter("startDate", searchParams.getStartDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getEndDate() != null) {
			query.setParameter("endDate", searchParams.getEndDate(), TemporalType.TIMESTAMP);
		}

		if(searchParams.getParticipant() != null) {
			query.setParameter("participantKey", searchParams.getParticipant().getKey());
		}
		if(searchParams.getTeacher() != null) {
			query.setParameter("teacherKey", searchParams.getTeacher().getKey());
		}
		if(searchParams.getMasterCoach() != null) {
			query.setParameter("masterCoachKey", searchParams.getMasterCoach().getKey());
		}
		if(searchParams.getAbsenceCategory() != null) {
			query.setParameter("absenceCategoryKey", searchParams.getAbsenceCategory().getKey());
		}

		if(searchParams.getManagedOrganisations() != null && !searchParams.getManagedOrganisations().isEmpty()) {
			List<Long> organisationKeys = searchParams.getManagedOrganisations().stream()
					.map(OrganisationRef::getKey).collect(Collectors.toList());
			query.setParameter("organisationKey", organisationKeys);
		}
		
		List<AbsenceNotice> rawObjects = query.getResultList();
		List<AbsenceNoticeInfos> infos = new ArrayList<>(rawObjects.size());
		for(AbsenceNotice rawObject:rawObjects) {
			infos.add(new AbsenceNoticeInfos(rawObject));
		}
		return infos;
	}
	
	/**
	 * 
	 * @param calleeIdentity The callee (optional)
	 * @param lectureBlock The lecture block (mandatory)
	 * @return
	 */
	public List<AbsenceNotice> getAbsenceNotices(IdentityRef calleeIdentity, LectureBlock lectureBlock) {
		QueryBuilder sb = new QueryBuilder(1024);
		sb.append("select notice from absencenotice as notice")
		  .append(" inner join fetch notice.identity as aIdent")
		  .append(" inner join fetch aIdent.user as aUser");
		if(calleeIdentity != null) {
			sb.and().append(" aIdent.key=:identKey");
		}
		if(lectureBlock != null) {
			sb.and()
			  .append(" (")
			  .append("  exists (select noticeToBlock.key from absencenoticetolectureblock noticeToBlock")
			  .append("    where notice.target ").in(AbsenceNoticeTarget.lectureblocks)
			  .append("    and noticeToBlock.absenceNotice.key=notice.key and noticeToBlock.lectureBlock.key=:lectureBlockKey")
			  .append("  ) or exists (select noticeToEntry.key from absencenoticetoentry noticeToEntry")
			  .append("    where notice.target ").in(AbsenceNoticeTarget.entries)
			  .append("    and noticeToEntry.absenceNotice.key=notice.key and noticeToEntry.entry.key=:entryKey")
			  .append("    and (")
			  .append("      (notice.startDate<=:startDate and notice.endDate>=:endDate)")
			  .append("      or (notice.startDate>=:startDate and notice.startDate<:endDate)")
			  .append("      or (notice.endDate>=:startDate and notice.endDate<:endDate)")
			  .append("    )")
			  .append("  ) or (notice.target ").in(AbsenceNoticeTarget.allentries)
			  .append("    and (")
			  .append("      (notice.startDate<=:startDate and notice.endDate>=:endDate)")
			  .append("      or (notice.startDate>=:startDate and notice.startDate<:endDate)")
			  .append("      or (notice.endDate>=:startDate and notice.endDate<:endDate)")
			  .append("    )")
			  .append("  )")
			  .append(")");
		}

		TypedQuery<AbsenceNotice> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AbsenceNotice.class);
		if(calleeIdentity != null) {
			query.setParameter("identKey", calleeIdentity.getKey());
		}
		if(lectureBlock != null) {
			query.setParameter("lectureBlockKey", lectureBlock.getKey())
			     .setParameter("entryKey", lectureBlock.getEntry().getKey())
			     .setParameter("startDate", lectureBlock.getStartDate(), TemporalType.TIMESTAMP)
			     .setParameter("endDate", lectureBlock.getEndDate(), TemporalType.TIMESTAMP);
		}
		return query.getResultList();
	}

}
