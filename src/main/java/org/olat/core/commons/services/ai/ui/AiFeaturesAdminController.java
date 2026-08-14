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
import java.util.Objects;

import org.olat.core.commons.services.ai.AiEmbeddingSPI;
import org.olat.core.commons.services.ai.AiEssayGenerationService;
import org.olat.core.commons.services.ai.AiEssayGradingService;
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
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin form to configure which AI provider and model to use for each AI
 * feature. Currently supports: Taxonomy Matching, Image Description Generator,
 * MC Question Generator, Essay Question Generator, Essay Grading. Each feature
 * has an enable toggle, a provider dropdown, and a model selector.
 *
 * All form items are created once in {@link #initForm}, in their final visual
 * order. The model selector is the only item whose content depends on the
 * selected provider: a provider that publishes a model list shows a dropdown,
 * a provider that does not shows a free-text field. Both elements are created
 * up front; on a provider change the dropdown is repopulated in place
 * ({@link SingleSelection#setKeysAndValues}) and the active element is toggled
 * via visibility. No form item is ever removed and re-added, so the rendered
 * order is fixed by the init order alone.
 *
 * Initial date: 25.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiFeaturesAdminController extends FormBasicController {

	// No model can produce valid structured JSON output (required keys, nested
	// arrays) in fewer tokens than this, for any of the four AI features.
	private static final int MIN_MAX_OUTPUT_TOKENS = 1024;

	// A timeout below this can never let a real AI call complete and would
	// just cause constant spurious timeouts.
	private static final int MIN_TIMEOUT_SECONDS = 10;

	// Taxonomy Matching elements
	private FormToggle taxMatchEnabledEl;
	private SingleSelection taxMatchSpiEl;
	private SingleSelection taxMatchModelDropdownEl;
	private TextElement taxMatchModelTextEl;
	private FormItem taxMatchModelEl;

	// Image Description Generator elements
	private FormToggle imgDescEnabledEl;
	private SingleSelection imgDescSpiEl;
	private SingleSelection imgDescModelDropdownEl;
	private TextElement imgDescModelTextEl;
	private FormItem imgDescModelEl;
	private IntegerElement imgDescMaxOutputTokensEl;
	private IntegerElement imgDescTimeoutSecondsEl;
	private FormLink imgDescTestLink;

	// MC Question Generator elements
	private FormToggle mcEnabledEl;
	private SingleSelection mcGeneratorSpiEl;
	private SingleSelection mcModelDropdownEl;
	private TextElement mcModelTextEl;
	private FormItem mcGeneratorModelEl;
	private IntegerElement mcMaxOutputTokensEl;
	private IntegerElement mcTimeoutSecondsEl;
	private FormLink mcTestLink;

	// Essay Question Generator elements
	private FormToggle essayGenEnabledEl;
	private SingleSelection essayGenSpiEl;
	private SingleSelection essayGenModelDropdownEl;
	private TextElement essayGenModelTextEl;
	private FormItem essayGenModelEl;
	private IntegerElement essayGenMaxOutputTokensEl;
	private IntegerElement essayGenTimeoutSecondsEl;
	private FormLink essayGenTestLink;

	// Essay Grading elements
	private FormToggle essayGradingEnabledEl;
	private SingleSelection essayGradingSpiEl;
	private SingleSelection essayGradingModelDropdownEl;
	private TextElement essayGradingModelTextEl;
	private FormItem essayGradingModelEl;
	private IntegerElement essayGradingMaxOutputTokensEl;
	private IntegerElement essayGradingTimeoutSecondsEl;
	private FormLink essayGradingTestLink;

	// Test controller
	private AiFeaturesTestController testCtrl;
	
	private final boolean readOnly;

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiMCQuestionService mcQuestionService;
	@Autowired
	private AiImageDescriptionService imageDescriptionService;
	@Autowired
	private  AiEssayGenerationService aiEssayGenerationService;
	@Autowired
	private AiEssayGradingService aiEssayGradingService;
	@Autowired
	private TaxonomyMatchingModule taxonomyMatchingModule;
	@Autowired
	private TaxonomyMatchingService taxonomyMatchingService;

	public AiFeaturesAdminController(UserRequest ureq, WindowControl wControl, boolean readOnly) {
		super(ureq, wControl);
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("ai.features.title");
		setFormDescription("ai.features.desc");

		// ---- Taxonomy Matching section ----
		uifactory.addSpacerElement("taxMatchSpacer", formLayout, false);
		StaticTextElement taxonomyTitleEl = uifactory.addStaticTextElement("taxMatchTitle", null,
				"<h4>" + translate(AiFeature.TaxonomyMatching.getI18nKey()) + "</h4>", formLayout);
		taxonomyTitleEl.setDomWrapperElement(DomWrapperElement.div);
		
		boolean taxMatchEnabled = taxonomyMatchingModule != null && taxonomyMatchingModule.isEnabled();
		taxMatchEnabledEl = uifactory.addToggleButton("taxMatch.enabled", "ai.feature.enabled",
				translate("on"), translate("off"), formLayout);
		taxMatchEnabledEl.addActionListener(FormEvent.ONCHANGE);
		taxMatchEnabledEl.toggle(taxMatchEnabled);
		taxMatchEnabledEl.setEnabled(!readOnly);

		String taxMatchSpiId = taxonomyMatchingModule != null ? taxonomyMatchingModule.getSpiId() : null;
		taxMatchSpiEl = buildEmbeddingSpiDropdown("taxMatch.spi", taxMatchSpiId, formLayout);
		taxMatchSpiEl.setEnabled(!readOnly);
		taxMatchModelDropdownEl = addModelDropdown("taxMatch.model", "ai.feature.taxonomy-matching.model", formLayout);
		taxMatchModelDropdownEl.setEnabled(!readOnly);
		taxMatchModelTextEl = addModelTextElement("taxMatch.model.text", "ai.feature.taxonomy-matching.model", formLayout);
		taxMatchModelTextEl.setEnabled(!readOnly);
		
		// ---- Image Description Generator section ----
		uifactory.addSpacerElement("imgDescSpacer", formLayout, false);
		StaticTextElement imgDescTitleEl = uifactory.addStaticTextElement("imgDescTitle", null,
				"<h4>" + translate(AiFeature.ImageDescriptionGenerator.getI18nKey()) + "</h4>", formLayout);
		imgDescTitleEl.setDomWrapperElement(DomWrapperElement.div);
		
		boolean imgDescEnabled = aiModule.isImageDescriptionGeneratorEnabled();
		imgDescEnabledEl = uifactory.addToggleButton("imgDesc.enabled", "ai.feature.enabled",
				translate("on"), translate("off"), formLayout);
		imgDescEnabledEl.addActionListener(FormEvent.ONCHANGE);
		imgDescEnabledEl.toggle(imgDescEnabled);
		imgDescEnabledEl.setEnabled(!readOnly);

		imgDescSpiEl = buildSpiDropdown("imgDesc.spi", aiModule.getImgDescSpiId(), formLayout,
				aiModule.getEnabledProviders());
		imgDescSpiEl.setEnabled(!readOnly);
		imgDescModelDropdownEl = addModelDropdown("imgDesc.model", "ai.feature.image-description-generator.model", formLayout); 
		imgDescModelDropdownEl.setEnabled(!readOnly);
		imgDescModelTextEl = addModelTextElement("imgDesc.model.text", "ai.feature.image-description-generator.model", formLayout);
		imgDescModelTextEl.setEnabled(!readOnly);
		imgDescMaxOutputTokensEl = addMaxOutputTokensElement("imgDesc.max.output.tokens",
				"ai.feature.image-description-generator.max-output-tokens", aiModule.getImgDescMaxOutputTokens(), formLayout);
		imgDescMaxOutputTokensEl.setEnabled(!readOnly);
		imgDescTimeoutSecondsEl = addTimeoutElement("imgDesc.timeout.seconds",
				"ai.feature.image-description-generator.timeout-seconds", aiModule.getImgDescTimeoutSeconds(), formLayout);
		imgDescTimeoutSecondsEl.setEnabled(!readOnly);
		imgDescTestLink = addTestLink("imgDesc.test", formLayout);

		// ---- MC Question Generator section ----
		uifactory.addSpacerElement("mcSpacer", formLayout, false);
		StaticTextElement mcQuestionTitleEl = uifactory.addStaticTextElement("mcTitle", null,
				"<h4>" + translate(AiFeature.MCQuestionGenerator.getI18nKey()) + "</h4>", formLayout);
		mcQuestionTitleEl.setDomWrapperElement(DomWrapperElement.div);
		
		boolean mcEnabled = aiModule.isMCQuestionGeneratorEnabled();
		mcEnabledEl = uifactory.addToggleButton("mc.enabled", "ai.feature.enabled",
				translate("on"), translate("off"), formLayout);
		mcEnabledEl.setEnabled(!readOnly);
		mcEnabledEl.addActionListener(FormEvent.ONCHANGE);
		mcEnabledEl.toggle(mcEnabled);

		mcGeneratorSpiEl = buildSpiDropdown("mc.spi", aiModule.getMCGeneratorSpiId(), formLayout,
				aiModule.getEnabledProviders());
		mcGeneratorSpiEl.setEnabled(!readOnly);
		mcModelDropdownEl = addModelDropdown("mc.model", "ai.feature.model", formLayout);
		mcModelDropdownEl.setEnabled(!readOnly);
		mcModelTextEl = addModelTextElement("mc.model.text", "ai.feature.model", formLayout);
		mcModelTextEl.setEnabled(!readOnly);
		mcMaxOutputTokensEl = addMaxOutputTokensElement("mc.max.output.tokens",
				"ai.feature.mc-question-generator.max-output-tokens", aiModule.getMCGeneratorMaxOutputTokens(), formLayout);
		mcMaxOutputTokensEl.setEnabled(!readOnly);
		mcTimeoutSecondsEl = addTimeoutElement("mc.timeout.seconds",
				"ai.feature.mc-question-generator.timeout-seconds", aiModule.getMCGeneratorTimeoutSeconds(), formLayout);
		mcTimeoutSecondsEl.setEnabled(!readOnly);
		mcTestLink = addTestLink("mc.test", formLayout);

		// ---- Essay Question Generator section ----
		uifactory.addSpacerElement("essayGenSpacer", formLayout, false);
		StaticTextElement essayGenTitleEl = uifactory.addStaticTextElement("essayGenTitle", null,
				"<h4>" + translate(AiFeature.EssayGeneration.getI18nKey()) + "</h4>", formLayout);
		essayGenTitleEl.setDomWrapperElement(DomWrapperElement.div);
		
		boolean essayGenEnabled = aiModule.isEssayGenerationEnabled();
		essayGenEnabledEl = uifactory.addToggleButton("essayGen.enabled", "ai.feature.enabled",
				translate("on"), translate("off"), formLayout);
		essayGenEnabledEl.setEnabled(!readOnly);
		essayGenEnabledEl.addActionListener(FormEvent.ONCHANGE);
		essayGenEnabledEl.toggle(essayGenEnabled);

		essayGenSpiEl = buildSpiDropdown("essayGen.spi", aiModule.getEssayGenerationSpiId(), formLayout,
				aiModule.getEnabledProviders());
		essayGenSpiEl.setEnabled(!readOnly);
		essayGenModelDropdownEl = addModelDropdown("essayGen.model", "ai.feature.model", formLayout);
		essayGenModelDropdownEl.setEnabled(!readOnly);
		essayGenModelTextEl = addModelTextElement("essayGen.model.text", "ai.feature.model", formLayout);
		essayGenModelTextEl.setEnabled(!readOnly);
		essayGenMaxOutputTokensEl = addMaxOutputTokensElement("essayGen.max.output.tokens",
				"ai.feature.essay-generation.max-output-tokens", aiModule.getEssayGenerationMaxOutputTokens(), formLayout);
		essayGenMaxOutputTokensEl.setEnabled(!readOnly);
		essayGenTimeoutSecondsEl = addTimeoutElement("essayGen.timeout.seconds",
				"ai.feature.essay-generation.timeout-seconds", aiModule.getEssayGenerationTimeoutSeconds(), formLayout);
		essayGenTimeoutSecondsEl.setEnabled(!readOnly);
		essayGenTestLink = addTestLink("essayGen.test", formLayout);

		// ---- Essay Grading section ----
		uifactory.addSpacerElement("essayGradingSpacer", formLayout, false);
		StaticTextElement essayGradingTitle = uifactory.addStaticTextElement("essayGradingTitle", null,
				"<h4>" + translate(AiFeature.EssayGrading.getI18nKey()) + "</h4>", formLayout);
		essayGradingTitle.setDomWrapperElement(DomWrapperElement.div);
		
		boolean essayGradingEnabled = aiModule.isEssayGradingEnabled();
		essayGradingEnabledEl = uifactory.addToggleButton("essayGrading.enabled", "ai.feature.enabled",
				translate("on"), translate("off"), formLayout);
		essayGradingEnabledEl.setEnabled(!readOnly);
		essayGradingEnabledEl.addActionListener(FormEvent.ONCHANGE);
		essayGradingEnabledEl.toggle(essayGradingEnabled);

		essayGradingSpiEl = buildSpiDropdown("essayGrading.spi", aiModule.getEssayGradingSpiId(), formLayout,
				aiModule.getEnabledProviders());
		essayGradingSpiEl.setEnabled(!readOnly);
		essayGradingModelDropdownEl = addModelDropdown("essayGrading.model", "ai.feature.model", formLayout);
		essayGradingModelDropdownEl.setEnabled(!readOnly);
		essayGradingModelTextEl = addModelTextElement("essayGrading.model.text", "ai.feature.model", formLayout);
		essayGradingModelTextEl.setEnabled(!readOnly);
		essayGradingMaxOutputTokensEl = addMaxOutputTokensElement("essayGrading.max.output.tokens",
				"ai.feature.essay-grading.max-output-tokens", aiModule.getEssayGradingMaxOutputTokens(), formLayout);
		essayGradingMaxOutputTokensEl.setEnabled(!readOnly);
		essayGradingTimeoutSecondsEl = addTimeoutElement("essayGrading.timeout.seconds",
				"ai.feature.essay-grading.timeout-seconds", aiModule.getEssayGradingTimeoutSeconds(), formLayout);
		essayGradingTimeoutSecondsEl.setEnabled(!readOnly);
		essayGradingTestLink = addTestLink("essayGrading.test", formLayout);

		// Save button
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		if(!readOnly) {
			uifactory.addFormSubmitButton("save", buttonsCont);
		}

		// Populate the model selectors and set the initial visibility. The order
		// of these calls no longer affects the layout, only the item content and
		// visibility, because nothing is removed or re-added.
		updateTaxMatchModel(getSelectedKey(taxMatchSpiEl));
		updateImgDescModel(getSelectedKey(imgDescSpiEl));
		updateMcModel(getSelectedKey(mcGeneratorSpiEl));
		updateEssayGenModel(getSelectedKey(essayGenSpiEl));
		updateEssayGradingModel(getSelectedKey(essayGradingSpiEl));
	}

	private SingleSelection addModelDropdown(String elName, String labelKey, FormItemContainer container) {
		SingleSelection dropdown = uifactory.addDropdownSingleselect(elName, labelKey, container,
				new String[] { "-" }, new String[] { "-" }, null);
		dropdown.setMandatory(true);
		return dropdown;
	}

	private TextElement addModelTextElement(String elName, String labelKey, FormItemContainer container) {
		return uifactory.addTextElement(elName, labelKey, 256, "", container);
	}

	private IntegerElement addMaxOutputTokensElement(String elName, String labelKey, int currentValue,
			FormItemContainer container) {
		IntegerElement maxTokensEl = uifactory.addIntegerElement(elName, labelKey, currentValue, container);
		maxTokensEl.setHelpTextKey(labelKey + ".help", null);
		maxTokensEl.setMandatory(true);
		return maxTokensEl;
	}

	private IntegerElement addTimeoutElement(String elName, String labelKey, int currentValue,
			FormItemContainer container) {
		IntegerElement timeoutEl = uifactory.addIntegerElement(elName, labelKey, currentValue, container);
		timeoutEl.setHelpTextKey(labelKey + ".help", null);
		timeoutEl.setMandatory(true);
		return timeoutEl;
	}

	private FormLink addTestLink(String elName, FormItemContainer container) {
		FormLink testLink = uifactory.addFormLink(elName, elName, "ai.feature.test", null, container, Link.BUTTON_SMALL);
		testLink.setGhost(true);
		testLink.setIconLeftCSS("o_icon o_icon_ai");
		return testLink;
	}

	private SingleSelection buildSpiDropdown(String elName, String currentSpiId, FormItemContainer container,
			List<AiSPI> spis) {
		String[] keys;
		String[] values;
		if (spis.isEmpty()) {
			keys = new String[] { "-" };
			values = new String[] { translate("ai.feature.spi.none") };
		} else {
			SelectionValues sv = new SelectionValues();
			for (AiSPI spi : spis) {
				sv.add(SelectionValues.entry(spi.getId(), spi.getName()));
			}
			sv.sort(SelectionValues.VALUE_ASC);
			keys = sv.keys();
			values = sv.values();
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
		// Default to the first provider when nothing is configured yet, so the
		// dependent model dropdown can be populated on initial form load.
		if (!spis.isEmpty() && !spiEl.isOneSelected()) {
			spiEl.select(keys[0], true);
		}
		return spiEl;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == mcGeneratorSpiEl) {
			updateMcModel(getSelectedKey(mcGeneratorSpiEl));
		} else if (source == imgDescSpiEl) {
			updateImgDescModel(getSelectedKey(imgDescSpiEl));
		} else if (source == essayGenSpiEl) {
			updateEssayGenModel(getSelectedKey(essayGenSpiEl));
		} else if (source == essayGradingSpiEl) {
			updateEssayGradingModel(getSelectedKey(essayGradingSpiEl));
		} else if (source == taxMatchSpiEl) {
			updateTaxMatchModel(getSelectedKey(taxMatchSpiEl));
		} else if (source == mcEnabledEl) {
			updateMcVisibility();
		} else if (source == imgDescEnabledEl) {
			updateImgDescVisibility();
		} else if (source == essayGenEnabledEl) {
			updateEssayGenVisibility();
		} else if (source == essayGradingEnabledEl) {
			updateEssayGradingVisibility();
		} else if (source == taxMatchEnabledEl) {
			updateTaxMatchVisibility();
		} else if (source == mcTestLink) {
			doTestMcGenerator(ureq);
		} else if (source == imgDescTestLink) {
			doTestImgDescGenerator(ureq);
		} else if (source == essayGenTestLink) {
			doTestEssayGeneration(ureq);
		} else if (source == essayGradingTestLink) {
			doTestEssayGrading(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == testCtrl) {
			// test controller handles its own modal cleanup
		}
		super.event(ureq, source, event);
	}

	// ------ Model update: repopulate content, then set visibility ------
	//
	// Each method points the active model element (xxxModelEl) at the dropdown
	// when the provider publishes a model list, or at the free-text element
	// otherwise. No item is removed or re-added.

	private void updateTaxMatchModel(String spiId) {
		String currentModel = taxonomyMatchingModule != null ? taxonomyMatchingModule.getModel() : null;
		taxMatchModelEl = applyModel(spiId, getEmbeddingModelsForSpi(spiId), currentModel,
				taxMatchModelDropdownEl, taxMatchModelTextEl);
		updateTaxMatchVisibility();
	}

	private void updateImgDescModel(String spiId) {
		imgDescModelEl = applyModel(spiId, getModelsForSpi(spiId), aiModule.getImgDescModel(),
				imgDescModelDropdownEl, imgDescModelTextEl);
		updateImgDescVisibility();
	}

	private void updateMcModel(String spiId) {
		mcGeneratorModelEl = applyModel(spiId, getModelsForSpi(spiId), aiModule.getMCGeneratorModel(),
				mcModelDropdownEl, mcModelTextEl);
		updateMcVisibility();
	}

	private void updateEssayGenModel(String spiId) {
		essayGenModelEl = applyModel(spiId, getModelsForSpi(spiId), aiModule.getEssayGenerationModel(),
				essayGenModelDropdownEl, essayGenModelTextEl);
		updateEssayGenVisibility();
	}

	private void updateEssayGradingModel(String spiId) {
		essayGradingModelEl = applyModel(spiId, getModelsForSpi(spiId), aiModule.getEssayGradingModel(),
				essayGradingModelDropdownEl, essayGradingModelTextEl);
		updateEssayGradingVisibility();
	}

	/**
	 * Set the model selectors to the desired state for the given provider and
	 * return the element that is now active. When the provider publishes a model
	 * list the dropdown is repopulated and selected; otherwise the free-text
	 * element holds the configured model. Visibility is handled by the caller's
	 * updateXxxVisibility().
	 */
	private FormItem applyModel(String spiId, List<String> models, String currentModel,
			SingleSelection dropdownEl, TextElement textEl) {
		if (!models.isEmpty()) {
			SelectionValues sv = new SelectionValues();
			for (String model : models) {
				sv.add(SelectionValues.entry(model, model));
			}
			sv.sort(SelectionValues.VALUE_ASC);
			String[] keys = sv.keys();
			dropdownEl.setKeysAndValues(keys, sv.values(), null);

			boolean selected = false;
			if (StringHelper.containsNonWhitespace(currentModel)) {
				for (String key : keys) {
					if (key.equals(currentModel)) {
						dropdownEl.select(key, true);
						selected = true;
						break;
					}
				}
			}
			if (!selected && keys.length > 0) {
				dropdownEl.select(keys[0], true);
			}
			return dropdownEl;
		}

		boolean hasSpi = StringHelper.containsNonWhitespace(spiId) && !"-".equals(spiId);
		textEl.setValue(hasSpi ? currentModel : "");
		textEl.setMandatory(hasSpi);
		textEl.setEnabled(hasSpi);
		return textEl;
	}

	// ------ Visibility: gate the section on its enable toggle ------

	private void updateTaxMatchVisibility() {
		boolean on = taxMatchEnabledEl.isOn();
		taxMatchSpiEl.setVisible(on);
		setModelVisibility(on, taxMatchModelEl, taxMatchModelDropdownEl, taxMatchModelTextEl);
	}

	private void updateImgDescVisibility() {
		boolean on = imgDescEnabledEl.isOn();
		imgDescSpiEl.setVisible(on);
		setModelVisibility(on, imgDescModelEl, imgDescModelDropdownEl, imgDescModelTextEl);
		imgDescMaxOutputTokensEl.setVisible(on);
		imgDescTimeoutSecondsEl.setVisible(on);
		imgDescTestLink.setVisible(on && hasSpiSelected(imgDescSpiEl) && hasModelValue(imgDescModelEl));
	}

	private void updateMcVisibility() {
		boolean on = mcEnabledEl.isOn();
		mcGeneratorSpiEl.setVisible(on);
		setModelVisibility(on, mcGeneratorModelEl, mcModelDropdownEl, mcModelTextEl);
		mcMaxOutputTokensEl.setVisible(on);
		mcTimeoutSecondsEl.setVisible(on);
		mcTestLink.setVisible(on && hasSpiSelected(mcGeneratorSpiEl) && hasModelValue(mcGeneratorModelEl));
	}

	private void updateEssayGenVisibility() {
		boolean on = essayGenEnabledEl.isOn();
		essayGenSpiEl.setVisible(on);
		setModelVisibility(on, essayGenModelEl, essayGenModelDropdownEl, essayGenModelTextEl);
		essayGenMaxOutputTokensEl.setVisible(on);
		essayGenTimeoutSecondsEl.setVisible(on);
		essayGenTestLink.setVisible(on && hasSpiSelected(essayGenSpiEl) && hasModelValue(essayGenModelEl));
	}

	private void updateEssayGradingVisibility() {
		boolean on = essayGradingEnabledEl.isOn();
		essayGradingSpiEl.setVisible(on);
		setModelVisibility(on, essayGradingModelEl, essayGradingModelDropdownEl, essayGradingModelTextEl);
		essayGradingMaxOutputTokensEl.setVisible(on);
		essayGradingTimeoutSecondsEl.setVisible(on);
		essayGradingTestLink.setVisible(on && hasSpiSelected(essayGradingSpiEl) && hasModelValue(essayGradingModelEl));
	}

	/** Show only the active model element, and only while the section is enabled. */
	private void setModelVisibility(boolean on, FormItem activeModelEl,
			SingleSelection dropdownEl, TextElement textEl) {
		dropdownEl.setVisible(on && activeModelEl == dropdownEl);
		textEl.setVisible(on && activeModelEl == textEl);
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

	private void doTestEssayGeneration(UserRequest ureq) {
		String spiId = getSelectedSpiId(essayGenSpiEl);
		String model = extractModelValue(essayGenModelEl);
		if (!StringHelper.containsNonWhitespace(spiId) || !StringHelper.containsNonWhitespace(model)) {
			showError("ai.feature.test.not.configured");
			return;
		}
		ensureTestCtrl(ureq);
		testCtrl.testEssayGeneration(ureq, spiId, model);
	}

	private void doTestEssayGrading(UserRequest ureq) {
		String spiId = getSelectedSpiId(essayGradingSpiEl);
		String model = extractModelValue(essayGradingModelEl);
		if (!StringHelper.containsNonWhitespace(spiId) || !StringHelper.containsNonWhitespace(model)) {
			showError("ai.feature.test.not.configured");
			return;
		}
		ensureTestCtrl(ureq);
		testCtrl.testEssayGrading(ureq, spiId, model);
	}

	private void ensureTestCtrl(UserRequest ureq) {
		if (testCtrl == null) {
			testCtrl = new AiFeaturesTestController(ureq, getWindowControl(),
					mcQuestionService, imageDescriptionService,
					aiEssayGenerationService, aiEssayGradingService);
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
		if (essayGenEnabledEl.isOn() && hasSpiSelected(essayGenSpiEl) && essayGenModelEl != null) {
			allOk &= validateModelElement(essayGenModelEl);
		}
		if (essayGradingEnabledEl.isOn() && hasSpiSelected(essayGradingSpiEl) && essayGradingModelEl != null) {
			allOk &= validateModelElement(essayGradingModelEl);
		}
		if (taxMatchEnabledEl.isOn() && hasSpiSelected(taxMatchSpiEl) && taxMatchModelEl != null) {
			allOk &= validateModelElement(taxMatchModelEl);
		}

		if (mcEnabledEl.isOn()) {
			allOk &= validateMaxOutputTokens(mcMaxOutputTokensEl);
		}
		if (imgDescEnabledEl.isOn()) {
			allOk &= validateMaxOutputTokens(imgDescMaxOutputTokensEl);
		}
		if (essayGenEnabledEl.isOn()) {
			allOk &= validateMaxOutputTokens(essayGenMaxOutputTokensEl);
		}
		if (essayGradingEnabledEl.isOn()) {
			allOk &= validateMaxOutputTokens(essayGradingMaxOutputTokensEl);
		}

		if (mcEnabledEl.isOn()) {
			allOk &= validateTimeoutSeconds(mcTimeoutSecondsEl);
		}
		if (imgDescEnabledEl.isOn()) {
			allOk &= validateTimeoutSeconds(imgDescTimeoutSecondsEl);
		}
		if (essayGenEnabledEl.isOn()) {
			allOk &= validateTimeoutSeconds(essayGenTimeoutSecondsEl);
		}
		if (essayGradingEnabledEl.isOn()) {
			allOk &= validateTimeoutSeconds(essayGradingTimeoutSecondsEl);
		}

		return allOk;
	}

	private boolean validateMaxOutputTokens(IntegerElement el) {
		el.clearError();
		if (!StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("ai.feature.max-output-tokens.error.range");
			return false;
		}
		if (el.getIntValue() < 1) {
			el.setErrorKey("ai.feature.max-output-tokens.error.range");
			return false;
		}
		if (el.getIntValue() < MIN_MAX_OUTPUT_TOKENS) {
			el.setErrorKey("ai.feature.max-output-tokens.error.min");
			return false;
		}
		return true;
	}

	private boolean validateTimeoutSeconds(IntegerElement el) {
		el.clearError();
		if (!StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("ai.feature.timeout-seconds.error.range");
			return false;
		}
		if (el.getIntValue() < 1) {
			el.setErrorKey("ai.feature.timeout-seconds.error.range");
			return false;
		}
		if (el.getIntValue() < MIN_TIMEOUT_SECONDS) {
			el.setErrorKey("ai.feature.timeout-seconds.error.min");
			return false;
		}
		return true;
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
			aiModule.setMCQuestionGeneratorEnabled(true);
			aiModule.setMCQuestionGeneratorConfig(mcSpiId, mcModel);
			aiModule.setMCGeneratorMaxOutputTokens(mcMaxOutputTokensEl.getIntValue());
			aiModule.setMCGeneratorTimeoutSeconds(mcTimeoutSecondsEl.getIntValue());
			logAudit("MC question generator configured: provider=" + mcSpiId + ", model=" + mcModel);
		} else {
			aiModule.setMCQuestionGeneratorEnabled(false);
			logAudit("MC question generator disabled");
		}

		if (imgDescEnabledEl.isOn()) {
			String imgDescSpiId = getSelectedSpiId(imgDescSpiEl);
			String imgDescModel = extractModelValue(imgDescModelEl);
			aiModule.setImageDescriptionGeneratorEnabled(true);
			aiModule.setImageDescriptionGeneratorConfig(imgDescSpiId, imgDescModel);
			aiModule.setImgDescMaxOutputTokens(imgDescMaxOutputTokensEl.getIntValue());
			aiModule.setImgDescTimeoutSeconds(imgDescTimeoutSecondsEl.getIntValue());
			logAudit("Image description generator configured: provider=" + imgDescSpiId + ", model=" + imgDescModel);
		} else {
			aiModule.setImageDescriptionGeneratorEnabled(false);
			logAudit("Image description generator disabled");
		}

		if (essayGenEnabledEl.isOn()) {
			String essayGenSpiId = getSelectedSpiId(essayGenSpiEl);
			String essayGenModel = extractModelValue(essayGenModelEl);
			aiModule.setEssayGenerationEnabled(true);
			aiModule.setEssayGenerationConfig(essayGenSpiId, essayGenModel);
			aiModule.setEssayGenerationMaxOutputTokens(essayGenMaxOutputTokensEl.getIntValue());
			aiModule.setEssayGenerationTimeoutSeconds(essayGenTimeoutSecondsEl.getIntValue());
			logAudit("Essay question generator configured: provider=" + essayGenSpiId + ", model=" + essayGenModel);
		} else {
			aiModule.setEssayGenerationEnabled(false);
			logAudit("Essay question generator disabled");
		}

		if (essayGradingEnabledEl.isOn()) {
			String essayGradingSpiId = getSelectedSpiId(essayGradingSpiEl);
			String essayGradingModel = extractModelValue(essayGradingModelEl);
			aiModule.setEssayGradingEnabled(true);
			aiModule.setEssayGradingConfig(essayGradingSpiId, essayGradingModel);
			aiModule.setEssayGradingMaxOutputTokens(essayGradingMaxOutputTokensEl.getIntValue());
			aiModule.setEssayGradingTimeoutSeconds(essayGradingTimeoutSecondsEl.getIntValue());
			logAudit("Essay grading configured: provider=" + essayGradingSpiId + ", model=" + essayGradingModel);
		} else {
			aiModule.setEssayGradingEnabled(false);
			logAudit("Essay grading disabled");
		}

		if (taxonomyMatchingModule != null) {
			if (taxMatchEnabledEl.isOn()) {
				String taxSpiId = getSelectedSpiId(taxMatchSpiEl);
				String taxModel = extractModelValue(taxMatchModelEl);
				boolean modelChanged = !Objects.equals(taxSpiId, taxonomyMatchingModule.getSpiId())
						|| !Objects.equals(taxModel, taxonomyMatchingModule.getModel());
				taxonomyMatchingModule.setEnabled(true);
				taxonomyMatchingModule.setSpiId(taxSpiId);
				taxonomyMatchingModule.setModel(taxModel);
				logAudit("Taxonomy matching configured: provider=" + taxSpiId + ", model=" + taxModel);
				if (modelChanged && taxonomyMatchingService != null) {
					taxonomyMatchingService.scheduleFullReindex();
				}
			} else {
				taxonomyMatchingModule.setEnabled(false);
				logAudit("Taxonomy matching disabled");
			}
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

	private SingleSelection buildEmbeddingSpiDropdown(String elName, String currentSpiId, FormItemContainer container) {
		List<AiSPI> spis = aiModule.getEnabledProviders().stream()
				.filter(spi -> spi instanceof AiEmbeddingSPI es && es.isEmbeddingEnabled())
				.toList();
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
		if (!spis.isEmpty() && !spiEl.isOneSelected()) {
			spiEl.select(keys[0], true);
		}
		return spiEl;
	}

	private List<String> getEmbeddingModelsForSpi(String spiId) {
		if (!StringHelper.containsNonWhitespace(spiId) || "-".equals(spiId)) {
			return List.of();
		}
		for (AiSPI spi : aiModule.getAiProviders()) {
			if (spi.getId().equals(spiId) && spi instanceof AiEmbeddingSPI embSpi) {
				return embSpi.getAvailableEmbeddingModels();
			}
		}
		return List.of();
	}
}
