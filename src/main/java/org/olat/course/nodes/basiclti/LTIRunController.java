/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.basiclti;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.util.Util;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentParticipantViewController;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIDisplayOptions;
import org.olat.ims.lti.LTIModule;
import org.olat.ims.lti.ui.LTIDisplayContentController;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.ims.lti13.ui.LTI13DisplayController;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * is the controller for displaying contents in an iframe served by Basic LTI
 * @author guido
 * @author Charles Severance
 * 
 */
public class LTIRunController extends BasicController {
	private static final String PROP_NAME_DATA_EXCHANGE_ACCEPTED = "LtiDataExchageAccepted";
	
	private Link back;
	private Link startButton;
	private final StackedPanel mainPanel;
	private VelocityContainer startPage;
	
	private boolean fullScreen;
	private ChiefController thebaseChief;
	private AssessmentParticipantViewController assessmentParticipantViewCtrl;
	
	private final LTIDisplayOptions display;
	private final BasicLTICourseNode courseNode;
	private final ModuleConfiguration config;
	private final CourseEnvironment courseEnv;
	private final UserCourseEnvironment userCourseEnv;
	private final AssessmentConfig assessmentConfig;
	
	private LTIDisplayContentController ltiCtrl;
	private LTIDataExchangeDisclaimerController disclaimerCtrl;
	
	@Autowired
	private LTIModule ltiModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	/**
	 * Constructor for the preview in the editor of the course element.
	 *  
	 * @param wControl The window control
	 * @param config The course element configuration
	 * @param ureq The user request
	 * @param ltCourseNode The course element
	 * @param userCourseEnv The user course environment of the author
	 * @param courseEnv The course environment
	 */
	public LTIRunController(WindowControl wControl, ModuleConfiguration config, UserRequest ureq, BasicLTICourseNode ltCourseNode,
			UserCourseEnvironment userCourseEnv, CourseEnvironment courseEnv) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		this.courseNode = ltCourseNode;
		this.config = config;
		this.courseEnv = courseEnv;
		this.userCourseEnv = userCourseEnv;
		display = LTIDisplayOptions.iframe;
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		
		ltiCtrl = new LTI10DisplayController(ureq, getWindowControl(), courseNode, userCourseEnv, courseEnv, display);
		listenTo(ltiCtrl);
		mainPanel = putInitialPanel(ltiCtrl.getInitialComponent());
		ltiCtrl.openLtiContent(ureq);
	}

	/**
	 * Constructor for LTI run controller
	 * 
	 * @param wControl The window control
	 * @param config The module configuration
	 * @param ureq The user request
	 * @param courseNode The current course node
	 * @param userCourseEnv The course environment
	 */
	public LTIRunController(UserRequest ureq, WindowControl wControl, BasicLTICourseNode courseNode,
			UserCourseEnvironment userCourseEnv) {
 		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.userCourseEnv = userCourseEnv;
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		String displayStr = config.getStringValue(BasicLTICourseNode.CONFIG_DISPLAY, "iframe");
		display = LTIDisplayOptions.valueOfOrDefault(displayStr); 
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		
		ltiCtrl = new LTI10DisplayController(ureq, getWindowControl(), courseNode, userCourseEnv, courseEnv, display);
		listenTo(ltiCtrl);

		mainPanel = new SimpleStackedPanel("ltiContainer");
		putInitialPanel(mainPanel);
		doRun(ureq);
	}
	
	public LTIRunController(UserRequest ureq, WindowControl wControl, BasicLTICourseNode courseNode,
			LTI13ToolDeployment deployment, UserCourseEnvironment userCourseEnv) {
 		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.userCourseEnv = userCourseEnv;
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);

		mainPanel = new SimpleStackedPanel("ltiContainer");
		putInitialPanel(mainPanel);
		if(deployment == null) {
			String title = translate("error.configuration.title");
			String text = translate("error.configuration.text");
			Controller ctrl = MessageUIFactory.createErrorMessage(ureq, getWindowControl(), title, text);
			mainPanel.setContent(ctrl.getInitialComponent());
			display = LTIDisplayOptions.iframe;
		} else {
			String displayStr = deployment.getDisplay();
			display = LTIDisplayOptions.valueOfOrDefault(displayStr);
			
			ltiCtrl = new LTI13DisplayController(ureq, getWindowControl(), deployment, userCourseEnv);
			listenTo(ltiCtrl);
			doRun(ureq);
		}
	}

	/**
	 * Helper method to check if user has already accepted. this info is stored
	 * in a user property, the accepted values are stored as an MD5 hash (save
	 * space, privacy)
	 * 
	 * @param hash
	 *            MD5 hash with all user data
	 * @return true: user has already accepted for this hash; false: user has
	 *         not yet accepted or for other values
	 */
	private boolean checkHasDataExchangeAccepted(String hash) {
		boolean dataAccepted = false;
		CoursePropertyManager propMgr = courseEnv.getCoursePropertyManager();
		Property prop = propMgr.findCourseNodeProperty(courseNode, getIdentity(), null, PROP_NAME_DATA_EXCHANGE_ACCEPTED);
		if (prop != null) {
			// compare if value in property is the same as calculated today. If not, user as to accept again
			String storedHash = prop.getStringValue();
			if (storedHash != null && hash != null && storedHash.equals(hash)) {
				dataAccepted = true;
			} else {
				// remove property, not valid anymore
				propMgr.deleteProperty(prop);
			}
		}
		return dataAccepted;
	}

	/**
	 * Helper to initialize the ask-for-data-exchange screen
	 */
	private void doAskDataExchange() {
		mainPanel.setContent(disclaimerCtrl.getInitialComponent());
	}
	
	/**
	 * Helper to save the user accepted data exchange
	 */
	private void storeDataExchangeAcceptance() {
		String hash = disclaimerCtrl.getHashData();
		CoursePropertyManager propMgr = courseEnv.getCoursePropertyManager();
		Property prop = propMgr.createCourseNodePropertyInstance(this.courseNode, getIdentity(), null, PROP_NAME_DATA_EXCHANGE_ACCEPTED, null, null, hash, null);
		propMgr.saveProperty(prop);
	}
	
	/**
	 * Helper to initialize the LTI run view after user has accepted data exchange.
	 * @param ureq
	 */
	private void doRun(UserRequest ureq) {
		startPage = createVelocityContainer("overview");
		startPage.contextPut("menuTitle", courseNode.getShortTitle());
		startPage.contextPut("displayTitle", courseNode.getLongTitle());
		
		boolean hasScore = AssessmentConfig.Mode.none != assessmentConfig.getScoreMode();
		if (hasScore){
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, getWindowControl(), userCourseEnv, courseNode);
			if (highScoreCtr.isViewHighscore()) {
				Component highScoreComponent = highScoreCtr.getInitialComponent();
				startPage.put("highScore", highScoreComponent);							
			}
		}
		
		startButton = LinkFactory.createButton("start", startPage, this);
		startButton.setPrimary(true);

		boolean assessable = hasScore && userCourseEnv.isParticipant();
		if(hasScore && userCourseEnv.isParticipant()) {
			startPage.contextPut("isassessable", Boolean.valueOf(assessable));
			
			AssessmentEvaluation assessmentEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
			startPage.contextPut("attempts", assessmentEval.getAttempts());
			
			removeAsListenerAndDispose(assessmentParticipantViewCtrl);
			assessmentParticipantViewCtrl = new AssessmentParticipantViewController(ureq, getWindowControl(), assessmentEval, assessmentConfig, null, null);
			listenTo(assessmentParticipantViewCtrl);
			startPage.put("assessment", assessmentParticipantViewCtrl.getInitialComponent());

			mainPanel.setContent(startPage);
		}
		
		// only run when user as already accepted to data exchange or no data 
		// has to be exchanged or when it is configured to not show the accept
		// dialog
		boolean sendName = config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDNAME, false);
		boolean sendMail = config.getBooleanSafe(LTIConfigForm.CONFIG_KEY_SENDEMAIL, false);
		String customAttributes = (String)config.get(LTIConfigForm.CONFIG_KEY_CUSTOM);
		disclaimerCtrl = new LTIDataExchangeDisclaimerController(ureq, getWindowControl(), sendName, sendMail, customAttributes);
		listenTo(disclaimerCtrl);
		
		String dataExchangeHash = disclaimerCtrl.getHashData();
		Boolean skipAcceptLaunchPage = config.getBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_ACCEPT_LAUNCH_PAGE);
		if (dataExchangeHash == null || checkHasDataExchangeAccepted(dataExchangeHash)
				|| (!ltiModule.isForceLaunchPage() && skipAcceptLaunchPage != null && skipAcceptLaunchPage.booleanValue()) ) {
			Boolean skipLaunchPage = config.getBooleanEntry(BasicLTICourseNode.CONFIG_SKIP_LAUNCH_PAGE);
			if(!ltiModule.isForceLaunchPage() && skipLaunchPage != null && skipLaunchPage.booleanValue()) {
				// start the content immediately
				openBasicLTIContent(ureq);
			} else {
				// or show the start button
				mainPanel.setContent(startPage);
			}					
		} else {
			doAskDataExchange();
		}
	}
	
	private void openBasicLTIContent(UserRequest ureq) {
		updateAssessmentData();
		
		// container is "run", "runFullscreen" or "runPopup" depending in configuration
		ltiCtrl.openLtiContent(ureq);
		if (display == LTIDisplayOptions.fullscreen) {
			ChiefController cc = getWindowControl().getWindowBackOffice().getChiefController();
			if (cc != null) {
				thebaseChief = cc;
				String businessPath = getWindowControl().getBusinessControl().getAsString();
				thebaseChief.getScreenMode().setMode(Mode.full, businessPath);
			}
			fullScreen = true;
			getWindowControl().pushToMainArea(ltiCtrl.getInitialComponent());
		} else {
			mainPanel.setContent(ltiCtrl.getInitialComponent());
		}
	}
	
	private void updateAssessmentData() {
		// No preview? Data are stores as coach, author as well.
		
		courseAssessmentService.incrementAttempts(courseNode, userCourseEnv, Role.user);
		
		// Set status in Progress
		ScoreEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
		if (currentEval.getAssessmentStatus() == null
				|| currentEval.getAssessmentStatus() == AssessmentEntryStatus.notReady
				|| currentEval.getAssessmentStatus() == AssessmentEntryStatus.notStarted) {
			ScoreEvaluation scoreEval = new ScoreEvaluation(currentEval.getScore(), currentEval.getGrade(),
					currentEval.getGradeSystemIdent(), currentEval.getPerformanceClassIdent(),
					currentEval.getPassed(), AssessmentEntryStatus.inProgress,
					currentEval.getUserVisible(), currentEval.getCurrentRunStartDate(),
					currentEval.getCurrentRunCompletion(), currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
			courseAssessmentService.saveScoreEvaluation(courseNode, getIdentity(), scoreEval, userCourseEnv, false, Role.user);
		}
	}
	
	private void closeBasicLTI() {
		if (fullScreen && thebaseChief != null) {
			getWindowControl().pop();
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			thebaseChief.getScreenMode().setMode(Mode.standard, businessPath);
		}
		mainPanel.setContent(startPage);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (disclaimerCtrl == source && event == Event.DONE_EVENT) {
			storeDataExchangeAcceptance();
			doRun(ureq);
		} else if(ltiCtrl == source && event == Event.BACK_EVENT) {
			closeBasicLTI();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == startButton) {
			openBasicLTIContent(ureq);
		}  else if(source == back) {
			closeBasicLTI();
		}
	}
}