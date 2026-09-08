/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.manager.EvaluationFormExportResource.SessionPrintProvider;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferSurveyParticipationIdentifiers;
import org.olat.resource.accesscontrol.OfferToSurvey;
import org.olat.resource.accesscontrol.Order;

/**
 * Creates the printable view of an offer survey session. The offers of the
 * survey are loaded once, the order of a session on demand.
 *
 * Initial date: 8 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyPrintProvider implements SessionPrintProvider {

	private final EvaluationFormSurvey survey;
	private final Translator translator;
	private final ACService acService;
	private final Map<Long, Offer> offerByKey = new HashMap<>();
	private final Map<Long, Order> orderByKey = new HashMap<>();
	
	public OfferSurveyPrintProvider(EvaluationFormSurvey survey, Translator translator) {
		this.survey = survey;
		this.translator = translator;
		this.acService = CoreSpringFactory.getImpl(ACService.class);
		
		for (OfferToSurvey offerToSurvey : acService.loadOfferToSurveys(survey)) {
			offerByKey.put(offerToSurvey.getOffer().getKey(), offerToSurvey.getOffer());
		}
	}
	
	@Override
	public ControllerCreator create(EvaluationFormSession session) {
		EvaluationFormParticipation participation = session.getParticipation();
		if (participation == null || participation.getExecutor() == null) {
			return null;
		}
		
		Order order = loadOrder(participation);
		Offer offer = resolveOffer(order);
		String offerLabel = offer != null ? OfferSurveyUIFactory.getOfferLabel(offer, translator) : null;
		String orderNr = order != null ? order.getOrderNr() : null;
		
		return (lureq, lwControl) -> new OfferSurveyExecutionDetailController(lureq, lwControl, session, survey,
				offerLabel, orderNr, session.getSubmissionDate(), participation.getStatus(),
				participation.getExecutor(), true, false, false, false);
	}
	
	private Order loadOrder(EvaluationFormParticipation participation) {
		Long orderKey = OfferSurveyParticipationIdentifiers.getOrderKey(participation.getIdentifier());
		return orderByKey.computeIfAbsent(orderKey, key -> acService.loadOrderByKey(key));
	}
	
	private Offer resolveOffer(Order order) {
		if (order == null) {
			return null;
		}
		return order.getParts().stream()
				.flatMap(part -> part.getOrderLines().stream())
				.map(line -> offerByKey.get(line.getOffer().getKey()))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

}
