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
package org.olat.modules.ceditor.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.ui.AiAdminController;
import org.olat.core.commons.services.ai.essay.AiSourceCompanion;
import org.olat.core.commons.services.ai.essay.AiSourceCompanionFileStore;
import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.essay.EssayAiGradingFileStore;
import org.olat.core.commons.services.ai.essay.EssayFormativeFeedbackService;
import org.olat.core.commons.services.ai.essay.EssayGenerationQuizPartSink;
import org.olat.core.commons.services.ai.essay.EssayItemDraft;
import org.olat.core.commons.services.ai.model.MCQuestionData;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.pool.AiQtiItemFactory;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.QuizEditorController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

/**
 *
 * ceditor-side implementation of {@link EssayGenerationQuizPartSink}. On
 * job completion, converts each accepted {@link EssayItemDraft} into a QTI
 * {@code ExtendedTextInteraction} (essay) assessment item and each
 * {@link MCQuestionData} into a QTI multiple-choice item, and attaches
 * them to the target {@link QuizPart} as {@link QuizQuestion}s in an
 * interleaved MC-essay-MC-essay order (MC first). On failure it annotates
 * the placeholder settings so the renderer can show an error.
 * <p>
 * Keeps the core {@code ai.essay} package free of ceditor / QTI
 * dependencies.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayGenerationQuizPartSinkImpl implements EssayGenerationQuizPartSink {

	private static final Logger log = Tracing.createLoggerFor(EssayGenerationQuizPartSinkImpl.class);

	/** Settings title marker the renderer reads to show a "generating" placeholder. */
	public static final String GENERATING_TITLE_MARKER = "[AI:generating]";
	/** Settings description marker for the renderer to show an error placeholder. */
	public static final String FAILED_TITLE_MARKER = "[AI:failed]";

	@Autowired
	private DB dbInstance;
	@Autowired
	private PageService pageService;
	@Autowired
	private ContentEditorQti contentEditorQti;
	@Autowired
	private ContentEditorFileStorage contentEditorFileStorage;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private EssayAiGradingFileStore essayAiGradingFileStore;
	@Autowired
	private AiSourceCompanionFileStore aiSourceCompanionFileStore;
	@Autowired
	private AiQtiItemFactory aiQtiItemFactory;

	@Override
	public void attachDraftsAsEssayItems(Long pageKey, Long quizPartKey,
			List<EssayItemDraft> essayDrafts, Map<EssayItemDraft, EssayAiGrading> draftToGrading,
			List<MCQuestionData> mcQuestions, Locale locale) {
		if (pageKey == null || quizPartKey == null) return;
		try {
			QuizPart quizPart = loadQuizPart(quizPartKey);
			if (quizPart == null) {
				log.info("Markdown-import QuizPart {} vanished before drafts could be attached — skipping", quizPartKey);
				return;
			}

			// Ensure we have a storage path (placeholder may have been created before finish).
			if (!StringHelper.containsNonWhitespace(quizPart.getStoragePath())) {
				String storagePath = contentEditorQti.generateStoragePath(quizPart);
				if (storagePath == null) {
					log.warn("No storage path for QuizPart {} — cannot attach drafts", quizPartKey);
					markGenerationFailed(pageKey, quizPartKey, "no storage path");
					return;
				}
				quizPart.setStoragePath(storagePath);
			}

			QuizSettings settings = quizPart.getSettings();
			List<QuizQuestion> questions = new ArrayList<>(settings.getQuestions());

			// Build both lists as QTI items first, then interleave.
			List<QuizQuestion> mcItems = new ArrayList<>();
			if (mcQuestions != null) {
				for (MCQuestionData mc : mcQuestions) {
					QuizQuestion q = createMCQuestion(quizPart, mc, locale);
					if (q != null) {
						mcItems.add(q);
					}
				}
			}
			List<QuizQuestion> essayItems = new ArrayList<>();
			if (essayDrafts != null) {
				for (EssayItemDraft draft : essayDrafts) {
					EssayAiGrading grading = draftToGrading == null ? null : draftToGrading.get(draft);
					QuizQuestion q = createEssayQuestion(quizPart, draft, locale, grading);
					if (q != null) {
						essayItems.add(q);
					}
				}
			}

			// Interleave MC-essay-MC-essay. If one list is shorter the remaining
			// items are appended at the end so no data is lost (graceful
			// degradation when one leg produced fewer items).
			List<QuizQuestion> interleaved = interleave(mcItems, essayItems);
			questions.addAll(interleaved);
			int added = interleaved.size();

			settings.setQuestions(questions);
			String baseTitle = settings.getTitle();
			if (baseTitle == null || baseTitle.startsWith(GENERATING_TITLE_MARKER) || baseTitle.startsWith(FAILED_TITLE_MARKER)) {
				settings.setTitle(defaultTitle(locale));
				settings.setDescription(defaultDescription(locale));
			}
			if (added == 0) {
				// No items survived — leave a gentle note but drop the generating marker.
				String noteTitle = settings.getTitle();
				if (noteTitle == null || noteTitle.isBlank()) {
					settings.setTitle(defaultTitle(locale));
				}
			}
			quizPart.setSettings(settings);
			pageService.updatePart(quizPart);
			dbInstance.commit();
		} catch (Exception e) {
			log.error("Attaching AI drafts to QuizPart {} failed", quizPartKey, e);
		}
	}

	/**
	 * Produce the interleave MC, essay, MC, essay, ... leftovers of the
	 * longer list are appended at the end in their natural order.
	 */
	static List<QuizQuestion> interleave(List<QuizQuestion> mcItems, List<QuizQuestion> essayItems) {
		List<QuizQuestion> out = new ArrayList<>(
				(mcItems == null ? 0 : mcItems.size()) + (essayItems == null ? 0 : essayItems.size()));
		int mcSize = mcItems == null ? 0 : mcItems.size();
		int esSize = essayItems == null ? 0 : essayItems.size();
		int common = Math.min(mcSize, esSize);
		for (int i = 0; i < common; i++) {
			out.add(mcItems.get(i));
			out.add(essayItems.get(i));
		}
		if (mcSize > common) {
			out.addAll(mcItems.subList(common, mcSize));
		}
		if (esSize > common) {
			out.addAll(essayItems.subList(common, esSize));
		}
		return out;
	}

	@Override
	public void markGenerationFailed(Long pageKey, Long quizPartKey, String reason) {
		if (quizPartKey == null) return;
		try {
			QuizPart quizPart = loadQuizPart(quizPartKey);
			if (quizPart == null) return;
			QuizSettings settings = quizPart.getSettings();
			settings.setTitle(failedTitle(settings.getTitle()));
			quizPart.setSettings(settings);
			pageService.updatePart(quizPart);
			dbInstance.commit();
			log.warn("AI question generation failed for QuizPart {} (page {}): {}",
					quizPartKey, pageKey, reason);
		} catch (Exception e) {
			log.error("Marking QuizPart {} as failed raised an error", quizPartKey, e);
		}
	}

	/**
	 * Compose the persisted title for a failed generation: the failed marker
	 * plus the original base title. The failure reason is deliberately NOT
	 * part of the title — it is operator information (raw provider errors,
	 * server config hints) and the title is rendered to learners. The run and
	 * editor views translate the failed state from the marker alone.
	 */
	static String failedTitle(String currentTitle) {
		String baseTitle = currentTitle;
		if (baseTitle != null && baseTitle.startsWith(GENERATING_TITLE_MARKER)) {
			baseTitle = baseTitle.substring(GENERATING_TITLE_MARKER.length()).trim();
		} else if (baseTitle != null && baseTitle.startsWith(FAILED_TITLE_MARKER)) {
			baseTitle = baseTitle.substring(FAILED_TITLE_MARKER.length()).trim();
		}
		if (baseTitle == null || baseTitle.isBlank()) {
			return FAILED_TITLE_MARKER;
		}
		return FAILED_TITLE_MARKER + " " + baseTitle;
	}

	private QuizPart loadQuizPart(Long quizPartKey) {
		return dbInstance.getCurrentEntityManager().find(QuizPart.class, quizPartKey);
	}

	/**
	 * Create a persisted QTI essay item on disk for the QuizPart's storage
	 * path and return a {@link QuizQuestion} pointing at it. Sets the
	 * draft's {@code modelAnswer} as solution feedback so the runtime's
	 * "show solution" button renders the expected answer.
	 * <p>
	 * When {@code grading} is non-null the AI grading metadata is written
	 * as {@code ai-grading.json} into the question's directory next to the
	 * QTI item XML so the runtime can pick it up at learner-submit time
	 * for AI correction. The {@code contentHash} is recomputed before the
	 * file is written so the integrity check at grading time succeeds.
	 * <p>
	 * The QTI item itself is constructed by the shared
	 * {@link AiQtiItemFactory} so both this sink and the question-pool sink
	 * produce structurally identical items.
	 */
	private QuizQuestion createEssayQuestion(QuizPart quizPart, EssayItemDraft draft, Locale locale,
			EssayAiGrading grading) {
		try {
			AiModule aiModule = CoreSpringFactory.getImpl(AiModule.class);
			String spiId = aiModule == null ? null : aiModule.getEssayGenerationSpiId();
			String model = aiModule == null ? null : aiModule.getEssayGenerationModel();

			File questionsDir = contentEditorFileStorage.getFile(quizPart.getStoragePath());
			String questionId = IdentifierGenerator.newAsString(QTI21QuestionType.essay.getPrefix());
			File questionDir = new File(questionsDir, questionId);
			if (!questionDir.exists() && !questionDir.mkdirs()) {
				log.warn("Could not create question dir {}", questionDir);
				return null;
			}
			File questionFile = new File(questionDir, questionId + ".xml");

			AiQtiItemFactory.EssayItem built = aiQtiItemFactory.buildEssayItem(
					draft, locale, solutionTitle(locale), grading);
			AssessmentItem item = built.item();
			// Override the auto-derived title with the package-default fallback when
			// the draft yielded no usable title — keeps existing behaviour where the
			// ceditor sink uses translator-backed defaults.
			String title = built.title();
			if (!StringHelper.containsNonWhitespace(title) || title.startsWith("Essay question")
					|| title.startsWith("Essay-Frage")) {
				title = defaultQuestionTitle(locale);
				item.setTitle(title);
			}
			qtiService.persistAssessmentObject(questionFile, item);
			
			// Record AI provenance in the companion file (the QTI XML keeps the
			// default OpenOlat toolName so the standard editor recognises it).
			if (StringHelper.containsNonWhitespace(spiId)) {
				aiSourceCompanionFileStore.save(questionDir, new AiSourceCompanion(spiId, model, null, true));
			}

			// Persist the per-question AI grading metadata next to the QTI XML.
			// The directory name (= questionId) becomes the
			// assessmentItemIdentifier on load.
			if (grading != null) {
				grading.setAssessmentItemIdentifier(questionId);
				grading.setContentHash(EssayFormativeFeedbackService.computeContentHash(grading));
				essayAiGradingFileStore.save(questionDir, grading);
			}

			QuizQuestion quizQuestion = new QuizQuestion();
			quizQuestion.setId(questionId);
			quizQuestion.setType(QTI21QuestionType.essay.name());
			quizQuestion.setTitle(title);
			quizQuestion.setXmlFilePath(contentEditorFileStorage.getRelativePath(questionFile));
			return quizQuestion;
		} catch (Exception e) {
			log.warn("Could not persist AI essay draft as QTI item: {}", e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Create a persisted QTI multiple-choice item on disk for the QuizPart's
	 * storage path and return a {@link QuizQuestion} pointing at it. Solution
	 * feedback lists the correct answers so the runtime "show solution"
	 * button works.
	 * <p>
	 * The QTI item itself is constructed by the shared
	 * {@link AiQtiItemFactory} (same code path as the question-pool sink).
	 */
	private QuizQuestion createMCQuestion(QuizPart quizPart, MCQuestionData data, Locale locale) {
		try {
			AiModule aiModule = CoreSpringFactory.getImpl(AiModule.class);
			String spiId = aiModule == null ? null : aiModule.getMCGeneratorSpiId();
			String model = aiModule == null ? null : aiModule.getMCGeneratorModel();

			AiQtiItemFactory.McItem built = aiQtiItemFactory.buildMcItem(
					data, locale, solutionTitle(locale), wrongTitle(locale), spiId, model);
			if (built == null) {
				return null;
			}

			File questionsDir = contentEditorFileStorage.getFile(quizPart.getStoragePath());
			String questionId = IdentifierGenerator.newAsString(QTI21QuestionType.mc.getPrefix());
			File questionDir = new File(questionsDir, questionId);
			if (!questionDir.exists() && !questionDir.mkdirs()) {
				log.warn("Could not create question dir {}", questionDir);
				return null;
			}
			File questionFile = new File(questionDir, questionId + ".xml");

			String title = built.title();
			if (!StringHelper.containsNonWhitespace(title) || title.startsWith("Multiple choice question")
					|| title.startsWith("MC-Frage")) {
				title = defaultMcTitle(locale);
				built.item().setTitle(title);
			}
			qtiService.persistAssessmentObject(questionFile, built.item());

			// Record AI provenance in the companion file (the QTI XML keeps the
			// default OpenOlat toolName so the standard editor recognises it).
			if (StringHelper.containsNonWhitespace(spiId)) {
				aiSourceCompanionFileStore.save(questionDir, new AiSourceCompanion(spiId, model, null, true));
			}

			QuizQuestion quizQuestion = new QuizQuestion();
			quizQuestion.setId(questionId);
			quizQuestion.setType(QTI21QuestionType.mc.name());
			quizQuestion.setTitle(title);
			quizQuestion.setXmlFilePath(contentEditorFileStorage.getRelativePath(questionFile));
			return quizQuestion;
		} catch (Exception e) {
			log.warn("Could not persist AI MC question as QTI item: {}", e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Build a translator backed by the ceditor UI i18n bundle. The class is in
	 * the manager package, so we deliberately reuse the UI-side bundle (where
	 * the related {@code import.ai.generate.*} keys already live) instead of
	 * scattering manager-side properties files.
	 */
	private Translator translator(Locale locale) {
		return Util.createPackageTranslator(QuizEditorController.class, locale);
	}

	private String defaultTitle(Locale locale) {
		return translator(locale).translate("ai.quiz.default.title");
	}

	private String defaultDescription(Locale locale) {
		return translator(locale).translate("ai.quiz.default.description");
	}

	private String defaultQuestionTitle(Locale locale) {
		return translator(locale).translate("ai.quiz.default.question.title");
	}

	private String defaultMcTitle(Locale locale) {
		return translator(locale).translate("ai.quiz.default.mc.title");
	}

	private String solutionTitle(Locale locale) {
		return Util.createPackageTranslator(AiAdminController.class, locale)
				.translate("ai.solution.title");
	}

	private String wrongTitle(Locale locale) {
		return Util.createPackageTranslator(AiAdminController.class, locale)
				.translate("mc.feedback.wrong.title");
	}
}
