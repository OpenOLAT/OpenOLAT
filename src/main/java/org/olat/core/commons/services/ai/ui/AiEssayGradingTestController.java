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

import org.olat.core.commons.services.ai.AiEssayGradingService;
import org.olat.core.commons.services.ai.essay.AiBloomLevel;
import org.olat.core.commons.services.ai.essay.AiGradingTier;
import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.essay.GradingSuggestion;
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
 * Admin-side smoke test for the essay grading feature on a specific SPI + model
 * combination. Shows the built-in reference-excerpt, model answer and rubric
 * plus a textarea for a pre-filled short-tier student answer, then renders the
 * grading signals below the form on submit. Bypasses the full
 * {@link org.olat.core.commons.services.ai.essay.EssayFormativeFeedbackService}
 * pipeline — no pre-filters, no integrity hash check, no persisted feedback —
 * this is a raw service ping.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class AiEssayGradingTestController extends FormBasicController {

	static final String ESSAY_GRADING_REFERENCE = "Zellen sind die kleinsten lebensfähigen Einheiten aller "
			+ "Lebewesen. Eukaryotische Zellen besitzen einen Zellkern, der die DNA enthält. "
			+ "Prokaryoten wie Bakterien haben keinen echten Zellkern — ihre DNA liegt frei im Zytoplasma.";

	static final String ESSAY_GRADING_MODEL_ANSWER = "Eukaryotische Zellen besitzen einen Zellkern mit DNA, "
			+ "während Prokaryoten keinen Kern haben und ihre DNA frei im Zytoplasma liegt.";

	static final String ESSAY_GRADING_KEY_POINTS_JSON = "["
			+ "{\"id\":\"kp1\",\"text\":\"Eukaryoten haben einen Zellkern\",\"weight\":0.5,\"required\":true},"
			+ "{\"id\":\"kp2\",\"text\":\"Prokaryoten haben keinen Zellkern, DNA liegt im Zytoplasma\","
			+ "\"weight\":0.5,\"required\":true}]";

	static final String ESSAY_GRADING_RUBRIC_JSON = "["
			+ "{\"id\":\"c1\",\"name\":\"Inhalt\",\"weight\":0.7,\"scope\":\"CONTENT\"},"
			+ "{\"id\":\"c2\",\"name\":\"Sprache\",\"weight\":0.3,\"scope\":\"LANGUAGE\"}]";

	// Pre-filled Kurz-tier (~40 words) German student answer.
	static final String ESSAY_GRADING_STUDENT_ANSWER = "Eukaryotische Zellen haben einen Zellkern, in dem die "
			+ "DNA gespeichert wird. Prokaryotische Zellen wie Bakterien haben keinen Kern. Trotzdem können "
			+ "beide Typen sich vermehren und reagieren auf ihre Umgebung.";

	private final String spiId;
	private final String modelName;
	private final AiEssayGradingService aiEssayGradingService;
	private final EssayAiGrading externalGrading;

	private TextAreaElement answerEl;
	private FormSubmit submitButton;

	private GradingSuggestion resultSuggestion;
	private String errorMessage;

	/**
	 * Admin entry point: builds an inline German biology sample grading and
	 * pre-fills a Kurz-tier student answer for a one-click smoke test.
	 */
	public AiEssayGradingTestController(UserRequest ureq, WindowControl wControl,
			String spiId, String modelName, AiEssayGradingService aiEssayGradingService) {
		this(ureq, wControl, spiId, modelName, aiEssayGradingService, null);
	}

	/**
	 * Editor entry point: caller supplies a pre-built {@link EssayAiGrading}
	 * (typically the in-progress form values from the QTI essay editor) and
	 * the student-answer field starts blank so the author can type a test
	 * answer. Pass {@code null} for {@code grading} to keep the inline
	 * admin-default behaviour.
	 */
	public AiEssayGradingTestController(UserRequest ureq, WindowControl wControl,
			String spiId, String modelName, AiEssayGradingService aiEssayGradingService,
			EssayAiGrading grading) {
		super(ureq, wControl, "ai_essay_grading_test");
		this.spiId = spiId;
		this.modelName = modelName;
		this.aiEssayGradingService = aiEssayGradingService;
		this.externalGrading = grading;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Read-only reference block rendered by the template using context keys.
		String reference = externalGrading != null && StringHelper.containsNonWhitespace(externalGrading.getReferenceExcerpt())
				? externalGrading.getReferenceExcerpt() : ESSAY_GRADING_REFERENCE;
		String model = externalGrading != null && StringHelper.containsNonWhitespace(externalGrading.getModelAnswer())
				? externalGrading.getModelAnswer() : ESSAY_GRADING_MODEL_ANSWER;
		flc.contextPut("referenceExcerpt", reference);
		flc.contextPut("modelAnswer", model);
		flc.contextPut("referenceLabel", translate("ai.feature.essay-grading.test.reference.label"));

		// Editor-launched mode starts blank so the author types a fresh test answer.
		String defaultAnswer = externalGrading == null ? ESSAY_GRADING_STUDENT_ANSWER : "";
		answerEl = uifactory.addTextAreaElement("essayGrading.answer",
				"ai.feature.essay-grading.test.answer.label", 10_000, 5, 80, true, false,
				defaultAnswer, formLayout);
		answerEl.setMandatory(true);

		submitButton = uifactory.addFormSubmitButton("essayGrading.submit",
				"ai.feature.essay-grading.test.button", formLayout);
		submitButton.setIconLeftCSS("o_icon o_icon_ai");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		resultSuggestion = null;
		errorMessage = null;

		String answer = answerEl.getValue();
		if (!StringHelper.containsNonWhitespace(answer)) {
			errorMessage = translate("ai.feature.essay-grading.test.error");
			return;
		}

		if (!StringHelper.containsNonWhitespace(spiId) || !StringHelper.containsNonWhitespace(modelName)) {
			errorMessage = translate("ai.feature.test.not.configured");
			return;
		}

		try {
			EssayAiGrading grading = externalGrading != null ? externalGrading : buildTestGrading();
			AiGradingTier tier = AiGradingTier.classify(answer);
			Locale locale = resolveLocale(grading);
			AiEssayGradingService.GradingRun run = aiEssayGradingService.gradeWithLog(null, grading,
					answer, locale, tier, spiId, modelName);
			resultSuggestion = run == null ? null : run.suggestion();
			if (resultSuggestion == null) {
				errorMessage = translate("ai.feature.essay-grading.test.error");
			}
		} catch (Exception e) {
			errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
		}

		flc.contextPut("suggestionView", toSuggestionView(resultSuggestion));
		flc.contextPut("error", errorMessage);
	}

	/**
	 * Flatten the nested suggestion into a map tree with stringified enums and
	 * lists already shaped for Velocity — avoids calling enum methods inside
	 * Velocity #foreach, which has been observed to render raw previously.
	 * Returns null if input is null so the template can branch cleanly.
	 */
	private static Map<String, Object> toSuggestionView(GradingSuggestion s) {
		if (s == null) return null;
		Map<String, Object> v = new LinkedHashMap<>();
		v.put("offTopicFlag", s.offTopicFlag() == null ? "" : s.offTopicFlag().name());
		v.put("confidence", s.confidence() == null ? "" : s.confidence().name());
		v.put("overallAssessment", nullToEmpty(s.overallAssessment()));
		v.put("estimatedScorePercent", Integer.toString(s.estimatedScorePercent()));
		v.put("feedbackToCoach", nullToEmpty(s.feedbackToCoach()));

		GradingSuggestion.ContentSignals c = s.contentSignals();
		Map<String, Object> cv = new LinkedHashMap<>();
		if (c == null) {
			cv.put("coherenceNote", "");
			cv.put("argumentNote", "");
			cv.put("relevanceNote", "");
			cv.put("keyPointsHit", List.of());
			cv.put("keyPointsMissed", List.of());
		} else {
			cv.put("coherenceNote", nullToEmpty(c.coherenceNote()));
			cv.put("argumentNote", nullToEmpty(c.argumentNote()));
			cv.put("relevanceNote", nullToEmpty(c.relevanceNote()));
			List<Map<String, String>> hits = new ArrayList<>();
			if (c.keyPointsHit() != null) {
				for (GradingSuggestion.KeyPointHit h : c.keyPointsHit()) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("id", nullToEmpty(h.id()));
					m.put("evidence", nullToEmpty(h.evidence()));
					hits.add(m);
				}
			}
			cv.put("keyPointsHit", hits);
			List<Map<String, String>> missed = new ArrayList<>();
			if (c.keyPointsMissed() != null) {
				for (GradingSuggestion.KeyPointMissed m : c.keyPointsMissed()) {
					Map<String, String> row = new LinkedHashMap<>();
					row.put("id", nullToEmpty(m.id()));
					row.put("reason", nullToEmpty(m.reason()));
					missed.add(row);
				}
			}
			cv.put("keyPointsMissed", missed);
		}
		v.put("contentSignals", cv);

		GradingSuggestion.LanguageSignals l = s.languageSignals();
		Map<String, Object> lv = new LinkedHashMap<>();
		if (l == null) {
			lv.put("grammarIssues", List.of());
			lv.put("spellingIssues", List.of());
		} else {
			List<Map<String, String>> grammar = new ArrayList<>();
			if (l.grammarIssues() != null) {
				for (GradingSuggestion.GrammarIssue g : l.grammarIssues()) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("span", nullToEmpty(g.span()));
					m.put("note", nullToEmpty(g.note()));
					m.put("severity", g.severity() == null ? "" : g.severity().name());
					grammar.add(m);
				}
			}
			lv.put("grammarIssues", grammar);
			List<Map<String, String>> spelling = new ArrayList<>();
			if (l.spellingIssues() != null) {
				for (GradingSuggestion.SpellingIssue sp : l.spellingIssues()) {
					Map<String, String> m = new LinkedHashMap<>();
					m.put("word", nullToEmpty(sp.word()));
					m.put("suggestion", nullToEmpty(sp.suggestion()));
					spelling.add(m);
				}
			}
			lv.put("spellingIssues", spelling);
		}
		v.put("languageSignals", lv);

		GradingSuggestion.StudentFeedback fb = s.feedbackToStudent();
		Map<String, String> fv = new LinkedHashMap<>();
		if (fb == null) {
			fv.put("whatWentWell", "");
			fv.put("whatIsMissing", "");
			fv.put("nextStep", "");
		} else {
			fv.put("whatWentWell", nullToEmpty(fb.whatWentWell()));
			fv.put("whatIsMissing", nullToEmpty(fb.whatIsMissing()));
			fv.put("nextStep", nullToEmpty(fb.nextStep()));
		}
		v.put("feedbackToStudent", fv);
		return v;
	}

	private static String nullToEmpty(String s) {
		return s == null ? "" : s;
	}

	private static Locale resolveLocale(EssayAiGrading g) {
		if (g != null && StringHelper.containsNonWhitespace(g.getLanguage())) {
			Locale parsed = Locale.forLanguageTag(g.getLanguage().trim());
			if (StringHelper.containsNonWhitespace(parsed.getLanguage())) {
				return parsed;
			}
		}
		return Locale.GERMAN;
	}

	/**
	 * Build a minimal but valid {@link EssayAiGrading} in-memory. Not persisted —
	 * this is a raw service ping, so we skip the integrity-hash round-trip that
	 * the production pipeline performs.
	 */
	private EssayAiGrading buildTestGrading() {
		EssayAiGrading g = new EssayAiGrading();
		g.setAssessmentItemIdentifier("ai-admin-test-" + System.currentTimeMillis());
		g.setLanguage("de");
		g.setReferenceExcerpt(ESSAY_GRADING_REFERENCE);
		g.setModelAnswer(ESSAY_GRADING_MODEL_ANSWER);
		g.setKeyPointsJson(ESSAY_GRADING_KEY_POINTS_JSON);
		g.setRubricCriteriaJson(ESSAY_GRADING_RUBRIC_JSON);
		g.setBloomLevel(AiBloomLevel.UNDERSTAND.name());
		g.setLearningObjective("Unterschied zwischen eukaryotischen und prokaryotischen Zellen erklären.");
		g.setTokenEstimate(80);
		g.setContentHash("admin-test");
		return g;
	}
}
