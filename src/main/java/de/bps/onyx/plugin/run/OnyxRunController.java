
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.plugin.run;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.HtmlStaticPageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQSecurityCallback;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.OnyxModule.PlayerTemplate;
import de.bps.onyx.plugin.OnyxResultManager;
import de.bps.onyx.plugin.course.nodes.iq.IQEditController;
import de.bps.onyx.plugin.wsclient.OnyxPluginServices;
import de.bps.onyx.plugin.wsclient.PluginService;
import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;

/**
 * @author Ingmar Kroll
 */
public class OnyxRunController extends BasicController {

	private VelocityContainer myContent, onyxReporterVC, onyxPlugin;
	private ModuleConfiguration modConfig;
	private IQTESTCourseNode courseNodeTest;
	private IQSURVCourseNode courseNodeSurvey;
	private IQSELFCourseNode courseNodeSelf;
	private UserCourseEnvironment userCourseEnv;
	private Link startButton, onyxBack;
	private IFrameDisplayController iFrameCtr;
	private String CP;
	private String uniqueId;
	// <OLATCE-99>
	private CloseableModalController onyxPluginController;
	// </OLATCE-99>
	private boolean isAjaxEnabled;
	WindowManager wManager;

	private final static int NOENTRYVIEW = -1;
	private final static int DISCVIEW = 0;
	private final static int SURVEYVIEW = 1;
	private final static int ENDVIEW = 2;

	private Link showOnyxReporterButton;

	//<ONYX-705>
	private final static OLog log = Tracing.createLoggerFor(OnyxRunController.class);
	//</ONYX-705>
	
	//<OLATCE-1124>
	private boolean isCourseCoach = false;
	//</OLATCE-1124>

	// <OLATCE-1054>
	// add boolean activateModalController
	public OnyxRunController(UserRequest ureq, WindowControl wControl, OLATResourceable fileResource, boolean activateModalController) {
		super(ureq, wControl);
		VelocityContainer vc = showOnyxTestInModalController(ureq, fileResource, activateModalController);
		// </OLATCE-1054>
		putInitialPanel(vc);
	}
	
	/**
	 * Constructor for a test run controller
	 *
	 * @param userCourseEnv
	 * @param moduleConfiguration
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @param testCourseNode
	 */
	public OnyxRunController(UserCourseEnvironment userCourseEnv, ModuleConfiguration moduleConfiguration, IQSecurityCallback secCallback,
			UserRequest ureq, WindowControl wControl, IQSURVCourseNode courseNode) {
		super(ureq, wControl);
		this.modConfig = moduleConfiguration;
		this.userCourseEnv = userCourseEnv;
		myContent = createVelocityContainer("onyxrun");
		this.courseNodeSurvey = courseNode;
		this.CP = getCP(courseNode.getReferencedRepositoryEntry().getOlatResource());
		putInitialPanel(myContent);
		//<OLATCE-1124>
		this.isCourseCoach = userCourseEnv.getCourseEnvironment().getCourseGroupManager().isIdentityCourseAdministrator(ureq.getIdentity()) || userCourseEnv.getCourseEnvironment().getCourseGroupManager().isIdentityCourseCoach(ureq.getIdentity());
		//</OLATCE-1124>
		showView(ureq, SURVEYVIEW);
	}

	/**
	 * Constructor for a test run controller
	 *
	 * @param userCourseEnv
	 * @param moduleConfiguration
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @param testCourseNode
	 */
	public OnyxRunController(UserCourseEnvironment userCourseEnv, ModuleConfiguration moduleConfiguration, IQSecurityCallback secCallback,
			UserRequest ureq, WindowControl wControl, IQSELFCourseNode courseNode) {
		super(ureq, wControl);
		this.modConfig = moduleConfiguration;
		this.userCourseEnv = userCourseEnv;
		myContent = createVelocityContainer("onyxrun");
		this.courseNodeSelf = courseNode;
		this.CP = getCP(courseNode.getReferencedRepositoryEntry().getOlatResource());
		putInitialPanel(myContent);
		// <OLATBPS-363>
		int confValue = 0;
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_ATTEMPTS) != null) {
			confValue = Integer.valueOf(moduleConfiguration.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
		}
		if (confValue == 0 || am.getNodeAttempts(courseNode, ureq.getIdentity()).intValue() < confValue) {
			// running allowed
			showView(ureq, DISCVIEW);
		} else {
			// only one time allowed
			showView(ureq, NOENTRYVIEW);
		}
		// </OLATBPS-363>
	}

	/**
	 * Constructor for a test run controller
	 *
	 * @param userCourseEnv
	 * @param moduleConfiguration
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @param testCourseNode
	 */
	public OnyxRunController(UserCourseEnvironment userCourseEnv, ModuleConfiguration moduleConfiguration, IQSecurityCallback secCallback,
			UserRequest ureq, WindowControl wControl, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		this.modConfig = moduleConfiguration;
		this.userCourseEnv = userCourseEnv;
		myContent = createVelocityContainer("onyxrun");
		this.courseNodeTest= courseNode;
		this.CP=getCP(courseNode.getReferencedRepositoryEntry().getOlatResource());
		putInitialPanel(myContent);
		int confValue = 0;
		// <OLATBPS-363>
		if (moduleConfiguration.get(IQEditController.CONFIG_KEY_ATTEMPTS) != null) {
		// </OLATBPS-363>
			confValue = Integer.valueOf(moduleConfiguration.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
		}
		if (confValue == 0 || courseNode.getUserAttempts(userCourseEnv).intValue() < confValue) {
			// running allowed
			showView(ureq, DISCVIEW);
		} else {
			// only one time allowed
			showView(ureq, NOENTRYVIEW);
		}
	}

	private void showView(UserRequest ureq, int viewmode) {
		myContent.contextPut("viewmode", new Integer(viewmode));
		// <OLATBPS-363>
		int confValue = 0; //per default
		if (modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS) != null) {
			confValue = Integer.valueOf(modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
		}
		if (confValue != 0) {
			myContent.contextPut("attemptsConfig", String.valueOf(confValue));
		}
		// </OLATBPS-363>

		if (viewmode != SURVEYVIEW) {
			ScoreEvaluation se = null;
			
			//<OLATCE-1232>
			boolean isVisibilityPeriod = AssessmentHelper.isResultVisible(modConfig);
			
			if(!isVisibilityPeriod){
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
			} else {
				myContent.contextPut("showResultsVisible",Boolean.TRUE);
			}
			//</OLATCE-1232>
				
			if (courseNodeTest != null) {
				// <OLATCE-498>
				Integer attempts = courseNodeTest.getUserAttempts(userCourseEnv);
				myContent.contextPut("attempts", attempts);
				Boolean hasResults = false;
				if (attempts > 0) {
					//<ONYX-705>
					try{
						OnyxReporterConnector onyxReporter = new OnyxReporterConnector();
						String assessmentType = courseNodeTest.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString();
						hasResults = onyxReporter.hasResultXml(ureq.getIdentity().getName(), assessmentType, "" + courseNodeTest.getIdent());
					} catch (OnyxReporterException e) {
						log.error(e.getMessage(), e);
					}
					//</ONYX-705>
				}
				myContent.contextPut("hasResults", hasResults.booleanValue());
				// </OLATCE-498>
				myContent.contextPut("comment", courseNodeTest.getUserUserComment(userCourseEnv));

				if (courseNodeTest.getUserAttempts(userCourseEnv) > 0) {
					se = courseNodeTest.getUserScoreEvaluation(userCourseEnv);
				}
			} else if (courseNodeSelf != null) {
				myContent.contextPut("self", Boolean.TRUE);
				// <OLATBPS-363>
				AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
				// <OLATCE-498>
				Integer attempts = am.getNodeAttempts(courseNodeSelf, ureq.getIdentity());
				myContent.contextPut("attempts", attempts);
				Boolean hasResults = false;
				if (attempts > 0) {
					try{
						OnyxReporterConnector onyxReporter = new OnyxReporterConnector();
						String assessmentType = courseNodeSelf.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString();
						// <OLATCE-643>
						hasResults = onyxReporter.hasResultXml(ureq.getIdentity().getName(), assessmentType, "" + courseNodeSelf.getIdent());
						// </OLATCE-643>
					} catch (OnyxReporterException e) {
						log.error(e.getMessage(), e);
					}
				}
				myContent.contextPut("hasResults", hasResults.booleanValue());
				// </OLATCE-498>
				// <OLATBPS-363>
				se = courseNodeSelf.getUserScoreEvaluation(userCourseEnv);
			}
			boolean hasResult = se != null && se.getScore() != null;
			if (hasResult) {
				myContent.contextPut("hasResult", Boolean.TRUE);
				boolean isPassesSet = se.getPassed() != null;
				myContent.contextPut(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE, modConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE));
				if (isPassesSet) {
					myContent.contextPut("passed", se.getPassed());
				} else {
					myContent.contextPut("passed", "");
				}
				float score = se.getScore();
				myContent.contextPut("score", AssessmentHelper.getRoundedScore(score));
			} else {
				myContent.contextPut("hasResult", Boolean.FALSE);
			}
			showOnyxReporterButton = LinkFactory.createButtonSmall("cmd.showOnyxReporter", myContent, this);
		}

		switch (viewmode) {
			case DISCVIEW:
				// push title and learning objectives, only visible on intro page
				if (courseNodeTest != null) {
					myContent.contextPut("menuTitle", courseNodeTest.getShortTitle());
					myContent.contextPut("displayTitle", courseNodeTest.getLongTitle());
				} else if (courseNodeSelf != null) {
					myContent.contextPut("menuTitle", courseNodeSelf.getShortTitle());
					myContent.contextPut("displayTitle", courseNodeSelf.getLongTitle());
				}

				// fetch disclaimer file
				String sDisclaimer = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
				if (sDisclaimer != null) {
					setDisclaimer(sDisclaimer, ureq);
				}
				startButton = LinkFactory.createButton("startapplet", myContent, this);
				break;
			case ENDVIEW:
				break;
			case SURVEYVIEW:
				//fetch disclaimer file
				String sDisc = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
				if (sDisc != null) {
					setDisclaimer(sDisc, ureq);
				}
				// push title and learning objectives, only visible on intro page
				myContent.contextPut("menuTitle", courseNodeSurvey.getShortTitle());
				myContent.contextPut("displayTitle", courseNodeSurvey.getLongTitle());
				myContent.contextPut("showReporter", Boolean.FALSE);

				myContent.contextPut("attempts", courseNodeSurvey.getUserAttempts(userCourseEnv));
				startButton = LinkFactory.createButton("startapplet", myContent, this);

				//<OLATCE-1124>
				// <OLATBPS-96>
				if (isCourseCoach && existsResultsForSurvey()) {
				// </OLATBPS-96>
				//</OLATCE-1124>
					myContent.contextPut("showReporter", Boolean.TRUE);
					showOnyxReporterButton = LinkFactory.createCustomLink("cmd.showOnyxReporter", "cmd.showOnyxReporter", "onyxreporter.button.survey",  Link.BUTTON_SMALL, myContent, this);
				}
				break;
		}
	}

	/**
	 * @param sDisclaimer
	 * @param ureq
	 */
	private void setDisclaimer (String sDisclaimer, UserRequest ureq) {
		VFSContainer baseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
		int lastSlash = sDisclaimer.lastIndexOf('/');
		if (lastSlash != -1) {
			baseContainer = (VFSContainer) baseContainer.resolve(sDisclaimer.substring(0, lastSlash));
			sDisclaimer = sDisclaimer.substring(lastSlash);
			// first check if disclaimer exists on filesystem
			if (baseContainer == null || baseContainer.resolve(sDisclaimer) == null) {
				showWarning("disclaimer.file.invalid", sDisclaimer);
			} else {
				//screenreader do not like iframes, display inline
				if (getWindowControl().getWindowBackOffice().getWindowManager().isForScreenReader()) {
					HtmlStaticPageComponent disclaimerComp = new HtmlStaticPageComponent("disc", baseContainer);
					myContent.put("disc", disclaimerComp);
					disclaimerComp.setCurrentURI(sDisclaimer);
					myContent.contextPut("hasDisc", Boolean.TRUE);
				} else {
					iFrameCtr = new IFrameDisplayController(ureq, getWindowControl(), baseContainer);
					listenTo(iFrameCtr);//dispose automatically
					myContent.put("disc", iFrameCtr.getInitialComponent());
					iFrameCtr.setCurrentURI(sDisclaimer);
					myContent.contextPut("hasDisc", Boolean.TRUE);
				}
			}
		}
	}

	// <OLATBPS-451>
	/**
	 * This method looks for the latest qtiResultSet in the DB which belongs to this course node and user
	 * and updates the UserScoreEvaluation.
	 */
	private void updateUserScoreEvaluationFromQtiResult(Long courseResourceableId, AssessableCourseNode courseNode) {
		QTIResultManager qrm = QTIResultManager.getInstance();
		List<QTIResultSet> resultSets = qrm.getResultSets(courseResourceableId, courseNode.getIdent(), courseNode.getReferencedRepositoryEntry().getKey(), userCourseEnv.getIdentityEnvironment().getIdentity());
		QTIResultSet latestResultSet = null;
		for (QTIResultSet resultSet : resultSets) {
			if (latestResultSet == null) {
				latestResultSet = resultSet;
				continue;
			}
			// if a best score is given select the latest resultset with the best score
			if (resultSet.getLastModified().after(latestResultSet.getLastModified())) {
				latestResultSet = resultSet;
			}
		}
		if (latestResultSet != null) {
			ScoreEvaluation sc = new ScoreEvaluation(latestResultSet.getScore(), latestResultSet.getIsPassed(), latestResultSet.getAssessmentID());
			courseNode.updateUserScoreEvaluation(sc, userCourseEnv, userCourseEnv.getIdentityEnvironment().getIdentity(), false);
		}
	}
	// </OLATBPS-451>

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller controller, Event event) {
		if (controller == onyxPluginController) {
			if  (courseNodeSurvey != null) {
				showView(ureq, SURVEYVIEW);
			} else if (courseNodeTest != null) {
				
				// <OLATBPS-451>
				updateUserScoreEvaluationFromQtiResult(userCourseEnv.getCourseEnvironment().getCourseResourceableId(), courseNodeTest);
				// </OLATBPS-451>
				// <OLATBPS-363>
				int confValue = 0;
				if (modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS) != null) {
						confValue = Integer.valueOf(modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
				}
				// </OLATBPS-363>
				int userAttempts = 0;
				userAttempts = courseNodeTest.getUserAttempts(userCourseEnv).intValue();
				if (confValue == 0 || userAttempts < confValue) {
					// running allowed
					showView(ureq, DISCVIEW);
				} else {
					// only one time allowed
					showView(ureq, NOENTRYVIEW);
				}
			} else {
				showView(ureq, DISCVIEW);
			}
			//tell the RunMainController that it has to be updated
			userCourseEnv.getScoreAccounting().evaluateAll();
			fireEvent(ureq, Event.DONE_EVENT);
			Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(isAjaxEnabled);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startButton) {
			// <OLATCE-654>
			startOnyx(ureq);
			// </OLATCE-654>
			return;
		} else if (source == showOnyxReporterButton) {
			onyxReporterVC = createVelocityContainer("onyxreporter");
			int error = showOnyxReporter(ureq);
			if (error == 0) {
			CloseableModalController cmc = new CloseableModalController(getWindowControl(), "close", onyxReporterVC);
			cmc.activate();
			} else if (error == 1) {
				getWindowControl().setInfo(translate("oynxreporter.noresults"));
				Tracing.createLoggerFor(this.getClass()).error("could not connect to OnyxPlugin webservice");
			} else if (error == 2) {
				getWindowControl().setError(translate("onyxreporter.error"));
				Tracing.createLoggerFor(this.getClass()).error("could not connect to OnyxPlugin webservice");
			}
			return;
		}
		showView(ureq, ENDVIEW);
	}

	// <OLATCE-654>
	public void startOnyx(UserRequest ureq){
		//increase attempts when starting the test -> attempts do not depend on test
		//running correct
		// <OLATCE-445>
		isAjaxEnabled = Windows.getWindows(ureq).getWindowManager().isAjaxEnabled();
		Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(false);
		// </OLATCE-445>
		onyxPlugin = createVelocityContainer("onyxstart");
		String onyxBackLabel = "onyx.back";
		if (courseNodeTest != null) {
			onyxPlugin.contextPut("isSurvey", Boolean.FALSE);
		} else if (courseNodeSurvey != null) {
			onyxPlugin.contextPut("isSurvey", Boolean.TRUE);
			onyxBackLabel = "onyx.survey.back";
		}
		try {
			connectToOnyxWS(ureq);
			String urlonyxplugin = OnyxModule.getUserViewLocation() + "?id=" + this.uniqueId;
			onyxPlugin.contextPut("urlonyxplugin", urlonyxplugin);
			// <OLATCE-99>
			onyxPluginController = new CloseableModalController(getWindowControl(), "close", onyxPlugin, true);
			onyxPlugin.contextPut("showHint", Boolean.TRUE);
			// </OLATCE-99>
			onyxBack = LinkFactory.createCustomLink("onyx.back", "onyx.back", onyxBackLabel, Link.BUTTON_SMALL, onyxPlugin, onyxPluginController);
			listenTo(onyxPluginController);
			onyxPluginController.activate();
			//now increase attampts; if an exception occurred before, this will be not reached
			if (courseNodeTest != null) {
				courseNodeTest.incrementUserAttempts(userCourseEnv);
			} else if (courseNodeSurvey != null) {
				courseNodeSurvey.incrementUserAttempts(userCourseEnv);
			} else if(courseNodeSelf != null){
				courseNodeSelf.incrementUserAttempts(userCourseEnv);
				}
		} catch(Exception e) {
			getWindowControl().setError(translate("error.connectonyxws"));
			Tracing.createLoggerFor(this.getClass()).warn("could not connect to OnyxPlugin webservice", e);
		}
	}
	// </OLATCE-654>
	/**
	 * This methods calls the OnyxReporter and shows it in an iframe.
	 * @param ureq The UserRequest for getting the identity and role of the current user.
	 * @return	0 OK
	 * 					1 NO RESULTS
	 * 					2 ERROR
	 */
	private int showOnyxReporter(UserRequest ureq) {
		//<ONYX-705>
		OnyxReporterConnector onyxReporter = null;
			try{
				onyxReporter = new OnyxReporterConnector();
			} catch (OnyxReporterException e) {
				log.error(e.getMessage(), e);
			}
		//</ONYX-705>			
			if (onyxReporter != null) {
				List<Identity> identity = new ArrayList<Identity>();
				String iframeSrc = "";
				try {
					if (courseNodeTest != null) {
						identity.add(userCourseEnv.getIdentityEnvironment().getIdentity());
						long assasmentId = courseNodeTest.getUserScoreEvaluation(userCourseEnv).getAssessmentID();
						//<OLATCE-1124>
						//<ONYX-705>
						iframeSrc = onyxReporter.startReporterGUI(ureq.getIdentity(), identity, courseNodeTest, assasmentId, true, false);
						//</ONYX-705>
						//</OLATCE-1124>
					//<OLATCE-1048>
					} else if (courseNodeSelf != null){
						identity.add(userCourseEnv.getIdentityEnvironment().getIdentity());
						long assasmentId = courseNodeSelf.getUserScoreEvaluation(userCourseEnv).getAssessmentID();
						//<OLATCE-1124>
						iframeSrc = onyxReporter.startReporterGUI(ureq.getIdentity(), identity, courseNodeSelf, assasmentId, true, false);
						//</OLATCE-1124>
					//</OLATCE-1048>
					} else {
						//<ONYX-705>
						// <OLATBPS-96>
						iframeSrc = onyxReporter.startReporterGUIForSurvey(ureq.getIdentity(), courseNodeSurvey, getSurveyResultPath());
						// </OLATBPS-96>
						//</ONYX-705>
					}
				} catch (OnyxReporterException oE) {
					if (oE.getMessage().equals("noresults")) {
						oE.printStackTrace();
						return 1;
					}
				} catch (Exception e) {
					e.printStackTrace();
					return 2;
				}
				onyxReporterVC.contextPut("iframeOK", Boolean.TRUE);
				onyxReporterVC.contextPut("onyxReportLink", iframeSrc);
				return 0;
			} else {
				return 2;
			}
	}
	
	// <OLATBPS-96>
	/**
	 * This method checks if the directory where the results are stored for this survey exists.
	 * @return
	 */
	private boolean existsResultsForSurvey() {
		File surveyDir = new File(getSurveyResultPath());
		if (surveyDir.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * This method generates the path to the results directory for a survey
	 * @return A String representing the path
	 */
	private String getSurveyResultPath() {
		return userCourseEnv.getCourseEnvironment().getCourseBaseContainer()
			.getBasefile() + File.separator + courseNodeSurvey.getIdent() + File.separator;
	}
	// </OLATBPS-96>

	/**
	 * Static metohd to start the an onyx test as learningressource or bookmark.
	 * @param ureq
	 * @param repositoryEntry
	 */
	// <OLATCE-1054>
	public VelocityContainer showOnyxTestInModalController(UserRequest ureq, OLATResourceable fileResource, boolean activateModalController) {
	// </OLATCE-1054>
		OnyxPluginServices onyxplugin = null;
		try {
			onyxplugin = new PluginService().getOnyxPluginServicesPort(); 
		} catch(Exception e) {
			getWindowControl().setError(translate("error.connectonyxws"));
			Tracing.createLoggerFor(this.getClass()).warn("could not connect to OnyxPlugin webservice", e);
			return null;
		}
		
		String CP = getCP(fileResource);
		String language = ureq.getLocale().toString().toLowerCase();
		String tempalteId = "onyxdefault";

		this.uniqueId = OnyxResultManager.getUniqueIdForShowOnly();

		try {
			File cpFile = new File(CP);
			Long fileLength = cpFile.length();
			byte[] byteArray = new byte[fileLength.intValue()];
			System.out.println("CP : "+CP);
			java.io.FileInputStream inp = new java.io.FileInputStream(cpFile);
			inp.read(byteArray);
			onyxplugin.run(this.uniqueId, byteArray, language, "", tempalteId, OnyxModule.getConfigName(), true);
		} catch (FileNotFoundException e) {
			Tracing.createLoggerFor(this.getClass()).error("Cannot find CP of Onyx Test with assassmentId: " + uniqueId, e);
		} catch (IOException e) {
			Tracing.createLoggerFor(this.getClass()).error("Cannot find CP of Onyx Test with assassmentId: " + uniqueId, e);
		}

		String urlonyxplugin = OnyxModule.getUserViewLocation() + "?id=" + this.uniqueId;

		onyxPlugin = createVelocityContainer("onyxstart");
		onyxPlugin.contextPut("isSurvey", Boolean.FALSE);
		onyxPlugin.contextPut("urlonyxplugin", urlonyxplugin);
		onyxPlugin.contextPut("nohint", Boolean.TRUE);
		// <OLATCE-99>
		onyxPluginController = new CloseableModalController(getWindowControl(), "close", onyxPlugin, true);
		// </OLATCE-99>
		onyxBack = LinkFactory.createCustomLink("onyx.back", "onyx.back", "onyx.back", Link.BUTTON_SMALL, onyxPlugin, onyxPluginController);
		onyxBack.setVisible(false);
		// <OLATBPS-102>
		// <OLATCE-1054>
		if (activateModalController) {
			onyxPluginController.activate();
		}
		// </OLATCE-1054>
		// </OLATBPS-102>
		return onyxPlugin;
	}

	private void connectToOnyxWS(UserRequest ureq) {
		OnyxPluginServices onyxplugin = new PluginService().getOnyxPluginServicesPort(); 
		CourseNode courseNode = null;
		//<OLATCE-982>
		Boolean allowShowSolution = (Boolean) modConfig.get(IQEditController.CONFIG_KEY_ALLOW_SHOW_SOLUTION);
		// set allowShowSolution either to the configured value (!= null) or to defaultvalue false if test or survey, if selftest then the default is true 
		allowShowSolution = allowShowSolution!=null ? allowShowSolution : courseNodeSelf != null;
		//</OLATCE-982>
		if (courseNodeTest != null) {
			courseNode = courseNodeTest;
		} else if  (courseNodeSurvey != null) {
			courseNode = courseNodeSurvey;
		} else if  (courseNodeSelf != null) {
			courseNode = courseNodeSelf;
		}
		this.uniqueId = OnyxResultManager.getUniqueId(ureq.getIdentity(), courseNode, this.userCourseEnv);
	  String instructions = new String();
	  try {
			String sDisclaimer = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
			if (sDisclaimer != null) {
				VFSLeaf disc = (VFSLeaf) this.userCourseEnv.getCourseEnvironment().getCourseFolderContainer().resolve(sDisclaimer);
				BufferedReader r_disc = new BufferedReader(new InputStreamReader(disc.getInputStream()));
				String disc_s=new String();
				while (r_disc.ready()){
					disc_s+=r_disc.readLine();
				}
				instructions = disc_s;
			}
		} catch (IOException e) {
			Tracing.createLoggerFor(this.getClass()).error("could not read disclaimer");
		}
		String language = ureq.getLocale().toString().toLowerCase();
		// <OLATCE-499>
		String templateId = "onyxdefault";
		String tempalteId_config = (String) modConfig.get(IQEditController.CONFIG_KEY_TEMPLATE);
		if(tempalteId_config != null && tempalteId_config.length() > 0) {
			templateId = tempalteId_config;
			boolean isTemplateValid = false;
			for (PlayerTemplate template : OnyxModule.PLAYERTEMPLATES) {
				if (template.id.equals(templateId)) {
					isTemplateValid = true;
					break;
				}
			}
			if (!isTemplateValid) {
				templateId = OnyxModule.PLAYERTEMPLATES.get(0).id;
			}
		}
		// </OLATCE-499>
		// <ONYX-673>
		if (courseNodeSurvey != null) {
			// if this is a survey
			templateId += "_survey";
		}
		// </ONYX-673>

		try {

				File cpFile = new File(CP);
				Long fileLength = cpFile.length();
				byte[] byteArray = new byte[fileLength.intValue()];
				System.out.println("CP : "+CP);
				java.io.FileInputStream inp = new java.io.FileInputStream(cpFile);
				inp.read(byteArray);
				// <OLATCE-499>
				onyxplugin.run(this.uniqueId, byteArray, language, instructions, templateId, OnyxModule.getConfigName(), allowShowSolution);
				// </OLATCE-499>
			} catch (FileNotFoundException e) {
				Tracing.createLoggerFor(this.getClass()).error("Cannot find CP of Onyx Test with assassmentId: " + uniqueId, e);
			} catch (IOException e) {
				Tracing.createLoggerFor(this.getClass()).error("Cannot find CP of Onyx Test with assassmentId: " + uniqueId, e);
		}
	}

	private static String getCP(OLATResourceable fileResource){
		//get content-package (= onyx test zip-file)
		//OLATResourceable fileResource = repositoryEntry.getOlatResource();
		String unzipedDir = FileResourceManager.getInstance().unzipFileResource(fileResource).getAbsolutePath();
		String zipdirName = FileResourceManager.ZIPDIR;
		
		//String name = repositoryEntry.getResourcename();//getDisplayname();
		String name = FileResourceManager.getInstance().getFileResource(fileResource).getName();
		String pathToFile = unzipedDir.substring(0, unzipedDir.indexOf(zipdirName));
		String completePath = (pathToFile + name);
		File cpFile = new File(completePath);
		if (!cpFile.exists()) {
			//look for imported file
			String importedFileName = "repo.zip";
			File impFile = new File(pathToFile + importedFileName);
			if (impFile.exists()) {
				impFile.renameTo(cpFile);
			} else {
				Tracing.createLoggerFor(OnyxRunController.class).error("Cannot open Onyx CP File: " + completePath + " , also the imported repo.zip is not here!");
			}
		}
		return completePath;
	}

	@Override
	protected void doDispose() {
	}
}

