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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
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
import org.olat.modules.ModuleConfiguration;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author guido
 * @author Charles Severance
 */
public class LTIConfigForm extends FormBasicController {

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
	
	private ModuleConfiguration config;
	
	private TextElement thost;
	private TextElement tkey;
	private TextElement tpass;
	
	private MultipleSelectionElement skipLaunchPageEl;
	private MultipleSelectionElement skipAcceptLaunchPageEl;
	private DialogBoxController confirmDialogCtr;
	
	private SelectionElement sendName;
	private SelectionElement sendEmail;
	private SelectionElement doDebug;

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
	private Boolean sendNameConfig;
	private Boolean sendEmailConfig;
	private Boolean doDebugConfig;
	private final boolean ignoreInCourseAssessmentAvailable;
	private boolean isAssessable;
	private String key;
	private String pass;
	
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
	private LTIModule ltiModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	/**
	 * Constructor for the tunneling configuration form
	 * @param name
	 * @param config
	 * @param nodeAccessType 
	 * @param withCancel
	 */
	public LTIConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, NodeAccessType nodeAccessType) {
		super(ureq, wControl);
		this.config = config;
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
		
		sendNameConfig = config.getBooleanEntry(CONFIG_KEY_SENDNAME);
		if (sendNameConfig == null) sendNameConfig = Boolean.FALSE;

		sendEmailConfig = config.getBooleanEntry(CONFIG_KEY_SENDEMAIL);
		if (sendEmailConfig == null) sendEmailConfig = Boolean.FALSE;

		doDebugConfig = config.getBooleanEntry(CONFIG_KEY_DEBUG);
		if (doDebugConfig == null) doDebugConfig = Boolean.FALSE;
    
		Boolean assessable = config.getBooleanEntry(BasicLTICourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		isAssessable = assessable == null ? false : assessable.booleanValue();

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.title");
		setFormContextHelp("Other#_lti_config");
		formLayout.setElementCssClass("o_sel_lti_config_form");

		thost = uifactory.addTextElement("host", "LTConfigForm.url", 255, fullURI, formLayout);
		thost.setElementCssClass("o_sel_lti_config_title");
		thost.setExampleKey("LTConfigForm.url.example", null);
		thost.setDisplaySize(64);
		thost.setMandatory(true);
		
		tkey  = uifactory.addTextElement ("key","LTConfigForm.key", 255, key, formLayout);
		tkey.setElementCssClass("o_sel_lti_config_key");
		tkey.setExampleKey ("LTConfigForm.key.example", null);
		tkey.setMandatory(true);
		
		tpass = uifactory.addTextElement ("pass","LTConfigForm.pass", 255, pass, formLayout);
		tpass.setElementCssClass("o_sel_lti_config_pass");
		tpass.setExampleKey("LTConfigForm.pass.example", null);
		tpass.setMandatory(true);

		uifactory.addSpacerElement("attributes", formLayout, false);

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
		
		uifactory.addSpacerElement("attributes", formLayout, false);

		sendName = uifactory.addCheckboxesHorizontal("sendName", "display.config.sendName", formLayout, new String[]{"xx"}, new String[]{null});
		sendName.select("xx", sendNameConfig);
		sendName.addActionListener(FormEvent.ONCHANGE);
		
		sendEmail = uifactory.addCheckboxesHorizontal("sendEmail", "display.config.sendEmail", formLayout, new String[]{"xx"}, new String[]{null});
		sendEmail.select("xx", sendEmailConfig);
		sendEmail.addActionListener(FormEvent.ONCHANGE);
		
		boolean sendEnabled = sendName.isSelected(0) || sendEmail.isSelected(0);
		skipAcceptLaunchPageEl.setVisible(sendEnabled);
		
		String page = velocity_root + "/custom.html";
		customParamLayout = FormLayoutContainer.createCustomFormLayout("custom_fields", getTranslator(), page);
		customParamLayout.setRootForm(mainForm);
		customParamLayout.setLabel("display.config.custom", null);
		formLayout.add(customParamLayout);
		customParamLayout.contextPut("nameValuePairs", nameValuePairs);
		updateNameValuePair((String)config.get(CONFIG_KEY_CUSTOM));
		if(nameValuePairs.isEmpty()) {
			createNameValuePair("", "", -1);
		}
		
		uifactory.addSpacerElement("roles", formLayout, false);
		uifactory.addStaticTextElement("roletitle", "roles.title.oo", translate("roles.title.lti"), formLayout);
		
		authorRoleEl = uifactory.addCheckboxesHorizontal("author", "author.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		udpateRoles(authorRoleEl, BasicLTICourseNode.CONFIG_KEY_AUTHORROLE, "Instructor,Administrator,TeachingAssistant,ContentDeveloper,Mentor"); 
		coachRoleEl = uifactory.addCheckboxesHorizontal("coach", "coach.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		udpateRoles(coachRoleEl, BasicLTICourseNode.CONFIG_KEY_COACHROLE, "Instructor,TeachingAssistant,Mentor");
		participantRoleEl = uifactory.addCheckboxesHorizontal("participant", "participant.roles", formLayout, ltiRolesKeys, ltiRolesValues);
		udpateRoles(participantRoleEl, BasicLTICourseNode.CONFIG_KEY_PARTICIPANTROLE, "Learner"); 
		
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
		
		String display = config.getStringValue(BasicLTICourseNode.CONFIG_DISPLAY, "iframe");
		displayEl = uifactory.addRadiosVertical("display.window", "display.config.window", formLayout, displayKeys, displayValues);
		for(String displayKey:displayKeys) {
			if(displayKey.equals(display)) {
				displayEl.select(displayKey, true);
			}
		}
		
		String height = config.getStringValue(BasicLTICourseNode.CONFIG_HEIGHT, BasicLTICourseNode.CONFIG_HEIGHT_AUTO);
		heightEl = uifactory.addDropdownSingleselect("display.height", "display.config.height", formLayout, heightKeys, heightValues, null);
		for(String heightKey:heightKeys) {
			if(heightKey.equals(height)) {
				heightEl.select(heightKey, true);
			}
		}

		String width = config.getStringValue(BasicLTICourseNode.CONFIG_WIDTH, BasicLTICourseNode.CONFIG_HEIGHT_AUTO);
		widthEl = uifactory.addDropdownSingleselect("display.width", "display.config.width", formLayout, heightKeys, heightValues, null);
		for(String heightKey:heightKeys) {
			if(heightKey.equals(width)) {
				widthEl.select(heightKey, true);
			}
		}
		
		uifactory.addSpacerElement("debug", formLayout, false);
		
		doDebug = uifactory.addCheckboxesHorizontal("doDebug", "display.config.doDebug", formLayout, new String[]{"xx"}, new String[]{null});
		doDebug.select("xx", doDebugConfig);
				
		uifactory.addSpacerElement("buttons", formLayout, false);
		uifactory.addFormSubmitButton("save", formLayout);
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
	
	private void udpateRoles(MultipleSelectionElement roleEl, String configKey, String defaultRoles) {
		Object configRoles = config.get(configKey);
		String roles = defaultRoles;
		if(configRoles instanceof String) {
			roles = (String)configRoles;
		}
		String[] roleArr = roles.split(",");
		for(String role:roleArr) {
			roleEl.select(role, true);
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
			new URL(thost.getValue());
		} catch (MalformedURLException e) {
			thost.setErrorKey("LTConfigForm.invalidurl", null);
			allOk &= false;
		}
		allOk &= validateFloat(cutValueEl);
		allOk &= validateFloat(scaleFactorEl);
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
		if(source == isAssessableEl) {
			boolean assessEnabled = isAssessableEl.isAtLeastSelected(1);
			scaleFactorEl.setVisible(assessEnabled);
			cutValueEl.setVisible(assessEnabled);
			ignoreInCourseAssessmentEl.setVisible(ignoreInCourseAssessmentAvailable && assessEnabled);
			flc.setDirty(true);
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
		if ((ltiModule.isForceLaunchPage() || skipAcceptLaunchPageEl.isAtLeastSelected(1)) && (sendName.isSelected(0) || sendEmail.isSelected(0))) {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE, Boolean.TRUE);
		} else {
			config.setBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE, Boolean.FALSE);
		}
		
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
		return config;
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
