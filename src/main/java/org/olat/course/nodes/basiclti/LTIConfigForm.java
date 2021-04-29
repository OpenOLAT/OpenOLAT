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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.basiclti;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.LTIModule;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author guido
 * @author Charles Severance
 */
public class LTIConfigForm extends FormBasicController {
	
	public static final String CONFIGKEY_13_DEPLOYMENT_KEY = "deploymentKey";
	
	public static final String CONFIGKEY_LTI_VERSION = "ltiversion";
	public static final String CONFIGKEY_LTI_11 = "LTI11";
	public static final String CONFIGKEY_LTI_13 = "LTI13";

	public static final String CONFIGKEY_PASS = "pass";
	public static final String CONFIGKEY_KEY = "key";
	public static final String CONFIGKEY_PORT = "port";
	public static final String CONFIGKEY_URI = "uri";
	public static final String CONFIGKEY_QUERY = "query";
	public static final String CONFIGKEY_HOST = "host";
	public static final String CONFIGKEY_PROTO = "proto";

	public static final String CONFIG_KEY_DEBUG = "debug";
	public static final String CONFIG_KEY_CUSTOM = "custom";
	public static final String CONFIG_KEY_SENDNAME = "sendname";
	public static final String CONFIG_KEY_SENDEMAIL = "sendemail";
  
	public static final String usageIdentifyer = LTIManager.class.getCanonicalName();
	
	private TextElement thost;
	private TextElement tkey;
	private TextElement tpass;
	
	private TextElement clientIdEl;
	private StaticTextElement deploymentIdEl;
	private SingleSelection ltiVersionEl; 
	private SingleSelection publicKeyTypeEl;
	private TextElement publicKeyEl;
	private TextElement publicKeyUrlEl;
	private TextElement initiateLoginUrlEl;
	private TextElement redirectUrlEl;
	
	private MultipleSelectionElement skipLaunchPageEl;
	private MultipleSelectionElement skipAcceptLaunchPageEl;
	private DialogBoxController confirmDialogCtr;
	
	private MultipleSelectionElement sendName;
	private MultipleSelectionElement sendEmail;
	private MultipleSelectionElement doDebug;

	private TextElement scaleFactorEl;
	private TextElement cutValueEl;
	private MultipleSelectionElement ignoreInCourseAssessmentEl;
	private MultipleSelectionElement isAssessableEl;
	private MultipleSelectionElement authorRoleEl;
	private MultipleSelectionElement coachRoleEl;
	private MultipleSelectionElement participantRoleEl;
	private FormLayoutContainer customParamLayout;
	private SingleSelection displayEl;
	private SingleSelection heightEl;
	private SingleSelection widthEl;

	private String fullURI;
	private Boolean doDebugConfig;
	private final boolean ignoreInCourseAssessmentAvailable;
	private boolean isAssessable;
	private String key;
	private String pass;
	
	
	private final String subIdent;
	private final ModuleConfiguration config;
	private final RepositoryEntry courseEntry;
	
	private LTI13Tool tool;
	private LTI13ToolDeployment toolDeployement;
	private LTI13ToolDeployment backupToolDeployement;
	
	private List<NameValuePair> nameValuePairs = new ArrayList<>();
	
	private static final String[] enabledKeys = new String[]{"on"};

	private String[] ltiRolesKeys = new String[]{
			"Learner", "Instructor", "Administrator", "TeachingAssistant", "ContentDeveloper", "Mentor"
	};
	private String[] ltiRolesValues;
	
	private String[] displayKeys = new String[]{
			LTIDisplayOptions.iframe.name(),
			LTIDisplayOptions.fullscreen.name(),
			LTIDisplayOptions.window.name()
	};
	private String[] displayValues;
	
	private String[] customTypeKeys = new String[] {
		"free", "userprops"	
	};
	private String[] customTypeValues;
	
	private String[] heightKeys = new String[]{ BasicLTICourseNode.CONFIG_HEIGHT_AUTO, "460", "480", 
			"500", "520", "540", "560", "580",
			"600", "620", "640", "660", "680",
			"700", "720", "730", "760", "780",
			"800", "820", "840", "860", "880",
			"900", "920", "940", "960", "980",
			"1000", "1020", "1040", "1060", "1080",
			"1100", "1120", "1140", "1160", "1180",
			"1200", "1220", "1240", "1260", "1280",
			"1300", "1320", "1340", "1360", "1380"
	};
	private String[] heightValues;
	private KeyValues userPropKeysValues;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTIModule ltiModule;
	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	/**
	 * Constructor for the tunneling configuration form
	 * @param name
	 * @param config
	 * @param nodeAccessType 
	 * @param withCancel
	 */
	public LTIConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, NodeAccessType nodeAccessType,
			RepositoryEntry courseEntry, String subIdent) {
		super(ureq, wControl);
		this.config = config;
		this.subIdent = subIdent;
		this.courseEntry = courseEntry;
		int configVersion = config.getConfigurationVersion();
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		
		Translator userPropsTranslator = userManager.getPropertyHandlerTranslator(getTranslator());
		
		ltiRolesValues = new String[]{
				translate("roles.lti.learner"),
				translate("roles.lti.instructor"),
				translate("roles.lti.administrator"),
				translate("roles.lti.teachingAssistant"),
				translate("roles.lti.contentDeveloper"),
				translate("roles.lti.mentor")		
		};

		displayValues = new String[]{
				translate("display.config.window.iframe"),
				translate("display.config.window.fullScreen"), 
				translate("display.config.window.window")
		};
		
		heightValues = new String[]{ translate("height.auto"), "460px", "480px", 
				"500px", "520px", "540px", "560px", "580px",
				"600px", "620px", "640px", "660px", "680px",
				"700px", "720px", "730px", "760px", "780px",
				"800px", "820px", "840px", "860px", "880px",
				"900px", "920px", "940px", "960px", "980px",
				"1000px", "1020px", "1040px", "1060px", "1080px",
				"1100px", "1120px", "1140px", "1160px", "1180px",
				"1200px", "1220px", "1240px", "1260px", "1280px",
				"1300px", "1320px", "1340px", "1360px", "1380px"
		};
		
		customTypeValues = new String[]{
				translate("display.config.free"), translate("display.config.free.userprops")
		};
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, true);
		userPropKeysValues = new KeyValues();
		userPropKeysValues.add(KeyValues.entry(LTIManager.USER_NAME_PROP, userPropsTranslator.translate("form.name.username")));
		for (int i=userPropertyHandlers.size(); i-->0; ) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			userPropKeysValues.add(KeyValues.entry(handler.getName(), userPropsTranslator.translate(handler.i18nFormElementLabelKey())));
		}
		
		String proto = (String)config.get(CONFIGKEY_PROTO);
		String host = (String)config.get(CONFIGKEY_HOST);
		String uri = (String)config.get(CONFIGKEY_URI);
		if (uri != null && uri.length() > 0 && uri.charAt(0) == '/')
			uri = uri.substring(1);
		String query = null;
		if (configVersion >= 2) {
			//query string is available since config version 2
			query = (String) config.get(LTIConfigForm.CONFIGKEY_QUERY);
		}
		Integer port = (Integer)config.get(CONFIGKEY_PORT);
		
		key = (String)config.get(CONFIGKEY_KEY);
		if (key == null) key = "";
		
		pass = (String)config.get(CONFIGKEY_PASS);
		if (pass == null) pass = "";
		
		fullURI = getFullURL(proto, host, port, uri, query).toString();

		doDebugConfig = config.getBooleanEntry(CONFIG_KEY_DEBUG);
		if (doDebugConfig == null) doDebugConfig = Boolean.FALSE;
    
		Boolean assessable = config.getBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		isAssessable = assessable != null && assessable.booleanValue();
		
		if(CONFIGKEY_LTI_13.equals(config.getStringValue(CONFIGKEY_LTI_VERSION, CONFIGKEY_LTI_11))) {
			toolDeployement = lti13Service.getToolDeployment(courseEntry, subIdent);
			tool = toolDeployement == null ? null: toolDeployement.getTool();	
		}

		initForm(ureq);
		updateUI();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.title");
		setFormContextHelp("Other#_lti_config");
		formLayout.setElementCssClass("o_sel_lti_config_form");
		
		KeyValues kValues = new KeyValues();
		kValues.add(KeyValues.entry(CONFIGKEY_LTI_11, translate("config.lti.11")));
		if(lti13Module.isEnabled()) {
			kValues.add(KeyValues.entry(CONFIGKEY_LTI_13, translate("config.lti.13")));
			List<LTI13Tool> tools = lti13Service.getTools(LTI13ToolType.EXT_TEMPLATE);
			for(LTI13Tool template:tools) {
				kValues.add(KeyValues.entry(template.getKey().toString(), template.getToolName()));
			}
		} else if(tool != null) {
			if(tool.getToolTypeEnum() == LTI13ToolType.EXT_TEMPLATE) {
				kValues.add(KeyValues.entry(tool.getKey().toString(), tool.getToolName()));
			} else {
				kValues.add(KeyValues.entry(CONFIGKEY_LTI_13, translate("config.lti.13")));
			}
		}
		ltiVersionEl = uifactory.addDropdownSingleselect("config.lti.version", "config.lti.version", formLayout, kValues.keys(), kValues.values());
		ltiVersionEl.addActionListener(FormEvent.ONCHANGE);
		String version = config.getStringValue(CONFIGKEY_LTI_VERSION, CONFIGKEY_LTI_11);
		if(tool != null && ltiVersionEl.containsKey(tool.getKey().toString())) {
			ltiVersionEl.select(tool.getKey().toString(), true);
		} else if(kValues.containsKey(version)) {
			ltiVersionEl.select(version, true);
		} else {
			ltiVersionEl.select(CONFIGKEY_LTI_13, true);
		}
		
		thost = uifactory.addTextElement("host", "LTConfigForm.url", 255, fullURI, formLayout);
		thost.setElementCssClass("o_sel_lti_config_title");
		thost.setExampleKey("LTConfigForm.url.example", null);
		thost.setMandatory(true);

		initLti10Form(formLayout);
		initLti13Form(formLayout);
		
		initLaunchForm(formLayout);
		initAttributesForm(formLayout);
		initRolesForm(formLayout);
	
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	protected void initLti13Form(FormItemContainer formLayout) {
		if(toolDeployement != null && StringHelper.containsNonWhitespace(toolDeployement.getTargetUrl())) {
			thost.setValue(toolDeployement.getTargetUrl());
		} else if(tool != null) {
			thost.setValue(tool.getToolUrl());
		}
		
		String clientId = tool == null ? null : tool.getClientId();
		clientIdEl = uifactory.addTextElement("config.client.id", "config.client.id", 255, clientId, formLayout);

		String deploymentId = toolDeployement == null ? null : toolDeployement.getDeploymentId();
		deploymentIdEl = uifactory.addStaticTextElement("config.deployment.id", deploymentId, formLayout);
		deploymentIdEl.setExampleKey("config.deployment.id.example", null);
		
		KeyValues kValues = new KeyValues();
		kValues.add(KeyValues.entry(PublicKeyType.KEY.name(), translate("config.public.key.type.key")));
		kValues.add(KeyValues.entry(PublicKeyType.URL.name(), translate("config.public.key.type.url")));
		PublicKeyType publicKeyType = tool == null ? null : tool.getPublicKeyTypeEnum();
		publicKeyTypeEl = uifactory.addDropdownSingleselect("config.public.key.type", "config.public.key.type", formLayout, kValues.keys(), kValues.values());
		publicKeyTypeEl.addActionListener(FormEvent.ONCHANGE);
		if(publicKeyType != null && kValues.containsKey(publicKeyType.name())) {
			publicKeyTypeEl.select(publicKeyType.name(), true);
		} else {
			publicKeyTypeEl.select(kValues.keys()[0], true);
		}
		
		String publicKey = tool == null ? null : tool.getPublicKey();
		publicKeyEl = uifactory.addTextAreaElement("config.public.key.value", "config.public.key.value", -1, 15, 60, false, true, true, publicKey, formLayout);
		
		String publicKeyUrl = tool == null ? null : tool.getPublicKeyUrl();
		publicKeyUrlEl = uifactory.addTextElement("config.public.key.url", "config.public.key.url", 255, publicKeyUrl, formLayout);
		
		String initiateLoginUrl = tool == null ? null : tool.getInitiateLoginUrl();
		initiateLoginUrlEl = uifactory.addTextElement("config.initiate.login.url", "config.initiate.login.url", 255, initiateLoginUrl, formLayout);
		String redirectUrl = tool == null ? null : tool.getRedirectUrl();
		redirectUrlEl = uifactory.addTextAreaElement("config.redirect.url", "config.redirect.url", -1, 4, 60, false, false, true, redirectUrl, formLayout);
		redirectUrlEl.setHelpTextKey("config.redirect.url.hint", null);
	}
	
	private void updateLtiVersion() {
		String versionKey = ltiVersionEl.getSelectedKey();
		if(CONFIGKEY_LTI_11.equals(versionKey)) {
			// do something
		} else if(CONFIGKEY_LTI_13.equals(versionKey)) {
			if(toolDeployement != null && toolDeployement.getTool().getToolTypeEnum() == LTI13ToolType.EXT_TEMPLATE) {
				backupToolDeployement = toolDeployement;
				toolDeployement = null;
			}
			thost.setValue(null);
			clientIdEl.setValue(null);
			deploymentIdEl.setValue("");
			publicKeyEl.setValue(null);
			publicKeyUrlEl.setValue(null);
			initiateLoginUrlEl.setValue(null);
			redirectUrlEl.setValue(null);
		} else if(StringHelper.isLong(versionKey)) {
			tool = lti13Service.getToolByKey(Long.valueOf(versionKey));
			boolean configurable = tool.getToolTypeEnum() == LTI13ToolType.EXTERNAL;
			
			// be nice and try to save the data
			String targetUrl = null;
			if(toolDeployement != null && toolDeployement.getTool().equals(tool)) {
				targetUrl = toolDeployement.getTargetUrl();
			}
			if(targetUrl == null && backupToolDeployement != null && backupToolDeployement.getTool().equals(tool)) {
				targetUrl = backupToolDeployement.getTargetUrl();
			}
			if(StringHelper.containsNonWhitespace(targetUrl)) {
				thost.setValue(targetUrl);
			} else {
				thost.setValue(tool.getToolUrl());
			}
			clientIdEl.setValue(tool.getClientId());
			clientIdEl.setEnabled(false);
			publicKeyTypeEl.select(tool.getPublicKeyTypeEnum().name(), true);
			publicKeyTypeEl.setEnabled(configurable);
			publicKeyEl.setValue(tool.getPublicKey());
			publicKeyEl.setEnabled(configurable);
			publicKeyUrlEl.setValue(tool.getPublicKeyUrl());
			publicKeyUrlEl.setEnabled(configurable);
			initiateLoginUrlEl.setValue(tool.getInitiateLoginUrl());
			initiateLoginUrlEl.setEnabled(configurable);
			redirectUrlEl.setValue(tool.getRedirectUrl());
			redirectUrlEl.setEnabled(configurable);
			
			if(toolDeployement != null && !toolDeployement.getTool().equals(tool)) {
				backupToolDeployement = toolDeployement;
				toolDeployement = null;
				deploymentIdEl.setValue("");
			} else if(backupToolDeployement != null && backupToolDeployement.getTool().equals(tool)) {
				toolDeployement = backupToolDeployement;
				backupToolDeployement = null;
				deploymentIdEl.setValue(toolDeployement.getDeploymentId());
			}
		}
		updateUI();
	}
	
	
	private void updateUI() {
		String selectedVersionKey = ltiVersionEl.getSelectedKey();
		boolean lti13 = !CONFIGKEY_LTI_11.equals(selectedVersionKey);
		boolean sharedTool = StringHelper.isLong(selectedVersionKey)
				&& tool != null && tool.getToolTypeEnum() == LTI13ToolType.EXT_TEMPLATE;
		
		// LTI 1.3
		clientIdEl.setVisible(lti13);
		clientIdEl.setEnabled(!sharedTool);
		deploymentIdEl.setVisible(lti13);
		publicKeyTypeEl.setVisible(lti13);
		publicKeyTypeEl.setEnabled(!sharedTool);
		publicKeyEl.setVisible(lti13 && PublicKeyType.KEY.name().equals(publicKeyTypeEl.getSelectedKey()));
		publicKeyEl.setEnabled(!sharedTool);
		publicKeyUrlEl.setVisible(lti13 && PublicKeyType.URL.name().equals(publicKeyTypeEl.getSelectedKey()));
		publicKeyUrlEl.setEnabled(!sharedTool);
		initiateLoginUrlEl.setVisible(lti13);
		initiateLoginUrlEl.setEnabled(!sharedTool);
		redirectUrlEl.setVisible(lti13);
		redirectUrlEl.setEnabled(!sharedTool);
		
		// LTI 1.1
		tkey.setVisible(!lti13);
		tpass.setVisible(!lti13);
		
		// Assessment
		boolean assessEnabled = isAssessableEl.isAtLeastSelected(1);
		scaleFactorEl.setVisible(assessEnabled);
		cutValueEl.setVisible(assessEnabled);
		ignoreInCourseAssessmentEl.setVisible(ignoreInCourseAssessmentAvailable && assessEnabled);
		
		boolean newWindow = LTIDisplayOptions.window.name().equals(displayEl.getSelectedKey());
		boolean sizeVisible = !newWindow || !lti13;
		heightEl.setVisible(sizeVisible);
		widthEl.setVisible(sizeVisible); 
	}
	
	protected void initLti10Form(FormItemContainer formLayout) {
		tkey  = uifactory.addTextElement ("key","LTConfigForm.key", 255, key, formLayout);
		tkey.setElementCssClass("o_sel_lti_config_key");
		tkey.setExampleKey ("LTConfigForm.key.example", null);
		tkey.setMandatory(true);
		
		tpass = uifactory.addTextElement ("pass","LTConfigForm.pass", 255, pass, formLayout);
		tpass.setElementCssClass("o_sel_lti_config_pass");
		tpass.setExampleKey("LTConfigForm.pass.example", null);
		tpass.setMandatory(true);
	}
	
	protected void initLaunchForm(FormItemContainer formLayout) {
		uifactory.addSpacerElement("launch", formLayout, false);

		String[] enableValues = new String[]{ translate("on") };	
		skipLaunchPageEl = uifactory.addCheckboxesHorizontal("display.config.skipLaunchPage", formLayout, enabledKeys, enableValues);
		if(ltiModule.isForceLaunchPage()) {
			skipLaunchPageEl.select(enabledKeys[0], true);
			skipLaunchPageEl.setEnabled(false);
		} else if (config.getBooleanSafe(BasicLTICourseNode.CONFIG_SKIP_LAUNCH_PAGE)) {
			skipLaunchPageEl.select(enabledKeys[0], true);
		}
			
		skipAcceptLaunchPageEl = uifactory.addCheckboxesHorizontal("display.config.skipAcceptLaunchPage", formLayout, enabledKeys, enableValues);
		if(ltiModule.isForceLaunchPage()) {
			skipAcceptLaunchPageEl.select(enabledKeys[0], true);
			skipAcceptLaunchPageEl.setEnabled(false);
		} else if (config.getBooleanSafe(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE)) {
			skipAcceptLaunchPageEl.select(enabledKeys[0], true);
		}
		skipAcceptLaunchPageEl.setHelpTextKey("display.config.skipAcceptLaunchPageWarning", null);
		skipAcceptLaunchPageEl.addActionListener(FormEvent.ONCHANGE);
	}

	protected void initAttributesForm(FormItemContainer formLayout) {	
		uifactory.addSpacerElement("attributes", formLayout, false);

		sendName = uifactory.addCheckboxesHorizontal("sendName", "display.config.sendName", formLayout, new String[]{"xx"}, new String[]{null});
		sendName.addActionListener(FormEvent.ONCHANGE);
		if((toolDeployement != null && toolDeployement.getSendUserAttributesList().contains(UserConstants.LASTNAME))
				|| config.getBooleanSafe(CONFIG_KEY_SENDNAME, false)) {
			sendName.select("xx", true);
		}
		
		sendEmail = uifactory.addCheckboxesHorizontal("sendEmail", "display.config.sendEmail", formLayout, new String[]{"xx"}, new String[]{null});
		sendEmail.addActionListener(FormEvent.ONCHANGE);
		if((toolDeployement != null && toolDeployement.getSendUserAttributesList().contains(UserConstants.EMAIL))
				|| config.getBooleanSafe(CONFIG_KEY_SENDEMAIL, false)) {
			sendEmail.select("xx", true);
		}
		
		boolean sendEnabled = sendName.isSelected(0) || sendEmail.isSelected(0);
		skipAcceptLaunchPageEl.setVisible(sendEnabled);
		
		String page = velocity_root + "/custom.html";
		customParamLayout = FormLayoutContainer.createCustomFormLayout("custom_fields", getTranslator(), page);
		customParamLayout.setRootForm(mainForm);
		customParamLayout.setLabel("display.config.custom", null);
		formLayout.add(customParamLayout);
		customParamLayout.contextPut("nameValuePairs", nameValuePairs);
		
		String customConfig = toolDeployement != null ? toolDeployement.getSendCustomAttributes() : (String)config.get(CONFIG_KEY_CUSTOM);
		updateNameValuePair(customConfig);
		if(nameValuePairs.isEmpty()) {
			createNameValuePair("", "", -1);
		}
	}
	
	protected void initRolesForm(FormItemContainer formLayout) {
		uifactory.addSpacerElement("roles", formLayout, false);
		uifactory.addStaticTextElement("roletitle", "roles.title.oo", translate("roles.title.lti"), formLayout);	
		
		authorRoleEl = uifactory.addCheckboxesHorizontal("author", "author.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		String authorDeploymentRoles = toolDeployement == null ? null : toolDeployement.getAuthorRoles();
		udpateRoles(authorRoleEl, BasicLTICourseNode.CONFIG_KEY_AUTHORROLE, authorDeploymentRoles, "Instructor,Administrator,TeachingAssistant,ContentDeveloper,Mentor"); 
		coachRoleEl = uifactory.addCheckboxesHorizontal("coach", "coach.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		String coachDeploymentRoles = toolDeployement == null ? null : toolDeployement.getCoachRoles();
		udpateRoles(coachRoleEl, BasicLTICourseNode.CONFIG_KEY_COACHROLE, coachDeploymentRoles, "Instructor,TeachingAssistant,Mentor");
		participantRoleEl = uifactory.addCheckboxesHorizontal("participant", "participant.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		String participantsDeploymentRoles = toolDeployement == null ? null : toolDeployement.getParticipantRoles();
		udpateRoles(participantRoleEl, BasicLTICourseNode.CONFIG_KEY_PARTICIPANTROLE, participantsDeploymentRoles, "Learner"); 
		
		uifactory.addSpacerElement("scoring", formLayout, false);
		
		//add score info
		String[] assessableKeys = new String[]{ "on" };
		String[] assessableValues = new String[]{ "" };
		isAssessableEl = uifactory.addCheckboxesHorizontal("isassessable", "assessable.label", formLayout, assessableKeys, assessableValues);
		isAssessableEl.setElementCssClass("o_sel_lti_config_assessable");
		isAssessableEl.addActionListener(FormEvent.ONCHANGE);
		if(isAssessable) {
			isAssessableEl.select("on", true);
		}
	
		Float scaleValue = config.getFloatEntry(BasicLTICourseNode.CONFIG_KEY_SCALEVALUE);
		String scaleFactor = scaleValue == null ? "1.0" : scaleValue.toString();
		scaleFactorEl = uifactory.addTextElement("scale", "scaleFactor", 10, scaleFactor, formLayout);
		scaleFactorEl.setElementCssClass("o_sel_lti_config_scale");
		scaleFactorEl.setDisplaySize(3);
		scaleFactorEl.setVisible(isAssessable);
		
		Float cutValue = config.getFloatEntry(BasicLTICourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		String cut = cutValue == null ? "" : cutValue.toString();
		cutValueEl = uifactory.addTextElement("cutvalue", "cutvalue.label", 10, cut, formLayout);
		cutValueEl.setElementCssClass("o_sel_lti_config_cutval");
		cutValueEl.setDisplaySize(3);
		cutValueEl.setVisible(isAssessable);
		
		ignoreInCourseAssessmentEl = uifactory.addCheckboxesHorizontal("ignore.in.course.assessment", formLayout,
				new String[] { "xx" }, new String[] { null });
		boolean ignoreInCourseAssessment = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		ignoreInCourseAssessmentEl.select(ignoreInCourseAssessmentEl.getKey(0), ignoreInCourseAssessment);
		ignoreInCourseAssessmentEl.setVisible(ignoreInCourseAssessmentAvailable && isAssessable);
		
		uifactory.addSpacerElement("display", formLayout, false);
		
		String display = toolDeployement != null ? toolDeployement.getDisplay()
				: config.getStringValue(BasicLTICourseNode.CONFIG_DISPLAY, "iframe");
		displayEl = uifactory.addRadiosVertical("display.window", "display.config.window", formLayout, displayKeys, displayValues);
		displayEl.addActionListener(FormEvent.ONCHANGE);
		for(String displayKey:displayKeys) {
			if(displayKey.equals(display)) {
				displayEl.select(displayKey, true);
			}
		}
		
		String height = toolDeployement != null ? toolDeployement.getDisplayHeight()
				: config.getStringValue(BasicLTICourseNode.CONFIG_HEIGHT, BasicLTICourseNode.CONFIG_HEIGHT_AUTO);
		heightEl = uifactory.addDropdownSingleselect("display.height", "display.config.height", formLayout, heightKeys, heightValues, null);
		for(String heightKey:heightKeys) {
			if(heightKey.equals(height)) {
				heightEl.select(heightKey, true);
			}
		}

		String width = toolDeployement != null ? toolDeployement.getDisplayWidth()
				: config.getStringValue(BasicLTICourseNode.CONFIG_WIDTH, BasicLTICourseNode.CONFIG_HEIGHT_AUTO);
		widthEl = uifactory.addDropdownSingleselect("display.width", "display.config.width", formLayout, heightKeys, heightValues, null);
		for(String heightKey:heightKeys) {
			if(heightKey.equals(width)) {
				widthEl.select(heightKey, true);
			}
		}
		
		uifactory.addSpacerElement("debug", formLayout, false);
		
		doDebug = uifactory.addCheckboxesHorizontal("doDebug", "display.config.doDebug", formLayout, new String[]{"xx"}, new String[]{null});
		doDebug.select("xx", doDebugConfig);
	
	}
	
	@Override
	protected void doDispose() {
		// Dispose confirm dialog controller since it isn't listend to.
		if (confirmDialogCtr != null) {
			removeAsListenerAndDispose(confirmDialogCtr);
		}
	}
	
	private void updateNameValuePair(String custom) {
		if(StringHelper.containsNonWhitespace(custom)) {
			String[] params = custom.split("[\n;]");
			for (int i = 0; i < params.length; i++) {
				String param = params[i];
				if (StringHelper.containsNonWhitespace(param)) {
					int pos = param.indexOf("=");
					if (pos > 1 && pos + 1 < param.length()) {
						String key = BasicLTIUtil.mapKeyName(param.substring(0, pos));
						if(key != null) {
							String value = param.substring(pos + 1).trim();
							if (value.length() >= 1) {
								createNameValuePair(key, value, -1);
							}
						}
					}
				}
			}
		}
	}
	
	private void createNameValuePair(String key, String value, int index) {
		String guid = Long.toString(CodeHelper.getRAMUniqueID());
		NameValuePair pair = new NameValuePair(guid);
		
		TextElement nameEl = uifactory.addTextElement("name_" + guid, null, 100, key, customParamLayout);
		nameEl.setDisplaySize(16);
		pair.setNameEl(nameEl);
		
		SingleSelection typeEl = uifactory.addDropdownSingleselect("typ_" + guid, null, customParamLayout, customTypeKeys, customTypeValues, null);
		typeEl.setUserObject(pair);
		typeEl.addActionListener(FormEvent.ONCHANGE);
		pair.setCustomType(typeEl);
		
		boolean userprops = value != null && value.startsWith(LTIManager.USER_PROPS_PREFIX);
		if(userprops) {
			typeEl.select("userprops", true);
			value = value.substring(LTIManager.USER_PROPS_PREFIX.length(), value.length());
		} else {
			typeEl.select("free", true);
		}
		
		SingleSelection userPropsChoice = uifactory.addDropdownSingleselect("userprops_" + guid, null, customParamLayout,
				userPropKeysValues.keys(), userPropKeysValues.values(), null);
		userPropsChoice.setUserObject(pair);
		userPropsChoice.setVisible(userprops);
		if(userprops && userPropKeysValues.containsKey(value)) {
			userPropsChoice.select(value, true);
		}
		pair.setUserPropsChoice(userPropsChoice);

		TextElement valEl = uifactory.addTextElement("val_" + guid, null, 100, value, customParamLayout);
		valEl.setDisplaySize(16);
		valEl.setVisible(!userprops);
		pair.setValueEl(valEl);
		
		FormLink addButton = uifactory.addFormLink("add_" + guid, "add", null, customParamLayout, Link.BUTTON_XSMALL);
		addButton.setUserObject(pair);
		pair.setAddButton(addButton);
		FormLink removeButton = uifactory.addFormLink("rm_" + guid, "remove", null, customParamLayout, Link.BUTTON_XSMALL);
		removeButton.setUserObject(pair);
		pair.setRemoveButton(removeButton);
		
		if(index < 0 || index >= nameValuePairs.size()) {
			nameValuePairs.add(pair);
		} else {
			nameValuePairs.add(index, pair);
		}
	}
	
	private void udpateRoles(MultipleSelectionElement roleEl, String configKey, String deploymentRoles, String defaultRoles) {
		Object configRoles = config.get(configKey);
		String roles = defaultRoles;
		if(StringHelper.containsNonWhitespace(deploymentRoles)) {
			roles = deploymentRoles;
		} else if(configRoles instanceof String) {
			roles = (String)configRoles;
		}
		String[] roleArr = roles.split(",");
		for(String role:roleArr) {
			roleEl.select(role, true);
		}
	}
	
	private String getRoles(MultipleSelectionElement roleEl) {
		StringBuilder sb = new StringBuilder();
		for(String role:roleEl.getSelectedKeys()) {
			if(sb.length() > 0) sb.append(',');
			sb.append(role);
		}
		return sb.toString();
	}
	
	protected static StringBuilder getFullURL(String proto, String host, Integer port, String uri, String query) {
		StringBuilder fullURL = new StringBuilder();
		if (proto != null && host != null) {
			fullURL.append(proto).append("://");
			fullURL.append(host);
			if (port != null) {
				if (proto.equals("http") || proto.equals("https")) {
					if (proto.equals("http") && port.intValue() != 80) fullURL.append(":" + port);
					else if (proto.equals("https") && port.intValue() != 443) fullURL.append(":" + port);
				}	else fullURL.append(":" + port);
			}
			if (uri == null) {
				fullURL.append("/");
			} else {
				// append "/" if not already there, old configurations might have no "/" 
				if (uri.indexOf("/") != 0) fullURL.append("/");
				fullURL.append(uri);
			}
			if (query != null) fullURL.append("?").append(query);
		}
		return fullURL;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) { 
		boolean allOk = super.validateFormLogic(ureq);
		try {
			thost.clearError();
			boolean lti13 = CONFIGKEY_LTI_13.equals(ltiVersionEl.getSelectedKey()) || StringHelper.isLong(ltiVersionEl.getSelectedKey());
			URL url = new URL(thost.getValue());
			if(url.getHost() == null) {
				thost.setErrorKey("LTConfigForm.invalidurl", null);
				allOk &= false;
			} else if(lti13 && tool != null
					&& StringHelper.containsNonWhitespace(tool.getToolUrl())
					&& !(new URL(tool.getToolUrl()).getHost().equals(url.getHost()))) {
				thost.setErrorKey("LTConfigForm.urlToolIncompatible", new String[] { new URL(tool.getToolUrl()).getHost() });
				allOk &= false;
			}
		} catch (MalformedURLException e) {
			thost.setErrorKey("LTConfigForm.invalidurl", null);
			allOk &= false;
		}
		if(cutValueEl != null) {
			allOk &= validateFloat(cutValueEl);
			allOk &= validateFloat(scaleFactorEl);
		}
		
		//lti 1.3
		allOk &= validateTextElement(initiateLoginUrlEl, 2000, true);
		allOk &= validateTextElement(redirectUrlEl, 2000, false);
		
		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength, boolean mandatory) {
		boolean allOk = true;

		el.clearError();
		if(el.isVisible() && el.isEnabled()) {
			String val = el.getValue();
			if(!StringHelper.containsNonWhitespace(val) && mandatory) {
				el.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
				el.setErrorKey("input.toolong", new String[]{ Integer.toString(maxLength) });
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validateFloat(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(el.isVisible()) {
			String value = el.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Float.parseFloat(value);
				} catch(Exception e) {
					el.setErrorKey("form.error.wrongFloat", null);
					allOk = false;
				}
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(isAssessableEl == source || displayEl == source || publicKeyTypeEl == source) {
			updateUI();
		} else if(ltiVersionEl == source) {
			updateLtiVersion();
		} else if (sendName == source || sendEmail == source) {
			boolean sendEnabled = sendName.isSelected(0) || sendEmail.isSelected(0);
			skipAcceptLaunchPageEl.setVisible(sendEnabled);
		} else if (source == skipAcceptLaunchPageEl && skipAcceptLaunchPageEl.isAtLeastSelected(1)) {
			confirmDialogCtr = activateYesNoDialog(ureq, null, translate("display.config.skipAcceptLaunchPageConfirm"), confirmDialogCtr);
		} else if(source instanceof FormLink && source.getName().startsWith("add_")) {
			NameValuePair pair = (NameValuePair)source.getUserObject();
			doAddNameValuePair(pair);
		} else if(source instanceof FormLink && source.getName().startsWith("rm_")) {
			NameValuePair pair = (NameValuePair)source.getUserObject();
			doRemoveNameValuePair(pair);
			if(nameValuePairs.isEmpty()) {
				// add a new empty default pair
				createNameValuePair("", "", -1);
			}
		} else if(source instanceof SingleSelection && source.getName().startsWith("typ_")) {
			NameValuePair pair = (NameValuePair)source.getUserObject();
			SingleSelection typeChoice = (SingleSelection)source;
			if("free".equals(typeChoice.getSelectedKey())) {
				pair.getUserPropsChoice().setVisible(false);
				pair.getValueEl().setVisible(true);
			} else if("userprops".equals(typeChoice.getSelectedKey())) {
				pair.getUserPropsChoice().setVisible(true);
				pair.getValueEl().setVisible(false);
			}
			customParamLayout.setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmDialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				skipAcceptLaunchPageEl.select(enabledKeys[0], true);
			} else {
				skipAcceptLaunchPageEl.select(enabledKeys[0], false);
			}
		}
	} 

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	private void doAddNameValuePair(NameValuePair parentPair) {
		int index = nameValuePairs.indexOf(parentPair);
		createNameValuePair("", "", index + 1);
		customParamLayout.setDirty(true);
	}
	
	private void doRemoveNameValuePair(NameValuePair pair) {
		nameValuePairs.remove(pair);
		customParamLayout.setDirty(true);
	}

	/**
	 * @return the updated module configuration using the form data
	 */
	protected ModuleConfiguration getUpdatedConfig() {
		URL url = null;
		try {
			url = new URL(thost.getValue());
		} catch (MalformedURLException e) {
			throw new OLATRuntimeException("MalformedURL in LTConfigForm which should not happen, since we've validated before. URL: " + thost.getValue(), e);
		}
		config.setConfigurationVersion(BasicLTICourseNode.CURRENT_VERSION);
		config.set(CONFIGKEY_PROTO, url.getProtocol());
		config.set(CONFIGKEY_HOST, url.getHost());
		config.set(CONFIGKEY_URI, url.getPath());
		config.set(CONFIGKEY_QUERY, url.getQuery());
		int port = url.getPort();
		config.set(CONFIGKEY_PORT, Integer.valueOf(port != -1 ? port : url.getDefaultPort()));
		
		String ltiVersion = ltiVersionEl.getSelectedKey();
		if(CONFIGKEY_LTI_13.equals(ltiVersion) || StringHelper.isLong(ltiVersion)) {
			config.set(CONFIGKEY_LTI_VERSION, CONFIGKEY_LTI_13);
			return getUpdatedConfigLti13();
		}
		config.set(CONFIGKEY_LTI_VERSION, CONFIGKEY_LTI_11);
		return getUpdatedConfigLti11();
	}
	
	private ModuleConfiguration getUpdatedConfigLti13() {
		String targetUrl = thost.getValue();
		String clientId = clientIdEl.getValue();
		String initiateLoginUrl = initiateLoginUrlEl.getValue();
		String redirectUrl = redirectUrlEl.getValue();
		
		boolean canUpdateTool = tool == null || LTI13ToolType.EXTERNAL.equals(tool.getToolTypeEnum());
		if(tool == null) {
			tool = lti13Service.createExternalTool(courseEntry.getDisplayname(), targetUrl, clientId,
					initiateLoginUrl, redirectUrl, LTI13ToolType.EXTERNAL);
		} else if(canUpdateTool) {
			tool.setToolUrl(targetUrl);
		}
		
		if(canUpdateTool) {
			tool.setInitiateLoginUrl(initiateLoginUrl);
			tool.setRedirectUrl(redirectUrl);
			
			PublicKeyType publicKeyType = PublicKeyType.valueOf(publicKeyTypeEl.getSelectedKey());
			tool.setPublicKeyTypeEnum(publicKeyType);
			if(publicKeyType == PublicKeyType.KEY) {
				tool.setPublicKey(publicKeyEl.getValue());
				tool.setPublicKeyUrl(null);
			} else if(publicKeyType == PublicKeyType.URL) {
				tool.setPublicKey(null);
				tool.setPublicKeyUrl(publicKeyUrlEl.getValue());
			}
			tool = lti13Service.updateTool(tool);
		}
		
		if(backupToolDeployement != null && backupToolDeployement.getTool().equals(tool)) {
			toolDeployement = backupToolDeployement;
			backupToolDeployement = null;
		}
		if(toolDeployement == null || !toolDeployement.getTool().equals(tool)) {
			toolDeployement = lti13Service.createToolDeployment(targetUrl, tool, courseEntry, subIdent);
		} else {
			dbInstance.commit();// make sure the tool is persisted
			toolDeployement = lti13Service.getToolDeploymentByKey(toolDeployement.getKey());
			toolDeployement.setTargetUrl(targetUrl);
		}
		deploymentIdEl.setValue(toolDeployement.getDeploymentId());
		
		List<String> sendAttributes = new ArrayList<>();
		if(sendName.isAtLeastSelected(1)) {
			sendAttributes.add(UserConstants.FIRSTNAME);
			sendAttributes.add(UserConstants.LASTNAME);
		}
		if(sendEmail.isAtLeastSelected(1)) {
			sendAttributes.add(UserConstants.EMAIL);
		}
		toolDeployement.setSendUserAttributesList(sendAttributes);
		toolDeployement.setSendCustomAttributes(getCustomConfig());
		
		toolDeployement.setAuthorRoles(getRoles(authorRoleEl));
		toolDeployement.setCoachRoles(getRoles(coachRoleEl));
		toolDeployement.setParticipantRoles(getRoles(participantRoleEl));
		
		String display = displayEl.isOneSelected() ?displayEl.getSelectedKey() : LTIDisplayOptions.iframe.name();
		toolDeployement.setDisplay(display);
		String height = heightEl.isOneSelected() ? heightEl.getSelectedKey() : null;
		toolDeployement.setDisplayHeight(height);
		String width =  widthEl.isOneSelected() ?  widthEl.getSelectedKey() : null;
		toolDeployement.setDisplayWidth(width);
		
		boolean assessable = isAssessableEl.isAtLeastSelected(1);
		toolDeployement.setAssessable(assessable);
		
		boolean skipLaunchPage = (ltiModule.isForceLaunchPage() || skipAcceptLaunchPageEl.isAtLeastSelected(1))
				&& (sendName.isSelected(0) || sendEmail.isSelected(0));
		toolDeployement.setSkipLaunchPage(skipLaunchPage);

		toolDeployement = lti13Service.updateToolDeployment(toolDeployement);
		tool = toolDeployement.getTool();
		tool.getKey();// prevent lazy loading exception
		
		config.setStringValue(CONFIGKEY_13_DEPLOYMENT_KEY, toolDeployement.getKey().toString());
		getUpdateConfigCommon();
		return config;
	}
	
	private ModuleConfiguration getUpdatedConfigLti11() {
		config.set(CONFIGKEY_KEY, getFormKey());
		config.set(CONFIGKEY_PASS, tpass.getValue());
		config.set(CONFIG_KEY_DEBUG, Boolean.toString(doDebug.isSelected(0)));
		config.set(CONFIG_KEY_CUSTOM, getCustomConfig());
		if (ltiModule.isForceLaunchPage() || skipLaunchPageEl.isAtLeastSelected(1)) {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_LAUNCH_PAGE, Boolean.TRUE);
		} else {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_LAUNCH_PAGE, Boolean.FALSE);
		}

		config.set(CONFIG_KEY_SENDNAME, Boolean.toString(sendName.isSelected(0)));
		config.set(CONFIG_KEY_SENDEMAIL, Boolean.toString(sendEmail.isSelected(0)));
		
		config.set(BasicLTICourseNode.CONFIG_KEY_AUTHORROLE, getRoles(authorRoleEl));
		config.set(BasicLTICourseNode.CONFIG_KEY_COACHROLE, getRoles(coachRoleEl));
		config.set(BasicLTICourseNode.CONFIG_KEY_PARTICIPANTROLE, getRoles(participantRoleEl));
		
		if(displayEl.isOneSelected()) {
			config.set(BasicLTICourseNode.CONFIG_DISPLAY, displayEl.getSelectedKey());
		} else {
			config.set(BasicLTICourseNode.CONFIG_DISPLAY, "iframe");
		}
		if(heightEl.isOneSelected()) {
			config.set(BasicLTICourseNode.CONFIG_HEIGHT, heightEl.getSelectedKey());
		}
		if(widthEl.isOneSelected()) {
			config.set(BasicLTICourseNode.CONFIG_WIDTH, widthEl.getSelectedKey());
		}
		
		if ((ltiModule.isForceLaunchPage() || skipAcceptLaunchPageEl.isAtLeastSelected(1)) && (sendName.isSelected(0) || sendEmail.isSelected(0))) {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE, Boolean.TRUE);
		} else {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE, Boolean.FALSE);
		}
		
		getUpdateConfigCommon();
		return config;
	}
	
	private void getUpdateConfigCommon() {
		if(isAssessableEl.isAtLeastSelected(1)) {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD, Boolean.TRUE);
			
			Float scaleVal = getFloat(scaleFactorEl.getValue());
			if(scaleVal != null && scaleVal.floatValue() > 0.0f) {
				config.set(BasicLTICourseNode.CONFIG_KEY_SCALEVALUE, scaleVal);
			} else {
				config.remove(BasicLTICourseNode.CONFIG_KEY_SCALEVALUE);
			}

			String cutValue = cutValueEl.getValue();
			Float cutVal = getFloat(cutValueEl.getValue());
			if(cutVal != null && cutVal.floatValue() > 0.0f) {
				config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_PASSED_FIELD, Boolean.TRUE);
				config.set(BasicLTICourseNode.CONFIG_KEY_PASSED_CUT_VALUE, cutValue);
			} else {
				config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_PASSED_FIELD, Boolean.FALSE);
				config.remove(BasicLTICourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			}
			
			boolean ignoreInCourseAssessment = ignoreInCourseAssessmentEl.isVisible() && ignoreInCourseAssessmentEl.isAtLeastSelected(1);
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
		} else {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD, Boolean.FALSE);
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_PASSED_FIELD, Boolean.FALSE);
			config.remove(BasicLTICourseNode.CONFIG_KEY_SCALEVALUE);
			config.remove(BasicLTICourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		}
	}
	
	private Float getFloat(String text) {
		Float floatValue = null;
		if(StringHelper.containsNonWhitespace(text)) {
			try {
				floatValue = Float.parseFloat(text);
			} catch(Exception e) {
				//can happens
			}
		}
		return floatValue;
	}
	
	private String getCustomConfig() {
		StringBuilder sb = new StringBuilder();
		for(NameValuePair pair:nameValuePairs) {
			String key = pair.getNameEl().getValue();
			if(!StringHelper.containsNonWhitespace(key)
					|| !pair.getCustomType().isOneSelected()) {
				continue;
			}
			String value = null;
			String type = pair.getCustomType().getSelectedKey();
			if("free".equals(type)) {
				value = pair.getValueEl().getValue();
			} else if("userprops".equals(type)) {
				if(pair.getUserPropsChoice().isOneSelected()) {
					value = LTIManager.USER_PROPS_PREFIX + pair.getUserPropsChoice().getSelectedKey();
				}
			}
			if(!StringHelper.containsNonWhitespace(value)) {
				continue;
			}
				
			if(sb.length() > 0) sb.append(";");
			sb.append(key).append('=').append(value);
		}
		return sb.toString();
	}

	private String getFormKey() {
		if (StringHelper.containsNonWhitespace(tkey.getValue())) {
			return tkey.getValue();
		}
		return null;
	}
	
	public static class NameValuePair {
		private TextElement nameEl;
		private TextElement valueEl;
		private SingleSelection customType;
		private SingleSelection userPropsChoice;
		private FormLink addButton;
		private FormLink removeButton;
		private final String guid;
		
		public NameValuePair(String guid) {
			this.guid = guid;
		}
		
		public String getGuid() {
			return guid;
		}

		public TextElement getNameEl() {
			return nameEl;
		}
		
		public void setNameEl(TextElement nameEl) {
			this.nameEl = nameEl;
		}
		
		public SingleSelection getCustomType() {
			return customType;
		}

		public void setCustomType(SingleSelection customType) {
			this.customType = customType;
		}

		public SingleSelection getUserPropsChoice() {
			return userPropsChoice;
		}

		public void setUserPropsChoice(SingleSelection userPropsChoice) {
			this.userPropsChoice = userPropsChoice;
		}

		public TextElement getValueEl() {
			return valueEl;
		}
		
		public void setValueEl(TextElement valueEl) {
			this.valueEl = valueEl;
		}

		public FormLink getAddButton() {
			return addButton;
		}

		public void setAddButton(FormLink addButton) {
			this.addButton = addButton;
		}

		public FormLink getRemoveButton() {
			return removeButton;
		}

		public void setRemoveButton(FormLink removeButton) {
			this.removeButton = removeButton;
		}
	}
}
