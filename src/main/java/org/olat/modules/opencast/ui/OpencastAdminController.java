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
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.nodes.basiclti.LTIConfigForm;
import org.olat.modules.opencast.AuthDelegate;
import org.olat.modules.opencast.AuthDelegate.Type;
import org.olat.modules.opencast.OpencastModule;
import org.olat.modules.opencast.OpencastService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Aug 2020<br>
 * @admin uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpencastAdminController extends FormBasicController {
	
	private static final String[] ENABLED_KEYS = new String[]{ "on" };
	
	private String[] ltiRolesKeys = new String[]{
			"Learner", "Instructor", "Administrator", "TeachingAssistant", "ContentDeveloper", "Mentor"
	};
	
	private MultipleSelectionElement enabledEl;
	private TextElement apiUrlEl;
	private TextElement apiUrlPresentationEl;
	private TextElement apiUsernameEl;
	private TextElement apiPasswordEl;
	private TextElement ltiUrlEl;
	private TextElement ltiSignUrlEl;
	private TextElement ltiKeyEl;
	private TextElement ltiSectretEl;
	private SpacerElement bbbSpacerEl;
	private MultipleSelectionElement bbbEnabledEl;
	private SpacerElement courseNodeSpacerEl;
	private MultipleSelectionElement courseNodeEnabledEl;
	private SingleSelection authDelegateTypeEl;
	private TextElement authDelegateRolesEl;
	private MultipleSelectionElement rolesAdminEl;
	private MultipleSelectionElement rolesCoachEl;
	private MultipleSelectionElement rolesParticipantEl;
	private FormLink checkApiConnectionButton;
	
	@Autowired
	private OpencastModule opencastModule;
	@Autowired
	private OpencastService opencastService;

	public OpencastAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		super.setTranslator(Util.createPackageTranslator(LTIConfigForm.class, ureq.getLocale(), getTranslator()));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		
		String[] enableValues = new String[]{ translate("on") };
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, ENABLED_KEYS, enableValues);
		enabledEl.select(ENABLED_KEYS[0], opencastModule.isEnabled());
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		
		String apiUrl = opencastModule.getApiUrl();
		apiUrlEl = uifactory.addTextElement("admin.api.url", "admin.api.url", 128, apiUrl, formLayout);
		apiUrlEl.setExampleKey("admin.api.url.example", null);
		apiUrlEl.setMandatory(true);
		
		String apiPresentationUrl = opencastModule.getApiPresentationUrl();
		apiUrlPresentationEl = uifactory.addTextElement("admin.api.presentation.url", "admin.api.presentation.url", 128, apiPresentationUrl, formLayout);
		apiUrlPresentationEl.setExampleKey("admin.api.presentation.url.example", null);
		apiUrlPresentationEl.setMandatory(true);
		
		String apiUsername = opencastModule.getApiUsername();
		apiUsernameEl = uifactory.addTextElement("admin.api.username", 128, apiUsername, formLayout);
		apiUsernameEl.setMandatory(true);
		
		String apiPassword = opencastModule.getApiPassword();
		apiPasswordEl = uifactory.addPasswordElement("admin.api.password", "admin.api.password", 128, apiPassword, formLayout);
		apiPasswordEl.setAutocomplete("new-password");
		apiPasswordEl.setMandatory(true);
		
		String ltiUrl = opencastModule.getLtiUrl();
		ltiUrlEl = uifactory.addTextElement("admin.lti.url", "admin.lti.url", 128, ltiUrl, formLayout);
		ltiUrlEl.setExampleKey("admin.lti.url.example", null);
		ltiUrlEl.setMandatory(true);
		
		String ltiSignUrl = opencastModule.getLtiSignUrlRaw();
		ltiSignUrlEl = uifactory.addTextElement("admin.lti.sign.url", "admin.lti.sign.url", 128, ltiSignUrl, formLayout);
		ltiSignUrlEl.setExampleKey("admin.lti.sign.url.example", null);
		ltiSignUrlEl.setHelpTextKey("admin.lti.sign.url.help", null);
		
		String ltiKey = opencastModule.getLtiKey();
		ltiKeyEl = uifactory.addTextElement("admin.lti.key", 123, ltiKey, formLayout);
		ltiKeyEl.setMandatory(true);
		
		String ltiSecret = opencastModule.getLtiSecret();
		ltiSectretEl = uifactory.addPasswordElement("admin.lti.secret", "admin.lti.secret", 128, ltiSecret, formLayout);
		ltiSectretEl.setAutocomplete("new-password");
		ltiSectretEl.setMandatory(true);
		
		bbbSpacerEl = uifactory.addSpacerElement("spacer.bbb", formLayout, false);
		bbbEnabledEl = uifactory.addCheckboxesHorizontal("admin.bbb.enabled", formLayout, ENABLED_KEYS, enableValues);
		
		courseNodeSpacerEl = uifactory.addSpacerElement("spacer.cn", formLayout, false);
		courseNodeEnabledEl = uifactory.addCheckboxesHorizontal("admin.course.node.enabled", formLayout, ENABLED_KEYS, enableValues);
		
		SelectionValues authDelegateKV = new SelectionValues();
		authDelegateKV.add(SelectionValues.entry(AuthDelegate.Type.None.name(), translate("admin.auth.delegate.type.none")));
		authDelegateKV.add(SelectionValues.entry(AuthDelegate.Type.User.name(), translate("admin.auth.delegate.type.user")));
		authDelegateKV.add(SelectionValues.entry(AuthDelegate.Type.Roles.name(), translate("admin.auth.delegate.type.roles")));
		authDelegateTypeEl = uifactory.addRadiosHorizontal("admin.auth.delegate.type", formLayout, authDelegateKV.keys(), authDelegateKV.values());
		authDelegateTypeEl.addActionListener(FormEvent.ONCHANGE);
		authDelegateRolesEl = uifactory.addTextElement("admin.auth.delegate.roles", 128, null, formLayout);
		authDelegateRolesEl.setMandatory(true);
		
		String[] ltiRolesValues = new String[]{
				translate("roles.lti.learner"),
				translate("roles.lti.instructor"),
				translate("roles.lti.administrator"),
				translate("roles.lti.teachingAssistant"),
				translate("roles.lti.contentDeveloper"),
				translate("roles.lti.mentor")
		};
		rolesAdminEl = uifactory.addCheckboxesHorizontal("admin", "author.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		rolesCoachEl = uifactory.addCheckboxesHorizontal("coach", "coach.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		rolesParticipantEl = uifactory.addCheckboxesHorizontal("participant", "participant.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		checkApiConnectionButton = uifactory.addFormLink("admin.check.api.connection", buttonLayout, Link.BUTTON);
	}
	
	private void udpateRoles(MultipleSelectionElement roleEl, String roles) {
		String[] roleArr = roles.split(",");
		for (String role:roleArr) {
			roleEl.select(role, true);
		}
	}
	
	private void initializeValues() {
		bbbEnabledEl.select(ENABLED_KEYS[0], opencastModule.isBigBlueButtonEnabledRaw());
		courseNodeEnabledEl.select(ENABLED_KEYS[0], opencastModule.isCourseNodeEnabledRaw());
		authDelegateTypeEl.select(opencastModule.getAuthDelegateType().name(), true);
		authDelegateRolesEl.setValue(opencastModule.getAuthDelegateRoles());
		udpateRoles(rolesAdminEl, opencastModule.getRolesAdmin());
		udpateRoles(rolesCoachEl, opencastModule.getRolesCoach());
		udpateRoles(rolesParticipantEl, opencastModule.getRolesParticipant());
	}
	
	private void updateUI() {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		if (enabled) {
			initializeValues();
		}
		bbbSpacerEl.setVisible(enabled);
		bbbEnabledEl.setVisible(enabled);
		courseNodeSpacerEl.setVisible(enabled);
		courseNodeEnabledEl.setVisible(enabled);
		authDelegateTypeEl.setVisible(enabled);
		boolean authDelegateRoles = authDelegateTypeEl.isOneSelected() && Type.Roles == Type.valueOf(authDelegateTypeEl.getSelectedKey());
		authDelegateRolesEl.setVisible(enabled && authDelegateRoles);
		rolesAdminEl.setVisible(enabled);
		rolesCoachEl.setVisible(enabled);
		rolesParticipantEl.setVisible(enabled);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			updateUI();
		} else if (source == authDelegateTypeEl) {
			boolean authDelegateRoles = authDelegateTypeEl.isOneSelected() && Type.Roles == Type.valueOf(authDelegateTypeEl.getSelectedKey());
			authDelegateRolesEl.setVisible(authDelegateRoles);
		} else if (source == checkApiConnectionButton) {
			doCheckApiConnection();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		//validate only if the module is enabled
		if(enabledEl.isAtLeastSelected(1)) {
			allOk &= validateIsMandatory(apiUrlEl);
			allOk &= validateIsMandatory(apiUrlPresentationEl);
			allOk &= validateIsMandatory(apiUsernameEl);
			allOk &= validateIsMandatory(apiPasswordEl);
			allOk &= validateIsMandatory(ltiUrlEl);
			allOk &= validateIsMandatory(ltiKeyEl);
			allOk &= validateIsMandatory(ltiSectretEl);
			if (authDelegateRolesEl.isVisible() && courseNodeEnabledEl.isAtLeastSelected(1)) {
				allOk &= validateIsMandatory(authDelegateRolesEl);
			}
		}
		
		return allOk;
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
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		opencastModule.setEnabled(enabled);
		
		String apiUrl = apiUrlEl.getValue();
		apiUrl = apiUrl.endsWith("/")? apiUrl.substring(0, apiUrl.length() - 1): apiUrl;
		opencastModule.setApiUrl(apiUrl);
		
		String apiPresentationUrl = apiUrlPresentationEl.getValue();
		apiPresentationUrl = apiPresentationUrl.endsWith("/")? apiPresentationUrl.substring(0, apiPresentationUrl.length() - 1): apiPresentationUrl;
		opencastModule.setApiPresentationUrl(apiPresentationUrl);
		
		String apiUsername = apiUsernameEl.getValue();
		String apiPassword = apiPasswordEl.getValue();
		opencastModule.setApiCredentials(apiUsername, apiPassword);
		
		String ltiUrl = ltiUrlEl.getValue();
		ltiUrl = ltiUrl.endsWith("/")? ltiUrl.substring(0, ltiUrl.length() - 1): ltiUrl;
		opencastModule.setLtiUrl(ltiUrl);
		
		String ltiSignUrl = ltiSignUrlEl.getValue();
		if (StringHelper.containsNonWhitespace(ltiSignUrl)) {
			ltiSignUrl = ltiSignUrl.endsWith("/")? ltiSignUrl.substring(0, ltiSignUrl.length() - 1): ltiSignUrl;
		} else {
			ltiSignUrl = null;
		}
		opencastModule.setLtiSignUrl(ltiSignUrl);
		
		String ltiKey = ltiKeyEl.getValue();
		opencastModule.setLtiKey(ltiKey);
		
		String ltiSecret = ltiSectretEl.getValue();
		opencastModule.setLtiSecret(ltiSecret);
		
		if (enabled) {
			boolean bbbEnabled = bbbEnabledEl.isAtLeastSelected(1);
			opencastModule.setBigBlueButtonEnabled(bbbEnabled);
			
			boolean cnEnabled = courseNodeEnabledEl.isAtLeastSelected(1);
			opencastModule.setCourseNodeEnabled(cnEnabled);
			
			Type authDelegateType = authDelegateTypeEl.isOneSelected()
					? Type.valueOf(authDelegateTypeEl.getSelectedKey())
					: Type.User;
			opencastModule.setAuthDelegateType(authDelegateType);
			
			String authDelegateRoles = Type.Roles == authDelegateType
					? authDelegateRolesEl.getValue()
					: null;
			opencastModule.setAuthDelegateRoles(authDelegateRoles);
			
			String rolesAdmin = getRoles(rolesAdminEl);
			opencastModule.setRolesAdmin(rolesAdmin);
			
			String rolesCoach = getRoles(rolesCoachEl);
			opencastModule.setRolesCoach(rolesCoach);
			
			String rolesParticipant = getRoles(rolesParticipantEl);
			opencastModule.setRolesParticipant(rolesParticipant);
		}
	}
	
	private String getRoles(MultipleSelectionElement roleEl) {
		StringBuilder sb = new StringBuilder();
		for(String key:roleEl.getSelectedKeys()) {
			if(sb.length() > 0) sb.append(',');
			sb.append(key);
		}
		return sb.toString();
	}

	private void doCheckApiConnection() {
		boolean connectionOk = opencastService.checkApiConnection();
		if (connectionOk) {
			showInfo("check.api.connection.ok");
		} else {
			showError("check.api.connection.nok");
		}
	}

}
