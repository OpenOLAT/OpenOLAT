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

import org.olat.core.commons.services.ai.AiMCQuestionGeneratorSPI;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin form to configure which AI provider and model to use for each AI
 * feature. Currently supports: MC Question Generator.
 * When a provider is selected, the available models for that provider are
 * loaded and shown as a dropdown. If the API call fails or no API key is
 * configured, a free text input is shown as fallback.
 *
 * Initial date: 25.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiFeaturesAdminController extends FormBasicController {

	private FormItemContainer formLayout;
	private FormLayoutContainer mcButtonsCont;
	private SingleSelection mcGeneratorSpiEl;

	// The current model element: SingleSelection (dropdown) when models are loaded from API,
	// TextElement (free text) as fallback when API call fails or no API key is set.
	// Added directly to formLayout; replaced by removing and re-adding before mcButtonsCont.
	private FormItem mcGeneratorModelEl;

	@Autowired
	private AiModule aiModule;

	/**
	 * Standard constructor
	 *
	 * @param ureq
	 * @param wControl
	 */
	public AiFeaturesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		this.formLayout = formLayout;
		setFormTitle("ai.features.title");
		setFormDescription("ai.features.desc");

		// Build SPI dropdown with enabled SPIs that implement the MC generator feature
		List<AiSPI> mcSpis = aiModule.getEnabledSPIsFor(AiMCQuestionGeneratorSPI.class);
		String[] spiKeys;
		String[] spiValues;
		if (mcSpis.isEmpty()) {
			spiKeys = new String[] { "-" };
			spiValues = new String[] { translate("ai.feature.spi.none") };
		} else {
			spiKeys = new String[mcSpis.size()];
			spiValues = new String[mcSpis.size()];
			for (int i = 0; i < mcSpis.size(); i++) {
				spiKeys[i] = mcSpis.get(i).getId();
				spiValues[i] = mcSpis.get(i).getName();
			}
		}

		mcGeneratorSpiEl = uifactory.addDropdownSingleselect("ai.feature.spi", "ai.feature.spi",
				formLayout, spiKeys, spiValues, null);
		mcGeneratorSpiEl.setEnabled(!mcSpis.isEmpty());
		mcGeneratorSpiEl.addActionListener(FormEvent.ONCHANGE);

		// Pre-select currently configured SPI
		String currentSpiId = aiModule.getMCGeneratorSpiId();
		if (StringHelper.containsNonWhitespace(currentSpiId)) {
			for (String key : spiKeys) {
				if (key.equals(currentSpiId)) {
					mcGeneratorSpiEl.select(key, true);
					break;
				}
			}
		}

		// Save button - added now so it exists; doUpdateModelDropdown re-adds it after the model element
		mcButtonsCont = FormLayoutContainer.createButtonLayout("mcButtons", getTranslator());
		mcButtonsCont.setRootForm(mainForm);
		formLayout.add(mcButtonsCont);
		uifactory.addFormSubmitButton("save", mcButtonsCont);

		// Create initial model element based on current/pre-selected SPI.
		// This removes and re-adds mcButtonsCont so the order is: spi → model → buttons.
		String initialSpiId = mcGeneratorSpiEl.isOneSelected() ? mcGeneratorSpiEl.getSelectedKey() : null;
		doUpdateModelDropdown(initialSpiId);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == mcGeneratorSpiEl) {
			String selectedKey = mcGeneratorSpiEl.isOneSelected() ? mcGeneratorSpiEl.getSelectedKey() : null;
			doUpdateModelDropdown(selectedKey);
		}
	}

	/**
	 * Replace the model element with one populated from the given SPI's available models.
	 * If the API call succeeds, shows a dropdown. If no models are available (no API key
	 * configured or API call failed), shows a free text input as fallback.
	 * The buttons container is re-added after the new element to keep correct ordering.
	 *
	 * @param spiId The SPI identifier, or null/"-" for no selection
	 */
	private void doUpdateModelDropdown(String spiId) {
		// Remove old model element and buttons container from formLayout
		if (mcGeneratorModelEl != null) {
			formLayout.remove(mcGeneratorModelEl);
			mcGeneratorModelEl = null;
		}
		formLayout.remove(mcButtonsCont);

		// Get available models for the selected SPI (live API call)
		List<String> models = getModelsForSpi(spiId);
		String currentModel = aiModule.getMCGeneratorModel();

		// Use a unique element name each time to avoid form component name conflicts
		String elName = "ai.feature.model." + CodeHelper.getRAMUniqueID();

		if (!models.isEmpty()) {
			// Show dropdown with models loaded from the provider API
			String[] keys = models.toArray(new String[0]);
			SingleSelection dropdown = uifactory.addDropdownSingleselect(elName, "ai.feature.model",
					formLayout, keys, keys, null);
			dropdown.setMandatory(true);

			// Pre-select currently configured model if it's in the list, else first item
			boolean selected = false;
			if (StringHelper.containsNonWhitespace(currentModel)) {
				for (String key : keys) {
					if (key.equals(currentModel)) {
						dropdown.select(key, true);
						selected = true;
						break;
					}
				}
			}
			if (!selected) {
				dropdown.select(keys[0], true);
			}
			mcGeneratorModelEl = dropdown;
		} else {
			// Fallback: API not reachable or no API key configured - show free text input
			boolean hasSpi = StringHelper.containsNonWhitespace(spiId) && !"-".equals(spiId);
			TextElement textEl = uifactory.addTextElement(elName, "ai.feature.model", 256,
					hasSpi ? currentModel : "", formLayout);
			textEl.setMandatory(hasSpi);
			textEl.setEnabled(hasSpi);
			mcGeneratorModelEl = textEl;
		}

		// Re-add buttons container so it stays after the model element
		formLayout.add(mcButtonsCont);
	}

	/**
	 * Get the list of available models for the given SPI.
	 *
	 * @param spiId The SPI identifier
	 * @return List of model names, empty if not found or no models
	 */
	private List<String> getModelsForSpi(String spiId) {
		if (!StringHelper.containsNonWhitespace(spiId) || "-".equals(spiId)) {
			return List.of();
		}
		for (AiSPI spi : aiModule.getAiProviders()) {
			if (spi.getId().equals(spiId) && spi instanceof AiMCQuestionGeneratorSPI generator) {
				return generator.getAvailableMCGeneratorModels();
			}
		}
		return List.of();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		// Validate model when a valid SPI is selected
		boolean hasSpi = mcGeneratorSpiEl.isEnabled() && mcGeneratorSpiEl.isOneSelected()
				&& !"-".equals(mcGeneratorSpiEl.getSelectedKey());
		if (hasSpi && mcGeneratorModelEl != null) {
			mcGeneratorModelEl.clearError();
			if (mcGeneratorModelEl instanceof TextElement textEl) {
				if (!StringHelper.containsNonWhitespace(textEl.getValue())) {
					textEl.setErrorKey("form.legende.mandatory");
					allOk = false;
				}
			} else if (mcGeneratorModelEl instanceof SingleSelection singleSel) {
				if (!singleSel.isOneSelected() || "-".equals(singleSel.getSelectedKey())) {
					singleSel.setErrorKey("form.legende.mandatory");
					allOk = false;
				}
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String spiId = null;
		if (mcGeneratorSpiEl.isEnabled() && mcGeneratorSpiEl.isOneSelected()) {
			String selectedKey = mcGeneratorSpiEl.getSelectedKey();
			if (!"-".equals(selectedKey)) {
				spiId = selectedKey;
			}
		}

		String model = null;
		if (mcGeneratorModelEl instanceof TextElement textEl && textEl.isEnabled()) {
			String val = textEl.getValue();
			if (StringHelper.containsNonWhitespace(val)) {
				model = val.trim();
			}
		} else if (mcGeneratorModelEl instanceof SingleSelection singleSel && singleSel.isEnabled()
				&& singleSel.isOneSelected()) {
			String key = singleSel.getSelectedKey();
			if (!"-".equals(key)) {
				model = key;
			}
		}

		aiModule.setMCQuestionGeneratorConfig(spiId, model);
		logAudit("MC question generator configured: provider=" + spiId + ", model=" + model);

		fireEvent(ureq, Event.DONE_EVENT);
	}
}
