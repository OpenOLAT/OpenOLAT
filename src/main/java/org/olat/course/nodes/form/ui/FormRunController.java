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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.forms.ui.ProgressEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormRunController extends BasicController {
	
	private final EvaluationFormExecutionController executionCtrl;
	
	private final FormCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;

	@Autowired
	private FormManager formManager;

	public FormRunController(UserRequest ureq, WindowControl wControl, FormCourseNode courseNode,
			UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		EvaluationFormSurveyIdentifier surveyIdent = formManager.getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdent);
		EvaluationFormParticipation participation = formManager.loadOrCreateParticipation(survey, getIdentity());
		EvaluationFormSession session = formManager.loadOrCreateSesssion(participation);
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, FormCourseNode.EMPTY_STATE);
		listenTo(executionCtrl);
		putInitialPanel(executionCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl) {
			if (event == Event.DONE_EVENT) {
				doExecutionFinished(ureq);
			} else if (event instanceof ProgressEvent) {
				ProgressEvent pe = (ProgressEvent)event;
				doQuickSaved(pe.getProgress());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		//
	}

	private void doExecutionFinished(UserRequest ureq) {
		formManager.onExecutionFinished(courseNode, userCourseEnv);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doQuickSaved(Double completion) {
		formManager.onQuickSave(courseNode, userCourseEnv, completion);
	}

}
