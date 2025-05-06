/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.quality.ui;


import static org.olat.core.util.StringHelper.blankIfNull;

import java.util.Objects;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.forms.EvaluationFormDispatcher;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.security.DataCollectionSecurityCallback;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: May 2, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ParticipationPublicLinkController extends FormBasicController {
	
	private FormToggle enabledEl;
	private TextElement identifierEl;
	private FormLayoutContainer urlCont;
	
	private QualityDataCollection dataCollection;
	private DataCollectionSecurityCallback secCallback;
	private String savedPublicParticipationIdentifier;

	@Autowired
	private QualityService qualityService;

	protected ParticipationPublicLinkController(UserRequest ureq, WindowControl wControl,
			DataCollectionSecurityCallback secCallback, QualityDataCollection dataCollection) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.dataCollection = dataCollection;
		
		savedPublicParticipationIdentifier = qualityService.getPublicParticipationIdentifier(dataCollection);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("participation.public.title");
		
		enabledEl = uifactory.addToggleButton("participation.public.enabled", "participation.public.enabled",
				translate("on"), translate("off"), formLayout);
		enabledEl.toggle(StringHelper.containsNonWhitespace(savedPublicParticipationIdentifier));
		enabledEl.setEnabled(secCallback.canEditPublicParticipation());
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		
		identifierEl = uifactory.addTextElement("participation.public.link", 100, savedPublicParticipationIdentifier, formLayout);
		identifierEl.setMandatory(true);
		identifierEl.setEnabled(secCallback.canEditPublicParticipation());
		identifierEl.addActionListener(FormEvent.ONCHANGE);
		
		String externalUrlPage = velocity_root + "/participation_public_url.html";
		urlCont = FormLayoutContainer.createCustomFormLayout("preview", getTranslator(), externalUrlPage);
		urlCont.setLabel("participation.public.preview", null);
		urlCont.setRootForm(mainForm);
		formLayout.add(urlCont);
	
		if (secCallback.canEditPublicParticipation()) {
			FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonLayout.setRootForm(mainForm);
			formLayout.add(buttonLayout);
			uifactory.addFormSubmitButton("save", buttonLayout);
		}
	}
	
	private void updateUI() {
		boolean enabled = enabledEl.isOn();
		
		identifierEl.setVisible(enabled);
		if (enabled && !StringHelper.containsNonWhitespace(identifierEl.getValue())) {
			identifierEl.setValue(UUID.randomUUID().toString().replace("-", ""));
		}
		
		urlCont.setVisible(enabled);
		if (enabled) {
			String publicParticipationIdentifier = identifierEl.getValue();
			publicParticipationIdentifier = FilterFactory.getHtmlTagAndDescapingFilter().filter(publicParticipationIdentifier);
			identifierEl.setValue(publicParticipationIdentifier);
			String url = EvaluationFormDispatcher.getPublicParticipationUrl(blankIfNull(publicParticipationIdentifier));
			urlCont.contextPut("url", url);
		}
	}

	public void onChanged(QualityDataCollection dataCollection, DataCollectionSecurityCallback secCallback) {
		this.dataCollection = dataCollection;
		this.secCallback = secCallback;
		
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			updateUI();
		} else if (source == identifierEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (identifierEl.isVisible()) {
			String identifier = identifierEl.getValue();
			if (!StringHelper.containsNonWhitespace(identifier)) {
				identifierEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if (!Objects.equals(identifier, savedPublicParticipationIdentifier) 
					&& (!Objects.equals(identifier.toLowerCase(), blankIfNull(savedPublicParticipationIdentifier).toLowerCase()))
					&& !qualityService.isPublicParticipationIdentifierAvailable(identifier)) {
				identifierEl.setErrorKey("error.participation.public.identifier.not.available");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String publicParticipationIdentifier = null; 
		if (enabledEl.isOn()) {
			publicParticipationIdentifier = identifierEl.getValue();
			publicParticipationIdentifier = FilterFactory.getHtmlTagAndDescapingFilter().filter(publicParticipationIdentifier);
		}
		qualityService.updatePublicParticipationIdentifier(dataCollection, publicParticipationIdentifier);
		savedPublicParticipationIdentifier = publicParticipationIdentifier;
	}

}
