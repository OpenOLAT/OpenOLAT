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

import javax.persistence.TypedQuery;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
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
	
	EvaluationFormParticipation loadByExecutor(EvaluationFormSurvey survey, IdentityRef executor) {
		if (survey == null || executor == null) return null;
		
		StringBuilder query = new StringBuilder();
		query.append("select participation from evaluationformparticipation as participation");
		query.append(" inner join participation.executor executor");
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

	EvaluationFormParticipation loadByIdentifier(EvaluationFormSurvey survey,
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

}
