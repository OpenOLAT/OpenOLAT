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

import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiImageDescriptionService;
import org.olat.core.commons.services.ai.AiMCQuestionService;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin form to configure which AI provider and model to use for each AI
 * feature. Currently supports: MC Question Generator, Image Description Generator.
 * Each feature has an enable toggle, a provider dropdown, and a model selector.
 *
 * Initial date: 25.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiFeaturesAdminController extends FormBasicController {

	private FormItemContainer formLayout;

	// MC Question Generator elements
	private FormToggle mcEnabledEl;
	private SingleSelection mcGeneratorSpiEl;
	private FormItem mcGeneratorModelEl;
	private FormLink mcTestLink;

	// Image Description Generator elements — removed and re-added to maintain ordering
	private SpacerElement imgDescSpacer;
	private StaticTextElement imgDescTitle;
	private FormToggle imgDescEnabledEl;
	private SingleSelection imgDescSpiEl;
	private FormItem imgDescModelEl;
	private FormLink imgDescTestLink;

	// Buttons — removed and re-added to maintain ordering
	private FormLayoutContainer buttonsCont;

	// Test controller
	private AiFeaturesTestController testCtrl;

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiMCQuestionService mcQuestionService;
	@Autowired
	private AiImageDescriptionService imageDescriptionService;

	public AiFeaturesAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		this.formLayout = formLayout;
		setFormTitle("ai.features.title");
		setFormDescription("ai.features.desc");

		// ---- MC Question Generator section ----
		uifactory.addSpacerElement("mcSpacer", formLayout, false);
		uifactory.addStaticTextElement("mcTitle", null,
				"<h4>" + translate(AiFeature.MCQuestionGenerator.getI18nKey()) + "</h4>", formLayout);

		boolean mcEnabled = aiModule.isMCQuestionGeneratorEnabled();
		mcEnabledEl = uifactory.addToggleButton("mc.enabled", "ai.feature.enabled",
				translate("on"), translate("off"), formLayout);
		mcEnabledEl.addActionListener(FormEvent.ONCHANGE);
		mcEnabledEl.toggle(mcEnabled);

		mcGeneratorSpiEl = buildSpiDropdown("mc.spi", aiModule.getMCGeneratorSpiId(), formLayout);

		// ---- Image Description Generator section ----
		imgDescSpacer = uifactory.addSpacerElement("imgDescSpacer", formLayout, false);
		imgDescTitle = uifactory.addStaticTextElement("imgDescTitle", null,
				"<h4>" + translate(AiFeature.ImageDescriptionGenerator.getI18nKey()) + "</h4>", formLayout);

		boolean imgDescEnabled = aiModule.isImageDescriptionGeneratorEnabled();
		imgDescEnabledEl = uifactory.addToggleButton("imgDesc.enabled", "ai.feature.enabled",
				translate("on"), translate("off"), formLayout);
		imgDescEnabledEl.addActionListener(FormEvent.ONCHANGE);
		imgDescEnabledEl.toggle(imgDescEnabled);

		imgDescSpiEl = buildSpiDropdown("imgDesc.spi", aiModule.getImgDescSpiId(), formLayout);

		// Save button
		buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);

		// Create initial model elements and set visibility
		doUpdateMcModelDropdown(getSelectedKey(mcGeneratorSpiEl));
		doUpdateImgDescModelDropdown(getSelectedKey(imgDescSpiEl));
		updateMcVisibility();
		updateImgDescVisibility();
	}

	private SingleSelection buildSpiDropdown(String elName, String currentSpiId, FormItemContainer container) {
		List<AiSPI> spis = aiModule.getEnabledProviders();
		String[] keys;
		String[] values;
		if (spis.isEmpty()) {
			keys = new String[] { "-" };
			values = new String[] { translate("ai.feature.spi.none") };
		} else {
			keys = new String[spis.size()];
			values = new String[spis.size()];
			for (int i = 0; i < spis.size(); i++) {
				keys[i] = spis.get(i).getId();
				values[i] = spis.get(i).getName();
			}
		}

		SingleSelection spiEl = uifactory.addDropdownSingleselect(elName, "ai.feature.spi",
				container, keys, values, null);
		spiEl.setEnabled(!spis.isEmpty());
		spiEl.addActionListener(FormEvent.ONCHANGE);

		if (StringHelper.containsNonWhitespace(currentSpiId)) {
			for (String key : keys) {
				if (key.equals(currentSpiId)) {
					spiEl.select(key, true);
					break;
				}
			}
		}
		return spiEl;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == mcGeneratorSpiEl) {
			doUpdateMcModelDropdown(getSelectedKey(mcGeneratorSpiEl));
		} else if (source == imgDescSpiEl) {
			doUpdateImgDescModelDropdown(getSelectedKey(imgDescSpiEl));
		} else if (source == mcEnabledEl) {
			if (mcEnabledEl.isOn()) {
				doUpdateMcModelDropdown(getSelectedKey(mcGeneratorSpiEl));
			} else {
				updateMcVisibility();
			}
		} else if (source == imgDescEnabledEl) {
			if (imgDescEnabledEl.isOn()) {
				doUpdateImgDescModelDropdown(getSelectedKey(imgDescSpiEl));
			} else {
				updateImgDescVisibility();
			}
		} else if (source == mcTestLink) {
			doTestMcGenerator(ureq);
		} else if (source == imgDescTestLink) {
			doTestImgDescGenerator(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == testCtrl) {
			// test controller handles its own modal cleanup
		}
		super.event(ureq, source, event);
	}

	private void updateMcVisibility() {
		boolean on = mcEnabledEl.isOn();
		mcGeneratorSpiEl.setVisible(on);
		if (mcGeneratorModelEl != null) {
			mcGeneratorModelEl.setVisible(on);
		}
		if (mcTestLink != null) {
			mcTestLink.setVisible(on && hasSpiSelected(mcGeneratorSpiEl) && hasModelValue(mcGeneratorModelEl));
		}
	}

	private void updateImgDescVisibility() {
		boolean on = imgDescEnabledEl.isOn();
		imgDescSpiEl.setVisible(on);
		if (imgDescModelEl != null) {
			imgDescModelEl.setVisible(on);
		}
		if (imgDescTestLink != null) {
			imgDescTestLink.setVisible(on && hasSpiSelected(imgDescSpiEl) && hasModelValue(imgDescModelEl));
		}
	}

	private void doUpdateMcModelDropdown(String spiId) {
		if (mcGeneratorModelEl != null) {
			formLayout.remove(mcGeneratorModelEl);
		}
		if (mcTestLink != null) {
			formLayout.remove(mcTestLink);
		}
		removeImgDescSection();
		formLayout.remove(buttonsCont);

		mcGeneratorModelEl = buildModelElement(spiId, "ai.feature.model",
				getModelsForSpi(spiId), aiModule.getMCGeneratorModel());

		mcTestLink = uifactory.addFormLink("mc.test", "mc.test", "ai.feature.test", null, formLayout, Link.BUTTON_SMALL);
		mcTestLink.setGhost(true);
		mcTestLink.setIconLeftCSS("o_icon o_icon_ai");

		reAddImgDescSection();
		formLayout.add(buttonsCont);
		updateMcVisibility();
	}

	private void doUpdateImgDescModelDropdown(String spiId) {
		if (imgDescModelEl != null) {
			formLayout.remove(imgDescModelEl);
		}
		if (imgDescTestLink != null) {
			formLayout.remove(imgDescTestLink);
		}
		formLayout.remove(buttonsCont);

		imgDescModelEl = buildModelElement(spiId, "ai.feature.image-description-generator.model",
				getModelsForSpi(spiId), aiModule.getImgDescModel());

		imgDescTestLink = uifactory.addFormLink("imgDesc.test", "imgDesc.test", "ai.feature.test", null, formLayout, Link.BUTTON_SMALL);
		imgDescTestLink.setGhost(true);
		imgDescTestLink.setIconLeftCSS("o_icon o_icon_ai");

		formLayout.add(buttonsCont);
		updateImgDescVisibility();
	}

	private void removeImgDescSection() {
		if (imgDescModelEl != null) {
			formLayout.remove(imgDescModelEl);
		}
		if (imgDescTestLink != null) {
			formLayout.remove(imgDescTestLink);
		}
		formLayout.remove(imgDescSpiEl);
		formLayout.remove(imgDescEnabledEl);
		formLayout.remove(imgDescTitle);
		formLayout.remove(imgDescSpacer);
	}

	private void reAddImgDescSection() {
		formLayout.add(imgDescSpacer);
		formLayout.add(imgDescTitle);
		formLayout.add(imgDescEnabledEl);
		formLayout.add(imgDescSpiEl);
		if (imgDescModelEl != null) {
			formLayout.add(imgDescModelEl);
		}
		if (imgDescTestLink != null) {
			formLayout.add(imgDescTestLink);
		}
	}

	private FormItem buildModelElement(String spiId, String labelKey, List<String> models, String currentModel) {
		String elName = labelKey + "." + CodeHelper.getRAMUniqueID();
		boolean hasSpi = StringHelper.containsNonWhitespace(spiId) && !"-".equals(spiId);

		if (!models.isEmpty()) {
			String[] keys = models.toArray(new String[0]);
			SingleSelection dropdown = uifactory.addDropdownSingleselect(elName, labelKey,
					formLayout, keys, keys, null);
			dropdown.setMandatory(true);
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
			return dropdown;
		} else {
			TextElement textEl = uifactory.addTextElement(elName, labelKey, 256,
					hasSpi ? currentModel : "", formLayout);
			textEl.setMandatory(hasSpi);
			textEl.setEnabled(hasSpi);
			return textEl;
		}
	}

	private List<String> getModelsForSpi(String spiId) {
		if (!StringHelper.containsNonWhitespace(spiId) || "-".equals(spiId)) {
			return List.of();
		}
		for (AiSPI spi : aiModule.getAiProviders()) {
			if (spi.getId().equals(spiId)) {
				return spi.getAvailableModels();
			}
		}
		return List.of();
	}

	// ------ Test methods ------

	private void doTestMcGenerator(UserRequest ureq) {
		String spiId = getSelectedSpiId(mcGeneratorSpiEl);
		String model = extractModelValue(mcGeneratorModelEl);
		if (!StringHelper.containsNonWhitespace(spiId) || !StringHelper.containsNonWhitespace(model)) {
			showError("ai.feature.test.not.configured");
			return;
		}
		ensureTestCtrl(ureq);
		testCtrl.testMcGenerator(spiId, model);
	}

	private void doTestImgDescGenerator(UserRequest ureq) {
		String spiId = getSelectedSpiId(imgDescSpiEl);
		String model = extractModelValue(imgDescModelEl);
		if (!StringHelper.containsNonWhitespace(spiId) || !StringHelper.containsNonWhitespace(model)) {
			showError("ai.feature.test.not.configured");
			return;
		}
		ensureTestCtrl(ureq);
		testCtrl.testImgDescGenerator(spiId, model);
	}

	private void ensureTestCtrl(UserRequest ureq) {
		if (testCtrl == null) {
			testCtrl = new AiFeaturesTestController(ureq, getWindowControl(), mcQuestionService, imageDescriptionService);
			listenTo(testCtrl);
		}
	}

	// ------ Validation and Save ------

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (mcEnabledEl.isOn() && hasSpiSelected(mcGeneratorSpiEl) && mcGeneratorModelEl != null) {
			allOk &= validateModelElement(mcGeneratorModelEl);
		}
		if (imgDescEnabledEl.isOn() && hasSpiSelected(imgDescSpiEl) && imgDescModelEl != null) {
			allOk &= validateModelElement(imgDescModelEl);
		}

		return allOk;
	}

	private boolean validateModelElement(FormItem modelEl) {
		modelEl.clearError();
		if (modelEl instanceof TextElement textEl) {
			if (!StringHelper.containsNonWhitespace(textEl.getValue())) {
				textEl.setErrorKey("form.legende.mandatory");
				return false;
			}
		} else if (modelEl instanceof SingleSelection singleSel) {
			if (!singleSel.isOneSelected() || "-".equals(singleSel.getSelectedKey())) {
				singleSel.setErrorKey("form.legende.mandatory");
				return false;
			}
		}
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (mcEnabledEl.isOn()) {
			String mcSpiId = getSelectedSpiId(mcGeneratorSpiEl);
			String mcModel = extractModelValue(mcGeneratorModelEl);
			aiModule.setMCQuestionGeneratorConfig(mcSpiId, mcModel);
			logAudit("MC question generator configured: provider=" + mcSpiId + ", model=" + mcModel);
		} else {
			aiModule.setMCQuestionGeneratorConfig("", "");
			logAudit("MC question generator disabled");
		}

		if (imgDescEnabledEl.isOn()) {
			String imgDescSpiId = getSelectedSpiId(imgDescSpiEl);
			String imgDescModel = extractModelValue(imgDescModelEl);
			aiModule.setImageDescriptionGeneratorConfig(imgDescSpiId, imgDescModel);
			logAudit("Image description generator configured: provider=" + imgDescSpiId + ", model=" + imgDescModel);
		} else {
			aiModule.setImageDescriptionGeneratorConfig("", "");
			logAudit("Image description generator disabled");
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	// ------ Helpers ------

	private String getSelectedKey(SingleSelection el) {
		return el.isOneSelected() ? el.getSelectedKey() : null;
	}

	private boolean hasSpiSelected(SingleSelection el) {
		return el.isEnabled() && el.isOneSelected() && !"-".equals(el.getSelectedKey());
	}

	private String getSelectedSpiId(SingleSelection el) {
		if (hasSpiSelected(el)) {
			return el.getSelectedKey();
		}
		return null;
	}

	private String extractModelValue(FormItem modelEl) {
		if (modelEl instanceof TextElement textEl && textEl.isEnabled()) {
			String val = textEl.getValue();
			if (StringHelper.containsNonWhitespace(val)) {
				return val.trim();
			}
		} else if (modelEl instanceof SingleSelection singleSel && singleSel.isEnabled()
				&& singleSel.isOneSelected()) {
			String key = singleSel.getSelectedKey();
			if (!"-".equals(key)) {
				return key;
			}
		}
		return null;
	}

	private boolean hasModelValue(FormItem modelEl) {
		return StringHelper.containsNonWhitespace(extractModelValue(modelEl));
	}
}
