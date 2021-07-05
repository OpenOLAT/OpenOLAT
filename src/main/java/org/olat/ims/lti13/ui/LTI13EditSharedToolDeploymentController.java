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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13Module;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.LTI13Tool.PublicKeyType;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13EditSharedToolDeploymentController extends FormBasicController {
	
	private TextElement deploymentIdEl;
	private SingleSelection platformsEl;
	private SingleSelection publicKeyTypeEl;
	private TextElement publicKeyEl;
	private StaticTextElement publicKeyUrlEl;
	
	private RepositoryEntry entry;
	private BusinessGroup businessGroup;
	private LTI13SharedToolDeployment deployment;
	private final List<LTI13Platform> platforms;

	@Autowired
	private LTI13Module lti13Module;
	@Autowired
	private LTI13Service lti13Service;
	
	public LTI13EditSharedToolDeploymentController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, BusinessGroup businessGroup) {
		super(ureq, wControl);
		this.entry = entry;
		this.businessGroup = businessGroup;
		platforms = lti13Service.getPlatforms();
		initForm(ureq);
		updatePlatformPublicKeys();
	}
	
	public LTI13EditSharedToolDeploymentController(UserRequest ureq, WindowControl wControl,
			LTI13SharedToolDeployment deployment) {
		super(ureq, wControl);
		this.deployment = deployment;
		platforms = lti13Service.getPlatforms();
		initForm(ureq);
		updatePlatformPublicKeys();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues platformsValues = new SelectionValues();
		for(LTI13Platform platform:platforms) {
			platformsValues.add(SelectionValues.entry(platform.getKey().toString(), label(platform)));
		}
		
		platformsEl = uifactory.addDropdownSingleselect("deployment.platform", formLayout,
				platformsValues.keys(), platformsValues.values(), null);
		platformsEl.addActionListener(FormEvent.ONCHANGE);
		platformsEl.setMandatory(true);
		if(deployment != null && platformsValues.containsKey(deployment.getPlatform().getKey().toString())) {
			platformsEl.select(deployment.getPlatform().getKey().toString(), true);
			platformsEl.setEnabled(false);
		} else if(platformsValues.size() > 0) {
			platformsEl.select(platformsValues.keys()[0], true);
		}
		
		String deploymentId = deployment == null ? null : deployment.getDeploymentId();
		deploymentIdEl = uifactory.addTextElement("deployment.id", 255, deploymentId, formLayout);
		deploymentIdEl.setMandatory(true);
		
		uifactory.addSpacerElement("setup", formLayout, false);
		
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
		
		publicKeyEl = uifactory.addTextAreaElement("tool.public.key.value", "tool.public.key.value", -1, 15, 60, false, true, true, "", formLayout);
		publicKeyEl.setEnabled(false);
		publicKeyUrlEl = uifactory.addStaticTextElement("tool.public.key.url", "", formLayout);
		publicKeyUrlEl.setVisible(false);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private String label(LTI13Platform platform) {
		StringBuilder sb = new StringBuilder(64);
		if(StringHelper.containsNonWhitespace(platform.getName())) {
			sb.append(platform.getName())
			  .append(" (").append(platform.getIssuer()).append(")");
		} else {
			sb.append(platform.getIssuer());
		}
		return sb.toString();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(publicKeyTypeEl == source) {
			updatePublicKeyUI();
		} else if(platformsEl == source) {
			updatePlatformPublicKeys();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updatePlatformPublicKeys() {
		LTI13Platform platform = getSelectedPlatform();
		if(platform == null) {
			publicKeyEl.setValue("");
			publicKeyUrlEl.setValue("");
		} else {
			publicKeyEl.setValue(platform.getPublicKey());
			publicKeyUrlEl.setValue(platform.getPublicKeyUrl());
		}
	}
	
	private void updatePublicKeyUI() {
		if(!publicKeyTypeEl.isOneSelected()) return;
		
		String selectedKey = publicKeyTypeEl.getSelectedKey();
		publicKeyEl.setVisible(PublicKeyType.KEY.name().equals(selectedKey));
		publicKeyUrlEl.setVisible(PublicKeyType.URL.name().equals(selectedKey));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		publicKeyTypeEl.clearError();
		if(!publicKeyTypeEl.isOneSelected()) {
			publicKeyTypeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		platformsEl.clearError();
		if(!platformsEl.isOneSelected()) {
			platformsEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		deploymentIdEl.clearError();
		if(!StringHelper.containsNonWhitespace(deploymentIdEl.getValue())) {
			deploymentIdEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(!validateUniqueDeployment()) {
			deploymentIdEl.setErrorKey("error.unique.deployment", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateUniqueDeployment() {
		String deploymentId = deploymentIdEl.getValue();
		LTI13Platform selectedPlatform = getSelectedPlatform();
		if(selectedPlatform != null) {
			LTI13SharedToolDeployment savedDeployment = lti13Service.getSharedToolDeployment(deploymentId, selectedPlatform);
			return savedDeployment == null || savedDeployment.equals(deployment);
		}
		return true;// no platform makes an error before
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String deploymentId = deploymentIdEl.getValue();
		
		if(deployment == null) {
			LTI13Platform selectedPlatform = getSelectedPlatform();
			selectedPlatform = lti13Service.getPlatformByKey(selectedPlatform.getKey());
			lti13Service.createSharedToolDeployment(deploymentId, selectedPlatform, entry, businessGroup);
		} else {
			deployment.setDeploymentId(deploymentIdEl.getValue());
			deployment = lti13Service.updateSharedToolDeployment(deployment);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private LTI13Platform getSelectedPlatform() {
		LTI13Platform selectedPlatform = null;
		if(platformsEl.isOneSelected()) {
			String selectedKey = platformsEl.getSelectedKey();
			for(LTI13Platform platform:platforms) {
				if(selectedKey.equals(platform.getKey().toString())) {
					selectedPlatform = platform;
				}
			}
		}
		return selectedPlatform;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
