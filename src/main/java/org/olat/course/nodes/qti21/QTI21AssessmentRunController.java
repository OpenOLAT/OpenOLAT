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
package org.olat.course.nodes.qti21;

import java.text.DateFormat;
import java.util.Date;

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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.DisposedCourseRestartController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.QTI21AssessmentCourseNode;
import org.olat.course.nodes.iq.AssessmentEvent;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.ui.QTI21DisplayController;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentRunController extends BasicController implements GenericEventListener {
	
	private static final OLATResourceable assessmentEventOres = OresHelper.createOLATResourceableType(AssessmentEvent.class);
	private static final OLATResourceable assessmentInstanceOres = OresHelper.createOLATResourceableType(AssessmentInstance.class);
	
	private Link startButton;
	private Link showResultsButton, hideResultsButton;
	private final VelocityContainer mainVC;
	
	private boolean assessmentStopped = true;
	private final CourseNode courseNode;
	private EventBus singleUserEventCenter;
	private final UserSession userSession;
	private final ModuleConfiguration config;
	private final UserCourseEnvironment userCourseEnv;
	
	private QTI21DisplayController displayCtrl;
	private LayoutMain3ColsController displayContainerController;
	
	public QTI21AssessmentRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, QTI21AssessmentCourseNode courseNode) {
		super(ureq, wControl);
		
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		userSession = ureq.getUserSession();
		config = courseNode.getModuleConfiguration();
		singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		
		mainVC = createVelocityContainer("run");
		init();
		initAssessment(ureq);
		putInitialPanel(mainVC);
	}
	
	private void init() {
		startButton = LinkFactory.createButton("start", mainVC, this);
		startButton.setElementCssClass("o_sel_start_qti21assessment");
		startButton.setPrimary(true);
	}
	
	private void initAssessment(UserRequest ureq) {
	    // config : show score info
		boolean enableScoreInfo= config.getBooleanSafe(QTI21AssessmentCourseNode.CONFIG_KEY_ENABLESCOREINFO);
		mainVC.contextPut("enableScoreInfo", new Boolean(enableScoreInfo));	
	   
	    // configuration data
		mainVC.contextPut("attemptsConfig", config.get(QTI21AssessmentCourseNode.CONFIG_KEY_ATTEMPTS));
	    // user data
	    if (!(courseNode instanceof AssessableCourseNode)) {
	    	throw new AssertException("exposeUserTestDataToVC can only be called for test nodes, not for selftest or questionnaire");
	    }
	    
		AssessableCourseNode acn = (AssessableCourseNode)courseNode; // assessment nodes are assesable
		ScoreEvaluation scoreEval = acn.getUserScoreEvaluation(userCourseEnv);
		
		//block if test passed (and config set to check it)
		boolean blockAfterSuccess = config.getBooleanSafe(QTI21AssessmentCourseNode.CONFIG_KEY_BLOCK_AFTER_SUCCESS);
		Boolean blocked = Boolean.FALSE;
		if(blockAfterSuccess) {
			Boolean passed = scoreEval.getPassed();
			if(passed != null && passed.booleanValue()) {
				blocked = Boolean.TRUE;
			}
		}
		mainVC.contextPut("blockAfterSuccess", blocked );
		
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		mainVC.contextPut("score", AssessmentHelper.getRoundedScore(scoreEval.getScore()));
		mainVC.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
		mainVC.contextPut("passed", scoreEval.getPassed());
		StringBuilder comment = Formatter.stripTabsAndReturns(acn.getUserUserComment(userCourseEnv));
		mainVC.contextPut("comment", StringHelper.xssScan(comment));
		mainVC.contextPut("attempts", acn.getUserAttempts(userCourseEnv));
		
		UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
		mainVC.contextPut("log", am.getUserNodeLog(courseNode, identity));
						
		exposeResults(ureq);
	}
	
	/**
	 * Provides the show results button if results available or a message with the visibility period.
	 * @param ureq
	 */
	private void exposeResults(UserRequest ureq) {
		//migration: check if old tests have no summary configured
		String configuredSummary = config.getStringValue(IQEditController.CONFIG_KEY_SUMMARY);
		boolean noSummary = configuredSummary == null || (configuredSummary!=null && configuredSummary.equals(AssessmentInstance.QMD_ENTRY_SUMMARY_NONE));
		if(!noSummary) {
			boolean showResultsOnHomePage = config.getBooleanSafe(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);
			mainVC.contextPut("showResultsOnHomePage",new Boolean(showResultsOnHomePage));			
			boolean dateRelatedVisibility = AssessmentHelper.isResultVisible(config);		
			if(showResultsOnHomePage && dateRelatedVisibility) {
				mainVC.contextPut("showResultsVisible",Boolean.TRUE);
				showResultsButton = LinkFactory.createButton("command.showResults", mainVC, this);
				hideResultsButton = LinkFactory.createButton("command.hideResults", mainVC, this);
			} else if(showResultsOnHomePage) {
				Date startDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
				Date endDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
				String visibilityStartDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(startDate);
				String visibilityEndDate = "-";
				if(endDate != null) {
					visibilityEndDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(endDate);
				}
				String visibilityPeriod = getTranslator().translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate});
				mainVC.contextPut("visibilityPeriod", visibilityPeriod);
				mainVC.contextPut("showResultsVisible", Boolean.FALSE);
			}
		}		
	}
	
	@Override
	protected void doDispose() {
		
		singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
		singleUserEventCenter.deregisterFor(this, InstantMessagingService.TOWER_EVENT_ORES);
		
		if (!assessmentStopped) {		 
			AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
			singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
		}
	}

	@Override
	public void event(Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(startButton == source) {
			doStart(ureq);
		} else if(showResultsButton == source) {
			
		} else if(hideResultsButton == source) {
			
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == displayCtrl) {
			//do something
		}
		super.event(ureq, source, event);
	}

	private void doStart(UserRequest ureq) {
		removeAsListenerAndDispose(displayCtrl);

		OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck("test");
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);
		
		RepositoryEntry assessmentEntry = courseNode.getReferencedRepositoryEntry();
		displayCtrl = new QTI21DisplayController(ureq, bwControl, assessmentEntry);
		listenTo(displayCtrl);
		if(displayCtrl.isTerminated()) {
			//do nothing
		} else {
			// in case displayController was unable to initialize, a message was set by displayController
			// this is the case if no more attempts or security check was unsuccessfull
			displayContainerController = new LayoutMain3ColsController(ureq, getWindowControl(), displayCtrl);
			listenTo(displayContainerController); // autodispose
			
			//need to wrap a course restart controller again, because IQDisplay
			//runs on top of GUIStack
			Long courseResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			ICourse course = CourseFactory.loadCourse(courseResId);
			RepositoryEntry courseRepositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			
			Panel empty = new Panel("empty");//empty panel set as "menu" and "tool"
			Controller courseCloser = new DisposedCourseRestartController(ureq, getWindowControl(), courseRepositoryEntry);
			Controller disposedRestartController = new LayoutMain3ColsController(ureq, getWindowControl(), empty, courseCloser.getInitialComponent(), "disposed course while in assessment run " + courseResId);
			displayContainerController.setDisposedMessageController(disposedRestartController);
			
			boolean fullWindow = config.getBooleanSafe(QTI21AssessmentCourseNode.CONFIG_FULLWINDOW);
			if(fullWindow) {
				displayContainerController.setAsFullscreen(ureq);
			}
			displayContainerController.activate();
			

			assessmentStopped = false;		
			singleUserEventCenter.registerFor(this, getIdentity(), assessmentInstanceOres);
			singleUserEventCenter.fireEventToListenersOf(new AssessmentEvent(AssessmentEvent.TYPE.STARTED, ureq.getUserSession()), assessmentEventOres);						
		}
	}
}
