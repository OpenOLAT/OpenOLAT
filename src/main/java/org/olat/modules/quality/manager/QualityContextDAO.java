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
package org.olat.modules.quality.manager;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRef;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionRef;
import org.olat.modules.quality.model.QualityContextImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class QualityContextDAO {

	private static final Logger log = Tracing.createLoggerFor(QualityContextDAO.class);

	@Autowired
	private DB dbInstance;
	
	QualityContext createContext(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation, QualityContextRole role,
			String location, RepositoryEntry audienceRepositoryEntry, CurriculumElement audienceCurriculumElement) {
		QualityContextImpl context = new QualityContextImpl();
		context.setCreationDate(new Date());
		context.setLastModified(context.getCreationDate());
		context.setDataCollection(dataCollection);
		context.setEvaluationFormParticipation(evaluationFormParticipation);
		context.setRole(role != null? role: QualityContextRole.none);
		context.setLocation(location);
		context.setAudienceRepositoryEntry(audienceRepositoryEntry);
		context.setAudienceCurriculumElement(audienceCurriculumElement);
		dbInstance.getCurrentEntityManager().persist(context);
		log.debug("Quality context created: " + context.toString());
		return context;
	}
	
	QualityContext loadByKey(QualityContextRef contextRef) {
		if (contextRef == null || contextRef.getKey() == null) return null;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.key = :contextKey");
		
		 List<QualityContext> contexts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContext.class)
				.setParameter("contextKey", contextRef.getKey())
				.getResultList();
		return contexts.isEmpty() ? null : contexts.get(0);
	}

	List<QualityContext> loadByDataCollection(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.dataCollection.key = :dataCollectionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContext.class)
				.setParameter("dataCollectionKey", dataCollectionRef.getKey())
				.getResultList();
	}
	
	List<QualityContext> loadByDataCollectionFetched(QualityDataCollectionRef dataCollectionRef) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) {
			return Collections.emptyList();
		}
		//TODO jee9 remove 2 fetch
		StringBuilder sb = new StringBuilder();
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.append("       left join fetch context.audienceRepositoryEntry");
		sb.append("       left join fetch context.audienceCurriculumElement");
		sb.append("       left join fetch contexttocurriculumelement contextToCurriculumElement");
		sb.append("              on contextToCurriculumElement.context.key = context.key");
		sb.append("       left join fetch curriculumelement as curriculumElement");
		sb.append("              on contextToCurriculumElement.curriculumElement.key = curriculumElement.key");
		sb.append("       left join fetch contexttocurriculum contextToCurriculum");
		sb.append("              on contextToCurriculum.context.key = context.key");
		sb.append("       left join contextToCurriculum.curriculum as contextCurriculum");
		sb.append("       left join fetch contexttoorganisation contextToOrganisation");
		sb.append("              on contextToOrganisation.context.key = context.key");
		sb.append("       left join contextToOrganisation.organisation as contextOrganisation");
		sb.append("       left join fetch contexttotaxonomylevel contextToTaxonomyLevel");
		sb.append("              on contextToTaxonomyLevel.context.key = context.key");
		sb.append("       left join fetch ctaxonomylevel as taxonomyLevel");
		sb.append("              on contextToTaxonomyLevel.taxonomyLevel.key = taxonomyLevel.key");
		sb.append(" where context.dataCollection.key = :dataCollectionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContext.class)
				.setParameter("dataCollectionKey", dataCollectionRef.getKey())
				.getResultList();
	}

	List<QualityContext> loadByParticipation(EvaluationFormParticipationRef participationRef) {
		if (participationRef == null || participationRef.getKey() == null) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.evaluationFormParticipation.key = :participationKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContext.class)
				.setParameter("participationKey", participationRef.getKey())
				.getResultList();
	}

	public List<QualityContext> loadBySession(EvaluationFormSessionRef sessionRef) {
		if (sessionRef == null || sessionRef.getKey() == null) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.evaluationFormSession.key = :sessionKey");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContext.class)
				.setParameter("sessionKey", sessionRef.getKey())
				.getResultList();
	}

	/**
	 * Load the context by participation and no audience.
	 * The returned list should have 0 or 1 entry, but it is safer to even load faulty entries.
	 *
	 * @param participationRef
	 * @return
	 */
	List<QualityContext> loadByWithoutAudience(EvaluationFormParticipationRef participationRef) {
		if (participationRef == null || participationRef.getKey() == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.evaluationFormParticipation.key = :participationKey");
		sb.append("   and context.audienceRepositoryEntry is null");
		sb.append("   and context.audienceCurriculumElement is null");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContext.class)
				.setParameter("participationKey", participationRef.getKey())
				.getResultList();
	}

	/**
	 * Load the context by participation and audience repository entry.
	 * The returned list should have 0 or 1 entry, but it is safer to even load faulty entries.
	 *
	 * @param participationRef
	 * @param repositoryEntryRef
	 * @return
	 */
	List<QualityContext> loadByAudienceRepositoryEntry(
			EvaluationFormParticipationRef participationRef, RepositoryEntryRef repositoryEntryRef, QualityContextRole role) {
		if (participationRef == null || participationRef.getKey() == null || repositoryEntryRef == null
				|| repositoryEntryRef.getKey() == null || role == null) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.evaluationFormParticipation.key = :participationKey");
		sb.append("   and context.audienceRepositoryEntry.key = :entryKey");
		sb.append("   and context.role = :roleName");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContext.class)
				.setParameter("participationKey", participationRef.getKey())
				.setParameter("entryKey", repositoryEntryRef.getKey())
				.setParameter("roleName", role)
				.getResultList();
	}

	/**
	 * Load the context by participation and audience curriculum element.
	 * The returned list should have 0 or 1 entry, but it is safer to even load faulty entries.
	 *
	 * @param participationRef
	 * @param repositoryEntryRef
	 * @return
	 */
	List<QualityContext> loadByAudienceCurriculumElement(EvaluationFormParticipationRef participationRef,
			CurriculumElementRef curriculumElementRef, QualityContextRole role) {
		if (participationRef == null || participationRef.getKey() == null || curriculumElementRef == null
				|| curriculumElementRef.getKey() == null || role == null) {
			return Collections.emptyList();
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.evaluationFormParticipation.key = :participationKey");
		sb.append("   and context.audienceCurriculumElement.key = :curriculumElementKey");
		sb.append("   and context.role = :roleName");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContext.class)
				.setParameter("participationKey", participationRef.getKey())
				.setParameter("curriculumElementKey", curriculumElementRef.getKey())
				.setParameter("roleName", role)
				.getResultList();
	}

	List<EvaluationFormParticipation> loadParticipationByRole(QualityDataCollectionRef dataCollectionRef,
			QualityContextRole role) {
		if (dataCollectionRef == null || dataCollectionRef.getKey() == null) return Collections.emptyList();
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context.evaluationFormParticipation");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.dataCollection.key = :dataCollectionKey");
		sb.append("   and context.role = :roleName");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormParticipation.class)
				.setParameter("dataCollectionKey", dataCollectionRef.getKey())
				.setParameter("roleName", role)
				.getResultList();
	}

	public boolean hasContexts(EvaluationFormParticipationRef participationRef) {
		if (participationRef == null || participationRef.getKey() == null) return false;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select context.key");
		sb.append("  from qualitycontext as context");
		sb.append(" where context.evaluationFormParticipation.key = :participationKey");
		
		List<Long> types = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("participationKey", participationRef.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return types != null && !types.isEmpty() && types.get(0) != null && types.get(0).longValue() > 0;
	}
	
	public boolean hasContexts(RepositoryEntryRef entryRef) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select context.key from qualitycontext as context")
		  .append(" where context.audienceRepositoryEntry.key=:entryKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("entryKey", entryRef.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}

	public boolean hasContexts(CurriculumElementRef curriculumElementRef) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("select context.key from qualitycontext as context")
		  .append(" where context.audienceCurriculumElement.key=:curriculumElementKey");
		
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("curriculumElementKey", curriculumElementRef.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}

	void finish(EvaluationFormParticipation participation, EvaluationFormSession session) {
		if (participation == null || session == null) return;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.and().append("context.evaluationFormParticipation.key = :participationKey");
		
		List<QualityContextImpl> contexts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContextImpl.class)
				.setParameter("participationKey", participation.getKey())
				.getResultList();
		for (QualityContextImpl context : contexts) {
			context.setEvaluationFormSession(session);
			context.setEvaluationFormParticipation(null);
			context.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(context);
		}
	}
	
	void reopen(EvaluationFormSession session, EvaluationFormParticipation participation) {
		if (participation == null || session == null) return;
		
		QueryBuilder sb = new QueryBuilder();
		sb.append("select context");
		sb.append("  from qualitycontext as context");
		sb.and().append("context.evaluationFormSession.key = :sessionKey");
		
		List<QualityContextImpl> contexts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), QualityContextImpl.class)
				.setParameter("sessionKey", session.getKey())
				.getResultList();
		for (QualityContextImpl context : contexts) {
			context.setEvaluationFormSession(null);
			context.setEvaluationFormParticipation(participation);
			context.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(context);
		}
	}
	
	void deleteContext(QualityContextRef contextRef) {
		if (contextRef == null || contextRef.getKey() == null) return;
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("delete from qualitycontext as context");
		sb.append(" where context.key = :contextKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("contextKey", contextRef.getKey())
				.executeUpdate();
		
		log.debug("Quality context deleted. Keys: " + contextRef.getKey());
	}

}
