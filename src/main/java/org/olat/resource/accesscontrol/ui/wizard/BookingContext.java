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
package org.olat.resource.accesscontrol.ui.wizard;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.OfferToSurvey;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderStatus;

/**
 * Booking data the wizard steps and the finish callback need to share. The
 * form and invoice-details step controllers are kept alive across back and
 * forward navigation separately, via {@link org.olat.core.gui.control.generic.wizard.CachedRunContextController}.
 *
 * Initial date: 2 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BookingContext {

	public static final String RUN_CONTEXT_KEY = "bookingContext";

	private final OfferAccess offerAccess;
	private final Identity bookedIdentity;
	private final Identity doer;
	private final OrderStatus orderStatus;
	private final List<OfferToSurvey> offerToSurveys;

	private Object argument;
	private Order order;
	private boolean accessible;
	private BillingAddress billingAddress;
	private String purchaseOrderNumber;
	private String comment;

	public BookingContext(OfferAccess offerAccess, Identity bookedIdentity, Identity doer, OrderStatus orderStatus,
			List<OfferToSurvey> offerToSurveys) {
		this.offerAccess = offerAccess;
		this.bookedIdentity = bookedIdentity;
		this.doer = doer;
		this.orderStatus = orderStatus;
		this.offerToSurveys = offerToSurveys;
	}

	public OfferAccess getOfferAccess() {
		return offerAccess;
	}

	public Identity getBookedIdentity() {
		return bookedIdentity;
	}

	public Identity getDoer() {
		return doer;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public List<OfferToSurvey> getOfferToSurveys() {
		return offerToSurveys;
	}

	public Object getArgument() {
		return argument;
	}

	public void setArgument(Object argument) {
		this.argument = argument;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public boolean isAccessible() {
		return accessible;
	}

	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	public BillingAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}

	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * The survey is reloaded so its lazy associations (the form's repository
	 * entry) are bound to the current request's Hibernate session, since the
	 * wizard's steps and finish callback each run in a separate request from
	 * the one that first loaded {@link #getOfferToSurveys()}.
	 */
	public EvaluationFormSurvey reload(EvaluationFormSurvey survey) {
		return CoreSpringFactory.getImpl(EvaluationFormManager.class).loadSurveyByKey(survey.getKey());
	}

}
