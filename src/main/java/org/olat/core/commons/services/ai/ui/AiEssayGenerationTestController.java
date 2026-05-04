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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.commons.services.ai.AiEssayGenerationService;
import org.olat.core.commons.services.ai.essay.AiBloomLevel;
import org.olat.core.commons.services.ai.essay.AiContentChunk;
import org.olat.core.commons.services.ai.essay.EssayItemDraft;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 *
 * Admin-side smoke test for the essay question generator feature on a specific
 * SPI + model combination. Shows a small markdown textarea pre-filled with a
 * generic paragraph and renders the first generated draft (learning objective,
 * reference excerpt, model answer, first key point) below the form on submit.
 * Bypasses the full {@code EssayGenerationService} pipeline (no chunker, no
 * validator, no persistence) — this is a raw service ping.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class AiEssayGenerationTestController extends FormBasicController {

	static final String ESSAY_GEN_TEST_INPUT = "Cells are the basic structural and functional units of life. "
			+ "Eukaryotic cells contain a nucleus that stores the genetic material (DNA), surrounded by a "
			+ "membrane. They also contain organelles such as mitochondria, which produce energy through "
			+ "cellular respiration, and ribosomes, which synthesize proteins. Prokaryotic cells, like "
			+ "bacteria, lack a true nucleus — their DNA floats freely in the cytoplasm. Despite these "
			+ "differences, both cell types perform metabolism, respond to their environment, and can "
			+ "reproduce.";

	private final String spiId;
	private final String modelName;
	private final AiEssayGenerationService aiEssayGenerationService;

	private TextAreaElement promptEl;
	private FormSubmit submitButton;

	private List<EssayItemDraft> resultDrafts;
	private String errorMessage;

	public AiEssayGenerationTestController(UserRequest ureq, WindowControl wControl,
			String spiId, String modelName, AiEssayGenerationService aiEssayGenerationService) {
		super(ureq, wControl, "ai_essay_generation_test");
		this.spiId = spiId;
		this.modelName = modelName;
		this.aiEssayGenerationService = aiEssayGenerationService;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		promptEl = uifactory.addTextAreaElement("essayGen.prompt", "ai.feature.essay-generation.test.prompt.label",
				10_000, 6, 80, true, false, ESSAY_GEN_TEST_INPUT, formLayout);
		promptEl.setMandatory(true);
		submitButton = uifactory.addFormSubmitButton("essayGen.submit",
				"ai.feature.essay-generation.test.button", formLayout);
		submitButton.setIconLeftCSS("o_icon o_icon_ai");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		resultDrafts = null;
		errorMessage = null;
		String prompt = promptEl.getValue();
		if (!StringHelper.containsNonWhitespace(prompt)) {
			errorMessage = translate("ai.feature.essay-generation.test.error");
			return;
		}

		if (!StringHelper.containsNonWhitespace(spiId) || !StringHelper.containsNonWhitespace(modelName)) {
			errorMessage = translate("ai.feature.test.not.configured");
			return;
		}

		try {
			// Two-question service ping. We deliberately bypass EssayGenerationService
			// (chunker, validator, persistence) — this is a raw service ping so the
			// admin can inspect the full model output for multiple drafts.
			AiContentChunk chunk = new AiContentChunk("test-chunk-1", prompt, List.of("Test"), 0, false);
			List<EssayItemDraft> drafts = aiEssayGenerationService.generateEssayQuestions(
					null,
					List.of(chunk),
					List.of("Understand the key concepts in the given paragraph."),
					List.of(AiBloomLevel.UNDERSTAND),
					2,
					Locale.ENGLISH,
					spiId,
					modelName);
			if (drafts != null && !drafts.isEmpty()) {
				resultDrafts = drafts;
			} else {
				errorMessage = translate("ai.feature.essay-generation.test.error");
			}
		} catch (Exception e) {
			errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
		}

		flc.contextPut("draftViews", resultDrafts == null ? List.of() : toDraftViews(resultDrafts));
		flc.contextPut("error", errorMessage);
	}

	/**
	 * Flatten drafts into view maps with stringified enums, tagged key-point /
	 * rubric lists and comma-joined misconceptions — everything the template
	 * needs without Velocity having to call record accessors or enum methods
	 * inside a foreach (that was observed to render raw previously).
	 */
	private static List<Map<String, Object>> toDraftViews(List<EssayItemDraft> drafts) {
		List<Map<String, Object>> views = new ArrayList<>(drafts.size());
		for (int i = 0; i < drafts.size(); i++) {
			EssayItemDraft d = drafts.get(i);
			Map<String, Object> v = new LinkedHashMap<>();
			v.put("index", Integer.valueOf(i + 1));
			v.put("stimulus", nullToEmpty(d.stimulus()));
			v.put("modelAnswer", nullToEmpty(d.modelAnswer()));
			v.put("learningObjective", nullToEmpty(d.learningObjective()));
			v.put("referenceExcerpt", nullToEmpty(d.referenceExcerpt()));
			v.put("bloomLevel", d.bloomLevel() == null ? "" : d.bloomLevel().name());
			v.put("languageTag", nullToEmpty(d.languageTag()));
			v.put("tokenEstimate", Integer.toString(d.tokenEstimate()));
			v.put("difficulty", Integer.toString(d.difficulty()));
			v.put("gradingHints", nullToEmpty(d.gradingHints()));
			v.put("commonMisconceptions", d.commonMisconceptions() == null
					? List.<String>of() : d.commonMisconceptions());

			List<Map<String, String>> kps = new ArrayList<>();
			if (d.keyPoints() != null) {
				for (EssayItemDraft.KeyPoint kp : d.keyPoints()) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("id", nullToEmpty(kp.id()));
					m.put("text", nullToEmpty(kp.text()));
					m.put("weight", Double.toString(kp.weight()));
					m.put("required", Boolean.toString(kp.isRequiredEffective()));
					kps.add(m);
				}
			}
			v.put("keyPoints", kps);

			List<Map<String, String>> rcs = new ArrayList<>();
			if (d.rubricCriteria() != null) {
				for (EssayItemDraft.RubricCriterion rc : d.rubricCriteria()) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("id", nullToEmpty(rc.id()));
					m.put("name", nullToEmpty(rc.name()));
					m.put("descriptor", nullToEmpty(rc.descriptor()));
					m.put("weight", Double.toString(rc.weight()));
					m.put("scope", rc.scope() == null ? "" : rc.scope().name());
					rcs.add(m);
				}
			}
			v.put("rubricCriteria", rcs);

			views.add(v);
		}
		return views;
	}

	private static String nullToEmpty(String s) {
		return s == null ? "" : s;
	}
}
