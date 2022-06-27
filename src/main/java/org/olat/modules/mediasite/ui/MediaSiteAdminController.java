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
package org.olat.modules.mediasite.ui;

import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.MediaSiteModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 07.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class MediaSiteAdminController extends FormBasicController {
	
	private static final String[] enabledKeys = new String[]{"on"};
	
	private final boolean usedInAdministration;
	
	private MultipleSelectionElement enabledEl;
	private MultipleSelectionElement globalServerEnabledEl;
	private TextElement enterpriseKeyEl;
	private TextElement enterpriseSecretEl;
	private TextElement serverNameEl;
	private TextElement baseUrlEl;
	private TextElement administrationUrlEl;
	private TextElement usernamePropertyKeyEl;
	private MultipleSelectionElement supressDataTransmissionEl;
	
	private List<FormItem> formItems;
		
	@Autowired
	private MediaSiteModule mediaSiteModule;

	public MediaSiteAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		this.usedInAdministration = true;
		
		initForm(ureq);
		loadDataFromModule();
		updateUi();
	}
	
	public MediaSiteAdminController(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, rootForm);
		
		this.usedInAdministration = false;
		
		initForm(ureq);
		loadDataFromModule();
		updateUi();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (usedInAdministration) {
			setFormTitle("admin.title");
			setFormDescription("admin.description");
			setFormContextHelp("manual_user/course_elements/Course_Element_Mediasite/");
			
			enabledEl = uifactory.addCheckboxesHorizontal("enabled", formLayout, enabledKeys, TranslatorHelper.translateAll(getTranslator(), enabledKeys));
			enabledEl.addActionListener(FormEvent.ONCHANGE);
			
			globalServerEnabledEl = uifactory.addCheckboxesHorizontal("global.login", formLayout, enabledKeys, TranslatorHelper.translateAll(getTranslator(), enabledKeys));
			globalServerEnabledEl.addActionListener(FormEvent.ONCHANGE);
			
			serverNameEl = uifactory.addTextElement("server.name", -1, null, formLayout);
			serverNameEl.setMandatory(true);
		}
		
		baseUrlEl = uifactory.addTextElement("base.url", -1, null, formLayout);
		baseUrlEl.setMandatory(true);
		administrationUrlEl = uifactory.addTextElement("administration.url", -1, null, formLayout);
		administrationUrlEl.setMandatory(true);
		enterpriseKeyEl = uifactory.addTextElement("enterprise.key", -1, null, formLayout);
		enterpriseKeyEl.setMandatory(true);
		enterpriseSecretEl = uifactory.addTextElement("enterprise.secret", -1, null, formLayout);
		enterpriseSecretEl.setMandatory(true);
		usernamePropertyKeyEl = uifactory.addTextElement("username.property.key", -1, null, formLayout);
		usernamePropertyKeyEl.setMandatory(true);
		supressDataTransmissionEl = uifactory.addCheckboxesHorizontal("supress.data.transmission", formLayout, enabledKeys, TranslatorHelper.translateAll(getTranslator(), enabledKeys));
		
		formItems = Arrays.asList(enterpriseKeyEl, enterpriseSecretEl, serverNameEl, baseUrlEl, administrationUrlEl, usernamePropertyKeyEl, supressDataTransmissionEl);
		
		if (usedInAdministration) {
			uifactory.addFormSubmitButton("save", formLayout);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= checkMandatoryElement(enterpriseKeyEl);
		allOk &= checkMandatoryElement(enterpriseSecretEl);
		allOk &= checkMandatoryElement(serverNameEl);
		allOk &= checkMandatoryElement(baseUrlEl);
		allOk &= checkMandatoryElement(administrationUrlEl);
		allOk &= checkMandatoryElement(usernamePropertyKeyEl);
		
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		if (usedInAdministration) {
			mediaSiteModule.setEnabled(enabledEl.isAtLeastSelected(1));
			mediaSiteModule.setGlobalLoginEnabled(globalServerEnabledEl.isAtLeastSelected(1));
			mediaSiteModule.setEnterpriseKey(enterpriseKeyEl.getValue());
			mediaSiteModule.setEnterpriseSecret(enterpriseSecretEl.getValue());
			mediaSiteModule.setServerName(serverNameEl.getValue());
			mediaSiteModule.setBaseURL(baseUrlEl.getValue());
			mediaSiteModule.setAdministrationURL(administrationUrlEl.getValue());
			mediaSiteModule.setUsernameProperty(usernamePropertyKeyEl.getValue());
			mediaSiteModule.setSupressDataTransmissionAgreement(supressDataTransmissionEl.isAtLeastSelected(1));
		}
		
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			globalServerEnabledEl.setVisible(enabledEl.isAtLeastSelected(1));
			updateUi();
		} if (source == globalServerEnabledEl) {
			updateUi();
		}
	}
	
	private void loadDataFromModule() {	
		if (enabledEl != null) {
			enabledEl.select("on", mediaSiteModule.isEnabled());
		}
		
		if (globalServerEnabledEl != null) {
			globalServerEnabledEl.select("on", mediaSiteModule.isGlobalLoginEnabled());
		}
		
		if (serverNameEl != null) {
			serverNameEl.setValue(mediaSiteModule.getServerName());
		}
		
		enterpriseKeyEl.setValue(mediaSiteModule.getEnterpriseKey());
		enterpriseSecretEl.setValue(mediaSiteModule.getEnterpriseSecret());
		baseUrlEl.setValue(mediaSiteModule.getBaseURL());
		administrationUrlEl.setValue(mediaSiteModule.getAdministrationURL());
		usernamePropertyKeyEl.setValue(mediaSiteModule.getUsernameProperty());
		supressDataTransmissionEl.select("on", mediaSiteModule.isSupressDataTransmissionAgreement());
	}
	
	public void loadFromCourseNodeConfig(ModuleConfiguration config) {
		enterpriseKeyEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_KEY));
		enterpriseSecretEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_SECRET));
		baseUrlEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_SERVER_URL));
		administrationUrlEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_ADMINISTRATION_URL));
		usernamePropertyKeyEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_USER_NAME_KEY, mediaSiteModule.getUsernameProperty()));
		supressDataTransmissionEl.select("on", config.getBooleanSafe(MediaSiteCourseNode.CONFIG_SUPRESS_AGREEMENT, mediaSiteModule.isSupressDataTransmissionAgreement()));
	}
	
	public boolean safeToModulConfiguration(UserRequest ureq, ModuleConfiguration config) {
		if (validateFormLogic(ureq)) {
			config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, true);
			config.setStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_KEY, enterpriseKeyEl.getValue());
			config.setStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_SECRET, enterpriseSecretEl.getValue());
			config.setStringValue(MediaSiteCourseNode.CONFIG_USER_NAME_KEY, usernamePropertyKeyEl.getValue());
			config.setStringValue(MediaSiteCourseNode.CONFIG_SERVER_URL, baseUrlEl.getValue());
			config.setBooleanEntry(MediaSiteCourseNode.CONFIG_SUPRESS_AGREEMENT, supressDataTransmissionEl.isAtLeastSelected(1));
			config.setStringValue(MediaSiteCourseNode.CONFIG_ADMINISTRATION_URL, administrationUrlEl.getValue());
			
			return true;
		}
		
		config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, false);
		
		return false;
	}
	
	private void updateUi() {
		boolean visible = !usedInAdministration || (globalServerEnabledEl.isAtLeastSelected(1) && enabledEl.isAtLeastSelected(1));
		
		formItems.stream().filter(item -> item != null).forEach(test -> test.setVisible(visible));
	}
	
	private boolean checkMandatoryElement(TextElement textElement) {
		if (textElement == null) {
			return true;
		}
		
		textElement.clearError();
		
		if (!StringHelper.containsNonWhitespace(textElement.getValue())) {
			textElement.setErrorKey("form.legende.mandatory", null);
			return false;
		}
		
		return true;
	}
}
