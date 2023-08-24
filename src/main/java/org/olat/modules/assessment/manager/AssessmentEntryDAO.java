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
package org.olat.modules.assessment.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentEntryScoring;
import org.olat.modules.assessment.ObligationOverridable;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentEntryImpl;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentEntryDAO {
	
	@Autowired
	private DB dbInstance;
	
	public AssessmentEntry createAssessmentEntry(Identity assessedIdentity, String anonymousIdentifier,
			RepositoryEntry entry, String subIdent, Boolean entryRoot, RepositoryEntry referenceEntry) {
		
		AssessmentEntryImpl data = new AssessmentEntryImpl();
		data.setCreationDate(new Date());
		data.setLastModified(data.getCreationDate());
		data.setRun(1);
		data.setIdentity(assessedIdentity);
		data.setAnonymousIdentifier(anonymousIdentifier);
		data.setRepositoryEntry(entry);
		data.setSubIdent(subIdent);
		data.setEntryRoot(entryRoot);
		data.setReferenceEntry(referenceEntry);
		dbInstance.getCurrentEntityManager().persist(data);
		return data;
	}
	
	public AssessmentEntry loadAssessmentEntryById(Long id) {
		List<AssessmentEntry> nodeAssessment = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAssessmentEntryById", AssessmentEntry.class)
				.setParameter("key", id)
				.getResultList();
		return nodeAssessment.isEmpty() ? null : nodeAssessment.get(0);
	}

	public AssessmentEntry loadAssessmentEntry(IdentityRef assessedIdentity, String anonymousIdentifier, RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select data from assessmententry data")
		  .append(" left join fetch data.identity ident")
		  .append(" where data.repositoryEntry.key=:repositoryEntryKey");

		if(subIdent != null) {
			sb.append(" and data.subIdent=:subIdent");
		} else {
			sb.append(" and data.subIdent is null");
		}
		
		if(anonymousIdentifier != null) {
			sb.append(" and data.anonymousIdentifier=:anonymousIdentifier");
		} else {
			sb.append(" and data.identity.key=:identityKey")
			  .append(" and data.anonymousIdentifier is null");
		}

		TypedQuery<AssessmentEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey());
		if(subIdent != null) {
			query.setParameter("subIdent", subIdent);
		} 
		if(anonymousIdentifier != null) {
			query.setParameter("anonymousIdentifier", anonymousIdentifier);
		} else {
			query.setParameter("identityKey", assessedIdentity.getKey());
		}

		List<AssessmentEntry> entries = query.getResultList();
		return entries.isEmpty() ? null : entries.get(0);
	}
	
	public AssessmentEntry loadAssessmentEntry(IdentityRef assessedIdentity, RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select data from assessmententry data");
		sb.append(" where data.repositoryEntry.key=:repositoryEntryKey and data.identity.key=:identityKey");
		if(subIdent != null) {
			sb.append(" and data.subIdent=:subIdent");
		} else {
			sb.append(" and data.subIdent is null");
		}

		TypedQuery<AssessmentEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("identityKey", assessedIdentity.getKey());
		if(subIdent != null) {
			query.setParameter("subIdent", subIdent);
		}
		List<AssessmentEntry> entries = query.getResultList();
		return entries.isEmpty() ? null : entries.get(0);
	}
	
	public AssessmentEntry loadAssessmentEntry(IdentityRef assessedIdentity, RepositoryEntryRef entry, String subIdent, RepositoryEntryRef referenceEntry) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select data from assessmententry data");
		if(referenceEntry != null) {
			sb.append(" inner join data.referenceEntry referenceEntry");
		}
		
		sb.append(" where data.repositoryEntry.key=:repositoryEntryKey and data.identity.key=:identityKey");
		if(subIdent != null) {
			sb.append(" and data.subIdent=:subIdent");
		} else {
			sb.append(" and data.subIdent is null");
		}
		
		if(referenceEntry != null) {
			sb.append(" and referenceEntry.key=:referenceEntryKey");
		} else {
			sb.append(" and data.referenceEntry is null");
		}

		TypedQuery<AssessmentEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("identityKey", assessedIdentity.getKey());
		if(subIdent != null) {
			query.setParameter("subIdent", subIdent);
		}
		if(referenceEntry != null) {
			query.setParameter("referenceEntryKey", referenceEntry.getKey());
		}
		List<AssessmentEntry> entries = query.getResultList();
		return entries.isEmpty() ? null : entries.get(0);
	}
	
	public boolean hasAssessmentEntry(IdentityRef assessedIdentity, RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select new java.lang.Boolean(count(data) > 0)");
		sb.append("  from assessmententry data");
		sb.append(" where data.repositoryEntry.key=:repositoryEntryKey and data.identity.key=:identityKey");
		
		Boolean exists = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Boolean.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("identityKey", assessedIdentity.getKey())
				.getSingleResult();
		return exists.booleanValue();
	}

	public boolean hasGrades(RepositoryEntryRef remositoryEntry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select data.key");
		sb.append("  from assessmententry data");
		sb.and().append(" data.repositoryEntry.key=:repositoryEntryKey");
		sb.and().append(" data.subIdent=:subIdent");
		sb.and().append(" data.grade is not null");
		
		return !dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("repositoryEntryKey", remositoryEntry.getKey())
				.setParameter("subIdent", subIdent)
				.setMaxResults(1)
				.getResultList()
				.isEmpty();
	}
	
	public Long getScoreCount(RepositoryEntryRef repositoryEntry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(data.key)");
		sb.append("  from assessmententry data");
		sb.and().append(" data.repositoryEntry.key=:repositoryEntryKey");
		sb.and().append(" data.subIdent=:subIdent");
		sb.and().append(" data.score is not null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repositoryEntryKey", repositoryEntry.getKey())
				.setParameter("subIdent", subIdent)
				.getSingleResult();
	}
	
	public AssessmentEntry resetAssessmentEntry(AssessmentEntry nodeAssessment) {
		nodeAssessment.setScore(null);
		nodeAssessment.setAttempts(0);
		nodeAssessment.setLastAttempt(null);
		nodeAssessment.setCompletion(null);
		nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.notStarted);
		nodeAssessment.setAssessmentDoneBy(null);
		if (nodeAssessment instanceof AssessmentEntryImpl impl) {
			impl.setLastModified(new Date());
			impl.setRawPassed(null);
			impl.setPassedOriginal(null);
			impl.setPassedDate(null);
			impl.setPassedModificationIdentity(null);
			impl.setPassedModificationDate(null);
		}
		return mergeAndLoadIdentity(nodeAssessment);
	}
	
	public AssessmentEntry setLastVisit(AssessmentEntry nodeAssessment, Date lastVisit) {
		if (nodeAssessment instanceof AssessmentEntryImpl impl) {
			impl.setLastVisit(lastVisit);
			if (nodeAssessment.getFirstVisit() == null) {
				impl.setFirstVisit(lastVisit);
			}
			int numVisits = nodeAssessment.getNumberOfVisits() != null? nodeAssessment.getNumberOfVisits().intValue(): 0;
			numVisits++;
			impl.setNumberOfVisits(Integer.valueOf(numVisits));
			impl.setLastModified(new Date());
			return mergeAndLoadIdentity(impl);
		}
		return nodeAssessment;
	}
	
	public AssessmentEntry updateAssessmentEntry(AssessmentEntry nodeAssessment) {
		if (nodeAssessment instanceof AssessmentEntryImpl impl) {
			impl.setLastModified(new Date());
			Overridable<Boolean> passed = nodeAssessment.getPassedOverridable();
			impl.setRawPassed(passed.getCurrent());
			impl.setPassedOriginal(passed.getOriginal());
			impl.setPassedModificationIdentity(passed.getModBy());
			impl.setPassedModificationDate(passed.getModDate());
			Overridable<Date> end = nodeAssessment.getEndDate();
			impl.setEndDate(end.getCurrent());
			impl.setEndDateOriginal(end.getOriginal());
			impl.setEndDateModificationIdentity(end.getModBy());
			impl.setEndDateModificationDate(end.getModDate());
			ObligationOverridable obligation = nodeAssessment.getObligation();
			impl.setObligation(obligation.getCurrent());
			impl.setObligationInherited(obligation.getInherited());
			impl.setObligationEvaluated(obligation.getEvaluated());
			impl.setObligationConfig(obligation.getConfigCurrent());
			impl.setObligationOriginal(obligation.getConfigOriginal());
			impl.setObligationModIdentity(obligation.getModBy());
			impl.setObligationModDate(obligation.getModDate());
		}
		return mergeAndLoadIdentity(nodeAssessment);
	}
	
	/**
	 * Merge the specified assessment entry and ensure the associated
	 * identity and user is load.
	 */
	private AssessmentEntry mergeAndLoadIdentity(AssessmentEntry assessmentEntry) {
		assessmentEntry = dbInstance.getCurrentEntityManager().merge(assessmentEntry);
		Identity identity = assessmentEntry.getIdentity();
		if(identity != null) {
			identity.getUser().getKey();
		}
		return assessmentEntry;
	}
	
	public void resetAllRootPassed(RepositoryEntry entry) {
		if (entry == null) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("update assessmententry data");
		sb.append("   set data.passedOriginal = :passedOriginal");
		sb.append("     , data.lastModified = :lastModified");
		sb.append(" where data.entryRoot = true");
		sb.append("   and data.passedModificationDate is not null");
		sb.append("   and data.repositoryEntry.key = :repositoryEntryKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("passedOriginal", null)
				.setParameter("lastModified", new Date())
				.setParameter("repositoryEntryKey", entry.getKey())
				.executeUpdate();
		
		sb = new StringBuilder();
		sb.append("update assessmententry data");
		sb.append("   set data.passed = :passed");
		sb.append("     , data.passedDate = :passedDate");
		sb.append("     , data.lastModified = :lastModified");
		sb.append(" where data.entryRoot = true");
		sb.append("   and data.passedModificationDate is null");
		sb.append("   and data.repositoryEntry.key = :repositoryEntryKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("passed", null)
				.setParameter("passedDate", null)
				.setParameter("lastModified", new Date())
				.setParameter("repositoryEntryKey", entry.getKey())
				.executeUpdate();
	}
	
	public void resetAllOverridenRootPassed(RepositoryEntry entry) {
		if (entry == null) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("update assessmententry data");
		sb.append("   set data.passed = data.passedOriginal");
		sb.append("     , data.passedDate = :passedDate");
		sb.append("     , data.passedOriginal = :passedOriginal");
		sb.append("     , data.passedModificationDate = :passedModificationDate");
		sb.append("     , data.passedModificationIdentity = :passedModificationIdentity");
		sb.append("     , data.lastModified = :lastModified");
		sb.append(" where data.entryRoot = true");
		sb.append("   and data.passedModificationDate is not null");
		sb.append("   and data.repositoryEntry.key = :repositoryEntryKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("passedOriginal", null)
				.setParameter("passedDate", null)
				.setParameter("passedModificationDate", null)
				.setParameter("passedModificationIdentity", null)
				.setParameter("lastModified", new Date())
				.setParameter("repositoryEntryKey", entry.getKey())
				.executeUpdate();
	}
	
	public Long loadAssessmentEntriesCount(RepositoryEntryRef entry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(data.key)");
		sb.append("  from assessmententry data");
		sb.and().append(" data.repositoryEntry.key=:repositoryEntryKey");

		if(subIdent != null) {
			sb.and().append("data.subIdent=:subIdent");
		} else {
			sb.and().append("data.subIdent is null");
		}

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repositoryEntryKey", entry.getKey());
		if(subIdent != null) {
			query.setParameter("subIdent", subIdent);
		} 

		return query.getSingleResult();
	}
	
	/**
	 * Load all assessment entries for the specific assessed repository entry with
	 * the specific sub identifier (it is mandatory). The anonym users are excluded
	 * by the query. Reference entry is fetched if exists.
	 * 
	 * @param entry The entry (mandatory)
	 * @param subIdent The subIdent (mandatory)
	 * @return A list of assessment entries
	 */
	public List<AssessmentEntry> loadAssessmentEntryBySubIdent(RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select data from assessmententry data ")
		   .append(" inner join fetch data.identity ident")
		   .append(" inner join fetch ident.user identuser")
		   .append(" left join fetch data.referenceEntry referenceEntry")
		   .append(" where data.repositoryEntry.key=:repositoryEntryKey and data.subIdent=:subIdent");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}
	
	/**
	 * Load all assessment entries for the specific assessed repository entry with
	 * the specific sub identifier (it is mandatory). The anonym users are excluded
	 * by the query. The status of the assessment entry is optional 
	 * 
	 * @param entry The entry (mandatory)
	 * @param subIdent The subIdent (mandatory)
	 * @param status The status of the assessment entry (optional)
	 * @param excludeZeroScore disallow zero (0) scores
	 * @param userVisibleOnly 
	 * @return A list of assessment entries
	 */
	public List<AssessmentEntry> loadAssessmentEntryBySubIdentWithStatus(RepositoryEntryRef entry, String subIdent,
			AssessmentEntryStatus status, boolean excludeZeroScore, boolean userVisibleOnly) {
		StringBuilder sb = new StringBuilder();
		sb.append("select data from assessmententry data ")
		   .append(" inner join fetch data.identity ident") 
		   .append(" inner join fetch ident.user identuser")
		   .append(" where data.repositoryEntry.key=:repositoryEntryKey")
		   .append(" and data.subIdent=:subIdent")
		   .append(" and data.score is not null")
		   .append(" and ident.key in ( select membership.identity.key from repoentrytogroup as rel, bgroupmember membership ")
		   .append(" where rel.entry.key=:repositoryEntryKey and rel.group.key=membership.group.key and membership.role='")
		   .append(GroupRole.participant).append("'")
		   .append(" )");
		
		if (status != null) {
			sb.append(" and data.status=:status");
		}		
		if(excludeZeroScore) {
			sb.append(" and data.score > 0");
		}
		if(userVisibleOnly) {
			sb.append("and data.userVisibility = true");
		}
		
		TypedQuery<AssessmentEntry> typedQuery = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("subIdent", subIdent);
		
		if (status != null) {
			typedQuery.setParameter("status", status.name());	
		}	
		
		return typedQuery.getResultList();
	}
	
	public List<Identity> getAllIdentitiesWithAssessmentData(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct data.identity from assessmententry data where data.repositoryEntry.key=:repositoryEntryKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.getResultList();
	}

	/**
	 * Load all the assessment entries for a specific user and a specific assessed repository entry
	 * (typically a course).
	 * 
	 * @param assessedIdentity The assessed user
	 * @param entry The assessed course / repository entry
	 * @return A list of assessment entries
	 */
	public List<AssessmentEntry> loadAssessmentEntriesByAssessedIdentity(Identity assessedIdentity, RepositoryEntry entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("select data from assessmententry data where data.repositoryEntry.key=:repositoryEntryKey and data.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("identityKey", assessedIdentity.getKey())
				.getResultList();
	}
	
	public List<Long> loadResourceIds(Identity assessedIdentity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct ores.resId from assessmententry data")
		  .append(" inner join data.repositoryEntry as v")
		  .append(" inner join v.olatResource as ores")
		  .append(" where data.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", assessedIdentity.getKey())
				.getResultList();
	}

	/**
	 * Load all the assessment entry of the specific group. But aware that the query exclude the default group
	 * of the repository entry! The query doesn't check the member ship but only the relation to the course.
	 * 
	 * @param assessedGroup The group (mandatory)
	 * @param entry The repository entry (mandatory)
	 * @param subIdent The sub identifier (mandatory)
	 * @return A list of assessment entries
	 */
	public List<AssessmentEntry> loadAssessmentEntryByGroup(Group assessedGroup, RepositoryEntry entry, String subIdent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select data from assessmententry data")
		  .append(" inner join data.repositoryEntry v")
		  .append(" inner join v.groups as relGroup on relGroup.defaultGroup=false")
		  .append(" where relGroup.group.key=:groupKey and v.key=:repositoryEntryKey and data.subIdent=:subIdent");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", entry.getKey())
				.setParameter("groupKey", assessedGroup.getKey())
				.setParameter("subIdent", subIdent)
				.getResultList();
	}

	public List<AssessmentEntryScoring> loadRootAssessmentEntriesByAssessedIdentity(Identity assessedIdentity, Collection<Long> entryKeys) {
		if (assessedIdentity == null || entryKeys == null || entryKeys.isEmpty()) return Collections.emptyList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.assessment.model.AssessmentEntryScoringImpl(");
		sb.append("       ae.key");
		sb.append("     , ae.repositoryEntry.key");
		sb.append("     , ae.completion");
		sb.append("     , ae.score");
		sb.append("     , ae.maxScore");
		sb.append("     , ae.passed");
		sb.append("     , ae.passedOriginal");
		sb.append("     , ae.passedModificationDate");
		sb.append("     )");
		sb.append("  from assessmententry ae");
		sb.and().append(" ae.entryRoot = true");
		sb.and().append(" ae.identity.key = :identityKey");
		sb.and().append(" ae.repositoryEntry.key in (:entryKeys)");
		
		List<Long> entryKeysList = new ArrayList<>(entryKeys);
		List<AssessmentEntryScoring> assessmentEntries = new ArrayList<>(entryKeys.size());
		
		int count = 0;
		int batch = 5000;
		do {
			int toIndex = Math.min(count + batch, entryKeysList.size());
			List<Long> toLoad = entryKeysList.subList(count, toIndex);
			List<AssessmentEntryScoring> batchOfEntries = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), AssessmentEntryScoring.class)
					.setParameter("identityKey", assessedIdentity.getKey())
					.setParameter("entryKeys", toLoad)
					.setFlushMode(FlushModeType.COMMIT)
					.getResultList();
					
			assessmentEntries.addAll(batchOfEntries);
			count += batch;
		} while(count < entryKeysList.size());
		return assessmentEntries;
	}

	public List<AssessmentEntryCompletion> loadAvgCompletionsByIdentities(RepositoryEntry entry, Collection<Long> identityKeys) {
		if (entry == null || identityKeys == null || identityKeys.isEmpty()) return Collections.emptyList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.assessment.model.AssessmentEntryCompletionImpl(");
		sb.append("       ae.identity.key");
		sb.append("     , ae.completion)");
		sb.append("  from assessmententry ae");
		sb.and().append(" ae.entryRoot = true");
		sb.and().append(" ae.repositoryEntry.key = :entryKey");
		sb.and().append(" ae.identity.key in (:identityKeys)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntryCompletion.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("identityKeys", identityKeys)
				.getResultList();
	}
	
	public List<AssessmentEntryCompletion> loadAvgCompletionsByCurriculumElements(Identity assessedIdentity, Collection<Long> curriculumElementKeys) {
		if (assessedIdentity == null || curriculumElementKeys == null || curriculumElementKeys.isEmpty()) return Collections.emptyList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select new org.olat.modules.assessment.model.AssessmentEntryCompletionImpl(");
		sb.append("       el.key ");
		sb.append("     , (select avg(ae2.completion) ");
		sb.append("          from assessmententry ae2");
		sb.append("         where ae2.key in (");
		sb.append("               select distinct ae.key");
		sb.append("                 from assessmententry ae");
		sb.append("                    , repositoryentry re");
		sb.append("                    , repoentrytogroup reToGroup");
		sb.append("                    , bgroupmember participants");
		sb.append("                    , curriculumelement el2");
		sb.append("                where ae.entryRoot = true");
		sb.append("                  and ae.identity.key = participants.identity.key");
		sb.append("                  and ae.repositoryEntry.key = reToGroup.entry.key");
		sb.append("                  and re.key = reToGroup.entry.key");
		sb.append("                  and re.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
		sb.append("                  and participants.role = '").append(CurriculumRoles.participant.name()).append("'");
		sb.append("                  and participants.identity.key = :identityKey");
		sb.append("                  and reToGroup.group.key = participants.group.key");
		sb.append("                  and el2.group.key = reToGroup.group.key");
		sb.append("                  and el2.materializedPathKeys like concat(el.materializedPathKeys, '%')");
		sb.append("                )");
		sb.append("       )");
		sb.append("     )");
		sb.append("  from curriculumelement el");
		sb.append(" where el.key in (:elKeys)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntryCompletion.class)
				.setParameter("identityKey", assessedIdentity.getKey())
				.setParameter("elKeys", curriculumElementKeys)
				.getResultList();
	}
	
	public List<AssessmentEntryCompletion> loadAvgCompletionsByIdentities(CurriculumElement curriculumElement,
			List<Long> identityKeys) {
		if (curriculumElement == null || identityKeys == null || identityKeys.isEmpty()) return Collections.emptyList();
			
			QueryBuilder sb = new QueryBuilder();
			sb.append("select new org.olat.modules.assessment.model.AssessmentEntryCompletionImpl(");
			sb.append("       ident.key");
			sb.append("     , (select avg(ae2.completion)");
			sb.append("          from assessmententry ae2");
			sb.append("         where ae2.key in (");
			sb.append("               select distinct ae.key");
			sb.append("                 from assessmententry ae");
			sb.append("                    , repositoryentry re");
			sb.append("                    , repoentrytogroup reToGroup");
			sb.append("                    , bgroupmember participants");
			sb.append("                    , curriculumelement el2");
			sb.append("                where ae.entryRoot = true");
			sb.append("                  and ae.identity.key = participants.identity.key");
			sb.append("                  and ae.repositoryEntry.key = reToGroup.entry.key");
			sb.append("                  and re.key = reToGroup.entry.key");
			sb.append("                  and re.status ").in(RepositoryEntryStatusEnum.preparationToClosed());
			sb.append("                  and participants.role = '").append(CurriculumRoles.participant.name()).append("'");
			sb.append("                  and participants.identity.key = ident.key");
			sb.append("                  and reToGroup.group.key = participants.group.key");
			sb.append("                  and el2.group.key = reToGroup.group.key");
			sb.append("                  and el2.materializedPathKeys like concat(:curEleMatPathKeys, '%')");
			sb.append("                )");
			sb.append("       )");
			sb.append("     )");
			sb.append("  from ").append(IdentityImpl.class.getName()).append(" as ident");
			sb.append(" where ident.key in (:identityKeys)");
			
			return dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), AssessmentEntryCompletion.class)
					.setParameter("curEleMatPathKeys", curriculumElement.getMaterializedPathKeys())
					.setParameter("identityKeys", identityKeys)
					.getResultList();
	}

	public List<AssessmentEntry> getRootEntriesWithStartOverSubEntries(Date start) {
		if (start == null) return Collections.emptyList();
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae");
		sb.append("  from assessmententry ae");
		sb.append("       inner join fetch ae.repositoryEntry re");
		sb.append("       inner join fetch re.olatResource reresource");
		sb.append("       inner join fetch ae.identity identity");
		sb.append("       inner join fetch identity.user user");
		sb.append(" where ae.entryRoot = true");
		sb.append("   and (ae.repositoryEntry.key, ae.identity.key) in (");
		sb.append("       select subae.repositoryEntry.key, subae.identity.key");
		sb.append("          from assessmententry subae");
		sb.append("          inner join courseelement as subelem on (subelem.repositoryEntry.key=subae.repositoryEntry.key and  subelem.subIdent=subae.subIdent)");
		sb.append("         where subae.startDate <= :start");
		sb.append("       )");
		sb.append("   and re.status").in(RepositoryEntryStatusEnum.preparationToClosed());
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("start", start)
				.getResultList();
	}

	public List<AssessmentEntry> loadRootEntriesWithoutPassed(RepositoryEntryRef repositoryEntry) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae");
		sb.append("  from assessmententry ae");
		sb.append("       inner join fetch ae.identity identity");
		sb.append("       inner join fetch identity.user user");
		sb.and().append("ae.entryRoot = true");
		sb.and().append("ae.passed is null");
		sb.and().append("ae.repositoryEntry.key = :repositoryEntryKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("repositoryEntryKey", repositoryEntry.getKey())
				.getResultList();
	}
	
	public boolean hasAssessmentEntryWithoutCoachAssignment(RepositoryEntryRef repositoryEntry, String subIdent) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae.key")
		  .append("  from assessmententry ae")
		  .append("  inner join bgroupmember as bmember on (bmember.identity.key=ae.identity.key and bmember.role='").append(GroupRoles.participant.name()).append("')")
		  .append("  inner join bmember.group as relGroup")
		  .append("  inner join repoentrytogroup as rel on (relGroup.key=rel.group.key)")
		  .and().append("ae.identity.key is not null")
		  .and().append("ae.coach.key is null")
		  .and().append("ae.repositoryEntry.key=:repositoryEntryKey")
		  .and().append("ae.subIdent=:subIdent")
		  .and().append("rel.entry.key=:repositoryEntryKey");
		
		List<Long> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("repositoryEntryKey", repositoryEntry.getKey())
				.setParameter("subIdent", subIdent)
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return counts != null && !counts.isEmpty() && counts.get(0) != null && counts.get(0).longValue() > 0;
	}

	public List<Long> loadIdentityKeys(RepositoryEntry entry, String subIdent, Collection<AssessmentObligation> obligations) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae.identity.key");
		sb.append("  from assessmententry ae");
		sb.and().append("ae.repositoryEntry.key=:entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append("ae.subIdent=:subIdent");
		}
		if (obligations != null && !obligations.isEmpty()) {
			sb.append(" and (");
			if (obligations.contains(AssessmentObligation.mandatory)) {
				sb.append("ae.obligation is null or ");
			}
			sb.append(" ae.obligation in (:assessmentObligations))");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("entryKey", entry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		if (obligations != null && !obligations.isEmpty()) {
			query.setParameter("assessmentObligations", obligations);
		}
		return query.getResultList();
	}
	
	public List<AssessmentEntry> loadAssessmentEntriesCoachAssigned(RepositoryEntryRef entry, String subIdent, boolean withCoachAssign) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae from assessmententry ae")
		  .append(" inner join fetch ae.identity as ident")
		  .where().append("ae.repositoryEntry.key=:entryKey");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append("ae.subIdent=:subIdent");
		}
		sb.and().append("ae.coach.key is").append(" not", withCoachAssign).append(" null");
		
		TypedQuery<AssessmentEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("entryKey", entry.getKey());
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		return query.getResultList();
	}
	
	public List<AssessmentEntry> loadAssessmentEntriesForCoach(RepositoryEntryRef entry, IdentityRef coach) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ae from assessmententry ae")
		  .append(" inner join fetch ae.identity as ident")
		  .where().append("ae.repositoryEntry.key=:entryKey")
		  .and().append("ae.coach.key=:coachKey");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentEntry.class)
				.setParameter("entryKey", entry.getKey())
				.setParameter("coachKey", coach.getKey())
				.getResultList();
	}
	
	/**
	 * Delete all the entry where the specified repository entry is
	 * referenced as a test.
	 * 
	 * @param entry
	 * @return
	 */
	public int removeEntryForReferenceEntry(RepositoryEntryRef entry) {
		StringBuilder sb = new StringBuilder();
		sb.append("update assessmententry data set data.referenceEntry.key=null where data.referenceEntry.key=:referenceKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("referenceKey", entry.getKey())
				.executeUpdate();
	}
	
	/**
	 * Delete all entries where the specified repository entry (typically
	 * a course) is linked to them.
	 * 
	 * @param entry
	 * @return
	 */
	public int deleteEntryForRepositoryEntry(RepositoryEntryRef entry) {
		String query = "delete from assessmententry data where data.repositoryEntry.key=:entryKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("entryKey", entry.getKey())
				.executeUpdate();
	}
	
	public int deleteEntryForIdentity(IdentityRef identity) {
		String query = "delete from assessmententry data where data.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	
}
