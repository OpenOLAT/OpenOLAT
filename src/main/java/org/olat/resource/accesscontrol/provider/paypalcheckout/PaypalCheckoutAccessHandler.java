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
package org.olat.resource.accesscontrol.provider.paypalcheckout;

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
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.AuthorACSecurityCallback;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.paypalcheckout.ui.PaypalCheckoutAccessConfigurationController;
import org.olat.resource.accesscontrol.provider.paypalcheckout.ui.PaypalCheckoutAccessController;
import org.olat.resource.accesscontrol.provider.paypalcheckout.ui.PaypalCheckoutTransactionDetailsController;
import org.olat.resource.accesscontrol.provider.paypalcheckout.ui.PaypalSmartButtonAccessController;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.FormController;

/**
 * 
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutAccessHandler implements AccessMethodHandler {
	
	public static final String METHOD_TYPE = "checkout.method";
	public static final String METHOD_CSS_CLASS = "o_ac_paypal";

	@Override
	public boolean isPaymentMethod() {
		return true;
	}
	
	@Override
	public boolean isOverlapAllowed(AccessMethodHandler handler) {
		if(handler instanceof PaypalCheckoutAccessHandler) {
			PaypalCheckoutModule paypalModule = CoreSpringFactory.getImpl(PaypalCheckoutModule.class);
			return !paypalModule.isSmartButtons();
		}
		return true;
	}

	@Override
	public String getType() {
		return METHOD_TYPE;
	}

	@Override
	public String getMethodName(Locale locale) {
		Translator translator = Util.createPackageTranslator(PaypalCheckoutAccessController.class, locale);
		return translator.translate("paypal.checkout.method");
	}
	
	@Override
	public AccessMethodSecurityCallback getSecurityCallback(Identity identity, Roles roles) {
		return new AuthorACSecurityCallback(roles);
	}

	@Override
	public Controller createAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		PaypalCheckoutModule paypalModule = CoreSpringFactory.getImpl(PaypalCheckoutModule.class);
		if(paypalModule.isSmartButtons()) {
			return new PaypalSmartButtonAccessController(ureq, wControl, link);
		}
		return new PaypalCheckoutAccessController(ureq, wControl, link);
	}

	@Override
	public AbstractConfigurationMethodController editConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			CatalogInfo catalogInfo) {
		return new PaypalCheckoutAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogInfo, true);
	}

	@Override
	public AbstractConfigurationMethodController createConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			CatalogInfo catalogInfo) {
		return new PaypalCheckoutAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogInfo, false);
	}
	
	@Override
	public FormController createTransactionDetailsController(UserRequest ureq, WindowControl wControl, Order order, OrderPart part, AccessMethod method, Form form) {
		PaypalCheckoutManager paypalManager = CoreSpringFactory.getImpl(PaypalCheckoutManager.class);
		PaypalCheckoutTransaction transaction = paypalManager.loadTransaction(order, part);
		if(transaction == null) {
			return null;
		}
		return new PaypalCheckoutTransactionDetailsController(ureq, wControl, transaction, form);
	}
	

	@Override
	public boolean checkArgument(OfferAccess link, Object argument) {
		return true;
	}

	@Override
	public List<PSPTransaction> getPSPTransactions(List<Order> orders) {
		PaypalCheckoutManager paypalManager = CoreSpringFactory.getImpl(PaypalCheckoutManager.class);
		return paypalManager.loadTransactions(orders);
	}
}