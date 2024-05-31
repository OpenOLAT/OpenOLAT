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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MSResultDetailsController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private EvaluationFormExecutionController formCtrl;

	private EvaluationFormSession session;
	
	@Autowired
	private MSService msService;

	public MSResultDetailsController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv, CourseNode courseNode,
			EvaluationFormProvider evaluationFormProvider) {
		super(ureq, wControl);

		mainVC = createVelocityContainer("result_details");
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(config);
		RepositoryEntry ores = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		String nodeIdent = courseNode.getIdent();
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		UserNodeAuditManager auditManager = assessedUserCourseEnv.getCourseEnvironment().getAuditManager();
		AuditEnv auditEnv = AuditEnv.of(auditManager , courseNode, assessedIdentity, getIdentity(), Role.coach);
		session =  msService.getOrCreateSession(formEntry, ores, nodeIdent, evaluationFormProvider, assessedIdentity, auditEnv);
		
		formCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), session, true, false, false, null);
		listenTo(formCtrl);
		mainVC.put("evaluationForm", formCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
