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
package org.olat.modules.grading.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingTimeRecord;
import org.olat.modules.grading.model.GradingAssignmentImpl;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters.SearchStatus;
import org.olat.modules.grading.model.GradingAssignmentWithInfos;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GradingAssignmentDAO {
	
	@Autowired
	private DB dbInstance;
	
	public GradingAssignment createGradingAssignment(GraderToIdentity grader, RepositoryEntry referenceEntry,
			AssessmentEntry assessmentEntry, Date assessmentDate, Date deadLine) {
		GradingAssignmentImpl assignment = new GradingAssignmentImpl();
		assignment.setCreationDate(new Date());
		assignment.setLastModified(assignment.getCreationDate());
		assignment.setDeadline(deadLine);
		if(grader == null) {
			assignment.setAssignmentStatus(GradingAssignmentStatus.unassigned);
		} else {
			assignment.setAssignmentStatus(GradingAssignmentStatus.assigned);
			assignment.setAssignmentDate(assignment.getCreationDate());
		}
		assignment.setAssessmentDate(assessmentDate);
		assignment.setReferenceEntry(referenceEntry);
		assignment.setAssessmentEntry(assessmentEntry);
		assignment.setGrader(grader);
		dbInstance.getCurrentEntityManager().persist(assignment);
		return assignment;
	}
	
	public List<GradingAssignment> getGradingAssignments(RepositoryEntryRef referenceEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" left join fetch assignment.grader as grader")
		  .append(" left join fetch grader.identity as graderIdent")
		  .append(" left join fetch graderIdent.user as graderUser")
		  .append(" where assignment.referenceEntry.key=:referenceEntryKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.setParameter("referenceEntryKey", referenceEntry.getKey())
				.getResultList();
	}
	
	public List<GradingAssignment> getGradingAssignments(GraderToIdentity grader) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" where assignment.grader.key=:graderKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.setParameter("graderKey", grader.getKey())
				.getResultList();
	}
	
	public List<GradingAssignment> getGradingAssignments(GraderToIdentity grader, GradingAssignmentStatus... assignmentStatus) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" where assignment.grader.key=:graderKey and assignment.status ").in(assignmentStatus);
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.setParameter("graderKey", grader.getKey())
				.getResultList();
	}
	
	public List<GradingAssignment> getGradingAssignments(IdentityRef grader) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" left join fetch assignment.grader as grader")
		  .append(" left join fetch grader.identity as graderIdent")
		  .append(" left join fetch graderIdent.user as graderUser")
		  .append(" where graderIdent.key=:graderKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.setParameter("graderKey", grader.getKey())
				.getResultList();
	}
	
	/**
	 * @param key The assignment primary key
	 * @return The assignment with the grader's identity fetched
	 */
	public GradingAssignment loadByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" left join fetch assignment.grader as grader")
		  .append(" left join fetch grader.identity as graderIdent")
		  .append(" left join fetch graderIdent.user as graderUser")
		  .append(" where assignment.key=:assignmentKey");
		
		List<GradingAssignment> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.setParameter("assignmentKey", key)
				.getResultList();
		return assignments == null || assignments.isEmpty() ? null : assignments.get(0);
	}
	
	/**
	 * @param key The assignment primary key
	 * @return The assignment with the grader's identity fetched, reference entry,
	 * 		assessment entry, repository entry in assessment entry
	 */
	public GradingAssignment loadFullByKey(Long key) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" left join fetch assignment.grader as grader")
		  .append(" left join fetch grader.identity as graderIdent")
		  .append(" left join fetch graderIdent.user as graderUser")
		  .append(" left join fetch assignment.referenceEntry as refEntry")
		  .append(" left join fetch refEntry.olatResource as refEntryResource")
		  .append(" left join fetch assignment.assessmentEntry as assessmentEntry")
		  .append(" left join fetch assessmentEntry.repositoryEntry as assessmentRe")
		  .append(" left join fetch assessmentRe.olatResource as assessmentReResource")
		  .append(" where assignment.key=:assignmentKey");
		
		List<GradingAssignment> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.setParameter("assignmentKey", key)
				.getResultList();
		return assignments == null || assignments.isEmpty() ? null : assignments.get(0);
	}
	
	
	public List<RepositoryEntry> getEntries(RepositoryEntryRef referenceEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct assessmentRe from gradingassignment as assignment")
		  .append(" inner join assignment.assessmentEntry as assessmentEntry")
		  .append(" inner join assessmentEntry.repositoryEntry as assessmentRe")
		  .append(" inner join fetch assessmentRe.olatResource as assessmentReResource")
		  .append(" where assignment.referenceEntry.key=:referenceKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("referenceKey", referenceEntry.getKey())
				.getResultList();
	}
	
	public List<RepositoryEntry> getEntries(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct assessmentRe from gradingassignment as assignment")
		  .append(" inner join assignment.assessmentEntry as assessmentEntry")
		  .append(" inner join assessmentEntry.repositoryEntry as assessmentRe")
		  .append(" inner join assignment.referenceEntry as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and membership.role ").in(GroupRoles.owner,
				OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator, OrganisationRoles.principal);
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<GradingAssignmentWithInfos> findGradingAssignments(GradingAssignmentSearchParameters searchParams) {
		RepositoryEntry referenceEntry = searchParams.getReferenceEntry();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment, record from gradingassignment as assignment")
		  .append(" inner join fetch assignment.assessmentEntry as assessmentEntry")
		  .append(" inner join fetch assessmentEntry.repositoryEntry as assessmentRe")
		  .append(" inner join fetch assessmentRe.olatResource as assessmentReResource")
		  .append(" left join gradingtimerecord as record on (record.grader.key=assignment.grader.key and assignment.key=record.assignment.key)")
		  .append(" left join fetch assignment.grader as grader")
		  .append(" left join fetch grader.identity as graderIdent")
		  .append(" left join fetch graderIdent.user as graderUser");
		if(referenceEntry == null) {
			sb.append(" inner join fetch assignment.referenceEntry as refEntry")
			  .append(" inner join fetch refEntry.olatResource as refResource");
		}
		applyGradingAssignmentSearchParameters(sb, searchParams);
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		
		applyGradingAssignmentSearchParameters(query, searchParams);
		List<Object[]> rawObjects = query.getResultList();
		
		Map<Long,GradingAssignmentWithInfos> infosMap = new HashMap<>();
		for(Object[] rawObject:rawObjects) {
			final GradingAssignment assignment = (GradingAssignment)rawObject[0];
			final GradingTimeRecord record = (GradingTimeRecord)rawObject[1];
			final RepositoryEntry reference = referenceEntry == null ? assignment.getReferenceEntry() : referenceEntry;
			infosMap
				.computeIfAbsent(assignment.getKey(), key -> new GradingAssignmentWithInfos(assignment, reference))
				.addTimeRecord(record);
		}
		return new ArrayList<>(infosMap.values());
	}
	
	private void applyGradingAssignmentSearchParameters(QueryBuilder sb, GradingAssignmentSearchParameters searchParams) {
		if(searchParams.getManager() != null) {
			sb.and()
			  .append("exists (select membership.key from repoentrytogroup as relGroup")
			  .append(" inner join relGroup.group as baseGroup")
			  .append(" inner join baseGroup.members as membership")
			  .append(" where relGroup.entry.key=assignment.referenceEntry.key and membership.identity.key=:managerKey")
			  .append("  and membership.role ").in(GroupRoles.owner, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator, OrganisationRoles.principal)
			  .append(")");
		}
		
		if(searchParams.getGrader() != null) {
			sb.and().append("graderIdent.key=:graderKey");
		}
		if(searchParams.getReferenceEntry() != null) {
			sb.and().append("assignment.referenceEntry.key=:referenceEntryKey");
		}
		if(searchParams.getPassed() != null) {
			sb.and().append("assessmentEntry.passed=:passed");
		}
		
		if(searchParams.getClosedFromDate() != null && searchParams.getClosedToDate() != null) {
			sb.and().append("assignment.closingDate>=:closedFromDate and assignment.closingDate<=:closedToDate");
		} else if(searchParams.getClosedFromDate() != null) {
			sb.and().append("assignment.closingDate>=:closedFromDate");
		} else if(searchParams.getClosedToDate() != null) {
			sb.and().append("assignment.closingDate<=:closedToDate");
		}

		applyAssignmentSearchParameters(sb, searchParams.getGradingFromDate(), searchParams.getGradingToDate());
		
		if(searchParams.getScoreFrom() != null) {
			sb.and().append("assessmentEntry.score>=:scoreFrom");
		}
		if(searchParams.getScoreTo() != null) {
			sb.and().append("assessmentEntry.score<=:scoreTo");
		}
		if(searchParams.getTaxonomyLevels() != null && !searchParams.getTaxonomyLevels().isEmpty()) {
			sb.and()
			  .append("exists (select levelRelation from repositoryentrytotaxonomylevel levelRelation")
			  .append("  where levelRelation.entry.key=assignment.referenceEntry.key and levelRelation.taxonomyLevel.key in (:taxonomyLevelKeys)")
			  .append(")");
		}
		if(searchParams.getEntry() != null) {
			sb.and().append("assessmentRe.key=:entryKey");
		}
		
		if(searchParams.getAssignmentStatus() != null && !searchParams.getAssignmentStatus().isEmpty()) {
			sb.and().append("(");
			boolean or = false;
			for(SearchStatus status:searchParams.getAssignmentStatus()) {
				if(or) {
					sb.append(" or ");
				} else {
					or = true;
				}
				
				if(status == SearchStatus.unassigned) {
					sb.append("assignment.status ").in(GradingAssignmentStatus.unassigned);
				} else if(status == SearchStatus.closed) {
					sb.append("assignment.status ").in(GradingAssignmentStatus.done);
				} else if(status == SearchStatus.open) {
					sb.append("assignment.status ").in(GradingAssignmentStatus.assigned, GradingAssignmentStatus.inProcess);
				} else if(status == SearchStatus.reminder1) {
					sb.append("assignment.reminder1Date is not null");
				} else if(status == SearchStatus.reminder2) {
					sb.append("assignment.reminder2Date is not null");
				} else if(status == SearchStatus.deadlineMissed) {
					sb.append("assignment.deadline < current_timestamp");
				}
			}
			sb.append(")");
			
			if(!searchParams.getAssignmentStatus().contains(SearchStatus.closed)) {
				sb.and().append(" not(assignment.status ").in(GradingAssignmentStatus.done).append(")");
			}
		} else {
			sb.and().append("assignment.status ").in(GradingAssignmentStatus.unassigned, GradingAssignmentStatus.assigned,
					GradingAssignmentStatus.inProcess, GradingAssignmentStatus.done);
		}
	}
	
	protected static void applyAssignmentSearchParameters(QueryBuilder sb, Date from, Date to) {
		if(from != null && to != null) {
			sb.and()
			  .append("(")
			  .append(" (assignment.assignmentDate>=:gradingFromDate and assignment.assignmentDate<=:gradingToDate)")
			  .append(" or")
			  .append(" (assignment.closingDate>=:gradingFromDate and assignment.closingDate<=:gradingToDate)")
			  .append(" or")
			  .append(" (assignment.assignmentDate>=:gradingFromDate and (assignment.closingDate is null or assignment.closingDate<=:gradingToDate))")
			  .append(")");	
		} else if(from != null) {
			sb.and().append("assignment.assignmentDate>=:gradingFromDate");
		} else if(to != null) {
			sb.and().append("(assignment.assignmentDate<=:gradingToDate or assignment.closingDate<=:gradingToDate) ");
		}
	}
	
	private void applyGradingAssignmentSearchParameters(TypedQuery<?> query, GradingAssignmentSearchParameters searchParams) {
		if(searchParams.getReferenceEntry() != null) {
			query.setParameter("referenceEntryKey", searchParams.getReferenceEntry().getKey());
		}
		if(searchParams.getGrader() != null) {
			query.setParameter("graderKey", searchParams.getGrader().getKey());
		}
		if(searchParams.getPassed() != null) {
			query.setParameter("passed", searchParams.getPassed());
		}
		if(searchParams.getGradingFromDate() != null) {
			query.setParameter("gradingFromDate", searchParams.getGradingFromDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getGradingToDate() != null) {
			query.setParameter("gradingToDate", searchParams.getGradingToDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getClosedFromDate() != null) {
			query.setParameter("closedFromDate", searchParams.getClosedFromDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getClosedToDate() != null) {
			query.setParameter("closedToDate", searchParams.getClosedToDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getScoreFrom() != null) {
			query.setParameter("scoreFrom", searchParams.getScoreFrom());
		}
		if(searchParams.getScoreTo() != null) {
			query.setParameter("scoreTo", searchParams.getScoreTo());
		}
		if(searchParams.getTaxonomyLevels() != null && !searchParams.getTaxonomyLevels().isEmpty()) {
			List<Long> levelKeys = searchParams.getTaxonomyLevels().stream()
					.map(TaxonomyLevel::getKey)
					.collect(Collectors.toList());
			query.setParameter("taxonomyLevelKeys", levelKeys);
		}
		if(searchParams.getEntry() != null) {
			query.setParameter("entryKey", searchParams.getEntry().getKey());
		}
		if(searchParams.getManager() != null) {
			query.setParameter("managerKey", searchParams.getManager().getKey());
		}
	}
	
	public boolean hasGradingAssignment(RepositoryEntryRef referenceEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment.key from gradingassignment as assignment")
		  .append(" where assignment.referenceEntry.key=:referenceEntryKey");
		
		List<Long> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("referenceEntryKey", referenceEntry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return !assignments.isEmpty() && assignments.get(0) != null && assignments.get(0).longValue() > 0;
	}
	
	public boolean hasGradingAssignment(RepositoryEntryRef referenceEntry, AssessmentEntry assessmentEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment.key from gradingassignment as assignment")
		  .append(" where assignment.referenceEntry.key=:referenceEntryKey")
		  .append(" and assignment.assessmentEntry.key=:assessmentEntryKey");
		
		List<Long> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("referenceEntryKey", referenceEntry.getKey())
				.setParameter("assessmentEntryKey", assessmentEntry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return !assignments.isEmpty() && assignments.get(0) != null && assignments.get(0).longValue() > 0;
	}
	
	public GradingAssignment getGradingAssignment(RepositoryEntryRef referenceEntry, AssessmentEntry assessmentEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" where assignment.referenceEntry.key=:referenceEntryKey")
		  .append(" and assignment.assessmentEntry.key=:assessmentEntryKey");
		
		List<GradingAssignment> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.setParameter("referenceEntryKey", referenceEntry.getKey())
				.setParameter("assessmentEntryKey", assessmentEntry.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return assignments.isEmpty() ? null : assignments.get(0);
	}
	
	/**
	 * This is not an exact method! check the configuration before send remidners.
	 * 
	 * @return A list of grading assignments where the status is assigned or
	 *   in process, the assignment was done and the configuration defined some
	 *   reminders date.
	 */
	public List<GradingAssignment> getGradingAssignmentsOpenWithPotentialToRemind() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment as assignment")
		  .append(" inner join assignment.referenceEntry referenceEntry")
		  .append(" inner join gradingconfiguration as config on (config.entry.key=referenceEntry.key)")
		  .append(" where config.gradingEnabled=true and (config.firstReminder is not null or config.secondReminder is not null)")
		  .append(" and assignment.status ").in(GradingAssignmentStatus.assigned, GradingAssignmentStatus.inProcess)
		  .append(" and assignment.assignmentDate is not null")
		  .append(" and (")
		  .append("   (assignment.reminder1Date is null and assignment.assignmentDate <= current_date)")
		  .append("   or")
		  .append("   (assignment.reminder2Date is null and assignment.assignmentDate <= current_date)")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.getResultList();
	}
	
	public List<GradingAssignment> getAssignmentsForGradersNotify(IdentityRef grader) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select assignment from gradingassignment assignment")
		  .append(" inner join fetch assignment.referenceEntry referenceEntry")
		  .append(" inner join assignment.grader as grader")
		  .append(" inner join grader.identity as ident")
		  .append(" where assignment.status<>'").append(GradingAssignmentStatus.done).append("'")
		  .append(" and (assignment.assignmentNotificationDate is null or (")
		  .append("  (assignment.extendedDeadline is null and assignment.deadline < current_date) or assignment.extendedDeadline < current_date")
		  .append(" ))")
		  .append(" and ident.key=:graderKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignment.class)
				.setParameter("graderKey", grader.getKey())
				.getResultList();
	}
	
	/**
	 * @return A list of identities with new assignment or overdue assignments
	 */
	public List<Identity> getGradersIdentityToNotify() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select distinct ident from gradingassignment assignment")
		  .append(" inner join assignment.grader as grader")
		  .append(" inner join grader.identity as ident")
		  .append(" inner join ident.user as identUser")
		  .append(" where assignment.status<>'").append(GradingAssignmentStatus.done).append("'")
		  .append(" and (assignment.assignmentNotificationDate is null or (")
		  .append("  (assignment.extendedDeadline is null and assignment.deadline < current_date) or assignment.extendedDeadline < current_date")
		  .append(" ))");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.getResultList();
	}
	
	public void removeDeadline(RepositoryEntryRef referenceEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("update gradingassignment assignment set assignment.deadline=null where assignment.referenceEntry.key=:referenceKey");
		dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString())
			.setParameter("referenceKey", referenceEntry.getKey())
			.executeUpdate();
	}
	
	public GradingAssignment updateAssignment(GradingAssignment assignment) {
		((GradingAssignmentImpl)assignment).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(assignment);
	}

}
