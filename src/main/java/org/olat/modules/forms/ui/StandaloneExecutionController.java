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
package org.olat.modules.forms.ui;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationIdentifier;
import org.olat.modules.forms.EvaluationFormParticipationStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormStandaloneProvider;
import org.olat.modules.forms.EvaluationFormStandaloneProviderFactory;
import org.olat.modules.forms.ui.model.ExecutionIdentity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class StandaloneExecutionController extends BasicController implements Controller {

	private static final Logger log = Tracing.createLoggerFor(StandaloneExecutionController.class);
	
	public static final String PARTICIPATION_IDENTIFIER = "evaluation-form-participation-identifier";
	
	private VelocityContainer mainVC;
	private Controller headerCtrl;
	private Controller executionCtrl;
	private Controller messageCtrl;

	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private EvaluationFormStandaloneProviderFactory standaloneProviderFactory;

	public StandaloneExecutionController(UserRequest ureq, WindowControl wControl, EvaluationFormParticipationIdentifier identifier) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("standalone");
		putInitialPanel(mainVC);
		
		EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByIdentifier(identifier);
		if (participation == null) {
			doShowNotFound(ureq);
			log.debug("No participation found for " + identifier);
		} else if (EvaluationFormParticipationStatus.done.equals(participation.getStatus())) {
			doShowAlreadyDone(ureq);
			log.debug("Participation already done: " + identifier);
		} else {
			OLATResourceable surveyOres = participation.getSurvey().getIdentifier().getOLATResourceable();
			EvaluationFormStandaloneProvider standaloneProvider = standaloneProviderFactory.getProvider(surveyOres);
			if (standaloneProvider.isExecutable(participation)) {
				doShowExecution(ureq, participation);
				log.debug("Execute evaluation form with " + identifier);
			} else {
				doShowNotExecuteable(ureq);
				log.debug("Participation not executeable (" + standaloneProvider + "): " + identifier);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl) {
			if (event == Event.DONE_EVENT) {
				doShowDoneNow(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(executionCtrl);
		removeAsListenerAndDispose(messageCtrl);
		removeAsListenerAndDispose(headerCtrl);
		executionCtrl = null;
		messageCtrl = null;
		headerCtrl = null;
	}

	private void doShowExecution(UserRequest ureq, EvaluationFormParticipation participation) {
		OLATResourceable surveyOres = participation.getSurvey().getIdentifier().getOLATResourceable();
		EvaluationFormStandaloneProvider standaloneProvider = standaloneProviderFactory.getProvider(surveyOres);
		headerCtrl = standaloneProvider.getExecutionHeader(ureq, getWindowControl(), participation);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		EvaluationFormSession session = loadOrCreateSesssion(participation);
		Identity executor = participation.getExecutor() != null? participation.getExecutor(): getIdentity();
		ExecutionIdentity executionIdentity = new ExecutionIdentity(executor);
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), null, null, session, executionIdentity, null, false, true);
		listenTo(executionCtrl);
		mainVC.put("execution", executionCtrl.getInitialComponent());
	}
	
	private EvaluationFormSession loadOrCreateSesssion(EvaluationFormParticipation participation) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participation);
		if (session == null) {
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}

	private void doShowNotFound(UserRequest ureq) {
		doShowMessage(ureq, "standalone.not.found");
	}

	private void doShowAlreadyDone(UserRequest ureq) {
		doShowMessage(ureq, "standalone.already.done");
	}

	private void doShowDoneNow(UserRequest ureq) {
		doShowMessage(ureq, "standalone.done.now");
	}
	private void doShowNotExecuteable(UserRequest ureq) {
		doShowMessage(ureq, "standalone.not.executable");
	}
	
	private void doShowMessage(UserRequest ureq, String i18nKey) {
		mainVC.clear();
		removeAsListenerAndDispose(messageCtrl);
		messageCtrl = MessageUIFactory.createInfoMessage(ureq, getWindowControl(), null, translate(i18nKey));
		listenTo(messageCtrl);
		mainVC.put("message", messageCtrl.getInitialComponent());
	}

}
