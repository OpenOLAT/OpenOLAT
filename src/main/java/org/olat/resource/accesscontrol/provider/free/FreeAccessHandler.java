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

package org.olat.resource.accesscontrol.provider.free;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
import org.olat.resource.accesscontrol.model.DefaultACSecurityCallback;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.free.ui.FreeAccessConfigurationController;
import org.olat.resource.accesscontrol.provider.free.ui.FreeAccessController;
import org.olat.resource.accesscontrol.provider.free.ui.FreeSubmitController;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.FormController;

/**
 * 
 * Description:<br>
 * Implementation of the access handler for the free method
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FreeAccessHandler implements AccessMethodHandler {
	
	public static final String METHOD_TYPE = "free.method";
	public static final String METHOD_CSS_CLASS = "o_ac_free";

	@Override
	public boolean isPaymentMethod() {
		return false;
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
		Translator translator = Util.createPackageTranslator(FreeSubmitController.class, locale);
		return translator.translate("free.method");
	}
	
	@Override
	public AccessMethodSecurityCallback getSecurityCallback(Identity identity, Roles roles) {
		return new DefaultACSecurityCallback(roles);
	}

	@Override
	public Controller createAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		return new FreeAccessController(ureq, wControl, link);
	}

	@Override
	public AbstractConfigurationMethodController editConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean catalogSupported) {
		return new FreeAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogSupported, true);
	}

	@Override
	public AbstractConfigurationMethodController createConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			boolean catalogSupported) {
		return new FreeAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogSupported, false);
	}

	@Override
	public FormController createTransactionDetailsController(UserRequest ureq, WindowControl wControl, Order order, OrderPart part, AccessMethod method, Form form) {
		return null;
	}

	@Override
	public boolean checkArgument(OfferAccess link, Object argument) {
		return true;
	}

	@Override
	public List<PSPTransaction> getPSPTransactions(List<Order> orders) {
		return Collections.emptyList();
	}
}
