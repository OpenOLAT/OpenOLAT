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

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.BillingAddress;

/**
 * 
 * Initial date: Feb 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressUIFactory {
	
	public static String getFormattedAddress(BillingAddress billingAddress) {
		StringBuilder description = new StringBuilder();
		appendAddressLine(description, billingAddress.getNameLine1());
		appendAddressLine(description, billingAddress.getNameLine2());
		appendAddressLine(description, billingAddress.getAddressLine1());
		appendAddressLine(description, billingAddress.getAddressLine2());
		appendAddressLine(description, billingAddress.getAddressLine3());
		appendAddressLine(description, billingAddress.getAddressLine4());
		appendAddressLine(description, billingAddress.getPoBox());
		appendAddressLine(description, billingAddress.getRegion());
		appendAddressLine(description, getCityLine(billingAddress));
		appendAddressLine(description, billingAddress.getCountry());
		return description.toString();
	}

	private static void appendAddressLine(StringBuilder description, String addressPart) {
		if (StringHelper.containsNonWhitespace(addressPart)) {
			description.append("<div>").append(StringHelper.escapeHtml(addressPart)).append("</div>");
		}
	}
	
	private static String getCityLine(BillingAddress billingAddress) {
		String line = null;
		if (StringHelper.containsNonWhitespace(billingAddress.getZip())) {
			line = billingAddress.getZip();
		}
		if (StringHelper.containsNonWhitespace(billingAddress.getCity())) {
			if (line != null) {
				line += " " + billingAddress.getCity();
			} else {
				line = billingAddress.getCity();
			}
		}
		return line;
	}
	
	public static String getEnabledLabel(Translator translator, Boolean enabled) {
		if (enabled != null) {
			return enabled
					? translator.translate("billing.address.status.active")
					: translator.translate("billing.address.status.inactive");
		}
		return null;
	}
	
	public static String getEnabledIconCss(Boolean enabled) {
		if (enabled != null) {
			return enabled? "o_ac_billing_address_active_icon": "o_ac_billing_address_inactive_icon";
		}
		return null;
	}
	
	public static String getEnabledLabelCss(Boolean enabled) {
		if (enabled != null) {
			return enabled? "o_ac_billing_address_active": "o_ac_billing_address_inactive";
		}
		return null;
	}
	
	public static String getEnabledLabelLightCss(Boolean enabled) {
		if (enabled != null) {
			return enabled? "o_ac_billing_address_active_light": "o_ac_billing_address_inactive_light";
		}
		return null;
	}

}
