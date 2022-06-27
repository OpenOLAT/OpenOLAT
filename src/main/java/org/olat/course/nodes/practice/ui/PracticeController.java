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
package org.olat.course.nodes.practice.ui;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentEvents;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.PracticeCourseNode;
import org.olat.course.nodes.practice.PlayMode;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.ui.events.NextSerieEvent;
import org.olat.course.nodes.practice.ui.events.OverviewEvent;
import org.olat.course.nodes.practice.ui.events.ResponseEvent;
import org.olat.course.nodes.practice.ui.events.SkipEvent;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesAssessmentItemListener;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.audit.DefaultAssessmentSessionAuditLogger;
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.ims.qti21.ui.ResponseInput;
import org.olat.ims.qti21.ui.assessment.TerminatedStaticCandidateSessionContext;
import org.olat.ims.qti21.ui.components.ItemBodyResultFormItem;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.qpool.QPoolService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.RootNodeLookup;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 10 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeController extends BasicController implements OutcomesAssessmentItemListener {

	private final Link closeLink;
	private final ProgressBar progressBar;
	private final VelocityContainer mainVC;
	
	private final boolean saveSerie;
	private final boolean authorMode;
	private final RepositoryEntry courseEntry;
	private final PracticeCourseNode courseNode;
	private final List<RunningPracticeItem> runningPracticeItems;
	private final AssessmentSessionAuditLogger candidateAuditLogger = new DefaultAssessmentSessionAuditLogger();
	
	private final int maxLevels;
	private int currentIndex = 0;
	private final PlayMode playMode;
	private final int questionPerSeries;
	private RunningPracticeItem currentItem;
	private AssessmentTestSession testSession; 
	private final UserCourseEnvironment userCourseEnv;
	
	private EndController endCtrl;
	private ErrorController errorCtrl;
	private CloseableModalController cmc;
	private FeedbackController feedbackCtrl;
	private ConfirmCancelController cancelCtrl;
	private PracticeAssessmentItemController assessmentItemCtrl;
	
	private final Date startDate;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private PracticeService practiceService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public PracticeController(UserRequest ureq, WindowControl wControl, 
			RepositoryEntry courseEntry, PracticeCourseNode courseNode, List<PracticeItem> items,
			PlayMode playMode, UserCourseEnvironment userCourseEnv, boolean saveSerie, boolean authorMode) {
		super(ureq, wControl);
		
		this.playMode = playMode;
		this.saveSerie = saveSerie;
		this.authorMode = authorMode;
		this.courseNode = courseNode;
		this.courseEntry = courseEntry;
		runningPracticeItems = items.stream()
				.map(RunningPracticeItem::new)
				.collect(Collectors.toList());
		startDate = ureq.getRequestTimestamp();
		this.userCourseEnv = userCourseEnv;
		questionPerSeries = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_QUESTIONS_PER_SERIE, 10);
		maxLevels = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_LEVELS, 3);
		
		AssessmentEntry assessmentEntry;
		if(authorMode) {
			assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null, courseEntry, courseNode.getIdent(), Boolean.FALSE, null);
		} else {
			assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnv);
		}
		testSession = qtiService.createAssessmentTestSession(getIdentity(), null, assessmentEntry,
				courseEntry, courseNode.getIdent(), courseEntry, null, authorMode);

		mainVC = createVelocityContainer("practice");
		mainVC.contextPut("numOfItems", Integer.valueOf(items.size()));
		closeLink = LinkFactory.createIconClose("close", mainVC, this);
		mainVC.put("close", closeLink);
		
		progressBar = new ProgressBar("progress-serie", 100, 0.0f, Float.valueOf(items.size()), null);
		progressBar.setWidthInPercent(true);
		mainVC.put("progressBar", progressBar);
		
		putInitialPanel(mainVC);

		doQuestion(ureq);
	}

	@Override
	protected void doDispose() {
		if(testSession.isCancelled() || (testSession.getFinishTime() == null && testSession.getTerminationTime() == null)) {
			qtiService.deleteAssessmentTestSession(testSession);
		}
		super.doDispose();
	}

	@Override
	public void outcomes(AssessmentTestSession candidateSession, Float score, Boolean pass) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(closeLink == source) {
			doConfirmCancel(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentItemCtrl == source) {
			if(event instanceof ResponseEvent) {
				doFeedback(ureq, (ResponseEvent)event);
			} else if(event instanceof SkipEvent) {
				doNextQuestion(ureq);
			}
		} else if(feedbackCtrl == source || errorCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doNextQuestion(ureq);
			}
		} else if(endCtrl == source) {
			if(event instanceof NextSerieEvent || event instanceof OverviewEvent) {
				fireEvent(ureq, event);
			}
		} else if(cancelCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
			cmc.deactivate();
		}
	}
	
	private void doConfirmCancel(UserRequest ureq) {
		cancelCtrl = new ConfirmCancelController(ureq, getWindowControl(), playMode);
		listenTo(cancelCtrl);
		
		String title = translate("confirm.back.title");
		cmc = new CloseableModalController(getWindowControl(), "close", cancelCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doNextQuestion(UserRequest ureq) {
		RunningPracticeItem nextQuestion = getNextQuestion();
		if(nextQuestion == null && hasIncorrectAnsweredItem()) {
			currentIndex = -1;
			nextQuestion = getNextQuestion();
		}
		
		if(nextQuestion == null) {
			doEnd(ureq);
		} else {
			doQuestion(ureq);
		}
	}
	
	private RunningPracticeItem getNextQuestion() {
		int nextIndex = currentIndex + 1;
		for(int i=nextIndex; i<runningPracticeItems.size(); i++) {
			RunningPracticeItem item = runningPracticeItems.get(i);
			currentIndex = i;
			if((item.getAttempts() == 0 || !item.isCorrect()) && !item.isSkip() && !item.isError()) {
				return item;
			}
		}
		return null;
	}
	
	private boolean hasIncorrectAnsweredItem() {
		for(RunningPracticeItem item:runningPracticeItems) {
			if(!item.isCorrect()) {
				return true;
			}
		}
		return false;
	}
	
	private long numOfCorrectAnswers() {
		return runningPracticeItems.stream()
				.filter(RunningPracticeItem::isCorrect)
				.count();
	}
	
	private void doEnd(UserRequest ureq) {
		testSession = qtiService.reloadAssessmentTestSession(testSession);
		testSession.setTerminationTime(new Date());
		if(runningPracticeItems.size() >= questionPerSeries && saveSerie) {
			testSession.setPassed(Boolean.TRUE);
		} else {
			testSession.setPassed(Boolean.FALSE);
		}
		testSession.setNumOfAnsweredQuestions(runningPracticeItems.size());
		testSession.setNumOfQuestions(runningPracticeItems.size());
		testSession.setDuration(ureq.getRequestTimestamp().getTime() - startDate.getTime());
		testSession = qtiService.updateAssessmentTestSession(testSession);
		dbInstance.commitAndCloseSession();
		
		if(!userCourseEnv.isCourseReadOnly()) {
			updateCalculatedScoreAndStatus(ureq);
		}
		
		endCtrl = new EndController(ureq, getWindowControl());
		listenTo(endCtrl);
		mainVC.put("endCmp", endCtrl.getInitialComponent());
	}
	
	private void updateCalculatedScoreAndStatus(UserRequest ureq) {
		int seriesPerChallenge = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_SERIE_PER_CHALLENGE, 2);
		int challengesToComplete = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_NUM_CHALLENGES_FOR_COMPLETION, 2);
		int totalSeries = seriesPerChallenge * challengesToComplete;
		long completedSeries = practiceService.countCompletedSeries(getIdentity(), courseEntry, courseNode.getIdent());
		
		AssessmentEntryStatus status;
		AssessmentRunStatus runStatus;
		double progress;
		if(totalSeries == 0 || completedSeries == 0) {
			progress = 0.0d;
			status = AssessmentEntryStatus.inProgress;
			runStatus = AssessmentRunStatus.running;
		} else {
			progress = completedSeries / (double)totalSeries;
			if(progress > 1.0d) {
				progress = 1.0d;
			}
			status = completedSeries >= totalSeries ? AssessmentEntryStatus.done : AssessmentEntryStatus.inProgress;
			runStatus = completedSeries >= totalSeries ? AssessmentRunStatus.done : AssessmentRunStatus.running;
		}
		
		AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
		Float score = scoreEval.getScore();
		int currentScore = score == null ? 0 : Math.round(score.floatValue());
		int numOfQuestions = runningPracticeItems.size();
		if(numOfQuestions >= 10 && numOfQuestions < 20) {
			currentScore += 2;
		} else if(numOfQuestions >= 20 && numOfQuestions < 50) {
			currentScore += 4;
		} else if(numOfQuestions >= 50) {
			currentScore += 10;
		}
		
		for(RunningPracticeItem item:runningPracticeItems) {
			if(item.isCorrectAtFirstAttempts()) {
				if(runningPracticeItems.size() > 1) {
					currentScore += 2;
				} else {
					currentScore += 1;
				}
			}
			if(item.isCorrect()) {
				currentScore += 1;
			}
		}

		ScoreEvaluation doneEval = new ScoreEvaluation(Float.valueOf(currentScore), scoreEval.getGrade(),
				scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), status,
				scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(),
				Double.valueOf(progress), runStatus, scoreEval.getAssessmentID());
		
		courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, userCourseEnv, getIdentity(),
				false, Role.user);
		
		if(status == AssessmentEntryStatus.done && scoreEval.getAssessmentStatus() != status) {
			fireEvent(ureq, AssessmentEvents.CHANGED_EVENT);
		}
	}
	
	private void doFeedback(UserRequest ureq, ResponseEvent re) {
		removeAsListenerAndDispose(feedbackCtrl);
		
		File itemFile = assessmentItemCtrl.getItemFile();
		File fUnzippedDirRoot = assessmentItemCtrl.getUnzippedDirectoryRoot();
		ResolvedAssessmentItem resolvedAssessmentItem = assessmentItemCtrl.getResolvedAssessmentItem();
		ItemSessionState sessionState = assessmentItemCtrl.getItemSessionState();
		AssessmentTestSession candidateSession = assessmentItemCtrl.getCandidateSession();
		
		feedbackCtrl = new FeedbackController(ureq, getWindowControl(),
				resolvedAssessmentItem, candidateSession, sessionState, fUnzippedDirRoot, itemFile,
				re.getPracticeAssessmentItemGlobalRef(), re.getPasssed());
		listenTo(feedbackCtrl);
		
		mainVC.put("feedbackCmp", feedbackCtrl.getInitialComponent());
	}
	
	private void doQuestion(UserRequest ureq) {
		if(currentIndex < runningPracticeItems.size()) {
			currentItem = runningPracticeItems.get(currentIndex);

			long correctAnswers = numOfCorrectAnswers();
			progressBar.setActual(correctAnswers);
			String progressNumbers = translate("progres.numbers",
					Long.toString(correctAnswers), Integer.toString(runningPracticeItems.size()));
			mainVC.contextPut("progressNumbers", progressNumbers);
			updateUIQuestion(ureq, currentItem);
		}
	}
	
	private void cleanUp() {
		mainVC.remove("feedbackCmp");
		mainVC.remove("assessmentItemCmp");
		
		removeAsListenerAndDispose(assessmentItemCtrl);
		removeAsListenerAndDispose(feedbackCtrl);
		removeAsListenerAndDispose(errorCtrl);
		assessmentItemCtrl = null;
		feedbackCtrl = null;
		errorCtrl = null;
	}
	
	private void updateUIQuestion(UserRequest ureq, RunningPracticeItem runningItem) {
		cleanUp();
		
		QTI21DeliveryOptions options = QTI21DeliveryOptions.defaultSettings();
		options.setEnableAssessmentItemBack(false);
		options.setEnableAssessmentItemResetHard(false);
		options.setEnableAssessmentItemSkip(false);
		options.setEnableCancel(false);
		options.setEnableSuspend(false);
		

		final File itemFile;
		final File fUnzippedDirRoot;
		final ResolvedAssessmentItem resolvedAssessmentItem;
		
		PracticeItem item = runningItem.getItem();
		if(item.getRepositoryEntry() != null) {
			AssessmentItemRef itemRef = item.getItemRef();
			FileResourceManager frm = FileResourceManager.getInstance();
			fUnzippedDirRoot = frm.unzipFileResource(item.getRepositoryEntry().getOlatResource());

			ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
			resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			if(resolvedAssessmentItem == null) {
				return;
			}
			RootNodeLookup<AssessmentItem> rootNode = resolvedAssessmentItem.getItemLookup();
			if(rootNode != null) {
				URI itemUri = rootNode.getSystemId();
				itemFile = new File(itemUri);
			} else {
				return;
			}
		} else if(item.getItem() != null) {
			itemFile = qpoolService.getRootFile(item.getItem());
			fUnzippedDirRoot = qpoolService.getRootDirectory(item.getItem());
			URI assessmentItemUri = itemFile.toURI();
			resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItem(assessmentItemUri, fUnzippedDirRoot);
		} else {
			return;
		}
		
		if(resolvedAssessmentItem != null && resolvedAssessmentItem.getRootNodeLookup().wasSuccessful()) {
			AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null,
					courseEntry, courseNode.getIdent(), Boolean.FALSE, courseEntry);

			assessmentItemCtrl = new PracticeAssessmentItemController(ureq, getWindowControl(),
					courseEntry, courseNode.getIdent(), courseEntry,
					assessmentEntry, authorMode, resolvedAssessmentItem, fUnzippedDirRoot, itemFile,
					runningItem, options, this, candidateAuditLogger);
			listenTo(assessmentItemCtrl);
			mainVC.put("assessmentItemCmp", assessmentItemCtrl.getInitialComponent());
		} else {
			runningItem.setError(true);
			
			errorCtrl = new ErrorController(ureq, getWindowControl());
			listenTo(errorCtrl);
			mainVC.put("assessmentItemCmp", errorCtrl.getInitialComponent());
		}
	}
	
	private static class RunningPracticeItem {
		
		private final PracticeItem item;
		private int attempts;
		private boolean correct;
		private boolean correctAtFirstAttempts;
		private boolean skip = false;
		private boolean error = false;
		
		public RunningPracticeItem(PracticeItem item) {
			this.item = item;
		}
		
		public String getIdentifier() {
			return item.getIdentifier();
		}
		
		public PracticeItem getItem() {
			return item;
		}

		public boolean isCorrect() {
			return correct;
		}

		public void setCorrect(boolean correct) {
			this.correct = correct;
		}

		public boolean isError() {
			return error;
		}

		public void setError(boolean error) {
			this.error = error;
		}

		public int getAttempts() {
			return attempts;
		}

		public void incrementAttempts() {
			this.attempts++;
		}

		public boolean isCorrectAtFirstAttempts() {
			return correctAtFirstAttempts;
		}

		public void setCorrectAtFirstAttempts(boolean correctAtFirstAttempts) {
			this.correctAtFirstAttempts = correctAtFirstAttempts;
		}

		public boolean isSkip() {
			return skip;
		}

		public void setSkip(boolean skip) {
			this.skip = skip;
		}
	}

	private class EndController extends FormBasicController {
		
		private FormLink backButton;
		
		public EndController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "practice_end");
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if(formLayout instanceof FormLayoutContainer) {
				FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
				if(playMode == PlayMode.freeShuffle) {
					layoutCont.contextPut("msg", translate("serie.completed"));
				} else {
					layoutCont.contextPut("msg", translate("serie.completed.individual"));
				}

				long numCorrectAtFirstAttempts = runningPracticeItems.stream()
						.filter(RunningPracticeItem::isCorrectAtFirstAttempts)
						.count();
				double correctAtFirstAttempts = numCorrectAtFirstAttempts / (double)runningPracticeItems.size();
				long procentCorrect = Math.round(correctAtFirstAttempts * 100.0d);
				layoutCont.contextPut("procentCorrect", Long.toString(procentCorrect));
				
				List<AssessmentTestSession> series = practiceService.getSeries(getIdentity(), courseEntry, courseNode.getIdent());
				
				int seriesToday = 0;
				long duration = 0l;
				for(AssessmentTestSession session:series) {
					if(DateUtils.isSameDay(ureq.getRequestTimestamp(), session.getCreationDate())) {
						seriesToday++;
						if(session.getDuration() != null) {
							duration += session.getDuration().longValue();
						}
					}
				}

				String durationStr = "-";
				if(duration > 0l) {
					durationStr = Formatter.formatDuration(duration);
				}
				layoutCont.contextPut("durationToday", durationStr);
				layoutCont.contextPut("numOfSeriesToday", Integer.toString(seriesToday));
				if(saveSerie) {
					initProgressInChallenges(layoutCont, series);
				}
			}
			
			backButton = uifactory.addFormLink("back.overview", formLayout, Link.BUTTON);
			if(playMode == PlayMode.freeShuffle) {
				uifactory.addFormSubmitButton("next.serie", formLayout);
			}
		}
		
		private void initProgressInChallenges(FormLayoutContainer layoutCont, List<AssessmentTestSession> series) {
			int seriesPerChallenge = courseNode.getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_SERIE_PER_CHALLENGE, 2);
			
			final int completedSeries = PracticeHelper.completedSeries(series);
			final int currentNumOfSeries = completedSeries % seriesPerChallenge;
			
			// Series
			String currentSeriesI18n = seriesPerChallenge > 1 ? "current.series.plural" : "current.series.singular";
			String currentSeriesStr;
			// Check if the user completed a challenge
			boolean ended = currentNumOfSeries == 0 && completedSeries >= seriesPerChallenge;
			if(ended) {
				currentSeriesStr = Integer.toString(seriesPerChallenge);
			} else {
				currentSeriesStr = Integer.toString(currentNumOfSeries);
			}
			String currentSeries = translate(currentSeriesI18n, currentSeriesStr, Integer.toString(seriesPerChallenge));
			flc.contextPut("currentSeries", currentSeries);
			
			double currentSeriesProgress = 0.0d;
			int previousNumOfSeries = 0;
			if(ended) {
				previousNumOfSeries = seriesPerChallenge - 1;
				currentSeriesProgress = 100.0d;
			} else if(currentNumOfSeries > 0) {
				previousNumOfSeries = currentNumOfSeries - 1;
				currentSeriesProgress = (currentNumOfSeries / (double)seriesPerChallenge) * 100.0d;
			}
			
			double previousSeriesProgress = Math.max(0.0d, ((previousNumOfSeries / (double)seriesPerChallenge) * 100.0d));
			layoutCont.contextPut("currentSeries", currentSeries);
			layoutCont.contextPut("previousSeriesProgress", Double.valueOf(previousSeriesProgress));
			layoutCont.contextPut("currentSeriesProgress", Double.valueOf(currentSeriesProgress));
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(backButton == source) {
				fireEvent(ureq, new OverviewEvent());
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, new NextSerieEvent(playMode));
		}	
	}
	
	private class ErrorController extends FormBasicController {
		
		public ErrorController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "item_error", Util.createPackageTranslator(AssessmentItemDisplayController.class, ureq.getLocale()));
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FormSubmit nextButton = uifactory.addFormSubmitButton("next.question", formLayout);
			nextButton.setFocus(true);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private class FeedbackController extends FormBasicController {
		
		private final Boolean passed;
		private final ItemSessionState sessionState;
		private final ResolvedAssessmentItem resolvedAssessmentItem;
		private CandidateSessionContext candidateSessionContext;
		private final File submissionDirToDispose;
		private final PracticeAssessmentItemGlobalRef globalRef;
		
		private final String mapperUri;
		private final String submissionMapperUri = null;//TODO practice
		private final ResourceLocator inputResourceLocator;
		
		public FeedbackController(UserRequest ureq, WindowControl wControl, ResolvedAssessmentItem resolvedAssessmentItem,
				AssessmentTestSession testSession, ItemSessionState sessionState, File fUnzippedDirRoot, File itemFile,
				PracticeAssessmentItemGlobalRef globalRef, Boolean passed) {
			super(ureq, wControl, "feedback", Util.createPackageTranslator(AssessmentItemDisplayController.class, ureq.getLocale()));
			this.passed = passed;
			this.globalRef = globalRef;
			this.sessionState = sessionState;
			this.resolvedAssessmentItem = resolvedAssessmentItem;
			candidateSessionContext = new TerminatedStaticCandidateSessionContext(testSession);
			
			ResourceLocator fileResourceLocator = new PathResourceLocator(fUnzippedDirRoot.toPath());
			inputResourceLocator = 
	        		ImsQTI21Resource.createResolvingResourceLocator(fileResourceLocator);
			
			submissionDirToDispose = qtiService.getSubmissionDirectory(testSession);
			mapperUri = registerCacheableMapper(ureq, UUID.randomUUID().toString(),
					new ResourcesMapper(itemFile.toURI(), fUnzippedDirRoot, submissionDirToDispose));
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if(formLayout instanceof FormLayoutContainer) {
				FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
				layoutCont.contextPut("passed", passed);
				
				String itemTitle = resolvedAssessmentItem.getItemLookup().extractIfSuccessful().getTitle();
				layoutCont.contextPut("itemTitle", itemTitle);

				layoutCont.contextPut("itemLevel", Integer.valueOf(globalRef.getLevel()));
				layoutCont.contextPut("maxItemLevel", Integer.valueOf(maxLevels));
				
				List<Integer> globalLevels = new ArrayList<>();
				for(int i=1; i<=maxLevels; i++) {
					globalLevels.add(Integer.valueOf(i));
				}
				layoutCont.contextPut("globalLevels", globalLevels);
			}
			
			ItemBodyResultFormItem userFormItem = new ItemBodyResultFormItem("userResponseItem", resolvedAssessmentItem);
			initInteractionResultFormItem(userFormItem);
			formLayout.add("userResponseItem", userFormItem);
			
			// solution
			ItemBodyResultFormItem solutionFormItem = new ItemBodyResultFormItem("solutionItem", resolvedAssessmentItem);
			solutionFormItem.setShowSolution(true);
			solutionFormItem.setReport(true);
			solutionFormItem.setVisible(passed == null || !passed.booleanValue());
			initInteractionResultFormItem(solutionFormItem);
			formLayout.add("solutionItem", solutionFormItem);
			
			FormSubmit nextButton = uifactory.addFormSubmitButton("next.question", formLayout);
			nextButton.setFocus(true);
		}
		
		private void initInteractionResultFormItem(ItemBodyResultFormItem formItem) {
			formItem.setItemSessionState(sessionState);
			formItem.setCandidateSessionContext(candidateSessionContext);
			formItem.setResourceLocator(inputResourceLocator);
			formItem.setMapperUri(mapperUri);
			if(submissionMapperUri != null) {
				formItem.setSubmissionMapperUri(submissionMapperUri);
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private class PracticeAssessmentItemController extends AssessmentItemDisplayController {
		
		private AssessmentResult assessmentResult;
		private ItemSessionState itemSessionState;
		private final File unzippedDirectoryRoot;
		private final File assessmentItemFile;
		private final RunningPracticeItem practiceItem;
		
		public PracticeAssessmentItemController(UserRequest ureq, WindowControl wControl,
				RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, AssessmentEntry assessmentEntry, boolean authorMode,
				ResolvedAssessmentItem resolvedAssessmentItem, File fUnzippedDirRoot, File itemFile, RunningPracticeItem practiceItem,
				QTI21DeliveryOptions deliveryOptions, OutcomesAssessmentItemListener outcomesListener,
				AssessmentSessionAuditLogger candidateAuditLogger) {
			super(ureq, wControl, entry, subIdent, testEntry, assessmentEntry, authorMode,
					resolvedAssessmentItem, fUnzippedDirRoot, itemFile, practiceItem.getIdentifier(),
					deliveryOptions, outcomesListener, candidateAuditLogger);
			unzippedDirectoryRoot = fUnzippedDirRoot;
			this.assessmentItemFile = itemFile;
			this.practiceItem = practiceItem;
			if(practiceItem.getAttempts() > 0) {
				itemSessionController.resetItemSessionHard(ureq.getRequestTimestamp(), false);
			}
			
			List<PracticeAssessmentItemGlobalRef> refs = practiceService.getPracticeAssessmentItemGlobalRefs(List.of(practiceItem.getItem()), getIdentity());
			int level = refs.isEmpty() ? 0 : refs.get(0).getLevel();
			qtiWorksCtrl.showQuestionLevel(level, maxLevels);
			qtiWorksCtrl.setSubmitI18nKey("submit.check");
			qtiWorksCtrl.setEnableAlwaysSkip(practiceItem.getAttempts() >= 1);
		}
		
		protected ItemSessionState getItemSessionState() {
			return itemSessionState;
		}
		
		protected ResolvedAssessmentItem getResolvedAssessmentItem() {
			return resolvedAssessmentItem;
		}
		
		public File getUnzippedDirectoryRoot() {
			return unzippedDirectoryRoot;
		}
		
		public File getItemFile() {
			return assessmentItemFile;
		}

		@Override
		protected ItemSessionState loadItemSessionState() {
			itemSessionState = qtiService.loadItemSessionState(candidateSession, itemSession);
			if(itemSessionState == null) {
				itemSessionState = new ItemSessionState();
			}
			return itemSessionState;
		}
		
		@Override
		protected AssessmentItemSession getItemSession(ResolvedAssessmentItem rAssessmentItem, String externalRefIdentifier) {
			String  assessmentItemIdentifier = rAssessmentItem.getRootNodeLookup().extractIfSuccessful().getIdentifier() + "-" + (currentIndex);
			return qtiService.getOrCreateAssessmentItemSession(candidateSession, null, assessmentItemIdentifier, externalRefIdentifier);
		}

		@Override
		protected AssessmentTestSession initOrResumeAssessmentTestSession(RepositoryEntry cEntry, String subIdent,
				RepositoryEntry testEntry, AssessmentEntry assessmentEntry, boolean author) {
			candidateSession = testSession;
			return testSession;
		}

		@Override
		public void handleResponses(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap,
				Map<Identifier,ResponseInput> fileResponseMap, String candidateComment) {
			super.handleResponses(ureq, stringResponseMap, fileResponseMap, candidateComment);
			
			final Boolean passed = itemSession.getPassed();
			final boolean correct = passed == null || passed.booleanValue();
			
			practiceItem.setCorrect(correct);
			practiceItem.incrementAttempts();
			final boolean firstAttempt = practiceItem.getAttempts() == 1;
			if(firstAttempt && correct) {
				practiceItem.setCorrectAtFirstAttempts(true);
			}

			PracticeAssessmentItemGlobalRef globalRef = practiceService
					.updateAssessmentItemGlobalRef(getIdentity(), practiceItem.getIdentifier(), firstAttempt, correct);
			if(passed != null) {
				fireEvent(ureq, new ResponseEvent(passed, globalRef));
			} else {
				fireEvent(ureq, new ResponseEvent(null, globalRef));
			}
		}

		@Override
		protected void next(UserRequest ureq, QTIWorksAssessmentItemEvent event) {
			super.next(ureq, event);
			
			if(event.getEvent() == QTIWorksAssessmentItemEvent.Event.skip) {
				practiceItem.setSkip(true);
				fireEvent(ureq, new SkipEvent(null));
			}
		}

		@Override
		protected void collectOutcomeVariablesForItemSession(BigDecimal score, BigDecimal maxScore, Boolean passed) {
			if(passed == null) {
				if(score == null) {
					itemSession.setPassed(Boolean.TRUE);
				} else if(maxScore != null) {
					if(score.equals(maxScore)) {
						itemSession.setPassed(Boolean.TRUE);
					} else {
						itemSession.setPassed(Boolean.FALSE);
					}
				} else if(score.doubleValue() <= 0.01d) {
					itemSession.setPassed(Boolean.FALSE);
				} else {
					itemSession.setPassed(Boolean.TRUE);
				}
			} else {
				itemSession.setPassed(passed);
			}
		}

		@Override
		protected ItemSessionController enterSession(UserRequest ureq) {
			File assessmentResultFile = qtiService.getAssessmentResultFile(candidateSession);
			if(assessmentResultFile.exists()) {
				assessmentResult = qtiService.getAssessmentResult(candidateSession);
			}
			return super.enterSession(ureq);
		}
		
		@Override
	    protected AssessmentResult updateSessionFinishedStatus(UserRequest ureq) {
	        // we don't close the candidate session
	        return computeAndRecordItemAssessmentResult(ureq);
	    }

		@Override
		public AssessmentResult computeItemAssessmentResult(UserRequest ureq) {
	    	String baseUrl = "http://localhost:8080/olat";
	        final URI sessionIdentifierSourceId = URI.create(baseUrl);
	        final String sessionIdentifier = "itemsession/" + (candidateSession == null ? "sdfj" : candidateSession.getKey());
	        if(assessmentResult == null) {
	        	assessmentResult = itemSessionController.computeAssessmentResult(ureq.getRequestTimestamp(), sessionIdentifier, sessionIdentifierSourceId);
	        	return assessmentResult;
	        }
	        return itemSessionController.computeAssessmentResult(assessmentResult, ureq.getRequestTimestamp());
	    }
	}
}
