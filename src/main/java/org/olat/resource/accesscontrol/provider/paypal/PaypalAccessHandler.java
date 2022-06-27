/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.resource.accesscontrol.provider.paypal;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.AuthorACSecurityCallback;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.paypal.manager.PaypalManager;
import org.olat.resource.accesscontrol.provider.paypal.model.PaypalTransaction;
import org.olat.resource.accesscontrol.provider.paypal.ui.PaypalAccessConfigurationController;
import org.olat.resource.accesscontrol.provider.paypal.ui.PaypalAccessController;
import org.olat.resource.accesscontrol.provider.paypal.ui.PaypalSubmitController;
import org.olat.resource.accesscontrol.provider.paypal.ui.PaypalTransactionDetailsController;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.FormController;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  26 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PaypalAccessHandler implements AccessMethodHandler {
	
	public static final String METHOD_TYPE = "paypal.method";
	public static final String METHOD_CSS_CLASS = "o_ac_paypal";

	@Override
	public boolean isPaymentMethod() {
		return true;
	}
	
	@Override
	public boolean isOverlapAllowed(AccessMethodHandler handler) {
		return true;
	}

	@Override
	public String getType() {
		return METHOD_TYPE;
	}

	@Override
	public String getMethodName(Locale locale) {
		Translator translator = Util.createPackageTranslator(PaypalSubmitController.class, locale);
		return translator.translate("paypal.method");
	}
	
	@Override
	public AccessMethodSecurityCallback getSecurityCallback(Identity identity, Roles roles) {
		return new AuthorACSecurityCallback(roles);
	}

	@Override
	public Controller createAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		return new PaypalAccessController(ureq, wControl, link);
	}

	@Override
	public AbstractConfigurationMethodController editConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean catalogSupported) {
		return new PaypalAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogSupported, true);
	}

	@Override
	public AbstractConfigurationMethodController createConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean catalogSupported) {
		return new PaypalAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogSupported, false);
	}
	
	@Override
	public FormController createTransactionDetailsController(UserRequest ureq, WindowControl wControl, Order order, OrderPart part, AccessMethod method, Form form) {
		PaypalManager paypalManager = CoreSpringFactory.getImpl(PaypalManager.class);
		PaypalTransaction transaction = paypalManager.loadTransaction(order, part);
		return new PaypalTransactionDetailsController(ureq, wControl, transaction, form);
	}
	

	@Override
	public boolean checkArgument(OfferAccess link, Object argument) {
		return true;
	}

	@Override
	public List<PSPTransaction> getPSPTransactions(List<Order> orders) {
		PaypalManager paypalManager = CoreSpringFactory.getImpl(PaypalManager.class);
		List<PSPTransaction> transactions = paypalManager.loadTransactions(orders);
		return transactions;
	}
}