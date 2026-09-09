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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.model.jpa.EvaluationFormParticipationImpl;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OfferSurveyParticipationIdentifiers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Enforces the invariant that per survey and booking order there is at most
 * one participation that is not canceled. EvaluationFormManager stays
 * unaware of it, the rule belongs to the offer context.
 *
 * Initial date: 1 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ACOfferSurveyParticipationDAO {

	@Autowired
	private DB dbInstance;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public EvaluationFormParticipation createParticipation(EvaluationFormSurvey survey, Order order, Identity executor) {
		Optional<EvaluationFormParticipation> activeParticipation = loadParticipations(survey, order).stream()
				.filter(participation -> participation.getStatus() != EvaluationFormParticipationStatus.canceled)
				.findFirst();
		if (activeParticipation.isPresent()) {
			return activeParticipation.get();
		}
		EvaluationFormParticipationIdentifier identifier = OfferSurveyParticipationIdentifiers.of(order);
		int run = evaluationFormManager.loadParticipationsByExecutor(survey, executor).size() + 1;
		return evaluationFormManager.createParticipation(survey, identifier, executor, run);
	}

	public List<EvaluationFormParticipation> createParticipations(EvaluationFormSurvey survey, List<Order> orders) {
		Set<Long> orderKeysWithActiveParticipation = loadParticipations(survey).stream()
				.filter(participation -> participation.getStatus() != EvaluationFormParticipationStatus.canceled)
				.map(participation -> OfferSurveyParticipationIdentifiers.getOrderKey(participation.getIdentifier()))
				.collect(Collectors.toSet());
		List<EvaluationFormParticipation> created = new ArrayList<>();
		int count = 0;
		for (Order order : orders) {
			if (orderKeysWithActiveParticipation.add(order.getKey())) {
				EvaluationFormParticipationIdentifier identifier = OfferSurveyParticipationIdentifiers.of(order);
				Identity executor = order.getDelivery();
				int run = evaluationFormManager.loadParticipationsByExecutor(survey, executor).size() + 1;
				created.add(evaluationFormManager.createParticipation(survey, identifier, executor, run));
				if (++count % 20 == 0) {
					dbInstance.commit();
				}
			}
		}
		return created;
	}

	public EvaluationFormParticipation cancelParticipation(EvaluationFormParticipation participation) {
		if (participation instanceof EvaluationFormParticipationImpl impl) {
			impl.setLastRun(false);
			evaluationFormManager.updateParticipation(impl);
			return evaluationFormManager.cancelParticipation(impl);
		}
		return participation;
	}

	public boolean hasActiveParticipation(EvaluationFormSurvey survey, Order order) {
		return loadParticipations(survey, order).stream()
				.anyMatch(participation -> participation.getStatus() != EvaluationFormParticipationStatus.canceled);
	}

	public List<EvaluationFormParticipation> loadParticipations(EvaluationFormSurvey survey, Order order) {
		Long orderKey = order.getKey();
		return loadParticipations(survey).stream()
				.filter(participation -> orderKey.equals(OfferSurveyParticipationIdentifiers.getOrderKey(participation.getIdentifier())))
				.toList();
	}

	public List<EvaluationFormParticipation> loadParticipations(EvaluationFormSurvey survey) {
		return evaluationFormManager.loadParticipations(survey, null, false, true).stream()
				.filter(participation -> OfferSurveyParticipationIdentifiers.isOfferSurveyParticipation(participation.getIdentifier()))
				.toList();
	}

}
