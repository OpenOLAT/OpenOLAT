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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
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
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormRunController extends BasicController {
	
	private EvaluationFormExecutionController executionCtrl;
	private MessageController messageCtrl;
	
	private final FormCourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;

	@Autowired
	private FormManager formManager;

	public FormRunController(UserRequest ureq, WindowControl wControl, FormCourseNode courseNode,
			UserCourseEnvironment userCourseEnv, RepositoryEntry referencedEntry) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		EvaluationFormSurveyIdentifier surveyIdent = formManager.getSurveyIdentifier(courseNode, courseEntry);
		EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdent);
		if (RepositoryEntryStatusEnum.deleted == referencedEntry.getEntryStatus()
				|| RepositoryEntryStatusEnum.trash == referencedEntry.getEntryStatus()) {
			EmptyStateConfig emptyState = EmptyStateConfig.builder()
					.withIconCss("o_icon_form")
					.withIndicatorIconCss("o_icon_deleted")
					.withMessageI18nKey("error.form.deleted")
					.build();
			EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
			emptyStateCmp.setTranslator(getTranslator());
			putInitialPanel(emptyStateCmp);
			return;
		}
		if (checkDeadline()) {
			EvaluationFormParticipation participation = formManager.loadOrCreateParticipation(survey, getIdentity());
			EvaluationFormSession session = formManager.loadOrCreateSession(participation);
			executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, FormCourseNode.EMPTY_STATE);
			listenTo(executionCtrl);
			putInitialPanel(executionCtrl.getInitialComponent());
		} else {
			EvaluationFormSession session = formManager.getDoneSession(survey, getIdentity());
			if (session != null) {
				executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, FormCourseNode.EMPTY_STATE);
				listenTo(executionCtrl);
				putInitialPanel(executionCtrl.getInitialComponent());
			} else {
				String title = translate("participation.deadline.over.title");
				String text = translate("participation.deadline.over.text");
				messageCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, text);
				listenTo(messageCtrl);
				putInitialPanel(messageCtrl.getInitialComponent());
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
				doExecutionFinished(ureq);
			} else if (event instanceof ProgressEvent) {
				ProgressEvent pe = (ProgressEvent)event;
				doQuickSaved(ureq, pe.getProgress());
			}
		}
		super.event(ureq, source, event);
	}

	private boolean checkDeadline() {
		Date deadline = formManager.getParticipationDeadline(courseNode,
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				userCourseEnv.getIdentityEnvironment().getIdentity());
		return deadline == null || deadline.after(new Date()) || userCourseEnv.isCourseReadOnly();
	}

	private void doExecutionFinished(UserRequest ureq) {
		formManager.onExecutionFinished(courseNode, userCourseEnv);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doQuickSaved(UserRequest ureq, Double completion) {
		formManager.onQuickSave(courseNode, userCourseEnv, completion);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
