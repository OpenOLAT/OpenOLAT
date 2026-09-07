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
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.Order;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 *
 * Initial date: 4 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyParticipationRow extends UserPropertiesRow implements Persistable {

	private static final long serialVersionUID = 1L;

	private final EvaluationFormParticipation participation;
	private final Order order;
	private final Offer offer;
	private final String offerLabel;
	private final EvaluationFormSession session;

	private FormLink viewLink;
	private FormLink toolsLink;

	public OfferSurveyParticipationRow(EvaluationFormParticipation participation, Order order, Offer offer,
			String offerLabel, EvaluationFormSession session, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(participation.getExecutor(), userPropertyHandlers, locale);
		this.participation = participation;
		this.order = order;
		this.offer = offer;
		this.offerLabel = offerLabel;
		this.session = session;
	}

	@Override
	public Long getKey() {
		return participation.getKey();
	}

	public EvaluationFormParticipation getParticipation() {
		return participation;
	}

	public Identity getExecutor() {
		return participation.getExecutor();
	}

	public Order getOrder() {
		return order;
	}

	public String getOrderNr() {
		return order != null ? order.getOrderNr() : null;
	}

	public Offer getOffer() {
		return offer;
	}

	public String getOfferLabel() {
		return offerLabel;
	}

	public EvaluationFormSession getSession() {
		return session;
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
		if (obj instanceof OfferSurveyParticipationRow row) {
			return getKey() != null && getKey().equals(row.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
