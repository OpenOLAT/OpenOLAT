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
package org.olat.resource.accesscontrol.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Persistable;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.Order;

/**
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OrderFormRow implements Persistable {

	private static final long serialVersionUID = 1L;

	private final EvaluationFormSurvey survey;
	private final EvaluationFormParticipation participation;
	private final Order order;
	private final Offer offer;
	private final String offerLabel;
	private EvaluationFormSession session;

	private FormLink viewLink;
	private FormLink toolsLink;

	public OrderFormRow(EvaluationFormSurvey survey, EvaluationFormParticipation participation, Order order,
			Offer offer, String offerLabel) {
		this.survey = survey;
		this.participation = participation;
		this.order = order;
		this.offer = offer;
		this.offerLabel = offerLabel;
	}

	@Override
	public Long getKey() {
		return participation.getKey();
	}

	public EvaluationFormSurvey getSurvey() {
		return survey;
	}

	public EvaluationFormParticipation getParticipation() {
		return participation;
	}

	public Order getOrder() {
		return order;
	}

	public Offer getOffer() {
		return offer;
	}

	public String getOfferLabel() {
		return offerLabel;
	}

	public String getOrderNr() {
		return order != null ? order.getOrderNr() : null;
	}

	public EvaluationFormSession getSession() {
		return session;
	}

	public void setSession(EvaluationFormSession session) {
		this.session = session;
	}

	public String getTitle() {
		return survey.getFormEntry().getDisplayname();
	}

	public String getReference() {
		return survey.getFormEntry().getExternalRef();
	}

	public String getStepName() {
		return survey.getDisplayName();
	}

	public EvaluationFormParticipationStatus getStatus() {
		return participation.getStatus();
	}

	public boolean isEditable() {
		EvaluationFormParticipationStatus status = getStatus();
		return status == EvaluationFormParticipationStatus.prepared || status == EvaluationFormParticipationStatus.done;
	}

	public Date getSubmissionDate() {
		return session != null ? session.getSubmissionDate() : null;
	}

	public FormLink getViewLink() {
		return viewLink;
	}

	public void setViewLink(FormLink viewLink) {
		this.viewLink = viewLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 1 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof OrderFormRow row) {
			return getKey() != null && getKey().equals(row.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
