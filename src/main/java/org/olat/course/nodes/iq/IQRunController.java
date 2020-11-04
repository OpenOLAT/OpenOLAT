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

package org.olat.course.nodes.iq;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.DisposedCourseRestartController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.manager.AssessmentNotificationsHandler;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.nodes.SelfAssessableCourseNode;
import org.olat.course.nodes.ms.DocumentsMapper;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti.QTIChangeLogMessage;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.navigator.NavigatorDelegate;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.iq.IQDisplayController;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.modules.iq.IQSubmittedEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * Run controller for the qti test, selftest and survey course node.
 * Call assessmentStopped if test is finished, closed or at dispose (e.g. course tab gets closed).
 * 
 * Initial Date:  Oct 13, 2004
 * @author Felix Jost
 */
public class IQRunController extends BasicController implements GenericEventListener, Activateable2, NavigatorDelegate {

	private VelocityContainer myContent;
	
	private IQSecurityCallback secCallback;
	private ModuleConfiguration modConfig;
	
	private LayoutMain3ColsController displayContainerController;
	private IQDisplayController displayController;
	private CourseNode courseNode;
	private String type;
	private UserCourseEnvironment userCourseEnv;
	private Link startButton;
	private Link showResultsButton;
	private Link hideResultsButton;

	private IFrameDisplayController iFrameCtr;

	private StackedPanel mainPanel;
	
	private boolean assessmentStopped = true; //default: true
		
	private UserSession userSession;
	private final EventBus singleUserEventCenter;
	private final OLATResourceable assessmentEventOres = OresHelper.createOLATResourceableType(AssessmentEvent.class);
	private final OLATResourceable assessmentInstanceOres = OresHelper.createOLATResourceableType(AssessmentInstance.class);
	
	private final RepositoryEntry referenceTestEntry; 
	
	@Autowired
	private IQManager iqManager;
	@Autowired
	private AssessmentNotificationsHandler assessmentNotificationsHandler;
	@Autowired
	private CourseModule courseModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	
	/**
	 * Constructor for a test run controller
	 * @param userCourseEnv
	 * @param moduleConfiguration
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @param testCourseNode
	 */
	public IQRunController(UserCourseEnvironment userCourseEnv, ModuleConfiguration moduleConfiguration,
			IQSecurityCallback secCallback, UserRequest ureq, WindowControl wControl,
			IQTESTCourseNode testCourseNode, RepositoryEntry testEntry) {
		
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		
		this.modConfig = moduleConfiguration;
		this.secCallback = secCallback;
		this.userCourseEnv = userCourseEnv;
		this.courseNode = testCourseNode;
		this.type = AssessmentInstance.QMD_ENTRY_TYPE_ASSESS;
		this.singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		this.referenceTestEntry = testEntry;
		
		this.userSession = ureq.getUserSession();
		
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));
		
		myContent = createVelocityContainer("testrun");
		
		mainPanel = putInitialPanel(myContent);

		if (!modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
			throw new OLATRuntimeException("IQRunController launched with Test constructor but module configuration not configured as test" ,null);
		}
		
		HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, wControl, userCourseEnv, courseNode);
		if (highScoreCtr.isViewHighscore()) {
			Component highScoreComponent = highScoreCtr.getInitialComponent();
			myContent.put("highScore", highScoreComponent);							
		}
		
		init(ureq);
		exposeUserTestDataToVC(ureq);
		
		StringBuilder qtiChangelog = createChangelogMsg(ureq);
		// decide about changelog in VC
		if(qtiChangelog.length()>0){
			//there is some message
			myContent.contextPut("changeLog", qtiChangelog);
		}
		
	  //if show results on test home page configured - show log
		Boolean showResultOnHomePage = testCourseNode.getModuleConfiguration().getBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);		
		myContent.contextPut("showChangelog", showResultOnHomePage);

		//admin setting whether to show change log and info box or not
		myContent.contextPut("infobox", courseModule.isDisplayInfoBox());
		myContent.contextPut("changelogconfig", courseModule.isDisplayChangeLog());
	}

	private StringBuilder createChangelogMsg(UserRequest ureq) {
		//re could be null, but if we are here it should not be null!
		Roles userRoles = ureq.getUserSession().getRoles();
		boolean showAll = userRoles.isAuthor() || userRoles.isAdministrator() || userRoles.isLearnResourceManager();
		//get changelog
		Formatter formatter = Formatter.getInstance(ureq.getLocale());
		ImsRepositoryResolver resolver = new ImsRepositoryResolver(referenceTestEntry);
		QTIChangeLogMessage[] qtiChangeLog = resolver.getDocumentChangeLog();
		
		StringBuilder qtiChangelog = new StringBuilder();

		if(qtiChangeLog.length>0){
			List<QTIChangeLogMessage> qtiChangeLogList = new ArrayList<>(qtiChangeLog.length);
			for (int i=qtiChangeLog.length; i-->0 ; ) {
				if(qtiChangeLog[i] != null) {
					qtiChangeLogList.add(qtiChangeLog[i]);
				}
			}
			//there are resource changes
			Collections.sort(qtiChangeLogList);
			
			for (int i = qtiChangeLogList.size()-1; i >= 0 ; i--) {
				QTIChangeLogMessage qtiChangeLogEntry = qtiChangeLogList.get(i);
				//show latest change first
				if(!showAll && qtiChangeLogEntry.isPublic()){
					//logged in person is a normal user, hence public messages only
					Date msgDate = new Date(qtiChangeLogEntry.getTimestmp());
					qtiChangelog.append("\nChange date: ").append(formatter.formatDateAndTime(msgDate)).append("\n");
					String msg = StringHelper.escapeHtml(qtiChangeLogEntry.getLogMessage());
					qtiChangelog.append(msg);
					qtiChangelog.append("\n********************************\n");
				}else if (showAll){
					//logged in person is an author, olat admin, owner, show all messages
					Date msgDate = new Date(qtiChangeLogEntry.getTimestmp());
					qtiChangelog.append("\nChange date: ").append(formatter.formatDateAndTime(msgDate)).append("\n");
					String msg = StringHelper.escapeHtml(qtiChangeLogEntry.getLogMessage());
					qtiChangelog.append(msg);
					qtiChangelog.append("\n********************************\n");
				}//else non public messages are not shown to normal user
			}
		}
		return qtiChangelog;
	}

	/**
	 * Constructor for a self-test run controller
	 * @param userCourseEnv
	 * @param moduleConfiguration
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @param selftestCourseNode
	 */
	public IQRunController(UserCourseEnvironment userCourseEnv, ModuleConfiguration moduleConfiguration, IQSecurityCallback secCallback, UserRequest ureq, WindowControl wControl, IQSELFCourseNode selftestCourseNode) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		
		this.modConfig = moduleConfiguration;
		this.secCallback = secCallback;
		this.userCourseEnv = userCourseEnv;
		this.courseNode = selftestCourseNode;
		this.type = AssessmentInstance.QMD_ENTRY_TYPE_SELF;
		this.referenceTestEntry = selftestCourseNode.getReferencedRepositoryEntry();
		this.singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		iqManager = CoreSpringFactory.getImpl(IQManager.class);

		addLoggingResourceable(LoggingResourceable.wrap(courseNode));

		myContent = createVelocityContainer("selftestrun");

		mainPanel = putInitialPanel(myContent);		

		if (!modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)) {
			throw new OLATRuntimeException("IQRunController launched with Selftest constructor but module configuration not configured as selftest" ,null);
		}
		init(ureq);
		exposeUserSelfTestDataToVC(ureq);
				
		StringBuilder qtiChangelog = createChangelogMsg(ureq);
		// decide about changelog in VC
		if(qtiChangelog.length()>0){
			//there is some message
			myContent.contextPut("changeLog", qtiChangelog);
		}
		//per default change log is not open
		myContent.contextPut("showChangelog", Boolean.FALSE);
		//admin setting whether to show change log and info box or not
		myContent.contextPut("infobox", courseModule.isDisplayInfoBox());
		myContent.contextPut("changelogconfig", courseModule.isDisplayChangeLog());
	}

	/**
	 * Constructor for a survey run controller
	 * @param userCourseEnv
	 * @param moduleConfiguration
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @param surveyCourseNode
	 */
	public IQRunController(UserCourseEnvironment userCourseEnv, ModuleConfiguration moduleConfiguration, IQSecurityCallback secCallback, UserRequest ureq, WindowControl wControl, IQSURVCourseNode surveyCourseNode) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		
		this.modConfig = moduleConfiguration;
		this.secCallback = secCallback;
		this.userCourseEnv = userCourseEnv;
		this.courseNode = surveyCourseNode;
		this.referenceTestEntry = surveyCourseNode.getReferencedRepositoryEntry();
		this.type = AssessmentInstance.QMD_ENTRY_TYPE_SURVEY;
		this.singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		iqManager = CoreSpringFactory.getImpl(IQManager.class);
		
		addLoggingResourceable(LoggingResourceable.wrap(courseNode));

		myContent = createVelocityContainer("surveyrun");
		
		mainPanel = putInitialPanel(myContent);		

		if (!modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
			throw new OLATRuntimeException("IQRunController launched with Survey constructor but module configuration not configured as survey" ,null);
		}
		init(ureq);
		exposeUserQuestionnaireDataToVC();
		
		StringBuilder qtiChangelog = createChangelogMsg(ureq);
		// decide about changelog in VC
		if(qtiChangelog.length()>0){
			//there is some message
			myContent.contextPut("changeLog", qtiChangelog);
		}
		//per default change log is not open
		myContent.contextPut("showChangelog", Boolean.FALSE);
		//admin setting whether to show change log and info box or not
		myContent.contextPut("infobox", courseModule.isDisplayInfoBox());
		myContent.contextPut("changelogconfig", courseModule.isDisplayChangeLog());
	}
	
	private void init(UserRequest ureq) {
		startButton = LinkFactory.createButton("start", myContent, this);
		startButton.setElementCssClass("o_sel_start_qti12_test");
		startButton.setPrimary(true);
		startButton.setVisible(!userCourseEnv.isCourseReadOnly());
	
		// fetch disclaimer file
		String sDisclaimer = (String)modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
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
					iFrameCtr = new IFrameDisplayController(ureq, getWindowControl(), baseContainer);
					listenTo(iFrameCtr);//dispose automatically
					myContent.put("disc", iFrameCtr.getInitialComponent());
					iFrameCtr.setCurrentURI(sDisclaimer);
					myContent.contextPut("hasDisc", Boolean.TRUE);
					myContent.contextPut("indisclaimer", isPanelOpen(ureq, "disclaimer", true));
				}
			}
		}

		// push title and learning objectives, only visible on intro page
		myContent.contextPut("menuTitle", courseNode.getShortTitle());
		myContent.contextPut("displayTitle", courseNode.getLongTitle());
		
		// Adding learning objectives
		String learningObj = courseNode.getLearningObjectives();
		if (learningObj != null) {
			Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq); 
			myContent.put("learningObjectives", learningObjectives);
			myContent.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator					
		}
		
		if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
			checkChats(ureq);
			singleUserEventCenter.registerFor(this, getIdentity(), InstantMessagingService.TOWER_EVENT_ORES);
		}
	}
	
	private void checkChats (UserRequest ureq) {
		List<?> allChats = null;
		if (ureq != null) {
			allChats = ureq.getUserSession().getChats();
		}
		if (allChats == null || allChats.size() == 0) {
			startButton.setEnabled (true);
			myContent.contextPut("hasChatWindowOpen", false);
		} else {
			startButton.setEnabled (false);
			myContent.contextPut("hasChatWindowOpen", true);
		}
	}
	
	@Override
	public void event(Event event) {
		if (type == AssessmentInstance.QMD_ENTRY_TYPE_ASSESS) {
			if (event.getCommand().startsWith("ChatWindow")) {
				checkChats(null);
			}
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startButton && startButton.isEnabled() && startButton.isVisible()){
			long courseResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId().longValue();
			String courseNodeIdent = courseNode.getIdent();
			removeAsListenerAndDispose(displayController);

			OLATResourceable ores = OresHelper.createOLATResourceableTypeWithoutCheck("test");
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
			WindowControl bwControl = addToHistory(ureq, ores, null);
			Controller returnController = iqManager.createIQDisplayController(modConfig, secCallback, ureq, bwControl, courseResId, courseNodeIdent, this);
			/*
			 * either returnController is a MessageController or it is a IQDisplayController
			 * this should not serve as pattern to be copy&pasted.
			 * FIXME:2008-11-21:pb INTRODUCED because of read/write QTI Lock solution for scalability II, 6.1.x Release 
			 */
			if(returnController instanceof IQDisplayController){
				displayController = (IQDisplayController)returnController;
				listenTo(displayController);
				if(displayController.isClosed()) {
					//do nothing
				} else  if (displayController.isReady()) {
					// in case displayController was unable to initialize, a message was set by displayController
					// this is the case if no more attempts or security check was unsuccessfull
					displayContainerController = new LayoutMain3ColsController(ureq, getWindowControl(), displayController);
					listenTo(displayContainerController); // autodispose

					//need to wrap a course restart controller again, because IQDisplay
					//runs on top of GUIStack
					ICourse course = CourseFactory.loadCourse(courseResId);
					RepositoryEntry courseRepositoryEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
					Panel empty = new Panel("empty");//empty panel set as "menu" and "tool"
					Controller courseCloser = new DisposedCourseRestartController(ureq, getWindowControl(), courseRepositoryEntry);
					Controller disposedRestartController = new LayoutMain3ColsController(ureq, getWindowControl(), empty, courseCloser.getInitialComponent(), "disposed course whily in iqRun" + courseResId);
					displayContainerController.setDisposedMessageController(disposedRestartController);
					
					final boolean fullWindow = modConfig.getBooleanSafe(IQEditController.CONFIG_FULLWINDOW, true);
					if(fullWindow) {
						displayContainerController.setAsFullscreen(ureq);
					}
					displayContainerController.activate();
					
					if (modConfig.get(IQEditController.CONFIG_KEY_TYPE).equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
						assessmentStopped = false;		
						singleUserEventCenter.registerFor(this, getIdentity(), assessmentInstanceOres);
						singleUserEventCenter.fireEventToListenersOf(new AssessmentEvent(AssessmentEvent.TYPE.STARTED, ureq.getUserSession()), assessmentEventOres);						
					}
				}//endif isReady
			
				
			}else{
				// -> qti file was locked -> show info message
				// user must click again on course node to activate
				mainPanel.pushContent(returnController.getInitialComponent());
			}
		} else if(source == showResultsButton) {			
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			Long assessmentID = am.getAssessmentID(courseNode, ureq.getIdentity());			
			if(assessmentID==null) {
				//fallback solution: if the assessmentID is not available via AssessmentManager than try to get it via IQManager
				long callingResId = userCourseEnv.getCourseEnvironment().getCourseResourceableId().longValue();
				String callingResDetail = courseNode.getIdent();
				assessmentID = iqManager.getLastAssessmentID(ureq.getIdentity(), callingResId, callingResDetail);
			}
			if(assessmentID!=null && !assessmentID.equals("")) {
				Document doc = iqManager.getResultsReportingFromFile(ureq.getIdentity(), type, assessmentID);
				//StringBuilder resultsHTML = LocalizedXSLTransformer.getInstance(ureq.getLocale()).renderResults(doc);
				String summaryConfig = (String)modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
				int summaryType = AssessmentInstance.SUMMARY_NONE;
				try {
					summaryType = AssessmentInstance.getSummaryType(summaryConfig);
				} catch (Exception e) {
					// cannot change AssessmentInstance: fallback if the the configuration is inherited from a QTI 2.1 configuration
					if(StringHelper.containsNonWhitespace(summaryConfig)) {
						summaryType = AssessmentInstance.SUMMARY_DETAILED;
					}
					logError("", e);
				}
				String resultsHTML = iqManager.transformResultsReporting(doc, ureq.getLocale(), summaryType);
				myContent.contextPut("displayreporting", resultsHTML);
				myContent.contextPut("resreporting", resultsHTML);
				myContent.contextPut("showResults", Boolean.TRUE);
			} 
		} else if (source == hideResultsButton) {
			myContent.contextPut("showResults", Boolean.FALSE);
		} else if("show".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), true);
		} else if("hide".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), false);
		}
	}

	@Override
	public void submitAssessment(AssessmentInstance ai) {
		if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
			AssessmentContext ac = ai.getAssessmentContext();
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			Boolean fullyAssed = am.getNodeFullyAssessed(courseNode, getIdentity());
			
			String correctionMode = courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
			Boolean userVisibility;
			AssessmentEntryStatus assessmentStatus;
			if(IQEditController.CORRECTION_MANUAL.equals(correctionMode)) {
				assessmentStatus = AssessmentEntryStatus.inReview;
				userVisibility = Boolean.FALSE;
			} else {
				assessmentStatus = AssessmentEntryStatus.done;
				userVisibility = Boolean.TRUE;
			}
			
			ScoreEvaluation sceval = new ScoreEvaluation(ac.getScore(), ac.isPassed(), assessmentStatus, userVisibility, null, null, null, ai.getAssessID());
			courseAssessmentService.updateScoreEvaluation(courseNode, sceval, userCourseEnv, getIdentity(), true, Role.user);
				
			// Mark publisher for notifications
			Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			assessmentNotificationsHandler.markPublisherNews(getIdentity(), courseId);
			if(!assessmentStopped) {
				assessmentStopped = true;					  
				AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
				singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
				singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
			}
		} else if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
			// save number of attempts
			// although this is not an assessable node we still use the assessment
			// manager since this one uses caching
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			am.incrementNodeAttempts(courseNode, getIdentity(), userCourseEnv, Role.user);
		} else if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)){
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			am.incrementNodeAttempts(courseNode, getIdentity(), userCourseEnv, Role.user);
		}
	}

	@Override
	public void cancelAssessment(AssessmentInstance ai) {
		//
	}

	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == displayController) {
			if (event instanceof IQSubmittedEvent) {
				// Save results in case of test
				if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {		
					exposeUserTestDataToVC(urequest);
				} 
				// Save results in case of questionnaire
				else if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SURVEY)) {
					exposeUserQuestionnaireDataToVC();
					
					if(displayContainerController != null) {
						displayContainerController.deactivate(urequest);
					} else {
						getWindowControl().pop();
					}
					OLATResourceable ores = OresHelper.createOLATResourceableInstance("test", -1l);
					addToHistory(urequest, ores, null);
				}
				// Don't save results in case of self-test
				// but do safe attempts !
				else if(type.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF)){
					//am.incrementNodeAttempts(courseNode, urequest.getIdentity(), userCourseEnv);
				}
			} else if (event.equals(Event.DONE_EVENT)) {
				stopAssessment(urequest, event);
			} else if (IQEvent.TEST_PULLED.equals(event.getCommand())) {
				stopAssessment(urequest, event);
				showWarning("error.assessment.pulled");
			} else if (IQEvent.TEST_STOPPED.equals(event.getCommand())) {
				stopAssessment(urequest, event);
				showWarning("error.assessment.stopped");
			}
		}
	}
	
	private void stopAssessment(UserRequest ureq, Event event) {
		if(displayContainerController != null) {
			displayContainerController.deactivate(ureq);
		} else {
			getWindowControl().pop();
		}	
		removeHistory(ureq);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("test", -1l);
		addToHistory(ureq, ores, null);
		if (type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS) && !assessmentStopped ) {
			assessmentStopped = true;					
			AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
			singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
			singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
		}
		fireEvent(ureq, event);
	}

	private void exposeUserTestDataToVC(UserRequest ureq) {
		// config : show score info
		Object enableScoreInfoObject = modConfig.get(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
		if (enableScoreInfoObject != null) {
			myContent.contextPut("enableScoreInfo", enableScoreInfoObject );	
		} else {
			myContent.contextPut("enableScoreInfo", Boolean.TRUE );
		}
		// configuration data
		myContent.contextPut("attemptsConfig", modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS));

		// user data
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnv);
		if(assessmentEntry == null) {
			myContent.contextPut("blockAfterSuccess", Boolean.FALSE);
			myContent.contextPut("score", null);
			myContent.contextPut("hasPassedValue", Boolean.FALSE);
			myContent.contextPut("passed", Boolean.FALSE);
			myContent.contextPut("comment", null);
			myContent.contextPut("docs", null);
			myContent.contextPut("attempts", 0);
		} else {
			//block if test passed (and config set to check it)
			Boolean blockAfterSuccess = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_BLOCK_AFTER_SUCCESS);
			Boolean blocked = Boolean.FALSE;
			if(blockAfterSuccess != null && blockAfterSuccess.booleanValue()) {
				Boolean passed = assessmentEntry.getPassed();
				if(passed != null && passed.booleanValue()) {
					blocked = Boolean.TRUE;
				}
			}
			myContent.contextPut("blockAfterSuccess", blocked);
			boolean resultsVisible = assessmentEntry.getUserVisibility() == null || assessmentEntry.getUserVisibility().booleanValue();
			myContent.contextPut("resultsVisible", resultsVisible);
			myContent.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
			myContent.contextPut("hasPassedValue", (assessmentEntry.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
			myContent.contextPut("passed", assessmentEntry.getPassed());
			if(resultsVisible) {
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
				if(assessmentConfig.hasComment()) {
					StringBuilder comment = Formatter.stripTabsAndReturns(assessmentEntry.getComment());
					myContent.contextPut("comment", StringHelper.xssScan(comment));
					myContent.contextPut("incomment", isPanelOpen(ureq, "comment", true));
				}

				if(assessmentConfig.hasIndividualAsssessmentDocuments()) {
					List<File> docs = courseAssessmentService.getIndividualAssessmentDocuments(courseNode, userCourseEnv);
					String mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(docs));
					myContent.contextPut("docsMapperUri", mapperUri);
					myContent.contextPut("docs", docs);
					myContent.contextPut("inassessmentDocuments", isPanelOpen(ureq, "assessmentDocuments", true));
				}
			}
			myContent.contextPut("attempts", assessmentEntry.getAttempts() == null ? 0 : assessmentEntry.getAttempts());
		}
		
		UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
		String userLog = am.getUserNodeLog(courseNode, identity);
		myContent.contextPut("log", StringHelper.escapeHtml(userLog));
		exposeResults(ureq);
	}
	
	/**
	 * Provides the self test score and results, if any, to the velocity container.
	 * @param ureq
	 */
	private void exposeUserSelfTestDataToVC(UserRequest ureq) {
		if (!(courseNode instanceof SelfAssessableCourseNode)) {
			throw new AssertException("exposeUserSelfTestDataToVC can only be called for selftest nodes, not for test or questionnaire");
		}
		
		// config : show score info
		Object enableScoreInfoObject = modConfig.get(IQEditController.CONFIG_KEY_ENABLESCOREINFO);
		if (enableScoreInfoObject != null) {
			myContent.contextPut("enableScoreInfo", enableScoreInfoObject );	
		} else {
			myContent.contextPut("enableScoreInfo", Boolean.TRUE );
		}

		SelfAssessableCourseNode acn = (SelfAssessableCourseNode)courseNode; 
		ScoreEvaluation scoreEval = acn.getUserScoreEvaluation(userCourseEnv);
		if (scoreEval != null) {
			myContent.contextPut("hasResults", Boolean.TRUE);
			myContent.contextPut("resultsVisible", Boolean.TRUE);
			myContent.contextPut("score", AssessmentHelper.getRoundedScore(scoreEval.getScore()));
			myContent.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
			myContent.contextPut("passed", scoreEval.getPassed());
			myContent.contextPut("attempts", new Integer(1)); //at least one attempt
			
			exposeResults(ureq);
		}
	}
		
	/**
	 * Provides the show results button if results available or a message with the visibility period.
	 * @param ureq
	 */
	private void exposeResults(UserRequest ureq) {
    //migration: check if old tests have no summary configured
	  String configuredSummary = (String) modConfig.get(IQEditController.CONFIG_KEY_SUMMARY);
	  boolean noSummary = configuredSummary==null || (configuredSummary!=null && configuredSummary.equals(AssessmentInstance.QMD_ENTRY_SUMMARY_NONE));
	  if(!noSummary) {
			Boolean showResultsObj = modConfig.getBooleanEntry(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE);		
			boolean showResultsOnHomePage = (showResultsObj!=null && showResultsObj.booleanValue());
			myContent.contextPut("showResultsOnHomePage",new Boolean(showResultsOnHomePage));
			myContent.contextPut("in-results", isPanelOpen(ureq, "results", true));
			boolean dateRelatedVisibility = isResultVisible();		
			if(showResultsOnHomePage && dateRelatedVisibility) {
				myContent.contextPut("showResultsVisible",Boolean.TRUE);
				showResultsButton = LinkFactory.createButton("command.showResults", myContent, this);
				hideResultsButton = LinkFactory.createButton("command.hideResults", myContent, this);
			} else if(showResultsOnHomePage) {
				Date startDate = (Date)modConfig.get(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
				Date endDate = (Date)modConfig.get(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			  	String visibilityStartDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(startDate);
			  	String visibilityEndDate = "-";
			  	if(endDate!=null) {
				  	visibilityEndDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale()).format(endDate);
			  	}
			  	String visibilityPeriod = getTranslator().translate("showResults.visibility", new String[] { visibilityStartDate, visibilityEndDate});
				myContent.contextPut("visibilityPeriod",visibilityPeriod);
				myContent.contextPut("showResultsVisible",Boolean.FALSE);
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
	private boolean isResultVisible() {
		boolean isVisible = false;
		String showResultsActive = modConfig.getStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS);
		Date startDate, endDate;
		Date passedStartDate, passedEndDate;
		Date failedStartDate, failedEndDate;
		Date currentDate;
		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, userCourseEnv);
		
		switch (showResultsActive) {
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_ALWAYS:
			isVisible = true;
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_DIFFERENT:
			passedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
			passedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
			currentDate = new Date();
			if(scoreEval.getPassed() && passedStartDate != null && currentDate.after(passedStartDate) && (passedEndDate == null || currentDate.before(passedEndDate))) {
				isVisible = true;
				break;
			}
			
			failedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			failedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			if(!scoreEval.getPassed() && failedStartDate != null && currentDate.after(failedStartDate) && (failedEndDate == null || currentDate.before(failedEndDate))) {
				isVisible = true;
				break;
			}
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_FAILED_ONLY:
			if (scoreEval.getPassed()) {
				isVisible = false;
				break;
			}
			currentDate = new Date();
			failedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
			failedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
			if(!scoreEval.getPassed() && failedStartDate != null && currentDate.after(failedStartDate) && (failedEndDate == null || currentDate.before(failedEndDate))) {
				isVisible = true;
				break;
			}
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_PASSED_ONLY:
			if (!scoreEval.getPassed()) {
				isVisible = false;
				break;
			}
			currentDate = new Date();
			passedStartDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
			passedEndDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
			if(scoreEval.getPassed() && passedStartDate != null && currentDate.after(passedStartDate) && (passedEndDate == null || currentDate.before(passedEndDate))) {
				isVisible = true;
				break;
			}
			break;
		case IQEditController.CONFIG_VALUE_DATE_DEPENDENT_RESULT_SAME:
			startDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
			endDate = modConfig.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
			currentDate = new Date();
			if(startDate != null && currentDate.after(startDate) && (endDate == null || currentDate.before(endDate))) {
				isVisible = true;
			}
			break;
		default:
			isVisible = true;
			break;
		}

		return isVisible;
	}

	private void exposeUserQuestionnaireDataToVC() {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		// although this is not an assessable node we still use the assessment
		// manager since this one uses caching
		myContent.contextPut("attempts", am.getNodeAttempts(courseNode, identity));
	}
	
	private boolean isPanelOpen(UserRequest ureq, String panelId, boolean def) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(IQRunController.class, getOpenPanelId(panelId));
		return showConfig == null ? def : showConfig.booleanValue();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(IQRunController.class, getOpenPanelId(panelId), new Boolean(newValue));
		}
		myContent.contextPut("in-" + panelId, new Boolean(newValue));
	}
	
	private String getOpenPanelId(String panelId) {
		return panelId + "::" + userCourseEnv.getCourseEnvironment().getCourseResourceableId() + "::" + courseNode.getIdent();
	}
	
	@Override
	protected void doDispose() {
		// child controllers disposed by basic controller
		if (!type.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS)) {
			return;
		}
		
		singleUserEventCenter.deregisterFor(this, assessmentInstanceOres);
		singleUserEventCenter.deregisterFor(this, InstantMessagingService.TOWER_EVENT_ORES);
		
		if (!assessmentStopped) {		 
				AssessmentEvent assessmentStoppedEvent = new AssessmentEvent(AssessmentEvent.TYPE.STOPPED, userSession);
				singleUserEventCenter.fireEventToListenersOf(assessmentStoppedEvent, assessmentEventOres);
		}
		
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.remove(0);
		if("test".equals(ce.getOLATResourceable().getResourceableTypeName())) {
			Long resourceId = ce.getOLATResourceable().getResourceableId();
			if(resourceId != null && resourceId.longValue() >= 0) {
				//event(ureq, startButton, Event.CHANGED_EVENT);
			}
		}
	}
}
