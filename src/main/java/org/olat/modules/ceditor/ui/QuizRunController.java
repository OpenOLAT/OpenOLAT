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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.manager.ContentEditorQti;
import org.olat.modules.ceditor.model.QuizQuestion;
import org.olat.modules.ceditor.model.QuizSettings;
import org.olat.modules.ceditor.model.jpa.QuizPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.manager.MediaDAO;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * Initial date: 2024-03-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class QuizRunController extends BasicController implements PageRunElement, OutcomesAssessmentItemListener {

	private final VelocityContainer mainVC;
	private final AssessmentEntry assessmentEntry;
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

	@Autowired
	private ContentEditorQti contentEditorQti;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private MediaDAO mediaDAO;

	public QuizRunController(UserRequest ureq, WindowControl wControl, QuizPart quizPart, boolean editable,
							 RepositoryEntry entry, String subIdent) {
		super(ureq, wControl);
		this.quizPart = quizPart;
		questionIndex = 0;
		this.editable = editable;
		this.entry = entry;
		this.subIdent = subIdent + "_" + quizPart.getId();

		assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null, entry,
				this.subIdent, Boolean.FALSE, entry);

		mainVC = createVelocityContainer("quiz_run");
		mainVC.setElementCssClass("o_quiz_run_element_css_class");
		setBlockLayoutClass(quizPart.getSettings());
		putInitialPanel(mainVC);

		AssessmentRunStatus runStatus = assessmentEntry.getCurrentRunStatus();
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
		AssessmentTestSession lastSession = qtiService.getResumableAssessmentItemsSession(getIdentity(),
				null, entry, subIdent, entry, false);
		if (lastSession != null) {
			List<AssessmentItemSession> itemSessions = qtiService.getAssessmentItemSessions(lastSession);
			for (AssessmentItemSession itemSession : itemSessions) {
				int score = itemSession.getScore() != null ? itemSession.getScore().intValue() : 0;
				if (StringHelper.containsNonWhitespace(itemSession.getAssessmentItemIdentifier())) {
					questionPassedState.put(itemSession.getAssessmentItemIdentifier(), score > 0);
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
		mainVC.contextPut("title", quizPart.getSettings().getTitle());
		mainVC.contextPut("description", substituteVariables(quizPart.getSettings().getDescription()));
		startButton = LinkFactory.createButton("quiz.start", mainVC, this);
		startButton.setIconLeftCSS("o_icon o_icon-fw o_icon_play");
		startButton.setPrimary(true);
		startButton.setEnabled(!editable);
		mainVC.put("quiz.start", startButton);
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
				updateUI(ureq);
			}
		} else if (assessmentItemDisplayController == source) {
			if (event instanceof QTIWorksAssessmentItemEvent qtiWorksAssessmentItemEvent) {
				if (QTIWorksAssessmentItemEvent.Event.next.name().equals(qtiWorksAssessmentItemEvent.getCommand())) {
					doNext(ureq);
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
		}
	}

	private void reset() {
		updateRunStatus(AssessmentRunStatus.notStarted);
		updateCompletion(0.0);
		qtiService.deleteAssessmentTestSession(List.of(getIdentity()), entry, entry, subIdent);
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
		if (assessmentEntry.getCompletion() == null) {
			return 0.0d;
		}
		return assessmentEntry.getCompletion();
	}

	private void updateCompletion(Double completion) {
		assessmentEntry.setCompletion(completion);
		assessmentService.updateAssessmentEntry(assessmentEntry);
	}

	private void updateRunStatus(AssessmentRunStatus runStatus) {
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
		mainVC.contextPut("quizOutcomeClass", "");
		updateUI(ureq);

		ContentEditorQti.QuizQuestionStorageInfo storageInfo = contentEditorQti.getStorageInfo(quizPart, quizQuestion);
		URI assessmentItemUri = storageInfo.questionFile().toURI();
		ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItem(assessmentItemUri,
				storageInfo.questionDirectory());

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
				questionPassedState.put(resultIdentifier, score >= 1);
				mainVC.contextPut("quizOutcomeClass", score >= 1 ? "o_correct" : "o_incorrect");
			}
		}
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
	}
}
