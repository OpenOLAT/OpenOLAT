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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolDeploymentType;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.mediasite.LtiVersion;
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
	private SpacerElement serverSpacer;
	private TextElement serverNameEl;
	private SingleSelection ltiVersionEl;
	
	private TextElement baseUrlEl;
	private TextElement administrationUrlEl;
	private TextElement enterpriseKeyEl;
	private TextElement enterpriseSecretEl;
	private TextElement usernamePropertyKeyEl;
	
	private StaticTextElement lti13ClientIdEl;
	private StaticTextElement lti13DeploymentIdEl;
	private TextElement lti13InitiateLoginUrlEl;
	private TextElement lti13RedirectUrlEl;
	private TextElement lti13JwksUrlEl;
	private TextElement lti13BaseUrlEl;

	private List<FormItem> ltiCommonFormItems;
	private List<FormItem> lti11FormItems;
	private List<FormItem> lti13FormItems;
		
	private MultipleSelectionElement supressDataTransmissionEl;
	
	@Autowired
	private MediaSiteModule mediaSiteModule;
	@Autowired
	private LTI13Service lti13Service;

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
		ltiCommonFormItems = new ArrayList<>();
		if (usedInAdministration) {
			setFormTitle("admin.title");
			setFormDescription("admin.description");
			setFormContextHelp("manual_user/learningresources/Course_Element_Mediasite/");
			
			enabledEl = uifactory.addCheckboxesHorizontal("enabled", formLayout, enabledKeys, TranslatorHelper.translateAll(getTranslator(), enabledKeys));
			enabledEl.addActionListener(FormEvent.ONCHANGE);
			
			globalServerEnabledEl = uifactory.addCheckboxesHorizontal("global.login", formLayout, enabledKeys, TranslatorHelper.translateAll(getTranslator(), enabledKeys));
			globalServerEnabledEl.addActionListener(FormEvent.ONCHANGE);
			
			serverSpacer = uifactory.addSpacerElement("server.spacer", formLayout, false);

			serverNameEl = uifactory.addTextElement("server.name", -1, null, formLayout);
			serverNameEl.setMandatory(true);
			
			ltiCommonFormItems.add(serverSpacer);
			ltiCommonFormItems.add(serverNameEl);
		}

		SelectionValues ltiVersionKV = new SelectionValues();
		ltiVersionKV.add(SelectionValues.entry(LtiVersion.lti_1_1.name(), "1.1"));
		ltiVersionKV.add(SelectionValues.entry(LtiVersion.lti_1_3.name(), "1.3"));
		ltiVersionEl = uifactory.addRadiosHorizontal("lti.version", formLayout, ltiVersionKV.keys(), ltiVersionKV.values());
		ltiVersionEl.addActionListener(FormEvent.ONCHANGE);
		ltiCommonFormItems.add(ltiVersionEl);
		
		SpacerElement lti11Spacer = uifactory.addSpacerElement("lti11.spacer", formLayout, false);
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
		
		lti11FormItems = Arrays.asList(lti11Spacer, enterpriseKeyEl, enterpriseSecretEl, baseUrlEl, administrationUrlEl, 
				usernamePropertyKeyEl);

		SpacerElement lti13Spacer = uifactory.addSpacerElement("lti13.spacer", formLayout, false);
		lti13ClientIdEl = uifactory.addStaticTextElement("lti13.client.id", "lti13.client.id", "", formLayout);
		lti13DeploymentIdEl = uifactory.addStaticTextElement("lti13.deployment.id", "lti13.deployment.id", "", formLayout);
		lti13InitiateLoginUrlEl = uifactory.addTextElement("lti13.initiate.login.url", -1, null, formLayout);
		lti13InitiateLoginUrlEl.setMandatory(true);
		lti13RedirectUrlEl = uifactory.addTextElement("lti13.redirect.url", -1, null, formLayout);
		lti13RedirectUrlEl.setMandatory(true);
		lti13JwksUrlEl = uifactory.addTextElement("lti13.jwks.url", -1, null, formLayout);
		lti13JwksUrlEl.setMandatory(true);
		lti13BaseUrlEl = uifactory.addTextElement("lti13.base.url", -1, null, formLayout);
		lti13BaseUrlEl.setMandatory(true);

		lti13FormItems = Arrays.asList(lti13Spacer, lti13ClientIdEl, lti13DeploymentIdEl, lti13InitiateLoginUrlEl, 
				lti13RedirectUrlEl, lti13JwksUrlEl, lti13BaseUrlEl);

		SpacerElement suppressSpacer = uifactory.addSpacerElement("suppress.spacer", formLayout, false);
		supressDataTransmissionEl = uifactory.addCheckboxesHorizontal("supress.data.transmission", formLayout, enabledKeys, TranslatorHelper.translateAll(getTranslator(), enabledKeys));
		
		if (usedInAdministration) {
			uifactory.addFormSubmitButton("save", formLayout);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= checkMandatoryElement(serverNameEl);

		if (ltiVersionEl != null && ltiVersionEl.isVisible()) {
			if (LtiVersion.lti_1_1.name().equals(ltiVersionEl.getSelectedKey())) {
				allOk &= validateLti11Elements();
			} else if (LtiVersion.lti_1_3.name().equals(ltiVersionEl.getSelectedKey())) {
				allOk &= validateLti13Elemens();
			}
		}
		
		return allOk;
	}

	private boolean validateLti11Elements() {
		boolean allOk = true;
		for (FormItem formItem : lti11FormItems) {
			if (formItem instanceof TextElement textElement) {
				allOk &= checkMandatoryElement(textElement);
			}
		}
		return allOk;
	}

	private boolean validateLti13Elemens() {
		boolean allOk = true;
		allOk &= checkMandatoryElement(lti13InitiateLoginUrlEl);
		allOk &= checkMandatoryElement(lti13RedirectUrlEl);
		allOk &= checkMandatoryElement(lti13JwksUrlEl);
		allOk &= checkMandatoryElement(lti13BaseUrlEl);
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (usedInAdministration) {
			boolean moduleEnabled = enabledEl.isAtLeastSelected(1);
			mediaSiteModule.setEnabled(moduleEnabled);
			if (moduleEnabled) {
				boolean globalServerEnabled = globalServerEnabledEl.isAtLeastSelected(1);
				mediaSiteModule.setGlobalLoginEnabled(globalServerEnabled);
				mediaSiteModule.setServerName(serverNameEl.getValue());
				LtiVersion ltiVersion = LtiVersion.valueOf(ltiVersionEl.getSelectedKey());
				mediaSiteModule.setLtiVersion(ltiVersion);
				switch (ltiVersion) {
					case lti_1_1 -> {
						mediaSiteModule.setEnterpriseKey(enterpriseKeyEl.getValue());
						mediaSiteModule.setEnterpriseSecret(enterpriseSecretEl.getValue());
						mediaSiteModule.setBaseURL(baseUrlEl.getValue());
						mediaSiteModule.setAdministrationURL(administrationUrlEl.getValue());
						mediaSiteModule.setUsernameProperty(usernamePropertyKeyEl.getValue());
					}
					case lti_1_3 -> {
						List<LTI13Tool> tools = lti13Service.getTools(LTI13ToolType.MEDIASITE_GLOBAL);
						LTI13Tool tool;
						if (tools.isEmpty()) {
							tool = lti13Service.createExternalTool(
									mediaSiteModule.getServerName(), 
									lti13RedirectUrlEl.getValue(),
									lti13Service.newClientId(),
									lti13InitiateLoginUrlEl.getValue(),
									lti13RedirectUrlEl.getValue(),
									LTI13ToolType.MEDIASITE_GLOBAL);
							tool.setPublicKeyTypeEnum(LTI13Tool.PublicKeyType.URL);
							tool.setPublicKeyUrl(lti13JwksUrlEl.getValue());
							tool = lti13Service.updateTool(tool);
							LTI13ToolDeployment deployment = lti13Service.createToolDeployment(
									null, LTI13ToolDeploymentType.MULTIPLE_CONTEXTS,
									UUID.randomUUID().toString(), tool);
							mediaSiteModule.setLti13ToolKey(tool.getKey());
							mediaSiteModule.setLti13DeploymentKey(deployment.getKey());
						} else {
							tool = tools.get(0);
							tool.setInitiateLoginUrl(lti13InitiateLoginUrlEl.getValue());
							tool.setToolUrl(lti13RedirectUrlEl.getValue());
							tool.setRedirectUrl(lti13RedirectUrlEl.getValue());
							tool.setPublicKeyUrl(lti13JwksUrlEl.getValue());
							tool = lti13Service.updateTool(tool);
						}
						mediaSiteModule.setLti13BaseUrl(lti13BaseUrlEl.getValue());
						lti13ClientIdEl.setValue(tool.getClientId());
						List<LTI13ToolDeployment> deployments = lti13Service.getToolDeploymentByTool(tool);
						if (!deployments.isEmpty()) {
							lti13DeploymentIdEl.setValue(deployments.get(0).getDeploymentId());
						}
					}
				}
			}
			mediaSiteModule.setSupressDataTransmissionAgreement(supressDataTransmissionEl.isAtLeastSelected(1));
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			globalServerEnabledEl.setVisible(enabledEl.isAtLeastSelected(1));
			updateUi();
		} else if (source == globalServerEnabledEl) {
			updateUi();
		} else if (source == ltiVersionEl) {
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
		
		if (ltiVersionEl != null) {
			ltiVersionEl.select(mediaSiteModule.getLtiVersion().name(), true);
		}
		
		enterpriseKeyEl.setValue(mediaSiteModule.getEnterpriseKey());
		enterpriseSecretEl.setValue(mediaSiteModule.getEnterpriseSecret());
		baseUrlEl.setValue(mediaSiteModule.getBaseURL());
		administrationUrlEl.setValue(mediaSiteModule.getAdministrationURL());
		usernamePropertyKeyEl.setValue(mediaSiteModule.getUsernameProperty());

		Long toolKey = mediaSiteModule.getLti13ToolKey();
		if (toolKey != null) {
			LTI13Tool tool = lti13Service.getToolByKey(toolKey);
			if (tool != null) {
				lti13ClientIdEl.setValue(StringHelper.blankIfNull(tool.getClientId()));
				lti13InitiateLoginUrlEl.setValue(StringHelper.blankIfNull(tool.getInitiateLoginUrl()));
				lti13RedirectUrlEl.setValue(StringHelper.blankIfNull(tool.getRedirectUrl()));
				lti13JwksUrlEl.setValue(StringHelper.blankIfNull(tool.getPublicKeyUrl()));
				Long deploymentKey = mediaSiteModule.getLti13DeploymentKey();
				if (deploymentKey != null) {
					LTI13ToolDeployment deployment = lti13Service.getToolDeploymentByKey(deploymentKey);
					if (deployment != null) {
						lti13DeploymentIdEl.setValue(deployment.getDeploymentId());
					}
				}
				lti13BaseUrlEl.setValue(StringHelper.blankIfNull(mediaSiteModule.getLti13BaseUrl()));
			}
		}

		supressDataTransmissionEl.select("on", mediaSiteModule.isSupressDataTransmissionAgreement());
	}
	
	public void loadFromCourseNodeConfig(ModuleConfiguration config) {
		lti13ClientIdEl.setValue("");
		lti13InitiateLoginUrlEl.setValue("");
		lti13RedirectUrlEl.setValue("");
		lti13JwksUrlEl.setValue("");
		lti13DeploymentIdEl.setValue("");
		lti13BaseUrlEl.setValue("");

		ltiVersionEl.select(config.getStringValue(MediaSiteCourseNode.CONFIG_LTI_VERSION, LtiVersion.lti_1_1.name()), true);

		LtiVersion ltiVersion = LtiVersion.valueOf(ltiVersionEl.getSelectedKey());
		
		switch (ltiVersion) {
			case lti_1_1 -> {
				enterpriseKeyEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_KEY));
				enterpriseSecretEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_SECRET));
				baseUrlEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_SERVER_URL));
				administrationUrlEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_ADMINISTRATION_URL));
				usernamePropertyKeyEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_USER_NAME_KEY, mediaSiteModule.getUsernameProperty()));
			}
			case lti_1_3 -> {
				String courseToolKeyStr = config.getStringValue(MediaSiteCourseNode.CONFIG_LTI13_TOOL_KEY);
				if (StringHelper.containsNonWhitespace(courseToolKeyStr)) {
					LTI13Tool courseTool = lti13Service.getToolByKey(Long.valueOf(courseToolKeyStr));
					if (courseTool != null) {
						lti13ClientIdEl.setValue(StringHelper.blankIfNull(courseTool.getClientId()));
						lti13InitiateLoginUrlEl.setValue(StringHelper.blankIfNull(courseTool.getInitiateLoginUrl()));
						lti13RedirectUrlEl.setValue(StringHelper.blankIfNull(courseTool.getRedirectUrl()));
						lti13JwksUrlEl.setValue(StringHelper.blankIfNull(courseTool.getPublicKeyUrl()));
						List<LTI13ToolDeployment> deployments = lti13Service.getToolDeploymentByTool(courseTool);
						if (!deployments.isEmpty()) {
							lti13DeploymentIdEl.setValue(deployments.get(0).getDeploymentId());
						}
						lti13BaseUrlEl.setValue(config.getStringValue(MediaSiteCourseNode.CONFIG_LTI13_BASE_URL, ""));
					}
				}
			}
		}

		supressDataTransmissionEl.select("on", config.getBooleanSafe(MediaSiteCourseNode.CONFIG_SUPRESS_AGREEMENT, mediaSiteModule.isSupressDataTransmissionAgreement()));
	
		updateUi();
	}
	
	public boolean saveToModuleConfiguration(UserRequest ureq, ModuleConfiguration config) {
		if (validateFormLogic(ureq)) {
			config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, true);
			config.setStringValue(MediaSiteCourseNode.CONFIG_LTI_VERSION, ltiVersionEl.getSelectedKey());

			LtiVersion ltiVersion = LtiVersion.valueOf(ltiVersionEl.getSelectedKey());
			switch (ltiVersion) {
				case lti_1_1 -> {
					config.setStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_KEY, enterpriseKeyEl.getValue());
					config.setStringValue(MediaSiteCourseNode.CONFIG_PRIVATE_SECRET, enterpriseSecretEl.getValue());
					config.setStringValue(MediaSiteCourseNode.CONFIG_USER_NAME_KEY, usernamePropertyKeyEl.getValue());
					config.setStringValue(MediaSiteCourseNode.CONFIG_SERVER_URL, baseUrlEl.getValue());
					config.setStringValue(MediaSiteCourseNode.CONFIG_ADMINISTRATION_URL, administrationUrlEl.getValue());
				}
				case lti_1_3 -> {
					String courseToolKeyStr = config.getStringValue(MediaSiteCourseNode.CONFIG_LTI13_TOOL_KEY);
					LTI13Tool courseTool;
					if (!StringHelper.containsNonWhitespace(courseToolKeyStr)) {
						courseTool = lti13Service.createExternalTool(
								mediaSiteModule.getServerName(), 
								lti13RedirectUrlEl.getValue(),
								lti13Service.newClientId(),
								lti13InitiateLoginUrlEl.getValue(),
								lti13RedirectUrlEl.getValue(),
								LTI13ToolType.MEDIASITE_COURSE);
						courseTool.setPublicKeyTypeEnum(LTI13Tool.PublicKeyType.URL);
						courseTool.setPublicKeyUrl(lti13JwksUrlEl.getValue());
						courseTool = lti13Service.updateTool(courseTool);
						lti13Service.createToolDeployment(null, LTI13ToolDeploymentType.SINGLE_CONTEXT,
								UUID.randomUUID().toString(), courseTool);
						config.setStringValue(MediaSiteCourseNode.CONFIG_LTI13_TOOL_KEY, String.valueOf(courseTool.getKey()));
					} else {
						courseTool = lti13Service.getToolByKey(Long.valueOf(courseToolKeyStr));
						if (courseTool != null) {
							courseTool.setInitiateLoginUrl(lti13InitiateLoginUrlEl.getValue());
							courseTool.setToolUrl(lti13RedirectUrlEl.getValue());
							courseTool.setRedirectUrl(lti13RedirectUrlEl.getValue());
							courseTool.setPublicKeyUrl(lti13JwksUrlEl.getValue());
							lti13Service.updateTool(courseTool);
						}
					}
					config.setStringValue(MediaSiteCourseNode.CONFIG_LTI13_BASE_URL, lti13BaseUrlEl.getValue());
				}
			}

			config.setBooleanEntry(MediaSiteCourseNode.CONFIG_SUPRESS_AGREEMENT, supressDataTransmissionEl.isAtLeastSelected(1));
			
			return true;
		}
		
		config.setBooleanEntry(MediaSiteCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN, false);
		
		return false;
	}
	
	private void updateUi() {
		if (usedInAdministration) {
			updateUiInAdministration();
		} else {
			updateUiInCourse();
		}
	}

	private void updateUiInAdministration() {
		boolean moduleEnabled = enabledEl.isAtLeastSelected(1);
		globalServerEnabledEl.setVisible(moduleEnabled);
		boolean ltiVisible = moduleEnabled && globalServerEnabledEl.isAtLeastSelected(1);

		ltiCommonFormItems.stream().filter(Objects::nonNull).forEach(item -> item.setVisible(ltiVisible));

		final LtiVersion ltiVersion = LtiVersion.valueOf(ltiVersionEl.getSelectedKey());

		lti11FormItems.stream().filter(Objects::nonNull)
				.forEach(item -> item.setVisible(ltiVisible && LtiVersion.lti_1_1.name().equals(ltiVersionEl.getSelectedKey())));
		lti13FormItems.stream().filter(Objects::nonNull)
				.forEach(item -> item.setVisible(ltiVisible && LtiVersion.lti_1_3.name().equals(ltiVersionEl.getSelectedKey())));

		supressDataTransmissionEl.setVisible(ltiVisible);
	}

	private void updateUiInCourse() {
		lti11FormItems.stream().filter(Objects::nonNull)
				.forEach(item -> item.setVisible(LtiVersion.lti_1_1.name().equals(ltiVersionEl.getSelectedKey())));
		lti13FormItems.stream().filter(Objects::nonNull)
				.forEach(item -> item.setVisible(LtiVersion.lti_1_3.name().equals(ltiVersionEl.getSelectedKey())));
	}

	private boolean checkMandatoryElement(TextElement textElement) {
		if (textElement == null) {
			return true;
		}
		
		if (!textElement.isVisible()) {
			return true;
		}
		
		textElement.clearError();
		
		if (!StringHelper.containsNonWhitespace(textElement.getValue())) {
			textElement.setErrorKey("form.legende.mandatory");
			return false;
		}
		
		return true;
	}
}
