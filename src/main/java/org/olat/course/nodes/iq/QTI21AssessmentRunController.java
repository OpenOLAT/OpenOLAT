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
package org.olat.course.nodes.iq;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.DisposedCourseRestartController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.SelfAssessableCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesListener;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
import org.olat.ims.qti21.QTI21LoggingAction;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.AssessmentResultController;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.QTI21Event;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentRunController extends BasicController implements GenericEventListener, OutcomesListener {
	
	private static final OLATResourceable assessmentEventOres = OresHelper.createOLATResourceableType(AssessmentEvent.class);
	private static final OLATResourceable assessmentInstanceOres = OresHelper.createOLATResourceableType(AssessmentInstance.class);
	
	private Link startButton, showResultsButton, hideResultsButton;
	private final VelocityContainer mainVC;
	
	private boolean assessmentStopped = true;
	
	private EventBus singleUserEventCenter;
	private final boolean anonym;
	private final UserSession userSession;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;
	private final QTICourseNode courseNode;
	private final RepositoryEntry testEntry;
	private final QTI21DeliveryOptions deliveryOptions;
	// The test is really assessment not a self test or a survey
	private final boolean assessmentType = true;
	
	private AssessmentResultController resultCtrl;
	private AssessmentTestDisplayController displayCtrl;
	private LayoutMain3ColsController displayContainerController;
	private AtomicBoolean incrementAttempts = new AtomicBoolean(true);
	
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21AssessmentRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, QTICourseNode courseNode) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(AssessmentTestDisplayController.class, getLocale(), getTranslator()));
		
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		userSession = ureq.getUserSession();
		anonym = userSession.getRoles().isGuestOnly();
		config = courseNode.getModuleConfiguration();
		testEntry = courseNode.getReferencedRepositoryEntry();
		singleUserEventCenter = userSession.getSingleUserEventCenter();
		mainVC = createVelocityContainer("assessment_run");
		
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));
		
		if(courseNode instanceof IQTESTCourseNode) {
			mainVC.contextPut("type", "test");
		} else if(courseNode instanceof IQSELFCourseNode) {
			mainVC.contextPut("type", "self");
		}
		
		deliveryOptions = getDeliveryOptions();
		init(ureq);
		initAssessment(ureq);
		putInitialPanel(mainVC);
	}
	
	private void init(UserRequest ureq) {
		startButton = LinkFactory.createButton("start", mainVC, this);
		startButton.setElementCssClass("o_sel_start_qti21assessment");
		startButton.setPrimary(true);
		startButton.setVisible(!userCourseEnv.isCourseReadOnly());
		
		// fetch disclaimer file
		String sDisclaimer = (String)config.get(IQEditController.CONFIG_KEY_DISCLAIMER);
		if (sDisclaimer != null) {
			VFSContainer baseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
			int lastSlash = sDisclaimer.lastIndexOf('/');
			if (lastSlash != -1) {
				baseContainer = (VFSContainer)baseContainer.resolve(sDisclaimer.substring(0, lastSlash));
				sDisclaimer = sDisclaimer.substring(lastSlash);
				// first check if disclaimer exists on filesystem
				if (baseContainer == null || baseContainer.resolve(sDisclaimer) == null) {
					showWarning("disclaimer.file.invalid", sDisclaimer);
				} else {
					//screenreader do not like iframes, display inline
					IFrameDisplayController iFrameCtr = new IFrameDisplayController(ureq, getWindowControl(), baseContainer);
					listenTo(iFrameCtr);//dispose automatically
					mainVC.put("disc", iFrameCtr.getInitialComponent());
					iFrameCtr.setCurrentURI(sDisclaimer);
					mainVC.contextPut("hasDisc", Boolean.TRUE);
				}
			}
		}
		
		if (assessmentType) {
			checkChats(ureq);
			singleUserEventCenter.registerFor(this, getIdentity(), InstantMessagingService.TOWER_EVENT_ORES);
		}
	}
	
	private void initAssessment(UserRequest ureq) {
	    // config : show score info
		boolean enableScoreInfo= config.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
		mainVC.contextPut("enableScoreInfo", new Boolean(enableScoreInfo));
	   
	    // configuration data
		int maxAttempts = deliveryOptions.getMaxAttempts();
		if(maxAttempts > 0) {
			mainVC.contextPut("attemptsConfig", new Integer(maxAttempts));
		} else {
			mainVC.contextPut("attemptsConfig", Boolean.FALSE);
		}
		
	    // user data
		if (courseNode instanceof SelfAssessableCourseNode) {
			SelfAssessableCourseNode acn = (SelfAssessableCourseNode)courseNode; 
			ScoreEvaluation scoreEval = acn.getUserScoreEvaluation(userCourseEnv);
			Integer attempts = acn.getUserAttempts(userCourseEnv);
			if (scoreEval != null) {
				mainVC.contextPut("hasResults", Boolean.TRUE);
				mainVC.contextPut("score", AssessmentHelper.getRoundedScore(scoreEval.getScore()));
				mainVC.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
				mainVC.contextPut("passed", scoreEval.getPassed());
				mainVC.contextPut("attempts", attempts); //at least one attempt
			}
		} else if(courseNode instanceof IQTESTCourseNode) {
			IQTESTCourseNode testCourseNode = (IQTESTCourseNode)courseNode;
			AssessmentEntry assessmentEntry = testCourseNode.getUserAssessmentEntry(userCourseEnv);
			if(assessmentEntry == null) {
				mainVC.contextPut("blockAfterSuccess", Boolean.FALSE);
				mainVC.contextPut("score", null);
				mainVC.contextPut("hasPassedValue", Boolean.FALSE);
				mainVC.contextPut("passed", Boolean.FALSE);
				mainVC.contextPut("comment", null);
				mainVC.contextPut("attempts", 0);
			} else {
				Boolean passed = assessmentEntry.getPassed();
				//block if test passed (and config set to check it)
				Boolean blocked = Boolean.FALSE;
				boolean blockAfterSuccess = config.getBooleanSafe(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS);
				if(blockAfterSuccess && passed != null && passed.booleanValue()) {
					blocked = Boolean.TRUE;
				}
				mainVC.contextPut("blockAfterSuccess", blocked);
				
				Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
				mainVC.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
				mainVC.contextPut("hasPassedValue", (passed == null ? Boolean.FALSE : Boolean.TRUE));
				mainVC.contextPut("passed", passed);
				StringBuilder comment = Formatter.stripTabsAndReturns(testCourseNode.getUserUserComment(userCourseEnv));
				mainVC.contextPut("comment", StringHelper.xssScan(comment));
				Integer attempts = assessmentEntry.getAttempts();
				mainVC.contextPut("attempts", attempts == null ? new Integer(0) : attempts);
	
				if(!anonym) {
					UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
					mainVC.contextPut("log", am.getUserNodeLog(courseNode, identity));
				}
			}
		}
		
		exposeResults(ureq);
	}
	
	private void checkChats (UserRequest ureq) {
		List<?> allChats = null;
		if (ureq != null) {
			allChats = ureq.getUserSession().getChats();
		}
		if (allChats == null || allChats.size() == 0) {
			startButton.setEnabled (true);
			mainVC.contextPut("hasChatWindowOpen", false);
		} else {
			startButton.setEnabled (false);
			mainVC.contextPut("hasChatWindowOpen", true);
		}
	}
	
	/**
	 * WARNING! The variables showResultsOnHomePage, showResultsVisible and showChangelog are not used 
	 * in the velocity template and the CONFIG_KEY_RESULT_ON_HOME_PAGE is not editable
	 * in the configuration of the course element for QTI 2.1!!!!
	 * 
	 * Provides the show results button if results available or a message with the visibility period.
	 * @param ureq
	 */
	private void exposeResults(UserRequest ureq) {
		//migration: check if old tests have no summary configured
		boolean showResultsOnHomePage = config.getBooleanSafe(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
		
		ShowResultsOnFinish showSummary = deliveryOptions.getShowResultsOnFinish();
		String defaultConfSummary = showSummary == null ? AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT : showSummary.getIQEquivalent();
		String configuredSummary = config.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, defaultConfSummary);
		boolean noSummary = configuredSummary == null || (configuredSummary!=null && configuredSummary.equals(AssessmentInstance.QMD_ENTRY_SUMMARY_NONE));
		if(!noSummary) {
			mainVC.contextPut("showResultsOnHomePage", new Boolean(showResultsOnHomePage));			
			boolean dateRelatedVisibility = isResultVisible(config);		
			if(showResultsOnHomePage && dateRelatedVisibility) {
				mainVC.contextPut("showResultsVisible",Boolean.TRUE);
				showResultsButton = LinkFactory.createLink("command.showResults", "command.showResults", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
				showResultsButton.setCustomDisplayText(translate("showResults.title"));
				showResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_open_togglebox");
				
				hideResultsButton = LinkFactory.createLink("command.hideResults", "command.hideResults", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
				hideResultsButton.setCustomDisplayText(translate("showResults.title"));
				hideResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_close_togglebox");
			} else if(showResultsOnHomePage) {
				Date startDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
				Date endDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
				String visibilityStartDate = Formatter.getInstance(getLocale()).formatDate(startDate);
				String visibilityEndDate = "-";
				if(endDate != null) {
					visibilityEndDate = Formatter.getInstance(getLocale()).formatDate(endDate);
				}
				String visibilityPeriod = getTranslator().translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate});
				mainVC.contextPut("visibilityPeriod", visibilityPeriod);
				mainVC.contextPut("showResultsVisible", Boolean.FALSE);
			}
		}
		
		UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
		mainVC.contextPut("log", am.getUserNodeLog(courseNode, getIdentity()));	
		mainVC.contextPut("showChangelog", showResultsOnHomePage);
	}
	
	private boolean isResultVisible(ModuleConfiguration modConfig) {
		boolean isVisible = false;
		boolean showResultsActive = modConfig.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
		if(showResultsActive) {
			Date startDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			Date endDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			if(startDate == null && endDate == null) {
				isVisible = true;
			} else {
				Date currentDate = new Date();
				if(startDate != null && currentDate.after(startDate) && (endDate == null || currentDate.before(endDate))) {
					isVisible = true;
				}
			}
		} else {
			isVisible = true;
		}
		return isVisible;
	}
	
	@Override
	protected void doDispose() {
		if (assessmentType) {
			singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
			singleUserEventCenter.deregisterFor(this, InstantMessagingService.TOWER_EVENT_ORES);
			
			if (!assessmentStopped) {		 
				AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
				singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
			}
		}
	}
	
	@Override
	public void event(Event event) {
		if (assessmentType) {
			if (event.getCommand().startsWith("ChatWindow")) {
				checkChats(null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(startButton == source && startButton.isEnabled() && startButton.isVisible()) {
			doStart(ureq);
		}else if(source == showResultsButton) {			
			doShowResults(ureq);
		} else if (source == hideResultsButton) {
			doHideResults();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == displayCtrl) {
			if(event == Event.CANCELLED_EVENT) {
				doExitAssessment(ureq, event, false);
				exposeResults(ureq);
				showInfo("assessment.test.cancelled");
			} else if("suspend".equals(event.getCommand())) {
				doExitAssessment(ureq, event, false);
				exposeResults(ureq);
				showInfo("assessment.test.suspended");
			} else if(event instanceof QTI21Event) {
				QTI21Event qe = (QTI21Event)event;
				if(QTI21Event.EXIT.equals(qe.getCommand())) {
					if(!displayCtrl.isResultsVisible()) {
						doExitAssessment(ureq, event, true);
						initAssessment(ureq);
					}
				} else if(QTI21Event.CLOSE_RESULTS.equals(qe.getCommand())) {
					doExitAssessment(ureq, event, true);
					initAssessment(ureq);
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doShowResults(UserRequest ureq) {
		removeAsListenerAndDispose(resultCtrl);
		
		AssessmentTestSession session = null;
		if(courseNode instanceof SelfAssessableCourseNode) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			session = qtiService.getLastAssessmentTestSessions(courseEntry, courseNode.getIdent(), testEntry, getIdentity());
		} else {
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			AssessmentEntry assessmentEntry = am.getAssessmentEntry(courseNode, getIdentity());
			session = qtiService.getAssessmentTestSession(assessmentEntry.getAssessmentId());
		}
		
		if(session == null) {
			mainVC.contextPut("showResults", Boolean.FALSE);
		} else {
			doShowResults(ureq, session);
		}
	}

	private void doShowResults(UserRequest ureq, AssessmentTestSession session) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
		URI assessmentObjectUri = qtiService.createAssessmentObjectUri(fUnzippedDirRoot);
		File submissionDir = qtiService.getAssessmentResultFile(session);
		String mapperUri = registerCacheableMapper(null, "QTI21CNResults::" + session.getTestEntry().getKey(),
				new ResourcesMapper(assessmentObjectUri, submissionDir));

		resultCtrl = new AssessmentResultController(ureq, getWindowControl(), getIdentity(), true,
				session, getDeliveryOptions().getShowResultsOnFinish(), fUnzippedDirRoot, mapperUri, false, false);
		listenTo(resultCtrl);
		mainVC.put("resultReport", resultCtrl.getInitialComponent());
		mainVC.contextPut("showResults", Boolean.TRUE);
	}
	
	
	private void doHideResults() {
		mainVC.contextPut("showResults", Boolean.FALSE);
	}

	private void doStart(UserRequest ureq) {
		removeAsListenerAndDispose(displayCtrl);

		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck("test");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		RepositoryEntry courseRe = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		displayCtrl = new AssessmentTestDisplayController(ureq, bwControl, this, testEntry, courseRe, courseNode.getIdent(), deliveryOptions, true, false, false);
		listenTo(displayCtrl);
		if(displayCtrl.isTerminated()) {
			//do nothing
		} else {
			// in case displayController was unable to initialize, a message was set by displayController
			// this is the case if no more attempts or security check was unsuccessfull
			displayContainerController = new LayoutMain3ColsController(ureq, getWindowControl(), displayCtrl);
			listenTo(displayContainerController); // autodispose

			Panel empty = new Panel("empty");//empty panel set as "menu" and "tool"
			Controller courseCloser = new DisposedCourseRestartController(ureq, getWindowControl(), courseRe);
			Controller disposedRestartController = new LayoutMain3ColsController(ureq, getWindowControl(), empty, courseCloser.getInitialComponent(), "disposed");
			displayContainerController.setDisposedMessageController(disposedRestartController);
			
			boolean fullWindow = config.getBooleanSafe(IQEditController.CONFIG_FULLWINDOW);
			if(fullWindow) {
				displayContainerController.setAsFullscreen(ureq);
			}
			displayContainerController.activate();

			assessmentStopped = false;		
			singleUserEventCenter.registerFor(this, getIdentity(), assessmentInstanceOres);
			singleUserEventCenter.fireEventToListenersOf(new AssessmentEvent(AssessmentEvent.TYPE.STARTED, ureq.getUserSession()), assessmentEventOres);
			
			ThreadLocalUserActivityLogger.log(QTI21LoggingAction.QTI_START_IN_COURSE, getClass());
		}
	}
	
	private QTI21DeliveryOptions getDeliveryOptions() {
		QTI21DeliveryOptions testOptions = qtiService.getDeliveryOptions(testEntry);
		QTI21DeliveryOptions finalOptions = testOptions.clone();
		finalOptions.setMaxAttempts(config.getIntegerSafe(IQEditController.CONFIG_KEY_ATTEMPTS, testOptions.getMaxAttempts()));
		finalOptions.setBlockAfterSuccess(config.getBooleanSafe(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS, testOptions.isBlockAfterSuccess()));
		finalOptions.setShowTitles(config.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTIONTITLE, testOptions.isShowTitles()));
		finalOptions.setPersonalNotes(config.getBooleanSafe(IQEditController.CONFIG_KEY_MEMO, testOptions.isPersonalNotes()));
		finalOptions.setEnableCancel(config.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLECANCEL, testOptions.isEnableCancel()));
		finalOptions.setEnableSuspend(config.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLESUSPEND, testOptions.isEnableSuspend()));
		finalOptions.setDisplayQuestionProgress(config.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, testOptions.isDisplayQuestionProgress()));
		finalOptions.setDisplayScoreProgress(config.getBooleanSafe(IQEditController.CONFIG_KEY_SCOREPROGRESS, testOptions.isDisplayScoreProgress()));
		finalOptions.setShowResultsOnFinish(ShowResultsOnFinish.fromIQEquivalent(config.getStringValue(IQEditController.CONFIG_KEY_SUMMARY), ShowResultsOnFinish.compact));
		finalOptions.setShowMenu(config.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLEMENU, testOptions.isShowMenu()));
		finalOptions.setAllowAnonym(config.getBooleanSafe(IQEditController.CONFIG_ALLOW_ANONYM, testOptions.isAllowAnonym()));
		return finalOptions;
	}

	/**
	 * Remove the runtime from the GUI stack only.
	 * @param ureq
	 * @param event
	 * @param testEnded true if the test was ended and not suspended or cancelled (use to control increment of attempts)
	 */
	private void doExitAssessment(UserRequest ureq, Event event, boolean testEnded) {
		if(displayContainerController != null) {
			displayContainerController.deactivate(ureq);
		} else {
			getWindowControl().pop();
		}	
		
		removeHistory(ureq);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("test", -1l);
		addToHistory(ureq, ores, null);
		if (!assessmentStopped) {
			assessmentStopped = true;
			singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
			AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
			singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
		}
		if(testEnded) {
			incrementAttempts.set(true);
		}
		
		fireEvent(ureq, event);
	}

	@Override
	public void updateOutcomes(Float score, Boolean pass) {
		//ScoreEvaluation sceval = new ScoreEvaluation(score, pass, Boolean.FALSE);
		//courseNode.updateUserScoreEvaluation(sceval, userCourseEnv, getIdentity(), false);
	}

	@Override
	public void submit(Float score, Boolean pass, Long assessmentId) {
		if(anonym) return;
		
		if(courseNode instanceof IQTESTCourseNode) {
			AssessmentEntryStatus assessmentStatus;
			if(IQEditController.CORRECTION_MANUAL.equals(courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE))) {
				assessmentStatus = AssessmentEntryStatus.inReview;
			} else {
				assessmentStatus = AssessmentEntryStatus.done;
			}
			ScoreEvaluation sceval = new ScoreEvaluation(score, pass, assessmentStatus, Boolean.TRUE, assessmentId);
			
			boolean increment = incrementAttempts.getAndSet(false);
			((IQTESTCourseNode)courseNode).updateUserScoreEvaluation(sceval, userCourseEnv, getIdentity(), increment);
			if(increment) {
				ThreadLocalUserActivityLogger.log(QTI21LoggingAction.QTI_CLOSE_IN_COURSE, getClass());
			}
		} else if(courseNode instanceof SelfAssessableCourseNode) {
			boolean increment = incrementAttempts.getAndSet(false);
			if(increment) {
				((SelfAssessableCourseNode)courseNode).incrementUserAttempts(userCourseEnv);
			}
		}
	}
}
