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
package org.olat.course.nodes.gta.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ui.tool.AssessmentIdentityCourseNodeController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachedParticipantGradingController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private final AssessmentIdentityCourseNodeController assessmentForm;
	
	private Task assignedTask;
	private final GTACourseNode gtaNode;
	private final Identity assessedIdentity;
	private final UserCourseEnvironment coachCourseEnv;

	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public GTACoachedParticipantGradingController(UserRequest ureq, WindowControl wControl,
			GTACourseNode gtaNode, Task assignedTask, UserCourseEnvironment coachCourseEnv,
			Identity assessedIdentity) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.assignedTask = assignedTask;
		this.assessedIdentity = assessedIdentity;
		this.coachCourseEnv = coachCourseEnv;
		
		mainVC = createVelocityContainer("coach_grading");
		
		RepositoryEntry courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		assessmentForm = new AssessmentIdentityCourseNodeController(ureq, getWindowControl(), null, courseEntry, gtaNode, coachCourseEnv, assessedIdentity,
				false, false, false, false);
		listenTo(assessmentForm);
		
		mainVC.put("assessmentform", assessmentForm.getInitialComponent());

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentForm == source && event instanceof AssessmentFormEvent) {
			UserCourseEnvironment assessedUserCourseEnv = assessmentForm.getAssessedUserCourseEnvironment();
			if(AssessmentFormEvent.ASSESSMENT_REOPEN.equals(event.getCommand())) {
				if(assignedTask != null) {
					assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.grading, gtaNode, false, getIdentity(), Role.coach);
				}
			} else {
				doGraded(assessedUserCourseEnv);
			}
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private void doGraded(UserCourseEnvironment assessedUserCourseEnv) {
		AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(gtaNode, assessedUserCourseEnv);
		if(scoreEval.getAssessmentStatus() == AssessmentEntryStatus.done) {
			if(assignedTask == null) {
				RepositoryEntry courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				TaskList taskList = gtaManager.createIfNotExists(courseEntry, gtaNode);
				assignedTask = gtaManager.createTask(null, taskList, TaskProcess.graded, null, assessedIdentity, gtaNode);
			} else {
				assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.graded, gtaNode, false, getIdentity(), Role.coach);
			}
		}
	}
}
