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
package org.olat.resource.accesscontrol.provider.paypalcheckout.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The account settings, it needs to be a Paypal Business Account.
 * 
 * Initial date: 25 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutAccountConfigurationController extends FormBasicController {
	
	private TextElement clientIdEl;
	private TextElement clientSecretEl;

	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private PaypalCheckoutModule paypalModule;
	
	public PaypalCheckoutAccountConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("checkout.config.title");

		if(acModule.isPaypalEnabled()) {
			setFormDescription("checkout.config.description");
			setFormContextHelp("PayPal Configuration");
			
			String clientId = paypalModule.getClientId();
			clientIdEl = uifactory.addTextElement("checkout.client.id", 128, clientId, formLayout);
			String clientSecret = paypalModule.getClientSecret();
			clientSecretEl = uifactory.addTextElement("checkout.client.secret", 128, clientSecret, formLayout);

			final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonGroupLayout);
			
			uifactory.addFormSubmitButton("save", buttonGroupLayout);
		} else {
			String fxSupport = "contact@frentix.com";
			setFormWarning("config.disabled.warning", new String[]{fxSupport});
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		paypalModule.setClientId(clientIdEl.getValue());
		paypalModule.setClientSecret(clientSecretEl.getValue());
		showInfo("saved");
	}
}