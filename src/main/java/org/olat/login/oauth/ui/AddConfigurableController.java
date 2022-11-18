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

import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
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
 * Initial date: 17 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddConfigurableController extends FormBasicController {

	private TextElement nameEl;
	private TextElement displayNameEl;
	private TextElement apiKeyEl;
	private TextElement apiSecretEl;
	
	private SingleSelection responseTypeEl;
	private TextElement scopesEl;
	
	private TextElement issuerEl;
	private TextElement authorizationEndPointEl;
	private TextElement tokenEndPointEl;
	private TextElement userInfoEndPointEl;
	
	private final SelectionValues responseTypes;

	@Autowired
	private OAuthLoginModule oauthModule;

	public AddConfigurableController(UserRequest ureq, WindowControl wControl, JSONObject configuration) {
		super(ureq, wControl);
		
		responseTypes = new SelectionValues();
		responseTypes.add(SelectionValues.entry("code", "code"));
		responseTypes.add(SelectionValues.entry("id_token", "id_token"));
		responseTypes.add(SelectionValues.entry("id_token token", "id_token token"));
		
		initForm(ureq);
		
		issuerEl.setValue(getValue(configuration, "issuer"));
		authorizationEndPointEl.setValue(getValue(configuration, "authorization_endpoint"));
		tokenEndPointEl.setValue(getValue(configuration, "token_endpoint"));
		userInfoEndPointEl.setValue(getValue(configuration, "userinfo_endpoint"));
		
		List<String> possibleResponseTypes = getArray(configuration, "response_types_supported");
		if(!possibleResponseTypes.isEmpty()) {
			for(String responseType:possibleResponseTypes) {
				if(responseTypeEl.containsKey(responseType)) {
					responseTypeEl.select(responseType, true);
					break;
				}
			}
		}
		
		List<String> possibleScopes = getArray(configuration, "scopes_supported");
		if(!possibleScopes.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for(String possibleScope:possibleScopes) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(possibleScope);
			}
			scopesEl.setValue(sb.toString());
		}
	}
	
	private String getValue(JSONObject configuration, String attribute) {
		return configuration == null ? null : configuration.optString(attribute);
	}
	
	private List<String> getArray(JSONObject configuration, String attribute) {
		if(configuration == null) return List.of();
		
		JSONArray arr = configuration.optJSONArray(attribute);
		List<String> values = new ArrayList<>();
		if(arr != null && arr.length() > 0) {
			for(int i=0; i<arr.length(); i++) {
				values.add(arr.getString(i));
			}
		}
		return values;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		nameEl = uifactory.addTextElement("openidconnectif.name", "openidconnectif.name", 256, "", formLayout);
		nameEl.setMandatory(true);
		nameEl.setMaxLength(8);
		nameEl.setRegexMatchCheck("[a-zA-Z0-9._-]{3,8}", "openidconnectif.name.error");
		
		displayNameEl = uifactory.addTextElement("openidconnectif.displayname", "openidconnectif.displayname", 256, "", formLayout);
		displayNameEl.setMandatory(true);

		apiKeyEl = uifactory.addTextElement("openidconnectif.id", "openidconnectif.api.id", 256, "", formLayout);
		apiKeyEl.setMandatory(true);
		apiSecretEl = uifactory.addTextElement("openidconnectif.secret", "openidconnectif.api.secret", 256, "", formLayout);
		apiSecretEl.setMandatory(true);
		
		responseTypeEl = uifactory.addDropdownSingleselect("response.type", formLayout, responseTypes.keys(), responseTypes.values());
		responseTypeEl.setMandatory(true);
		
		scopesEl = uifactory.addTextElement("scopes", "scopes", 64, null, formLayout);
		
		issuerEl = uifactory.addTextElement("openidconnectif.issuer", "openidconnectif.issuer", 256, "", formLayout);
		issuerEl.setExampleKey("openidconnectif.issuer.example", null);
		issuerEl.setMandatory(true);

		authorizationEndPointEl = uifactory.addTextElement("openidconnectif.authorization.endpoint", "openidconnectif.authorization.endpoint", 256, "", formLayout);
		authorizationEndPointEl.setExampleKey("openidconnectif.authorization.endpoint.example", null);
		authorizationEndPointEl.setMandatory(true);
		
		tokenEndPointEl = uifactory.addTextElement("token.endpoint", "token.endpoint", 256, "", formLayout);
		tokenEndPointEl.setExampleKey("token.endpoint.example", null);
		tokenEndPointEl.setMandatory(true);
		
		userInfoEndPointEl = uifactory.addTextElement("userinfo.endpoint", "userinfo.endpoint", 256, "", formLayout);
		userInfoEndPointEl.setExampleKey("userinfo.endpoint.example", null);
	
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validate(nameEl);
		List<ValidationStatus> nameValidation = new ArrayList<>();
		nameEl.validate(nameValidation);
		allOk &= nameValidation.isEmpty();  
		allOk &= validate(displayNameEl);
		allOk &= validate(apiKeyEl);
		allOk &= validate(apiSecretEl);
		allOk &= validate(issuerEl);
		allOk &= validate(authorizationEndPointEl);
		allOk &= validate(tokenEndPointEl);
		
		String providerName = nameEl.getValue();
		if(StringHelper.containsNonWhitespace(providerName)) {
			OAuthSPI existingSpi = oauthModule.getProvider(providerName);
			if(existingSpi != null) {
				nameEl.setErrorKey("error.duplicate.provider", null);
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
		String providerName = nameEl.getValue();
		String displayName = displayNameEl.getValue();
		String apiKey = apiKeyEl.getValue();
		String apiSecret = apiSecretEl.getValue();
		String issuer = issuerEl.getValue();
		String responseType = responseTypeEl.getSelectedKey();
		String scopes = scopesEl.getValue();
		String authorizationEndPoint = authorizationEndPointEl.getValue();
		String tokenEndPoint = tokenEndPointEl.getValue();
		String userInfoEndPoint = userInfoEndPointEl.getValue();
		oauthModule.setGenericOAuth(providerName, displayName, false, issuer,
				authorizationEndPoint, tokenEndPoint, userInfoEndPoint,
				responseType, scopes, apiKey, apiSecret);
	
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
