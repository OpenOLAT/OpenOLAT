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
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormDispatcher;
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
public class StandaloneExecutionController extends BasicController {

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
		
		// PUBLIC_PARTICIPATION_PATH is used as special marker for not available public participations
		if (identifier != null && EvaluationFormDispatcher.PUBLIC_PARTICIPATION_PATH.equalsIgnoreCase(identifier.getType())) {
			doShowNotExecuteable();
			log.debug("Public participation not executeable: {}", identifier);
			return;
		}
		
		EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByIdentifier(identifier);
		if (participation == null) {
			doShowNotFound();
			log.debug("No participation found for {}", identifier);
		} else if (EvaluationFormParticipationStatus.done.equals(participation.getStatus())) {
			doShowAlreadyDone();
			log.debug("Participation already done: {}", identifier);
		} else {
			OLATResourceable surveyOres = participation.getSurvey().getIdentifier().getOLATResourceable();
			EvaluationFormStandaloneProvider standaloneProvider = standaloneProviderFactory.getProvider(surveyOres);
			if (standaloneProvider.isExecutable(participation)) {
				doShowExecution(ureq, participation);
				log.debug("Execute evaluation form with {}", identifier);
			} else {
				doShowNotExecuteable();
				log.debug("Participation not executeable ({}): {}", standaloneProvider, identifier);
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
				doShowDoneNow();
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
        super.doDispose();
	}

	private void doShowExecution(UserRequest ureq, EvaluationFormParticipation participation) {
		OLATResourceable surveyOres = participation.getSurvey().getIdentifier().getOLATResourceable();
		EvaluationFormStandaloneProvider standaloneProvider = standaloneProviderFactory.getProvider(surveyOres);
		headerCtrl = standaloneProvider.getExecutionHeader(ureq, getWindowControl(), participation);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		EvaluationFormSession session = loadOrCreateSesssion(participation);
		ExecutionIdentity executionIdentity;
		if (participation.getExecutor() != null) {
			executionIdentity = ExecutionIdentity.ofIdentity(participation.getExecutor());
		} else if (StringHelper.containsNonWhitespace(participation.getEmail())) {
			executionIdentity = ExecutionIdentity.ofEmail(participation.getEmail());
		} else if (getIdentity() != null) {
			executionIdentity = ExecutionIdentity.ofIdentity(getIdentity());
		} else {
			executionIdentity = ExecutionIdentity.ofNone();
		}
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), null, null, session,
				executionIdentity, null, null, false, true, false, false, null);
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

	private void doShowNotFound() {
		doShowMessage(null, null, "message.not.found", null);
	}

	private void doShowAlreadyDone() {
		doShowMessage("o_icon_qual_part_participated", "o_no_icon", "message.participated.earlier", "message.participated.earlier.hint");
	}

	private void doShowDoneNow() {
		doShowMessage("o_icon_qual_part_participated", "o_no_icon", "message.participated.now", "message.participated.now.hint");
	}
	
	private void doShowNotExecuteable() {
		doShowMessage(null, "o_no_icon", "message.not.executable", "message.not.executable.hint");
	}
	
	private void doShowMessage(String iconCss, String indicatorIconCss, String messageI18nKey, String hintI18nKey) {
		mainVC.clear();
		
		EmptyStateConfig emptyState = EmptyStateConfig.builder()
				.withIconCss(iconCss)
				.withIndicatorIconCss(indicatorIconCss)
				.withMessageI18nKey(messageI18nKey)
				.withHintI18nKey(hintI18nKey)
				.build();
		EmptyState doneState = EmptyStateFactory.create("doneState", mainVC, this, emptyState);
		doneState.setTranslator(getTranslator());
	}

}
