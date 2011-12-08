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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.method;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.AccessMethodSecurityCallback;
import org.olat.resource.accesscontrol.model.DefaultACSecurityCallback;
import org.olat.resource.accesscontrol.model.Offer;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.olat.resource.accesscontrol.model.OfferAccess;
import org.olat.resource.accesscontrol.model.Order;
import org.olat.resource.accesscontrol.model.OrderPart;
import org.olat.resource.accesscontrol.model.PSPTransaction;
import org.olat.resource.accesscontrol.ui.FormController;
import org.olat.resource.accesscontrol.ui.TokenAccessConfigurationController;
import org.olat.resource.accesscontrol.ui.TokenAccessController;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for TokenAccessHandler
 * 
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TokenAccessHandler implements AccessMethodHandler {
	
	public static final String METHOD_TYPE = "token.method";
	public static final String METHOD_CSS_CLASS = "b_access_method_token";

	@Override
	public String getType() {
		return METHOD_TYPE;
	}

	@Override
	public String getMethodName(Locale locale) {
		Translator translator = Util.createPackageTranslator(TokenAccessController.class, locale);
		return translator.translate("token.method");
	}
	
	@Override
	public AccessMethodSecurityCallback getSecurityCallback(Identity identity, Roles roles) {
		return new DefaultACSecurityCallback(roles);
	}

	@Override
	public TokenAccessController createAccessController(UserRequest ureq, WindowControl wControl, OfferAccess link, Form form) {
		if(form == null) {
			return new TokenAccessController(ureq, wControl, link);
		} else {
			return new TokenAccessController(ureq, wControl, link, form);
		}
		
	}

	@Override
	public TokenAccessConfigurationController createConfigurationController(UserRequest ureq, WindowControl wControl, OfferAccess link) {
		return new TokenAccessConfigurationController(ureq, wControl, link);
	}
	
	@Override
	public FormController createTransactionDetailsController(UserRequest ureq, WindowControl wControl, Order order, OrderPart part, AccessMethod method, Form form) {
		return null;
	}

	@Override
	public boolean checkArgument(OfferAccess link, Object argument) {
		if(argument instanceof String && StringHelper.containsNonWhitespace((String)argument)) {
			Offer offer = link.getOffer();
			if(offer instanceof OfferImpl && argument.equals(((OfferImpl)offer).getToken())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<PSPTransaction> getPSPTransactions(List<Order> orders) {
		return Collections.emptyList();
	}
}