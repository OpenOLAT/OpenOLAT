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
package org.olat.modules.ceditor.ui;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.essay.AiOverloadedException;
import org.olat.core.commons.services.ai.essay.AiRateLimitExceededException;
import org.olat.core.commons.services.ai.essay.EssayAiGrading;
import org.olat.core.commons.services.ai.essay.EssayAiGradingFileStore;
import org.olat.core.commons.services.ai.essay.EssayAiCorrectionService;
import org.olat.core.commons.services.ai.essay.FormativeFeedback;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesAssessmentItemListener;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.audit.DefaultAssessmentSessionAuditLogger;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent;
import org.olat.ims.qti21.ui.ResponseInput;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.manager.ContentEditorQti;
import org.olat.modules.ceditor.manager.EssayGenerationQuizPartSinkImpl;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * Initial date: 2024-03-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuizRunController extends BasicController implements PageRunElement, OutcomesAssessmentItemListener {

	private final VelocityContainer mainVC;
	private AssessmentEntry assessmentEntry;
	private QuizPart quizPart;
	private final boolean editable;
	private Link startButton;
	private Link retryButton;
	private State state = State.intro;
	private AssessmentItemDisplayController assessmentItemDisplayController;
	private final AssessmentSessionAuditLogger candidateAuditLogger = new DefaultAssessmentSessionAuditLogger();
	private final RepositoryEntry entry;
	private final String subIdent;
	private int questionIndex = 0;
	private ProgressBar progressBar;
	private Map<String, Boolean> questionPassedState = new HashMap<>();
	private QuizQuestion currentQuizQuestion;

	/** Async AI correction — per-question state. Reset on each new question. */
	private Long aiCorrectionKey;
	/** Maximum poll attempts before the UI gives up (≈ 35 s at 2-s cadence). */
	private static final int MAX_POLL_ATTEMPTS = 18;
	/** Generation poll cadence (matches QuizEditorController for parity). */
	private static final int AI_GEN_POLL_DELAY_MS = 3000;
	private static final int MAX_AI_GEN_POLL_ATTEMPTS = 90;
	private Link aiGenerationPollLink;
	private int aiGenerationPollAttempts = 0;
	private boolean aiGenerationPollTimedOut = false;
	/** Threshold (inclusive) above which an AI-graded essay counts as
	 *  passed in the final quiz summary. Aligned with the assessment-bucket
	 *  boundaries (50 = "mittelmässig" and above). */
	private static final int AI_CORRECTION_PASS_THRESHOLD_PERCENT = 50;
	private int aiCorrectionPollAttempts = 0;
	/** Hidden link clicked by a JS setTimeout to re-dispatch a poll. */
	private Link aiCorrectionPollLink;
	/** Question identifier the in-flight AI correction belongs to. We snapshot
	 *  it at trigger time because by the time the polling tick runs the
	 *  current quiz question may have advanced. */
	private String aiCorrectionForQuestionId;
	/** Flattened view map rendered by quiz_run.html when feedback is ready. */
	private Map<String, Object> aiCorrectionFeedbackView;
	private String aiCorrectionError;
	private boolean aiCorrectionVisible;

	@Autowired
	private ContentEditorQti contentEditorQti;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private MediaDAO mediaDAO;
	@Autowired
	private EssayAiGradingFileStore essayAiGradingFileStore;
	@Autowired
	private EssayAiCorrectionService essayAiCorrectionService;

	public QuizRunController(UserRequest ureq, WindowControl wControl, QuizPart quizPart, boolean editable,
							 RepositoryEntry entry, String subIdent) {
		super(ureq, wControl);
		this.quizPart = quizPart;
		questionIndex = 0;
		this.editable = editable;
		this.entry = entry;
		this.subIdent = StringHelper.containsNonWhitespace(subIdent) ? subIdent + "_" + quizPart.getId() : "";

		if (this.entry != null && StringHelper.containsNonWhitespace(this.subIdent)) {
			assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null, entry,
					this.subIdent, Boolean.FALSE, entry, false);
		}

		mainVC = createVelocityContainer("quiz_run");
		mainVC.setElementCssClass("o_quiz_run_element_css_class");
		setBlockLayoutClass(quizPart.getSettings());
		putInitialPanel(mainVC);

		AssessmentRunStatus runStatus = assessmentEntry != null ? assessmentEntry.getCurrentRunStatus() : null;
		if (runStatus != null) {
			switch (runStatus) {
				case running -> doStart(ureq);
				case done -> {
					initQuestionPassedStates();
					doShowResult(ureq);
				}
				default -> updateUI(ureq);
			}
		} else {
			updateUI(ureq);
		}
	}

	private void initQuestionPassedStates() {
		questionPassedState = new HashMap<>();
		// Build a lookup of question id -> whether it uses manual grading so
		// essays (etc.) are not falsely marked as failed on resume (they always
		// score 0 since there is no automatic correctness check).
		Map<String, Boolean> manualIds = new HashMap<>();
		List<QuizQuestion> configured = quizPart.getSettings().getQuestions();
		if (configured != null) {
			for (QuizQuestion q : configured) {
				if (q != null && StringHelper.containsNonWhitespace(q.getId())) {
					manualIds.put(q.getId(), isManuallyGradedType(q));
				}
			}
		}
		AssessmentTestSession lastSession = qtiService.getResumableAssessmentItemsSession(getIdentity(),
				null, entry, subIdent, entry, false);
		if (lastSession != null) {
			List<AssessmentItemSession> itemSessions = qtiService.getAssessmentItemSessions(lastSession);
			for (AssessmentItemSession itemSession : itemSessions) {
				int score = itemSession.getScore() != null ? itemSession.getScore().intValue() : 0;
				String itemId = itemSession.getAssessmentItemIdentifier();
				if (StringHelper.containsNonWhitespace(itemId)) {
					boolean manual = Boolean.TRUE.equals(manualIds.get(itemId));
					questionPassedState.put(itemId, manual || score > 0);
				}
			}
		}
	}

	private void setBlockLayoutClass(QuizSettings quizSettings) {
		mainVC.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(quizSettings, false));
	}

	private void updateUI(UserRequest ureq) {
		mainVC.contextPut("state", state.name());

		switch (state) {
			case intro -> updateIntroUI(ureq);
			case quiz -> updateQuizUI();
			case result -> updateResultUI(ureq);
		}
	}

	private void updateIntroUI(UserRequest ureq) {
		updateImage(ureq);

		mainVC.contextPut("quizOutcomeClass", "");
		String rawTitle = quizPart.getSettings().getTitle();
		String aiState = "";
		String displayTitle = rawTitle;
		if (rawTitle != null && rawTitle.startsWith(EssayGenerationQuizPartSinkImpl.GENERATING_TITLE_MARKER)) {
			aiState = "generating";
			displayTitle = rawTitle.substring(EssayGenerationQuizPartSinkImpl.GENERATING_TITLE_MARKER.length()).trim();
		} else if (rawTitle != null && rawTitle.startsWith(EssayGenerationQuizPartSinkImpl.FAILED_TITLE_MARKER)) {
			aiState = "failed";
			displayTitle = rawTitle.substring(EssayGenerationQuizPartSinkImpl.FAILED_TITLE_MARKER.length()).trim();
		}
		if ("generating".equals(aiState) && aiGenerationPollTimedOut) {
			// Client-side poll cap reached: render the stalled hint without the
			// poll link so the JS interval self-clears. The job may still finish
			// server-side; reloading the page resumes polling.
			aiState = "stalled";
		}
		boolean aiBusy = "generating".equals(aiState) || "stalled".equals(aiState);
		// A failed generation leaves the quiz without questions — starting it
		// would be a silent no-op, so hide the button in that state.
		boolean failedEmpty = "failed".equals(aiState)
				&& quizPart.getSettings().getQuestions().isEmpty();
		mainVC.contextPut("aiState", aiState);
		mainVC.contextPut("title", displayTitle);
		mainVC.contextPut("description", substituteVariables(quizPart.getSettings().getDescription()));
		startButton = LinkFactory.createButton("quiz.start", mainVC, this);
		startButton.setIconLeftCSS("o_icon o_icon-fw o_icon_play");
		startButton.setPrimary(true);
		startButton.setEnabled(!editable && !aiBusy && !failedEmpty);
		startButton.setVisible(!aiBusy && !failedEmpty);
		startButton.setTitle("quiz.start");
		startButton.setAriaLabel("quiz.start");
		mainVC.put("quiz.start", startButton);

		// Generation-state polling: while the marker is still on the title,
		// re-fetch the QuizPart from DB on every tick and re-render so the
		// page editor preview reflects completion as soon as the async job
		// finishes. Idempotent — once the marker is gone the link is dropped
		// from the DOM and the JS interval auto-clears.
		if ("generating".equals(aiState)) {
			if (aiGenerationPollLink == null) {
				aiGenerationPollLink = LinkFactory.createCustomLink(
						"ai.generation.poll", "ai.generation.poll", "",
						Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS, mainVC, this);
				aiGenerationPollLink.setCustomDisplayText("");
				aiGenerationPollLink.setElementCssClass("o_ai_generation_poll");
			}
			mainVC.contextPut("aiGenerationPollDelayMs", Integer.valueOf(AI_GEN_POLL_DELAY_MS));
		} else {
			mainVC.contextPut("aiGenerationPollDelayMs", Integer.valueOf(0));
		}
	}

	private void updateImage(UserRequest ureq) {
		if (quizPart.getBackgroundImageMedia() != null && quizPart.getBackgroundImageMediaVersion() != null) {
			MediaVersion updatedMediaVersion = mediaDAO.loadVersionByKey(quizPart.getBackgroundImageMediaVersion().getKey());
			if (mainVC.getComponent("image") == null) {
				mainVC.put("image", ComponentsFactory.getImageComponent(ureq, updatedMediaVersion));
			} else if (mainVC.getComponent("image") instanceof ImageComponent existingImageComponent) {
				String existingPath = existingImageComponent.getMedia().getRelPath();
				if (!existingPath.endsWith(updatedMediaVersion.getRootFilename())) {
					mainVC.put("image", ComponentsFactory.getImageComponent(ureq, updatedMediaVersion));
				}
			}
		}
	}

	private void updateQuizUI() {
		mainVC.contextPut("questionNumber", questionIndex + 1);
		mainVC.contextPut("questionIndex", questionIndex);
		mainVC.contextPut("numberOfQuestions", getNumberOfQuestions());

		updateProgressBar();
		progressBar.setActual(questionIndex + 1);

		// AI correction state → template flags. Only one of the three blocks
		// is rendered at a time (overlay while pending, feedback on done,
		// error on timeout/fail). All three stay empty for manual essays.
		mainVC.contextPut("aiCorrectionVisible", aiCorrectionVisible);
		mainVC.contextPut("aiCorrectionFeedback", aiCorrectionFeedbackView);
		mainVC.contextPut("aiCorrectionError", aiCorrectionError);
		mainVC.contextPut("aiCorrectionWaitingLabel", translate("ai.essay.correction.waiting"));
		if (aiCorrectionVisible) {
			if (aiCorrectionPollLink == null) {
				aiCorrectionPollLink = LinkFactory.createCustomLink(
						"ai.correction.poll", "ai.correction.poll", "",
						Link.NONTRANSLATED | Link.LINK_CUSTOM_CSS, mainVC, this);
				aiCorrectionPollLink.setCustomDisplayText("");
				aiCorrectionPollLink.setElementCssClass("o_essay_ai_correction_poll");
			}
			mainVC.contextPut("aiCorrectionPollDelayMs", Integer.valueOf(2000));
		} else {
			mainVC.contextPut("aiCorrectionPollDelayMs", Integer.valueOf(0));
		}
	}

	private void updateProgressBar() {
		if (progressBar == null) {
			progressBar = new ProgressBar("progress", 100, 0.0f, getNumberOfQuestions(), null);
			progressBar.setWidthInPercent(true);
			progressBar.setLabelAlignment(ProgressBar.LabelAlignment.none);
			progressBar.setRenderSize(ProgressBar.RenderSize.small);
			mainVC.put("progressBar", progressBar);
		}
	}

	private void updateResultUI(UserRequest ureq) {
		updateImage(ureq);
		mainVC.contextPut("quizOutcomeClass", "");
		mainVC.contextPut("title", quizPart.getSettings().getTitle());
		retryButton = LinkFactory.createButton("quiz.retry", mainVC, this);
		retryButton.setIconLeftCSS("o_icon o_icon-fw o_icon_retry");
		retryButton.setPrimary(true);
		retryButton.setTitle("quiz.retry");
		retryButton.setAriaLabel("quiz.retry");
		updateProgressBar();
		progressBar.setActual(getNumberOfPassedQuestions());
		
		FigureWidget figures = WidgetFactory.createFigureWidget("quiz.figures", mainVC, translate("quiz.your.result"), "o_icon_score");
		figures.setValue(String.valueOf(getNumberOfPassedQuestions()));
		figures.setDesc(translate("quiz.figures.desc", String.valueOf(getNumberOfQuestions())));
		figures.setAdditionalComp(progressBar);
		figures.setAdditionalCssClass("o_widget_progress");
	}

	private String substituteVariables(String text) {
		if (!StringHelper.containsNonWhitespace(text)) {
			return text;
		}
		return text.replace("$numberOfQuestions", "" + getNumberOfQuestions());
	}

	int getNumberOfQuestions() {
		List<QuizQuestion> quizQuestions = quizPart.getSettings().getQuestions();
		if (quizQuestions == null) {
			return 0;
		}
		return quizQuestions.size();
	}

	int getNumberOfPassedQuestions() {
		int nbOfPassedQuestions = 0;
		for (String key : questionPassedState.keySet()) {
			if (questionPassedState.get(key)) {
				nbOfPassedQuestions++;
			}
		}
		return nbOfPassedQuestions;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.getElement() instanceof QuizPart updatedQuizPart) {
				quizPart = updatedQuizPart;
				setBlockLayoutClass(quizPart.getSettings());
				reset();
				updateUI(ureq);
			}
		} else if (assessmentItemDisplayController == source) {
			if (event instanceof QTIWorksAssessmentItemEvent qtiWorksAssessmentItemEvent) {
				if (QTIWorksAssessmentItemEvent.Event.next.name().equals(qtiWorksAssessmentItemEvent.getCommand())) {
					doNext(ureq);
				} else if (QTIWorksAssessmentItemEvent.Event.showSolution.name().equals(qtiWorksAssessmentItemEvent.getCommand())) {
					mainVC.contextPut("quizOutcomeClass", "o_correct");
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (startButton == source) {
			updateRunStatus(AssessmentRunStatus.running);
			doStart(ureq);
			updateUI(ureq);
		} else if (retryButton == source) {
			reset();
			doStart(ureq);
			updateUI(ureq);
		} else if (aiCorrectionPollLink != null && source == aiCorrectionPollLink) {
			doPollAiCorrection(ureq);
		} else if (aiGenerationPollLink != null && source == aiGenerationPollLink) {
			doPollAiGeneration(ureq);
		}
	}

	/**
	 * Generation poll tick — re-fetches the QuizPart from DB and re-renders
	 * if the {@code [AI:generating]} marker is still on its title. Stops at
	 * {@link #MAX_AI_GEN_POLL_ATTEMPTS} as a defensive client-side cap.
	 * Mirrors the pattern in {@link QuizEditorController}.
	 */
	private void doPollAiGeneration(UserRequest ureq) {
		aiGenerationPollAttempts++;
		QuizPart fresh = CoreSpringFactory.getImpl(DB.class)
				.getCurrentEntityManager().find(QuizPart.class, quizPart.getKey());
		if (fresh != null) {
			quizPart = fresh;
		}
		String rawTitle = quizPart.getSettings().getTitle();
		boolean stillGenerating = rawTitle != null
				&& rawTitle.startsWith(EssayGenerationQuizPartSinkImpl.GENERATING_TITLE_MARKER);
		if (stillGenerating && aiGenerationPollAttempts >= MAX_AI_GEN_POLL_ATTEMPTS) {
			// Client-side cap: stop polling but don't fake a failure — the job
			// may legitimately still be running on the batch queue. The stalled
			// state renders without the poll link, so the JS interval clears.
			aiGenerationPollTimedOut = true;
			updateUI(ureq);
			mainVC.setDirty(true);
		} else if (stillGenerating) {
			// Suppress repaint — interval keeps firing on its own schedule.
			mainVC.setDirty(false);
		} else {
			updateUI(ureq);
			mainVC.setDirty(true);
		}
	}

	/**
	 * Poll tick — invoked by the hidden {@code poll} link which is
	 * re-clicked via a JS {@code setTimeout} on every render while the
	 * overlay is visible. Stops after {@link #MAX_POLL_ATTEMPTS} ticks
	 * (client-side safety cap ≈ 35 s at the 2-s cadence).
	 */
	private void doPollAiCorrection(UserRequest ureq) {
		if (aiCorrectionKey == null) {
			return;
		}
		aiCorrectionPollAttempts++;
		EssayAiCorrectionService.CorrectionStatusView status = essayAiCorrectionService.getStatus(aiCorrectionKey, getIdentity());
		boolean stateChanged = true;
		if (status == null || status.status() == null) {
			// Correction vanished — treat as failed, drop overlay with an error message.
			aiCorrectionError = translate("ai.essay.correction.failed");
			finishAiCorrection();
		} else {
			switch (status.status()) {
				case DONE -> {
					FormativeFeedback feedback = essayAiCorrectionService.parseFeedback(status.feedbackJson());
					aiCorrectionFeedbackView = AiEssayFeedbackViewFlattener.flatten(feedback, getTranslator());
					if (aiCorrectionFeedbackView == null) {
						aiCorrectionError = translate("ai.essay.correction.failed");
					} else {
						applyAiScoreToPassedState(feedback);
					}
					finishAiCorrection();
				}
				case FAILED -> {
					aiCorrectionError = translate("ai.essay.correction.failed");
					finishAiCorrection();
				}
				case TIMEOUT -> {
					aiCorrectionError = translate("ai.essay.correction.timeout");
					finishAiCorrection();
				}
				case PENDING, RUNNING -> {
					if (aiCorrectionPollAttempts >= MAX_POLL_ATTEMPTS) {
						aiCorrectionError = translate("ai.essay.correction.timeout");
						finishAiCorrection();
					} else {
						// Still running — no observable state change. Skip
						// re-render so the SVG animation in the overlay does
						// not restart on every poll tick.
						stateChanged = false;
					}
				}
			}
		}
		if (stateChanged) {
			updateQuizUI();
		} else {
			// Suppress the otherwise-default repaint that the click event
			// triggers. The browser-side setInterval keeps firing on its own
			// schedule, independent of server-driven re-renders.
			mainVC.setDirty(false);
		}
	}

	/**
	 * Translate the AI-grader's self-estimated score percent into the quiz
	 * summary's passed/failed state for the question that produced the
	 * grading. ≥ {@link #AI_CORRECTION_PASS_THRESHOLD_PERCENT}% → passed,
	 * otherwise failed. {@link FormativeFeedback.Type#REJECTED} and
	 * {@link FormativeFeedback.Type#REFUSED_LONG} (pre-filter rejections)
	 * count as failed.
	 */
	private void applyAiScoreToPassedState(FormativeFeedback feedback) {
		if (aiCorrectionForQuestionId == null) {
			return;
		}
		boolean passed = false;
		if (feedback != null && feedback.type() == FormativeFeedback.Type.OK
				&& feedback.suggestion() != null) {
			int percent = feedback.suggestion().estimatedScorePercent();
			passed = percent >= AI_CORRECTION_PASS_THRESHOLD_PERCENT;
		}
		questionPassedState.put(aiCorrectionForQuestionId, passed);
	}

	private void finishAiCorrection() {
		aiCorrectionKey = null;
		aiCorrectionVisible = false;
		aiCorrectionPollAttempts = 0;
	}

	/**
	 * Reset the AI-correction overlay state when moving to a fresh
	 * question. The feedback view from a prior question must not leak
	 * into the current one.
	 */
	private void resetAiCorrectionState() {
		aiCorrectionKey = null;
		aiCorrectionForQuestionId = null;
		aiCorrectionPollAttempts = 0;
		aiCorrectionVisible = false;
		aiCorrectionFeedbackView = null;
		aiCorrectionError = null;
	}

	/**
	 * Called after the learner submits a response. If the current
	 * question is an essay backed by an {@link EssayAiGrading} row we
	 * trigger the async AI correction job and show the overlay. Manual
	 * essay items (no grading row) fall through to the legacy
	 * "Awaiting correction" behaviour unchanged.
	 */
	private void triggerAiCorrectionIfEssay(UserRequest ureq, String studentAnswer) {
		if (currentQuizQuestion == null) {
			logDebug("AI correction skipped: currentQuizQuestion is null");
			return;
		}
		if (!isManuallyGradedType(currentQuizQuestion)) {
			logDebug("AI correction skipped: type " + currentQuizQuestion.getType() + " is not manually graded");
			return;
		}
		QTI21QuestionType type = QTI21QuestionType.safeValueOf(currentQuizQuestion.getType());
		if (type != QTI21QuestionType.essay) {
			logDebug("AI correction skipped: type " + type + " is not essay");
			return;
		}
		if (!StringHelper.containsNonWhitespace(studentAnswer)) {
			// Empty answer → no AI grading possible. The default "passed=true"
			// fallback applied by outcomes(...) for manually-graded items would
			// otherwise inflate the quiz summary. Downgrade the entry now so
			// the final score reflects reality.
			if (StringHelper.containsNonWhitespace(currentQuizQuestion.getId())) {
				questionPassedState.put(currentQuizQuestion.getId(), Boolean.FALSE);
			}
			logDebug("AI correction skipped: empty studentAnswer");
			return;
		}
		ContentEditorQti.QuizQuestionStorageInfo storageInfo =
				contentEditorQti.getStorageInfo(quizPart, currentQuizQuestion);
		if (storageInfo == null || storageInfo.questionDirectory() == null) {
			logDebug("AI correction skipped: no question storage info for "
					+ currentQuizQuestion.getId());
			return;
		}
		EssayAiGrading grading = essayAiGradingFileStore.load(storageInfo.questionDirectory());
		if (grading == null) {
			logDebug("AI correction skipped: no ai-grading.json next to QTI item for assessmentItemIdentifier="
					+ currentQuizQuestion.getId()
					+ " (quiz may have been generated before the file-store refactor — regenerate to enable AI correction)");
			return;
		}
		Long itemSessionKey = null;
		if (assessmentItemDisplayController instanceof QuizAssessmentItemDisplayController quizCtrl) {
			itemSessionKey = quizCtrl.getItemSessionKey();
		}
		try {
			aiCorrectionKey = essayAiCorrectionService.submit(quizPart.getStoragePath(),
					currentQuizQuestion.getId(), studentAnswer, itemSessionKey, getIdentity());
			aiCorrectionForQuestionId = currentQuizQuestion.getId();
			aiCorrectionPollAttempts = 0;
			aiCorrectionVisible = true;
			aiCorrectionFeedbackView = null;
			aiCorrectionError = null;
			logInfo("AI correction " + aiCorrectionKey
					+ " submitted for assessmentItemIdentifier=" + currentQuizQuestion.getId());
		} catch (AiOverloadedException ol) {
			logInfo("Essay AI correction refused (overload): " + ol.getMessage());
			aiCorrectionError = translate("ai.essay.correction.overloaded");
			aiCorrectionVisible = false;
		} catch (AiRateLimitExceededException rl) {
			logInfo("Essay AI correction throttled: " + rl.getMessage());
			aiCorrectionError = translate("ai.essay.feedback.error.ratelimit");
			aiCorrectionVisible = false;
		} catch (Exception e) {
			logError("Failed to submit essay AI correction job", e);
			aiCorrectionError = translate("ai.essay.correction.failed");
			aiCorrectionVisible = false;
		}
		updateQuizUI();
		mainVC.setDirty(true);
	}

	private void reset() {
		updateRunStatus(AssessmentRunStatus.notStarted);
		updateCompletion(0.0);
		if (entry != null && StringHelper.containsNonWhitespace(subIdent)) {
			qtiService.deleteAssessmentTestSession(List.of(getIdentity()), entry, entry, subIdent);
		}
	}

	private void doStart(UserRequest ureq) {
		List<QuizQuestion> questions = quizPart.getSettings().getQuestions();
		if (questions.isEmpty()) {
			return;
		}

		state = State.quiz;

		initQuestionIndex(questions);
		doShowQuestion(ureq, questions.get(questionIndex));
	}

	private void initQuestionIndex(List<QuizQuestion> questions) {
		if (questions.isEmpty()) {
			return;
		}
		double completion = getCompletion();
		questionIndex = Math.min((int) Math.round(completion * questions.size()), questions.size() - 1);
	}

	private double getCompletion() {
		if (assessmentEntry == null || assessmentEntry.getCompletion() == null) {
			return 0.0d;
		}
		return assessmentEntry.getCompletion();
	}

	private void updateCompletion(Double completion) {
		if (assessmentEntry == null) {
			return;
		}
		assessmentEntry.setCompletion(completion);
		assessmentService.updateAssessmentEntry(assessmentEntry);
	}

	private void updateRunStatus(AssessmentRunStatus runStatus) {
		if (assessmentEntry == null) {
			return;
		}
		assessmentEntry.setCurrentRunStatus(runStatus);
		assessmentService.updateAssessmentEntry(assessmentEntry);
	}

	private void doNext(UserRequest ureq) {
		List<QuizQuestion> questions = quizPart.getSettings().getQuestions();
		if ((questionIndex + 1) < questions.size()) {
			questionIndex++;
			updateCompletion((double) questionIndex / questions.size());
			doShowQuestion(ureq, questions.get(questionIndex));
		} else {
			updateCompletion(1.0);
			updateRunStatus(AssessmentRunStatus.done);
			doShowResult(ureq);
		}
	}

	private void doShowQuestion(UserRequest ureq, QuizQuestion quizQuestion) {
		currentQuizQuestion = quizQuestion;
		mainVC.contextPut("quizOutcomeClass", "");
		resetAiCorrectionState();
		updateUI(ureq);

		ContentEditorQti.QuizQuestionStorageInfo storageInfo = contentEditorQti.getStorageInfo(quizPart, quizQuestion);
		URI assessmentItemUri = storageInfo.questionFile().toURI();
		ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItem(assessmentItemUri,
				storageInfo.questionDirectory());
		if (resolvedAssessmentItem == null || resolvedAssessmentItem.getItemLookup() == null ||
				resolvedAssessmentItem.getItemLookup().extractIfSuccessful() == null) {
			showError(translate("error.header") + ": " + assessmentItemUri);
			return;
		}

		AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
		if (!assessmentItem.getIdentifier().equals(quizQuestion.getId())) {
			assessmentItem.setIdentifier(quizQuestion.getId());
		}

		QTI21DeliveryOptions deliveryOptions = QTI21DeliveryOptions.defaultSettings();
		deliveryOptions.setPageMode(true);
		deliveryOptions.setLastQuestion(questionIndex >= (getNumberOfQuestions() - 1));
		assessmentItemDisplayController = new QuizAssessmentItemDisplayController(ureq, getWindowControl(),
				resolvedAssessmentItem, storageInfo.questionDirectory(), storageInfo.questionFile(),
				quizQuestion.getId(), deliveryOptions);
		listenTo(assessmentItemDisplayController);
		mainVC.put("question", assessmentItemDisplayController.getInitialComponent());
	}

	private void doShowResult(UserRequest ureq) {
		state = State.result;
		updateUI(ureq);
	}

	@Override
	public Component getComponent() {
		return getInitialComponent();
	}

	@Override
	public boolean validate(UserRequest ureq, List<ValidationMessage> messages) {
		return false;
	}

	@Override
	public void outcomes(String resultIdentifier, AssessmentTestSession candidateSession, Float score, Boolean pass,
						 SessionStatus sessionStatus) {
		if (sessionStatus.equals(SessionStatus.FINAL)) {
			if (StringHelper.containsNonWhitespace(resultIdentifier)) {
				updateRunStatus(AssessmentRunStatus.running);
				if (isManuallyGradedType(currentQuizQuestion)) {
					// Essay / upload / drawing items have no auto-scoring. Any
					// submitted response is valid and must NOT be rendered as
					// "wrong". We treat the submission as passed so progress
					// counters and the "show solution" flow behave correctly.
					questionPassedState.put(resultIdentifier, Boolean.TRUE);
					mainVC.contextPut("quizOutcomeClass", "o_submitted");
				} else {
					boolean passed = score != null && score >= 1;
					questionPassedState.put(resultIdentifier, passed);
					mainVC.contextPut("quizOutcomeClass", passed ? "o_correct" : "o_incorrect");
				}
			}
		}
	}

	/**
	 * True for question types that have no automatic correctness check
	 * (essay, upload, drawing). The learner's submission can only be graded
	 * manually or via AI grading — the zero-score outcome must not be
	 * rendered as "wrong".
	 */
	private static boolean isManuallyGradedType(QuizQuestion quizQuestion) {
		if (quizQuestion == null || !StringHelper.containsNonWhitespace(quizQuestion.getType())) {
			return false;
		}
		QTI21QuestionType type = QTI21QuestionType.safeValueOf(quizQuestion.getType());
		return type == QTI21QuestionType.essay
				|| type == QTI21QuestionType.upload
				|| type == QTI21QuestionType.drawing;
	}

	private enum State {
		intro,
		quiz,
		result
	}

	private class QuizAssessmentItemDisplayController extends AssessmentItemDisplayController {

		public QuizAssessmentItemDisplayController(UserRequest ureq, WindowControl wControl,
												   ResolvedAssessmentItem resolvedAssessmentItem, File fUnzippedDirRoot,
												   File itemFile, String externalRefIdentifier,
												   QTI21DeliveryOptions deliveryOptions) {
			super(ureq, wControl, entry, subIdent, entry, assessmentEntry, false, resolvedAssessmentItem,
					fUnzippedDirRoot, itemFile, externalRefIdentifier, deliveryOptions,
					QuizRunController.this, candidateAuditLogger);
		}

		@Override
		protected AssessmentTestSession initOrResumeAssessmentTestSession(RepositoryEntry courseEntry, String subIdent,
																		  RepositoryEntry referenceEntry,
																		  AssessmentEntry assessmentEntry,
																		  boolean authorMode) {
			if (courseEntry == null || !StringHelper.containsNonWhitespace(subIdent)) {
				candidateSession = qtiService.createInMemoryAssessmentTestSession(getIdentity());
				return candidateSession;
			}

			AssessmentTestSession lastSession = qtiService.getResumableAssessmentItemsSession(getIdentity(),
					null, courseEntry, subIdent, entry, authorMode);
			if (lastSession == null) {
				candidateSession = qtiService.createAssessmentTestSession(getIdentity(), null,
						assessmentEntry, courseEntry, subIdent, entry, null, authorMode);
				return candidateSession;
			}
			return lastSession;
		}

		@Override
		protected ItemSessionState loadItemSessionState() {
			ItemSessionState itemSessionState = qtiService.loadItemSessionState(candidateSession, itemSession);
			if (itemSessionState == null) {
				itemSessionState = new ItemSessionState();
			}
			return itemSessionState;
		}

		@Override
		public void handleResponses(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap, Map<Identifier,
				ResponseInput> fileResponseMap, String candidateComment, FormItem source) {
			// Snapshot the student's free-text answer BEFORE calling super: the
			// QTI engine does not keep the raw pre-binding strings after
			// response processing. Only essays send a StringInput — other types
			// (MC, upload, ...) produce no answer here.
			String essayAnswer = extractEssayAnswer(stringResponseMap);
			super.handleResponses(ureq, stringResponseMap, fileResponseMap, candidateComment, source);
			// Kick off the async AI correction job (if the question is an essay
			// backed by an EssayAiGrading row) AFTER super has persisted the
			// item session — we need its key for provenance on the usage log.
			triggerAiCorrectionIfEssay(ureq, essayAnswer);
		}

		Long getItemSessionKey() {
			return itemSession == null ? null : itemSession.getKey();
		}
	}

	/**
	 * Extract the learner's free-text essay answer from the QTI string
	 * response map. Essays use a single {@code RESPONSE} identifier; if
	 * more than one string response is present we concatenate them with
	 * a newline separator. Returns {@code null} if the map is empty or
	 * carries no non-blank strings (e.g. non-essay interactions).
	 */
	private static String extractEssayAnswer(Map<Identifier, ResponseInput> stringResponseMap) {
		if (stringResponseMap == null || stringResponseMap.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Identifier, ResponseInput> entry : stringResponseMap.entrySet()) {
			if (entry.getValue() instanceof ResponseInput.StringInput si) {
				String[] data = si.getResponseData();
				if (data != null) {
					for (String piece : data) {
						if (StringHelper.containsNonWhitespace(piece)) {
							if (sb.length() > 0) sb.append('\n');
							sb.append(piece);
						}
					}
				}
			}
		}
		String joined = sb.toString().trim();
		return joined.isEmpty() ? null : joined;
	}
}
