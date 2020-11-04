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
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olat.core.CoreSpringFactory;
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
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.resource.WindowedResourceableList;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseModule;
import org.olat.course.DisposedCourseRestartController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.manager.AssessmentNotificationsHandler;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.SelfAssessableCourseNode;
import org.olat.course.nodes.ms.DocumentsMapper;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.DownloadeableMediaResource;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesListener;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21LoggingAction;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.model.InMemoryOutcomeListener;
import org.olat.ims.qti21.ui.AssessmentResultController;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.QTI21Event;
import org.olat.ims.qti21.ui.QTI21OverrideOptions;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.event.CompletionEvent;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentRunController extends BasicController implements GenericEventListener, OutcomesListener {
	
	private static final OLATResourceable assessmentEventOres = OresHelper.createOLATResourceableType(AssessmentEvent.class);
	private static final OLATResourceable assessmentInstanceOres = OresHelper.createOLATResourceableType(AssessmentInstance.class);
	
	private Link startButton;
	private Link showResultsButton;
	private Link hideResultsButton;
	private Link signatureDownloadLink;
	private final VelocityContainer mainVC;
	private final VelocityContainer disclaimerVC;
	
	private boolean assessmentStopped = true;
	
	private EventBus singleUserEventCenter;
	private final boolean anonym;
	private final UserSession userSession;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;
	private final QTICourseNode courseNode;
	private final RepositoryEntry testEntry;
	private final QTI21DeliveryOptions deliveryOptions;
	private final QTI21OverrideOptions overrideOptions;
	// The test is really assessment not a self test or a survey
	private final boolean assessmentType = true;
	private final WindowedResourceableList resourceList;
	private AtomicBoolean incrementAttempts = new AtomicBoolean(true);
	
	private AssessmentResultController resultCtrl;
	private AssessmentTestDisplayController displayCtrl;
	private QTI21AssessmentMainLayoutController displayContainerController;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private CourseModule courseModule;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentNotificationsHandler assessmentNotificationsHandler;
	
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
		mainVC.setDomReplaceable(false); // DOM ID set in velocity
		
		resourceList = userSession.getResourceList();
		if(!resourceList.registerResourceable(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				courseNode.getIdent(), getWindow())) {
			showWarning("warning.multi.window");
		}
		
		disclaimerVC = createVelocityContainer("assessment_disclaimer");
		disclaimerVC.setDomReplacementWrapperRequired(false);
		mainVC.put("disclaimer", disclaimerVC);
						
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));
		
		mainVC.contextPut("infobox", courseModule.isDisplayInfoBox());
		mainVC.contextPut("changelogconfig", courseModule.isDisplayChangeLog());
		
		if(courseNode instanceof IQTESTCourseNode) {
			mainVC.contextPut("type", "test");
		} else if(courseNode instanceof IQSELFCourseNode) {
			mainVC.contextPut("type", "self");
		}
		
		deliveryOptions = getDeliveryOptions();
		overrideOptions = getOverrideOptions();
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
		String sDisclaimer = config.getStringValue(IQEditController.CONFIG_KEY_DISCLAIMER);
		if (sDisclaimer != null) {
			int lastSlash = sDisclaimer.lastIndexOf('/');
			if (lastSlash != -1) {
				VFSContainer baseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
				baseContainer = (VFSContainer)baseContainer.resolve(sDisclaimer.substring(0, lastSlash));
				sDisclaimer = sDisclaimer.substring(lastSlash);
				// first check if disclaimer exists on filesystem
				if (baseContainer == null || baseContainer.resolve(sDisclaimer) == null) {
					showWarning("disclaimer.file.invalid", sDisclaimer);
				} else {
					//screenreader do not like iframes, display inline
					IFrameDisplayController iFrameCtr = new IFrameDisplayController(ureq, getWindowControl(), baseContainer);
					listenTo(iFrameCtr);//dispose automatically
					disclaimerVC.put("disc", iFrameCtr.getInitialComponent());
					iFrameCtr.setCurrentURI(sDisclaimer);
					disclaimerVC.contextPut("hasDisc", Boolean.TRUE);
					disclaimerVC.contextPut("indisclaimer", isPanelOpen(ureq, "disclaimer", true));
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
		mainVC.contextPut("enableScoreInfo", Boolean.valueOf(enableScoreInfo));
	   
	    // configuration data
		int maxAttempts = deliveryOptions.getMaxAttempts();
		if(maxAttempts > 0) {
			mainVC.contextPut("attemptsConfig", Integer.valueOf(maxAttempts));
		} else {
			mainVC.contextPut("attemptsConfig", Boolean.FALSE);
		}
		// configure date period
		mainVC.contextPut("blockDate", Boolean.valueOf(blockedBasedOnDate()));
		// time limit
		Long timeLimit = getAssessmentTestMaxTimeLimit();
		if(timeLimit != null) {
			mainVC.contextPut("timeLimit", Formatter.formatHourAndSeconds(timeLimit.longValue() * 1000l));
		}

		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		if (assessmentConfig.isAssessable()) {
			if (Mode.none != assessmentConfig.getScoreMode() || userCourseEnv.isCoach()){
				HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, getWindowControl(), userCourseEnv, courseNode);
				if (highScoreCtr.isViewHighscore()) {
					Component highScoreComponent = highScoreCtr.getInitialComponent();
					mainVC.put("highScore", highScoreComponent);
				}
			}
		}
		
	    // user data
		if (courseNode instanceof SelfAssessableCourseNode) {
			SelfAssessableCourseNode acn = (SelfAssessableCourseNode)courseNode; 
			ScoreEvaluation scoreEval = acn.getUserScoreEvaluation(userCourseEnv);
			Integer attempts = acn.getUserAttempts(null, userCourseEnv);
			if (scoreEval != null) {
				mainVC.contextPut("resultsVisible", Boolean.TRUE);
				mainVC.contextPut("hasResults", Boolean.TRUE);
				mainVC.contextPut("score", AssessmentHelper.getRoundedScore(scoreEval.getScore()));
				mainVC.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
				mainVC.contextPut("passed", scoreEval.getPassed());
				mainVC.contextPut("attempts", attempts); //at least one attempt
				mainVC.contextPut("showChangeLog", Boolean.TRUE && enableScoreInfo);
				exposeResults(ureq, true, scoreEval.getPassed() != null && scoreEval.getPassed().booleanValue());
			} else {
				exposeResults(ureq, false, false);
			}
		} else if(courseNode instanceof IQTESTCourseNode) {
			IQTESTCourseNode testCourseNode = (IQTESTCourseNode)courseNode;
			AssessmentEntry assessmentEntry = userCourseEnv.isParticipant()
					? courseAssessmentService.getAssessmentEntry(testCourseNode, userCourseEnv)
					: null;
			if(assessmentEntry == null) {
				mainVC.contextPut("blockAfterSuccess", Boolean.FALSE);
				mainVC.contextPut("score", null);
				mainVC.contextPut("hasPassedValue", Boolean.FALSE);
				mainVC.contextPut("passed", Boolean.FALSE);
				mainVC.contextPut("comment", null);
				mainVC.contextPut("attempts", 0);
				mainVC.contextPut("showChangeLog", Boolean.FALSE);
				if (!userCourseEnv.isParticipant()) {
					mainVC.contextPut("enableScoreInfo", Boolean.FALSE);
				}
			} else {
				boolean passed = assessmentEntry.getPassed() != null && assessmentEntry.getPassed().booleanValue();
				//block if test passed (and config set to check it)
				Boolean blocked = Boolean.FALSE;
				boolean blockAfterSuccess = deliveryOptions.isBlockAfterSuccess();
				if(blockAfterSuccess && passed) {
					blocked = Boolean.TRUE;
				}
				mainVC.contextPut("blockAfterSuccess", blocked);
				
				boolean resultsVisible = assessmentEntry.getUserVisibility() == null || assessmentEntry.getUserVisibility().booleanValue();
				mainVC.contextPut("resultsVisible", resultsVisible);
				mainVC.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
				mainVC.contextPut("hasPassedValue", (assessmentEntry.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
				mainVC.contextPut("passed", passed);
				if(resultsVisible) {
					if(assessmentConfig.hasComment()) {
						StringBuilder comment = Formatter.stripTabsAndReturns(
								courseAssessmentService.getUserComment(testCourseNode, userCourseEnv));
						if (comment != null && comment.length() > 0) {
							mainVC.contextPut("comment", StringHelper.xssScan(comment));
							mainVC.contextPut("incomment", isPanelOpen(ureq, "comment", true));
						}
					}
					
					if(assessmentConfig.hasIndividualAsssessmentDocuments()) {
						List<File> docs = courseAssessmentService.getIndividualAssessmentDocuments(testCourseNode,
								userCourseEnv);
						String mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(docs));
						mainVC.contextPut("docsMapperUri", mapperUri);
						mainVC.contextPut("docs", docs);
						mainVC.contextPut("inassessmentDocuments", isPanelOpen(ureq, "assessmentDocuments", true));
					}
				}
				Integer attempts = assessmentEntry.getAttempts();
				mainVC.contextPut("attempts", attempts == null ? Integer.valueOf(0) : attempts);
				boolean showChangelog = (!anonym && enableScoreInfo && resultsVisible && isResultVisible(config));
				mainVC.contextPut("showChangeLog", showChangelog);
				
				if(deliveryOptions.isDigitalSignature()) {
					AssessmentTestSession session = qtiService.getAssessmentTestSession(assessmentEntry.getAssessmentId());
					if(session != null) {
						File signature = qtiService.getAssessmentResultSignature(session);
						if(signature != null && signature.exists()) {
							signatureDownloadLink = LinkFactory.createLink("digital.signature.download.link", mainVC, this);
							signatureDownloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
							signatureDownloadLink.setTarget("_blank");
							
							Date issueDate = qtiService.getAssessmentResultSignatureIssueDate(session);
							if(issueDate != null) {
								mainVC.contextPut("signatureIssueDate", Formatter.getInstance(getLocale()).formatDateAndTime(issueDate));
							}
						}
					}
				}

				exposeResults(ureq, resultsVisible, passed);
			}
		}
	}
	
	private boolean blockedBasedOnDate() {
		mainVC.contextRemove("startTestDate");
		mainVC.contextRemove("endTestDate");
		
		boolean dependOnDate = config.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, false);
		if(dependOnDate) {
			Date startTestDate = config.getDateValue(IQEditController.CONFIG_KEY_START_TEST_DATE);
			if(startTestDate != null) {
				Formatter formatter = Formatter.getInstance(getLocale());
				mainVC.contextPut("startTestDate", formatter.formatDateAndTime(startTestDate));
				
				Date endTestDate = config.getDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE);
				if(endTestDate != null) {
					mainVC.contextPut("endTestDate", formatter.formatDateAndTime(endTestDate));
				}
				Date now = new Date();
				if(startTestDate.after(now)) {
					return true;
				}
	
				if(endTestDate != null && endTestDate.before(now)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void checkChats(UserRequest ureq) {
		List<?> allChats = null;
		if (ureq != null) {
			allChats = ureq.getUserSession().getChats();
		}
		if (allChats == null || allChats.isEmpty()) {
			startButton.setEnabled (true);
			mainVC.contextPut("hasChatWindowOpen", false);
		} else {
			startButton.setEnabled (false);
			mainVC.contextPut("hasChatWindowOpen", true);
		}
	}
	
	/**
	 * WARNING! The variables  and are not used 
	 * in the velocity template and the CONFIG_KEY_RESULT_ON_HOME_PAGE is not editable
	 * in the configuration of the course element for QTI 2.1!!!!
	 * 
	 * Provides the show results button if results available or a message with the visibility period 
	 * if there is difference when displaying passed or failed results.
	 * 
	 * @param ureq
	 */
	private void exposeResults(UserRequest ureq, boolean resultsAvailable, boolean passed) {
		//migration: check if old tests have no summary configured
		boolean showResultsOnHomePage = config.getBooleanSafe(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
		QTI21AssessmentResultsOptions showSummary = deliveryOptions.getAssessmentResultsOptions();
		if(resultsAvailable && !showSummary.none()) {
			mainVC.contextPut("showResultsOnHomePage", Boolean.valueOf(showResultsOnHomePage));			
			boolean dateRelatedVisibility = isResultVisible(config);		
			if(showResultsOnHomePage && dateRelatedVisibility) {
				mainVC.contextPut("showResultsVisible",Boolean.TRUE);
				showResultsButton = LinkFactory.createLink("command.showResults", "command.showResults", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
				showResultsButton.setCustomDisplayText(translate("showResults.title"));
				showResultsButton.setElementCssClass("o_qti_show_assessment_results");
				showResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_open_togglebox");
				
				hideResultsButton = LinkFactory.createLink("command.hideResults", "command.hideResults", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
				hideResultsButton.setCustomDisplayText(translate("showResults.title"));
				hideResultsButton.setElementCssClass("o_qti_hide_assessment_results");
				hideResultsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_close_togglebox");
				if(isPanelOpen(ureq, "results", true)) {
					doShowResults(ureq);
				}
			} else {
				exposeVisiblityPeriod(passed);
				mainVC.contextPut("showResultsVisible", Boolean.FALSE);
			}
		} else {
			exposeVisiblityPeriod(passed);
			mainVC.contextPut("showResultsVisible", Boolean.FALSE);
			mainVC.contextPut("showResultsOnHomePage", Boolean.valueOf(showResultsOnHomePage && !showSummary.none()));	
		}
		
		if(!anonym && resultsAvailable && userCourseEnv.isParticipant()) {
			UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
			String userLog = am.getUserNodeLog(courseNode, getIdentity());
			mainVC.contextPut("log", StringHelper.escapeHtml(userLog));	
		}
	}
	
	private void exposeVisiblityPeriod(boolean passed) {
		String showResultsActive = config.getStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS);
		
		if (showResultsActive != null) {
			Date startDate;
			Date endDate;
			Date currentDate = new Date();

			switch (showResultsActive) {
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS:
				mainVC.contextPut("visibilityPeriod", translate("showResults.visibility.future"));
				break;
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT:
				if (passed) {
					startDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
					endDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);

					if(startDate != null && currentDate.before(startDate)) {
						Formatter formatter = Formatter.getInstance(getLocale());
						String visibilityStartDate = formatter.formatDateAndTime(startDate);
						String visibilityEndDate = "-";
						if(endDate != null && currentDate.before(endDate)) {
							visibilityEndDate = formatter.formatDateAndTime(endDate);
						} else if(endDate != null && currentDate.after(endDate)) {
							String visibilityPeriod = translate("showResults.visibility.past");
							mainVC.contextPut("visibilityPeriod", visibilityPeriod);
							break;
						}
						String visibilityPeriod = translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate });
						mainVC.contextPut("visibilityPeriod", visibilityPeriod);
						break;
					}
				} else {
					startDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
					endDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);

					if(startDate != null && currentDate.before(startDate)) {
						Formatter formatter = Formatter.getInstance(getLocale());
						String visibilityStartDate = formatter.formatDateAndTime(startDate);
						String visibilityEndDate = "-";
						if(endDate != null && currentDate.before(endDate)) {
							visibilityEndDate = formatter.formatDateAndTime(endDate);
						} else if(endDate != null && currentDate.after(endDate)) {
							String visibilityPeriod = translate("showResults.visibility.past");
							mainVC.contextPut("visibilityPeriod", visibilityPeriod);
							break;
						}
						String visibilityPeriod = translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate });
						mainVC.contextPut("visibilityPeriod", visibilityPeriod);
						break;
					}
				}
				mainVC.contextPut("visibilityPeriod", translate("showResults.visibility.future"));
				break;
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY:
				if (!passed) {
					startDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
					endDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);

					if(startDate != null && currentDate.before(startDate)) {
						Formatter formatter = Formatter.getInstance(getLocale());
						String visibilityStartDate = formatter.formatDateAndTime(startDate);
						String visibilityEndDate = "-";
						if(endDate != null && currentDate.before(endDate)) {
							visibilityEndDate = formatter.formatDateAndTime(endDate);
						} else if(endDate != null && currentDate.after(endDate)) {
							String visibilityPeriod = translate("showResults.visibility.past");
							mainVC.contextPut("visibilityPeriod", visibilityPeriod);
							break;
						}
						String visibilityPeriod = translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate });
						mainVC.contextPut("visibilityPeriod", visibilityPeriod);
						break;
					}
				} else {
					mainVC.contextPut("showResultsOnHomePage", Boolean.valueOf(false));
					break;
				}
				mainVC.contextPut("visibilityPeriod", translate("showResults.visibility.future"));
				break;
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY:
				if (passed) {
					startDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
					endDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);

					if(startDate != null && currentDate.before(startDate)) {
						Formatter formatter = Formatter.getInstance(getLocale());
						String visibilityStartDate = formatter.formatDateAndTime(startDate);
						String visibilityEndDate = "-";
						if(endDate != null && currentDate.before(endDate)) {
							visibilityEndDate = formatter.formatDateAndTime(endDate);
						} else if(endDate != null && currentDate.after(endDate)) {
							String visibilityPeriod = translate("showResults.visibility.past");
							mainVC.contextPut("visibilityPeriod", visibilityPeriod);
							break;
						}
						String visibilityPeriod = translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate });
						mainVC.contextPut("visibilityPeriod", visibilityPeriod);
						break;
					}
				} else {
					mainVC.contextPut("showResultsOnHomePage", Boolean.valueOf(false));
					break;
				}
				mainVC.contextPut("visibilityPeriod", translate("showResults.visibility.future"));
				break;
			case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME:
				startDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
				endDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);

				if(startDate != null && currentDate.before(startDate)) {
					Formatter formatter = Formatter.getInstance(getLocale());
					String visibilityStartDate = formatter.formatDateAndTime(startDate);
					String visibilityEndDate = "-";
					if(endDate != null && currentDate.before(endDate)) {
						visibilityEndDate = formatter.formatDateAndTime(endDate);
					} else if(endDate != null && currentDate.after(endDate)) {
						String visibilityPeriod = translate("showResults.visibility.past");
						mainVC.contextPut("visibilityPeriod", visibilityPeriod);
						break;
					}
					String visibilityPeriod = translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate });
					mainVC.contextPut("visibilityPeriod", visibilityPeriod);
					break;
				}
				mainVC.contextPut("visibilityPeriod", translate("showResults.visibility.future"));
				break;
			default:
				mainVC.contextPut("visibilityPeriod", translate("showResults.visibility.future"));
				break;
			}
		}
	}
	
	/**
	 * Evaluates if the results are visble or not in respect of the configured CONFIG_KEY_DATE_DEPENDENT_RESULTS parameter. <br>
	 * The results are always visible if not date dependent.
	 * EndDate could be null, that is there is no restriction for the end date.
	 * 
	 * @return true if is visible.
	 */
	private boolean isResultVisible(ModuleConfiguration modConfig) {
		boolean isVisible = false;
		String showResultsActive = modConfig.getStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS);
		Date startDate, endDate;
		Date passedStartDate, passedEndDate;
		Date failedStartDate, failedEndDate;
		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
		
		switch (showResultsActive) {
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS:
			isVisible = true;
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT:
			passedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
			passedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
			failedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			failedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			
			isVisible = isResultVisible(scoreEval, passedStartDate, passedEndDate, failedStartDate, failedEndDate);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY:
			failedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			failedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			
			isVisible = isResultVisibleFailedOnly(scoreEval, failedStartDate, failedEndDate);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY:
			passedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			passedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			
			isVisible = isResultVisibleFailedOnly(scoreEval, passedStartDate, passedEndDate);
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME:
			startDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			endDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			isVisible = isResultVisible(startDate, endDate);
			break;
		default:
			break;
		}

		return isVisible;
	}
	
	private boolean isResultVisible(Date startDate, Date endDate) {
		boolean isVisible = true;
		Date currentDate = new Date();
		
		if (startDate != null && currentDate.before(startDate)) {
			isVisible &= false;
		} 
		
		if (endDate != null && currentDate.after(endDate)) {
			isVisible &= false;
		}
		
		return isVisible;
	}
	
	private boolean isResultVisibleFailedOnly(ScoreEvaluation scoreEval, Date startDate, Date endDate) {
		boolean isVisible = scoreEval != null;
		
		if (isVisible) {			
			if (scoreEval.getPassed() != null && !scoreEval.getPassed().booleanValue()) {
				isVisible &= isResultVisible(startDate, endDate);
			} else {
				isVisible &= false;
			}
		}
		
		return isVisible;
	}
	
	private boolean isResultVisible(ScoreEvaluation scoreEval, Date passedStartDate, Date passedEndDate, Date failedStartDate, Date failedEndDate) {
		boolean isVisible = scoreEval != null;
		
		if (isVisible) {
			if (scoreEval.getPassed() != null && scoreEval.getPassed().booleanValue()) {
				isVisible &= isResultVisible(passedStartDate, passedEndDate);
			} else {
				isVisible &= isResultVisible(failedStartDate, failedEndDate);
			}
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
		
		resourceList.deregisterResourceable(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				courseNode.getIdent(), getWindow());
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
			doHideResults(ureq);
		} else if (source == signatureDownloadLink) {
			doDownloadSignature(ureq);
		} else if("show".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), true);
		} else if("hide".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), false);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == displayCtrl) {
			if(event == Event.CANCELLED_EVENT) {
				doCancelAssessment(ureq);
				initAssessment(ureq);
				showInfo("assessment.test.cancelled");
			} else if("suspend".equals(event.getCommand())) {
				doExitAssessment(ureq, event, false);
				initAssessment(ureq);
				showInfo("assessment.test.suspended");
			} else if(event instanceof QTI21Event) {
				QTI21Event qe = (QTI21Event)event;
				if(QTI21Event.EXIT.equals(qe.getCommand())) {
					if(!displayCtrl.isResultsVisible()) {
						doExitAssessment(ureq, event, true);
						initAssessment(ureq);
						fireEvent(ureq, Event.CHANGED_EVENT);
					}
				} else if(QTI21Event.CLOSE_RESULTS.equals(qe.getCommand())) {
					doExitAssessment(ureq, event, true);
					initAssessment(ureq);
					fireEvent(ureq, Event.CHANGED_EVENT);
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
			AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnv);
			if(userCourseEnv.isParticipant() && assessmentEntry.getAssessmentId() != null) {
				session = qtiService.getAssessmentTestSession(assessmentEntry.getAssessmentId());
			} else {
				RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				session = qtiService.getLastAssessmentTestSessions(courseEntry, courseNode.getIdent(), testEntry, getIdentity());
			}
		}
		
		if(session == null) {
			mainVC.contextPut("showResults", Boolean.FALSE);
		} else {
			FileResourceManager frm = FileResourceManager.getInstance();
			File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
			URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
			File submissionDir = qtiService.getSubmissionDirectory(session);
			String mapperUri = registerCacheableMapper(ureq, "QTI21CNResults::" + session.getTestEntry().getKey(),
					new ResourcesMapper(assessmentObjectUri, submissionDir));

			resultCtrl = new AssessmentResultController(ureq, getWindowControl(), getIdentity(), true, session,
					fUnzippedDirRoot, mapperUri, null, getDeliveryOptions().getAssessmentResultsOptions(), false, false, true);
			listenTo(resultCtrl);
			mainVC.put("resultReport", resultCtrl.getInitialComponent());
			mainVC.contextPut("showResults", Boolean.TRUE);
		}
		saveOpenPanel(ureq, "results", Boolean.TRUE);
	}

	private void doHideResults(UserRequest ureq) {
		mainVC.contextPut("showResults", Boolean.FALSE);
		saveOpenPanel(ureq, "results", Boolean.FALSE);
	}
	
	private boolean isPanelOpen(UserRequest ureq, String panelId, boolean def) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(QTI21AssessmentRunController.class, getOpenPanelId(panelId));
		return showConfig == null ? def : showConfig.booleanValue();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.commit(QTI21AssessmentRunController.class, getOpenPanelId(panelId), Boolean.valueOf(newValue));
		}
		mainVC.getContext().put("in-".concat(panelId), Boolean.valueOf(newValue));
	}
	
	private String getOpenPanelId(String panelId) {
		return panelId + "::" + userCourseEnv.getCourseEnvironment().getCourseResourceableId() + "::" + courseNode.getIdent();
	}
	
	private void doDownloadSignature(UserRequest ureq) {
		MediaResource resource = null;
		if(courseNode instanceof IQTESTCourseNode) {
			AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnv);
			AssessmentTestSession session = qtiService.getAssessmentTestSession(assessmentEntry.getAssessmentId());
			File signature = qtiService.getAssessmentResultSignature(session);
			if(signature.exists()) {
				resource = new DownloadeableMediaResource(signature);
			}
		}
		if(resource == null) {
			resource = new NotFoundMediaResource();
		}
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	private void doStart(UserRequest ureq) {
		removeAsListenerAndDispose(displayCtrl);

		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck("test");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		RepositoryEntry courseRe = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if (!userCourseEnv.isParticipant() && courseNode instanceof IQTESTCourseNode) {
			boolean authorMode = !ureq.getUserSession().getRoles().isGuestOnly();
			displayCtrl = new AssessmentTestDisplayController(ureq, getWindowControl(), new InMemoryOutcomeListener(),
					testEntry, courseRe, courseNode.getIdent(), deliveryOptions, overrideOptions, true, authorMode, true);
		} else {
			displayCtrl = new AssessmentTestDisplayController(ureq, bwControl, this, testEntry, courseRe,
					courseNode.getIdent(), deliveryOptions, overrideOptions, true, false, false);
		}
		listenTo(displayCtrl);
		if(displayCtrl.getAssessmentTest() == null) {
			logError("Test cannot be read: " + testEntry + " in course: " + courseRe + " element: " + courseNode.getIdent() , null);
			showError("error.resource.corrupted");
		} else if(displayCtrl.isEnded()) {
			if(!displayCtrl.isResultsVisible()) {
				doExitAssessment(ureq, null, true);
				initAssessment(ureq);
			}
		} else {
			// in case displayController was unable to initialize, a message was set by displayController
			// this is the case if no more attempts or security check was unsuccessfull

			displayContainerController = new QTI21AssessmentMainLayoutController(ureq, getWindowControl(), displayCtrl);
			listenTo(displayContainerController); // autodispose


			Panel empty = new Panel("empty");//empty panel set as "menu" and "tool"
			Controller courseCloser = new DisposedCourseRestartController(ureq, getWindowControl(), courseRe);
			Controller disposedRestartController = new LayoutMain3ColsController(ureq, getWindowControl(), empty, courseCloser.getInitialComponent(), "disposed");
			displayContainerController.setDisposedMessageController(disposedRestartController);
			
			boolean fullWindow = deliveryOptions.isHideLms();
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
	
	/**
	 * @return The maximum time limit in seconds.
	 */
	private Long getAssessmentTestMaxTimeLimit() {
		if(overrideOptions != null && overrideOptions.getAssessmentTestMaxTimeLimit() != null) {
			Long timeLimits = overrideOptions.getAssessmentTestMaxTimeLimit();
			return timeLimits.longValue() > 0 ? timeLimits.longValue() : null;
		}
		
		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		if(assessmentTest != null && assessmentTest.getTimeLimits() != null && assessmentTest.getTimeLimits().getMaximum() != null) {
			return assessmentTest.getTimeLimits().getMaximum().longValue();
		}
		return null;
	}
	
	private QTI21DeliveryOptions getDeliveryOptions() {
		QTI21DeliveryOptions testOptions = qtiService.getDeliveryOptions(testEntry);
		QTI21DeliveryOptions finalOptions = testOptions.clone();
		boolean configRef = config.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIG_REF, false);
		if(!configRef) {
			finalOptions.setMaxAttempts(config.getIntegerSafe(IQEditController.CONFIG_KEY_ATTEMPTS, testOptions.getMaxAttempts()));
			finalOptions.setBlockAfterSuccess(config.getBooleanSafe(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS, testOptions.isBlockAfterSuccess()));
			finalOptions.setHideLms(config.getBooleanSafe(IQEditController.CONFIG_FULLWINDOW, testOptions.isHideLms()));
			finalOptions.setShowTitles(config.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTIONTITLE, testOptions.isShowTitles()));
			finalOptions.setPersonalNotes(config.getBooleanSafe(IQEditController.CONFIG_KEY_MEMO, testOptions.isPersonalNotes()));
			finalOptions.setEnableCancel(config.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLECANCEL, testOptions.isEnableCancel()));
			finalOptions.setEnableSuspend(config.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLESUSPEND, testOptions.isEnableSuspend()));
			finalOptions.setDisplayQuestionProgress(config.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTIONPROGRESS, testOptions.isDisplayQuestionProgress()));
			finalOptions.setDisplayMaxScoreItem(config.getBooleanSafe(IQEditController.CONFIG_KEY_QUESTION_MAX_SCORE, testOptions.isDisplayMaxScoreItem()));
			finalOptions.setDisplayScoreProgress(config.getBooleanSafe(IQEditController.CONFIG_KEY_SCOREPROGRESS, testOptions.isDisplayScoreProgress()));
			finalOptions.setHideFeedbacks(config.getBooleanSafe(IQEditController.CONFIG_KEY_HIDE_FEEDBACKS, testOptions.isHideFeedbacks()));
			finalOptions.setAssessmentResultsOptions(QTI21AssessmentResultsOptions.parseString(config.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT)));
			finalOptions.setShowMenu(config.getBooleanSafe(IQEditController.CONFIG_KEY_ENABLEMENU, testOptions.isShowMenu()));
			finalOptions.setAllowAnonym(config.getBooleanSafe(IQEditController.CONFIG_ALLOW_ANONYM, testOptions.isAllowAnonym()));
			finalOptions.setDigitalSignature(config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE, testOptions.isDigitalSignature()));
			finalOptions.setDigitalSignatureMail(config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL, testOptions.isDigitalSignatureMail()));
		}
		
		
		if(!AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT.equals(config.getStringValue(IQEditController.CONFIG_KEY_SUMMARY))) {
			//if this setting is set, override the summary
			finalOptions.setAssessmentResultsOptions(QTI21AssessmentResultsOptions.parseString(config.getStringValue(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT)));
		}
		Boolean assessmentResultOnFinish = config.getBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_FINISH);
		if(assessmentResultOnFinish != null) {
			finalOptions.setShowAssessmentResultsOnFinish(assessmentResultOnFinish.booleanValue());
		} else if(finalOptions.getAssessmentResultsOptions() != null
				&& !finalOptions.getAssessmentResultsOptions().none()) {
			finalOptions.setShowAssessmentResultsOnFinish(true);
		}
		return finalOptions;
	}
	
	private QTI21OverrideOptions getOverrideOptions() {
		boolean configRef = config.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIG_REF, false);
		Long maxTimeLimit = null;
		if(!configRef) {
			int timeLimit = config.getIntegerSafe(IQEditController.CONFIG_KEY_TIME_LIMIT, -1);
			if(timeLimit > 0) {
				maxTimeLimit = new Long(timeLimit);
			}
		}
		
		Date startTestDate = null;
		Date endTestDate = null;
		boolean dependOnDate = config.getBooleanSafe(IQEditController.CONFIG_KEY_DATE_DEPENDENT_TEST, false);
		if(dependOnDate) {
			startTestDate = config.getDateValue(IQEditController.CONFIG_KEY_START_TEST_DATE);
			if(startTestDate != null) {
				endTestDate = config.getDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE);
			}
		}
		
		return new QTI21OverrideOptions(maxTimeLimit, startTestDate, endTestDate);
	}
	
	private void doCancelAssessment(UserRequest ureq) {
		if(displayContainerController != null) {
			displayContainerController.deactivate(ureq);
		} else {
			getWindowControl().pop();
		}	
		
		removeHistory(ureq);
		if(userCourseEnv.isParticipant() && courseNode instanceof IQTESTCourseNode) {
			courseAssessmentService.updateCurrentCompletion(courseNode, userCourseEnv, null, null, null, Role.user);
		}
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("test", -1l);
		addToHistory(ureq, ores, null);
	}

	/**
	 * Remove the runtime from the GUI stack only.
	 * @param ureq
	 * @param event The event which triggered the method (optional)
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
		
		if(event != null) {
			fireEvent(ureq, event);
		}
	}
	
	@Override
	public void decorateConfirmation(AssessmentTestSession candidateSession, DigitalSignatureOptions options, Date timestamp, Locale locale) {
		decorateCourseConfirmation(candidateSession, options, userCourseEnv.getCourseEnvironment(), courseNode, testEntry, timestamp, locale);
	}
	
	public static void decorateCourseConfirmation(AssessmentTestSession candidateSession, DigitalSignatureOptions options,
			CourseEnvironment courseEnv, CourseNode courseNode, RepositoryEntry testEntry, Date timestamp, Locale locale)  {
		MailBundle bundle = new MailBundle();
		bundle.setToId(candidateSession.getIdentity());
		Identity assessedIdentity = candidateSession.getIdentity();
		String fullname = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(assessedIdentity);
		Date assessedDate = candidateSession.getFinishTime() == null ? timestamp : candidateSession.getFinishTime();

		String[] args = new String[] {
				courseEnv.getCourseTitle(),						// 0
				courseEnv.getCourseResourceableId().toString(),	// 1
				courseNode.getShortTitle(),						// 2
				courseNode.getIdent(),							// 3
				testEntry.getDisplayname(),						// 4
				fullname,										// 5
				Formatter.getInstance(locale)
					.formatDateAndTime(assessedDate), 			// 6
				assessedIdentity.getName(),						// 7
				assessedIdentity.getUser()
					.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, locale),// 8
				assessedIdentity.getUser()
					.getProperty(UserConstants.INSTITUTIONALNAME, locale),			// 9
		};

		Translator translator = Util.createPackageTranslator(QTI21AssessmentRunController.class, locale);
		String subject = translator.translate("digital.signature.mail.subject", args);
		String body = translator.translate("digital.signature.mail.body", args);
		bundle.setContent(subject, body);
		options.setMailBundle(bundle);
		options.setSubIdentName(courseNode.getShortTitle());
	}

	@Override
	public void updateOutcomes(Float score, Boolean pass, Date start, Double completion) {
		if(courseNode instanceof IQTESTCourseNode) {
			courseAssessmentService.updateCurrentCompletion(courseNode, userCourseEnv, start, completion, AssessmentRunStatus.running,
					Role.user);
			coordinatorManager.getCoordinator().getEventBus()
				.fireEventToListenersOf(new CompletionEvent(CompletionEvent.PROGRESS, courseNode.getIdent(),
						start, completion, AssessmentRunStatus.running, getIdentity().getKey()),
						userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseResource());
		}
	}

	@Override
	public void submit(Float score, Boolean pass, Date start, Double completion, Long assessmentId) {
		if(anonym) {
			assessmentNotificationsHandler.markPublisherNews(getIdentity(), userCourseEnv.getCourseEnvironment().getCourseResourceableId());
			return;
		}
		
		if(courseNode instanceof IQTESTCourseNode) {
			Boolean visibility;
			AssessmentEntryStatus assessmentStatus;
			String correctionMode = courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
			if(IQEditController.CORRECTION_MANUAL.equals(correctionMode) || IQEditController.CORRECTION_GRADING.equals(correctionMode)) {
				assessmentStatus = AssessmentEntryStatus.inReview;
				visibility = Boolean.FALSE;
			} else {
				assessmentStatus = AssessmentEntryStatus.done;
				visibility = Boolean.TRUE;
			}
			ScoreEvaluation sceval = new ScoreEvaluation(score, pass, assessmentStatus, visibility, start, completion,
					AssessmentRunStatus.done, assessmentId);
			
			boolean increment = incrementAttempts.getAndSet(false);
			courseAssessmentService.updateScoreEvaluation(courseNode, sceval, userCourseEnv, getIdentity(),
					increment, Role.user);
			if(increment) {
				ThreadLocalUserActivityLogger.log(QTI21LoggingAction.QTI_CLOSE_IN_COURSE, getClass());
			}
			coordinatorManager.getCoordinator().getEventBus()
				.fireEventToListenersOf(new CompletionEvent(CompletionEvent.PROGRESS, courseNode.getIdent(),
						start, completion, AssessmentRunStatus.done, getIdentity().getKey()),
						userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseResource());
			
			if(IQEditController.CORRECTION_GRADING.equals(correctionMode)) {
				AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnv);
				gradingService.assignGrader(testEntry, assessmentEntry, new Date(), true);
			}

			assessmentNotificationsHandler.markPublisherNews(getIdentity(), userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		} else if(courseNode instanceof SelfAssessableCourseNode) {
			boolean increment = incrementAttempts.getAndSet(false);
			if(increment) {
				((SelfAssessableCourseNode)courseNode).incrementUserAttempts(null, userCourseEnv, Role.user);
			}
		}
	}
}
