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
package org.olat.resource.accesscontrol.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferRef;
import org.olat.resource.accesscontrol.OfferSurveyParticipationIdentifiers;
import org.olat.resource.accesscontrol.OfferToSurvey;
import org.olat.resource.accesscontrol.model.OfferToSurveyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ACOfferSurveyDAO {

	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public EvaluationFormSurvey createSurvey(OLATResource resource, RepositoryEntry formEntry, String displayName) {
		EvaluationFormSurveyIdentifier identifier = EvaluationFormSurveyIdentifier.of(
				OfferSurveyParticipationIdentifiers.getSurveyOLATResourceable(resource));
		EvaluationFormSurvey survey = evaluationFormManager.createSurvey(identifier, formEntry);
		return evaluationFormManager.updateSurveyDisplayName(survey, displayName);
	}

	public List<EvaluationFormSurvey> loadSurveys(OLATResource resource) {
		EvaluationFormSurveyIdentifier identifier = EvaluationFormSurveyIdentifier.of(
				OfferSurveyParticipationIdentifiers.getSurveyOLATResourceable(resource));
		return evaluationFormManager.loadSurveys(identifier);
	}

	public void deleteSurvey(EvaluationFormSurvey survey) {
		for (OfferToSurvey offerToSurvey : loadOfferToSurveys(survey)) {
			deleteOfferToSurvey(offerToSurvey);
		}
		evaluationFormManager.deleteSurvey(survey);
	}

	public OfferToSurvey createOfferToSurvey(Offer offer, EvaluationFormSurvey survey, int pos) {
		OfferToSurveyImpl offerToSurvey = new OfferToSurveyImpl();
		offerToSurvey.setCreationDate(new Date());
		offerToSurvey.setLastModified(offerToSurvey.getCreationDate());
		offerToSurvey.setOffer(offer);
		offerToSurvey.setSurvey(survey);
		offerToSurvey.setPos(pos);
		dbInstance.getCurrentEntityManager().persist(offerToSurvey);
		return offerToSurvey;
	}

	public OfferToSurvey updateOfferToSurvey(OfferToSurvey offerToSurvey) {
		((OfferToSurveyImpl)offerToSurvey).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(offerToSurvey);
	}

	public void deleteOfferToSurvey(OfferToSurvey offerToSurvey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("delete from offertosurvey ots where ots.key=:key");
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("key", offerToSurvey.getKey())
				.executeUpdate();
	}

	public List<OfferToSurvey> loadOfferToSurveys(OfferRef offer) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ots from offertosurvey ots");
		sb.append(" inner join fetch ots.survey survey");
		sb.and().append("ots.offer.key=:offerKey");
		sb.append(" order by ots.pos asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OfferToSurvey.class)
				.setParameter("offerKey", offer.getKey())
				.getResultList();
	}

	public List<OfferToSurvey> loadOfferToSurveys(EvaluationFormSurvey survey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ots from offertosurvey ots");
		sb.append(" inner join fetch ots.offer offer");
		sb.and().append("ots.survey.key=:surveyKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OfferToSurvey.class)
				.setParameter("surveyKey", survey.getKey())
				.getResultList();
	}

	public boolean isSurveyUsed(EvaluationFormSurvey survey) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select ots.key from offertosurvey ots where ots.survey.key=:surveyKey");
		List<Long> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("surveyKey", survey.getKey())
				.setMaxResults(1)
				.getResultList();
		return !keys.isEmpty();
	}

}
