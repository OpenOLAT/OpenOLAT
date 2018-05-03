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
package org.olat.course.nodes.survey;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyRunController extends BasicController {
	
	private VelocityContainer mainVC;
	private Link commandsLink;
	
	private CloseableCalloutWindowController calloutCtrl;
	private SurveyRunCommandsController commandsCtrl;
	private CloseableModalController cmc;
	private SurveyDeleteDataConfirmationController deleteDataConfirmationCtrl;
	private EvaluationFormExecutionController executionCtrl;
	
	private final OLATResourceable ores;
	private final String subIdent;
	private final SurveyRunSecurityCallback secCallback;
	private EvaluationFormSurvey survey;
	private EvaluationFormParticipation participation;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public SurveyRunController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, SurveyCourseNode courseNode,
			SurveyRunSecurityCallback secCallback) {
		super(ureq, wControl);
		this.ores = ores;
		this.subIdent = courseNode.getIdent();
		this.secCallback = secCallback;

		mainVC = createVelocityContainer("run");
		putInitialPanel(mainVC);

		initVelocityContainer(ureq);
	}

	private void initVelocityContainer(UserRequest ureq) {
		mainVC.clear();
		
		if (secCallback.canRunCommands()) {
			commandsLink = LinkFactory.createButton("run.commands", mainVC, this);
			commandsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_surv_commands");
		}
		
		survey = evaluationFormManager.loadSurvey(ores, subIdent);
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
			String sessionId = ureq.getUserSession().getSessionInfo().getSession().getId();
			EvaluationFormParticipationIdentifier identifier = new EvaluationFormParticipationIdentifier("course-node", sessionId);
			return loadOrCreateParticipation(identifier);
		}
		return loadOrCreateParticipation(getIdentity());
	}

	private EvaluationFormParticipation loadOrCreateParticipation(EvaluationFormParticipationIdentifier identifier) {
		EvaluationFormParticipation loadedParticipation = evaluationFormManager.loadParticipationByIdentifier(survey, identifier);
		if (loadedParticipation == null) {
			loadedParticipation = evaluationFormManager.createParticipation(survey, identifier);
			loadedParticipation.setAnonymous(true);
			loadedParticipation = evaluationFormManager.updateParticipation(loadedParticipation);
		}
		return loadedParticipation;
	}

	private EvaluationFormParticipation loadOrCreateParticipation(Identity executor) {
		EvaluationFormParticipation loadedParticipation = evaluationFormManager.loadParticipationByExecutor(survey, executor);
		if (loadedParticipation == null) {
			loadedParticipation = evaluationFormManager.createParticipation(survey, executor);
			loadedParticipation.setAnonymous(true);
			loadedParticipation = evaluationFormManager.updateParticipation(loadedParticipation);
		}
		return loadedParticipation;
	}

	private EvaluationFormSession loadOrCreateSesssion(EvaluationFormParticipation participation) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == commandsLink) {
			doOpenCommands(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl && event == Event.DONE_EVENT) {
			doShowReporting(ureq);
		} else if(source == commandsCtrl) {
			calloutCtrl.deactivate();
			if(SurveyRunCommandsController.EVENT_DELETE_ALL_DATA.equals(event.getCommand())) {
				doConfirmDeleteAllData(ureq);
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
		removeAsListenerAndDispose(commandsCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(cmc);
		deleteDataConfirmationCtrl = null;
		commandsCtrl = null;
		calloutCtrl = null;
		cmc = null;
	}

	private void doConfirmDeleteAllData(UserRequest ureq) {
		deleteDataConfirmationCtrl = new SurveyDeleteDataConfirmationController(ureq, getWindowControl());
		listenTo(deleteDataConfirmationCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				deleteDataConfirmationCtrl.getInitialComponent(), true, translate("run.command.delete.data.all.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doDeleteAllData(UserRequest ureq) {
		evaluationFormManager.deleteAllData(survey);
		initVelocityContainer(ureq);
	}

	private void doOpenCommands(UserRequest ureq) {
		removeAsListenerAndDispose(commandsCtrl);
		commandsCtrl = new SurveyRunCommandsController(ureq, getWindowControl());
		listenTo(commandsCtrl);
		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), commandsCtrl.getInitialComponent(),
				commandsLink, "", true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}

	private void doShowExecution(UserRequest ureq) {
		removeAllComponents();
		EvaluationFormSession session = loadOrCreateSesssion(participation);
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session);
		listenTo(executionCtrl);
		mainVC.put("execution", executionCtrl.getInitialComponent());
	}
	
	private void doShowReporting(UserRequest ureq) {
		removeAllComponents();
		participation = loadOrCreateParticipation(ureq);
		Controller reportingCtrl = new SurveyReportingController(ureq, getWindowControl(), survey);
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
