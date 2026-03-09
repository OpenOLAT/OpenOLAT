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
package org.olat.core.commons.services.ai.ui;

import java.util.List;

import org.olat.core.commons.services.ai.AiApiKeySPI;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * Generic admin controller for AI service providers that authenticate via an
 * API key. The SPI must implement {@link AiApiKeySPI} (and typically also
 * {@link AiSPI}).
 * <p>
 * Behaviour:
 * <ul>
 *   <li>No key stored &rarr; empty field + warning</li>
 *   <li>Key stored &rarr; PLACEHOLDER shown; "Check API key" button visible</li>
 *   <li>Save with PLACEHOLDER unchanged &rarr; no-op</li>
 *   <li>Save with new non-empty value &rarr; verify against provider, then store</li>
 *   <li>Save with empty value &rarr; remove stored key</li>
 * </ul>
 *
 * Initial date: 28.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class GenericAiApiKeyAdminController extends FormBasicController {
	public static final Event DELETE_EVENT = new Event("delete-spi");

	private static final String PLACEHOLDER = "xxx-placeholder-xxx";
	private static final int KEY_MAXLENGTH = 512;

	private FormToggle enabledToggle;
	private TextElement apiKeyEl;
	private FormLink checkKeyLink;
	private FormLink deleteLink;

	// Set by validateFormLogic when a new key passes API verification; consumed by formOK
	private List<String> verifiedModels;

	private final AiApiKeySPI spi;
	private final AiSPI aiSpi;
	private final String spiName;

	/**
	 * @param ureq
	 * @param wControl
	 * @param spi      the provider; must implement {@link AiApiKeySPI}
	 */
	public GenericAiApiKeyAdminController(UserRequest ureq, WindowControl wControl, AiApiKeySPI spi) {
		super(ureq, wControl);
		this.spi = spi;
		this.aiSpi = (spi instanceof AiSPI s) ? s : null;
		this.spiName = aiSpi != null ? aiSpi.getName() : spi.getClass().getSimpleName();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle(spi.getAdminTitleI18nKey());
		setFormDescription(spi.getAdminDescI18nKey());

		// Enable/disable toggle
		if (aiSpi != null) {
			enabledToggle = uifactory.addToggleButton("enabled", "ai.spi.enabled",
					translate("on"), translate("off"), formLayout);
			enabledToggle.addActionListener(FormEvent.ONCHANGE);
			if (aiSpi.isEnabled()) {
				enabledToggle.toggleOn();
			} else {
				enabledToggle.toggleOff();
			}
		}

		boolean hasKey = StringHelper.containsNonWhitespace(spi.getApiKey());
		apiKeyEl = uifactory.addPasswordElement("apikey", spi.getAdminApiKeyI18nKey(), KEY_MAXLENGTH,
				hasKey ? PLACEHOLDER : "", formLayout);
		if (!hasKey) {
			apiKeyEl.setWarningKey("ai.apikey.not.set", spiName);
		}

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		checkKeyLink = uifactory.addFormLink("ai.apikey.check", buttonsCont, Link.BUTTON);
		checkKeyLink.setGhost(true);
		checkKeyLink.getComponent().setSuppressDirtyFormWarning(true);
		checkKeyLink.setVisible(hasKey);
		deleteLink = uifactory.addFormLink("ai.delete.config", buttonsCont, Link.BUTTON);
		deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == checkKeyLink) {
			doCheckStoredKey();
		} else if (source == enabledToggle && aiSpi != null) {
			aiSpi.setEnabled(enabledToggle.isOn());
			logAudit("AI provider " + spiName + " enabled: " + enabledToggle.isOn());
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == deleteLink) {
			fireEvent(ureq, DELETE_EVENT);
		}
	}

	private void doCheckStoredKey() {
		apiKeyEl.clearError();
		try {
			List<String> models = spi.verifyApiKey(spi.getApiKey());
			showInfo("ai.apikey.verify.success", new String[] { spiName, String.valueOf(models.size()) });
		} catch (Exception e) {
			apiKeyEl.setErrorKey("ai.apikey.verify.error", spiName);
			showError("ai.apikey.verify.error.detail", e.getMessage());
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		verifiedModels = null;
		boolean allOk = super.validateFormLogic(ureq);
		apiKeyEl.clearError();

		String val = apiKeyEl.getValue();
		if (val.length() >= KEY_MAXLENGTH) {
			apiKeyEl.setErrorKey("form.legende.form.error.toolong", String.valueOf(KEY_MAXLENGTH));
			return false;
		}

		// Verify new key against the provider API (skip if unchanged placeholder)
		if (StringHelper.containsNonWhitespace(val) && !PLACEHOLDER.equals(val)) {
			try {
				verifiedModels = spi.verifyApiKey(val.trim());
			} catch (Exception e) {
				apiKeyEl.setErrorKey("ai.apikey.verify.error", spiName);
				showError("ai.apikey.verify.error.detail", e.getMessage());
				allOk = false;
			}
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String val = apiKeyEl.getValue().trim();
		if (PLACEHOLDER.equals(val)) {
			// Key unchanged, nothing to do
			fireEvent(ureq, Event.DONE_EVENT);
			return;
		}

		spi.setApiKey(val);
		logAudit(spiName + " API key has been updated");

		if (verifiedModels != null) {
			apiKeyEl.setValue(PLACEHOLDER);
			apiKeyEl.clearWarning();
			checkKeyLink.setVisible(true);
			showInfo("ai.apikey.verify.success", new String[] { spiName, String.valueOf(verifiedModels.size()) });
		} else {
			// Empty value was saved — key removed
			apiKeyEl.setValue("");
			apiKeyEl.setWarningKey("ai.apikey.not.set", spiName);
			checkKeyLink.setVisible(false);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
