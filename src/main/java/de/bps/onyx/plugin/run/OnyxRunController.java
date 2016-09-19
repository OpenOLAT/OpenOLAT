
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

import static org.olat.course.nodes.iq.IQEditController.CONFIG_KEY_ALLOW_SUSPENSION_ALLOWED;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.OnyxResultManager;
import de.bps.onyx.plugin.wsclient.OnyxPluginServices;
import de.bps.onyx.plugin.wsclient.PluginService;
import de.bps.onyx.plugin.wsserver.TestState;
import de.bps.onyx.util.ExamPoolManager;
import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;
import de.bps.webservices.clients.onyxreporter.ReporterRole;

/**
 * @author Ingmar Kroll
 */
public class OnyxRunController extends BasicController {

	private VelocityContainer vc, onyxReporterVC, onyxPlugin;
	private ModuleConfiguration modConfig;
	private IQTESTCourseNode courseNodeTest;
	private IQSURVCourseNode courseNodeSurvey;
	private IQSELFCourseNode courseNodeSelf;
	private UserCourseEnvironment userCourseEnv;
	private Link onyxBack;
	private IFrameDisplayController iFrameCtr;
	private String uniqueId;
	// <OLATCE-99>
	private CloseableModalController onyxPluginController;
	// </OLATCE-99>
	private boolean isAjaxEnabled;
	WindowManager wManager;

	private StartButtonForm startForm;

	private final static int NOENTRYVIEW = -1;
	private final static int DISCVIEW = 0;
	private final static int SURVEYVIEW = 1;
	private final static int ENDVIEW = 2;

	private Link showOnyxReporterButton;

	// <ONYX-705>
	private final static OLog log = Tracing.createLoggerFor(OnyxRunController.class);
	// </ONYX-705>

	// <OLATCE-1124>
	private boolean isCourseCoach = false;
	// </OLATCE-1124>

	// <OLATCE-1054>
	// add boolean activateModalController
	public OnyxRunController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean activateModalController) {
		super(ureq, wControl);
		vc = showOnyxTestInModalController(ureq, entry, activateModalController);
		// </OLATCE-1054>
		putInitialPanel(vc);
	}

	public OnyxRunController(UserRequest ureq, WindowControl wControl, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		this.modConfig = courseNode.getModuleConfiguration();
		this.courseNodeTest = courseNode;
		vc = showOnyxTestInModalController(ureq, courseNode.getReferencedRepositoryEntry(), false);
		// </OLATCE-1054>
		putInitialPanel(vc);
	}

	/**
	 * General constructor for onyx-tests, self-tests or questionaires / surveys
	 * controllers
	 * 
	 * @param userCourseEnv
	 * @param moduleConfiguration
	 * @param secCallback
	 * @param ureq
	 * @param wControl
	 * @param testCourseNode
	 */
	public OnyxRunController(UserCourseEnvironment userCourseEnv, ModuleConfiguration moduleConfiguration,
			UserRequest ureq, WindowControl wControl, QTICourseNode courseNode) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));

		this.modConfig = moduleConfiguration;
		this.userCourseEnv = userCourseEnv;
		vc = createVelocityContainer("onyxrun");
		// <OLATCE-1124>
		this.isCourseCoach = userCourseEnv.getCourseEnvironment().getCourseGroupManager().isIdentityCourseAdministrator(ureq.getIdentity())
				|| userCourseEnv.getCourseEnvironment().getCourseGroupManager().isIdentityCourseCoach(ureq.getIdentity());
		// </OLATCE-1124>

		if (courseNode instanceof IQSURVCourseNode) {
			this.courseNodeSurvey = (IQSURVCourseNode) courseNode;
			showView(ureq, SURVEYVIEW);
		} else {
			int confValue = 0;
			final AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			if (moduleConfiguration.get(IQEditController.CONFIG_KEY_ATTEMPTS) != null) {
				confValue = Integer.valueOf(moduleConfiguration.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
			}
			if (courseNode instanceof IQSELFCourseNode) {
				this.courseNodeSelf = (IQSELFCourseNode) courseNode;
			} else if (courseNode instanceof IQTESTCourseNode) {
				this.courseNodeTest = (IQTESTCourseNode) courseNode;
			}

			int attempts = am.getNodeAttempts(courseNode, ureq.getIdentity()).intValue();
			final QTIResultSet result = OnyxResultManager.getLastSuspendedQTIResultSet(ureq.getIdentity(), courseNode);

			if (confValue == 0 || (attempts < confValue) || (result != null && OnyxResultManager.isLastTestTry(result) && --attempts < confValue)) {
				// running allowed
				showView(ureq, DISCVIEW);
			} else {
				// only one time allowed
				showView(ureq, NOENTRYVIEW);
			}
		}
		putInitialPanel(vc);
	}

	private void showView(UserRequest ureq, int viewmode) {
		vc.contextPut("viewmode", new Integer(viewmode));
		// <OLATBPS-363>
		int confValue = 0; // per default
		if (modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS) != null) {
			confValue = Integer.valueOf(modConfig.get(IQEditController.CONFIG_KEY_ATTEMPTS).toString()).intValue();
		}
		if (confValue != 0) {
			vc.contextPut("attemptsConfig", String.valueOf(confValue));
		}
		// </OLATBPS-363>

		QTICourseNode node = null;
		if (courseNodeTest != null) {
			node = courseNodeTest;
		} else if (courseNodeSelf != null) {
			node = courseNodeSelf;
		} else if (courseNodeSurvey != null) {
			node = courseNodeSurvey;
		}

		if (viewmode != SURVEYVIEW) {
			ScoreEvaluation se = null;
			
			//<OLATCE-1232>
			boolean isVisibilityPeriod = AssessmentHelper.isResultVisible(modConfig);
			vc.contextPut("showResultsVisible", new Boolean(isVisibilityPeriod));
			//</OLATCE-1232>
				
			if (courseNodeTest != null) {
				// <OLATCE-498>
				Integer attempts = courseNodeTest.getUserAttempts(userCourseEnv);
				vc.contextPut("attempts", attempts);
				Boolean hasResults = false;
				if (attempts > 0) {
					// <ONYX-705>
					try {
						OnyxReporterConnector onyxReporter = new OnyxReporterConnector();
						String assessmentType = courseNodeTest.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString();
						hasResults = onyxReporter.hasResults(ureq.getIdentity().getName(), assessmentType, courseNodeTest);
					} catch (OnyxReporterException e) {
						log.error(e.getMessage(), e);
					}
					// </ONYX-705>
				}
				vc.contextPut("hasResults", hasResults.booleanValue());
				// </OLATCE-498>
				StringBuilder comment = Formatter.stripTabsAndReturns(courseNodeTest.getUserUserComment(userCourseEnv));
				vc.contextPut("comment", StringHelper.xssScan(comment));

				if (courseNodeTest.getUserAttempts(userCourseEnv) > 0) {
					se = courseNodeTest.getUserScoreEvaluation(userCourseEnv);
				}
			} else if (courseNodeSelf != null) {
				vc.contextPut("self", Boolean.TRUE);
				// <OLATBPS-363>
				AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
				// <OLATCE-498>
				Integer attempts = am.getNodeAttempts(courseNodeSelf, ureq.getIdentity());
				vc.contextPut("attempts", attempts);
				Boolean hasResults = false;
				if (attempts > 0) {
					// <ONYX-705>
					try {
						OnyxReporterConnector onyxReporter = new OnyxReporterConnector();
						String assessmentType = courseNodeSelf.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString();
						// <OLATCE-643>
						hasResults = onyxReporter.hasResults(ureq.getIdentity().getName(), assessmentType, courseNodeSelf);
						// </OLATCE-643>
					} catch (OnyxReporterException e) {
						log.error(e.getMessage(), e);
					}
					// </ONYX-705>
				}
				vc.contextPut("hasResults", hasResults.booleanValue());
				// </OLATCE-498>
				// <OLATBPS-363>
				se = courseNodeSelf.getUserScoreEvaluation(userCourseEnv);
			}

			Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
			boolean hasResult = se != null && se.getScore() != null
					&& OnyxModule.existsResultSet(userCourseEnv.getCourseEnvironment().getCourseResourceableId(), node, identity, se.getAssessmentID());
			if (hasResult) {
				vc.contextPut("hasResult", Boolean.TRUE);
				boolean isPassesSet = se.getPassed() != null;
				vc.contextPut("showResultsOnHomePage", modConfig.get(IQEditController.CONFIG_KEY_RESULT_ON_HOME_PAGE));
				if (isPassesSet) {
					vc.contextPut("passed", se.getPassed());
				} else {
					vc.contextPut("passed", "");
				}
				float score = se.getScore();
				vc.contextPut("score", AssessmentHelper.getRoundedScore(score));

				Boolean fullyAssessed = se.getFullyAssessed();
				vc.contextPut("fullyAssessed", fullyAssessed);
				showOnyxReporterButton = LinkFactory.createButtonSmall("cmd.showOnyxReporter", vc, this);
			} else {
				vc.contextPut("hasResult", Boolean.FALSE);
			}
		}

		Boolean confAllowSuspension = modConfig.getBooleanEntry(CONFIG_KEY_ALLOW_SUSPENSION_ALLOWED);
		confAllowSuspension = confAllowSuspension != null ? confAllowSuspension : false;

		boolean resumeSuspended = false;
		if (confAllowSuspension) {
			QTIResultSet suspended = OnyxResultManager.getLastSuspendedQTIResultSet(ureq.getIdentity(), node);
			if (suspended != null && OnyxResultManager.isLastTestTry(suspended)) {
				resumeSuspended = true;
			}
		}

		switch (viewmode) {
		case DISCVIEW:
			// push title and learning objectives, only visible on intro page
			vc.contextPut("menuTitle", node.getShortTitle());
			vc.contextPut("displayTitle", node.getLongTitle());

			// fetch disclaimer file
			String sDisclaimer = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
			if (sDisclaimer != null) {
				setDisclaimer(sDisclaimer, ureq);
			}
			// startButton = LinkFactory.createButton("startapplet", content,
			// this);
			// startButton.setContextMenuAllowed(false);

			addStartButton(ureq, resumeSuspended);
			// <OLATCE-654>
			vc.contextPut("intro", translate("intro"));
			// </OLATCE-654>
			break;
		case ENDVIEW:
			break;
		case SURVEYVIEW:
			// fetch disclaimer file
			String sDisc = (String) modConfig.get(IQEditController.CONFIG_KEY_DISCLAIMER);
			if (sDisc != null) {
				setDisclaimer(sDisc, ureq);
			}
			// push title and learning objectives, only visible on intro page
			vc.contextPut("menuTitle", courseNodeSurvey.getShortTitle());
			vc.contextPut("displayTitle", courseNodeSurvey.getLongTitle());
			vc.contextPut("showReporter", Boolean.FALSE);

			Integer attemptsCnt = courseNodeSurvey.getUserAttempts(userCourseEnv);
			if (resumeSuspended) {
				attemptsCnt = 0;
			}

			vc.contextPut("attempts", attemptsCnt);
			// startButton = LinkFactory.createButton("startapplet", content,
			// this);
			// startButton.setContextMenuAllowed(false);
			addStartButton(ureq, resumeSuspended);

			// <OLATCE-1124>
			// <OLATBPS-96>
			if (isCourseCoach && existsResultsForSurvey()) {
				// </OLATBPS-96>
				vc.contextPut("showReporter", Boolean.TRUE);
				showOnyxReporterButton = LinkFactory.createCustomLink("cmd.showOnyxReporter", "cmd.showOnyxReporter", "onyxreporter.button.survey", Link.BUTTON_SMALL, vc, this);
			}
			break;
		}
	}

	private void addStartButton(UserRequest ureq, boolean resumeSuspended) {
		startForm = new StartButtonForm(ureq, getWindowControl(), resumeSuspended);
		listenTo(startForm);
		vc.put("startapplet", startForm.getInitialComponent());
	}

	/**
	 * @param sDisclaimer
	 * @param ureq
	 */
	private void setDisclaimer(String sDisclaimer, UserRequest ureq) {
		VFSContainer baseContainer = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
		int lastSlash = sDisclaimer.lastIndexOf('/');
		if (lastSlash != -1) {
			baseContainer = (VFSContainer) baseContainer.resolve(sDisclaimer.substring(0, lastSlash));
			sDisclaimer = sDisclaimer.substring(lastSlash);
			// first check if disclaimer exists on filesystem
			if (baseContainer == null || baseContainer.resolve(sDisclaimer) == null) {
				showWarning("disclaimer.file.invalid", sDisclaimer);
			} else {
				iFrameCtr = new IFrameDisplayController(ureq, getWindowControl(), baseContainer);
				listenTo(iFrameCtr);// dispose automatically
				vc.put("disc", iFrameCtr.getInitialComponent());
				iFrameCtr.setCurrentURI(sDisclaimer);
				vc.contextPut("hasDisc", Boolean.TRUE);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller controller, Event event) {
		if (controller == startForm) {
			// <OLATCE-654>
			startOnyx(ureq);
			// </OLATCE-654>
			return;
		} else if (controller == onyxPluginController) {

			CourseNode node = null;
			if (courseNodeTest != null) {
				node = courseNodeTest;
			} else if (courseNodeSelf != null) {
				node = courseNodeSelf;
			} else if (courseNodeSurvey != null) {
				node = courseNodeSurvey;
			}

			Boolean confAllowSuspension = modConfig.getBooleanEntry(CONFIG_KEY_ALLOW_SUSPENSION_ALLOWED);
			confAllowSuspension = confAllowSuspension != null ? confAllowSuspension : false;

			boolean resumeSuspended = false;
			if (confAllowSuspension) {
				QTIResultSet suspended = OnyxResultManager.getLastSuspendedQTIResultSet(ureq.getIdentity(), node);
				if (suspended != null && OnyxResultManager.isLastTestTry(suspended)) {
					resumeSuspended = true;
				}
			}

			startForm.setSuspended(resumeSuspended);

			if (courseNodeSurvey != null) {
				showView(ureq, SURVEYVIEW);
			} else if (courseNodeTest != null) {

				// <OLATBPS-451>
				// ScoreEvaluation se =
				// OnyxModule.getUserScoreEvaluationFromQtiResult(userCourseEnv.getCourseEnvironment().getCourseResourceableId(),
				// courseNodeTest, isBestResultConfigured(), userCourseEnv);
				// courseNodeTest.updateUserScoreEvaluation(se, userCourseEnv,
				// userCourseEnv.getIdentityEnvironment().getIdentity(), false);
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
			if (!resumeSuspended) {
				// tell the RunMainController that it has to be updated
				userCourseEnv.getScoreAccounting().evaluateAll();
			}
			fireEvent(ureq, Event.DONE_EVENT);
			Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(isAjaxEnabled);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showOnyxReporterButton) {
			onyxReporterVC = createVelocityContainer("onyxreporter");
			ScoreEvaluation eval = null;
			if (courseNodeTest != null) {
				eval = courseNodeTest.getUserScoreEvaluation(userCourseEnv);
			} else if (courseNodeSelf != null) {
				eval = courseNodeSelf.getUserScoreEvaluation(userCourseEnv);
			}
			long assessmentId = 0;
			if (eval != null) {
				assessmentId = eval.getAssessmentID();
			}
			int error = showOnyxReporter(ureq, assessmentId);
			if (error == 0) {
				CloseableModalController cmc = new CloseableModalController(getWindowControl(), "close", onyxReporterVC);
				cmc.activate();
			} else if (error == 1) {
				getWindowControl().setInfo(translate("oynxreporter.noresults"));
				log.error("could not connect to OnyxPlugin webservice");
			} else if (error == 2) {
				getWindowControl().setError(translate("onyxreporter.error"));
				log.error("could not connect to OnyxPlugin webservice");
			}
			return;
		}

		showView(ureq, ENDVIEW);
	}

	// <OLATCE-654>
	public Long startOnyx(UserRequest ureq) {
		Long assessmentId = null;

		CourseNode courseNode = courseNodeTest != null ? courseNodeTest : (courseNodeSelf != null ? courseNodeSelf : (courseNodeSurvey != null ? courseNodeSurvey : null));
		Identity student = ureq.getIdentity();

		String onyxBackLabel = "onyx.back";

		onyxPlugin = createVelocityContainer("onyxstart");

		if (courseNode instanceof IQSURVCourseNode) {
			onyxPlugin.contextPut("isSurvey", Boolean.TRUE);
			onyxBackLabel = "onyx.survey.back";
		} else {
			onyxPlugin.contextPut("isSurvey", Boolean.FALSE);
		}

		try {
			isAjaxEnabled = Windows.getWindows(ureq).getWindowManager().isAjaxEnabled();
			Windows.getWindows(ureq).getWindowManager().setAjaxEnabled(false);

			final ExamPoolManager manager = ExamPoolManager.getInstance();
			final ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());

			final Long pool = manager.getExamPoolId(course, courseNode);
			if (pool != null) {
				QTIResultSet resultSet = null;
				TestState currentState = null;

				Boolean exammode = modConfig.getBooleanEntry(ExamPoolManager.CONFIG_KEY_EXAM_CONTROL);
				exammode = exammode != null ? exammode : false;

				if (exammode) {
					resultSet = manager.getAssessmentForStudent(pool, student);
					currentState = manager.getStudentState(pool, student);
				}

				if (resultSet == null || currentState == TestState.FINISHED || currentState == TestState.CANCELED || currentState == TestState.SUSPENDED) {
					// did not find a active attempt
					// try to check for a suspended one
					Boolean confAllowSuspension = modConfig.getBooleanEntry(CONFIG_KEY_ALLOW_SUSPENSION_ALLOWED);
					confAllowSuspension = confAllowSuspension != null ? confAllowSuspension : false;
					Boolean formerlySuspended = (currentState == TestState.SUSPENDED);

					if (confAllowSuspension) {
						QTIResultSet suspended = null;
						suspended = OnyxResultManager.getLastSuspendedQTIResultSet(student, courseNode);

						if (suspended != null) {
							if (OnyxResultManager.isLastTestTry(suspended)) {
								resultSet = suspended;
								log.info("try to use last suspended resultset");
								formerlySuspended = true;
							} else {
								log.info("skip use of last suspended resultset " + suspended.getAssessmentID() + " where had been newer tries");
							}
						} else {
							log.info("no suspended resultset found");
						}
					}
					// if there is no suspended attempt, create a new one
					if (!formerlySuspended) {
						assessmentId = Long.valueOf(CodeHelper.getGlobalForeverUniqueID().hashCode());
						log.info("Create new testTry for user " + student.getName() + " assessmentId " + assessmentId);
						resultSet = OnyxResultManager.createQTIResultSet(student, courseNode, course.getResourceableId(), assessmentId);

						//now increase attempts; if an exception occurred before, this will be not reached
						if (currentState != TestState.CANCELED) {
							AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();
							am.incrementNodeAttempts(courseNode, student, userCourseEnv);
						}
					} else {
						assessmentId = resultSet.getAssessmentID();
						log.info("Resume suspended testTry for user " + student.getName() + " assessmentId " + assessmentId);
					}

					// register attempt at exampool
					manager.addStudentToExamPool(course, courseNode, student, formerlySuspended ? TestState.SUSPENDED : TestState.WAITING, resultSet);
				} else {
					assessmentId = resultSet.getAssessmentID();
					log.info("Reuse testTry for user " + student.getName() + " assessmentId " + assessmentId);
					List<Identity> identities = new ArrayList<Identity>();
					identities.add(student);
					manager.controllExam(pool, identities, TestState.RESUME_REQUESTED);
				}

				String onyxRunURL = OnyxModule.getUserViewLocation() + "?id=" + assessmentId;
				log.info(onyxRunURL);

				onyxPlugin.contextPut("urlonyxplugin", onyxRunURL);

				onyxPluginController = new CloseableModalController(getWindowControl(), translate("close"), onyxPlugin, true);
				// Link closeLink = onyxPluginController.getCloseLink();
				// onyxPlugin.contextPut("closeBtnId",
				// closeLink.getElementId());
				onyxPlugin.contextPut("showHint", Boolean.TRUE);
				onyxPluginController.setCustomWindowCSS("onyx_overlay");
				onyxBack = LinkFactory.createCustomLink("onyx.back", "onyx.back", onyxBackLabel, Link.BUTTON_SMALL, onyxPlugin, onyxPluginController);

				listenTo(onyxPluginController);
				onyxPluginController.activate();
			} else {
				log.error("unable to connect to onyxws");
				getWindowControl().setError(translate("error.connectonyxws"));
			}

		} catch (Exception e) {
			getWindowControl().setError(translate("error.connectonyxws"));
			log.error("could not connect to OnyxPlugin webservice", e);
		}
		return assessmentId;
	}

	// </OLATCE-654>
	/**
	 * This methods calls the OnyxReporter and shows it in an iframe.
	 * 
	 * @param ureq
	 *            The UserRequest for getting the identity and role of the
	 *            current user.
	 * @return 0 OK 1 NO RESULTS 2 ERROR
	 */
	private int showOnyxReporter(UserRequest ureq, long assasmentId) {
		// <ONYX-705>
		OnyxReporterConnector onyxReporter = null;
		try {
			onyxReporter = new OnyxReporterConnector();
		} catch (OnyxReporterException e) {
			log.error(e.getMessage(), e);
		}
		// </ONYX-705>
		if (onyxReporter != null) {
			List<Identity> identity = new ArrayList<Identity>();
			String iframeSrc = "";
			try {
				if (courseNodeTest != null) {
					identity.add(userCourseEnv.getIdentityEnvironment().getIdentity());
					iframeSrc = onyxReporter.startReporterGUI(ureq.getIdentity(), identity, courseNodeTest, assasmentId, ReporterRole.STUDENT);
				} else if (courseNodeSelf != null) {
					identity.add(userCourseEnv.getIdentityEnvironment().getIdentity());
					iframeSrc = onyxReporter.startReporterGUI(ureq.getIdentity(), identity, courseNodeSelf, assasmentId, ReporterRole.STUDENT);
				} else {
					iframeSrc = onyxReporter.startReporterGUIForSurvey(ureq.getIdentity(), courseNodeSurvey, getSurveyResultPath());
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
	 * This method checks if the directory where the results are stored for this
	 * survey exists.
	 * 
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
	 * 
	 * @return A String representing the path
	 */
	private String getSurveyResultPath() {
		return userCourseEnv.getCourseEnvironment().getCourseBaseContainer().getBasefile() + File.separator + courseNodeSurvey.getIdent() + File.separator;
	}

	// </OLATBPS-96>

	/**
	 * Static metohd to start the an onyx test as learningressource or bookmark.
	 * 
	 * @param ureq
	 * @param repositoryEntry
	 */
	// <OLATCE-1054>
	public VelocityContainer showOnyxTestInModalController(UserRequest ureq, RepositoryEntry entry, boolean activateModalController) {
		// </OLATCE-1054>
		OnyxPluginServices onyxplugin = null;
		try {
			onyxplugin = new PluginService().getOnyxPluginServicesPort();
		} catch (Exception e) {
			getWindowControl().setError(translate("error.connectonyxws"));
			log.warn("could not connect to OnyxPlugin webservice", e);
			return null;
		}

		String CP = getCP(entry.getOlatResource());
		String language = ureq.getLocale().toString().toLowerCase();
		String tempalteId = "onyxdefault";

		this.uniqueId = OnyxResultManager.getUniqueIdForShowOnly(ureq.getIdentity(), entry);

		java.io.FileInputStream inp = null;
		try {
			File cpFile = new File(CP);
			Long fileLength = cpFile.length();
			byte[] byteArray = new byte[fileLength.intValue()];
			inp = new java.io.FileInputStream(cpFile);
			inp.read(byteArray);
			onyxplugin.run(this.uniqueId, byteArray, language, "", tempalteId, OnyxModule.getConfigName(), true);
		} catch (FileNotFoundException e) {
			log.error("Cannot find CP of Onyx Test with assassmentId: " + uniqueId, e);
		} catch (IOException e) {
			log.error("Cannot find CP of Onyx Test with assassmentId: " + uniqueId, e);
		} finally {
			if (inp != null) {
				try {
					inp.close();
				} catch (IOException e) {
					log.error("Unable to close input-stream ", e);
				}
			}
		}

		String urlonyxplugin = OnyxModule.getUserViewLocation() + "?id=" + this.uniqueId;

		onyxPlugin = createVelocityContainer("onyxstart");
		onyxPlugin.contextPut("isSurvey", Boolean.FALSE);
		onyxPlugin.contextPut("urlonyxplugin", urlonyxplugin);
		onyxPlugin.contextPut("nohint", Boolean.TRUE);
		// <OLATCE-99>
		onyxPluginController = new CloseableModalController(getWindowControl(), translate("close"), onyxPlugin, true);
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

	private static String getCP(OLATResourceable fileResource) {
		// get content-package (= onyx test zip-file)
		// OLATResourceable fileResource = repositoryEntry.getOlatResource();
		String unzipedDir = FileResourceManager.getInstance().unzipFileResource(fileResource).getAbsolutePath();
		String zipdirName = FileResourceManager.ZIPDIR;

		// String name = repositoryEntry.getResourcename();//getDisplayname();
		String name = FileResourceManager.getInstance().getFileResource(fileResource).getName();
		String pathToFile = unzipedDir.substring(0, unzipedDir.indexOf(zipdirName));
		String completePath = (pathToFile + name);
		File cpFile = new File(completePath);
		if (!cpFile.exists()) {
			// look for imported file
			String importedFileName = "repo.zip";
			File impFile = new File(pathToFile + importedFileName);
			if (impFile.exists()) {
				impFile.renameTo(cpFile);
			} else {
				log.error("Cannot open Onyx CP File: " + completePath + " , also the imported repo.zip is not here!");
			}
		}
		return completePath;
	}

	@Override
	protected void doDispose() {
	}

	class StartButtonForm extends FormBasicController {

		private FormLink startButton;
		private boolean resumeSuspended;
		private final static String START_LABEL = "start";
		private final static String RESUME_LABEL = "resume";

		public StartButtonForm(UserRequest ureq, WindowControl wControl, boolean resumeSuspended) {
			super(ureq, wControl, LAYOUT_BAREBONE);
			this.resumeSuspended = resumeSuspended;
			initForm(ureq);
		}

		public void setSuspended(boolean resumeSuspended) {
			this.resumeSuspended = resumeSuspended;
			startButton.setI18nKey(resumeSuspended ? RESUME_LABEL : START_LABEL);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			startButton = FormUIFactory.getInstance().addFormLink(START_LABEL, resumeSuspended ? RESUME_LABEL : START_LABEL, null, flc, Link.BUTTON);
			startButton.setPrimary(true);
			startButton.addActionListener(FormEvent.ONCLICK);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			fireEvent(ureq, Event.DONE_EVENT);
		}

		public FormLink getStartButton() {
			return startButton;
		}

		public void setStartButton(FormLink startButton) {
			this.startButton = startButton;
		}

	}
}
