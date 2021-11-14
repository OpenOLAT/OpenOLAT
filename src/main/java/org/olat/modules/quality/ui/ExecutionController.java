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
package org.olat.modules.quality.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExecutionController extends BasicController {

	private VelocityContainer mainVC;
	private Link back;
	private Controller headerCtrl;
	private Controller executionCtrl;
	
	private final QualityExecutorParticipation qualityParticipation;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public ExecutionController(UserRequest ureq, WindowControl wControl, QualityExecutorParticipation qualityParticipation) {
		super(ureq, wControl);
		this.qualityParticipation = qualityParticipation;
		
		mainVC = createVelocityContainer("execution");
		initVelocityContainer(ureq);
		putInitialPanel(mainVC);
	}

	protected void initVelocityContainer(UserRequest ureq) {
		back = LinkFactory.createLinkBack(mainVC, this);
		mainVC.put("back", back);
		
		headerCtrl = new ExecutionHeaderController(ureq, getWindowControl(), qualityParticipation);
		listenTo(headerCtrl);
		mainVC.put("header", headerCtrl.getInitialComponent());
		
		EvaluationFormSession session = loadOrCreateSession(qualityParticipation.getParticipationRef());
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, null);
		listenTo(executionCtrl);
		mainVC.put("execution", executionCtrl.getInitialComponent());
	}

	private EvaluationFormSession loadOrCreateSession(EvaluationFormParticipationRef participationRef) {
		EvaluationFormSession session = evaluationFormManager.loadSessionByParticipation(participationRef);
		if (session == null) {
			EvaluationFormParticipation participation = evaluationFormManager.loadParticipationByKey(participationRef);
			session = evaluationFormManager.createSession(participation);
		}
		return session;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl && event == Event.DONE_EVENT) {
			fireEvent(ureq, event);
		} 
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == back) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}

	@Override
	protected void doDispose() {
		removeAsListenerAndDispose(executionCtrl);
		removeAsListenerAndDispose(headerCtrl);
		executionCtrl = null;
		headerCtrl = null;
        super.doDispose();
	}

}
