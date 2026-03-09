/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai.spi.generic;

import java.util.List;

import org.olat.core.commons.services.ai.ui.AiAdminController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * Admin form for a single generic OpenAI-compatible AI provider instance.
 * Shows fields for name, base URL, API key (optional), models (comma-separated),
 * an enable/disable toggle, and save/check/delete buttons.
 *
 * Fires Event.DONE_EVENT on save, and a custom DELETE_EVENT when the
 * delete action is confirmed.
 *
 * Initial date: 09.03.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class GenericAiSpiAdminController extends FormBasicController {
	public static final Event DELETE_EVENT = new Event("delete-generic-spi");

	private static final String PLACEHOLDER = "xxx-placeholder-xxx";
	private static final int KEY_MAXLENGTH = 512;

	private FormToggle enabledToggle;
	private TextElement nameEl;
	private TextElement baseUrlEl;
	private TextElement apiKeyEl;
	private TextAreaElement modelsEl;
	private FormLink checkLink;
	private FormLink deleteLink;

	private final GenericAiSpiInstance instance;

	public GenericAiSpiAdminController(UserRequest ureq, WindowControl wControl, GenericAiSpiInstance instance) {
		super(ureq, wControl, Util.createPackageTranslator(AiAdminController.class, ureq.getLocale()));
		this.instance = instance;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("ai.generic.title");
		setFormDescription("ai.generic.desc");

		enabledToggle = uifactory.addToggleButton("enabled", "ai.spi.enabled",
				translate("on"), translate("off"), formLayout);
		enabledToggle.addActionListener(FormEvent.ONCHANGE);
		if (instance.isEnabled()) {
			enabledToggle.toggleOn();
		} else {
			enabledToggle.toggleOff();
		}

		nameEl = uifactory.addTextElement("name", "ai.generic.name", 256,
				instance.getName().startsWith("Generic #") ? "" : instance.getName(), formLayout);
		nameEl.setMandatory(true);

		baseUrlEl = uifactory.addTextElement("baseUrl", "ai.generic.base.url", 1024,
				instance.getBaseUrl(), formLayout);
		baseUrlEl.setMandatory(true);
		baseUrlEl.setExampleKey("ai.generic.base.url.example", null);

		boolean hasKey = StringHelper.containsNonWhitespace(instance.getApiKey());
		apiKeyEl = uifactory.addPasswordElement("apikey", "ai.generic.apikey", KEY_MAXLENGTH,
				hasKey ? PLACEHOLDER : "", formLayout);

		modelsEl = uifactory.addTextAreaElement("models", "ai.generic.models", 4000,
				3, 60, false, false, instance.getModels(), formLayout);
		modelsEl.setHelpTextKey("ai.generic.models.help", null);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		checkLink = uifactory.addFormLink("ai.generic.check", buttonsCont, Link.BUTTON);
		checkLink.setGhost(true);
		checkLink.getComponent().setSuppressDirtyFormWarning(true);
		deleteLink = uifactory.addFormLink("ai.delete.config", buttonsCont, Link.BUTTON);
		deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledToggle) {
			instance.setEnabled(enabledToggle.isOn());
			logAudit("Generic AI provider [" + instance.getName() + "] enabled: " + enabledToggle.isOn());
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == checkLink) {
			doCheckConnection();
		} else if (source == deleteLink) {
			fireEvent(ureq, DELETE_EVENT);
		}
	}

	private void doCheckConnection() {
		baseUrlEl.clearError();
		try {
			// Use the currently stored API key (not the form field which may be PLACEHOLDER)
			List<String> models = instance.verifyApiKey(instance.getApiKey());
			showInfo("ai.apikey.verify.success", new String[] { instance.getName(), String.valueOf(models.size()) });
		} catch (Exception e) {
			baseUrlEl.setErrorKey("ai.apikey.verify.error", instance.getName());
			showError("ai.apikey.verify.error.detail", e.getMessage());
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		nameEl.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		baseUrlEl.clearError();
		if (!StringHelper.containsNonWhitespace(baseUrlEl.getValue())) {
			baseUrlEl.setErrorKey("form.legende.mandatory");
			allOk = false;
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		instance.setName(nameEl.getValue().trim());
		instance.setBaseUrl(baseUrlEl.getValue().trim());
		instance.setModels(modelsEl.getValue().trim());

		// Handle API key: only update if changed from placeholder
		String keyVal = apiKeyEl.getValue().trim();
		if (!PLACEHOLDER.equals(keyVal)) {
			instance.setApiKey(keyVal);
		}

		// Show placeholder if key is set
		if (StringHelper.containsNonWhitespace(instance.getApiKey())) {
			apiKeyEl.setValue(PLACEHOLDER);
		} else {
			apiKeyEl.setValue("");
		}

		logAudit("Generic AI provider saved: " + instance.getName() + " [" + instance.getId() + "]");
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
