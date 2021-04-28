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
package org.olat.modules.forms.manager;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyRef;
import org.olat.modules.forms.model.jpa.EvaluationFormParticipationImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class EvaluationFormParticipationDAO {

	private static final Logger log = Tracing.createLoggerFor(EvaluationFormParticipationDAO.class);

	@Autowired
	private DB dbInstance;

	EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey,
			EvaluationFormParticipationIdentifier identifier, boolean anonymous, Identity executor) {
		EvaluationFormParticipationImpl participation = new EvaluationFormParticipationImpl();
		participation.setCreationDate(new Date());
		participation.setLastModified(participation.getCreationDate());
		participation.setSurvey(survey);
		participation.setIdentifier(identifier);
		participation.setStatus(EvaluationFormParticipationStatus.prepared);
		participation.setAnonymous(anonymous);
		participation.setExecutor(executor);
		dbInstance.getCurrentEntityManager().persist(participation);
		log.debug("Participation created: " + participation.toString());
		return participation;
	}

	EvaluationFormParticipation changeStatus(EvaluationFormParticipation participation, EvaluationFormParticipationStatus status) {
		if (participation instanceof EvaluationFormParticipationImpl) {
			EvaluationFormParticipationImpl participationImpl = (EvaluationFormParticipationImpl) participation;
			participationImpl.setStatus(status);
			return update(participationImpl);
		}
		return participation;
	}
	
	EvaluationFormParticipation updateParticipation(EvaluationFormParticipation participation) {
		if (participation instanceof EvaluationFormParticipationImpl) {
			EvaluationFormParticipationImpl participationImpl = (EvaluationFormParticipationImpl) participation;
			return update(participationImpl);
		}
		return participation;
	}
	
	private EvaluationFormParticipation update(EvaluationFormParticipationImpl participationImpl) {
		participationImpl.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(participationImpl);
	}

	EvaluationFormParticipation loadByKey(EvaluationFormParticipationRef participationRef) {
		if (participationRef == null || participationRef.getKey() == null) return null;
		
		StringBuilder query = new StringBuilder();
		query.append("select participation from evaluationformparticipation as participation");
		query.append(" inner join fetch participation.survey survey");
		query.append(" where participation.key=:key");

		List<EvaluationFormParticipation> participations = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), EvaluationFormParticipation.class)
				.setParameter("key", participationRef.getKey())
				.getResultList();
		return participations.isEmpty() ? null : participations.get(0);
	}
	
	List<EvaluationFormParticipation> loadBySurvey(EvaluationFormSurveyRef survey,
			EvaluationFormParticipationStatus status, boolean fetchExecutor) {
		if (survey == null) return null;
		
		StringBuilder query = new StringBuilder();
		query.append("select participation from evaluationformparticipation as participation");
		if (fetchExecutor) {
			query.append(" left join fetch participation.executor executor");
			query.append(" left join fetch executor.user user");
		}
		query.append(" where participation.survey.key=:surveyKey");
		if (status != null) {
			query.append("   and participation.status=:status");
		}

		TypedQuery<EvaluationFormParticipation> typedQuery = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), EvaluationFormParticipation.class)
				.setParameter("surveyKey", survey.getKey());
		if (status != null) {
			typedQuery.setParameter("status", status);
		}
		return typedQuery.getResultList();
	}
	
	EvaluationFormParticipation loadByExecutor(EvaluationFormSurveyRef survey, IdentityRef executor) {
		if (survey == null || executor == null) return null;
		
		StringBuilder query = new StringBuilder();
		query.append("select participation from evaluationformparticipation as participation");
		query.append("  left join participation.executor executor");
		query.append(" where participation.survey.key=:surveyKey");
		query.append("   and participation.executor.key=:executorKey");

		List<EvaluationFormParticipation> participations = dbInstance.getCurrentEntityManager()
				.createQuery(query.toString(), EvaluationFormParticipation.class)
				.setParameter("surveyKey", survey.getKey())
				.setParameter("executorKey", executor.getKey())
				.getResultList();
		return participations.isEmpty() ? null : participations.get(0);
	}

	EvaluationFormParticipation loadByIdentifier(EvaluationFormParticipationIdentifier identifier) {
		return loadByIdentifier(null, identifier);
	}

	EvaluationFormParticipation loadByIdentifier(EvaluationFormSurveyRef survey,
			EvaluationFormParticipationIdentifier identifier) {
		if (identifier == null) return null;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select participation from evaluationformparticipation as participation");
		sb.append("  left join participation.executor executor");
		sb.append(" where participation.identifier.type=:identifierType");
		sb.append("   and participation.identifier.key=:identifierKey");
		if (survey != null) {
			sb.append(" and participation.survey.key=:surveyKey");
		}

		TypedQuery<EvaluationFormParticipation> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), EvaluationFormParticipation.class)
				.setParameter("identifierType", identifier.getType())
				.setParameter("identifierKey", identifier.getKey());
		if (survey != null) {
			query.setParameter("surveyKey", survey.getKey());
		}
		List<EvaluationFormParticipation> participations = query.getResultList();
		return participations.isEmpty() || participations.size() > 1? null : participations.get(0);
	}

	void deleteParticipations(List<? extends EvaluationFormParticipationRef> participationRefs) {
		if (participationRefs == null || participationRefs.isEmpty()) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from evaluationformparticipation as participation");
		sb.append(" where participation.key in (:participationKeys)");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("participationKeys", getParticipationsKeys(participationRefs))
				.executeUpdate();	
	}

	void deleteParticipations(EvaluationFormSurveyRef survey) {
		if (survey == null) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("delete from evaluationformparticipation as participation");
		sb.append(" where participation.survey.key=:surveyKey");
		
		dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("surveyKey", survey.getKey())
				.executeUpdate();
	}
	
	private Object getParticipationsKeys(List<? extends EvaluationFormParticipationRef> participationRefs) {
		return participationRefs.stream().map(EvaluationFormParticipationRef::getKey).collect(Collectors.toList());
	}

}
