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
package org.olat.modules.adobeconnect.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.manager.AdobeConnectSPI;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.openmeetings.manager.OpenMeetingsException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectConfigurationController extends FormBasicController {
	
	private static final String[] CLEAN_KEYS = { "-", "1", "2", "3", "4", "5", "7", "14", "21", "30" };
	private static final String[] CREATE_KEYS = { "immediately", "differed" };
	private static final String[] SINGLE_KEYS = { "single", "perdate" };
	private static final String[] FOR_KEYS = { "courses", "groups" };
	private static final String PLACEHOLDER = "xxx-placeholder-xxx";
	
	private FormLink checkLink;
	private TextElement urlEl;
	private TextElement loginEl;
	private TextElement passwordEl;
	private TextElement accountIdEl;
	private SpacerElement spacerEl;
	private SingleSelection providerEl;
	private SingleSelection cleanMeetingsEl;
	private SingleSelection createMeetingEl;
	private SingleSelection singleMeetingEl;
	private MultipleSelectionElement moduleEnabled;
	private MultipleSelectionElement enabledForEl;

	private static final String[] enabledKeys = new String[]{"on"};
	private final String[] enabledValues;
	
	private String replacedValue;
	
	@Autowired
	private AdobeConnectModule adobeConnectModule;
	@Autowired
	private AdobeConnectManager adobeConnectManager;
	@Autowired
	private List<AdobeConnectSPI> providers;
	
	public AdobeConnectConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		enabledValues = new String[]{translate("enabled")};
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("adobeconnect.title");
		setFormInfo("adobeconnect.intro");
		setFormContextHelp("Communication and Collaboration#_openmeeting_config");
		
		moduleEnabled = uifactory.addCheckboxesHorizontal("adobeconnect.module.enabled", formLayout, enabledKeys, enabledValues);
		moduleEnabled.select(enabledKeys[0], adobeConnectModule.isEnabled());
		moduleEnabled.addActionListener(FormEvent.ONCHANGE);
		
		String[] forValues = new String[] {
			translate("adobeconnect.module.enabled.for.courses"), translate("adobeconnect.module.enabled.for.groups")
		};
		enabledForEl = uifactory.addCheckboxesVertical("adobeconnect.module.enabled.for", formLayout, FOR_KEYS, forValues, 1);
		enabledForEl.select(FOR_KEYS[0], adobeConnectModule.isCoursesEnabled());
		enabledForEl.select(FOR_KEYS[1], adobeConnectModule.isGroupsEnabled());
			
		//spacer
		spacerEl = uifactory.addSpacerElement("spacer", formLayout, false);

		//account configuration
		String[] providerKeys = new String[providers.size()];
		String[] providerValues = new String[providers.size()];
		for(int i=providers.size(); i-->0; ) {
			AdobeConnectSPI provider = providers.get(i);
			providerKeys[i] = provider.getId();
			providerValues[i] = provider.getName();
		}
		providerEl = uifactory.addDropdownSingleselect("adobeconnect.module.provider", formLayout, providerKeys, providerValues);
		String providerId = adobeConnectModule.getProviderId();
		if(providerId != null) {
			for(String providerKey:providerKeys) {
				if(providerId.equals(providerKey)) {
					providerEl.select(providerKey, true);
				}
			}
		}
		
		URI uri = adobeConnectModule.getAdobeConnectURI();
		String uriStr = uri == null ? "" : uri.toString();
		urlEl = uifactory.addTextElement("aconnect-url", "option.baseurl", 255, uriStr, formLayout);
		urlEl.setDisplaySize(60);
		urlEl.setExampleKey("option.baseurl.example", null);
		String login = adobeConnectModule.getAdminLogin();
		loginEl = uifactory.addTextElement("aconnect-login", "option.adminlogin", 255, login, formLayout);
		String credential = adobeConnectModule.getAdminPassword();
		if(StringHelper.containsNonWhitespace(credential)) {
			replacedValue = credential;
			credential = PLACEHOLDER;
		}
		passwordEl = uifactory.addPasswordElement("aconnect-password", "option.adminpassword", 255, credential, formLayout);
		passwordEl.setAutocomplete("new-password");
		
		String accountId = adobeConnectModule.getAccountId();
		accountIdEl = uifactory.addTextElement("aconnect-id", "option.accountid", 32, accountId, formLayout);
		accountIdEl.setHelpTextKey("option.accountid.explain", null);
		
		// delete meeting
		String[] cleanValues = Arrays.copyOf(CLEAN_KEYS, CLEAN_KEYS.length);
		cleanValues[0] = translate("option.dont.clean.meetings");
		cleanMeetingsEl = uifactory.addDropdownSingleselect("option.clean.meetings", formLayout, CLEAN_KEYS, cleanValues);
		if(adobeConnectModule.isCleanupMeetings()) {
			long days = adobeConnectModule.getDaysToKeep();
			String dayStr = Long.toString(days);
			for(String key:CLEAN_KEYS) {
				if(dayStr.equals(key)) {
					cleanMeetingsEl.select(key, true);
				}
			}
		} else {
			cleanMeetingsEl.select(CLEAN_KEYS[0], true);
		}
		
		// create meeting ASAP
		String[] createValues = new String[] { translate("option.create.meeting.immediately"), translate("option.create.meeting.differed") };
		createMeetingEl = uifactory.addRadiosHorizontal("option.create.meeting", "option.create.meeting", formLayout, CREATE_KEYS, createValues);
		if(adobeConnectModule.isCreateMeetingImmediately()) {
			createMeetingEl.select(CREATE_KEYS[0], true);
		} else {
			createMeetingEl.select(CREATE_KEYS[1], true);
		}
		
		String[] singleMeetingValues = new String[] { translate("option.single.meeting.single"), translate("option.single.meeting.perdate") };
		singleMeetingEl = uifactory.addRadiosHorizontal("option.single.meeting", formLayout, SINGLE_KEYS, singleMeetingValues);
		if(adobeConnectModule.isSingleMeetingMode()) {
			singleMeetingEl.select(SINGLE_KEYS[0], true);
		} else {
			singleMeetingEl.select(SINGLE_KEYS[1], true);
		}
		
		//buttons save - check
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		checkLink = uifactory.addFormLink("check", buttonLayout, Link.BUTTON);
	}
	
	private void updateUI() {
		boolean enabled = moduleEnabled.isAtLeastSelected(1);
		enabledForEl.setVisible(enabled);
		checkLink.setVisible(enabled);
		urlEl.setVisible(enabled);
		loginEl.setVisible(enabled);
		passwordEl.setVisible(enabled);
		accountIdEl.setVisible(enabled);
		spacerEl.setVisible(enabled);
		providerEl.setVisible(enabled);
		cleanMeetingsEl.setVisible(enabled);
		createMeetingEl.setVisible(enabled);
		singleMeetingEl.setVisible(enabled);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		//validate only if the module is enabled
		if(moduleEnabled.isAtLeastSelected(1)) {
			providerEl.clearError();
			if(!providerEl.isOneSelected()) {
				providerEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
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
		
		String login = loginEl.getValue();
		loginEl.clearError();
		if(!StringHelper.containsNonWhitespace(login)) {
			loginEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(login.length() >= 255) {
			loginEl.setErrorKey("form.error.toolong", new String[] { "255" });
			allOk &= false;
		}
		
		String password = passwordEl.getValue();
		passwordEl.clearError();
		if(!StringHelper.containsNonWhitespace(password)) {
			passwordEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateConnection() {
		boolean allOk = true;
		try {
			AdobeConnectErrors errors = new AdobeConnectErrors();
			boolean ok = checkConnection(errors);
			if(!ok || errors.hasErrors()) {
				passwordEl.setValue("");
				urlEl.setErrorKey("error.customerDoesntExist", null);
				allOk &= false;
			}
		} catch (Exception e) {
			showError(OpenMeetingsException.SERVER_NOT_I18N_KEY);
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
			adobeConnectModule.setEnabled(enabled);
			// update collaboration tools list
			if(enabled) {
				String url = urlEl.getValue();
				adobeConnectModule.setAdobeConnectURI(new URI(url));
				adobeConnectModule.setAdminLogin(loginEl.getValue());
				adobeConnectModule.setProviderId(providerEl.getSelectedKey());
				adobeConnectModule.setAccountId(accountIdEl.getValue());
				adobeConnectModule.setCoursesEnabled(enabledForEl.isSelected(0));
				adobeConnectModule.setGroupsEnabled(enabledForEl.isSelected(1));
				if(cleanMeetingsEl.isSelected(0)) {
					adobeConnectModule.setCleanupMeetings(false);
					adobeConnectModule.setDaysToKeep(null);
				} else {
					adobeConnectModule.setCleanupMeetings(true);
					adobeConnectModule.setDaysToKeep(cleanMeetingsEl.getSelectedKey());
				}
				adobeConnectModule.setSingleMeetingMode(singleMeetingEl.isSelected(0));
				adobeConnectModule.setCreateMeetingImmediately(createMeetingEl.isSelected(0));
				String credential = passwordEl.getValue();
				if(!PLACEHOLDER.equals(credential)) {
					adobeConnectModule.setAdminPassword(credential);
					passwordEl.setValue(PLACEHOLDER);
				} else if(StringHelper.containsNonWhitespace(replacedValue)) {
					adobeConnectModule.setAdminPassword(replacedValue);
				}
			} else {
				adobeConnectModule.setAdobeConnectURI(null);
				adobeConnectModule.setAdminLogin(null);
				adobeConnectModule.setAdminPassword(null);
				adobeConnectModule.setAccountId(null);
			}
			CollaborationToolsFactory.getInstance().initAvailableTools();
		} catch (URISyntaxException e) {
			logError("", e);
			urlEl.setErrorKey("error.url.invalid", null);
		}
	}
	
	private void doCheckConnection() {
		AdobeConnectErrors errors = new AdobeConnectErrors();
		boolean loginOk = checkConnection(errors);
		if(errors.hasErrors()) {
			getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
		} else if(loginOk) {
			showInfo("connection.successful");
		} else {
			showError("connection.failed");
		}
	}
	
	private boolean checkConnection(AdobeConnectErrors errors) {
		String url = urlEl.getValue();
		String login = loginEl.getValue();
		String credential = passwordEl.getValue();
		if(PLACEHOLDER.equals(credential)) {
			if(StringHelper.containsNonWhitespace(replacedValue)) {
				credential = replacedValue;
			} else {
				credential = adobeConnectModule.getAdminPassword();
			}
		} else {
			replacedValue = credential;
			passwordEl.setValue(PLACEHOLDER);
		}
		return adobeConnectManager.checkConnection(url, login, credential, errors);
	}
}
