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
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.FlushModeType;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.model.GraderStatistics;
import org.olat.modules.grading.model.GraderToIdentityImpl;
import org.olat.modules.grading.model.GradersSearchParameters;
import org.olat.modules.grading.model.ReferenceEntryWithStatistics;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GraderToIdentityDAO {
	
	@Autowired
	private DB dbInstance;
	
	public GraderToIdentity createRelation(RepositoryEntry entry, Identity grader) {
		GraderToIdentityImpl config = new GraderToIdentityImpl();
		config.setCreationDate(new Date());
		config.setLastModified(config.getCreationDate());
		config.setGraderStatus(GraderStatus.activated);
		config.setIdentity(grader);
		config.setEntry(entry);
		dbInstance.getCurrentEntityManager().persist(config);
		return config;
	}
	
	public boolean isGrader(IdentityRef identity) {
		List<Long> results = dbInstance.getCurrentEntityManager()
			.createNamedQuery("isGrader", Long.class)
			.setFlushMode(FlushModeType.COMMIT)
			.setParameter("identityKey", identity.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return results != null && !results.isEmpty() && results.get(0) != null && results.get(0).longValue() > 0l;
	}
	
	public boolean isGradingManager(IdentityRef identity, String resourceName) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v.key from repositoryentry as v")
		  .append(" inner join v.groups as relGroup on relGroup.defaultGroup=true")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join gradingconfiguration as config on (v.key=config.entry.key)")
		  .append(" where membership.identity.key=:identityKey and membership.role ").in(GroupRoles.owner)
		  .append("  and config.gradingEnabled=true")
		  .append("  and exists (select oresname.key from ").append(OLATResourceImpl.class.getName()).append(" as oresname")
		  .append("    where oresname.key=v.olatResource.key and oresname.resName=:resourceName")
		  .append(" )");

		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resourceName", resourceName)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	public List<RepositoryEntry> getReferenceRepositoryEntries(IdentityRef identity, String resourceName) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v from repositoryentry as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join gradingconfiguration as config on (v.key=config.entry.key)")
		  .append(" where membership.identity.key=:identityKey and membership.role ").in(GroupRoles.owner,
				  OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator, OrganisationRoles.principal)
		  .append("  and v.status ").in(RepositoryEntryStatusEnum.preparationToPublished())
		  .append("  and config.gradingEnabled=true")
		  .append("  and exists (select oresname.key from ").append(OLATResourceImpl.class.getName()).append(" as oresname")
		  .append("    where oresname.key=v.olatResource.key and oresname.resName=:resourceName")
		  .append(" )");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resourceName", resourceName)
				.getResultList();
	}
	
	public List<Identity> getGraders(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ident from grader2identity as rel")
		  .append(" inner join rel.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" inner join rel.entry as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and membership.role ").in(GroupRoles.owner,
				  OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator, OrganisationRoles.principal );

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<RepositoryEntry> getReferenceRepositoryEntriesAsGrader(IdentityRef grader) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select v from grader2identity as rel")
		  .append(" inner join rel.entry as v")
		  .append(" inner join v.olatResource as vResource")
		  .append(" where rel.identity.key=:identityKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntry.class)
				.setParameter("identityKey", grader.getKey())
				.getResultList();
	}
	
	public List<GraderToIdentity> getGraderRelations(IdentityRef grader) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from grader2identity as rel")
		  .append(" where rel.identity.key=:graderKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), GraderToIdentity.class)
				.setParameter("graderKey", grader.getKey())
				.getResultList();
	}
	
	public boolean isGraderOf(RepositoryEntryRef entry, IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel.key from grader2identity as rel")
		  .append(" where rel.identity.key=:identityKey and rel.entry.key=:entryKey");
		
		List<Long> results = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("identityKey", identity.getKey())
			.setParameter("entryKey", entry.getKey())
			.setFirstResult(0)
			.setMaxResults(1)
			.getResultList();
		return results != null && !results.isEmpty() && results.get(0) != null && results.get(0).longValue() > 0l;
	}
	
	public GraderToIdentity getGrader(RepositoryEntryRef entry, IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from grader2identity as rel")
		  .append(" inner join fetch rel.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where rel.entry.key=:entryKey and rel.identity.key=:identityKey");
		
		List<GraderToIdentity> graders = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), GraderToIdentity.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		return graders.isEmpty() ? null : graders.get(0);
	}
	
	public List<GraderToIdentity> getGraders(RepositoryEntryRef entry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from grader2identity as rel")
		  .append(" inner join fetch rel.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where rel.entry.key=:entryKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), GraderToIdentity.class)
			.setParameter("entryKey", entry.getKey())
			.getResultList();
	}
	
	public List<GraderToIdentity> findGraders(GradersSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel from grader2identity as rel")
		  .append(" inner join fetch rel.identity as ident")
		  .append(" inner join fetch ident.user as identUser");
		if(searchParams.getReferenceEntry() == null) {
			sb.append(" inner join fetch rel.entry as refEntry")
			  .append(" inner join fetch refEntry.olatResource as refResource");
		}
		
		if(searchParams.getManager() != null) {
			sb.and()
			  .append("exists (select membership.key from repoentrytogroup as relGroup")
			  .append(" inner join relGroup.group as baseGroup")
			  .append(" inner join baseGroup.members as membership")
			  .append(" where relGroup.entry.key=rel.entry.key and membership.identity.key=:managerKey")
			  .append("  and membership.role ").in(GroupRoles.owner, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator, OrganisationRoles.principal)
			  .append(")");
		}
		
		if(searchParams.getReferenceEntry() != null) {
			sb.and().append("rel.entry.key=:entryKey");
		}
		if(searchParams.getGrader() != null) {
			sb.and().append("rel.identity.key=:graderKey");
		}
		if(searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("rel.status in (:statusList)");
		}
		if(searchParams.getGradingFrom() != null || searchParams.getGradingTo() != null) {
			sb.and()
			  .append(" exists (select assignment from gradingassignment as assignment")
			  .append("  where assignment.grader.key=rel.key");
			if(searchParams.getGradingFrom() != null) {
				sb.and().append("assignment.assignmentDate>=:gradingFromDate");
			}
			if(searchParams.getGradingTo() != null) {
				sb.append(" and assignment.assignmentDate<=:gradingToDate");
			}
			sb.append(")");
		}

		TypedQuery<GraderToIdentity> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), GraderToIdentity.class);
		applyGradersSearchParameters(query, searchParams);
		return query.getResultList();
	}

	public List<ReferenceEntryWithStatistics> getReferenceEntriesStatistics(Identity grader) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select refEntry,")
		  .append(" count(distinct assignment.key) as totalAssignments,")
		  .append(" sum(case when assignment.status ").in(GradingAssignmentStatus.done).append(" then 1 else 0 end) as doneAssignments,")
		  .append(" sum(case when assignment.status ").in(GradingAssignmentStatus.assigned, GradingAssignmentStatus.inProcess).append(" then 1 else 0 end) as openAssignments,")
		  .append(" sum(case when assignment.status ").in(GradingAssignmentStatus.assigned, GradingAssignmentStatus.inProcess)
		  .append("   and ((assignment.extendedDeadline is null and assignment.deadline < current_date) or assignment.extendedDeadline < current_date )")
		  .append("   then 1 else 0 end) as overdueAssignments,")
		  .append(" min(assignment.creationDate),")
		  .append(" sum(record.time) as recordedTime")
		  .append(" from gradingassignment as assignment")
		  .append(" inner join assignment.referenceEntry as refEntry")
		  .append(" inner join grader2identity rel on (assignment.grader.key=rel.key)")
		  .append(" left join gradingtimerecord record on (record.assignment.key=assignment.key and record.grader.key=assignment.grader.key)")
		  .append(" where rel.identity.key=:graderKey")
		  .append(" group by refEntry");
		
		List<Object[]> rawObjects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("graderKey", grader.getKey())
				.getResultList();
		List<ReferenceEntryWithStatistics> statistics = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			int pos = 0;
			RepositoryEntry referenceEntry = (RepositoryEntry)rawObject[pos++];
			long total = PersistenceHelper.extractPrimitiveLong(rawObject, pos++);
			long done = PersistenceHelper.extractPrimitiveLong(rawObject, pos++);
			long open = PersistenceHelper.extractPrimitiveLong(rawObject, pos++);
			long overdue = PersistenceHelper.extractPrimitiveLong(rawObject, pos++);
			Date oldest = (Date)rawObject[pos++];
			long time = PersistenceHelper.extractPrimitiveLong(rawObject, pos);
			statistics.add(new ReferenceEntryWithStatistics(referenceEntry, total, done, open, overdue, oldest, time));
		}
		return statistics;
	}
	
	public List<GraderStatistics> getGradersStatistics(GradersSearchParameters searchParams) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select rel.identity.key,")
		  .append(" count(distinct assignment.key) as totalAssignments,")
		  .append(" sum(case when assignment.status ").in(GradingAssignmentStatus.done).append(" then 1 else 0 end) as doneAssignments,")
		  .append(" sum(case when assignment.status ").in(GradingAssignmentStatus.assigned, GradingAssignmentStatus.inProcess).append(" then 1 else 0 end) as openAssignments,")
		  .append(" sum(case when assignment.status ").in(GradingAssignmentStatus.assigned, GradingAssignmentStatus.inProcess)
		  .append("   and ((assignment.extendedDeadline is null and assignment.deadline < current_date) or assignment.extendedDeadline < current_date )")
		  .append("   then 1 else 0 end) as overdueAssignments,")
		  .append(" min(assignment.creationDate),")
		  .append(" sum(record.time) as recordedTime")
		  .append(" from gradingassignment as assignment")
		  .append(" inner join grader2identity rel on (assignment.grader.key=rel.key)")
		  .append(" left join gradingtimerecord record on (record.assignment.key=assignment.key and record.grader.key=assignment.grader.key)");

		if(searchParams.getManager() != null) {
			sb.and()
			  .append("exists (select membership.key from repoentrytogroup as relGroup")
			  .append(" inner join relGroup.group as baseGroup")
			  .append(" inner join baseGroup.members as membership")
			  .append(" where relGroup.entry.key=rel.entry.key and membership.identity.key=:managerKey")
			  .append("  and membership.role ").in(GroupRoles.owner, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator, OrganisationRoles.principal)
			  .append(")");
		}
		
		if(searchParams.getReferenceEntry() != null) {
			sb.and().append("rel.entry.key=:entryKey");
		}
		if(searchParams.getGrader() != null) {
			sb.and().append("rel.identity.key=:graderKey");
		}
		if(searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			sb.and().append("rel.status in (:statusList)");
		}
		if(searchParams.getGradingFrom() != null) {
			sb.and().append("assignment.assignmentDate>=:gradingFromDate");
		}
		if(searchParams.getGradingTo() != null) {
			sb.and().append("assignment.assignmentDate<=:gradingToDate");
		}
		
		sb.append(" group by rel.identity.key");
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class);
		applyGradersSearchParameters(query, searchParams);

		List<Object[]> rawObjects = query.getResultList();
		List<GraderStatistics> statistics = new ArrayList<>(rawObjects.size());
		for(Object[] rawObject:rawObjects) {
			int pos = 0;
			Long identityKey = (Long)rawObject[pos++];
			long total = PersistenceHelper.extractPrimitiveLong(rawObject, pos++);
			long done = PersistenceHelper.extractPrimitiveLong(rawObject, pos++);
			long open = PersistenceHelper.extractPrimitiveLong(rawObject, pos++);
			long overdue = PersistenceHelper.extractPrimitiveLong(rawObject, pos++);
			Date oldest = (Date)rawObject[pos++];
			long time = PersistenceHelper.extractPrimitiveLong(rawObject, pos);
			statistics.add(new GraderStatistics(identityKey, total, done, open, overdue, oldest, time));
		}
	
		return statistics;
	}
	
	private void applyGradersSearchParameters(TypedQuery<?> query, GradersSearchParameters searchParams) {
		if(searchParams.getReferenceEntry() != null) {
			query.setParameter("entryKey", searchParams.getReferenceEntry().getKey());
		}
		if(searchParams.getGrader() != null) {
			query.setParameter("graderKey", searchParams.getGrader().getKey());
		}
		if(searchParams.getStatus() != null && !searchParams.getStatus().isEmpty()) {
			List<String> statusList = searchParams.getStatus().stream()
					.map(GraderStatus::name).collect(Collectors.toList());
			query.setParameter("statusList", statusList);
		}
		if(searchParams.getGradingFrom() != null) {
			query.setParameter("gradingFromDate", searchParams.getGradingFrom(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getGradingTo() != null) {
			query.setParameter("gradingToDate", searchParams.getGradingTo(), TemporalType.TIMESTAMP);
		}
		if(searchParams.getManager() != null) {
			query.setParameter("managerKey", searchParams.getManager().getKey());
		}
	}
	
	public GraderToIdentity updateGrader(GraderToIdentity grader) {
		((GraderToIdentityImpl)grader).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(grader);
	}
	
	public void deleteGraderRelation(GraderToIdentity grader) {
		dbInstance.getCurrentEntityManager().remove(grader);
	}
}
