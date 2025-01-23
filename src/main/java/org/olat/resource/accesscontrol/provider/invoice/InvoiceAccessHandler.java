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

package org.olat.resource.accesscontrol.provider.invoice;

import java.util.Collection;
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
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.DefaultACSecurityCallback;
import org.olat.resource.accesscontrol.model.NotAvailableACSecurityCallback;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.provider.invoice.ui.InvoiceAccessConfigurationController;
import org.olat.resource.accesscontrol.provider.invoice.ui.InvoiceAccessController;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.FormController;

/**
 * Initial date: 4 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class InvoiceAccessHandler implements AccessMethodHandler {
	
	public static final String METHOD_TYPE = "invoice.method";
	public static final String METHOD_CSS_CLASS = "o_ac_invoice";
	private static final String ORES_TYPE_CURRICULUM_ELEMENT = OresHelper.calculateTypeName(CurriculumElement.class);
	
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
		Translator translator = Util.createPackageTranslator(InvoiceAccessController.class, locale);
		return translator.translate("invoice.method");
	}

	@Override
	public String getDescription(Locale locale) {
		Translator translator = Util.createPackageTranslator(InvoiceAccessController.class, locale);
		return translator.translate("invoice.method.desc");
	}
	
	@Override
	public AccessMethodSecurityCallback getSecurityCallback(OLATResource resource, Identity identity, Roles roles) {
		return ORES_TYPE_CURRICULUM_ELEMENT.equals(resource.getResourceableTypeName())
				? new DefaultACSecurityCallback(roles)
				: NotAvailableACSecurityCallback.get();
	}

	@Override
	public Controller createAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link, Identity identity) {
		return new InvoiceAccessController(ureq, wControl, link, identity);
	}

	@Override
	public AbstractConfigurationMethodController editConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			CatalogInfo catalogInfo) {
		return new InvoiceAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogInfo, true);
	}

	@Override
	public InvoiceAccessConfigurationController createConfigurationController(UserRequest ureq, WindowControl wControl,
			OfferAccess link, boolean offerOrganisationsSupported, Collection<Organisation> offerOrganisations,
			CatalogInfo catalogInfo) {
		return new InvoiceAccessConfigurationController(ureq, wControl, link, offerOrganisationsSupported,
				offerOrganisations, catalogInfo, false);
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
		return List.of();
	}
}