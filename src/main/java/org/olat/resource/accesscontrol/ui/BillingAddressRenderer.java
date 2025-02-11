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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.BillingAddress;

/**
 * 
 * Initial date: Feb 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		BillingAddressComponent bac = (BillingAddressComponent)source;
		BillingAddress billingAddress = bac.getBillingAddress();
		if (billingAddress == null) {
			return;
		}
		
		sb.append("<div class=\"o_ac_billing_address");
		if (bac.isTemporaryAddressWarning() && billingAddress.getOrganisation() == null && billingAddress.getIdentity() == null) {
			sb.append(" o_ac_billing_address_temporary");
		}
		sb.append("\">");
		sb.append("<div class=\"o_ac_billing_address_inner\">");
		sb.append("<div class=\"o_ac_billing_address_icon\"><i class=\"o_icon o_icon_billing_address\"> </i></div>");
		
		sb.append("<div class=\"o_ac_billing_address_address_wrapper\">");
		
		String identifier = getIdentifier(bac, billingAddress);
		if (StringHelper.containsNonWhitespace(identifier)) {
			sb.append("<div class=\"o_ac_billing_address_identifier\">");
			sb.append(identifier);
			sb.append("</div>");
		}
		
		sb.append("<div class=\"o_ac_billing_address_address\">");
		sb.append(BillingAddressUIFactory.getFormattedAddress(billingAddress));
		sb.append("</div>");
		sb.append("</div>");
		
		sb.append("</div>"); // o_ac_billing_address_inner
		sb.append("</div>"); // o_ac_billing_address
	}

	private String getIdentifier(BillingAddressComponent bac, BillingAddress billingAddress) {
		if (bac.isTemporaryAddressWarning() && billingAddress.getOrganisation() == null && billingAddress.getIdentity() == null) {
			return "<i class=\"o_icon o_icon_important\"> </i> " + bac.getCompTranslator().translate("billing.address.proposal");
		} else if (StringHelper.containsNonWhitespace(billingAddress.getIdentifier())) {
			return StringHelper.escapeHtml(billingAddress.getIdentifier());
		}
		return null;
	}

}
