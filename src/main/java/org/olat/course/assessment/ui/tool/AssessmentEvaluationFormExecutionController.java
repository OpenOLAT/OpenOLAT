/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.tool;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ms.AuditEnv;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.forms.ui.ProgressEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEvaluationFormExecutionController extends BasicController {

	private EvaluationFormSession session;
	private final CourseNode courseNode;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	private final EvaluationFormExecutionController executionCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private MSService msService;
	
	public AssessmentEvaluationFormExecutionController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv, boolean edit, boolean reopen,
			CourseNode courseNode, EvaluationFormProvider evaluationFormProvider) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.assessedUserCourseEnv = assessedUserCourseEnv;

		ModuleConfiguration config = courseNode.getModuleConfiguration();
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		UserNodeAuditManager auditManager = assessedUserCourseEnv.getCourseEnvironment().getAuditManager();
		AuditEnv auditEnv = AuditEnv.of(auditManager , courseNode, assessedIdentity, getIdentity(), Role.coach);

		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(config);
		RepositoryEntry ores = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		String nodeIdent = courseNode.getIdent();
		session =  msService.getOrCreateSession(formEntry, ores, nodeIdent, evaluationFormProvider, assessedIdentity, auditEnv);
		if(edit && session.getEvaluationFormSessionStatus() == EvaluationFormSessionStatus.done) {
			if(reopen) {
				session = msService.reopenSession(session, auditEnv);
			} else {
				edit = false;
			}
		}
	
		boolean readOnly = !edit;
		boolean showDoneButton = edit;
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), null, null, session, null, null,
				readOnly, showDoneButton, false, null);
		executionCtrl.setSaveDisplayText(translate("save"));
		listenTo(executionCtrl);
		
		VelocityContainer mainVC = createVelocityContainer("evaluation_form_execution");
		mainVC.put("execution", executionCtrl.getInitialComponent());
		
		EvaluationFormSessionStatus evaluationFormStatus = session == null ? null : session.getEvaluationFormSessionStatus();
		String status = new EvaluationFormSessionStatusCellRenderer(getLocale(), true, true, false).render(evaluationFormStatus);
		mainVC.contextPut("status", status);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(executionCtrl == source) {
			if(Event.DONE_EVENT.equals(event)) {
				doSetAssessmentScore();
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof ProgressEvent) {
				fireEvent(ureq, event);
			}
		}
	}
	
	private void doSetAssessmentScore() {
		session = msService.getSession(session);
		if (courseNode instanceof MSCourseNode msCourseNode) {
			msCourseNode.updateScoreEvaluation(getIdentity(), assessedUserCourseEnv, Role.coach, session, getLocale());
		} else if (courseNode instanceof GTACourseNode gtaCourseNode) {
			gtaCourseNode.updateScoreEvaluation(getIdentity(), assessedUserCourseEnv, Role.coach, session, getLocale());
		}
		dbInstance.commit();
	}
}
