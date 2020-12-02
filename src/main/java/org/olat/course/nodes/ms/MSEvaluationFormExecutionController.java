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
package org.olat.course.nodes.ms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.ui.tool.AssessmentFormCallback;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSEvaluationFormExecutionController extends BasicController implements AssessmentFormCallback {

	private final VelocityContainer mainVC;
	private final Link reopenLink;
	private EvaluationFormExecutionController executionCtrl;

	private final UserCourseEnvironment assessedUserCourseEnv;
	private final ModuleConfiguration config;
	private final CourseNode courseNode;

	private final AuditEnv auditEnv;
	private EvaluationFormSession session;
	private boolean assessmentDone;

	@Autowired
	private MSService msService;

	public MSEvaluationFormExecutionController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv, CourseNode msCourseNode) {
		super(ureq, wControl);
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.courseNode = msCourseNode;
		this.config = msCourseNode.getModuleConfiguration();
		
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		UserNodeAuditManager auditManager = assessedUserCourseEnv.getCourseEnvironment().getAuditManager();
		this.auditEnv = AuditEnv.of(auditManager , msCourseNode, assessedIdentity, getIdentity(), Role.coach);

		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(config);
		RepositoryEntry ores = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		String nodeIdent = msCourseNode.getIdent();
		session =  msService.getOrCreateSession(formEntry, ores, nodeIdent, assessedIdentity, auditEnv);
		
		AssessmentManager am = assessedUserCourseEnv.getCourseEnvironment().getAssessmentManager();
		AssessmentEntry aEntry = am.getAssessmentEntry(msCourseNode, assessedIdentity);
		assessmentDone = aEntry != null && aEntry.getAssessmentStatus() == AssessmentEntryStatus.done;
		
		mainVC = createVelocityContainer("evaluation_form_execution");
		reopenLink = LinkFactory.createButton("evaluation.execution.reopen", mainVC, this);
		updateUI(ureq);
		
		putInitialPanel(mainVC);
	}

	private void updateUI(UserRequest ureq) {
		refreshExecutionController(ureq);
		updateUIReopen();
	}

	private void refreshExecutionController(UserRequest ureq) {
		if (executionCtrl != null) {
			mainVC.remove(executionCtrl.getInitialComponent());
			removeAsListenerAndDispose(executionCtrl);
		}
		
		executionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), null, null, session, null, null, false, true);
		listenTo(executionCtrl);
		mainVC.put("execution", executionCtrl.getInitialComponent());
	}

	private void updateUIReopen() {
		boolean reopenVisible = !assessmentDone && isSessionClosed();
		reopenLink.setVisible(reopenVisible);
		mainVC.setDirty(true);
	}

	private boolean isSessionClosed() {
		return EvaluationFormSessionStatus.done.equals(session.getEvaluationFormSessionStatus());
	}

	@Override
	public void assessmentDone(UserRequest ureq) {
		assessmentDone = true;
		session = msService.closeSession(session, auditEnv);
		updateUI(ureq);
	}

	@Override
	public void assessmentReopen(UserRequest ureq) {
		assessmentDone = false;
		session = msService.reopenSession(session, auditEnv);
		updateUI(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == reopenLink) {
			session = msService.reopenSession(session, auditEnv);
			updateUI(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl && Event.DONE_EVENT.equals(event)) {
			doSetAssessmentScore();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}

	private void doSetAssessmentScore() {
		session = msService.getSession(session);
		if (courseNode instanceof MSCourseNode) {
			MSCourseNode msCourseNode = (MSCourseNode) courseNode;
			msCourseNode.updateScoreEvaluation(getIdentity(), assessedUserCourseEnv, Role.coach, session);
		}
		updateUIReopen();
	}

	@Override
	protected void doDispose() {
		//
	}

}
