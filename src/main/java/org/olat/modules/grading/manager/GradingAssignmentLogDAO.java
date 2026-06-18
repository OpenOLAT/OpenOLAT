/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.grading.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentLog;
import org.olat.modules.grading.model.GradingAssignmentLogImpl;
import org.olat.modules.grading.model.GradingAssignmentLogSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class GradingAssignmentLogDAO {
	
	@Autowired
	private DB dbInstance;
	
	public GradingAssignmentLog createLog(GradingAssignment assignment,
			Identity grader, Identity assignee, long time, long metadataTime,
			RepositoryEntry referenceEntry, RepositoryEntry entry) {
		GradingAssignmentLogImpl assignmentLog = new GradingAssignmentLogImpl();
		assignmentLog.setCreationDate(new Date());
		assignmentLog.setLastModified(assignmentLog.getCreationDate());
		assignmentLog.setDeleted(false);
		assignmentLog.setGradingAssignmentKey(assignment.getKey());
		assignmentLog.setGrader(grader);
		assignmentLog.setAssignee(assignee);
		assignmentLog.setReferenceEntryKey(referenceEntry.getKey());
		assignmentLog.setReferenceEntryDisplayName(referenceEntry.getDisplayname());
		assignmentLog.setReferenceEntryExternalRef(referenceEntry.getExternalRef());
		if(entry != null) {
			assignmentLog.setRepositoryEntryKey(entry.getKey());
			assignmentLog.setRepositoryEntryDisplayName(entry.getDisplayname());
			assignmentLog.setRepositoryEntryExternalRef(entry.getExternalRef());
		}
		assignmentLog.setStatus(assignment.getAssignmentStatus());
		assignmentLog.setClosingDate(assignment.getClosingDate());
		assignmentLog.setTime(time);
		assignmentLog.setMetadataTime(metadataTime);
		dbInstance.getCurrentEntityManager().persist(assignmentLog);
		return assignmentLog;
	}
	
	public GradingAssignmentLog update(GradingAssignmentLog assessmentLog) {
		assessmentLog.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(assessmentLog);
	}
	
	public int markAsDeleted(GradingAssignment assignment) {
		String query = "update gradingassignmentlog as alog set alog.deleted=true where alog.gradingAssignmentKey=:assignmentKey";
		return dbInstance.getCurrentEntityManager().createQuery(query)
			.setParameter("assignmentKey", assignment.getKey())
			.executeUpdate();
	}
	
	public boolean hasGradingAssignmentLog(IdentityRef grader) {
		String query = """
				select alog.key from gradingassignmentlog as alog
				where alog.grader.key=:graderKey""";
		
		List<Long> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("graderKey", grader.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return assignments != null && !assignments.isEmpty()
				&& assignments.get(0) != null && assignments.get(0).longValue() > 0;
	}
	
	public GradingAssignmentLog loadLastGradingAssignment(GradingAssignment assignment) {
		String query = """
				select alog from gradingassignmentlog as alog
				where alog.gradingAssignmentKey=:assignmentKey and alog.deleted=false
				order by alog.creationDate desc""";
		
		List<GradingAssignmentLog> assignments = dbInstance.getCurrentEntityManager()
				.createQuery(query, GradingAssignmentLog.class)
				.setParameter("assignmentKey", assignment.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return assignments == null || assignments.isEmpty()
				? null
				: assignments.get(0);
	}
	
	public List<GradingAssignmentLog> getGradingAssignmentsLogs(GradingAssignmentLogSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select alog from gradingassignmentlog as alog")
		  .append(" left join fetch alog.grader as graderIdent")
		  .append(" left join fetch graderIdent.user as graderUser")
		  .append(" left join fetch alog.assignee as assigneeIdent")
		  .append(" left join fetch assigneeIdent.user as assigneeUser");

		if(searchParams.getGrader() != null) {
			sb.and().append(" graderIdent.key=:graderKey");
		}
		if(searchParams.getReferenceEntry() != null) {
			sb.and().append(" alog.referenceEntryKey=:referenceEntryKey");
		}
		if(searchParams.getEntry() != null) {
			sb.and().append(" alog.repositoryEntryKey=:repositoryEntryKey");
		}
		if(searchParams.getClosedFromDate() != null && searchParams.getClosedToDate() != null) {
			sb.and()
			  .append("((alog.closingDate>=:closedFromDate and alog.closingDate<=:closedToDate)")
			  .append(" or ")
			  .append("(alog.closingDate is null and alog.creationDate>=:closedFromDate and alog.creationDate<=:closedToDate))");
		} else if(searchParams.getClosedFromDate() != null) {
			sb.and()
			  .append("(alog.closingDate>=:closedFromDate")
			  .append(" or ")
			  .append("(alog.closingDate is null and alog.creationDate>=:closedFromDate))");
		} else if(searchParams.getClosedToDate() != null) {
			sb.and()
			  .append("(alog.closingDate<=:closedToDate")
			  .append(" or ")
			  .append("(alog.closingDate is null and alog.creationDate<=:closedToDate))");
		}
		
		TypedQuery<GradingAssignmentLog> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GradingAssignmentLog.class);
		if(searchParams.getGrader() != null) {
			query.setParameter("graderKey", searchParams.getGrader().getKey());
		}
		if(searchParams.getReferenceEntry() != null) {
			query.setParameter("referenceEntryKey", searchParams.getReferenceEntry().getKey());
		}
		if(searchParams.getEntry() != null) {
			query.setParameter("repositoryEntryKey", searchParams.getEntry().getKey());
		}
		if(searchParams.getClosedFromDate() != null) {
			query.setParameter("closedFromDate", searchParams.getClosedFromDate(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getClosedToDate() != null) {
			query.setParameter("closedToDate", searchParams.getClosedToDate(), TemporalType.TIMESTAMP);
		}
		return query.getResultList();
	}
}
