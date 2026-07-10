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
package org.olat.ims.qti21.pool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.essay.EssayItemDraft;
import org.olat.core.commons.services.ai.model.MCQuestionData;
import org.olat.core.commons.services.ai.model.MCQuestionData.McAnswerOption;
import org.olat.core.commons.services.ai.ui.AiAdminController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.ModalFeedbackBuilder;
import org.olat.ims.qti21.model.xml.ModalFeedbackCondition;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * Shared helper that builds QTI 2.1 assessment items from AI-generated
 * payloads. Two consumers exist:
 * <ul>
 *   <li>{@code EssayGenerationQuizPartSinkImpl} — page-editor QuizPart flow,
 *       persists items on the QuizPart's storage path.</li>
 *   <li>{@code EssayGenerationPoolSinkImpl} — question-pool flow, persists
 *       items via {@link QTI21QPoolServiceProvider#importExcelItem}.</li>
 * </ul>
 * Centralising the QTI-builder construction guarantees that both sinks
 * produce structurally identical items (same scoring, same shuffle,
 * same solution feedback shape, same tool metadata stamp).
 * <p>
 * Each factory method returns a built {@link uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem}
 * wrapped together with its builder. The caller decides where and how to
 * persist the item.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class AiQtiItemFactory {

	/** Tool name prefix mirroring {@code NewAiItemController}'s legacy stamp. */
	public static final String TOOL_PREFIX = "OpenOlat.AI.QTI21.Generator.";

	/** Maximum length of a derived title before it is truncated with an ellipsis. */
	private static final int MAX_TITLE_LEN = 120;
	/** Maximum length when falling back to body / stimulus excerpt. */
	private static final int MAX_TITLE_FALLBACK_LEN = 80;

	@Autowired
	private QTI21Service qtiService;

	/**
	 * Build a multiple-choice QTI item for {@code data} ready to be persisted.
	 * The returned item already has shuffled choices, all-correct-answers
	 * scoring, max score 1.0, a "show solution" modal feedback listing the
	 * correct answers, and a per-option modal feedback for every WRONG choice
	 * whose {@link McAnswerOption#getFeedback()} is non-blank. Correct choices
	 * intentionally have no per-option feedback.
	 * <p>
	 * The QTI {@code toolName} stays at the default {@link org.olat.ims.qti21.QTI21Constants#TOOLNAME}
	 * value set by {@code AssessmentItemFactory} so the standard OpenOlat
	 * editor recognises the item. AI provenance ({@code spiId} / {@code model})
	 * is recorded in a companion file ({@code ai-source.json}) by the caller —
	 * see {@link org.olat.core.commons.services.ai.essay.AiSourceCompanionFileStore}.
	 *
	 * @param data           generated MC payload (must carry question + at least one correct answer)
	 * @param locale         locale for any UI strings (fallback solution title)
	 * @param solutionTitle  translated "Solution" label, used by the solution modal feedback header
	 * @param wrongTitle     translated "Wrong" label, used as the title of per-option feedback for wrong choices
	 * @param spiId          reserved for callers that want the value back from the factory; not stamped onto the QTI item
	 * @param model          reserved for callers that want the value back from the factory; not stamped onto the QTI item
	 * @return a {@link McItem} carrying the built builder + item; {@code null}
	 *         if the payload is incomplete and no item can be produced
	 */
	public McItem buildMcItem(MCQuestionData data, Locale locale, String solutionTitle,
			String wrongTitle, String spiId, String model) {
		if (data == null || !StringHelper.containsNonWhitespace(data.getQuestion())
				|| data.getCorrectAnswers() == null || data.getCorrectAnswers().isEmpty()) {
			return null;
		}
		String title = deriveMcTitle(data, locale);
		MultipleChoiceAssessmentItemBuilder mcBuilder = new MultipleChoiceAssessmentItemBuilder(
				StringHelper.xssScan(title), "New answer", qtiService.qtiSerializer());
		mcBuilder.setQuestion(StringHelper.xssScan(data.getQuestion()));
		mcBuilder.clearSimpleChoices();
		mcBuilder.setShuffle(true);
		mcBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		mcBuilder.setMaxScore(1d);
		mcBuilder.clearMapping();

		// Per-option modal feedbacks — collected while building choices and
		// registered via additionalFeedbacks so build() emits them correctly.
		List<ModalFeedbackBuilder> choiceFeedbacks = new ArrayList<>();

		// correct answers — no per-option feedback by design (telling the learner
		// "this is correct" after they got it right is not pedagogically useful).
		for (McAnswerOption option : data.getCorrectAnswers()) {
			if (option == null || !StringHelper.containsNonWhitespace(option.getText())) continue;
			String safe = StringHelper.xssScan(option.getText());
			ChoiceInteraction interaction = mcBuilder.getChoiceInteraction();
			SimpleChoice newChoice = AssessmentItemFactory.createSimpleChoice(interaction, safe,
					mcBuilder.getQuestionType().getPrefix());
			mcBuilder.addSimpleChoice(newChoice);
			mcBuilder.addCorrectAnswer(newChoice.getIdentifier());
		}
		// wrong answers
		if (data.getWrongAnswers() != null) {
			for (McAnswerOption option : data.getWrongAnswers()) {
				if (option == null || !StringHelper.containsNonWhitespace(option.getText())) continue;
				String safe = StringHelper.xssScan(option.getText());
				ChoiceInteraction interaction = mcBuilder.getChoiceInteraction();
				SimpleChoice newChoice = AssessmentItemFactory.createSimpleChoice(interaction, safe,
						QTI21QuestionType.mc.getPrefix());
				mcBuilder.addSimpleChoice(newChoice);
				if (StringHelper.containsNonWhitespace(option.getFeedback())) {
					choiceFeedbacks.add(buildChoiceFeedback(mcBuilder, newChoice.getIdentifier(),
							wrongTitle, option.getFeedback()));
				}
			}
		}

		// Register per-choice feedbacks so build() emits them.
		if (!choiceFeedbacks.isEmpty()) {
			mcBuilder.setAdditionalFeedbackBuilders(choiceFeedbacks);
		}

		// solution feedback — list the correct answers
		ModalFeedbackBuilder solution = mcBuilder.createCorrectSolutionFeedback();
		solution.setTitle(solutionTitle);
		solution.setText(mcSolutionHtml(data.getCorrectAnswers()));

		mcBuilder.build();
		AssessmentItem item = mcBuilder.getAssessmentItem();
		// Intentionally NOT stamping toolName / toolVersion: AI provenance is
		// carried by an on-disk companion file ({@code ai-source.json}) so the
		// QTI XML stays compatible with the standard OpenOlat editor detection.
		return new McItem(mcBuilder, item, title);
	}

	/**
	 * Build an essay QTI item for {@code draft} ready to be persisted. The
	 * returned item carries the draft stimulus as the body and (when present)
	 * the model answer as a "show solution" modal feedback.
	 *
	 * @param draft         essay draft (must not be {@code null})
	 * @param locale        locale for fallback title generation
	 * @param solutionTitle translated "Solution" label for the modal feedback
	 * @param grading       optional AI grading metadata; when present the
	 *                      generator SPI / model is stamped onto the item
	 * @return an {@link EssayItem} carrying the built builder + item; never
	 *         {@code null} for a non-null draft
	 */
	public EssayItem buildEssayItem(EssayItemDraft draft, Locale locale, String solutionTitle,
			EssayAiGrading grading) {
		String title = deriveEssayTitle(draft, locale);
		EssayAssessmentItemBuilder builder = new EssayAssessmentItemBuilder(title, qtiService.qtiSerializer());
		String stimulusHtml = stimulusToHtml(draft == null ? null : draft.stimulus());
		String instructionsHtml = buildEssayInstructionsHtml(draft, locale);
		builder.setQuestion(stimulusHtml + instructionsHtml);

		if (draft != null && StringHelper.containsNonWhitespace(draft.modelAnswer())) {
			ModalFeedbackBuilder solution = builder.createCorrectSolutionFeedback();
			solution.setTitle(solutionTitle);
			solution.setText(stimulusToHtml(draft.modelAnswer()));
		}

		builder.build();
		AssessmentItem item = builder.getAssessmentItem();
		// Intentionally NOT stamping toolName / toolVersion: AI provenance for
		// essays is carried by {@code ai-grading.json} (and optionally
		// {@code ai-source.json}) so the QTI XML stays compatible with the
		// standard OpenOlat editor detection.
		return new EssayItem(builder, item, title);
	}

	private String deriveEssayTitle(EssayItemDraft draft, Locale locale) {
		if (draft != null && StringHelper.containsNonWhitespace(draft.questionTitle())) {
			return clamp(draft.questionTitle().trim(), MAX_TITLE_LEN);
		}
		if (draft != null && StringHelper.containsNonWhitespace(draft.learningObjective())) {
			return clamp(draft.learningObjective().trim(), MAX_TITLE_LEN);
		}
		if (draft != null && StringHelper.containsNonWhitespace(draft.stimulus())) {
			return clamp(draft.stimulus().trim(), MAX_TITLE_FALLBACK_LEN);
		}
		// Locale param reserved for future i18n fallback titles.
		return locale != null && "de".equals(locale.getLanguage()) ? "Essay-Frage" : "Essay question";
	}

	private String deriveMcTitle(MCQuestionData data, Locale locale) {
		if (data != null && StringHelper.containsNonWhitespace(data.getTitle())) {
			return clamp(data.getTitle().trim(), MAX_TITLE_LEN);
		}
		if (data != null && StringHelper.containsNonWhitespace(data.getQuestion())) {
			return clamp(data.getQuestion().trim(), MAX_TITLE_FALLBACK_LEN);
		}
		return locale != null && "de".equals(locale.getLanguage()) ? "MC-Frage" : "Multiple choice question";
	}

	private static String clamp(String s, int max) {
		if (s == null) return null;
		if (s.length() <= max) return s;
		return s.substring(0, max - 3) + "...";
	}

	/**
	 * Build the per-question "Notes for your answer" block appended to the
	 * essay stimulus. Tells the learner the expected answer length, how many
	 * key concepts they must address and which dimensions are graded.
	 * <p>
	 * Returns an empty string when the draft has no usable hints (no token
	 * estimate, no key points, no rubric criteria).
	 */
	private String buildEssayInstructionsHtml(EssayItemDraft draft, Locale locale) {
		if (draft == null) {
			return "";
		}
		Translator translator = Util.createPackageTranslator(AiAdminController.class,
				locale == null ? Locale.ENGLISH : locale);
		StringBuilder list = new StringBuilder();

		// Length hint — derived from tokenEstimate. Tokens × 0.75 ≈ words; round
		// to a friendly bucket and label.
		int tokens = draft.tokenEstimate();
		if (tokens > 0) {
			String descriptorKey;
			if (tokens < 80) descriptorKey = "ai.essay.instructions.length.short";
			else if (tokens < 160) descriptorKey = "ai.essay.instructions.length.medium";
			else if (tokens < 280) descriptorKey = "ai.essay.instructions.length.short_paragraph";
			else if (tokens < 400) descriptorKey = "ai.essay.instructions.length.paragraph";
			else descriptorKey = "ai.essay.instructions.length.long";
			int approxWords = Math.max(10, Math.round(tokens * 0.75f / 10) * 10);
			String descriptor = translator.translate(descriptorKey);
			list.append("<li>")
					.append(translator.translate("ai.essay.instructions.length",
							new String[] { String.valueOf(approxWords), descriptor }))
					.append("</li>");
		}

		// Key concepts hint
		int kpCount = draft.keyPoints() == null ? 0 : draft.keyPoints().size();
		if (kpCount == 1) {
			list.append("<li>")
					.append(translator.translate("ai.essay.instructions.keypoints.singular"))
					.append("</li>");
		} else if (kpCount > 1) {
			list.append("<li>")
					.append(translator.translate("ai.essay.instructions.keypoints",
							new String[] { String.valueOf(kpCount) }))
					.append("</li>");
		}

		// Grading dimensions — derived from rubric scopes
		if (draft.rubricCriteria() != null && !draft.rubricCriteria().isEmpty()) {
			boolean hasContent = false;
			boolean hasLanguage = false;
			for (EssayItemDraft.RubricCriterion rc : draft.rubricCriteria()) {
				if (rc == null || rc.scope() == null) continue;
				if (rc.scope() == EssayItemDraft.RubricScope.CONTENT) hasContent = true;
				else if (rc.scope() == EssayItemDraft.RubricScope.LANGUAGE) hasLanguage = true;
			}
			List<String> labels = new ArrayList<>(2);
			if (hasContent) labels.add(translator.translate("ai.essay.instructions.grading.content"));
			if (hasLanguage) labels.add(translator.translate("ai.essay.instructions.grading.language"));
			if (!labels.isEmpty()) {
				list.append("<li>")
						.append(translator.translate("ai.essay.instructions.grading",
								new String[] { String.join(", ", labels) }))
						.append("</li>");
			}
		}

		if (list.length() == 0) {
			return "";
		}
		return "<div class=\"o_ai_essay_instructions\">"
				+ "<p><strong>" + translator.translate("ai.essay.instructions.title") + "</strong></p>"
				+ "<ul>" + list + "</ul>"
				+ "</div>";
	}

	/**
	 * Turn a plain-prose stimulus / answer into a QTI-friendly HTML block.
	 * Newlines become paragraph breaks. No rich formatting is inferred.
	 */
	static String stimulusToHtml(String stimulus) {
		if (!StringHelper.containsNonWhitespace(stimulus)) {
			return "<p></p>";
		}
		String[] parts = stimulus.trim().split("\\r?\\n\\s*\\r?\\n");
		StringBuilder sb = new StringBuilder();
		for (String p : parts) {
			if (p.isBlank()) continue;
			sb.append("<p>").append(escapeXml(p.trim().replace("\n", " "))).append("</p>");
		}
		if (sb.length() == 0) {
			sb.append("<p></p>");
		}
		return sb.toString();
	}

	/**
	 * Build a {@link ModalFeedbackBuilder} of type {@code additional} that fires
	 * when the learner selects the choice identified by {@code choiceIdentifier}.
	 * The builder is later registered via
	 * {@link AssessmentItemBuilder#setAdditionalFeedbackBuilders(List)} so that
	 * {@code build()} emits both the {@code <modalFeedback>} element and the
	 * matching {@code <responseCondition>} (Member-based, MULTIPLE cardinality).
	 */
	private ModalFeedbackBuilder buildChoiceFeedback(MultipleChoiceAssessmentItemBuilder mcBuilder,
			Identifier choiceIdentifier, String feedbackTitle, String feedbackText) {
		ModalFeedbackBuilder fb = new ModalFeedbackBuilder(mcBuilder.getAssessmentItem(),
				ModalFeedbackBuilder.ModalFeedbackType.additional);
		fb.setTitle(feedbackTitle);
		fb.setText("<p>" + escapeXml(feedbackText.trim()) + "</p>");
		ModalFeedbackCondition condition = new ModalFeedbackCondition(
				ModalFeedbackCondition.Variable.response,
				ModalFeedbackCondition.Operator.equals,
				choiceIdentifier.toString());
		fb.setFeedbackConditions(Collections.singletonList(condition));
		return fb;
	}

	private static String mcSolutionHtml(List<McAnswerOption> correctAnswers) {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (McAnswerOption option : correctAnswers) {
			if (option == null || !StringHelper.containsNonWhitespace(option.getText())) continue;
			sb.append("<li>").append(escapeXml(option.getText().trim())).append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}

	private static String escapeXml(String s) {
		return s
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	/** Wrapper holding the built MC builder, the resulting QTI item, and the derived title. */
	public record McItem(MultipleChoiceAssessmentItemBuilder builder, AssessmentItem item, String title) { }

	/** Wrapper holding the built essay builder, the resulting QTI item, and the derived title. */
	public record EssayItem(EssayAssessmentItemBuilder builder, AssessmentItem item, String title) { }
}
