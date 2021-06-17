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
package org.olat.course.nodes.survey.ui;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.survey.SurveyManager;
import org.olat.course.nodes.survey.SurveyRunSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.forms.ui.ProgressEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyRunController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(SurveyRunController.class);
	
	private VelocityContainer mainVC;
	private Link resetLink;
	
	private CloseableModalController cmc;
	private SurveyDeleteDataConfirmationController deleteDataConfirmationCtrl;
	private EvaluationFormExecutionController executionCtrl;
	
	private final UserCourseEnvironment userCourseEnv;
	private final RepositoryEntry courseEntry;
	private final SurveyCourseNode courseNode;
	private final SurveyRunSecurityCallback secCallback;
	private final EvaluationFormSurveyIdentifier surveyIdent;
	private EvaluationFormSurvey survey;
	private EvaluationFormParticipation participation;
	
	@Autowired
	private SurveyManager surveyManager;

	public SurveyRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			SurveyCourseNode courseNode, SurveyRunSecurityCallback secCallback) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		this.courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		this.surveyIdent = surveyManager.getSurveyIdentifier(courseNode, courseEntry);
		this.secCallback = secCallback;

		mainVC = createVelocityContainer("run");
		putInitialPanel(mainVC);

		initVelocityContainer(ureq);
	}

	private void initVelocityContainer(UserRequest ureq) {
		mainVC.clear();
		
		if (secCallback.canResetAll()) {
			resetLink = LinkFactory.createButtonSmall("run.reset", mainVC, this);
			resetLink.setIconLeftCSS("o_icon o_icon-fw o_icon_surv_reset");
			mainVC.contextPut("withCmds", Boolean.TRUE);
		}
		
		survey = surveyManager.loadSurvey(surveyIdent);
		if (survey == null) {
			String title = getTranslator().translate("run.no.survey.title");
			String message = getTranslator().translate("run.no.survey.message");
			doShowMessage(ureq, title, message);
			log.warn("Published survey course node has no survey in the database!");
			return;
		}
		
		doShowView(ureq);
	}

	private void doShowView(UserRequest ureq) {
		if (secCallback.canParticipate()) {
			participation = loadOrCreateParticipation(ureq);
		}
		if (secCallback.canViewReporting(participation)) {
			doShowReporting(ureq);
		} else if (secCallback.hasParticipated(participation)) {
			doShowParticipationDone(ureq);
		} else if (secCallback.isReadOnly()) {
			doShowReadOnly(ureq);
		} else if (secCallback.canExecute(participation)) {
			doShowExecution(ureq);
		} else {
			doShowNoAccess(ureq);
		}
	}

	private EvaluationFormParticipation loadOrCreateParticipation(UserRequest ureq) {
		if (secCallback.isGuestOnly()) {
			return surveyManager.loadOrCreateGuestParticipation(survey, ureq.getUserSession());
		}
		return surveyManager.loadOrCreateParticipation(survey, getIdentity());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == resetLink) {
			doConfirmDeleteAllData(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl) {
			if (event == Event.DONE_EVENT) {
				doExecutionFinished(ureq);
			} else if (event instanceof ProgressEvent) {
				ProgressEvent pe = (ProgressEvent)event;
				doQuickSaved(ureq, pe.getProgress());
			}
		} else if(source == deleteDataConfirmationCtrl) {
			if (event == Event.DONE_EVENT) {
				doDeleteAllData(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} 
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(deleteDataConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		deleteDataConfirmationCtrl = null;
		cmc = null;
	}

	private void doExecutionFinished(UserRequest ureq) {
		surveyManager.onExecutionFinished(courseNode, userCourseEnv);
		doShowView(ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doQuickSaved(UserRequest ureq, Double completion) {
		surveyManager.onQuickSave(courseNode, userCourseEnv, completion);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doConfirmDeleteAllData(UserRequest ureq) {
		Long allSessions = surveyManager.getSessionsCount(SessionFilterFactory.create(survey));
		Long doneSessions = surveyManager.getSessionsCount(SessionFilterFactory.createSelectDone(survey));
		deleteDataConfirmationCtrl = new SurveyDeleteDataConfirmationController(ureq, getWindowControl(), allSessions, doneSessions);
		listenTo(deleteDataConfirmationCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				deleteDataConfirmationCtrl.getInitialComponent(), true, translate("run.command.delete.data.all.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doDeleteAllData(UserRequest ureq) {
		surveyManager.deleteAllData(survey, courseNode, userCourseEnv);
		initVelocityContainer(ureq);
	}

	private void doShowExecution(UserRequest ureq) {
		removeAllComponents();
		EvaluationFormSession session = surveyManager.loadOrCreateSesssion(participation);
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, null);
		listenTo(executionCtrl);
		mainVC.put("execution", executionCtrl.getInitialComponent());
	}
	
	private void doShowReporting(UserRequest ureq) {
		removeAllComponents();
		participation = loadOrCreateParticipation(ureq);
		Controller reportingCtrl = new SurveyReportingController(ureq, getWindowControl(), courseEntry, courseNode, survey);
		mainVC.put("reporting", reportingCtrl.getInitialComponent());
	}

	private void doShowNoAccess(UserRequest ureq) {
		String title = getTranslator().translate("run.noaccess.title");
		String message = getTranslator().translate("run.noaccess.message");
		doShowMessage(ureq, title, message);
	}

	private void doShowReadOnly(UserRequest ureq) {
		Translator trans = Util.createPackageTranslator(SurveyCourseNode.class, getLocale());
		String title = trans.translate("freezenoaccess.title");
		String message = trans.translate("freezenoaccess.message");
		doShowMessage(ureq, title, message);
	}

	private void doShowParticipationDone(UserRequest ureq) {
		String title = getTranslator().translate("run.participation.done.title");
		String message = getTranslator().translate("run.participation.done.message");
		doShowMessage(ureq, title, message);
	}
	
	private void doShowMessage(UserRequest ureq, String title, String message) {
		removeAllComponents();
		Controller ctrl = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), title, message);
		mainVC.put("message", ctrl.getInitialComponent());
	}
	
	private void removeAllComponents() {
		mainVC.remove("message");
		mainVC.remove("execution");
		mainVC.remove("reporting");
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(executionCtrl);
		executionCtrl = null;
	}

}
