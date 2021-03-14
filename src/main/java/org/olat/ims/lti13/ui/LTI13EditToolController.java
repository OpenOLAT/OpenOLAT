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
package org.olat.ims.lti13.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.olat.ims.lti13.LTI13ToolType;
import org.olat.ims.lti13.manager.LTI13IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13EditToolController extends FormBasicController {

	private TextElement toolNameEl;
	private TextElement toolUrlEl;
	private SingleSelection publicKeyTypeEl;
	private TextElement publicKeyEl;
	private TextElement publicKeyUrlEl;
	private TextElement initiateLoginUrlEl;
	
	private LTI13Tool tool;
	private final String clientId;
	private final LTI13ToolType toolType;
	
	@Autowired
	private LTI13Module ltiModule;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private LTI13IDGenerator idGenerator;
	
	public LTI13EditToolController(UserRequest ureq, WindowControl wControl, LTI13ToolType toolType) {
		super(ureq, wControl);
		this.toolType = toolType;
		clientId = idGenerator.newId();
		
		initForm(ureq);
		updatePublicKeyUI();
	}
	
	public LTI13EditToolController(UserRequest ureq, WindowControl wControl, LTI13Tool tool) {
		super(ureq, wControl);
		this.tool = tool;
		toolType = tool.getToolTypeEnum();
		clientId = tool.getClientId();
		initForm(ureq);
		updatePublicKeyUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String toolName = tool == null ? null : tool.getToolName();
		toolNameEl = uifactory.addTextElement("tool.name", "tool.name", 255, toolName, formLayout);
		String toolUrl = tool == null ? null : tool.getToolUrl();
		toolUrlEl = uifactory.addTextElement("tool.url", "tool.url", 255, toolUrl, formLayout);

		uifactory.addStaticTextElement("tool.client.id", clientId, formLayout);
		
		KeyValues kValues = new KeyValues();
		kValues.add(KeyValues.entry(PublicKeyType.KEY.name(), translate("tool.public.key.type.key")));
		kValues.add(KeyValues.entry(PublicKeyType.URL.name(), translate("tool.public.key.type.url")));
		PublicKeyType publicKeyType = tool == null ? PublicKeyType.KEY : tool.getPublicKeyTypeEnum();
		publicKeyTypeEl = uifactory.addDropdownSingleselect("tool.public.key.type", "tool.public.key.type", formLayout, kValues.keys(), kValues.values());
		publicKeyTypeEl.addActionListener(FormEvent.ONCHANGE);
		if(publicKeyType != null && kValues.containsKey(publicKeyType.name())) {
			publicKeyTypeEl.select(publicKeyType.name(), true);
		} else {
			publicKeyTypeEl.select(kValues.keys()[0], true);
		}
		
		String publicKey = tool == null ? null : tool.getPublicKey();
		publicKeyEl = uifactory.addTextAreaElement("tool.public.key.value", "tool.public.key.value", -1, 15, 60, false, true, true, publicKey, formLayout);
		
		String publicKeyUrl = tool == null ? null : tool.getPublicKeyUrl();
		publicKeyUrlEl = uifactory.addTextElement("tool.public.key.url", "tool.public.key.url", 255, publicKeyUrl, formLayout);
		
		String initiateLoginUrl = tool == null ? null : tool.getInitiateLoginUrl();
		initiateLoginUrlEl = uifactory.addTextElement("tool.initiate.login.url", "tool.initiate.login.url", 255, initiateLoginUrl, formLayout);
		
		uifactory.addSpacerElement("platform", formLayout, false);
		
		String issuer = ltiModule.getPlatformIss();
		uifactory.addStaticTextElement("lti13.platform.iss", issuer, formLayout);
		String loginUri = ltiModule.getPlatformAuthorizationUri();
		uifactory.addStaticTextElement("lti13.platform.login.uri", loginUri, formLayout);
		String tokenUri = ltiModule.getPlatformTokenUri();
		uifactory.addStaticTextElement("lti13.platform.token.uri", tokenUri, formLayout);
		String jwkSetUri = ltiModule.getPlatformJwkSetUri();
		uifactory.addStaticTextElement("lti13.platform.jwkset.uri", jwkSetUri, formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private void updatePublicKeyUI() {
		if(!publicKeyTypeEl.isOneSelected()) return;
		
		String selectedKey = publicKeyTypeEl.getSelectedKey();
		publicKeyEl.setVisible(PublicKeyType.KEY.name().equals(selectedKey));
		publicKeyUrlEl.setVisible(PublicKeyType.URL.name().equals(selectedKey));
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		publicKeyTypeEl.clearError();
		publicKeyEl.clearError();
		publicKeyUrlEl.clearError();
		if(!publicKeyTypeEl.isOneSelected()) {
			publicKeyTypeEl.setErrorKey("form.legende.mandatory", null);
			allOk  &= false;
		} else if(PublicKeyType.KEY.name().equals(publicKeyTypeEl.getSelectedKey())
				&& !StringHelper.containsNonWhitespace(publicKeyEl.getValue())) {
			publicKeyEl.setErrorKey("form.legende.mandatory", null);
			allOk  &= false;
		} else if(PublicKeyType.URL.name().equals(publicKeyTypeEl.getSelectedKey())
				&& !StringHelper.containsNonWhitespace(publicKeyUrlEl.getValue())) {
			publicKeyUrlEl.setErrorKey("form.legende.mandatory", null);
			allOk  &= false;
		}
		
		initiateLoginUrlEl.clearError();
		if(!StringHelper.containsNonWhitespace(initiateLoginUrlEl.getValue())) {
			initiateLoginUrlEl.setErrorKey("form.legende.mandatory", null);
			allOk  &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(publicKeyTypeEl == source) {
			updatePublicKeyUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String toolName = toolNameEl.getValue();
		String toolUrl = toolUrlEl.getValue();
		String initiateLoginUrl = initiateLoginUrlEl.getValue();
		
		if(tool == null) {
			tool = lti13Service.createExternalTool(toolName, toolUrl, clientId, initiateLoginUrl, toolType);
		} else {
			tool.setToolName(toolName);
			tool.setToolUrl(toolUrl);
			tool.setInitiateLoginUrl(initiateLoginUrl);
			tool.setClientId(clientId);
		}
		
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
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
