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

import java.math.BigDecimal;
import java.net.URL;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
	private JupyterHub jupyterHub;
	private TextElement nameEl, jupyterHubUrlEl, ramGuaranteeEl, ramLimitEl, cpuGuaranteeEl, cpuLimitEl, imageCheckingServiceUrlEl;
	private TextAreaElement additionalFieldsEl;
	private RichTextElement infoTextEl;
	private SingleSelection dataTransmissionAgreementEl;
	private FormLink checkConnectionButton;

	@Autowired
	private LTI13IDGenerator idGenerator;

	@Autowired
	private JupyterManager jupyterManager;

	public EditJupyterHubController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		clientId = idGenerator.newId();
		initForm(ureq);
	}

	public EditJupyterHubController(UserRequest ureq, WindowControl wControl, JupyterHub jupyterHub) {
		super(ureq, wControl);
		this.jupyterHub = jupyterHub;
		clientId = jupyterHub.getLtiTool().getClientId();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_jupyterhub_configuration");
		
		String name = jupyterHub != null ? jupyterHub.getName() : "";
		nameEl = uifactory.addTextElement("jupyterHub.name", "table.header.hub.name", 255, name, formLayout);
		nameEl.setElementCssClass("o_sel_jupyterhub_name");
		nameEl.setMandatory(true);

		String cpuGuarantee = getInitialCpuGuaranteeValue(jupyterHub);
		cpuGuaranteeEl = uifactory.addTextElement("jupyterHub.cpuGuarantee", "table.header.hub.cpu.guarantee", 4, cpuGuarantee, formLayout);
		cpuGuaranteeEl.setExampleKey("form.hub.cpu.example", null);

		String cpuLimit = getInitialCpuLimitValue(jupyterHub);
		cpuLimitEl = uifactory.addTextElement("jupyterHub.cpuLimit", "table.header.hub.cpu.limit", 4, cpuLimit, formLayout);
		cpuLimitEl.setExampleKey("form.hub.cpu.example", null);

		String ramGuarantee = getInitialRamGuaranteeValue(jupyterHub);
		ramGuaranteeEl = uifactory.addTextElement("jupyterHub.ramGuarantee", "table.header.hub.ram.guarantee", 32, ramGuarantee, formLayout);
		ramGuaranteeEl.setExampleKey("form.hub.ram.example", null);

		String ramLimit = getInitialRamLimitValue(jupyterHub);
		ramLimitEl = uifactory.addTextElement("jupyterHub.ramLimit", "table.header.hub.ram.limit", 32, ramLimit, formLayout);
		ramLimitEl.setExampleKey("form.hub.ram.example", null);

		String additionalFields = jupyterHub != null ? jupyterHub.getAdditionalFields() : "";
		additionalFieldsEl = uifactory.addTextAreaElement("jupyterHub.additionalFields",
				"form.hub.additionalFields", -1, -1, -1, true,
				true, additionalFields, formLayout);
		additionalFieldsEl.setHelpTextKey("form.hub.additionalFields.help", null);
		additionalFieldsEl.setExampleKey("form.hub.additionalFields.example", null);

		String imageCheckingServiceUrl = jupyterHub != null ? jupyterHub.getImageCheckingServiceUrl() : "";
		imageCheckingServiceUrlEl = uifactory.addTextElement("jupyterHub.imageCheckingServiceUrl",
				"form.hub.imageCheckingServiceUrl", 255, imageCheckingServiceUrl, formLayout);
		imageCheckingServiceUrlEl.setExampleKey("form.hub.imageCheckingServiceUrl.example", null);

		String infoText = jupyterHub != null ? jupyterHub.getInfoText() : "";
		infoTextEl = uifactory.addRichTextElementVeryMinimalistic("jupyterHub.infoText",
				"form.hub.infoText", infoText, 3, -1, true, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		infoTextEl.getEditorConfiguration().disableImageAndMovie();
		infoTextEl.getEditorConfiguration().disableMathEditor();
		infoTextEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.oneLine);

		uifactory.addSpacerElement("jupyterHub.spacer", formLayout, false);

		String jupyterHubUrl = jupyterHub != null ? jupyterHub.getJupyterHubUrl() : "";
		jupyterHubUrlEl = uifactory.addTextElement("jupyterHub.url", "form.hub.url", 255, jupyterHubUrl, formLayout);
		jupyterHubUrlEl.setElementCssClass("o_sel_jupyterhub_url");
		jupyterHubUrlEl.setMandatory(true);

		StaticTextElement clientEl = uifactory.addStaticTextElement("jupyterHub.clientId", "table.header.hub.clientId", clientId, formLayout);
		clientEl.setElementCssClass("text-muted");

		SelectionValues dataTransmissionAgreementKV = new SelectionValues();
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

	private String getInitialCpuGuaranteeValue(JupyterHub jupyterHub) {
		if (jupyterHub == null) {
			return "0.5";
		}
		if (jupyterHub.getCpuGuarantee() == null) {
			return "";
		}
		return jupyterHub.getCpuGuarantee().stripTrailingZeros().toPlainString();
	}

	private String getInitialCpuLimitValue(JupyterHub jupyterHub) {
		if (jupyterHub == null) {
			return "1";
		}
		if (jupyterHub.getCpuLimit() == null) {
			return "";
		}
		return jupyterHub.getCpuLimit().stripTrailingZeros().toPlainString();
	}

	private String getInitialRamGuaranteeValue(JupyterHub jupyterHub) {
		if (jupyterHub == null) {
			return "0.5 G";
		}
		if (!StringHelper.containsNonWhitespace(jupyterHub.getRamGuarantee())) {
			return "";
		}
		return jupyterHub.getRamGuarantee();
	}

	private String getInitialRamLimitValue(JupyterHub jupyterHub) {
		if (jupyterHub == null) {
			return "1 G";
		}
		if (!StringHelper.containsNonWhitespace(jupyterHub.getRamLimit())) {
			return "";
		}
		return jupyterHub.getRamLimit();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateName();

		cpuGuaranteeEl.clearError();
		cpuLimitEl.clearError();
		allOk &= validateCpu(cpuGuaranteeEl);
		allOk &= validateCpu(cpuLimitEl);
		allOk &= validateBothSetOrNotSet(cpuGuaranteeEl, cpuLimitEl, "table.header.hub.cpu.warning");

		ramGuaranteeEl.clearError();
		ramLimitEl.clearError();
		allOk &= validateRam(ramGuaranteeEl);
		allOk &= validateRam(ramLimitEl);
		allOk &= validateBothSetOrNotSet(ramGuaranteeEl, ramLimitEl, "table.header.hub.ram.warning");

		allOk &= validateAdditionalFields(additionalFieldsEl);

		allOk &= validateImageChecker();
		allOk &= validateJupyterHubUrl();

		return allOk;
	}

	private boolean validateName() {
		nameEl.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.mandatory.hover");
			return false;
		}
		return true;
	}

	private boolean validateCpu(TextElement cpuEl) {
		if (!StringHelper.containsNonWhitespace(cpuEl.getValue())) {
			return true;
		}
		try {
			double cpuDouble = Double.parseDouble(cpuEl.getValue());
			if (cpuDouble <= 0 || cpuDouble > 32) {
				throw new IllegalArgumentException("Invalid CPU value");
			}
		} catch (Exception e) {
			cpuEl.setErrorKey("form.hub.cpu.error");
			return false;
		}

		return true;
	}

	private boolean validateRam(TextElement ramEl) {
		if (!StringHelper.containsNonWhitespace(ramEl.getValue())) {
			return true;
		}
		if (!JupyterHub.validateRam(ramEl.getValue())) {
			ramEl.setErrorKey("form.hub.ram.error");
			return false;
		}

		return true;
	}

	private boolean validateBothSetOrNotSet(TextElement el1, TextElement el2, String warningKey) {
		if (StringHelper.containsNonWhitespace(el1.getValue()) ^ StringHelper.containsNonWhitespace(el2.getValue())) {
			el1.setErrorKey(warningKey);
			return false;
		}

		return true;
	}

	private boolean validateAdditionalFields(TextAreaElement additionalFieldsEl) {
		additionalFieldsEl.clearError();
		if (!StringHelper.containsNonWhitespace(additionalFieldsEl.getValue())) {
			return true;
		}
		Map<String, String> fields = JupyterHub.parseFields(additionalFieldsEl.getValue());
		if (fields.isEmpty()) {
			additionalFieldsEl.setErrorKey("form.hub.additionalFields.error");
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
			jupyterHubUrlEl.setErrorKey("form.mandatory.hover");
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
		String ramGuarantee = ramGuaranteeEl.getValue();
		String ramLimit = ramLimitEl.getValue();
		BigDecimal cpuGuarantee = parseAsBigDecimal(cpuGuaranteeEl);
		BigDecimal cpuLimit = parseAsBigDecimal(cpuLimitEl);
		String additionalFields = additionalFieldsEl.getValue();
		String imageCheckingServiceUrl = imageCheckingServiceUrlEl.getValue();
		String infoText = infoTextEl.getValue();
		JupyterHub.AgreementSetting agreementSetting = JupyterHub.AgreementSetting.valueOf(dataTransmissionAgreementEl.getSelectedKey());

		if (jupyterHub == null) {
			jupyterHub = jupyterManager.createJupyterHub(name, jupyterHubUrl, clientId, ramGuarantee, ramLimit,
					cpuGuarantee, cpuLimit, additionalFields, agreementSetting);
		} else {
			jupyterHub.setName(nameEl.getValue());
			jupyterHub.setJupyterHubUrl(jupyterHubUrl);
			jupyterHub.setRamGuarantee(ramGuarantee);
			jupyterHub.setRamLimit(ramLimit);
			jupyterHub.setCpuGuarantee(cpuGuarantee);
			jupyterHub.setCpuLimit(cpuLimit);
			jupyterHub.setAdditionalFields(additionalFields);
		}

		jupyterHub.setImageCheckingServiceUrl(imageCheckingServiceUrl);
		jupyterHub.setInfoText(infoText);

		jupyterManager.updateJupyterHub(jupyterHub);

		fireEvent(ureq, Event.DONE_EVENT);
	}

	private BigDecimal parseAsBigDecimal(TextElement cpuEl) {
		if (!StringHelper.containsNonWhitespace(cpuEl.getValue())) {
			return null;
		}
		double cpuAsDouble = Double.parseDouble(cpuEl.getValue());
		return BigDecimal.valueOf(cpuAsDouble);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == checkConnectionButton) {
			doCheckConnection();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doCheckConnection() {
		JupyterManager.CheckConnectionResponse response = jupyterManager.checkConnection(
				jupyterHubUrlEl.getValue(), clientId, getIdentity().getKey().toString());
		if (response.success()) {
			showInfo("jupyterHub.checkConnection.ok", jupyterHubUrlEl.getValue());
		} else {
			showError("jupyterHub.checkConnection.error", new String[] { jupyterHubUrlEl.getValue(), response.message() });
		}
	}
}
