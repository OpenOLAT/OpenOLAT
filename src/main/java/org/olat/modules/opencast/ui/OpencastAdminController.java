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
package org.olat.modules.opencast.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.opencast.OpencastModule;
import org.olat.modules.opencast.OpencastService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpencastAdminController extends FormBasicController {
	
	private static final String[] ENABLED_KEYS = new String[]{ "on" };
	
	private MultipleSelectionElement enabledEl;
	private TextElement apiUrlEl;
	private TextElement apiUsernameEl;
	private TextElement apiPasswordEl;
	private TextElement ltiUrlEl;
	private TextElement ltiKeyEl;
	private TextElement ltiSectretEl;
	private FormLink checkApiConnectionButton;
	
	@Autowired
	private OpencastModule opencastModule;
	@Autowired
	private OpencastService opencastService;

	public OpencastAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		
		String[] enableValues = new String[]{ translate("on") };
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, ENABLED_KEYS, enableValues);
		enabledEl.select(ENABLED_KEYS[0], opencastModule.isEnabled());
		
		String apiUrl = opencastModule.getApiUrl();
		apiUrlEl = uifactory.addTextElement("admin.api.url", "admin.api.url", 128, apiUrl, formLayout);
		apiUrlEl.setMandatory(true);
		
		String apiUsername = opencastModule.getApiUsername();
		apiUsernameEl = uifactory.addTextElement("admin.api.username", 128, apiUsername, formLayout);
		apiUsernameEl.setMandatory(true);
		
		String apiPassword = opencastModule.getApiPassword();
		apiPasswordEl = uifactory.addPasswordElement("admin.api.password", "admin.api.password", 128, apiPassword, formLayout);
		apiPasswordEl.setAutocomplete("new-password");
		apiPasswordEl.setMandatory(true);
		
		String ltiUrl = opencastModule.getApiUrl();
		ltiUrlEl = uifactory.addTextElement("admin.lti.url", "admin.lti.url", 128, ltiUrl, formLayout);
		ltiUrlEl.setMandatory(true);
		
		String ltiKey = opencastModule.getLtiKey();
		ltiKeyEl = uifactory.addTextElement("admin.lti.key", 123, ltiKey, formLayout);
		ltiKeyEl.setMandatory(true);
		
		String ltiSecret = opencastModule.getLtiSecret();
		ltiSectretEl = uifactory.addPasswordElement("admin.lti.secret", "admin.lti.secret", 128, ltiSecret, formLayout);
		ltiSectretEl.setAutocomplete("new-password");
		ltiSectretEl.setMandatory(true);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		checkApiConnectionButton = uifactory.addFormLink("admin.check.api.connection", buttonLayout, Link.BUTTON);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		//validate only if the module is enabled
		if(enabledEl.isAtLeastSelected(1)) {
			allOk &= validateIsMandatory(apiUrlEl);
			allOk &= validateIsMandatory(apiUsernameEl);
			allOk &= validateIsMandatory(apiPasswordEl);
			allOk &= validateIsMandatory(ltiUrlEl);
			allOk &= validateIsMandatory(ltiKeyEl);
			allOk &= validateIsMandatory(ltiSectretEl);
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	private boolean validateIsMandatory(TextElement textElement) {
		boolean allOk = true;
		
		if (!StringHelper.containsNonWhitespace(textElement.getValue())) {
			textElement.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == checkApiConnectionButton) {
			doCheckApiConnection();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		opencastModule.setEnabled(enabled);
		
		String apiUrl = apiUrlEl.getValue();
		apiUrl = apiUrl.endsWith("/")? apiUrl.substring(0, apiUrl.length() - 1): apiUrl;
		opencastModule.setApiUrl(apiUrl);
		
		String apiUsername = apiUsernameEl.getValue();
		String apiPassword = apiPasswordEl.getValue();
		opencastModule.setApiCredentials(apiUsername, apiPassword);
		
		String ltiUrl = ltiUrlEl.getValue();
		ltiUrl = ltiUrl.endsWith("/")? ltiUrl.substring(0, ltiUrl.length() - 1): ltiUrl;
		opencastModule.setLtiUrl(ltiUrl);
		
		String ltiKey = ltiKeyEl.getValue();
		opencastModule.setLtiKey(ltiKey);
		
		String ltiSecret = ltiSectretEl.getValue();
		opencastModule.setLtiSecret(ltiSecret);
	}

	private void doCheckApiConnection() {
		boolean connectionOk = opencastService.checkApiConnection();
		if (connectionOk) {
			showInfo("check.api.connection.ok");
		} else {
			showError("check.api.connection.nok");
		}
	}

	@Override
	protected void doDispose() {
		//
	}

}
