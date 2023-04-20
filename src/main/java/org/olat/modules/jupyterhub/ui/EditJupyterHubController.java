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
package org.olat.modules.jupyterhub.ui;

import java.net.URL;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.manager.LTI13IDGenerator;
import org.olat.modules.jupyterhub.JupyterHub;
import org.olat.modules.jupyterhub.JupyterManager;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-04-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class EditJupyterHubController extends FormBasicController {

	private final String clientId;
	private final String accessToken;
	private JupyterHub jupyterHub;
	private TextElement nameEl, jupyterHubUrlEl, ramEl, cpuEl, imageCheckingServiceUrlEl, ltiKeyEl;
	private RichTextElement infoTextEl;
	private SingleSelection dataTransmissionAgreementEl;
	private SelectionValues dataTransmissionAgreementKV;
	private StaticTextElement accessTokenEl;
	private StaticTextElement clientEl;
	private FormLink checkConnectionButton;

	@Autowired
	private LTI13IDGenerator idGenerator;

	@Autowired
	private JupyterManager jupyterManager;

	public EditJupyterHubController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		clientId = idGenerator.newId();
		accessToken = generateAccessToken();
		initForm(ureq);
	}

	private String generateAccessToken() {
		String source = idGenerator.newId();
		return source.substring(source.length() - 12);
	}

	public EditJupyterHubController(UserRequest ureq, WindowControl wControl, JupyterHub jupyterHub) {
		super(ureq, wControl);
		this.jupyterHub = jupyterHub;
		clientId = jupyterHub.getLtiTool().getClientId();
		accessToken = StringHelper.containsNonWhitespace(jupyterHub.getAccessToken()) ? jupyterHub.getAccessToken() : generateAccessToken();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String name = jupyterHub != null ? jupyterHub.getName() : "";
		nameEl = uifactory.addTextElement("jupyterHub.name", "table.header.hub.name", 255, name, formLayout);
		nameEl.setMandatory(true);

		String cpu = jupyterHub != null ? jupyterHub.getCpu() + "" : "1";
		cpuEl = uifactory.addTextElement("jupyterHub.cpu", "table.header.hub.cpu", 4, cpu, formLayout);
		cpuEl.setMandatory(true);

		String ram = jupyterHub != null ? jupyterHub.getRam() : "1 GB";
		ramEl = uifactory.addTextElement("jupyterHub.ram", "table.header.hub.ram", 32, ram, formLayout);
		ramEl.setMandatory(true);

		String imageCheckingServiceUrl = jupyterHub != null ? jupyterHub.getImageCheckingServiceUrl() : "";
		imageCheckingServiceUrlEl = uifactory.addTextElement("jupyterHub.imageCheckingServiceUrl",
				"form.hub.imageCheckingServiceUrl", 255, imageCheckingServiceUrl, formLayout);

		String infoText = jupyterHub != null ? jupyterHub.getInfoText() : "";
		infoTextEl = uifactory.addRichTextElementVeryMinimalistic("jupyterHub.infoText",
				"form.hub.infoText", infoText, 3, -1, true, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		infoTextEl.getEditorConfiguration().disableImageAndMovie();
		infoTextEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);

		uifactory.addSpacerElement("jupyterHub.spacer", formLayout, false);

		String jupyterHubUrl = jupyterHub != null ? jupyterHub.getJupyterHubUrl() : "";
		jupyterHubUrlEl = uifactory.addTextElement("jupyterHub.url", "form.hub.url", 255, jupyterHubUrl, formLayout);
		jupyterHubUrlEl.setMandatory(true);

		String ltiKey = jupyterHub != null && jupyterHub.getLtiKey() != null ? jupyterHub.getLtiKey() : "";
		ltiKeyEl = uifactory.addTextElement("jupyterHub.ltiKey", "form.hub.ltiKey", 255, ltiKey, formLayout);

		accessTokenEl = uifactory.addStaticTextElement("jupyterHub.accessToken", "form.hub.accessToken", accessToken, formLayout);
		accessTokenEl.setElementCssClass("text-muted");

		clientEl = uifactory.addStaticTextElement("jupyterHub.clientId", "table.header.hub.clientId", clientId, formLayout);
		clientEl.setElementCssClass("text-muted");

		dataTransmissionAgreementKV = new SelectionValues();
		dataTransmissionAgreementKV.add(SelectionValues.entry(JupyterHub.AgreementSetting.requireAgreement.name(), translate("form.agreement.requireAgreement")));
		dataTransmissionAgreementKV.add(SelectionValues.entry(JupyterHub.AgreementSetting.suppressAgreement.name(), translate("form.agreement.suppressAgreement")));
		dataTransmissionAgreementKV.add(SelectionValues.entry(JupyterHub.AgreementSetting.configurableByAuthor.name(), translate("form.agreement.configurableByAuthor")));
		dataTransmissionAgreementEl = uifactory.addDropdownSingleselect("jupyterHub.dataTransmissionAgreement", formLayout, dataTransmissionAgreementKV.keys(), dataTransmissionAgreementKV.values());
		dataTransmissionAgreementEl.setMandatory(true);
		JupyterHub.AgreementSetting agreementSetting = jupyterHub != null ? jupyterHub.getAgreementSetting() : JupyterHub.AgreementSetting.suppressAgreement;
		dataTransmissionAgreementEl.select(agreementSetting.name(), true);

		FormLayoutContainer buttons = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttons);
		uifactory.addFormCancelButton("cancel", buttons, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttons);
		checkConnectionButton = uifactory.addFormLink("form.checkConnection", buttons, Link.BUTTON);
	}


	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateName();
		allOk &= validateCpu();
		allOk &= validateRam();
		allOk &= validateImageChecker();
		allOk &= validateJupyterHubUrl();

		return allOk;
	}

	private boolean validateName() {
		nameEl.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			return false;
		}
		return true;
	}

	private boolean validateCpu() {
		cpuEl.clearError();
		try {
			long cpu = Long.parseLong(cpuEl.getValue());
			if (cpu <= 0 || cpu > 32) {
				throw new IllegalArgumentException("Invalid CPU value");
			}
		} catch (Exception e) {
			cpuEl.setErrorKey("form.hub.cpu.error");
			return false;
		}

		return true;
	}

	private boolean validateRam() {
		ramEl.clearError();
		if (!JupyterHub.validateRam(ramEl.getValue())) {
			ramEl.setErrorKey("form.hub.ram.error");
			return false;
		}

		return true;
	}

	private boolean validateImageChecker() {
		imageCheckingServiceUrlEl.clearError();
		if (StringHelper.containsNonWhitespace(imageCheckingServiceUrlEl.getValue())) {
			String testUrl = String.format(imageCheckingServiceUrlEl.getValue(), "test/image");
			try {
				if (!imageCheckingServiceUrlEl.getValue().contains("%s")) {
					throw new IllegalArgumentException("URL does not contain string placehoder");
				}
				new URL(testUrl);
			} catch (Exception e) {
				imageCheckingServiceUrlEl.setErrorKey("form.hub.invalidUrl");
				return false;
			}
		}

		return true;
	}

	private boolean validateJupyterHubUrl() {
		jupyterHubUrlEl.clearError();
		if (!StringHelper.containsNonWhitespace(jupyterHubUrlEl.getValue())) {
			jupyterHubUrlEl.setErrorKey("form.legende.mandatory");
			return false;
		}

		try {
			new URL(jupyterHubUrlEl.getValue());
		} catch (Exception e) {
			jupyterHubUrlEl.setErrorKey("form.hub.invalidUrl");
			return false;
		}

		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = nameEl.getValue();
		String jupyterHubUrl = jupyterHubUrlEl.getValue();
		String ram = ramEl.getValue();
		long cpu = Long.parseLong(cpuEl.getValue());
		String imageCheckingServiceUrl = imageCheckingServiceUrlEl.getValue();
		String infoText = infoTextEl.getValue();
		String ltiKey = ltiKeyEl.getValue();
		JupyterHub.AgreementSetting agreementSetting = JupyterHub.AgreementSetting.valueOf(dataTransmissionAgreementEl.getSelectedKey());

		if (jupyterHub == null) {
			jupyterHub = jupyterManager.createJupyterHub(name, jupyterHubUrl, clientId, ram, cpu, agreementSetting);
		} else {
			jupyterHub.setName(nameEl.getValue());
			jupyterHub.setJupyterHubUrl(jupyterHubUrl);
			jupyterHub.setRam(ram);
			jupyterHub.setCpu(cpu);
		}

		jupyterHub.setImageCheckingServiceUrl(imageCheckingServiceUrl);
		jupyterHub.setInfoText(infoText);
		jupyterHub.setLtiKey(ltiKey);

		jupyterManager.updateJupyterHub(jupyterHub);

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
