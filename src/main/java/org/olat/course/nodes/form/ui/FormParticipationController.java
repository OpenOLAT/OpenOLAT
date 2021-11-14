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
package org.olat.course.nodes.form.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.learningpath.ui.CoachedIdentityLargeInfosController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormParticipationController extends BasicController {

	private static final EmptyStateConfig EMPTY_STATE = EmptyStateConfig.builder()
			.withIconCss(FormCourseNode.ICON_CSS)
			.withMessageI18nKey("form.not.filled.in")
			.build();
	
	private CoachedIdentityLargeInfosController coachedIdentityLargeInfosCtrl;
	private EvaluationFormExecutionController executionCtrl;
	
	@Autowired
	private FormManager formManager;

	public FormParticipationController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment coachedCourseEnv) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("participation");
		String courseTitle = coachedCourseEnv.getCourseEnvironment().getCourseTitle();
		mainVC.contextPut("courseTitle", courseTitle);
		
		coachedIdentityLargeInfosCtrl = new CoachedIdentityLargeInfosController(ureq, wControl, coachedCourseEnv);
		listenTo(coachedIdentityLargeInfosCtrl);
		mainVC.put("user", coachedIdentityLargeInfosCtrl.getInitialComponent());
		
		RepositoryEntry courseEntry = coachedCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		EvaluationFormSurveyIdentifier surveyIdent = formManager.getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdent);
		EvaluationFormParticipation participation = formManager.loadParticipation(survey,
				coachedCourseEnv.getIdentityEnvironment().getIdentity());
		if (participation != null) {
			EvaluationFormSession session = formManager.loadOrCreateSession(participation);
			if (session.getEvaluationFormSessionStatus() == EvaluationFormSessionStatus.done) {
				executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, true, false,
						false, FormCourseNode.EMPTY_STATE);
				listenTo(executionCtrl);
				mainVC.put("evaluationForm", executionCtrl.getInitialComponent());
			}
		} else {
		}
		EmptyStateFactory.create("emptyState", mainVC, this, EMPTY_STATE);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
