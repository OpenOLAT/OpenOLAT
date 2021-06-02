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
package org.olat.course.assessment.manager;

import static org.olat.core.commons.persistence.PersistenceHelper.appendAnd;
import static org.olat.core.commons.persistence.PersistenceHelper.appendFuzzyLike;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroupRef;
import org.olat.group.area.BGtoAreaRelationImpl;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentModeDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentMode getAssessmentModeById(Long key) {
		List<AssessmentMode> modes = dbInstance.getCurrentEntityManager()
			.createNamedQuery("assessmentModeById", AssessmentMode.class)
			.setParameter("modeKey", key)
			.getResultList();
		
		return modes == null || modes.isEmpty() ? null : modes.get(0);
	}
	
	public AssessmentMode getAssessmentModeByLecture(LectureBlock lectureBlock) {
		StringBuilder sb = new StringBuilder(512);
		sb.append("select mode from courseassessmentmode mode")
		  .append(" inner join fetch mode.repositoryEntry v")
		  .append(" inner join fetch v.olatResource res")
		  .append(" inner join fetch mode.lectureBlock block")
		  .append(" where block.key=:blockKey");
		
		List<AssessmentMode> modes = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), AssessmentMode.class)
			.setParameter("blockKey", lectureBlock.getKey())
			.getResultList();
		
		return modes == null || modes.isEmpty() ? null : modes.get(0);
	}
	
	public List<AssessmentMode> findAssessmentMode(SearchAssessmentModeParams params) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select mode from courseassessmentmode mode")
		  .append(" inner join fetch mode.repositoryEntry v")
		  .append(" inner join fetch v.olatResource res");
		
		boolean where = false;
		
		Date dateFrom = params.getDateFrom();
		Date dateTo = params.getDateTo();
		if(dateFrom != null && dateTo != null) {
			where = appendAnd(sb, where);
			sb.append("mode.beginWithLeadTime>=:dateFrom and mode.endWithFollowupTime<=:dateTo");
		} else if(dateFrom != null) {
			where = appendAnd(sb, where);
			sb.append("mode.beginWithLeadTime>=:dateFrom");
		} else if(dateTo != null) {
			where = appendAnd(sb, where);
			sb.append("mode.endWithFollowupTime<=:dateTo");
		}
		
		String name = params.getName();
		if(StringHelper.containsNonWhitespace(name)) {
			name = PersistenceHelper.makeFuzzyQueryString(name);
			where = appendAnd(sb, where);
			sb.append("(");
			appendFuzzyLike(sb, "v.displayname", "name", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "mode.name", "name", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		Long id = null;
		String refs = null;
		String fuzzyRefs = null;
		if(StringHelper.containsNonWhitespace(params.getIdAndRefs())) {
			refs = params.getIdAndRefs();
			fuzzyRefs = PersistenceHelper.makeFuzzyQueryString(refs);
			where = appendAnd(sb, where);
			sb.append(" (v.externalId=:ref or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.externalRef", "fuzzyRefs", dbInstance.getDbVendor());
			sb.append(" or v.softkey=:ref");
			if(StringHelper.isLong(refs)) {
				try {
					id = Long.parseLong(refs);
					sb.append(" or v.key=:vKey or res.resId=:vKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}
		
		sb.append(" order by mode.beginWithLeadTime desc ");

		TypedQuery<AssessmentMode> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class);
		if(StringHelper.containsNonWhitespace(params.getName())) {
			query.setParameter("name", name);
		}
		if(id != null) {
			query.setParameter("vKey", id);
		}
		if(refs != null) {
			query.setParameter("ref", refs);
		}
		if(fuzzyRefs != null) {
			query.setParameter("fuzzyRefs", fuzzyRefs);
		}
		if(dateFrom != null) {
			query.setParameter("dateFrom", dateFrom, TemporalType.TIMESTAMP);
		}
		if(dateTo != null) {
			query.setParameter("dateTo", dateTo, TemporalType.TIMESTAMP);
		}
		return query.getResultList();
	}
	
	public List<AssessmentMode> getAssessmentModeFor(RepositoryEntryRef entry) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("assessmentModeByRepoEntry", AssessmentMode.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}
	
	public List<AssessmentMode> getPlannedAssessmentMode(RepositoryEntryRef entry, Date from, Date to) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mode from courseassessmentmode mode")
		  .append(" left join fetch mode.lectureBlock block")
		  .append(" where mode.repositoryEntry.key=:entryKey ")
		  .append(" and ((mode.begin>=:from and mode.begin<=:to) or (mode.end>=:from and mode.end<=:to) or (mode.begin<=:from and mode.end >=:to))")
		  .append(" order by mode.begin asc");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("from", from)
				.setParameter("to", to)
				.getResultList();
	}
	
	public List<AssessmentMode> getAssessmentModes(Date now) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select mode from courseassessmentmode mode where ")
		  .append(" (mode.beginWithLeadTime<=:now and mode.endWithFollowupTime>=:now")
		  .append("   and (mode.manualBeginEnd=false or (mode.manualBeginEnd=true and mode.leadTime>0)))")
		  .append(" or mode.statusString ").in(Status.leadtime, Status.assessment, Status.followup)
		  .append(" or (mode.statusString ").in(Status.end.name()).append(" and mode.endStatusString ").in(EndStatus.withoutDisadvantage).append(")");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class)
				.setParameter("now", now)
				.getResultList();
	}
	
	public boolean isInAssessmentMode(RepositoryEntryRef entry, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(mode) from courseassessmentmode mode where ")
		  .append(" mode.repositoryEntry.key=:repoKey and (")
		  .append(" (mode.beginWithLeadTime<=:now and mode.endWithFollowupTime>=:now ")
		  .append("   and (mode.manualBeginEnd=false or (mode.manualBeginEnd=true and mode.leadTime>0)))")
		  .append(" or mode.statusString ").in(Status.leadtime, Status.assessment, Status.followup)
		  .append(" or (mode.statusString ").in(Status.end.name()).append(" and mode.endStatusString ").in(EndStatus.withoutDisadvantage).append("))");

		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("now", date)
				.setParameter("repoKey", entry.getKey())
				.getResultList();
		return count != null && !count.isEmpty() && count.get(0).intValue() > 0;
	}
	
	public List<AssessmentMode> getCurrentAssessmentMode(RepositoryEntryRef entry, Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select mode from courseassessmentmode mode")
		  .append(" left join fetch mode.lectureBlock block")
		  .append(" where mode.repositoryEntry.key=:repoKey and (")
		  .append(" (mode.beginWithLeadTime<=:now and mode.endWithFollowupTime>=:now")
		  .append("   and (mode.manualBeginEnd=false or (mode.manualBeginEnd=true and mode.leadTime>0)))")
		  .append(" or mode.statusString ").in(Status.leadtime, Status.assessment, Status.followup)
		  .append(" or (mode.statusString ").in(Status.end.name()).append(" and mode.endStatusString ").in(EndStatus.withoutDisadvantage).append("))");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class)
				.setParameter("now", date)
				.setParameter("repoKey", entry.getKey())
				.getResultList();
	}
	
	
	protected List<AssessmentMode> loadAssessmentModeFor(IdentityRef identity, List<AssessmentMode> currentModes) {
		StringBuilder sb = new StringBuilder(1500);
		sb.append("select mode from courseassessmentmode mode ")
		  .append(" inner join fetch mode.repositoryEntry entry")
		  .append(" left join mode.groups as modeToGroup")
		  .append(" left join mode.areas as modeToArea")
		  .append(" left join mode.curriculumElements as modeToCurriculumElement")
		  .append(" where mode.key in (:modeKeys)")
		  .append("  and ((mode.targetAudienceString in ('").append(AssessmentMode.Target.courseAndGroups.name()).append("','").append(AssessmentMode.Target.groups.name()).append("')")
		  .append("   and (exists (select businessGroup from businessgroup as businessGroup, bgroupmember as membership")
		  .append("     where modeToGroup.businessGroup.key=businessGroup.key and membership.group.key=businessGroup.baseGroup.key and membership.identity.key=:identityKey")
		  .append("     and (membership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and membership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("   ) or exists (select areaToGroup from ").append(BGtoAreaRelationImpl.class.getName()).append(" as areaToGroup,businessgroup as businessGroupArea, bgroupmember as membership")
		  .append("     where modeToArea.area=areaToGroup.groupArea and areaToGroup.businessGroup=businessGroupArea and membership.group=businessGroupArea.baseGroup and membership.identity.key=:identityKey")
		  .append("     and (membership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and membership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("  ))) or (mode.targetAudienceString in ('").append(AssessmentMode.Target.courseAndGroups.name()).append("','").append(AssessmentMode.Target.course.name()).append("')")
		  .append("   and exists (select rel from repoentrytogroup as rel,  bgroupmember as membership ")
		  .append("     where mode.repositoryEntry.key=rel.entry.key and membership.group.key=rel.group.key and rel.defaultGroup=true and membership.identity.key=:identityKey")
		  .append("     and (membership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and membership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("  )) or (mode.targetAudienceString in ('").append(AssessmentMode.Target.courseAndGroups.name()).append("','").append(AssessmentMode.Target.curriculumEls.name()).append("')")
		  .append("   and exists (select curElement from curriculumelement as curElement,  bgroupmember as curMembership ")
		  .append("     where modeToCurriculumElement.curriculumElement.key=curElement.key and curMembership.group.key=curElement.group.key and curMembership.identity.key=:identityKey")
		  .append("     and (curMembership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and curMembership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("  ))")
		  .append(" )");

		List<Long> modeKeys = new ArrayList<>(currentModes.size());
		for(AssessmentMode mode:currentModes) {
			modeKeys.add(mode.getKey());
		}
		List<AssessmentMode> modeList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("modeKeys", modeKeys)
				.getResultList();
		//quicker than distinct
		return new ArrayList<>(new HashSet<>(modeList));
	}
	
	public boolean isNodeInUse(RepositoryEntryRef entry, CourseNode node) {
		if(entry == null || node == null) return false;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(mode) from courseassessmentmode mode where ")
		  .append(" mode.repositoryEntry.key=:repoKey ")
		  .append(" and (mode.startElement=:startIdent or mode.elementList like :nodeIdent)")
		  .append(" and (mode.beginWithLeadTime>=:now")
		  .append(" or mode.statusString ").in(Status.none, Status.leadtime, Status.assessment, Status.followup)
		  .append(" or mode.endStatusString ").in(EndStatus.withoutDisadvantage).append(")");

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		
		List<Number> count = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("repoKey", entry.getKey())
				.setParameter("startIdent", node.getIdent())
				.setParameter("nodeIdent", "%" + node.getIdent() + "%")
				.setParameter("now", cal.getTime(), TemporalType.TIMESTAMP)
				.getResultList();
		return count != null && !count.isEmpty() && count.get(0).intValue() > 0;
	}
	
	public void delete(AssessmentMode assessmentMode) {
		AssessmentMode refMode = getAssessmentModeById(assessmentMode.getKey());
		if(refMode != null) {
			dbInstance.getCurrentEntityManager().remove(refMode);
		}
	}
	
	/**
	 * Delete all assessment modes of a course.
	 * 
	 * @param entry The course
	 */
	public void delete(RepositoryEntryRef entry) {
		for(AssessmentMode mode: getAssessmentModeFor(entry)) {
			delete(mode);
		}
	}
	
	public void deleteAssessmentModesToGroup(BusinessGroupRef businessGroup) {
		String q = "delete from courseassessmentmodetogroup as modegrrel where modegrrel.businessGroup.key=:groupKey";
		dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("groupKey", businessGroup.getKey())
			.executeUpdate();
	}
	
	/**
	 * Delete the relations between assessment mode and group for the specified business group and course.
	 * @param businessGroup
	 * @param entry
	 */
	public void delete(BusinessGroupRef businessGroup, RepositoryEntryRef entry) {
		String q = "delete from courseassessmentmodetogroup as modegrrel where modegrrel.businessGroup.key=:groupKey and modegrrel.assessmentMode.key in (select amode.key from courseassessmentmode amode where amode.repositoryEntry.key=:repoKey)";
		dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("groupKey", businessGroup.getKey())
			.setParameter("repoKey", entry.getKey())
			.executeUpdate();
	}

}
