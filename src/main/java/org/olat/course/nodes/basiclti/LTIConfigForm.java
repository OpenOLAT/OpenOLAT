/**
* OLAT - Online Learning and Training<br>
* https://www.olat.org
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
* <a href="https://www.openolat.org">
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
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.LTIModule;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.LTI13ToolDeploymentType;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.manager.LTI13IDGenerator;
import org.olat.ims.lti13.ui.LTI13ChooseResourceController;
import org.olat.ims.lti13.ui.LTI13ContentItemsListEditController;
import org.olat.ims.lti13.ui.events.LTI13ContentItemAddEvent;
import org.olat.ims.lti13.ui.events.LTI13ContentItemRemoveEvent;
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

	private static final String PLACEHOLDER = "x";
	public static final String CONFIGKEY_13_CONTEXT_KEY = "contextKey";
	public static final String CONFIGKEY_13_DEPLOYMENT_KEY_DEP = "deploymentKey";
	public static final String CONFIGKEY_13_CONTENT_ITEM_KEYS_ORDER = "contentItemKeysOrder";
	
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
	
	private TextElement tHostEl;
	private TextElement tKeyEl;
	private TextElement tPassEl;
	
	private StaticTextElement clientIdEl;
	private StaticTextElement deploymentIdEl;
	private SingleSelection ltiVersionEl; 
	private SingleSelection publicKeyTypeEl;
	private TextElement publicKeyEl;
	private TextElement publicKeyUrlEl;
	private TextElement initiateLoginUrlEl;
	private TextElement redirectUrlEl;
	private FormLink chooseResourceButton;
	private final FormItem itemListEditEl;
	
	private StaticTextElement platformIssEl;
	private StaticTextElement loginUriEl;
	private StaticTextElement tokenUriEl;
	private StaticTextElement jwkSetUriEl;
	
	private MultipleSelectionElement skipLaunchPageEl;
	private MultipleSelectionElement skipAcceptLaunchPageEl;
	private DialogBoxController confirmDialogCtr;
	
	private MultipleSelectionElement sendName;
	private MultipleSelectionElement sendEmail;
	private MultipleSelectionElement doDebug;
	private SpacerElement debugSpacer;

	private TextElement scaleFactorEl;
	private TextElement cutValueEl;
	private FormToggle includeInCourseAssessmentEl;
	private SpacerElement includeInCourseAssessmentSpacer;
	private TextElement scoreScalingEl;
	private FormToggle isAssessableEl;
	private MultipleSelectionElement authorRoleEl;
	private MultipleSelectionElement coachRoleEl;
	private MultipleSelectionElement participantRoleEl;
	private FormLayoutContainer customParamLayout;
	private SingleSelection displayEl;
	private SingleSelection heightEl;
	private SingleSelection widthEl;

	private final String fullURI;
	private Boolean doDebugConfig;
	private final boolean ignoreInCourseAssessmentAvailable;
	private final boolean isAssessable;
	private String key;
	private String pass;
	
	
	private final String subIdent;
	private final ModuleConfiguration config;
	private final RepositoryEntry courseEntry;
	private final boolean scoreScalingEnabled;
	
	private LTI13Tool tool;
	private LTI13Context ltiContext;
	private LTI13Context backupLtiContext;
	
	private List<NameValuePair> nameValuePairs = new ArrayList<>();
	
	private static final String[] enabledKeys = new String[]{"on"};

	private final String[] ltiRolesKeys = new String[]{
			"Learner", "Instructor", "Administrator", "TeachingAssistant", "ContentDeveloper", "Mentor"
	};
	private String[] ltiRolesValues;
	
	private final String[] displayKeys = new String[]{
			LTIDisplayOptions.iframe.name(),
			LTIDisplayOptions.fullscreen.name(),
			LTIDisplayOptions.window.name()
	};
	private String[] displayValues;
	
	private final String[] customTypeKeys = new String[] {
		"free", "userprops"	
	};
	private String[] customTypeValues;
	
	private final String[] heightKeys = new String[]{ BasicLTICourseNode.CONFIG_HEIGHT_AUTO, "460", "480",
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
	private SelectionValues userPropKeysValues;
	
	private CloseableModalController cmc;
	private LTI13ChooseResourceController chooseResourceCtrl;
	private final LTI13ContentItemsListEditController itemListEditCtrl;
	
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
	private LTI13IDGenerator idGenerator;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	/**
	 * Constructor for the tunneling configuration form
	 * @param name
	 * @param config
	 * @param nodeAccessType 
	 * @param withCancel
	 */
	public LTIConfigForm(UserRequest ureq, WindowControl wControl, ICourse course, ModuleConfiguration config,
			NodeAccessType nodeAccessType, RepositoryEntry courseEntry, String subIdent) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.config = config;
		this.subIdent = subIdent;
		this.courseEntry = courseEntry;
		int configVersion = config.getConfigurationVersion();
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(nodeAccessType);
		scoreScalingEnabled = ScoreScalingHelper.isEnabled(course);
		
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
		userPropKeysValues = new SelectionValues();
		userPropKeysValues.add(SelectionValues.entry(LTIManager.USER_NAME_PROP, userPropsTranslator.translate("form.name.username")));
		for (int i=userPropertyHandlers.size(); i-->0; ) {
			UserPropertyHandler handler = userPropertyHandlers.get(i);
			userPropKeysValues.add(SelectionValues.entry(handler.getName(), userPropsTranslator.translate(handler.i18nFormElementLabelKey())));
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
		// pass is used as textValue in tPassEl, thus hiding it
		if (StringHelper.containsNonWhitespace(pass)) {
			pass = PLACEHOLDER.repeat(pass.length());
		}

		fullURI = getFullURL(proto, host, port, uri, query).toString();

		doDebugConfig = config.getBooleanEntry(CONFIG_KEY_DEBUG);
		if (doDebugConfig == null) doDebugConfig = Boolean.FALSE;
    
		Boolean assessable = config.getBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		isAssessable = assessable != null && assessable.booleanValue();
		
		if(CONFIGKEY_LTI_13.equals(config.getStringValue(CONFIGKEY_LTI_VERSION, CONFIGKEY_LTI_11))) {
			String contextKey = config.getStringValue(CONFIGKEY_13_CONTEXT_KEY);
			if(StringHelper.isLong(contextKey)) {
				ltiContext = lti13Service.getContextByKey(Long.valueOf(contextKey));
			} else {
				String deploymentKey = config.getStringValue(CONFIGKEY_13_DEPLOYMENT_KEY_DEP);
				ltiContext = lti13Service.getContextBackwardCompatibility(deploymentKey, courseEntry, subIdent);
			}
			tool = ltiContext == null ? null: ltiContext.getDeployment().getTool();	
		}

		itemListEditCtrl = new LTI13ContentItemsListEditController(ureq, getWindowControl(), mainForm);
		listenTo(itemListEditCtrl);
		itemListEditEl = itemListEditCtrl.getInitialFormItem();

		initForm(ureq);
		loadContentItems(config.getList(CONFIGKEY_13_CONTENT_ITEM_KEYS_ORDER, Long.class), -1);
		updateUI();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer ltiCont = uifactory.addDefaultFormLayout("lti.config", null, formLayout);
		ltiCont.setFormTitle(translate("form.title"));
		ltiCont.setFormContextHelp("manual_user/learningresources/Course_Element_LTI_Page/");
		ltiCont.setElementCssClass("o_sel_lti_config_form");
		
		SelectionValues kValues = new SelectionValues();
		kValues.add(SelectionValues.entry(CONFIGKEY_LTI_11, translate("config.lti.11")));
		if(lti13Module.isEnabled()) {
			kValues.add(SelectionValues.entry(CONFIGKEY_LTI_13, translate("config.lti.13")));
			List<LTI13Tool> tools = lti13Service.getTools(LTI13ToolType.EXT_TEMPLATE);
			for(LTI13Tool template:tools) {
				kValues.add(SelectionValues.entry(template.getKey().toString(), template.getToolName()));
			}
		} else if(tool != null) {
			if(tool.getToolTypeEnum() == LTI13ToolType.EXT_TEMPLATE) {
				kValues.add(SelectionValues.entry(tool.getKey().toString(), tool.getToolName()));
			} else {
				kValues.add(SelectionValues.entry(CONFIGKEY_LTI_13, translate("config.lti.13")));
			}
		}
		ltiVersionEl = uifactory.addDropdownSingleselect("config.lti.version", "config.lti.version", ltiCont, kValues.keys(), kValues.values());
		ltiVersionEl.addActionListener(FormEvent.ONCHANGE);
		String version = config.getStringValue(CONFIGKEY_LTI_VERSION, CONFIGKEY_LTI_11);
		if(tool != null && ltiVersionEl.containsKey(tool.getKey().toString())) {
			ltiVersionEl.select(tool.getKey().toString(), true);
		} else if(kValues.containsKey(version)) {
			ltiVersionEl.select(version, true);
		} else {
			ltiVersionEl.select(CONFIGKEY_LTI_13, true);
		}
		
		tHostEl = uifactory.addTextElement("host", "LTConfigForm.url", 255, fullURI, ltiCont);
		tHostEl.setElementCssClass("o_sel_lti_config_title");
		tHostEl.setExampleKey("LTConfigForm.url.example", null);
		tHostEl.setMandatory(true);

		initLti10Form(ltiCont);
		initLti13Form(ltiCont);
		initLaunchForm(ltiCont);
		initAttributesForm(ltiCont);
		initRolesForm(ltiCont);
	
		FormLayoutContainer gradingCont = uifactory.addDefaultFormLayout("grading.config", null, formLayout);
		gradingCont.setElementCssClass("o_sel_lti_grading_form");
		initGradingForm(gradingCont);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, gradingCont);
		buttonsCont.setElementCssClass("o_sel_buttons");
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	protected void initLti13Form(FormLayoutContainer formLayout) {
		if(ltiContext != null && StringHelper.containsNonWhitespace(ltiContext.getTargetUrl())) {
			tHostEl.setValue(ltiContext.getTargetUrl());
		} else if(ltiContext != null && ltiContext.getDeployment().getTargetUrl() != null) {
			tHostEl.setValue(ltiContext.getDeployment().getTargetUrl());
		} else if(tool != null) {
			tHostEl.setValue(tool.getToolUrl());
		}
		itemListEditEl.setLabel("config.content.items", null);
		formLayout.add(itemListEditEl);
		
		chooseResourceButton = uifactory.addFormLink("choose.resource", formLayout, Link.BUTTON);
		chooseResourceButton.setTextReasonForDisabling(translate("hint.resource.chooser.disabled"));
		
		String clientId = tool == null ? null : tool.getClientId();
		clientIdEl = uifactory.addStaticTextElement("config.client.id", clientId, formLayout);
		if(!StringHelper.containsNonWhitespace(clientId)) {
			clientIdEl.setExampleKey("config.client.id.example", null);
		}

		String deploymentId = ltiContext == null ? null : ltiContext.getDeployment().getDeploymentId();
		deploymentIdEl = uifactory.addStaticTextElement("config.deployment.id", deploymentId, formLayout);
		if(!StringHelper.containsNonWhitespace(deploymentId)) {
			deploymentIdEl.setExampleKey("config.deployment.id.example", null);
		}
		
		SelectionValues kValues = new SelectionValues();
		kValues.add(SelectionValues.entry(PublicKeyType.KEY.name(), translate("config.public.key.type.key")));
		kValues.add(SelectionValues.entry(PublicKeyType.URL.name(), translate("config.public.key.type.url")));
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
		
		String issuer = lti13Module.getPlatformIss();
		platformIssEl = uifactory.addStaticTextElement("lti13.platform.iss", issuer, formLayout);
		String loginUri = lti13Module.getPlatformAuthorizationUri();
		loginUriEl = uifactory.addStaticTextElement("lti13.platform.login.uri", loginUri, formLayout);
		String tokenUri = lti13Module.getPlatformTokenUri();
		tokenUriEl = uifactory.addStaticTextElement("lti13.platform.token.uri", tokenUri, formLayout);
		String jwkSetUri = lti13Module.getPlatformJwkSetUri();
		jwkSetUriEl = uifactory.addStaticTextElement("lti13.platform.jwkset.uri", jwkSetUri, formLayout);
	}
	
	private void updateLtiVersion() {
		String versionKey = ltiVersionEl.getSelectedKey();
		if(CONFIGKEY_LTI_11.equals(versionKey)) {
			// do something
		} else if(CONFIGKEY_LTI_13.equals(versionKey)) {
			if(ltiContext != null && ltiContext.getDeployment().getTool().getToolTypeEnum() == LTI13ToolType.EXT_TEMPLATE) {
				backupLtiContext = ltiContext;
				ltiContext = null;
				tool = null;
			}
			tHostEl.setValue(null);
			clientIdEl.setValue("");
			clientIdEl.setExampleKey("config.client.id.example", null);
			deploymentIdEl.setValue("");
			deploymentIdEl.setExampleKey("config.deployment.id.example", null);
			publicKeyEl.setValue(null);
			publicKeyUrlEl.setValue(null);
			initiateLoginUrlEl.setValue(null);
			redirectUrlEl.setValue(null);
		} else if(StringHelper.isLong(versionKey)) {
			tool = lti13Service.getToolByKey(Long.valueOf(versionKey));
			boolean configurable = tool.getToolTypeEnum() == LTI13ToolType.EXTERNAL;
			
			// be nice and try to save the data
			String targetUrl = null;
			if(ltiContext != null && ltiContext.getDeployment().getTool().equals(tool)) {
				targetUrl = ltiContext.getTargetUrl();
			}
			if(targetUrl == null && backupLtiContext != null && backupLtiContext.getDeployment().getTool().equals(tool)) {
				targetUrl = backupLtiContext.getDeployment().getTargetUrl();
			}
			if(StringHelper.containsNonWhitespace(targetUrl)) {
				tHostEl.setValue(targetUrl);
			} else {
				tHostEl.setValue(tool.getToolUrl());
			}
			clientIdEl.setValue(tool.getClientId());
			clientIdEl.setExampleKey(null, null);
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
			
			if(ltiContext != null && !ltiContext.getDeployment().getTool().equals(tool)) {
				backupLtiContext = ltiContext;
				ltiContext = null;
				deploymentIdEl.setValue("");
				deploymentIdEl.setExampleKey("config.deployment.id.example", null);
			} else if(backupLtiContext != null && backupLtiContext.getDeployment().getTool().equals(tool)) {
				ltiContext = backupLtiContext;
				backupLtiContext = null;
				deploymentIdEl.setValue(ltiContext.getDeployment().getDeploymentId());
				deploymentIdEl.setExampleKey(null, null);
			}
		}
		updateUI();
	}
	
	
	private void updateUI() {
		String selectedVersionKey = ltiVersionEl.getSelectedKey();
		boolean lti13 = !CONFIGKEY_LTI_11.equals(selectedVersionKey);
		boolean sharedTool = StringHelper.isLong(selectedVersionKey)
				&& tool != null && tool.getToolTypeEnum() == LTI13ToolType.EXT_TEMPLATE;
		boolean deepLink = (sharedTool && tool.getDeepLinking() != null && tool.getDeepLinking().booleanValue())
				|| (lti13 && !sharedTool);
		
		// LTI 1.3
		clientIdEl.setVisible(lti13 && !sharedTool);
		deploymentIdEl.setVisible(lti13 && !sharedTool);
		publicKeyTypeEl.setVisible(lti13 && !sharedTool);
		publicKeyTypeEl.setEnabled(!sharedTool);
		publicKeyEl.setVisible(lti13 && PublicKeyType.KEY.name().equals(publicKeyTypeEl.getSelectedKey())  && !sharedTool);
		publicKeyEl.setEnabled(!sharedTool);
		publicKeyUrlEl.setVisible(lti13 && PublicKeyType.URL.name().equals(publicKeyTypeEl.getSelectedKey())  && !sharedTool);
		publicKeyUrlEl.setEnabled(!sharedTool);
		initiateLoginUrlEl.setVisible(lti13 && !sharedTool);
		initiateLoginUrlEl.setEnabled(!sharedTool);
		redirectUrlEl.setVisible(lti13 && !sharedTool);
		redirectUrlEl.setEnabled(!sharedTool);
		platformIssEl.setVisible(lti13 && !sharedTool);
		loginUriEl.setVisible(lti13 && !sharedTool);
		tokenUriEl.setVisible(lti13 && !sharedTool);
		jwkSetUriEl.setVisible(lti13 && !sharedTool);
		itemListEditEl.setVisible(lti13 && !itemListEditCtrl.isEmpty());
		chooseResourceButton.setVisible(deepLink);
		// A deployment ID is mandatory
		chooseResourceButton.setEnabled(tool != null && tool.getKey() != null && ltiContext != null && ltiContext.getKey() != null);
		
		// LTI 1.1
		tKeyEl.setVisible(!lti13);
		tPassEl.setVisible(!lti13);
		
		// Assessment
		boolean assessEnabled = isAssessableEl.isOn();
		scaleFactorEl.setVisible(assessEnabled);
		cutValueEl.setVisible(assessEnabled);
		includeInCourseAssessmentSpacer.setVisible(assessEnabled);
		includeInCourseAssessmentEl.setVisible(ignoreInCourseAssessmentAvailable && assessEnabled);
		scoreScalingEl.setVisible(includeInCourseAssessmentEl.isVisible()
				&& includeInCourseAssessmentEl.isOn() && scoreScalingEnabled);
		
		boolean newWindow = displayEl.isOneSelected() && LTIDisplayOptions.window.name().equals(displayEl.getSelectedKey());
		boolean sizeVisible = !newWindow || !lti13;
		heightEl.setVisible(sizeVisible);
		widthEl.setVisible(sizeVisible); 
		
		doDebug.setVisible(!lti13);
		debugSpacer.setVisible(!lti13);
	}
	
	protected LTI13ToolDeployment getMultiContextToolDeployment(LTI13Tool tool) {
		if(tool != null && tool.getToolTypeEnum() == LTI13ToolType.EXT_TEMPLATE) {
			List<LTI13ToolDeployment> toolDeployments = lti13Service.getToolDeploymentByTool(tool);
			for(LTI13ToolDeployment d: toolDeployments) {
				if(d.getDeploymentType() == LTI13ToolDeploymentType.MULTIPLE_CONTEXTS) {
					return d;
				}
				
			}
		}
		return null;
	}
	
	protected void loadContentItems(List<Long> orderItemsKeys, int position) {
		String selectedVersionKey = ltiVersionEl.getSelectedKey();
		boolean lti13 = !CONFIGKEY_LTI_11.equals(selectedVersionKey);
		if(lti13) {
			List<LTI13ContentItem> items = lti13Service.getContentItems(ltiContext);
			items = lti13Service.reorderContentItems(items, orderItemsKeys, position);
			itemListEditCtrl.loadItems(items);	
			itemListEditCtrl.getInitialFormItem().setVisible(!itemListEditCtrl.isEmpty());
		} else {
			itemListEditCtrl.getInitialFormItem().setVisible(false);
		}
	}
	
	protected void initLti10Form(FormLayoutContainer formLayout) {
		tKeyEl = uifactory.addTextElement ("key","LTConfigForm.key", 255, key, formLayout);
		tKeyEl.setElementCssClass("o_sel_lti_config_key");
		tKeyEl.setExampleKey ("LTConfigForm.key.example", null);
		tKeyEl.setMandatory(true);
		
		tPassEl = uifactory.addPasswordElement ("pass","LTConfigForm.pass", 255, pass, formLayout);
		tPassEl.setElementCssClass("o_sel_lti_config_pass");
		tPassEl.setExampleKey("LTConfigForm.pass.example", null);
		tPassEl.setMandatory(true);
	}
	
	protected void initLaunchForm(FormLayoutContainer formLayout) {
		uifactory.addSpacerElement("launch", formLayout, false);

		String[] enableValues = new String[]{ translate("on") };	
		skipLaunchPageEl = uifactory.addCheckboxesHorizontal("display.config.skipLaunchPage", formLayout, enabledKeys, enableValues);
		if(ltiModule.isForceLaunchPage()) {
			skipLaunchPageEl.select(enabledKeys[0], true);
			skipLaunchPageEl.setEnabled(false);
		} else if ((ltiContext != null && ltiContext.isSkipLaunchPage())
				|| config.getBooleanSafe(BasicLTICourseNode.CONFIG_SKIP_LAUNCH_PAGE)) {
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

	protected void initAttributesForm(FormLayoutContainer formLayout) {	
		uifactory.addSpacerElement("attributes", formLayout, false);

		sendName = uifactory.addCheckboxesHorizontal("sendName", "display.config.sendName", formLayout, new String[]{"xx"}, new String[]{null});
		sendName.addActionListener(FormEvent.ONCHANGE);
		if((ltiContext != null && ltiContext.getSendUserAttributesList().contains(UserConstants.LASTNAME))
				|| config.getBooleanSafe(CONFIG_KEY_SENDNAME, false)) {
			sendName.select("xx", true);
		}
		
		sendEmail = uifactory.addCheckboxesHorizontal("sendEmail", "display.config.sendEmail", formLayout, new String[]{"xx"}, new String[]{null});
		sendEmail.addActionListener(FormEvent.ONCHANGE);
		if((ltiContext != null && ltiContext.getSendUserAttributesList().contains(UserConstants.EMAIL))
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
		
		String customConfig = ltiContext != null ? ltiContext.getSendCustomAttributes() : (String)config.get(CONFIG_KEY_CUSTOM);
		updateNameValuePair(customConfig);
		if(nameValuePairs.isEmpty()) {
			createNameValuePair("", "", -1);
		}
	}
	
	protected void initRolesForm(FormLayoutContainer formLayout) {
		uifactory.addSpacerElement("roles", formLayout, false);
		uifactory.addStaticTextElement("roletitle", "roles.title.oo", translate("roles.title.lti"), formLayout);	
		
		authorRoleEl = uifactory.addCheckboxesHorizontal("author", "author.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		String authorDeploymentRoles = ltiContext == null ? null : ltiContext.getAuthorRoles();
		udpateRoles(authorRoleEl, BasicLTICourseNode.CONFIG_KEY_AUTHORROLE, authorDeploymentRoles, "Instructor,Administrator,TeachingAssistant,ContentDeveloper,Mentor"); 
		coachRoleEl = uifactory.addCheckboxesHorizontal("coach", "coach.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		String coachDeploymentRoles = ltiContext == null ? null : ltiContext.getCoachRoles();
		udpateRoles(coachRoleEl, BasicLTICourseNode.CONFIG_KEY_COACHROLE, coachDeploymentRoles, "Instructor,TeachingAssistant,Mentor");
		participantRoleEl = uifactory.addCheckboxesHorizontal("participant", "participant.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		String participantsDeploymentRoles = ltiContext == null ? null : ltiContext.getParticipantRoles();
		udpateRoles(participantRoleEl, BasicLTICourseNode.CONFIG_KEY_PARTICIPANTROLE, participantsDeploymentRoles, "Learner"); 
		
		uifactory.addSpacerElement("scoring", formLayout, false);
		
		String display = ltiContext != null ? ltiContext.getDisplay()
				: config.getStringValue(BasicLTICourseNode.CONFIG_DISPLAY, "iframe");
		displayEl = uifactory.addRadiosVertical("display.window", "display.config.window", formLayout, displayKeys, displayValues);
		displayEl.addActionListener(FormEvent.ONCHANGE);
		for(String displayKey:displayKeys) {
			if(displayKey.equals(display)) {
				displayEl.select(displayKey, true);
			}
		}
		if(!displayEl.isOneSelected()) {
			displayEl.select(LTIDisplayOptions.iframe.name(), true);
		}
		
		String height = ltiContext != null ? ltiContext.getDisplayHeight()
				: config.getStringValue(BasicLTICourseNode.CONFIG_HEIGHT, BasicLTICourseNode.CONFIG_HEIGHT_AUTO);
		heightEl = uifactory.addDropdownSingleselect("display.height", "display.config.height", formLayout, heightKeys, heightValues, null);
		for(String heightKey:heightKeys) {
			if(heightKey.equals(height)) {
				heightEl.select(heightKey, true);
			}
		}

		String width = ltiContext != null ? ltiContext.getDisplayWidth()
				: config.getStringValue(BasicLTICourseNode.CONFIG_WIDTH, BasicLTICourseNode.CONFIG_HEIGHT_AUTO);
		widthEl = uifactory.addDropdownSingleselect("display.width", "display.config.width", formLayout, heightKeys, heightValues, null);
		for(String heightKey:heightKeys) {
			if(heightKey.equals(width)) {
				widthEl.select(heightKey, true);
			}
		}
		
		debugSpacer = uifactory.addSpacerElement("debug", formLayout, false);
		
		doDebug = uifactory.addCheckboxesHorizontal("doDebug", "display.config.doDebug", formLayout, new String[]{"xx"}, new String[]{null});
		doDebug.select("xx", doDebugConfig);
	
	}
	
	protected void initGradingForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("grading.configuration.title"));
		
		isAssessableEl = uifactory.addToggleButton("isassessable", "assessable.label",
				translate("on"), translate("off"), formLayout);
		isAssessableEl.setElementCssClass("o_sel_lti_config_assessable");
		isAssessableEl.addActionListener(FormEvent.ONCHANGE);
		isAssessableEl.toggle(isAssessable);
	
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
		
		includeInCourseAssessmentSpacer = uifactory.addSpacerElement("spacer.scaling", formLayout, false);
		
		boolean ignoreInCourseAssessment = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
		includeInCourseAssessmentEl = uifactory.addToggleButton("incorporate.in.course.assessment", "incorporate.in.course.assessment",
				translate("on"), translate("off"), formLayout);
		includeInCourseAssessmentEl.toggle(!ignoreInCourseAssessment);
		includeInCourseAssessmentEl.setVisible(ignoreInCourseAssessmentAvailable && isAssessable);
		
		String scaling = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		scoreScalingEl = uifactory.addTextElement("score.scaling", "score.scaling", 10, scaling, formLayout);
		scoreScalingEl.setExampleKey("score.scaling.example", null);
	}
	
	@Override
	protected void doDispose() {
		// Dispose confirm dialog controller since it isn't listend to.
		if (confirmDialogCtr != null) {
			removeAsListenerAndDispose(confirmDialogCtr);
		}
        super.doDispose();
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
		boolean localLti13 = ltiVersionEl.isOneSelected()
				&& CONFIGKEY_LTI_13.equals(ltiVersionEl.getSelectedKey());
		boolean sharedLti13 = ltiVersionEl.isOneSelected()
				&& StringHelper.isLong(ltiVersionEl.getSelectedKey());
		try {
			tHostEl.clearError();
			URL url = new URL(tHostEl.getValue());
			if(url.getHost() == null) {
				tHostEl.setErrorKey("LTConfigForm.invalidurl");
				allOk &= false;
			} else if(sharedLti13 && tool != null
					&& StringHelper.containsNonWhitespace(tool.getToolUrl())
					&& !(new URL(tool.getToolUrl()).getHost().equals(url.getHost()))) {
				tHostEl.setErrorKey("LTConfigForm.urlToolIncompatible", new URL(tool.getToolUrl()).getHost());
				allOk &= false;
			} else if(localLti13 && !"https".equalsIgnoreCase(url.getProtocol())) {
				tHostEl.setErrorKey("LTConfigForm.invalidhttpsurl");
				allOk &= false;
			}
		} catch (MalformedURLException e) {
			tHostEl.setErrorKey("LTConfigForm.invalidurl");
			allOk &= false;
		}
		if(cutValueEl != null) {
			allOk &= validateFloat(cutValueEl);
			allOk &= validateFloat(scaleFactorEl);
		}
		
		allOk &= ScoreScalingHelper.validateScoreScaling(scoreScalingEl);
		
		//lti 1.3
		if(publicKeyTypeEl != null) {
			publicKeyTypeEl.clearError();
			publicKeyEl.clearError();
			publicKeyUrlEl.clearError();
		}
		if(localLti13) {
			if(publicKeyTypeEl != null && publicKeyTypeEl.isOneSelected()
					&& PublicKeyType.URL.name().equals(publicKeyTypeEl.getSelectedKey())) {
				allOk &= validateTextElement(publicKeyUrlEl, 32000, true);
			} else {
				allOk &= validateTextElement(publicKeyEl, 128000, true);
			}
		}
		
		allOk &= validateTextElement(initiateLoginUrlEl, 2000, true);
		allOk &= validateTextElement(redirectUrlEl, 2000, false);
		
		displayEl.clearError();
		if(!displayEl.isOneSelected()) {
			displayEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateTextElement(TextElement el, int maxLength, boolean mandatory) {
		boolean allOk = true;

		el.clearError();
		if(el.isVisible() && el.isEnabled()) {
			String val = el.getValue();
			if(!StringHelper.containsNonWhitespace(val) && mandatory) {
				el.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
				el.setErrorKey("input.toolong", Integer.toString(maxLength));
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
					el.setErrorKey("form.error.wrongFloat");
					allOk = false;
				}
			}
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(chooseResourceButton == source) {
			doChooseResource(ureq, -1);
		} else if(isAssessableEl == source || displayEl == source
				|| publicKeyTypeEl == source || includeInCourseAssessmentEl == source) {
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
		} else if(source instanceof SingleSelection typeChoice && typeChoice.getName().startsWith("typ_")) {
			NameValuePair pair = (NameValuePair)source.getUserObject();
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
		} else if(chooseResourceCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadContentItems(itemListEditCtrl.getOrderedItemsKey(), chooseResourceCtrl.getAddPosition());
				updateContentItemsKeysOrder();
				fireEvent(ureq, Event.CHANGED_EVENT);
				markDirty();
			}
			cmc.deactivate();
			cleanUp();
		} else if(itemListEditCtrl == source) {
			if(event instanceof LTI13ContentItemAddEvent ciae) {
				doChooseResource(ureq, ciae.getAddPosition());
			} else if(event instanceof LTI13ContentItemRemoveEvent) {
				loadContentItems(itemListEditCtrl.getOrderedItemsKey(), -1);
				updateContentItemsKeysOrder();
				fireEvent(ureq, Event.CHANGED_EVENT);
				markDirty();
			}
		} else if(cmc == source) {
			if(chooseResourceCtrl != null) {
				loadContentItems(itemListEditCtrl.getOrderedItemsKey(), chooseResourceCtrl.getAddPosition());
				updateContentItemsKeysOrder();
				fireEvent(ureq, Event.CHANGED_EVENT);
				markDirty();
			}
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(chooseResourceCtrl);
		removeAsListenerAndDispose(cmc);
		chooseResourceCtrl = null;
		cmc = null;
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
			url = new URL(tHostEl.getValue());
		} catch (MalformedURLException e) {
			throw new OLATRuntimeException("MalformedURL in LTConfigForm which should not happen, since we've validated before. URL: " + tHostEl.getValue(), e);
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
		String targetUrl = tHostEl.getValue();
		String initiateLoginUrl = initiateLoginUrlEl.getValue();
		String redirectUrl = redirectUrlEl.getValue();
		
		boolean canUpdateTool = tool == null || LTI13ToolType.EXTERNAL.equals(tool.getToolTypeEnum());
		if(tool == null) {
			String clientId = idGenerator.newId();
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
		
		if(backupLtiContext != null && backupLtiContext.getDeployment().getTool().equals(tool)) {
			ltiContext = backupLtiContext;
			backupLtiContext = null;
		}
		if(ltiContext == null || !ltiContext.getDeployment().getTool().equals(tool)) {
			LTI13ToolDeployment toolDeployment = getMultiContextToolDeployment(tool);
			if(toolDeployment == null) {
				toolDeployment = lti13Service.createToolDeployment(targetUrl, LTI13ToolDeploymentType.SINGLE_CONTEXT, null, tool);
			}
			ltiContext = lti13Service.createContext(targetUrl, toolDeployment, courseEntry, subIdent, null);
			ltiContext.setNameAndRolesProvisioningServicesEnabled(true);
		} else {
			dbInstance.commit();// make sure the tool is persisted
			ltiContext = lti13Service.getContextByKey(ltiContext.getKey());
			ltiContext.setTargetUrl(targetUrl);
		}
		deploymentIdEl.setValue(ltiContext.getDeployment().getDeploymentId());
		if(StringHelper.containsNonWhitespace(ltiContext.getDeployment().getDeploymentId())) {
			deploymentIdEl.setExampleKey(null, null);
		}
		
		List<String> sendAttributes = new ArrayList<>();
		if(sendName.isAtLeastSelected(1)) {
			sendAttributes.add(UserConstants.FIRSTNAME);
			sendAttributes.add(UserConstants.LASTNAME);
		}
		if(sendEmail.isAtLeastSelected(1)) {
			sendAttributes.add(UserConstants.EMAIL);
		}
		ltiContext.setSendUserAttributesList(sendAttributes);
		ltiContext.setSendCustomAttributes(getCustomConfig());
		
		ltiContext.setAuthorRoles(getRoles(authorRoleEl));
		ltiContext.setCoachRoles(getRoles(coachRoleEl));
		ltiContext.setParticipantRoles(getRoles(participantRoleEl));
		
		String display = displayEl.isOneSelected() ? displayEl.getSelectedKey() : LTIDisplayOptions.iframe.name();
		ltiContext.setDisplay(display);
		String height = heightEl.isOneSelected() ? heightEl.getSelectedKey() : null;
		ltiContext.setDisplayHeight(height);
		String width =  widthEl.isOneSelected() ?  widthEl.getSelectedKey() : null;
		ltiContext.setDisplayWidth(width);
		
		boolean assessable = isAssessableEl.isOn();
		ltiContext.setAssessable(assessable);
		
		boolean skipLaunchPage = ltiModule.isForceLaunchPage() || skipLaunchPageEl.isAtLeastSelected(1);
		ltiContext.setSkipLaunchPage(skipLaunchPage);

		ltiContext = lti13Service.updateContext(ltiContext);
		tool = ltiContext.getDeployment().getTool();
		tool.getKey();// prevent lazy loading exception

		clientIdEl.setValue(tool.getClientId());
		if(StringHelper.containsNonWhitespace(tool.getClientId())) {
			clientIdEl.setExampleKey(null, null);
		}
		
		config.setStringValue(CONFIGKEY_13_CONTEXT_KEY, ltiContext.getKey().toString());
		config.remove(CONFIGKEY_13_DEPLOYMENT_KEY_DEP);
		getUpdateConfigCommon();
		
		updateContentItemsKeysOrder();
		
		// A deployment ID is mandatory
		chooseResourceButton.setEnabled(tool != null && tool.getKey() != null && ltiContext != null && ltiContext.getKey() != null);
		
		return config;
	}
	
	private void updateContentItemsKeysOrder() {
		if(itemListEditEl != null && itemListEditEl.isVisible()) {
			List<Long> itemKeys = itemListEditCtrl.commitConfig();
			config.setList(CONFIGKEY_13_CONTENT_ITEM_KEYS_ORDER, itemKeys);
		} else {
			config.remove(CONFIGKEY_13_CONTENT_ITEM_KEYS_ORDER);
		}
	}
	
	private ModuleConfiguration getUpdatedConfigLti11() {
		config.set(CONFIGKEY_KEY, getFormKey());

		// resetting tPassElement and setting value for CONFIGKEY_PASS only if password has changed
		String password = tPassEl.getValue();
		if (!pass.equals(password)) {
			String newPlaceholderValue = PLACEHOLDER.repeat(password.length());
			pass = newPlaceholderValue;
			tPassEl.setValue(newPlaceholderValue);
			config.set(CONFIGKEY_PASS, password);
		}

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
			config.set(BasicLTICourseNode.CONFIG_DISPLAY, LTIDisplayOptions.iframe.name());
		}
		if(heightEl.isOneSelected()) {
			config.set(BasicLTICourseNode.CONFIG_HEIGHT, heightEl.getSelectedKey());
		}
		if(widthEl.isOneSelected()) {
			config.set(BasicLTICourseNode.CONFIG_WIDTH, widthEl.getSelectedKey());
		}
		
		getUpdateConfigCommon();
		return config;
	}
	
	private void getUpdateConfigCommon() {
		if ((ltiModule.isForceLaunchPage() || skipAcceptLaunchPageEl.isAtLeastSelected(1)) && (sendName.isSelected(0) || sendEmail.isSelected(0))) {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE, Boolean.TRUE);
		} else {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE, Boolean.FALSE);
		}
		
		if(isAssessableEl.isOn()) {
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
			
			boolean ignoreInCourseAssessment = includeInCourseAssessmentEl.isVisible() && !includeInCourseAssessmentEl.isOn();
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
			if(ignoreInCourseAssessment || !scoreScalingEnabled) {
				config.remove(MSCourseNode.CONFIG_KEY_SCORE_SCALING);
			} else {
				config.setStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, scoreScalingEl.getValue());
			}
		} else {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD, Boolean.FALSE);
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_PASSED_FIELD, Boolean.FALSE);
			config.remove(BasicLTICourseNode.CONFIG_KEY_SCALEVALUE);
			config.remove(BasicLTICourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			config.remove(MSCourseNode.CONFIG_KEY_SCORE_SCALING);
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
		if (StringHelper.containsNonWhitespace(tKeyEl.getValue())) {
			return tKeyEl.getValue();
		}
		return null;
	}
	
	private void doChooseResource(UserRequest ureq, int addPosition) {
		chooseResourceCtrl = new LTI13ChooseResourceController(ureq, getWindowControl(), ltiContext, addPosition);
		listenTo(chooseResourceCtrl);
		
		String title = translate("choose.resource");
		cmc = new CloseableModalController(getWindowControl(), "close", chooseResourceCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
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
