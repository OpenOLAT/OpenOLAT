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
package org.olat.login.oauth.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddOpenIDConnectIFFullConfigurableController extends FormBasicController {

	private static final String[] keys = new String[]{ "on" };
	private static final String[] values = new String[] { "" };

	private TextElement openIdConnectIFName;
	private TextElement openIdConnectIFDisplayName;
	private TextElement openIdConnectIFApiKeyEl;
	private TextElement openIdConnectIFApiSecretEl;
	private TextElement openIdConnectIFIssuerEl;
	private TextElement openIdConnectIFAuthorizationEndPointEl;
	
	private MultipleSelectionElement openIdConnectIFDefaultEl;

	@Autowired
	private OAuthLoginModule oauthModule;
	
	public AddOpenIDConnectIFFullConfigurableController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		openIdConnectIFDefaultEl = uifactory.addCheckboxesHorizontal("openidconnectif.default.enabled", formLayout, keys, values);
		openIdConnectIFDefaultEl.addActionListener(FormEvent.ONCHANGE);
		
		openIdConnectIFName = uifactory.addTextElement("openidconnectif.name", "openidconnectif.name", 256, "", formLayout);
		openIdConnectIFName.setMaxLength(8);
		openIdConnectIFName.setRegexMatchCheck("[a-zA-Z0-9._-]{3,8}", "openidconnectif.name.error");
		openIdConnectIFDisplayName = uifactory.addTextElement("openidconnectif.displayname", "openidconnectif.displayname", 256, "", formLayout);

		openIdConnectIFApiKeyEl = uifactory.addTextElement("openidconnectif.id", "openidconnectif.api.id", 256, "", formLayout);
		openIdConnectIFApiSecretEl = uifactory.addTextElement("openidconnectif.secret", "openidconnectif.api.secret", 256, "", formLayout);
		
		openIdConnectIFIssuerEl = uifactory.addTextElement("openidconnectif.issuer", "openidconnectif.issuer", 256, "", formLayout);
		openIdConnectIFIssuerEl.setExampleKey("openidconnectif.issuer.example", null);

		openIdConnectIFAuthorizationEndPointEl = uifactory.addTextElement("openidconnectif.authorization.endpoint", "openidconnectif.authorization.endpoint", 256, "", formLayout);
		openIdConnectIFAuthorizationEndPointEl.setExampleKey("openidconnectif.authorization.endpoint.example", null);
	
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validate(openIdConnectIFName);
		List<ValidationStatus> nameValidation = new ArrayList<>();
		openIdConnectIFName.validate(nameValidation);
		allOk &= nameValidation.isEmpty();  
		allOk &= validate(openIdConnectIFDisplayName);
		allOk &= validate(openIdConnectIFApiKeyEl);
		allOk &= validate(openIdConnectIFApiSecretEl);
		allOk &= validate(openIdConnectIFIssuerEl);
		allOk &= validate(openIdConnectIFAuthorizationEndPointEl);
		
		String providerName = openIdConnectIFName.getValue();
		if(StringHelper.containsNonWhitespace(providerName)) {
			OAuthSPI existingSpi = oauthModule.getProvider(providerName);
			if(existingSpi != null) {
				openIdConnectIFName.setErrorKey("error.duplicate.provider", null);
				allOk &= false;
			}
		}

		return allOk;
	}
	
	private boolean validate(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(!StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String providerName = openIdConnectIFName.getValue();
		String displayName = openIdConnectIFDisplayName.getValue();
		String issuer = openIdConnectIFIssuerEl.getValue();
		String endPoint = openIdConnectIFAuthorizationEndPointEl.getValue();
		String apiKey = openIdConnectIFApiKeyEl.getValue();
		String apiSecret = openIdConnectIFApiSecretEl.getValue();
		boolean rootEnabled = openIdConnectIFDefaultEl.isAtLeastSelected(1);
		oauthModule.setAdditionalOpenIDConnectIF(providerName, displayName, rootEnabled, issuer, endPoint, apiKey, apiSecret);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
