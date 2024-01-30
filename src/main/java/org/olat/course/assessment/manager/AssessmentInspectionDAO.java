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
package org.olat.course.assessment.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.model.AssessmentEntryInspection;
import org.olat.course.assessment.model.AssessmentInspectionImpl;
import org.olat.course.assessment.ui.inspection.SearchAssessmentInspectionParameters;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentInspectionDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentInspection createInspection(Identity identity, Date from, Date to, Integer extraTime,
			String accessCode, String subIdent, AssessmentInspectionConfiguration configuration) {
		AssessmentInspectionImpl rel = new AssessmentInspectionImpl();
		rel.setCreationDate(new Date());
		rel.setLastModified(rel.getCreationDate());
		rel.setFromDate(from);
		rel.setToDate(to);
		rel.setExtraTime(extraTime);
		rel.setAccessCode(accessCode);
		rel.setSubIdent(subIdent);
		rel.setIdentity(identity);
		rel.setInspectionStatus(AssessmentInspectionStatusEnum.scheduled);
		rel.setConfiguration(configuration);
		dbInstance.getCurrentEntityManager().persist(rel);
		return rel;
	}
	
	public AssessmentInspection updateInspection(AssessmentInspection inspection) {
		inspection.setLastModified(new Date());
		inspection = dbInstance.getCurrentEntityManager().merge(inspection);
		return inspection;
	}
	
	public List<AssessmentInspection> searchInspection(RepositoryEntry entry, String subIdent) {
		String query = """
				select inspection from courseassessmentinspection as inspection
				inner join fetch inspection.identity as ident
				inner join fetch ident.user as identUser
				inner join inspection.configuration as configuration
				where configuration.repositoryEntry.key=:entryKey and inspection.subIdent=:subIdent
				""";
		return dbInstance.getCurrentEntityManager().createQuery(query, AssessmentInspection.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	public List<AssessmentInspection> searchInspectionFor(IdentityRef identity, Date date) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select inspection from courseassessmentinspection as inspection")
		  .append(" inner join fetch inspection.configuration as configuration")
		  // make sure an inspection is linked to test session
		  .append(" inner join qtiassessmenttestsession testSession on (testSession.identity.key=inspection.identity.key and configuration.repositoryEntry.key=testSession.repositoryEntry.key and testSession.subIdent=inspection.subIdent)")
		  .and().append("inspection.identity.key=:identityKey")
		  .and().append("inspection.inspectionStatus").in(AssessmentInspectionStatusEnum.scheduled, AssessmentInspectionStatusEnum.inProgress)
		  .and().append("inspection.fromDate<=:date and inspection.toDate>=:date")
		  // make sure the test session is finished and valid
		  .and().append("(testSession.terminationTime is not null or testSession.finishTime is not null) and testSession.exploded=false and testSession.cancelled=false");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentInspection.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("date", date, TemporalType.TIMESTAMP)
				.getResultList();
	}
	
	public AssessmentInspection searchInspectionFor(IdentityRef identity, Date date, Long inspectionKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select inspection from courseassessmentinspection as inspection")
		  .append(" inner join fetch inspection.configuration as configuration")
		  // make sure an inspection is linked to test session
		  .append(" inner join qtiassessmenttestsession testSession on (testSession.identity.key=inspection.identity.key and configuration.repositoryEntry.key=testSession.repositoryEntry.key and testSession.subIdent=inspection.subIdent)")
		  
		  .and().append("inspection.identity.key=:identityKey and inspection.key=:inspectionKey")
		  .and().append("inspection.inspectionStatus").in(AssessmentInspectionStatusEnum.scheduled, AssessmentInspectionStatusEnum.inProgress)
		  .and().append("inspection.fromDate<=:date and inspection.toDate>=:date")
		  // make sure the test session is finished and valid
		  .and().append("(testSession.terminationTime is not null or testSession.finishTime is not null) and testSession.exploded=false and testSession.cancelled=false");
		
		List<AssessmentInspection> inspectionList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentInspection.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("inspectionKey", inspectionKey)
				.setParameter("date", date, TemporalType.TIMESTAMP)
				.getResultList();
		return inspectionList == null || inspectionList.isEmpty() ? null : inspectionList.get(0);
	}
	
	public boolean hasAssessmentTestSession(IdentityRef identity, RepositoryEntryRef courseEntry, String subIdent) {
		String query = """
			select testSession.key from qtiassessmenttestsession as testSession
			where testSession.identity.key=:identityKey and testSession.repositoryEntry.key=:repositoryEntryKey and testSession.subIdent=:subIdent
			and (testSession.terminationTime is not null or testSession.finishTime is not null)
			and testSession.exploded=false and testSession.cancelled=false and testSession.authorMode=false""";
		
		List<Long> inspectionList = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("repositoryEntryKey", courseEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
		return inspectionList != null && !inspectionList.isEmpty()
				&& inspectionList.get(0) != null && inspectionList.get(0).longValue() > 0;
	}
	
	public AssessmentInspection loadByKey(Long inspectionKey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select inspection from courseassessmentinspection as inspection")
		  .append(" inner join fetch inspection.configuration as configuration")
		  .append(" inner join fetch inspection.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .and().append("inspection.key=:inspectionKey");
		List<AssessmentInspection> inspectionList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentInspection.class)
				.setParameter("inspectionKey", inspectionKey)
				.getResultList();
		return inspectionList == null || inspectionList.isEmpty() ? null : inspectionList.get(0);
	}
	
	public int hasInspection(AssessmentInspectionConfiguration configuration) {
		String query = """
				select count(inspection.key) from courseassessmentinspection as inspection
				where inspection.configuration.key=:configurationKey""";
		
		List<Long> inspectionList = dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("configurationKey", configuration.getKey())
				.getResultList();
		return (inspectionList != null && !inspectionList.isEmpty()
				&& inspectionList.get(0) != null) ? inspectionList.get(0).intValue() : 0;
	}
	
	public List<AssessmentInspection> searchNoShowInspections(Date date) {
		String query = """
				select inspection from courseassessmentinspection as inspection
				inner join fetch inspection.configuration as configuration
				inner join fetch configuration.repositoryEntry as entry
				inner join fetch inspection.identity as ident
				where inspection.toDate<:date and inspection.inspectionStatus=:status
				""";
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, AssessmentInspection.class)
			.setParameter("date", date, TemporalType.TIMESTAMP)
			.setParameter("status", AssessmentInspectionStatusEnum.scheduled)
			.getResultList();
	}
	
	public List<AssessmentInspection> searchInspectionsToStart(Date date) {
		String query = """
				select inspection from courseassessmentinspection as inspection
				inner join fetch inspection.configuration as configuration
				inner join fetch configuration.repositoryEntry as entry
				inner join fetch inspection.identity as ident
				inner join qtiassessmenttestsession testSession on (testSession.identity.key=inspection.identity.key and configuration.repositoryEntry.key=testSession.repositoryEntry.key and testSession.subIdent=inspection.subIdent)
				where inspection.fromDate<=:date and inspection.toDate>:date and inspection.inspectionStatus=:status
				and (testSession.terminationTime is not null or testSession.finishTime is not null) and testSession.exploded=false and testSession.cancelled=false
				""";
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, AssessmentInspection.class)
			.setParameter("date", date, TemporalType.TIMESTAMP)
			.setParameter("status", AssessmentInspectionStatusEnum.scheduled)
			.getResultList();
	}
	
	public List<AssessmentInspection> searchInProgressInspectionsToClose(Date date) {
		String query = """
				select inspection from courseassessmentinspection as inspection
				inner join fetch inspection.configuration as configuration
				inner join fetch inspection.identity as ident
				where inspection.endTime<:date and inspection.inspectionStatus=:status
				""";
		return dbInstance.getCurrentEntityManager()
			.createQuery(query, AssessmentInspection.class)
			.setParameter("date", date, TemporalType.TIMESTAMP)
			.setParameter("status", AssessmentInspectionStatusEnum.inProgress)
			.getResultList();
	}
	
	public List<AssessmentEntryInspection> searchInspection(SearchAssessmentInspectionParameters params) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select inspection, entry from courseassessmentinspection as inspection")
		  .append(" inner join fetch inspection.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join fetch inspection.configuration as configuration")
		  .append(" left join assessmententry entry on (entry.identity.key=inspection.identity.key and entry.subIdent=inspection.subIdent and entry.repositoryEntry.key=configuration.repositoryEntry.key)");
		if(params.getEntry() != null) {
			sb.and().append("configuration.repositoryEntry.key=:entryKey");
		}
		if(params.hasSubIdents()) {
			sb.and().append("inspection.subIdent in :subIdents");
		}
		
		List<AssessmentInspectionStatusEnum> inspectionStatus = null;
		if(params.hasInspectionStatus()) {
			if(params.getInspectionStatus().size() == 1 && params.getInspectionStatus().contains(AssessmentInspectionStatusEnum.scheduled) && params.getActiveInspections() != null) {
				sb.and().append(" (");
				queryActiveInspection(sb, params.getActiveInspections());
				sb.append(")");
			} else {
				inspectionStatus = params.getInspectionStatus();
				sb.and().append("( inspection.inspectionStatus in :inspectionStatus");
				if(params.getActiveInspections() != null) {
					sb.append(" or ");
					queryActiveInspection(sb, params.getActiveInspections());
				}
				sb.append(")");
			}
		} else if(params.getActiveInspections() != null) {
			sb.and();
			queryActiveInspection(sb, params.getActiveInspections());
		}
		
		if(params.hasAssessmentStatus()) {
			sb.and().append("entry.status in :assessmentStatus");
		}
		if(params.hasConfigurationsKeys()) {
			sb.and().append("configuration.key in :configurationsKeys");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(params.getEntry() != null) {
			query.setParameter("entryKey", params.getEntry().getKey());
		}
		if(params.hasSubIdents()) {
			query.setParameter("subIdents", params.getSubIdents());
		}
		if(inspectionStatus != null && !inspectionStatus.isEmpty()) {
			List<String> statusList = params.getInspectionStatus().stream()
					.map(AssessmentInspectionStatusEnum::toString)
					.toList();
			query.setParameter("inspectionStatus", statusList);	
		}
		if(params.getActiveInspections() != null) {
			query.setParameter("now", new Date(), TemporalType.TIMESTAMP);	
		}
		if(params.hasAssessmentStatus()) {
			List<String> statusList = params.getAssessmentStatus().stream()
					.map(AssessmentEntryStatus::toString)
					.toList();
			query.setParameter("assessmentStatus", statusList);	
		}
		if(params.hasConfigurationsKeys()) {
			query.setParameter("configurationsKeys", params.getConfigurationsKeys());	
		}
		
		List<Object[]> rawObjects = query.getResultList();
		return rawObjects.stream().map(objects -> {
			AssessmentInspection inspection = (AssessmentInspection)objects[0];
			AssessmentEntry assessmentEntry = (AssessmentEntry)objects[1];
			return new AssessmentEntryInspection(inspection, assessmentEntry);
		}).toList();
	}
	
	private void queryActiveInspection(QueryBuilder sb, Boolean activeInspections) {
		sb.append("(");
		if(activeInspections.booleanValue()) {
			sb.append("inspection.fromDate<=:now and inspection.toDate>=:now");
		} else {
			sb.append("(inspection.fromDate>=:now or inspection.toDate<=:now)");
		}
		sb.append(" and inspection.inspectionStatus").in(AssessmentInspectionStatusEnum.scheduled).append(")");
	}
}
