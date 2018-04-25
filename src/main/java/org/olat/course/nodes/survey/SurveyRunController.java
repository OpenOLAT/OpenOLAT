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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyRunController extends BasicController {
	
	private final VelocityContainer mainVC;
	private EvaluationFormExecutionController executionCtrl;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public SurveyRunController(UserRequest ureq, WindowControl wControl, OLATResourceable ores,
			SurveyCourseNode courseNode) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("run");

		EvaluationFormSession session = loadOrCreateSesssion(ores, courseNode);
		executionCtrl = new EvaluationFormExecutionController(ureq, wControl, session);
		mainVC.put("execution", executionCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	private EvaluationFormSession loadOrCreateSesssion(OLATResourceable ores, SurveyCourseNode courseNode) {
		EvaluationFormSession session = evaluationFormManager.loadSession(ores, courseNode.getIdent(), getIdentity());
		if (session == null) {
			session = evaluationFormManager.createSession(ores, courseNode.getIdent(), getIdentity(),
					courseNode.getReferencedRepositoryEntry());
		}
		return session;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
