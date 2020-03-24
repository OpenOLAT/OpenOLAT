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
package org.olat.modules.bigbluebutton.ui;

import java.net.URI;
import java.net.URISyntaxException;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.model.BigBlueButtonException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonConfigurationController extends FormBasicController {

	private static final String[] FOR_KEYS = { "courses", "groups" };
	private static final String PLACEHOLDER = "xxx-placeholder-xxx";
	
	private FormLink checkLink;
	private TextElement urlEl;
	private SpacerElement spacerEl;
	private TextElement sharedSecretEl;
	private MultipleSelectionElement moduleEnabled;
	private MultipleSelectionElement enabledForEl;
	private MultipleSelectionElement permanentForEl;
	private MultipleSelectionElement adhocForEl;

	private static final String[] enabledKeys = new String[]{"on"};
	private final String[] enabledValues;
	
	private String replacedSharedSecretValue;
	
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		enabledValues = new String[]{translate("enabled")};
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("bigbluebutton.title");
		setFormInfo("bigbluebutton.intro");
		setFormContextHelp("Communication and Collaboration#_bigbluebutton_config");
		
		moduleEnabled = uifactory.addCheckboxesHorizontal("bigbluebutton.module.enabled", formLayout, enabledKeys, enabledValues);
		moduleEnabled.select(enabledKeys[0], bigBlueButtonModule.isEnabled());
		moduleEnabled.addActionListener(FormEvent.ONCHANGE);
		
		String[] forValues = new String[] {
			translate("bigbluebutton.module.enabled.for.courses"), translate("bigbluebutton.module.enabled.for.groups")
		};
		enabledForEl = uifactory.addCheckboxesVertical("bigbluebutton.module.enabled.for", formLayout, FOR_KEYS, forValues, 1);
		enabledForEl.select(FOR_KEYS[0], bigBlueButtonModule.isCoursesEnabled());
		enabledForEl.select(FOR_KEYS[1], bigBlueButtonModule.isGroupsEnabled());
		
		permanentForEl = uifactory.addCheckboxesHorizontal("enable.permanent.meeting", formLayout, enabledKeys, enabledValues);
		permanentForEl.select(enabledKeys[0], bigBlueButtonModule.isPermanentMeetingEnabled());
		
		adhocForEl = uifactory.addCheckboxesHorizontal("enable.adhoc.meeting", formLayout, enabledKeys, enabledValues);
		adhocForEl.select(enabledKeys[0], bigBlueButtonModule.isAdhocMeetingEnabled());
		adhocForEl.setVisible(false);//TODO bbb
		
		//spacer
		spacerEl = uifactory.addSpacerElement("spacer", formLayout, false);

		URI uri = bigBlueButtonModule.getBigBlueButtonURI();
		String uriStr = uri == null ? "" : uri.toString();
		urlEl = uifactory.addTextElement("bbb-url", "option.baseurl", 255, uriStr, formLayout);
		urlEl.setDisplaySize(60);
		urlEl.setExampleKey("option.baseurl.example", null);

		String sharedSecret = bigBlueButtonModule.getSharedSecret();
		if(StringHelper.containsNonWhitespace(sharedSecret)) {
			replacedSharedSecretValue = sharedSecret;
			sharedSecret = PLACEHOLDER;
		}
		sharedSecretEl = uifactory.addPasswordElement("shared.secret", "option.bigbluebutton.shared.secret", 255, sharedSecret, formLayout);
		sharedSecretEl.setAutocomplete("new-password");

		
		//buttons save - check
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		checkLink = uifactory.addFormLink("check", buttonLayout, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void updateUI() {
		boolean enabled = moduleEnabled.isAtLeastSelected(1);
		adhocForEl.setVisible(enabled);
		permanentForEl.setVisible(enabled);
		enabledForEl.setVisible(enabled);
		checkLink.setVisible(enabled);
		urlEl.setVisible(enabled);
		sharedSecretEl.setVisible(enabled);
		spacerEl.setVisible(enabled);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		//validate only if the module is enabled
		if(moduleEnabled.isAtLeastSelected(1)) {
			allOk &= validateUrlFields();
			if(allOk) {
				allOk &= validateConnection();
			}
		}
		
		return allOk;
	}

	private boolean validateUrlFields() {
		boolean allOk = true;
		
		String url = urlEl.getValue();
		urlEl.clearError();
		if(StringHelper.containsNonWhitespace(url)) {
			try {
				URI uri = new URI(url);
				uri.getHost();
			} catch(Exception e) {
				urlEl.setErrorKey("error.url.invalid", null);
				allOk &= false;
			}
		} else {
			urlEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		String password = sharedSecretEl.getValue();
		sharedSecretEl.clearError();
		if(!StringHelper.containsNonWhitespace(password)) {
			sharedSecretEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateConnection() {
		boolean allOk = true;
		try {
			BigBlueButtonErrors errors = new BigBlueButtonErrors();
			boolean ok = checkConnection(errors);
			if(!ok || errors.hasErrors()) {
				sharedSecretEl.setValue("");
				urlEl.setErrorKey("error.customerDoesntExist", null);
				allOk &= false;
			}
		} catch (Exception e) {
			showError(BigBlueButtonException.SERVER_NOT_I18N_KEY);
			allOk &= false;
		}
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == moduleEnabled) {
			updateUI();
		} else if(source == checkLink) {
			if(validateUrlFields()) {
				doCheckConnection();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		try {
			boolean enabled = moduleEnabled.isSelected(0);
			bigBlueButtonModule.setEnabled(enabled);
			// update collaboration tools list
			if(enabled) {
				String url = urlEl.getValue();
				bigBlueButtonModule.setBigBlueButtonURI(new URI(url));
				bigBlueButtonModule.setCoursesEnabled(enabledForEl.isSelected(0));
				bigBlueButtonModule.setGroupsEnabled(enabledForEl.isSelected(1));
				bigBlueButtonModule.setPermanentMeetingEnabled(permanentForEl.isAtLeastSelected(1));
				bigBlueButtonModule.setAdhocMeetingEnabled(adhocForEl.isAtLeastSelected(1));
				
				String sharedSecret = sharedSecretEl.getValue();
				if(!PLACEHOLDER.equals(sharedSecret)) {
					bigBlueButtonModule.setSharedSecret(sharedSecret);
					sharedSecretEl.setValue(PLACEHOLDER);
				} else if(StringHelper.containsNonWhitespace(replacedSharedSecretValue)) {
					bigBlueButtonModule.setSharedSecret(replacedSharedSecretValue);
				}
			} else {
				bigBlueButtonModule.setBigBlueButtonURI(null);
				bigBlueButtonModule.setSecret(null);
				bigBlueButtonModule.setSharedSecret(null);
			}
			CollaborationToolsFactory.getInstance().initAvailableTools();
		} catch (URISyntaxException e) {
			logError("", e);
			urlEl.setErrorKey("error.url.invalid", null);
		}
	}
	
	private void doCheckConnection() {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		boolean loginOk = checkConnection(errors);
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		} else if(loginOk) {
			showInfo("connection.successful");
		} else {
			showError("connection.failed");
		}
	}
	
	private boolean checkConnection(BigBlueButtonErrors errors) {
		String url = urlEl.getValue();
		String sharedSecret = sharedSecretEl.getValue();
		if(PLACEHOLDER.equals(sharedSecret)) {
			if(StringHelper.containsNonWhitespace(replacedSharedSecretValue)) {
				sharedSecret = replacedSharedSecretValue;
			} else {
				sharedSecret = bigBlueButtonModule.getSharedSecret();
			}
		} else {
			replacedSharedSecretValue = sharedSecret;
			sharedSecretEl.setValue(PLACEHOLDER);
		}
		
		return bigBlueButtonManager.checkConnection(url, sharedSecret, errors);
	}
}
