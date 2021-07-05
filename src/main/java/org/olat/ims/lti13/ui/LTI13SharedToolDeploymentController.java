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
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13SharedToolDeploymentController extends FormBasicController {
	
	private SingleSelection publicKeyTypeEl;
	private TextElement publicKeyEl;
	private TextElement publicKeyUrlEl;
	
	private LTI13Platform platform;
	private LTI13SharedToolDeployment deployment;
	
	@Autowired
	private LTI13Module lti13Module;
	
	public LTI13SharedToolDeploymentController(UserRequest ureq, WindowControl wControl, LTI13SharedToolDeployment deployment) {
		super(ureq, wControl);
		this.deployment = deployment;
		platform = deployment.getPlatform();
		initForm(null);
		updatePublicKeyUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		uifactory.addStaticTextElement("deployment.id", deployment.getDeploymentId(), formLayout);
		uifactory.addStaticTextElement("tool.client.id", platform.getClientId(), formLayout);
		
		String url = deployment.getToolUrl();
		uifactory.addStaticTextElement("tool.url", url, formLayout);
		
		String loginInitiationUrl = lti13Module.getToolLoginInitiationUri();
		uifactory.addStaticTextElement("tool.login.initiation", loginInitiationUrl, formLayout);
		String loginRedirectUrl = lti13Module.getToolLoginRedirectUri();
		uifactory.addStaticTextElement("tool.login.redirection", loginRedirectUrl, formLayout);
		
		SelectionValues kValues = new SelectionValues();
		kValues.add(SelectionValues.entry(PublicKeyType.KEY.name(), translate("tool.public.key.type.key")));
		kValues.add(SelectionValues.entry(PublicKeyType.URL.name(), translate("tool.public.key.type.url")));
		publicKeyTypeEl = uifactory.addDropdownSingleselect("tool.public.key.type", "tool.public.key.type", formLayout, kValues.keys(), kValues.values());
		publicKeyTypeEl.addActionListener(FormEvent.ONCHANGE);
		publicKeyTypeEl.select(kValues.keys()[0], true);
		
		String publicKey = platform.getPublicKey();
		publicKeyEl = uifactory.addTextAreaElement("tool.public.key.value", "tool.public.key.value", -1, 15, 60, false, true, true, publicKey, formLayout);
		publicKeyEl.setEnabled(false);
		
		String publicKeyUrl = platform.getPublicKeyUrl();
		publicKeyUrlEl = uifactory.addTextElement("tool.public.key.url", "tool.public.key.url", 255, publicKeyUrl, formLayout);
		publicKeyUrlEl.setEnabled(false);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("ok", buttonLayout);
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(publicKeyTypeEl == source) {
			updatePublicKeyUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
